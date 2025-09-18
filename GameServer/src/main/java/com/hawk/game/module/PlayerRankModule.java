package com.hawk.game.module;

import java.util.ArrayList;
import java.util.List;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.RankGroup;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Rank.GuardRankInfo;
import com.hawk.game.protocol.Rank.GuardRankResp;
import com.hawk.game.protocol.Rank.HPPushRank;
import com.hawk.game.protocol.Rank.HPPushTopRank;
import com.hawk.game.protocol.Rank.HPSendRank;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankObject;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 排行榜
 * 
 * @author zdz
 * @reviewer link
 *
 */
public class PlayerRankModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	public PlayerRankModule(Player player) {
		super(player);
	}
	
	

	@Override
	protected boolean onPlayerLogin() {
		// 超过新手期的玩家,登陆时更新玩家个人排行榜数据
		if(player.getCityLevel() >= WorldMapConstProperty.getInstance().getStepCityLevel1()){
			player.updateRankScore(MsgId.PLAYER_FIGHT_RANK_REFRESH, RankType.PLAYER_FIGHT_RANK, player.getPower());
			player.updateRankScore(MsgId.PLAYER_KILL_RANK_REFRESH, RankType.PLAYER_KILL_ENEMY_RANK, player.getData().getStatisticsEntity().getArmyKillCnt());
		}
		return super.onPlayerLogin();
	}


	/**
	 * 打开排行面板
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.RANK_OPEN_PANEL_C_VALUE)
	private boolean onOpenRankPanel(HawkProtocol protocol) {
		HPPushTopRank.Builder builder = HPPushTopRank.newBuilder();
		List<RankInfo> myRankList = new ArrayList<>();
		for(RankType rankType : RankType.values()) {
			String targetId = player.getId();
			if(rankType == RankType.ALLIANCE_FIGHT_KEY || rankType == RankType.ALLIANCE_KILL_ENEMY_KEY) {
				targetId = player.getGuildId();
			}
			
			if(HawkOSOperator.isEmptyString(targetId)) {
				continue;
			}
			
			String banRankInfo = GlobalData.getInstance().getBanRankInfo(targetId, rankType.name().toLowerCase());
			if(HawkOSOperator.isEmptyString(banRankInfo)) {
				continue;
			}
			
			String[] infos = banRankInfo.split(":");
			RankService.getInstance().sendBanRankNotice(player, Long.valueOf(infos[1]), infos[2]);
			break;
		}
		
		for(RankType rankType : RankType.values()) {
			String rankKey = player.getId();
			GuildInfoObject guildInfo = null;
			Player playerInfo = null;
			if (RankService.getInstance().isGuildTypeRank(rankType)) {
				rankKey = player.getGuildId();
				if (HawkOSOperator.isEmptyString(rankKey)) {
					continue;
				}
				guildInfo = GuildService.getInstance().getGuildInfoObject(rankKey);
			} else {
				playerInfo = GlobalData.getInstance().makesurePlayer(rankKey);
			}
			
			//榜单中自己的排行信息
			RankInfo selfInfo = RankService.getInstance().getRankInfo(rankType, rankKey);
			if (selfInfo == null) {
				RankObject rankObject = RankService.getInstance().getRankObject(rankType);
				selfInfo = rankObject.buildRankInfo(0, rankKey, 0, playerInfo, guildInfo, RankGroup.ALLIANCE_TYPE_VALUE);
			}
			
			myRankList.add(selfInfo);
		}
		
		builder.addAllRankInfo(myRankList);
		
		String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
		if (HawkOSOperator.isEmptyString(guardPlayerId)) {
			builder.setGuardRank(-1);
		}  else {
			int guardRankNo = RankService.getInstance().getGuardRankObject().getRank(player.getId(), guardPlayerId);
			builder.setGuardRank(guardRankNo);
		} 
					
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RANK_OPEN_PANEL_S_VALUE, builder));
		
		return true;
	}

	/**
	 * 请求排行榜信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.RANK_INFO_C_VALUE)
	private boolean onPlayerRank(HawkProtocol protocol) {
		HPSendRank req = protocol.parseProtocol(HPSendRank.getDefaultInstance());
		RankType rankType = req.getRankType();
		if (rankType == null) {
			logger.error("get rank failed, rankType unset playerId: {}", player.getId());
			return false;
		}
		
		HPPushRank.Builder builder = HPPushRank.newBuilder();
		builder.setUpdateTime(HawkTime.getMillisecond());
		builder.setRankType(rankType);
		player.getData().getPowerElectric().getPowerData();
		List<RankInfo> rankInfos = RankService.getInstance().getRankCache(rankType);
		builder.addAllRankInfo(rankInfos);
 		
		String rankKey = player.getId();
		if (RankService.getInstance().isGuildTypeRank(rankType)) {
			rankKey = player.getGuildId();
		}
		
		//榜单中自己的排行信息
		HawkTuple2<Integer, Long> selfInfo = RankService.getInstance().getRankTuple(rankType, rankKey, player);
		builder.setMyRank(selfInfo.first);
		builder.setMyRankScore(selfInfo.second);
		
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
		if (guildInfo != null) {
			builder.setGuildLeaderName(guildInfo.getLeaderName());
			builder.setGuildName(guildInfo.getName());
			builder.setGuildTag(guildInfo.getTag());
			builder.setGuildFlag(guildInfo.getFlagId());
		}

		// 大本等级达成时间
		if (rankType.equals(RankType.PLAYER_CASTLE_KEY) && player.getCityLevel() >= 40) {
			builder.setRankTime(GlobalData.getInstance().getCityRankTime(player.getId()));
		}
		
		HawkProtocol resp = HawkProtocol.valueOf(HP.code.RANK_INFO_S_VALUE, builder);
		player.sendProtocol(resp);
		return true;
	}
	
	@ProtocolHandler(code = HP.code.GUARD_RANK_REQ_VALUE)
	public void onGuardRankReq(HawkProtocol hawkProtocol) {
		GuardRankResp.Builder sbuilder = GuardRankResp.newBuilder();
		sbuilder.addAllRankList(RankService.getInstance().getGuardRankObject().getGuardRankInfo());
		String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
		if (!HawkOSOperator.isEmptyString(guardPlayerId)) {
			int rankNo = RankService.getInstance().getGuardRankObject().getRank(player.getId(), guardPlayerId);
			GuardRankInfo.Builder builder = GuardRankInfo.newBuilder();
			builder.setRankNo(rankNo);
			String[] idArray = RankService.getInstance().getGuardRankObject().sortPlayerId(player.getId(), guardPlayerId);
			Player tmpPlayer = null;
			tmpPlayer = GlobalData.getInstance().makesurePlayer(idArray[0]);
			builder.setFirstPlayerName(tmpPlayer.getName());
			builder.setFirstPlayerGuildTag(tmpPlayer.getGuildTag());
			builder.setFirstPlayerId(tmpPlayer.getId());
			builder.setFirstCommon(BuilderUtil.genPlayerCommonBuilder(tmpPlayer));
			
			tmpPlayer = GlobalData.getInstance().makesurePlayer(idArray[1]);
			builder.setSecondPlayerName(tmpPlayer.getName());
			builder.setSecondPlayerGuildTag(tmpPlayer.getGuildTag());
			builder.setSecondPlayerId(tmpPlayer.getId());
			builder.setSecondCommon(BuilderUtil.genPlayerCommonBuilder(tmpPlayer));
			
			builder.setGuardValue(RelationService.getInstance().getGuardValue(player.getId()));
			
			sbuilder.setSelfRankInfo(builder);
		}
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.GUARD_RANK_RESP_VALUE, sbuilder);
		player.sendProtocol(respProtocol);
	}
}
 