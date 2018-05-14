package com.crinoidtechnologies.web;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crinoidtechnologies.general.utils.ConnectivityUtils;
import com.crinoidtechnologies.web.R;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseWebViewActivity extends AppCompatActivity implements BaseWebViewClient.MyBrowserListener, ConnectivityUtils.ConnectivityListener,  BaseWebChromeClient.MyWebChromeClientListener {

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
    protected View circularProgressBar;
    protected View noInternetLayout;
    protected boolean isLoadingError = false;
    protected String url;
    protected BaseWebChromeClient chromeClient;

    List<String> invalidUrls = new ArrayList<>();
    private FilePickerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setView(savedInstanceState);
        setUrl();

        if (noInternetLayout != null) {
            noInternetLayout.setVisibility(View.GONE);
        }

        if (horizontalProgressBar != null) {
            horizontalProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (circularProgressBar != null) {
            //circularProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            //  webView.addJavascriptInterface(new WebAppInterface(this,this), "Android");
            webView.setWebViewClient(getWebViewClient());
            chromeClient = new BaseWebChromeClient(this, horizontalProgressBar, circularProgressBar, mContainer);
            webView.setWebChromeClient(chromeClient);
            CookieManager.getInstance().setAcceptCookie(true);
        }

        loadUrlInWebView();

    }

    protected WebViewClient getWebViewClient() {
        return new BaseWebViewClient(horizontalProgressBar, circularProgressBar, mContainer, this);
    }

    /**
     * noInternetLayout = findViewById(R.id.no_internet_layout);
     * webView = (WebView) findViewById(R.id.webview);
     * mContainer = (FrameLayout) findViewById(R.id.webview_container);
     * horizontalProgressBar = (ProgressBar) findViewById(R.id.progressBar);
     * circularProgressBar = (ProgressBar) findViewById(R.id.progressBar);
     */
    protected abstract void setUrl();

    /**
     * setContentView(R.layout.activity_s);
     *
     * @param savedInstanceState
     */
    protected abstract void setView(Bundle savedInstanceState);

    void showMessages(int stringId) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show();
    }

    protected void loadUrlInWebView() {
        webView.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityUtils.register(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConnectivityUtils.unregister(getApplicationContext(),this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
        webView = null;
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {

            showMessages(R.string.exit_app);

            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
            //  changeVisibilityOfUrlIcon(View.VISIBLE,true);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
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

//    private void openFileSelectionDialog() {
//
//        if (null != dialog && dialog.isShowing()) {
//            dialog.dismiss();
//        }
//
//        //Create a DialogProperties object.
//        final DialogProperties properties = new DialogProperties();
//
//        //Instantiate FilePickerDialog with Context and DialogProperties.
//        dialog = new FilePickerDialog(getActivity(), properties);
//        dialog.setTitle("Select a File");
//        dialog.setPositiveBtnName("Select");
//        dialog.setNegativeBtnName("Cancel");
//        properties.selection_mode = DialogConfigs.MULTI_MODE; // for multiple files
////        properties.selection_mode = DialogConfigs.SINGLE_MODE; // for single file
//        properties.selection_type = DialogConfigs.FILE_SELECT;
//
//        //Method handle selected files.
//        dialog.setDialogSelectionListener(new DialogSelectionListener() {
//            @Override
//            public void onSelectedFilePaths(String[] files) {
//                results = new Uri[files.length];
//                for (int i = 0; i < files.length; i++) {
//                    String filePath = new File(files[i]).getAbsolutePath();
//                    if (!filePath.startsWith("file://")) {
//                        filePath = "file://" + filePath;
//                    }
//                    results[i] = Uri.parse(filePath);
//                    Log.d(LOG_TAG, "file path: " + filePath);
//                    Log.d(LOG_TAG, "file uri: " + String.valueOf(results[i]));
//                }
//                mUploadMessage.onReceiveValue(results);
//                mUploadMessage = null;
//            }
//        });
//        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//                if (null != mUploadMessage) {
//                    if (null != results && results.length >= 1) {
//                        mUploadMessage.onReceiveValue(results);
//                    } else {
//                        mUploadMessage.onReceiveValue(null);
//                    }
//                }
//                mUploadMessage = null;
//            }
//        });
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                if (null != mUploadMessage) {
//                    if (null != results && results.length >= 1) {
//                        mUploadMessage.onReceiveValue(results);
//                    } else {
//                        mUploadMessage.onReceiveValue(null);
//                    }
//                }
//                mUploadMessage = null;
//            }
//        });
//
//        dialog.show();
//
//    }

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

    @Override
    public void startActivity(WebView view, Intent intent) {
        startActivity(intent);
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
