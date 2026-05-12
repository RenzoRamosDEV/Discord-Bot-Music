package com.bot.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages the queue of audio tracks and handles track lifecycle events.
 * This is the "playlist manager" that automatically plays the next song.
 */
public class TrackScheduler extends AudioEventAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private LoopMode loopMode = LoopMode.OFF;
    private AudioTrack lastTrack = null;

    public enum LoopMode {
        OFF,
        TRACK,
        QUEUE
    }

    /**
     * Constructor
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add a track to the queue. If nothing is playing, start immediately.
     * @param track The track to add
     * @return true if added to queue, false if started immediately
     */
    public boolean queue(AudioTrack track) {
        logger.debug("Attempting to queue track: {}", track.getInfo().title);
        boolean started = player.startTrack(track, true);
        
        if (!started) {
            // Track didn't start immediately, add to queue
            queue.offer(track);
            logger.debug("Track added to queue (position: {})", queue.size());
            return true;
        } else {
            // Track started playing immediately
            logger.debug("Track started playing immediately");
            return false;
        }
    }

    /**
     * Skip the current track and start the next one
     */
    public void nextTrack() {
        AudioTrack nextTrack = queue.poll();
        
        if (nextTrack == null && loopMode == LoopMode.QUEUE && lastTrack != null) {
            // If queue is empty and loop mode is QUEUE, replay the last track
            player.startTrack(lastTrack.makeClone(), false);
        } else {
            player.startTrack(nextTrack, false);
        }
    }

    /**
     * Called when a track ends
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;

        // Only start next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (loopMode == LoopMode.TRACK) {
                // Repeat the same track
                player.startTrack(track.makeClone(), false);
            } else {
                // Play next track in queue
                nextTrack();
            }
        }
    }

    /**
     * Called when a track starts
     */
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        logger.info("🎵 Track started: {} | Duration: {}ms | Volume: {}", 
                   track.getInfo().title, track.getDuration(), player.getVolume());
    }
    
    /**
     * Called when a track throws an exception
     */
    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        logger.error("❌ Track exception for '{}': {}", track.getInfo().title, exception.getMessage(), exception);
    }
    
    /**
     * Called when a track gets stuck
     */
    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        logger.warn("⚠️ Track stuck: {} (threshold: {}ms)", track.getInfo().title, thresholdMs);
    }

    /**
     * Get the current queue as a list (for display)
     * @return List of tracks in queue
     */
    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queue);
    }

    /**
     * Get the size of the queue
     * @return Number of tracks in queue
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Clear the entire queue
     */
    public void clearQueue() {
        queue.clear();
    }

    /**
     * Remove a specific track from the queue by position
     * @param position Position in queue (0-indexed)
     * @return true if removed successfully
     */
    public boolean removeTrack(int position) {
        if (position < 0 || position >= queue.size()) {
            return false;
        }
        
        List<AudioTrack> tempList = new ArrayList<>(queue);
        AudioTrack removed = tempList.remove(position);
        
        if (removed != null) {
            queue.clear();
            queue.addAll(tempList);
            return true;
        }
        return false;
    }

    /**
     * Shuffle the queue randomly
     */
    public void shuffle() {
        List<AudioTrack> tempList = new ArrayList<>(queue);
        Collections.shuffle(tempList);
        queue.clear();
        queue.addAll(tempList);
    }

    /**
     * Get the current loop mode
     */
    public LoopMode getLoopMode() {
        return loopMode;
    }

    /**
     * Set the loop mode
     */
    public void setLoopMode(LoopMode mode) {
        this.loopMode = mode;
    }

    /**
     * Get the audio player
     */
    public AudioPlayer getPlayer() {
        return player;
    }
}
