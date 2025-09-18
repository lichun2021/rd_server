package com.hawk.game.invoker;

import java.util.List;
import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.log.Action;

/**
 * 修改联盟简称
 * 
 * @author Jesse
 *
 */
public class GuildGetTaskRewardRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;
	
	/** 任务id*/
	private List<Integer> taskIds;

	/** 奖励信息 */
	private AwardItems awardItems;

	/** 协议Id */
	private int hpCode;

	public GuildGetTaskRewardRpcInvoker(Player player, List<Integer> taskIds, AwardItems awardItems, int hpCode) {
		this.player = player;
		this.awardItems = awardItems;
		this.taskIds = taskIds;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult= GuildService.getInstance().getGuildTaskReward(player, taskIds, awardItems);
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			awardItems.rewardTakeAffectAndPush(player, Action.GUILD_TASK_AWARD, true);
			player.responseSuccess(hpCode);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getHpCode() {
		return hpCode;
	}

	public List<Integer> getTaskIds() {
		return taskIds;
	}

	public AwardItems getAwardItems() {
		return awardItems;
	}
	
}
