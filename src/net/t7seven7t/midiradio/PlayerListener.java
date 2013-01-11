/**
 * Copyright (C) 2012 t7seven7t
 */
package net.t7seven7t.midiradio;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author t7seven7t
 */
public class PlayerListener implements Listener {
	private final MidiRadio plugin;
	
	public PlayerListener(final MidiRadio plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
//		plugin.getMidiPlayer().tuneIn(event.getPlayer());
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		plugin.getMidiPlayer().tuneOut(event.getPlayer());
		
	}
	
}
