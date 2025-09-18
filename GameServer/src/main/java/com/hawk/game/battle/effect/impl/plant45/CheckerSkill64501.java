package com.hawk.game.battle.effect.impl.plant45;

import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;

/**
 *  【64501】狙击兵
- 狙击兵（兵种类型 = 6），新增技能id = 64501
- 技能效果：自身出征数量每达到1万，自身防御、生命+XX%（该效果可加，至多X层）
  - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 此为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - 相关技能参数读取battle_soldier_skill表，对应参数效果如下
    - trigger：触发概率，这里无意义（填上10000）
      - 配置格式：万分比
    - p1：自身出征数 生效指定数值
      - 配置格式：绝对值
    - p2：单次增加防御、生命加成数值
      - 配置格式：万分比
    - p3：该效果最多叠加层数
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.HP, Type.DEF })
@EffectChecker(effType = EffType.EFF_SOLDIER_SKILL_64501)
public class CheckerSkill64501 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		List<BattleUnity> fottList = parames.unitStatic.getPlayerSoldierTable().get(parames.unity.getPlayerId(), SoldierType.FOOT_SOLDIER_6);
		if(fottList==null){
			return CheckerKVResult.DefaultVal; 
		}
		Optional<BattleSoldier> op = fottList.stream().map(BattleUnity::getSolider).filter(s -> s.hasSkill(PBSoldierSkill.SOLDIER_SKILL_64501)).findAny();

		if (!op.isPresent()) {
			return CheckerKVResult.DefaultVal;
		}

		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 64501);
		if (scfg == null) {
			return CheckerKVResult.DefaultVal;
		}

		int march = (int) parames.unitStatic.getPlayerArmyCountMapMarch().get(parames.unity.getPlayerId());
		int effPer = Math.min(march / scfg.getP1IntVal(), scfg.getP3IntVal()) * scfg.getP2IntVal();

		return new CheckerKVResult(effPer, 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
