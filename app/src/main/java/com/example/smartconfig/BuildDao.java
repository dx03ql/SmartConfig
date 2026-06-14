package com.example.smartconfig;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BuildDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BuildEntity build);

    @Query("SELECT * FROM builds ORDER BY createdAt DESC")
    List<BuildEntity> getAll();

    @Query("SELECT COUNT(*) FROM builds")
    int countBuilds();

    /** Total parts across all builds (0 if no builds). */
    @Query("SELECT COALESCE(SUM(partCount), 0) FROM builds")
    int totalPartsSaved();

    @Query("SELECT * FROM builds WHERE id = :id LIMIT 1")
    BuildEntity findById(String id);

    @Delete
    void delete(BuildEntity build);

    @Query("DELETE FROM builds WHERE id = :id")
    void deleteById(String id);
}