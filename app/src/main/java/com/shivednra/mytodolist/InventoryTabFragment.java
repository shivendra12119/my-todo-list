package com.shivednra.mytodolist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InventoryTabFragment extends Fragment {

    private int tabPosition;
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private View layoutButtons;
    private TextView textViewResult;
    private List<InventoryItem> inventoryList = new ArrayList<>();
    private AppDatabase db;

    public static InventoryTabFragment newInstance(int position) {
        InventoryTabFragment fragment = new InventoryTabFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabPosition = getArguments().getInt("position");
        }
        db = AppDatabase.getDatabase(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory_tab, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerViewInventory);
        layoutButtons = view.findViewById(R.id.layoutButtons);
        textViewResult = view.findViewById(R.id.textViewResult);
        Button buttonManual = view.findViewById(R.id.buttonManualEntry);
        Button buttonCamera = view.findViewById(R.id.buttonCamera);
        Button buttonUpload = view.findViewById(R.id.buttonUploadPhoto);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        if (tabPosition == 0) {
            layoutButtons.setVisibility(View.GONE);
            textViewResult.setVisibility(View.GONE);
        } else {
            layoutButtons.setVisibility(View.VISIBLE);
            textViewResult.setVisibility(View.VISIBLE);
            buttonManual.setVisibility(tabPosition == 1 ? View.VISIBLE : View.GONE);
        }

        buttonManual.setOnClickListener(v -> ((InventoryActivity)requireActivity()).showManualEntryDialog());
        buttonCamera.setOnClickListener(v -> ((InventoryActivity)requireActivity()).launchCamera());
        buttonUpload.setOnClickListener(v -> ((InventoryActivity)requireActivity()).launchPhotoPicker());

        refreshList();

        return view;
    }

    public void refreshList() {
        if (db == null || recyclerView == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();

        if (tabPosition == 0) {
            inventoryList = db.inventoryDao().getAllActive();
        } else if (tabPosition == 1) {
            inventoryList = db.inventoryDao().getRecentAdds(todayStart);
        } else {
            inventoryList = db.inventoryDao().getRecentRemoves(todayStart);
        }

        if (adapter == null) {
            adapter = new InventoryAdapter(inventoryList, tabPosition, new InventoryAdapter.OnItemInteractionListener() {
                @Override
                public void onItemClick(InventoryItem item) {
                    ((InventoryActivity)requireActivity()).showDetailsDialog(item);
                }
                @Override
                public void onItemEdit(InventoryItem item) {
                    ((InventoryActivity)requireActivity()).showEditItemDialog(item);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(inventoryList, tabPosition);
        }
    }
    
    public void updateResultText(String text) {
        if (textViewResult != null) {
            textViewResult.setText(text);
        }
    }
}
