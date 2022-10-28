package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.SourceLocation;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
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

public class ASTTypeVisitor implements ASTVisitor {

    // if the AST changed in this pass (some node get it's type)
    private boolean changed = false;

    /**
     * get the declaration type from an ident.
     * 
     * @param ident
     * @return type of the declaration
     * @throws TypeCheckException thrown if the type of the ident is not NUM_LIT,
     *                            STRING_LIT, IDENT or BOOLEAN_LIT
     */
    private Type getDecTypeByIdent(IToken ident) throws TypeCheckException {
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
            case IDENT -> {
                return Type.PROCEDURE;
            }
            default -> {
                throw new TypeCheckException();
            }
        }
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        for (ConstDec constDec : block.constDecs) {
            if (constDec.getType() == null) {
                this.changed = true;
                try {
                    Type type = this.getDecTypeByIdent(constDec.ident);
                    if (type == Type.PROCEDURE) {
                        // type error: declare procedure in const declaration.
                        throw new TypeCheckException();
                    } else {
                        constDec.setType(type);
                    }
                } catch (TypeCheckException e) {
                    throw new TypeCheckException("wrong type for const declaration",
                            constDec.ident.getSourceLocation());
                }
            }
        }

        for (ProcDec procDec : block.procedureDecs) {
            if (procDec.getType() == null) {
                this.changed = true;
                procDec.setType(Type.PROCEDURE);
            }
            procDec.block.visit(this, null);
        }
        block.statement.visit(this, null);
        return null;
    }

    /* arg: if it's the typing visit. */
    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        // visit the children if anything is typed on the last visit.
        do {
            this.changed = false;
            program.block.visit(this, null);
        } while (this.changed);

        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        Ident ident = statementAssign.ident;
        Expression expression = statementAssign.expression;
        if (ident.getDec().getType() == null && expression.getType() != null) {
            ident.getDec().setType(expression.getType());
        } else if (ident.getDec().getType() != null && expression.getType() == null) {
            expression.visit(this, ident.getDec().getType());
        } else if (ident.getDec().getType() != null && expression.getType() != null
                && ident.getDec().getType() != expression.getType()) {
            throw new TypeCheckException("variable type error", ident.getSourceLocation());
        }
        // else, the ident's type can't be determined, ignore it.
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        throw new UnsupportedException();
    }

}
