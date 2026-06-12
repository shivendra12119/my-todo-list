package com.shivednra.mytodolist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_transactions")
public class InventoryTransaction {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String type; // "ADDED", "REMOVED", "EDITED"
    private String method; // "MANUAL", "SCAN"
    private String itemType; // "Implant" or "Abutment"
    private long timestamp;
    
    private String diameter;
    private String length;
    private String postHeight;
    private String ref;
    private String lot;

    public InventoryTransaction(String type, String method, String itemType, long timestamp, String diameter, String length, String postHeight, String ref, String lot) {
        this.type = type;
        this.method = method;
        this.itemType = itemType;
        this.timestamp = timestamp;
        this.diameter = diameter;
        this.length = length;
        this.postHeight = postHeight;
        this.ref = ref;
        this.lot = lot;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDiameter() { return diameter; }
    public void setDiameter(String diameter) { this.diameter = diameter; }

    public String getLength() { return length; }
    public void setLength(String length) { this.length = length; }

    public String getPostHeight() { return postHeight; }
    public void setPostHeight(String postHeight) { this.postHeight = postHeight; }

    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }
}
