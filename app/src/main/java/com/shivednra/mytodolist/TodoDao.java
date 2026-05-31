package com.shivednra.mytodolist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoDao {
    @Query("SELECT * FROM todo_items")
    List<TodoItem> getAll();

    @Insert
    void insert(TodoItem item);

    @Update
    void update(TodoItem item);

    @Delete
    void delete(TodoItem item);
}
