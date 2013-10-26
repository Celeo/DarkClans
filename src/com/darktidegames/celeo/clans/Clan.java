package com.darktidegames.celeo.clans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.darktidegames.empyrean.C;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;

/**
 * Clan object
 * 
 * @author Celeo
 */
public class Clan
{

	/*
	 * Data
	 */

	/** Instance of DarkClans plugin for server/method access */
	private final DarkClans plugin;
	/** Name of the clan */
	private String name;
	/** The clan's leader */
	private String leader;
	/** The clan's officers */
	private List<String> officers;
	/** The clan's normal members */
	private List<String> members;
	/** Requests by players to join the clan */
	private List<String> requests;
	/** Invites by officers to others to join the clan */
	private List<String> invites;
	/** Clans that are allies of this clan */
	private List<String> allies;
	/** Clans that are enemies of this clan */
	private List<String> enemies;
	/** Clans that are neutral to this clan */
	private List<String> neutrals;
	/** Titles for members */
	private Map<String, String> titles = new HashMap<String, String>();
	/** Clan experience points */
	private int experience;
	/** Message of the Day */
	private String motd;
	/** Runtime variable for keeping track of cannons */
	public boolean cannonsEnabled = false;

	/*
	 * Settings
	 */

	private boolean openInvitation = false;
	private boolean autoMemberRegionAdd = false;
	private boolean officersCanKick = true;
	private boolean officersCanKickOfficers = false;
	private boolean offcersCanAddTitles = false;

	/*
	 * Games
	 */

	private int capturePoints = 0;

	/**
	 * Basic constructor
	 * 
	 * @param plugin
	 *            DarkClans
	 * @param name
	 *            String
	 */
	public Clan(DarkClans plugin, String name)
	{
		this.plugin = plugin;
		this.name = name;
		leader = "-server";
		officers = new ArrayList<String>();
		members = new ArrayList<String>();
		requests = new ArrayList<String>();
		invites = new ArrayList<String>();
		allies = new ArrayList<String>();
		enemies = new ArrayList<String>();
		neutrals = new ArrayList<String>();
		experience = 0;
	}

	/**
	 * Adds a player as a basic member to the clan<br>
	 * <br>
	 * Removes them from the request and invite lists if they are present
	 * 
	 * @param player
	 *            String
	 */
	public void addMember(String player)
	{
		requests.remove(player);
		invites.remove(player);
		moveMember(player, MemberType.MEMBER);
		if (autoMemberRegionAdd)
			for (ProtectedRegion region : plugin.getClanRegions(this))
			{
				if (region.getId().equals(name + "1"))
				{
					DefaultDomain domain = region.getMembers();
					domain.addPlayer(player);
					region.setMembers(domain);
				}
			}
	}

	/**
	 * Moves the member into the designated list
	 * 
	 * @param player
	 *            String
	 * @param type
	 *            Clan.MemberType
	 */
	public void moveMember(String player, MemberType type)
	{
		switch (type)
		{
		case LEADER:
			if (!isLeader(player))
			{
				leader = player;
				if (isOfficer(player))
					officers.remove(player);
				if (isMember(player))
					members.remove(player);
			}
			break;
		case OFFICER:
			if (!isOfficer(player))
			{
				officers.add(player);
				if (isMember(player))
					members.remove(player);
			}
			break;
		case MEMBER:
			if (!isMember(player))
				members.add(player);
			break;
		}
		updateGroups(player);
		broadcast("§a" + player + " §eis now in the position §a"
				+ getMemberType(player).name().toLowerCase());
	}

	/**
	 * Removes the player from the clan, and organizes the remaining members as
	 * needed
	 * 
	 * @param player
	 *            String
	 */
	public void quit(String player)
	{
		broadcast("§a" + player + " §chas left the clan");
		switch (getMemberType(player))
		{
		case LEADER:
			if (officers.size() != 0)
			{
				String newLeader = officers.get(0);
				officers.remove(newLeader);
				leader = newLeader;
				broadcast("§b" + newLeader + " §6is now the leader of the clan");
			}
			else
			{
				broadcast(player
						+ " is inactive but there are no mods to replace the leader");
				leader = "SERVER";
			}
			break;
		case OFFICER:
			officers.remove(player);
			break;
		case MEMBER:
			members.remove(player);
			break;
		}
		removeFromClanLand(player);
		updateGroups(player);
	}

