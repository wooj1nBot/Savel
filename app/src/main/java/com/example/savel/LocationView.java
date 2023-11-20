package com.example.savel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class LocationView extends RelativeLayout {
    public LocationView(Context context, AttributeSet attrs) {
        super(context);
        init(context);
    }

    public LocationView(Context context) {
        super(context);
        init(context);
    }
    private void init(Context context){
        LayoutInflater inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.loaction_add_view,this,true);
    }
}


