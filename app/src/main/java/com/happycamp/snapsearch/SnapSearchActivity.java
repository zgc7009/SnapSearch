package com.happycamp.snapsearch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SnapSearchActivity extends Activity {

    private final String TAG = SnapSearchActivity.class.getSimpleName();
    private final static int CAMERA_RESULTCODE=1;
    private final static int FILECHOOSER_RESULTCODE=2;


    private WebView mWebView;
    private ValueCallback<Uri[]> mFilePathCallback;     // for lollipop +
    private ValueCallback<Uri> mUploadMessage;          // for pre-lollipop
    private String mCameraPhotoPath;
    private Uri mCapturedImageUri = null;

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
        mWebView.setWebViewClient(new SnapSearchWebViewClient(this));

        /* WEB CHROME CLIENT */
        // You can create external class extends with WebChromeClient
        // Taking WebViewClient as inner class
        // we will define openFileChooser for select file from camera or sdcard
        mWebView.setWebChromeClient(new SnapSearchWebChromeClient(this));

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
            Toast.makeText(SnapSearchActivity.this, "Need to enter a valid image URL before searching", Toast.LENGTH_SHORT).show();
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

    public void openImageChooserIntentPostLollipop(ValueCallback<Uri[]> filePathCallback){
        mFilePathCallback = filePathCallback;
        openImageChooserIntent(filePathCallback);
    }

    public void openImageChooserIntentPreLollipop(ValueCallback<Uri> uploadMessage){
        mUploadMessage = uploadMessage;
        openImageChooserIntent(uploadMessage);
    }

    /**
     * Will create our intent that will provide an Image Chooser.
     */
    private <T> void openImageChooserIntent(ValueCallback<T> filePathCallback){
        if(filePathCallback == null)
            return;

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

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri[] results = result == null? null: new Uri[1];
                    if(results != null)
                        results[0] = result;
                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                }
                else {
                    this.mUploadMessage.onReceiveValue(result);
                    this.mUploadMessage = null;
                }
                //searchByImage(mWebView);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showSearchResults(String[] searchResultImageUrls){
        if(searchResultImageUrls == null) {
            Toast.makeText(this, "Unable to match your search results, please try again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SnapSearchActivity.class));
        }
        else{
            Intent searchResults = new Intent(this, SnapSearchResultActivity.class);
            searchResults.putExtra(SnapSearchResultActivity.SEARCH_RESULT_IMAGE_URLS_KEY, searchResultImageUrls);
            startActivity(searchResults);
        }

        finish();
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
