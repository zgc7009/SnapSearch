package com.happycamp.snapsearch.custom_views.web_view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.happycamp.snapsearch.R;
import com.happycamp.snapsearch.SnapSearchActivity;

/**
 * Created by Zach on 5/17/2015.
 */
public class SnapSearchWebView extends WebView{
    private static final boolean CACHE_WEB_VIEW = false;

    public class InvalidActivityException extends Exception{
        @Override
        public String getMessage() {
            return "SnapSearchWebView must be in an Activity that extends SnapSearchActivity";
        }
    }

    private SnapSearchActivity mActivity;
    private WebSettings mWebSettings;

    public SnapSearchWebView(Context context) {
        super(context);
        init();
    }

    public SnapSearchWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void setActivityBasedOnContext() throws InvalidActivityException{
        if(getContext() instanceof SnapSearchActivity)
            mActivity = (SnapSearchActivity) getContext();
        else
            throw new InvalidActivityException();
    }

    private void init(){
        try{
            setActivityBasedOnContext();
        } catch(InvalidActivityException e){
            e.printStackTrace();
        }

        mWebSettings = getSettings();

        if (CACHE_WEB_VIEW && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        else
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebSettings.setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setUseWideViewPort(true);

        loadUrl("https://www.google.com/imghp?client=android-google&hl=en&tab=wi&ei=7V6gVI-LDI3mggS0oIHADA&ved=0CAQQqi4oAg");

         /* WEB VIEW CLIENT */
        setWebViewClient(new SnapSearchWebViewClient(mActivity));

        /* WEB CHROME CLIENT */
        // You can create external class extends with WebChromeClient
        // Taking WebViewClient as inner class
        // we will define openFileChooser for select file from camera or sdcard
        setWebChromeClient(new SnapSearchWebChromeClient(mActivity));
    }

    /**
     * When we click the button to search by URL, this method will call the appropriate sequence of javascript (that I manually pulled out of Google's search page) to
     * allow us to perform a search-by-image via Google for the corresponding URL.
     */
    public void searchByUrl(){

        loadUrl("javascript:(function(){l=document.getElementById('qbug');l.style.display = 'block';})()");
        loadUrl("javascript:(function(){l=document.getElementById('qbig');l.style.display = 'none';})()");

        String imageUrl = ((EditText) findViewById(R.id.edit_text_image_url)).getEditableText().toString();

        if(!imageUrl.equals("")) {
            loadUrl("javascript:(function(){l=document.getElementById('qbui');l.value = '" + imageUrl + "';})()");

            String id = "qbbtc";
            loadUrl("javascript:(function(){l=document.getElementById('" + id + "');c=l.getElementsByClassName(\"gbqfb kpbb\");e=document.createEvent('HTMLEvents');" +
                    "e.initEvent('click',true,true);c.item(0).dispatchEvent(e);})()");
        }
        else
            Toast.makeText(mActivity, "Need to enter a valid image URL before searching", Toast.LENGTH_SHORT).show();
    }

    /**
     * When we click the button to search by image, this method will call the appropriate sequence of javascript (that I manually pulled out of Google's search page) to
     * allow us to perform a search-by-image via Google. The big thing here is that this requires a file chooser to be triggered within our WebView so we can
     * determine where that image comes from.
     */
    public void searchByImage(){

        loadUrl("javascript:(function(){l=document.getElementById('qbug');l.style.display = 'none';})()");
        loadUrl("javascript:(function(){l=document.getElementById('qbig');l.style.display = 'block';})()");

        String id = "qbfile";
        loadUrl("javascript:(function(){l=document.getElementById('" + id + "');e=document.createEvent('HTMLEvents');" +
                "e.initEvent('click',true,true);l.dispatchEvent(e);})()");
    }
}
