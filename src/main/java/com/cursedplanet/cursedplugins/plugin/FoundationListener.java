package com.cursedplanet.cursedplugins.plugin;

import com.cursedplanet.cursedlibrary.lib.*;
import com.cursedplanet.cursedlibrary.lib.MinecraftVersion.V;
import com.cursedplanet.cursedlibrary.lib.constants.FoPermissions;
import com.cursedplanet.cursedlibrary.lib.model.*;
import com.cursedplanet.cursedlibrary.lib.settings.SimpleLocalization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServiceRegisterEvent;

import java.util.List;
import java.util.Map;

/**
 * Listens for some events we handle for you automatically
 */
final class FoundationListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent event) {
		final SpigotUpdater check = SimplePlugin.getInstance().getUpdateCheck();

		if (check != null && check.isNewVersionAvailable() && PlayerUtil.hasPerm(event.getPlayer(), FoPermissions.NOTIFY_UPDATE))
			Common.tellLater(4 * 20, event.getPlayer(), check.getNotifyMessage());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event) {
		SimpleScoreboard.clearBoardsFor(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServiceRegister(ServiceRegisterEvent event) {
		HookManager.updateVaultIntegration();
	}

	/**
	 * Handler for {@link ChatPaginator}
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {

		final Player player = event.getPlayer();
		final String message = event.getMessage();
		final String[] args = message.split(" ");

		if (!message.startsWith("/#flp"))
			return;

		if (args.length != 2) {
			Common.tell(player, SimpleLocalization.Pages.NO_PAGE_NUMBER);

			event.setCancelled(true);
			return;
		}

		final String nbtPageTag = ChatPaginator.getPageNbtTag();

		if (!player.hasMetadata(nbtPageTag)) {
			event.setCancelled(true);

			return;
		}

		// Prevent shading issue with multiple plugins having Foundation shaded
		if (player.hasMetadata("FoPages") && !player.getMetadata("FoPages").get(0).asString().equals(SimplePlugin.getNamed()))
			return;

		final String numberRaw = args[1];
		int page = -1;

		try {
			page = Integer.parseInt(numberRaw) - 1;

		} catch (final NumberFormatException ex) {
			Common.tell(player, SimpleLocalization.Pages.INVALID_PAGE.replace("{input}", numberRaw));

			event.setCancelled(true);
			return;
		}

		final ChatPaginator chatPages = (ChatPaginator) player.getMetadata(nbtPageTag).get(0).value();
		final Map<Integer, List<SimpleComponent>> pages = chatPages.getPages();

		// Remove empty lines
		pages.entrySet().removeIf(entry -> entry.getValue().isEmpty());

		if (!pages.containsKey(page)) {
			final String playerMessage = SimpleLocalization.Pages.NO_PAGE;

			if (Messenger.ENABLED)
				Messenger.error(player, playerMessage);
			else
				Common.tell(player, playerMessage);

			event.setCancelled(true);
			return;
		}

		{ // Send the message body
			for (final SimpleComponent component : chatPages.getHeader())
				component.send(player);

			final List<SimpleComponent> messagesOnPage = pages.get(page);
			int count = 1;

			for (final SimpleComponent comp : messagesOnPage)
				comp.replace("{count}", page + count++).send(player);

			int whiteLines = chatPages.getLinesPerPage();

			if (whiteLines == 15 && pages.size() == 1) {
				if (messagesOnPage.size() < 17)
					whiteLines = 7;
				else
					whiteLines += 2;
			}

			for (int i = messagesOnPage.size(); i < whiteLines; i++)
				SimpleComponent.of("&r").send(player);

			for (final SimpleComponent component : chatPages.getFooter())
				component.send(player);
		}

		// Fill in the pagination line
		if (MinecraftVersion.atLeast(V.v1_7) && pages.size() > 1) {
			Common.tellNoPrefix(player, " ");

			final int pagesDigits = (int) (Math.log10(pages.size()) + 1);
			final int multiply = 23 - (int) MathUtil.ceiling(pagesDigits);

			final SimpleComponent pagination = SimpleComponent.of(chatPages.getThemeColor() + "&m" + Common.duplicate("-", multiply) + "&r");

			if (page == 0)
				pagination.append(" &7« ");
			else
				pagination.append(" &6« ").onHover(SimpleLocalization.Pages.GO_TO_PAGE.replace("{page}", String.valueOf(page))).onClickRunCmd("/#flp " + page);

			pagination.append("&f" + (page + 1)).onHover(SimpleLocalization.Pages.GO_TO_FIRST_PAGE).onClickRunCmd("/#flp 1");
			pagination.append("/");
			pagination.append(pages.size() + "").onHover(SimpleLocalization.Pages.TOOLTIP);

			if (page + 1 >= pages.size())
				pagination.append(" &7» ");
			else
				pagination.append(" &6» ").onHover(SimpleLocalization.Pages.GO_TO_PAGE.replace("{page}", String.valueOf(page + 2))).onClickRunCmd("/#flp " + (page + 2));

			pagination.append(chatPages.getThemeColor() + "&m" + Common.duplicate("-", multiply));

			pagination.send(player);
		}

		// Prevent "Unknown command message"
		event.setCancelled(true);
	}
}
