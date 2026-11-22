package com.example.appmaytinhcamtay;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import android.widget.Button;
import android.widget.TextView;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class KeypadInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(MainActivity.class);

    // ---------- Hàm hỗ trợ ----------

    private void press(String... keys) {
        for (String k : keys) {
            onView(allOf(withText(k), isAssignableFrom(Button.class))).perform(click());
        }
    }

    private void expectExpression(String text) {
        onView(withId(R.id.textExpression)).check(matches(withText(text)));
    }

    private void expectResult(String text) {
        onView(withId(R.id.textResult)).check(matches(withText(text)));
    }

    private String readPercent() {
        AtomicReference<String> ref = new AtomicReference<>("");
        rule.getScenario().onActivity(activity -> {
            TextView tv = activity.findViewById(R.id.textPercent);
            ref.set(tv.getText().toString());
        });
        return ref.get();
    }

    // ---------- Test thao tác phím ----------

    @Test
    public void pressDigits_showsExpression() {
        press("1", "2", "3");
        expectExpression("123");
    }

    @Test
    public void addition_showsResult() {
        press("2", "+", "2", "=");
        expectResult("=4");
    }

    @Test
    public void twoOperatorsInARow_replacesOldOne() {
        press("5", "+", "*", "3");
        expectExpression("5*3");
    }

    @Test
    public void secondDotInSameNumber_isIgnored() {
        press("1", ".", "2", ".", "5");
        expectExpression("1.25");
    }

    @Test
    public void dotAtStart_addsLeadingZero() {
        press(".", "5");
        expectExpression("0.5");
    }

    @Test
    public void operatorFirst_onlyMinusAllowed() {
        press("+", "*");
        expectExpression("");
        press("-", "4");
        expectExpression("-4");
    }

    @Test
    public void backspace_removesLastChar() {
        press("1", "2", "⌫");
        expectExpression("1");
    }

    @Test
    public void clear_emptiesExpressionAndResult() {
        press("2", "+", "2", "=", "Ac");
        expectExpression("");
        expectResult("");
    }

    @Test
    public void divideByZero_showsError() {
        press("5", "/", "0", "=");
        expectResult("Lỗi: chia cho 0");
    }

    @Test
    public void afterEquals_operatorContinuesWithPreviousResult() {
        press("2", "+", "2", "=", "*", "3");
        expectExpression("4*3");
        press("=");
        expectResult("=12");
    }

    @Test
    public void afterEquals_newDigitStartsNewCalculation() {
        press("2", "+", "2", "=", "7");
        expectExpression("7");
        expectResult("");
    }

    @Test
    public void toggleDegRad_changesButtonText() {
        press("deg");
        onView(allOf(withText("rad"), isAssignableFrom(Button.class)))
                .check(matches(isDisplayed()));
    }

    // ---------- Test bộ đếm thời gian ----------

    @Test
    public void timer_startsAtZeroPercent() {
        expectPercentText("0%");
    }

    private void expectPercentText(String text) {
        onView(withId(R.id.textPercent)).check(matches(withText(text)));
    }

    @Test
    public void timer_startsAfterFirstKey() throws InterruptedException {
        press("7");
        Thread.sleep(1500);
        onView(withId(R.id.textPercent)).check(matches(not(withText("0%"))));
    }

    @Test
    public void timer_stopsAfterEquals() throws InterruptedException {
        press("2", "+", "2", "=");
        Thread.sleep(500);
        String first = readPercent();
        Thread.sleep(1500);
        String second = readPercent();
        assertEquals("Bộ đếm phải dừng sau khi bấm =", first, second);
    }

    @Test
    public void timer_resetsAfterClear() {
        press("7", "Ac");
        expectPercentText("0%");
    }

    @Test
    public void timer_timeout_clearsExpression() throws InterruptedException {
        press("7");
        Thread.sleep(11_000); // bộ đếm 10 giây
        expectExpression("");
    }
}
