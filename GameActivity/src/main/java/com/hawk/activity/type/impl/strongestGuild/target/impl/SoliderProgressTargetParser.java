package com.hawk.activity.type.impl.strongestGuild.target.impl;

import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildEntity;
import com.hawk.activity.type.impl.strongestGuild.target.StrongestGuildTargetParser;

/***
 * 王者联盟， 士兵训练解析器
 * @author yang.rao
 *
 */
public class SoliderProgressTargetParser implements StrongestGuildTargetParser<TrainSoldierCompleteEvent> {

	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.SOLIDER_PROGRESS;
	}

	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public long onEvent(StrongestGuildEntity entity, StrongestGuildCfg circularCfg,
			TrainSoldierCompleteEvent event) {
		int level = event.getLevel();
		int count = event.getNum();
		//杀死一个兵的积分
		Integer killScoree = circularCfg.getIndexScoreList().get(level - 1);
		if (killScoree == null) {
			return 0;
		}
		int addScore = killScoree * count; //理论增加的分数
		long scoreBef = entity.getScore(); //
		long scoreAft = checkScoreLimit(circularCfg, entity.getScore() + addScore);
		entity.setScore(scoreAft);
		long actualAddScore = scoreAft - scoreBef; //实际增加的分数
		return actualAddScore;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, StrongestGuildEntity entity,
			TrainSoldierCompleteEvent event) {
	}

	@Override
	public boolean march(StrongestEvent event) {
		return event instanceof TrainSoldierCompleteEvent;
	}
}
