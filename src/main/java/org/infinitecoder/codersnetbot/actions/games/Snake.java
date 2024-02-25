package org.infinitecoder.codersnetbot.actions.games;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.infinitecoder.codersnetbot.Main;
import org.infinitecoder.codersnetbot.UI;
import org.infinitecoder.codersnetbot.actions.GameManager;
import org.infinitecoder.codersnetbot.actions.PointsSystem;

import java.util.LinkedList;
import java.util.List;

public class Snake implements Game {
    private static final int mapWidth = 7, mapHeight = 5;
    private int appleX, appleY;
    private List<List<Integer>> snake;
    private String player;

    public Snake(String player) {
        this.player = player;
        snake = new LinkedList<>();
        snake.add(new LinkedList<>(List.of(3, 1)));
        snake.add(new LinkedList<>(List.of(2, 1)));
        snake.add(new LinkedList<>(List.of(1, 1)));
        randomizeApple();
    }

    private void randomizeApple() {
        char[][] map = display();
        while (map[appleY][appleX] != ' ') {
            appleX = (int) (Math.random() * (mapWidth - 2) + 1);
            appleY = (int) (Math.random() * (mapHeight - 2) + 1);
        }
    }

    public char[][] buildMaze() {
        char[][] map = GameManager.buildEmptyField(mapWidth, mapHeight);
        for (int i = 1; i < snake.size(); i++) {
            map[snake.get(i).get(1)][snake.get(i).get(0)] = '/';
        }
        return map;
    }

    public char[][] display() {
        char[][] map = buildMaze();
        map[appleY][appleX] = '\uF8FF';
        map[snake.get(0).get(1)][snake.get(0).get(0)] = ')';
        return map;
    }

    @Override
    public Emoji[] getControls() {
        return new Emoji[]{Emoji.fromUnicode("U+2B05"), Emoji.fromUnicode("U+27A1"), Emoji.fromUnicode("U+2B06"), Emoji.fromUnicode("U+2B07")};
    }

    public GameManager.GameState action(String event) {
        for (int i = snake.size() - 1; i > 0; i--) {
            snake.set(i, new LinkedList<>(snake.get(i - 1)));
        }
        switch (event) {
            case "⬅" -> snake.get(0).set(0, snake.get(0).get(0) - 1);
            case "➡" -> snake.get(0).set(0, snake.get(0).get(0) + 1);
            case "⬆" -> snake.get(0).set(1, snake.get(0).get(1) - 1);
            case "⬇" -> snake.get(0).set(1, snake.get(0).get(1) + 1);
        }
        char[][] map = buildMaze();
        if ("/#".indexOf(map[snake.get(0).get(1)][snake.get(0).get(0)]) != -1 && snake.size() > 1 && !snake.get(0).equals(snake.get(1))) {
            return GameManager.GameState.GameOver;
        }
        if (snake.get(0).get(0) == appleX && snake.get(0).get(1) == appleY) {
            randomizeApple();
            snake.add(1, new LinkedList<>(snake.get(0)));
            if (snake.size() >= ((mapWidth - 2) * (mapHeight - 2) - 1)) {
                String token = UI.generateTokenText("game", 1024);
                Main.jda.getUsersByName(player, false).get(0).openPrivateChannel().flatMap(ch -> ch.sendMessage("You won! You reward (apply with /bank add <token>): " + token)).queue();
                PointsSystem.getInstance().addToken(token, 1000);
                return GameManager.GameState.Win;
            }
        }
        return GameManager.GameState.Playing;
    }
}
