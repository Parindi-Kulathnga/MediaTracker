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
import com.example.mediatracker.models.ListItem;
import com.example.mediatracker.models.Movie;
import com.example.mediatracker.utils.FirebaseDatabaseHelper;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private android.content.Context context;
    private SharedPreferences prefs;
    private FirebaseDatabaseHelper firebaseHelper;

    public MovieAdapter(android.content.Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
        this.firebaseHelper = FirebaseDatabaseHelper.getInstance();
        
        // Initialize SharedPreferences
        prefs = context.getSharedPreferences("MediaTrackerPrefs", android.content.Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvYear.setText(movie.getYear() != null ? movie.getYear() : "N/A");
        
        // Set rating
        if (movie.getImdbRating() != null && !movie.getImdbRating().equals("N/A") && !movie.getImdbRating().isEmpty()) {
            holder.tvRating.setText(movie.getImdbRating() + "/10");
            holder.tvRating.setVisibility(View.VISIBLE);
        } else {
            holder.tvRating.setVisibility(View.GONE);
        }
        
        if (movie.getPoster() != null && !movie.getPoster().equals("N/A") && !movie.getPoster().isEmpty()) {
            String imageUrl = movie.getPoster();
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

        String userId = prefs.getString("user_id", null);
        final int currentPosition = position;
        
        // Check if movie is in watchlist
        if (userId != null && !userId.isEmpty()) {
            firebaseHelper.checkItemInList(userId, movie.getImdbID(), "watchlist", 
                    new FirebaseDatabaseHelper.OnItemCheckListener() {
                        @Override
                        public void onResult(boolean exists) {
                            holder.btnAddToWatchlist.setImageResource(exists ? 
                                    android.R.drawable.star_big_on : android.R.drawable.ic_input_add);
                            
                            holder.btnAddToWatchlist.setOnClickListener(v -> {
                                if (userId == null || userId.isEmpty()) {
                                    Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (exists) {
                                    // Remove from watchlist
                                    firebaseHelper.removeItemFromList(userId, movie.getImdbID(), "watchlist",
                                            new FirebaseDatabaseHelper.OnCompleteListener() {
                                                @Override
                                                public void onSuccess() {
                                                    Toast.makeText(context, "Removed from watchlist", Toast.LENGTH_SHORT).show();
                                                    notifyItemChanged(currentPosition);
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    Toast.makeText(context, "Failed to remove: " + error, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    // Add to watchlist
                                    ListItem listItem = new ListItem();
                                    listItem.setUserId(userId);
                                    listItem.setItemId(movie.getImdbID());
                                    listItem.setTitle(movie.getTitle());
                                    listItem.setType("movie");
                                    listItem.setYear(movie.getYear());
                                    listItem.setPoster(movie.getPoster());
                                    listItem.setDescription(movie.getPlot() != null ? movie.getPlot() : "");
                                    listItem.setListType("watchlist");

                                    firebaseHelper.addItemToList(userId, listItem,
                                            new FirebaseDatabaseHelper.OnCompleteListener() {
                                                @Override
                                                public void onSuccess() {
                                                    Toast.makeText(context, "Added to watchlist", Toast.LENGTH_SHORT).show();
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
            holder.btnAddToWatchlist.setImageResource(android.R.drawable.ic_input_add);
            holder.btnAddToWatchlist.setOnClickListener(v -> {
                Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public void updateList(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvYear, tvRating;
        ImageButton btnAddToWatchlist;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnAddToWatchlist = itemView.findViewById(R.id.btnAddToWatchlist);
        }
    }
}

