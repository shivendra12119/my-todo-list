package com.shivednra.mytodolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryDisplayItem> inventoryList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(InventoryDisplayItem item);
    }

    // Helper class for UI display
    public static class InventoryDisplayItem {
        public String diameter;
        public String length;
        public int quantity;

        public InventoryDisplayItem(String diameter, String length, int quantity) {
            this.diameter = diameter;
            this.length = length;
            this.quantity = quantity;
        }
    }

    public InventoryAdapter(List<InventoryDisplayItem> inventoryList, OnItemClickListener listener) {
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
        InventoryDisplayItem item = inventoryList.get(position);
        holder.textViewSize.setText(item.diameter + " X " + item.length);
        holder.textViewQuantity.setText("Qty: " + item.quantity);
        
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

    public void updateList(List<InventoryDisplayItem> newList) {
        this.inventoryList = newList;
        notifyDataSetChanged();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSize;
        TextView textViewQuantity;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSize = itemView.findViewById(R.id.textViewTask);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
        }
    }
}
