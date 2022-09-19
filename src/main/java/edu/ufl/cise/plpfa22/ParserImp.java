package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.VarDec;

public class ParserImp implements IParser {
    ILexer lexer;
    IToken nextToken;

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
        if (this.isKind(this.nextToken, Kind.DOT)) {
            this.match(Kind.DOT);
        } else {
            throw new SyntaxException("A program must end with a '.'");
        }
        return new Program(firstToken, block);

    }

    private Block block() {
        // TODO
        IToken firstToken = this.nextToken;
        List<ConstDec> constDecs = new ArrayList<>();
        List<VarDec> varDecs = new ArrayList<>();
        List<ProcDec> procDecs = new ArrayList<>();
        Statement statement = new StatementEmpty(firstToken);
        return new Block(firstToken, constDecs, varDecs, procDecs, statement);
    }

}
