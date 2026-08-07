package com.dialog.dtg.core.service;

import com.dialog.dtg.core.model.Condition.Operator;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ConditionEvaluator {

    public boolean evaluate(Operator op, String actual, String expected) {
        return switch (op) {
            case EQ       -> actual != null && actual.equals(expected);
            case NE       -> !Objects.equals(actual, expected);
            case CONTAINS -> actual != null && actual.contains(expected != null ? expected : "");
            case EXISTS   -> actual != null && !actual.isBlank();
            case EMPTY    -> actual == null || actual.isBlank();
            case LT_STATUS -> {
                try { yield Integer.parseInt(actual) < Integer.parseInt(expected); }
                catch (NumberFormatException e) { yield false; }
            }
        };
    }
}
