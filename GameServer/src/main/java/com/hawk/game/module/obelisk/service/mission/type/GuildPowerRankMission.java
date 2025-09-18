package com.hawk.game.module.obelisk.service.mission.type;

import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.entity.ObeliskEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.obelisk.service.mission.ObeliskMission;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Obelisk;
import com.hawk.game.protocol.Obelisk.PBObelisk.Builder;
import com.hawk.game.protocol.Rank;
import com.hawk.game.protocol.Rank.HPPushRank;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.event.EventPowerCreate;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 所在联盟进入联盟战力排行榜前X名
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.GUIlD_POWER_RANK)
public class GuildPowerRankMission implements IObeliskMission{

	@Override
	public Class<? extends MissionEvent> getEventClassType(){
		return EventPowerCreate.class;
	}

	@Override
	public ObeliskMissionType getObeliskMissionType() {
		return IObeliskMission.super.getObeliskMissionType();
	}



	@Override
	public void onTick(ObeliskMissionItem missionItem) {
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.NOTOPEN) {
			// 如果任务成功开启
			boolean start = startMission(missionItem);
			if (start) {
				onMissionStart(missionItem);
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.OPEN) {
			// 检查任务关闭 通知红点
			boolean close = closeMission(missionItem);
			if (close) {
				ObeliskService.getInstance().noticeNewPoint();
				List<Rank.RankInfo> rankList = RankService.getInstance().getRankCache(Rank.RankType.ALLIANCE_FIGHT_KEY);
				missionItem.setRankInfo(genHpPushRankBuilder(rankList).build());
				//排行榜类型的结束更新相关玩家数据
				updateGuildMemberObeliskRankInfo(rankList, missionItem);
				HawkLog.logPrintln("GuildPowerRankMission closeMission success rankData is save cache rankSize:{}", rankList.size());
			}
		}
	}

	@Override
	public void onMissionStart(ObeliskMissionItem missionItem) {
		boolean isCheck = checkOverTimeComplete(missionItem,  false);
		//校验是否直接完成
		if (isCheck){
			List<Rank.RankInfo> rankList = new ArrayList<>();
			missionItem.setRankInfo(genHpPushRankBuilder(rankList).build());
			//修改状态
			missionItem.setState(Obelisk.PBObeliskMissionState.CLOSED);
		}
	}

	/**
	 * 更新排行榜中,相关联盟中玩家的数据
	 */
	public void updateGuildMemberObeliskRankInfo(List<Rank.RankInfo> rankList, ObeliskMissionItem missionItem){
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		for (Rank.RankInfo rankInfo:rankList) {
			int rank = rankInfo.getRank();
			List<ItemInfo> reward = cfg.getRankReward(rank);
			if (reward != null){
				String guildId = rankInfo.getId();
				missionItem.setGuildValue(guildId, rank);
				//联盟成员修改数据
				Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
				for (String playerId:memberIds) {
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					if (player != null){
						ObeliskEntity entity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
						entity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
						//排行榜类型,贡献度字段存他联盟排名
						entity.setContribution(rank);
						HawkLog.logPrintln("GuildPowerRankMission updateGuildMemberObeliskRankInfo success playerId:{}, rank:{}", player.getId(), rank);
					}
				}
			}
		}
	}

	@Override
	public Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		Obelisk.PBObelisk.Builder obeliskPb = Obelisk.PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		// 有联盟才有进度 . 这里是联盟进度
		if (player.hasGuild()) {
			int guildNumber = missionItem.getGuildValue(player.getGuildId());
			obeliskPb.setNum(guildNumber);
		}
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.NOTOPEN) {
			// 未开始
			obeliskPb.setState(Obelisk.PBObeliskPlayerState.NOT_OPEN);
		} else if (missionItem.getState() == Obelisk.PBObeliskMissionState.OPEN) {
			// 进行中
			obeliskPb.setState(Obelisk.PBObeliskPlayerState.OPENING);
		} else if (missionItem.getState() == Obelisk.PBObeliskMissionState.CLOSED) {
			ObeliskCfg cfg = missionItem.getObeliskCfg();
			// 任务已结束. 结算玩家数据, 记录贡献度等
			ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
			if (obeliskEntity.getState().getNumber() < Obelisk.PBObeliskPlayerState.FAILED.getNumber()) {
				// 排行榜任务结束时统一结算修改该盟的玩家状态,,,此逻辑还未结算的,认定为当时不在联盟,所以设置失败状态
				obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
				HawkLog.logPrintln("GuildPowerRankMission buildPbToPlayer end playerId:{},state:{}", player.getId(), obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		//榜单类型
		obeliskPb.setRankType(Rank.RankType.ALLIANCE_FIGHT_KEY);
		HPPushRank rankInfo = missionItem.getRankInfo();
		if (rankInfo != null){
			rankInfo = rankInfo.toBuilder().setMyRank(obeliskPb.getContribution()).build();
			obeliskPb.setRankInfo(rankInfo);
		}
		return obeliskPb;
	}

	/**
	 * 排行榜pb
	 */
	public Rank.HPPushRank.Builder genHpPushRankBuilder(List<Rank.RankInfo> rankInfoList){
		Rank.HPPushRank.Builder rankBuilder = Rank.HPPushRank.newBuilder();
		rankBuilder.addAllRankInfo(rankInfoList);
		rankBuilder.setRankType(Rank.RankType.ALLIANCE_FIGHT_KEY);
		rankBuilder.setUpdateTime(HawkTime.getMillisecond());
		//榜单中自己的排行信息
		rankBuilder.setMyRank(0);
		rankBuilder.setMyRankScore(0);
		return rankBuilder;
	}


}
