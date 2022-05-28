package com.example.dosime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.data.LoudnessDatabase;
import com.example.data.PitchDatabase;
import com.example.data.loudnessDB;
import com.example.data.pitchDB;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

//public class MainActivity extends Activity {
public class MainActivity extends AppCompatActivity {
    ArrayList<Entry> yVals;
    ArrayList<Entry> yPVals;
    boolean refreshed=false, refreshedP=false;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String timeStr;
    Speedometer speedometer;
    public static Typeface tf, tfA;
    ImageButton infoButton;
    ImageButton refreshButton;
    LineChart mChart, pitchChart;
    AudioDispatcher dispatcher;

    PitchDatabase dbP;
    LoudnessDatabase dbL;
    DecimalFormat df1 = new DecimalFormat("####.0");


    TextView minVal;
    TextView maxVal;
    TextView mmVal;
    TextView curVal;

    TextView minPVal;
    TextView maxPVal;
    TextView mmPVal;
    TextView curPVal;

    long currentTime=0;
    long savedTime=0;
    long savedTimePitch=0;
    boolean isChart=false;
    boolean isPChart=false;
    /* Decibel */
    private boolean bListener = true;
    private boolean isThreadRun = true;
    private Thread thread;
    float volume = 10000;
    int refresh=0;
    private MyMediaRecorder mRecorder ;
    Map<String, Float> pitchMap = new HashMap<String, Float>();
    Map<String, ArrayList<Float>> loudMapNew = new HashMap<String, ArrayList<Float>>();
    private int clock=0;
    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == 1){
                if(World.dbCount>=60.0f && World.dbCount<200.0f){
//                    val = World.dbCount;
                    curVal.setText(df1.format(World.dbCount));
                    minVal.setText(df1.format(World.minDB));
                    mmVal.setText(df1.format((World.minDB+World.maxDB)/2));
                    maxVal.setText(df1.format(World.maxDB));
                    updateData(World.dbCount, 0);   //only update when above threshold
                }
            }
        }
    };
    @SuppressLint("HandlerLeak")
    final Handler handlerPitch = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DecimalFormat df1 = new DecimalFormat("####.0");
            if (msg.what == 1) {
//                if (!isPChart) {
//                    initChartPitch();
//                    return;
//                }
//                speedometer.refresh();
//                totalTime++;
//                StringBuilder sb = new StringBuilder().append("");
                minPVal.setText(df1.format(World.minPitch));
                mmPVal.setText(df1.format((World.minPitch + World.maxPitch) / 2));
                maxPVal.setText(df1.format(World.maxPitch));

//                updateDataPitch(World.dbCount,0); --> UPAR ho raha
                System.out.println();
                if (World.pitchCount!=-1.0f) {
//                    System.out.println("- - - - - - - - - -pitch prob = " );
//                           sb.setLength(0);
//                            TextView text = (TextView) findViewById(R.id.curPval);
//                            text.setText(df1.format(World.pitchCount));
                    curPVal.setText(df1.format(World.pitchCount));
                }
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        tf= Typeface.createFromAsset(this.getAssets(), "fonts/Let_s go Digital Regular.ttf");
        minVal=(TextView)findViewById(R.id.minval);minVal.setTypeface(tf);
        mmVal=(TextView)findViewById(R.id.mmval);mmVal.setTypeface(tf);
        maxVal=(TextView)findViewById(R.id.maxval);maxVal.setTypeface(tf);
        curVal=(TextView)findViewById(R.id.curval);curVal.setTypeface(tf);

        minPVal=(TextView)findViewById(R.id.minPval);minPVal.setTypeface(tf);
        mmPVal=(TextView)findViewById(R.id.mmPval);mmPVal.setTypeface(tf);
        maxPVal=(TextView)findViewById(R.id.maxPval);maxPVal.setTypeface(tf);
        curPVal=(TextView)findViewById(R.id.curPval);curPVal.setTypeface(tf);

//        speakTime=totalTime=0;
//        infoButton=(ImageButton)findViewById(R.id.infobutton);
//        infoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                InfoDialog.Builder builder = new InfoDialog.Builder(MainActivity.this);
//                builder.setMessage(getString(R.string.activity_infobull));
//                builder.setTitle(getString(R.string.activity_infotitle));
//                builder.setNegativeButton(getString(R.string.activity_infobutton),
//                        new android.content.DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                builder.create().show();
//            }
//        });

//        refreshButton=(ImageButton)findViewById(R.id.refreshbutton);
//        refreshButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                refreshed=true;
//                refreshedP=true;
//                World.minDB=100;
//                World.dbCount=0;
//                World.lastDbCount=0;
//                World.maxDB=0;
//
//                World.minPitch=100;
//                World.pitchCount=0;
//                World.lastPitchCount=0;
//                World.maxPitch=0;
//
//                initChart();
//                initChartPitch();
//            }
//        });

//        speedometer=(Speedometer)findViewById(R.id.speed);
        mRecorder = new MyMediaRecorder();

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        dbP = PitchDatabase.getDatabase(getApplicationContext());
        dbL = LoudnessDatabase.getDatabase(getApplicationContext());

//        dbP.pitchDao().getPitchList();
//        dbL.loudnessDao().getLoudnessList();
//        PitchDatabase dbp = Room.databaseBuilder(getApplicationContext(), PitchDatabase.class, "pitch").build();
//        LoudnessDatabase dbl = Room.databaseBuilder(getApplicationContext(), LoudnessDatabase.class, "loudness").build();
    }

    public void openGraph(View View)
    {
        Intent graph = new Intent(this, GraphActivity.class);
        startActivity(graph);
    }

    private void insertDataPitch(float val, int time) {
        //2022-04-02 09:27:22
        timeStr = sdf.format(new Date());   //doesn't cause much of diff if value recorded at ms level

        final Iterator<Map.Entry<String, Float>>[] itr = new Iterator[]{pitchMap.entrySet().iterator()};
        if(itr[0].hasNext()){
            if(itr[0].next().getKey().substring(0,16).equals(timeStr.substring(0,16)))
                pitchMap.put(timeStr, val);
            else{
//                    System.out.println("Map size = " + pitchMap.size());
//                    System.out.println(pitchMap);
                //storing at milliseconds seconds level
                dbL.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("******************* Begin Inserting Pitch with time= "+timeStr);
                        itr[0] = pitchMap.entrySet().iterator();
                        while (itr[0].hasNext()){
                            Map.Entry<String, Float> pair = (Map.Entry)itr[0].next();
                            float curVal = pair.getValue();
                            if(Float.isNaN(curVal))
                                continue;
                            dbP.pitchDao().insertPitch(new pitchDB(pair.getKey(), curVal));
//                            System.out.println("    ###### Inserted Pitch with time "+pair.getKey() + " val=" + curVal);// + "   speakT=" + speakTime + "    totalT=" + totalTime);
                        }
                        System.out.println("******************* Inserted with time= "+timeStr);
                    }
                });
                pitchMap.clear();
                pitchMap.put(timeStr, val);
            }
        } else{
            pitchMap.put(timeStr, val);
        }
    }

    private void updateData(float val, int time) {
            //2022-04-02 09:27:22
            timeStr = sdf.format(new Date());

            final Iterator<Map.Entry<String, Float>>[] itr = new Iterator[]{pitchMap.entrySet().iterator()};
            if(itr[0].hasNext()){
                if(itr[0].next().getKey().substring(0,16).equals(timeStr.substring(0,16)))
                    pitchMap.put(timeStr, val);
                else{
//                    System.out.println("Map size = " + loudMap.size());
//                    System.out.println(loudMap);
                    //storing at milliseconds seconds level
                    dbL.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("******************* Begin Inserting with time= "+timeStr);
                            itr[0] = pitchMap.entrySet().iterator();
//                            float sum=0, min_=Float.MAX_VALUE, max_=Float.MIN_VALUE;
//                            int speakTime=0, totalTime=0;
                            while (itr[0].hasNext()){
                                Map.Entry<String, Float> pair = (Map.Entry)itr[0].next();
                                float curVal = pair.getValue();
                                if(Float.isNaN(curVal))
                                    continue;
                                dbL.loudnessDao().insertLoudness(new loudnessDB(pair.getKey(), curVal, curVal, curVal));
                                System.out.println("    ###### Inserted with time "+pair.getKey() + " val=" + curVal);// + "   speakT=" + speakTime + "    totalT=" + totalTime);
                            }
                            System.out.println("******************* Inserted with time= "+timeStr);
                        }
                    });
