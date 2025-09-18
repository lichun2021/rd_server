package com.hawk.game.battle.effect.impl.ailinna12081;

import java.util.Objects;
import java.util.Optional;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.hero.skill.Skill1089;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12091】
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
 */
@BattleTupleType(tuple = Type.ATKFIRE)
@EffectChecker(effType = EffType.EFF_12091)
public class Checker12091 extends Checker12081 implements IChecker {
	/**
	 * - 中文名
	- 埃琳娜
	- 昵称（称号）
	- 战场穿梭者
	- 英文名
	- Elina
	 */
	final int ElinaId = 1089;

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!isJinzhan(parames.type)) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		Integer effPlayerVal = 0;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			effPlayerVal = (Integer) object;
		} else {
			Skill1089 skill = getskill(parames);
			if (Objects.nonNull(skill)) {
				effPlayerVal = selectPlayer(parames, skill.getEffect12091p2(), skill.getEffect12091p3(), skill.getEffect12091p4());
			}
			parames.putLeaderExtryParam(getSimpleName(), effPlayerVal);
		}
		return new CheckerKVResult(effPlayerVal, 0);
	}

	private Skill1089 getskill(CheckerParames parames) {
		for (BattleUnity unity : parames.unityList) {
			if (unity.getEffVal(effType()) <= 0) {
				continue;
			}
			// 负面效果的概率 必带sss凯恩, 没有就骂驾驶证
			Optional<PlayerHero> kaienOp = unity.getPlayer().getHeroByCfgId(ElinaId);
			if (!kaienOp.isPresent()) {
				continue;
			}
			PlayerHero kaiEn = kaienOp.get();
			Optional<IHeroSkill> skillOp = kaiEn.getProficiencySkill();
			if (!skillOp.isPresent()) {
				continue;
			}
			Skill1089 skill = (Skill1089) skillOp.get();
			return skill;
		}
		return null;
	}

}
