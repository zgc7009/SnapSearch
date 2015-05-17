package com.happycamp.snapsearch;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;

/**
 * Created by Zach on 5/16/2015.
 */
public class SnapSearchWebViewClient extends WebViewClient {

    private SnapSearchActivity mActivity;
    private boolean overridingUrlLoadForSearchResult = false;

    public SnapSearchWebViewClient(SnapSearchActivity activity){
        mActivity = activity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        overridingUrlLoadForSearchResult = true;
        Log.d("URL", "URL is " + url);
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if(overridingUrlLoadForSearchResult) {
            overridingUrlLoadForSearchResult = false;
            new ParseDocumentWithJSOUP().execute(url);
            return;
        }

        view.loadUrl("javascript:(function(){document.getElementById('qbi').click();})()");

        mActivity.hideLoadingBar();
    }


    class ParseDocumentWithJSOUP extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... urls) {
            try {
                // TODO need to fix this process so we can pull individual image results/responses from the url load.
                // This may go in shouldOverrideResult
                Document document = Jsoup.parse(new URL(urls[0]), 5000);
                Elements images =  document.select("img");
                String imageSrc[] = new String[images.size()];
                boolean noNullUrls = true;
                for(int i = 0; i < images.size(); i++){
                    String src = images.get(i).absUrl("src");
                    String attrSrc = images.get(i).attr("abs:src");
                    imageSrc[i] = (src != null && !src.equals(""))? src: (attrSrc != null && !attrSrc.equals(""))? attrSrc: null;
                    if(imageSrc[i] == null)
                        noNullUrls = false;
                }

                if(noNullUrls)
                    return imageSrc;
            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] imageResultUrls) {
            mActivity.showSearchResults(imageResultUrls);
        }
    }
}
