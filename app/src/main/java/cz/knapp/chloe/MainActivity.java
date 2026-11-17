package cz.knapp.chloe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Chloe – obal kolem appky "Domácnost Knappovi" (Claude artifact).
 * Otevře dětský režim přes celou obrazovku. Data, přihlášení a vše ostatní
 * zůstává v Claudovi, takže mobil i ranní souhrn fungují dál.
 */
public class MainActivity extends Activity {

    /** Adresa appky. Na konci #deti = rovnou dětský režim. */
    static final String START_URL = "https://claude.ai/artifact/UtgQeuB5yxGmSwgBU4LsMS#deti";

    /** Domény, které se otevírají uvnitř Chloe (Claude + přihlášení). Ostatní jdou do prohlížeče. */
    private static final String[] INTERNAL_HOSTS = {
            "claude.ai", "claude.com", "anthropic.com", "claudeusercontent.com",
            "accounts.google.com", "google.com", "gstatic.com", "googleusercontent.com",
            "apple.com", "appleid.apple.com"
    };

    private WebView web;
    private boolean showingError = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#D7263D"));
        setContentView(web);
        goFullscreen();

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false); // cinkání hvězdiček
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);
        // Google odmítá přihlášení ve "webview" – schováme tu značku z identifikace prohlížeče.
        s.setUserAgentString(s.getUserAgentString().replace("; wv", ""));

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                if ((scheme.equals("https") || scheme.equals("http")) && isInternal(uri.getHost())) {
                    return false; // necháme uvnitř Chloe
                }
                openExternally(uri);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    showOffline();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
                if (url != null && url.startsWith("http")) showingError = false;
            }
        });

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(START_URL);
        }
    }

    static boolean isInternal(String host) {
        if (host == null) return false;
        host = host.toLowerCase();
        for (String h : INTERNAL_HOSTS) {
            if (host.equals(h) || host.endsWith("." + h)) return true;
        }
        return false;
    }

    private void openExternally(Uri uri) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (ActivityNotFoundException ignored) {
            // nic, čím to otevřít
        }
    }

    private void showOffline() {
        showingError = true;
        String html = "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>body{margin:0;height:100vh;display:flex;flex-direction:column;align-items:center;justify-content:center;"
                + "font-family:sans-serif;color:#fff;text-align:center;background:linear-gradient(160deg,#d7263d,#8e0f22)}"
                + ".s{font-size:90px}h1{margin:10px 0 6px}p{opacity:.85;margin:0 20px 24px}"
                + "a{background:#fff;color:#c8102e;font-weight:700;font-size:20px;padding:14px 28px;border-radius:99px;text-decoration:none}</style>"
                + "</head><body><div class='s'>⭐</div><h1>Chloe je bez internetu</h1>"
                + "<p>Zkontroluj Wi-Fi a zkus to znovu.</p>"
                + "<a href='" + START_URL + "'>Zkusit znovu</a></body></html>";
        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    private void goFullscreen() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            w.setDecorFitsSystemWindows(false);
            WindowInsetsController c = w.getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) goFullscreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
        if (showingError) {
            showingError = false;
            web.loadUrl(START_URL);
        }
    }

    @Override
    protected void onPause() {
        web.onPause();
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (web.canGoBack() && !showingError) {
            web.goBack();
        } else {
            moveTaskToBack(true); // nezavírat, jen schovat
        }
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
        }
        super.onDestroy();
    }
}
