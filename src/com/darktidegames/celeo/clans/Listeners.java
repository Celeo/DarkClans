package com.darktidegames.celeo.clans;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.darktidegames.celeo.clans.Clan.MemberType;
import com.darktidegames.empyrean.C;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Listeners
 * 
 * @author Celeo
 */
public class Listeners implements Listener
{

	final DarkClans plugin;

	public Listeners(DarkClans plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * If the player logging out is the last online member of their clan,
	 * disable cannons in their land
	 * 
	 * @param event
	 *            PlayerQuitEvent
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		plugin.checkOnlineStatus();
	}

	/**
	 * If the player logging in is the first online member of their clan, enable
	 * cannons in their land cannons
	 * 
	 * @param event
	 *            PlayerJoinEvent
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		plugin.checkOnlineStatus();
		plugin.showMotD(event.getPlayer());
	}

	/**
	 * Send the player a message if they are attaking a clanmate
	 * 
	 * @param event
	 *            EntityDamageEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onClanMateAttackAnother(EntityDamageEvent event)
	{
		if (event.isCancelled())
			return;
		if (!(event.getEntity() instanceof Player))
			return;
		if (!(event instanceof EntityDamageByEntityEvent))
			return;
		EntityDamageByEntityEvent eve = (EntityDamageByEntityEvent) event;
		if (!(eve.getDamager() instanceof Player))
			return;
		Player hurt = (Player) event.getEntity();
		Player damager = (Player) eve.getDamager();
		if (plugin.sameClan(hurt.getName(), damager.getName()))
			damager.sendMessage("§a" + hurt.getName() + " is in your clan");
		if (plugin.areAllies(hurt, damager))
			damager.sendMessage("§a" + hurt.getName()
					+ " is allied to your clan");
	}

	/**
	 * 20% reduced incoming damage for players in their clan's land
	 * 
	 * @param event
	 *            EntityDamageEvent
	 */
	@EventHandler
	public void onClanTakeDamageInLand(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		if (!(event instanceof EntityDamageByEntityEvent))
			return;
		EntityDamageByEntityEvent eve = (EntityDamageByEntityEvent) event;
		if (!(eve.getDamager() instanceof Player))
			return;
		Player damaged = (Player) event.getEntity();
		if (!plugin.isInClanLand(damaged))
			return;
		event.setDamage((int) (event.getDamage() * 0.8));
	}

	@EventHandler
	public void onPlayerTryPlace(BlockPlaceEvent event)
	{
		tryBuild(event.getPlayer(), event.getBlockAgainst());
	}

	@EventHandler
	public void onPlayerTryBreak(BlockBreakEvent event)
	{
		tryBuild(event.getPlayer(), event.getBlock());
	}

	/**
	 * Damages the player if they cannot build in that region<br>
	 * <br>
	 * Does not damage in the empyrean1 region nor in any "no hurt" regions
	 * 
	 * @param player
	 *            Player
	 * @param block
	 *            Block
	 */
	private void tryBuild(Player player, Block block)
	{
		if (!plugin.wg.canBuild(player, block))
		{
			List<String> regions = plugin.wg.getRegionManager(block.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(block));
			if (!regions.contains("empyrean1")
					&& !C.containsAny(regions, plugin.noHurtRegions))
			{
				player.damage(1);
			}
		}
	}

	/**
	 * Do not allow owners of clan land to remove their leader
	 * 
	 * @param event
	 *            PlayerCommandPreprocessEvent
	 */
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		Clan clan = plugin.getClanFor(player.getName());
		if (clan == null)
			return;
		MemberType position = clan.getMemberType(player.getName());
		if (position == null)
			return;
		if (!event.getMessage().startsWith("/rg removeowner")
				&& !event.getMessage().startsWith("/region removeowner"))
			return;
		if (event.getMessage().split(" ").length != 4)
			return;
		String cmd = event.getMessage().replace("/region ", "").replace("/rg ", "");
		if (cmd.equalsIgnoreCase("removeowner" + clan.getName() + "1"))
			if (position.equals(MemberType.LEADER))
				return;
		ProtectedRegion region = plugin.wg.getRegionManager(player.getWorld()).getRegion(cmd.split(" ")[1]);
		if (region == null)
			return;
		Location loc = new Location(player.getWorld(), region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ());
		String leader = event.getMessage().split(" ")[3];
		Clan inside = plugin.getClanForLocation(loc);
		if (inside == null)
			return;
		if (inside.getName().equals(clan.getName())
				&& inside.getLeader().equalsIgnoreCase(leader))
		{
			event.setCancelled(true);
			player.sendMessage("§cYou cannot remove your leader from that region.");
		}
	}

}