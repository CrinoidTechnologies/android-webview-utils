package com.crinoidtechnologies.web;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

/**
 * Created by ${Vivek} on 3/31/2016 for Likeadish.Be careful
 */
public class BaseWebChromeClient extends WebChromeClient {

    public interface MyWebChromeClientListener {
        void setUploadMessage(ValueCallback<Uri> uploadMsg);
        void setUploadMessageLollipop(ValueCallback<Uri[]> filePathCallback);
        void startFileChooserActivity(Intent chooser, int resultCode);
    }

    private MyWebChromeClientListener listener;
    private ProgressBar progressBar;
    private View circularProgressBar;
    private FrameLayout container;
    private int hideProgressAtPercentage = 30;


    public BaseWebChromeClient(MyWebChromeClientListener listener, ProgressBar progressBar, View circularProgressBar, FrameLayout container) {
        this.listener = listener;
        this.progressBar = progressBar;
        this.circularProgressBar = circularProgressBar;
        this.container = container;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (progressBar != null) {
            progressBar.setProgress(newProgress);
        }
       // Log.d(TAG, "onProgressChanged() called with: view = [" + view + "], newProgress = [" + newProgress + "]");
        if (newProgress > hideProgressAtPercentage) {
            if (circularProgressBar != null) {
                circularProgressBar.setVisibility(View.GONE);
            }
        }
    }

    //The undocumented magic method override
    //Eclipse will swear at you if you try to put @Override here
    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        // Log.d(TAG, "openFileChooser: ...................................");
        listener.setUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        listener.startFileChooserActivity(Intent.createChooser(i, "File Chooser"), BaseWebViewActivity.FILE_CHOOSER_RESULT_CODE);

    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        // Log.d(TAG, "openFileChooser: ...........2........................");

        listener.setUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        listener.startFileChooserActivity(Intent.createChooser(i, "File Browser"), BaseWebViewActivity.FILE_CHOOSER_RESULT_CODE);
    }

    //For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        //  Log.d(TAG, "openFileChooser: ................3...................");

        listener.setUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        listener.startFileChooserActivity(Intent.createChooser(i, "File Chooser"), BaseWebViewActivity.FILE_CHOOSER_RESULT_CODE);

    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {
        WebView mWebviewPop = new WebView(view.getContext());
        mWebviewPop.setVerticalScrollBarEnabled(false);
        mWebviewPop.setHorizontalScrollBarEnabled(false);
        mWebviewPop.setWebViewClient(new BaseWebViewClient(progressBar, circularProgressBar, container, null));
        mWebviewPop.getSettings().setJavaScriptEnabled(true);
        mWebviewPop.getSettings().setSavePassword(false);
        mWebviewPop.setTag(12);
        mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        container.addView(mWebviewPop);
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(mWebviewPop);
        resultMsg.sendToTarget();

        return true;
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

        listener.setUploadMessageLollipop(filePathCallback);
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = fileChooserParams.createIntent();
        }
        try {
            listener.startFileChooserActivity(intent, BaseWebViewActivity.FILE_CHOOSER_RESULT_CODE);
        } catch (ActivityNotFoundException e) {
            listener.setUploadMessageLollipop(null);
            Toast.makeText(container.getContext(), "Cannot open file chooser", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        Log.d(TAG, "onJsAlert: ");
        final JsResult finalJsResult = result;
        new AlertDialog.Builder(view.getContext()).setMessage(message).setTitle(android.R.string.dialog_alert_title).setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finalJsResult.confirm();
            }
        }).setCancelable(false).create().show();
        return true;
        // return super.onJsAlert(view, url, message, result);
    }

    public int getHideProgressAtPercentage() {
        return hideProgressAtPercentage;
    }

    public void setHideProgressAtPercentage(int hideProgressAtPercentage) {
        this.hideProgressAtPercentage = hideProgressAtPercentage;
    }
}

