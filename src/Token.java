enum TokenType {
    // Literals
    NUMBER, STRING, IDENTIFIER, BOOLEAN,

    // Keywords
    LET, IF, ELSE, WHILE, FOR, FUNCTION, RETURN, TRUE, FALSE, PRINT,
    CLASS, EXTENDS, THIS, SUPER, NEW,

    // Types
    INT, DOUBLE, STRING_TYPE, BOOL, VOID,

    // Operators
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    ASSIGN, EQUALS, NOT_EQUALS, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL,
    AND, OR, NOT,

    // Delimiters
    SEMICOLON, COMMA, DOT, COLON,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,

    // Special
    EOF, NEWLINE
}

class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}