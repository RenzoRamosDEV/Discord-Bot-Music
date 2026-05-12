package com.bot.music.listeners;

import com.bot.music.audio.GuildMusicManager;
import com.bot.music.audio.PlayerManager;
import com.bot.music.audio.TrackScheduler;
import com.bot.music.commands.info.InfoCommands;
import com.bot.music.commands.music.MusicCommands;
import com.bot.music.commands.music.PlayCommand;
import com.bot.music.utils.EmbedUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Bot listo! Conectado como: {}", event.getJDA().getSelfUser().getAsTag());
        logger.info("El bot está en {} servidores", event.getJDA().getGuilds().size());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        logger.info("Comando '{}' ejecutado por {} en el servidor {}",
                   commandName,
                   event.getUser().getAsTag(),
                   event.getGuild() != null ? event.getGuild().getName() : "DM");

        try {
            switch (commandName.toLowerCase()) {
                case "play" -> PlayCommand.execute(event);
                case "skip" -> MusicCommands.skip(event);
                case "stop" -> MusicCommands.stop(event);
                case "queue" -> MusicCommands.queue(event);
                case "nowplaying", "np" -> MusicCommands.nowPlaying(event);
                case "pause" -> MusicCommands.pause(event);
                case "resume" -> MusicCommands.resume(event);
                case "shuffle" -> MusicCommands.shuffle(event);
                case "clear" -> MusicCommands.clear(event);
                case "loop" -> handleLoop(event);
                case "volume" -> handleVolume(event);
                case "seek" -> handleSeek(event);
                case "remove" -> handleRemove(event);

                // ========== INFO COMMANDS ==========
                case "info" -> InfoCommands.info(event);
                case "me" -> InfoCommands.me(event);
                case "connect" -> InfoCommands.connect(event);
                case "ping" -> InfoCommands.ping(event);
                case "help" -> InfoCommands.help(event);

                default -> event.reply("Comando no reconocido.").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            logger.error("Error ejecutando el comando: {}", commandName, e);
            try {
                if (event.isAcknowledged()) {
                    event.getHook().sendMessageEmbeds(
                        EmbedUtils.errorEmbed("❌ Error",
                            "Ocurrió un error al ejecutar el comando. Por favor, inténtalo de nuevo.")
                    ).queue();
                } else {
                    event.replyEmbeds(
                        EmbedUtils.errorEmbed("❌ Error",
                            "Ocurrió un error al ejecutar el comando. Por favor, inténtalo de nuevo.")
                    ).setEphemeral(true).queue();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void handleLoop(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        OptionMapping modeOption = event.getOption("mode");

        TrackScheduler.LoopMode newMode;
        if (modeOption == null) {
            TrackScheduler.LoopMode currentMode = musicManager.getTrackScheduler().getLoopMode();
            newMode = switch (currentMode) {
                case OFF -> TrackScheduler.LoopMode.TRACK;
                case TRACK -> TrackScheduler.LoopMode.QUEUE;
                case QUEUE -> TrackScheduler.LoopMode.OFF;
            };
        } else {
            String modeStr = modeOption.getAsString().toUpperCase();
            newMode = TrackScheduler.LoopMode.valueOf(modeStr);
        }

        musicManager.getTrackScheduler().setLoopMode(newMode);

        String emoji = switch (newMode) {
            case TRACK -> "🔂";
            case QUEUE -> "🔁";
            case OFF -> "▶️";
        };

        String modeText = switch (newMode) {
            case TRACK -> "Repetir canción actual";
            case QUEUE -> "Repetir cola completa";
            case OFF -> "Sin repetición";
        };

        event.replyEmbeds(
            EmbedUtils.successEmbed(
                emoji + " Modo de repetición",
                "**Modo:** " + modeText
            )
        ).queue();
    }

    /**
     * Handle /volume command
     */
    private void handleVolume(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        OptionMapping volumeOption = event.getOption("level");
        if (volumeOption == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ Error", "Debes especificar un nivel de volumen (0-100).")
            ).setEphemeral(true).queue();
            return;
        }

        int volume = (int) volumeOption.getAsLong();
        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        musicManager.getAudioPlayer().setVolume(volume);

        String volumeEmoji = volume == 0 ? "🔇" : volume < 30 ? "🔈" : volume < 70 ? "🔉" : "🔊";

        event.replyEmbeds(
            EmbedUtils.successEmbed(
                volumeEmoji + " Volumen ajustado",
                String.format("Volumen establecido a **%d%%**", volume)
            )
        ).queue();
    }

    /**
     * Handle /seek command
     */
    private void handleSeek(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        
        if (musicManager.getAudioPlayer().getPlayingTrack() == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ No hay música", "No hay ninguna canción reproduciéndose.")
            ).setEphemeral(true).queue();
            return;
        }

        OptionMapping timeOption = event.getOption("time");
        if (timeOption == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ Error", "Debes especificar un tiempo (mm:ss o segundos).")
            ).setEphemeral(true).queue();
            return;
        }

        String timeStr = timeOption.getAsString();
        long milliseconds;

        try {
            // Try parsing as mm:ss
            if (timeStr.contains(":")) {
                String[] parts = timeStr.split(":");
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                milliseconds = (minutes * 60L + seconds) * 1000L;
            } else {
                // Parse as seconds
                milliseconds = Long.parseLong(timeStr) * 1000L;
            }

            long trackDuration = musicManager.getAudioPlayer().getPlayingTrack().getDuration();
            if (milliseconds > trackDuration) {
                event.replyEmbeds(
                    EmbedUtils.errorEmbed("❌ Error", "El tiempo especificado supera la duración de la canción.")
                ).setEphemeral(true).queue();
                return;
            }

            musicManager.getAudioPlayer().getPlayingTrack().setPosition(milliseconds);

            event.replyEmbeds(
                EmbedUtils.successEmbed(
                    "⏩ Posición actualizada",
                    String.format("Reproduciendo desde: `%s`", formatDuration(milliseconds))
                )
            ).queue();

        } catch (NumberFormatException e) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ Formato inválido", 
                    "Usa el formato `mm:ss` o especifica los segundos.")
            ).setEphemeral(true).queue();
        }
    }

    /**
     * Handle /remove command
     */
    private void handleRemove(SlashCommandInteractionEvent event) {
        if (!checkVoiceState(event)) return;

        OptionMapping positionOption = event.getOption("position");
        if (positionOption == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("❌ Error", "Debes especificar la posición de la canción.")
            ).setEphemeral(true).queue();
            return;
        }

        int position = (int) positionOption.getAsLong() - 1; // Convert to 0-indexed

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        
        if (musicManager.getTrackScheduler().removeTrack(position)) {
            event.replyEmbeds(
                EmbedUtils.successEmbed(
                    "✅ Canción eliminada",
                    String.format("Se eliminó la canción en la posición #%d", position + 1)
                )
            ).queue();
        } else {
            event.replyEmbeds(
                EmbedUtils.errorEmbed(
                    "❌ Posición inválida",
                    String.format("No hay ninguna canción en la posición #%d", position + 1)
                )
            ).setEphemeral(true).queue();
        }
    }

    /**
     * Helper: Check if user is in voice channel
     */
    private boolean checkVoiceState(SlashCommandInteractionEvent event) {
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
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
