package com.example.appmaytinhcamtay;

public class ExpressionEvaluator {

    private String expressionText;   // chuỗi biểu thức đang xử lý
    private int currentIndex;        // vị trí ký tự đang đọc
    private boolean isDegreeMode = true;

    public double evaluate(String expression, boolean degreeMode) {
        this.expressionText = expression;
        this.currentIndex = 0;
        this.isDegreeMode = degreeMode;

        double value = parseExpression();
        if (currentIndex < expressionText.length()) {
            throw new IllegalArgumentException("Biểu thức không hợp lệ");
        }
        return value;
    }

    public double evaluate(String expression) {
        return evaluate(expression, true);
    }

    // Cộng trừ (ưu tiên thấp)
    private double parseExpression() {
        double value = parseTerm();
        while (currentIndex < expressionText.length()) {
            char c = expressionText.charAt(currentIndex);
            if (c == '+') { currentIndex++; value += parseTerm(); }
            else if (c == '-') { currentIndex++; value -= parseTerm(); }
            else break;
        }
        return value;
    }

    // Nhân chia (ưu tiên cao hơn)
    private double parseTerm() {
        double value = parseFactor();
        while (currentIndex < expressionText.length()) {
            char c = expressionText.charAt(currentIndex);
            if (c == '*') { currentIndex++; value *= parseFactor(); }
            else if (c == '/') {
                currentIndex++;
                double divisor = parseFactor();
                if (divisor == 0) throw new ArithmeticException("Chia cho 0");
                value /= divisor;
            }
            else break;
        }
        return value;
    }

    // Đọc số, hằng số (e, μ) hoặc hàm (sin) hỗ trợ tự động đóng ngoặc nếu thiếu
    private double parseFactor() {
        if (currentIndex < expressionText.length() && expressionText.charAt(currentIndex) == '-') {
            currentIndex++;
            return -parseFactor();
        }

        // Hằng số e (2.71828...)
        if (currentIndex < expressionText.length() && expressionText.charAt(currentIndex) == 'e') {
            currentIndex++;
            return Math.E;
        }

        // Hằng số μ (1e-6)
        if (currentIndex < expressionText.length() && expressionText.charAt(currentIndex) == 'μ') {
            currentIndex++;
            return 1e-6;
        }

        // Hàm sin(x) với cơ chế tự động đóng ngoặc nếu người dùng quên gõ dấu ')'
        if (currentIndex + 3 <= expressionText.length() && expressionText.substring(currentIndex, currentIndex + 3).equalsIgnoreCase("sin")) {
            currentIndex += 3;
            boolean hasParenthesis = false;
            if (currentIndex < expressionText.length() && expressionText.charAt(currentIndex) == '(') {
                hasParenthesis = true;
                currentIndex++;
            }
            double val = parseExpression();
            if (hasParenthesis) {
                if (currentIndex < expressionText.length() && expressionText.charAt(currentIndex) == ')') {
                    currentIndex++;
                }
                // Nếu người dùng quên gõ ')' ở cuối, hệ thống vẫn tự động chấp nhận và tính toán bình thường
            }
            return isDegreeMode ? Math.sin(Math.toRadians(val)) : Math.sin(val);
        }

        int start = currentIndex;
        while (currentIndex < expressionText.length()
                && (Character.isDigit(expressionText.charAt(currentIndex)) || expressionText.charAt(currentIndex) == '.')) {
            currentIndex++;
        }
        if (start == currentIndex) throw new IllegalArgumentException("Biểu thức không hợp lệ");
        return Double.parseDouble(expressionText.substring(start, currentIndex));
    }
}
