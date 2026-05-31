package com.shivednra.mytodolist;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_items")
public class InventoryItem {
    @PrimaryKey
    @NonNull
    private String size;
    private int quantity;

    public InventoryItem(@NonNull String size, int quantity) {
        this.size = size;
        this.quantity = quantity;
    }

    @NonNull
    public String getSize() {
        return size;
    }

    public void setSize(@NonNull String size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
