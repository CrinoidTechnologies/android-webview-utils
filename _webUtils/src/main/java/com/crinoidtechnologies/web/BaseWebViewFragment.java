package com.crinoidtechnologies.web;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crinoidtechnologies.general.template.fragments.BaseFragment;
import com.crinoidtechnologies.general.utils.ConnectivityUtils;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public abstract class BaseWebViewFragment extends BaseFragment implements BaseWebViewClient.MyBrowserListener, ConnectivityUtils.ConnectivityListener,  BaseWebChromeClient.MyWebChromeClientListener {

    public static final int FILE_CHOOSER_RESULT_CODE = 23132;

    protected static final String TAG = "Main";
    protected ValueCallback<Uri> mUploadMessage;
    protected ValueCallback<Uri[]> mUploadMessageLollipop;

    protected WebView webView;
    protected FrameLayout mContainer;

    protected Boolean exit = false;
    @Nullable
    protected ProgressBar horizontalProgressBar;
    @Nullable
    protected ProgressBar circularProgressBar;
    protected View noInternetLayout;
    protected boolean isLoadingError = false;
    protected String url;


    List<String> invalidUrls = new ArrayList<>();


    @Override
    protected void initView() {
        setView();
        if (noInternetLayout != null) {
            noInternetLayout.setVisibility(View.GONE);
        }

        if (horizontalProgressBar != null) {
            horizontalProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        }

//        if (circularProgressBar != null) {
//            circularProgressBar.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
//        }


        if (webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            //  webView.addJavascriptInterface(new WebAppInterface(this,this), "Android");
            webView.setWebViewClient(new BaseWebViewClient(horizontalProgressBar, circularProgressBar, mContainer, this));
            webView.setWebChromeClient(new BaseWebChromeClient(this, horizontalProgressBar, circularProgressBar, mContainer));
            CookieManager.getInstance().setAcceptCookie(true);
        }

    }

    @Override
    protected void initViewWithData() {
        setUrl();
        loadUrlInWebView();
    }

    /**
     * noInternetLayout = view.findViewById(R.id.no_internet_layout);
     * webView = (WebView) view.findViewById(R.id.webview);
     * mContainer = (FrameLayout) view.findViewById(R.id.webview_container);
     * horizontalProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
     * circularProgressBar = (ProgressBar) view.findViewById(R.id.circularProgressBar);
     */
    protected abstract void setView();

    protected abstract void setUrl();


    protected void loadUrlInWebView() {
        webView.loadUrl(url);
    }


    protected void showMessages(int stringId) {
        Toast.makeText(getActivity(), stringId, Toast.LENGTH_LONG).show();
        // Snackbar.make(mContainer,stringId,Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void onResume() {
        super.onResume();
        ConnectivityUtils.register(getActivity(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ConnectivityUtils.unregister(getActivity(),this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mUploadMessageLollipop == null) return;
                mUploadMessageLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                mUploadMessageLollipop = null;
            } else {
                if (null == mUploadMessage) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 21: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void onTryAgainButtonClick(View view) {
        reloadWebView();
    }

    @Override
    public void onConnectionEstablished() {
        reloadWebView();
    }

    private void reloadWebView() {
        showMessages(R.string.reloading);
        Log.d(TAG, "reloadWebView: " + webView.getUrl());
        webView.reload();

    }

    @Override
    public void onConnectionLost() {
        showMessages(R.string.no_internet_connection);
    }


    public ValueCallback<Uri> getmUploadMessage() {
        return mUploadMessage;
    }

    public void setUploadMessage(ValueCallback<Uri> mUploadMessage) {
        if (this.mUploadMessage != null) {
            this.mUploadMessage = null;
        }
        this.mUploadMessage = mUploadMessage;
    }

    public ValueCallback<Uri[]> getmUploadMessageLollipop() {
        return mUploadMessageLollipop;
    }

    public void setUploadMessageLollipop(ValueCallback<Uri[]> mUploadMessageLollipop) {
        if (this.mUploadMessageLollipop != null) {
            this.mUploadMessageLollipop.onReceiveValue(null);
            this.mUploadMessageLollipop = null;

        }
        this.mUploadMessageLollipop = mUploadMessageLollipop;
    }

    @Override
    public void startFileChooserActivity(Intent chooser, int resultCode) {
        startActivityForResult(chooser, resultCode);
    }

    @Override
    public void onPageStarted(WebView view, String url) {
        isLoadingError = false;

    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

        if (noInternetLayout != null) {
            noInternetLayout.setVisibility(View.VISIBLE);
        }

        isLoadingError = true;
        Log.d(TAG, "onReceivedError: " + failingUrl + "\n" + description + "\n" + errorCode);
        if (errorCode == -2 || description.equals("ERR_NAME_NOT_RESOLVED")) {
            //changeVisibilityOfUrlIcon(View.VISIBLE,true);
            if (!invalidUrls.contains(failingUrl)) {
                invalidUrls.add(failingUrl);
            }
            if (webView.canGoBack()) {
                webView.goBack();
            }
            // showUrlEditDialogBox();
        }
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        horizontalProgressBar.setVisibility(View.GONE);
        if (!isLoadingError && noInternetLayout != null) {
            noInternetLayout.setVisibility(View.GONE);
        }
    }


    public void onHomeButtonClick(View view) {
        webView.loadUrl(url);
    }


    class WebpageLoader extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }


}
