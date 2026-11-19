package com.example.appmaytinhcamtay;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void testAddition() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        assertEquals(4.0, evaluator.evaluate("2+2"), 0.0001);
    }

    @Test
    public void testOperatorPrecedence() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        assertEquals(14.0, evaluator.evaluate("2+3*4"), 0.0001);
    }

    @Test
    public void testDivisionByZero() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        assertThrows(ArithmeticException.class, () -> {
            evaluator.evaluate("5/0");
        });
    }

    @Test
    public void testSinFunction() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        assertEquals(1.0, evaluator.evaluate("sin(90)", true), 0.0001);
    }

    @Test
    public void testResultFormatter() {
        assertEquals("12,454", ResultFormatter.format(12454.0));
    }
}