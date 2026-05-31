package com.shivednra.mytodolist;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class MainActivity extends AppCompatActivity {

    private EditText editTextTask;
    private Button buttonAdd;
    private Button buttonUploadPhoto;
    private RadioButton radioAdd;
    private TextView textViewResult;
    private RecyclerView recyclerViewTasks;
    private InventoryAdapter adapter;
    private List<InventoryItem> inventoryList;
    private AppDatabase db;

    private ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextTask = findViewById(R.id.editTextTask);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        radioAdd = findViewById(R.id.radioAdd);
        textViewResult = findViewById(R.id.textViewResult);
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);

        inventoryList = db.inventoryDao().getAll();
        adapter = new InventoryAdapter(inventoryList);

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(adapter);

        buttonAdd.setOnClickListener(v -> {
            String size = editTextTask.getText().toString();
            if (!size.isEmpty()) {
                performUpdate(size, 1);
                refreshInventoryList();
                editTextTask.setText("");
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

    private void processImage(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String resultText = visionText.getText();
                        extractSize(resultText);
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, getString(R.string.msg_failed_recognize), Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractSize(String text) {
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*[xX]\\s*(\\d+(\\.\\d+)?)\\s*(mm|cm|inch|m)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        int count = 0;
        int change = radioAdd.isChecked() ? 1 : -1;
        StringBuilder foundSizes = new StringBuilder();

        while (matcher.find()) {
            String size = matcher.group(0);
            performUpdate(size, change);
            if (count > 0) {
                foundSizes.append(", ");
            }
            foundSizes.append(size);
            count++;
        }

        if (count > 0) {
            refreshInventoryList();
            textViewResult.setText(getString(R.string.extracted_summary, count, foundSizes.toString()));
            Toast.makeText(this, "Successfully processed " + count + " items", Toast.LENGTH_SHORT).show();
        } else {
            textViewResult.setText(getString(R.string.extracted_size_not_found));
            Toast.makeText(this, getString(R.string.msg_not_found), Toast.LENGTH_LONG).show();
        }
    }

    private void performUpdate(String size, int change) {
        InventoryItem existingItem = db.inventoryDao().findBySize(size);
        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + change;
            if (newQty < 0) newQty = 0;
            existingItem.setQuantity(newQty);
            db.inventoryDao().update(existingItem);
        } else if (change > 0) {
            InventoryItem newItem = new InventoryItem(size, change);
            db.inventoryDao().insert(newItem);
        } else {
            Toast.makeText(this, "Cannot reduce. Size " + size + " not in inventory.", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshInventoryList() {
        inventoryList = db.inventoryDao().getAll();
        adapter.updateList(inventoryList);
    }
}
