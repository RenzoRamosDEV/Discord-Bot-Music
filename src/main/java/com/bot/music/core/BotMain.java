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

/**
 * Main entry point for the Discord Music Bot.
 * Initializes JDA, registers listeners, and connects to Discord Gateway.
 */
public class BotMain {
    private static final Logger logger = LoggerFactory.getLogger(BotMain.class);
    private static JDA jda;

    public static void main(String[] args) {
        try {
            logger.info("Starting Discord Music Bot...");
            
            // Load environment variables
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            String token = dotenv.get("DISCORD_TOKEN");
            if (token == null || token.isEmpty() || token.equals("your_bot_token_here")) {
                logger.error("DISCORD_TOKEN not found in .env file. Please create a .env file with your bot token.");
                System.exit(1);
            }

            String activityText = dotenv.get("BOT_ACTIVITY", "🎵 Music");
            String statusStr = dotenv.get("BOT_STATUS", "ONLINE");
            
            // Initialize PlayerManager (singleton)
            PlayerManager.getInstance();
            
            // Build JDA instance
            jda = JDABuilder.createDefault(token)
                    // Required Intents for Music Bot
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT // For prefix commands if needed
                    )
                    // Cache Configuration (Optimize memory usage)
                    .setMemberCachePolicy(MemberCachePolicy.VOICE)
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                    .enableCache(CacheFlag.VOICE_STATE)
                    // Status and Activity
                    .setStatus(parseStatus(statusStr))
                    .setActivity(Activity.listening(activityText))
                    // Register Event Listeners
                    .addEventListeners(new EventListener())
                    // Build and wait for ready
                    .build()
                    .awaitReady();

            logger.info("Bot successfully connected as: {}", jda.getSelfUser().getAsTag());
            logger.info("Bot is in {} guilds", jda.getGuilds().size());
            
            // Register Slash Commands after bot is ready
            SlashCommandConfig.registerCommands(jda);
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down Discord Music Bot...");
                if (jda != null) {
                    jda.shutdown();
                }
                PlayerManager.getInstance().shutdown();
                logger.info("Bot shutdown complete.");
            }));
            
        } catch (InterruptedException e) {
            logger.error("Bot initialization was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Failed to start bot", e);
            System.exit(1);
        }
    }

    /**
     * Parse status string to OnlineStatus enum
     */
    private static OnlineStatus parseStatus(String status) {
        try {
            return OnlineStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status '{}', defaulting to ONLINE", status);
            return OnlineStatus.ONLINE;
        }
    }

    /**
     * Get the JDA instance
     */
    public static JDA getJDA() {
        return jda;
    }
}
