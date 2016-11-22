package com.Ben12345rocks.VotingPlugin.VoteParty;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.Ben12345rocks.AdvancedCore.Listeners.DayChangeEvent;
import com.Ben12345rocks.AdvancedCore.Objects.RewardHandler;
import com.Ben12345rocks.AdvancedCore.Objects.UUID;
import com.Ben12345rocks.AdvancedCore.Util.Misc.ArrayUtils;
import com.Ben12345rocks.AdvancedCore.Util.Misc.PlayerUtils;
import com.Ben12345rocks.AdvancedCore.Util.Misc.StringUtils;
import com.Ben12345rocks.VotingPlugin.Main;
import com.Ben12345rocks.VotingPlugin.Config.ConfigFormat;
import com.Ben12345rocks.VotingPlugin.Config.ConfigOtherRewards;
import com.Ben12345rocks.VotingPlugin.Data.ServerData;
import com.Ben12345rocks.VotingPlugin.Objects.User;
import com.Ben12345rocks.VotingPlugin.UserManager.UserManager;

// TODO: Auto-generated Javadoc
/**
 * The Class VoteParty.
 */
public class VoteParty implements Listener {

	/** The instance. */
	static VoteParty instance = new VoteParty();

	/** The plugin. */
	static Main plugin = Main.plugin;

	/**
	 * Gets the single instance of VoteParty.
	 *
	 * @return single instance of VoteParty
	 */
	public static VoteParty getInstance() {
		return instance;
	}

	/**
	 * Instantiates a new vote party.
	 */
	private VoteParty() {
	}

	/**
	 * Instantiates a new vote party.
	 *
	 * @param plugin
	 *            the plugin
	 */
	public VoteParty(Main plugin) {
		VoteParty.plugin = plugin;
	}

	public void register() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void addTotal(User user) {
		setTotalVotes(getTotalVotes() + 1);
		user.setVotePartyVotes(user.getVotePartyVotes() + 1);
	}

	@EventHandler
	public void onDayChange(DayChangeEvent event) {
		if (ConfigOtherRewards.getInstance().getVotePartyResetEachDay()) {
			setTotalVotes(0);
			setVotedUsers(new ArrayList<String>());
		}
	}

	/**
	 * Adds the vote player.
	 *
	 * @param user
	 *            the user
	 */
	public void addVotePlayer(User user) {
		String uuid = user.getUUID();
		ArrayList<String> voted = getVotedUsers();
		if (voted == null) {
			voted = new ArrayList<String>();
		}
		if (!voted.contains(uuid)) {
			voted.add(uuid);
			setVotedUsers(voted);
		}
	}

	/**
	 * Check.
	 */
	public void check() {
		if (getTotalVotes() >= ConfigOtherRewards.getInstance()
				.getVotePartyVotesRequired()) {
			setTotalVotes(getTotalVotes()
					- ConfigOtherRewards.getInstance()
							.getVotePartyVotesRequired());
			giveRewards();
		}

	}

	/**
	 * Command vote party.
	 *
	 * @param sender
	 *            the sender
	 */
	public void commandVoteParty(CommandSender sender) {
		ArrayList<String> msg = ConfigFormat.getInstance()
				.getCommandsVoteParty();
		ArrayList<String> lines = new ArrayList<String>();
		int votesRequired = ConfigOtherRewards.getInstance()
				.getVotePartyVotesRequired();
		int votes = getTotalVotes();
		int neededVotes = votesRequired - votes;
		for (String line : msg) {
			line = line.replace("%VotesRequired%", "" + votesRequired)
					.replace("%NeededVotes%", "" + neededVotes)
					.replace("%Votes%", "" + votes);
			lines.add(StringUtils.getInstance().colorize(line));
		}
		sender.sendMessage(ArrayUtils.getInstance().convert(lines));
	}

	/**
	 * Gets the needed votes.
	 *
	 * @return the needed votes
	 */
	public int getNeededVotes() {
		int votesRequired = ConfigOtherRewards.getInstance()
				.getVotePartyVotesRequired();
		int votes = getTotalVotes();
		int neededVotes = votesRequired - votes;
		return neededVotes;
	}

	/**
	 * Gets the offline vote party votes.
	 *
	 * @param user
	 *            the user
	 * @return the offline vote party votes
	 */
	public int getOfflineVotePartyVotes(User user) {
		return user.getPluginData().getInt("VoteParty.OfflineVotes");
	}

	/**
	 * Gets the total votes.
	 *
	 * @return the total votes
	 */
	public int getTotalVotes() {
		return ServerData.getInstance().getData().getInt("VoteParty.Total");
	}

	/**
	 * Gets the voted users.
	 *
	 * @return the voted users
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> getVotedUsers() {
		ArrayList<String> list = (ArrayList<String>) ServerData.getInstance()
				.getData().getList("VoteParty.Voted");
		if (list != null) {
			return list;
		}
		return new ArrayList<String>();
	}

	/**
	 * Give reward.
	 *
	 * @param user
	 *            the user
	 */
	public void giveReward(User user) {
		if (PlayerUtils.getInstance().isPlayerOnline(user.getPlayerName())) {
			if (user.getVotePartyVotes() >= ConfigOtherRewards.getInstance()
					.getUserVotesRequired()) {
				for (String reward : ConfigOtherRewards.getInstance()
						.getVotePartyRewards()) {
					RewardHandler.getInstance().giveReward(user, reward);
				}
			}
		} else {
			setOfflineVotePartyVotes(user, getOfflineVotePartyVotes(user) + 1);
		}
	}

	/**
	 * Give rewards.
	 */
	public void giveRewards() {
		if (ConfigOtherRewards.getInstance().getVotePartyGiveAllPlayers()) {
			for (User user : UserManager.getInstance().getVotingPluginUsers()) {
				giveReward(user);
			}
		} else {
			for (String uuid : getVotedUsers()) {
				User user = UserManager.getInstance().getVotingPluginUser(
						new UUID(uuid));
				giveReward(user);
			}
		}
		reset();
	}

	public void reset() {
		setVotedUsers(new ArrayList<String>());
		for (User user : UserManager.getInstance().getVotingPluginUsers()) {
			if (user.getVotePartyVotes() != 0) {
				user.setVotePartyVotes(0);
			}
		}
	}

	/**
	 * Sets the offline vote party votes.
	 *
	 * @param user
	 *            the user
	 * @param value
	 *            the value
	 */
	public void setOfflineVotePartyVotes(User user, int value) {
		user.setPluginData("VoteParty.OfflineVotes", value);
	}

	/**
	 * Sets the total votes.
	 *
	 * @param value
	 *            the new total votes
	 */
	public void setTotalVotes(int value) {
		ServerData.getInstance().getData().set("VoteParty.Total", value);
		ServerData.getInstance().saveData();
	}

	/**
	 * Sets the voted users.
	 *
	 * @param value
	 *            the new voted users
	 */
	public void setVotedUsers(ArrayList<String> value) {
		ServerData.getInstance().getData().set("VoteParty.Voted", value);
		ServerData.getInstance().saveData();
	}

}