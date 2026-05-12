package com.bot.music.commands.info;

import com.bot.music.utils.EmbedUtils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Info commands: /info, /me, /connect
 */
public class InfoCommands {

    /**
     * /info - Display server information
     */
    public static void info(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("Error", "Este comando solo funciona en servidores.")
            ).setEphemeral(true).queue();
            return;
        }

        Member owner = guild.getOwner();
        String ownerTag = owner != null ? owner.getUser().getAsTag() : "Desconocido";
        
        String creationDate = guild.getTimeCreated()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String description = String.format(
            "**📊 Estadísticas del Servidor**\n\n" +
            "👥 **Miembros:** %d\n" +
            "💬 **Canales de texto:** %d\n" +
            "🔊 **Canales de voz:** %d\n" +
            "😀 **Emojis:** %d\n" +
            "🎭 **Roles:** %d\n\n" +
            "📅 **Creado:** %s\n" +
            "👑 **Propietario:** %s\n" +
            "🆔 **ID:** `%s`\n" +
            "🌍 **Región:** %s\n" +
            "🔐 **Nivel de verificación:** %s",
            guild.getMemberCount(),
            guild.getTextChannels().size(),
            guild.getVoiceChannels().size(),
            guild.getEmojis().size(),
            guild.getRoles().size(),
            creationDate,
            ownerTag,
            guild.getId(),
            guild.getLocale().getLanguageName(),
            guild.getVerificationLevel().name()
        );

        event.replyEmbeds(
            EmbedUtils.infoEmbedWithThumbnail(
                "ℹ️ Información de " + guild.getName(),
                description,
                guild.getIconUrl()
            )
        ).queue();
    }

    /**
     * /me - Display user profile
     */
    public static void me(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed("Error", "No se pudo obtener tu información.")
            ).setEphemeral(true).queue();
            return;
        }

        User user = member.getUser();
        String joinDate = member.getTimeJoined()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String creationDate = user.getTimeCreated()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Get roles (excluding @everyone)
        List<Role> roles = member.getRoles();
        String rolesStr = roles.isEmpty() ? "Sin roles" : 
            roles.stream()
                 .map(Role::getAsMention)
                 .collect(Collectors.joining(", "));

        String description = String.format(
            "**👤 Perfil de Usuario**\n\n" +
            "🏷️ **Tag:** %s\n" +
            "🆔 **ID:** `%s`\n" +
            "🎭 **Apodo:** %s\n" +
            "📅 **Cuenta creada:** %s\n" +
            "📥 **Unido al servidor:** %s\n" +
            "🎨 **Roles:** %s\n" +
            "🤖 **Bot:** %s",
            user.getAsTag(),
            user.getId(),
            member.getNickname() != null ? member.getNickname() : "Sin apodo",
            creationDate,
            joinDate,
            rolesStr,
            user.isBot() ? "Sí" : "No"
        );

        event.replyEmbeds(
            EmbedUtils.infoEmbedWithThumbnail(
                "👤 Tu Perfil",
                description,
                user.getEffectiveAvatarUrl()
            )
        ).queue();
    }

    /**
     * /connect - Display users in voice channel
     */
    public static void connect(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || 
            !member.getVoiceState().inAudioChannel()) {
            event.replyEmbeds(
                EmbedUtils.errorEmbed(
                    "❌ No estás en un canal de voz",
                    "Debes estar en un canal de voz para usar este comando."
                )
            ).setEphemeral(true).queue();
            return;
        }

        AudioChannel channel = member.getVoiceState().getChannel();
        List<Member> members = channel.getMembers();

        if (members.isEmpty()) {
            event.replyEmbeds(
                EmbedUtils.infoEmbed("🔊 Canal vacío", "No hay nadie en el canal de voz.")
            ).setEphemeral(true).queue();
            return;
        }

        StringBuilder description = new StringBuilder();
        description.append(String.format("**🔊 Canal:** %s\n", channel.getName()));
        description.append(String.format("**👥 Usuarios conectados:** %d\n\n", members.size()));

        for (Member m : members) {
            GuildVoiceState voiceState = m.getVoiceState();
            String status = "";
            
            if (voiceState != null) {
                if (voiceState.isMuted() || voiceState.isSelfMuted()) {
                    status += "🔇 ";
                }
                if (voiceState.isDeafened() || voiceState.isSelfDeafened()) {
                    status += "🔈 ";
                }
                if (voiceState.isStream()) {
                    status += "📹 ";
                }
            }

            description.append(String.format("%s %s%s\n", 
                m.getUser().isBot() ? "🤖" : "👤",
                status,
                m.getEffectiveName()
            ));
        }

        event.replyEmbeds(
            EmbedUtils.infoEmbed("🔊 Usuarios Conectados", description.toString())
        ).queue();
    }

    /**
     * /ping - Display bot latency
     */
    public static void ping(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        
        event.deferReply().queue(hook -> {
            long responseTime = System.currentTimeMillis() - event.getTimeCreated().toInstant().toEpochMilli();
            
            String description = String.format(
                "🏓 **Pong!**\n\n" +
                "⏱️ **Latencia de respuesta:** `%dms`\n" +
                "🌐 **Latencia de Gateway:** `%dms`",
                responseTime,
                gatewayPing
            );

            hook.sendMessageEmbeds(
                EmbedUtils.successEmbed("🏓 Latencia", description)
            ).queue();
        });
    }

    /**
     * /help - Display all commands
     */
    public static void help(SlashCommandInteractionEvent event) {
        String description = 
            "**🎵 Comandos de Música**\n" +
            "`/play [url/búsqueda]` - Reproduce música\n" +
            "`/skip` - Salta a la siguiente canción\n" +
            "`/stop` - Detiene la reproducción y limpia la cola\n" +
            "`/queue` - Muestra la cola de reproducción\n" +
            "`/nowplaying` - Muestra la canción actual\n" +
            "`/pause` - Pausa la reproducción\n" +
            "`/resume` - Reanuda la reproducción\n" +
            "`/shuffle` - Mezcla la cola\n" +
            "`/clear` - Limpia la cola\n" +
            "`/loop [mode]` - Activa/desactiva repetición\n" +
            "`/volume [0-100]` - Ajusta el volumen\n" +
            "`/seek [tiempo]` - Salta a un momento específico\n" +
            "`/remove [posición]` - Elimina una canción de la cola\n\n" +
            "**ℹ️ Comandos de Información**\n" +
            "`/info` - Información del servidor\n" +
            "`/me` - Tu perfil de usuario\n" +
            "`/connect` - Usuarios en el canal de voz\n" +
            "`/ping` - Latencia del bot\n" +
            "`/help` - Muestra esta ayuda";

        event.replyEmbeds(
            EmbedUtils.infoEmbed("📖 Comandos Disponibles", description)
        ).setEphemeral(true).queue();
    }
}
