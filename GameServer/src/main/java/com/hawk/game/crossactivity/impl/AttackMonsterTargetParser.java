package com.hawk.game.crossactivity.impl;


import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.game.config.CrossIntegralCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.CrossTargetParser;
import com.hawk.game.crossactivity.CrossTargetType;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.util.GsConst;

public class AttackMonsterTargetParser implements CrossTargetParser<MonsterAttackEvent> {

	@Override
	public CrossTargetType getTargetType() {
		return CrossTargetType.ATTACK_MONSTER;
	}

	@Override
	public boolean calcScore(int termId, CrossIntegralCfg circularCfg, MonsterAttackEvent event) {
		String playerId = event.getPlayerId();
		if (!event.isKill()) {
			return false;
		}
		int monsterLevel = event.getMonsterLevel();
		int monsterType = event.getMosterType();
		if ((monsterType != MonsterType.TYPE_1_VALUE && monsterType != MonsterType.TYPE_2_VALUE) || !event.isKill()) {
			return false;
		}
		Double score = circularCfg.getIndexScoreList().get(monsterLevel - 1);
		if (score == null) {
			return false;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		double addScore = score * event.getAtkTimes();
		// 跨服行为积分翻倍
		if (!GlobalData.getInstance().isLocalServer(player.getMainServerId())) {
			addScore = addScore * circularCfg.getCrossScoreMag() / GsConst.RANDOM_MYRIABIT_BASE;
		}
		int addEff = player.getData().getEffVal(EffType.CROSS_MONSTER_SCORE_ADD);
		addScore = Math.floor(GsConst.EFF_PER * addScore * (GsConst.RANDOM_MYRIABIT_BASE + addEff));
		
		CrossActivityService.getInstance().addScore(player, getTargetType(), (long)addScore);
		return true;
	}

}
