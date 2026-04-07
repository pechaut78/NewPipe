package org.schabi.newpipe.player.ui.carousel;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.player.Player;
import org.schabi.newpipe.player.playqueue.PlayQueue;
import org.schabi.newpipe.player.playqueue.PlayQueueItem;
import org.schabi.newpipe.player.playqueue.SinglePlayQueue;
import org.schabi.newpipe.util.NavigationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the video carousel that manages the list of videos
 * and handles navigation between them.
 */
public class VideoCarouselController {
    
    private static final String TAG = "VideoCarouselController";
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
        
        Log.d(TAG, "VideoCarouselController initialized with adapter: " + adapter);
        
        // Add touch listener to maintain controls visibility during scroll
        carouselRecyclerView.setOnTouchListener((v, event) -> {
            Log.d(TAG, "Touch event on carousel: " + event.getAction());
            maintainControlsVisibility();
            return false; // Let RecyclerView handle the touch event
        });
        
        // Add scroll listener to maintain controls visibility during scroll
        carouselRecyclerView.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(androidx.recyclerview.widget.RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING ||
                    newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING) {
                    Log.d(TAG, "Carousel scrolling, maintaining controls visibility");
                    maintainControlsVisibility();
                }
            }
        });
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
        
        // If there's only one video in the queue, use related items from "nouveautés"
        if (playQueue.size() == 1) {
            return buildCarouselFromRelatedItems(currentIndex);
        }
        
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
     * Build carousel items using related videos when only one video is in the queue
     */
    private List<VideoCarouselItem> buildCarouselFromRelatedItems(int currentIndex) {
        List<VideoCarouselItem> items = new ArrayList<>();
        
        // Get current stream info to access related items
        if (player != null && player.getCurrentStreamInfo().isPresent()) {
            StreamInfo currentStreamInfo = player.getCurrentStreamInfo().get();
            
            // Get related items (these are the "nouveautés" or trending videos)
            List<InfoItem> relatedItems = currentStreamInfo.getRelatedItems();
            if (relatedItems != null && !relatedItems.isEmpty()) {
                
                List<StreamInfoItem> streamItems = relatedItems.stream()
                    .filter(item -> item instanceof StreamInfoItem)
                    .map(item -> (StreamInfoItem) item)
                    .collect(java.util.stream.Collectors.toList());
                
                // Add videos before current video (left side of carousel) 
                int maxBefore = Math.min(3, streamItems.size() / 2);
                for (int i = 0; i < maxBefore; i++) {
                    StreamInfoItem streamItem = streamItems.get(i);
                    VideoCarouselItem carouselItem = new VideoCarouselItem(
                        streamItem, // Pass the StreamInfoItem directly
                        false, // Not current video
                        -1 // Related video, not in queue
                    );
                    items.add(carouselItem);
                }
                
                // Add current video in the center
                PlayQueueItem currentQueueItem = player.getPlayQueue().getItem(currentIndex);
                if (currentQueueItem != null) {
                    VideoCarouselItem currentItem = new VideoCarouselItem(
                        currentQueueItem.getThumbnails(),
                        currentQueueItem.getTitle(),
                        currentQueueItem.getDuration(),
                        true, // Current video
                        currentIndex
                    );
                    items.add(currentItem);
                }
                
                // Add videos after current video (right side of carousel)
                int startAfter = maxBefore;
                int maxAfter = Math.min(streamItems.size(), startAfter + 5);
                for (int i = startAfter; i < maxAfter; i++) {
                    StreamInfoItem streamItem = streamItems.get(i);
                    VideoCarouselItem carouselItem = new VideoCarouselItem(
                        streamItem, // Pass the StreamInfoItem directly
                        false, // Not current video
                        -1 // Related video, not in queue
                    );
                    items.add(carouselItem);
                }
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
        Log.d(TAG, "Carousel item clicked: " + item.getTitle() + ", position: " + item.getPosition());
        
        if (player == null || player.getPlayQueue() == null) {
            Log.w(TAG, "Player or PlayQueue is null, cannot handle click");
            return;
        }
        
        // Maintain UI visibility during interaction
        maintainControlsVisibility();
        
        // If it's a queue item, jump to that position
        if (item.getPosition() >= 0 && item.getPosition() < player.getPlayQueue().size()) {
            Log.d(TAG, "Playing queue item at position: " + item.getPosition());
            player.selectQueueItem(player.getPlayQueue().getItem(item.getPosition()));
        } else if (item.getPosition() == -1) {
            // Handle related video click (position -1 means it's a related video, not from queue)
            Log.d(TAG, "Playing related video: " + item.getTitle());
            
            // Create a new SinglePlayQueue with the clicked video
            StreamInfoItem streamInfoItem = item.getStreamInfoItem();
            if (streamInfoItem != null) {
                try {
                    SinglePlayQueue newQueue = new SinglePlayQueue(streamInfoItem);
                    // Use NavigationHelper to play the video
                    NavigationHelper.playOnMainPlayer(player.getContext(), newQueue, false);
                    Log.d(TAG, "Successfully started playing related video");
                } catch (Exception e) {
                    Log.e(TAG, "Error playing related video", e);
                }
            } else {
                Log.e(TAG, "StreamInfoItem is null for related video");
            }
        }
        // TODO: Handle history items (would need to add them to queue and play)  
    }
    
    /**
     * Maintain UI controls visibility during carousel interaction
     */
    private void maintainControlsVisibility() {
        if (player != null && player.UIs() != null) {
            Log.d(TAG, "Maintaining controls visibility");
            // Call showControls on MainPlayerUi to reset the auto-hide timer
            player.UIs().call(ui -> {
                if (ui instanceof org.schabi.newpipe.player.ui.MainPlayerUi) {
                    // MainPlayerUi hérite de VideoPlayerUi, donc elle a showControls()
                    org.schabi.newpipe.player.ui.MainPlayerUi mainUi = 
                        (org.schabi.newpipe.player.ui.MainPlayerUi) ui;
                    // Utilisation de la constante de durée standard
                    mainUi.showControls(300L); // DEFAULT_CONTROLS_DURATION
                    Log.d(TAG, "Called showControls on MainPlayerUi");
                }
            });
        } else {
            Log.w(TAG, "Cannot maintain controls visibility: player or UIs is null");
        }
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
    public void hideCarousel() {
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