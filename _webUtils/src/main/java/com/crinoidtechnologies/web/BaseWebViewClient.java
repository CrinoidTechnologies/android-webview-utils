package com.crinoidtechnologies.web;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ${Vivek} on 3/31/2016 for Likeadish.Be careful
 */
public class BaseWebViewClient extends WebViewClient {
    protected ProgressBar progressBar;
    protected View circularProgressBar;
    protected FrameLayout container;
    protected boolean isLoadingError = false;
    protected MyBrowserListener delegate;
    protected Map<String, Boolean> loadedUrls = new HashMap<>();
    public static String hostUrl = "";

    public interface MyBrowserListener {
        void onPageStarted(WebView view, String url);

        void onPageFinished(WebView view, String url);

        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

        void startActivity(WebView view, Intent intent);
    }

    public BaseWebViewClient(ProgressBar progressBar, View circularProgressBar, FrameLayout container, MyBrowserListener delegate) {
        this.progressBar = progressBar;
        this.circularProgressBar = circularProgressBar;
        this.container = container;
        this.delegate = delegate;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("TAG", "shouldOverrideUrlLoading " + url);

        if (url.startsWith("http:") || url.startsWith("https:")) {
            String host = Uri.parse(url).getHost();
            if (host.contains(hostUrl)) {
                View mWebviewPop = container.findViewWithTag(12);
                if (mWebviewPop != null) {
                    mWebviewPop.setVisibility(View.GONE);
                    container.removeView(mWebviewPop);
                }
                return false;
            }
            if (host.equals("m.facebook.com")) {
                return false;
            }
            return false;
        } else if (url.startsWith("tel:")) {
            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            if (delegate != null) {
                delegate.startActivity(view, tel);
            }
            return true;
        } else if (url.startsWith("mailto:")) {
            String body = "Enter your Question, Enquiry or Feedback below:\n\n";
            Intent mail = new Intent(Intent.ACTION_SEND);
            mail.setType("application/octet-stream");
            mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"email address"});
            mail.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            mail.putExtra(Intent.EXTRA_TEXT, body);
            if (delegate != null) {
                delegate.startActivity(view, mail);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        isLoadingError = false;
        if (progressBar != null) {
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
        }
        if (circularProgressBar != null) {
            if (circularProgressBar instanceof ProgressBar) {
                ((ProgressBar) circularProgressBar).setProgress(0);
            }
            circularProgressBar.setVisibility(View.VISIBLE);
        }
        if (delegate != null) {
            delegate.onPageStarted(view, url);
        }

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (circularProgressBar != null) {
            circularProgressBar.setVisibility(View.GONE);
        }
        if (delegate != null) {
            delegate.onPageFinished(view, url);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        // view.loadUrl("about:blank");
        isLoadingError = true;
        if (delegate != null) {
            delegate.onReceivedError(view, errorCode, description, failingUrl);
        }
    }
}
