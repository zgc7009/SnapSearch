package com.happycamp.snapsearch.custom_views.web_view;

import android.app.Activity;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.happycamp.snapsearch.SnapSearchActivity;

/**
 * Created by Zach on 5/16/2015.
 */
public class SnapSearchWebChromeClient extends WebChromeClient{

    private SnapSearchActivity mActivity;

    public SnapSearchWebChromeClient(SnapSearchActivity activity){
        mActivity = activity;
    }

    // onShowFileChooser for Android 5.0+
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        mActivity.openImageChooserIntentPostLollipop(filePathCallback);
        return true;
    }

    // openFileChooser for Android 3.0 -> 4.2
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        mActivity.openImageChooserIntentPreLollipop(uploadMsg);
    }

    // openFileChooser for Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "");
    }

    //openFileChooser for other Android versions (not exactly sure what these are though but it covers all my bases)
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        openFileChooser(uploadMsg, acceptType);
    }
}
