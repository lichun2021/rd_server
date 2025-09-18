package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.log.Action;
import com.hawk.log.Source;
/**
 * 联盟科技研究
 * @author Jesse
 *
 */
public class GuildScienceResearchInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟科技Id */
	private int scienceId;

	/** 协议Id */
	private int hpCode;

	public GuildScienceResearchInvoker(Player player, int scienceId, int hpCode) {
		this.player = player;
		this.scienceId = scienceId;
		this.hpCode = hpCode;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onGuildScienceResearch(player, scienceId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.GUILD_SCIENCE_RESEARCH_C_VALUE);
			// 行为日志
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_SCIENCE_RESEARCH,
					Params.valueOf("guildId", player.getGuildId()),
					Params.valueOf("scienceId", scienceId));
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getScienceId() {
		return scienceId;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
