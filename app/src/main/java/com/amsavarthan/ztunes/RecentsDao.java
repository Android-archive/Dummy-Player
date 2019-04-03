package com.amsavarthan.ztunes;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RecentsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAll(List<RecentsEntity> recentsEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(RecentsEntity recentsEntity);

    @Update
    void update(RecentsEntity recentsEntity);

    @Delete
    void delete(RecentsEntity recentsEntity);

    @Query("SELECT * FROM recents")
    LiveData<List<RecentsEntity>> findAll();

    @Query("SELECT * FROM recents")
    List<RecentsEntity> getAll();

    @Query("SELECT COUNT(*) FROM recents")
    int countRecents();

    @Query("SELECT * FROM recents WHERE name LIKE:name")
    RecentsEntity findByName(String name);

}
