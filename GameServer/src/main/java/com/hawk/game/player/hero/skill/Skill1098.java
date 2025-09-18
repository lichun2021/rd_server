package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

@HeroSkill(skillID = { 109801, 109802, 109803, 109804, 109805 })
public class Skill1098 extends ISSSHeroSkill {
	/**
	 * - 【万分比】【12211】战技持续期间，自身出征数量最多的突击步兵受到攻击时，伤害减少 +XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 自身出征数量最多的突击步兵
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的突击步兵（兵种类型 = 5）生效（若存在多个突击步兵数量一样且最高，取等级高的）
  - 该作用号为伤害减少效果，与其他作用号累乘计算，即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1_参数2
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即 本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：战技持续时间
        - 配置格式：绝对值（单位：秒）
	 */

	private int effect12211;// 填效果1作用号【12071】
	private double effect12211p1;
	private int effectTime;// 参数2：战技持续时间

	@Override
	public List<PBHeroEffect> effectVal() {
		// proficiencyEffect="1657_0.6_0_720_6000_1_1"
		try {
			// 12071_1.1_300|12072_0.09|12073_0.18_|12074_0.26
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.replace("|", "_").split("_");
			effect12211 = NumberUtils.toInt(ps[0]);
			effect12211p1 = NumberUtils.toDouble(ps[1]);
			effectTime = NumberUtils.toInt(ps[2]);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.effectVal();
	}

	@Override
	public int getShowProficiencyEffect() {
		if (isEffecting()) {
			return effect12211;
		}
		return 0;
	}

	@Override
	public int getProficiencyEffect(EffType effType, EffectParams effParams) {
		if (!isEffecting()) {
			return 0;
		}

		if (effType.getNumber() == effect12211) {
			return (int) Math.ceil(effect12211p1 * getParent().getParent().attrVale(101));
		}

		return 0;

	}

	@Override
	public int effectTime() {
		return effectTime * 1000 + getSoulEffVal(EffType.EFF_12329) * 1000;
	}

}