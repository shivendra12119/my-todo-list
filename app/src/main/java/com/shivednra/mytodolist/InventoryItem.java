package com.shivednra.mytodolist;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_items")
public class InventoryItem {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String diameter;
    @NonNull
    private String length;

    private String ref;
    private String lot;
    private String mfgDate;
    private String expDate;

    private long addedTime;
    private long removedTime; // 0 if not removed

    public InventoryItem(@NonNull String diameter, @NonNull String length, String ref, String lot, String mfgDate, String expDate) {
        this.diameter = diameter;
        this.length = length;
        this.ref = ref;
        this.lot = lot;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
        this.addedTime = System.currentTimeMillis();
        this.removedTime = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getDiameter() { return diameter; }
    public void setDiameter(@NonNull String diameter) { this.diameter = diameter; }

    @NonNull
    public String getLength() { return length; }
    public void setLength(@NonNull String length) { this.length = length; }

    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public String getMfgDate() { return mfgDate; }
    public void setMfgDate(String mfgDate) { this.mfgDate = mfgDate; }

    public String getExpDate() { return expDate; }
    public void setExpDate(String expDate) { this.expDate = expDate; }

    public long getAddedTime() { return addedTime; }
    public void setAddedTime(long addedTime) { this.addedTime = addedTime; }

    public long getRemovedTime() { return removedTime; }
    public void setRemovedTime(long removedTime) { this.removedTime = removedTime; }
}
