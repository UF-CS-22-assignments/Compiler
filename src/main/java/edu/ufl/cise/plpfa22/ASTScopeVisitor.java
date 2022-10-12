package edu.ufl.cise.plpfa22;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
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

public class ASTScopeVisitor implements ASTVisitor {
    // the nesting level for the current nesting scope.
    // since the nesting Level for the first scope is 0 and nesting level would add
    // one when entering each scope, the default value for nesting level is -1.
    private int nestingLevel = -1;

    // Notice that this is not the current scope id, which should be found at the
    // top of the scopeStack
    private int nextScopeID = 0;

    private class attribute {
        Declaration dec;

        public attribute(Declaration dec) {
            this.dec = dec;
        }
    }

    // ident name string as key, list of ident with that same name as value.
    // TODO: what type should be stored in the List?
    private HashMap<String, HashMap<Integer, attribute>> symbolTable = new HashMap<>();

    // store the scope nesting structure, by storing the scope IDs.
    // the top of the stack represents the ID of the current scope
    // replace stack by deque for better performance.
    private Deque<Integer> scopeStack = new ArrayDeque<>();

    private int getCurrentScopeId() {
        return this.scopeStack.peek();
    }

    /**
     * insert the current ident at the current scope and nesting level
     * 
     * @param ident the new ident
     * @param dec   the declaration in which the ident is contained.
     * @throws PLPException
     */
    private void insertIdent(IToken ident, Declaration dec) throws PLPException {
        String name = new String(ident.getText());
        if (this.symbolTable.containsKey(name)) {
            // there's already an identifier in the symbol table with the same name, put if
            // it has a different scope id, otherwise, throw an exception
            HashMap<Integer, attribute> identMap = this.symbolTable.get(name);
            if (identMap.containsKey(this.getCurrentScopeId())) {
                throw new ScopeException(
                        "re-declaration of identifier " + new String(ident.getText()),
                        ident.getSourceLocation().line(),
                        ident.getSourceLocation().column());
            } else {
                identMap.put(this.getCurrentScopeId(), new attribute(dec));
            }
        } else {
            HashMap<Integer, attribute> identMap = new HashMap<>();
            identMap.put(this.getCurrentScopeId(), new attribute(dec));
            this.symbolTable.put(name, identMap);
        }

    }

    /**
     * entering a new scope
     * 1. get the new scope ID for this scope
     * 2. push the scope ID to the scopeStack (Thus the top element of the stack is
     * the ID for the current scope)
     * 3. increment nesting level
     */
    private void enterScope() {
        int scopeID = this.getNextScopeID();
        this.scopeStack.push(scopeID);
        this.nestingLevel++;
    }

    /**
     * closing the current scope
     * 1. pop the scope stack
     * 2 decrement the nesting level.
     */
    private void closeScope() {
        this.scopeStack.pop();
        this.nestingLevel--;
    }

    /**
     * return the next scope ID and increment it after returning it.
     * 
     * @return the next scope ID.
     */
    private int getNextScopeID() {
        return this.nextScopeID++;
    }

    /**
     * arg is a boolean, representing 'is building symbol table(first pass)'
     */
    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        boolean isFirst = (boolean) arg;
        // start of the scope, call enterScope()
        this.enterScope();

        // call every visit() of the sub-nodes in the AST
        if (isFirst) {
            // const and var declaration will only be called at the first pass
            for (ConstDec constDec : block.constDecs) {
                constDec.visit(this, arg);
            }

            for (VarDec varDec : block.varDecs) {
                varDec.visit(this, arg);
            }

        }

        for (ProcDec procDec : block.procedureDecs) {
            procDec.visit(this, arg);
        }

        block.statement.visit(this, arg);

        // end of the scope, call closeScope
        this.closeScope();
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        // 1st pass, build the symbol table
        program.block.visit(this, true);

        // 2nd pass, decorate the AST.
        program.block.visit(this, false);
        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        if (!(boolean) arg) {
            // for the second pass, do nothing.
            return null;
        }
        this.insertIdent(varDec.ident, varDec);
        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        if (!(boolean) arg) {
            // for the second pass, do nothing.
            return null;
        }
        this.insertIdent(constDec.ident, constDec);
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        // maybe: boolean[] haha = (boolean[]) arg;
        // TODO Auto-generated method stub
        return null;
    }

}