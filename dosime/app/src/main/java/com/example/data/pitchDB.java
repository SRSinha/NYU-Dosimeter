package com.example.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "pitch")
public class pitchDB {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    public String tid;
    @ColumnInfo(name = "value")
    public float value;

//    @ColumnInfo(name = "min")
//    public float min;

//    @ColumnInfo(name = "max")
//    public float max;

    public pitchDB() {}

    public pitchDB(String tid, float val) {
        this.tid = tid;
        this.value = val;
    }
}
