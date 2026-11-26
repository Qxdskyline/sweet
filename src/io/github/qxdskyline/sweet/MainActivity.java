package io.github.qxdskyline.sweet;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class MainActivity extends Activity {
    private static final int PAPER = 0xfff8f3ed;
    private static final int CREAM = 0xfffffaf5;
    private static final int INK = 0xff382930;
    private static final int MUTED = 0xff6e555b;
    private static final int PLUM = 0xff532e3a;
    private static final int ROSE = 0xff874454;
    private static final int PALE = 0xfff1e2de;
    private static final int LINE = 0xffeadbd5;
    private static final int DAYS = 0;
    private static final int FOOD = 1;
    private static final int SETTINGS = 2;
    private static final int MATCH = LinearLayout.LayoutParams.MATCH_PARENT;
    private static final int WRAP = LinearLayout.LayoutParams.WRAP_CONTENT;

    private final Random random = new Random();
    private JSONObject config;
    private SharedPreferences preferences;
    private FrameLayout content;
    private TextView headerAction;
    private TextView daysTab;
    private TextView foodTab;
    private View navigation;
    private ScrollView daysPage;
    private ScrollView foodPage;
    private ScrollView settingsPage;
    private WheelView wheel;
    private ObjectAnimator spin;
    private LocalDate displayedDate;
    private int screen = DAYS;
    private int previousTab = DAYS;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        } else {
            getWindow().setStatusBarColor(PAPER);
            getWindow().setNavigationBarColor(PAPER);
        }
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        if (Build.VERSION.SDK_INT >= 29) getWindow().setNavigationBarContrastEnforced(false);
        preferences = getSharedPreferences("sweet_settings", MODE_PRIVATE);
        config = loadConfig();
        if (state != null) previousTab = state.getInt("selectedTab", DAYS);
        createShell();
        show(previousTab, false);
    }

    @Override protected void onResume() {
        super.onResume();
        if (displayedDate != null && !displayedDate.equals(LocalDate.now())) {
            if (daysPage != null) content.removeView(daysPage);
            daysPage = null;
            if (screen == DAYS) show(DAYS, false);
        }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("selectedTab", screen == SETTINGS ? previousTab : screen);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        if (spin != null) spin.cancel();
        super.onDestroy();
    }

    private JSONObject loadConfig() {
        String saved = preferences.getString("config", "");
        if (!saved.isEmpty()) {
            try {
                return new JSONObject(saved);
            } catch (JSONException ignored) {
                // Recover from an unreadable local settings record using the packaged defaults.
            }
        }
        try (InputStream input = getAssets().open("defaults.json");
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            for (int read; (read = input.read(buffer)) != -1;) output.write(buffer, 0, read);
            return new JSONObject(new String(output.toByteArray(), StandardCharsets.UTF_8));
        } catch (IOException | JSONException error) {
            throw new IllegalStateException("Missing or invalid Sweet defaults", error);
        }
    }

    private void createShell() {
        LinearLayout root = column();
        root.setBackgroundColor(PAPER);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                int keyboard = insets.getInsets(WindowInsets.Type.ime()).bottom;
                view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
            }
            return insets;
        });
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(24), 0, dp(18), 0);
        TextView brand = text("Sweet", 27, PLUM);
        brand.setTypeface(Typeface.create("serif", Typeface.BOLD_ITALIC));
        header.addView(brand, new LinearLayout.LayoutParams(0, WRAP, 1f));
        headerAction = button("设置", 14, PLUM, PALE);
        headerAction.setOnClickListener(view -> {
            if (screen == SETTINGS) closeSettings();
            else show(SETTINGS, true);
        });
        header.addView(headerAction, new LinearLayout.LayoutParams(dp(66), dp(48)));
        root.addView(header, new LinearLayout.LayoutParams(MATCH, dp(72)));
        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        LinearLayout tabs = new LinearLayout(this);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.setPadding(dp(6), dp(6), dp(6), dp(6));
        tabs.setBackground(card(CREAM, 26, LINE));
        daysTab = button("我们", 15, MUTED, CREAM);
        foodTab = button("今天吃什么", 15, MUTED, CREAM);
        daysTab.setOnClickListener(view -> show(DAYS, true));
        foodTab.setOnClickListener(view -> show(FOOD, true));
        tabs.addView(daysTab, new LinearLayout.LayoutParams(0, dp(48), 1));
        tabs.addView(foodTab, new LinearLayout.LayoutParams(0, dp(48), 1));
        LinearLayout.LayoutParams tabLayout = new LinearLayout.LayoutParams(MATCH, WRAP);
        tabLayout.setMargins(dp(22), dp(5), dp(22), dp(9));
        root.addView(tabs, tabLayout);
        navigation = tabs;
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 30) {
            int lightBars = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            getWindow().getInsetsController().setSystemBarsAppearance(lightBars, lightBars);
        }
        root.requestApplyInsets();
    }

    private void show(int target, boolean animate) {
        screen = target;
        if (target != SETTINGS) previousTab = target;
        ScrollView selected;
        if (target == SETTINGS) {
            if (settingsPage == null) settingsPage = createSettingsPage();
            selected = settingsPage;
        } else if (target == FOOD) {
            if (foodPage == null) foodPage = createFoodPage();
            selected = foodPage;
        } else {
            if (daysPage == null) daysPage = createDaysPage();
            selected = daysPage;
        }
        if (selected.getParent() == null) content.addView(selected,
            new FrameLayout.LayoutParams(MATCH, MATCH));
        for (int i = 0; i < content.getChildCount(); i++) {
            View page = content.getChildAt(i);
            page.animate().cancel();
            page.setAlpha(1f);
            page.setTranslationY(0f);
            page.setVisibility(page == selected ? View.VISIBLE : View.GONE);
        }
        if (animate && android.animation.ValueAnimator.areAnimatorsEnabled()) {
            selected.setAlpha(.82f);
            selected.setTranslationY(dp(5));
            selected.animate().alpha(1f).translationY(0f).setDuration(155).start();
        }
        navigation.setVisibility(target == SETTINGS ? View.GONE : View.VISIBLE);
        headerAction.setText(target == SETTINGS ? "返回" : "设置");
        styleTab(daysTab, target == DAYS);
        styleTab(foodTab, target == FOOD);
    }

    private void styleTab(TextView tab, boolean selected) {
        tab.setSelected(selected);
        tab.setTextColor(selected ? Color.WHITE : MUTED);
        tab.setBackground(touch(selected ? PLUM : CREAM, 21));
    }

    @Override public void onBackPressed() {
        if (screen == SETTINGS) closeSettings();
        else if (screen == FOOD) show(DAYS, true);
        else super.onBackPressed();
    }

    private void closeSettings() {
        hideKeyboard();
        if (settingsPage != null) content.removeView(settingsPage);
        settingsPage = null;
        show(previousTab, true);
    }

    private ScrollView createDaysPage() {
        LinearLayout page = pageBody();
        String names = config.optString("myName") + "  &  " + config.optString("herName");
        add(page, text(names, 15, ROSE), 4);
        add(page, title("我们的日子"), 8);
        LocalDate start = LocalDate.parse(config.optString("startDate"));
        displayedDate = LocalDate.now();
        long elapsed = CoupleDate.daysBetween(start, displayedDate);

        LinearLayout hero = column();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(14), dp(28), dp(14), dp(27));
        GradientDrawable surface = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[] {0xff6c3e4b, 0xff492936});
        surface.setCornerRadius(dp(32));
        hero.setBackground(surface);
        add(hero, centered(elapsed < 0 ? "距离开始还有" : "在一起已经", 16, 0xfff9e0d8), 0);
        TextView count = centered(Long.toString(Math.abs(elapsed)), 73, CREAM);
        count.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        count.setIncludeFontPadding(false);
        count.setContentDescription(Math.abs(elapsed) + " 天");
        add(hero, count, 11);
        add(hero, centered("天", 17, 0xfff9e0d8), 8);
        String dateLabel = start.getYear() + " 年 " + start.getMonthValue()
            + " 月 " + start.getDayOfMonth() + " 日";
        add(hero, centered(dateLabel, 14, 0xffffe9de), 20);
        add(page, hero, 21);

        List<String> quotes = strings(config.optJSONArray("sweetLines"));
        if (!quotes.isEmpty()) {
            LinearLayout quoteCard = panel();
            String nickname = config.optString("nickname");
            add(quoteCard, text(nickname.isEmpty() ? "想对你说" : "想对" + nickname + "说", 14, ROSE), 0);
            int[] quote = {(int) Math.floorMod(displayedDate.toEpochDay(), (long) quotes.size())};
            TextView message = text(quotes.get(quote[0]), 19, INK);
            message.setLineSpacing(dp(4), 1f);
            add(quoteCard, message, 10);
            if (quotes.size() > 1) {
                TextView next = button("换一句", 14, ROSE, PALE);
                next.setOnClickListener(view -> {
                    quote[0] = (quote[0] + 1) % quotes.size();
                    message.setText(quotes.get(quote[0]));
                });
                LinearLayout.LayoutParams width = new LinearLayout.LayoutParams(dp(94), dp(48));
                width.topMargin = dp(15);
                quoteCard.addView(next, width);
            }
            add(page, quoteCard, 16);
        }

        JSONArray dates = config.optJSONArray("anniversaries");
        if (dates != null && dates.length() > 0) {
            LinearLayout anniversaryCard = panel();
            add(anniversaryCard, text("纪念日", 17, PLUM), 0);
            for (int i = 0; i < dates.length(); i++) {
                JSONObject entry = dates.optJSONObject(i);
                if (entry == null) continue;
                LocalDate next = CoupleDate.nextAnniversary(entry.optString("date"),
                    entry.optString("type").equals("yearly"), displayedDate);
                String date = next == null ? entry.optString("date") + " · 已过"
                    : next + (next.equals(displayedDate) ? " · 今天" : "");
                add(anniversaryCard, text(entry.optString("name") + "  ·  " + date, 15, MUTED), 10);
            }
            add(page, anniversaryCard, 16);
        }
        return scrolling(page);
    }

    private ScrollView createFoodPage() {
        LinearLayout page = pageBody();
        add(page, title("今天吃什么"), 0);
        LinearLayout panel = panel();
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        List<String> options = strings(config.optJSONArray("foodOptions"));
        FrameLayout wheelFrame = new FrameLayout(this);
        int widthDp = getResources().getConfiguration().screenWidthDp;
        int side = dp(Math.min(340, Math.max(180, widthDp - 90)));
        wheel = new WheelView(this);
        wheel.setOptions(options);
        FrameLayout.LayoutParams wheelSize = new FrameLayout.LayoutParams(side, side, Gravity.CENTER);
        wheelSize.topMargin = dp(10);
        wheelFrame.addView(wheel, wheelSize);
        View pointer = new View(this) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Path triangle = new Path();

            @Override protected void onDraw(Canvas canvas) {
                triangle.rewind();
                triangle.moveTo(0, 0);
                triangle.lineTo(getWidth(), 0);
                triangle.lineTo(getWidth() / 2f, getHeight());
                triangle.close();
                paint.setColor(PLUM);
                canvas.drawPath(triangle, paint);
            }
        };
        pointer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        wheelFrame.addView(pointer, new FrameLayout.LayoutParams(dp(24), dp(21),
            Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        panel.addView(wheelFrame, new LinearLayout.LayoutParams(MATCH, side + dp(20)));
        TextView result = centered("转一次，决定这一餐", 17, MUTED);
        result.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        add(panel, result, 10);
        TextView start = button("开始转", 17, Color.WHITE, PLUM);
        start.setOnClickListener(view -> spinWheel(options, start, result));
        LinearLayout.LayoutParams spinSize = new LinearLayout.LayoutParams(MATCH, dp(54));
        spinSize.topMargin = dp(16);
        panel.addView(start, spinSize);
        add(page, panel, 18);

        TextView edit = button("编辑备选菜单 · " + options.size() + " 项", 15, ROSE, PALE);
        edit.setOnClickListener(view -> {
            show(SETTINGS, true);
            settingsPage.post(() -> {
                if (settingsPage != null) settingsPage.fullScroll(View.FOCUS_DOWN);
            });
        });
        LinearLayout.LayoutParams editSize = new LinearLayout.LayoutParams(MATCH, dp(49));
        editSize.topMargin = dp(13);
        page.addView(edit, editSize);
        return scrolling(page);
    }

    private void spinWheel(List<String> options, TextView start, TextView result) {
        if (spin != null) return;
        WheelView activeWheel = wheel;
        int winner = random.nextInt(options.size());
        float initial = activeWheel.getRotation() % 360f;
        float angle = WheelView.targetAngle(winner, options.size());
        float advance = (angle - initial + 360f) % 360f;
        start.setEnabled(false);
        start.setText("转动中…");
        result.setTextColor(MUTED);
        result.setText("正在决定…");
        activeWheel.setRotation(initial);
        if (!android.animation.ValueAnimator.areAnimatorsEnabled()) {
            activeWheel.setRotation(angle);
            finishSpin(options.get(winner), start, result);
            return;
        }
        spin = ObjectAnimator.ofFloat(activeWheel, View.ROTATION,
            initial, initial + 360f * 4 + advance);
        spin.setDuration(3550);
        spin.setInterpolator(new PathInterpolator(.12f, .68f, .12f, 1f));
        spin.addListener(new android.animation.AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override public void onAnimationCancel(android.animation.Animator animation) {
                cancelled = true;
            }

            @Override public void onAnimationEnd(android.animation.Animator animation) {
                spin = null;
                if (!cancelled) {
                    activeWheel.setRotation(angle);
                    finishSpin(options.get(winner), start, result);
                }
            }
        });
        spin.start();
    }

    private void finishSpin(String choice, TextView start, TextView result) {
        result.setText("今天就吃 " + choice);
        result.setTextColor(PLUM);
        if (screen == FOOD) start.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        start.setText("再转一次");
        start.setEnabled(true);
    }

    private ScrollView createSettingsPage() {
        LinearLayout page = pageBody();
        page.setFocusableInTouchMode(true);
        page.setDescendantFocusability(LinearLayout.FOCUS_BEFORE_DESCENDANTS);
        add(page, title("设置"), 0);
        LinearLayout couple = panel();
        add(couple, text("我们的资料", 18, PLUM), 0);
        EditText myName = field(couple, "我的名字", config.optString("myName"), 1);
        EditText herName = field(couple, "她的名字", config.optString("herName"), 1);
        EditText nickname = field(couple, "昵称", config.optString("nickname"), 1);
        add(couple, text("在一起的日期", 14, MUTED), 14);
        TextView date = button(config.optString("startDate"), 16, PLUM, PALE);
        date.setContentDescription("在一起的日期 " + date.getText());
        date.setOnClickListener(view -> {
            hideKeyboard();
            LocalDate current = LocalDate.parse(date.getText());
            new DatePickerDialog(this, (picker, year, month, day) -> {
                String value = LocalDate.of(year, month + 1, day).toString();
                date.setText(value);
                date.setContentDescription("在一起的日期 " + value);
            }, current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth()).show();
        });
        add(couple, date, 6);
        EditText quotes = field(couple, "想对她说的话 · 每行一句",
            TextUtils.join("\n", strings(config.optJSONArray("sweetLines"))), 3);
        EditText anniversaries = field(couple, "纪念日 · 每行一条",
            formatAnniversaries(config.optJSONArray("anniversaries")), 3);
        add(couple, text("示例　第一次见面 | 03-12 | 每年\n一次性纪念日填完整日期，例如 2025-03-12", 13, MUTED), 7);
        add(page, couple, 17);

        LinearLayout menu = panel();
        add(menu, text("转盘菜单", 18, PLUM), 0);
        EditText options = field(menu, "每行一个菜名 · 2 到 20 项",
            TextUtils.join("\n", strings(config.optJSONArray("foodOptions"))), 4);
        add(page, menu, 14);

        TextView save = button("保存设置", 16, Color.WHITE, PLUM);
        save.setOnClickListener(view -> {
            List<String> foods = lines(options.getText(), true);
            if (foods.size() < 2 || foods.size() > 20) {
                options.setError("请输入 2 到 20 个不同的选项");
                options.requestFocus();
                return;
            }
            JSONArray events;
            try {
                events = parseAnniversaries(anniversaries.getText());
            } catch (IllegalArgumentException error) {
                anniversaries.setError(error.getMessage());
                anniversaries.requestFocus();
                return;
            }
            try {
                JSONObject updated = new JSONObject();
                updated.put("myName", nameOrDefault(myName, "我"));
                updated.put("herName", nameOrDefault(herName, "你"));
                updated.put("nickname", nickname.getText().toString().trim());
                updated.put("startDate", date.getText().toString());
                updated.put("sweetLines", new JSONArray(lines(quotes.getText(), false)));
                updated.put("anniversaries", events);
                updated.put("foodOptions", new JSONArray(foods));
                preferences.edit().putString("config", updated.toString()).apply();
                config = updated;
            } catch (JSONException error) {
                throw new IllegalStateException(error);
            }
            if (spin != null) spin.cancel();
            if (daysPage != null) content.removeView(daysPage);
            if (foodPage != null) content.removeView(foodPage);
            daysPage = null;
            foodPage = null;
            closeSettings();
            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams saveSize = new LinearLayout.LayoutParams(MATCH, dp(54));
        saveSize.topMargin = dp(17);
        page.addView(save, saveSize);
        ScrollView scroll = scrolling(page);
        page.requestFocus();
        return scroll;
    }

    private static String nameOrDefault(EditText field, String fallback) {
        String name = field.getText().toString().trim();
        return name.isEmpty() ? fallback : name;
    }

    private static List<String> strings(JSONArray array) {
        List<String> values = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i).trim();
                if (!value.isEmpty()) values.add(value);
            }
        }
        return values;
    }

    private static List<String> lines(CharSequence input, boolean deduplicate) {
        List<String> values = new ArrayList<>();
        for (String token : input.toString().split("\\R")) {
            String value = token.trim();
            if (!value.isEmpty()) values.add(value);
        }
        return deduplicate ? new ArrayList<>(new LinkedHashSet<>(values)) : values;
    }

    private static String formatAnniversaries(JSONArray array) {
        List<String> values = new ArrayList<>();
        if (array == null) return "";
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.optJSONObject(i);
            if (entry != null) values.add(entry.optString("name") + " | " + entry.optString("date")
                + " | " + (entry.optString("type").equals("yearly") ? "每年" : "一次"));
        }
        return TextUtils.join("\n", values);
    }

    private static JSONArray parseAnniversaries(CharSequence input) {
        JSONArray events = new JSONArray();
        for (String line : lines(input, false)) {
            String[] fields = line.split("\\|", -1);
            if (fields.length != 3 || fields[0].trim().isEmpty()) {
                throw new IllegalArgumentException("每行请填写名称 | 日期 | 每年或一次");
            }
            boolean yearly = fields[2].trim().equals("每年");
            if (!yearly && !fields[2].trim().equals("一次")) {
                throw new IllegalArgumentException("纪念日频率请选择「每年」或「一次」");
            }
            try {
                JSONObject event = new JSONObject();
                event.put("name", fields[0].trim());
                String date = fields[1].trim();
                CoupleDate.nextAnniversary(date, yearly, LocalDate.now());
                event.put("date", date);
                event.put("type", yearly ? "yearly" : "once");
                events.put(event);
            } catch (DateTimeParseException error) {
                throw new IllegalArgumentException("请填写有效日期。每年重复时可用 MM-DD");
            } catch (JSONException error) {
                throw new IllegalStateException(error);
            }
        }
        return events;
    }

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        if (focused != null) {
            getSystemService(InputMethodManager.class).hideSoftInputFromWindow(focused.getWindowToken(), 0);
            focused.clearFocus();
        }
    }

    private EditText field(LinearLayout group, String label, String initial, int rows) {
        TextView caption = text(label, 14, MUTED);
        EditText edit = new EditText(this);
        edit.setId(View.generateViewId());
        caption.setLabelFor(edit.getId());
        add(group, caption, 13);
        edit.setText(initial);
        edit.setTextSize(16);
        edit.setTextColor(INK);
        edit.setPadding(dp(13), dp(10), dp(13), dp(10));
        edit.setBackground(card(PAPER, 13, LINE));
        edit.setInputType(InputType.TYPE_CLASS_TEXT
            | (rows > 1 ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : InputType.TYPE_TEXT_FLAG_CAP_SENTENCES));
        edit.setGravity(Gravity.TOP | Gravity.START);
        edit.setMinLines(rows);
        edit.setMinHeight(dp(48));
        add(group, edit, 6);
        return edit;
    }

    private LinearLayout column() {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        return group;
    }

    private LinearLayout pageBody() {
        LinearLayout body = column();
        body.setPadding(dp(23), dp(9), dp(23), dp(20));
        return body;
    }

    private LinearLayout panel() {
        LinearLayout group = column();
        group.setBackground(card(CREAM, 25, LINE));
        group.setPadding(dp(19), dp(17), dp(19), dp(18));
        return group;
    }

    private ScrollView scrolling(LinearLayout body) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.addView(body, new ScrollView.LayoutParams(MATCH, WRAP));
        return scroll;
    }

    private void add(LinearLayout group, View view, int gap) {
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(MATCH, WRAP);
        layout.topMargin = dp(gap);
        group.addView(view, layout);
    }

    private TextView title(String value) {
        TextView label = text(value, 33, INK);
        label.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        return label;
    }

    private TextView text(String value, int size, int color) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextSize(size);
        label.setTextColor(color);
        return label;
    }

    private TextView centered(String value, int size, int color) {
        TextView label = text(value, size, color);
        label.setGravity(Gravity.CENTER);
        return label;
    }

    private TextView button(String value, int size, int color, int background) {
        Button control = new Button(this);
        control.setText(value);
        control.setTextSize(size);
        control.setTextColor(color);
        control.setAllCaps(false);
        control.setGravity(Gravity.CENTER);
        control.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        control.setPadding(dp(7), 0, dp(7), 0);
        control.setMinimumHeight(dp(48));
        control.setStateListAnimator(null);
        control.setBackground(touch(background, 22));
        return control;
    }

    private RippleDrawable touch(int color, int radius) {
        return new RippleDrawable(ColorStateList.valueOf(0x23954458),
            card(color, radius, 0), card(Color.WHITE, radius, 0));
    }

    private GradientDrawable card(int color, int radius, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        if (stroke != 0) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
