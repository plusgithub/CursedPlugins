package com.cursedplanet.cursedplugins.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.cursedplanet.cursedlibrary.lib.model.HookManager;
import com.cursedplanet.cursedlibrary.lib.model.SimpleEnchantment;
import com.cursedplanet.cursedlibrary.lib.remain.CompMaterial;
import org.bukkit.inventory.ItemStack;

/**
 * Listens to and intercepts packets using Foundation inbuilt features
 */
final class FoundationPacketListener {

	/**
	 * Registers our packet listener for some of the more advanced features of Foundation
	 */
	static void addPacketListener() {
		if (HookManager.isProtocolLibLoaded())

			// Auto placement of our lore when items are custom enchanted
			HookManager.addPacketListener(new PacketAdapter(SimplePlugin.getInstance(), PacketType.Play.Server.SET_SLOT) {
				@Override
				public void onPacketSending(PacketEvent event) {

					final StructureModifier<ItemStack> itemModifier = event.getPacket().getItemModifier();
					ItemStack item = itemModifier.read(0);

					if (item != null && !CompMaterial.isAir(item.getType())) {
						item = SimpleEnchantment.addEnchantmentLores(item);

						// Write the item
						if (item != null)
							itemModifier.write(0, item);
					}
				}
			});
	}
}
