package com.happycamp.snapsearch;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.util.prefs.BackingStoreException;

/**
 * Created by Zach on 5/16/2015.
 */
public class SnapSearchResultActivity extends Activity{

    public static final String SEARCH_RESULT_IMAGE_URLS_KEY = "com.happycamp.snapsearch.SEARCH_RESULT_IMAGE_URLS_KEY";

    private String mSearchResultImageUrls[];

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        mSearchResultImageUrls = getIntent().getExtras().getStringArray(SEARCH_RESULT_IMAGE_URLS_KEY);
        if(mSearchResultImageUrls != null){
            ((ListView) findViewById(R.id.list_search_results)).setAdapter(new SearchResultsAdapter());
        }
    }

    private class SearchResultsAdapter extends BaseAdapter{

        private final int NUM_HEADER_IMAGES_TO_EXCLUDE = 1;

        @Override
        public int getCount() {
            return mSearchResultImageUrls.length - NUM_HEADER_IMAGES_TO_EXCLUDE;
        }

        @Override
        public String getItem(int position) {
            return mSearchResultImageUrls[position + NUM_HEADER_IMAGES_TO_EXCLUDE];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.list_search_results_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            Picasso.with(SnapSearchResultActivity.this)
                    .load(getItem(position))
                    .into(viewHolder.searchResultImage);

            return convertView;
        }

        class ViewHolder{
            ImageView searchResultImage;

            public ViewHolder(View convertView){
                searchResultImage = (ImageView) convertView.findViewById(R.id.image_search_result);
            }
        }
    }


}
