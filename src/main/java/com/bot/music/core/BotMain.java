package com.bot.music.core;

import com.bot.music.audio.PlayerManager;
import com.bot.music.listeners.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class BotMain {
    private static final Logger logger = LoggerFactory.getLogger(BotMain.class);
    private static JDA jda;

    public static void main(String[] args) {
        try {
            logger.info("Iniciando Discord Music Bot...");

            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            String token = dotenv.get("DISCORD_TOKEN");
            if (token == null || token.isEmpty() || token.equals("your_bot_token_here")) {
                logger.error("DISCORD_TOKEN no encontrado en el archivo .env. Por favor crea un archivo .env con tu token.");
                System.exit(1);
            }

            String activityText = dotenv.get("BOT_ACTIVITY", "🎵 Music");
            String statusStr = dotenv.get("BOT_STATUS", "ONLINE");
            
            PlayerManager.getInstance();

            jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .setMemberCachePolicy(MemberCachePolicy.VOICE)
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                    .enableCache(CacheFlag.VOICE_STATE)
                    .setStatus(parseStatus(statusStr))
                    .setActivity(Activity.listening(activityText))
                    .addEventListeners(new EventListener())
                    .build()
                    .awaitReady();

            logger.info("Bot conectado como: {}", jda.getSelfUser().getAsTag());
            logger.info("El bot está en {} servidores", jda.getGuilds().size());

            SlashCommandConfig.registerCommands(jda);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Apagando Discord Music Bot...");
                if (jda != null) {
                    jda.shutdown();
                }
                PlayerManager.getInstance().shutdown();
                logger.info("Bot apagado correctamente.");
            }));
            
        } catch (InterruptedException e) {
            logger.error("Bot initialization was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Failed to start bot", e);
            System.exit(1);
        }
    }

    private static OnlineStatus parseStatus(String status) {
        try {
            return OnlineStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Estado '{}' inválido, usando ONLINE por defecto", status);
            return OnlineStatus.ONLINE;
        }
    }

    public static JDA getJDA() {
        return jda;
    }
}
