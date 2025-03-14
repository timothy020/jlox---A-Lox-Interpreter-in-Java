package com.craftinginterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{
    private final Stmt.Function declaration;
    LoxFunction (Stmt.Function declaration) {
        this.declaration = declaration;
    }
    @Override
    public int arity() {
        return declaration.params.size();
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // Each Function Call has its own environment. Otherwise, recursion would break.
        Environment environment = new Environment(interpreter.globals);
        // Bind Parameter and Argument
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(
                    declaration.params.get(i).lexeme,
                    arguments.get(i));
        }
        // Execute Body.
        // executeBlock() discards this function-local environment and restores the previous one
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }
    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
