package com.shivednra.mytodolist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_items")
public class TodoItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String task;
    private boolean isCompleted;

    public TodoItem(String task) {
        this.task = task;
        this.isCompleted = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
