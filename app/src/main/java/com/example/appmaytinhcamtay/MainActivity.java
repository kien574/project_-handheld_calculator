package com.example.appmaytinhcamtay;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

public class MainActivity extends AppCompatActivity {
    private TextView textExpression, textResult, textPercent;
    private ProgressBar progressBarTimeout;
    private GridLayout keypad;
    private final StringBuilder expression = new StringBuilder();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    private CountDownTimer countDownTimer;
    private boolean isDegreeMode = true; // Trạng thái DEG / RAD cho phím deg
    private Button degBtn; // Tham chiếu tới nút deg để đổi chữ

    // Định nghĩa cấu trúc phím cho phần lưới bên trái (hàng, cột, rowspan, colspan)
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

    // Phần lưới bên trái (Hàng 0 là các phím khoa học dẹt, Hàng 1-5 là các phím chính trừ cột phải)
    private final Key[] leftKeys = {
            // Hàng 0: phím khoa học
            new Key("e",   0, 0, 1, 1),
            new Key("μ",   0, 1, 1, 1),
            new Key("sin", 0, 2, 1, 1),
            // ("deg" nằm ở cột 3 được xử lý riêng)

            // Hàng 1
            new Key("Ac", 1, 0, 1, 1),
            new Key("⌫", 1, 1, 1, 1),
            new Key("/",  1, 2, 1, 1),

            // Hàng 2
            new Key("7", 2, 0, 1, 1),
            new Key("8", 2, 1, 1, 1),
            new Key("9", 2, 2, 1, 1),

            // Hàng 3
            new Key("4", 3, 0, 1, 1),
            new Key("5", 3, 1, 1, 1),
            new Key("6", 3, 2, 1, 1),

            // Hàng 4
            new Key("1", 4, 0, 1, 1),
            new Key("2", 4, 1, 1, 1),
            new Key("3", 4, 2, 1, 1),

            // Hàng 5
            new Key("0", 5, 0, 1, 2), // Chiếm 2 cột
            new Key(".", 5, 2, 1, 1)
    };

    // Cột bên phải (cột 3): gồm *, -, +, = chia thành 5 khoảng chiều cao tỉ lệ (weight)
    private final String[] rightLabels = {"*", "-", "+", "="};
    // Tỉ lệ chiều cao tương ứng cho 4 phím cột phải: *, -, +, =
    private final float[] rightWeights = {2f, 2f, 3f, 3f};

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

    // Kiểm tra phím có thuộc hàng khoa học phía trên không
    private boolean isScientific(String label) {
        return label.equals("e") || label.equals("μ") || label.equals("sin") || label.equals("deg") || label.equals("rad");
    }

