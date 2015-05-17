package com.happycamp.snapsearch.custom_views.edit_text;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.widget.EditText;

import com.happycamp.snapsearch.R;

/**
 * Created by Zach on 5/17/2015.
 */
public class CustomEditTextModifier {

    public static void addColorToEditText(EditText genericEditText, int backgroundColor) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        Drawable background = getColorAsDrawable(genericEditText.getResources().getDrawable(R.drawable.rounded_button), backgroundColor);
        // set background
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            genericEditText.setBackground(background);
        else
            genericEditText.setBackgroundDrawable(background);
    }

    public static void addColorToEditTextViaResource(EditText genericEditText, int backgroundColor) {
        Drawable background = getColorAsDrawable(genericEditText.getResources().getDrawable(R.drawable.rounded_button),
                genericEditText.getResources().getColor(backgroundColor));
        // set background
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            genericEditText.setBackground(background);
        else
            genericEditText.setBackgroundDrawable(background);
    }

    /**
     * Will add our color selector
     *
     * @param buttonBackground
     * @param colorId
     * @param <T>
     * @return
     */
    private static <T> T getColorAsDrawable(T buttonBackground, int colorId){
        if(buttonBackground instanceof ShapeDrawable)
            ((ShapeDrawable) buttonBackground).getPaint().setColor(colorId);
        else if(buttonBackground instanceof GradientDrawable){
            buttonBackground = (T) new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {colorId, colorId});
            ((GradientDrawable) buttonBackground).setCornerRadius(10);
        }

        return buttonBackground;
    }
}
