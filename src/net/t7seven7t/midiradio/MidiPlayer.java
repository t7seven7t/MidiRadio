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
import java.util.TimerTask;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author t7seven7t
 */
public class MidiPlayer {
	// 10 milliseconds per tick rather than bukkit's 50
	public static final long MILLIS_PER_TICK = 10;
	
	private final MidiRadio plugin;
	
	private final List<Player> tunedIn = new ArrayList<Player>();
	private final List<MidiTrack> midiTracks = new ArrayList<MidiTrack>();
	private final Map<Byte, Byte> channelInstruments = new HashMap<Byte, Byte>();
	
	private @Getter boolean nowPlaying = false;
	public float tempo;
	public int resolution;
	private long timeLeft;
	
	private int currSong = 0;
	private String midiName;
	
	public MidiPlayer(MidiRadio plugin) {
		this.plugin = plugin;
	}
	
	public void tuneIn(Player player) {
		
		tunedIn.add(player);
		
		player.sendMessage(ChatColor.AQUA + "Now playing: " + ChatColor.YELLOW + midiName);

	}
	
	public void tuneOut(Player player) {
		tunedIn.remove(player);
	}
	
	public void stopPlaying() {
		
		synchronized (midiTracks) {
			
			nowPlaying = false;
			midiTracks.clear();
			plugin.resetTimer();
			
		}
		
	}
	
	public void playNextSong() {
		currSong++;
		
		String[] midiFileNames = plugin.listMidiFiles();
		
		if (currSong >= midiFileNames.length)
			currSong = 0;
		
		playMidi(midiFileNames[currSong]);
	}
	
	// This method learns the music and then plays it
	public void playMidi(final String midiName) {
		
		this.midiName = midiName;
		
		File midiFile = plugin.getMidiFile(midiName);
		if (midiFile == null)
			return;
		
		int track = -1;
		
		try {
		
			Sequence midi = MidiSystem.getSequence(midiFile);
			if (midi.getTracks().length <= track)
				return;
			
			/** 
			 * 	Following code grabs the MPQN and converts it 
			 * 	into a tempo (I think? - not a muso here :P)
			 */
			int microsPerQuarterNote = 0;
			// Get the first track (depending on midi type the file may have more than one track but usually tempo data is in first track)
			Track firstTrack = midi.getTracks()[0];
			// Iterate over first track till we find a tempo change event
			for (int i = 0; i < firstTrack.size(); i++) {
				
				if (firstTrack.get(i).getMessage().getStatus() == MetaMessage.META
						&& firstTrack.get(i).getMessage().getMessage()[1] == 81 /** this is equivalent to getting type on MetaMessage **/) {
					
					MetaMessage message = (MetaMessage) firstTrack.get(i).getMessage();
					byte[] data = message.getData();
					
					for (byte b : data) {
						
						// Shift what we have left 8 bits and add the next byte
						microsPerQuarterNote <<= 8;
						microsPerQuarterNote += b;
						
					}
					
				}
				
			}			
			
			if (microsPerQuarterNote > 0) {
				
				// Adapted from the original tempo in midibanks to account for the change in millis per tick
				tempo = (500000.0f / microsPerQuarterNote) * 0.8f * (MILLIS_PER_TICK / 20f);
				
			}
			
			timeLeft = midi.getMicrosecondLength() / 1000;
			resolution = (int) Math.floor(midi.getResolution() / 24);
			
			// There probably shouldn't be a need for this method to be synchronized any longer - only useful if we don't know if the player is already playing
			synchronized(midiTracks) {
				
				if (track < 0) {
					
					for (int i = 0; i < midi.getTracks().length; i++) {
						
						MidiTrack midiTrack = new MidiTrack(this, midi.getTracks()[i]);
						midiTracks.add(midiTrack);
						
					}
					
				}
				
			}			
			
		} catch (InvalidMidiDataException ex) {
			System.err.println("Invalid midi file: " + midiName);
		} catch (IOException ex) {
			System.err.println("Can't read file: " + midiName);
		}
		
		for (Player player : tunedIn) {
			
			player.sendMessage(ChatColor.AQUA + "Now playing: " + ChatColor.YELLOW + midiName);
			
		}
		
		plugin.getTimer().scheduleAtFixedRate(new TickTask(), MILLIS_PER_TICK, MILLIS_PER_TICK);
		
	}
	
	public void onMidiEvent(MidiEvent event) {
		
		if (event.getMessage().getStatus() >> 4 == 0x9) { // Note ON event
			
			// Grab note and volume from message
			int midiNote = event.getMessage().getMessage()[1];
			float volume = event.getMessage().getMessage()[2] / 127;
			
			if (volume == 0)
				volume = 1;
					
			// Translate note into minecraft note - the 6 translates it into the right note range
			int note = Integer.valueOf((midiNote - 6) % 24);
			
			// We need to store instrument information so we can change them
			int channel = event.getMessage().getStatus() - (0x9 << 4);
			byte instrument = 1;
			if (channelInstruments.containsKey((byte) channel))
				instrument = channelInstruments.get((byte) channel);
			
			if (note >= 0) {
				
				for (Player player : tunedIn) {
					
					// Play the sound to each player tuned in
					player.playSound(player.getLocation(), Instrument.getInstrument(instrument), volume, NotePitch.getPitch(note));
					
				}
				
			}
			
		} else if (event.getMessage().getStatus() == MetaMessage.META) { // Meta message event
			
			MetaMessage message = (MetaMessage) event.getMessage();
			
			// Tempo event to change tempo for all tracks
			if (message.getType() == 0x51) {
				
				int microsPerQuarterNote = 0;
				byte[] data = message.getData();
				
				for (byte b : data) {
					
					microsPerQuarterNote <<= 8;
					microsPerQuarterNote += b;
					
				}
				
				if (microsPerQuarterNote > 0) {
					
					// Adapted from the original tempo in midibanks to account for the change in millis per tick
					tempo = (500000.0f / microsPerQuarterNote) * 0.8f * (MILLIS_PER_TICK / 20f);
					
				}
				
				return;
				
			}
			
		} else if (event.getMessage().getStatus() >> 4 == 0xC) { 
			
			// Program change.
			int channel = event.getMessage().getStatus() - (0xC << 4);
			
			channelInstruments.put((byte) channel, event.getMessage().getMessage()[1]);
			
		}
		
	}
	
	public class TickTask extends TimerTask {
		
		public TickTask() {
			super();
			nowPlaying = true;
		}
		
		public void run() {
			
			if (nowPlaying) {
				
				synchronized(midiTracks) {
					
					for (MidiTrack track : midiTracks) {
						track.nextTick();
					}
					
				}
				
				timeLeft -= MILLIS_PER_TICK;
				
				if (timeLeft < 0) {
					
					stopPlaying();
					
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable() {
						
						public void run() {
							
							playNextSong();
							
						}
						
					});
					
				}
				
			} else {
				
				this.cancel();
				
			}
			
		}
		
	}

}
