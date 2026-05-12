package com.bot.music.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;

/**
 * Utility class for creating consistent, beautiful embeds.
 * Centralizes embed styling for the entire bot.
 */
public class EmbedUtils {

    // Color palette
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);  // Green
    private static final Color ERROR_COLOR = new Color(231, 76, 60);     // Red
    private static final Color INFO_COLOR = new Color(52, 152, 219);     // Blue
    private static final Color WARNING_COLOR = new Color(243, 156, 18);  // Orange
    private static final Color MUSIC_COLOR = new Color(155, 89, 182);    // Purple

    /**
     * Create a success embed (green)
     */
    public static MessageEmbed successEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }

    /**
     * Create an error embed (red)
     */
    public static MessageEmbed errorEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(ERROR_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }

    /**
     * Create an info embed (blue)
     */
    public static MessageEmbed infoEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(INFO_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }

    /**
     * Create an info embed with thumbnail
     */
    public static MessageEmbed infoEmbedWithThumbnail(String title, String description, String thumbnailUrl) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(INFO_COLOR)
                .setTimestamp(Instant.now());
        
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            builder.setThumbnail(thumbnailUrl);
        }
        
        return builder.build();
    }

    /**
     * Create a warning embed (orange)
     */
    public static MessageEmbed warningEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(WARNING_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }

    /**
     * Create a music embed (purple) with optional thumbnail
     */
    public static MessageEmbed musicEmbed(String title, String description, String thumbnailUrl) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(MUSIC_COLOR)
                .setTimestamp(Instant.now());
        
        // Try to extract YouTube thumbnail from URL
        if (thumbnailUrl != null && thumbnailUrl.contains("youtube.com")) {
            String videoId = extractYouTubeVideoId(thumbnailUrl);
            if (videoId != null) {
                builder.setThumbnail("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg");
            }
        } else if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            builder.setThumbnail(thumbnailUrl);
        }
        
        return builder.build();
    }

    /**
     * Create a music embed with footer
     */
    public static MessageEmbed musicEmbedWithFooter(String title, String description, 
                                                    String thumbnailUrl, String footer) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(MUSIC_COLOR)
                .setTimestamp(Instant.now())
                .setFooter(footer);
        
        if (thumbnailUrl != null && thumbnailUrl.contains("youtube.com")) {
            String videoId = extractYouTubeVideoId(thumbnailUrl);
            if (videoId != null) {
                builder.setThumbnail("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg");
            }
        }
        
        return builder.build();
    }

    /**
     * Extract YouTube video ID from URL
     */
    private static String extractYouTubeVideoId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Handle youtu.be short URLs
        if (url.contains("youtu.be/")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                String videoId = parts[1].split("[?&#]")[0];
                return videoId;
            }
        }

        // Handle youtube.com URLs
        if (url.contains("youtube.com/watch?v=")) {
            String[] parts = url.split("v=");
            if (parts.length > 1) {
                String videoId = parts[1].split("[?&#]")[0];
                return videoId;
            }
        }

        return null;
    }
}
