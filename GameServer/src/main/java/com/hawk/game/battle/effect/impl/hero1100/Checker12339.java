package com.hawk.game.battle.effect.impl.hero1100;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 12339】
- 【万分比】【12339】集结战斗开始前，若自身出征攻城车数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的攻城车于本场战斗中获取如下效果（多个威尔森同时存在时，至多有 2 个攻城车单位生效）;在战斗处于偶数回合时，开启聚能环护模式:  并在本回合结束时恢复其于最近的 2 个回合损失的 XX.XX%的部队（该效果无法叠加）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 注：此作用号绑定在作用号【12337】生效的目标单位上
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12339AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身出征部队总数 50% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12339SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的攻城车
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的攻城车（兵种类型 = 7）生效（若存在多个攻城车数量一样且最高，取等级高的）
      - 在战斗开始前进行判定，选中目标后战中不再变化
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队无效！
  - 在战斗处于偶数回合时，开启聚能环护模式 
    - 即在战斗处于第2、4、....偶数回合时，该作用号才生效
  - 并在本回合结束时恢复其于最近的 2 个回合损失的 XX.XX%的部队（该效果无法叠加）
    - 最近的 2 个回合是指【本回合+上一回合】
      - 读取const表，字段effect12339CountRound
        - 配置格式：绝对值
    - 该效果无法叠加
      - 即某单位挂上该作用号后，后续再生效此作用号时，数值不变且持续回合数不变
    - 恢复部队是指：该单位在此期间在战斗中损失的部队数量，直接恢复其一定比率后继续参与战斗
    - 注：因为此处涉及到战报中输出和战损不一致的问题，需要在某兵种恢复时，对应扣除掉对其产生输出的所有兵种的输出数据
  - （多个威尔森同时存在时，至多有 2 个攻城车单位生效）
    - 单玩家拥有此作用号时，只能生效 1 个攻城车单位；集结中存在多个此作用号时，可以同时生效
    - 限制该作用号生效人数上限
      - 注：这里限制的是生效人数上限，若集结中超出此上限，取作用号数值高的，若作用号数值相同，取战报列表中排序靠前的；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12339Maxinum
        - 配置格式：绝对值
 * @author lwt
 * @date 2024年5月25日
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12339)
public class Checker12339 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12337) <= 0) {
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