	/**
	 * Raw
	 * 
	 * @param name
	 *            String
	 * @param position
	 *            String
	 */
	public void remove(String name, String position)
	{
		MemberType rem = null;
		for (MemberType type : MemberType.values())
		{
			if (type.name().equalsIgnoreCase(position))
				rem = type;
		}
		if (rem == null)
			return;
		switch (rem)
		{
		default:
		case MEMBER:
			members.remove(name);
			break;
		case OFFICER:
			officers.remove(name);
			break;
		case LEADER:
			if (leader.equalsIgnoreCase(name))
				leader = "SERVER";
			break;
		}
	}

	/**
	 * Accepts the player into the clan as a regular member
	 * 
	 * @param officer
	 *            Player
	 * @param toInvite
	 *            String
	 */
	public void accept(Player officer, String toInvite)
	{
		broadcast("§b" + officer.getName() + " §ehas invited §a" + toInvite
				+ " §einto your clan");
		addMember(toInvite);
		requests.remove(toInvite);
		plugin.sendMessage(toInvite, "§aYour application to " + name
				+ " was accepted!");
	}

	/**
	 * Declines the player's request to join this clan
	 * 
	 * @param officer
	 *            Player
	 * @param toDecline
	 *            String
	 */
	public void decline(Player officer, String toDecline)
	{
		broadcast("§b" + officer.getName() + " §ehas declined §a" + toDecline
				+ "'s §erequest to join your clan");
		requests.remove(toDecline);
		plugin.sendMessage(toDecline, "§7Your application to " + name
				+ " was declined");
	}

	/**
	 * Invites a player to join this clan
	 * 
	 * @param officer
	 *            Player
	 * @param toInvite
	 *            String
	 */
	public void invite(Player officer, String toInvite)
	{
		if (requests.contains(toInvite))
		{
			addMember(toInvite);
			return;
		}
		if (invites.contains(toInvite))
		{
			invites.remove(toInvite);
			officer.sendMessage("§eYou have retracted an invite from §a"
					+ toInvite);
			return;
		}
		invites.add(toInvite);
		plugin.sendMessage(toInvite, "§eYou have been invited to join §a"
				+ name + " §eby §a" + officer.getName());
		broadcast("§a" + officer.getName() + " §ehas invited §a" + toInvite
				+ " §eto join your clan");
	}

	/**
	 * Called when a player requests to join this clan. Checks to see if they
	 * have an outstanding invite. If they do, they join the clan. If not, they
	 * are added to the request list.
	 * 
	 * @param player
	 *            String
	 */
	public void tryJoin(Player player)
	{
		String playerName = player.getName();
		if (!player.hasPermission("darkclans.joinclan"))
		{
			player.sendMessage("§7You must be greylisted before joining a clan - §a/help greylist");
			return;
		}
		if (hasMember(playerName))
		{
			player.sendMessage("§cYou are already a member of this clan, you cannot rejoin");
			return;
		}
		if (invites.contains(playerName))
			addMember(playerName);
		else if (player.hasPermission("clans.bypass"))
			addMember(playerName);
		else if (openInvitation)
			addMember(playerName);
		else
		{
			requests.add(playerName);
			broadcastOfficers("§eNew join request from §a" + playerName);
			player.sendMessage("§eYou now have an request to join pending for §a"
					+ name);
		}
	}

	/**
	 * Kicks the member from the clan
	 * 
	 * @param officer
	 *            Player
	 * @param toKick
	 *            String
	 */
	public void kickMember(Player officer, String toKick)
	{
		broadcast("§b" + officer.getName() + " §ehas kicked §a" + toKick
				+ " §efrom your clan");
		members.remove(toKick);
		plugin.sendMessage(toKick, "§cYou were kicked from §e" + name
				+ " §cby §e" + officer.getName());
		removeFromClanLand(toKick);
	}

	/**
	 * Kicks an Officer.<br>
	 * Used when officersCanKickOfficers is set to true.<br>
	 * <br>
	 * <i>Note: Requires pre-check of that boolean!</i>
	 * 
	 * @param kicker
	 *            Player
	 * @param toKick
	 *            String
	 */
	public void kickOfficer(Player kicker, String toKick)
	{
		broadcast("§b" + kicker.getName() + " §ehas kicked §a" + toKick
				+ " §efrom your clan");
		officers.remove(toKick);
		plugin.sendMessage(toKick, "§cYou were kicked from §e" + name
				+ " §cby §e" + kicker.getName());
		removeFromClanLand(toKick);
	}

