package com.nagihong.drawpath;

import android.app.Activity;
import android.app.Notification;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by channagihong on 2016/9/23.
 */
public class TestActivity extends Activity {

    TestView testView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        testView = (TestView) findViewById(R.id.test_view);
        findViewById(R.id.test_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testView.yTrans -=50;
                testView.invalidate();
            }
        });
        findViewById(R.id.test_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testView.yTrans += 50;
                testView.invalidate();
            }
        });
        findViewById(R.id.test_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testView.xTrans -= 50;
                testView.invalidate();
            }
        });
        findViewById(R.id.test_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testView.xTrans += 50;
                testView.invalidate();
            }
        });
        findViewById(R.id.test_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testView.scale += 0.1f;
                testView.invalidate();
            }
        });
        findViewById(R.id.test_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testView.scale -= 0.1f;
                testView.invalidate();
            }
        });
    }

}
