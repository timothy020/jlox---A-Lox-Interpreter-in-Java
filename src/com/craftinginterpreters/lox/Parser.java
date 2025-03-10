package com.craftinginterpreters.lox;

import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return equity();
    }
    private Expr equity() {
        Expr expr = comparison();
        expr = parseRecursive(Arrays.asList(BANG_EQUAL, EQUAL_EQUAL));
        return expr;
    }
    private Expr comparison() {
        Expr expr = term();
        expr = parseRecursive(Arrays.asList(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL));
        return expr;
    }
    private Expr term() {
        Expr expr = factor();
        expr = parseRecursive(Arrays.asList(MINUS, PLUS));
        return expr;
    }
    private Expr factor() {
        Expr expr = unary();
        expr = parseRecursive(Arrays.asList(SLASH, STAR));
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

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
     }

    /**
     * private tool methods
     */

    private Expr parseRecursive(List<TokenType> types) {
        TokenType[] matchTypes = types.toArray(new TokenType[0]);

        Expr expr = null;
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
}
