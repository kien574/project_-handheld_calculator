package com.example.appmaytinhcamtay;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

public class KeypadManager {

    public static class Key {
        final String label;
        final int row, col, colSpan;

        public Key(String label, int row, int col, int colSpan) {
            this.label = label;
            this.row = row;
            this.col = col;
            this.colSpan = colSpan;
        }
    }

    private final Context context;
    private final GridLayout keypad;
    private final OnKeyClickListener listener;
    private Button degBtn;
    private boolean isDegreeMode = true;

    public interface OnKeyClickListener {
        void onKeyClick(String label);
    }

    public KeypadManager(Context context, GridLayout keypad, OnKeyClickListener listener) {
        this.context = context;
        this.keypad = keypad;
        this.listener = listener;
    }

    public boolean isDegreeMode() {
        return isDegreeMode;
    }

    public void toggleDegreeMode() {
        isDegreeMode = !isDegreeMode;
        if (degBtn != null) {
            degBtn.setText(isDegreeMode ? "deg" : "rad");
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private boolean isScientific(String label) {
        return label.equals("e") || label.equals("μ") || label.equals("sin") || label.equals("deg") || label.equals("rad");
    }

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
                    ContextCompat.getColor(context, R.color.color_btn_Operation)));
            btn.setTextColor(ContextCompat.getColor(context, R.color.color_btn_text_value));
        } else if (label.equals("Ac") || label.equals("⌫")) {
            btn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.color_btn_value)));
            btn.setTextColor(ContextCompat.getColor(context, R.color.color_btn_text_cancel));
        } else if (isScientific(label)) {
            int bg = ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(context, R.color.color_btn_value), 140);
            int text = ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(context, R.color.color_btn_Operation), 255);
            btn.setBackgroundTintList(ColorStateList.valueOf(bg));
            btn.setTextColor(text);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            btn.setMinHeight(0);
            btn.setMinimumHeight(0);
            btn.setPadding(0, 0, 0, 0);
            btn.setIncludeFontPadding(false);
        } else {
            btn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.color_btn_value)));
            btn.setTextColor(ContextCompat.getColor(context, R.color.color_btn_text_value));
        }

        btn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onKeyClick(label);
            }
        });
        return btn;
    }

    public void buildKeypad() {
        LayoutInflater inflater = LayoutInflater.from(context);
        int horizontalMargin = dpToPx(8);
        int verticalMargin = dpToPx(5);

        keypad.removeAllViews();
        keypad.setColumnCount(4);
        keypad.setRowCount(6);
        keypad.setClipChildren(false);
        keypad.setClipToPadding(false);

        Key[] leftKeys = {
                new Key("e",   0, 0, 1),
                new Key("μ",   0, 1, 1),
                new Key("sin", 0, 2, 1),
                new Key("Ac", 1, 0, 1),
                new Key("⌫", 1, 1, 1),
                new Key("/",  1, 2, 1),
                new Key("7", 2, 0, 1),
                new Key("8", 2, 1, 1),
                new Key("9", 2, 2, 1),
                new Key("4", 3, 0, 1),
                new Key("5", 3, 1, 1),
                new Key("6", 3, 2, 1),
                new Key("1", 4, 0, 1),
                new Key("2", 4, 1, 1),
                new Key("3", 4, 2, 1),
                new Key("0", 5, 0, 2),
                new Key(".", 5, 2, 1)
        };

        String[] rightLabels = {"*", "-", "+", "="};
        float[] rightWeights = {2f, 2f, 3f, 3f};

        for (Key k : leftKeys) {
            Button btn = createButton(inflater, keypad, k.label);
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

        degBtn = createButton(inflater, keypad, isDegreeMode ? "deg" : "rad");
        GridLayout.LayoutParams degLp = new GridLayout.LayoutParams(
                GridLayout.spec(0, 1, GridLayout.FILL, 0.5f),
                GridLayout.spec(3, 1, GridLayout.FILL, 1f));
        degLp.width = 0;
        degLp.height = 0;
        degLp.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
        degBtn.setLayoutParams(degLp);
        keypad.addView(degBtn);

        LinearLayout rightColumn = new LinearLayout(context) {
            {
                setChildrenDrawingOrderEnabled(true);
            }

            @Override
            protected int getChildDrawingOrder(int childCount, int drawingPosition) {
                return childCount - 1 - drawingPosition; // vẽ ngược: "=" trước, "*" sau cùng
            }
        };
        rightColumn.setOrientation(LinearLayout.VERTICAL);
        rightColumn.setClipChildren(false);
        rightColumn.setClipToPadding(false);

        for (int i = 0; i < rightLabels.length; i++) {
            FrameLayout cell = new FrameLayout(context);
            cell.setClipChildren(false);
            cell.setClipToPadding(false);
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

        GridLayout.LayoutParams rightLp = new GridLayout.LayoutParams(
                GridLayout.spec(1, 5, GridLayout.FILL),
                GridLayout.spec(3, 1, GridLayout.FILL, 1f));
        rightLp.width = 0;
        rightLp.height = 0;
        rightColumn.setLayoutParams(rightLp);
        keypad.addView(rightColumn, 0);
    }
}
