package com.shivednra.mytodolist;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<TodoItem> todoList;
    private OnTodoChangeListener listener;

    public interface OnTodoChangeListener {
        void onTodoChanged(TodoItem item);
        void onTodoLongClick(TodoItem item);
    }

    public TodoAdapter(List<TodoItem> todoList, OnTodoChangeListener listener) {
        this.todoList = todoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_row, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = todoList.get(position);
        holder.textViewTask.setText(item.getTask());
        holder.checkBox.setChecked(item.isCompleted());

        if (item.isCompleted()) {
            holder.textViewTask.setPaintFlags(holder.textViewTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewTask.setPaintFlags(holder.textViewTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.checkBox.setOnClickListener(v -> {
            item.setCompleted(holder.checkBox.isChecked());
            listener.onTodoChanged(item);
            notifyItemChanged(position);
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onTodoLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public void updateList(List<TodoItem> newList) {
        this.todoList = newList;
        notifyDataSetChanged();
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTask;
        CheckBox checkBox;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTask = itemView.findViewById(R.id.textViewTodoTask);
            checkBox = itemView.findViewById(R.id.checkBoxTodo);
        }
    }
}
