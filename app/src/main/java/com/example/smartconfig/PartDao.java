package com.example.smartconfig;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * PartDao — the SQL queries Room generates for the parts table.
 */
@Dao
public interface PartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PartEntity> parts);

    @Query("SELECT COUNT(*) FROM parts")
    int count();

    @Query("SELECT * FROM parts")
    List<PartEntity> getAll();

    @Query("SELECT * FROM parts WHERE category = :category")
    List<PartEntity> getByCategory(String category);

    @Query("SELECT * FROM parts WHERE id = :id LIMIT 1")
    PartEntity findById(String id);

    @Query("DELETE FROM parts")
    void clear();
}