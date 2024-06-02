package com.mesniak.jecksel.expression;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Implementation of the Shunting-Yard algorithm to parse and evaluate
 * expressions taking precedence into account.
 *
 * Note that we do not provide any form of error handling but will
 * simply throw an exception if the expression is not valid.
 */
public class Expression {
    public static class Element {
    }

    public static class Operator extends Element {
        private final char operator;

        public Operator(char operator) {
            this.operator = operator;
        }

        public char getOperator() {
            return operator;
        }

        @Override
        public String toString() {
            return "%c".formatted(operator);
        }
    }

    public static class Number extends Element {
        private final Double value;

        public Number(Double value) {
            this.value = value;
        }

        public Double get() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static Double evaluate(String expression) {
        ArrayList<Element> stack = createStack(expression);
        return compute(stack);
    }

    private static Double compute(ArrayList<Element> stack) {
        var result = new LinkedList<Double>();

        for (Element element : stack) {
            switch (element) {
                case Number n:
                    result.add(n.get());
                    break;
                case Operator o:
                    var b = result.removeLast();
                    var a = result.removeLast();
                    switch (o.getOperator()) {
                        case '+':
                            result.add(a + b);
                            break;
                        case '-':
                            result.add(a - b);
                            break;
                        case '*':
                            result.add(a * b);
                            break;
                        case '/':
                            result.add(a / b);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + o);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + element);
            }
        }

        return result.getFirst();
    }

    public static ArrayList<Element> createStack(String expression) {
        ArrayList<Element> token = tokenize(expression);
        ArrayList<Element> output = new ArrayList<>();
        ArrayList<Element> operator = new ArrayList<>();

        for (int i = 0; i < token.size(); i++) {
            Element e = token.get(i);

            if (e instanceof Number) {
                output.add(e);
                continue;
            }

            // Operator precendence.
            Operator o1 = (Operator) e;
            while (!operator.isEmpty()) {
                Operator o2 = (Operator) operator.getLast();
                if (!isLowerPrecedence(o1.getOperator(), o2.getOperator())) {
                    break;
                }

                var t = operator.removeLast();
                output.add(t);
            }

            // Parenthesis handling.
            if (o1.getOperator() == '(') {
                operator.add(o1);
            } else if (o1.getOperator() == ')') {
                while (!operator.isEmpty()) {
                    Operator o = (Operator) operator.removeLast();
                    if (o.getOperator() == '(') {
                        break;
                    }
                    output.add(o);
                }
            } else {
                operator.add(o1);
            }

        }
        output.addAll(operator.reversed());

        return output;
    }

    private static boolean isLowerPrecedence(char o1, char o2) {
        char[] precendence = new char[]{'+', '-', '*', '/'};

        var i1 = 0;
        for (int i = 0; i < precendence.length; i++) {
            if (precendence[i] == o1) {
                i1 = i;
                break;
            }
        }

        var i2 = 0;
        for (int i = 0; i < precendence.length; i++) {
            if (precendence[i] == o2) {
                i2 = i;
                break;
            }
        }

        return i1 < i2;
    }

    public static ArrayList<Element> tokenize(String expression) {
        ArrayList<Element> token = new ArrayList<>();
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isSpaceChar(c)) {
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                number.append(c);
            } else {
                if (!number.isEmpty()) {
                    token.add(new Number(Double.parseDouble(number.toString())));
                    number = new StringBuilder();
                }
                token.add(new Operator(c));
            }
        }
        if (!number.isEmpty()) {
            token.add(new Number(Double.parseDouble(number.toString())));
        }
        return token;
    }
}
