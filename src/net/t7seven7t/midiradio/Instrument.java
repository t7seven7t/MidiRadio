/**
 * Copyright (C) 2012 t7seven7t
 */
package net.t7seven7t.midiradio;

import org.bukkit.Sound;

/**
 * @author t7seven7t
 */
public class Instrument {
	
	public static Sound getInstrument(byte patch, int channel) {
		
		if (channel == 9) { // Drums - should actually be 10 but for some reason java makes it 9...
			return Sound.NOTE_BASS_DRUM;
		}
		
		if ((patch >= 28 && patch <= 40) || (patch >= 44 && patch <= 46)) { // Guitars & bass
			return Sound.NOTE_BASS_GUITAR;
		}
		
		if (patch >= 113 && patch <= 119) { // Percussive
			return Sound.NOTE_BASS_DRUM;
		}
		
		if (patch >= 120 && patch <= 127) { // Misc.
			return Sound.NOTE_SNARE_DRUM;
		}
		
		return Sound.NOTE_PIANO;
		
	}
	
}
