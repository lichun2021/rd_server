package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

/**
 * 作用号逻辑【1654】 
每 3 回合释放一次，士兵类型 = 2 的 主战坦克，攻击力提升x，持续1回合
【1654】是万分比数值
主战坦克攻击力 = 基础攻击力 * （1+其他攻击百分比数值+【1654】）
 
作用号逻辑【1655】 
每 3 回合释放一次，士兵类型 = 2 的 主战坦克，暴击伤害提升x，持续1回合
【1655】是万分比数值
最终暴击伤害 = （基础暴击伤害倍率 +其他暴击伤害倍率+【1655】）
 
作用号逻辑【1656】 
每 3 回合释放一次 ，由士兵类型 = 2 的 主战坦克 士兵数量最多的一排，释放额外攻击，攻击目标为随机1个兵种类型 = 3或4的目标，最终伤害 = 基础伤害 * （1+其他伤害加成）*【1656】，被攻击的目标有概率被附负面状态，概率随着专属技能等级提升而提升，基础概率读取const表effect1656per,
例子 effect1656per = 1000/1500/2200/3000/4000
代表1级-5级专属技能负面状态命中该目标基础概率为 10%/15%/22%/30%/40%
 
实际概率 = 基础概率 * max（释放的主战坦克士兵数量/目标空军士兵数量，1）
 
命中 兵种类型 = 3 的负面效果，负面状态持续期间，禁止该目标，释放兵种技能302 （智能护盾），并清除作用号【1544】，智能护盾吸收反击伤害累计的值，禁止【1544】继续累计值
命中 兵种类型 = 4 的负面效果，负面状态持续期间，直升机基础防御值，变更为0
 
作用号逻辑【1657】 战技作用号
p1 填作用号
p2 为联动【1656】伤害效果 p2关联三围值成长，为万分比数值
最终伤害 = 基础伤害 * （1+其他伤害加成）*（【1656】+【1657】p2）
 
p3 为联动【1656】伤害效果 p3不关联三围值成长，为万分比固定填表数值
最终伤害 = 基础伤害 * （1+其他伤害加成）*（【1656】+【1657】p2+【1657】p3）
 
p4为战技持续时间，单位时间为秒 ，填300 即 5分钟
 
p5 为联动【1656】负面效果触发基础概率追加值 填 500 即额外多5%概率
    最终基础概率 = effect1656per + p5
 
p6为联动【1656】，【1656】基础攻击目标为随机（1+p6）个兵种类型 = 3或4的目标 
 
p7为联动【1654】【1655】【1656】释放频率减少，填1，即减少1个回合 
【1654】【1655】【1656】变为 每 （3-p7)回合释放
 */
@HeroSkill(skillID = { 107801, 107802, 107803, 107804, 107805 })
public class Skill1078 extends ISSSHeroSkill {
	private int p1;// 填作用号
	private double p2;// 为联动【1656】伤害效果 p2关联三围值成长，为万分比数值
	// 最终伤害 = 基础伤害 * （1+其他伤害加成）*（【1656】+【1657】p2）

	private double p3;// 为联动【1656】伤害效果 p3不关联三围值成长，为万分比固定填表数值
	// 最终伤害 = 基础伤害 * （1+其他伤害加成）*（【1656】+【1657】p2+【1657】p3）

	private int p4;// 为战技持续时间，单位时间为秒 ，填300 即 5分钟

	private int p5;// 为联动【1656】负面效果触发基础概率追加值 填 500 即额外多5%概率
	// 最终基础概率 = effect1656per + p5

	private int p6;// 为联动【1656】，【1656】基础攻击目标为随机（1+p6）个兵种类型 = 3或4的目标

	private int p7;// 为联动【1654】【1655】【1656】释放频率减少，填1，即减少1个回合
	// 【1654】【1655】【1656】变为 每 （3-p7)回合释放

	@Override
	public List<PBHeroEffect> effectVal() {
		// proficiencyEffect="1657_0.6_0_720_6000_1_1"
		try {
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.split("_");
			p1 = NumberUtils.toInt(ps[0]);
			p2 = NumberUtils.toDouble(ps[1]);
			p3 = NumberUtils.toDouble(ps[2]);
			p4 = NumberUtils.toInt(ps[3]);
			p5 = NumberUtils.toInt(ps[4]);
			p6 = NumberUtils.toInt(ps[5]);
			p7 = NumberUtils.toInt(ps[6]);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.effectVal();
	}

	@Override
	public int getShowProficiencyEffect() {
		if (isEffecting()) {
			return p1;
		}
		return 0;
	}

	@Override
	public int getProficiencyEffect(EffType effType, EffectParams effParams) {
		if (effType.getNumber() != p1) {
			return 0;
		}

		if (!isEffecting()) {
			return 0;
		}
		int result = (int) Math.ceil(p2 * getParent().getParent().attrVale(101) + p3);
		return result;

	}

	@Override
	public int effectTime() {
		return p4 * 1000 + getSoulEffVal(EffType.EFF_12321) * 1000;
	}

	public int getP1() {
		return p1;
	}

	public void setP1(int p1) {
		this.p1 = p1;
	}

	public double getP2() {
		return p2;
	}

	public void setP2(double p2) {
		this.p2 = p2;
	}

	public double getP3() {
		return p3;
	}

	public void setP3(double p3) {
		this.p3 = p3;
	}

	public int getP4() {
		return p4;
	}

	public void setP4(int p4) {
		this.p4 = p4;
	}

	public int getP5() {
		return p5;
	}

	public void setP5(int p5) {
		this.p5 = p5;
	}

	public int getP6() {
		return p6;
	}

	public void setP6(int p6) {
		this.p6 = p6;
	}

	public int getP7() {
		return p7;
	}

	public void setP7(int p7) {
		this.p7 = p7;
	}

}