package com.hawk.game.module;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.championship.ChampionshipService;
import com.hawk.game.guild.championship.GCConst.GCGuildGrade;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.GuildChampionship.GCFlushMarch;
import com.hawk.game.protocol.GuildChampionship.GCGetMemberMarchReq;
import com.hawk.game.protocol.GuildChampionship.GCGetRankReq;
import com.hawk.game.protocol.GuildChampionship.GCGetRankResp;
import com.hawk.game.protocol.GuildChampionship.GCGuildBattle;
import com.hawk.game.protocol.GuildChampionship.GCPageInfo;
import com.hawk.game.protocol.GuildChampionship.GetGuildBattleInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 泰伯利亚之战
 * 
 * @author Jesse
 */
public class PlayerGuildChampionshipModule extends PlayerModule {

	public PlayerGuildChampionshipModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		if (!player.isCsPlayer()) {
			ChampionshipService.getInstance().syncPageInfo(player);
		}
		return true;
	}

	/**
	 * 请求界面信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_GET_PAGE_INFO_C_VALUE)
	private void onGetPageInfo(HawkProtocol protocol) {
		GCPageInfo.Builder pageInfo = ChampionshipService.getInstance().genPageInfo(player.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CHAMPIONSHIP_GET_PAGE_INFO_S_VALUE, pageInfo));
	}

	/**
	 * 报名
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_SIGN_UP_C_VALUE)
	private void onSignUp(HawkProtocol protocol) {
		GCFlushMarch req = protocol.parseProtocol(GCFlushMarch.getDefaultInstance());
		int result = ChampionshipService.getInstance().saveBattlePlayer(player, req.getMarchInfo());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
		} else {
			sendError(protocol.getType(), result);
		}
	}

	/**
	 * 刷新阵容
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_FLUSH_MARCH_C_VALUE)
	private void onFlushMarch(HawkProtocol protocol) {
		GCFlushMarch req = protocol.parseProtocol(GCFlushMarch.getDefaultInstance());
		int result = ChampionshipService.getInstance().saveBattlePlayer(player, req.getMarchInfo());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
		} else {
			sendError(protocol.getType(), result);
		}
	}

	/**
	 * 查看成员出战阵容
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_GET_MARCH_INFO_C_VALUE)
	private void onGetMemberMarchInfo(HawkProtocol protocol) {
		GCGetMemberMarchReq req = protocol.parseProtocol(GCGetMemberMarchReq.getDefaultInstance());
		ChampionshipService.getInstance().onGetMemberMarchInfo(player, req.getPlayerId());
	}

	/**
	 * 获取联盟对战详情
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_GET_GUILDBATTLE_C_VALUE)
	private void onGetGuildBattleInfo(HawkProtocol protocol) {
		GetGuildBattleInfo req = protocol.parseProtocol(GetGuildBattleInfo.getDefaultInstance());
		GCGuildBattle.Builder builder = RedisProxy.getInstance().getGCGuildBattle(ChampionshipService.activityInfo.getTermId(), req.getGBattleId());
		if (builder == null) {
			sendError(protocol.getType(), Status.Error.CHAMPIONSHIP_GUILD_BATTLE_NOT_EXIST_VALUE);
			return;
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CHAMPIONSHIP_GET_GUILDBATTLE_S, builder));
	}

	/**
	 * 获取小组历史战斗数据
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_GET_HISTORY_BATTLE_C_VALUE)
	private void onGetHistoryBattleInfo(HawkProtocol protocol) {
		int result = ChampionshipService.getInstance().onGetHistoryBattle(player);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), result);
		}
	}

	/**
	 * 获取小组排行信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_GET_GROUP_RANK_C_VALUE)
	private void onGetGroupRank(HawkProtocol protocol) {
		int result = ChampionshipService.getInstance().onGetGroupRank(player);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), result);
		}
	}

	/**
	 * 领取奖励信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_GET_REWARD_C_VALUE)
	private void onGetReward(HawkProtocol protocol) {
		int result = ChampionshipService.getInstance().getReward(player);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
		} else {
			sendError(protocol.getType(), result);
		}
	}
	
	/**
	 * 获取段位排行信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_GET_GRANK_PAGEINFO_C_VALUE)
	private void onGetGRankPageInfo(HawkProtocol protocol) {
		GCGetRankResp.Builder builder = ChampionshipService.getInstance().getGRankPageInfo(player);
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CHAMPIONSHIP_GET_GRANK_PAGEINFO_S, builder));
			return;
		} else {
			sendError(protocol.getType(), -1);
		}
	}
	/**
	 * 获取段位排行信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CHAMPIONSHIP_GET_RANK_INFO_C_VALUE)
	private void onGetRankInfo(HawkProtocol protocol) {
		GCGetRankReq req = protocol.parseProtocol(GCGetRankReq.getDefaultInstance());
		GCGuildGrade grade = null;
		if (req.hasGrade()) {
			grade = GCGuildGrade.valueOf(req.getGrade());
		}
		GCGetRankResp.Builder builder = ChampionshipService.getInstance().getRankInfoBuilder(req.getRankType(), grade, player);
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CHAMPIONSHIP_GET_RANK_INFO_S, builder));
			return;
		} else {
			sendError(protocol.getType(), -1);
		}
	}

}
