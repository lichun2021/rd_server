package com.hawk.game.battle.effect.impl.hero1106;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * - 【万分比】【12461~12463】集结战斗开始前，若自身出征直升机数量超过自身出征部队总数 50%且不低于集结部队总数 5%，自身出征数量最多的直升机于本场战斗中获取如下效果（多个薇拉同时存在时，至多有 2 (effect12461Maxinum)个直升机单位生效）: 空中缠斗：每第 5 (effect12461AtkRound)回合开始时，若敌方存在空军单位，则选中其中 1 (effect12461InitChooseNum)个空军单位与自身进入缠斗状态（持续 3(effect12461ContinueRound) 回合）
  - 缠斗状态 下，受到己方直升机干扰，敌方该空军单位在造成伤害时，有 XX.XX% 【12461】的伤害将强制分摊至己方该直升机上
  - 缠斗状态 下，己方直升机以耗损 XX.XX%【12462】 的伤害效率的代价，在受到缠斗目标伤害时，额外减少 +XX.XX%【12463】
  - 注：此处新增3个作用号需求，实际主作用号为【12461】，辅作用号【12462~12463】绑定【12461】生效，此处分开处理仅为支持配置各作用号数值
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的直升机
    - 数量1 = 某玩家出征携带的直升机（兵种类型 = 4）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12461AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身部队总数 50% 的直升机
    - 数量1 = 某玩家出征携带的直升机（兵种类型 = 4）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12461SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的直升机在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的直升机（兵种类型 = 4）生效（若存在多个直升机数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对自身幻影部队不生效
  - 空中缠斗：每第 5 回合开始时，若敌方存在空军单位，则选中其中 1 个空军单位与自身进入缠斗状态（持续 3 回合）
    - 每第 5 回合开始时
      - 战斗中每第 5 的倍数的回合开始后，自身开始普攻前，进行1次作用号使用
        - 注：须确保此作用号在伊娜莉莎的轮番轰炸技能释放之前生效
[图片]
      - 指定回合数读取const表，字段effect12461AtkRound
        - 配置格式：绝对值
    - 若敌方存在空军单位
      - 若释放时敌方不存在空军兵种，则此作用号无法发动
        - 空军部队类型包含有：轰炸机（兵种类型 = 3）、直升机（兵种类型 = 4）
    - 选中其中 1 个空军单位与自身进入缠斗状态
      - 缠斗状态需要标记主动方和被动方，主动方为缠斗方，被动方为被缠斗方
        - 一个缠斗方可以关联多个被缠斗方
        - 一个被缠斗方只关联一个缠斗方
        - 往下严格使用此逻辑；战斗单位可同时身为缠斗方和被缠斗方
      - 具体随机规则如下
        - 优先于敌方未处于被缠斗状态下的轰炸机中随机选取，再从敌方未处于被缠斗状态下的直升机中随机选取，直至敌方目标单位不足或已达到可选取上限
      - 注：若目标为某荣耀所罗门的幻影部队的母体部队，此debuff效果也给其幻影部队挂上
      - 基础可选取上限数量读取const表，字段effect12461InitChooseNum
        - 配置格式：绝对值
    - 持续 3 回合（本回合被附加到下回合开始算 1 回合）
      - 持续回合数读取const表，字段effect12461ContinueRound
        - 配置格式：绝对值
  - 【12461】缠斗状态 下，受到己方直升机干扰，敌方该空军单位在造成伤害时，有 XX.XX% 的伤害将强制分摊至己方该直升机上
    - 注：若伤害目标为己方该直升机单位，则自然无效
    - 被缠斗方空军即将造成伤害时生效，减少目标本次受到的伤害，并将减少的伤害施加至缠斗方
      - 此效果与现有埃琳娜的伤害分摊【12282】机制，规则完全相同，此处判定优先级高于埃琳娜
      - 具体为在造成任意伤害时，优先进行此分摊，然后再进行埃琳娜的分摊
        - 【受攻击方】分摊1次后伤害 = 基础伤害 * （1 - 【本作用值】）（一次分摊）
          - 【受攻击方】实际受伤 = 【受攻击方】分摊1次后伤害 * （1 - 【防御坦克A】【12282】 - 【防御坦克B】【12282】）（二次分摊）
        - 【直升机】分摊1次后伤害 = 基础伤害 * 【本作用值】（一次分摊）
          - 【直升机】实际受伤 = 【直升机】分摊1次后伤害* （1 - 【防御坦克A】【12282】 - 【防御坦克B】【12282】）（二次分摊）
        - 【防御坦克A】实际受伤 = 为【受攻击方】分摊值 + 为【直升机】分摊值
          - 为【受攻击方】分摊值 = 【受攻击方】分摊1次后伤害 * 【防御坦克A】【12282】 = 基础伤害 * （1 - 【本作用值】）* 【防御坦克A】【12282】
          - 为【直升机】分摊值 = 【直升机】分摊1次后伤害 * 【防御坦克A】【12282】= 基础伤害 * 【本作用值】* 【防御坦克A】【12282】
        - 【防御坦克B】实际受伤 = 为【受攻击方】分摊值 + 为【直升机】分摊值
          - 为【受攻击方】分摊值 = 【受攻击方】分摊1次后伤害 * 【防御坦克B】【12282】 = 基础伤害 * （1 - 【本作用值】）* 【防御坦克B】【12282】
          - 为【直升机】分摊值 = 【直升机】分摊1次后伤害 * 【防御坦克B】【12282】= 基础伤害 * 【本作用值】* 【防御坦克B】【12282】
