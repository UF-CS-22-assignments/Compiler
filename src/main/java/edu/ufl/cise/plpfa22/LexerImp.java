package edu.ufl.cise.plpfa22;

import java.util.Map;
import static java.util.Map.entry;

import edu.ufl.cise.plpfa22.IToken.Kind;

public class LexerImp implements ILexer {
    private String input;

    // The line and column number of the next character to be read (at this
    // position).
    // The line number and column number starts from 1.
    private int lineNum = 1;
    private int colNum = 1;

    // the next character's position in input that will be sent to the DFA.
    private int pos = 0;
    // record the start index for each token
    private int startIndex = 0;

    // A map from reserved words to the type of the token. This is needed to check
    // for every identifier token.
    // TODO: check this map after recognizing an identifier token
    private Map<String, Kind> reservedWords = Map.ofEntries(
            entry("TRUE", Kind.BOOLEAN_LIT),
            entry("FALSE", Kind.BOOLEAN_LIT),
            entry("CONST", Kind.KW_CONST),
            entry("VAR", Kind.KW_VAR),
            entry("PROCEDURE", Kind.KW_PROCEDURE),
            entry("CALL", Kind.KW_CALL),
            entry("BEGIN", Kind.KW_BEGIN),
            entry("END", Kind.KW_END),
            entry("IF", Kind.KW_IF),
            entry("Then", Kind.KW_THEN),
            entry("WHILE", Kind.KW_WHILE),
            entry("DO", Kind.KW_DO));

    private enum State {
        START, // start state
        IN_NUM, // ,
        IDENT, // identifier
        DOT, // .
        COMMA, // ,
        SEMI, // ;
        QUOTE, // " (not a singel character token)
        LPAREN, // (
        RPAREN, // )
        PLUS, // +
        MINUS, // -
        TIMES, // *
        DIV, // /
        MOD, // %
        QUESTION, // ?
        BANG, // !
        ASSIGN1, // first character of :=
        ASSIGN2, // second character of :=
        EQ, // =
        NEQ, // #
        LT, // <
        LE, // <= (after LT)
        GT, // >
        GE, // >= (after GT)

    }

    private State currentState = State.START;

