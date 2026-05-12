package com.bot.music.commands.music;

import com.bot.music.audio.GuildMusicManager;
import com.bot.music.audio.PlayerManager;
import com.bot.music.utils.EmbedUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PlayCommand {

    public static void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Member member = event.getMember();
        if (member == null) {
            event.getHook().sendMessageEmbeds(
                EmbedUtils.errorEmbed("Error", "No se pudo obtener tu información de usuario.")
            ).queue();
            return;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            event.getHook().sendMessageEmbeds(
                EmbedUtils.errorEmbed("❌ No estás en un canal de voz",
                    "Debes estar en un canal de voz para usar este comando.")
            ).queue();
            return;
        }

        GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (botVoiceState != null && botVoiceState.inAudioChannel() &&
            !botVoiceState.getChannel().equals(voiceState.getChannel())) {
            event.getHook().sendMessageEmbeds(
                EmbedUtils.errorEmbed("❌ Bot en otro canal",
                    "El bot ya está siendo usado en otro canal de voz.")
            ).queue();
            return;
        }

        OptionMapping queryOption = event.getOption("query");
        if (queryOption == null) {
            event.getHook().sendMessageEmbeds(
                EmbedUtils.errorEmbed("❌ Parámetro faltante", 
                    "Debes proporcionar una URL o término de búsqueda.")
            ).queue();
            return;
        }

        String query = queryOption.getAsString();
        AudioChannel audioChannel = voiceState.getChannel();

        PlayerManager.getInstance().loadAndPlay(
            event.getGuild(),
            query,
            audioChannel,
            track -> {
                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
                boolean isPlaying = musicManager.getAudioPlayer().getPlayingTrack() != null;

                if (isPlaying && musicManager.getTrackScheduler().getQueueSize() > 0) {
                    event.getHook().sendMessageEmbeds(
                        EmbedUtils.musicEmbed(
                            "✅ Añadido a la cola",
                            String.format("**[%s](%s)**\n\n" +
                                         "⏱️ Duración: `%s`\n" +
                                         "📊 Posición en cola: `#%d`",
                                         track.getInfo().title,
                                         track.getInfo().uri,
                                         formatDuration(track.getDuration()),
                                         musicManager.getTrackScheduler().getQueueSize()),
                            track.getInfo().uri
                        )
                    ).queue();
                } else {
                    event.getHook().sendMessageEmbeds(
                        EmbedUtils.musicEmbed(
                            "▶️ Reproduciendo ahora",
                            String.format("**[%s](%s)**\n\n" +
                                         "⏱️ Duración: `%s`\n" +
                                         "👤 Solicitado por: %s",
                                         track.getInfo().title,
                                         track.getInfo().uri,
                                         formatDuration(track.getDuration()),
                                         member.getAsMention()),
                            track.getInfo().uri
                        )
                    ).queue();
                }
            },
            errorMessage -> {
                event.getHook().sendMessageEmbeds(
                    EmbedUtils.errorEmbed("❌ Error al cargar", errorMessage)
                ).queue();
            }
        );
    }

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
