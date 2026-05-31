package com.shivednra.mytodolist;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<InventoryTransaction> transactions;

    public TransactionAdapter(List<InventoryTransaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_row, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        InventoryTransaction trans = transactions.get(position);
        
        holder.textViewType.setText(trans.getType());
        if ("ADDED".equals(trans.getType())) {
            holder.textViewType.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.textViewType.setBackgroundColor(Color.parseColor("#F44336")); // Red
        }

        holder.textViewSize.setText(trans.getDiameter() + " X " + trans.getLength());
        holder.textViewDetails.setText("LOT: " + trans.getLot() + " | REF: " + trans.getRef());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        holder.textViewTime.setText(sdf.format(new Date(trans.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateList(List<InventoryTransaction> newList) {
        this.transactions = newList;
        notifyDataSetChanged();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewType, textViewSize, textViewDetails, textViewTime;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewSize = itemView.findViewById(R.id.textViewTransSize);
            textViewDetails = itemView.findViewById(R.id.textViewTransDetails);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
    }
}
