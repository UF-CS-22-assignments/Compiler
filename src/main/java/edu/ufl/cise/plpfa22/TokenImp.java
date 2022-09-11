package edu.ufl.cise.plpfa22;

public class TokenImp implements IToken {
    private Kind kind;

    private SourceLocation location;

    private String text;

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
        // TODO
        // TODO: make sure for string)lit, it returns the raw characters.
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
        // TODO
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
            // TODO: raise a custom exception.
        }
        return this.text.equals("TRUE");
    }

    /**
     * Precondition: getKind == STRING_LIT
     * 
     * @return String value represented by the characters in this IToken. The
     *         returned String does not include the delimiters, and escape sequences
     *         have been handled.
     */
    @Override
    public String getStringValue() {
        String stringValue = "";
        int i=1;
        while(i<text.length()-1){
            if(text.charAt(i) == '\\'){
                switch (text.charAt(i+1)){
                    case 'n'->{
                        stringValue +='\n';
                    }
                    case 't' ->{
                        stringValue += '\t';
                    }
                }
                // TODO: Add other special characters
                i += 2;
                continue;
            }
            stringValue += text.charAt(i);
            i++;

        }
        return stringValue;
    }

}
