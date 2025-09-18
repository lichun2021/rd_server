package com.hawk.game.battle.effect.impl.heroJiaLaNi12498508;

import java.util.Optional;

import com.hawk.game.battle.ICannonSoldier;
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
import com.hawk.game.player.hero.skill.Skill1086;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * Skill1086
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12023)
public class Checker12023 implements IChecker {

	final int heroId = 1086;

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean foot = parames.type == SoldierType.CANNON_SOLDIER_7 || parames.type == SoldierType.CANNON_SOLDIER_8;
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
		Skill1086 skill = (Skill1086) skillOp.get();

		ICannonSoldier soldier = (ICannonSoldier) parames.solider;
		soldier.setEffect12023p2(skill.getEffect12023p2());
		soldier.setEffect12023p3(skill.getEffect12023p3());
		soldier.setEffect12023p4(skill.getEffect12023p4());
		return new CheckerKVResult(effPer, effNum);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
