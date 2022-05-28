package com.example.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "loudness")
public class loudnessDB {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    public String tid;

    @ColumnInfo(name = "value")
    public float value;

    @ColumnInfo(name = "min")
    public float min;

    @ColumnInfo(name = "max")
    public float max;

    public loudnessDB() {}

    public loudnessDB(String tid, float val, float min, float max) {
        this.tid = tid;
        this.value = val;
        this.min = min;
        this.max = max;
    }
}
