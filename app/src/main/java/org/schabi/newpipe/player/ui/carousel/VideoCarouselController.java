package org.schabi.newpipe.player.ui.carousel;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.schabi.newpipe.R;
import org.schabi.newpipe.player.Player;
import org.schabi.newpipe.player.playqueue.PlayQueue;
import org.schabi.newpipe.player.playqueue.PlayQueueItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the video carousel that manages the list of videos
 * and handles navigation between them.
 */
public class VideoCarouselController {
    
    private static final int MAX_HISTORY_ITEMS = 5;
    private static final int MAX_UPCOMING_ITEMS = 10;
    
    private final Player player;
    private final RecyclerView carouselRecyclerView;
    private final View carouselContainer;
    private final VideoCarouselAdapter adapter;
    private final LinearLayoutManager layoutManager;
    
    public VideoCarouselController(Player player, View rootView) {
        this.player = player;
        this.carouselContainer = rootView.findViewById(R.id.videoCarouselContainer);
        this.carouselRecyclerView = rootView.findViewById(R.id.videoCarouselRecyclerView);
        
        // Setup RecyclerView
        this.adapter = new VideoCarouselAdapter();
        this.layoutManager = new LinearLayoutManager(player.getContext(), LinearLayoutManager.HORIZONTAL, false);
        
        carouselRecyclerView.setLayoutManager(layoutManager);
        carouselRecyclerView.setAdapter(adapter);
        
        // Set click listener
        adapter.setOnItemClickListener(this::onCarouselItemClicked);
    }
    
    /**
     * Update the carousel with the current playlist and position
     */
    public void updateCarousel() {
        if (player == null || player.getPlayQueue() == null) {
            hideCarousel();
            return;
        }
        
        PlayQueue playQueue = player.getPlayQueue();
        int currentIndex = playQueue.getIndex();
        
        // Show carousel for demo/testing purposes - even with single video
        // TODO: Later, uncomment this condition to only show with multiple videos
        // if (playQueue.size() <= 1 && getHistorySize() == 0) {
        //     hideCarousel();
        //     return;
        // }
        
        List<VideoCarouselItem> carouselItems = buildCarouselItems(playQueue, currentIndex);
        
        // Always try to show carousel for testing
        adapter.setItems(carouselItems);
        showCarousel();
        
        // Scroll to center the current item
        scrollToCurrentItem(carouselItems);
    }
    
    /**
     * Build the list of carousel items including history and upcoming videos
     */
    private List<VideoCarouselItem> buildCarouselItems(PlayQueue playQueue, int currentIndex) {
        List<VideoCarouselItem> items = new ArrayList<>();
        
        // Add recent history items (to the left)
        List<VideoCarouselItem> historyItems = getHistoryItems();
        items.addAll(historyItems);
        
        // Add items from the current playlist
        int startIndex = Math.max(0, currentIndex - 2);
        int endIndex = Math.min(playQueue.size(), currentIndex + MAX_UPCOMING_ITEMS);
        
        for (int i = startIndex; i < endIndex; i++) {
            PlayQueueItem queueItem = playQueue.getItem(i);
            if (queueItem != null) {
                boolean isCurrent = (i == currentIndex);
                VideoCarouselItem carouselItem = new VideoCarouselItem(
                    queueItem.getThumbnails(),
                    queueItem.getTitle(), 
                    queueItem.getDuration(), 
                    isCurrent, 
                    i);
                items.add(carouselItem);
            }
        }
        
        return items;
    }
    
    /**
     * Get recent history items (placeholder implementation)
     * In a real implementation, this would fetch from the history database
     */
    private List<VideoCarouselItem> getHistoryItems() {
        // TODO: Implement real history fetching
        // For now, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Get the size of available history (placeholder)
     */
    private int getHistorySize() {
        // TODO: Implement real history size calculation
        return 0;
    }
    
    /**
     * Handle click on carousel item
     */
    private void onCarouselItemClicked(VideoCarouselItem item, int adapterPosition) {
        if (player == null || player.getPlayQueue() == null) {
            return;
        }
        
        // If it's a queue item, jump to that position
        if (item.getPosition() >= 0 && item.getPosition() < player.getPlayQueue().size()) {
            player.selectQueueItem(player.getPlayQueue().getItem(item.getPosition()));
        }
        // TODO: Handle history items (would need to add them to queue and play)
    }
    
    /**
     * Scroll the carousel to center the current item
     */
    private void scrollToCurrentItem(List<VideoCarouselItem> items) {
        int currentItemPosition = -1;
        
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isCurrentVideo()) {
                currentItemPosition = i;
                break;
            }
        }
        
        if (currentItemPosition >= 0) {
            // Smooth scroll to center the current item
            layoutManager.scrollToPositionWithOffset(currentItemPosition, 
                carouselRecyclerView.getWidth() / 2 - 50); // 50 is half of item width
        }
    }
    
    /**
     * Show the carousel
     */
    private void showCarousel() {
        if (carouselContainer != null) {
            carouselContainer.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Hide the carousel
     */
    private void hideCarousel() {
        if (carouselContainer != null) {
            carouselContainer.setVisibility(View.GONE);
        }
    }
    
    /**
     * Update the current video indicator when the position changes
     */
    public void onCurrentItemChanged(int newPosition) {
        adapter.updateCurrentVideoPosition(newPosition);
        
        // Re-scroll to center the new current item
        carouselRecyclerView.post(() -> {
            List<VideoCarouselItem> items = new ArrayList<>(); // Get current items
            scrollToCurrentItem(items);
        });
    }
    
    /**
     * Check if carousel should be visible based on current state
     */
    public boolean shouldShowCarousel() {
        return player != null && 
               player.getPlayQueue() != null && 
               (player.getPlayQueue().size() > 1 || getHistorySize() > 0);
    }
    
    /**
     * Get the carousel container view
     */
    public View getCarouselContainer() {
        return carouselContainer;
    }
}