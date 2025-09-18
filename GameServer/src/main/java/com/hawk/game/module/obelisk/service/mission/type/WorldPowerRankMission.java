package com.hawk.game.module.obelisk.service.mission.type;

import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.entity.ObeliskEntity;
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
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.event.EventPowerCreate;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 个人进入个人战力排行榜前N
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.PLAYER_POWER_RANK)
public class WorldPowerRankMission implements IObeliskMission{

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
			boolean start = startMission(missionItem);
			if (start) {
				// 如果任务成功开启
				onMissionStart(missionItem);
				ObeliskService.getInstance().noticeNewPoint();
			}
		}

		if (missionItem.getState() == Obelisk.PBObeliskMissionState.OPEN) {
			// 如果是榜类的, 这里要检查榜单, 并更新数据 initMission cumulativeMission refreshMission 三个方法则不需要实现
			boolean close = closeMission(missionItem);
			if (close) {
				ObeliskService.getInstance().noticeNewPoint();
				//缓存榜单
				List<Rank.RankInfo> rankList = RankService.getInstance().getRankCache(Rank.RankType.PLAYER_FIGHT_RANK);
				missionItem.setRankInfo(genHpPushRankBuilder(rankList).build());
				HawkLog.logPrintln("WorldPowerRankMission closeMission success rankData is save cache rankSize:{}", rankList.size());
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
		HPPushRank rankInfo = missionItem.getRankInfo();
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
			// 玩家还没有结算过
			if (obeliskEntity.getState().getNumber() < Obelisk.PBObeliskPlayerState.FAILED.getNumber()) {
				// 玩家还没有结算过, 针对超时过期的任务进行特殊处理
				if (checkOverTimeComplete(missionItem,false)){
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				}else {
					if (rankInfo != null){
						List<Rank.RankInfo> rankInfoList = rankInfo.getRankInfoList();
						Optional<Rank.RankInfo> optional = rankInfoList.stream().filter(data -> data.getId().equals(player.getId())).findAny();
						if (optional.isPresent()){
							Rank.RankInfo myRankInfo = optional.get();
							//排行榜类型,贡献度字段存他自己排名
							obeliskEntity.setContribution(myRankInfo.getRank());
							obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
						}
						else {
							obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
						}
					}else {
						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
					}
					HawkLog.logPrintln("WorldPowerRankMission buildPbToPlayer end change state playerId:{}, hasRank:{}", player.getId(), (rankInfo != null),obeliskEntity.getState().getNumber());

				}

			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		//榜单类型
		obeliskPb.setRankType(Rank.RankType.PLAYER_FIGHT_RANK);
		if (rankInfo != null){
			rankInfo = rankInfo.toBuilder().setMyRank(obeliskPb.getContribution()).build();
			obeliskPb.setRankInfo(rankInfo);
		}
		return obeliskPb;
	}

	/**
	 * 排行榜pb
	 * @param rankInfoList
	 * @return
	 */
	public Rank.HPPushRank.Builder genHpPushRankBuilder(List<Rank.RankInfo> rankInfoList){
		Rank.HPPushRank.Builder rankBuilder = Rank.HPPushRank.newBuilder();
		rankBuilder.addAllRankInfo(rankInfoList);
		rankBuilder.setRankType(Rank.RankType.PLAYER_FIGHT_RANK);
		rankBuilder.setUpdateTime(HawkTime.getMillisecond());
		//榜单中自己的排行信息
		rankBuilder.setMyRank(0);
		rankBuilder.setMyRankScore(0);
		return rankBuilder;
	}

}
