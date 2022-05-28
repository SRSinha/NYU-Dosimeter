package com.example.data;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.room.Database(entities = {pitchDB.class, loudnessDB.class}, version = 1)
public abstract class LoudnessDatabase extends RoomDatabase {
    private static LoudnessDatabase INSTANCE;
    public abstract LoudnessDao loudnessDao();

    private static final int NUMBER_OF_THREADS = 1;
    public static final ExecutorService loudnessWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static LoudnessDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, LoudnessDatabase.class, "loudness")
//Room.inMemoryDatabaseBuilder(context.getApplicationContext(), AppDatabase.class)
                            // To simplify the exercise, allow queries on the main thread.
                            // Don't do this on a real app!
                            .allowMainThreadQueries()
                            // recreate the database if necessary
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
