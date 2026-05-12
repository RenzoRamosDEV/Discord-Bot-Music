package com.bot.music.commands.music;

import com.bot.music.audio.GuildMusicManager;
import com.bot.music.audio.PlayerManager;
import com.bot.music.utils.EmbedUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class MusicCommands {

    public static void skip(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        AudioTrack currentTrack = musicManager.getAudioPlayer().getPlayingTrack();

        if (currentTrack == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ No hay música reproduciéndose", 
                    "No hay ninguna canción para saltar.")
            ).setEphemeral(true).queue();
            return;
        }

        String skippedTitle = currentTrack.getInfo().title;
        musicManager.getTrackScheduler().nextTrack();

        AudioTrack nextTrack = musicManager.getAudioPlayer().getPlayingTrack();
        if (nextTrack != null) {
            event.replyEmbeds(
                EmbedUtils.successEmbed("⏭️ Canción saltada", 
                    String.format("**Saltada:** %s\n**Reproduciendo:** %s", 
                                  skippedTitle, nextTrack.getInfo().title))
            ).queue();
        } else {
            event.replyEmbeds(
                EmbedUtils.successEmbed("⏭️ Canción saltada", 
                    "No hay más canciones en la cola.")
            ).queue();
        }
    }

    public static void stop(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        PlayerManager playerManager = PlayerManager.getInstance();
        playerManager.disconnect(event.getGuild());

        event.replyEmbeds(
            EmbedUtils.successEmbed("⏹️ Reproducción detenida", 
                "La cola ha sido limpiada y el bot se ha desconectado.")
        ).queue();
    }

    /**
     * /queue - Display current queue
     */
    public static void queue(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        AudioTrack currentTrack = musicManager.getAudioPlayer().getPlayingTrack();
        List<AudioTrack> queue = musicManager.getTrackScheduler().getQueue();

        if (currentTrack == null && queue.isEmpty()) {
            event.replyEmbeds(
                EmbedUtils.infoEmbed("📜 Cola vacía", "No hay canciones en la cola.")
            ).setEphemeral(true).queue();
            return;
        }

        StringBuilder description = new StringBuilder();

        if (currentTrack != null) {
            description.append("**▶️ Reproduciendo:**\n")
                      .append(String.format("[%s](%s) - `%s`\n\n",
                                          currentTrack.getInfo().title,
                                          currentTrack.getInfo().uri,
                                          formatDuration(currentTrack.getDuration())));
        }

        if (!queue.isEmpty()) {
            description.append("**📋 Próximas canciones:**\n");
            int maxDisplay = Math.min(queue.size(), 10);

            for (int i = 0; i < maxDisplay; i++) {
                AudioTrack track = queue.get(i);
                description.append(String.format("`%d.` [%s](%s) - `%s`\n",
                                                i + 1,
                                                track.getInfo().title,
                                                track.getInfo().uri,
                                                formatDuration(track.getDuration())));
            }

            if (queue.size() > 10) {
                description.append(String.format("\n*...y %d canciones más*", queue.size() - 10));
            }

            long totalDuration = queue.stream().mapToLong(AudioTrack::getDuration).sum();
            description.append(String.format("\n\n**Total:** %d canciones | **Duración:** %s",
                                           queue.size(), formatDuration(totalDuration)));
        }

        event.replyEmbeds(
            EmbedUtils.musicEmbed("🎵 Cola de reproducción", description.toString(), null)
        ).queue();
    }

    public static void nowPlaying(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        AudioTrack track = musicManager.getAudioPlayer().getPlayingTrack();

        if (track == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ No hay música", "No hay ninguna canción reproduciéndose.")
            ).setEphemeral(true).queue();
            return;
        }

        long position = track.getPosition();
        long duration = track.getDuration();
        int progress = (int) ((double) position / duration * 20);

        String progressBar = "▬".repeat(progress) + "🔘" + "▬".repeat(20 - progress);
        String timeInfo = String.format("`%s` %s `%s`",
                                       formatDuration(position),
                                       progressBar,
                                       formatDuration(duration));

        String description = String.format(
            "**[%s](%s)**\n\n%s\n\n" +
            "👤 Autor: `%s`\n" +
            "🔊 Volumen: `%d%%`\n" +
            "🔁 Repetición: `%s`",
            track.getInfo().title,
            track.getInfo().uri,
            timeInfo,
            track.getInfo().author,
            musicManager.getAudioPlayer().getVolume(),
            musicManager.getTrackScheduler().getLoopMode().toString().toLowerCase()
        );

        event.replyEmbeds(
            EmbedUtils.musicEmbed("🎵 Reproduciendo ahora", description, track.getInfo().uri)
        ).queue();
    }

    /**
     * /pause - Pause playback
     */
    public static void pause(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        
        if (musicManager.getAudioPlayer().getPlayingTrack() == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ No hay música", "No hay nada que pausar.")
            ).setEphemeral(true).queue();
            return;
        }

        if (musicManager.getAudioPlayer().isPaused()) {
            event.replyEmbeds(
                EmbedUtils.infoEmbed("ℹ️ Ya está pausado", "La reproducción ya está pausada.")
            ).setEphemeral(true).queue();
            return;
        }

        musicManager.getAudioPlayer().setPaused(true);
        event.replyEmbeds(
            EmbedUtils.successEmbed("⏸️ Pausado", "La reproducción ha sido pausada.")
        ).queue();
    }

    /**
     * /resume - Resume playback
     */
    public static void resume(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        
        if (musicManager.getAudioPlayer().getPlayingTrack() == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ No hay música", "No hay nada que reanudar.")
            ).setEphemeral(true).queue();
            return;
        }

        if (!musicManager.getAudioPlayer().isPaused()) {
            event.replyEmbeds(
                EmbedUtils.infoEmbed("ℹ️ Ya está reproduciendo", "La música no está pausada.")
            ).setEphemeral(true).queue();
            return;
        }

        musicManager.getAudioPlayer().setPaused(false);
        event.replyEmbeds(
            EmbedUtils.successEmbed("▶️ Reanudado", "La reproducción ha sido reanudada.")
        ).queue();
    }

    /**
     * /shuffle - Shuffle the queue
     */
    public static void shuffle(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        
        if (musicManager.getTrackScheduler().getQueueSize() < 2) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ Cola muy pequeña", 
                    "Necesitas al menos 2 canciones en la cola para mezclar.")
            ).setEphemeral(true).queue();
            return;
        }

        musicManager.getTrackScheduler().shuffle();
        event.replyEmbeds(
            EmbedUtils.successEmbed("🔀 Cola mezclada", 
                String.format("Se han mezclado %d canciones.", 
                             musicManager.getTrackScheduler().getQueueSize()))
        ).queue();
    }

    /**
     * /clear - Clear the queue
     */
    public static void clear(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        int queueSize = musicManager.getTrackScheduler().getQueueSize();
        
        if (queueSize == 0) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ Cola vacía", "No hay canciones en la cola para limpiar.")
            ).setEphemeral(true).queue();
            return;
        }

        musicManager.getTrackScheduler().clearQueue();
        event.replyEmbeds(
            EmbedUtils.successEmbed("🗑️ Cola limpiada", 
                String.format("Se han eliminado %d canciones de la cola.", queueSize))
        ).queue();
    }

    /**
     * Helper: Check if user and bot are in voice channel
     */
    private static boolean checkVoiceState(SlashCommandInteractionEvent event) {
        if (event.getMember() == null || event.getMember().getVoiceState() == null || 
            !event.getMember().getVoiceState().inAudioChannel()) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ No estás en un canal de voz", 
                    "Debes estar en un canal de voz para usar este comando.")
            ).setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    /**
     * Format duration helper
     */
    private static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
}
