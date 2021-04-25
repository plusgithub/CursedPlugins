package com.cursedplanet.cursedplugins.plugin;

import com.cursedplanet.cursedlibrary.lib.Common;
import com.cursedplanet.cursedlibrary.lib.collection.StrictList;
import com.cursedplanet.cursedlibrary.lib.command.SimpleCommandGroup;
import com.cursedplanet.cursedlibrary.lib.event.SimpleListener;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * A simple way of registering events and other things that
 * are cancelled automatically when the plugin is reloaded.
 */
final class Reloadables {

	/**
	 * A list of currently enabled event listeners
	 */
	private final StrictList<Listener> listeners = new StrictList<>();

	/**
	 * A list of registered command groups
	 */
	private final StrictList<SimpleCommandGroup> commandGroups = new StrictList<>();

	// -------------------------------------------------------------------------------------------
	// Main
	// -------------------------------------------------------------------------------------------

	/**
	 * Remove all listeners and cancel all running tasks
	 */
	void reload() {
		for (final Listener listener : listeners)
			HandlerList.unregisterAll(listener);

		listeners.clear();

		for (final SimpleCommandGroup commandGroup : commandGroups)
			commandGroup.unregister();

		commandGroups.clear();
	}

	// -------------------------------------------------------------------------------------------
	// Events / Listeners
	// -------------------------------------------------------------------------------------------

	/**
	 * Register events to Bukkit
	 *
	 * @param listener
	 */
	void registerEvents(Listener listener) {
		Common.registerEvents(listener);

		listeners.add(listener);
	}

	/**
	 * Register events to Bukkit using our listener
	 *
	 * @param <T>
	 * @param listener
	 */
	<T extends Event> void registerEvents(SimpleListener<T> listener) {
		listener.register();

		listeners.add(listener);
	}

	// -------------------------------------------------------------------------------------------
	// Command groups
	// -------------------------------------------------------------------------------------------

	/**
	 * Register the given command group
	 *
	 * @param label
	 * @param aliases
	 * @param group
	 */
	void registerCommands(final String label, final List<String> aliases, final SimpleCommandGroup group) {
		group.register(label, aliases);

		commandGroups.add(group);
	}

	/**
	 * Register the given command group
	 *
	 * @param labelAndAliases
	 * @param group
	 */
	void registerCommands(final StrictList<String> labelAndAliases, final SimpleCommandGroup group) {
		group.register(labelAndAliases);

		commandGroups.add(group);
	}
}
