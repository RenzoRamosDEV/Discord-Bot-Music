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

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public boolean queue(AudioTrack track) {
        logger.debug("Intentando encolar pista: {}", track.getInfo().title);
        boolean started = player.startTrack(track, true);
        if (!started) {
            queue.offer(track);
            logger.debug("Pista agregada a la cola (posición: {})", queue.size());
            return true;
        }
        logger.debug("Pista iniciada inmediatamente");
        return false;
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

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;

        if (endReason.mayStartNext) {
            if (loopMode == LoopMode.TRACK) {
                player.startTrack(track.makeClone(), false);
            } else {
                nextTrack();
            }
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        logger.info("Pista iniciada: {} | Duración: {}ms | Volumen: {}",
                   track.getInfo().title, track.getDuration(), player.getVolume());
    }
    
    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        logger.error("Error en la pista '{}': {}", track.getInfo().title, exception.getMessage(), exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        logger.warn("Pista atascada: {} (umbral: {}ms)", track.getInfo().title, thresholdMs);
    }

    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queue);
    }

    public int getQueueSize() {
        return queue.size();
    }

    public void clearQueue() {
        queue.clear();
    }

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

    public void shuffle() {
        List<AudioTrack> tempList = new ArrayList<>(queue);
        Collections.shuffle(tempList);
        queue.clear();
        queue.addAll(tempList);
    }

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(LoopMode mode) {
        this.loopMode = mode;
    }

    public AudioPlayer getPlayer() {
        return player;
    }
}
