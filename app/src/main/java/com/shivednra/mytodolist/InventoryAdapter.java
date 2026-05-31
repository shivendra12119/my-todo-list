package com.shivednra.mytodolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryItem> inventoryList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public InventoryAdapter(List<InventoryItem> inventoryList, OnItemClickListener listener) {
        this.inventoryList = inventoryList;
        this.listener = listener;
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
        
        // Extract Month and Year from YYYY-MM-DD
        String expDate = item.getExpDate();
        String displayExp = "No Exp";
        if (expDate != null && expDate.length() >= 7) {
            try {
                String year = expDate.substring(0, 4);
                String month = expDate.substring(5, 7);
                displayExp = month + "/" + year;
            } catch (Exception e) {
                displayExp = expDate;
            }
        }
        
        holder.textViewExp.setText(displayExp);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    public void updateList(List<InventoryItem> newList) {
        this.inventoryList = newList;
        notifyDataSetChanged();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSize;
        TextView textViewExp;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSize = itemView.findViewById(R.id.textViewTask);
            textViewExp = itemView.findViewById(R.id.textViewQuantity);
        }
    }
}
