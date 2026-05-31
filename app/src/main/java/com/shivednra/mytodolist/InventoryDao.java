package com.shivednra.mytodolist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InventoryDao {
    @Query("SELECT * FROM inventory_items WHERE removedTime = 0 ORDER BY diameter ASC, length ASC, expDate ASC")
    List<InventoryItem> getAllActive();

    @Query("SELECT * FROM inventory_items WHERE diameter = :diameter AND length = :length AND removedTime = 0 ORDER BY expDate ASC LIMIT 1")
    InventoryItem findOldestActiveStock(String diameter, String length);

    @Query("SELECT * FROM inventory_items WHERE diameter = :diameter AND length = :length AND removedTime = 0 ORDER BY expDate ASC")
    List<InventoryItem> getAllActivePacketsForSize(String diameter, String length);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(InventoryItem item);

    @Update
    void update(InventoryItem item);

    @Query("UPDATE inventory_items SET removedTime = :time WHERE removedTime = 0")
    void softDeleteAll(long time);
}
