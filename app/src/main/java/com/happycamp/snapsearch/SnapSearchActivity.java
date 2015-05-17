package com.happycamp.snapsearch;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.Toast;

import com.happycamp.snapsearch.custom_views.web_view.SnapSearchWebView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SnapSearchActivity extends Activity {

    private final String TAG = SnapSearchActivity.class.getSimpleName();
    private final static int CAMERA_RESULTCODE=1;
    private final static int FILECHOOSER_RESULTCODE=2;

    private View mWrapperSearch, mLoadingBar;
    private SnapSearchWebView mWebView;
    private ValueCallback<Uri[]> mFilePathCallback;     // for lollipop +
    private ValueCallback<Uri> mUploadMessage;          // for pre-lollipop
    private Uri mCapturedImageUri;
    private boolean performingLoad = false;
    private boolean initialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingBar = findViewById(R.id.loading_bar);
        mWrapperSearch = findViewById(R.id.wrapper_search);
        mWebView = (SnapSearchWebView) findViewById(R.id.my_web_view);

        /* SEARCH VIA URL */
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!performingLoad)
                    mWebView.searchByUrl();
            }
        });

        /* SEARCH VIA IMAGE FILE */
        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!performingLoad)
                    mWebView.searchByImage();
            }
        });
    }

    /**
     * Without this we were having buggy issues having the app perform more than once
     */
    @Override
    public void onResume(){
        super.onResume();
        mWebView.clearCache(true);
        if(!initialLoad)
            hideLoadingBar();
    }

    @Override
    public void onBackPressed(){
        if(mWebView.getVisibility() == View.VISIBLE)
            hideWebView();
        else
            super.onBackPressed();
    }

    public void showWebView(){
        mWrapperSearch.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
    }

    private void hideWebView(){
        mWrapperSearch.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);
    }

    public void showLoadingBar(){
        mLoadingBar.setVisibility(View.VISIBLE);
        performingLoad = true;
    }

    public void hideLoadingBar(){
        mLoadingBar.setVisibility(View.GONE);
        performingLoad = false;
    }


    /**
     * Will create our intent that will provide an Image Chooser - Post Lollipop (5.0)
     * @param filePathCallback
     */
    public void openImageChooserIntentPostLollipop(ValueCallback<Uri[]> filePathCallback){
        mFilePathCallback = filePathCallback;
        openImageChooserIntent(filePathCallback);
    }

    /**
     * Will create our intent that will provide an Image Chooser - Pre Lollipop (5.0)
     * NOTE: Not working for KitKat (4.3). Apparently a Cordova Plugin will fix this?
     *
     * @param uploadMessage
     */
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

        showLoadingBar();

        // Set up the take picture intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                File photoFile = createImageFile();
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra("SnapSearch",  "file:" + photoFile.getAbsolutePath());
                    mCapturedImageUri = Uri.fromFile(photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageUri);
                } else {
                    takePictureIntent = null;
                }
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Unable to create Image File", ex);
            }
        }

        // Set up the intent to get an existing image
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        // Set up the intents for the Intent chooser
        Intent[] intentArray = null;
        if(takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        if(intentArray != null)
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


    /**
     * Return here when file selected from camera or from SDcard
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if(requestCode == CAMERA_RESULTCODE || requestCode==FILECHOOSER_RESULTCODE)
        {
            Uri result;
            try {
                if (resultCode != RESULT_OK ||
                        (requestCode == CAMERA_RESULTCODE && ((intent == null || intent.getData() == null) && mCapturedImageUri == null))) {
                    hideLoadingBar();
                    return;
                }

                result = (intent != null && intent.getData() != null)? intent.getData(): mCapturedImageUri;

                if(mFilePathCallback != null) {
                    Uri[] results = result == null? null: new Uri[1];
                    if(results != null)
                        results[0] = result;
                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                }
                else if(mUploadMessage != null) {
                    this.mUploadMessage.onReceiveValue(result);
                    this.mUploadMessage = null;
                }
                else
                    showSearchResults(null);
                //searchByImage(mWebView);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called from our SnapSearchWebViewClient once it is done parsing
     *
     * @param searchResultImageUrls
     */
    public void showSearchResults(String[] searchResultImageUrls){
        if(searchResultImageUrls == null)
            Toast.makeText(this, "Unable to match your search results, please try again.", Toast.LENGTH_LONG).show();
        else{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if(fragmentTransaction.isEmpty())
                fragmentTransaction.add(R.id.frame_search_results, SnapSearchResultFragment.newInstance(searchResultImageUrls), "Search Results");
            else
                fragmentTransaction.replace(R.id.frame_search_results, SnapSearchResultFragment.newInstance(searchResultImageUrls), "Search Results");
            fragmentTransaction.commit();
        }

        hideLoadingBar();
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
