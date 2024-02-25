package org.infinitecoder.codersnetbot.actions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.infinitecoder.codersnetbot.actions.games.Game;
import org.infinitecoder.codersnetbot.actions.games.Snake;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GameManager {
    public static Map<String, GameManager> games = new HashMap<>();

    public static final char[][] gameOverPattern = {
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', 'g', 'a', 'm', 'e', ' ', 'o', 'v', 'e', 'r', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'}
    };
    public static final char[][] winPattern = {
            {'#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', 'y', 'o', 'u', ' ', 'w', 'i', 'n', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#'}
    };

    public static char[][] buildEmptyField(int width, int height) {
        char[][] map = new char[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                map[i][j] = ' ';
            }
        }
        for (int i = 0; i < width; i++) {
            map[0][i] = '#';
            map[height - 1][i] = '#';
        }
        for (int i = 0; i < height; i++) {
            map[i][0] = '#';
            map[i][width - 1] = '#';
        }
        return map;
    }

    public static void processCommand(MessageReceivedEvent event, String command) {
        MessageChannel channel = event.getChannel();
        String[] args = command.split(" ");
        if (args.length != 1) return;
        switch (args[0].toLowerCase()) {
            case "snake":
                if (PointsSystem.getInstance().getPoints(event.getAuthor().getName()) >= 100) {
                    PointsSystem.getInstance().addPoints(event.getAuthor().getName(), -100);
                    new GameManager(channel, new Snake(event.getAuthor().getName()));
                } else {
                    event.getAuthor().openPrivateChannel().flatMap(ch -> ch.sendMessage(PointsSystem.notEnoguh())).queue();
                }
                break;
        }
    }

    public Game game;
    public String gameMessageId = null;

    public GameManager(MessageChannel channel, Game game) {
        this.game = game;
        updateDisplay(channel, game.display(), game.getControls());
    }

    public static MessageEmbed helpMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Games");
        eb.setColor(new Color(0x2EFF1F));
        eb.setDescription(String.format("""
                    Play games and have fun :)
                    /play <Game that you want to play>
                    Games:
                    Snake - costs 100 Points to play, you get 1000 points if you win 
                    
                """));
        return eb.build();
    }

    public void updateDisplay(MessageChannel channel, char[][] pattern, Emoji[] reactions) {
        StringBuilder sb = buildDiscordGrid(pattern);
        if (gameMessageId != null) {
            channel.editMessageById(gameMessageId, sb.toString()).queue(msg -> {
                gameMessageId = msg.getId();
                for (Emoji reaction : reactions) {
                    msg.addReaction(reaction).queue();
                }
            });
            for (Emoji reaction : reactions) {
                channel.removeReactionById(gameMessageId, reaction);
            }
        } else {
            channel.sendMessage(sb.toString()).queue(msg -> {
                gameMessageId = msg.getId();
                games.put(gameMessageId, this);
                for (Emoji reaction : reactions) {
                    msg.addReaction(reaction).queue();
                }
            });
        }
    }

    public GameState gameAction(String event, MessageChannel channel) {
        GameState state = game.action(event);
        if (state == GameState.GameOver) {
            updateDisplay(channel, gameOverPattern, new Emoji[0]);
        } else if (state == GameState.Win) {
            updateDisplay(channel, winPattern, new Emoji[0]);
        } else if (state == GameState.Playing) {
            updateDisplay(channel, game.display(), game.getControls());
        }
        return state;
    }

    private static StringBuilder buildDiscordGrid(char[][] pattern) {
        StringBuilder sb = new StringBuilder();
        for (char[] line : pattern) {
            for (char cell : line) {
                sb.append(switch (cell) {
                    case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' ->
                            String.format(":regional_indicator_%c:", cell);
                    case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> ":1234:";
                    case '\uF8FF' -> ":apple:";
                    case ')' -> ":grinning:";
                    case '/' -> ":green_square:";
                    case ' ' -> ":black_large_square:";
                    case '#' -> ":white_large_square:";
                    default -> "?";
                });
            }
            sb.append("\n");
        }
        return sb;
    }

    public enum GameState {
        Playing,
        Win,
        GameOver
    }
}
