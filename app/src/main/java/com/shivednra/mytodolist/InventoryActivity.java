package com.shivednra.mytodolist;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryActivity extends AppCompatActivity {

    private EditText editTextSize;
    private Button buttonUploadPhoto;
    private RadioButton radioAdd;
    private TextView textViewResult;
    private RecyclerView recyclerViewInventory;
    private InventoryAdapter adapter;
    private List<InventoryAdapter.InventoryDisplayItem> inventoryDisplayList;
    private AppDatabase db;

    private ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        db = AppDatabase.getDatabase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Inventory Management");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_inventory), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextSize = findViewById(R.id.editTextSize);
        Button buttonAdd = findViewById(R.id.buttonAdd);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        radioAdd = findViewById(R.id.radioAdd);
        textViewResult = findViewById(R.id.textViewResult);
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);

        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        refreshInventoryList();

        buttonAdd.setOnClickListener(v -> {
            String input = editTextSize.getText().toString().trim();
            if (!input.isEmpty()) {
                manualInputProcessing(input);
                editTextSize.setText("");
            }
        });

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processImage(uri);
                    }
                });

        buttonUploadPhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
    }

    private void manualInputProcessing(String input) {
        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[xX]\\s*(\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(input);
        if (m.find()) {
            performUpdate(m.group(1), m.group(2), "", "", "", "", 1);
            refreshInventoryList();
        } else {
            Toast.makeText(this, "Invalid format. Use: DiameterXLength", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDetailsDialog(InventoryAdapter.InventoryDisplayItem item) {
        List<InventoryItem> packets = db.inventoryDao().getAllActivePacketsForSize(item.diameter, item.length);
        
        StringBuilder message = new StringBuilder();
        message.append("Total Quantity: ").append(item.quantity).append("\n\n");
        message.append("Packets:\n");
        
        for (InventoryItem p : packets) {
            message.append("- LOT: ").append(p.getLot() == null || p.getLot().isEmpty() ? "N/A" : p.getLot())
                   .append(", EXP: ").append(p.getExpDate() == null || p.getExpDate().isEmpty() ? "N/A" : p.getExpDate())
                   .append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Details: " + item.diameter + " X " + item.length)
                .setMessage(message.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_clear_inventory) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Data")
                    .setMessage("Are you sure you want to clear the entire inventory?")
                    .setPositiveButton("Clear All", (dialog, which) -> {
                        db.inventoryDao().softDeleteAll(System.currentTimeMillis());
                        refreshInventoryList();
                        textViewResult.setText(R.string.extracted_size_none);
                        Toast.makeText(this, "Inventory cleared (Soft Deleted)", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processImage(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> extractData(visionText.getText()))
                    .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.msg_failed_recognize), Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractData(String text) {
        List<String[]> sizes = new ArrayList<>();
        Pattern sizePattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[xX]\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
        Matcher sizeMatcher = sizePattern.matcher(text);
        while (sizeMatcher.find()) {
            sizes.add(new String[]{sizeMatcher.group(1), sizeMatcher.group(2)});
        }

        List<String> refs = new ArrayList<>();
        Pattern refPattern = Pattern.compile("REF\\s*([\\d\\s.]+)", Pattern.CASE_INSENSITIVE);
        Matcher refMatcher = refPattern.matcher(text);
        while (refMatcher.find()) {
            String refVal = refMatcher.group(1);
            if (refVal != null) refs.add(refVal.replaceAll("\\s+", ""));
        }

        List<String> lots = new ArrayList<>();
        Pattern lotPattern = Pattern.compile("LOT\\s*([A-Z0-9]{5})", Pattern.CASE_INSENSITIVE);
        Matcher lotMatcher = lotPattern.matcher(text);
        while (lotMatcher.find()) {
            lots.add(lotMatcher.group(1));
        }

        List<String> dates = new ArrayList<>();
        Pattern datePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
        Matcher dateMatcher = datePattern.matcher(text);
        while (dateMatcher.find()) {
            dates.add(dateMatcher.group(1));
        }

        int count = sizes.size();
        int change = radioAdd.isChecked() ? 1 : -1;
        StringBuilder summary = new StringBuilder();

        for (int i = 0; i < count; i++) {
            String dia = sizes.get(i)[0];
            String len = sizes.get(i)[1];
            String ref = i < refs.size() ? refs.get(i) : "";
            String lot = i < lots.size() ? lots.get(i) : "";
            String mfg = (i * 2) < dates.size() ? dates.get(i * 2) : "";
            String exp = (i * 2 + 1) < dates.size() ? dates.get(i * 2 + 1) : "";

            performUpdate(dia, len, ref, lot, mfg, exp, change);
            if (i > 0) summary.append(", ");
            summary.append(dia).append("X").append(len);
        }

        if (count > 0) {
            refreshInventoryList();
            textViewResult.setText(getString(R.string.extracted_summary, count, summary.toString()));
            Toast.makeText(this, "Processed " + count + " items", Toast.LENGTH_SHORT).show();
        } else {
            textViewResult.setText(getString(R.string.extracted_size_not_found));
            Toast.makeText(this, getString(R.string.msg_not_found), Toast.LENGTH_LONG).show();
        }
    }

    private void performUpdate(String dia, String len, String ref, String lot, String mfg, String exp, int change) {
        if (change > 0) {
            db.inventoryDao().insert(new InventoryItem(dia, len, ref, lot, mfg, exp));
        } else {
            InventoryItem toRemove = db.inventoryDao().findOldestActiveStock(dia, len);
            if (toRemove != null) {
                toRemove.setRemovedTime(System.currentTimeMillis());
                db.inventoryDao().update(toRemove);
            } else {
                Toast.makeText(this, "Active stock for " + dia + "X" + len + " not found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshInventoryList() {
        List<InventoryItem> allActive = db.inventoryDao().getAllActive();
        Map<String, InventoryAdapter.InventoryDisplayItem> aggregatedMap = new LinkedHashMap<>();
        
        for (InventoryItem item : allActive) {
            String key = item.getDiameter() + "X" + item.getLength();
            if (aggregatedMap.containsKey(key)) {
                InventoryAdapter.InventoryDisplayItem displayItem = aggregatedMap.get(key);
                if (displayItem != null) displayItem.quantity += 1;
            } else {
                aggregatedMap.put(key, new InventoryAdapter.InventoryDisplayItem(item.getDiameter(), item.getLength(), 1));
            }
        }
        
        inventoryDisplayList = new ArrayList<>(aggregatedMap.values());
        if (adapter == null) {
            adapter = new InventoryAdapter(inventoryDisplayList, this::showDetailsDialog);
            recyclerViewInventory.setAdapter(adapter);
        } else {
            adapter.updateList(inventoryDisplayList);
        }
    }
}
