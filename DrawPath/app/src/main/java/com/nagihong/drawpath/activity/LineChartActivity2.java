package com.nagihong.drawpath.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.nagihong.drawpath.R;

/**
 * Created by channagihong on 30/05/2017.
 */

public class LineChartActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        findViewById(R.id.linechart2).setVisibility(View.VISIBLE);
    }

}