	/**
	 * Kicks the member or officer from the clan
	 * 
	 * @param leader
	 *            Player
	 * @param toKick
	 *            String
	 */
	public void kickByLeader(Player leader, String toKick)
	{
		broadcast("§b" + leader.getName() + " §ehas kicked §a" + toKick
				+ " §efrom your clan");
		if (isMember(toKick))
			members.remove(toKick);
		else if (isOfficer(toKick))
			officers.remove(toKick);
		plugin.sendMessage(toKick, "§cYou were kicked from §e" + name
				+ " §cby §e" + leader.getName());
		removeFromClanLand(toKick);
	}

	public void removeFromClanLand(String player)
	{
		List<ProtectedRegion> regions = plugin.getClanRegions(this);
		for (ProtectedRegion region : regions)
		{
			if (region.isMember(player))
				region.getMembers().removePlayer(player);
			if (region.isOwner(player))
				region.getOwners().removePlayer(player);
		}
	}

	/**
	 * Promotional tracks
	 * 
	 * @param leader
	 *            Player
	 * @param toPromote
	 *            String
	 */
	public void promote(Player leader, String toPromote)
	{
		switch (getMemberType(toPromote))
		{
		case LEADER:
			leader.sendMessage("§cYou cannot promote yourself, nor is there a position after leader");
			break;
		case OFFICER:
			leader.sendMessage("§cThe command to promote a officer to take your position is §a/clan successor [name]");
			break;
		case MEMBER:
			members.remove(toPromote);
			officers.add(toPromote);
			plugin.sendMessage(toPromote, "§eYou are now an officer of §a"
					+ name);
			broadcast("§a" + leader.getName() + " §ehas made §a" + toPromote
					+ "§e a officer");
			updateGroups(toPromote);
			break;
		}
	}

	/**
	 * Demotional tracks
	 * 
	 * @param leader
	 *            Player
	 * @param toDemote
	 *            String
	 */
	public void demote(Player leader, String toDemote)
	{
		switch (getMemberType(toDemote))
		{
		case LEADER:
			leader.sendMessage("§cYou cannot demote yourself from leader. Look into §a/clan successor [name]");
			break;
		case OFFICER:
			officers.remove(toDemote);
			members.add(toDemote);
			plugin.sendMessage(toDemote, "§aYou are now a regular member of §e"
					+ name);
			broadcast("§a" + leader.getName() + " §ehas made §a" + toDemote
					+ " §ea regular member");
			updateGroups(toDemote);
			break;
		case MEMBER:
			leader.sendMessage("§cYou cannot demote a regular member. Look into §a/clan kick [name]");
			break;
		}
	}

	/**
	 * Switches the leader with that officer
	 * 
	 * @param retiring
	 *            Player
	 * @param officer
	 *            String
	 */
	public void takeOver(Player retiring, String officer)
	{
		this.leader = officer;
		officers.remove(officer);
		officers.add(retiring.getName());
		broadcast("§a" + retiring.getName() + " §ehas given §a" + officer
				+ " §eleadership of the clan");
	}

	/**
	 * Sends the message to all online clan members
	 * 
	 * @param message
	 *            String
	 */
	public void broadcast(String message)
	{
		for (Player p : getOnlinePlayers())
			p.sendMessage(message);
	}

	/**
	 * Sends the message to all online clan officers
	 * 
	 * @param message
	 *            String
	 */
	public void broadcastOfficers(String message)
	{
		for (Player p : getOnlineOfficers())
			p.sendMessage(message);
	}

	/**
	 * 
	 * @return True if there is at least 1 clan member online
	 */
	public boolean hasOnlineMembers()
	{
		return !getOnlinePlayers().isEmpty();
	}

	/**
	 * Returns a list of all clan members that are online
	 * 
	 * @return List of Player objects
	 */
	public List<Player> getOnlinePlayers()
	{
		List<Player> ret = new ArrayList<Player>();
		Player temp = null;
		for (String name : getAllClanMembers())
		{
			temp = plugin.getServer().getPlayerExact(name);
			if (temp != null && temp.isOnline())
				ret.add(temp);
		}
		return ret;
	}

