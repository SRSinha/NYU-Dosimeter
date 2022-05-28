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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.data.LoudnessDatabase;
import com.example.data.PitchDatabase;
import com.example.data.loudnessDB;
import com.example.data.pitchDB;
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
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GraphActivity extends AppCompatActivity {
    CombinedChart combChart, combChartMin;
    PitchDatabase dbP;
    BarDataSet barDataSet, barDataSetMin;
    LineDataSet lineDataSet;
    BarData barData, barDataMin;
    LineData lineData;
    CombinedData combinedData, combinedDataMin;
    ArrayList<BarEntry> yVals, yValsMin;
    ArrayList<Entry> lineyVals;
    ArrayList<String> xVals;
    public static Typeface tf;
    DatePickerDialog picker;
    EditText eText;
    Button btnGetMin, btnGetHourly, btnGetWeekly;
    TextView tvw, phonationTimeTV, avgPitchTV, totalCycleDoseTV;
    DecimalFormat df1 = new DecimalFormat("####.0");
    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private static String[] suffix = new String[]{"","k", "m", "b", "t"};
    private static int MAX_LENGTH = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.enter, R.anim.exit);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.graph_main);
        tf= Typeface.createFromAsset(this.getAssets(), "fonts/Let_s go Digital Regular.ttf");

        dbP = PitchDatabase.getDatabase(getApplicationContext());
        combChart = findViewById(R.id.graphCOMBChart_view);
        combChartMin = findViewById(R.id.graphCOMBChart_view_Min);
        ImageButton backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //init combo graph properties
        initBarDataSet();
        initBarDataSetMin();

        phonationTimeTV = (TextView)findViewById(R.id.phonationTime);//phonationTimeTV.setTypeface(tf);
        avgPitchTV = (TextView)findViewById(R.id.avgPitch);//avgPitchTV.setTypeface(tf);
        totalCycleDoseTV = (TextView)findViewById(R.id.totalCycleDose);//totalCycleDoseTV.setTypeface(tf);
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
                picker = new DatePickerDialog(GraphActivity.this,
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
//        btnGetMin = findViewById(R.id.button1);

        btnGetWeekly = findViewById(R.id.buttonW);
        btnGetWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please select a date!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String dt = eText.getText().toString();
                System.out.println("date is -------> "+eText.getText().toString());
                SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd");
                Date userDate, curDate = new Date();
                try {
                    userDate=dateFormatter.parse(dt);
                } catch (ParseException e) {
                    System.out.println("Couldn't parse the date. Skipping");
                    Toast.makeText(getApplicationContext(), "Couldn't parse the date. Skipping",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                List<List<pitchDB>> weekDaysAvgPitch = new ArrayList<>();
                List<List<pitchDB>> weekDaysSecondsPitch = new ArrayList<>();

                while(curDate.after(userDate)){
//                    System.out.println("usd tostr ----->"+dateFormatter.format(userDate));
                    weekDaysAvgPitch.add(dbP.pitchDao().getPitchDateAvg(dateFormatter.format(userDate)));
                    weekDaysSecondsPitch.add(dbP.pitchDao().getPitchDateSecondlyList(dateFormatter.format(userDate)));
                    userDate = new Date(userDate.getTime() + MILLIS_IN_A_DAY);
                }
                updateComboChartWeek(weekDaysAvgPitch);
                updateComboChartMinWeek(weekDaysSecondsPitch);
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
                String dt = eText.getText().toString().substring(0,10);
                List<pitchDB> dateHourlyList = dbP.pitchDao().getPitchDateHourlyList(dt);
//                List<pitchDB> dateMinList = dbP.pitchDao().getPitchDateMinlyList(dt);
                List<pitchDB> dateSecList = dbP.pitchDao().getPitchDateSecondlyList(dt);
                List<pitchDB> avgPitch = dbP.pitchDao().getPitchDateAvg(dt);
                updateComboChartHour(dateHourlyList, dateSecList, avgPitch);
                updateComboChartMinHour(dateHourlyList, dateSecList);
            }
        });
        //2022-04-02 09:27:22
//        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

