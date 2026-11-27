package io.github.qxdskyline.sweet;

import android.content.res.AssetManager;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.util.Collections;
import java.util.regex.Pattern;

final class LocalAssetClient extends WebViewClient {
    private static final String HOST = "sweet.local";
    static final String ENTRY_URL = "https://" + HOST + "/index.html";

    // Restrict the settings bridge to pages served from APK assets.
    private static final Pattern ASSET_PATH =
        Pattern.compile("/(?:[a-z0-9_-]+/)*[a-z0-9_-]+\\.(?:html|css|js|json)");

    private final AssetManager assets;

    LocalAssetClient(AssetManager assets) {
        this.assets = assets;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return !isLocal(request.getUrl());
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();
        String path = uri.getEncodedPath();
        if (isLocal(uri) && path != null && ASSET_PATH.matcher(path).matches()) {
            try {
                return new WebResourceResponse(mimeType(path), "UTF-8",
                    assets.open(path.substring(1)));
            } catch (IOException missing) {
                // Missing assets receive the same local 404 as unsupported paths.
            }
        }
        return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found",
            Collections.emptyMap(), null);
    }

    private static boolean isLocal(Uri uri) {
        return "https".equals(uri.getScheme()) && HOST.equals(uri.getEncodedAuthority());
    }

    private static String mimeType(String path) {
        if (path.endsWith(".js")) return "text/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".json")) return "application/json";
        return "text/html";
    }
}
