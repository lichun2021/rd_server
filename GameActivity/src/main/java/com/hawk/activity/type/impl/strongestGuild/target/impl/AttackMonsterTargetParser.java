package com.hawk.activity.type.impl.strongestGuild.target.impl;

import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildEntity;
import com.hawk.activity.type.impl.strongestGuild.target.StrongestGuildTargetParser;
import com.hawk.game.protocol.World.MonsterType;

/***
 * 王者联盟打野阶段解析器
 * @author yang.rao
 *
 */
public class AttackMonsterTargetParser implements StrongestGuildTargetParser<MonsterAttackEvent> {

	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.ATTACK_MONSTER;
	}

	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public long onEvent(StrongestGuildEntity entity, StrongestGuildCfg circularCfg, MonsterAttackEvent event) {
		int monsterLevel = event.getMonsterLevel();
		int monsterType = event.getMosterType();
		if ((monsterType != MonsterType.TYPE_1_VALUE && monsterType != MonsterType.TYPE_2_VALUE) || !event.isKill()) {
			return 0;
		}
		//这个score就是击杀野怪的分数
		Integer killScore = circularCfg.getIndexScoreList().get(monsterLevel - 1);
		if (killScore == null) {
			return 0;
		}
		
		killScore = killScore * event.getAtkTimes();
		long scoreBef = entity.getScore(); //当前积分
		long curScore = checkScoreLimit(circularCfg, scoreBef + killScore);
		entity.setScore(curScore);
		long addScore = curScore - scoreBef;
		return addScore;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, StrongestGuildEntity entity,
			MonsterAttackEvent event) {
	}

	@Override
	public boolean march(StrongestEvent event) {
		return event instanceof MonsterAttackEvent;
	}
}
