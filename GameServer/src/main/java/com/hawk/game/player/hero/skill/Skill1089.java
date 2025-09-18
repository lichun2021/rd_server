package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

/**
 *【12091】
- 【万分比】【12091】集结战前，若自身出征防御坦克数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身和友军近战部队超能攻击额外 +XX.XX%（多个埃琳娜存在时，至多有 2 个防御坦克生效）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 若自身出征防御坦克数量超过自身出征部队总数 50%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
  - 若自身出征携带防御坦克数量不低于集结部队总数 5%
    - 数量1 = 某玩家出征携带的防御坦克（兵种类型 = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1_参数2_参数3_参数4_参数5
      - 参数1：作用号系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：自身出征防御坦克相对自身出征部队总数的阈值
        - 配置格式：万分比
      - 参数3：自身出征防御坦克相对集结出征部队总数的阈值
        - 配置格式：万分比
      - 参数4：生效层数；若集结战斗中己方存在多个此作用号效果，限制其生效个数上限（若超出此上限，取作用号数值高的）；即这里只能有 2 个玩家携带的作用号生效
        - 配置格式：绝对值
      - 参数5：战技持续时间
        - 配置格式：绝对值（单位：秒）
【12092】
- 【万分比】【12092】战技持续期间，受到远程庇护效果的部队，生命加成 +XX.XX%（持续 2 回合）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 此作用号为作用号【12085】生效时的远程单位附加的额外效果
    - 注：【12085】作用号有叠加层数效果，但本作用号没有层数效果（只分有没有）
    - 注：此作用号被附加后与【12085】就再无关系，不会随【12085】消失而消失（随自身持续回合数）
  - 生命加成为常规外围属性加成；即实际生命 = 基础生命*（1 + 各类加成 +【本作用值】/10000）
  - 此为英雄战技专属作用号，配置格式如下：
    - 作用号id_参数1_参数2
      - 参数1：作用号系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：持续回合数
        - 配置格式：绝对值
        - 此效果附加后至本回合结束算1回合
 */
@HeroSkill(skillID = { 108901, 108902, 108903, 108904, 108905 })
public class Skill1089 extends ISSSHeroSkill {
	private int effect12091;// 填效果1作用号【12091】
	private double effect12091p1;
	private int effect12091p2;
	private int effect12091p3;
	private int effect12091p4;
	private int effectTime;// 参数2：战技持续时间

	private int effect12092;// 填效果1作用号【12092】
	private double effect12092p1;
	private int effect12092p2;

	@Override
	public List<PBHeroEffect> effectVal() {
		// proficiencyEffect="1657_0.6_0_720_6000_1_1"
		try {
			// 12091_1.1_300|12072_0.09|12073_0.18_|12074_0.26
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.replace("|", "_").split("_");
			effect12091 = NumberUtils.toInt(ps[0]);
			effect12091p1 = NumberUtils.toDouble(ps[1]);
			effect12091p2 = NumberUtils.toInt(ps[2]);
			effect12091p3 = NumberUtils.toInt(ps[3]);
			effect12091p4 = NumberUtils.toInt(ps[4]);
			effectTime = NumberUtils.toInt(ps[5]);

			effect12092 = NumberUtils.toInt(ps[6]);
			effect12092p1 = NumberUtils.toDouble(ps[7]);
			effect12092p2 = NumberUtils.toInt(ps[8]);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.effectVal();
	}

	@Override
	public int getShowProficiencyEffect() {
		if (isEffecting()) {
			return effect12091;
		}
		return 0;
	}

	@Override
	public int getProficiencyEffect(EffType effType, EffectParams effParams) {
		if (!isEffecting()) {
			return 0;
		}

		if (effType.getNumber() == effect12091) {
			return (int) Math.ceil(effect12091p1 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == effect12092) {
			return (int) Math.ceil(effect12092p1 * getParent().getParent().attrVale(101));
		}

		return 0;

	}

	@Override
	public int effectTime() {
		return effectTime * 1000 + getSoulEffVal(EffType.EFF_12326) * 1000;
	}

	public int getEffect12091() {
		return effect12091;
	}

	public void setEffect12091(int effect12091) {
		this.effect12091 = effect12091;
	}

	public double getEffect12091p1() {
		return effect12091p1;
	}

	public void setEffect12091p1(double effect12091p1) {
		this.effect12091p1 = effect12091p1;
	}

	public int getEffect12091p2() {
		return effect12091p2;
	}

	public int getEffect12091p3() {
		return effect12091p3;
	}

	public void setEffect12091p3(int effect12091p3) {
		this.effect12091p3 = effect12091p3;
	}

	public int getEffect12091p4() {
		return effect12091p4;
	}

	public void setEffect12091p4(int effect12091p4) {
		this.effect12091p4 = effect12091p4;
	}

	public int getEffectTime() {
		return effectTime;
	}

	public void setEffectTime(int effectTime) {
		this.effectTime = effectTime;
	}

	public int getEffect12092() {
		return effect12092;
	}

	public void setEffect12092(int effect12092) {
		this.effect12092 = effect12092;
	}

	public double getEffect12092p1() {
		return effect12092p1;
	}

	public void setEffect12092p1(double effect12092p1) {
		this.effect12092p1 = effect12092p1;
	}

	public int getEffect12092p2() {
		return effect12092p2;
	}

	public void setEffect12092p2(int effect12092p2) {
		this.effect12092p2 = effect12092p2;
	}

	public void setEffect12091p2(int effect12091p2) {
		this.effect12091p2 = effect12091p2;
	}

}