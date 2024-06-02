package com.mesniak.jecksel;

public record Value(Type type, Object value) {
    public enum Type {
        STRING,
        NUMBER,
        FORMULA,
    }

    public String toString() {
        switch (type) {
            case STRING, FORMULA -> {
                return (String) value;
            }
            case NUMBER -> {
                return String.valueOf(value);
            }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    public Type getType() {
        return type;
    }

    public Double getDouble() {
        if (type != Type.NUMBER) {
            throw new IllegalStateException("Value is not a number");
        }
        return (Double) value;
    }

    public String getString() {
        if (type != Type.STRING) {
            throw new IllegalStateException("Value is not a string");
        }
        return (String) value;
    }

    public String getFormula() {
        if (type != Type.FORMULA) {
            throw new IllegalStateException("Value is not a formula");
        }
        return (String) value;
    }

    public static Value from(String value) {
        if (value.startsWith("=")) {
            return new Value(Type.FORMULA, value);
        }
        try {
            return new Value(Type.NUMBER, Double.parseDouble(value));
        } catch (NumberFormatException e) {
            if (value.equals("\"\"")) {
                // Intellij saves empty strings as "\"\""
                // instead of using no value in CSV files.
                return new Value(Type.STRING, "");
            }

            return new Value(Type.STRING, value);
        }
    }

    public static Value from(Double number) {
        return new Value(Type.NUMBER, number);
    }
}
