package com.hawk.activity.type.impl.stronestleader.target.impl;

import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularCfg;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetParser;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;

public class FightPowerTargetParser implements StrongestTargetParser<BattlePointChangeEvent> {
	
	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.FIGHT_POWER_UP;
	}
	
	@Override
	public boolean isOverrideScore() {
		return true;
	}

	@Override
	public boolean onEvent(ActivityStrongestLeaderEntity entity, ActivityCircularCfg circularCfg, BattlePointChangeEvent event) {
		Integer coefficient = circularCfg.getIndexScoreList().get(0);
		long battlePoint = event.getPowerData().getBattlePoint();
		long scoreBef = entity.getScore();
		long score = (battlePoint - entity.getInitBattlePoint()) * coefficient;
		entity.setScore(checkScoreLimit(circularCfg, score));

		logger.info("[strongest] FIGHT_POWER_UP target score change, playerId: {}, scoreBef: {}, scoreAft: {}, battlePoint: {}, initBattlePoint: {}", entity.getPlayerId(),
				scoreBef, entity.getScore(), battlePoint, entity.getInitBattlePoint());
		return true;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, ActivityStrongestLeaderEntity entity, BattlePointChangeEvent event) {
		PowerData powerData = dataGeter.getPowerData(playerId);
		if (powerData == null) {
			return;
		}
		long battlePoint = powerData.getBattlePoint();
		// 如果是事件触发,且未记录初始数据,则计算变化前玩家的战力数据进行记录
		if(event != null && entity.getInitBattlePoint() == 0){
			PowerChangeData changeDate = event.getChangeData();
			int  battlePowerChange = changeDate.getBattleChange();
			int buildPowerChange = changeDate.getBuildBattleChange();
			// 如果玩家当前的建筑战力不等于建筑变更战力,则认为此次战力变更有效,以变更前的数据作为初始战力
			if (buildPowerChange != powerData.getBuildBattlePoint()) {
				battlePoint -= battlePowerChange;
			}
			logger.info("[strongest] FIGHT_POWER_UP target init by event. playerId: {}, battlePoint: {}, battlePowerChange: {}", event.getPlayerId(), battlePoint, battlePowerChange);
		}
		entity.setInitBattlePoint(battlePoint);
		
		logger.info("[strongest] FIGHT_POWER_UP target init. playerId: {}, battlePoint: {}", playerId, battlePoint);
	}
}
