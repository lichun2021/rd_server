package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.global.LocalRedis;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.GuildManager.GetGuildInfoResp;
import com.hawk.game.protocol.GuildManager.GuildApplyInfo;
import com.hawk.game.protocol.GuildManager.GuildApplyInfo.Builder;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.GuildOperType;

public class NoGuildMemberApplyRecommendInvoker extends HawkRpcInvoker {

	/** 玩家 */
	private Player player;

	/** 联盟Id */
	private String guildId;

	/** 申请信息 */
	private GuildApplyInfo.Builder applyInfo;

	/** 协议Id */
	private int hpCode;
	
	public NoGuildMemberApplyRecommendInvoker(Player player, String guildId, Builder applyInfo, int hpCode){
		this.player = player;
		this.guildId = guildId;
		this.applyInfo = applyInfo;
		this.hpCode = hpCode;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		HawkTuple2<Integer, String> operationResult = GuildService.getInstance().onApplyGuildRecommend(applyInfo, guildId, true);
		result.put("res", operationResult.first);
		result.put("guildId", operationResult.second);
		int res = (int) result.get("res");
		String guildId = (String) result.get("guildId");
		if (res == Status.SysError.SUCCESS_OK_VALUE) {
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_APPLY, Params.valueOf("guildId", guildId));
			if (guildId != null) {
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
			} else {
				LocalRedis.getInstance().addPlayerGuildApply(player.getId(), guildId);
			}
			player.responseSuccess(hpCode);
			return true;
		}
		player.sendError(hpCode, res, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getGuildId() {
		return guildId;
	}

	public GuildApplyInfo.Builder getApplyInfo() {
		return applyInfo;
	}

	public int getHpCode() {
		return hpCode;
	}
}
