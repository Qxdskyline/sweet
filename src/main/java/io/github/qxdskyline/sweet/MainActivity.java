package io.github.qxdskyline.sweet;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.window.OnBackInvokedDispatcher;

public final class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout content = new FrameLayout(this);
        content.setBackgroundColor(0xfffcf5ed);
        if (Build.VERSION.SDK_INT >= 35) {
            content.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }

        webView = new WebView(this);
        webView.setBackgroundColor(0xfffcf5ed);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.addJavascriptInterface(new SettingsBridge(this), SettingsBridge.JS_NAME);
        webView.setWebViewClient(new LocalAssetClient(getAssets()));
        content.addView(webView);
        setContentView(content);

        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::handleBack);
        }

        if (state == null || webView.restoreState(state) == null) {
            webView.loadUrl(LocalAssetClient.ENTRY_URL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        webView.saveState(state);
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }

    private void handleBack() {
        String url = webView.getUrl();
        String path = url == null ? "" : android.net.Uri.parse(url).getPath();
        if ("/settings/index.html".equals(path) || "/settings/particles.html".equals(path)) {
            // Use the same return target as the settings page button.
            webView.evaluateJavascript("document.getElementById('return').click()", null);
            return;
        }
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

}
