package com.example.appmaytinhcamtay;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private TextView textExpression, textResult, textPercent;
    private ProgressBar progressBarTimeout;
    private GridLayout keypad;
    private final StringBuilder expression = new StringBuilder();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    private CountDownTimer countDownTimer;

    // Mô tả một phím: chữ, hàng, cột, số hàng chiếm, số cột chiếm
    private static class Key {
        final String label;
        final int row, col, rowSpan, colSpan;

        public Key(String label, int row, int col, int rowSpan, int colSpan) {
            this.label = label;
            this.row = row;
            this.col = col;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
        }
    }

    // Danh sách phím ban đầu khớp với giao diện mẫu
    private final Key[] keys = {
            new Key("e", 0, 0, 1, 1),
            new Key("μ", 0, 1, 1, 1),
            new Key("sin", 0, 2, 1, 1),
            new Key("deg", 0, 3, 1, 1),
            new Key("Ac", 1, 0, 1, 1),
            new Key("⌫", 1, 1, 1, 1),
            new Key("/", 1, 2, 1, 1),
            new Key("*", 1, 3, 1, 1),
            new Key("7", 2, 0, 1, 1),
            new Key("8", 2, 1, 1, 1),
            new Key("9", 2, 2, 1, 1),
            new Key("-", 2, 3, 1, 1),
            new Key("4", 3, 0, 1, 1),
            new Key("5", 3, 1, 1, 1),
            new Key("6", 3, 2, 1, 1),
            new Key("+", 3, 3, 1, 1),
            new Key("1", 4, 0, 1, 1),
            new Key("2", 4, 1, 1, 1),
            new Key("3", 4, 2, 1, 1),
            new Key("=", 4, 3, 2, 1),
            new Key("0", 5, 0, 1, 2),
            new Key(".", 5, 2, 1, 1)
    };

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // Quản lý đồng hồ đếm ngược 10 giây (Timeout Manager)
    private void startTimeoutTimer() {
        stopTimeoutTimer();
        progressBarTimeout.setProgress(0);
        textPercent.setText("0%");

        countDownTimer = new CountDownTimer(10000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsed = 10000 - millisUntilFinished;
                int percent = (int) ((elapsed * 100) / 10000);
                if (percent > 100) percent = 100;
                progressBarTimeout.setProgress(percent);
                textPercent.setText(percent + "%");
            }

            @Override
            public void onFinish() {
                progressBarTimeout.setProgress(100);
                textPercent.setText("100%");
                // Kịch bản quá hạn (Timeout Reset): xóa sạch biểu thức và kết quả
                expression.setLength(0);
                textExpression.setText("");
                textResult.setText("");
                progressBarTimeout.setProgress(0);
                textPercent.setText("0%");
            }
        }.start();
    }

    private void stopTimeoutTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void resetTimeoutTimer() {
        stopTimeoutTimer();
        progressBarTimeout.setProgress(0);
        textPercent.setText("0%");
    }

    // Nhân bản nút mẫu cho từng phím và thêm vào bàn phím
    private void buildKeypad() {
        LayoutInflater inflater = LayoutInflater.from(this);
        int horizontalMargin = dpToPx(8); // Lề ngang rộng hơn (8dp)
        int verticalMargin = dpToPx(5);   // Lề dọc hẹp hơn (5dp)

        for (Key k : keys) {
            int layoutId = k.label.equals("=") ? R.layout.activity_item_button_equal
                    : R.layout.activity_item_button;
            Button btn = (Button) inflater.inflate(layoutId, keypad, false);
            btn.setText(k.label);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(k.row, k.rowSpan, GridLayout.FILL, 1f),
                    GridLayout.spec(k.col, k.colSpan, GridLayout.FILL, 1f));
            lp.width = 0;
            lp.height = 0;
            lp.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
            btn.setLayoutParams(lp);

            // Tùy chỉnh màu sắc từng loại nút theo thiết kế mẫu
            if (k.label.equals("=")) {
                btn.setBackgroundResource(R.drawable.bg_equals_button);
                btn.setBackgroundTintList(null);
                btn.setTextColor(Color.WHITE);
            } else if (k.label.equals("+") || k.label.equals("-") || k.label.equals("*") || k.label.equals("/")) {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_btn_Operation)));
                btn.setTextColor(ContextCompat.getColor(this, R.color.color_btn_text_value));
            } else if (k.label.equals("Ac") || k.label.equals("⌫")) {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_btn_value)));
                btn.setTextColor(ContextCompat.getColor(this, R.color.color_btn_text_cancel));
            } else {
                // Phím số và phím hàm khoa học (e, μ, sin, deg)
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_btn_value)));
                btn.setTextColor(ContextCompat.getColor(this, R.color.color_btn_text_value));
            }

            btn.setOnClickListener(v -> onKeyClick(k.label));
            keypad.addView(btn);
        }
    }

    private void onKeyClick(String label) {
        switch (label) {
            case "Ac":
                expression.setLength(0);          // xóa sạch biểu thức
                textExpression.setText("");
                textResult.setText("");
                resetTimeoutTimer();              // Khởi tạo/reset bộ đếm về 0%
                break;

            case "⌫":
                if (expression.length() > 0) {
                    expression.deleteCharAt(expression.length() - 1);  // xóa 1 ký tự cuối
                    textExpression.setText(expression.toString());
                    if (expression.length() == 0) {
                        resetTimeoutTimer();
                    }
                }
                break;

            case "=":
                if (expression.length() == 0) break;
                try {
                    // Dừng ngay lập tức bộ đếm thời gian khi bấm dấu =
                    stopTimeoutTimer();
                    double result = evaluator.evaluate(expression.toString());
                    textResult.setText("=" + ResultFormatter.format(result));   // luôn là String
                } catch (ArithmeticException e) {
                    textResult.setText("Lỗi: chia cho 0");
                } catch (IllegalArgumentException e) {
                    textResult.setText("Biểu thức không hợp lệ");
                }
                break;

            default:                  // số, dấu chấm, + - * /, e, μ, sin, deg
                appendKey(label);
                break;
        }
    }

    private void appendKey(String label) {
        // Nếu biểu thức đang trống và người dùng bấm ký tự số hoặc phép tính đầu tiên -> Kích hoạt bộ đếm 10 giây
        if (expression.length() == 0) {
            startTimeoutTimer();
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

        buildKeypad();
        resetTimeoutTimer();
    }
}
