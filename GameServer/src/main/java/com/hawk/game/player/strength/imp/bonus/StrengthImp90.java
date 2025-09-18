package com.hawk.game.player.strength.imp.bonus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.config.SuperSoldierStarLevelCfg;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 机甲
 * 
 * @author Golden
 *
 */
@StrengthType(strengthType = 90)
public class StrengthImp90 implements StrengthBonusImp {

	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();

		// 组织下参数
		Map<Integer, List<Integer>> superSoldierMap = new HashMap<>();
		String param1 = typeCfg.getParam1();
		String[] split = param1.split(",");
		for (int i = 0; i < split.length; i++) {
			String[] split2 = split[i].split("_");
			Integer type = Integer.valueOf(split2[0]);
			List<Integer> superSoldierList = new ArrayList<>();
			for (int j = 1; j < split2.length; j++) {
				superSoldierList.add(Integer.valueOf(split2[j]));
			}
			superSoldierMap.put(type, superSoldierList);
		}

		// 计算的机甲id
		List<Integer> superSoldiers = superSoldierMap.get(soldierType.getNumber());
		if (superSoldiers == null) {
			return;
		}

		int atkValue = 0;
		int hpValue = 0;
		for (int superSoldierId : superSoldiers) {
			Optional<SuperSoldier> superSoldierOp = getSuperSoldierByCfgId(playerData, superSoldierId);
			if (superSoldierOp.isPresent()) {
				SuperSoldier superSoldier = superSoldierOp.get();
				SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(
						SuperSoldierStarLevelCfg.class, superSoldier.getCfgId(), superSoldier.getStar(),
						superSoldier.getStep());

				atkValue += starLevelCfg.getAtkAttr().getOrDefault(soldierType.getNumber(), 0);
				hpValue += starLevelCfg.getHpAttr().getOrDefault(soldierType.getNumber(), 0);

			}
		}

		cell.setAtk(Math.min(typeCfg.getAtkAttrMax(), atkValue));
		cell.setHp(Math.min(typeCfg.getHpAttrMax(), hpValue));
	}

	/**
	 * 取得超级兵按照配置文件ID
	 */
	private Optional<SuperSoldier> getSuperSoldierByCfgId(PlayerData playerData, int soldierId) {
		return getAllSuperSoldier(playerData).stream().filter(e -> e.getCfgId() == soldierId).findAny();
	}

	private List<SuperSoldier> getAllSuperSoldier(PlayerData playerData) {
		List<SuperSoldier> result = playerData.getSuperSoldierEntityList().stream()
				.map(SuperSoldierEntity::getSoldierObj).collect(Collectors.toList());
		return result;
	}
}
