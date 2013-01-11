/**
 * Copyright (C) 2012 t7seven7t
 */
package net.t7seven7t.midiradio;

import org.bukkit.Sound;

/**
 * @author t7seven7t
 */
public class Instrument {
	
	// This method could use some fixing to include the other noteblock sounds...
	public static Sound getInstrument(int instrument) {
		
		if (instrument <= 8 || // Piano family
			(instrument >= 17 && instrument <= 32) || // Organs, guitars
			(instrument >= 41 && instrument <= 112)) { // Strings, ensemble, brass, reed, pipe, synth, ethnic
			
			return Sound.NOTE_PIANO;
						
		} else {
			
			return Sound.NOTE_BASS_GUITAR;
			
		}
		
	}
	
}
