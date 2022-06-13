package com.kv.kidsdrawiingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import yuku.ambilwarna.AmbilWarnaDialog;

public class colorpalleteActivity extends AppCompatActivity {
    ConstraintLayout mLayout;
    int mDefaultColor;
    Button mbutton;
    View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_pallete);
        mLayout =(ConstraintLayout) findViewById(R.id.layout);
        v= (View) findViewById(R.id.c_view);
        mDefaultColor = ContextCompat.getColor(this,R.color.purple_500);
        mbutton = (Button) findViewById(R.id.color_btn);
        mbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });

    }
    public void openColorPicker(){
        AmbilWarnaDialog cp = new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaultColor = color;
                v.setBackgroundColor(color);
            }
        });
        cp.show();
    }
}
