package com.happycamp.snapsearch.custom_views.color_button;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.happycamp.snapsearch.R;


/**
 * Created by Zach on 3/1/2015.
 */
public class ColorButton extends Button{
    public ColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorButton, 0, 0);

        try {
            ColorButtonModifier.addColorToButton(this,
                    a.getColor(R.styleable.ColorButton_unclickColor, getResources().getColor(android.R.color.darker_gray)),
                    a.getColor(R.styleable.ColorButton_clickColor, getResources().getColor(android.R.color.tertiary_text_dark)));
        } finally {
            a.recycle();
        }
    }
}