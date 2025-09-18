package com.hawk.game.player.hero.skill;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.util.EffectParams;

@HeroSkill(skillID = { 109301, 109302, 109303, 109304, 109305 })
public class Skill1093 extends ISSSHeroSkill {
	/**
	 * 【12141】
	- 【万分比】【12141】战技持续期间，主战坦克受到攻击时，伤害减少 +XX.XX%
	  - 战报相关
	- 于战报中展示
	- 不合并至精简战报中
	  - 此伤害减少效果算式与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
	  - 此为英雄战技专属作用号，配置格式如下
	- 作用号id_参数1_参数2
	  - 参数1：作用号系数
	    - 配置格式：浮点数
	    - 即本作用值 = 英雄军事值 * 参数1/10000
	  - 参数2：战技持续时间
	    - 配置格式：绝对值（单位：秒）
	
	【12142】
	- 【万分比】【12142】战技持续期间，触发磁暴干扰时，暴击几率 +XX.XX%，
	  - 战报相关
	- 于战报中隐藏
	- 不合并至精简战报中
	  - 此作用号绑定磁暴干扰作用号【12133】，仅在【12133】的当次追加攻击命中目标时生效
	  - 此为英雄战技专属作用号，配置格式如下：
	- 作用号id_参数1
	  - 参数1：作用号系数
	    - 配置格式：浮点数
	    - 即本作用值 = 英雄军事值 * 参数1/10000
	
	【12143】
	- 【万分比】【12143】战技持续期间，触发磁暴干扰时，并额外降低目标 +XX.XX% 的轰炸概率（该效果不可叠加，持续 2 回合）
	  - 战报相关
	- 于战报中隐藏
	- 不合并至精简战报中
	  - 此作用号绑定磁暴干扰作用号【12133】，仅在【12133】的当次追加攻击命中目标后生效
	  - 此伤害加成降低效果为debuff效果，记在被攻击方身上
	  - 轰炸概率
	- 对直升机兵种，是指其兵种技能【黑鹰轰炸】【id = 403】的发动概率
	  - 即【黑鹰轰炸】实际概率 = 基础概率 + 其他加成 - 【本作用值】
	    - 基础概率读取const表，字段trigger
	- 对轰炸机兵种，是指其兵种技能【俯冲轰炸】【id = 303】的发动概率
	  - 即【俯冲轰炸】实际概率 = 基础概率 + 其他加成 - 【本作用值】
	    - 基础概率读取const表，字段trigger
	  - 该效果不可叠加，先到先得，持续回合结束后消失
	- 数值不叠加
	- 回合数不可刷新重置
	  - 另外：此作用号效果与英雄米迦勒的专属芯片效果【1553】不可同时生效
	- 具体实现为：当作用号【12143】存在时，强制让作用号【1553】失效
	[图片]
	  - 此为英雄战技专属作用号，配置格式如下：
	- 作用号id_参数1_参数2
	  - 参数1：作用号系数
	    - 配置格式：浮点数
	    - 即本作用值 = 英雄军事值 * 参数1/10000
	  - 参数2：持续回合数
	    - 配置格式：绝对值
	    - 注：由被附加开始到当前回合结束，算作 1 回合
	 */

	private int effect12141;// 填效果1作用号【12071】
	private double effect12141p1;
	private int effectTime;// 参数2：战技持续时间

	private int effect12142;// 填效果1作用号【12072】
	private double effect12142p1;

	private int effect12143;// 填效果1作用号【12072】
	private double effect12143p1;
	private int effect12143p2;

	@Override
	public List<PBHeroEffect> effectVal() {
		// proficiencyEffect="1657_0.6_0_720_6000_1_1"
		try {
			// 12071_1.1_300|12072_0.09|12073_0.18_|12074_0.26
			String proficiencyEffect = getCfg().getProficiencyEffect();
			String[] ps = proficiencyEffect.replace("|", "_").split("_");
			effect12141 = NumberUtils.toInt(ps[0]);
			effect12141p1 = NumberUtils.toDouble(ps[1]);
			effectTime = NumberUtils.toInt(ps[2]);

			effect12142 = NumberUtils.toInt(ps[3]);
			effect12142p1 = NumberUtils.toDouble(ps[4]);

			effect12143 = NumberUtils.toInt(ps[5]);
			effect12143p1 = NumberUtils.toDouble(ps[6]);
			effect12143p2 = NumberUtils.toInt(ps[7]);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.effectVal();
	}

	@Override
	public int getShowProficiencyEffect() {
		if (isEffecting()) {
			return effect12141;
		}
		return 0;
	}

	@Override
	public int getProficiencyEffect(EffType effType, EffectParams effParams) {
		if (!isEffecting()) {
			return 0;
		}

		if (effType.getNumber() == effect12141) {
			return (int) Math.ceil(effect12141p1 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == effect12142) {
			return (int) Math.ceil(effect12142p1 * getParent().getParent().attrVale(101));
		}
		if (effType.getNumber() == effect12143) {
			return (int) Math.ceil(effect12143p1 * getParent().getParent().attrVale(101));
		}
		if(effType == EffType.SSS_SKILL_12143_P2){ 
			return effect12143p2;
		}

		return 0;

	}

	@Override
	public int effectTime() {
		return effectTime * 1000 + getSoulEffVal(EffType.EFF_12327) * 1000;
	}

	public int getEffect12141() {
		return effect12141;
	}

	public void setEffect12141(int effect12141) {
		this.effect12141 = effect12141;
	}

	public double getEffect12141p1() {
		return effect12141p1;
	}

	public void setEffect12141p1(double effect12141p1) {
		this.effect12141p1 = effect12141p1;
	}

	public int getEffectTime() {
		return effectTime;
	}

	public void setEffectTime(int effectTime) {
		this.effectTime = effectTime;
	}

	public int getEffect12142() {
		return effect12142;
	}

	public void setEffect12142(int effect12142) {
		this.effect12142 = effect12142;
	}

	public double getEffect12142p1() {
		return effect12142p1;
	}

	public void setEffect12142p1(double effect12142p1) {
		this.effect12142p1 = effect12142p1;
	}

	public int getEffect12143() {
		return effect12143;
	}

	public void setEffect12143(int effect12143) {
		this.effect12143 = effect12143;
	}

	public double getEffect12143p1() {
		return effect12143p1;
	}

	public void setEffect12143p1(double effect12143p1) {
		this.effect12143p1 = effect12143p1;
	}

	public int getEffect12143p2() {
		return effect12143p2;
	}

	public void setEffect12143p2(int effect12143p2) {
		this.effect12143p2 = effect12143p2;
	}

}