package com.hawk.game.invoker;

import java.util.List;
import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CreateGuildEvent;
import com.hawk.game.guild.GuildCreateObj;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg.PLAYERSTAT_PARAM;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.CreateGuildResp;
import com.hawk.game.protocol.GuildManager.GetGuildInfoResp;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildAction;
import com.hawk.log.LogConst.GuildOperType;
import com.hawk.log.Source;

/**
 * 联盟创建
 * 
 * @author Jesse
 *
 */
public class GuildCreateRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟创建信息 */
	private GuildCreateObj creatObj;

	public GuildCreateRpcInvoker(Player player, GuildCreateObj creatObj) {
		this.player = player;
		this.creatObj = creatObj;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		HawkTuple2<Integer, String> resultTuple = GuildService.getInstance().onCreateGuild(player, creatObj);
		result.put("res", resultTuple.first);
		result.put("guildId", resultTuple.second);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(creatObj.getProtoType(), operationResult, 0);
			return false;
		}

		String guildId = (String) result.get("guildId");
		player.joinGuild(guildId, true);
		CreateGuildResp.Builder builder = CreateGuildResp.newBuilder();
		GetGuildInfoResp.Builder info = GuildService.getInstance().getGuildInfo(guildId, true);
		builder.setInfo(info);
		player.getPush().syncGuildInfo();
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_CREATE_S, builder));
		
		// 检查当前联盟领地是否达到开启条件，如果达到则修改状态，并发送通知邮件
		List<GuildManorObj> manors = GuildManorService.getInstance().getGuildManors(guildId);
		if (manors != null) {
			for (GuildManorObj guildManor : manors) {
				// 判断联盟领地是否达到开启条件
				guildManor.checkUnlockAndChangeStat();
			}
			// 推送联盟领地信息
			GuildManorList.Builder manorbuilder = GuildManorService.getInstance().makeManorListBuilder(guildId);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, manorbuilder));
		} else {
			GuildManorService.logger.warn("guild has not init Manor, guildId=" + guildId);
		}

		// 推送联盟战争条数
		player.getPush().syncGuildWarCount();

		LogUtil.logGuildFlow(player, GuildOperType.GUILD_CREATE, guildId, null);
		
		GameUtil.scoreBatch(player,ScoreType.GUILD_CREATE, HawkTime.getMillisecond());

		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CREATE, Params.valueOf("guildId", guildId));

		//解锁盟主头像
		HawkTaskManager.getInstance().postMsg(player.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.MENGZHU));
		
		WarFlagService.getInstance().checkWarFlagCount(guildId);
		
		ActivityManager.getInstance().postEvent(new CreateGuildEvent(player.getId(), guildId));
		
		LogUtil.logGuildAction(GuildAction.GUILD_CREATE, guildId);
		
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public GuildCreateObj getCreatObj() {
		return creatObj;
	}

}
