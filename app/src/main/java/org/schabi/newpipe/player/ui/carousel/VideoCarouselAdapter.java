package org.schabi.newpipe.player.ui.carousel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.schabi.newpipe.R;
import org.schabi.newpipe.player.helper.PlayerHelper;
import org.schabi.newpipe.util.Localization;
import org.schabi.newpipe.util.image.CoilHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the video carousel RecyclerView
 */
public class VideoCarouselAdapter extends RecyclerView.Adapter<VideoCarouselAdapter.CarouselViewHolder> {
    
    private List<VideoCarouselItem> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(VideoCarouselItem item, int position);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setItems(List<VideoCarouselItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void updateCurrentVideoPosition(int position) {
        for (int i = 0; i < items.size(); i++) {
            VideoCarouselItem item = items.get(i);
            if (item.getPosition() == position) {
                // Update the item to mark it as current
                items.set(i, new VideoCarouselItem(
                    item.getStreamInfoItem(),
                    true,
                    item.getPosition()
                ));
                notifyItemChanged(i);
            } else if (item.isCurrentVideo()) {
                // Update previously current item
                items.set(i, new VideoCarouselItem(
                    item.getStreamInfoItem(),
                    false,
                    item.getPosition()
                ));
                notifyItemChanged(i);
            }
        }
    }
    
    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_carousel_item, parent, false);
        return new CarouselViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        VideoCarouselItem item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    class CarouselViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView durationView;
        private final TextView titleView;
        private final View currentVideoIndicator;
        
        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.itemThumbnail);
            durationView = itemView.findViewById(R.id.itemDuration);
            titleView = itemView.findViewById(R.id.itemTitle);
            currentVideoIndicator = itemView.findViewById(R.id.currentVideoIndicator);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(items.get(position), position);
                }
            });
        }
        
        public void bind(VideoCarouselItem item) {
            // Load thumbnail using CoilHelper like other parts of NewPipe
            if (item.getThumbnails() != null && !item.getThumbnails().isEmpty()) {
                CoilHelper.INSTANCE.loadThumbnail(thumbnailView, item.getThumbnails());
            } else {
                thumbnailView.setImageResource(R.drawable.placeholder_thumbnail_video);
            }
            
            // Show duration if available
            if (item.getDuration() > 0) {
                durationView.setText(Localization.getDurationString(item.getDuration()));
                durationView.setVisibility(View.VISIBLE);
            } else {
                durationView.setVisibility(View.GONE);
            }
            
            // Set title (usually invisible but useful for debugging)
            titleView.setText(item.getTitle());
            
            // Show current video indicator
            currentVideoIndicator.setVisibility(item.isCurrentVideo() ? View.VISIBLE : View.GONE);
            
            // Scale effect for current video
            float scale = item.isCurrentVideo() ? 1.1f : 1.0f;
            itemView.setScaleX(scale);
            itemView.setScaleY(scale);
        }
    }
}