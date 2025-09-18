package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.GetGuildInfoResp;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildOperType;
import com.hawk.log.Source;

/**
 * 申请加入联盟
 * @author Jesse
 *
 */
public class GuildQuickJoinRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 协议Id */
	private int hpCode;

	public GuildQuickJoinRpcInvoker(Player player, int hpCode) {
		this.player = player;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {		
		HawkTuple2<Integer, String> operationResult = GuildService.getInstance().onQuickJoinGuild(player);
		result.put("res", operationResult.first);
		result.put("guildId", operationResult.second);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int res = (int) result.get("res");
		String guildId = (String) result.get("guildId");
		if (res == Status.SysError.SUCCESS_OK_VALUE) {
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_APPLY, Params.valueOf("guildId", guildId));
			player.joinGuild(guildId, false);
			LogUtil.logGuildFlow(player, GuildOperType.GUILD_JOIN, guildId, null);
			
			GetGuildInfoResp.Builder builder = GuildService.getInstance().getGuildInfo(player.getGuildId(), true);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETINFO_S, builder));

			player.getPush().syncGuildInfo();
			
			//推送联盟战争条数
			player.getPush().syncGuildWarCount();
			
			//推送联盟领地信息
			GuildManorList.Builder manorbuilder = GuildManorService.getInstance().makeManorListBuilder(guildId);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, manorbuilder));
			player.responseSuccess(hpCode);
		}else{
			player.sendError(hpCode, res, 0);
			/** 给客户端一条协议，让其监听然后提示创建联盟 **/
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUICK_JOIN_GUILD_FAIL_NOTICE_VALUE));
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
