package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildScience.DonateType;
import com.hawk.game.protocol.GuildScience.GuildScienceDonateResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.GuildService;

/**
 * 联盟科技-捐献及奖励发放
 * 
 * @author Jesse
 *
 */
public class GuildScienceDonateAfterInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 捐献类型 */
	private DonateType type;

	/** 联盟科技id */
	private int scienceId;
	
	/** 联盟积分*/
	private int guildScore;

	/** 科技积分*/
	private int scienceDonate;

	/** 暴击次数*/
	private int crit;

	/** 玩家贡献*/
	private int contribution;

	public GuildScienceDonateAfterInvoker(Player player, DonateType type, int scienceId, int guildScore, int scienceDonate, int contribution, int crit) {
		this.player = player;
		this.type = type;
		this.scienceId = scienceId;
		this.guildScore = guildScore;
		this.scienceDonate = scienceDonate;
		this.crit = crit;
		this.contribution = contribution;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		GuildScienceDonateResp.Builder resp = GuildService.getInstance().onGuildScienceDonate(player, type, scienceId, scienceDonate, guildScore, contribution, crit);
		if (resp != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_DONATE_S_VALUE, resp));
			return true;
		}
		return false;
	}

	public Player getPlayer() {
		return player;
	}

	public DonateType getType() {
		return type;
	}

	public int getScienceId() {
		return scienceId;
	}

	public int getGuildScore() {
		return guildScore;
	}

	public int getScienceDonate() {
		return scienceDonate;
	}

	public int getCrit() {
		return crit;
	}

	public int getContribution() {
		return contribution;
	}

}
