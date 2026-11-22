package com.example.appmaytinhcamtay;

import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements KeypadManager.OnKeyClickListener {
    private TextView textExpression, textResult, textPercent;
    private ProgressBar progressBarTimeout;
    private GridLayout keypad;
    private final StringBuilder expression = new StringBuilder();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    private TimeoutManager timeoutManager;
    private KeypadManager keypadManager;
    private boolean isNewCalculation = false; // Cờ kiểm tra sau khi bấm dấu =
    private double lastResult = 0.0; // Lưu kết quả trước đó để nối tiếp phép tính (ví dụ: 2+2=4, bấm *3 thành 4*3)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View từ XML sang Java
        textExpression = findViewById(R.id.textExpression);
        textResult = findViewById(R.id.textResult);
        progressBarTimeout = findViewById(R.id.progressBarTimeout);
        textPercent = findViewById(R.id.textPercent);
        keypad = findViewById(R.id.keypad);

        timeoutManager = new TimeoutManager(progressBarTimeout, textPercent, () -> {
            expression.setLength(0);
            textExpression.setText("");
            textResult.setText("");
            isNewCalculation = false;
        });

        keypadManager = new KeypadManager(this, keypad, this);
        keypadManager.buildKeypad();
        timeoutManager.reset();
    }

    @Override
    public void onKeyClick(String label) {
        switch (label) {
            case "Ac":
                expression.setLength(0);          // xóa sạch biểu thức
                textExpression.setText("");
                textResult.setText("");
                timeoutManager.reset();           // Khởi tạo/reset bộ đếm về 0%
                isNewCalculation = false;
                break;

            case "⌫":
                if (expression.length() > 0) {
                    expression.deleteCharAt(expression.length() - 1);  // xóa 1 ký tự cuối
                    textExpression.setText(expression.toString());
                    if (expression.length() == 0) {
                        timeoutManager.reset();
                    }
                }
                break;

            case "=":
                if (expression.length() == 0) break;
                try {
                    timeoutManager.stop();
                    double result = evaluator.evaluate(expression.toString(), keypadManager.isDegreeMode());
                    lastResult = result;
                    textResult.setText("=" + ResultFormatter.format(result));
                    isNewCalculation = true; // Đánh dấu đã xong phép tính
                } catch (ArithmeticException e) {
                    textResult.setText("Lỗi: chia cho 0");
                    isNewCalculation = true;
                } catch (IllegalArgumentException e) {
                    textResult.setText("Biểu thức không hợp lệ");
                    isNewCalculation = true;
                }
                break;

            case "sin":
                if (isNewCalculation) {
                    expression.setLength(0);
                    textResult.setText("");
                    isNewCalculation = false;
                }
                if (expression.length() == 0) timeoutManager.start();
                expression.append("sin(");
                textExpression.setText(expression.toString());
                break;

            case "e":
            case "μ":
                if (isNewCalculation) {
                    expression.setLength(0);
                    textResult.setText("");
                    isNewCalculation = false;
                }
                if (expression.length() == 0) timeoutManager.start();
                expression.append(label);
                textExpression.setText(expression.toString());
                break;

            case "deg":
            case "rad":
                keypadManager.toggleDegreeMode();
                // Tự động tính toán lại kết quả ngay lập tức khi chuyển đổi DEG / RAD
                if (expression.length() > 0 && textResult.length() > 0) {
                    try {
                        double result = evaluator.evaluate(expression.toString(), keypadManager.isDegreeMode());
                        lastResult = result;
                        textResult.setText("=" + ResultFormatter.format(result));
                    } catch (Exception ignored) {}
                }
                break;

            default:                  // số, dấu chấm, + - * /
                appendKey(label);
                break;
        }
    }

    private void appendKey(String label) {
        // Nếu vừa bấm dấu = xong và bấm tiếp toán tử (+, -, *, /) -> Nối tiếp kết quả trước đó (ví dụ: 4 * 3)
        if (isNewCalculation) {
            if (isOperator(label)) {
                expression.setLength(0);
                expression.append(ResultFormatter.format(lastResult));
            } else {
                // Bấm số mới -> Bắt đầu phép tính mới
                expression.setLength(0);
                textResult.setText("");
            }
            isNewCalculation = false;
        }

        // Nếu biểu thức đang trống và người dùng bấm ký tự số hoặc phép tính đầu tiên -> Kích hoạt bộ đếm 10 giây
        if (expression.length() == 0) {
            timeoutManager.start();
        }

        if (isOperator(label)) {
            if (expression.length() == 0) {
                // Chưa có số: chỉ cho phép dấu trừ (số âm)
                if (!label.equals("-")) return;
            } else if (expression.length() == 1 && lastChar() == '-') {
                // Biểu thức chỉ có mỗi dấu "-": không cho thêm phép toán nữa
                return;
            } else if (isOperator(lastChar())) {
                // Hai phép toán liền nhau: thay phép cũ bằng phép mới
                expression.deleteCharAt(expression.length() - 1);
            }
        } else if (label.equals(".")) {
            // Duyệt ngược về đầu số hiện tại, nếu đã có dấu chấm thì bỏ qua
            int i = expression.length() - 1;
            while (i >= 0 && !isOperator(expression.charAt(i))) {
                if (expression.charAt(i) == '.') return;
                i--;
            }
            // Bắt đầu số bằng dấu chấm thì tự thêm số 0 phía trước
            if (expression.length() == 0 || isOperator(lastChar())) {
                expression.append("0");
            }
        }
        expression.append(label);
        textExpression.setText(expression.toString());
    }

    private boolean isOperator(String label) {
        return label.equals("+") || label.equals("-") || label.equals("*") || label.equals("/");
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private char lastChar() {
        return expression.charAt(expression.length() - 1);
    }
}
