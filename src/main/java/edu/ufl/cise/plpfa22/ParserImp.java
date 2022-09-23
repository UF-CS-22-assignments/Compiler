package edu.ufl.cise.plpfa22;

import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.VarDec;

public class ParserImp implements IParser {
    private final ILexer lexer;
    private IToken nextToken;

    public ParserImp(ILexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public ASTNode parse() throws PLPException {
        // init the nextToken in here instead of in the constructor, because we don't
        // want to throw the lexical exception before parse() is invoked.
        this.nextToken = this.lexer.next();

        return this.program();
    }

    /**
     * Get the next token if it has the expected kind, then move forward the lexer.
     * 
     * @param kind the expected kind.
     * @return
     * @throws PLPException raise an exception if the kind of the next token is not
     *                      right or if there's a lexical error
     */
    private IToken match(Kind kind) throws PLPException {
        if (this.isKind(this.nextToken, kind)) {
            return this.consume();
        } else {
            throw new SyntaxException("The next token is not the correct type",
                    this.nextToken.getSourceLocation().line(), this.nextToken.getSourceLocation().column());
        }
    }

    /**
     * return the next token and let the lexer moves forward by one token.
     * TODO: not sure if it's necessary to encapsulate this method.
     * 
     * @return
     * @throws PLPException raise an exception if there's a lexical error
     */
    private IToken consume() throws PLPException {
        IToken res = this.nextToken;
        this.nextToken = this.lexer.next();
        return res;
    }

    /**
     * check if a token is a certain kind.
     * 
     * @param token
     * @param kind  the expected kind.
     * @return return true if the token is that kind
     */
    private boolean isKind(IToken token, Kind kind) {
        return token.getKind() == kind;
    }

    private Program program() throws PLPException {
        IToken firstToken = this.nextToken;
        Block block = block();

        // A program must end with a dot, so just simply call match, syntax error will
        // be thrown in match if there isn't a dot token.
        this.match(Kind.DOT);
        return new Program(firstToken, block);

    }

    private Block block() throws PLPException {
        IToken firstToken = this.nextToken;
        List<ConstDec> constDecs = new ArrayList<>();
        List<VarDec> varDecs = new ArrayList<>();
        List<ProcDec> procDecs = new ArrayList<>();

        while (this.isKind(this.nextToken, Kind.KW_CONST)) {
            constDecs.addAll(this.constDecList());
        }

        while (this.isKind(this.nextToken, Kind.KW_VAR)) {
            varDecs.add(this.varDec());
        }

        while (this.isKind(this.nextToken, Kind.KW_PROCEDURE)) {
            procDecs.add(this.procDec());
        }

        Statement statement = this.statement();
        return new Block(firstToken, constDecs, varDecs, procDecs, statement);
    }

    /**
     * (CONST <ident> = <const_val> ( , <ident> = <const_val> )* ; )*
     * List<ConstDec>
     * 
     * @return a list of ConstDec
     * @throws PLPException
     */
    private List<ConstDec> constDecList() throws PLPException {
        IToken firstToken = this.nextToken;
        List<ConstDec> constDecs = new ArrayList<>();

        this.match(Kind.KW_CONST);
        IToken ident = this.match(Kind.IDENT);
        this.match(Kind.EQ);
        Expression constExpression = this.constVal(); // get the <const_val> non-terminal
        Object constVal = this.getConstValValue(constExpression); // get the value of that non-terminal
        constDecs.add(new ConstDec(firstToken, ident, constVal));

        while (this.isKind(this.nextToken, Kind.COMMA)) {
            firstToken = this.nextToken;
            this.match(Kind.COMMA);
            ident = this.match(Kind.IDENT);
            this.match(Kind.EQ);
            constExpression = this.constVal();
            constVal = this.getConstValValue(constExpression);
            constDecs.add(new ConstDec(firstToken, ident, constVal));
        }

        this.match(Kind.SEMI);

        return constDecs;
    }

    /**
     * get the const value of a const expression(the value of a <const_val>). For
     * example, passing an ExpressionNumLit should return an Integer type.
     * 
     * @param constExpression could be ExpressionBooleanLit, ExpressionNumLit,
     *                        ExpressStringLit
     * @return String or Boolean or Integer
     */
    private Object getConstValValue(Expression constExpression) throws PLPException {
        IToken litToken = constExpression.getFirstToken();
        switch (litToken.getKind()) {
            case NUM_LIT -> {
                return litToken.getIntValue();
            }
            case STRING_LIT -> {
                return litToken.getStringValue();
            }
            case BOOLEAN_LIT -> {
                return litToken.getBooleanValue();
            }
            default -> {
                throw new SyntaxException("can get value of the token type", litToken.getSourceLocation().line(),
                        litToken.getSourceLocation().column());
            }
        }
    }

    private VarDec varDec() throws PLPException {
        // TODO
        return new VarDec(this.nextToken, null);
    }

    private ProcDec procDec() throws PLPException {
        // TODO
        return new ProcDec(this.nextToken, null, null);
    }

    private Statement statement() throws PLPException {
        // TODO
        return new StatementEmpty(this.nextToken);
    }

    /**
     * <const_val> non-terminal, return an Expression type.
     * 
     * <const_val> := <num_lit> ExpressionNumLit
     * | <string_lit> ExpressionStringLit
     * | <boolean_lit> ExpressionBooleanLit
     * 
     * @return
     * @throws PLPException
     */
    private Expression constVal() throws PLPException {
        Expression constVal;
        IToken firsToken = this.nextToken;
        switch (this.nextToken.getKind()) {
            case NUM_LIT -> {
                this.match(Kind.NUM_LIT);
                constVal = new ExpressionNumLit(firsToken);
            }
            case STRING_LIT -> {
                this.match(Kind.STRING_LIT);
                constVal = new ExpressionStringLit(firsToken);
            }
            case BOOLEAN_LIT -> {
                this.match(Kind.BOOLEAN_LIT);
                constVal = new ExpressionBooleanLit(firsToken);
            }
            default -> {
                throw new SyntaxException("unsupported const type", firsToken.getSourceLocation().line(),
                        firsToken.getSourceLocation().column());
            }
        }
        return constVal;
    }

}
