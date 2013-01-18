/**
 * Copyright (C) 2012 t7seven7t
 */
package net.t7seven7t.midiradio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author t7seven7t
 */
public class SequencerMidiPlayer implements Receiver, MidiPlayer {
	private final MidiRadio plugin;
	private final Sequencer sequencer;
	
	private final List<Player> tunedIn = new ArrayList<Player>();
	// For the life of me I can't figure out java's resources for Soundbank so we'll stick with this for now
	private final Map<Integer, Byte> channelPatches = new HashMap<Integer, Byte>(); 
	
	private boolean nowPlaying = false;
	private int currentSong = 0;
	private String midiName;
	
	public SequencerMidiPlayer(MidiRadio plugin) throws MidiUnavailableException {
		this.plugin = plugin;
		
		sequencer = MidiSystem.getSequencer();
		sequencer.open();
		
		if (plugin.getConfig().getBoolean("server-playback")) {
			sequencer.getTransmitter().setReceiver(this);
		} else { // Is the only way to stop this by hogging all transmitters? :/
			for (Transmitter t : sequencer.getTransmitters()) {
				t.setReceiver(this);
			}
		}
	}
	
	public void tuneIn(Player player) {
		tunedIn.add(player);
		
		player.sendMessage(ChatColor.AQUA + "Now playing: " + ChatColor.YELLOW + midiName);
	}
	
	public void tuneOut(Player player) {
		tunedIn.remove(player);
	}
	
	public boolean isNowPlaying() {
		return nowPlaying;
	}
	
	public void stopPlaying() {
		sequencer.stop();
		plugin.getServer().getScheduler().cancelTasks(plugin);
	}
	
	public void playNextSong() {
		currentSong++;
		
		String[] midiFileNames = plugin.listMidiFiles();
		
		if (currentSong >= midiFileNames.length)
			currentSong = 0;
		
		playSong(midiFileNames[currentSong]);
	}
	
	public void playSong(String midiName) {
		
		this.midiName = midiName;
		
		File midiFile = plugin.getMidiFile(midiName);
		if (midiFile == null)
			return;
		
		try {
			Sequence midi = MidiSystem.getSequence(midiFile);
			sequencer.setSequence(midi);
			sequencer.start();
			nowPlaying = true;
		} catch (InvalidMidiDataException ex) {
			System.err.println("Invalid midi file: " + midiName);
		} catch (IOException e) {
			System.err.println("Can't read file: " + midiName);
		}
		
		for (Player player : tunedIn) {
			
			player.sendMessage(ChatColor.AQUA + "Now playing: " + ChatColor.YELLOW + midiName);
			
		}
				
		new BukkitRunnable() {

			@Override
			public void run() {
				if (!nowPlaying)
					this.cancel();
			
				if (!sequencer.isRunning() || sequencer.getMicrosecondPosition() > sequencer.getMicrosecondLength()) {
					stopPlaying();
					playNextSong();
				}
					
			}
			
		}.runTaskTimer(plugin, 20L, 20L);
		
	}
	
	@Override
	protected void finalize() {
		sequencer.close();
	}

	@Override
	public void close() {
		// We don't really need this in this case, thanks anyway oracle <3
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {

		if (!(message instanceof ShortMessage))
			return; // Not interested in meta events
		
		ShortMessage event = (ShortMessage) message;
		
		if (event.getCommand() == ShortMessage.NOTE_ON) {
									
			int midiNote = event.getData1();
			float volume = event.getData2() / 127;
			
			if (volume == 0)
				volume = 1;
			
			int note = Integer.valueOf((midiNote - 6) % 24);
			
			int channel = event.getChannel();
			byte patch = 1;
			if (channelPatches.containsKey(channel))
				patch = channelPatches.get(channel);
			
			for (Player player : tunedIn) {
				
				//Play the sound to each player tuned in
				player.playSound(player.getLocation(), Instrument.getInstrument(patch, channel), volume, NotePitch.getPitch(note));
				
			}
			
		} else if (event.getCommand() == ShortMessage.PROGRAM_CHANGE) {
									
			channelPatches.put(event.getChannel(), (byte) event.getData1());
			
		} else if (event.getCommand() == ShortMessage.STOP) {
			
			stopPlaying();
			playNextSong();
			
		}
	}

}
