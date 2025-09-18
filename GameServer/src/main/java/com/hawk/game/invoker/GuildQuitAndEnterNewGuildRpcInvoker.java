package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManager.GuildApplyInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.GuildAction;

/***
 * 退出联盟并立即加入新的联盟
 * @author yang.rao
 *
 */
public class GuildQuitAndEnterNewGuildRpcInvoker extends HawkRpcInvoker {

	/** 玩家 */
	private Player player;

	/** 联盟Id */
	private String curGuildId;

	/** 协议Id */
	private int hpCode;
	
	/** 需要加入的联盟ID **/
	private String enterGuildId;
	
	 public GuildQuitAndEnterNewGuildRpcInvoker(Player player, String curGuildId, String enterGuildId, int hpCode) {
		this.player = player;
		this.curGuildId = curGuildId;
		this.hpCode = hpCode;
		this.enterGuildId = enterGuildId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = GuildService.getInstance().onQuitGuild(curGuildId, player.getId());
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(hpCode, operationResult, 0);
			return false;
		}

		if (GameUtil.isScoreBatchEnable(player) && !player.isActiveOnline()) {
			LocalRedis.getInstance().addScoreBatchFlag(player.getId(), ScoreType.GUILD_MEMBER_CHANGE.intValue(), "2");
		} else {
			GameUtil.scoreBatch(player, ScoreType.GUILD_MEMBER_CHANGE, 2);
		}
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_QUIT, Params.valueOf("guildId", player.getGuildId()));
		// 返回协议
		player.responseSuccess(hpCode);
		reqNewGuildRpc(enterGuildId);
		//加入新的联盟
		return true;
	}
	
	public Player getPlayer() {
		return player;
	}

	public String getcurGuildId() {
		return curGuildId;
	}

	public int getHpCode() {
		return hpCode;
	}
	
	private boolean reqNewGuildRpc(String guildId){
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.CREAT_ALLIANCE)) {
			player.sendError(hpCode, Status.Error.GUILD_ALREADYJOIN.getNumber(), 0);
			return false;
		}
		if (player.getCityLv() > GuildConstProperty.getInstance().getCreateGuildCostGoldLevel()) {
			if (HawkTime.getMillisecond() - GuildService.getInstance().getPlayerQuitGuildTime(player.getId()) < GuildConstProperty.getInstance().getAllianceJoinCooldownTime()) {
				player.sendError(hpCode, Status.Error.GUILD_QUITTIME_ILLEGAL.getNumber(), 0);
				return false;
			}
		}
		if (GuildService.getInstance().containGuildApply(player.getId(), guildId)) {
			player.sendError(hpCode, Status.Error.GUILD_ALREADY_APPLY_VALUE, 0);
			return false;
		}
		GuildApplyInfo.Builder applyInfo = GuildApplyInfo.newBuilder();
		applyInfo.setPlayerId(player.getId());
		applyInfo.setPlayerName(player.getName());
		applyInfo.setPower(player.getPower());
		applyInfo.setVip(player.getVipLevel());
		applyInfo.setLanguage(player.getLanguage());
		applyInfo.setIcon(player.getIcon());
		applyInfo.setCommonderLevel(player.getLevel());
		applyInfo.setBuildingLevel(player.getCityLv());
		applyInfo.setVipStatus(player.getData().getVipActivated());
		applyInfo.setKillEnemy(player.getData().getStatisticsEntity().getArmyKillCnt());
		applyInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		String pfIcon = player.getPfIcon();
		if (!HawkOSOperator.isEmptyString(pfIcon)) {
			applyInfo.setPfIcon(pfIcon);
		}
		player.rpcCall(MsgId.APPLY_GUILD, GuildService.getInstance(), new NoGuildMemberApplyRecommendInvoker(player, guildId, applyInfo, hpCode));
		LogUtil.logGuildAction(GuildAction.GUILD_APPLAY, guildId);
		return true;
	}
}
