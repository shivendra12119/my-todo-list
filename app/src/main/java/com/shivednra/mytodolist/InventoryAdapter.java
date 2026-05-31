package com.shivednra.mytodolist;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryItem> inventoryList;
    private List<Integer> backgroundColors;
    private OnItemClickListener listener;
    private int mode; // 0: Stock, 1: Add, 2: Remove

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public InventoryAdapter(List<InventoryItem> inventoryList, int mode, OnItemClickListener listener) {
        this.inventoryList = inventoryList;
        this.mode = mode;
        this.listener = listener;
        calculateColors();
    }

    private void calculateColors() {
        backgroundColors = new ArrayList<>();
        if (inventoryList == null || inventoryList.isEmpty()) return;

        int currentColorIndex = 0; // 0 for white, 1 for light gray
        backgroundColors.add(currentColorIndex);

        for (int i = 1; i < inventoryList.size(); i++) {
            InventoryItem current = inventoryList.get(i);
            InventoryItem previous = inventoryList.get(i - 1);

            // If size is different, flip the color
            if (!current.getDiameter().equals(previous.getDiameter()) || 
                !current.getLength().equals(previous.getLength())) {
                currentColorIndex = 1 - currentColorIndex;
            }
            backgroundColors.add(currentColorIndex);
        }
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_row, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = inventoryList.get(position);
        holder.textViewSize.setText(item.getDiameter() + " X " + item.getLength());
        
        String rightText = "";
        if (mode == 0) { // Stock -> Show Expiry
            String expDate = item.getExpDate();
            rightText = "No Exp";
            if (expDate != null && expDate.length() >= 7) {
                try {
                    String year = expDate.substring(0, 4);
                    String month = expDate.substring(5, 7);
                    rightText = month + "/" + year;
                } catch (Exception e) {
                    rightText = expDate;
                }
            }
        } else { // Add or Remove -> Show Time
            long time = (mode == 1) ? item.getAddedTime() : item.getRemovedTime();
            if (time > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
                rightText = sdf.format(new Date(time));
            } else {
                rightText = "N/A";
            }
        }
        
        holder.textViewRight.setText(rightText);

        // Apply background color based on grouping (only relevant for Stock tab usually)
        int color;
        if (backgroundColors != null && position < backgroundColors.size() && backgroundColors.get(position) == 1) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.light_gray);
        } else {
            color = Color.WHITE;
        }
        holder.itemView.setBackgroundColor(color);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryList == null ? 0 : inventoryList.size();
    }

    public void updateList(List<InventoryItem> newList, int newMode) {
        this.inventoryList = newList;
        this.mode = newMode;
        calculateColors();
        notifyDataSetChanged();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSize;
        TextView textViewRight;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSize = itemView.findViewById(R.id.textViewTask);
            textViewRight = itemView.findViewById(R.id.textViewQuantity);
        }
    }
}
