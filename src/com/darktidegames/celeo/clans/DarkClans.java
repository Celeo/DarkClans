package com.darktidegames.celeo.clans;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.darktidegames.empyrean.C;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import com.google.common.io.Files;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;

/**
 * DarkClans
 * 
 * @author Celeo
 */
public class DarkClans extends JavaPlugin
{

	/*
	 * Variables
	 */

	/** Master list of all clans in memory */
	private List<Clan> clans = new ArrayList<Clan>();

	/*
	 * Connections
	 */

	/** WorldEdit connection */
	public WorldEditPlugin we = null;
	/** WorldGuard connection */
	public WorldGuardPlugin wg = null;
	/** Essentials connection */
	public Essentials ess = null;

	/*
	 * Settings
	 */

	/** List of regions that restricted building does not hurt the player */
	public List<String> noHurtRegions = new ArrayList<String>();

	/*
	 * XP changes
	 */

	public Map<DeathCase, Integer> clanXpChanges = new HashMap<DeathCase, Integer>();

	/*
	 * Games
	 */

	 public String capture_pos = "capture_pos";
	 public String capture_neg = "capture_neg";

	/*
	 * And here we go
	 */

	@Override
	public void onLoad()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		getLogger().info("Loaded");
	}

	@Override
	public void onEnable()
	{
		getCommand("f").setExecutor(this);
		getCommand("clan").setExecutor(this);
		getCommand("clans").setExecutor(this);
		getCommand("dclans").setExecutor(this);
		getCommand("darkclans").setExecutor(this);
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		setupPlugins();
		load();
		// getServer().getScheduler().runTaskLaterAsynchronously(this, new
		// Runnable()
		// {
		// @Override
		// public void run()
		// {
		// auditClans();
		// }
		// }, 600L);
		// getServer().getScheduler().scheduleSyncRepeatingTask(this, new
		// Runnable()
		// {
		// @Override
		// public void run()
		// {
		// World world = getServer().getWorld("world");
		// for (Player online : getServer().getOnlinePlayers())
		// {
		// if (!online.getWorld().equals(world))
		// continue;
		// if (online.getHealth() < 1)
		// continue;
		// if (online.hasPermission("essentials.vanish"))
		// continue;
		// if (isInCaptureRegion(online)
		// && getClanFor(online.getName()) != null)
		// getClanFor(online.getName()).addCapturePoints(1);
		// }
		// ProtectedRegion pos =
		// wg.getRegionManager(world).getRegion(capture_pos
		// + "_floor");
		// if (pos == null)
		// return;
		// ProtectedRegion neg =
		// wg.getRegionManager(world).getRegion(capture_neg
		// + "_floor");
		// if (neg == null)
		// return;
		// String[][] map = new String[10][10];
		// int arkna = 0;
		// int arkna_points = getClan("Arkna").getCapturePoints();
		// int valhath = 0;
		// int valhath_points = getClan("Valhath").getCapturePoints();
		// int kovarn = 0;
		// int kovarn_points = getClan("Kovarn").getCapturePoints();
		// int torma = 0;
		// int torma_points = getClan("Torma").getCapturePoints();
		// int total = arkna_points + valhath_points + kovarn_points
		// + torma_points;
		// if (total < 100)
		// return;
		// arkna = Double.valueOf((double) arkna_points / total *
		// 100).intValue();
		// valhath = Double.valueOf((double) valhath_points / total *
		// 100).intValue();
		// kovarn = Double.valueOf((double) kovarn_points / total *
		// 100).intValue();
		// torma = Double.valueOf((double) torma_points / total *
		// 100).intValue();
		// if (arkna + valhath + torma + kovarn > 100)
		// valhath -= (arkna + valhath + torma + kovarn) - 100;
		// if (arkna + valhath + torma + kovarn < 100)
		// torma += 100 - (arkna + valhath + torma + kovarn);
		// for (int x = 0; x < 10; x++)
		// for (int z = 0; z < 10; z++)
		// {
		// if (arkna > 0)
		// {
		// map[x][z] = "arkna";
		// arkna--;
		// continue;
		// }
		// if (valhath > 0)
		// {
		// map[x][z] = "valhath";
		// valhath--;
		// continue;
		// }
		// if (kovarn > 0)
		// {
		// map[x][z] = "kovarn";
		// kovarn--;
		// continue;
		// }
		// if (torma > 0)
		// {
		// map[x][z] = "torma";
		// torma--;
		// continue;
		// }
		// }
		// int x_offset = pos.getMinimumPoint().getBlockX();
		// int y = pos.getMaximumPoint().getBlockY();
		// int z_offset = pos.getMinimumPoint().getBlockZ();
		// for (int i = 0; i < 10; i++)
		// {
		// for (int j = 0; j < 10; j++)
		// {
		// if (map[i][j].equals("arkna"))
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 11);
		// else if (map[i][j].equals("valhath"))
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 14);
		// else if (map[i][j].equals("kovarn"))
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 4);
		// else if (map[i][j].equals("torma"))
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 13);
		// else
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 10);
		// }
		// }
		// x_offset = neg.getMinimumPoint().getBlockX();
		// y = neg.getMaximumPoint().getBlockY();
		// z_offset = neg.getMinimumPoint().getBlockZ();
		// for (int i = 0; i < 10; i++)
		// {
		// for (int j = 0; j < 10; j++)
		// {
		// if (map[i][j].equals("arkna"))
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 11);
		// else if (map[i][j].equals("valhath"))
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 14);
		// else if (map[i][j].equals("kovarn"))
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 4);
		// else if (map[i][j].equals("torma"))
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 13);
		// else
		// world.getBlockAt(new Location(world, i + x_offset, y, j
		// + z_offset)).setData((byte) 10);
		// }
		// }
		// }
		// }, 60L, 60L);
		checkOnlineStatus();
		getLogger().info("Enabled");
	}

	// public boolean isInCaptureRegion(Player player)
	// {
	// for (ProtectedRegion region :
	// wg.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()))
	// if (region.getId().equals(capture_pos + "_check")
	// || region.getId().equals(capture_neg + "_check"))
	// return true;
	// return false;
	// }

	private void auditClans()
	{
		getLogger().info("Starting audit");
		User user = null;
		UserMap map = ess.getUserMap();
		boolean remove = false;
		try
		{
			File from = new File(getDataFolder(), "config.yml");
			File to = new File(getDataFolder(), "config.preaudit.yml");
			if (to.exists())
			{
				to.delete();
				to.createNewFile();
			}
			Files.copy(from, to);
		}
		catch (Exception e)
		{
			getLogger().warning("Could not make a backup file for the configuration before auditing. "
					+ e.getMessage());
		}
		for (Clan clan : clans)
		{
			for (String name : clan.getAllClanMembers())
			{
				if (name.equals("SERVER"))
					continue;
				remove = false;
				try
				{
					user = map.getUser(name);
					if (user == null)
						remove = true;
					else if (user.getLastLogin() == 0
							|| user.getLastLogout() == 0)
						remove = true;
					else if (user.getLastLogin() > user.getLastLogout())
						remove = false;
					/*
					 * 1000 = 1 second, * 60 = 1 minute, * 60 = 1 hour, * 24 = 1
					 * day, * 14 = 2 weeks
					 */
					// TODO: This is wrong
					else if (user.getLastLogout() - System.currentTimeMillis() > 1000
							* 60 * 60 * 24 * 14)
						remove = false;
					if (remove)
					{
						clan.quit(name);
						getLogger().info("Removed " + name + " from "
								+ clan.getName() + " for inactivity");
					}
				}
				catch (Exception e)
				{
					continue;
				}
			}
		}
		for (Clan clan : clans)
			for (String player : clan.getAllClanMembers())
				clan.updateGroups(player);
		getLogger().info("Audit complete");
	}

	public void removeAllGroups(String player)
	{
		String world = "world";
		CalculableType type = CalculableType.USER;
		for (Clan clan : clans)
		{
			ApiLayer.removeGroup(world, type, player, clan.getName() + "mbr");
			ApiLayer.removeGroup(world, type, player, clan.getName() + "off");
			ApiLayer.removeGroup(world, type, player, clan.getName() + "ldr");
		}
	}

	/**
	 * Connects to the third-party plugins that we need
	 */
	private void setupPlugins()
	{
		Plugin temp = getServer().getPluginManager().getPlugin("WorldEdit");
		we = (WorldEditPlugin) temp;
		temp = getServer().getPluginManager().getPlugin("WorldGuard");
		wg = (WorldGuardPlugin) temp;
		temp = getServer().getPluginManager().getPlugin("Essentials");
		ess = (Essentials) temp;
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		for (Clan clan : clans)
			disableCannons(clan);
		save();
		getLogger().info("Disabled");
	}

	private void updateRemoteDatabase()
	{
		String all = "";
		// String sql =
		// "Update phpbb_profile_fields_data set pf_clan='%s' where pf_minecraftchar='%s'";
		// for (Clan c : clans)
		// for (String name : c.getAllClanMembers())
		// getLogger().info(String.format(sql, c.getName(), name));
		// getLogger().info("=====");
		// for (Clan c : clans)
		// for (String name : c.getAllClanMembers())
		// getLogger().info(String.format("Insert into darkclans values ('%s', '', '%s', '%s')",
		// name, c.getName(), c.getMemberType(name).name().toLowerCase()));
		for (Clan c : clans)
		{
			if (all.equals(""))
				all += c.getName() + ":";
			else
				all += ":" + c.getName() + ":";
			for (String name : c.getAllClanMembers())
			{
				all += getShortHandMemberType(c.getMemberType(name).name().toLowerCase())
						+ "." + name;
			}
		}
		System.out.println(all);
	}

	private String getShortHandMemberType(String full)
	{
		if (full.equals("leader"))
			return "l";
		else if (full.equals("officer"))
			return "o";
		else
			return "m";
	}

	@SuppressWarnings("boxing")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			if (args != null && args[0].equalsIgnoreCase("updatesql"))
			{
				getServer().getScheduler().runTaskAsynchronously(this, new Runnable()
				{
					@Override
					public void run()
					{
						updateRemoteDatabase();
					}
				});
				getLogger().info("Done");
				return true;
			}
			if (args == null || args.length != 3)
			{
				getLogger().info("/clan addxp [clan] [amount]");
				return true;
			}
			String c = args[1];
			int amount = C.i(args[2]);
			Clan clan = getClan(c);
			if (clan == null)
			{
				getLogger().info("No clan with that name found");
				return true;
			}
			clan.addExperience(amount);
			getLogger().info(String.format("%d xp added to %s, bringing their total to %d", amount, c, clan.getExperience()));
			return true;
		}
		Player player = (Player) sender;
		if (args == null || args.length == 0)
		{
			player.sendMessage("§eFor help, read §bhttp://www.darktidegames.com/clans.php");
			return true;
		}
		String param = args[0].toLowerCase();
		String name = player.getName();
		Clan isLeader = getClanIsLeader(name);
		Clan isOfficer = getClanIsOfficer(name);
		Clan isIn = getClanFor(name);
		if (param.equals("join"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9join §aname");
				return true;
			}
			if (isIn != null)
			{
				player.sendMessage("§cYou are already in a clan.");
				return true;
			}
			Clan toJoin = getClan(args[1]);
			if (toJoin == null)
				player.sendMessage("§cCould not find a clan with that name. Try §a/clan list");
			else
				toJoin.tryJoin(player);
			return true;
		}
		if (param.equals("leave") || param.equals("quit"))
		{
			if (isIn == null)
				player.sendMessage("§cYou are not in a clan.");
			else
				isIn.quit(name);
			return true;
		}
		if (param.equals("status"))
		{
			if (isOfficer == null)
				player.sendMessage("§cYou do not have the power to moderate a clan");
			else
				isOfficer.showStatus(player);
			return true;
		}
		if (param.equals("accept"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9accept §aname");
				return true;
			}
			if (isOfficer == null)
				player.sendMessage("§cYou do not have the power to moderate a clan");
			else
			{
				if (isOfficer.isPendingRequest(args[1]))
					isOfficer.accept(player, args[1]);
				else
					player.sendMessage("§a" + args[1]
							+ " §cis not requesting to join your clan");
			}
			return true;
		}
		if (param.equals("decline"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9decline §aname");
				return true;
			}
			if (isOfficer == null)
				player.sendMessage("§cYou do not have the power to moderate a clan");
			else
			{
				if (isOfficer.isPendingRequest(args[1]))
					isOfficer.decline(player, args[1]);
				else
					player.sendMessage("§a" + args[1]
							+ " §cis not requesting to join your clan");
			}
			return true;
		}
		if (param.equals("invite"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9invite §aname");
				return true;
			}
			if (isOfficer == null)
				player.sendMessage("§cYou do not have the power to moderate a clan");
			else
			{
				if (isOfficer.hasMember(args[1]))
					player.sendMessage("§cThat player is already in your clan");
				else
					isOfficer.invite(player, args[1]);
			}
			return true;
		}
		if (param.equals("ally") || param.equals("enemy")
				|| param.equals("neutral"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9ally §aname");
				return true;
			}
			if (isLeader == null)
				player.sendMessage("§cYou are not the leader of a clan");
			else
			{
				Clan other = getClan(args[1]);
				if (other == null)
					player.sendMessage("§cCould not find a clan with that name. Try §a/clan list");
				else if (isLeader.equals(other))
					player.sendMessage("§cYou cannot use that command on your own clan");
				else
					isLeader.setRelationship(player, other, args[0]);
			}
			return true;
		}
		if (param.equals("kick"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9kick §aplayer");
				return true;
			}
			if (isLeader != null)
			{
				if (isLeader.isMember(args[1]) || isLeader.isOfficer(args[1]))
				{
					isLeader.kickByLeader(player, args[1]);
					return true;
				}
				player.sendMessage("§a" + args[1] + " §cis not in your clan");
			}
			else if (isOfficer != null)
			{
				if (isOfficer.isMember(args[1])
						&& isOfficer.isOfficersCanKick())
				{
					isOfficer.kickMember(player, args[1]);
					return true;
				}
				else if (isOfficer.isOfficer(args[1])
						&& isOfficer.isOfficersCanKickOfficers())
				{
					isOfficer.kickOfficer(player, args[1]);
					return true;
				}
				player.sendMessage("§a"
						+ args[1]
						+ " §cis not in your clan, or you do not have the power to kick them");
			}
			else
				player.sendMessage("§cYou do not have the power to moderate a clan");
			return true;
		}
		if (param.equals("promote"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9promote §aplayer");
				return true;
			}
			if (player.hasPermission("clans.bypass"))
				isIn.promote(player, player.getName());
			else if (isLeader == null)
				player.sendMessage("§cYou are not the leader of a clan");
			else
			{
				if (isLeader.hasMember(args[1]))
					isLeader.promote(player, args[1]);
				else
					player.sendMessage("§cThat player is not in your clan");
			}
			return true;
		}
		if (param.equals("demote"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9demote §aplayer");
				return true;
			}
			if (isLeader == null)
				player.sendMessage("§cYou are not the leader of a clan");
			else
			{
				if (isLeader.hasMember(args[1]))
					isLeader.demote(player, args[1]);
				else
					player.sendMessage("§cThat player is not in your clan");
			}
			return true;
		}
		if (param.equals("successor"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9successor §aplayer");
				return true;
			}
			if (isLeader == null)
				player.sendMessage("§cYou are not the leader of a clan");
			else
			{
				if (isLeader.isOfficer(args[1]))
					isLeader.takeOver(player, args[1]);
				else
					player.sendMessage("§cThat player is not a officer in your clan");
			}
			return true;
		}
		if (param.equals("list"))
		{
			player.sendMessage("§6=== Clans active on §2Empyrean Wars §6===");
			for (Clan c : clans)
				player.sendMessage(c.getNameFormattedFor(player.getName())
						+ " §4" + c.getOnlinePlayers().size()
						+ " §eonline of §4" + c.getAllClanMembers().size());
			return true;
		}
		if (param.equals("who"))
		{
			if (args.length != 2)
			{
				player.sendMessage("§6=== Clans active on §2Empyrean Wars §6===");
				for (Clan c : clans)
					player.sendMessage(c.getNameFormattedFor(player.getName())
							+ " §4" + c.getOnlinePlayers().size()
							+ " §eonline of §4" + c.getAllClanMembers().size());
				return true;
			}
			Clan c = getClan(args[1]);
			if (c == null)
			{
				if (getServer().getPlayerExact(args[1]) == null)
				{
					if (getServer().getPlayer(args[1]) == null)
					{
						if (getServer().getOfflinePlayer(args[1]) == null)
							player.sendMessage("§cCould not find clan (or player) with that name");
						else
						{
							String n = getServer().getOfflinePlayer(args[1]).getName();
							player.sendMessage("§4"
									+ n
									+ " §eis "
									+ (getClanFor(n) == null ? "not in a clan" : "in clan §4"
											+ getClanFor(n).getName()));
						}
					}
					else
					{
						String n = getServer().getPlayer(args[1]).getName();
						player.sendMessage("§4"
								+ n
								+ " §eis "
								+ (getClanFor(n) == null ? "not in a clan" : "in clan §4"
										+ getClanFor(n).getName()));
					}
				}
				else
				{
					String n = getServer().getPlayerExact(args[1]).getName();
					player.sendMessage("§4"
							+ n
							+ " §eis "
							+ (getClanFor(n) == null ? "not in a clan" : "in clan §4"
									+ getClanFor(n).getName()));
				}
			}
			else
				c.readOutTo(player);
			return true;
		}
		if (param.equals("title"))
		{
			if (args.length < 2)
			{
				player.sendMessage("§e/clan §9title §a[player in your clan] (new title)");
				return true;
			}
			else if (args.length == 3)
			{
				if (isLeader != null)
					isLeader.setTitle(player, args[1], args[2]);
				else if (isOfficer != null && isOfficer.isOffcersCanAddTitles())
					isOfficer.setTitle(player, args[1], args[2]);
				else
					player.sendMessage("§cYou do not have the power to set titles");
			}
			else
				player.sendMessage("§e/clan §9title §a[player in your clan] (new title)");
			return true;
		}
		if (param.equals("config"))
		{
			if (args.length != 3)
			{
				player.sendMessage("§e/clan §9config §a[key] [true|false]");
				return true;
			}
			if (isLeader == null)
			{
				player.sendMessage("§cYou do not have the power to change clan settings");
				return true;
			}
			String key = args[1];
			boolean value = false;
			if (args[2].equalsIgnoreCase("true"))
				value = true;
			if (key.equalsIgnoreCase("openInvitation"))
				isLeader.setOpenInvitation(value);
			else if (key.equalsIgnoreCase("autoMemberRegionAdd"))
				isLeader.setAutoMemberRegionAdd(value);
			else if (key.equalsIgnoreCase("officersCanKick"))
				isLeader.setOfficersCanKick(value);
			else if (key.equalsIgnoreCase("officersCanKickOfficers"))
				isLeader.setOfficersCanKickOfficers(value);
			else if (key.equalsIgnoreCase("offcersCanAddTitles"))
				isLeader.setOffcersCanAddTitles(value);
			else
			{
				player.sendMessage("§cUnknown key - see the wiki");
				return true;
			}
			player.sendMessage("§eSetting set to "
					+ (value ? "§atrue" : "§afalse"));
			return true;
		}
		if (param.equals("motd"))
		{
			if (isIn == null)
			{
				player.sendMessage("§cYou are not in a clan");
				return true;
			}
			isIn.showMotD(player);
			if (args.length > 2)
			{
				// clan motd [new message ...]
				String message = "";
				for (int i = 1; i < args.length; i++)
				{
					if (message.equals(""))
						message = args[i];
					else
						message += " " + args[i];
				}
				if (isOfficer != null)
					isOfficer.setMotD(player, message);
				else if (isLeader != null)
					isLeader.setMotD(player, message);
				else
					player.sendMessage("§cYou do not have the power to set the motd for your clan");
				return true;
			}
			return true;
		}
		if (param.equals("sr"))
		{
			if (args.length != 3)
			{
				player.sendMessage("§e/clan §9sr §a[add|remove|clear] [name of subregion]");
				return true;
			}
			if (isOfficer == null)
				player.sendMessage("§cYou do not have the power to moderate a clan");
			else
			{
				RegionManager regionManager = wg.getRegionManager(player.getWorld());
				ProtectedRegion region = regionManager.getRegion(args[2]);
				if (region == null)
				{
					player.sendMessage("§cNo such region exists");
					return true;
				}
				if (!region.getOwners().contains(player.getName()))
				{
					player.sendMessage("§cYou cannot use this command on a region you do not own");
					return true;
				}
				if (args[1].equalsIgnoreCase("add"))
				{
					DefaultDomain members = region.getMembers();
					for (String str : isOfficer.getMembers())
					{
						members.addPlayer(str);
					}
					region.setMembers(members);
					player.sendMessage("§eAll clan members added to the member list for the region");
				}
				else if (args[1].equalsIgnoreCase("remove"))
				{
					DefaultDomain members = region.getMembers();
					for (String str : isOfficer.getMembers())
					{
						members.removePlayer(str);
					}
					region.setMembers(members);
					player.sendMessage("§eAll clan members removed from the member list for the region");
				}
				else if (args[1].equalsIgnoreCase("clear"))
				{
					region.setMembers(new DefaultDomain());
					player.sendMessage("§eAll members removed from the member list for the region");
				}
				else
					player.sendMessage("§e/clan §9sr §a[add|remove|clear] [name of subregion]");
				try
				{
					regionManager.save();
				}
				catch (ProtectionDatabaseException e)
				{
					e.printStackTrace();
				}
			}
			return true;
		}
		if (param.equals("-create"))
		{
			if (!hasPerms(player, "clans.admin"))
				return true;
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9-create §aname");
				return true;
			}
			Clan test = getClan(args[1]);
			if (test != null)
			{
				player.sendMessage("§cA clan with that name already exists");
				return true;
			}
			Clan c = new Clan(this, args[1]);
			c.setLeader("SERVER");
			clans.add(c);
			player.sendMessage("§aClan created!");
			return true;
		}
		if (param.equals("-audit"))
		{
			if (!hasPerms(player, "clans.admin"))
				return true;
			auditClans();
			player.sendMessage("§aDone");
			return true;
		}
		if (param.equals("-checknew"))
		{
			if (!hasPerms(player, "clans.admin"))
				return true;
			if (args.length != 2)
			{
				player.sendMessage("§e/clan §9-checknew §awho");
				return true;
			}
			player.sendMessage("§a" + args[1] + " §eis "
					+ (isNewPlayer(args[1]) ? "" : "§anot ") + "§ea new player");
			return true;
		}
		if (param.equals("-save"))
		{
			if (!player.hasPermission("clans.admin"))
				return true;
			save();
			player.sendMessage("§aData saved to disk");
			return true;
		}
		if (param.equals("-reload"))
		{
			if (!player.hasPermission("clans.admin"))
				return true;
			load();
			player.sendMessage("§aIn-game memory replaced with data from disk");
			return true;
		}
		if (param.equals("-set"))
		{
			if (!player.hasPermission("clans.admin"))
				return true;
			if (args.length != 4)
			{
				player.sendMessage("§e/clan §9-set §aplayer clan position");
				return true;
			}
			String p = args[1];
			String c = args[2];
			String t = args[3];
			if (getClan(c) == null)
			{
				player.sendMessage("§cCannot find a clan with that name");
				return true;
			}
			if (!t.equals("member") && !t.equals("officer")
					&& !t.equals("leader"))
			{
				player.sendMessage("§cValid positions: member, officer, leader");
				return true;
			}
			getClan(c).put(p, t);
			player.sendMessage("§aDone");
			return true;
		}
		if (param.equals("-remove"))
		{
			if (!player.hasPermission("clans.admin"))
				return true;
			if (args.length != 4)
			{
				player.sendMessage("§e/clan §9-remove §aplayer clan position");
				return true;
			}
			String p = args[1];
			String c = args[2];
			String t = args[3];
			if (getClan(c) == null)
			{
				player.sendMessage("§cCannot find a clan with that name");
				return true;
			}
			getClan(c).remove(p, t);
			player.sendMessage("§aDone");
			return true;
		}
		if (param.equals("xp"))
		{
			if (!player.hasPermission("clans.admin"))
				return true;
			if (args.length != 4)
			{
				player.sendMessage("§e/clan §9xp §a[add|remove|set] clan amount");
				return true;
			}
			String c = args[2];
			int amount = C.i(args[3]);
			Clan clan = getClan(c);
			if (clan == null)
			{
				getLogger().info("No clan with that name found");
				return true;
			}
			if (args[1].equalsIgnoreCase("add"))
				clan.addExperience(amount);
			else if (args[1].equalsIgnoreCase("remove"))
				clan.addExperience(-amount);
			else if (args[1].equalsIgnoreCase("set"))
				clan.setExperience(amount);
			else
			{
				player.sendMessage("§e/clan §9xp §a[add|remove|set] clan amount");
				return true;
			}
			player.sendMessage(String.format("§eTotal for §a%s §eis now §a%d", c, clan.getExperience()));
			return true;
		}
		if (param.equals("-checkonlinestatus"))
		{
			if (!player.hasPermission("clans.admin"))
				return true;
			checkOnlineStatus();
			player.sendMessage("§aDone");
			return true;
		}
		player.sendMessage("§eFor help, read §bhttp://www.darktidegames.com/clans.php");
		return true;
	}

	private Clan getClan(String name)
	{
		for (Clan c : clans)
			if (c.getName().equals(name))
				return c;
		for (Clan c : clans)
			if (c.getName().toLowerCase().startsWith(name.toLowerCase())
					|| c.getName().toLowerCase().contains(name.toLowerCase()))
				return c;
		return null;
	}

	private static boolean hasPerms(Player player, String node)
	{
		if (!player.hasPermission(node))
		{
			player.sendMessage("§cYou cannot use this command");
			return false;
		}
		return true;
	}

	public boolean isInClan(String name)
	{
		return getClanFor(name) != null;
	}

	public Clan getClanFor(String name)
	{
		for (Clan c : clans)
			if (c.hasMember(name))
				return c;
		return null;
	}

	/**
	 * 
	 * @param name
	 *            String
	 * @return Clan the player is an officer in, null if not
	 */
	public Clan getClanIsOfficer(String name)
	{
		for (Clan c : clans)
			if (c.isOfficer(name) || c.isLeader(name))
				return c;
		return null;
	}

	/**
	 * 
	 * @param name
	 *            String
	 * @return Clan the player is the leader in, null if not
	 */
	public Clan getClanIsLeader(String name)
	{
		for (Clan c : clans)
			if (c.isLeader(name))
				return c;
		return null;
	}

	/**
	 * Saves all data to the configuration file
	 */
	@SuppressWarnings("boxing")
	private void save()
	{
		List<String> all = new ArrayList<String>();
		String p = null;
		for (Clan c : clans)
		{
			getLogger().info("Saving clan '" + c.getName()
					+ "' to configuration");
			all.add(c.getName());
			p = "clans." + c.getName() + ".";
			getConfig().set(p + "leader", c.getLeaderForDisk());
			getConfig().set(p + "officers", c.getOfficersForDisk());
			getConfig().set(p + "members", c.getMembersForDisk());
			getConfig().set(p + "requests", c.getRequests());
			getConfig().set(p + "invites", c.getInvites());
			getConfig().set(p + "allies", c.getAllies());
			getConfig().set(p + "enemies", c.getEnemies());
			getConfig().get(p + "neutrals", c.getNeutrals());
			getConfig().set(p + "experience", c.getExperience());
			getConfig().set(p + "settings.openInvitation", c.isOpenInvitation());
			getConfig().set(p + "settings.autoMemberRegionAdd", c.isAutoMemberRegionAdd());
			getConfig().set(p + "settings.officersCanKick", c.isOfficersCanKick());
			getConfig().set(p + "settings.officersCanKickOfficers", c.isOfficersCanKickOfficers());
			getConfig().set(p + "settings.offcersCanAddTitles", c.isOffcersCanAddTitles());
			getConfig().set(p + "settings.capturePoints", c.getCapturePoints());
			getConfig().set(p + "settings.motd", c.getMotD().replace("§", "&"));
		}
		getConfig().set("allClans", all);
		getConfig().set("settings.noHurtRegions", noHurtRegions);
		getConfig().set("games.capture_pos", capture_pos);
		getConfig().set("games.capture_neg", capture_neg);
		saveConfig();
		getLogger().info("Saved all data to configuration");
	}

	/**
	 * Loads all data from the configuration file
	 */
	private void load()
	{
		getLogger().info("Loading from configuration");
		reloadConfig();
		capture_pos = getConfig().getString("games.capture_pos", "capture_pos");
		capture_neg = getConfig().getString("games.capture_neg", "capture_neg");
		clans.clear();
		String p = null;
		for (String name : getConfig().getStringList("allClans"))
		{
			Clan c = new Clan(this, name);
			getLogger().info("Loading clan '" + name + "' from configuration");
			p = "clans." + name + ".";
			c.setLeaderFromDisk(getConfig().getString(p + "leader"));
			c.setOfficersFromDisk(getConfig().getStringList(p + "officers"));
			c.setMembersFromDisk(getConfig().getStringList(p + "members"));
			c.setRequests(getConfig().getStringList(p + "requests"));
			c.setInvites(getConfig().getStringList(p + "invites"));
			c.setAllies(getConfig().getStringList(p + "allies"));
			c.setEnemies(getConfig().getStringList(p + "enemies"));
			c.setNeutrals(getConfig().getStringList(p + "neutrals"));
			c.setExperience(getConfig().getInt(p + "experience"));
			c.setOpenInvitation(getConfig().getBoolean(p
					+ "settings.openInvitation", false));
			c.setAutoMemberRegionAdd(getConfig().getBoolean(p
					+ "settings.autoMemberRegionAdd", false));
			c.setOfficersCanKick(getConfig().getBoolean(p
					+ "settings.officersCanKick", true));
			c.setOfficersCanKickOfficers(getConfig().getBoolean(p
					+ "settings.officersCanKickOfficers", true));
			c.setOffcersCanAddTitles(getConfig().getBoolean(p
					+ "settings.offcersCanAddTitles", false));
			c.setCapturePoints(getConfig().getInt(p + "settings.capturePoints", 0));
			c.setMotD(null, getConfig().getString(p + "settings.motd"));
			clans.add(c);
		}
		noHurtRegions = getConfig().getStringList("settings.noHurtRegions");
		if (noHurtRegions == null)
			noHurtRegions = new ArrayList<String>();
		clanXpChanges.put(new DeathCase(true, true), Integer.valueOf(getConfig().getInt("settings.transfers.fightBack.attacking")));
		clanXpChanges.put(new DeathCase(false, true), Integer.valueOf(getConfig().getInt("settings.transfers.flee.attacking")));
		clanXpChanges.put(new DeathCase(true, false), Integer.valueOf(getConfig().getInt("settings.transfers.fightBack.defending")));
		clanXpChanges.put(new DeathCase(false, false), Integer.valueOf(getConfig().getInt("settings.transfers.flee.defending")));
		getLogger().info("Loaded!");
	}

	private int getDeathCase(boolean foughtBack, boolean attacking)
	{
		DeathCase deathCase = new DeathCase(foughtBack, attacking);
		for (DeathCase dc : clanXpChanges.keySet())
			if (dc.equals(deathCase))
				return clanXpChanges.get(dc).intValue();
		return 0;
	}

	/**
	 * TODO: This is jambled
	 * 
	 * @param name
	 * @return
	 */
	public boolean isNewPlayer(String name)
	{
		Player player = getServer().getPlayerExact(name);
		if (player != null && player.isOnline())
			if (System.currentTimeMillis() - player.getFirstPlayed() > 1000
					* 60 * 60 * 24 * 4)
				return false;
		OfflinePlayer offline = getServer().getOfflinePlayer(name);
		if (offline != null)
			if (System.currentTimeMillis() - offline.getFirstPlayed() > 1000
					* 60 * 60 * 24 * 4)
				return false;
		return true;
	}

	/**
	 * Enables explosions from cannons in all WorldGuard regions by this clan
	 * 
	 * @param clan
	 *            Clan
	 */
	public void enableCannons(Clan clan)
	{
		for (ProtectedRegion region : getClanRegions(clan))
		{
			try
			{
				region.setFlag(DefaultFlag.OTHER_EXPLOSION, DefaultFlag.OTHER_EXPLOSION.parseInput(wg, getServer().getConsoleSender(), "allow"));
				region.setFlag(DefaultFlag.TNT, DefaultFlag.TNT.parseInput(wg, getServer().getConsoleSender(), "allow"));
			}
			catch (Exception e)
			{
				getLogger().warning("Could not enable the cannon flags for "
						+ clan.getName());
			}
		}
	}

	/**
	 * Disabled explosions from cannons in all WorldGuard regions by this clan
	 * 
	 * @param clan
	 *            Clan
	 */
	public void disableCannons(Clan clan)
	{
		for (ProtectedRegion region : getClanRegions(clan))
		{
			try
			{
				region.setFlag(DefaultFlag.OTHER_EXPLOSION, DefaultFlag.OTHER_EXPLOSION.parseInput(wg, getServer().getConsoleSender(), "deny"));
				region.setFlag(DefaultFlag.TNT, DefaultFlag.TNT.parseInput(wg, getServer().getConsoleSender(), "deny"));
			}
			catch (Exception e)
			{
				getLogger().warning("Could not disable the cannon flags for "
						+ clan.getName());
			}
		}
	}

	public void checkOnlineStatus()
	{
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				for (Clan clan : clans)
				{
					if (!clan.hasOnlineMembers() && clan.cannonsEnabled)
						disableCannons(clan);
					else if (clan.hasOnlineMembers() && !clan.cannonsEnabled)
						enableCannons(clan);
				}
			}
		}, 100L);
	}

	/**
	 * Returns a list of all ProtectedRegions owned by the clan
	 * 
	 * @param clan
	 *            Clan
	 * @return List of String objects
	 */
	public List<ProtectedRegion> getClanRegions(Clan clan)
	{
		List<ProtectedRegion> ret = new ArrayList<ProtectedRegion>();
		for (Entry<String, ProtectedRegion> e : wg.getRegionManager(getServer().getWorld("world")).getRegions().entrySet())
			if (e.getKey().startsWith(clan.getName().toLowerCase()))
				ret.add(e.getValue());
		return ret;
	}

	/**
	 * Tries to send a message to a player that may or may not be online
	 * 
	 * @param playerName
	 *            String
	 * @param message
	 *            String
	 * @return True if the message was delivered successfully
	 */
	public boolean sendMessage(String playerName, String message)
	{
		Player temp = getServer().getPlayer(playerName);
		if (temp == null || !temp.isOnline())
			return false;
		temp.sendMessage(message);
		return true;
	}

	/**
	 * Returns a list of online players that are in the clan
	 * 
	 * @param clan
	 *            String
	 * @return List of Player objects
	 */
	public List<Player> getOnlinePlayersInClan(String clan)
	{
		Clan c = getClan(clan);
		if (c == null)
			return new ArrayList<Player>();
		return c.getOnlinePlayers();
	}

	/**
	 * Returns a list of online officers (and the leader, if online) that are in
	 * the clan
	 * 
	 * @param clan
	 *            String
	 * @return List of Player objects
	 */
	public List<Player> getOnlineOfficersInClan(String clan)
	{
		Clan c = getClan(clan);
		if (c == null)
			return new ArrayList<Player>();
		return c.getOnlineOfficers();
	}

	/**
	 * 
	 * @param player
	 *            String
	 * @return Clan name that the player is in, null if not in a clan
	 */
	public String getPlayerFaction(String player)
	{
		Clan c = getClanFor(player);
		if (c == null)
			return "";
		return c.getName();
	}

	/**
	 * @param player
	 *            String
	 * @param onlooker
	 *            String
	 * @return Name formatted with color and position tag as if the onlooker was
	 *         seeing the player in chat
	 */
	public String getRelationTag(String player, String onlooker)
	{
		Clan speaker = getClanFor(player);
		if (speaker == null)
			return "";
		Clan looker = getClanFor(onlooker);
		if (looker == null)
			return speaker.getName() + speaker.getPositionTag(player) + "§f";
		if (speaker.equals(looker))
			return "§a" + speaker.getName() + speaker.getPositionTag(player)
					+ "§f";
		switch (looker.getRelationshipFor(speaker))
		{
		case ALLY:
			return "§5" + speaker.getName() + speaker.getPositionTag(player)
					+ "§f";
		case ENEMY:
			return "§c" + speaker.getName() + speaker.getPositionTag(player)
					+ "§f";
		case NEUTRAL:
			return "§e" + speaker.getName() + speaker.getPositionTag(player)
					+ "§f";
		}
		return speaker.getName() + speaker.getPositionTag(player) + "§f";
	}

	public String getFactionTag(String player)
	{
		Clan clan = getClanFor(player);
		if (clan == null)
			return "";
		return clan.getPositionTag(player);
	}

	/**
	 * 
	 * @param one
	 *            String
	 * @param two
	 *            String
	 * @return True if both names are in the same clan
	 */
	@SuppressWarnings("null")
	public boolean sameClan(String one, String two)
	{
		Clan c_one = getClanFor(one);
		Clan c_two = getClanFor(two);
		if (c_one == null && c_two == null)
			return true;
		if (c_one == null && c_two != null)
			return false;
		if (c_one != null && c_two == null)
			return false;
		if (c_one.equals(c_two))
			return true;
		return (c_one.getName().equals(c_two.getName()));
	}

	/**
	 * 
	 * @param player
	 *            Player
	 * @return True if the player is standing in a region owned by his/her clan
	 */
	public boolean isInClanLand(Player player)
	{
		return getClanPlayerIsIn(player) != null;
	}

	public String getTitleFor(Player player)
	{
		return getTitleFor(player.getName());
	}

	public String getTitleFor(String playerName)
	{
		Clan clan = getClanFor(playerName);
		if (clan != null)
			return clan.getTitleFor(playerName);
		return "";
	}

	public Clan getClanForLocation(Location location)
	{
		List<String> regions = wg.getRegionManager(location.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(location));
		if (regions == null || regions.isEmpty())
			return null;
		for (String region : regions)
			for (Clan c : clans)
				if (region.toLowerCase().startsWith(c.getName().toLowerCase()))
					return c;
		return null;
	}

	public Clan getClanPlayerIsIn(Player player)
	{
		return getClanForLocation(player.getLocation());
	}

	/**
	 * 
	 * @param playerName
	 *            String
	 * @return True if the player with that name is online
	 */
	public boolean isOnline(String playerName)
	{
		Player temp = getServer().getPlayerExact(playerName);
		return temp != null && temp.isOnline();
	}

	/**
	 * Add, subtract, and transfer xp to and from clans for pvp
	 * 
	 * @param killed
	 *            Player
	 * @param killer
	 *            Entity
	 * @param attacking
	 *            boolean
	 * @param foughtBack
	 *            boolean
	 */
	public void logKill(Player killed, Entity killer, boolean foughtBack, boolean attacking)
	{
		if (clanXpChanges == null || clanXpChanges.isEmpty())
			throw new NullPointerException(String.format("Clan xp change map is empty! Data: '%s killed %s, foughtBack: %b, was attacking: %b'", killed.getName(), killer.toString().toLowerCase(), Boolean.valueOf(foughtBack), Boolean.valueOf(attacking)));
		int amount = getDeathCase(foughtBack, attacking);
		boolean transfer = false;
		if (!foughtBack)
			transfer = true;
		Clan clan_killed = getClanFor(killed.getName());
		if (clan_killed == null)
			return;
		Player pKiller = null;
		if (killer instanceof Player)
			pKiller = (Player) killer;
		Clan clan_killer = pKiller == null ? null : getClanFor(pKiller.getName());
		if (transfer)
		{
			clan_killed.addExperience(-amount);
			if (clan_killer != null)
				clan_killer.addExperience(amount);
		}
		else if (clan_killer != null)
			clan_killer.addExperience(amount);
	}

	public boolean areAllies(Player one, Player two)
	{
		return getRelationTag(one.getName(), two.getName()).startsWith("§5");
	}

	public void showMotD(Player player)
	{
		Clan clan = getClanFor(player.getName());
		if (clan == null)
			return;
		clan.showMotD(player);
	}

}