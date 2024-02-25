package org.infinitecoder.codersnetbot.actions;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.infinitecoder.codersnetbot.Main;
import org.infinitecoder.codersnetbot.UI;
import org.infinitecoder.codersnetbot.libs.AudioPlayerSendHandler;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicSystem implements AudioEventListener, AudioLoadResultHandler {
    private AudioChannelUnion voiceChannel = null;
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private AudioPlayer player = null;

    private static MusicSystem instance;

    public static MusicSystem getInstance() {
        if (instance == null) {
            instance = new MusicSystem();
        }
        return instance;
    }

    public static void processCommand(MessageReceivedEvent event, String command) {
        MessageChannel channel = event.getChannel();
        MusicSystem system = getInstance();
        String[] args = command.split(" ", 2);
        if (args.length == 0) return;
        switch (args[0]) {
            case "join":
                if (system.voiceChannel == null && event.getMember() != null && event.getMember().getVoiceState() != null) {
                    system.voiceChannel = event.getMember().getVoiceState().getChannel();
                    if (system.voiceChannel == null) {
                        channel.sendMessage("You are not connected to a voice channel!").queue();
                        return;
                    }
                    AudioManager audioManager = event.getGuild().getAudioManager();
                    audioManager.openAudioConnection(system.voiceChannel);
                    channel.sendMessage("Connected to the voice channel!").queue();
                    UI.showDJControls(true);
                }
                return;

            case "play":
                if (args.length != 2) return;
                if (system.voiceChannel == null) {
                    channel.sendMessage("Connect to a voice channel and type /music join first!").queue();
                    return;
                }

                if (PointsSystem.getInstance().getPoints(event.getAuthor().getName()) < 100) {
                    event.getAuthor().openPrivateChannel().flatMap(ch -> ch.sendMessage(PointsSystem.notEnoguh())).queue();
                    return;
                }

                String id = getYouTubeId(args[1]);

                if (system.player == null) {
                    AudioSourceManagers.registerRemoteSources(system.playerManager);
                    system.player = system.playerManager.createPlayer();
                    system.voiceChannel.getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(system.player));
                    system.player.addListener(system);
                }
                if (id != null) {
                    PointsSystem.getInstance().addPoints(event.getAuthor().getName(), -100);
                    system.playerManager.loadItem(id, system);
                }
                return;
            case "current":
                channel.sendMessage("Now playing: " + (system.player == null || system.player.getPlayingTrack() == null ?
                        "nothing" :
                        system.player.getPlayingTrack().getInfo().title)).queue();
                return;
            case "skip":
                Main.requireAdmin(event, system::skipTrack);
        }
    }

    private static String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed/)[^#&?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    public static MessageEmbed helpMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Music");
        eb.setColor(new Color(0x1F4DFF));
        eb.setDescription(String.format("""
                    Play your favorite songs and listen to them with your friends!
                    /music join - join the voice channel, necessary to play music
                    /music play <youtube link or video ID> - play a song, costs %s
                    /music current - show current playing song
                    For Admins:
                    /music skip - skip current playing track
                """, PointsSystem.format(100)));
        return eb.build();
    }

    public void skipTrack() {
        if (player != null) {
            System.out.println("Skipping the track!");
            player.stopTrack();
        }
    }

    public void exit() {
        if (voiceChannel != null) {
            voiceChannel.getGuild().getAudioManager().closeAudioConnection();
            voiceChannel = null;
        }
        if (player != null) {
            player.stopTrack();
            player.destroy();
            player = null;
        }
        UI.showDJControls(false);
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        UI.musicQueue.addElement(track);
        if (player != null) {
            if (player.getPlayingTrack() == null) {
                System.out.println("Starting the queue!");
                player.playTrack(UI.musicQueue.get(0));
            }
        }
    }

    @Override
    public void onEvent(AudioEvent event) {
        if (event instanceof TrackEndEvent) {
            System.out.println("Track has ended!");
            UI.musicQueue.removeElement(((TrackEndEvent) event).track);
            if (player != null && !UI.musicQueue.isEmpty()) {
                System.out.println("Playing the next track!");
                player.playTrack(UI.musicQueue.get(0));
            }
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        UI.musicQueue.addAll(playlist.getTracks());
    }

    @Override
    public void noMatches() {
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        exception.printStackTrace();
    }
}
