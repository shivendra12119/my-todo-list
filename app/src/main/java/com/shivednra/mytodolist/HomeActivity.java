package com.shivednra.mytodolist;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialCardView cardTodo = findViewById(R.id.cardTodo);
        MaterialCardView cardInventory = findViewById(R.id.cardInventory);

        cardTodo.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TodoActivity.class);
            startActivity(intent);
        });

        cardInventory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InventoryActivity.class);
            startActivity(intent);
        });
    }
}
