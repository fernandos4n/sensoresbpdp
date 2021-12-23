package mx.com.cuatronetworks.sensoresbpdp.model;

import java.io.InputStream;

import javazoom.jl.decoder.JavaLayerException;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

/**
 *
 * @author david
 */
public class AudioPlayer {
     public static void play(InputStream speechStream) throws JavaLayerException {
        //
        // create an MP3 player
        //
        AdvancedPlayer player = new AdvancedPlayer(speechStream,javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
 
        player.setPlayBackListener(new PlaybackListener() {
            @Override
            public void playbackStarted(PlaybackEvent evt) {
                System.out.println("Playback started");
            }
            @Override
            public void playbackFinished(PlaybackEvent evt) {
                System.out.println("Playback finished");
            }
        });
        //
        // Play the speech stream
        //
        player.play();
    }
}
