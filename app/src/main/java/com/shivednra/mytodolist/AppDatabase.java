package com.shivednra.mytodolist;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {InventoryItem.class, TodoItem.class, InventoryTransaction.class}, version = 10)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InventoryDao inventoryDao();
    public abstract TodoDao todoDao();
    public abstract InventoryTransactionDao transactionDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
