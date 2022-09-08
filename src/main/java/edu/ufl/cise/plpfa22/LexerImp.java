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
        PLUS, // +
        MINUS, // -
        IN_NUM, // ,
        IDENT // identifier
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
                        case '+' -> {
                            this.currentState = State.PLUS;
                        }
                        case '-' -> {
                            this.currentState = State.MINUS;
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
                        // TODO: modify the float later!!
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
                case PLUS -> {
                    // return token of PLUS no matter what the next character is.
                    this.currentState = State.START;

                    // TODO: get the correct lineNum and colNum. lineNum and colNum is currently at
                    // the start of the next token.
                    return new TokenImp(Kind.PLUS, this.lineNum, this.colNum - 1, input);

                }
                case MINUS -> {
                    // return token of MINUS no matter what the next character is.
                    this.currentState = State.START;

                    // TODO: get the correct lineNum and colNum. lineNum and colNum is currently at
                    // the start of the next token.
                    return new TokenImp(Kind.MINUS, this.lineNum, this.colNum - 1, input);
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
