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
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GraphActivityLoudness extends AppCompatActivity {
    CombinedChart combChart;
    LoudnessDatabase dbL;
    BarDataSet barDataSet;
    LineDataSet lineDataSet;
    BarData barData;
    LineData lineData;
    CombinedData combinedData;
    ArrayList<BarEntry> yVals;
    ArrayList<Entry> lineyVals;
    ArrayList<String> xVals;
    public static Typeface tf;
    DatePickerDialog picker;
    EditText eText;
    Button btnGetMin, btnGetHourly;
    TextView tvw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.enter, R.anim.exit);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.graph_main);
        tf= Typeface.createFromAsset(this.getAssets(), "fonts/Let_s go Digital Regular.ttf");

        dbL = LoudnessDatabase.getDatabase(getApplicationContext());
        combChart = findViewById(R.id.graphCOMBChart_view);
        ImageButton backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //init combo graph properties
        initBarDataSet();
//        tvw=(TextView)findViewById(R.id.textView1);
        eText = findViewById(R.id.editText1);
        eText.setInputType(InputType.TYPE_NULL);
        eText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(GraphActivityLoudness.this,
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
        btnGetMin = findViewById(R.id.buttonW);
        btnGetMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
//                tvw.setText("Selected Date: "+ eText.getText().toString());
                if(eText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please select a date!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String date = eText.getText().toString();
                List<loudnessDB> dateList = getDateDbData(date);
                updateChart(dateList);
                System.out.println("******* phonation seconds=");
//                int a = dbL.loudnessDao().getLoudnessDateTotalEntries(date);
                List<loudnessDB> x = dbL.loudnessDao().getLoudnessDateList(date);
                HashMap<String, Integer> phonMap = new HashMap<>();
                int count=0, total=0;
                for(int i = 0; i < x.size(); i++){
                    if(!phonMap.containsKey(x.get(i).tid.substring(0,19))){
                        phonMap.put(x.get(i).tid.substring(0,19), 0);
                    }
                    if(x.get(i).value!=0.0f){
                        if(phonMap.get(x.get(i).tid.substring(0,19))==0) {   //full date, hh:mm:ss. not millisecond)
                            phonMap.put(x.get(i).tid.substring(0,19), 1);
                            count++;
                        }
                    }
//                    System.out.println(x.get(i).tid);
                }
                total = phonMap.size();
                System.out.println("phonation Seconds="+count + "   total recording seconds="+total);
            }
        });
        btnGetHourly = findViewById(R.id.button2);
        btnGetHourly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please select a date!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                List<loudnessDB> dateHourlyList = dbL.loudnessDao().getLoudnessDateHourlyList(eText.getText().toString().substring(0,10));
                List<loudnessDB> dateMinList = dbL.loudnessDao().getLoudnessDateMinlyList(eText.getText().toString().substring(0,10));
                List<loudnessDB> dateSecList = dbL.loudnessDao().getLoudnessDateSecondlyList(eText.getText().toString().substring(0,10));
                updateComboChart(dateHourlyList, dateMinList);
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

    private void updateComboChart(List<loudnessDB> dateHourlyList, List<loudnessDB> dateMinList) {
        if(combChart==null){
            return;
        }
        yVals.clear();
        lineyVals.clear();

        HashMap<Integer, Float[]> hourlyMapStd = new HashMap<>();  //hour, [(x-xM)^2, N, avgHour]
        for(int i = 0; i < dateHourlyList.size(); i++){     //max 24 values
            Integer hour = Integer.parseInt(dateHourlyList.get(i).tid.substring(11,13));
            hourlyMapStd.put(hour, new Float[]{0.0f, 0.0f, dateHourlyList.get(i).value});
            //ready for graph
            BarEntry barEntry = new BarEntry(hour, new float[]{
                    dateHourlyList.get(i).value, dateHourlyList.get(i).value-10
            });
            yVals.add(barEntry);
        }
//        2022-05-07 13:16:25.948
        for(int i = 0; i < dateMinList.size(); i++){
            Integer hour = Integer.parseInt(dateMinList.get(i).tid.substring(11,13));
            float curr =  hourlyMapStd.get(hour)[0] + (float)(Math.pow(dateMinList.get(i).value-hourlyMapStd.get(hour)[2], 2));
            hourlyMapStd.get(hour)[0] = curr;                             //sum (x-xM)^2
            hourlyMapStd.get(hour)[1] = hourlyMapStd.get(hour)[1]+1;      //incr count
        }

        for(int i = 0; i < dateHourlyList.size(); i++){
            Integer hour = Integer.parseInt(dateMinList.get(i).tid.substring(11,13));
            float curr =  hourlyMapStd.get(hour)[0] + (float)(Math.pow(dateMinList.get(i).value-hourlyMapStd.get(hour)[2], 2));
            hourlyMapStd.get(hour)[0] = curr;                             //sum (x-xM)^2
            hourlyMapStd.get(hour)[1] = hourlyMapStd.get(hour)[1]+1;      //incr count
        }

        for (Integer key : hourlyMapStd.keySet()) {
            Entry lineEntry = new Entry(key, (float)Math.sqrt(hourlyMapStd.get(key)[0]/hourlyMapStd.get(key)[1]));
            lineyVals.add(lineEntry);
        }

        combinedData.notifyDataChanged();
        combChart.getData().notifyDataChanged();
        combChart.notifyDataSetChanged();
        combinedData.setDrawValues(true);
        combChart.invalidate();
    }



    private void updateChart(List<loudnessDB> list) {
        if(combChart==null){
            return;
        }
        yVals.clear();
        lineyVals.clear();
//        (hour, AvgVal)
        for(int i = 0; i < list.size(); i++){
            BarEntry barEntry = new BarEntry(i, new float[]{
                    list.get(i).value, list.get(i).value-10
            });
            yVals.add(barEntry);
            Entry lineEntry = new Entry(i, list.get(i).value);
            lineyVals.add(lineEntry);
        }
        combinedData.notifyDataChanged();
        combChart.getData().notifyDataChanged();
        combChart.notifyDataSetChanged();
        combinedData.setDrawValues(true);
        combChart.invalidate();
    }

    private void initBarDataSet(){
        xVals = new ArrayList<>();
        yVals = new ArrayList<>();
        lineyVals = new ArrayList<>();
        yVals.add(new BarEntry(0,0));
        lineyVals.add(new Entry(0,0));

        //remove the description label text located at the lower right corner
        Description description = new Description();
        description.setEnabled(false);

        combChart.setDescription(description);
        combChart.setViewPortOffsets(50, 20, 5, 60);
        combChart.setAutoScaleMinMaxEnabled(true);
        combChart.setDrawGridBackground(false);

        //setting animation for y-axis, the bar will pop up from 0 to its value within the time we set
        combChart.animateY(500);
        //setting animation for x-axis, the bar will pop up separately within the time we set
        combChart.animateX(500);
//        combChart.setDrawBarShadow(true);
        combChart.setFitsSystemWindows(true);
        combChart.resetZoom();
        combChart.fitScreen();

        XAxis xAxis = combChart.getXAxis();
        //change the position of x-axis to the bottom
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //set the horizontal distance of the grid line
//        xAxis.setGranularity(1f);
        //hiding the x-axis line, default true if not set
        //hiding the vertical grid lines, default true if not set
//        xAxis.setDrawGridLines(false);
//        xAxis.setLabelCount(8, false);
//        xAxis.setEnabled(true);
        xAxis.setTypeface(tf);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setAxisMaxValue(24);

        YAxis leftAxis = combChart.getAxisLeft();
        //hiding the left y-axis line, default true if not set
        leftAxis.setDrawAxisLine(true);
//        leftAxis.setLabelCount(4, false);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setTypeface(tf);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setAxisMinValue(0);
//        leftAxis.setAxisMaxValue(250);
        //hiding the right y-axis line, default true if not set

        Legend legend = combChart.getLegend();
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

        String title = "Loudness levels";

        barDataSet = new BarDataSet(yVals, title);
        //Changing the color of the bar
        barDataSet.setColor(Color.parseColor("#AE0C6E"));
        //Setting the size of the form in the legend
        barDataSet.setFormSize(15f);
        //showing the value of the bar, default true if not set
        barDataSet.setDrawValues(true);
        //setting the text size of the value of the bar
        barDataSet.setValueTextSize(12f);
        barDataSet.setValueTypeface(tf);
//        barDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        barDataSet.setHighLightColor(Color.GREEN);

        lineDataSet = new LineDataSet(lineyVals, title);
        //Changing the color of the line
        lineDataSet.setColor(Color.YELLOW);
        //Setting the size of the form in the legend
        lineDataSet.setFormSize(15f);
        //showing the value of the bar, default true if not set
        lineDataSet.setDrawValues(true);
        //setting the text size of the value of the bar
//        lineDataSet.setValueTextSize(12f);
        lineDataSet.setValueTypeface(tf);
        lineDataSet.setHighLightColor(Color.YELLOW);

        lineDataSet.setLineWidth(1f);
//        lineDataSet.setFormLineWidth(5f);
//        lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
//        lineDataSet.setFormSize(5.f);
//        lineDataSet.setDrawFilled(true);
//        lineDataSet.enableDashedLine(10f, 0f, 0f);
//        lineDataSet.enableDashedHighlightLine(10f, 0f, 0f);

        barData = new BarData(barDataSet);
        barData.setValueTextSize(9f);
//        barData.setDrawValues(false);

        lineData = new LineData(lineDataSet);
        lineData.setValueTextSize(9f);
//        lineData.setDrawValues(false);

        combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);
        combChart.setData(combinedData);
        combChart.getLegend().setEnabled(false);
//        combChart.animateXY(500, 500);

        // dont forget to refresh the drawing
        combChart.invalidate();
    }
}