    // Tạo một nút từ layout mẫu, tô màu theo loại phím và gắn sự kiện bấm
    private Button createButton(LayoutInflater inflater, ViewGroup parent, String label) {
        int layoutId = label.equals("=") ? R.layout.activity_item_button_equal
                : R.layout.activity_item_button;
        Button btn = (Button) inflater.inflate(layoutId, parent, false);
        btn.setText(label);

        if (label.equals("=")) {
            btn.setBackgroundResource(R.drawable.bg_equals_button);
            btn.setBackgroundTintList(null);
            btn.setTextColor(Color.WHITE);

        } else if (label.equals("+") || label.equals("-") || label.equals("*") || label.equals("/")) {
            btn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.color_btn_Operation)));
            btn.setTextColor(ContextCompat.getColor(this, R.color.color_btn_text_value));

        } else if (label.equals("Ac") || label.equals("⌫")) {
            btn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.color_btn_value)));
            btn.setTextColor(ContextCompat.getColor(this, R.color.color_btn_text_cancel));

        } else if (isScientific(label)) {
            // Hàng phím khoa học: dẹt, nền và chữ nhạt hơn, chữ nhỏ hơn
            int bg = ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(this, R.color.color_btn_value), 140);
            int text = ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(this, R.color.color_btn_Operation), 255);
            btn.setBackgroundTintList(ColorStateList.valueOf(bg));
            btn.setTextColor(text);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            btn.setMinHeight(0);
            btn.setMinimumHeight(0);
            btn.setPadding(0, 0, 0, 0);
            btn.setIncludeFontPadding(false);

        } else {
            // Phím số và dấu chấm
            btn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.color_btn_value)));
            btn.setTextColor(ContextCompat.getColor(this, R.color.color_btn_text_value));
        }

        btn.setOnClickListener(v -> onKeyClick(label));
        return btn;
    }

    // Dựng bàn phím: lưới bên trái + cột phải (*, -, +, =) có chiều cao theo tỉ lệ
    private void buildKeypad() {
        LayoutInflater inflater = LayoutInflater.from(this);
        int horizontalMargin = dpToPx(8); // Lề ngang (8dp)
        int verticalMargin = dpToPx(5);   // Lề dọc (5dp)

        keypad.removeAllViews();
        keypad.setColumnCount(4);
        keypad.setRowCount(6);

        // ----- Phần bên trái: các phím trong cột 0-2 -----
        for (Key k : leftKeys) {
            Button btn = createButton(inflater, keypad, k.label);

            // Hàng 0 (phím khoa học) chỉ cao bằng nửa hàng thường
            float rowWeight = (k.row == 0) ? 0.5f : 1f;

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(k.row, 1, GridLayout.FILL, rowWeight),
                    GridLayout.spec(k.col, k.colSpan, GridLayout.FILL, k.colSpan));
            lp.width = 0;
            lp.height = 0;
            lp.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
            btn.setLayoutParams(lp);
            keypad.addView(btn);
        }

        // ----- Phím "deg/rad" nằm ở cột 3 của hàng 0 -----
        degBtn = createButton(inflater, keypad, isDegreeMode ? "deg" : "rad");
        GridLayout.LayoutParams degLp = new GridLayout.LayoutParams(
                GridLayout.spec(0, 1, GridLayout.FILL, 0.5f),
                GridLayout.spec(3, 1, GridLayout.FILL, 1f));
        degLp.width = 0;
        degLp.height = 0;
        degLp.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
        degBtn.setLayoutParams(degLp);
        keypad.addView(degBtn);

        // ----- Phần bên phải: cột dọc chiếm hàng 1-5, chia chiều cao theo tỉ lệ 2:2:3:3 -----
        LinearLayout rightColumn = new LinearLayout(this);
        rightColumn.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < rightLabels.length; i++) {
            // FrameLayout bọc ngoài để lề nằm TRONG phần weight -> các hàng thẳng đúng với lưới trái
            FrameLayout cell = new FrameLayout(this);
            cell.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, rightWeights[i]));

            Button btn = createButton(inflater, cell, rightLabels[i]);
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            flp.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
            btn.setLayoutParams(flp);

            cell.addView(btn);
            rightColumn.addView(cell);
        }

        // Weight hàng = 0 vì chiều cao do các phím bên trái quyết định; FILL sẽ kéo giãn cột phải
        GridLayout.LayoutParams rightLp = new GridLayout.LayoutParams(
                GridLayout.spec(1, 5, GridLayout.FILL),
                GridLayout.spec(3, 1, GridLayout.FILL, 1f));
        rightLp.width = 0;
        rightLp.height = 0;
        rightColumn.setLayoutParams(rightLp);
        keypad.addView(rightColumn);
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
                    double result = evaluator.evaluate(expression.toString(), isDegreeMode);
                    textResult.setText("=" + ResultFormatter.format(result));   // luôn là String
                } catch (ArithmeticException e) {
                    textResult.setText("Lỗi: chia cho 0");
                } catch (IllegalArgumentException e) {
                    textResult.setText("Biểu thức không hợp lệ");
                }
                break;

            case "sin":
                if (expression.length() == 0) startTimeoutTimer();
                expression.append("sin(");
                textExpression.setText(expression.toString());
                break;

            case "e":
            case "μ":
                if (expression.length() == 0) startTimeoutTimer();
                expression.append(label);
                textExpression.setText(expression.toString());
                break;

            case "deg":
            case "rad":
                isDegreeMode = !isDegreeMode;
                if (degBtn != null) {
                    degBtn.setText(isDegreeMode ? "deg" : "rad");
                }
                // Tự động tính toán lại kết quả ngay lập tức khi chuyển đổi DEG / RAD
                if (expression.length() > 0 && textResult.length() > 0) {
                    try {
                        double result = evaluator.evaluate(expression.toString(), isDegreeMode);
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
