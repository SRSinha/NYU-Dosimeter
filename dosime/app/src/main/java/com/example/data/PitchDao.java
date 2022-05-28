package com.example.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PitchDao {
//    2022-04-26 17:12:26.437
    @Query("Select * from pitch")
    List<pitchDB> getPitchList();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPitch(pitchDB val);

    @Delete
    void deletePitch(pitchDB val);

    @Query("Select * from pitch WHERE tid LIKE :date || '%'")
    List<pitchDB> getPitchDateList(String date);          //all from that date, ms level

    @Query("Select tid, AVG(value) as tid, value from pitch WHERE tid LIKE :date || '%' GROUP BY SUBSTR(tid, 12, 2)")
    List<pitchDB> getPitchDateHourlyList(String date);  //hourly

    @Query("Select tid, AVG(value) as tid, value from pitch WHERE tid LIKE :date || '%' GROUP BY SUBSTR(tid, 12, 5)")
    List<pitchDB> getPitchDateMinlyList(String date);   //min

    @Query("Select tid, AVG(value) as tid, value from pitch WHERE tid LIKE :date || '%' GROUP BY SUBSTR(tid, 12, 8)")
    List<pitchDB> getPitchDateSecondlyList(String date);//sec

    @Query("Select tid, AVG(value) as tid, value from pitch WHERE tid LIKE :date || '%' GROUP BY SUBSTR(tid, 1, 10)")
    List<pitchDB> getPitchDateAvg(String date);
}