	/**
	 * Returns a list of all clan officers that are online
	 * 
	 * @return List of Player objects
	 */
	public List<Player> getOnlineOfficers()
	{
		List<Player> ret = new ArrayList<Player>();
		Player temp = null;
		for (String str : getOfficers())
		{
			temp = plugin.getServer().getPlayerExact(str);
			if (temp != null && temp.isOnline())
				ret.add(temp);
		}
		temp = plugin.getServer().getPlayerExact(leader);
		if (temp != null && temp.isOnline())
			ret.add(temp);
		return ret;
	}

	/**
	 * 
	 * @return True if the leader of this clan is online
	 */
	public boolean isLeaderOnline()
	{
		Player temp = plugin.getServer().getPlayerExact(leader);
		return temp != null && temp.isOnline();
	}

	/**
	 * Returns a lis of all clan members from all positions
	 * 
	 * @return List of String objects
	 */
	public List<String> getAllClanMembers()
	{
		List<String> ret = new ArrayList<String>();
		ret.add(leader);
		ret.addAll(officers);
		ret.addAll(members);
		return ret;
	}

	/**
	 * 
	 * @param name
	 *            String
	 * @return True if the name matches anyone in the clan
	 */
	public boolean hasMember(String name)
	{
		if (leader == null)
			leader = "-server";
		if (officers == null)
			officers = new ArrayList<String>();
		if (members == null)
			members = new ArrayList<String>();
		return leader.equals(name) || officers.contains(name)
				|| members.contains(name);
	}

	/**
	 * Returns the type of member for the player
	 * 
	 * @param name
	 *            String
	 * @return Clan.MemberType
	 */
	public MemberType getMemberType(String name)
	{
		if (leader.equals(name))
			return MemberType.LEADER;
		else if (officers.contains(name))
			return MemberType.OFFICER;
		else if (members.contains(name))
			return MemberType.MEMBER;
		return null;
	}

	/**
	 * 
	 * @param name
	 *            String
	 * @return If the name belongs to a regular member
	 */
	public boolean isMember(String name)
	{
		return members.contains(name);
	}

	/**
	 * 
	 * @param name
	 *            String
	 * @return If the name belongs to an officer
	 */
	public boolean isOfficer(String name)
	{
		return officers.contains(name);
	}

	/**
	 * 
	 * @param name
	 *            String
	 * @return If the name matches the leader
	 */
	public boolean isLeader(String name)
	{
		return leader != null && leader.equals(name);
	}

	/**
	 * Updates permissions groups for the player
	 * 
	 * @param player
	 *            String
	 */
	public void updateGroups(String player)
	{
		/*
		 * MEMBER: name + mbr, OFFICER: name + off, LEADER: name + ldr
		 */
		String world = "world";
		CalculableType type = CalculableType.USER;
		if (getMemberType(player) == null)
		{
			ApiLayer.removeGroup(world, type, player, name + "mbr");
			ApiLayer.removeGroup(world, type, player, name + "off");
			ApiLayer.removeGroup(world, type, player, name + "ldr");
			return;
		}
		switch (getMemberType(player))
		{
		default:
			ApiLayer.removeGroup(world, type, player, name + "mbr");
			ApiLayer.removeGroup(world, type, player, name + "off");
			ApiLayer.removeGroup(world, type, player, name + "ldr");
			break;
		case MEMBER:
			ApiLayer.addGroup(world, type, player, name + "mbr");
			ApiLayer.removeGroup(world, type, player, name + "off");
			ApiLayer.removeGroup(world, type, player, name + "ldr");
			break;
		case OFFICER:
			ApiLayer.removeGroup(world, type, player, name + "mbr");
			ApiLayer.addGroup(world, type, player, name + "off");
			ApiLayer.removeGroup(world, type, player, name + "ldr");
			break;
		case LEADER:
			ApiLayer.removeGroup(world, type, player, name + "mbr");
			ApiLayer.removeGroup(world, type, player, name + "off");
			ApiLayer.addGroup(world, type, player, name + "ldr");
			break;
		}
	}

	/**
	 * Shows all outstanding requests and invites to join the clan
	 * 
	 * @param player
	 *            Player
	 */
	@SuppressWarnings("boxing")
	public void showStatus(Player player)
	{
		String names = C.listToString(requests).replace(",", ", ");
		player.sendMessage("§7Pending requests to join the clan:§a "
				+ (names.equals("") ? "§8-none-" : names));
		names = C.listToString(invites).replace(",", ", ");
		player.sendMessage("§7Pending outstanding invites:§a "
				+ (names.equals("") ? "§8-none-" : names));
		player.sendMessage(String.format("§7OpenInvitation: §6%b\n§7AutoMemberRegionAdd: §6%b\n§7OfficersCanKick: §6%b\n§7OffcersCanAddTitles: §6%b", openInvitation, autoMemberRegionAdd, officersCanKick, offcersCanAddTitles));
	}

