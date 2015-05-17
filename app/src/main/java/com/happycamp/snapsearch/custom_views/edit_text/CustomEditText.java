package com.happycamp.snapsearch.custom_views.edit_text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.happycamp.snapsearch.R;
import com.happycamp.snapsearch.custom_views.color_button.ColorButtonModifier;

/**
 * Created by Zach on 5/17/2015.
 */
public class CustomEditText extends EditText{
    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0);

        try {
            CustomEditTextModifier.addColorToEditText(this,
                    a.getColor(R.styleable.ColorButton_unclickColor, getResources().getColor(android.R.color.darker_gray)));

            //This is application specific
            setTypeface(null, Typeface.BOLD);
        } finally {
            a.recycle();
        }
    }

}
