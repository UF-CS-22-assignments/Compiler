package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;

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

        Program program = this.program();
        this.match(Kind.EOF);
        return program;
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
            varDecs.addAll(this.varDecList());
        }

        while (this.isKind(this.nextToken, Kind.KW_PROCEDURE)) {
            procDecs.add(this.procDec());
        }

        Statement statement = this.statement();
        return new Block(firstToken, constDecs, varDecs, procDecs, statement);
    }

    /**
     * CONST <ident> = <const_val> ( , <ident> = <const_val> )* ;
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
     * VAR <ident> ( , <ident> )* ) ;
     */
    private List<VarDec> varDecList() throws PLPException {
        IToken firstToken = this.nextToken;
        List<VarDec> varDecs = new ArrayList<>();

        this.match(Kind.KW_VAR);
        IToken ident = this.match(Kind.IDENT);
        varDecs.add(new VarDec(firstToken, ident));

        while (this.isKind(this.nextToken, Kind.COMMA)) {
            firstToken = this.nextToken;
            this.match(Kind.COMMA);
            ident = this.match(Kind.IDENT);
            varDecs.add(new VarDec(firstToken, ident));
        }
        this.match(Kind.SEMI);

        return varDecs;
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

    /**
     * PROCEDURE <ident> ; <block> ;
     * get a procedure.
     * 
     * @return
     * @throws PLPException
     */
    private ProcDec procDec() throws PLPException {
        IToken firstToken = this.nextToken;
        this.match(Kind.KW_PROCEDURE);
        IToken ident = this.match(Kind.IDENT);
        this.match(Kind.SEMI);
        Block block = this.block();
        this.match(Kind.SEMI);
        return new ProcDec(firstToken, ident, block);
    }

    /**
     * <ident> := <expression> StatementAssign(Ident,Expression)
     * | CALL <ident> StatementCall(Ident)
     * | ? <ident> StatementInput(Ident)
     * | ! <expression> StatementOutput(Expression)
     * | BEGIN <statement> ( ; <statement> )* END
     * StatementBlock(List<Statement>)
     * | IF <expression> THEN <statement>
     * StatementIf(Expression, Statement)
     * | WHILE <expression> DO <statement>
     * StatementWhile(Expression, Statement)
     * | ε StatementEmpty
     * 
     * @return
     * @throws PLPException
     */
    private Statement statement() throws PLPException {
        IToken firstToken = this.nextToken;
        Statement statement;
        switch (this.nextToken.getKind()) {
            case IDENT -> {
                statement = this.statementAssign();
            }
            case KW_CALL -> {
                statement = this.statementCall();
            }
            case QUESTION -> {
                statement = this.statementInput();
            }
            case BANG -> {
                statement = this.statementOutput();
            }
            case KW_BEGIN -> {
                statement = this.statementBlock();
            }
            case KW_IF -> {
                statement = this.statementIf();
            }
            case KW_WHILE -> {
                statement = this.statementWhile();
            }
            case DOT, SEMI, KW_END -> {
                // should be FOLLOW(Statement) = FOLLOW(Block) union {END} since Statement only
                // occurs in <block> and block only occurs in <block>(precedure, followed by
                // semi) and <program>(followed by dot).
                // Then FOLLOW(block) = {DOT, SEMI}
                statement = new StatementEmpty(firstToken);
            }
            default -> {
                throw new SyntaxException("error parsing Statement, illegal token",
                        this.nextToken.getSourceLocation().line(), this.nextToken.getSourceLocation().column());
            }
        }
        return statement;
    }

    /**
     * BEGIN <statement> ( ; <statement> )* END
     * StatementBlock(List<Statement>)
     * 
     * @return
     * @throws PLPException
     */
    private StatementBlock statementBlock() throws PLPException {
        IToken firstToken = this.nextToken;
        List<Statement> statements = new ArrayList<>();
        this.match(Kind.KW_BEGIN);
        Statement statement = this.statement();
        statements.add(statement);
        while (this.nextToken.getKind() == Kind.SEMI) {
            this.match(Kind.SEMI);
            statement = this.statement();
            statements.add(statement);
        }
        this.match(Kind.KW_END);

        return new StatementBlock(firstToken, statements);
    }

    private StatementWhile statementWhile() throws PLPException {
        IToken firstToken = this.nextToken;
        this.match(Kind.KW_WHILE);
        Expression expression = this.expression();
        this.match(Kind.KW_DO);
        Statement statement = this.statement();
        return new StatementWhile(firstToken, expression, statement);
    }

    /**
     * IF <expression> THEN <statement>
     * StatementIf(Expression, Statement)
     * 
     * @return
     * @throws PLPException
     */
    private StatementIf statementIf() throws PLPException {
        IToken firstToken = this.nextToken;
        this.match(Kind.KW_IF);
        Expression expression = this.expression();
        this.match(Kind.KW_THEN);
        Statement statement = this.statement();
        return new StatementIf(firstToken, expression, statement);
    }

    /**
     * ! <expression> StatementOutput(Expression)
     * 
     * @return
     * @throws PLPException
     */
    private StatementOutput statementOutput() throws PLPException {
        IToken firstToken = this.nextToken;
        this.match(Kind.BANG);
        Expression expression = this.expression();
        return new StatementOutput(firstToken, expression);
    }

    /**
     * ? <ident> StatementInput(Ident)
     * 
     * @return
     * @throws PLPException
     */
    private StatementInput statementInput() throws PLPException {
        IToken firstToken = this.nextToken;
        this.match(Kind.QUESTION);
        IToken ident = this.match(Kind.IDENT);
        return new StatementInput(firstToken, new Ident(ident));
    }

    /**
     * CALL <ident> StatementCall(Ident)
     * 
     * @return
     * @throws PLPException
     */
    private StatementCall statementCall() throws PLPException {
        IToken firstToken = this.nextToken;
        this.match(Kind.KW_CALL);
        IToken ident = this.match(Kind.IDENT);
        return new StatementCall(firstToken, new Ident(ident));
    }

    /**
     * <ident> := <expression> StatementAssign(Ident,Expression)
     * 
     * @return
     * @throws PLPException
     */
    private StatementAssign statementAssign() throws PLPException {
        IToken firstToken = this.nextToken;
        IToken ident = this.match(Kind.IDENT);
        this.match(Kind.ASSIGN);
        Expression expression = this.expression();
        // In the StatementAssign, the ident is a Ident type(ASTNode), not an IToken
        // type. It's sepcified in Slack by the teacher.
        return new StatementAssign(firstToken, new Ident(ident), expression);
    }

    private Expression expression() throws PLPException {
        IToken firstToken = this.nextToken;
        Expression leftExpression = this.additiveExpression();
        Kind nextTokenKind = this.nextToken.getKind();
        while (nextTokenKind == Kind.LT || nextTokenKind == Kind.GT || nextTokenKind == Kind.EQ
                || nextTokenKind == Kind.NEQ || nextTokenKind == Kind.LE || nextTokenKind == Kind.GE) {
            // consume whatever token if it has the above type
            IToken op = this.consume();
            leftExpression = new ExpressionBinary(firstToken, leftExpression, op, this.additiveExpression());
            nextTokenKind = this.nextToken.getKind();
        }

        return leftExpression;
    }

    private Expression additiveExpression() throws PLPException {
        IToken firstToken = this.nextToken;
        Expression leftExpression = this.multiplicativeExpression();
        Kind nextTokenKind = this.nextToken.getKind();
        while (nextTokenKind == Kind.PLUS || nextTokenKind == Kind.MINUS) {
            IToken op = this.consume();
            leftExpression = new ExpressionBinary(firstToken, leftExpression, op, this.multiplicativeExpression());
            nextTokenKind = this.nextToken.getKind();
        }
        return leftExpression;
    }

    private Expression multiplicativeExpression() throws PLPException {
        IToken firstToken = this.nextToken;
        Expression leftExpression = this.primaryExpression();
        Kind nextTokenKind = this.nextToken.getKind();
        while (nextTokenKind == Kind.TIMES || nextTokenKind == Kind.DIV || nextTokenKind == Kind.MOD) {
            IToken op = this.consume();
            leftExpression = new ExpressionBinary(firstToken, leftExpression, op, this.primaryExpression());
            nextTokenKind = this.nextToken.getKind();
        }
        return leftExpression;
    }

    private Expression primaryExpression() throws PLPException {
        IToken firstToken = this.nextToken;
        Expression expression;
        switch (this.nextToken.getKind()) {
            case IDENT -> {
                this.consume();
                expression = new ExpressionIdent(firstToken);
            }
            case LPAREN -> {
                this.match(Kind.LPAREN);
                expression = this.expression();
                this.match((Kind.RPAREN));
            }
            default -> {
                expression = this.constVal();
            }
        }
        return expression;
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
        IToken firstToken = this.nextToken;
        switch (this.nextToken.getKind()) {
            case NUM_LIT -> {
                this.match(Kind.NUM_LIT);
                constVal = new ExpressionNumLit(firstToken);
            }
            case STRING_LIT -> {
                this.match(Kind.STRING_LIT);
                constVal = new ExpressionStringLit(firstToken);
            }
            case BOOLEAN_LIT -> {
                this.match(Kind.BOOLEAN_LIT);
                constVal = new ExpressionBooleanLit(firstToken);
            }
            default -> {
                throw new SyntaxException("unsupported const type", firstToken.getSourceLocation().line(),
                        firstToken.getSourceLocation().column());
            }
        }

        return constVal;
    }

}
