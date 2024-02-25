package org.infinitecoder.codersnetbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.infinitecoder.codersnetbot.actions.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;

public class Main extends ListenerAdapter {
    public static JDA jda;
//    public static final String helpText = String.format("""
//            /help - display this help
//            /music play <YouTube video link> - play youtube video as music, cost 100 coins
//            /music current - display current playing track
//            /music skip - for admins, skip current track
//            /music join - invite bot to the music channel
//            /joke send <Token> <Joke> - send joke, token is joke token from admin
//            /joke random - for admins, choose random joke from list, display it and remove it from list
//            /%s get - get count of you coins
//            /%s add <Token> - use token and add coins to you balance
//            /%s play <Game name> - play the game (cost 100 coins), if you win, you will get 300 coins
//            Supported games: snake!""", PointsSystem.singlePointName, PointsSystem.singlePointName, PointsSystem.singlePointName);

    public static void main(String[] args) throws IOException {
        jda = JDABuilder.createDefault(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("token.txt"))).trim()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        jda.addEventListener(new Main());
        new UI();
    }

    public static boolean hasRole(MessageReceivedEvent event, String role) {
        Guild guild = event.getGuild();
        return guild.getMembersWithRoles(guild.getRolesByName(role, true).stream().findFirst().orElse(null)).contains(event.getMember());
    }

    public static void requireAdmin(MessageReceivedEvent event, Runnable action) {
        if (hasRole(event, "Admin")) {
            action.run();
        } else {
            event.getChannel().sendMessage("You must be an admin!").queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        System.out.println("[" + event.getAuthor().getName() + "] " + event.getMessage().getContentDisplay());

        if (event.getMessage().getContentRaw().equals("/help")) {
            event.getMessage().delete().queue();
            event.getChannel().sendMessageEmbeds(
                    MusicSystem.helpMessage(),
                    PointsSystem.helpMessage(),
                    GameManager.helpMessage(),
                    JokeSystem.helpMessage()
            ).queue();
        }

        if (event.getMessage().getContentRaw().startsWith("/music")) {
            MusicSystem.processCommand(event, event.getMessage().getContentRaw().substring(7));
            event.getMessage().delete().queue();
        }

        if (event.getMessage().getContentRaw().startsWith("/" + PointsSystem.command)) {
            PointsSystem.processCommand(event, event.getMessage().getContentRaw().substring(PointsSystem.command.length() + 2));
            event.getMessage().delete().queue();
        }

        if (event.getMessage().getContentRaw().startsWith("/play")) {
            GameManager.processCommand(event, event.getMessage().getContentRaw().substring(6));
            event.getMessage().delete().queue();
        }

        if (event.getMessage().getContentRaw().startsWith("/joke")) {
            JokeSystem.processCommand(event, event.getMessage().getContentRaw().substring(6));
            event.getMessage().delete().queue();
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) {
            return;
        }

        if (GameManager.games.containsKey(event.getMessageId())) {
            if (GameManager.games.get(event.getMessageId()).gameAction(event.getReaction().getEmoji().getName(), event.getChannel()) != GameManager.GameState.Playing) {
                GameManager.games.remove(event.getMessageId());
            }
        }
    }
}
