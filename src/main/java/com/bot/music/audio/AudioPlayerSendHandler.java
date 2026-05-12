package com.bot.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * Bridge between LavaPlayer and JDA.
 * Converts audio frames from LavaPlayer to Discord-compatible format.
 * 
 * This is the critical component that makes audio actually work.
 * LavaPlayer produces audio → This handler converts it → JDA sends to Discord.
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    /**
     * Constructor
     * @param audioPlayer The audio player this handler will send from
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        // Standard Discord frame size: 960 samples * 2 channels * 2 bytes (16-bit) = 3840 bytes
        this.buffer = ByteBuffer.allocate(3840);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    /**
     * Check if we can provide audio data
     * @return true if we have audio to send
     */
    @Override
    public boolean canProvide() {
        // Clear the buffer before providing new frame
        buffer.clear();
        // Request next frame from LavaPlayer
        return audioPlayer.provide(frame);
    }

    /**
     * Provide 20ms of audio data to Discord
     * @return ByteBuffer containing audio data
     */
    @Override
    public ByteBuffer provide20MsAudio() {
        // Flip the buffer from write mode to read mode
        // This sets limit to current position and position to 0
        ByteBuffer copy = buffer.flip();
        return copy;
    }

    /**
     * Check if the audio is Opus encoded
     * LavaPlayer provides PCM, not Opus, so return false
     * @return false (we're sending PCM)
     */
    @Override
    public boolean isOpus() {
        return false;
    }
}
