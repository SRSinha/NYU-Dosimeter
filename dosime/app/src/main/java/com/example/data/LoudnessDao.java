package com.example.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LoudnessDao {
//    2022-04-26 17:12:26.437
    @Query("Select * from loudness")
    List<loudnessDB> getLoudnessList();

    @Query("Select * from loudness WHERE tid LIKE :date || '%'")
    List<loudnessDB> getLoudnessDateList(String date);          //all from that date, ms level

    @Query("Select tid, AVG(value), AVG(min), AVG(max) as tid, value, min, max from loudness WHERE tid LIKE :date || '%' GROUP BY SUBSTR(tid, 12, 2)")
    List<loudnessDB> getLoudnessDateHourlyList(String date);    //hourly-level

    @Query("Select tid, AVG(value), AVG(min), AVG(max) as tid, value, min, max from loudness WHERE tid LIKE :date || '%' GROUP BY SUBSTR(tid, 12, 5)")
    List<loudnessDB> getLoudnessDateMinlyList(String date);    //min-level

    @Query("Select tid, AVG(value), AVG(min), AVG(max) as tid, value, min, max from loudness WHERE tid LIKE :date || '%' GROUP BY SUBSTR(tid, 12, 8)")
    List<loudnessDB> getLoudnessDateSecondlyList(String date);    //sescond-level

    @Query("Select count(*) from loudness WHERE tid LIKE :date || '%'")
    int getLoudnessDateTotalEntries(String date);       //count of 1 day msec entries

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLoudness(loudnessDB val);

    @Delete
    void deleteLoudness(loudnessDB val);

}
