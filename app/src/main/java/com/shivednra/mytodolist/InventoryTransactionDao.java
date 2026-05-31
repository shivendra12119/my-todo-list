package com.shivednra.mytodolist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InventoryTransactionDao {
    @Query("SELECT * FROM inventory_transactions ORDER BY timestamp DESC")
    List<InventoryTransaction> getAll();

    @Insert
    void insert(InventoryTransaction transaction);

    @Query("DELETE FROM inventory_transactions")
    void deleteAll();
}
