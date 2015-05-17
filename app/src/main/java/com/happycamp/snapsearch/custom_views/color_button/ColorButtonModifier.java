package com.happycamp.snapsearch.custom_views.color_button;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.widget.Button;

import com.happycamp.snapsearch.R;


/**
 * Created by Zach on 2/28/2015.
 */
public class ColorButtonModifier {

    public static void addColorToButton(Button genericButton, int unclickColor, int clickColor) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        // unclicked
        Drawable unclicked = getColorAsDrawable(genericButton.getResources().getDrawable(R.drawable.rounded_button), unclickColor);
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, unclicked);
        // clicked
        Drawable clicked = getColorAsDrawable(genericButton.getResources().getDrawable(R.drawable.rounded_button), clickColor);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, clicked);
        // set background
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            genericButton.setBackground(stateListDrawable);
        else
            genericButton.setBackgroundDrawable(stateListDrawable);
    }

    public static void addColorToButtonViaResource(Button genericButton, int unclickColorId, int clickColorId) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        // unclicked
        Drawable unclicked = getColorAsDrawable(genericButton.getResources().getDrawable(R.drawable.rounded_button),
                genericButton.getResources().getColor(unclickColorId));
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, unclicked);
        // clicked
        Drawable clicked = getColorAsDrawable(genericButton.getResources().getDrawable(R.drawable.rounded_button),
                genericButton.getResources().getColor(clickColorId));
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, clicked);
        // set background
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            genericButton.setBackground(stateListDrawable);
        else
            genericButton.setBackgroundDrawable(stateListDrawable);
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