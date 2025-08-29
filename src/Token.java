enum TokenType {
    NUMBER, STRING, IDENTIFIER, BOOLEAN,

    LET, IF, ELSE, WHILE, FOR, FUNCTION, RETURN, TRUE, FALSE, PRINT,
    CLASS, EXTENDS, INTERFACE, IMPLEMENTS, THIS, SUPER, NEW,

    INT, DOUBLE, STRING_TYPE, BOOL, VOID,

    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    ASSIGN, EQUALS, NOT_EQUALS, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL,
    AND, OR, NOT,

    SEMICOLON, COMMA, DOT, COLON,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,

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