/**
 * Copyright (C) 2012 t7seven7t
 */
package net.t7seven7t.midiradio;

import javax.sound.midi.Track;

/**
 * @author t7seven7t
 */
public class MidiTrack {

	private final MidiPlayer player;
	private final Track track;
	
	private double tick = 0;
	private int event = 0;
	
	public MidiTrack(MidiPlayer player, Track track) {
		this.player = player;
		this.track = track;
	}
	
	public void nextTick() {
		
		tick += player.resolution * player.tempo;
		if (tick >= track.ticks())
			return;
		
		for (; (event < (track.size() - 1)) && (track.get(event).getTick() <= tick); event++) {
			
			player.onMidiEvent(track.get(event));
			
		}
		
	}
	
}
