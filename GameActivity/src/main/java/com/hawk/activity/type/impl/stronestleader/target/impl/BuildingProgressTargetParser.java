package com.hawk.activity.type.impl.stronestleader.target.impl;

import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularCfg;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetParser;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;

public class BuildingProgressTargetParser implements StrongestTargetParser<BattlePointChangeEvent> {

	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.BUILDING_PROGRESS;
	}

	@Override
	public boolean isOverrideScore() {
		return true;
	}

	@Override
	public boolean onEvent(ActivityStrongestLeaderEntity entity, ActivityCircularCfg circularCfg, BattlePointChangeEvent event) {
		Integer coefficient1 = circularCfg.getIndexScoreList().get(0);
		Integer coefficient2 = circularCfg.getIndexScoreList().get(1);
		int buildBattlePoint = event.getPowerData().getBuildBattlePoint();
		int techBattlePoint = event.getPowerData().getTechBattlePoint() + event.getPowerData().getPlantScienceBattlePoint();
		long score = (buildBattlePoint - entity.getBuildBattlePoint()) * coefficient1 + (techBattlePoint - entity.getTechBattlePoint()) * coefficient2;
		if (entity.getScore() == score) {
			return false;
		}
		long scoreBef = entity.getScore();
		entity.setScore(checkScoreLimit(circularCfg, score));
		logger.info(
				"[strongest] BUILDING_PROGRESS target score change, playerId: {}, scoreBef: {}, scoreAft: {}, buildBattlePoint: {}, initBuildBattlePoint: {}, initBattlePoint: {}",
				entity.getPlayerId(), scoreBef, entity.getScore(), buildBattlePoint, entity.getInitBattlePoint(), techBattlePoint, entity.getTechBattlePoint());
		return true;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, ActivityStrongestLeaderEntity entity, BattlePointChangeEvent event) {
		PowerData powerData = dataGeter.getPowerData(playerId);
		if (powerData == null) {
			return;
		}
		int buildBattlePoint = powerData.getBuildBattlePoint();
		int techBattlePoint = powerData.getTechBattlePoint() + powerData.getPlantScienceBattlePoint();
		// 如果是事件触发,且未记录初始数据,则计算变化前玩家的战力数据进行记录
		if (event != null && entity.getBuildBattlePoint() == 0 && entity.getTechBattlePoint() == 0) {
			PowerChangeData changeDate = event.getChangeData();
			int buildPowerChange = changeDate.getBuildBattleChange();
			int techPowerChange = changeDate.getTechBattleChange() + changeDate.getPlantScienceBattlePoint();
			// 如果玩家的当前建筑战力,不等于建筑变更战力,则认为此次战力变更有效,以变更前的数据作为初始战力
			if (buildPowerChange != buildBattlePoint) {
				buildBattlePoint -= buildPowerChange;
				techBattlePoint -= techPowerChange;
			}
			logger.info("[strongest] BUILDING_PROGRESS target init by event. playerId: {}, buildBattlePoint: {}, buildPowerChange: {}, techBattlePoint: {}, techPowerChange: {}",
					playerId, buildBattlePoint, buildPowerChange, techBattlePoint, techPowerChange);
		}
		entity.setBuildBattlePoint(buildBattlePoint);
		entity.setTechBattlePoint(techBattlePoint);

		logger.info("[strongest] BUILDING_PROGRESS target init, playerId: {}, buildBattlePoint: {}, techBattlePoint: {}", entity.getPlayerId(), buildBattlePoint, techBattlePoint);
	}
}
