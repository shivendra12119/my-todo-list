package com.shivednra.mytodolist;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private AppDatabase db;
    private InventoryPagerAdapter pagerAdapter;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processImage(uri);
                }
            });

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

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new InventoryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Stock"); break;
                case 1: tab.setText("Add"); break;
                case 2: tab.setText("Remove"); break;
            }
        }).attach();
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                refreshActiveFragment();
            }
        });
    }

    public void launchPhotoPicker() {
        mGetContent.launch("image/*");
    }

    public void showManualEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.manual_entry_title);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputSize = new EditText(this);
        inputSize.setHint(R.string.hint_diameter_length);
        layout.addView(inputSize);

        final EditText inputRef = new EditText(this);
        inputRef.setHint(R.string.hint_ref);
        layout.addView(inputRef);

        final EditText inputLot = new EditText(this);
        inputLot.setHint(R.string.hint_lot);
        layout.addView(inputLot);

        final EditText inputMfg = new EditText(this);
        inputMfg.setHint(R.string.hint_mfg);
        layout.addView(inputMfg);

        final EditText inputExp = new EditText(this);
        inputExp.setHint(R.string.hint_exp);
        layout.addView(inputExp);

        builder.setView(layout);

        builder.setPositiveButton(R.string.button_add, (dialog, which) -> {
            String sizeStr = inputSize.getText().toString().trim();
            Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[xX]\\s*(\\d+(?:\\.\\d+)?)");
            Matcher m = p.matcher(sizeStr);
            
            if (m.find()) {
                performUpdate(m.group(1), m.group(2), 
                        inputRef.getText().toString().trim(),
                        inputLot.getText().toString().trim(),
                        inputMfg.getText().toString().trim(),
                        inputExp.getText().toString().trim(), 1, "MANUAL");
            } else {
                Toast.makeText(this, "Invalid Size format", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void showEditItemDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Packet Details");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputSize = new EditText(this);
        inputSize.setHint(R.string.hint_diameter_length);
        inputSize.setText(item.getDiameter() + "X" + item.getLength());
        layout.addView(inputSize);

        final EditText inputRef = new EditText(this);
        inputRef.setHint(R.string.hint_ref);
        inputRef.setText(item.getRef());
        layout.addView(inputRef);

        final EditText inputLot = new EditText(this);
        inputLot.setHint(R.string.hint_lot);
        inputLot.setText(item.getLot());
        layout.addView(inputLot);

        final EditText inputMfg = new EditText(this);
        inputMfg.setHint(R.string.hint_mfg);
        inputMfg.setText(item.getMfgDate());
        layout.addView(inputMfg);

        final EditText inputExp = new EditText(this);
        inputExp.setHint(R.string.hint_exp);
        inputExp.setText(item.getExpDate());
        layout.addView(inputExp);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String sizeStr = inputSize.getText().toString().trim();
            Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[xX]\\s*(\\d+(?:\\.\\d+)?)");
            Matcher m = p.matcher(sizeStr);
            
            if (m.find()) {
                long time = System.currentTimeMillis();
                item.setDiameter(m.group(1));
                item.setLength(m.group(2));
                item.setRef(inputRef.getText().toString().trim());
                item.setLot(inputLot.getText().toString().trim());
                item.setMfgDate(inputMfg.getText().toString().trim());
                item.setExpDate(inputExp.getText().toString().trim());
                
                db.inventoryDao().update(item);
                db.transactionDao().insert(new InventoryTransaction("EDITED", "MANUAL", time, item.getDiameter(), item.getLength(), item.getRef(), item.getLot()));
                
                refreshActiveFragment();
                Toast.makeText(this, "Packet updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid Size format", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Remove", (dialog, which) -> new AlertDialog.Builder(this)
                .setTitle("Confirm Removal")
                .setMessage("Are you sure you want to remove this packet from inventory?")
                .setPositiveButton("Remove", (d, w) -> {
                    long time = System.currentTimeMillis();
                    item.setRemovedTime(time);
                    db.inventoryDao().update(item);
                    db.transactionDao().insert(new InventoryTransaction("REMOVED", "MANUAL", time, item.getDiameter(), item.getLength(), item.getRef(), item.getLot()));
                    refreshActiveFragment();
                    Toast.makeText(this, "Packet removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.RED);
        });
        dialog.show();
    }

    public void showDetailsDialog(InventoryItem item) {
        StringBuilder message = new StringBuilder();
        message.append("REF: ").append(item.getRef() != null ? item.getRef() : "N/A").append("\n");
        message.append("LOT: ").append(item.getLot() != null ? item.getLot() : "N/A").append("\n");
        message.append("MFG: ").append(item.getMfgDate() != null ? item.getMfgDate() : "N/A").append("\n");
        message.append("EXP: ").append(item.getExpDate() != null ? item.getExpDate() : "N/A").append("\n");

        new AlertDialog.Builder(this)
                .setTitle("Details: " + item.getDiameter() + " X " + item.getLength())
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
                        refreshActiveFragment();
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
        int currentTab = viewPager.getCurrentItem();
        int change = currentTab == 1 ? 1 : -1;
        StringBuilder summary = new StringBuilder();

        for (int i = 0; i < count; i++) {
            String dia = sizes.get(i)[0];
            String len = sizes.get(i)[1];
            String ref = i < refs.size() ? refs.get(i) : "";
            String lot = i < lots.size() ? lots.get(i) : "";
            String mfg = (i * 2) < dates.size() ? dates.get(i * 2) : "";
            String exp = (i * 2 + 1) < dates.size() ? dates.get(i * 2 + 1) : "";

            performUpdate(dia, len, ref, lot, mfg, exp, change, "SCAN");
            if (i > 0) summary.append(", ");
            summary.append(dia).append("X").append(len);
        }

        if (count > 0) {
            refreshActiveFragment();
            InventoryTabFragment currentFrag = pagerAdapter.getFragment(viewPager.getCurrentItem());
            if (currentFrag != null) {
                currentFrag.updateResultText(getString(R.string.extracted_summary, count, summary.toString()));
            }
            Toast.makeText(this, "Processed " + count + " items", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.msg_not_found), Toast.LENGTH_LONG).show();
        }
    }

    private void performUpdate(String dia, String len, String ref, String lot, String mfg, String exp, int change, String method) {
        long time = System.currentTimeMillis();
        if (change > 0) {
            db.inventoryDao().insert(new InventoryItem(dia, len, ref, lot, mfg, exp));
            db.transactionDao().insert(new InventoryTransaction("ADDED", method, time, dia, len, ref, lot));
        } else {
            InventoryItem toRemove = db.inventoryDao().findOldestActiveStock(dia, len);
            if (toRemove != null) {
                toRemove.setRemovedTime(time);
                db.inventoryDao().update(toRemove);
                db.transactionDao().insert(new InventoryTransaction("REMOVED", method, time, dia, len, toRemove.getRef(), toRemove.getLot()));
            } else {
                Toast.makeText(this, "Active stock for " + dia + "X" + len + " not found.", Toast.LENGTH_SHORT).show();
            }
        }
        refreshActiveFragment();
    }

    private void refreshActiveFragment() {
        InventoryTabFragment fragment = pagerAdapter.getFragment(viewPager.getCurrentItem());
        if (fragment != null) {
            fragment.refreshList();
        }
    }

    private static class InventoryPagerAdapter extends FragmentStateAdapter {
        private final List<InventoryTabFragment> fragments = new ArrayList<>();

        public InventoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            fragments.add(InventoryTabFragment.newInstance(0));
            fragments.add(InventoryTabFragment.newInstance(1));
            fragments.add(InventoryTabFragment.newInstance(2));
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        public InventoryTabFragment getFragment(int position) {
            return fragments.get(position);
        }
    }
}
