package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

/**
 *【12071】
- 【万分比】【12071】战技持续期间，【轮番轰炸】命中步兵单位时，伤害额外 +XX.XX%
  - 该作用号为作用号【轮番轰炸】【12051】生效时的额外效果
  - 步兵类型包含有：狙击兵（兵种类型 = 6）、突击步兵（兵种类型 = 5）
  - 此伤害加成为额外伤害加成，与其他各类伤害加成累加计算；即实际伤害 = 基础伤害*（1 + 各类伤害加成 + 【本作用值】）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1_参数2
      - 参数1：作用号系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：战技持续时间
        - 配置格式：绝对值（单位：秒）

【12072】
- 【万分比】【12072】战技持续期间，【轻装简行】效果变更为: 降低自身【智能护盾】发动概率 +XX.XX%
  - 该作用号生效时，变更作用号【12053】的数值为本作用值
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000

【12073】
- 【万分比】【12073】战技持续期间，【轻装简行】效果变更为: 降低自身【智能护盾】吸收伤害比率 +XX.XX%
  - 该作用号生效时，变更作用号【12054】的数值为本作用值
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000

【12074】
- 【万分比】【12074】战技持续期间，自身所有直升机受到空军部队攻击时，伤害减少 +XX.XX%
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 该作用号对玩家携带的所有直升机（兵种类型 = 4）部队均生效
  - 空军部队类型包含有：直升机（兵种类型 = 4）、轰炸机（兵种类型 = 3）
  - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
 */
@HeroSkill(skillID = { 108801, 108802, 108803, 108804, 108805 })
public class Skill1088 extends ISSSHeroSkill {
	private int effect12071;// 填效果1作用号【12071】
	private double effect12071p1;
	private int effectTime;// 参数2：战技持续时间

	private int effect12072;// 填效果1作用号【12072】
	private double effect12072p1;

	private int effect12073;// 填效果1作用号【12072】
	private double effect12073p1;

	private int effect12074;// 填效果1作用号【12072】
	private double effect12074p1;
	
	private int effect12075;// 填效果1作用号【12072】
	private double effect12075p1;

	@Override
	public List<PBHeroEffect> effectVal() {
		// proficiencyEffect="1657_0.6_0_720_6000_1_1"
		try {
//			12071_1.1_300|12072_0.09|12073_0.18_|12074_0.26
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.replace("|", "_").split("_");
			effect12071 = NumberUtils.toInt(ps[0]);
			effect12071p1 = NumberUtils.toDouble(ps[1]);
			effectTime = NumberUtils.toInt(ps[2]);

			effect12072 = NumberUtils.toInt(ps[3]);
			effect12072p1 = NumberUtils.toDouble(ps[4]);

			effect12073 = NumberUtils.toInt(ps[5]);
			effect12073p1 = NumberUtils.toDouble(ps[6]);

			effect12074 = NumberUtils.toInt(ps[7]);
			effect12074p1 = NumberUtils.toDouble(ps[8]);
			
			effect12075 = NumberUtils.toInt(ps[9]);
			effect12075p1 = NumberUtils.toDouble(ps[10]);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.effectVal();
	}

	@Override
	public int getShowProficiencyEffect() {
		if (isEffecting()) {
			return effect12071;
		}
		return 0;
	}

	@Override
	public int getProficiencyEffect(EffType effType, EffectParams effParams) {
		if (!isEffecting()) {
			return 0;
		}

		if (effType.getNumber() == effect12071) {
			return (int) Math.ceil(effect12071p1 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == effect12072) {
			return (int) Math.ceil(effect12072p1 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == effect12073) {
			return (int) Math.ceil(effect12073p1 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == effect12074) {
			return (int) Math.ceil(effect12074p1 * getParent().getParent().attrVale(101));
		}
		
		if (effType.getNumber() == effect12075) {
			return (int) Math.ceil(effect12075p1 * getParent().getParent().attrVale(101));
		}

		return 0;

	}

	@Override
	public int effectTime() {
		return effectTime * 1000 + getSoulEffVal(EffType.EFF_12325) * 1000;
	}

	public int getEffect12071() {
		return effect12071;
	}

	public void setEffect12071(int effect12071) {
		this.effect12071 = effect12071;
	}

	public double getEffect12071p1() {
		return effect12071p1;
	}

	public void setEffect12071p1(double effect12071p1) {
		this.effect12071p1 = effect12071p1;
	}

	public int getEffectTime() {
		return effectTime;
	}

	public void setEffectTime(int effectTime) {
		this.effectTime = effectTime;
	}

	public int getEffect12072() {
		return effect12072;
	}

	public void setEffect12072(int effect12072) {
		this.effect12072 = effect12072;
	}

	public double getEffect12072p1() {
		return effect12072p1;
	}

	public void setEffect12072p1(double effect12072p1) {
		this.effect12072p1 = effect12072p1;
	}

	public int getEffect12073() {
		return effect12073;
	}

	public void setEffect12073(int effect12073) {
		this.effect12073 = effect12073;
	}

	public double getEffect12073p1() {
		return effect12073p1;
	}

	public void setEffect12073p1(double effect12073p1) {
		this.effect12073p1 = effect12073p1;
	}

	public int getEffect12074() {
		return effect12074;
	}

	public void setEffect12074(int effect12074) {
		this.effect12074 = effect12074;
	}

	public double getEffect12074p1() {
		return effect12074p1;
	}

	public void setEffect12074p1(double effect12074p1) {
		this.effect12074p1 = effect12074p1;
	}

}