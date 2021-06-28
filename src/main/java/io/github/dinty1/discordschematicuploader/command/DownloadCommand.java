package io.github.dinty1.discordschematicuploader.command;

import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import io.github.dinty1.discordschematicuploader.util.MessageUtil;

import java.awt.*;
import java.io.File;
import java.util.Objects;

public class DownloadCommand {

    public static void execute(DiscordGuildMessagePreProcessEvent event, File schematicFolder) {
        final Message message = event.getMessage();
        final String downloadCommand = Objects.requireNonNull(DiscordSchematicUploader.getPlugin().getConfig().getString("download-command"));

        event.setCancelled(true);

        // Make sure there's a schem name specified
        if (message.getContentRaw().trim().equals(downloadCommand)) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "You must specify the name of the schematic that you want to download.").build()).queue();
        } else {
            // Adding 1 to the length because the arg is specified after a space
            final String[] args = message.getContentRaw().substring(downloadCommand.length() + 1).split(" ");

            String schematicFileExtension = ".schem";

            // Make sure that it exists in one form or another
            if (!new File(schematicFolder, args[0] + ".schem").exists()) {
                if (!new File(schematicFolder, args[0] + ".schematic").exists()) {
                    message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "That schematic doesn't seem to exist.").build()).queue();
                    return;
                } else {
                    schematicFileExtension = ".schematic";
                }
            }

            String finalSchematicFileExtension = schematicFileExtension;
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.GRAY, message.getAuthor(), "Attempting to download schematic `" + args[0] + schematicFileExtension + "`...").build()).queue(sentMessage -> {
                final File schematicToDownload = new File(schematicFolder, args[0] + finalSchematicFileExtension);

                try {
                    message.getChannel().sendMessage("Here you go!").addFile(schematicToDownload).queue(sentSchematicMessage -> {
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), "Download successful!").build()).queue();
                    });
                } catch (IllegalArgumentException e) {
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "An error occurred when trying to download the schematic. The most likely cause is that it is too large to upload to Discord!").build()).queue();
                } catch (Exception e) {
                    DiscordSchematicUploader.getPlugin().getLogger().severe(e.getMessage());
                    e.printStackTrace();
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "An unknown error occurred when trying to download the schematic. Please check the server console for more details.").build()).queue();
                }
            });

        }
    }
}
