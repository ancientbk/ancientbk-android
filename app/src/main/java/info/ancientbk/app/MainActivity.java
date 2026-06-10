package info.ancientbk.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://ancientbk.info/";
    private WebView webView;
    private ProgressBar progressBar;
    private TextView errorView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        errorView = new TextView(this);

        errorView.setText("Нет соединения. Нажмите, чтобы попробовать снова.");
        errorView.setTextSize(18);
        errorView.setGravity(android.view.Gravity.CENTER);
        errorView.setVisibility(View.GONE);
        errorView.setOnClickListener(v -> webView.loadUrl(HOME_URL));

        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        root.addView(errorView, new FrameLayout.LayoutParams(-1, -1));
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(-1, 8);
        progressParams.gravity = android.view.Gravity.TOP;
        root.addView(progressBar, progressParams);
        setContentView(root);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        webView.setInitialScale(1);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString(s.getUserAgentString() + " AncientBKAndroidApp/1.0");

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setHorizontalScrollBarEnabled(false);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                errorView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectAutoFitScript(view);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (failingUrl != null && failingUrl.equals(view.getUrl())) showError();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        if (savedInstanceState == null) webView.loadUrl(HOME_URL);
        else webView.restoreState(savedInstanceState);
    }

    private void injectAutoFitScript(WebView view) {
        String js = "javascript:(function(){" +
                "function fit(){" +
                "var head=document.head||document.getElementsByTagName('head')[0];" +
                "var meta=document.querySelector('meta[name=viewport]');" +
                "if(!meta){meta=document.createElement('meta');meta.name='viewport';head.appendChild(meta);}" +
                "meta.setAttribute('content','width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no');" +
                "var style=document.getElementById('ancientbk_android_fit_style');" +
                "if(!style){style=document.createElement('style');style.id='ancientbk_android_fit_style';head.appendChild(style);}" +
                "style.innerHTML='html,body{margin:0!important;padding:0!important;overflow-x:hidden!important;} img,table,iframe,object,embed{max-width:100%!important;} input,select,textarea,button{font-size:16px!important;}';" +
                "document.body.style.transformOrigin='0 0';" +
                "document.body.style.webkitTransformOrigin='0 0';" +
                "document.body.style.zoom='';" +
                "var w=Math.max(document.documentElement.scrollWidth,document.body.scrollWidth,document.documentElement.offsetWidth,document.body.offsetWidth);" +
                "var vw=window.innerWidth||document.documentElement.clientWidth;" +
                "if(w>vw&&vw>0){var z=vw/w;document.body.style.zoom=z;}" +
                "}" +
                "fit();setTimeout(fit,300);setTimeout(fit,1000);window.addEventListener('resize',fit);" +
                "})()";
        view.evaluateJavascript(js, null);
    }

    private void showError() {
        webView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