暂时无法在飞书文档外展示此内容
  - 【12462~12463】缠斗状态 下，己方直升机以耗损 XX.XX% 的伤害效率的代价，在受到缠斗目标伤害时，额外减少 +XX.XX%
    - 【12462】缠斗方造成伤害效率降低；即实际造成伤害时 ；即实际造成伤害 = 基础伤害*（1 + 己方各类伤害加成）*（1 - 敌方某伤害减少）*（1 - 【本作用值】）
      - 这是个给自身施加的debuff效果，使自身造成的所有伤害降低
    - 【12463】缠斗方受到被缠斗方伤害时，使本次伤害减少；该作用号为伤害减少效果，与其他作用号累乘计算，即 
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【本作用值】）
  - （多个薇拉同时存在时，至多有 2 个直升机单位获得上述效果）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效（在战前判定即可，战中不再变化）
      - 层数上限读取const表，字段effect12461Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12461)
public class Checker12461 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {

		if (parames.type != SoldierType.PLANE_SOLDIER_4 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.PLANE_SOLDIER_4)) {
			return CheckerKVResult.DefaultVal;
		}

		Map<String, Integer> effPlayerVal = (Map<String, Integer>) parames.getLeaderExtryParam(getSimpleName());
		if (effPlayerVal == null) {
			effPlayerVal = selectPlayer(parames);
			parames.putLeaderExtryParam(getSimpleName(), effPlayerVal);
		}

		if (!effPlayerVal.containsKey(parames.unity.getPlayerId())) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = effPlayerVal.get(parames.unity.getPlayerId());

		return new CheckerKVResult(effPer, 0);
	}

	/**数值最高的玩家*/
	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new LinkedHashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayer())) {
				continue;
			}
			int effvalue = effvalue(unity, parames);
			valMap.put(unity.getPlayerId(), effvalue);
		}

		valMap = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().getEffect12461Maxinum())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return valMap;
	}

	private int effvalue(BattleUnity unity, CheckerParames parames) {
		try {
			if (unity.getEffVal(effType()) == 0) {
				return 0;
			}
			String playerId = unity.getPlayerId();
			int effPer = 0;
			// 采矿车数量
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.PLANE_SOLDIER_4);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12461AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12461SelfNumLimit() * GsConst.EFF_PER) {
					effPer = unity.getEffVal(effType());
				}
			}
			return effPer;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
