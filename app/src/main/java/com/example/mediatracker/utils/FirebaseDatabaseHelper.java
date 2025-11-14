package com.example.mediatracker.utils;

import android.util.Log;
import com.example.mediatracker.models.ListItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDatabaseHelper {
    private static final String TAG = "FirebaseDatabaseHelper";
    private static FirebaseDatabaseHelper instance;
    private DatabaseReference databaseReference;

    private FirebaseDatabaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseDatabaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseHelper();
        }
        return instance;
    }

    /**
     * Add an item to user's list
     */
    public void addItemToList(String userId, ListItem item, OnCompleteListener listener) {
        if (userId == null || userId.isEmpty()) {
            if (listener != null) {
                listener.onError("User ID is required");
            }
            return;
        }

        String listType = item.getListType();
        String itemId = item.getItemId();
        
        // Create path: users/{userId}/mylist/{listType}/{itemId}
        DatabaseReference itemRef = databaseReference
                .child("users")
                .child(userId)
                .child("mylist")
                .child(listType)
                .child(itemId);

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("itemId", item.getItemId());
        itemMap.put("title", item.getTitle());
        itemMap.put("type", item.getType());
        itemMap.put("year", item.getYear() != null ? item.getYear() : "");
        itemMap.put("poster", item.getPoster() != null ? item.getPoster() : "");
        itemMap.put("description", item.getDescription() != null ? item.getDescription() : "");
        itemMap.put("listType", item.getListType());

        itemRef.setValue(itemMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Item added successfully");
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding item", e);
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /**
     * Remove an item from user's list
     */
    public void removeItemFromList(String userId, String itemId, String listType, OnCompleteListener listener) {
        if (userId == null || userId.isEmpty()) {
            if (listener != null) {
                listener.onError("User ID is required");
            }
            return;
        }

        DatabaseReference itemRef = databaseReference
                .child("users")
                .child(userId)
                .child("mylist")
                .child(listType)
                .child(itemId);

        itemRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Item removed successfully");
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing item", e);
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /**
     * Check if an item exists in user's list
     */
    public void checkItemInList(String userId, String itemId, String listType, OnItemCheckListener listener) {
        if (userId == null || userId.isEmpty()) {
            if (listener != null) {
                listener.onResult(false);
            }
            return;
        }

        DatabaseReference itemRef = databaseReference
                .child("users")
                .child(userId)
                .child("mylist")
                .child(listType)
                .child(itemId);

        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                if (listener != null) {
                    listener.onResult(exists);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error checking item", error.toException());
                if (listener != null) {
                    listener.onResult(false);
                }
            }
        });
    }

    /**
     * Load all items from a specific list type for a user
     */
    public void loadListItems(String userId, String listType, OnListLoadListener listener) {
        if (userId == null || userId.isEmpty()) {
            if (listener != null) {
                listener.onListLoaded(new ArrayList<>());
            }
            return;
        }

        DatabaseReference listRef = databaseReference
                .child("users")
                .child(userId)
                .child("mylist")
                .child(listType);

        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ListItem> items = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ListItem item = itemSnapshot.getValue(ListItem.class);
                    if (item != null) {
                        // Ensure userId and itemId are set correctly
                        item.setUserId(userId);
                        if (item.getItemId() == null || item.getItemId().isEmpty()) {
                            item.setItemId(itemSnapshot.getKey());
                        }
                        items.add(item);
                    }
                }
                if (listener != null) {
                    listener.onListLoaded(items);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error loading list items", error.toException());
                if (listener != null) {
                    listener.onListLoaded(new ArrayList<>());
                }
            }
        });
    }

    /**
     * Load all items (both watchlist and toread) for a user
     */
    public void loadAllListItems(String userId, OnListLoadListener listener) {
        if (userId == null || userId.isEmpty()) {
            if (listener != null) {
                listener.onListLoaded(new ArrayList<>());
            }
            return;
        }

        DatabaseReference userListRef = databaseReference
                .child("users")
                .child(userId)
                .child("mylist");

        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ListItem> items = new ArrayList<>();
                for (DataSnapshot listTypeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot itemSnapshot : listTypeSnapshot.getChildren()) {
                        ListItem item = itemSnapshot.getValue(ListItem.class);
                        if (item != null) {
                            // Ensure userId and itemId are set correctly
                            item.setUserId(userId);
                            if (item.getItemId() == null || item.getItemId().isEmpty()) {
                                item.setItemId(itemSnapshot.getKey());
                            }
                            items.add(item);
                        }
                    }
                }
                if (listener != null) {
                    listener.onListLoaded(items);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error loading all list items", error.toException());
                if (listener != null) {
                    listener.onListLoaded(new ArrayList<>());
                }
            }
        });
    }

    /**
     * Add a real-time listener for list changes
     */
    public void addListListener(String userId, String listType, OnListLoadListener listener) {
        if (userId == null || userId.isEmpty()) {
            if (listener != null) {
                listener.onListLoaded(new ArrayList<>());
            }
            return;
        }

        DatabaseReference listRef = databaseReference
                .child("users")
                .child(userId)
                .child("mylist")
                .child(listType);

        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ListItem> items = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ListItem item = itemSnapshot.getValue(ListItem.class);
                    if (item != null) {
                        // Ensure userId and itemId are set correctly
                        item.setUserId(userId);
                        if (item.getItemId() == null || item.getItemId().isEmpty()) {
                            item.setItemId(itemSnapshot.getKey());
                        }
                        items.add(item);
                    }
                }
                if (listener != null) {
                    listener.onListLoaded(items);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error in list listener", error.toException());
                if (listener != null) {
                    listener.onListLoaded(new ArrayList<>());
                }
            }
        });
    }

    // Interface for completion callbacks
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    // Interface for item check callbacks
    public interface OnItemCheckListener {
        void onResult(boolean exists);
    }

    // Interface for list load callbacks
    public interface OnListLoadListener {
        void onListLoaded(List<ListItem> items);
    }
}

