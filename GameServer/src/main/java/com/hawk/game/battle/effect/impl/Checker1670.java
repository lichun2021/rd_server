package com.hawk.game.battle.effect.impl;

import java.util.Optional;

import com.hawk.game.battle.BattleSoldier_6;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.hero.skill.Skill1083;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1670)
public class Checker1670 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
			if (effPer > 0) {
				int p7 = getP7(parames);
				((BattleSoldier_6) parames.solider).setSkill1083p7(p7);
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}
	final int heroId = 1083;
	private int getP7(CheckerParames parames) {
		Optional<PlayerHero> kaienOp = parames.unity.getPlayer().getHeroByCfgId(heroId);
		if (!kaienOp.isPresent()) {
			return 2;
		}
		PlayerHero kaiEn = kaienOp.get();
		Optional<IHeroSkill> skillOp = kaiEn.getProficiencySkill();
		if (!skillOp.isPresent()) {
			return 2;
		}
		Skill1083 skill = (Skill1083) skillOp.get();
		return skill.getP7();
	}
}
