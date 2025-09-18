package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

/**
 *#12498508 【0608版本】【新英雄】【SSS】【战车双将】杰拉尼制作 https://meego.feishu.cn/ccredalert/story/detail/12498508
【12021】
- 【万分比】【12021】战技持续期间，近战支援效果中受到超能攻击效果支援的近战友军，超能攻击额外 +XX.XX%
  - 上述作用号为作用号【12004】生效时的友军附加的额外效果
    - 即作用号【12004】生效且选定目标后，若存在作用号【12021】；则该友军单位获得 超能攻击加成 = 【12004】+ 本作用值
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
      - 注：该作用号效果展示于被施加效果的友军部队上（即在集结中，只有友军为队长且被施加该效果时才能展示）
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1_参数2
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：战技持续时间
        - 配置格式：绝对值（单位：秒）

【12022】
- 【万分比】【12022】战技持续期间，远程支援效果中受到超能攻击效果支援的远程友军，超能攻击额外 +XX.XX%
  - 上述作用号为作用号【12014】生效时的友军附加的额外效果
    - 即作用号【12014】生效且选定目标后，若存在作用号【12022】；则该友军单位获得 超能攻击加成 = 【12014】+ 本作用值
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
      - 注：该作用号效果展示于被施加效果的友军部队上（即在集结中，只有友军为队长且被施加该效果时才能展示）
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000

【12023】
- 【万分比】【12023】战技持续期间，战斗中每第 5 回合开始前，战车获得超能护盾效果，本回合内首次受到攻击时，受到伤害减少 +XX.XX%
  - 该作用号与英雄埃托莉亚的英雄战技（作用号【1672】）效果类似，但不抵挡死亡效果
  - 该作用号对玩家携带的所有战车（兵种类型 = 7或8）部队均生效
  - 仅在受到以玩家战车为目标的攻击且命中时生效，按被命中次数减少所受伤害，并扣除对应可抵挡次数
    - 非攻击行为（如英雄埃托莉亚的专属芯片-超能步兵改装造成的持续伤害无法触发此效果）
  - 每第 5 回合
    - 战斗中每第 5 的倍数的回合开始前，获得此效果
  - 持续 1 回合（本回合被附加到下回合开始算 1 回合）
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1_参数2_参数3_参数4
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：间隔回合数
        - 配置格式：绝对值
      - 参数3：持续回合数
        - 配置格式：绝对值
      - 参数4：可抵挡次数
        - 配置格式：绝对值
 */
@HeroSkill(skillID = { 108601, 108602, 108603, 108604, 108605 })
public class Skill1086 extends ISSSHeroSkill {
	private int effect12021;// 填效果1作用号【1671】
	private double effect12021p1;
	private int effectTime;// 参数2：战技持续时间

	private int effect12022;// 填效果1作用号【1671】
	private double effect12022p1;

	private int effect12023;// 填效果1作用号【1671】
	private double effect12023p1;
	private int effect12023p2;
	private int effect12023p3;
	private int effect12023p4;

	@Override
	public List<PBHeroEffect> effectVal() {
		// proficiencyEffect="1657_0.6_0_720_6000_1_1"
		try {
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.replace("|", "_").split("_");
			effect12021 = NumberUtils.toInt(ps[0]);
			effect12021p1 = NumberUtils.toDouble(ps[1]);
			effectTime = NumberUtils.toInt(ps[2]);

			effect12022 = NumberUtils.toInt(ps[3]);
			effect12022p1 = NumberUtils.toDouble(ps[4]);

			effect12023 = NumberUtils.toInt(ps[5]);
			effect12023p1 = NumberUtils.toDouble(ps[6]);
			effect12023p2 = NumberUtils.toInt(ps[7]);
			effect12023p3 = NumberUtils.toInt(ps[8]);
			effect12023p4 = NumberUtils.toInt(ps[9]);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.effectVal();
	}

	@Override
	public int getShowProficiencyEffect() {
		if (isEffecting()) {
			return effect12021;
		}
		return 0;
	}

	@Override
	public int getProficiencyEffect(EffType effType, EffectParams effParams) {
		if (!isEffecting()) {
			return 0;
		}

		if (effType.getNumber() == effect12021) {
			return (int) Math.ceil(effect12021p1 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == effect12022) {
			return (int) Math.ceil(effect12022p1 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == effect12023) {
			return (int) Math.ceil(effect12023p1 * getParent().getParent().attrVale(101));
		}

		return 0;

	}

	@Override
	public int effectTime() {
		return effectTime * 1000 + getSoulEffVal(EffType.EFF_12324) * 1000;
	}

	public int getP1() {
		return effect12021;
	}

	public void setP1(int p1) {
		this.effect12021 = p1;
	}

	public int getEffect12021() {
		return effect12021;
	}

	public void setEffect12021(int effect12021) {
		this.effect12021 = effect12021;
	}

	public double getEffect12021p1() {
		return effect12021p1;
	}

	public void setEffect12021p1(double effect12021p1) {
		this.effect12021p1 = effect12021p1;
	}

	public int getEffectTime() {
		return effectTime;
	}

	public void setEffectTime(int effectTime) {
		this.effectTime = effectTime;
	}

	public int getEffect12022() {
		return effect12022;
	}

	public void setEffect12022(int effect12022) {
		this.effect12022 = effect12022;
	}

	public double getEffect12022p1() {
		return effect12022p1;
	}

	public void setEffect12022p1(double effect12022p1) {
		this.effect12022p1 = effect12022p1;
	}

	public int getEffect12023() {
		return effect12023;
	}

	public void setEffect12023(int effect12023) {
		this.effect12023 = effect12023;
	}

	public double getEffect12023p1() {
		return effect12023p1;
	}

	public void setEffect12023p1(double effect12023p1) {
		this.effect12023p1 = effect12023p1;
	}

	public int getEffect12023p2() {
		return effect12023p2;
	}

	public void setEffect12023p2(int effect12023p2) {
		this.effect12023p2 = effect12023p2;
	}

	public int getEffect12023p3() {
		return effect12023p3;
	}

	public void setEffect12023p3(int effect12023p3) {
		this.effect12023p3 = effect12023p3;
	}

	public int getEffect12023p4() {
		return effect12023p4;
	}

	public void setEffect12023p4(int effect12023p4) {
		this.effect12023p4 = effect12023p4;
	}

}