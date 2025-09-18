package com.hawk.game.battle.effect.impl.ailinna12081;

import java.util.Optional;

import com.hawk.game.battle.BattleSoldier_1;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.hero.skill.Skill1089;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12092】
- 【万分比】【12092】战技持续期间，受到远程庇护效果的部队，生命加成 +XX.XX%（持续 2 回合）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 此作用号为作用号【12085】生效时的远程单位附加的额外效果
    - 注：【12085】作用号有叠加层数效果，但本作用号没有层数效果（只分有没有）
    - 注：此作用号被附加后与【12085】就再无关系，不会随【12085】消失而消失
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
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12092)
public class Checker12092 implements IChecker {
	final int ElinaId = 1089;

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12085) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		// 负面效果的概率 必带sss凯恩, 没有就骂驾驶证
		Optional<PlayerHero> kaienOp = parames.unity.getPlayer().getHeroByCfgId(ElinaId);
		if (!kaienOp.isPresent()) {
			return CheckerKVResult.DefaultVal;
		}
		PlayerHero kaiEn = kaienOp.get();
		Optional<IHeroSkill> skillOp = kaiEn.getProficiencySkill();
		if (!skillOp.isPresent()) {
			return CheckerKVResult.DefaultVal;
		}
		Skill1089 skill = (Skill1089) skillOp.get();
		BattleSoldier_1 soldier = (BattleSoldier_1) parames.solider;
		soldier.setEff19092Round(skill.getEffect12092p2());

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
