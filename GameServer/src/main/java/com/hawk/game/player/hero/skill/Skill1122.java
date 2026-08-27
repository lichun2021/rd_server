package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

@HeroSkill(skillID = { 112201, 112202, 112203, 112204, 112205 })
public class Skill1122 extends ISSSHeroSkill {
	private int effect;
	private double effectCoefficient;
	private int durationSeconds;

	@Override
	public List<PBHeroEffect> effectVal() {
		try {
			String[] values = getCfg().getProficiencyEffect().replace("|", "_").split("_");
			effect = NumberUtils.toInt(values[0]);
			effectCoefficient = NumberUtils.toDouble(values[1]);
			durationSeconds = NumberUtils.toInt(values[2]);
		} catch (Exception exception) {
			HawkException.catchException(exception);
		}
		return super.effectVal();
	}

	@Override
	public int getShowProficiencyEffect() {
		return isEffecting() ? effect : 0;
	}

	@Override
	public int getProficiencyEffect(EffType effType, EffectParams effParams) {
		if (!isEffecting() || effType.getNumber() != effect) {
			return 0;
		}
		return (int) Math.ceil(effectCoefficient * getParent().getParent().attrVale(101));
	}

	@Override
	public int effectTime() {
		return durationSeconds * 1000 + getSoulEffVal(EffType.HERO_12994) * 1000;
	}
}
