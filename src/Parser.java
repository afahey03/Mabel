import java.util.*;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            if (match(TokenType.NEWLINE))
                continue;
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.CLASS))
                return classDeclaration();
            if (match(TokenType.FUNCTION))
                return function("function");
            if (match(TokenType.LET))
                return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect class name.");

        Expr.Variable superclass = null;
        if (match(TokenType.LESS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }

        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.NEWLINE))
                continue;

            if (match(TokenType.FUNCTION)) {
                methods.add(functionBody("method"));
            } else {
                error(peek(), "Expect method declaration.");
                advance();
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt.Function functionBody(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        List<Token> paramTypes = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                Token param = consume(TokenType.IDENTIFIER, "Expect parameter name.");
                parameters.add(param);

                Token paramType = null;
                if (match(TokenType.COLON)) {
                    paramType = consume(TokenType.IDENTIFIER, "Expect parameter type.");
                }
                paramTypes.add(paramType);
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

        Token returnType = null;
        if (match(TokenType.COLON)) {
            returnType = consume(TokenType.IDENTIFIER, "Expect return type.");
        }

        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, paramTypes, returnType, body);
    }

    private Stmt statement() {
        if (match(TokenType.IF))
            return ifStatement();
        if (match(TokenType.PRINT))
            return printStatement();
        if (match(TokenType.RETURN))
            return returnStatement();
        if (match(TokenType.WHILE))
            return whileStatement();
        if (match(TokenType.LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consumeEndOfStatement("Expect newline or semicolon after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!checkEndOfStatement()) {
            value = expression();
        }

        consumeEndOfStatement("Expect newline or semicolon after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }

        consumeEndOfStatement("Expect newline or semicolon after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consumeEndOfStatement("Expect newline or semicolon after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind) {
        return functionBody(kind);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.NEWLINE))
                continue;
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.NOT_EQUALS, TokenType.EQUALS)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.DIVIDE, TokenType.MULTIPLY, TokenType.MODULO)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.LEFT_BRACKET)) {
                Expr index = expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']' after array index.");
                expr = new Expr.Index(expr, index);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN,
                "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.THIS)) {
            return new Expr.This(previous());
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(TokenType.LEFT_BRACKET)) {
            List<Expr> elements = new ArrayList<>();
            if (!check(TokenType.RIGHT_BRACKET)) {
                do {
                    elements.add(expression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_BRACKET, "Expect ']' after array elements.");
            return new Expr.Array(elements);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private void consumeEndOfStatement(String message) {
        if (match(TokenType.SEMICOLON, TokenType.NEWLINE) || isAtEnd())
            return;
        throw error(peek(), message);
    }

    private boolean checkEndOfStatement() {
        return check(TokenType.SEMICOLON) || check(TokenType.NEWLINE) || isAtEnd();
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        MabelCompiler.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON)
                return;

            switch (peek().type) {
                case FUNCTION:
                case LET:
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                case PRINT:
                case NUMBER:
                case AND:
                case NEWLINE:
                case EOF:
                case RIGHT_BRACE:
                case MULTIPLY:
                case DOT:
                case EQUALS:
                case IDENTIFIER:
                case PLUS:
                case BOOLEAN:
                case LESS:
                case GREATER:
                case LEFT_BRACKET:
                case NOT:
                case MODULO:
                case STRING:
                case ELSE:
                case FALSE:
                case DIVIDE:
                case GREATER_EQUAL:
                case NOT_EQUALS:
                case COMMA:
                case RIGHT_BRACKET:
                case ASSIGN:
                case LEFT_BRACE:
                case LESS_EQUAL:
                case RIGHT_PAREN:
                case LEFT_PAREN:
                case OR:
                case SEMICOLON:
                case TRUE:
                case MINUS:
                case CLASS:
                case DOUBLE:
                case BOOL:
                case THIS:
                case VOID:
                case SUPER:
                case STRING_TYPE:
                case COLON:
                case NEW:
                case INT:
                    return;
            }

            advance();
        }
    }
}