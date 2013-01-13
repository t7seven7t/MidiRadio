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
		
		if (instrument >= 1 && instrument <= 8 || // Piano family
			(instrument >= 17 && instrument <= 27) || // Organs, Electric guitar (jazz) and acoustic guitards
			(instrument >= 41 && instrument <= 43) || 
			(instrument >= 47 && instrument <= 112)) { // Strings, ensemble, brass, reed, pipe, synth lead, pad & effects, ethnic
			
			return Sound.NOTE_PIANO;
						
		} else if ((instrument >= 28 && instrument <= 40) || // Guitar starting from Electric (clean) & bass
					(instrument >= 44 && instrument <= 46)) { // Contrabass to Pizzicato
			
			return Sound.NOTE_BASS_GUITAR;
			
		} else if (instrument == 0 || // drums
				(instrument >= 113 && instrument <= 119)) { // percussive
			
			return Sound.NOTE_BASS_DRUM;
			
		} else if (instrument >= 120 && instrument <= 127){
			
			return Sound.NOTE_SNARE_DRUM;
			
		} else {
			
			return Sound.NOTE_PIANO;
			
		}
			
	}
	
}
