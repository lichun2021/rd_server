package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

/**
 *【0323版本】【新英雄】荣耀SSS 军事步兵英雄 阿尔托莉雅
业务流程
功能需求-主工作流程
业务描述
技能描述1：提升180%步兵超能攻击+90%步兵伤害。 （已有作用号直接配置）
技能描述2：
突击步枪改造
突击步兵（兵种ID:5）攻击时，有3x%/2x%/x%的概率对非当前目标再攻击一次（近战优先），每触发一次会降低一定概率x%
effectid：1669， 每次攻击降低概率为 1/3*effectid：1669
每次攻击时进行一次判定，且本次的额外攻击不会再进行能否连击的判定（即不会触发额外攻击）。
优先攻击群组1：兵种ID:2 主战坦克。 兵种ID3:轰炸机。兵种ID8:采矿车。
优先攻击群组2：兵种ID:4 直升机。 兵种ID5:突击步兵。兵种ID6:狙击兵。兵种ID:攻城车。
对方有群组1的兵种时，在所有群组1内随机选择部队进行攻击。当没有群组1时，在群组2中随机选择部队进行攻击。当群组1和2都不存在时，攻击防御坦克 兵种ID：1
技能描述3：
狙击步枪改造
狙击步兵（兵种ID:6）攻击时使敌人受到持续伤害，每回合承受本次伤害的xx%（effectid:1670），持续2回合，对单一目标可以叠加。
说明：在第一回合攻击的敌人，第2/3回合会造成持续伤害，伤害等于第一回合造成实际伤害的xx%（effectid:1670）

战技：效果1
必中：步兵（兵种ID:5和6）有xx%（effectid:1671）攻击进入必中要害模式：本次造成（1+P3百分比）伤害，且无视护盾防御（302技能）且无视本英雄的护盾(108301-108305）技能效果
战技：效果2
必防（）：每5回合，在回合开始前（相当于回合准备阶段），给自己的步兵部队（兵种ID:5和6套一个3层的超能护盾，减少xx%（effectid:1672）的承受伤害，每受到一次伤害，减少一层护盾，并且降低1/3*xx%（effectid：1672）的减伤值。 回合结束护盾消失，不会带入下一回合。
特殊逻辑：本回合内受到来自昆那技能造成的死亡数量必定减少xx%（effectid：1672）。

p1 填效果1作用号【1671】
p2为效果1发动概率，关联三围值成长，为万分比数值
p3 额外伤害百分比，如果填10000，则造成完全翻倍的伤害
p4为战技持续时间，单位时间为秒 ，填300 即 5分钟
p5 填效果2作用号【1672】
p6为【1672】原部队伤害减免效果，关联三围值成长，为万分比数值
【套在最外层】实际伤害 = 基础伤害*（1 + 各类伤害加成）*（1 - 某伤害减免）*（1 - 本作用号值/10000）
p7 空闲
注：【1671】和【1672】均展示于战报中，不合并至精简战报的属性
https://meego.feishu.cn/ccredalert/story/detail/9756986
 */
@HeroSkill(skillID = { 108301, 108302, 108303, 108304, 108305 })
public class Skill1083 extends ISSSHeroSkill {
	private int p1;// 填效果1作用号【1671】
	private double p2;// 为效果1发动概率，关联三围值成长，为万分比数值
	private double p3;// 伤害效果
	private int p4;// 为战技持续时间，单位时间为秒 ，填300 即 5分钟
	private int p5;// 填效果2作用号【1672】
	private double p6;// 为【1672】原部队伤害减免效果，关联三围值成长，为万分比数值
	// 【套在最外层】实际伤害 = 基础伤害*（1 + 各类伤害加成）*（1 - 某伤害减免）*（1 - 本作用号值/10000）
	private int p7;// 空闲

	@Override
	public List<PBHeroEffect> effectVal() {
		// proficiencyEffect="1657_0.6_0_720_6000_1_1"
		try {
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.replace("|", "_").split("_");
			p1 = NumberUtils.toInt(ps[0]);
			p2 = NumberUtils.toDouble(ps[1]);
			p3 = NumberUtils.toDouble(ps[2]);
			p4 = NumberUtils.toInt(ps[3]);
			p5 = NumberUtils.toInt(ps[4]);
			p6 = NumberUtils.toDouble(ps[5]);
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
		if (!isEffecting()) {
			return 0;
		}

		if (effType.getNumber() == p1) {
			return (int) Math.ceil(p2 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == p5) {
			return (int) Math.ceil(p6 * getParent().getParent().attrVale(101));
		}

		return 0;

		// int result = (int) Math.ceil(p2 * getParent().getParent().attrVale(101) + p3);
		// return result;

	}

	@Override
	public int effectTime() {
		return p4 * 1000 + getSoulEffVal(EffType.EFF_12323) * 1000;
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

	public void setP6(double p6) {
		this.p6 = p6;
	}

	public int getP7() {
		return p7;
	}

	public void setP7(int p7) {
		this.p7 = p7;
	}

}