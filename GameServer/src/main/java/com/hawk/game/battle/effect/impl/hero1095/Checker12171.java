package com.hawk.game.battle.effect.impl.hero1095;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 12164~12171】
- 【万分比】【12164~12171】集结战斗时，若自身出征采矿车数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的采矿车于本场战斗中获取如下效果（多个杰西卡同时存在时，至多有 2 个采矿车单位生效）：引爆：在鼓风完成后，若杰西卡和杰拉尼同时出征，则采矿车额外向敌方处于【燃烧状态】的随机 5 个近战单位投掷油爆弹，对其造成爆炸伤害（伤害率XX.XX%），并使其进入【损坏状态】，持续 3 回合（引爆后，被引爆的敌方单位，其【燃烧状态】解除）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制绑定作用号【12163】，在【12163】攻击完成后（不论是否命中），使目标单位进入【损坏状态】
  - 【损坏状态】为一种负面状态标识，这里针对不同的近战单位类型单独处理
    - 主战坦克（兵种类型 = 2）
      - 【12164】主战坦克处于【损坏状态】时，其攻击加成减少 +XX.XX%
        - 此为外围属性加成减少效果；即实际攻击 = 基础攻击*（1 + 其他加成 - 【本作用值】）
      - 【12165】主战坦克处于【损坏状态】时，其在发起攻击时，伤害降低 +XX.XX%
        - 此为外围累乘减免效果（同作用号【12153】），即 敌方实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 某作用值伤害减免）*（1 - 【本作用值】）
    - 轰炸机（兵种类型 = 3）
      - 【12166】轰炸机处于【损坏状态】时，其攻击加成减少 +XX.XX%
        - 此为外围属性加成减少效果；即实际攻击 = 基础攻击*（1 + 其他加成 - 【本作用值】）
      - 【12167】轰炸机处于【损坏状态】时，其在发起攻击时，伤害降低 +XX.XX%
        - 此为外围累乘减免效果（同作用号【12153】），即 敌方实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 某作用值伤害减免）*（1 - 【本作用值】）
    - 防御坦克（兵种类型 = 1）
      - 【12168】防御坦克处于【损坏状态】时，其防御、生命加成减少 +XX.XX%
        - 此为外围属性加成减少效果；即实际防御 = 基础防御*（1 + 其他加成 - 【本作用值】）
      - 【12169】防御坦克处于【损坏状态】时，其在受到攻击时，伤害额外 +XX.XX%
        - 此为外围伤害加成效果，与其他作用号累乘计算；即实际伤害 = 基础伤害*（1 + 其他加成）*（1 +  【本作用值】）
    - 采矿车（兵种类型 = 8）
      - 【12170】采矿车处于【损坏状态】时，其防御、生命加成减少 +XX.XX%
        - 此为外围属性加成减少效果；即实际防御 = 基础防御*（1 + 其他加成 - 【本作用值】）
      - 【12171】采矿车处于【损坏状态】时，其在受到攻击时，伤害额外 +XX.XX%
        - 此为外围伤害加成效果，与其他作用号累乘计算；即实际伤害 = 基础伤害*（1 + 其他加成）*（1 +  【本作用值】）
  - 该效果不可叠加，可刷新重置
    - 即在此debuff生效期间，若敌方单位又受到此debuff效果，则刷新其debuff数值且重置其持续回合数
  - 持续 3 回合（本回合被附加到下回合开始算 1 回合）
    - 持续回合数读取const表，字段effect12164ContinueRound、effect12165ContinueRound、effect12166ContinueRound、effect12167ContinueRound、effect12168ContinueRound、effect12169ContinueRound、effect12170ContinueRound、effect12171ContinueRound
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12171)
public class Checker12171 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12163) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
