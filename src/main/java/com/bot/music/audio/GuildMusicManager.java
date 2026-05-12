package com.bot.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 * Holder for player and track scheduler per Guild (Server).
 * Each guild gets its own isolated music player instance.
 */
public class GuildMusicManager {
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final AudioPlayerSendHandler sendHandler;

    /**
     * Creates a player and track scheduler for a guild
     * @param manager Audio player manager from LavaPlayer
     */
    public GuildMusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.trackScheduler = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.trackScheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
        
        // Set initial volume to 50% (default is 100, range is 0-150)
        this.audioPlayer.setVolume(50);
    }

    /**
     * Get the audio player for this guild
     * @return AudioPlayer instance
     */
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    /**
     * Get the track scheduler for managing the queue
     * @return TrackScheduler instance
     */
    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    /**
     * Get the send handler for JDA
     * @return AudioPlayerSendHandler instance
     */
    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}