//        List<pitchDB> dateList = getAllDbData();
//        updateChart(dateList);
    }
    private List<pitchDB> getAllDbData() {
        return dbP.pitchDao().getPitchList();
    }

    private void updateComboChartWeek(List<List<pitchDB>> weekDaysAvgPitch) {
        if(combChart==null){
            return;
        }
        combChart.getXAxis().setAxisMaximum(7);
        yVals.clear();
        float avgPitch = 0.0f;
        float pitch;
        BarEntry barEntry;
        Integer day;
        for(int i = 0; i < weekDaysAvgPitch.size(); i++){     //max 24 values
            if(weekDaysAvgPitch.get(i).size()>0){
//                day = Integer.parseInt(weekDaysAvgPitch.get(i).get(0).tid.substring(11,13));
                //ready for graph
                pitch = weekDaysAvgPitch.get(i).get(0).value;
                barEntry = new BarEntry(i, new float[]{pitch});

                avgPitch+=pitch;
            } else{
                barEntry = new BarEntry(i, new float[]{0.0f});
                yVals.add(barEntry);
            }
            yVals.add(barEntry);
        }
        avgPitchTV.setText(df1.format(avgPitch)+" Hz");

        combinedData.notifyDataChanged();
        combChart.getData().notifyDataChanged();
        combChart.notifyDataSetChanged();
        combinedData.setDrawValues(true);
        combChart.invalidate();
    }

    private void updateComboChartMinWeek(List<List<pitchDB>> weekDaysSecondsPitch) {
        if(combChartMin==null){
            return;
        }
        combChartMin.getXAxis().setAxisMaximum(7);
        yValsMin.clear();

        HashMap<Integer, Integer> weeklyMapStd = new HashMap<>();
        float[] weeklyMins = new float[7];
        float totalweeklyCycles = 0.0f;
        for(int i = 0; i < weekDaysSecondsPitch.size(); i++){     //max 7 values
            weeklyMins[i] = weekDaysSecondsPitch.get(i).size()/60.f;
            for(pitchDB e : weekDaysSecondsPitch.get(i)){
                totalweeklyCycles+=e.value;
            }
        }
        float totalMins = 0.0f;
        for(int i = 0; i < weeklyMins.length; i++){     //max 7 values
            BarEntry barEntry = new BarEntry(i, new float[]{weeklyMins[i]});
            yValsMin.add(barEntry);
            totalMins+=weeklyMins[i];
        }

        String s = (Float.parseFloat(df1.format(totalMins))) + " mins";
        if(totalMins==0)
            s="1 min";
        phonationTimeTV.setText(s);

        totalCycleDoseTV.setText(format(totalweeklyCycles)+" cycles");

        combinedDataMin.notifyDataChanged();
        combChartMin.getData().notifyDataChanged();
        combChartMin.notifyDataSetChanged();
        combinedDataMin.setDrawValues(true);
        combChartMin.invalidate();
    }

    private static String format(double number) {
        String r = new DecimalFormat("##0E0").format(number);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        while(r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")){
            r = r.substring(0, r.length()-2) + r.substring(r.length() - 1);
        }
        return r;
    }

    private void updateComboChartMinHour(List<pitchDB> dateHourlyList, List<pitchDB> dateSecList) {
        if(combChartMin==null){
            return;
        }
        combChartMin.getXAxis().setAxisMaximum(23);
        yValsMin.clear();

        HashMap<Integer, Integer> hourlyMapStd = new HashMap<>();
        for(int i = 0; i < dateHourlyList.size(); i++){     //max 24 values
            Integer hour = Integer.parseInt(dateHourlyList.get(i).tid.substring(11,13));
            hourlyMapStd.put(hour, 0);
        }

        for(int i = 0; i < dateSecList.size(); i++){
            Integer hour = Integer.parseInt(dateSecList.get(i).tid.substring(11,13));
            hourlyMapStd.put(hour, hourlyMapStd.get(hour)+1);
        }
        for(int i = 0; i < hourlyMapStd.size(); i++){     //max 24 values
            Integer hour = Integer.parseInt(dateHourlyList.get(i).tid.substring(11,13));
            //ready for graph
            BarEntry barEntry = new BarEntry(hour, new float[]{
                    hourlyMapStd.get(hour)/60.0f
            });
            yValsMin.add(barEntry);
        }
        combinedDataMin.notifyDataChanged();
        combChartMin.getData().notifyDataChanged();
        combChartMin.notifyDataSetChanged();
        combinedDataMin.setDrawValues(true);
        combChartMin.invalidate();
    }

    private void updateComboChartHour(List<pitchDB> dateHourlyList, List<pitchDB> dateSecList, List<pitchDB> avgPitch) {
        if(combChart==null){
            return;
        }
        combChart.getXAxis().setAxisMaximum(23);
        yVals.clear();

        HashMap<Integer, Float[]> hourlyMapStd = new HashMap<>();
        for(int i = 0; i < dateHourlyList.size(); i++){     //max 24 values
            Integer hour = Integer.parseInt(dateHourlyList.get(i).tid.substring(11,13));
            hourlyMapStd.put(hour, new Float[]{0.0f, 0.0f, 0.0f});
            //ready for graph
            BarEntry barEntry = new BarEntry(hour, new float[]{
                    dateHourlyList.get(i).value
            });
            yVals.add(barEntry);
        }

        for(int i = 0; i < dateSecList.size(); i++){
            Integer hour = Integer.parseInt(dateSecList.get(i).tid.substring(11,13));
            float curr =  hourlyMapStd.get(hour)[0] + dateSecList.get(i).value;
            hourlyMapStd.get(hour)[0] = curr;                             //sum (x-xM)^2
            hourlyMapStd.get(hour)[1] = hourlyMapStd.get(hour)[1]+1;      //incr count
        }

        String s = (Float.parseFloat(df1.format(dateSecList.size() / 60.0f))) + " mins";
        if(dateSecList.size()/60==0)
            s="1 min";
        phonationTimeTV.setText(s);
        if(avgPitch.size()>0)
            avgPitchTV.setText(df1.format(avgPitch.get(0).value)+" Hz");

        float sum = 0.0f;
        for (pitchDB ele: dateSecList) {
            sum += ele.value;
        }
        totalCycleDoseTV.setText(format(sum)+" cycles");

        System.out.println("avg p : "+ avgPitch);
        combinedData.notifyDataChanged();
        combChart.getData().notifyDataChanged();
        combChart.notifyDataSetChanged();
        combinedData.setDrawValues(true);
        combChart.invalidate();
    }

    private void updateChart(List<pitchDB> list) {
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
//        xVals = new ArrayList<>();
        yVals = new ArrayList<>();
//        lineyVals = new ArrayList<>();
        yVals.add(new BarEntry(0,0));
//        lineyVals.add(new Entry(0,0));

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
        xAxis.setAxisMaximum(23);

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
        barDataSet.setValueTextSize(14f);
        barDataSet.setValueFormatter(new DecimalValueFormatter());
        barDataSet.setValueTypeface(tf);
        barDataSet.setValueTextColor(Color.WHITE);
//        barDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        barDataSet.setHighLightColor(Color.GREEN);

        /*lineDataSet = new LineDataSet(lineyVals, title);
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
*/
        barData = new BarData(barDataSet);
        barData.setValueTextSize(9f);
//        barData.setDrawValues(false);

//        lineData = new LineData(lineDataSet);
//        lineData.setValueTextSize(9f);
//        lineData.setDrawValues(false);

        combinedData = new CombinedData();
        combinedData.setData(barData);
//        combinedData.setData(lineData);
        combChart.setData(combinedData);
        combChart.getLegend().setEnabled(false);
//        combChart.animateXY(500, 500);

        // dont forget to refresh the drawing
        combChart.invalidate();
    }


    private void initBarDataSetMin(){
//        xValsMin = new ArrayList<>();
        yValsMin = new ArrayList<>();
//        lineyVals = new ArrayList<>();
        yValsMin.add(new BarEntry(0,0));
//        lineyVals.add(new Entry(0,0));

        //remove the description label text located at the lower right corner
        Description description = new Description();
        description.setEnabled(false);

        combChartMin.setDescription(description);
        combChartMin.setViewPortOffsets(50, 20, 5, 60);
        combChartMin.setAutoScaleMinMaxEnabled(true);
        combChartMin.setDrawGridBackground(false);

        //setting animation for y-axis, the bar will pop up from 0 to its value within the time we set
        combChartMin.animateY(500);
        //setting animation for x-axis, the bar will pop up separately within the time we set
        combChartMin.animateX(500);
//        combChart.setDrawBarShadow(true);
        combChartMin.setFitsSystemWindows(true);
        combChartMin.resetZoom();
        combChartMin.fitScreen();

        XAxis xAxis = combChartMin.getXAxis();
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
        xAxis.setAxisMaximum(23);

        YAxis leftAxis = combChartMin.getAxisLeft();
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

        Legend legend = combChartMin.getLegend();
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

        barDataSetMin = new BarDataSet(yValsMin, title);
        //Changing the color of the bar
        barDataSetMin.setColor(Color.parseColor("#FFFF00"));
//        barDataSetMin.setColor(Color.parseColor("#AE0C6E"));
        //Setting the size of the form in the legend
        barDataSetMin.setFormSize(15f);
        //showing the value of the bar, default true if not set
        barDataSetMin.setDrawValues(true);
        //setting the text size of the value of the bar
        barDataSetMin.setValueTextSize(14f);
        barDataSetMin.setValueTextColor(Color.WHITE);
        barDataSetMin.setValueTypeface(tf);
        barDataSetMin.setValueFormatter(new DecimalValueFormatter());
//        barDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        barDataSetMin.setHighLightColor(Color.GREEN);

        barDataMin = new BarData(barDataSetMin);
        barDataMin.setValueTextSize(9f);
//        barData.setDrawValues(false);

        combinedDataMin = new CombinedData();
        combinedDataMin.setData(barDataMin);
//        combinedDataMin.setData(lineData);
        combChartMin.setData(combinedDataMin);
        combChartMin.getLegend().setEnabled(false);
//        combChart.animateXY(500, 500);

        // dont forget to refresh the drawing
        combChartMin.invalidate();
    }
}