package com.example.mediatracker.adapters;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mediatracker.R;
import com.example.mediatracker.models.Book;
import com.example.mediatracker.models.ListItem;
import com.example.mediatracker.utils.FirebaseDatabaseHelper;
import com.squareup.picasso.Picasso;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books;
    private android.content.Context context;
    private SharedPreferences prefs;
    private FirebaseDatabaseHelper firebaseHelper;

    public BookAdapter(android.content.Context context, List<Book> books) {
        this.context = context;
        this.books = books;
        this.firebaseHelper = FirebaseDatabaseHelper.getInstance();
        
        // Initialize SharedPreferences
        prefs = context.getSharedPreferences("MediaTrackerPrefs", android.content.Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        Book.VolumeInfo volumeInfo = book.getVolumeInfo();
        
        if (volumeInfo != null) {
            holder.tvTitle.setText(volumeInfo.getTitle());
            
            holder.tvYear.setText(volumeInfo.getPublishedDate() != null ? 
                    volumeInfo.getPublishedDate() : "N/A");
            
            // Set rating
            if (volumeInfo.getAverageRating() != null && volumeInfo.getAverageRating() > 0) {
                holder.tvRating.setText(String.format("%.1f/5.0", volumeInfo.getAverageRating()));
                holder.tvRating.setVisibility(View.VISIBLE);
            } else {
                holder.tvRating.setVisibility(View.GONE);
            }

            // Load book cover image
            String imageUrl = null;
            if (volumeInfo.getImageLinks() != null) {
                // Try to get thumbnail first, then fallback to smallThumbnail
                if (volumeInfo.getImageLinks().getThumbnail() != null 
                        && !volumeInfo.getImageLinks().getThumbnail().isEmpty()) {
                    imageUrl = volumeInfo.getImageLinks().getThumbnail();
                } else if (volumeInfo.getImageLinks().getSmallThumbnail() != null 
                        && !volumeInfo.getImageLinks().getSmallThumbnail().isEmpty()) {
                    imageUrl = volumeInfo.getImageLinks().getSmallThumbnail();
                }
            }
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Fix HTTP to HTTPS for Google Books images (Android blocks HTTP)
                if (imageUrl.startsWith("http://")) {
                    imageUrl = imageUrl.replace("http://", "https://");
                }
                
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.movie_poster_default)
                        .error(R.drawable.broken_image_icon)
                        .fit()
                        .centerCrop()
                        .into(holder.ivCover, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                // Image loaded successfully
                            }

                            @Override
                            public void onError(Exception e) {
                                // Set broken image icon on error
                                holder.ivCover.setImageResource(R.drawable.broken_image_icon);
                            }
                        });
            } else {
                holder.ivCover.setImageResource(R.drawable.broken_image_icon);
            }

            String userId = prefs.getString("user_id", null);
            final int currentPosition = position;
            
            // Check if book is in to-read list
            if (userId != null && !userId.isEmpty()) {
                firebaseHelper.checkItemInList(userId, book.getId(), "toread", 
                        new FirebaseDatabaseHelper.OnItemCheckListener() {
                            @Override
                            public void onResult(boolean exists) {
                                holder.btnAddToRead.setImageResource(exists ? 
                                        android.R.drawable.star_big_on : android.R.drawable.ic_input_add);
                                
                                holder.btnAddToRead.setOnClickListener(v -> {
                                    if (userId == null || userId.isEmpty()) {
                                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if (exists) {
                                        // Remove from to-read list
                                        firebaseHelper.removeItemFromList(userId, book.getId(), "toread",
                                                new FirebaseDatabaseHelper.OnCompleteListener() {
                                                    @Override
                                                    public void onSuccess() {
                                                        Toast.makeText(context, "Removed from to-read list", Toast.LENGTH_SHORT).show();
                                                        notifyItemChanged(currentPosition);
                                                    }

                                                    @Override
                                                    public void onError(String error) {
                                                        Toast.makeText(context, "Failed to remove: " + error, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // Add to to-read list
                                        String description = volumeInfo.getDescription() != null ? 
                                                volumeInfo.getDescription() : "";
                                        String poster = volumeInfo.getImageLinks() != null && 
                                                volumeInfo.getImageLinks().getThumbnail() != null ?
                                                volumeInfo.getImageLinks().getThumbnail() : "";
                                        
                                        ListItem listItem = new ListItem();
                                        listItem.setUserId(userId);
                                        listItem.setItemId(book.getId());
                                        listItem.setTitle(volumeInfo.getTitle());
                                        listItem.setType("book");
                                        listItem.setYear(volumeInfo.getPublishedDate());
                                        listItem.setPoster(poster);
                                        listItem.setDescription(description);
                                        listItem.setListType("toread");

                                        firebaseHelper.addItemToList(userId, listItem,
                                                new FirebaseDatabaseHelper.OnCompleteListener() {
                                                    @Override
                                                    public void onSuccess() {
                                                        Toast.makeText(context, "Added to to-read list", Toast.LENGTH_SHORT).show();
                                                        notifyItemChanged(currentPosition);
                                                    }

                                                    @Override
                                                    public void onError(String error) {
                                                        Toast.makeText(context, "Failed to add: " + error, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                            }
                        });
            } else {
                holder.btnAddToRead.setImageResource(android.R.drawable.ic_input_add);
                holder.btnAddToRead.setOnClickListener(v -> {
                    Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    public void updateList(List<Book> newBooks) {
        this.books = newBooks;
        notifyDataSetChanged();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvYear, tvRating;
        ImageButton btnAddToRead;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnAddToRead = itemView.findViewById(R.id.btnAddToRead);
        }
    }
}

