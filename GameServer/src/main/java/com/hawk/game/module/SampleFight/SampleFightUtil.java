package com.hawk.game.module.SampleFight;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkOSOperator;
import org.hawk.xid.HawkXID;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.SampleFight.data.SFData;
import com.hawk.game.module.SampleFight.data.SFUnit;
import com.hawk.game.module.SampleFight.hero.SFHeroFactory;
import com.hawk.game.module.SampleFight.npc.SFPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.world.march.IWorldMarch;

public class SampleFightUtil {
	public static String fight(SFData data) {
		List<Player> atkPlayers = new ArrayList<>();
		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();

		int i = 0;
		for (SFUnit unit : data.getAtks()) {
			i++;
			buildMarch(unit, "a" + i, atkPlayers, atkMarchs);
		}

		for (SFUnit unit : data.getDefs()) {
			i++;
			buildMarch(unit, "d" + i, defPlayers, defMarchs);
		}

		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.YURI_YURIREVENGE, 0, atkPlayers, defPlayers, atkMarchs, defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.getBattle().setDuntype(DungeonMailType.TBLY);
		try {
			Field f1 = HawkOSOperator.getClassField(battleIncome.getDefCalcParames(), "duel");
			f1.set(battleIncome.getDefCalcParames(), true);
			Field f2 = HawkOSOperator.getClassField(battleIncome.getAtkCalcParames(), "duel");
			f2.set(battleIncome.getAtkCalcParames(), true);

			Field f3 = HawkOSOperator.getClassField(battleIncome.getDefCalcParames(), "decDieBecomeInjury");
			f3.set(battleIncome.getDefCalcParames(), 1000000);

			Field f4 = HawkOSOperator.getClassField(battleIncome.getAtkCalcParames(), "decDieBecomeInjury");
			f4.set(battleIncome.getAtkCalcParames(), 1000000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		battleIncome.getBattle().setSaveDebugLog(false);
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
//		"1aas-2pca6v-1"
//		FightMailService.getInstance().sendFightMail(WorldPointType.KING_PALACE_VALUE, battleIncome, battleOutcome, null);
		
		SFData result = new SFData();
		List<SFUnit> atkList = new ArrayList<>();
		List<SFUnit> defList = new ArrayList<>();
		
		for(Player atk : battleIncome.getAtkPlayers()){
			SFUnit unit = new SFUnit();
			unit.setHeros(battleIncome.getAtkPlayerHeros(atk.getId()).toString());
			unit.setArmys(battleOutcome.getAftArmyMapAtk().get(atk.getId()).toString());
			atkList.add(unit);
		}
		
		for(Player def : battleIncome.getDefPlayers()){
			SFUnit unit = new SFUnit();
			unit.setHeros(battleIncome.getDefPlayerHeros(def.getId()).toString());
			unit.setArmys(battleOutcome.getAftArmyMapDef().get(def.getId()).toString());
			defList.add(unit);
		}
		
		result.setAtkWin(battleOutcome.isAtkWin() ? 1 : 0);
		result.setAtks(atkList);
		result.setDefs(defList);
		
		String jsonString = JSONObject.toJSONString(result);
		return jsonString;
	}

	public static List<ArmyInfo> convertStringToArmyList(String armyStr) {
		// 军队
		List<ArmyInfo> armys = new ArrayList<ArmyInfo>();
		if (armyStr != null && !armyStr.equals("")) {
			String[] strs = armyStr.split(",");
			for (int i = 0; i < strs.length; i++) {
				String[] strs2 = strs[i].split("_");
				int armyId = Integer.parseInt(strs2[0]);
				int totalCount = Integer.parseInt(strs2[1]);
				ArmyInfo info = new ArmyInfo(armyId, totalCount);
				armys.add(info);
			}
		}
		return armys;
	}

	public static SFData testData() {
		SFData data = new SFData();
		SFUnit unit1 = new SFUnit();
		unit1.setHeros("1011,1012");
		unit1.setArmys("100706_8888,100806_77777");
		
		SFUnit unit3 = new SFUnit();
		unit3.setHeros("1011,1012");
		unit3.setArmys("100706_8888,100806_77777");
		
		
		SFUnit unit2 = new SFUnit();
		unit2.setHeros("1011,1012");
		unit2.setArmys("100706_6666,100806_77777");
		
		List<SFUnit> atkList = new ArrayList<>();
		atkList.add(unit1);
		atkList.add(unit3);
		
		List<SFUnit> defList = new ArrayList<>();
		defList.add(unit2);
		
		data.setAtks(atkList);
		data.setDefs(defList);
		
		System.out.println(JSONObject.toJSONString(data)); 
		return data;
	}

	private static IWorldMarch buildMarch(SFUnit battleUnit, String name, List<Player> atkPlayers, List<IWorldMarch> atkMarchs) {
		SFPlayer fighter = new SFPlayer(HawkXID.nullXid());
		fighter.setName(name);
		fighter.setPlayerId(name);
		// 军队
		List<ArmyInfo> armys = convertStringToArmyList(battleUnit.getArmys());

		List<Integer> heroIdsList = Splitter.on(",").omitEmptyStrings().splitToList(battleUnit.getHeros()).stream()
				.mapToInt(NumberUtils::toInt)
				.mapToObj(Integer::valueOf)
				.collect(Collectors.toList());

		List<PlayerHero> heroList = new ArrayList<>(2);
		for (int cfgId : heroIdsList) {
			heroList.add(SFHeroFactory.getHerobyCfgId(cfgId));
		}

		fighter.setHeros(heroList);

		TemporaryMarch atkMarch = new TemporaryMarch();
		atkMarch.setArmys(armys);
		atkMarch.setPlayer(fighter);
		atkMarch.getMarchEntity().setHeroIdList(heroIdsList);
		atkMarch.setHeros(heroList);

		atkPlayers.add(atkMarch.getPlayer());
		atkMarchs.add(atkMarch);
		return atkMarch;
	}
}
