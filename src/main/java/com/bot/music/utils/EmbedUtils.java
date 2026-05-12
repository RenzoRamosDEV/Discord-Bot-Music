package com.bot.music.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;

public class EmbedUtils {

    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color INFO_COLOR = new Color(52, 152, 219);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color MUSIC_COLOR = new Color(155, 89, 182);

    public static MessageEmbed successEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed errorEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(ERROR_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed infoEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(INFO_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }

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

    public static MessageEmbed warningEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(WARNING_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed musicEmbed(String title, String description, String thumbnailUrl) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(MUSIC_COLOR)
                .setTimestamp(Instant.now());

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

    private static String extractYouTubeVideoId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        if (url.contains("youtu.be/")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                return parts[1].split("[?&#]")[0];
            }
        }

        if (url.contains("youtube.com/watch?v=")) {
            String[] parts = url.split("v=");
            if (parts.length > 1) {
                return parts[1].split("[?&#]")[0];
            }
        }

        return null;
    }
}
