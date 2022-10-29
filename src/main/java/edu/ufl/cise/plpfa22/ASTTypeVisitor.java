package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.ast.Statement;

public class ASTTypeVisitor implements ASTVisitor {

    // if the AST changed in this pass (some node get it's type)
    private boolean changed = false;

    /**
     * set the type of an expression and set this.change to true. Use this method to
     * avoid forget setting this.change. If it's already typed, check if the type is
     * correct.
     * 
     * @param expr expr.type can be null or not null
     * @param type type must be not null
     * @throws TypeCheckException
     */
    private void setExprType(Expression expr, Type type) throws TypeCheckException {
        if (type == null) {
            // bug
            assert false;
        }
        if (expr.getType() != null && expr.getType() != type) {
            throw new TypeCheckException("type conflict", expr.firstToken.getSourceLocation());
        } else if (expr.getType() == null) {
            this.changed = true;
            expr.setType(type);
        }
    }

    /**
     * set the type of a declaration and set this.change to true. Use this method to
     * avoid forget setting this.change.
     * throw exception if conflict
     * 
     * @param dec
     * @param type
     * @throws TypeCheckException
     */
    private void setDecType(Declaration dec, Type type) throws TypeCheckException {
        if (dec.getType() != null && dec.getType() != type) {
            throw new TypeCheckException("type conflict", dec.firstToken.getSourceLocation());
        } else if (dec.getType() == null) {
            this.changed = true;
            dec.setType(type);
        }
    }

    /**
     * visit the node with arg, return the first untyped node.
     * 
     * @param node    the node to visit
     * @param arg     the arg to the node
     * @param untyped current untyped node
     * @return
     * @throws PLPException
     */
    private ASTNode visitSetUntyped(ASTNode node, Object arg, ASTNode untyped) throws PLPException {
        ASTNode res = (ASTNode) node.visit(this, arg);
        if (untyped == null) {
            return res;
        } else {
            return untyped;
        }
    }

    /**
     * get the declaration type from an ident.
     * 
     * @param ident
     * @return type of the declaration
     * @throws TypeCheckException thrown if the type of the ident is not NUM_LIT,
     *                            STRING_LIT or BOOLEAN_LIT
     */
    private Type getDecTypeByLit(IToken ident) throws TypeCheckException {
        switch (ident.getKind()) {
            case NUM_LIT -> {
                return Type.NUMBER;
            }
            case STRING_LIT -> {
                return Type.STRING;
            }
            case BOOLEAN_LIT -> {
                return Type.BOOLEAN;
            }
            default -> {
                throw new TypeCheckException();
            }
        }
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        ASTNode untypedNode = null;
        for (ConstDec constDec : block.constDecs) {
            constDec.visit(this, null);
        }

        for (ProcDec procDec : block.procedureDecs) {
            untypedNode = this.visitSetUntyped(procDec, null, untypedNode);
        }
        untypedNode = this.visitSetUntyped(block.statement, null, untypedNode);
        return untypedNode;
    }

    /* arg: if it's the typing visit. */
    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        ASTNode firstUntypedNode;
        // visit the children if anything is typed on the last visit.
        do {
            this.changed = false;
            firstUntypedNode = (ASTNode) program.block.visit(this, null);
        } while (this.changed);

