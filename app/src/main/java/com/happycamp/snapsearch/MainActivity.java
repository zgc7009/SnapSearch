package com.happycamp.snapsearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();
    private final static int CAMERA_RESULTCODE=1;
    private final static int FILECHOOSER_RESULTCODE=2;
    private final static boolean AUTOMATE_PROCESS = false;


    private WebView mWebView;
    private ValueCallback<Uri[]> mFilePathCallback;     // for lollipop +
    private ValueCallback<Uri> mUploadMessage;          // for pre-lollipop
    private String mCameraPhotoPath;
    private Uri mCapturedImageUri = null;
    int numRedirects = 0;
    private Runnable waitForLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.my_web_view);
        final WebSettings webSettings = mWebView.getSettings();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        mWebView.loadUrl("https://www.google.com/imghp?client=android-google&hl=en&tab=wi&ei=7V6gVI-LDI3mggS0oIHADA&ved=0CAQQqi4oAg");
        
        /* WEB VIEW CLIENT */
        mWebView.setWebViewClient(createWebView());

        /* WEB CHROME CLIENT */
        // You can create external class extends with WebChromeClient
        // Taking WebViewClient as inner class
        // we will define openFileChooser for select file from camera or sdcard
        mWebView.setWebChromeClient(createWebChromeClient());

        /* SEARCH VIA URL */
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchByUrl(mWebView);
            }
        });

        /* SEARCH VIA IMAGE FILE */
        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchByImage(mWebView);
            }
        });

    }

    /**
     * Will provide a web view that will handle the WebView lifecycle calls. This will automate us through to the
     * search by image page on Google via Google's javascript.
     *
     * @return
     */
    private WebViewClient createWebView(){
        return new WebViewClient() {

            private boolean overridingUrlLoadForSearchResult = false;

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
                    return;
                }

                view.loadUrl("javascript:(function(){document.getElementById('qbi').click();})()");
                numRedirects++;

                if(AUTOMATE_PROCESS) {
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (numRedirects == 1) {
                                numRedirects++;
                                mWebView.loadUrl("javascript:(function(){l=document.getElementById('qbug');l.style.display = 'none'");
                                mWebView.loadUrl("javascript:(function(){l=document.getElementById('qbig');l.style.display = 'block'");
                                mWebView.invalidate();
                                this.postDelayed(waitForLoad, 1000);
                            } else if (numRedirects == 2) {
                                numRedirects++;
                                mWebView.loadUrl("javascript:(function(){l=document.getElementById('qbfile');e=document.createEvent('HTMLEvents');" +
                                        "e.initEvent('click',true,true);l.dispatchEvent(e);})()");
                                findViewById(R.id.loading_bar).setVisibility(View.GONE);
                            }
                        }
                    };

                    waitForLoad = new Runnable() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(0);
                        }
                    };
                    handler.postDelayed(waitForLoad, 1000);
                }
                else
                    findViewById(R.id.loading_bar).setVisibility(View.GONE);
            }
        };
    }

    /**
     * Will provide a default client for Chrome, this will tell us what to do when the file chooser is triggered. In our case, it will
     * trigger the Image Chooser intent. NOTE - This is OS dependent
     *
     * @return
     */
    private WebChromeClient createWebChromeClient(){
        return new WebChromeClient() {
            // onShowFileChooser for Android 5.0+
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (filePathCallback != null) {
                    mFilePathCallback = filePathCallback;
                }

                openImageChooserIntent();
                return true;
            }

            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                onShowFileChooser(mWebView, null, null);
            }

            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            //openFileChooser for other Android versions (not exactly sure what these are though but it covers all my bases)
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
        };
    }

    /**
     * When we click the button to search by URL, this method will call the appropriate sequence of javascript (that I manually pulled out of Google's search page) to
     * allow us to perform a search-by-image via Google for the corresponding URL.
     *
     * @param mWebView
     */
    private void searchByUrl(WebView mWebView){
        mWebView.loadUrl("javascript:(function(){l=document.getElementById('qbug');l.style.display = 'block';})()");
        mWebView.loadUrl("javascript:(function(){l=document.getElementById('qbig');l.style.display = 'none';})()");

        String imageUrl = ((EditText) findViewById(R.id.edit_text_image_url)).getEditableText().toString();

        if(!imageUrl.equals("")) {
            mWebView.loadUrl("javascript:(function(){l=document.getElementById('qbui');l.value = '" + imageUrl + "';})()");

            String id = "qbbtc";
            mWebView.loadUrl("javascript:(function(){l=document.getElementById('" + id + "');c=l.getElementsByClassName(\"gbqfb kpbb\");e=document.createEvent('HTMLEvents');" +
                    "e.initEvent('click',true,true);c.item(0).dispatchEvent(e);})()");

            findViewById(R.id.input_fields).setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        }
        else
            Toast.makeText(MainActivity.this, "Need to enter a valid image URL before searching", Toast.LENGTH_SHORT).show();
    }

    /**
     * When we click the button to search by image, this method will call the appropriate sequence of javascript (that I manually pulled out of Google's search page) to
     * allow us to perform a search-by-image via Google. The big thing here is that this requires a file chooser to be triggered within our WebView so we can
     * determine where that image comes from. That is what the Web
     *
     * @param mWebView
     */
    private void searchByImage(WebView mWebView){
        mWebView.loadUrl("javascript:(function(){l=document.getElementById('qbug');l.style.display = 'none';})()");
        mWebView.loadUrl("javascript:(function(){l=document.getElementById('qbig');l.style.display = 'block';})()");

        String id = "qbfile";
        mWebView.loadUrl("javascript:(function(){l=document.getElementById('" + id + "');e=document.createEvent('HTMLEvents');" +
                "e.initEvent('click',true,true);l.dispatchEvent(e);})()");
    }

    /**
     * Will create our intent that will provide an Image Chooser.
     */
    private void openImageChooserIntent(){

        // Set up the take picture intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("SnapSearch", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Unable to create Image File", ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }

        // Set up the intent to get an existing image
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        // Set up the intents for the Intent chooser
        Intent[] intentArray;
        if(takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }


    // Return here when file selected from camera or from SDcard
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if(requestCode == CAMERA_RESULTCODE || requestCode==FILECHOOSER_RESULTCODE)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Uri[] results = new Uri[1];

                try {
                    if (resultCode != RESULT_OK) {
                        results = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        results[0] = intent == null ? mCapturedImageUri : intent.getData();
                        findViewById(R.id.input_fields).setVisibility(View.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                    }

                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                    //searchByImage(mWebView);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Uri result;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = intent == null ? mCapturedImageUri : intent.getData();
                        findViewById(R.id.input_fields).setVisibility(View.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                    }

                    this.mUploadMessage.onReceiveValue(result);
                    this.mUploadMessage = null;
                    searchByImage(mWebView);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
