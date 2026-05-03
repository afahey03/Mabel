import java.util.*;

class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("let", TokenType.LET),
            Map.entry("if", TokenType.IF),
            Map.entry("else", TokenType.ELSE),
            Map.entry("while", TokenType.WHILE),
            Map.entry("for", TokenType.FOR),
            Map.entry("function", TokenType.FUNCTION),
            Map.entry("return", TokenType.RETURN),
            Map.entry("true", TokenType.TRUE),
            Map.entry("false", TokenType.FALSE),
            Map.entry("and", TokenType.AND),
            Map.entry("or", TokenType.OR),
            Map.entry("not", TokenType.NOT),
            Map.entry("print", TokenType.PRINT),
            Map.entry("class", TokenType.CLASS),
            Map.entry("extends", TokenType.EXTENDS),
            Map.entry("this", TokenType.THIS),
            Map.entry("super", TokenType.SUPER),
            Map.entry("new", TokenType.NEW),
            Map.entry("int", TokenType.INT),
            Map.entry("double", TokenType.DOUBLE),
            Map.entry("string", TokenType.STRING_TYPE),
            Map.entry("bool", TokenType.BOOL),
            Map.entry("void", TokenType.VOID),
            Map.entry("interface", TokenType.INTERFACE),
            Map.entry("implements", TokenType.IMPLEMENTS));

    Lexer(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                addToken(TokenType.NEWLINE);
                break;
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '[':
                addToken(TokenType.LEFT_BRACKET);
                break;
            case ']':
                addToken(TokenType.RIGHT_BRACKET);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case ':':
                addToken(TokenType.COLON);
                break;
            case '*':
                addToken(TokenType.MULTIPLY);
                break;
            case '%':
                addToken(TokenType.MODULO);
                break;
            case '!':
                addToken(match('=') ? TokenType.NOT_EQUALS : TokenType.NOT);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUALS : TokenType.ASSIGN);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.DIVIDE);
                }
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    MabelCompiler.error(line, "Unexpected character: " + c);
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek()))
                advance();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            MabelCompiler.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}