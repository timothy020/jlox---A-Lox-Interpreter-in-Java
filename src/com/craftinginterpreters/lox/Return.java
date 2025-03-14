package com.craftinginterpreters.lox;

public class Return extends RuntimeException{
    final Object value;

    Return(Object value) {
        // null, null: 不设置异常信息 & 原因（即没有 message 和 cause）。
        // false, false: 不填充 stackTrace（优化性能，防止不必要的栈回溯）
        super(null, null, false, false);
        this.value = value;
    }
}
