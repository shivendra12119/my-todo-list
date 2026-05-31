package com.shivednra.mytodolist;

import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryActivity extends AppCompatActivity {

    private EditText editTextSize;
    private Button buttonAdd;
    private Button buttonUploadPhoto;
    private RadioButton radioAdd;
    private TextView textViewResult;
    private RecyclerView recyclerViewInventory;
    private InventoryAdapter adapter;
    private List<InventoryItem> inventoryList;
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
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        radioAdd = findViewById(R.id.radioAdd);
        textViewResult = findViewById(R.id.textViewResult);
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);

        inventoryList = db.inventoryDao().getAll();
        adapter = new InventoryAdapter(inventoryList, this::showEditDialog);

        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInventory.setAdapter(adapter);

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
            performUpdate(m.group(1), m.group(2), 1);
            refreshInventoryList();
        } else {
            Toast.makeText(this, "Invalid format. Use: DiameterXLength", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Qty: " + item.getDiameter() + " X " + item.getLength());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.getQuantity()));
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newQtyStr = input.getText().toString();
            if (!newQtyStr.isEmpty()) {
                try {
                    int newQty = Integer.parseInt(newQtyStr);
                    showConfirmationDialog(item, newQty);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showConfirmationDialog(InventoryItem item, int newQty) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Change")
                .setMessage("Change " + item.getDiameter() + " X " + item.getLength() + " quantity to " + newQty + "?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    item.setQuantity(newQty);
                    db.inventoryDao().update(item);
                    refreshInventoryList();
                })
                .setNegativeButton("Cancel", null)
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
                    .setMessage("Are you sure you want to delete the entire inventory?")
                    .setPositiveButton("Delete All", (dialog, which) -> {
                        db.inventoryDao().deleteAll(); // Need to add this to DAO or use clearAllTables
                        db.clearAllTables();
                        refreshInventoryList();
                        textViewResult.setText(R.string.extracted_size_none);
                        Toast.makeText(this, "Inventory cleared", Toast.LENGTH_SHORT).show();
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
                    .addOnSuccessListener(visionText -> extractSize(visionText.getText()))
                    .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.msg_failed_recognize), Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractSize(String text) {
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[xX]\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        int count = 0;
        int change = radioAdd.isChecked() ? 1 : -1;
        StringBuilder foundSizes = new StringBuilder();

        while (matcher.find()) {
            String diameter = matcher.group(1);
            String length = matcher.group(2);
            performUpdate(diameter, length, change);
            if (count > 0) foundSizes.append(", ");
            foundSizes.append(diameter).append("X").append(length);
            count++;
        }

        if (count > 0) {
            refreshInventoryList();
            textViewResult.setText(getString(R.string.extracted_summary, count, foundSizes.toString()));
            Toast.makeText(this, "Processed " + count + " items", Toast.LENGTH_SHORT).show();
        } else {
            textViewResult.setText(getString(R.string.extracted_size_not_found));
            Toast.makeText(this, getString(R.string.msg_not_found), Toast.LENGTH_LONG).show();
        }
    }

    private void performUpdate(String diameter, String length, int change) {
        InventoryItem existingItem = db.inventoryDao().findItem(diameter, length);
        if (existingItem != null) {
            int newQty = Math.max(0, existingItem.getQuantity() + change);
            existingItem.setQuantity(newQty);
            db.inventoryDao().update(existingItem);
        } else if (change > 0) {
            db.inventoryDao().insert(new InventoryItem(diameter, length, change));
        } else {
            Toast.makeText(this, "Size " + diameter + "X" + length + " not found to reduce.", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshInventoryList() {
        inventoryList = db.inventoryDao().getAll();
        adapter.updateList(inventoryList);
    }
}
