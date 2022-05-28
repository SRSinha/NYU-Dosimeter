package com.example.dosime;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.data.LoudnessDatabase;
import com.example.data.loudnessDB;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GraphActivityBackup extends AppCompatActivity {
    BarChart barChart;
    LoudnessDatabase dbL;
    BarDataSet barDataSet;

    BarData data;
    ArrayList<BarEntry> yVals;
    ArrayList<String> xVals;
    public static Typeface tf;
    DatePickerDialog picker;
    EditText eText;
    Button btnGet, btnGetHourly;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.enter, R.anim.exit);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.graph_main);
        tf= Typeface.createFromAsset(this.getAssets(), "fonts/Let_s go Digital Regular.ttf");

        dbL = LoudnessDatabase.getDatabase(getApplicationContext());
//        barChart = findViewById(R.id.graphBARChart_view);
        ImageButton refreshButton = findViewById(R.id.refreshbutton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initBarDataSet();
//        tvw=(TextView)findViewById(R.id.textView1);
        eText=(EditText) findViewById(R.id.editText1);
        eText.setInputType(InputType.TYPE_NULL);
        eText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(GraphActivityBackup.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String dayOfMonthStr = dayOfMonth<10 ? "0"+Integer.toString(dayOfMonth) : Integer.toString(dayOfMonth);
                                String monthOfYearStr = monthOfYear+1<10 ? "0"+Integer.toString(monthOfYear+1) : Integer.toString(monthOfYear+1);
                                eText.setText(year + "-" + monthOfYearStr +  "-" + dayOfMonthStr);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
        btnGet=(Button)findViewById(R.id.buttonW);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
//                tvw.setText("Selected Date: "+ eText.getText().toString());
                if(eText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please select a date!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                List<loudnessDB> dateList = getDateDbData(eText.getText().toString());
                updateChart(dateList);
            }
        });
        btnGetHourly=(Button)findViewById(R.id.button2);
        btnGetHourly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please select a date!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                List<loudnessDB> dateList = getDateHourlyDbData(eText.getText().toString());
                updateChart(dateList);
            }
        });
        //2022-04-02 09:27:22
//        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        List<loudnessDB> dateList = getAllDbData();
        updateChart(dateList);
    }

    private List<loudnessDB> getDateHourlyDbData(String date) {
        return dbL.loudnessDao().getLoudnessDateHourlyList(date.substring(0,10));
    }

    private List<loudnessDB> getAllDbData() {
        return dbL.loudnessDao().getLoudnessList();
    }

    private List<loudnessDB> getDateDbData(String date) {
        return dbL.loudnessDao().getLoudnessDateList(date.substring(0,10));
    }

    private void updateChart(List<loudnessDB> list) {
        if(barChart==null){
            return;
        }
        yVals.clear();
        //input data
        for(int i = 0; i < list.size(); i++){
            BarEntry barEntry = new BarEntry(i, new float[]{
                    list.get(i).value, list.get(i).value-10
            });
            yVals.add(barEntry);
        }


        data.notifyDataChanged();
        barDataSet.notifyDataSetChanged();
        barChart.getData().notifyDataChanged();
        barChart.notifyDataSetChanged();
        data.setDrawValues(true);
        barChart.setData(data);
        barChart.invalidate();
    }

    private void initBarDataSet(){
        xVals = new ArrayList<>();
        yVals = new ArrayList<>();
        yVals.add(new BarEntry(0,0));

        //remove the description label text located at the lower right corner
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);

//        barChart = (LineChart) findViewById(R.id.chartPitch);
        barChart.setViewPortOffsets(50, 20, 5, 60);
        barChart.setAutoScaleMinMaxEnabled(true);
        barChart.setDrawGridBackground(false);

        //setting animation for y-axis, the bar will pop up from 0 to its value within the time we set
        barChart.animateY(500);

        //setting animation for x-axis, the bar will pop up separately within the time we set
        barChart.animateX(500);
//        barChart.setDrawBarShadow(true);
        barChart.setFitBars(true);
//        barChart.setHighlightFullBarEnabled(true);


        XAxis xAxis = barChart.getXAxis();
        //change the position of x-axis to the bottom
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //set the horizontal distance of the grid line
        xAxis.setGranularity(1f);
        //hiding the x-axis line, default true if not set
//        xAxis.setDrawAxisLine(false);
        //hiding the vertical grid lines, default true if not set
//        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(8, false);
        xAxis.setEnabled(true);
        xAxis.setTypeface(tf);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setAxisLineColor(Color.WHITE);

        YAxis leftAxis = barChart.getAxisLeft();
        //hiding the left y-axis line, default true if not set
        leftAxis.setDrawAxisLine(true);
        leftAxis.setLabelCount(4, false);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setTypeface(tf);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setAxisMinValue(0);
        leftAxis.setAxisMaxValue(250);
        //hiding the right y-axis line, default true if not set

        Legend legend = barChart.getLegend();
        //setting the shape of the legend form to line, default square shape
        legend.setForm(Legend.LegendForm.LINE);
        //setting the text size of the legend
        legend.setTextSize(11f);
        //setting the alignment of legend toward the chart
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        //setting the stacking direction of legend
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //setting the location of legend outside the chart, default false if not set
        legend.setDrawInside(false);

//        BarDataSet set1 = new BarDataSet(yVals, "DataSet 1");
        String title = "Loudness levels";

        barDataSet = new BarDataSet(yVals, title);
        //Changing the color of the bar
        barDataSet.setColor(Color.LTGRAY);
        //Setting the size of the form in the legend
        barDataSet.setFormSize(15f);
        //showing the value of the bar, default true if not set
        barDataSet.setDrawValues(true);
        //setting the text size of the value of the bar
        barDataSet.setValueTextSize(12f);
        barDataSet.setValueTypeface(tf);
//        barDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        barDataSet.setHighLightColor(Color.GREEN);


//        BarData data = new BarData(barDataSet);
        data = new BarData(barDataSet);
        data.setValueTextSize(9f);
        data.setDrawValues(false);

        barChart.setData(data);
        barChart.getLegend().setEnabled(false);
        barChart.animateXY(2000, 2000);
        // dont forget to refresh the drawing
        barChart.invalidate();
    }
}

