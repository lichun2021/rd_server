package com.hawk.activity.type.impl.strongestGuild.target.impl;

import org.hawk.log.HawkLog;
import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildEntity;
import com.hawk.activity.type.impl.strongestGuild.target.StrongestGuildTargetParser;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;

/***
 * 王者联盟 建筑发展阶段成就解析
 * @author yang.rao
 *
 */
public class BuildingProgressTargetParser implements StrongestGuildTargetParser<BattlePointChangeEvent> {

	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.BUILDING_PROGRESS;
	}

	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public long onEvent(StrongestGuildEntity entity, StrongestGuildCfg circularCfg, BattlePointChangeEvent event) {
		Integer coefficient1 = circularCfg.getIndexScoreList().get(0);
		Integer coefficient2 = circularCfg.getIndexScoreList().get(1);
		PowerChangeData changeData = event.getChangeData();
		PowerData powerData = event.getPowerData();
		int buildBattlePoint = changeData.getBuildBattleChange();
		int techBattlePoint = changeData.getTechBattleChange() + changeData.getPlantScienceBattlePoint();
		if(powerData.getBuildBattlePoint() == changeData.getBuildBattleChange()){
			HawkLog.logPrintln("BuildingProgressTargetParser discard powerChange, playerId: {}, changeData: {}", event.getPlayerId(), changeData);
			return 0;
		}
		long score = buildBattlePoint * coefficient1 
				+ techBattlePoint * coefficient2;
		long scoreBef = entity.getScore();
		long curScore = checkScoreLimit(circularCfg, scoreBef + score);
		long add = curScore  - scoreBef;
		entity.setScore(curScore);
		return add;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, StrongestGuildEntity entity,
			BattlePointChangeEvent event) {
		
	}

	@Override
	public boolean march(StrongestEvent event) {
		return event instanceof BattlePointChangeEvent;
	}
}
