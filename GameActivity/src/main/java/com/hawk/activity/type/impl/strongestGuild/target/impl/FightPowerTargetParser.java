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
 * 王者联盟，战力提升解析器
 * @author yang.rao
 *
 */
public class FightPowerTargetParser implements StrongestGuildTargetParser<BattlePointChangeEvent> {

	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.FIGHT_POWER_UP;
	}

	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public long onEvent(StrongestGuildEntity entity, StrongestGuildCfg circularCfg, BattlePointChangeEvent event) {
		
		PowerData powerData = event.getPowerData();
		PowerChangeData changeData = event.getChangeData();
		// 如果玩家当前建筑战力,等于此次建筑变化战力,则此次战力变化事件不进行处理
		if(powerData.getBuildBattlePoint() == changeData.getBuildBattleChange()){
			HawkLog.logPrintln("FightPowerTarget discard powerChange, playerId: {}, changeData: {}", event.getPlayerId(), changeData);
			return 0;
		}
		Integer coefficient = circularCfg.getIndexScoreList().get(0);
		int battlePoint = changeData.getBattleChange(); //变化的战力
		long scoreBef = entity.getScore();
		long score = battlePoint * coefficient; //计算出来理论增加的分数
//		if(score <= 0){
//			logger.error("StrongestGuild power up error, tempPoint:{}, nowPoint:{}", entity.getTempBattlePoint(), event.getPowerData().getBattlePoint());
//			return 0;
//		}
		long cusScore = checkScoreLimit(circularCfg, (scoreBef + score)); //计算出来当前战力提升总分数
		long actualAddScore = cusScore - scoreBef; //计算出来实际增加的分数
		entity.setScore(cusScore);
		return actualAddScore;
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
