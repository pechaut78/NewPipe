package org.schabi.newpipe.player.ui.carousel;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

/**
 * Represents an item in the video carousel
 */
public class VideoCarouselItem {
    private final StreamInfoItem streamInfoItem;
    private final List<Image> thumbnails;
    private final String title;
    private final long duration;
    private final boolean isCurrentVideo;
    private final int position;
    
    public VideoCarouselItem(StreamInfoItem streamInfoItem, boolean isCurrentVideo, int position) {
        this.streamInfoItem = streamInfoItem;
        this.thumbnails = streamInfoItem != null ? streamInfoItem.getThumbnails() : null;
        this.title = streamInfoItem != null ? streamInfoItem.getName() : "";
        this.duration = streamInfoItem != null ? streamInfoItem.getDuration() : 0;
        this.isCurrentVideo = isCurrentVideo;
        this.position = position;
    }
    
    public VideoCarouselItem(List<Image> thumbnails, String title, long duration, boolean isCurrentVideo, int position) {
        this.streamInfoItem = null;
        this.thumbnails = thumbnails;
        this.title = title;
        this.duration = duration;
        this.isCurrentVideo = isCurrentVideo;
        this.position = position;
    }
    
    public StreamInfoItem getStreamInfoItem() {
        return streamInfoItem;
    }
    
    public List<Image> getThumbnails() {
        return thumbnails;
    }
    
    public String getTitle() {
        return title;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public boolean isCurrentVideo() {
        return isCurrentVideo;
    }
    
    public int getPosition() {
        return position;
    }
}