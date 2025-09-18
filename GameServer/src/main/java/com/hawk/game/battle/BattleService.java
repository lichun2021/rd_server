package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.activity.type.impl.guildDragonAttack.cfg.GuildDragonAttackKVCfg;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.CheckerFactory;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.grab.GrabRes;
import com.hawk.game.battle.guarder.GuarderPlayer;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.SWBattleCfg;
import com.hawk.game.config.SuperWeaponSoldierCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldStrongpointCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.skill.talent.Skill10104;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Hero.PBHeroState;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;
import com.hawk.game.protocol.World.PlayerPresetMarchInfo;
import com.hawk.game.protocol.World.PresetMarchInfo;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.guildtask.event.KillEnemyTaskEvent;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventPvpBattle;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.FoggyInfo;
import com.hawk.game.world.object.GhostInfo;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 战斗服务管理类
 * 
 * @author Jesse
 *
 */
public class BattleService extends HawkAppObj {
	/**
	 * 日志对象
	 */
	static Logger logger = LoggerFactory.getLogger("Battle");

	/**
	 * 单例对象
	 */
	private static BattleService instance = null;

	/**
	 * NPC玩家id
	 */
	public static final String NPC_ID = "NPC-";

	/** 部队击杀/击伤类型 */
	enum ArmyResultType {
		/** 击杀 */
		KILL,
		/** 击伤 */
		HURT,
		/** 击杀&击伤 */
		MIX
	}

	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static BattleService getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public BattleService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
	}
	
	
	private int lastDuelValDay;
	private int duelPower;

	@Override
	public boolean onTick() {
		final int dayOfYear = HawkTime.getYearDay();
		final int openDay = GameUtil.getServerOpenDay();
		boolean isDuelCheckDay = dayOfYear != lastDuelValDay && openDay % ConstProperty.getInstance().getDuelPowerCheckTimeInterval() == 0;
		if (duelPower == 0 || isDuelCheckDay) {
			HawkTuple2<Integer, Integer> dbVal = RedisProxy.getInstance().getDuelPower();
			if(dbVal.first==0){
				dbVal = LocalRedis.getInstance().getDuelPower();
			}
			int dbDay = dbVal.first;
			int dbPower = dbVal.second;
			if (dbDay == 0) {
				dbPower = ConstProperty.getInstance().getDuelInitPower();
			}
			List<RankInfo> rankInfos = RankService.getInstance().getRankCache(RankType.PLAYER_FIGHT_RANK);
			String[] arr = ConstProperty.getInstance().getDuelPowerRankInterval().split("_");
			int rankFrom = NumberUtils.toInt(arr[0]);
			int rankTo = NumberUtils.toInt(arr[1]);
			OptionalDouble average = rankInfos.stream()
					.filter(rank -> rank.getRank() >= rankFrom && rank.getRank() <= rankTo)
					.mapToLong(RankInfo::getRankInfoValue).average();
			int avgPower = (int) average.orElse(0);

			boolean hasNotSave = dbDay != dayOfYear;
			if (hasNotSave && avgPower > dbPower && isDuelCheckDay) {
				int upPct = ConstProperty.getInstance().getDuelPowerOnceAddLimit();
				if (upPct < GsConst.EFF_RATE) {
					upPct += GsConst.EFF_RATE;
				}
				dbPower = (int) Math.min(avgPower, dbPower * GsConst.EFF_PER * upPct);
				dbPower = (int) (Math.round(dbPower * 1D / 500000) * 500000);
			}

			duelPower = dbPower;
			lastDuelValDay = dayOfYear;
			RedisProxy.getInstance().saveDuelPower(HawkTuples.tuple(lastDuelValDay, duelPower));
		}
		return super.onTick();
	}

	/**
	 * 初始化攻打野怪战斗输入信息(全量部队战斗,无额外伤害加成)
	 * 
	 * @param battleType
	 * @param pointId
	 * @param monsterId
	 * @param atkPlayers
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initMonsterBattleData(BattleConst.BattleType battleType, int pointId, int monsterId, List<Player> atkPlayers, List<IWorldMarch> atkMarchs) {
		return initMonsterBattleData(battleType, pointId, monsterId, atkPlayers, atkMarchs, GsConst.RANDOM_MYRIABIT_BASE, 0);
	}

	/**
	 * 初始化攻打野怪战斗输入信息(无额外伤害加成)
	 * 
	 * @param battleType
	 * @param pointId
	 * @param monsterId
	 * @param atkPlayers
	 * @param atkMarchs
	 * @param leftPercent
	 * @return
	 */
	public PveBattleIncome initMonsterBattleData(BattleConst.BattleType battleType, int pointId, int monsterId, List<Player> atkPlayers, List<IWorldMarch> atkMarchs,
			int leftPercent) {
		return initMonsterBattleData(battleType, pointId, monsterId, atkPlayers, atkMarchs, leftPercent, 0);
	}

	/**
	 * 初始化攻打野怪战斗输入信息
	 * 
	 * @param battleType
	 * @param pointId
	 * @param monsterId
	 * @param atkPlayers
	 * @param atkMarchs
	 * @param leftPercent
	 *            剩余怪物万分比
	 * @param addHurt
	 *            额外伤害加成
	 * @return
	 */
	public PveBattleIncome initMonsterBattleData(BattleConst.BattleType battleType, int pointId, int monsterId,
			List<Player> atkPlayers, List<IWorldMarch> atkMarchs, int leftPercent, int addHurt) {
		WarEff atkTroopEffType = WarEff.NO_EFF;
		WarEff defTroopEffType = WarEff.NO_EFF;
		switch (battleType) {
		case ATTACK_MONSTER:
			atkTroopEffType = WarEff.ATK_MONSTER;
			if (atkPlayers.size() > 1) {
				atkTroopEffType = WarEff.ATK_MONSTER_MASS;
			}
			break;
		case ATTACK_NEW_MONSTER:
			atkTroopEffType = WarEff.ATK_NEW_MONSTER;
			if (atkPlayers.size() > 1) {
				atkTroopEffType = WarEff.ATK_NEW_MONSTER_MASS;
			}
			break;
		default:
			break;
		}

		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
		defPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);
		atkArmyList.forEach(u -> u.setHurtPer(addHurt));
		WorldEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		// 计算剩余部队列表
		List<ArmyInfo> monsterArmyList = cfg.getArmyList();
		for (ArmyInfo army : monsterArmyList) {
			army.setPlayerId(npcPlayer.getId());
			army.setTotalCount((int) Math.ceil(1d * army.getTotalCount() * leftPercent / GsConst.RANDOM_MYRIABIT_BASE));
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());
		}
		defArmyMap.put(npcPlayer.getId(), monsterArmyList);

		List<BattleUnity> monsterList = monsterArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, new EffectParams())).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkAalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setMonsterId(monsterId)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkAalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}

	/**
	 * 初始化超级兵营/航海要塞npc战斗输入数据
	 * 
	 * @param battleType
	 * @param cfg
	 * @param atkPlayers
	 * @param atkMarchs
	 */
	public PveBattleIncome initSuperWeaponPveBattleData(BattleConst.BattleType battleType, int pointId, SuperWeaponSoldierCfg cfg, List<IWorldMarch> atkMarchs, int weaponId) {
		List<Player> atkPlayers = new ArrayList<>();
		atkMarchs.forEach(march -> { // 填充攻击方玩家
			Player atkplayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			atkPlayers.add(atkplayer);
		});
		
		WarEff atkTroopEffType = WarEff.NO_EFF;
		switch (battleType) {
		case ATTACK_SUPER_WEAPON_PVE:
			atkTroopEffType = WarEff.ATK_SUPER_WEAPON_PVE;
			if (atkMarchs.size() > 0) {
				atkTroopEffType = WarEff.ATK_SUPER_WEAPON_MASS_PVE;
			}
			break;
		case ATTACK_FORTRESS_PVE:
			atkTroopEffType = WarEff.ATK_FORTRESS_PVE;
			if (atkMarchs.size() > 0) {
				atkTroopEffType = WarEff.ATK_FORTRESS_MASS_PVE;
			}
			break;
		default:
			break;
		}
		WarEff defTroopEffType = WarEff.NO_EFF;
		// troopEffType 部队作用号类型-xx时

		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
		defPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		List<Integer> heroIds = cfg.getHeroIdList();
		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		// 防守怪物部队
		List<ArmyInfo> defArmyList = cfg.getArmyList();
		for (ArmyInfo armyInfo : defArmyList) {
			armyInfo.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());

		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);

		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heroIds);
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, effParams)).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkAalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setMonsterId(weaponId)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkAalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}
	
	/**
	 * 初始化超级兵营/航海要塞npc战斗输入数据
	 */
	public PveBattleIncome initXZQPveBattleData(BattleConst.BattleType battleType, int pointId, TemporaryMarch npcMarch, List<IWorldMarch> atkMarchs, int weaponId) {
		List<Player> atkPlayers = new ArrayList<>();
		atkMarchs.forEach(march -> atkPlayers.add(march.getPlayer()));
		
		WarEff atkTroopEffType = WarEff.NO_EFF;
		switch (battleType) {
		case ATTACK_XZQ_PVE:
			atkTroopEffType = WarEff.ATK_XZQ_PVE;
			if (atkMarchs.size() > 1) {
				atkTroopEffType = WarEff.ATK_XZQ_MASS_PVE;
			}
			break;
		default:
			break;
		}
		WarEff defTroopEffType = WarEff.NO_EFF;
		// troopEffType 部队作用号类型-xx时

		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		Player npcPlayer = npcMarch.getPlayer();
		defPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		List<Integer> heroIds = npcMarch.getHeros().stream().map(PlayerHero::getCfgId).collect(Collectors.toList());
		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		// 防守怪物部队
		List<ArmyInfo> defArmyList = new ArrayList<>();
		for (ArmyInfo armyInfo : npcMarch.getArmys()) {
			int armyCount = armyInfo.getTotalCount() - armyInfo.getDeadCount();
			if(armyCount <= 0){
				continue;
			}
			defArmyList.add(armyInfo);
			armyInfo.setTotalCount(armyCount);
			armyInfo.setDeadCount(0);
			armyInfo.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());
		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);

		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heroIds);
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, effParams)).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkAalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setMonsterId(weaponId)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkAalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}

	/**
	 * 初始化迷雾要塞战斗输入数据
	 * 
	 * @param battleType
	 * @param pointId
	 * @param foggyInfo
	 * @param atkPlayers
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initFoggyBattleData(BattleConst.BattleType battleType, WorldPoint worldPoint, List<Player> atkPlayers, List<IWorldMarch> atkMarchs) {
		FoggyInfo foggyInfo = worldPoint.getFoggyInfoObj();
		WarEff atkTroopEffType = WarEff.ATK_FOGGY;
		if (atkPlayers.size() > 1) {
			atkTroopEffType = WarEff.ATK_FOGGY_MASS;
		}
		WarEff defTroopEffType = WarEff.NO_EFF;
		// troopEffType 部队作用号类型-xx时

		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = foggyInfo.getNpcPlayer();
		defPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		List<BattleUnity> atkArmyList = buildPlayerArmyList(worldPoint.getId(), atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		// 防守怪物部队
		List<ArmyInfo> defArmyList = foggyInfo.getArmyList();
		for (ArmyInfo armyInfo : defArmyList) {
			armyInfo.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());

		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(foggyInfo.getHeroIds());
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, effParams)).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, worldPoint.getId(), atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkAalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setMonsterId(worldPoint.getMonsterId())
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkAalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}

	/**
	 * 初始化迷雾要塞战斗输入数据
	 * 
	 * @param battleType
	 * @param pointId
	 * @param foggyInfo
	 * @param atkPlayers
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initTowerGhostData(BattleConst.BattleType battleType, WorldPoint worldPoint, List<Player> atkPlayers, List<IWorldMarch> atkMarchs) {
		GhostInfo ghostInfo = worldPoint.getGhostInfo();
		WarEff atkTroopEffType = WarEff.ATK_GHOST_TOWER;
		WarEff defTroopEffType = WarEff.NO_EFF;
		// troopEffType 部队作用号类型-xx时
		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = ghostInfo.getNpcPlayer();
		defPlayers.add(npcPlayer);


		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();


		List<BattleUnity> atkArmyList = buildPlayerArmyList(worldPoint.getId(), atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);


		// 防守怪物部队
		List<ArmyInfo> defArmyList = ghostInfo.getArmyList();
		for (ArmyInfo armyInfo : defArmyList) {
			armyInfo.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());
		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(ghostInfo.getHeroIds());
		effParams.setSuperSoliderId(ghostInfo.getSuperSoldierId());//##新
		effParams.setArmourSuit(ArmourSuitType.ONE);//##新
		effParams.setMechacoreSuit(MechaCoreSuitType.MECHA_ONE);
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, effParams)).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, worldPoint.getId(), atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);


		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);


		BattleCalcParames atkAalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();


		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();


		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setMonsterId(worldPoint.getMonsterId())
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkAalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}
	/**
	 * 初始化据点PVE战斗输入信息
	 * 
	 * @param battleType
	 * @param pointId
	 * @param strongPointId
	 * @param atkPlayers
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initStrongPointPveBattleData(BattleConst.BattleType battleType, int pointId, int strongPointId,
			List<Player> atkPlayers, List<IWorldMarch> atkMarchs) {
		WarEff atkTroopEffType = WarEff.ATK_STRONG_POINT_PVE;
		WarEff defTroopEffType = WarEff.NO_EFF;
		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
		defPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		WorldStrongpointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, strongPointId);
		List<ArmyInfo> defArmyList = cfg.getArmyList();
		for (ArmyInfo army : defArmyList) {
			army.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());
		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);

		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(cfg.getHeroIdList());
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, effParams)).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);

		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setMonsterId(strongPointId)
				.setBattle(battle)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkCalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);

		return income;
	}

	/**
	 * 初始化据点PVE战斗输入信息
	 * 
	 * @param battleType
	 * @param pointId
	 * @param strongPointId
	 * @param atkPlayers
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initYuristrikeBattleData(BattleConst.BattleType battleType, int pointId, int yuristrikeId,
			List<Player> atkPlayers, List<IWorldMarch> atkMarchs, IWorldMarch defMarch) {
		WarEff atkTroopEffType = WarEff.ATK_STRONG_POINT_PVE;
		WarEff defTroopEffType = WarEff.NO_EFF;
		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
		defPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		List<ArmyInfo> defArmyList = defMarch.getArmys();
		for (ArmyInfo army : defArmyList) {
			army.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());
		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, new EffectParams())).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);

		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setMonsterId(yuristrikeId)
				.setBattle(battle)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkCalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);

		return income;
	}

	/**
	 * 初始化尤里复仇战斗输入信息
	 * 
	 * @param battleType
	 * @param pointId
	 * @param monsterId
	 * @param atkPlayers
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initYuriRevengeBattleData(BattleConst.BattleType battleType, int pointId, List<ArmyInfo> monsterArmys, List<Player> defPlayers,
			List<IWorldMarch> defMarchs) {
		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();

		List<Player> atkPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
		atkPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();
		WarEff atkTroopEffType = WarEff.NO_EFF;
		WarEff defTroopEffType = WarEff.DEF_YURI_REVENGE;
		if (defPlayers.size() > 1) {
			defTroopEffType = WarEff.DEF_YURI_REVENGE_MASS;
		}

		// 进攻方部队信息处理(怪物)
		for (ArmyInfo info : monsterArmys) {
			info.setPlayerId(npcPlayer.getId());
		}
		atkArmyMap.put(npcPlayer.getId(), monsterArmys);

		List<BattleUnity> defArmyList = buildPlayerArmyList(pointId, defMarchs, defPlayers.get(0), defTypeSet, defArmyMap, defTroopEffType);
		List<BattleUnity> monsterList = monsterArmys.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, new EffectParams())).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(monsterList, defArmyList, atkTroopEffType, atkTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(defArmyList, monsterList, defTroopEffType, defTypeSet);

		// 构建战斗
		Battle battle = new Battle(defPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), defPlayers.get(0).getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkCalcParames = BattleCalcParames.newBuilder()
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder()
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkCalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}
	
	/**
	 * 初始化联盟机甲舱体守卫战斗输入信息
	 * 
	 * @param battleType
	 * @param pointId
	 * @param monsterId
	 * @param atkPlayers
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initGuildSpaceBattleData(BattleConst.BattleType battleType, int pointId, List<ArmyInfo> monsterArmys, List<Player> defPlayers,
			List<IWorldMarch> defMarchs, List<Integer> heroIds, int origionId, int enemyId) {
		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();

		List<Player> atkPlayers = new ArrayList<>();
		SpaceWorldPoint spacePoint = (SpaceWorldPoint)WorldPointService.getInstance().getWorldPoint(pointId);
		NpcPlayer npcPlayer = (NpcPlayer)spacePoint.getNpcPlayer(origionId);
		if (npcPlayer == null) {
			int[] origionPos = GameUtil.splitXAndY(origionId);
			HawkLog.errPrintln("spaceMecha init space battleData, guildId: {}, pointId: {}, origionId: {} {}-{}", spacePoint.getGuildId(), pointId, origionId, origionPos[0], origionPos[1]);
			npcPlayer = new NpcPlayer(HawkXID.nullXid());
			npcPlayer.setPlayerId(origionPos[0] + "_" + origionPos[1]);
			List<PlayerHero> heroList = new ArrayList<PlayerHero>();
			heroIds.stream().forEach(heroId -> heroList.add(NPCHeroFactory.getInstance().get(heroId)));
			npcPlayer.setHeros(heroList);
		}
		
		// 加作用号
		SpaceMechaEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
		if (enemyCfg != null) {
			for(Entry<Integer, Integer> entry : enemyCfg.getSoldierEffectMap().entrySet()) {
				EffType effType = EffType.valueOf(entry.getKey());
				if (effType != null) {
					npcPlayer.addEffectVal(effType, entry.getValue());
				}
			}
		}
		
		atkPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();
		WarEff atkTroopEffType = WarEff.NO_EFF;
		WarEff defTroopEffType = WarEff.DEF_SPACE_MECHA;
		if (defPlayers.size() > 1) {
			defTroopEffType = WarEff.DEF_SPACE_MECHA_MASS;
		}

		// 进攻方部队信息处理(怪物)
		for (ArmyInfo info : monsterArmys) {
			info.setPlayerId(npcPlayer.getId());
		}
		atkArmyMap.put(npcPlayer.getId(), monsterArmys);

		List<BattleUnity> defArmyList = buildPlayerArmyList(pointId, defMarchs, defPlayers.get(0), defTypeSet, defArmyMap, defTroopEffType);
		
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heroIds);
		final NpcPlayer npc = npcPlayer;
		List<BattleUnity> monsterList = monsterArmys.stream().map(army -> BattleUnity.valueOf(npc, army, effParams)).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(monsterList, defArmyList, atkTroopEffType, atkTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(defArmyList, monsterList, defTroopEffType, defTypeSet);

		// 构建战斗
		Battle battle = new Battle(defPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), defPlayers.get(0).getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(defArmyList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();
		
		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkCalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}

	/**
	 * 初始化幽灵行军战斗输入信息
	 * 
	 * @param battleType
	 * @param pointId
	 * @param monsterId
	 * @param atkPlayers
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initGhostMarchBattleData(BattleConst.BattleType battleType, int pointId, IWorldMarch atkMarch, List<Player> defPlayers,
			List<IWorldMarch> defMarchs) {
		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();

		List<Player> atkPlayers = new ArrayList<>();
		Player npcPlayer = atkMarch.getPlayer();
		atkPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();
		WarEff atkTroopEffType = WarEff.NO_EFF;
		WarEff defTroopEffType = WarEff.DEF_GHOST_MARCH;
		if (defPlayers.size() > 1) {
			defTroopEffType = WarEff.DEF_GHOST_MARCH_MASS;
		}
		List<ArmyInfo> atkArmys = atkMarch.getArmys();
		// 进攻方部队信息处理(怪物)
		for (ArmyInfo info : atkArmys) {
			info.setPlayerId(npcPlayer.getId());
		}
		atkArmyMap.put(npcPlayer.getId(), atkArmys);

		List<BattleUnity> defArmyList = buildPlayerArmyList(pointId, defMarchs, defPlayers.get(0), defTypeSet, defArmyMap, defTroopEffType);
		List<BattleUnity> monsterList = atkArmys.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, new EffectParams())).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(monsterList, defArmyList, atkTroopEffType, atkTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(defArmyList, monsterList, defTroopEffType, defTypeSet);

		// 构建战斗
		Battle battle = new Battle(defPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), defPlayers.get(0).getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkCalcParames = BattleCalcParames.newBuilder()
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder()
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkCalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}

	/**
	 * 初始化机甲BOSS战斗输入数据
	 * 
	 * @param battleType
	 * @param cfg
	 * @param atkPlayers
	 * @param atkMarchs
	 */
	public PveBattleIncome initGundamBattleData(BattleConst.BattleType battleType, int pointId, int monsterId, List<ArmyInfo> monsterArmyList, List<IWorldMarch> atkMarchs) {
		List<Player> atkPlayers = new ArrayList<>();
		atkMarchs.forEach(march -> { // 填充攻击方玩家
			Player atkplayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			atkPlayers.add(atkplayer);
		});

		WarEff atkTroopEffType = WarEff.ATK_GUNDAM_PVE;
		if (atkMarchs.size() > 0) {
			atkTroopEffType = WarEff.ATK_GUNDAM_MASS_PVE;
		}
		WarEff defTroopEffType = WarEff.NO_EFF;

		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
		List<EffectObject> nianParamEffectList = WorldMapConstProperty.getInstance().getNianParamEffectList();
		for (EffectObject eff :  nianParamEffectList) {
			int effValue = (int)WorldNianService.getInstance().getEffValue(eff);
			if (effValue > 0) {
				npcPlayer.addEffectVal(eff.getType(), effValue);
			}
		}
		
		defPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		// 防守怪物部队
		List<ArmyInfo> defArmyList = monsterArmyList;
		for (ArmyInfo armyInfo : defArmyList) {
			armyInfo.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());

		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, new EffectParams())).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkAalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setMonsterId(monsterId)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkAalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}
	
	
	

	/**
	 * 巨龙来袭
	 * 
	 * @param battleType
	 * @param cfg
	 * @param atkPlayers
	 * @param atkMarchs
	 */
	public PveBattleIncome initGuildDragonAttackBattleData(BattleConst.BattleType battleType, int pointId, int monsterId, List<ArmyInfo> monsterArmyList, List<IWorldMarch> atkMarchs) {
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		List<Player> atkPlayers = new ArrayList<>();
		atkMarchs.forEach(march -> { // 填充攻击方玩家
			Player atkplayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			atkPlayers.add(atkplayer);
			march.getMarchEntity().getEffectParams().addExtEff(cfg.getPlayerEffectMap());
		});

		WarEff atkTroopEffType = WarEff.ATK_GUNDAM_PVE;
		if (atkMarchs.size() > 0) {
			atkTroopEffType = WarEff.ATK_GUNDAM_MASS_PVE;
		}
		WarEff defTroopEffType = WarEff.NO_EFF;
		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
		for (Map.Entry<Integer, Integer> effEntry :  cfg.getEnemyEffectMap().entrySet()) {
			int effId = effEntry.getKey();
			int effValue = effEntry.getValue();
			EffType effType = EffType.valueOf(effId);
			if(Objects.nonNull(effType)){
				npcPlayer.addEffectVal(effType, effValue);
			}
		}
		
		defPlayers.add(npcPlayer);
		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		// 防守怪物部队
		List<ArmyInfo> defArmyList = monsterArmyList;
		for (ArmyInfo armyInfo : defArmyList) {
			armyInfo.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());

		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, new EffectParams())).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkAalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setMonsterId(monsterId)
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkAalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}
	
	/**
	 * 构建星甲召唤据点战斗输入数据
	 * 
	 * @param battleType
	 * @param pointId
	 * @param monsterId
	 * @param monsterArmyList
	 * @param atkMarchs
	 * @return
	 */
	public PveBattleIncome initStrongHoldBattleData(BattleConst.BattleType battleType, StrongHoldWorldPoint strongPoint, List<IWorldMarch> atkMarchs, List<Player> atkPlayers) {
		WarEff atkTroopEffType = WarEff.ATK_STRONG_HOLD_PVE;
		if (atkMarchs.size() > 0) {
			atkTroopEffType = WarEff.ATK_STRONG_HOLD_MASS_PVE;
		}
		WarEff defTroopEffType = WarEff.NO_EFF;

		// 据点配置
		SpaceMechaStrongholdCfg strongHoldCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, strongPoint.getStrongHoldId());

		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();
		List<Player> defPlayers = new ArrayList<>();
		List<Integer> heroIds = strongHoldCfg.getHeroIdList();
		List<PlayerHero> heroList = new ArrayList<>();
		heroIds.stream().forEach(heroId -> heroList.add(NPCHeroFactory.getInstance().get(heroId)));
		NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
		npcPlayer.setPlayerPos(strongPoint.getId());
		npcPlayer.setHeros(heroList);
		// 加作用号
		for(Entry<Integer, Integer> entry : strongHoldCfg.getSoldierEffectMap().entrySet()) {
			EffType effType = EffType.valueOf(entry.getKey());
			if (effType != null) {
				npcPlayer.addEffectVal(effType, entry.getValue());
			}
		}
		defPlayers.add(npcPlayer);

		// 部队类型列表
		Set<SoldierType> atkTypeSet = new HashSet<>();
		Set<SoldierType> defTypeSet = new HashSet<>();

		int pointId = strongPoint.getId();
		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		List<ArmyInfo> defArmyList = strongPoint.getDefArmyList();
		for (ArmyInfo armyInfo : defArmyList) {
			armyInfo.setPlayerId(npcPlayer.getId());
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			defTypeSet.add(soldierCfg.getSoldierType());

		}
		defArmyMap.put(npcPlayer.getId(), defArmyList);
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heroIds);
		List<BattleUnity> monsterList = defArmyList.stream().map(army -> BattleUnity.valueOf(npcPlayer, army, effParams)).collect(Collectors.toList());
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, monsterList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(monsterList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayers.get(0).getId(), battleType, pointId, atkPlayers.get(0).getId(), npcPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);

		BattleCalcParames atkAalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.build();

		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(monsterList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.build();

		PveBattleIncome income = new PveBattleIncome()
				.setBattle(battle)
				.setMonsterId(strongPoint.getId())
				.setAtkPlayers(atkPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setAtkCalcParames(atkAalcParames)
				.setDefPlayers(defPlayers)
				.setDefArmyMap(defArmyMap)
				.setDefCalcParames(defCalcParames);
		return income;
	}

	/**
	 * 初始化PVP战斗数据
	 */
	public PvpBattleIncome initPVPBattleData(BattleConst.BattleType battleType, int pointId, List<Player> atkPlayers, List<Player> defPlayers, List<IWorldMarch> atkMarchs,
			List<IWorldMarch> defMarchs) {
		return initPVPBattleData(battleType, pointId, atkPlayers, defPlayers, atkMarchs, defMarchs, BattleSkillType.BATTLE_SKILL_NONE, false, false);
	}
	/**
	 * 初始化PVP战斗数据
	 */
	public PvpBattleIncome initPVPBattleData(BattleConst.BattleType battleType, int pointId, List<Player> atkPlayers, List<Player> defPlayers, List<IWorldMarch> atkMarchs,
			List<IWorldMarch> defMarchs, boolean isAtkOwner, boolean isDefOwner) {
		return initPVPBattleData(battleType, pointId, atkPlayers, defPlayers, atkMarchs, defMarchs, BattleSkillType.BATTLE_SKILL_NONE, isAtkOwner, isDefOwner);
	}
	/**
	 * 初始化PVP战斗数据
	 */
	public PvpBattleIncome initPVPBattleData(BattleConst.BattleType battleType, int pointId, List<Player> atkPlayers, List<Player> defPlayers, List<IWorldMarch> atkMarchs,
			List<IWorldMarch> defMarchs,  BattleSkillType skillType) {
		return initPVPBattleData(battleType, pointId, atkPlayers, defPlayers, atkMarchs, defMarchs, skillType, false, false);
	}

	/**
	 * 初始化PVP战斗数据
	 * 
	 * @param battleType
	 *            战斗类型 @see BattleConst.TYPE_
	 * @param pointId
	 *            战斗地点坐标
	 * @param atkMarchs
	 *            进攻方行军列表
	 * @param defMarchs
	 *            防御方行军列表
	 * @param defPlayer
	 *            基地防守战防御方玩家,其他类型战斗传null
	 * @param atkPos
	 *            进攻方坐标
	 * @param defPos
	 *            防御方坐标
	 * @return
	 */
	public PvpBattleIncome initPVPBattleData(BattleConst.BattleType battleType, int pointId, List<Player> atkPlayers, List<Player> defPlayers,
			List<IWorldMarch> atkMarchs, List<IWorldMarch> defMarchs, BattleSkillType skillType, boolean isAtkOwner, boolean isDefOwner) {
		Player atkPlayer = atkPlayers.get(0);
		Player defPlayer = defPlayers.get(0);
		boolean massAtk = atkPlayers.size() > 1;
		boolean massDef = defPlayers.size() > 1;
		// 是否同一区服玩家的战斗
		String atkMainServerId = GlobalData.getInstance().getMainServerId(atkPlayer.getServerId());
		String defMainServerId = GlobalData.getInstance().getMainServerId(defPlayer.getServerId());
		boolean isSameServer = atkMainServerId.equals(defMainServerId);
		// 是否在副本中
		boolean isInDungeonMap = atkPlayer.isInDungeonMap();

		// troopEffType 部队作用号类型-xx时
		BattleConst.WarEff atkTroopEffType = BattleConst.WarEff.ATK;
		BattleConst.WarEff defTroopEffType = BattleConst.WarEff.DEF;

		if (massAtk) {
			atkTroopEffType = BattleConst.WarEff.ATK_MASS;
		}

		if (massDef) {
			defTroopEffType = BattleConst.WarEff.DEF_MASS;
		}
		switch (battleType) {
		case ATTACK_CITY:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_CITY_MASS;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_CITY;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_CITY_MASS;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_CITY;
			}
			break;
		case ATTACK_RES:
			atkTroopEffType = BattleConst.WarEff.ATK_RES;
			defTroopEffType = BattleConst.WarEff.DEF_RES;
			break;
		case ATTACK_QUARTERED:
			atkTroopEffType = BattleConst.WarEff.ATK_QUARTERED;
			defTroopEffType = BattleConst.WarEff.DEF_QUARTERED;
			break;
		case ATTACK_MANOR:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_MANOR_MASS;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_MANOR;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_MANOR_MASS;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_MANOR;
			}
			break;
		case RECOVER_MANOR:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_RECOVER_MANOR_MASS;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_RECOVER_MANOR;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_RECOVER_MANOR_MASS;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_RECOVER_MANOR;
			}
			break;
		case ATTACK_PRESIDENT:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_PRESIDENT_MASS;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_PRESIDENT;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_PRESIDENT_MASS;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_PRESIDENT;
			}
			break;
		case ATTACK_PRESIDENT_TOWER:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_PRESIDENT_TOWER_MASS;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_PRESIDENT_TOWER;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_PRESIDENT_TOWER_MASS;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_PRESIDENT_TOWER;
			}
			break;
		case ATTACK_STRONG_POINT_PVP:
			atkTroopEffType = BattleConst.WarEff.ATK_STRONG_POINT_PVP;
			defTroopEffType = BattleConst.WarEff.DEF_STRONG_POINT_PVP;
			break;
		case ATTACK_SUPER_WEAPON_PVP:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_SUPER_WEAPON_MASS_PVP;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_SUPER_WEAPON_PVP;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_SUPER_WEAPON_MASS;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_SUPER_WEAPON;
			}
			break;
		case ATTACK_XZQ_PVP:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_XZQ_MASS_PVP;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_XZQ_PVP;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_XZQ_MASS;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_XZQ;
			}
			break;
		case ATTACK_TREASURE_HUNT_RES:
			atkTroopEffType = BattleConst.WarEff.ATK_TREASURE_HUNT_RES_PVP;
			defTroopEffType = BattleConst.WarEff.DEF_TREASURE_HUNT_RES_PVP;
			break;
		case ATTACK_WAR_FLAG:
			atkTroopEffType = BattleConst.WarEff.ATTACK_WAR_FLAG_PVP;
			defTroopEffType = BattleConst.WarEff.DEF_WAR_FLAG_PVP;
			break;
		case ATTACK_FORTRESS_PVP:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_FORTRESS_MASS_PVP;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_FORTRESS_PVP;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_FORTRESS_MASS_PVP;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_FORTRESS_PVP;
			}
			break;
		case ATTACK_TBLY_BUILD:
			if (massAtk) {
				atkTroopEffType = BattleConst.WarEff.ATK_TBLY_BUILD_MASS_PVP;
			} else {
				atkTroopEffType = BattleConst.WarEff.ATK_TBLY_BUILD_PVP;
			}
			if (massDef) {
				defTroopEffType = BattleConst.WarEff.DEF_TBLY_BUILD_MASS_PVP;
			} else {
				defTroopEffType = BattleConst.WarEff.DEF_TBLY_BUILD_PVP;
			}
			break;
		case ATTACK_TBLY_RES:
			atkTroopEffType = BattleConst.WarEff.ATK_TBLY_RES;
			defTroopEffType = BattleConst.WarEff.DEF_TBLY_RES;
			break;
		case GUILD_CHAMPIONSHIP:
			atkTroopEffType = BattleConst.WarEff.NO_EFF;
			defTroopEffType = BattleConst.WarEff.NO_EFF;
			break;
		case SIMULATE_WAR:
			atkTroopEffType = BattleConst.WarEff.NO_EFF;
			defTroopEffType = BattleConst.WarEff.NO_EFF;
			break;
		case PYLON_WAR:
			atkTroopEffType = BattleConst.WarEff.ATK_PYLON;
			defTroopEffType = BattleConst.WarEff.DEF_PYLON;
			break;
		default:
			break;
		}

		// 守护玩家 
		if (WarEff.CITY_DEF.check(defTroopEffType) && defPlayer.getEffect().getEffVal(EffType.EFF_11044)>0) {
			TemporaryMarch guardMarch = guardPlayerMarch(pointId, defPlayer, defTroopEffType);
			if (Objects.nonNull(guardMarch)){
				defPlayers.add(guardMarch.getPlayer());
				defMarchs.add(guardMarch);
			}
		}
		
		
		// 部队信息
		Map<String, List<ArmyInfo>> atkArmyMap = new HashMap<>();
		Map<String, List<ArmyInfo>> defArmyMap = new HashMap<>();

		// 部队类型列表
		EnumSet<SoldierType> atkTypeSet = EnumSet.noneOf(SoldierType.class);
		EnumSet<SoldierType> defTypeSet = EnumSet.noneOf(SoldierType.class);
		List<BattleUnity> atkArmyList = buildPlayerArmyList(pointId, atkMarchs, null, atkTypeSet, atkArmyMap, atkTroopEffType);

		List<BattleUnity> defArmyList;
		// 是否开启决斗
		boolean isDuel = skillType == BattleSkillType.BATTLE_SKILL_DUEL;
		if (isDuel) {
			defArmyList = buildSortedDuelArmyList(defPlayer, defTypeSet, defArmyMap, atkArmyList);
			atkArmyList.forEach(unit -> unit.setDuel(isDuel));
			defArmyList.forEach(unit -> unit.setDuel(isDuel));
		} else {
			defArmyList = buildPlayerArmyList(pointId, defMarchs, defPlayer, defTypeSet, defArmyMap, defTroopEffType);
		}
		
		// 构建参战数据
		List<BattleSoldier> atkSoldiers = buildBattleSoldierList(atkArmyList, defArmyList, atkTroopEffType, defTypeSet);
		List<BattleSoldier> defSoldiers = buildBattleSoldierList(defArmyList, atkArmyList, defTroopEffType, atkTypeSet);

		// 构建战斗
		Battle battle = new Battle(atkPlayer.getId(), battleType, pointId, atkPlayer.getId(), defPlayer.getId());
		battle.addAtkSoldier(atkSoldiers);
		battle.addDefSoldier(defSoldiers);

		battle.getAttacker().setWarEff(atkTroopEffType);
		battle.getDefencer().setWarEff(defTroopEffType);
		double defDisHurtRate = 0;
		// 是否触发屠城(防御方所有伤兵变成死兵)
		BattleUnity unity = atkArmyList.get(0);
		boolean isButcher = WarEff.SELF_ATK.check(atkTroopEffType)
				&& HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < unity.getEffVal(EffType.WAR_SELF_MARCH_BUTCHER);
		if (isButcher) {
			defDisHurtRate = GsConst.EFF_RATE;
		} else {
			// 判定是否触发屠城(1060作用号,防御方部分伤兵转变成死兵)
			int rate = ConstProperty.getInstance().getEffect1060();
			if (WarEff.SELF_ATK.check(atkTroopEffType) && HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < rate) {
				defDisHurtRate = unity.getEffVal(EffType.WAR_SELF_ATK_BUTCHER);
			}
			// 单人攻城生效
			if (WarEff.ATK_CITY.check(atkTroopEffType)) {
				defDisHurtRate += unity.getEffVal(EffType.SELF_ATK_CITY_HURT_TO_DEAD);
			}
			// 跨服活动开启时攻城
			if (WarEff.CITY_ATK.check(atkTroopEffType) && CrossActivityService.getInstance().isOpen()) {
				defDisHurtRate += unity.getEffVal(EffType.CROSST_1254);
			}
			if (WarEff.CITY_DEF.check(defTroopEffType) && defPlayer.getEffect().getEffVal(EffType.CITY_ENEMY_MARCH_SPD) > 0) {
				defDisHurtRate -= defPlayer.getEffect().getEffVal(EffType.HERO_1630);
			}
			if (WarEff.CITY_ATK.check(atkTroopEffType) ) {
				defDisHurtRate += unity.getSolider().getEffVal(EffType.HERO_1649);
			}
		}

		BattleCalcParames atkCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(atkArmyList)
				.setAtk(true)
				.setWarEff(atkTroopEffType)
				.setDuel(isDuel)
				.setLifeSaving(skillType == BattleSkillType.BATTLE_SKILL_LIFESAVING)
				.setIsSameServer(isSameServer)
				.setIsOwner(isAtkOwner)
				.setInDungeonMap(isInDungeonMap)
				.setDecDieBecomeInjury(isDuel ? defPlayer.getEffect().getEffVal(EffType.HERO_1609) : 0)
				.build();
		BattleCalcParames defCalcParames = BattleCalcParames.newBuilder().addPlayerBattleUnity(defArmyList)
				.setAtk(false)
				.setWarEff(defTroopEffType)
				.setDuel(isDuel)
				.setDisHurtRate(defDisHurtRate)
				.setIsSameServer(isSameServer)
				.setIsOwner(isDefOwner)
				.setInDungeonMap(isInDungeonMap)
				.setDecDieBecomeInjury(isDuel ? atkPlayer.getEffect().getEffVal(EffType.HERO_1609) : 0)
				.build();
		PvpBattleIncome battleIncome = new PvpBattleIncome()
				.setBattle(battle)
				.setAtkPlayers(atkPlayers)
				.setDefPlayers(defPlayers)
				.setAtkArmyMap(atkArmyMap)
				.setDefArmyMap(defArmyMap)
				.setAtkCalcParames(atkCalcParames)
				.setDefCalcParames(defCalcParames)
				.setGrabResMarch(atkMarchs.get(0).isGrabResMarch())
				.setSkillType(skillType);
		return battleIncome;
	}

	/**守护玩家召唤*/
	private TemporaryMarch guardPlayerMarch(int pointId, Player defPlayer, BattleConst.WarEff defTroopEffType) {
		try {
			String guardPlayerId = RelationService.getInstance().getGuardPlayer(defPlayer.getId());
			if (HawkOSOperator.isEmptyString(guardPlayerId) || defPlayer.isInDungeonMap()) {
				return null;
			}
			List<ArmyInfo> defArmys = ArmyService.getInstance().getFreeArmyList(defPlayer);
			int max = defArmys.stream().filter(a -> a.getType().getNumber() <= 8).mapToInt(a -> a.getFreeCnt()).sum();
			max = Math.min(max, ConstProperty.getInstance().getEffect11044MaxNum());

			Player guardPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);

			List<ArmyInfo> guardarmys = ArmyService.getInstance().getFreeArmyList(guardPlayer);
			guardarmys.sort(Comparator.comparingInt((ArmyInfo a)-> a.getLevel()).reversed().thenComparing((ArmyInfo a) -> a.getType().getNumber()));
			int guiardNum = guardarmys.stream().mapToInt(ArmyInfo::getFreeCnt).sum();
			max = (int) Math.min(max, defPlayer.getEffect().getEffVal(EffType.EFF_11044)* GsConst.EFF_PER * guiardNum);
			List<ArmyInfo> armys = new ArrayList<>();
			for (ArmyInfo ar : guardarmys) {
				if (max <= 0) {
					break;
				}
				if (ar.getType().getNumber() > 8) {
					continue;
				}
				int count = Math.min(max, ar.getFreeCnt());
				armys.add(new ArmyInfo(ar.getArmyId(), count));
				max -= count;
			}

			EffectParams effParams = cityDefEffParames(pointId, defPlayer, defTroopEffType);
			GuarderPlayer shouhu = new GuarderPlayer(HawkXID.nullXid());
			shouhu.setName("神龙守护者");
			shouhu.setPlayer(defPlayer);
			shouhu.setGuarder(guardPlayer);

			TemporaryMarch atkMarch = new TemporaryMarch();
			atkMarch.setPlayer(shouhu);
			atkMarch.setArmys(armys);

			WorldMarch march = atkMarch.getMarchEntity();
			march.setEffParams(effParams);
			march.setSuperSoldierId(effParams.getSuperSoliderId());
			march.setHeroIdList(effParams.getHeroIds());
			return atkMarch;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * PVP战斗接口
	 * 
	 * @param income
	 *            战斗输入数据
	 * @return
	 */
	public BattleOutcome doBattle(IBattleIncome income) {
		long beginTimeMs = HawkTime.getMillisecond();
		income.getBattle().setDuntype(income.getDuntype());
		income.getBattle().warfare();
		HawkLog.debugPrintln("battlefield calc costtime: {}", HawkTime.getMillisecond() - beginTimeMs);
		BattleOutcome battleOutcome = income.gatherBattleResult();
		battleOutcome.setDuntype(income.getDuntype());
		// 战斗安全日志
		try {
			String atkPlayers = "";
			String defPlayers = "";
			for (Player player : income.getAtkPlayers()) {
				atkPlayers = atkPlayers + player.getStaffOffic().getStaffVal() + "_" + player.getId() + "_" + income.getAtkPlayerHeros(player.getId()) + "_"
						+ income.getAtkPlayerSuperSoldier(player.getId()) + "_" + income.getAtkPlayerArmourSuitId(player.getId()) + ",";
			}
			for (Player player : income.getDefPlayers()) {
				defPlayers = defPlayers + player.getStaffOffic().getStaffVal() + "_" + player.getId() + "_" + income.getDefPlayerHeros(player.getId()) + "_"
						+ income.getDefPlayerSuperSoldier(player.getId()) + "_" + income.getDefPlayerArmourSuitId(player.getId()) + ",";
			}
			BehaviorLogger.log4Service(null, Source.WORLD_ACTION, Action.DO_BATTLE,
					Params.valueOf("battleType", income.getBattle().getType()),
					Params.valueOf("atkPlayers", atkPlayers),
					Params.valueOf("atkArmys", battleOutcome.genBattleArmyMapAtkStr()),
					Params.valueOf("isAtkMass", income.isMassAtk()),
					Params.valueOf("defPlayers", defPlayers),
					Params.valueOf("defArmys", battleOutcome.genBattleArmyMapDefStr()),
					Params.valueOf("isDefMass", income.isAssitanceDef()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 记录战斗部队信息日志, 尤里复仇战斗的战斗结果不可靠，需要根据战斗结果做进一步计算，所以这里将其过滤掉，走单独记
		if (income.getBattle().getType() != BattleConst.BattleType.YURI_YURIREVENGE && income.getBattle().getType() != BattleConst.BattleType.GUILD_CHAMPIONSHIP
				&& income.getBattle().getType() != BattleConst.BattleType.SIMULATE_WAR) {
			BattleLogHelper battleLogHelper = new BattleLogHelper(income, battleOutcome);
			battleLogHelper.logBattleFlow();
		}
		for (Player player : income.getAtkPlayers()) {
			List<Integer> heros = income.getAtkPlayerHeros(player.getId());
			player.getHeroByCfgId(heros).forEach(hero -> hero.afterBattle(income, battleOutcome));
		}
		for (Player player : income.getDefPlayers()) {
			List<Integer> heros = income.getDefPlayerHeros(player.getId());
			player.getHeroByCfgId(heros).forEach(hero -> hero.afterBattle(income, battleOutcome));
		}

		return battleOutcome;
	}

	/**
	 * 根据参战方类型及行军点类型获取玩家伤兵转换比例
	 * 
	 * @param playerId
	 * @param troopType
	 *            TROOP_TYPE_ATK_CITY
	 * @param pointType
	 * @param lifeSaving
	 *            是否使用战场救援
	 * @param duel
	 *            是否决斗
	 * @return
	 */
	private int getHurtRate(String playerId, BattleCalcParames calcParames) {
		double hurtRate = 0; // 伤兵转换率：攻城攻击,攻城防守,资源点,其他

		WarEff eff = calcParames.warEff;
		// 部队作用号类型 -根据战斗类型,计算基础伤兵转化率
		// 基地-进攻死,防御伤
		if (WarEff.CITY.check(eff)) {
			if (WarEff.ATK.check(eff)) {
				hurtRate = 0;
			} else if (WarEff.DEF.check(eff))
				hurtRate = GsConst.EFF_RATE;
		}
		// 资源田-双方伤
		else if (WarEff.RES.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 怪物-双方伤
		else if (WarEff.MONSTER.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 驻扎点-双方伤
		else if (WarEff.QUARTERED.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 总统府-双方死
		else if (WarEff.PRESIDENT.check(eff)) {
			hurtRate = 0;
		}
		// 总统府电塔-双方死
		else if (WarEff.PRESIDENT_TOWER.check(eff)) {
			hurtRate = 0;
		}
		// 联盟领地-本盟伤,其他盟死
		else if (WarEff.MANOR.check(eff)) {
			if(calcParames.isOwner){
				hurtRate = GsConst.EFF_RATE;
			}
			else{
				hurtRate = 0;
			}
		}
		// 据点-双方伤
		else if (WarEff.STRONG_POINT.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 超级武器-双方伤
		else if (WarEff.SUPER_WEAPON.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 超级武器-双方伤
		else if (WarEff.XZQ.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 寻宝资源点-双方伤
		else if (WarEff.TREASURE_HUNT_RES.check(eff)){
			hurtRate = GsConst.EFF_RATE;
		}
		// 联盟旗帜-双方伤
		else if (WarEff.WAR_FLAG.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 航海要塞-双方伤
		else if (WarEff.FROTRESS.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 泰伯利亚建筑-双方伤
		else if (WarEff.WAR_TBLY_BUILD.check(eff)) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 能量塔战斗-双方伤
		else if(WarEff.WAR_PYLON.check(eff)){
			hurtRate = GsConst.EFF_RATE;
		}


		// 技能效果-伤兵转化率计算
		// 使用救援技能,进攻方死兵完全转伤兵
		if (calcParames.lifeSaving) {
			hurtRate = GsConst.EFF_RATE;
		}

		// 如果为决斗,则不进行伤兵转化
		if (calcParames.duel) {
			hurtRate = ConstProperty.getInstance().getDieBecomeInjury() - calcParames.decDieBecomeInjury;
		}

		// 伤兵转化率数值校正
		// 最高转换比率为100%
		if (hurtRate > GsConst.EFF_RATE) {
			hurtRate = GsConst.EFF_RATE;
		}
		// 最低伤兵转化率为0%
		else if (hurtRate < 0) {
			hurtRate = 0;
		}
		return (int) hurtRate;
	}

	/**
	 * 构建决斗时防御方部队信息
	 * 
	 * @param defPlayer
	 * @param defTypeSet
	 * @param defArmyMap
	 * @param atkArmys
	 *            进攻方部队列表
	 * @return
	 */
	private List<BattleUnity> buildSortedDuelArmyList(Player defPlayer, Set<SoldierType> defTypeSet, Map<String, List<ArmyInfo>> defArmyMap, List<BattleUnity> atkArmys) {
		List<Integer> heros = new ArrayList<>();
		int superSoldierId =  0;
		int armourSuit = defPlayer.getEntity().getArmourSuit();
		int mechaSuit = defPlayer.getPlayerMechaCore().getWorkSuit();
		int superLab = defPlayer.getData().getPlayerEntity().getLaboratory();
		int talent = defPlayer.getData().getPlayerEntity().getTalentType();
		Map<Integer,Integer> presetMap = new HashMap<>();// 预设计数
		Skill10104 skill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10104);
		PlayerPresetMarchInfo.Builder infos = GameUtil.makeMarchPresetBuilder(defPlayer.getId());
		Optional<PresetMarchInfo> preSetInfo = infos.getMarchInfosList().stream().filter(pre -> pre.getIdx() == GsConst.DUEL_INDEX).findAny();
		PlayerHero duelHero1 = null; // 玩家实际设置的决斗英雄
		PlayerHero duelHero2 = null;
		int manhattanAtkSwId = 0;
		int manhattanDefSwId = 0;
		if (preSetInfo.isPresent()) { // 玩家有预设置
			PresetMarchInfo info = preSetInfo.get();
			final int max = skill.maxSoldier(defPlayer);
			for (ArmySoldierPB arp : info.getArmyList()) {
				int count = arp.getCount();
				if (info.getPercentArmy()) {
					count = (int) (1D * max / 1000000 * count) + 1;
				}
				presetMap.put(arp.getArmyId(), count);
			}

			List<PlayerHero> preSetHeros = defPlayer.getHeroByCfgId(info.getHeroIdsList());
			duelHero1 = preSetHeros.size() > 0 ? preSetHeros.get(0) : null;
			duelHero2 = preSetHeros.size() > 1 ? preSetHeros.get(1) : null;
			if (Objects.nonNull(duelHero1) && duelHero1.getState() != PBHeroState.HERO_STATE_MARCH) {
				heros.add(duelHero1.getCfgId());
			}
			if (Objects.nonNull(duelHero2) && duelHero2.getState() != PBHeroState.HERO_STATE_MARCH) {
				heros.add(duelHero2.getCfgId());
			}

			Optional<SuperSoldier> suOf = defPlayer.getSuperSoldierByCfgId(info.getSuperSoldierId());
			if (suOf.isPresent()) {
				SuperSoldier sus = suOf.get();
				superSoldierId = sus.getState() == PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE ? sus.getCfgId() : 0;
			}
			if (info.hasArmourSuit() && info.getArmourSuit().getNumber() > 0) {
				armourSuit = info.getArmourSuit().getNumber();
			}
			if (info.hasMechacoreSuit()) {
				mechaSuit = info.getMechacoreSuit().getNumber();
			}
			
			if(info.hasTalentType()){
				talent = info.getTalentType().getNumber();
			}
			if(info.hasSuperLab()){
				superLab = info.getSuperLab();
			}
			if(info.hasManhattan()){
				manhattanAtkSwId = info.getManhattan().getManhattanAtkSwId();
				manhattanDefSwId = info.getManhattan().getManhattanDefSwId();
			}
		} 
		if (heros.size() < 2) {
			List<Integer> houbu = defCityHeros(defPlayer).stream().map(PlayerHero::getCfgId).collect(Collectors.toList());
			for (int heroId : houbu) {
				if (!heros.contains(heroId) && heros.size() < 2) {
					heros.add(heroId);
				}
			}
		}
		if (superSoldierId == 0) {
			Optional<SuperSoldier> ss = defCitySuperSoldier(defPlayer);
			superSoldierId = ss.isPresent() ? ss.get().getCfgId() : 0;
		}
		
		Map<Integer, Integer> armyIdCount = new HashMap<>();
		for (ArmyEntity armyEntity : defPlayer.getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
			if (cfg != null && !cfg.isDefWeapon()) {
				armyIdCount.put(armyEntity.getArmyId(), armyEntity.getFree());
			}
		}
		
		int maxArmy = skill.maxSoldier(defPlayer);
		for(Entry<Integer,Integer> pre: presetMap.entrySet()){
			int armyId = pre.getKey();
			int preSet = Math.max(0, pre.getValue());

			int freeCount = armyIdCount.getOrDefault(armyId, 0);
			int marchCount = Math.min(freeCount, preSet);
			marchCount = Math.min(marchCount, maxArmy);
			presetMap.put(armyId, marchCount);
			armyIdCount.merge(armyId, marchCount, (v1,v2)-> v1 -v2);
			maxArmy -= marchCount;
		}
		if (maxArmy > 0) {
			for (Entry<Integer, Integer> left : armyIdCount.entrySet()) {
				int armyId = left.getKey();
				int leftCount = left.getValue();
				int marchCount = Math.min(maxArmy, leftCount);
				presetMap.merge(armyId, marchCount, (v1, v2) -> v1 + v2);
				armyIdCount.merge(armyId, marchCount, (v1,v2)-> v1 -v2);
				maxArmy -= marchCount;
				if(maxArmy<=0){
					break;
				}
			}
		}
		
		List<ArmyInfo> armyInfos = new ArrayList<>();
		for(Entry<Integer,Integer> pre: presetMap.entrySet()){
			int armyId = pre.getKey();
			int marchCount = pre.getValue();
			if(marchCount<=0){
				continue;
			}
			ArmyInfo freeArmy = new ArmyInfo(armyId, marchCount);
			freeArmy.setPlayerId(defPlayer.getId());
			armyInfos.add(freeArmy);
		}
		armyInfos.sort(ArmyInfo.fightMailShow);
		
		// 由于attackPlayerMarch中没有对返回行军中英雄/超级兵做处理, 固记算出征时不加久英雄/超级兵. 
		ArmyService.getInstance().checkArmyAndMarch(defPlayer, armyInfos, Collections.emptyList(), 0);
		
		//添加影子部队信息
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heros);
		effParams.setSuperSoliderId(superSoldierId);
		effParams.setArmourSuit(ArmourSuitType.valueOf(armourSuit));
		effParams.setMechacoreSuit(MechaCoreSuitType.valueOf(mechaSuit));
		effParams.setSuperLab(superLab);
		effParams.setTalent(talent);
		effParams.setManhattanAdkSwId(manhattanAtkSwId);
		effParams.setManhattanDefSwId(manhattanDefSwId);
		effParams.setTroopEffType(WarEff.CITY_DEF);
		
		addShadowArmy(defPlayer, armyInfos, effParams);
		
		List<BattleUnity> armyList = new ArrayList<>();
		String playerId = defPlayer.getId();
		defArmyMap.put(playerId, armyInfos);
		for (ArmyInfo armyInfo : armyInfos) {
			if (armyInfo.getFreeCnt() <= 0) {
				continue;
			}
			ArmyInfo freeArmy = new ArmyInfo(armyInfo.getArmyId(), armyInfo.getFreeCnt() + armyInfo.getShadowCnt());
			// 兵种中添加影子部队信息
			freeArmy.setShadowCnt(armyInfo.getShadowCnt());
			freeArmy.setPlayerId(playerId);
			BattleUnity unity = BattleUnity.valueOf(defPlayer, freeArmy, effParams);
			armyList.add(unity);
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			defTypeSet.add(cfg.getSoldierType());
		}
		
		try { // 记录玩家当时的英雄状态
			List<String> marchIds = WorldMarchService.getInstance().getPlayerMarch(defPlayer.getId()).stream()
					.map(mar -> mar.getMarchId() + "_" + mar.getMarchType() + "_" + mar.getMarchEntity().getHeroIdStr() + "_" + mar.getMarchEntity().getSuperSoldierId())
					.collect(Collectors.toList());

			BehaviorLogger.log4Service(defPlayer, Source.SYSTEM_OPERATION, Action.WORLD_MARCH_REACH_ATTACK_PLAYER,
					Params.valueOf("duelHero1", duelHero1), // 设置守城英雄1
					Params.valueOf("duelHero2", duelHero2),
					Params.valueOf("defHeroFight", heros), // 实际出战英雄
					Params.valueOf("marchesAlternate", marchIds)); // 如果英雄替补, 记录占用英雄的行军
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return armyList;
	}

	/**
	 * 构建玩家战斗方部队信息
	 * 
	 * @param worldMarchs
	 * @param defPlayer
	 *            (仅限基地战传入防守方玩家,其他情况传null)
	 * @param typeSet
	 *            兵种类型列表
	 * @param armyMap
	 * @return
	 */
	public List<BattleUnity> buildPlayerArmyList(int battlePoint, List<IWorldMarch> worldMarchs, Player defPlayer, Set<SoldierType> typeSet, Map<String, List<ArmyInfo>> armyMap,
			WarEff defTroopEffType) {
		List<BattleUnity> armyList = new ArrayList<>();
		Set<Integer> idSet = new HashSet<>();
		// 添加基地站防守方玩家防守部队信息
		if (WarEff.CITY_DEF.check(defTroopEffType) && defPlayer != null) {
			EffectParams effParams = cityDefEffParames(battlePoint, defPlayer, defTroopEffType);
			List<ArmyInfo> defArmys = ArmyService.getInstance().getDefenceArmyReady(defPlayer);
			defArmys.sort(ArmyInfo.fightMailShow);
			addShadowArmy(defPlayer, defArmys, effParams);
			for (ArmyInfo armyInfo : defArmys) {
				ArmyInfo freeArmy = new ArmyInfo(armyInfo.getArmyId(), armyInfo.getFreeCnt() + armyInfo.getShadowCnt());
				// 兵种中添加影子部队信息
				freeArmy.setShadowCnt(armyInfo.getShadowCnt());
				freeArmy.setPlayerId(defPlayer.getId());
				BattleUnity unity = BattleUnity.valueOf(defPlayer, freeArmy, effParams);
				armyList.add(unity);
				idSet.add(armyInfo.getArmyId());
			}
			armyMap.put(defPlayer.getId(), defArmys);
		}

		// 构建兵种/英雄战斗单元
		for (IWorldMarch march : worldMarchs) {
			EffectParams effectParams = march.getMarchEntity().getEffectParams();
			effectParams.setImarch(march);
			effectParams.cleanEffValCache();
			effectParams.setBattlePoint(battlePoint);
			effectParams.setTroopEffType(defTroopEffType);
			String playerId = march.getPlayerId();
			Player player = march.getPlayer();
			List<ArmyInfo> armyInfos = march.getArmys();
			armyInfos.sort(ArmyInfo.fightMailShow);
			armyMap.put(playerId, armyInfos);
			//添加影子部队信息
			addShadowArmy(player, armyInfos, effectParams);
			for (ArmyInfo armyInfo : armyInfos) {
				if (armyInfo.getFreeCnt() <= 0) {
					continue;
				}
				ArmyInfo freeArmy = new ArmyInfo(armyInfo.getArmyId(), armyInfo.getFreeCnt() + armyInfo.getShadowCnt());
				freeArmy.setPlayerId(playerId);
				// 兵种中添加影子部队信息
				freeArmy.setShadowCnt(armyInfo.getShadowCnt());
				BattleUnity unity = BattleUnity.valueOf(march.getPlayer(), freeArmy, effectParams);
				armyList.add(unity);
				idSet.add(freeArmy.getArmyId());
			}
		}

		for (Integer id : idSet) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, id);
			typeSet.add(cfg.getSoldierType());
		}
		return armyList;
	}
	
	private EffectParams cityDefEffParames(int battlePoint,Player defPlayer,WarEff defTroopEffType){
		List<Integer> heros = defCityHeros(defPlayer).stream().map(PlayerHero::getCfgId).collect(Collectors.toList());
		Optional<SuperSoldier> ss = defCitySuperSoldier(defPlayer);
		int superSoldierId = ss.isPresent() ? ss.get().getCfgId() : 0;
		int armourSuit = defPlayer.getEntity().getArmourSuit();
		int mechaSuit = defPlayer.getPlayerMechaCore().getWorkSuit();
		
		//添加影子部队信息
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heros);
		effParams.setSuperSoliderId(superSoldierId);
		effParams.setArmourSuit(ArmourSuitType.valueOf(armourSuit));
		effParams.setMechacoreSuit(MechaCoreSuitType.valueOf(mechaSuit));
		effParams.setBattlePoint(battlePoint);
		effParams.setTroopEffType(defTroopEffType);
		return effParams;
	}
	
	/**
	 * 添加影子部队
	 * @param player
	 * @param armyList
	 * @param heros
	 * @param superSoldierId
	 */
	private void addShadowArmy(Player player, List<ArmyInfo> armyList, EffectParams effParams) {
		long shadowRate = effParams.getEffVal(player, EffType.HERO_1663) > 0 ? effParams.getEffVal(player, EffType.HERO_1663) : effParams.getEffVal(player, EffType.SHADOW_ARMY);
		if (shadowRate == 0 || armyList.isEmpty() || player instanceof GuarderPlayer) {
			return;
		}
//		Collections.shuffle(armyList);
		ArmyInfo mostArmy = null;
		for (ArmyInfo army : armyList) {
			if (!isNormalSoldier(army.getArmyId())) {
				continue;
			}
			army.setShadowCnt(0);
			if(mostArmy == null || army.getFreeCnt() > mostArmy.getFreeCnt()) {
				mostArmy = army;
			}
		}
		if(mostArmy != null){
			mostArmy.setShadowCnt((int) (shadowRate * mostArmy.getFreeCnt() * GsConst.EFF_PER));
			HawkLog.debugPrintln("shadow army info, playerId:{}, playerName:{}, armyId:{}, armyFree:{}, armyShadow:{}", player.getId(), player.getName(), mostArmy.getArmyId(),
					mostArmy.getFreeCnt(), mostArmy.getShadowCnt());
		}
	}

	/**
	 * 构建玩家部队的战斗单元列表
	 * 
	 * @param player
	 *            队长(怪为null)
	 * @param unitList
	 *            部队信息
	 * @param tarTypeSet
	 *            敌方兵type set
	 * @param troopEffType
	 *            部队作用号类型-集结时 @see WarEff_MASS;
	 * @return
	 */
	public List<BattleSoldier> buildBattleSoldierList(List<BattleUnity> unitList,List<BattleUnity> tarUnitList, BattleConst.WarEff troopEffType, Set<SoldierType> tarTypeSet) {
		ListIterator<BattleUnity> listIterator = unitList.listIterator();
		while (listIterator.hasNext()) {
			try {
				BattleUnity unity = listIterator.next();
				unity.initSolder();
			} catch (Exception e) {
				listIterator.remove();
				HawkException.catchException(e);
			}
		}
		ListIterator<BattleUnity> listIteratortar = tarUnitList.listIterator();
		while (listIteratortar.hasNext()) {
			try {
				BattleUnity unity = listIteratortar.next();
				unity.initSolder();
			} catch (Exception e) {
				listIteratortar.remove();
				HawkException.catchException(e);
			}
		}
		
		BattleUnityStatistics unitStatic = BattleUnityStatistics.create(unitList);
		BattleUnityStatistics tarStatic = BattleUnityStatistics.create(tarUnitList);
		for (BattleUnity unit : unitList) {
			unit.setUnitStatic(unitStatic);
			unit.setTarStatic(tarStatic);
			unit.getEffectParams().setStaffPointGreat(unitStatic.getStaffOfficePoint() >= tarStatic.getStaffOfficePoint());
		}
		for (BattleUnity tarunit : tarUnitList) {
			tarunit.setUnitStatic(tarStatic);
			tarunit.setTarStatic(unitStatic);
			tarunit.getEffectParams().setStaffPointGreat(tarStatic.getStaffOfficePoint() >= unitStatic.getStaffOfficePoint());
		}
		
		for (BattleTupleType.Type tupleType : BattleTupleType.Type.values()) {
			for (BattleUnity unity : unitList) {
				Player player = unity.getPlayer();
				if (player != null) {
					warEffMap(unity, tupleType, tarTypeSet, troopEffType);
				}
			}
		}
		List<BattleSoldier> soldierList = new ArrayList<>();
		unitList.stream().forEach(u -> soldierList.add(u.getSolider()));
		return soldierList;
	}

	/**
	 * 战后部队信息汇总处理
	 * 
	 * @param battleArmyMap
	 * @param aftArmyMap
	 * @param skillType
	 * @param armyMap
	 * @param battle
	 * @param isAtk
	 */
	public void calcArmyInfo(Map<String, List<ArmyInfo>> battleArmyMap, Map<String, List<ArmyInfo>> aftArmyMap,
			BattleCalcParames calcParames, Map<String, List<ArmyInfo>> armyMap, Battle battle) {
		double convertRate = GsConst.RANDOM_MYRIABIT_BASE;
		// 攻打野怪实际伤兵转换率
		if (WarEff.MONSTER.check(calcParames.warEff)) {
			int[] convertRange = WorldMapConstProperty.getInstance().getWoundedConvertRate();
			Random random = new Random();
			convertRate = convertRange[0] + random.nextInt(convertRange[1] - convertRange[0] + 1);
		}
		if (WarEff.XZQ.check(calcParames.warEff)) {
			convertRate = XZQService.getInstance().getHurtRate();
		}
		if(battle.getType() == BattleType.ATTACK_GUNDAM_PVE){ // 机甲无伤兵
			convertRate = 0;
		}
		if(calcParames.getConvertRate() > 0){
			convertRate = calcParames.getConvertRate();
		}
		for (Entry<String, List<ArmyInfo>> entry : armyMap.entrySet()) { 
			String playerId = entry.getKey();
			double selfCoverRete = convertRate;
			if (WarEff.SELF_ATK.check(calcParames.warEff) && !calcParames.duel) {
				int effSurvive =calcParames.getBattleEffVal(playerId, EffType.WAR_SELF_MARCH_WOUND_SURVIVE);
				selfCoverRete *= (GsConst.RANDOM_MYRIABIT_BASE - effSurvive) * GsConst.EFF_PER;
			}
			// 实际伤兵转换率校正
			selfCoverRete = Math.min(selfCoverRete, GsConst.RANDOM_MYRIABIT_BASE);
			selfCoverRete = Math.max(selfCoverRete, 0);

			List<BattleSoldier> soldierList;
			if (calcParames.isAtk) {
				soldierList = battle.getAtkSoldierList(playerId);
			} else {
				soldierList = battle.getDefSoldierList(playerId);
			}
			List<ArmyInfo> aftArmys = new ArrayList<>();
			for (BattleSoldier unit : soldierList) {
				aftArmys.add(unit.calcArmyInfo(selfCoverRete));
			}
			// 本次战斗参战部队列表
			List<ArmyInfo> battleArmyList = new ArrayList<>();
			// 玩家全部部队信息列表
			List<ArmyInfo> totalArmyList = new ArrayList<>();
			// 部队信息汇总
			calcLeftArmy(playerId, entry.getValue(), aftArmys, calcParames, battleArmyList, totalArmyList, battle);
			battleArmyMap.put(playerId, battleArmyList);
			aftArmyMap.put(playerId, totalArmyList);
		}
	}

	/**
	 * 部队信息结算(伤兵转换+部队汇总)
	 * 
	 * @param playerId
	 * @param orgArmys
	 * @param aftArmys
	 * @param troopType
	 * @param lifeSaving
	 *            是否开启救援
	 * @param duel
	 *            是否开启决斗
	 * @param battleArmyList
	 *            本次战斗部队结算列表
	 * @param totalArmyList
	 *            玩家部队汇总
	 * @param battle
	 */
	private void calcLeftArmy(String playerId, List<ArmyInfo> orgArmys, List<ArmyInfo> aftArmys, BattleCalcParames calcParames,
			List<ArmyInfo> battleArmyList, List<ArmyInfo> totalArmyList, Battle battle) {
		// 剩余部队信息
		Map<Integer, ArmyInfo> armyMap = new HashMap<>();
		if (orgArmys == null) {
			return;
		}
		for (ArmyInfo armyInfo : orgArmys) {
			ArmyInfo copy = armyInfo.getCopy();
			armyMap.put(copy.getArmyId(), copy);
		}
		totalArmyList.addAll(armyMap.values());

		int totalDeadCnt = 0;
		if (aftArmys == null) {
			return;
		}
		for (ArmyInfo info : aftArmys) {
			// 防御建筑和陷阱不参与伤兵结算
			if (!isNormalSoldier(info.getArmyId())) {
				battleArmyList.add(info);
				continue;
			}
			// 死亡数量
			int deadCnt = info.getDeadCount();
			// 未损伤的部队不参与结算
			if (deadCnt <= 0) {
				battleArmyList.add(info);
				continue;
			}
			totalDeadCnt += deadCnt;
		}
		aftArmys.removeAll(battleArmyList);

		// 获取存活比例,根据存活比例计算
		int hurtRate = getHurtRate(playerId, calcParames);
		int surrive = (int) (1l * totalDeadCnt * hurtRate / GsConst.EFF_RATE);

		// 存活部队数量数据修正
		surrive = Math.min(surrive, totalDeadCnt);
		surrive = Math.max(surrive, 0);

		// 伤兵额度分摊
		ArmyService.getInstance().convertTest(aftArmys, surrive);
		
		if(!calcParames.duel){
			// 伤兵转死兵(歼灭等效果)
			woundToDead(aftArmys, calcParames);
			
			// 死兵转伤兵(死亡抵抗等效果)
			deadToWound(aftArmys, calcParames, playerId, battle);
		}

		battleArmyList.addAll(aftArmys);
		for (ArmyInfo aftArmy : battleArmyList) {
			int armyId = aftArmy.getArmyId();
			ArmyInfo armyInfo = armyMap.get(armyId);
			armyInfo.setKillCount(armyInfo.getKillCount() + aftArmy.getKillCount());
			armyInfo.setDeadCount(armyInfo.getDeadCount() + aftArmy.getDeadCount());
			armyInfo.setSave1634(armyInfo.getSave1634() + aftArmy.getSave1634());
			armyInfo.setWoundedCount(armyInfo.getWoundedCount() + aftArmy.getWoundedCount());
			armyInfo.mergeKillInfo(aftArmy.getKillInfo());
			armyInfo.mergeKillDetail(aftArmy.getKillDetail());
			armyInfo.setDisBattlePoint(armyInfo.getDisBattlePoint() + aftArmy.getDisBattlePoint());
		}
	}

	/**
	 * 伤兵转死兵(歼灭/屠城等效果)
	 * 
	 * @param aftArmys
	 * @param calcParames
	 */
	private void woundToDead(List<ArmyInfo> aftArmys, BattleCalcParames calcParames) {
		// 伤兵转化为死兵的比例
		double rate = calcParames.disHurtRate;
		if (rate <= 0) {
			return;
		}
		for (ArmyInfo armyInfo : aftArmys) {
			int woundCnt = armyInfo.getWoundedCount();
			int deadCnt = armyInfo.getDeadCount();
			if (woundCnt == 0) {
				continue;
			}
			int disWoundCnt = (int) Math.floor(rate * GsConst.EFF_PER * woundCnt);
			disWoundCnt = Math.min(woundCnt, disWoundCnt);
			disWoundCnt = Math.max(0, disWoundCnt);
			woundCnt -= disWoundCnt;
			deadCnt += disWoundCnt;
			armyInfo.setWoundedCount(woundCnt);
			armyInfo.setDeadCount(deadCnt);
		}

	}

	/**
	 * 死兵转伤兵(时间回溯等效果)
	 * 
	 * @param aftArmys
	 * @param calcParames
	 * @param playerId
	 * @param battle
	 */
	private void deadToWound(List<ArmyInfo> aftArmys, BattleCalcParames calcParames, String playerId, Battle battle) {
		if (GameUtil.isNpcPlayer(playerId)) {
			return;
		}
		WarEff eff = calcParames.warEff;
		/******************* 1101,177,149作用号效果计算 *******************/
		double rate = 0;
		// 非守城时,作用号加成
		if (!WarEff.CITY_DEF.check(eff)) {
			rate += calcParames.getBattleEffVal(playerId, EffType.WAR_WOUND_RATE);
		}
		// 单人进攻时,作用号加成
		if (WarEff.SELF_ATK.check(eff)) {
			rate += calcParames.getBattleEffVal(playerId, EffType.WAR_SELF_ATK_WOUND_RATE);
		}
		// 单人攻城时,作用号加成
		if (WarEff.CITY_ATK.check(eff)) {
			rate += calcParames.getBattleEffVal(playerId, EffType.ATK_CITY_DEAD_TO_HURT);
		}
		if (rate > 0) {
			rate = Math.min(GsConst.EFF_RATE, rate);

			for (ArmyInfo armyInfo : aftArmys) {
				int woundCnt = armyInfo.getWoundedCount();
				int deadCnt = armyInfo.getDeadCount();
				if (deadCnt == 0) {
					continue;
				}
				int disDeadCnt = (int) Math.floor(rate * deadCnt * GsConst.EFF_PER);
				disDeadCnt = Math.min(deadCnt, disDeadCnt);
				disDeadCnt = Math.max(0, disDeadCnt);
				deadCnt -= disDeadCnt;
				woundCnt += disDeadCnt;
				armyInfo.setWoundedCount(woundCnt);
				armyInfo.setDeadCount(deadCnt);
			}
		}
		
		/******************* 1092作用号效果计算 *******************/
		double disRate = calcParames.getBattleEffVal(playerId, EffType.CROSS_DEAD_TO_HURT);
		double disRate1507 = 0;
		double disRate1522 = calcParames.getBattleEffVal(playerId, EffType.DEAD_TO_HURT_1522);
		double disRateHERO_1662 = calcParames.getBattleEffVal(playerId, EffType.HERO_1662);
		double disRate1523 = 0;
		// 星球大战死转伤
		double disRateSW = 0;
		double disRateSW155x = 0;
		if (battle.getDuntype() == DungeonMailType.SW) {
			disRateSW = HawkConfigManager.getInstance().getKVInstance(SWBattleCfg.class).getDeadToWoundPer();
			disRateSW155x = calcParames.getBattleEffVal(playerId, EffType.SW_1558);
			if (battle.getType() == BattleConst.BattleType.ATTACK_PRESIDENT_TOWER) {
				disRateSW155x += calcParames.getBattleEffVal(playerId, EffType.SW_1559);
			} else if (battle.getType() == BattleConst.BattleType.ATTACK_PRESIDENT) {
				disRateSW155x += calcParames.getBattleEffVal(playerId, EffType.SW_1560);
			}
		}
		if (!WarEff.CITY_DEF.check(eff)) {
			disRate1507 = calcParames.getBattleEffVal(playerId, EffType.DEAD_TO_HURT_1507); 
		}
		if(WarEff.CITY_ATK.check(eff)){
			disRate1523 = calcParames.getBattleEffVal(playerId, EffType.DEAD_TO_HURT_1523); 
		}
		disRate = Math.min(GsConst.EFF_RATE, disRate);
		disRate1507 = Math.min(GsConst.EFF_RATE, disRate1507);
		disRate1522 = Math.min(GsConst.EFF_RATE, disRate1522);
		disRate1523 = Math.min(GsConst.EFF_RATE, disRate1523);
		disRateSW = Math.min(GsConst.EFF_RATE, disRateSW);
		disRateSW155x = Math.min(GsConst.EFF_RATE, disRateSW155x);
		for (ArmyInfo armyInfo : aftArmys) {
			int woundCnt = armyInfo.getWoundedCount();
			int deadCnt = armyInfo.getDeadCount();
			if (deadCnt == 0) {
				continue;
			}
			if (disRate > 0) {
				int disDeadCnt = (int) Math.floor(disRate * deadCnt * GsConst.EFF_PER);
				disDeadCnt = Math.min(deadCnt, disDeadCnt);
				disDeadCnt = Math.max(0, disDeadCnt);
				deadCnt -= disDeadCnt;
				woundCnt += disDeadCnt;
			}

			if (disRate1507 > 0) {
				int disDeadCnt1507 = (int) Math.floor(disRate1507 * deadCnt * GsConst.EFF_PER);
				disDeadCnt1507 = Math.min(deadCnt, disDeadCnt1507);
				disDeadCnt1507 = Math.max(0, disDeadCnt1507);
				deadCnt -= disDeadCnt1507;
				woundCnt += disDeadCnt1507;
			}

			if (disRate1522 > 0) {
				int disDeadCnt1522 = (int) Math.floor(disRate1522 * deadCnt * GsConst.EFF_PER);
				disDeadCnt1522 = Math.min(deadCnt, disDeadCnt1522);
				disDeadCnt1522 = Math.max(0, disDeadCnt1522);
				deadCnt -= disDeadCnt1522;
				woundCnt += disDeadCnt1522;
			}

			if (disRate1523 > 0) {
				int disDeadCnt1523 = (int) Math.floor(disRate1523 * deadCnt * GsConst.EFF_PER);
				disDeadCnt1523 = Math.min(deadCnt, disDeadCnt1523);
				disDeadCnt1523 = Math.max(0, disDeadCnt1523);
				deadCnt -= disDeadCnt1523;
				woundCnt += disDeadCnt1523;
			}

			if (disRateSW > 0) {
				int disDeadCntSW = (int) Math.floor(disRateSW * deadCnt * GsConst.EFF_PER);
				disDeadCntSW = Math.min(deadCnt, disDeadCntSW);
				disDeadCntSW = Math.max(0, disDeadCntSW);
				deadCnt -= disDeadCntSW;
				woundCnt += disDeadCntSW;
			}

			if (disRateSW155x > 0) {
				int disDeadCntSW155x = (int) Math.floor(disRateSW155x * deadCnt * GsConst.EFF_PER);
				disDeadCntSW155x = Math.min(deadCnt, disDeadCntSW155x);
				disDeadCntSW155x = Math.max(0, disDeadCntSW155x);
				deadCnt -= disDeadCntSW155x;
				woundCnt += disDeadCntSW155x;
			}
			
			int disRate1650 = calcParames.getBattleSoldierEffVal(playerId, EffType.HERO_1650);
			if (disRate1650 > 0 && armyInfo.getType() == SoldierType.TANK_SOLDIER_1) {
				int disDeadCnt1650 = (int) Math.floor(disRate1650 * GsConst.EFF_PER * deadCnt);
				disDeadCnt1650 = Math.min(deadCnt, disDeadCnt1650);
				disDeadCnt1650 = Math.max(0, disDeadCnt1650);
				deadCnt -= disDeadCnt1650;
				woundCnt += disDeadCnt1650;
			}
			if (disRateHERO_1662 > 0) {
				BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
				if (armyCfg.isPlantSoldier()) {
					int disDeadCnt1662 = (int) Math.floor(disRateHERO_1662 * GsConst.EFF_PER * deadCnt);
					disDeadCnt1662 = Math.min(deadCnt, disDeadCnt1662);
					disDeadCnt1662 = Math.max(0, disDeadCnt1662);
					deadCnt -= disDeadCnt1662;
					woundCnt += disDeadCnt1662;
				}
			}
			
			
			armyInfo.setWoundedCount(woundCnt);
			armyInfo.setDeadCount(deadCnt);
		}
	}

	/**
	 * 掠夺
	 * 
	 * @param weightAry
	 *            每个玩这家的负重
	 * @param cityAry
	 *            每个玩这家的大本等级
	 * @param hasResAry
	 *            被掠夺玩家可被抢资源数
	 * @return player-res二维数组
	 */
	public Map<String, long[]> calcGrabRes(List<Player> players, int[] weightAry, long[] hasResAry) {
		if (hasResAry == null || weightAry == null || weightAry.length != players.size() || hasResAry.length != GsConst.RES_TYPE.length) {
			HawkLog.errPrintln("calcGrabRes error param");
			return null;
		}

		List<GrabRes> graList = GrabRes.valueOfAll(players, weightAry);
		int finish = 1;
		int size = graList.size();
		long graCount;
		while (finish == 1) {
			finish = 0;
			for (GrabRes gra : graList) {
				if (gra.overMax()) {
					continue;
				}
				if (gra.nextSafeAddWeight() == 0) {
					continue;
				}
				graCount = hasResAry[0] / size;
				if (graCount > 0 && (graCount = gra.incRes1007(graCount)) > 0) {
					hasResAry[0] -= graCount;
					finish |= 1;
				}
				graCount = hasResAry[1] / size;
				if (graCount > 0 && (graCount = gra.incRes1008(graCount)) > 0) {
					hasResAry[1] -= graCount;
					finish |= 1;
				}
				graCount = hasResAry[2] / size;
				if (graCount > 0 && (graCount = gra.incRes1010(graCount)) > 0) {
					hasResAry[2] -= graCount;
					finish |= 1;
				}
				graCount = hasResAry[3] / size;
				if (graCount > 0 && (graCount = gra.incRes1009(graCount)) > 0) {
					hasResAry[3] -= graCount;
					finish |= 1;
				}
			}
		}
		Map<String, long[]> resultAry = Maps.newHashMapWithExpectedSize(players.size());
		for (GrabRes gra : graList) {
			resultAry.put(gra.getPlayerId(), gra.grabResArr());
		}

		return resultAry;
	}

	/**
	 * 判断是否是普通兵种(除防御建筑/陷阱)
	 * 
	 * @param armyId
	 * @return
	 */
	private boolean isNormalSoldier(int armyId) {
		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		int armyType = cfg.getType();
		// 防御建筑和陷阱不参与伤兵结算
		return armyType != SoldierType.BARTIZAN_100_VALUE && armyType != SoldierType.WEAPON_LANDMINE_101_VALUE && armyType != SoldierType.WEAPON_ACKACK_102_VALUE
				&& armyType != SoldierType.WEAPON_ANTI_TANK_103_VALUE;
	}

//	/**
//	 * 初始化战斗部队相关作用效果(攻-防-加伤-减伤)
//	 * 
//	 * @param playerEffect
//	 * @param solider
//	 * @param typeSet
//	 *            目标兵种类型
//	 * @param troopEffType
//	 *            部队作用号类型
//	 * @param heroIds
//	 * @param armyList
//	 * @param addHurtPer
//	 *            额外伤害加成
//	 */
//	public void initSoldierWarEffMap(BattleUnity unity, Set<SoldierType> typeSet, BattleConst.WarEff troopEffType) {
//		for (BattleTupleType.Type tupleType : BattleTupleType.Type.values()) {
//			warEffMap(unity, tupleType, typeSet, troopEffType);
//		}
//
//	}

	/**
	 * 战斗相关作用效果(攻-防-加伤-减伤)
	 * 
	 * @param type
	 *            兵种类型
	 * @param typeSet
	 *            目标兵种类型
	 * @param troopEffType
	 *            部队作用号类型
	 */
	private void warEffMap(BattleUnity unity, BattleTupleType.Type tupleType, Set<SoldierType> typeSet, BattleConst.WarEff troopEffType) {
		getWarEffTuple(unity, SoldierType.XXXXXXXXXXXMAN, troopEffType, tupleType);
		for (SoldierType tarType : typeSet) {
			getWarEffTuple(unity, tarType, troopEffType, tupleType);
		}

	}

	private void getWarEffTuple(BattleUnity unity, SoldierType tarType, BattleConst.WarEff troopEffType, BattleTupleType.Type tupleType) {
		int effPer = 0;
		int effNum = 0;
		Collection<IChecker> map = CheckerFactory.getInstance().checkersMap(tupleType);
		BattleSoldier solider = unity.getSolider();
		CheckerParames params = CheckerParames.newBuilder()
				.setSolider(solider)
				.setTarType(tarType)
				.setTroopEffType(troopEffType)
				.setHeroId(unity.getEffectParams().getHeroIds())
				.setHurtPer(unity.getHurtPer())
				.setSupersoldierId(unity.getEffectParams().getSuperSoliderId())
				.setUnity(unity)
				.setTupleType(tupleType)
				.build();
		List<Integer> effPerList = new ArrayList<>(map.size() / 2);
		for (IChecker checker : map) {
			CheckerKVResult effTypeVal = CheckerKVResult.DefaultVal;
			try {
				effTypeVal = checker.valueV2(params);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			effPer = effPer + effTypeVal.first;
			effNum = effNum + effTypeVal.second;
			if (effTypeVal.first > 0) {
				effPerList.add(effTypeVal.first);
			}
			solider.setEffVal(checker.effType(), effTypeVal.first + effTypeVal.second);
		}
		HawkTuple3<Integer, Integer, List<Integer>> tuple = HawkTuples.tuple(effPer, effNum, ImmutableList.copyOf(effPerList));
		solider.putEffPerNum(tupleType, tarType, tuple);
	}

	public Optional<SuperSoldier> defCitySuperSoldier(Player defPlayer) {
		SuperSoldier result = null;
		for (SuperSoldier ss : defPlayer.getAllSuperSoldier()) {
			if (ss.getConfig().getSupersoldierClass() == 2) { // 工业型不出战
				continue;
			}
			if (ss.getState() == PBSuperSoldierState.SUPER_SOLDIER_STATE_MARCH) {
				continue;
			}

			if (ss.getConfig().getSupersoldierClass() == 1) {
				if (Objects.isNull(result) || ss.power() > result.power()) {
					result = ss;
				}
			}
			if (ss.getCityDefense() == BattleConst.CITY_DEF109) {
				result = ss;
				break;
			}
		}
		return Optional.ofNullable(result);
	}

	/** 守城英雄 */
	public List<PlayerHero> defCityHeros(Player defPlayer) {
		List<PlayerHero> defHeroList = new ArrayList<>();
		PlayerHero defHero1 = getHeroBycityDefenseId(defPlayer, BattleConst.CITY_DEF107);
		PlayerHero defHero2 = getHeroBycityDefenseId(defPlayer, BattleConst.CITY_DEF108);
		int defHeroCount = 0;
		if (Objects.nonNull(defHero1) && defHero1.getState() == PBHeroState.HERO_STATE_FREE) {
			defHeroList.add(defHero1);
			defHeroCount = defHeroCount + 1;
		}
		if (Objects.nonNull(defHero2) && defHero2.getState() == PBHeroState.HERO_STATE_FREE) {
			defHeroList.add(defHero2);
			defHeroCount = defHeroCount + 1;
		}
		
		List<PlayerHero> alternateHeroList = Collections.emptyList();
		if (defHeroCount < 2) {
			alternateHeroList = defPlayer.getAllHero().stream()
					.filter(hero -> hero.getConfig().getStaffOfficer() != 1)
					.filter(hero -> !Objects.equals(hero, defHero1))
					.filter(hero -> !Objects.equals(hero, defHero2))
					.filter(hero -> hero.getState() != PBHeroState.HERO_STATE_MARCH)
					.sorted(Comparator.comparingInt((PlayerHero hero) -> hero.attrVale(101)).reversed())
					.limit(5)
					.collect(Collectors.toList());
			for (int i = defHeroCount; i < 2 && i < alternateHeroList.size(); i++) {
				PlayerHero bxhero = alternateHeroList.get(i);
				if (!defHeroList.stream().anyMatch(hero -> hero.getCfgId() == bxhero.getConfig().getProhibitedHero())) {
					defHeroList.add(bxhero);
				}
			}
		}
		
		try { // 记录玩家当时的英雄状态
			List<String> marchIds = Collections.emptyList();
			if (!alternateHeroList.isEmpty()) {
				marchIds = WorldMarchService.getInstance().getPlayerMarch(defPlayer.getId()).stream()
						.map(mar -> mar.getMarchId() + "_" + mar.getMarchType() + "_" + mar.getMarchEntity().getHeroIdStr() + "_" + mar.getMarchEntity().getSuperSoldierId())
						.collect(Collectors.toList());
			}

			BehaviorLogger.log4Service(defPlayer, Source.SYSTEM_OPERATION, Action.WORLD_MARCH_REACH_ATTACK_PLAYER,
					Params.valueOf("defHero1", defHero1 != null ? defHero1.getCfgId() : "null"), // 设置守城英雄1
					Params.valueOf("defHero2", defHero2 != null ? defHero2.getCfgId() : "null"),
					Params.valueOf("defHeroFight", defHeroList.stream().map(PlayerHero::getCfgId).collect(Collectors.toList())), // 实际出战英雄
					Params.valueOf("topHerosIfAlternate", alternateHeroList.stream().map(PlayerHero::getCfgId).collect(Collectors.toList())), // 如果驻防英雄不在的话 按战力排前10英雄的替补英雄.
					Params.valueOf("marchesIfAlternate", marchIds)); // 如果英雄替补, 记录占用英雄的行军
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return defHeroList;
	}

	/**
	 * 取得驻防英雄
	 */
	public PlayerHero getHeroBycityDefenseId(Player player, int officeId) {
		for (PlayerHero hero : player.getAllHero()) {
			if (hero.getCityDefense() == officeId) {
				return hero;
			}
		}
		return null;
	}

	/**
	 * 计算处理攻防部队击杀/击伤数据
	 * 
	 * @param battleIncome
	 * @param battleOutcome
	 */
	public void dealWithPvpBattleEvent(IBattleIncome battleIncome, BattleOutcome battleOutcome, boolean isMass, WorldMarchType marchType) {
		// 计算进攻方玩家部队击杀/击伤信息
		calcArmyInfo(battleIncome.getAtkCalcParames(), battleOutcome.getBattleArmyMapAtk(), battleOutcome.getBattleArmyMapDef(), battleOutcome.isAtkWin, isMass, true, marchType,
				battleIncome.getAtkPlayers().get(0).getId());
		// 计算防御方玩家部队击杀/击伤信息
		calcArmyInfo(battleIncome.getDefCalcParames(), battleOutcome.getBattleArmyMapDef(), battleOutcome.getBattleArmyMapAtk(), !battleOutcome.isAtkWin, isMass, false, marchType,
				battleIncome.getDefPlayers().get(0).getId());
		
		//计算仇恨战损
		if (POWER_LOST_MARCH_TYPE_SET.contains(marchType)) {
			calcPowerLost(battleIncome, battleOutcome, isMass, marchType);
		}
	}
	
	/**
	 * 计算仇恨排行榜的行军类型
	 * 基地，驻扎，采集中的战力损失才会被记录
	 */
	public static final Set<WorldMarchType> POWER_LOST_MARCH_TYPE_SET = ImmutableSet.<WorldMarchType>builder()
			.add(WorldMarchType.COLLECT_RESOURCE)
			.add(WorldMarchType.ATTACK_PLAYER)
			.add(WorldMarchType.ASSISTANCE)
			.add(WorldMarchType.ARMY_QUARTERED)
			.add(WorldMarchType.MASS)
			.add(WorldMarchType.MASS_JOIN)
			.add(WorldMarchType.COLLECT_RES_TREASURE)
			.build();
	
	/**
	 * 计算双方战力损失 
	 * @param battleIncome
	 * @param battleOutcome
	 * @param isMass
	 * @param marchType
	 */
	private void calcPowerLost(IBattleIncome battleIncome, BattleOutcome battleOutcome, boolean isMass, WorldMarchType marchType){
		Map<String, List<ArmyInfo>> armyMapAtk = battleOutcome.getBattleArmyMapAtk();
		Map<String, List<ArmyInfo>> armyMapDef = battleOutcome.getBattleArmyMapDef();
		boolean isCrossAtk = CrossService.getInstance().isCrossPlayer(battleIncome.getAtkPlayers().get(0).getId());
		boolean isCrossDef = CrossService.getInstance().isCrossPlayer(battleIncome.getDefPlayers().get(0).getId());
		//攻守双方有leader有跨服则不计算
		if (isCrossAtk || isCrossDef) {
			return;
		}
		boolean is1v1 = (armyMapAtk.size() == 1 && armyMapDef.size() == 1);
		boolean is1vN = (armyMapAtk.size() == 1 && armyMapDef.size() > 1);
		boolean isNv1 = (armyMapAtk.size() > 1 && armyMapDef.size() == 1);
		if (is1v1) {
			calcOneVsOnePowerLose(armyMapAtk, armyMapDef);
		}else if(is1vN){
			//攻击方
			String atkPlayerId = getArmyPlayerId(armyMapAtk);
			//防守方多人
			calcSomeVsPowerLose(atkPlayerId, armyMapDef);
		}else if(isNv1){
			//防守方
			String defPlayerId = getArmyPlayerId(armyMapDef);
			//攻击方多人
			calcSomeVsPowerLose(defPlayerId, armyMapAtk);
		}
	}
	
	/**获取玩家ID,armyMap 是一个人的情况
	 * @param armyMap
	 * @return
	 */
	public String getArmyPlayerId(Map<String, List<ArmyInfo>> armyMap){
		String playerId = "";
		//防守方
		for (Entry<String, List<ArmyInfo>> entry : armyMap.entrySet()) {
			playerId = entry.getKey();
		}
		return playerId;
	}
	/**计算1vN 或 Nv1 的战损情况
	 * @param playerId
	 * @param armyMap
	 */
	public void calcSomeVsPowerLose(String playerId, Map<String, List<ArmyInfo>> armyMap){
		//多人参战一方
		for (Entry<String, List<ArmyInfo>> entry : armyMap.entrySet()) {
			String toPlayerId = entry.getKey();
			long beKilledDefPower = 0;
			long killPower = 0;
			List<ArmyInfo> armyList = entry.getValue();
			for (ArmyInfo armyInfo : armyList) {
				int armyId = armyInfo.getArmyId();
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
				float power = cfg.getPower();
				int sum = armyInfo.getDeadCount() + armyInfo.getWoundedCount() + armyInfo.getShadowDeadCnt();
				beKilledDefPower = beKilledDefPower  + (long) (power * sum);
				//击杀数量
				killPower = killPower + (long) (power * armyInfo.getKillCount());
			}
			//存数据
			RelationService.getInstance().addEnemyHateValue(playerId, toPlayerId, killPower, beKilledDefPower);
		}
	}
	
	/**计算1v1战损数据
	 * @param armyMapAtk
	 * @param armyMapDef
	 */
	public void calcOneVsOnePowerLose(Map<String, List<ArmyInfo>> armyMapAtk , Map<String, List<ArmyInfo>> armyMapDef){
		String atkPlayerId = "";
		String defPlayerId = "";
		//攻击方被杀掉的战力
		long beKilledAtkPower = 0;  
		//放手方被杀掉的战力
		long beKilledDefPower = 0;
		//攻击方
		for (Entry<String, List<ArmyInfo>> entry : armyMapAtk.entrySet()) {
			atkPlayerId = entry.getKey();
			List<ArmyInfo> armyList = entry.getValue();
			for (ArmyInfo armyInfo : armyList) {
				int armyId = armyInfo.getArmyId();
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
				float power = cfg.getPower();
				int sum = armyInfo.getDeadCount() + armyInfo.getWoundedCount() + armyInfo.getShadowDeadCnt();
				beKilledAtkPower = beKilledAtkPower + (long) (power * sum);
			}
		}
		//防守方
		for (Entry<String, List<ArmyInfo>> entry : armyMapDef.entrySet()) {
			defPlayerId = entry.getKey();
			List<ArmyInfo> armyList = entry.getValue();
			for (ArmyInfo armyInfo : armyList) {
				int armyId = armyInfo.getArmyId();
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
				float power = cfg.getPower();
				int sum = armyInfo.getDeadCount() + armyInfo.getWoundedCount() + armyInfo.getShadowDeadCnt();
				beKilledDefPower = beKilledDefPower + (long) (power * sum);
			}
		}
		//存数据
		RelationService.getInstance().addEnemyHateValue(atkPlayerId, defPlayerId, beKilledAtkPower, beKilledDefPower);
	}
	/**
	 * 计算处理部队击杀击伤数据
	 * 
	 * @param calcParames
	 * @param selfBattleArmyMap
	 * @param oppBattleArmyMap
	 */
	private void calcArmyInfo(BattleCalcParames calcParames, Map<String, List<ArmyInfo>> selfBattleArmyMap, Map<String, List<ArmyInfo>> oppBattleArmyMap, boolean isWin,
			boolean isMass, boolean isAttacker, WorldMarchType marchType, String leaderId) {
		Map<String, Map<Integer, Integer>> armyKillMap = new HashMap<>();
		Map<String, Map<Integer, Integer>> armyHurtMap = new HashMap<>();
		Map<String, Map<Long, Integer>> armyKillDetailMap = new HashMap<>();
		Map<String, Map<Long, Integer>> armyHurtDetailMap = new HashMap<>();

		// 计算玩家的击杀&击伤数据
		calcKillAndHurtInfo(armyKillMap, armyHurtMap, selfBattleArmyMap, oppBattleArmyMap);
		// 击杀击伤详情(带星级)
		calcKillAndHurtDetailInfo(armyKillDetailMap, armyHurtDetailMap, selfBattleArmyMap, oppBattleArmyMap);
		// 给参战玩家推送PVP战斗事件
		for (String playerId : selfBattleArmyMap.keySet()) {
			Map<Integer, Integer> killMap = armyKillMap.get(playerId);
			if (killMap == null) {
				killMap = new HashMap<>();
			}
			Map<Integer, Integer> hurtMap = armyHurtMap.get(playerId);
			if (hurtMap == null) {
				hurtMap = new HashMap<>();
			}
			Map<Long, Integer> killDetailMap = armyKillDetailMap.get(playerId);
			if (killDetailMap == null) {
				killDetailMap = new HashMap<>();
			}
			Map<Long, Integer> hurtDetailMap = armyHurtDetailMap.get(playerId);
			if (hurtDetailMap == null) {
				hurtDetailMap = new HashMap<>();
			}
			PvpBattleEvent battleEvent = new PvpBattleEvent(playerId, isWin, killMap, hurtMap, killDetailMap, hurtDetailMap, calcParames.isSameServer, calcParames.isAtk,
					WarEff.CITY.check(calcParames.warEff), marchType);
			// 给玩家发送战斗事件
			ActivityManager.getInstance().postEvent(battleEvent);

			// 跨服消息投递-战斗杀敌
			CrossActivityService.getInstance().postEvent(battleEvent);
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			MissionManager.getInstance().postMsg(player,
					new EventPvpBattle(isWin, killMap, hurtMap, isAttacker, isMass, marchType, playerId.equals(leaderId), selfBattleArmyMap.get(playerId)));
			//添加日志方便定位
			if (!killMap.isEmpty()) {
				HawkLog.logPrintln("BattleService calcArmyInfo, playerId: {}, killMap: {}", playerId, killMap);
			}
			// 联盟任务-消灭敌军
			GuildService.getInstance().postGuildTaskMsg(new KillEnemyTaskEvent(GuildService.getInstance().getPlayerGuildId(playerId), battleEvent));
		}
	}

	/**
	 * 计算玩家部队击杀/击伤数据
	 * 
	 * @param armyMap
	 * @param selfBattleArmyMap
	 */
	private void calcKillOrHurtInfo(Map<String, Map<Integer, Integer>> armyMap, Map<String, List<ArmyInfo>> selfBattleArmyMap) {
		for (Entry<String, List<ArmyInfo>> entry : selfBattleArmyMap.entrySet()) {
			String playerId = entry.getKey();
			Map<Integer, Integer> selfMap = new HashMap<>();
			armyMap.put(playerId, selfMap);
			List<ArmyInfo> armyInfoList = entry.getValue();
			for (ArmyInfo armyInfo : armyInfoList) {
				Map<Integer, Integer> killMap = armyInfo.getKillInfo();
				for (Entry<Integer, Integer> killEntry : killMap.entrySet()) {
					int armyId = killEntry.getKey();
					int cnt = killEntry.getValue();
					if (selfMap.containsKey(armyId)) {
						selfMap.put(armyId, selfMap.get(armyId) + cnt);
					} else {
						selfMap.put(armyId, cnt);
					}
				}
			}
		}
	}

	/**
	 * 计算玩家击杀&击伤数据(击杀数据均摊)
	 * 
	 * @param armyKillMap
	 * @param armyHurtMap
	 * @param selfBattleArmyMap
	 * @param oppBattleArmyMap
	 */
	public void calcKillAndHurtInfo(Map<String, Map<Integer, Integer>> armyKillMap, Map<String, Map<Integer, Integer>> armyHurtMap, Map<String, List<ArmyInfo>> selfBattleArmyMap,
			Map<String, List<ArmyInfo>> oppBattleArmyMap) {
		// 对方部队死亡信息
		Map<Integer, Integer> oppDeadMap = new HashMap<>();
		// 对方部队受伤信息
		Map<Integer, Integer> oppHurtMap = new HashMap<>();
		// 对方部队总损失信息
		Map<Integer, Integer> oppLoseMap = new HashMap<>();

		// 统计对方部队死伤数据
		for (List<ArmyInfo> armyList : oppBattleArmyMap.values()) {
			for (ArmyInfo army : armyList) {
				int armyId = army.getArmyId();
				// 影子部队的损失算为击伤
				int hurtCnt = army.getWoundedCount() + army.getShadowDeadCnt();
				int deadCnt = army.getDeadCount();

				if (deadCnt > 0) {
					if (oppDeadMap.containsKey(armyId)) {
						oppDeadMap.put(armyId, oppDeadMap.get(armyId) + deadCnt);
					} else {
						oppDeadMap.put(armyId, deadCnt);
					}
				}

				if (hurtCnt > 0) {
					if (oppHurtMap.containsKey(armyId)) {
						oppHurtMap.put(armyId, oppHurtMap.get(armyId) + hurtCnt);
					} else {
						oppHurtMap.put(armyId, hurtCnt);
					}
				}

				if (hurtCnt + deadCnt > 0) {
					if (oppLoseMap.containsKey(armyId)) {
						Integer oph = oppHurtMap.get(armyId);
						Integer opd = oppDeadMap.get(armyId);
						oppLoseMap.put(armyId, (oph == null ? 0 : oph) + (opd == null ? 0 : opd));
					} else {
						oppLoseMap.put(armyId, hurtCnt + deadCnt);
					}
				}
			}
		}
		// 对方部队全部受伤
		if (!oppHurtMap.isEmpty() && oppDeadMap.isEmpty()) {
			calcKillOrHurtInfo(armyHurtMap, selfBattleArmyMap);
		}
		// 对方部队全部死亡
		else if (oppHurtMap.isEmpty() && !oppDeadMap.isEmpty()) {
			calcKillOrHurtInfo(armyKillMap, selfBattleArmyMap);
		}
		// 对方部队无死伤
		else if (oppHurtMap.isEmpty() && oppDeadMap.isEmpty()) {
			return;
		}
		// 对方部队同时存在死兵和伤兵,进行均摊计算
		else {
			splitKillInfo(oppDeadMap, oppLoseMap, armyKillMap, armyHurtMap, selfBattleArmyMap);
		}

	}

	/**
	 * 均摊计算部队击杀数据
	 * 
	 * @param oppDeadMap
	 * @param oppLoseMap
	 * @param armyKillMap
	 * @param armyHurtMap
	 * @param selfBattleArmyMap
	 */
	private void splitKillInfo(Map<Integer, Integer> oppDeadMap, Map<Integer, Integer> oppLoseMap, Map<String, Map<Integer, Integer>> armyKillMap,
			Map<String, Map<Integer, Integer>> armyHurtMap,
			Map<String, List<ArmyInfo>> selfBattleArmyMap) {
		// 计算玩家击败数据
		Map<String, Map<Integer, Integer>> map = new HashMap<>();
		calcKillOrHurtInfo(map, selfBattleArmyMap);
		for (String playerId : selfBattleArmyMap.keySet()) {
			Map<Integer, Integer> hurtMap = map.get(playerId); // 玩家击败数据
			for (Entry<Integer, Integer> entry : hurtMap.entrySet()) {
				int armyId = entry.getKey();
				int hurtCnt = entry.getValue(); // 我击败的
				int oppDeadCnt = oppDeadMap.containsKey(armyId) ? oppDeadMap.get(armyId) : 0;
				int oppLose = oppLoseMap.containsKey(armyId) ? oppLoseMap.get(armyId) : 0;
				// 正确状态下oppLose不可能为0,容错处理
				if (oppLose == 0) {
					HawkLog.logPrintln("BattleService splitKillInfo error , oppLose is 0, playerId: {}, armyId: {}, hurtCnt: {}, oppDeadCnt: {}, oppLose: {}", playerId, armyId,
							hurtCnt, oppDeadCnt, oppLose);
					continue;
				}
				// 对方部队该兵种未死亡
				if (!oppDeadMap.containsKey(armyId)) {
					int hurt = (int) Math.floor(hurtCnt * ((oppLose - oppDeadCnt) / oppLose)); // hurtCnt全部都为击伤
					if (hurt > 0) {
						if (!armyHurtMap.containsKey(playerId)) {
							armyHurtMap.put(playerId, new HashMap<>());
						}
						armyHurtMap.get(playerId).put(armyId, hurt);
					}
					continue;
				}

				// 根据对方玩家该兵种死亡比例计算玩家击杀数
				// 玩家该兵种击杀数 = 玩家击伤该兵种总数 * (对方该兵种死亡数/对方该兵种死亡&受伤总数)
				int killCnt = (int) Math.floor(hurtCnt * (1d * oppDeadCnt / oppLose));
				int hurt = (int) Math.floor(hurtCnt * (1d * (oppLose - oppDeadCnt) / oppLose));
				if (killCnt > 0) {
					// 设置玩家实际击伤数量
					// entry.setValue(hurtCnt - killCnt);
					if (!armyKillMap.containsKey(playerId)) {
						armyKillMap.put(playerId, new HashMap<>());
					}
					// 设置玩家击杀数量
					armyKillMap.get(playerId).put(armyId, killCnt);
				}
				if (hurt > 0) {
					if (!armyHurtMap.containsKey(playerId)) {
						armyHurtMap.put(playerId, new HashMap<>());
					}
					// 设置玩家击杀数量
					armyHurtMap.get(playerId).put(armyId, hurt);
				}
			}
		}

	}
	
	/**
	 * 计算玩家部队击杀/击伤数据
	 * 
	 * @param armyMap
	 * @param selfBattleArmyMap
	 */
	private void calcKillOrHurtDetailInfo(Map<String, Map<Long, Integer>> armyMap, Map<String, List<ArmyInfo>> selfBattleArmyMap) {
		for (Entry<String, List<ArmyInfo>> entry : selfBattleArmyMap.entrySet()) {
			String playerId = entry.getKey();
			Map<Long, Integer> selfMap = new HashMap<>();
			armyMap.put(playerId, selfMap);
			List<ArmyInfo> armyInfoList = entry.getValue();
			for (ArmyInfo armyInfo : armyInfoList) {
				Map<Long, Integer> killMap = armyInfo.getKillDetail();
				for (Entry<Long, Integer> killEntry : killMap.entrySet()) {
					long calcedId = killEntry.getKey();
					int cnt = killEntry.getValue();
					if (selfMap.containsKey(calcedId)) {
						selfMap.put(calcedId, selfMap.get(calcedId) + cnt);
					} else {
						selfMap.put(calcedId, cnt);
					}
				}
			}
		}
	}

	/**
	 * 计算玩家击杀&击伤数据(击杀数据均摊)
	 * 
	 * @param armyKillDetailMap
	 * @param armyKillDetailMap
	 * @param selfBattleArmyMap
	 * @param oppBattleArmyMap
	 */
	public void calcKillAndHurtDetailInfo(Map<String, Map<Long, Integer>> armyKillDetailMap, Map<String, Map<Long, Integer>> armyHurtDetailMap, Map<String, List<ArmyInfo>> selfBattleArmyMap,
			Map<String, List<ArmyInfo>> oppBattleArmyMap) {
		// 对方部队死亡信息
		Map<Long, Integer> oppDeadMap = new HashMap<>();
		// 对方部队受伤信息
		Map<Long, Integer> oppHurtMap = new HashMap<>();
		// 对方部队总损失信息
		Map<Long, Integer> oppLoseMap = new HashMap<>();

		// 统计对方部队死伤数据
		for (List<ArmyInfo> armyList : oppBattleArmyMap.values()) {
			for (ArmyInfo army : armyList) {
				int armyId = army.getArmyId();
				long calcedId = calcArmyId(armyId, army.getStar());
				// 影子部队的损失算为击伤
				int hurtCnt = army.getWoundedCount() + army.getShadowDeadCnt();
				int deadCnt = army.getDeadCount();

				if (deadCnt > 0) {
					if (oppDeadMap.containsKey(calcedId)) {
						oppDeadMap.put(calcedId, oppDeadMap.get(calcedId) + deadCnt);
					} else {
						oppDeadMap.put(calcedId, deadCnt);
					}
				}

				if (hurtCnt > 0) {
					if (oppHurtMap.containsKey(calcedId)) {
						oppHurtMap.put(calcedId, oppHurtMap.get(calcedId) + hurtCnt);
					} else {
						oppHurtMap.put(calcedId, hurtCnt);
					}
				}

				if (hurtCnt + deadCnt > 0) {
					if (oppLoseMap.containsKey(calcedId)) {
						Integer oph = oppHurtMap.get(calcedId);
						Integer opd = oppDeadMap.get(calcedId);
						oppLoseMap.put(calcedId, (oph == null ? 0 : oph) + (opd == null ? 0 : opd));
					} else {
						oppLoseMap.put(calcedId, hurtCnt + deadCnt);
					}
				}
			}
		}
		// 对方部队全部受伤
		if (!oppHurtMap.isEmpty() && oppDeadMap.isEmpty()) {
			calcKillOrHurtDetailInfo(armyHurtDetailMap, selfBattleArmyMap);
		}
		// 对方部队全部死亡
		else if (oppHurtMap.isEmpty() && !oppDeadMap.isEmpty()) {
			calcKillOrHurtDetailInfo(armyKillDetailMap, selfBattleArmyMap);
		}
		// 对方部队无死伤
		else if (oppHurtMap.isEmpty() && oppDeadMap.isEmpty()) {
			return;
		}
		// 对方部队同时存在死兵和伤兵,进行均摊计算
		else {
			splitKillDetailInfo(oppDeadMap, oppLoseMap, armyKillDetailMap, armyHurtDetailMap, selfBattleArmyMap);
		}

	}

	/**
	 * 均摊计算部队击杀数据
	 * 
	 * @param oppDeadMap
	 * @param oppLoseMap
	 * @param armyKillMap
	 * @param armyHurtMap
	 * @param selfBattleArmyMap
	 */
	private void splitKillDetailInfo(Map<Long, Integer> oppDeadMap, Map<Long, Integer> oppLoseMap, Map<String, Map<Long, Integer>> armyKillMap,
			Map<String, Map<Long, Integer>> armyHurtMap,
			Map<String, List<ArmyInfo>> selfBattleArmyMap) {
		// 计算玩家击败数据
		Map<String, Map<Long, Integer>> map = new HashMap<>();
		calcKillOrHurtDetailInfo(map, selfBattleArmyMap);
		for (String playerId : selfBattleArmyMap.keySet()) {
			Map<Long, Integer> hurtMap = map.get(playerId); // 玩家击败数据
			for (Entry<Long, Integer> entry : hurtMap.entrySet()) {
				long calcedId = entry.getKey();
				int hurtCnt = entry.getValue(); // 我击败的
				int oppDeadCnt = oppDeadMap.containsKey(calcedId) ? oppDeadMap.get(calcedId) : 0;
				int oppLose = oppLoseMap.containsKey(calcedId) ? oppLoseMap.get(calcedId) : 0;
				// 正确状态下oppLose不可能为0,容错处理
				if (oppLose == 0) {
					HawkLog.logPrintln("BattleService splitKillDetailInfo error , oppLose is 0, playerId: {}, armyId: {}, hurtCnt: {}, oppDeadCnt: {}, oppLose: {}", playerId, calcedId,
							hurtCnt, oppDeadCnt, oppLose);
					continue;
				}
				// 对方部队该兵种未死亡
				if (!oppDeadMap.containsKey(calcedId)) {
					int hurt = (int) Math.floor(hurtCnt * ((oppLose - oppDeadCnt) / oppLose)); // hurtCnt全部都为击伤
					if (hurt > 0) {
						if (!armyHurtMap.containsKey(playerId)) {
							armyHurtMap.put(playerId, new HashMap<>());
						}
						armyHurtMap.get(playerId).put(calcedId, hurt);
					}
					continue;
				}

				// 根据对方玩家该兵种死亡比例计算玩家击杀数
				// 玩家该兵种击杀数 = 玩家击伤该兵种总数 * (对方该兵种死亡数/对方该兵种死亡&受伤总数)
				int killCnt = (int) Math.floor(hurtCnt * (1d * oppDeadCnt / oppLose));
				int hurt = (int) Math.floor(hurtCnt * (1d * (oppLose - oppDeadCnt) / oppLose));
				if (killCnt > 0) {
					// 设置玩家实际击伤数量
					// entry.setValue(hurtCnt - killCnt);
					if (!armyKillMap.containsKey(playerId)) {
						armyKillMap.put(playerId, new HashMap<>());
					}
					// 设置玩家击杀数量
					armyKillMap.get(playerId).put(calcedId, killCnt);
				}
				if (hurt > 0) {
					if (!armyHurtMap.containsKey(playerId)) {
						armyHurtMap.put(playerId, new HashMap<>());
					}
					// 设置玩家击杀数量
					armyHurtMap.get(playerId).put(calcedId, hurt);
				}
			}
		}

	}

	public int getDuelPower() {
		return duelPower;
	}
	
	/**
	 * 计算兵种id&星级的结合值
	 * @param armyId
	 * @param star
	 * @return
	 */
	public static long calcArmyId(int armyId, int star) {
		return GsConst.ARMY_STAR_OFFSET * star + armyId;
	}
	
	/**
	 * 计算兵种id&星级
	 * @param calcedId
	 * @return
	 */
	public static HawkTuple2<Integer, Integer> splicArmyId(long calcedId) {
		if (calcedId < GsConst.ARMY_STAR_OFFSET) {
			return new HawkTuple2<Integer, Integer>(0, (int) calcedId);
		}
		int star = (int) (calcedId / GsConst.ARMY_STAR_OFFSET);
		int armyId = (int) (calcedId % GsConst.ARMY_STAR_OFFSET);
		HawkTuple2<Integer, Integer> tuple = new HawkTuple2<Integer, Integer>(star, armyId);
		return tuple;
	}
}
