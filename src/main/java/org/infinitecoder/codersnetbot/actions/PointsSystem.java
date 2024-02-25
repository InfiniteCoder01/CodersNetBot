package org.infinitecoder.codersnetbot.actions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.infinitecoder.codersnetbot.libs.NumberFormatter;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PointsSystem {
    public static final String command = "bank";

    public static String howManyInToken() {
        return "How many Lines of Code will be in this token?";
    }

    public static String notEnoguh() {
        return "Not enough Lines of Code!";
    }

    public static String format(long amount) {
        final String name = "Lines of Code";
        final NumberFormatter formatter = new NumberFormatter();


        if (amount >= 1000) {
            return String.format("%s %s", formatter.formatNumber(amount), name);
        } else {
            if (amount == 1 || (amount > 20 && amount % 10 == 1)) return String.format("%d Line of Code", amount);
        }
        return String.format("%d %s", amount, name);

    }

    private static PointsSystem instance;

    public static PointsSystem getInstance() {
        if (instance == null) {
            instance = new PointsSystem();
        }
        return instance;
    }

    private final Map<String, Long> points = new HashMap<>();
    private final Map<String, Long> tokens = new HashMap<>();

    public static MessageEmbed helpMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Bank");
        eb.setColor(new Color(0xFF8F1F));
        eb.setDescription(String.format("""
                    Earn Lines Of Code and use them to play music, games and much more!
                    /%s get - See, how many Lines of Code you have
                    /%s add <Token> - If you have a token, you can add Lines of Code to your balance
                """, command, command));
        return eb.build();
    }

//    public static String gameMessageId = "";
//    public static String currentPlayer;
//    public static String currentGame;

    public long getPoints(String nickname) {
        if (!points.containsKey(nickname)) {
            points.put(nickname, 100L);
        }
        return points.get(nickname);
    }

    public void addPoints(String nickname, long difference) {
        points.put(nickname, getPoints(nickname) + difference);
        savePoints();
    }

    public void addToken(String id, long amount) {
        tokens.put(id, amount);
        saveTokens();
    }

    public static void processCommand(MessageReceivedEvent event, String command) {
        MessageChannel channel = event.getChannel();
        PointsSystem system = getInstance();
        String authorName = event.getAuthor().getName();
        String[] args = command.split(" ");
        if (args.length == 0) return;
        switch (args[0]) {
            case "get":
                event.getAuthor().openPrivateChannel().flatMap(ch -> ch.sendMessage(String.format("You have %s!", format(system.getPoints(authorName))))).queue();
                break;
            case "add":
                if (args.length != 2) return;
                String token = args[1];
                if (system.tokens.containsKey(token)) {
                    long reward = system.tokens.remove(token);
                    system.saveTokens();
                    system.addPoints(authorName, reward);
                    event.getAuthor().openPrivateChannel().flatMap(ch -> ch.sendMessage(String.format("You have got %s! Now you have %s!", format(reward), format(system.getPoints(authorName))))).queue();
                } else {
                    channel.sendMessage("Invalid token!").queue();
                }
                break;
        }
    }

    private void savePoints() {
        try {
            FileWriter fw = new FileWriter("bank.txt", false);
            StringBuilder pointsString = new StringBuilder();
            for (Map.Entry<String, Long> user : points.entrySet()) {
                pointsString.append(String.format("%s@@%d\n", user.getKey(), user.getValue()));
            }
            fw.write(pointsString.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTokens() {
        try {
            FileWriter fw = new FileWriter("tokens.txt", false);
            StringBuilder pointsString = new StringBuilder();
            for (Map.Entry<String, Long> user : tokens.entrySet()) {
                pointsString.append(String.format("%s@@%d\n", user.getKey(), user.getValue()));
            }
            fw.write(pointsString.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PointsSystem() {
        try {
            Scanner s = new Scanner(new File("bank.txt"));
            while (s.hasNextLine()) {
                String[] pair = s.nextLine().split("@@");
                points.put(pair[0], Long.parseLong(pair[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Scanner s = new Scanner(new File("tokens.txt"));
            while (s.hasNextLine()) {
                String[] pair = s.nextLine().split("@@");
                tokens.put(pair[0], Long.parseLong(pair[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