//                    System.out.println("Inserting with time= "+timeStr + " val=" + val + "    min=" + min_ + "    max="+ max_);
//                    dbL.loudnessDao().insertLoudness(new loudnessDB(timeStr, sum / loudMap.size(), min_, max_));

                    pitchMap.clear();
                    pitchMap.put(timeStr, val);
                }
                //System.out.println("key of : "+ itr.next().getKey() +" value of      Map"+itr.next().getValue());
            } else{
                pitchMap.put(timeStr, val);

            }
    }

    /*private void updateDataPitch(float val, long time, boolean ifUpdate) {
        if(pitchChart==null){
            return;
        }
        if (pitchChart.getData() != null &&  pitchChart.getData().getDataSetCount() > 0) {
            if(!ifUpdate){
                LineDataSet set1 = (LineDataSet)pitchChart.getData().getDataSetByIndex(0);
                set1.setValues(yPVals);
                Entry entry=new Entry(savedTimePitch,0);
                set1.addEntry(entry);
                if(set1.getEntryCount()>200){
                    set1.removeFirst();
                    set1.setDrawFilled(false);
                }
                pitchChart.getData().notifyDataChanged();
                pitchChart.notifyDataSetChanged();
                pitchChart.invalidate();
            } else{
                LineDataSet set1 = (LineDataSet)pitchChart.getData().getDataSetByIndex(0);
                set1.setValues(yPVals);
                Entry entry=new Entry(savedTimePitch,val);
                set1.addEntry(entry);
                if(set1.getEntryCount()>200){
                    set1.removeFirst();
                    set1.setDrawFilled(false);
                }
                pitchChart.getData().notifyDataChanged();
                pitchChart.notifyDataSetChanged();
                pitchChart.invalidate();
            }
            savedTimePitch++;
        }
    }*/

    private void initChart() {
        if(mChart!=null){
            if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {
                savedTime++;
                isChart=true;
            }
        }else{
            currentTime=new Date().getTime();
            mChart = (LineChart) findViewById(R.id.chart1);
            mChart.setViewPortOffsets(50, 20, 5, 60);
            // no description text
//            Description de = new Description(); de.setText("Hi hello");
//            de.setTextColor(Color.RED);
//            mChart.setDescription(de);
            // enable touch gestures
            mChart.setTouchEnabled(true);
            // enable scaling and dragging
            mChart.setDragEnabled(false);
            mChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(false);
            mChart.setDrawGridBackground(false);
            //mChart.setMaxHighlightDistance(400);
            XAxis x = mChart.getXAxis();
            x.setLabelCount(8, false);
            x.setEnabled(true);
            x.setTypeface(tf);
            x.setTextColor(Color.WHITE);
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setDrawGridLines(true);
            x.setAxisLineColor(Color.WHITE);
            YAxis y = mChart.getAxisLeft();
            y.setLabelCount(6, false);
            y.setTextColor(Color.WHITE);
            y.setTypeface(tf);
            y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            y.setDrawGridLines(false);
            y.setAxisLineColor(Color.WHITE);
            y.setAxisMinValue(0);
            y.setAxisMaxValue(120);
            mChart.getAxisRight().setEnabled(true);
            yVals = new ArrayList<Entry>();
            yVals.add(new Entry(0,0));
            LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
            set1.setValueTypeface(tf);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.02f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setCircleColor(Color.GREEN);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.WHITE);
            set1.setFillColor(Color.WHITE);
            set1.setFillAlpha(100);
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return -10;
                }
            });
            LineData data;
            if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
                data =  mChart.getLineData();
                data.clearValues();
                data.removeDataSet(0);
                data.addDataSet(set1);
            }else {
                data = new LineData(set1);
            }

            data.setValueTextSize(9f);
            data.setDrawValues(false);
            mChart.setData(data);
            mChart.getLegend().setEnabled(false);
            mChart.animateXY(2000, 2000);
            // dont forget to refresh the drawing
            mChart.invalidate();
            isChart=true;
        }

    }
    private void initChartPitch() {
        if(pitchChart!=null){
            if (pitchChart.getData() != null &&
                    pitchChart.getData().getDataSetCount() > 0) {
                savedTimePitch++;
                isPChart=true;
            }
        }else{
            currentTime=new Date().getTime();
            pitchChart = (LineChart) findViewById(R.id.chartPitch);
            pitchChart.setViewPortOffsets(50, 20, 5, 60);
            pitchChart.setAutoScaleMinMaxEnabled(true);
            // no description text
//            mChart.setDescription("");
            // enable touch gestures
            pitchChart.setTouchEnabled(true);
            // enable scaling and dragging
            pitchChart.setDragEnabled(false);
            pitchChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            pitchChart.setPinchZoom(false);
            pitchChart.setDrawGridBackground(false);
            //mChart.setMaxHighlightDistance(400);
            XAxis x = pitchChart.getXAxis();
            x.setLabelCount(8, false);
            x.setEnabled(true);
            x.setTypeface(tf);
            x.setTextColor(Color.WHITE);
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setDrawGridLines(true);
            x.setAxisLineColor(Color.WHITE);
            YAxis y = pitchChart.getAxisLeft();
            y.setLabelCount(4, false);
            y.setTextColor(Color.WHITE);
            y.setTypeface(tf);
            y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            y.setDrawGridLines(false);
            y.setAxisLineColor(Color.WHITE);
            y.setAxisMinValue(0);
            y.setAxisMaxValue(600);
            pitchChart.getAxisRight().setEnabled(true);
            yPVals = new ArrayList<Entry>();
            yPVals.add(new Entry(0,0));
            LineDataSet set1 = new LineDataSet(yPVals, "DataSet 1");
            set1.setValueTypeface(tf);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.02f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setCircleColor(Color.GREEN);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.WHITE);
            set1.setFillColor(Color.GREEN);
            set1.setFillAlpha(50);
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return -10;
                }
            });
            LineData data;
            if (pitchChart.getData() != null && pitchChart.getData().getDataSetCount() > 0) {
                data =  pitchChart.getLineData();
                data.clearValues();
                data.removeDataSet(0);
                data.addDataSet(set1);
            }else {
                data = new LineData(set1);
            }

            data.setValueTextSize(9f);
            data.setDrawValues(false);
            pitchChart.setData(data);
            pitchChart.getLegend().setEnabled(false);
            pitchChart.animateXY(2000, 2000);
            // dont forget to refresh the drawing
            pitchChart.invalidate();
            isPChart=true;
        }

    }
    /* Sub-chant analysis */
    private void startListenAudio() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        if(bListener) {
                            volume = mRecorder.getMaxAmplitude();  //Get the sound pressure value
                            if(volume > 0 && volume < 1000000) {
                            System.out.println("*******************************Running DB");
                                World.setDbCount(20 * (float)(Math.log10(volume)));  //Change the sound pressure value to the decibel value
                                // Update with thread
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                            }
                        }
