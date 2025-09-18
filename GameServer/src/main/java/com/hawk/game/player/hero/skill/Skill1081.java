package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

/**
 技能描述
所罗门在战斗时，向自己数量最多的一排士兵额外增加超时空援军，数量为该排士兵出征数量的 xx.xx%.
援军的到来令全军士气提升，全部友军攻击 +xx.xx%，全部友军防御 +xx.xx%。（多个所罗门存在时，攻击和防御效果可叠加，最多生效60%）

45%比例超时空援军伤亡时，不计入战斗力损失。


战技描述
战技持续期间，所罗门利用幻影技术，镜像自己数量最多的一排士兵，数量为该排士兵出征数量的 xx.xx%，原部队攻击后，幻影部队发起一次额外攻击（幻影部队本身没有排数，无法被攻击，在战斗结束后消失）
幻影部队对敌方造成干扰，减少原部队受到伤害的 XX.XX%

禁用英雄ID 1037

作用号需求
【1663】 
效果参考 【1087】效果，与1087属于互斥关系，1663生效的时候1087不生效.
作用号效果：
复制士兵数量的一排士兵并加入该排，效果值为万分比数值，
复制士兵数量  = 【1663】* 士兵出征数量

【1664】
作用号效果 ：所有部队（包含集结中友方士兵）攻击增加
【1664】是万分比数值
部队攻击加成 = 1+其他攻击百分比加成+【1664】
当集结部队中，有多个【1664】存在时，多个【1664】效果叠加，效果上限由const表 effect1664Maxinum控制
填6000 即上限为 60%

【1665】
作用号效果 ：所有部队（包含集结中友方士兵）防御增加
【1665】是万分比数值
部队防御加成 = 1+其他防御百分比加成+【1665】
当集结部队中，有多个【1665】存在时，多个【1665】效果叠加，效果上限由const表 effect1665Maxinum控制
填6000 即上限为 60%

【1666】
效果参考 【1425】效果，与1666属于互斥关系，1666生效的时候1425不生效.
作用号效果 ：【1666】值比例超时空援军伤亡时，不计入战斗力损失。
作用号随着升星提升，与军事值无关，
效果读取const表 effect1666Per 
效果填 1000，1500，2000，3000，4500
即英雄 1星到5星，依次有10%/15%/20%/30%/45%的超时空援军伤亡，不计入战斗力损失。

作用号逻辑【1667】 战技作用号
核心效果：原部队在攻击后，复制一排部队额外再发起1次攻击（复制部队数量 = 原部队出征数量 * XX.XX%；这个数量在战斗中不变化）
注：可以理解为一个无法被选中/攻击的，站在战场外围的幽灵宝宝

p1 填作用号
p2 为联动【1667】复制比例 p2关联三围值成长，为万分比数值
最终复制士兵数量 = 被复制的单位 * （【1667】p2+【1667】p3）

p3 为联动【1667】复制比例 p3不关联三围值成长，为万分比固定填表数值
最终复制士兵数量 = 被复制的单位 * （【1667】p2+【1667】p3）

p4为战技持续时间，单位时间为秒 ，填300 即 5分钟

p5 填作用号【1668】

p6为【1668】原部队伤害减免效果，关联三围值成长，为万分比数值
【套在最外层】实际伤害 = 基础伤害*（1 + 各类伤害加成）*（1 - 某伤害减免）*（1 - 本作用号值/10000）

p7 无开发需求
 */
@HeroSkill(skillID = { 108101, 108102, 108103, 108104, 108105 })
public class Skill1081 extends ISSSHeroSkill {
	private int p1;// 填作用号
	private double p2;
	private double p3;
	private int p4;
	private int p5;
	private double p6;

	@Override
	public List<PBHeroEffect> effectVal() {
		// 1667_0.24_0_360|1668_0.08_0
		try {
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.replace("|", "_").split("_");
			p1 = NumberUtils.toInt(ps[0]);
			p2 = NumberUtils.toDouble(ps[1]);
			p3 = NumberUtils.toDouble(ps[2]);
			p4 = NumberUtils.toInt(ps[3]);
			p5 = NumberUtils.toInt(ps[4]);
			p6 = NumberUtils.toDouble(ps[5]);

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
		if (!isEffecting()) {
			return 0;
		}
		int result = 0;
		if (effType.getNumber() == EffType.HERO_1667_VALUE) {
			result = (int) Math.ceil(p2 * getParent().getParent().attrVale(101) + p3);
		}
		if (effType.getNumber() == EffType.HERO_1668_VALUE) {
			result = (int) Math.ceil(p6 * getParent().getParent().attrVale(101));
		}
		return result;

	}

	@Override
	public int effectTime() {
		return p4 * 1000 + getSoulEffVal(EffType.EFF_12322) * 1000;
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

	public double getP6() {
		return p6;
	}

	public void setP6(int p6) {
		this.p6 = p6;
	}

}