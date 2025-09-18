package com.hawk.game.battle.effect.impl;

import java.util.Optional;

import com.hawk.game.battle.IFootSoldier;
import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.hero.skill.Skill1083;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * Skill1083
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1671)
public class Checker1671 implements IChecker {

	final int heroId = 1083;

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean foot = parames.type == SoldierType.FOOT_SOLDIER_5 || parames.type == SoldierType.FOOT_SOLDIER_6;
		if (!foot || parames.troopEffType == WarEff.NO_EFF || parames.unity.getEffVal(effType()) <= 0) {
			return CheckerKVResult.DefaultVal;
		}
		effPer = parames.unity.getEffVal(effType());
		Optional<PlayerHero> kaienOp = parames.unity.getPlayer().getHeroByCfgId(heroId);
		if (!kaienOp.isPresent()) {
			return CheckerKVResult.DefaultVal;
		}
		PlayerHero kaiEn = kaienOp.get();
		Optional<IHeroSkill> skillOp = kaiEn.getProficiencySkill();
		if (!skillOp.isPresent()) {
			return CheckerKVResult.DefaultVal;
		}
		Skill1083 skill = (Skill1083) skillOp.get();

		IFootSoldier soldier = (IFootSoldier) parames.solider;
		soldier.setSkill1083p3((int) skill.getP3());
		return new CheckerKVResult(effPer, effNum);
	}
}