    /**
     * read the next character, change the state, lineNum, ColNum, and pos
     * accordingly.
     * 
     * @return the next token.
     * 
     * @exception LexicalException the next token to read is illegal.
     */
    private IToken getNextToken() throws LexicalException {
        char ch;

        // start index, line, and col number of the next token.
        this.startIndex = pos;
        int startLineNum = this.lineNum;
        int startColNum = this.colNum;
        while (true) {
            // get the next character and handle EOF, which is that we reached the end of
            // the input string.
            if (this.pos != this.input.length()) {
                ch = this.input.charAt(this.pos);
            } else {
                ch = 0; // EOF
            }

            switch (this.currentState) {
                case START -> {
                    switch (ch) {
                        case '.' -> {
                            this.currentState = State.DOT;
                        }
                        case ',' -> {
                            this.currentState = State.COMMA;
                        }
                        case ';' -> {
                            this.currentState = State.SEMI;
                        }
                        case '(' -> {
                            this.currentState = State.LPAREN;
                        }
                        case ')' -> {
                            this.currentState = State.RPAREN;
                        }
                        case '+' -> {
                            this.currentState = State.PLUS;
                        }
                        case '-' -> {
                            this.currentState = State.MINUS;
                        }
                        case '*' -> {
                            this.currentState = State.TIMES;
                        }
                        case '/' -> {
                            this.currentState = State.DIV;
                        }
                        case '%' -> {
                            this.currentState = State.MOD;
                        }
                        case '?' -> {
                            this.currentState = State.QUESTION;
                        }
                        case '!' -> {
                            this.currentState = State.BANG;
                        }
                        case ':' -> {
                            this.currentState = State.ASSIGN1;
                        }
                        case '=' -> {
                            this.currentState = State.EQ;
                        }
                        case '#' -> {
                            this.currentState = State.NEQ;
                        }
                        case '<' -> {
                            this.currentState = State.LT;
                        }
                        case '>' -> {
                            this.currentState = State.GT;
                        }
                        case ' ' -> {
                            // skip white spaces

                            // since white spaces is not part of the token and is omitted, the start index
                            // and col number need to be updated (line number doesn't change).
                            this.startIndex = pos + 1; // add 1 because pos and colNum will add 1 that each loop.
                            startColNum = this.colNum + 1;
                        }
                        case '\n' -> {
                            // TODO: Consider \r\n
                            // reset col to 0 and increase lineNum by 1
                            this.colNum = 0;
                            this.lineNum += 1;

                            // since whitespaces is not part of the token and is omitted, the start index,
                            // start line and col number need to be updated.
                            this.startIndex = pos + 1; // add 1 because pos and colNum will add 1 that each loop.
                            startColNum = this.colNum + 1;
                            startLineNum = this.lineNum;
                        }
                        case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
                                's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                                'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                                '_', '$' -> {
                            this.currentState = State.IDENT;
                        }
                        case 0 -> {
                            // TODO: input is wrong(the third argument).
                            return new TokenImp(Kind.EOF, this.lineNum, this.colNum, input);
                        }
                        // start of num_lit
                        case '0' -> {
                            // TODO: if not dot, return zero
                            pos += 1;

                            return new TokenImp(Kind.NUM_LIT, this.lineNum, this.colNum, "0");
                        }
                        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            // keep track of the startIndex and make the substring later
                            this.currentState = State.IN_NUM;
                        }
                        default -> {
                            // Illegal character
                            throw new LexicalException("Illegal character at start state", this.lineNum, this.colNum);
                        }
                    }
                }
                case IDENT -> {
                    switch (ch) {
                        case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
                                's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                                'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                                '_', '$', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            // state not changed
                        }
                        default -> {
                            // Either it's an identifier, or it's a reserved word. The curState will always
                            // switch to START.
                            this.currentState = State.START;

                            // the text of the token, used to check if it's a reserved word and construct
                            // the Token instance.
                            String curTokenText = this.input.substring(startIndex, this.pos);

                            // check if it's a reserved word or an identifier
                            if (this.reservedWords.containsKey(curTokenText)) {
                                // it's a reserved word.
                                return new TokenImp(this.reservedWords.get(curTokenText), startLineNum, startColNum,
                                        curTokenText);
                            } else {
                                // it's an identifier.
                                return new TokenImp(Kind.IDENT, startLineNum, startColNum, curTokenText);
                            }

                        }
                    }
                }
                case DOT -> {
                    this.currentState = State.START; // TODO: Maybe this should be refactored to be executed every time
                                                     // before this method returns.
                    return new TokenImp(Kind.DOT, startLineNum, startColNum, ".");
                }
                case COMMA -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.COMMA, startLineNum, startColNum, ",");
                }
                case SEMI -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.SEMI, startLineNum, startColNum, ";");
                }
                case LPAREN -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.LPAREN, startLineNum, startColNum, "(");
                }
                case RPAREN -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.RPAREN, startLineNum, startColNum, ")");
                }

                case PLUS -> {
                    // return token of PLUS no matter what the next character is.
                    this.currentState = State.START;

                    // the start of the next token.
                    return new TokenImp(Kind.PLUS, startLineNum, startColNum, "+");

                }
                case MINUS -> {
                    // return token of MINUS no matter what the next character is.
                    this.currentState = State.START;

                    // the start of the next token.
                    return new TokenImp(Kind.MINUS, startLineNum, startColNum, "-");
                }
                case TIMES -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.TIMES, startLineNum, startColNum, "*");
                }
                case DIV -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.DIV, startLineNum, startColNum, "/");
                }
                case MOD -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.MOD, startLineNum, startColNum, "%");
                }
                case QUESTION -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.QUESTION, startLineNum, startColNum, "?");
                }
                case BANG -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.BANG, startLineNum, startColNum, "!");
                }
                case ASSIGN1 -> {
                    switch (ch) {
                        case '=' -> {
                            this.currentState = State.ASSIGN2;
                        }
                        default -> {
                            // Since ':' is not a valid token, any character follow ':' except '=' would
                            // cause a lexical error.
                            // ASSIGN1 is not an accept state.
                            throw new LexicalException("':' can't be followed by any character other than '='",
                                    startLineNum, startColNum);
                        }
                    }
                }
                case ASSIGN2 -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.ASSIGN, startLineNum, startColNum, ":=");
                }
                case EQ -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.EQ, startLineNum, startColNum, "=");
                }
                case NEQ -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.NEQ, startLineNum, startColNum, "#");
                }
                case LT -> {
                    switch (ch) {
                        case '=' -> {
                            this.currentState = State.LE;
                        }
                        default -> {
                            this.currentState = State.START;
                            return new TokenImp(Kind.LT, startLineNum, startColNum, "<");
                        }
                    }
                }
                case LE -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.LE, startLineNum, startColNum, "<=");
                }
                case GT -> {
                    switch (ch) {
                        case '=' -> {
                            this.currentState = State.GE;
                        }
                        default -> {
                            this.currentState = State.START;
                            return new TokenImp(Kind.GT, startLineNum, startColNum, ">");
                        }
                    }
                }
                case GE -> {
                    this.currentState = State.START;
                    return new TokenImp(Kind.GE, startLineNum, startColNum, ">=");
                }

                case IN_NUM -> {
                    // keep reading the input till next char is not int
                    switch (ch) {
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            // no need to add anything in this bracket so far
                        }
                        default -> {
                            // return the NUM_LIT token
                            this.currentState = State.START;
                            return new TokenImp(Kind.NUM_LIT, startLineNum, startColNum,
                                    input.substring(startIndex, this.pos));
                        }
                    }
                }
                default -> {
                    // TODO: this is actually not a LexicalException, this is when the DFA went to
                    // an unknown state, which is supposted to be an error of our code.
                    throw new LexicalException();
                }

            }
            this.pos += 1;
            this.colNum += 1;
        }
    }

    /**
     * Constructor of the LexerImp
     * 
     * @param input the string to be lexical-analysed
     */
    public LexerImp(String input) {
        this.input = input;
    }

    @Override
    public IToken next() throws LexicalException {
        return this.getNextToken();
    }

    @Override
    public IToken peek() throws LexicalException {
        // store the current states and position information.
        int lastPos = this.pos;
        int lastLineNum = this.lineNum;
        int lastColNum = this.colNum;
        State lastCurrentState = this.currentState;

        // get the next token. currentState and position information changed.
        IToken res = this.getNextToken();

        // restore the last state and position information.
        this.pos = lastPos;
        this.lineNum = lastLineNum;
        this.colNum = lastColNum;
        this.currentState = lastCurrentState;
        return res;
    }
}