        if (firstUntypedNode != null) {
            throw new TypeCheckException("insufficient type",
                    firstUntypedNode.firstToken.getSourceLocation());
        }

        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        Ident ident = statementAssign.ident;
        Expression expression = statementAssign.expression;
        if (ident.getDec().getType() == null && expression.getType() != null) {
            // set the ident's type by expression type
            this.setDecType(ident.getDec(), expression.getType());
            return null;
        } else if (ident.getDec().getType() != null && expression.getType() == null) {
            // visit the expression knowing that it has this type
            return expression.visit(this, ident.getDec().getType());
        } else if (ident.getDec().getType() != null && expression.getType() != null) {
            // inconsistant type.
            if (ident.getDec().getType() != expression.getType()) {
                throw new TypeCheckException("variable type error", ident.getSourceLocation());
            } else {
                // is it possible that an expression has type but it still needs to be visited?
                return expression.visit(this, expression.getType());
            }
        } else {
            // both are null, visit the expression and deal with this in the next pass.
            expression.visit(this, null);
            return ident;
        }
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        if (statementCall.ident.getDec().getType() != null
                && statementCall.ident.getDec().getType() != Type.PROCEDURE) {
            throw new TypeCheckException("can't call a statement that is not a procedure",
                    statementCall.ident.firstToken.getSourceLocation());
        }
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        Ident ident = statementInput.ident;
        Type identType = ident.getDec().getType();
        if (identType == null) {
            return ident;
        }
        if (identType != Type.NUMBER && identType != Type.BOOLEAN && identType != Type.STRING) {
            throw new TypeCheckException("can only input to number, boolean and string variable",
                    ident.getFirstToken().getSourceLocation());
        }
        return null;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        Expression expression = statementOutput.expression;
        Type exprType = expression.getType();
        if (exprType == null) {
            return expression.visit(this, null);
        } else {
            if (exprType != Type.BOOLEAN && exprType != Type.NUMBER && exprType != Type.STRING) {
                throw new TypeCheckException("output wrong type", expression.firstToken.getSourceLocation());
            } else {
                return expression.visit(this, null);
            }
        }
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {

        for (Statement statement : statementBlock.statements) {
            statement.visit(this, arg);
        }

        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        Expression expression = statementIf.expression;
        Type exprType = expression.getType();
        ASTNode untyped = null;
        if (exprType != null && exprType != Type.BOOLEAN) {
            throw new TypeCheckException("expression in statementIf shoule be type boolean",
                    statementIf.getFirstToken().getSourceLocation());
        } else {
            untyped = this.visitSetUntyped(expression, Type.BOOLEAN, untyped);
            untyped = this.visitSetUntyped(statementIf.statement, null, untyped);
            return untyped;
        }

    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        Expression expression = statementWhile.expression;
        Type exprType = expression.getType();
        ASTNode untyped = null;
        if (exprType != null && exprType != Type.BOOLEAN) {
            throw new TypeCheckException("expression in statementIf shoule be type boolean",
                    statementWhile.getFirstToken().getSourceLocation());
        } else {
            untyped = this.visitSetUntyped(expression, Type.BOOLEAN, untyped);
            untyped = this.visitSetUntyped(statementWhile.statement, null, untyped);
            return untyped;
        }
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        // check arg type
        if ((Type) arg != null) {
            this.setExprType(expressionBinary, (Type) arg);
        }

        // make sure that if there's only one expression that doesn't have type, set it
        // to expression 0
        // that is, there will only be null x null, not-null x null, not-null x not-null

        Expression expression0 = expressionBinary.e0.getType() == null ? expressionBinary.e1 : expressionBinary.e0;
        Expression expression1 = expressionBinary.e0.getType() == null ? expressionBinary.e0 : expressionBinary.e1;
        ASTNode untyped = null;

        switch (expressionBinary.op.getKind()) {
            case PLUS, MINUS, DIV, MOD, TIMES -> {
                // a x a -> a
                List<Type> allowedKindLeft = new ArrayList<>();
                switch (expressionBinary.op.getKind()) {
                    case PLUS -> {
                        allowedKindLeft.add(Type.NUMBER);
                        allowedKindLeft.add(Type.STRING);
                        allowedKindLeft.add(Type.BOOLEAN);
                    }
                    case MINUS, DIV, MOD -> {
                        allowedKindLeft.add(Type.NUMBER);
                    }
                    case TIMES -> {
                        allowedKindLeft.add(Type.NUMBER);
                        allowedKindLeft.add(Type.BOOLEAN);
                    }
                    default -> {
                        assert false;
                    }
                }
                if (expressionBinary.getType() != null) {
                    // _ x _ -> not-null
                    if (!allowedKindLeft.contains(expressionBinary.getType())) {
                        throw new TypeCheckException("wrong type for expressionBinary",
                                expressionBinary.getFirstToken().getSourceLocation());
                    }
                    untyped = this.visitSetUntyped(expression0, expressionBinary.getType(), untyped);
                    untyped = this.visitSetUntyped(expression1, expressionBinary.getType(), untyped);
                } else {
                    // _ x _ -> null
                    if (expression0.getType() != null) {
                        // not-null x _ -> null
                        if (!allowedKindLeft.contains(expression0.getType())) {
                            throw new TypeCheckException("wrong type for expressionBinary",
                                    expressionBinary.getFirstToken().getSourceLocation());
                        }
                        expressionBinary.setType(expression0.getType());
                        untyped = this.visitSetUntyped(expression0, null, untyped);
                        untyped = this.visitSetUntyped(expression1, expression0.getType(), untyped);
                    } else {
                        // null x null -> null
                        untyped = this.visitSetUntyped(expression0, null, untyped);
                        untyped = this.visitSetUntyped(expression1, null, untyped);
                    }
                }

            }
            case EQ, NEQ, LT, LE, GT, GE -> {
                // a x a -> BOOLEAN
                // a \in {NUMBER, BOOLEAN, STRING}
                if (expressionBinary.getType() != null && expressionBinary.getType() != Type.BOOLEAN) {
                    throw new TypeCheckException("This expressionBinary should have BOOLEAN type",
                            expressionBinary.getFirstToken().getSourceLocation());
                }
                if (expression0.getType() == null) {
                    // null x null -> BOOLEAN
                    untyped = this.visitSetUntyped(expression0, null, untyped);
                    untyped = this.visitSetUntyped(expression1, null, untyped);
                } else {
                    // not-null x _ -> BOOLEAN
                    untyped = this.visitSetUntyped(expression0, null, untyped);
                    untyped = this.visitSetUntyped(expression1, expression0.getType(), untyped);
                }

            }
            default -> {
                assert false;
            }
        }

        return untyped;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {

        if ((Type) arg != null) {
            this.setExprType(expressionIdent, (Type) arg);
        }
        if (expressionIdent.getDec().getType() != null) {
            this.setExprType(expressionIdent, expressionIdent.getDec().getType());
        } else {
            return expressionIdent;
        }
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        if ((Type) arg != null) {
            this.setExprType(expressionNumLit, (Type) arg);
        }
        this.setExprType(expressionNumLit, Type.NUMBER);
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        if ((Type) arg != null) {
            this.setExprType(expressionStringLit, (Type) arg);
        }
        this.setExprType(expressionStringLit, Type.STRING);
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        if ((Type) arg != null) {
            this.setExprType(expressionBooleanLit, (Type) arg);
        }
        this.setExprType(expressionBooleanLit, Type.BOOLEAN);
        return null;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        if (procDec.getType() == null) {
            this.setDecType(procDec, Type.PROCEDURE);
        }
        return procDec.block.visit(this, null);

    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        if (constDec.getType() == null) {
            if (constDec.val instanceof Integer) {
                this.setDecType(constDec, Type.NUMBER);
            } else if (constDec.val instanceof String) {
                this.setDecType(constDec, Type.STRING);
            } else if (constDec.val instanceof Boolean) {
                this.setDecType(constDec, Type.BOOLEAN);
            } else {
                throw new TypeCheckException("wrong type for const Declaration",
                        constDec.firstToken.getSourceLocation());
            }
        }
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

}