	/**
	 * Clan.MemberType<br>
	 * <br>
	 * LEADER<br>
	 * OFFICER<br>
	 * MEMBER
	 */
	public enum MemberType
	{
		LEADER, OFFICER, MEMBER;
	}

	/*
	 * GET
	 */

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getLeader()
	{
		return leader;
	}

	public List<String> getOfficers()
	{
		return officers;
	}

	public List<String> getMembers()
	{
		return members;
	}

	public List<String> getRequests()
	{
		return requests;
	}

	public boolean isRequesting(String string)
	{
		return requests.contains(string);
	}

	public List<String> getInvites()
	{
		return invites;
	}

	public boolean isInvited(String string)
	{
		return invites.contains(string);
	}

	/*
	 * SET
	 */

	public DarkClans getPlugin()
	{
		return plugin;
	}

	public void setLeader(String leader)
	{
		this.leader = leader;
	}

	public void setOfficers(List<String> officers)
	{
		this.officers = officers;
	}

	public void setMembers(List<String> members)
	{
		this.members = members;
	}

	public void setRequests(List<String> requests)
	{
		this.requests = requests;
	}

	public List<String> getAllies()
	{
		return allies;
	}

	public void setAllies(List<String> allies)
	{
		this.allies = allies;
	}

	public List<String> getEnemies()
	{
		return enemies;
	}

	public void setEnemies(List<String> enemies)
	{
		this.enemies = enemies;
	}

	public List<String> getNeutrals()
	{
		return neutrals;
	}

	public void setNeutrals(List<String> neutrals)
	{
		this.neutrals = neutrals;
	}

	public void setInvites(List<String> invites)
	{
		this.invites = invites;
	}

	/**
	 * 
	 * @param player
	 *            String
	 * @return Returns the name of the player, formatted with their position
	 *         against this clan
	 */
	public String getNameFormattedFor(String player)
	{
		if (!plugin.isInClan(player))
			return name;
		Clan other = plugin.getClanFor(player);
		if (enemies.contains(other.getName()))
			return "§c" + name;
		else if (allies.contains(other.getName()) || other.equals(this))
			return "§a" + name;
		return name;
	}

	public String getNameFormattedFor(Clan other)
	{
		if (other == null)
			return name;
		if (enemies.contains(other.getName()))
			return "§c" + name;
		else if (allies.contains(other.getName()) || other.equals(this))
			return "§a" + name;
		return name;
	}

	/**
	 * 
	 * @param player
	 *            String
	 * @return True if the player is trying to join the clan
	 */
	public boolean isPendingRequest(String player)
	{
		return requests.contains(player);
	}

	/**
	 * Shows information on this clan to the player
	 * 
	 * @param player
	 *            Player
	 */
	public void readOutTo(Player player)
	{
		player.sendMessage("§6=== " + name + " ===");
		player.sendMessage("§eMotD: §f" + motd.replace("&", "§"));
		player.sendMessage("§eLeader: "
				+ (plugin.isOnline(leader) ? "§a" : "§f")
				+ getNameWithTitle(leader));
		String ret = "";
		for (String o : officers)
		{
			if (plugin.ess.getVanishedPlayers().contains(o))
				o = "§f" + getNameWithTitle(o);
			else
			{
				if (plugin.isOnline(o))
					o = "§a" + getNameWithTitle(o);
				else
					o = "§f" + getNameWithTitle(o);
			}
			if (plugin.isNewPlayer(o))
				o += "§8(N)";
			if (ret.equals(""))
				ret = o;
			else
				ret += "§e, " + o;
		}
		player.sendMessage("§eOfficers (" + officers.size() + "): §f" + ret);
		ret = "";
		for (String o : members)
		{
			if (plugin.ess.getVanishedPlayers().contains(o))
				System.out.println(o + " is a member and vanished");
			else
			{
				if (plugin.isOnline(o))
					o = "§a" + getNameWithTitle(o);
				else
					o = "§f" + getNameWithTitle(o);
			}
			if (plugin.isNewPlayer(o))
				o += "§8(N)";
			if (ret.equals(""))
				ret = o;
			else
				ret += "§e, " + o;
		}
		player.sendMessage("§eMembers (" + members.size() + "): §f" + ret);
		player.sendMessage("§eAllies: §a"
				+ C.listToString(allies).replace(",", ", "));
		player.sendMessage("§eEnemies: §c"
				+ C.listToString(enemies).replace(",", ", "));
		player.sendMessage("§eNeutral: §f"
				+ C.listToString(neutrals).replace(",", ", "));
		player.sendMessage("§eExperience points: §f" + experience);
		player.sendMessage("§eCapture points: §f" + capturePoints);
	}