//                        if(refreshed){
//                            Thread.sleep(1000);
//                            refreshed=false;
//                        }else{
                        Thread.sleep(20);
//                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        bListener = false;
                    }
                }
            }
        });

        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                final float pitchProb = pitchDetectionResult.getProbability();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        System.out.println("*******************************Running pitch");
                        if(bListener && pitchProb>=0.8f) {
//                            System.out.println("*******************************Running pitch");
                            //                        System.out.println("pitchInHz = "+ pitchInHz + "    prob="+pitchDetectionResult.getProbability());
                            boolean ifUpdate = pitchInHz != -1.0f && pitchInHz >= 60.0f && pitchInHz <= 1000.0f; //
                            if (ifUpdate) {
                                World.setPitchCount(pitchInHz);  //Change the sound pressure value to the decibel value
                                curPVal.setText(df1.format(pitchInHz));
                                minPVal.setText(df1.format(World.minPitch));
                                mmPVal.setText(df1.format((World.minPitch + World.maxPitch) / 2));
                                maxPVal.setText(df1.format(World.maxPitch));
                                insertDataPitch(pitchInHz,0);
                            }
                        }
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            bListener=false;
                        }
                    }
                });
            }
        }));

        /*
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 1;
                        World.setPitchCount(pitchInHz);
                        World.setPitchProb(result.getProbability());

                         handlerPitch.sendMessage(message);
                         try {
                             Thread.sleep(10);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();
*/

//        thread.start();
        new Thread(dispatcher,"Audio Dispatcher").start();
    }
    /**
     * Start recording
     * @param fFile
     */
    public void startRecord(File fFile){
        try{
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            }else{
                Toast.makeText(this, getString(R.string.activity_recStartErr), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(this, getString(R.string.activity_recBusyErr), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.enter, R.anim.exit);
        File file = FileUtil.createFile("temp.amr");
        if (file != null) {
            startRecord(file);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_recFileErr), Toast.LENGTH_LONG).show();
        }
        bListener = true;
    }

    /**
     * Stop recording
     */
    @Override
    protected void onPause() {
        super.onPause();
        bListener = false;
        mRecorder.delete(); //Stop recording and delete the recording file
        thread = null;
        isChart=false;
        isPChart=false;
    }

    @Override
    protected void onDestroy() {
        if (thread != null) {
            isThreadRun = false;
            thread = null;
        }
        dbL.close();
        dbP.close();
        mRecorder.delete();
        super.onDestroy();
    }
}
