package com.craftinginterpreters.lox;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * wrapper：对外暴露的接口
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    /**
     * About Statement
     */
    private Stmt declaration() {
        try {
            if(match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if(match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        return expressionStatement();
    }
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /**
     * About Expression
     */
    private Expr expression() {
        return equality();
    }
    private Expr equality() {
        Expr expr = comparison();
        expr = parseRecursive(expr, Arrays.asList(BANG_EQUAL, EQUAL_EQUAL));
        return expr;
    }
    private Expr comparison() {
        Expr expr = term();
        expr = parseRecursive(expr, Arrays.asList(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL));
        return expr;
    }
    private Expr term() {
        Expr expr = factor();
        expr = parseRecursive(expr, Arrays.asList(MINUS, PLUS));
        return expr;
    }
    private Expr factor() {
        Expr expr = unary();
        expr = parseRecursive(expr, Arrays.asList(SLASH, STAR));
        return expr;
    }
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
     }

    /**
     * private tool methods
     */

    private Expr parseRecursive(Expr expr, List<TokenType> types) {
        TokenType[] matchTypes = types.toArray(new TokenType[0]);

        while (match(matchTypes)) {
            // left-associative【这就是为什么会“短路”】
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
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
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    //  错误处理相关
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