	/**
	 * 
	 * @param player
	 *            String
	 * @return title + player
	 */
	public String getNameWithTitle(String player)
	{
		if (getTitleFor(player).equals(""))
		{
			if (plugin.isOnline(player) && !plugin.ess.getVanishedPlayers().contains(player))
				return "§a" + player;
			return player;
		}
		return getTitleFor(player) + " "
				+ (plugin.isOnline(player) ? "§a" : "") + player;
	}

	/**
	 * 
	 * @param player
	 *            String
	 * @return Tag of this clan member
	 */
	public String getPositionTag(String player)
	{
		switch (getMemberType(player))
		{
		default:
		case MEMBER:
			return "";
		case OFFICER:
			return "*O*";
		case LEADER:
			return "*L*";
		}
	}

	public boolean isAllyWith(String otherClan)
	{
		return allies.contains(otherClan);
	}

	public boolean isEnemyWith(String otherClan)
	{
		return enemies.contains(otherClan);
	}

	public boolean isNeutralWith(String otherClan)
	{
		return neutrals.contains(otherClan)
				|| (!allies.contains(otherClan) && !enemies.contains(otherClan));
	}

	/**
	 * Insert the player into that position, regardless
	 * 
	 * @param player
	 *            String
	 * @param position
	 *            String
	 */
	public void put(String player, String position)
	{
		if (position.equalsIgnoreCase("member"))
		{
			if (!members.contains(player))
				members.add(player);
		}
		else if (position.equalsIgnoreCase("officer"))
		{
			if (!officers.contains(player))
			{
				officers.add(player);
				members.remove(player);
			}

		}
		else if (position.equalsIgnoreCase("leader"))
		{
			officers.add(leader);
			leader = player;
		}
	}

	/**
	 * 
	 * @param player
	 *            String
	 * @return String title for that player
	 */
	public String getTitleFor(String player)
	{
		if (titles.containsKey(player))
			return "§9" + titles.get(player) + "§f";
		return "";
	}

	/**
	 * 
	 * @param changer
	 *            Player
	 * @param player
	 *            String
	 * @param title
	 *            String
	 */
	public void setTitle(Player changer, String player, String title)
	{
		titles.put(player, title.equals("-1") ? null : title);
		changer.sendMessage(String.format("§eTitle for §a%s §eset to §a%s", player, title));
		broadcast(String.format("§a%s §eset the title of §a%s §eto §a%s", changer.getName(), player, title));
	}

	public int getExperience()
	{
		return experience;
	}

	public void setExperience(int experience)
	{
		this.experience = experience;
	}

	public void addExperience(int experience)
	{
		this.experience += experience;
	}

	public void setMembersFromDisk(List<String> ret)
	{
		for (String str : ret)
		{
			if (str.contains("-"))
			{
				members.add(str.split("-")[0]);
				titles.put(str.split("-")[0], str.split("-")[1]);
			}
			else
				members.add(str);
		}
	}

	public List<String> getMembersForDisk()
	{
		List<String> ret = new ArrayList<String>();
		for (String str : members)
			ret.add(str
					+ (titles.get(str) != null ? "-" + titles.get(str) : ""));
		return ret;
	}

	public void setOfficersFromDisk(List<String> ret)
	{
		for (String str : ret)
		{
			if (str.contains("-"))
			{
				officers.add(str.split("-")[0]);
				titles.put(str.split("-")[0], str.split("-")[1]);
			}
			else
				officers.add(str);
		}
	}

	public List<String> getOfficersForDisk()
	{
		List<String> ret = new ArrayList<String>();
		for (String str : officers)
			ret.add(str
					+ (titles.get(str) != null ? "-" + titles.get(str) : ""));
		return ret;
	}

	public boolean hasTitle(String who)
	{
		return titles.containsKey(who);
	}

	public String getLeaderForDisk()
	{
		if (hasTitle(leader))
			return leader + "-" + titles.get(leader);
		return leader;
	}

