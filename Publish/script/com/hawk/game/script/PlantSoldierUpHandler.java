package com.hawk.game.script;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.CrystalAnalysisChip;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.PlantCrystalAnalysis;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.cfg.PlantCrystalAnalysisChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.InstrumentChip;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.PlantInstrument;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.cfg.PlantInstrumentUpgradeChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitary;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitaryChip;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.CrackChip;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.PlantSoldierCrack;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg.PlantSoldierCrackCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg.PlantSoldierCrackChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg.PlantSoldierStrengthenTechCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Script.ScriptError;
/**
 * 泰能兵一键升级
 * 
 * localhost:8080/script/plantSoldierUp?playerId=1aat-2fqakl-1
 * 
 *
 */
public class PlantSoldierUpHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		return doAction(params);
	}

	public static String doAction(Map<String, String> params) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			PlantSoldierSchool school = player.getPlantSoldierSchool();

			upInstrument(school);
			onSoldierCrackUpgrade(school);
			onUpgradeCrystalAnalysisChip(school);
			onSoldierStrengthenTechLevelUp(school);
			onSoldierMilitaryUpgreade(school, Integer.parseInt(params.getOrDefault("term", "0")));
			school.notifyChange(null);
			return HawkScript.successResponse("SUCC");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");

	}

	private static void upInstrument(PlantSoldierSchool school) {
		final PlantInstrument instrument = school.getInstrument();

		for (InstrumentChip upchip : instrument.getChips()) {
			while (upchip.getCfg().getLevel() < instrument.getCfg().getMaxChipLevel()) {
				PlantInstrumentUpgradeChipCfg chipCfg = upchip.getCfg();
				// 消耗
				upchip.setCfgId(chipCfg.getPostStage());
			}
		}
		instrument.notifyChange();
	}

	private static void onSoldierCrackUpgrade(PlantSoldierSchool school) {

		for (PlantSoldierCrack upfactory : school.getCracks()) {

			while (true) {
				PlantSoldierCrackCfg cfg = upfactory.getCfg();
				PlantSoldierCrackCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierCrackCfg.class, cfg.getPostStage());

				if (upcfg == null) {
					break;
				}

				upfactory.setCfgId(upcfg.getId());
			}
			for (CrackChip upchip : upfactory.getChips()) {
				onSoldierCrackChipUpgrade(upchip);
			}
			upfactory.notifyChange();
		}

	}

	private static void onSoldierCrackChipUpgrade(CrackChip upchip) {
		while (true) {
			PlantSoldierCrackChipCfg chipCfg = upchip.getCfg();
			PlantSoldierCrackChipCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierCrackChipCfg.class, chipCfg.getPostStage());
			if (upcfg == null) {
				break;
			}
			upchip.setCfgId(upcfg.getId());
		}

	}

	private static void onUpgradeCrystalAnalysisChip(PlantSoldierSchool school) {
		final PlantCrystalAnalysis crystal = school.getCrystal();

		for (CrystalAnalysisChip upchip : crystal.getChips()) {
			if (upchip.getCfg().getLevel() >= crystal.getCfg().getMaxChipLevel()) {
				continue;
			}
			while (true){
				PlantCrystalAnalysisChipCfg chipCfg = upchip.getCfg();
				PlantCrystalAnalysisChipCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantCrystalAnalysisChipCfg.class, chipCfg.getPostStage());
				if(upcfg == null){
					break;
				}
				upchip.setCfgId(upcfg.getId());
			}
		}
		crystal.notifyChange();
	}

	private static void onSoldierStrengthenTechLevelUp(PlantSoldierSchool school) {
		for (SoldierType type : SoldierType.values()) {
			final SoldierStrengthen sthen = school.getSoldierStrengthenByType(type);
			if (sthen == null) {
				continue;
			}
			List<PlantSoldierStrengthenTechCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierStrengthenTechCfg.class).stream()
					.filter(cfg -> cfg.getType() == type).collect(Collectors.toList());
			for (PlantSoldierStrengthenTechCfg cfg : cfgList) {
				sthen.techUpGrade(cfg);
			}
			sthen.notifyChange();
		}
	}

	private static void onSoldierMilitaryUpgreade(PlantSoldierSchool school, int term) {
		for (SoldierType soldierType : SoldierType.values()) {
			// 未解锁
			PlantSoldierMilitary military = school.getSoldierMilitaryByType(soldierType);
			if(military == null){
				continue;
			}
			military.setUnlock(true);
			for (PlantSoldierMilitaryChip chip : military.getChips()) {
				while (chip.getNextCfg() != null) {
					// 没有下一等级了
					PlantSoldierMilitaryChipCfg nextChipCfg = chip.getNextCfg();
					chip.setCfgId(nextChipCfg.getId());
				}
			}
			military.notifyChange();
		}
		if(term>=3){
			for (SoldierType soldierType : SoldierType.values()) {
				// 未解锁
				PlantSoldierMilitary military = school.getSoldierMilitary3ByType(soldierType);
				if(military == null){
					continue;
				}
				military.setUnlock(true);
				for (PlantSoldierMilitaryChip chip : military.getChips()) {
					while (chip.getNextCfg() != null) {
						// 没有下一等级了
						PlantSoldierMilitaryChipCfg nextChipCfg = chip.getNextCfg();
						chip.setCfgId(nextChipCfg.getId());
					}
				}
				military.notifyChange();
			}
		}
	}
}