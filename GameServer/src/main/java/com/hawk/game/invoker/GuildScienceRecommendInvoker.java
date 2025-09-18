package com.hawk.game.invoker;

import java.util.List;

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
 * 修改联盟科技推荐
 * @author Jesse
 *
 */
public class GuildScienceRecommendInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 推荐科技Id列表 */
	private List<Integer> recommendIds;

	/** 取消推荐科技Id列表 */
	private List<Integer> cancleIds;

	/** 协议Id */
	private int hpCode;

	public GuildScienceRecommendInvoker(Player player, int hpCode, List<Integer> recommendIds, List<Integer> cancleIds) {
		this.player = player;
		this.hpCode = hpCode;
		this.recommendIds = recommendIds;
		this.cancleIds = cancleIds;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onGuildScienceRecommend(player, recommendIds, cancleIds);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.GUILD_SCIENCE_SET_RECOMMEND_C_VALUE);
			// 行为日志
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_SCIENCE_RECOMMEND,
					Params.valueOf("guildId", player.getGuildId()),
					Params.valueOf("recommendIds", recommendIds),
					Params.valueOf("cancleIds", cancleIds));
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public List<Integer> getRecommendIds() {
		return recommendIds;
	}

	public List<Integer> getCancleIds() {
		return cancleIds;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
