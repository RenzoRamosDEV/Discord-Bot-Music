package com.bot.music.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized configuration for all Slash Commands.
 * Registers commands globally with Discord API on bot startup.
 */
public class SlashCommandConfig {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandConfig.class);

    /**
     * Register all slash commands with Discord
     */
    public static void registerCommands(JDA jda) {
        logger.info("Registering slash commands...");
        
        try {
            jda.updateCommands()
                    // ========== MUSIC COMMANDS ==========
                    .addCommands(
                            Commands.slash("play", "Reproduce una canción desde YouTube, Spotify o búsqueda")
                                    .addOptions(new OptionData(OptionType.STRING, "query", "URL o término de búsqueda", true)),
                            
                            Commands.slash("skip", "Salta a la siguiente canción en la cola"),
                            
                            Commands.slash("stop", "Detiene la reproducción y limpia la cola"),
                            
                            Commands.slash("queue", "Muestra las canciones en la cola"),
                            
                            Commands.slash("nowplaying", "Muestra la canción que se está reproduciendo actualmente")
                                    .addOption(OptionType.STRING, "np", "Alias corto"),
                            
                            Commands.slash("pause", "Pausa la reproducción actual"),
                            
                            Commands.slash("resume", "Reanuda la reproducción pausada"),
                            
                            Commands.slash("shuffle", "Mezcla aleatoriamente la cola de reproducción"),
                            
                            Commands.slash("loop", "Activa/desactiva el modo de repetición")
                                    .addOptions(new OptionData(OptionType.STRING, "mode", "Modo de repetición", false)
                                            .addChoice("track", "track")
                                            .addChoice("queue", "queue")
                                            .addChoice("off", "off")),
                            
                            Commands.slash("volume", "Ajusta el volumen de reproducción (0-100)")
                                    .addOptions(new OptionData(OptionType.INTEGER, "level", "Nivel de volumen", true)
                                            .setMinValue(0)
                                            .setMaxValue(100)),
                            
                            Commands.slash("seek", "Salta a un momento específico de la canción")
                                    .addOptions(new OptionData(OptionType.STRING, "time", "Tiempo (mm:ss o segundos)", true)),
                            
                            Commands.slash("remove", "Elimina una canción específica de la cola")
                                    .addOptions(new OptionData(OptionType.INTEGER, "position", "Posición en la cola", true)
                                            .setMinValue(1)),
                            
                            Commands.slash("clear", "Limpia toda la cola sin detener la canción actual")
                    )
                    // ========== INFO COMMANDS ==========
                    .addCommands(
                            Commands.slash("info", "Muestra información del servidor"),
                            
                            Commands.slash("me", "Muestra tu perfil de usuario"),
                            
                            Commands.slash("connect", "Muestra usuarios conectados en el canal de voz"),
                            
                            Commands.slash("ping", "Muestra la latencia del bot"),
                            
                            Commands.slash("help", "Muestra todos los comandos disponibles")
                    )
                    .queue(
                            success -> logger.info("Successfully registered {} slash commands", success.size()),
                            error -> logger.error("Failed to register slash commands", error)
                    );
            
            logger.info("Slash command registration initiated (may take a few minutes to propagate globally)");
            
        } catch (Exception e) {
            logger.error("Error during slash command registration", e);
        }
    }
}
