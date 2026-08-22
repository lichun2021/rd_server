package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.battle.effect.impl.hero1120.Hero1120Rules;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

@HeroSkill(skillID = { 112001, 112002, 112003, 112004, 112005 })
public class Skill1120 extends ISSSHeroSkill {
	private int effect;
	private double effectp1;
	private int effectTime;

	@Override
	public List<PBHeroEffect> effectVal() {
		try {
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.replace("|", "_").split("_");
			effect = NumberUtils.toInt(ps[0]);
			effectp1 = NumberUtils.toDouble(ps[1]);
			effectTime = NumberUtils.toInt(ps[2]);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.effectVal();
	}

	@Override
	public int getShowProficiencyEffect() {
		return isEffecting() ? effect : 0;
	}

	@Override
	public int getProficiencyEffect(EffType effType, EffectParams effParams) {
		if (!isEffecting()) {
			return 0;
		}
		if (effType.getNumber() == effect) {
			return (int) Math.ceil(effectp1 * getParent().getParent().attrVale(101));
		}
		return 0;
	}

	@Override
	public int effectTime() {
		return Hero1120Rules.effectDurationMillis(effectTime, getSoulEffVal(EffType.HERO_12854));
	}
}
