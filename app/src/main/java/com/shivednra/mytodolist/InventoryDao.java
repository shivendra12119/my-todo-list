package com.shivednra.mytodolist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY diameter ASC, length ASC")
    List<InventoryItem> getAll();

    @Query("SELECT * FROM inventory_items WHERE diameter = :diameter AND length = :length LIMIT 1")
    InventoryItem findItem(String diameter, String length);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(InventoryItem item);

    @Update
    void update(InventoryItem item);
}
