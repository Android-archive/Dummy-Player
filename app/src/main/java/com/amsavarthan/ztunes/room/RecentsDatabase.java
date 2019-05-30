package com.amsavarthan.ztunes.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {RecentsEntity.class}, version = 1)
public abstract class RecentsDatabase extends RoomDatabase {

    private static RecentsDatabase INSTANCE;

    public abstract RecentsDao recentsDao();

    private static final Object sLock = new Object();

    public static RecentsDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        RecentsDatabase.class, "recents.db")
                        .allowMainThreadQueries()
                        .build();
            }
            return INSTANCE;
        }
    }
}