	public void setLeaderFromDisk(String string)
	{
		if (string.contains("-"))
		{
			leader = string.split("-")[0];
			titles.put(leader, string.split("-")[1]);
		}
		else
			leader = string;
	}

	public Map<String, String> getTitles()
	{
		return titles;
	}

	public void setTitles(Map<String, String> titles)
	{
		this.titles = titles;
	}

	public boolean isOpenInvitation()
	{
		return openInvitation;
	}

	public void setOpenInvitation(boolean openInvitation)
	{
		this.openInvitation = openInvitation;
	}

	public boolean isAutoMemberRegionAdd()
	{
		return autoMemberRegionAdd;
	}

	public void setAutoMemberRegionAdd(boolean autoMemberRegionAdd)
	{
		this.autoMemberRegionAdd = autoMemberRegionAdd;
	}

	public boolean isOfficersCanKick()
	{
		return officersCanKick;
	}

	public void setOfficersCanKick(boolean officersCanKick)
	{
		this.officersCanKick = officersCanKick;
	}

	public boolean isOfficersCanKickOfficers()
	{
		return officersCanKickOfficers;
	}

	public void setOfficersCanKickOfficers(boolean officersCanKickOfficers)
	{
		this.officersCanKickOfficers = officersCanKickOfficers;
	}

	public boolean isOffcersCanAddTitles()
	{
		return offcersCanAddTitles;
	}

	public void setOffcersCanAddTitles(boolean offcersCanAddTitles)
	{
		this.offcersCanAddTitles = offcersCanAddTitles;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object == null)
			return false;
		if (!(object instanceof Clan))
			return false;
		Clan test = (Clan) object;
		return test.getName().equals(name);
	}

	public enum Relationship
	{
		ALLY, ENEMY, NEUTRAL;
	}

	/**
	 * Allow messy command usage with crashing
	 * 
	 * @param other
	 *            Clan
	 * @param relationship
	 *            String (ally, enemy, neutral)
	 */
	public void setRelationship(Player sender, Clan other, String relationship)
	{
		try
		{
			setRelationship(other, Relationship.valueOf(relationship.toUpperCase()));
		}
		catch (Exception e)
		{
			sender.sendMessage("§cRelationship §7"
					+ relationship
					+ " §cnot recognized. Acceptable values: ally, enemy, neutral.");
		}
	}

	public void setRelationship(Clan other, Relationship relationship)
	{
		switch (relationship)
		{
		case ALLY:
			removeRelationship(other);
			allies.add(other.getName());
			break;
		case ENEMY:
			removeRelationship(other);
			enemies.add(other.getName());
			break;
		case NEUTRAL:
			removeRelationship(other);
			neutrals.add(other.getName());
			break;
		}
		plugin.getLogger().info(String.format("%s set relationship for %s to %s", name, other.getName(), relationship.name().toLowerCase()));
		broadcast("§eYour clan has set §7" + getRelationshipColor(relationship)
				+ " §eto §a" + other.getName());
	}

	public String getRelationshipColor(Relationship rel)
	{
		switch (rel)
		{
		case ALLY:
			return "§5ally";
		case ENEMY:
			return "§cenemy";
		case NEUTRAL:
			return "§eneutral";
		}
		return "§f";
	}

	public Relationship getRelationshipFor(Clan other)
	{
		if (allies.contains(other.getName()))
			return Relationship.ALLY;
		if (enemies.contains(other.getName()))
			return Relationship.ENEMY;
		return Relationship.NEUTRAL;
	}

	private void removeRelationship(Clan other)
	{
		String o = other.getName();
		allies.remove(o);
		enemies.remove(o);
		neutrals.remove(o);
	}

	public void addCapturePoints(int players)
	{
		capturePoints += players;
	}

	public int getCapturePoints()
	{
		return capturePoints;
	}

	public void setCapturePoints(int capturePoints)
	{
		this.capturePoints = capturePoints;
	}

	public String getMotD()
	{
		return motd;
	}

	public void setMotD(Player changer, String motd)
	{
		this.motd = motd;
		if (changer != null)
			broadcast("§a" + changer.getName() + " §ehas set the MotD to §a"
					+ motd.replace("&", "§"));
	}

	public void showMotD(Player player)
	{
		player.sendMessage("§eClan Message of the Day: §f"
				+ motd.replace("&", "§"));
	}

}