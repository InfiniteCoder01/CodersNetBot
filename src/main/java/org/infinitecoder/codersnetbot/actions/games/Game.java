package org.infinitecoder.codersnetbot.actions.games;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.infinitecoder.codersnetbot.actions.GameManager;

public interface Game {
    char[][] display();

    Emoji[] getControls();

    GameManager.GameState action(String event);
}
