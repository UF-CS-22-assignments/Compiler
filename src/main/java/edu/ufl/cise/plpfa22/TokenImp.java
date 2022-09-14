package edu.ufl.cise.plpfa22;

public class TokenImp implements IToken {
    private Kind kind;

    private SourceLocation location;

    private String text;

    private String stringValue;

    /**
     * construct a token
     * 
     * @param kind The type of the token. Kind is an enum defind in IToken
     * @param line the line number of the first character in the input character
     *             stream.
     * @param col  the col number of the first character in the in put character
     *             stream
     * @param text the string that contains the raw text of the token in the input
     *             string.
     */
    private int num;

    public TokenImp(Kind kind, int line, int col, String text) throws LexicalException {
        this.kind = kind;
        this.location = new SourceLocation(line, col);
        this.text = text;
        if (kind == Kind.NUM_LIT) {
            try {
                num = Integer.parseInt(text);
            } catch (NumberFormatException nfe) {
                throw new LexicalException(nfe);
            }
        } else if (kind == Kind.STRING_LIT) {
            this.stringValue = this.computeStringValue(text);
        }
    }

    /**
     * Returns the Kind of this IToken
     * 
     * @return
     */
    @Override
    public Kind getKind() {
        return this.kind;
    }

    /**
     * Returns a char array containing the characters from the source program that
     * represent this IToken.
     * 
     * Note that if the the IToken kind is a STRING_LIT, the characters are the raw
     * characters from the source, including the delimiters and unprocessed escape
     * sequences.
     * 
     * @return
     */
    @Override
    public char[] getText() {
        return text.toCharArray();
    }

    /**
     * Returns a SourceLocation record containing the line and position in the line
     * of the first character in this IToken.
     * 
     * @return
     */
    @Override
    public SourceLocation getSourceLocation() {
        return this.location;
    }

    /**
     * Precondition: getKind == NUM_LIT
     * 
     * @returns int value represented by the characters in this IToken
     */
    @Override
    public int getIntValue() {
        return num;
    }

    /**
     * Precondition: getKind == BOOLEAN_LIT
     * 
     * @return boolean value represented by the characters in this IToken
     */
    @Override
    public boolean getBooleanValue() {
        if (this.kind != Kind.BOOLEAN_LIT) {
            // the method can't be invoked if the kind of the token is not BOOLEAN_LIT
            assert false;
        }
        return this.text.equals("TRUE");
    }

    /**
     * compute the string value of a given text (mainly about the escape characters)
     * escape characters remain what it is, and two characters that combined to an
     * excape character shall be replaced by the coresponding escape characters.
     * 
     * @param text input text, including some escape characters.
     * @return the String value.
     */
    private String computeStringValue(String text) {
        StringBuilder resStringBuilder = new StringBuilder();
        int i = 1;
        // iterate text[1:length - 1], to remove the two quotes.
        while (i < text.length() - 1) {
            if (text.charAt(i) == '\\') {
                switch (text.charAt(i + 1)) {
                    case 'n' -> {
                        resStringBuilder.append('\n');
                    }
                    case 't' -> {
                        resStringBuilder.append('\t');
                    }
                    case 'b' -> {
                        resStringBuilder.append('\b');
                    }
                    case 'f' -> {
                        resStringBuilder.append('\f');
                    }
                    case 'r' -> {
                        resStringBuilder.append('\r');
                    }
                    case '"' -> {
                        resStringBuilder.append('\"');
                    }
                    case '\'' -> {
                        resStringBuilder.append('\'');
                    }
                    case '\\' -> {
                        resStringBuilder.append('\\');
                    }
                    default -> {
                        // it would be a bug to be here.
                        // since we can't modify the IToken class, we can't throw exceptions here, so
                        // just assert false here.
                        assert false;
                    }
                }
                i += 2;
                continue;
            }
            resStringBuilder.append(text.charAt(i));
            i++;

        }
        return resStringBuilder.toString();
    }

    /**
     * Precondition: getKind == STRING_LIT
     *
     * @return String value represented by the characters in this IToken. The
     *         returned String does not include the delimiters, and escape sequences
     *         have been handled.
     * @throws LexicalException
     */
    @Override
    public String getStringValue() {
        if (this.kind != Kind.STRING_LIT) {
            // a token other than STRING_LIT should not call this methods. I would like to
            // throw an exception here, but we can't change the IToken interface, so I can't
            // throw a lexicalException here, so just assert false here.

            assert false;
        }

        return this.stringValue;
    }

}
