package com.example.appmaytinhcamtay;

public class ExpressionEvaluator {

    private String s;   // chuỗi biểu thức đang xử lý
    private int pos;    // vị trí ký tự đang đọc

    public double evaluate(String expression) {
        s = expression;
        pos = 0;
        double value = parseExpression();
        if (pos < s.length()) {
            throw new IllegalArgumentException("Biểu thức không hợp lệ");
        }
        return value;
    }

    // Cộng trừ (ưu tiên thấp)
    private double parseExpression() {
        double value = parseTerm();
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (c == '+') { pos++; value += parseTerm(); }
            else if (c == '-') { pos++; value -= parseTerm(); }
            else break;
        }
        return value;
    }

    // Nhân chia (ưu tiên cao hơn)
    private double parseTerm() {
        double value = parseFactor();
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (c == '*') { pos++; value *= parseFactor(); }
            else if (c == '/') {
                pos++;
                double divisor = parseFactor();
                if (divisor == 0) throw new ArithmeticException("Chia cho 0");
                value /= divisor;
            }
            else break;
        }
        return value;
    }

    // Đọc một số (có thể có dấu âm đứng trước)
    private double parseFactor() {
        if (pos < s.length() && s.charAt(pos) == '-') {
            pos++;
            return -parseFactor();
        }
        int start = pos;
        while (pos < s.length()
                && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) {
            pos++;
        }
        if (start == pos) throw new IllegalArgumentException("Biểu thức không hợp lệ");
        return Double.parseDouble(s.substring(start, pos));
    }
}
