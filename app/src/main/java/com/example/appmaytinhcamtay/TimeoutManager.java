package com.example.appmaytinhcamtay;

import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TimeoutManager {
    private CountDownTimer countDownTimer;
    private final ProgressBar progressBar;
    private final TextView textPercent;
    private final Runnable onTimeout;

    public TimeoutManager(ProgressBar progressBar, TextView textPercent, Runnable onTimeout) {
        this.progressBar = progressBar;
        this.textPercent = textPercent;
        this.onTimeout = onTimeout;
    }

    public void start() {
        stop();
        if (progressBar != null) progressBar.setProgress(0);
        if (textPercent != null) textPercent.setText("0%");

        countDownTimer = new CountDownTimer(10000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsed = 10000 - millisUntilFinished;
                int percent = (int) ((elapsed * 100) / 10000);
                if (percent > 100) percent = 100;
                if (progressBar != null) progressBar.setProgress(percent);
                if (textPercent != null) textPercent.setText(percent + "%");
            }

            @Override
            public void onFinish() {
                if (progressBar != null) progressBar.setProgress(100);
                if (textPercent != null) textPercent.setText("100%");
                if (onTimeout != null) onTimeout.run();
                reset();
            }
        }.start();
    }

    public void stop() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void reset() {
        stop();
        if (progressBar != null) progressBar.setProgress(0);
        if (textPercent != null) textPercent.setText("0%");
    }
}
