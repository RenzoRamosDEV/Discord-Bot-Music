package com.bot.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PlayerManager {
    private static final Logger logger = LoggerFactory.getLogger(PlayerManager.class);
    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        this.audioPlayerManager.getConfiguration().setOutputFormat(
            StandardAudioDataFormats.DISCORD_PCM_S16_BE
        );
        this.audioPlayerManager.getConfiguration().setResamplingQuality(
            com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality.MEDIUM
        );

        YoutubeAudioSourceManager ytSourceManager = new YoutubeAudioSourceManager();
        this.audioPlayerManager.registerSourceManager(ytSourceManager);

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        logger.info("PlayerManager inicializado: DISCORD_PCM_S16_BE, remuestreo MEDIUM, plugin YouTube v2");
    }

    public static synchronized PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public synchronized GuildMusicManager getMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(), guildId -> {
            GuildMusicManager manager = new GuildMusicManager(audioPlayerManager);
            guild.getAudioManager().setSendingHandler(manager.getSendHandler());
            logger.info("Nuevo GuildMusicManager creado para el servidor: {} ({})", guild.getName(), guildId);
            return manager;
        });
    }

    /**
     * Load and play a track from a URL or search query
     * @param guild The guild where to play
     * @param trackUrl The URL or search query
     * @param channel The voice channel to join
     * @param onSuccess Callback when track loads successfully
     * @param onError Callback when loading fails
     */
    public void loadAndPlay(Guild guild, String trackUrl, AudioChannel channel, 
                           Consumer<AudioTrack> onSuccess, Consumer<String> onError) {
        GuildMusicManager musicManager = getMusicManager(guild);
        
        // Connect to voice channel if not connected
        connectToVoiceChannel(guild.getAudioManager(), channel);

        // If not a URL, treat as search query
        String finalUrl = trackUrl;
        if (!trackUrl.startsWith("http://") && !trackUrl.startsWith("https://")) {
            finalUrl = "ytsearch:" + trackUrl;
        }

        audioPlayerManager.loadItemOrdered(musicManager, finalUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                boolean wasQueued = musicManager.getTrackScheduler().queue(track);
                onSuccess.accept(track);
                
                if (wasQueued) {
                    logger.info("Added to queue: {} in guild {}", track.getInfo().title, guild.getName());
                } else {
                    logger.info("Now playing: {} in guild {}", track.getInfo().title, guild.getName());
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    // For search results, play the first track
                    AudioTrack firstTrack = playlist.getTracks().get(0);
                    musicManager.getTrackScheduler().queue(firstTrack);
                    onSuccess.accept(firstTrack);
                    logger.info("Playing search result: {}", firstTrack.getInfo().title);
                } else {
                    // For actual playlists, add all tracks
                    int addedCount = 0;
                    for (AudioTrack track : playlist.getTracks()) {
                        musicManager.getTrackScheduler().queue(track);
                        addedCount++;
                    }
                    logger.info("Added {} tracks from playlist: {}", addedCount, playlist.getName());
                    
                    if (!playlist.getTracks().isEmpty()) {
                        onSuccess.accept(playlist.getTracks().get(0));
                    }
                }
            }

            @Override
            public void noMatches() {
                onError.accept("No se encontraron resultados para: " + trackUrl);
                logger.warn("No matches found for: {}", trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                onError.accept("Error al cargar: " + exception.getMessage());
                logger.error("Failed to load track: {}", trackUrl, exception);
            }
        });
    }

    /**
     * Connect the bot to a voice channel
     */
    private void connectToVoiceChannel(AudioManager audioManager, AudioChannel channel) {
        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(channel);
            logger.info("Connected to voice channel: {}", channel.getName());
        }
    }

    /**
     * Disconnect from voice channel and cleanup
     */
    public void disconnect(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());
        if (musicManager != null) {
            musicManager.getTrackScheduler().clearQueue();
            musicManager.getAudioPlayer().destroy();
            musicManagers.remove(guild.getIdLong());
        }
        
        guild.getAudioManager().closeAudioConnection();
        logger.info("Disconnected from guild: {}", guild.getName());
    }

    /**
     * Shutdown all players (called on bot shutdown)
     */
    public void shutdown() {
        musicManagers.values().forEach(manager -> {
            manager.getAudioPlayer().destroy();
        });
        musicManagers.clear();
        logger.info("PlayerManager shutdown complete");
    }

    /**
     * Get the AudioPlayerManager (for advanced use)
     */
    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }
}
