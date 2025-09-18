package com.hawk.game.invoker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.msg.PlayerLockImageMsg;
import com.hawk.game.msg.PlayerLockImageMsg.LockParam;
import com.hawk.game.msg.PlayerLockImageMsg.LockType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildOperType;
import com.hawk.log.Source;

/**
 * 联盟解散
 * 
 * @author Jesse
 *
 */
public class GuildDismissRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟Id */
	private String guildId;

	/** 协议Id */
	private int hpCode;

	public GuildDismissRpcInvoker(Player player, String guildId, int hpCode) {
		super();
		this.player = player;
		this.guildId = guildId;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = GuildService.getInstance().onDissmiseGuild(guildId, player);
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult != Status.SysError.SUCCESS_OK_VALUE) {
			HawkLog.errPrintln("guild dismiss failed, playerId: {}, guildId: {}", player.getId(), guildId);
			if (hpCode > 0) {
				player.sendError(hpCode, operationResult, 0);
			}
			return false;
		}
		
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_DISMISSGUILD, Params.valueOf("guildId", guildId));
		HawkApp.getInstance().postMsg(player.getXid(), GuildQuitMsg.valueOf(false, guildId));
		LogUtil.logGuildFlow(player, GuildOperType.GUILD_DISSOLVE, guildId, null);
		
		GameUtil.scoreBatch(player,ScoreType.GUILD_DISSOLVE, HawkTime.getMillisecond());
		ActivityManager.getInstance().postEvent(new GuildDismissEvent(player.getId(), guildId));
		if (hpCode > 0) {
			player.responseSuccess(hpCode);
		}
		//盟主改变，上锁头像
		HawkTaskManager.getInstance().postMsg(player.getXid(), PlayerLockImageMsg.valueOf(LockType.PLAYERSTAT, LockParam.NO_MENGZHU));
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getHpCode() {
		return hpCode;
	}

	public void setHpCode(int hpCode) {
		this.hpCode = hpCode;
	}

}
