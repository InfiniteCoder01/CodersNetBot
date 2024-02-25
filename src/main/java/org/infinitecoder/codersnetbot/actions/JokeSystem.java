package org.infinitecoder.codersnetbot.actions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.infinitecoder.codersnetbot.Main;
import org.infinitecoder.codersnetbot.libs.NumberFormatter;

import java.awt.*;
import java.util.*;
import java.util.List;

public class JokeSystem {
    private static final List<Joke> jokes = new ArrayList<>();
    public static final List<String> tokens = new LinkedList<>();
    public static Joke lastJoke = null;

    public static void processCommand(MessageReceivedEvent event, String command) {
        MessageChannel channel = event.getChannel();
        String[] args = command.split(" ");
        if (args.length < 1) return;
        switch (args[0]) {
            case "submit":
                if (args.length < 2) return;
                String token = args[1];
                if (tokens.contains(token)) {
                    tokens.remove(token);
                    String joke = command.replaceFirst("send " + token + " ", "");
                    jokes.add(new Joke(event.getAuthor().getName(), joke));
                } else {
                    channel.sendMessage("Invalid token!").queue();
                }
                break;
            case "random":
                Main.requireAdmin(event, () -> {
                    if (jokes.isEmpty()) {
                        channel.sendMessage("No jokes available!").queue();
                        return;
                    }
                    int index = (int) (Math.random() * jokes.size());
                    Joke joke = jokes.get(index);
                    channel.sendMessage(String.format("Joke from %s: %s", joke.author, joke.joke)).queue();
                    jokes.remove(index);
                    lastJoke = joke;
                });
                break;
            case "good":
                Main.requireAdmin(event, () -> {
                    if (lastJoke != null) {
                        long reward = args.length > 1 ? NumberFormatter.parseStringLong(args[1]) : 300;
                        channel.sendMessage(String.format("Admin liked your joke, @%s! He's gonna give you %s", lastJoke.author, PointsSystem.format(reward))).queue();
                        PointsSystem.getInstance().addPoints(lastJoke.author, reward);
                        lastJoke = null;
                    }
                });
                break;
            case "terrible":
                Main.requireAdmin(event, () -> {
                    if (lastJoke != null) {
                        long punishment = args.length > 1 ? NumberFormatter.parseStringLong(args[1]) : 100;
                        channel.sendMessage(String.format("Admin disliked your joke, @%s! He's punishing you with -%s", lastJoke.author, PointsSystem.format(punishment))).queue();
                        PointsSystem.getInstance().addPoints(lastJoke.author, -punishment);
                        lastJoke = null;
                    }
                });
                break;
        }
    }

    public static MessageEmbed helpMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Jokes");
        eb.setColor(new Color(0xFF0000));
        eb.setDescription("""
                    /joke submit <Token> <Joke> - If you have a joke token, you can submit your own joke with this command. Note, that jokes & joke tokens don't live long!
                    For Admins:
                    /joke random - Choose a random joke to display
                    /joke good - Like the joke and reward the author
                    /joke terrible - Dislike the joke and punish the author
                """);
        return eb.build();
    }

    public static class Joke {
        public String author;
        public String joke;

        public Joke(String author, String joke) {
            this.author = author;
            this.joke = joke;
        }
    }
}
