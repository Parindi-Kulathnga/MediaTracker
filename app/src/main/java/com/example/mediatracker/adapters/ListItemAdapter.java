package com.example.mediatracker.adapters;

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
import com.example.mediatracker.models.ListItem;
import com.example.mediatracker.utils.FirebaseDatabaseHelper;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder> {
    private List<ListItem> items;
    private android.content.Context context;
    private String userId;
    private FirebaseDatabaseHelper firebaseHelper;

    public ListItemAdapter(android.content.Context context, List<ListItem> items, String userId) {
        this.context = context;
        this.items = items;
        this.userId = userId;
        this.firebaseHelper = FirebaseDatabaseHelper.getInstance();
    }

    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
        ListItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvType.setText(item.getType().toUpperCase());
        holder.tvYear.setText(item.getYear() != null ? item.getYear() : "N/A");

        // Load poster image
        if (item.getPoster() != null && !item.getPoster().isEmpty() && !item.getPoster().equals("N/A")) {
            String imageUrl = item.getPoster();
            // Fix HTTP to HTTPS for images
            if (imageUrl.startsWith("http://")) {
                imageUrl = imageUrl.replace("http://", "https://");
            }
            
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.movie_poster_default)
                    .error(R.drawable.broken_image_icon)
                    .fit()
                    .centerCrop()
                    .into(holder.ivPoster, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Image loaded successfully
                        }

                        @Override
                        public void onError(Exception e) {
                            // Set broken image icon on error
                            holder.ivPoster.setImageResource(R.drawable.broken_image_icon);
                        }
                    });
        } else {
            holder.ivPoster.setImageResource(R.drawable.broken_image_icon);
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseHelper.removeItemFromList(userId, item.getItemId(), item.getListType(), 
                    new FirebaseDatabaseHelper.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(context, "Removed from list", Toast.LENGTH_SHORT).show();
                            items.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, items.size());
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "Failed to remove item: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateList(List<ListItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvType, tvYear;
        ImageButton btnRemove;

        ListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvYear = itemView.findViewById(R.id.tvYear);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

