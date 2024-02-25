package org.infinitecoder.codersnetbot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import org.infinitecoder.codersnetbot.actions.JokeSystem;
import org.infinitecoder.codersnetbot.actions.MusicSystem;
import org.infinitecoder.codersnetbot.actions.PointsSystem;
import org.infinitecoder.codersnetbot.libs.NumberFormatter;

import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;

public class UI extends JFrame {
    public static final DefaultListModel<AudioTrack> musicQueue = new DefaultListModel<>();
    private static final ArrayList<Component> audioControls = new ArrayList<>();
    private final JTextField messageInput = new JTextField();

    public UI() throws HeadlessException {
        super("CodersNet Bot Control Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(generateButton("New Token", e -> makeTokenUI(askTokenType())));
        {
            audioControls.add(generateButton("Skip Track", e -> MusicSystem.getInstance().skipTrack()));
            audioControls.add(generateButton("Quit Voice Channel", e -> MusicSystem.getInstance().exit()));
            for (Component component : audioControls) controlPanel.add(component);
            showDJControls(false);
        }

        {
            Container container = getContentPane();

            container.add(controlPanel, BorderLayout.NORTH);
            JList<AudioTrack> musicList = new JList<>(musicQueue);
            musicList.setCellRenderer(new MusicListRenderer());
            container.add(new JScrollPane(musicList));
            container.add(messageInput, BorderLayout.SOUTH);
        }

        messageInput.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.jda.getTextChannelsByName("general", true).get(0).sendMessage(messageInput.getText()).queue();
                messageInput.setText("");
            }
        });

        pack();
        setVisible(true);
    }

    public static void showDJControls(boolean show) {
        for (Component component : audioControls) component.setVisible(show);
    }

    private static JButton generateButton(String text, ActionListener l) {
        JButton button = new JButton(text);
        button.addActionListener(l);
        return button;
    }

    private static String askTokenType() {
        String[] options = {"Bank", "Joke"};
        return (String) JOptionPane.showInputDialog(null, "Select token type", "New Token",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

    public static void makeTokenUI(String type) {
        String token = generateTokenText(type.toLowerCase(), 1024);
        switch (type) {
            case "Bank" -> {
                long amount = 100;
                while (true) {
                    try {
                        amount = (long) new NumberFormatter().parseString(showInputDialog(PointsSystem.howManyInToken(), "100"));
                        break;
                    } catch (Exception ignored) {
                        showMessageDialog(null, "Invalid amount!");
                    }
                }
                PointsSystem.getInstance().addToken(token, amount);
            }
            case "Joke" -> JokeSystem.tokens.add(token);
        }
        showMessageDialog(null, new JTextArea(String.format("%s token: %s!", type, token)));
    }

    public static String generateTokenText(String prefix, int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";
        StringBuilder token = new StringBuilder(prefix);
        for (int i = 0; i < length; i++) {
            token.append(characters.charAt((int) (characters.length() * Math.random())));
        }

        return token.toString();
    }

    public static class MusicListRenderer implements ListCellRenderer<AudioTrack> {
        @Override
        public Component getListCellRendererComponent(JList<? extends AudioTrack> list, AudioTrack value, int index, boolean isSelected, boolean cellHasFocus) {
            return new JLabel(value.getInfo().title);
        }
    }

    public static void refreshMusicQueue() {
        for (int i = 0; i < musicQueue.size(); i++) {
            musicQueue.setElementAt(musicQueue.getElementAt(i), i);
        }
    }
}
