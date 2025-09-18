package com.hawk.game.battle.effect.impl.hero1106;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * - 【万分比】【12466~12468】集结战斗开始前，若自身出征直升机数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机于本场战斗中获取如下效果（多个薇拉同时存在时，至多有 2(effect12466Maxinum)
 *  个直升机单位生效）：领域扩展: 在触发 散射攻击 技能时，使敌方降低攻击、防御、生命加成效果的叠加速率额外 +XX.XX%【12466】，且可叠加数值上限额外 +XX.XX%【12467】；并有 XX.XX%【12468】 的概率选中 2(effect12468AtkNum) 个敌方近战单位作为额外攻击目标
  - 注：此处新增3个作用号需求，实际主作用号为【12466】，辅作用号【12467~12468】绑定【12466】生效，此处分开处理仅为支持配置各作用号数值
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 此作用号绑定散射攻击 作用号【12062】，触发散射攻击时，额外触发以下效果
    - 作用号【12466】，其机制为改变伊娜莉莎专属作用号【12063】【12064】【12065】【12272】生效时单次给予敌方的debuff数值；即
      - 【12063】单次实际附加值 = 【12063】*（1 + 【12466】）
      - 【12064】单次实际附加值 = 【12064】*（1 + 【12466】）
      - 【12065】单次实际附加值 = 【12065】*（1 + 【12466】）
      - 【12272】单次实际附加值 = 【12272】*（1 + 【12466】）
        - 注：此作用号限制的是生效的玩家数量，单玩家实际释放时其数值走其自身数值
    - 作用号【12467】，其机制为改变伊娜莉莎专属作用号【12063】【12064】【12065】【12272】的数值叠加上限；即
      - 【12063】最终叠加上限 = 【12063】基础上限 *（1 + 【直升机A】【12467】 + 【直升机B】【12467】）
      - 【12064】最终叠加上限 = 【12064】基础上限 *（1 + 【直升机A】【12467】 + 【直升机B】【12467】）
      - 【12065】最终叠加上限 = 【12065】基础上限 *（1 + 【直升机A】【12467】 + 【直升机B】【12467】）
      - 【12272】最终叠加上限 = 【12272】基础上限 *（1 + 【直升机A】【12467】 + 【直升机B】【12467】）
        - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效，其数值叠加（对敌方全体生效，战前判定）
        - 各作用号原基础上限读取const表，字段effect12063MaxValue、effect12064MaxValue、effect12065MaxValue、effect12272MaxValue
          - 配置格式：万分比
    - 【12468】并有 XX% 的概率选中 2 个敌方近战单位作为额外攻击目标
      - XX% 的概率触发
        - 在触发散射攻击【12062】技能时，判断是否触发
          - 触发概率 = 【本作用值】
          - 每次满足条件时独立判定
          - 注：此为概率判定数值，最终取值合法区间为【0%,100%】
      - 以下为散射攻击 作用号【12062】的原先的选取目标逻辑
[图片]
      - 近战部队类型包含有：防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）
      - 各兵种类型随机权重读取const表，字段effect12468RoundWeight
        - 配置格式：类型1_权重1,类型2_权重2,类型3_权重3,类型4_权重4
      - 随机机制如下
        - 取敌方现有所有兵种类型，先按兵种类型权重随机出 1 个敌方兵种类型
        - 后于此兵种类型下纯随机出 1 个该兵种类型下的敌方单位
        - 选定后剔除掉该敌方单位，继续上述循环
        - 直至选满敌方单位数达到攻击目标数 或 敌方近战单位数不足
      - 额外选取近战单位数量读取const表，字段effect12468AtkNum
        - 配置格式：绝对值
      - 注：此作用号限制的是生效的玩家数量，单玩家实际释放时其数值走其自身数值
- （多个薇拉同时存在时，至多有 2 个直升机单位获得上述效果）
  - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
  - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
    - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效
    - 层数上限读取const表，字段分别为effect12466Maxinum
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12466)
public class Checker12466 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12062) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());

		return new CheckerKVResult(effPer, effNum);

	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
