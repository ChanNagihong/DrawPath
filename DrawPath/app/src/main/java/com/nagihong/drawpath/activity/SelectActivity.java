package com.nagihong.drawpath.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.nagihong.drawpath.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by channagihong on 2016/10/17.
 */

public class SelectActivity extends Activity {

    ListView listView;
    String CHART_BAR = "BarChart";
    String CHART_BUBBLE = "BubbleChart";
    String CHART_CANDLESTICK = "CandleStickChart";
    String CHART_COMPARISONBAR = "ComparisonBarChart";
    String CHART_DUALLINE = "DualLine";
    String CHART_HALFPIE = "HalfPieChart";
    String CHART_HORIZONTALBAR = "HorizontalBarChart";
    String CHART_LINE = "LineChart";
    String CHART_LINE2 = "LineChart2";
    String CHART_MULTIPLEBAR = "MultipleBarChart";
    String CHART_PIE = "PieChart";
    String CHART_STACKEDBAR = "StackedBarChartView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        listView = (ListView) findViewById(R.id.activity_select_listview);
        List<String> str = new LinkedList<>();
        str.add(CHART_BAR);
        str.add(CHART_BUBBLE);
        str.add(CHART_CANDLESTICK);
        str.add(CHART_COMPARISONBAR);
        str.add(CHART_DUALLINE);
        str.add(CHART_HALFPIE);
        str.add(CHART_HORIZONTALBAR);
        str.add(CHART_LINE);
        str.add(CHART_LINE2);
        str.add(CHART_MULTIPLEBAR);
        str.add(CHART_PIE);
        str.add(CHART_STACKEDBAR);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_select_item, str);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                String name = textView.getText().toString();
                Intent intent = null;
                if(name.equals(CHART_BAR)) {
                    intent = new Intent(SelectActivity.this, BarChartActivity.class);
                } else if(name.equals(CHART_BUBBLE)) {
                    intent = new Intent(SelectActivity.this, BubbleChartActivity.class);
                } else if(name.equals(CHART_CANDLESTICK)) {
                    intent = new Intent(SelectActivity.this, CandleStickChartActivity.class);
                } else if(name.equals(CHART_COMPARISONBAR)) {
                    intent = new Intent(SelectActivity.this, ComparisonBarChartActivity.class);
                } else if(name.equals(CHART_DUALLINE)) {
                    intent = new Intent(SelectActivity.this, DualLineChartActivity.class);
                } else if(name.equals(CHART_HALFPIE)) {
                    intent = new Intent(SelectActivity.this, HalfPieChartActivity.class);
                } else if(name.equals(CHART_HORIZONTALBAR)) {
                    intent = new Intent(SelectActivity.this, HorizontalBarChartActivity.class);
                } else if(name.equals(CHART_LINE)) {
                    intent = new Intent(SelectActivity.this, LineChartActivity.class);
                } else if(name.equals(CHART_LINE2)) {
                    intent = new Intent(SelectActivity.this, LineChartActivity2.class);
                } else if(name.equals(CHART_MULTIPLEBAR)) {
                    intent = new Intent(SelectActivity.this, MultipleBarChartActivity.class);
                } else if(name.equals(CHART_PIE)) {
                    intent = new Intent(SelectActivity.this, PieChartActivity.class);
                } else if(name.equals(CHART_STACKEDBAR)) {
                    intent = new Intent(SelectActivity.this, StackedBarChartActivity.class);
                }
                if(intent == null) {
                    return;
                }
                startActivity(intent);
            }
        });
    }
}
