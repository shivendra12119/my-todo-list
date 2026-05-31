package com.shivednra.mytodolist;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "inventory_items", primaryKeys = {"diameter", "length"})
public class InventoryItem {
    @NonNull
    private String diameter;
    @NonNull
    private String length;
    private int quantity;

    public InventoryItem(@NonNull String diameter, @NonNull String length, int quantity) {
        this.diameter = diameter;
        this.length = length;
        this.quantity = quantity;
    }

    @NonNull
    public String getDiameter() {
        return diameter;
    }

    public void setDiameter(@NonNull String diameter) {
        this.diameter = diameter;
    }

    @NonNull
    public String getLength() {
        return length;
    }

    public void setLength(@NonNull String length) {
        this.length = length;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
