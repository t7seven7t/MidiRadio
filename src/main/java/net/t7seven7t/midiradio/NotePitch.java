/**
 * Copyright (C) 2012 t7seven7t
 */
package net.t7seven7t.midiradio;

/**
 * @author t7seven7t
 */
public enum NotePitch {
	NOTE_0(0, 0.5f),
	NOTE_1(1, 0.53f),
	NOTE_2(2, 0.56f),
	NOTE_3(3, 0.6f),
	NOTE_4(4, 0.63f),
	NOTE_5(5, 0.67f),
	NOTE_6(6, 0.7f),
	NOTE_7(7, 0.76f),
	NOTE_8(8, 0.8f),
	NOTE_9(9, 0.84f),
	NOTE_10(10, 0.9f),
	NOTE_11(11, 0.94f),
	NOTE_12(12, 1f),
	NOTE_13(13, 1.06f),
	NOTE_14(14, 1.12f),
	NOTE_15(15, 1.18f),
	NOTE_16(16, 1.26f),
	NOTE_17(17, 1.34f),
	NOTE_18(18, 1.42f),
	NOTE_19(19, 1.5f),
	NOTE_20(20, 1.6f),
	NOTE_21(21, 1.68f),
	NOTE_22(22, 1.78f),
	NOTE_23(23, 1.88f),
	NOTE_24(24, 2f);
	
	public int note;
	public float pitch;
	NotePitch(int note, float pitch) {
		this.note = note;
		this.pitch = pitch;
	}
	
	public static float getPitch(int note) {
		
		for (NotePitch notePitch : values()) {
			
			if (notePitch.note == note)
				return notePitch.pitch;
			
		}
		
		return 0;
		
	}
	
}
