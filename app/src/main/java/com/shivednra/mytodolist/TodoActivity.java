package com.shivednra.mytodolist;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TodoActivity extends AppCompatActivity {

    private EditText editTextTodo;
    private RecyclerView recyclerViewTodo;
    private TodoAdapter adapter;
    private List<TodoItem> todoList;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        db = AppDatabase.getDatabase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Todo List");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_todo), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextTodo = findViewById(R.id.editTextTodo);
        Button buttonAddTodo = findViewById(R.id.buttonAddTodo);
        recyclerViewTodo = findViewById(R.id.recyclerViewTodo);

        todoList = db.todoDao().getAll();
        adapter = new TodoAdapter(todoList, new TodoAdapter.OnTodoChangeListener() {
            @Override
            public void onTodoChanged(TodoItem item) {
                db.todoDao().update(item);
            }

            @Override
            public void onTodoLongClick(TodoItem item) {
                showDeleteConfirmation(item);
            }
        });

        recyclerViewTodo.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTodo.setAdapter(adapter);

        buttonAddTodo.setOnClickListener(v -> {
            String task = editTextTodo.getText().toString().trim();
            if (!task.isEmpty()) {
                TodoItem newItem = new TodoItem(task);
                db.todoDao().insert(newItem);
                refreshList();
                editTextTodo.setText("");
            }
        });
    }

    private void showDeleteConfirmation(TodoItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.todoDao().delete(item);
                    refreshList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshList() {
        todoList = db.todoDao().getAll();
        adapter.updateList(todoList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
