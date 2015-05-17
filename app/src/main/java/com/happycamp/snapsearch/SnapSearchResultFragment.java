package com.happycamp.snapsearch;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

/**
 * Created by Zach on 5/16/2015.
 */
public class SnapSearchResultFragment extends Fragment {

    private static final String SEARCH_RESULT_IMAGE_URLS_KEY = "com.happycamp.snapsearch.SEARCH_RESULT_IMAGE_URLS_KEY";

    private View mFragment;
    private Button mFooterButton;
    private String mSearchResultImageUrls[];

    public static SnapSearchResultFragment newInstance(String[] searchResultImageUrls){
        Bundle arguments = new Bundle();
        arguments.putStringArray(SEARCH_RESULT_IMAGE_URLS_KEY, searchResultImageUrls);
        SnapSearchResultFragment instance = new SnapSearchResultFragment();
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_search_results, container, false);

        mSearchResultImageUrls = getArguments().getStringArray(SEARCH_RESULT_IMAGE_URLS_KEY);
        if(mSearchResultImageUrls != null){
            ((ListView) mFragment.findViewById(R.id.list_search_results)).setAdapter(new SearchResultsAdapter());
            mFragment.findViewById(R.id.button_footer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SnapSearchActivity) getActivity()).showWebView();
                }
            });
        }
        return mFragment;
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
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_search_results_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            Picasso.with(getActivity())
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
