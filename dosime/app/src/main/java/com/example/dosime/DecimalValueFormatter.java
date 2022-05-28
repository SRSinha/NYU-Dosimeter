package com.example.dosime;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class DecimalValueFormatter extends ValueFormatter{

    private DecimalFormat mFormat;

    public DecimalValueFormatter() {
        mFormat = new DecimalFormat("####.0");
    }

    @Override
    public String getFormattedValue(float value) {
        // write your logic here
        System.out.println(mFormat.format(value));
        return mFormat.format(value); // e.g. append a dollar-sign
    }
}