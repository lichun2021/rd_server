package com.hawk.game.module.obelisk.service.mission.type;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.entity.ObeliskEntity;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.obelisk.service.mission.ObeliskMission;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Obelisk;
import com.hawk.game.protocol.Obelisk.PBObelisk.Builder;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.event.EventBuildingUpgrade;

/**
 * 全服X名指挥官大本达到N级
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.WORLD_PLAYER_BUILD_LEVEL)
public class WorldBuildLevelMission implements IObeliskMission {

	@Override
	public Class<? extends MissionEvent> getEventClassType() {
		return EventBuildingUpgrade.class;
	}

	@Override
	public ObeliskMissionType getObeliskMissionType() {
		return IObeliskMission.super.getObeliskMissionType();
	}
	
	@Override
	public void onMissionStart(ObeliskMissionItem missionItem) {
		boolean isCheck = checkOverTimeComplete(missionItem, true);
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		// 校验是否直接完成
		if (isCheck) {
			// 修改状态
			missionItem.addValue(cfg.getCount());
			missionItem.setState(Obelisk.PBObeliskMissionState.CLOSED);
			return;
		}
	}

	@Override
	public void initMission(ObeliskMissionItem missionItem) {
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		int conditionLv = cfg.getPara1();
		
		ConfigIterator<BuildingCfg> it = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
		BuildingCfg buildCfg = it.stream()
			.filter(bcfg -> bcfg.getBuildType() == Const.BuildingType.CONSTRUCTION_FACTORY_VALUE)
			.filter(bcfg -> bcfg.getLevel() == conditionLv)
			.findFirst()
			.orElse(null);
		
		int count = 0;
		try {
			if (Objects.nonNull(buildCfg)) {
				String sql = String.format("select count(*) from building where type = %s AND buildingCfgId >= %s limit 1", buildCfg.getBuildType(), buildCfg.getId());
				List<BigInteger> resultList = HawkDBManager.getInstance().executeQuery(sql, null);
				BigInteger countStr = resultList.get(0);
				count = countStr.intValue();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		missionItem.addValue(count);

		HawkLog.logPrintln("WorldBuildLevelMission initMission cfgId:{}, value:{}", cfg.getId(), count);
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
			boolean close = closeMission(missionItem);
			if (close) {
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
	}

	@Override
	public void refreshMission(Player player, ObeliskMissionItem missionItem, MissionEvent missionEvent) {
		EventBuildingUpgrade eventBuildingUpgrade = (EventBuildingUpgrade) missionEvent;
		int buildCfgId = eventBuildingUpgrade.getBuildingCfgId();
		int afterLv = eventBuildingUpgrade.getAfterLevel();

		ObeliskCfg cfg = missionItem.getObeliskCfg();
		// 不满足条件不计数
		int conditionLv = cfg.getPara1();
		if (conditionLv != afterLv) {
			return;
		}
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildCfgId);
		// 获取建筑基础类型
		int buildType = buildingCfg.getBuildType();
		if (buildType != BuildingType.CONSTRUCTION_FACTORY_VALUE) {
			return;
		}
		// 增加数量
		missionItem.addValue(1);

		HawkLog.logPrintln("WorldBuildLevelMission refreshMission playerId:{}, afterLv:{}", player.getId(), afterLv);
	}

	@Override
	public Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		Obelisk.PBObelisk.Builder obeliskPb = Obelisk.PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		int num = missionItem.getNum();
		obeliskPb.setNum(num);
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
				if (checkOverTimeComplete(missionItem, false)) {
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				} else {
					// 之前计算出的进度值
					int number = obeliskPb.getNum();
					// 完成 后面可以领奖励
					if (number >= cfg.getCount()) {
						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
					} else {
						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
					}
				}
				HawkLog.logPrintln("WorldBuildLevelMission buildPbToPlayer end change state playerId:{},number:{},state:{}", player.getId(), obeliskPb.getNum(),
						obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		return obeliskPb;
	}
}
