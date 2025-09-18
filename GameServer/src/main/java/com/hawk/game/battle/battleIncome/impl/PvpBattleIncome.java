package com.hawk.game.battle.battleIncome.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.GsApp;
import com.hawk.game.battle.Battle;
import com.hawk.game.battle.BattleCalcParames;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.AllianceBeatbackCfg;
import com.hawk.game.config.AllianceCareCfg;
import com.hawk.game.config.AllianceCompensationCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.entity.GuildCounterattackEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.Counterattack;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.CYBORGExtraParam;
import com.hawk.game.lianmengstarwars.SWExtraParam;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZExtraParam;
import com.hawk.game.module.lianmengXianquhx.XQHXExtraParam;
import com.hawk.game.module.lianmengtaiboliya.TBLYExtraParam;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZExtraParam;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZExtraParam;
import com.hawk.game.module.nationMilitary.cfg.NationMilitaryBattleCfg;
import com.hawk.game.msg.HospiceLostPowerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.util.BattleUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.SysFunctionModuleId;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;

/**
 * 战前准备信息
 * 
 * @author Jesse
 */
public class PvpBattleIncome implements IBattleIncome {

	private boolean isGrabResMarch;
	/**
	 * 战斗数据
	 */
	private Battle battle;

	/**
	 * 进攻方玩家列表
	 */
	private List<Player> atkPlayers;

	/**
	 * 防御方玩家列表
	 */
	private List<Player> defPlayers;

	/**
	 * 进攻方部队信息<playerId, armyList>
	 */
	private Map<String, List<ArmyInfo>> atkArmyMap;

	/**
	 * 防御方部队信息<playerId, armyList>
	 */
	private Map<String, List<ArmyInfo>> defArmyMap;
	/**
	 * 进攻玩家计算相关信息
	 */
	private BattleCalcParames atkCalcParames;
	/**
	 * 防御玩家计算相关信息
	 */
	private BattleCalcParames defCalcParames;
	private BattleSkillType skillType;

	// 副本相关
	/**联盟军演邮件*/
	private DungeonMailType duntype = DungeonMailType.NONE;
	/** 如果在副本中战斗 副本名*/
	private String dungeon = "";
	/** 如果在副本中战斗 战场id*/
	private String dungeonId = "";
	/**是否联赛*/
	private int isLeaguaWar;
	/**赛季*/
	private int season;

	public PvpBattleIncome() {

	}

	public Battle getBattle() {
		return battle;
	}

	public PvpBattleIncome setBattle(Battle battle) {
		this.battle = battle;
		return this;
	}

	public List<Player> getAtkPlayers() {
		return atkPlayers;
	}

	public PvpBattleIncome setAtkPlayers(List<Player> atkPlayers) {
		this.atkPlayers = atkPlayers;
		return this;
	}

	public List<Player> getDefPlayers() {
		return defPlayers;
	}

	public PvpBattleIncome setDefPlayers(List<Player> defPlayers) {
		this.defPlayers = defPlayers;
		return this;
	}

	public Map<String, List<ArmyInfo>> getAtkArmyMap() {
		return atkArmyMap;
	}

	public PvpBattleIncome setAtkArmyMap(Map<String, List<ArmyInfo>> atkArmyMap) {
		this.atkArmyMap = atkArmyMap;
		return this;
	}

	public Map<String, List<ArmyInfo>> getDefArmyMap() {
		return defArmyMap;
	}

	public PvpBattleIncome setDefArmyMap(Map<String, List<ArmyInfo>> defArmyMap) {
		this.defArmyMap = defArmyMap;
		return this;
	}

	public BattleCalcParames getAtkCalcParames() {
		return atkCalcParames;
	}

	public PvpBattleIncome setAtkCalcParames(BattleCalcParames atkCalcParames) {
		this.atkCalcParames = atkCalcParames;
		return this;
	}

	public BattleCalcParames getDefCalcParames() {
		return defCalcParames;
	}

	public PvpBattleIncome setDefCalcParames(BattleCalcParames defCalcParames) {
		this.defCalcParames = defCalcParames;
		return this;
	}

	@Override
	public boolean isGrabResMarch() {
		return isGrabResMarch;
	}

	/**
	 * 是否为集结进攻
	 * 
	 * @return
	 */
	public boolean isMassAtk() {
		return atkPlayers.size() > 1;
	}

	/**
	 * 是否为集结防守
	 * 
	 * @return
	 */
	public boolean isAssitanceDef() {
		return defPlayers.size() > 1;
	}

	@Override
	public BattleOutcome gatherBattleResult() {
		Battle battle = this.getBattle();
		boolean isWin = battle.getWinTroop() == BattleConst.Troop.ATTACKER;
		// 进攻方本次参战部队信息
		Map<String, List<ArmyInfo>> battleArmyMapAtk = new HashMap<>();
		// 进攻方战后剩余部队信息
		Map<String, List<ArmyInfo>> aftArmyMapAtk = new HashMap<>();
		BattleService.getInstance().calcArmyInfo(battleArmyMapAtk, aftArmyMapAtk, this.getAtkCalcParames(), this.getAtkArmyMap(), battle);

		// 防御方本次参战部队信息
		Map<String, List<ArmyInfo>> battleArmyMapDef = new HashMap<>();
		// 防御方战后剩余部队信息
		Map<String, List<ArmyInfo>> aftArmyMapDef = new HashMap<>();
		BattleService.getInstance().calcArmyInfo(battleArmyMapDef, aftArmyMapDef, this.getDefCalcParames(), this.getDefArmyMap(), battle);
		BattleOutcome battleOutcome = new BattleOutcome(battleArmyMapAtk, battleArmyMapDef, aftArmyMapAtk, aftArmyMapDef, isWin);

		if (atkPlayers.get(0).getClass() == Player.class && getDuntype() == DungeonMailType.NONE) {
			// 发放伤兵补偿邮件
			hospice(battleOutcome);
		}

		return battleOutcome;
	}

	/**计算军功*/
	@Override
	public void calNationMilitary(BattleOutcome battleOutcome) {
		try {
			
			if (Objects.equals(atkPlayers.get(0).getMainServerId(), defPlayers.get(0).getMainServerId())) { // 不同服, 计算军功
				return;
			}
			if (atkPlayers.get(0).isInDungeonMap() && !battleOutcome.isYqzzNationMilly()) {
				return;
			}
			Map<String, Integer> nationMilitary = new HashMap<>();

			Map<String, Map<Integer, Integer>> atkKillMap = new HashMap<>();
			Map<String, Map<Integer, Integer>> atkHurtMap = new HashMap<>();
			BattleService.getInstance().calcKillAndHurtInfo(atkKillMap, atkHurtMap, battleOutcome.getBattleArmyMapAtk(), battleOutcome.getBattleArmyMapDef());
			calcKillAndHurtPower(nationMilitary, atkPlayers, atkKillMap, atkHurtMap);

			Map<String, Map<Integer, Integer>> defKillMap = new HashMap<>();
			Map<String, Map<Integer, Integer>> defHurtMap = new HashMap<>();
			BattleService.getInstance().calcKillAndHurtInfo(defKillMap, defHurtMap, battleOutcome.getBattleArmyMapDef(), battleOutcome.getBattleArmyMapAtk());
			calcKillAndHurtPower(nationMilitary, defPlayers, defKillMap, defHurtMap);

			// 攻击方剩余兵力
			// 计算损失兵力
			calcSelfLosePower(nationMilitary, atkPlayers, battleOutcome.getBattleArmyMapAtk());
			// 防守方剩余兵力
			// 计算损失兵力
			calcSelfLosePower(nationMilitary, defPlayers, battleOutcome.getBattleArmyMapDef());

			battleOutcome.setNationMilitary(nationMilitary);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 自己的击杀,击伤 
	 * @return */
	private void calcKillAndHurtPower(Map<String, Integer> nationMilitary, List<Player> battlePlayers, Map<String, Map<Integer, Integer>> battleKillMap,
			Map<String, Map<Integer, Integer>> battleHurtMap) {
		for (Player ppp : battlePlayers) {
			double fenshu = 0;
			Map<Integer, Integer> killMap = battleKillMap.get(ppp.getId());
			if (killMap != null) {
				for (Entry<Integer, Integer> ent : killMap.entrySet()) {
					BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, ent.getKey());
					if (!cfg.isDefWeapon() && ent.getValue() > 0) {
						NationMilitaryBattleCfg mcfg = HawkConfigManager.getInstance().getCombineConfig(NationMilitaryBattleCfg.class, cfg.getLevel(), cfg.getType());
						if (mcfg != null) {
							fenshu = fenshu + mcfg.getKill() * 0.001 * ent.getValue();
						}
					}
				}
			}

			if (fenshu > 1) {
				int val = ppp.increaseNationMilitary((int) fenshu, PlayerAttr.NATION_MILITARY_BATTLE_VALUE, Action.BATTLE_NATION_MILITARY, true);
				nationMilitary.merge(ppp.getId(), val, (v1, v2) -> v1 + v2);
			}
		}
	}

	private void calcSelfLosePower(Map<String, Integer> nationMilitary, List<Player> battlePlayers, Map<String, List<ArmyInfo>> leftArmyMap) {
		for (Player ppp : battlePlayers) {
			List<ArmyInfo> leftList = leftArmyMap.get(ppp.getId());
			if (leftList == null) {
				continue;
			}
			double fenshu = 0;
			for (ArmyInfo army : leftList) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
				if (!cfg.isDefWeapon() && army.getDeadCount() > 0) {
					NationMilitaryBattleCfg mcfg = HawkConfigManager.getInstance().getCombineConfig(NationMilitaryBattleCfg.class, cfg.getLevel(), cfg.getType());
					if (mcfg != null) {
						fenshu = fenshu + mcfg.getDie() * 0.001 * army.getDeadCount();
					}
				}
			}
			if (fenshu > 1) {
				int val = ppp.increaseNationMilitary((int) fenshu, PlayerAttr.NATION_MILITARY_BATTLE_VALUE, Action.BATTLE_NATION_MILITARY, true);
				nationMilitary.merge(ppp.getId(), val, (v1, v2) -> v1 + v2);
			}
		}
	}

	public PvpBattleIncome setGrabResMarch(boolean isGrabResMarch) {
		this.isGrabResMarch = isGrabResMarch;
		return this;
	}

	private void hospice(BattleOutcome battleOutcome) {
		for (Entry<String, List<ArmyInfo>> entry : battleOutcome.getBattleArmyMapDef().entrySet()) {
			String playerId = entry.getKey();
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			// 如果是跨服玩家,则不进行补偿发放
			if (player == null || player.isCsPlayer()) {
				continue;
			}
			List<ArmyInfo> battleArmy = entry.getValue();
			GsApp.getInstance().addDelayAction(5000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					PvpBattleIncome.this.sendDeferRecoup(atkPlayers.get(0), player, battleArmy, battleOutcome.isAtkWin());
				}
			});
		}

		for (Entry<String, List<ArmyInfo>> entry : battleOutcome.getBattleArmyMapAtk().entrySet()) {// 功方也有可能得到补偿
			String playerId = entry.getKey();
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			// 如果是跨服玩家,则不进行补偿发放
			if (player == null || player.isCsPlayer()) {
				continue;
			}
			List<ArmyInfo> battleArmy = entry.getValue();
			GsApp.getInstance().addDelayAction(5000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					PvpBattleIncome.this.sendAtkerRecoup(player, battleOutcome.isAtkWin(), defPlayers.get(0), battleArmy);
				}
			});
		}
	}

	private void sendAtkerRecoup(Player player, boolean atkWin, Player atker, List<ArmyInfo> battleArmy) {
		HawkTuple3<Double, Double, List<ArmyInfo>> lostCount = lostCount(battleArmy);
		double woundedPower = lostCount.first;
		double deadPower = lostCount.second;
		List<ArmyInfo> marchArmy = lostCount.third;

		AllianceCareCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCareCfg.class, player.getCityLevel());
		// 损失百分比过大
		boolean abc = (woundedPower + deadPower) / player.getData().getPowerElectric().getPowerData().getArmyBattlePoint() > cfg.getPyrrhicVictoryPercent();
		if (!atkWin || abc) {
			// 伤兵
			if (GameUtil.checkSysFunctionOpen(player, SysFunctionModuleId.HELPAWARD)) {
				sendHospice(atker, player, woundedPower, deadPower, marchArmy);
			}
		}
	}

	/**
	 * 发放伤死兵补偿邮件, 发启联盟反击
	 */
	private void sendDeferRecoup(Player attacker, Player player, List<ArmyInfo> defbattleArmy, boolean isAtkwin) {
		if (Objects.isNull(player)) {
			return;
		}
		HawkTuple3<Double, Double, List<ArmyInfo>> lostCount = lostCount(defbattleArmy);
		double woundedPower = lostCount.first;
		double deadPower = lostCount.second;
		List<ArmyInfo> marchArmy = lostCount.third;

		// 伤兵
		if (GameUtil.checkSysFunctionOpen(player, SysFunctionModuleId.HELPAWARD)) {
			sendHospice(attacker, player, woundedPower, deadPower, marchArmy);
		}
		// 联盟反击
		if (player.hasGuild() && isAtkwin) {
			startGuildCounterAttack(attacker, player, woundedPower, deadPower, marchArmy);
		}
		// 反击
		if (attacker.hasGuild()) {
			List<Counterattack> guildCounterList = GuildService.getInstance().guildCounterAttack(attacker.getGuildId());
			for (Counterattack ct : guildCounterList) {
				if (Objects.equals(ct.getDbObj().getAtkerId(), player.getId())) {
					ct.upWipout(attacker, (int) (woundedPower + deadPower), battle.getPointId());
				}
			}

		}
	}

	private HawkTuple3<Double, Double, List<ArmyInfo>> lostCount(List<ArmyInfo> battleArmy) {
		double woundedPower = 0;
		double deadPower = 0;
		List<ArmyInfo> marchArmy = new ArrayList<>(); // 非防御武器
		for (ArmyInfo army : battleArmy) {
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if (soldierCfg.isDefWeapon()) {
				continue;
			}
			marchArmy.add(army);
			woundedPower = woundedPower + soldierCfg.getPower() * army.getWoundedCount();
			deadPower = deadPower + soldierCfg.getPower() * army.getDeadCount();
		}
		return HawkTuples.tuple(woundedPower, deadPower, marchArmy);
	}

	/***/
	private void startGuildCounterAttack(Player attacker, Player player, double woundedPower, double deadPower, List<ArmyInfo> marchArmy) {
		Optional<AllianceBeatbackCfg> cfgOp = HawkConfigManager.getInstance().getConfigIterator(AllianceBeatbackCfg.class).stream()
				.filter(cfg -> cfg.getPower() < (woundedPower + deadPower))
				.sorted(Comparator.comparingInt(AllianceBeatbackCfg::getPower).reversed())
				.findFirst();

		if (!cfgOp.isPresent()) {
			return;
		}
		AllianceBeatbackCfg cfg = cfgOp.get();
		AllianceCompensationCfg comCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCompensationCfg.class, player.getCityLevel());
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(BattleUtil.cureCost(player, marchArmy, comCfg.getBeatbackInjuredSoldierSpeedUpCoefficient(), comCfg.getBeatbackInjuredSoldierResourceCoefficient()));
		award.addItemInfos(BattleUtil.deadCost(player, marchArmy, comCfg.getBeatbackdDeadSoldierSpeedUpCoefficient(), comCfg.getBeatbackDeadSoldierResourceCoefficient()));

		GuildCounterattackEntity entity = new GuildCounterattackEntity();
		entity.setGuildId(player.getGuildId());
		entity.setPlayerId(player.getId());
		entity.setLostPower((int) (woundedPower + deadPower));
		entity.setAtkerId(attacker.getId());
		entity.setCounterPower(cfg.getKillPower());
		entity.setAttackerPointId(WorldPlayerService.getInstance().getPlayerPos(attacker.getId()));
		entity.setRewards(cfg.getSystemMoney());
		entity.setBitBackRewards(ItemInfo.toString(award.getAwardItems()));
		entity.setOverTime(HawkTime.getMillisecond() + ConstProperty.getInstance().getAllianceCareHelpSustainTime() * 1000);
		Counterattack counter = Counterattack.create(entity);

		HawkDBManager.getInstance().create(entity);
		GuildService.getInstance().counterattackAdd(counter);
		// TODO 联盟聊天通知. 未给出配置
		// ChatParames parames = ChatParames.newBuilder()
		// .setChatType(Const.ChatType.CHAT_ALLIANCE)
		// .setKey(Const.NoticeCfgId.MAIL_SHARE)
		// .setPlayer(player)
		// .addParms(player.getName(), uuid, player.getId(), mail.getMailId(),
		// type)
		// .addParms(request.getParamesList().toArray())
		// .build();
		// ChatService.getInstance().addWorldBroadcastMsg(parames);

		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(attacker.getId());
		if (worldPoint == null) {
			return;
		}
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());

	}

	/** 发放伤死兵补偿邮件 */
	private void sendHospice(Player atkPlayer, Player player, double woundedPower, double deadPower, List<ArmyInfo> marchArmy) {
		long historyMax = player.getData().getGuildHospiceEntity().getHospiceObj().getGuildHospiceEntity().getMaxPower();
		AllianceCareCfg comCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCareCfg.class, player.getCityLevel());
		double lostRate = (woundedPower + deadPower) / historyMax;
		double buchongxishu = 0;
		if (lostRate > comCfg.getOverwhelmingRate()) {
			buchongxishu = comCfg.getOverwhelmingPay();
		}
		// 是否碾压
		List<ItemInfo> curelist = BattleUtil.cureCost(player, marchArmy, comCfg.getInjuredSoldierSpeedUpCoefficient() + buchongxishu,
				comCfg.getInjuredSoldierResourceCoefficient() + buchongxishu);

		List<ItemInfo> deadlist = BattleUtil.deadCost(player, marchArmy, comCfg.getDeadSoldierSpeedUpCoefficient() + buchongxishu,
				comCfg.getDeadSoldierResourceCoefficient() + buchongxishu);

		HospiceLostPowerMsg msg = HospiceLostPowerMsg.valueOf(atkPlayer.getId(), deadPower, woundedPower, curelist, deadlist);
		if (buchongxishu > 0) {
			msg.setOverwhelming(1);
		}
		HawkApp.getInstance().postMsg(player.getXid(), msg);
	}

	public BattleSkillType getSkillType() {
		return skillType;
	}

	public PvpBattleIncome setSkillType(BattleSkillType skillType) {
		this.skillType = skillType;
		return this;
	}

	public DungeonMailType getDuntype() {
		return duntype;
	}

	public void setDuntype(DungeonMailType duntype) {
		this.duntype = duntype;
	}

	public String getDungeon() {
		return dungeon;
	}

	public void setDungeon(String dungeon) {
		this.dungeon = dungeon;
	}

	public String getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(String dungeonId) {
		this.dungeonId = dungeonId;
	}

	public int getIsLeaguaWar() {
		return isLeaguaWar;
	}

	public void setIsLeaguaWar(int isLeaguaWar) {
		this.isLeaguaWar = isLeaguaWar;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public void setTBLYMail(TBLYExtraParam tblyMail) {
		this.duntype = DungeonMailType.TBLY;
		this.dungeon = "Tiberium";
		this.dungeonId = tblyMail.getBattleId();
		this.season = tblyMail.getSeason();
		this.isLeaguaWar = tblyMail.isLeaguaWar() ? 1 : 0;
		getBattle().setDuntype(DungeonMailType.TBLY);
	}

	public void setCYBORGMail(CYBORGExtraParam extParm) {
		duntype = DungeonMailType.CYBORG;
		this.dungeon = "Cyborg";
		this.dungeonId = extParm.getBattleId();
		this.season = extParm.getSeason();
		this.isLeaguaWar = extParm.isLeaguaWar() ? 1 : 0;
		getBattle().setDuntype(DungeonMailType.CYBORG);
	}

	public void setSWMail(SWExtraParam extParm) {
		this.duntype = DungeonMailType.SW;
		this.dungeon = "StarWars";
		this.dungeonId = extParm.getBattleId();
		getBattle().setDuntype(DungeonMailType.SW);
	}

	public void setDYZZMail(DYZZExtraParam extParm) {
		this.duntype = DungeonMailType.DYZZ;
		this.dungeon = "dyzzWars";
		this.dungeonId = extParm.getBattleId();
		getBattle().setDuntype(DungeonMailType.DYZZ);
	}

	public void setYQZZMail(YQZZExtraParam extParm) {
		this.duntype = DungeonMailType.YQZZ;
		this.dungeon = "MoonWar";
		this.dungeonId = extParm.getBattleId();
		getBattle().setDuntype(DungeonMailType.YQZZ);
		
	}
	
	public void setXHJZMail(XHJZExtraParam extParm) {
		this.duntype = DungeonMailType.XHJZ;
		this.dungeon = "xhjz";
		this.dungeonId = extParm.getBattleId();
		getBattle().setDuntype(DungeonMailType.XHJZ);
		
	}

	public void setXQHXMail(XQHXExtraParam extParm) {
		this.duntype = DungeonMailType.XQHX;
		this.dungeon = "xqhx";
		this.dungeonId = extParm.getBattleId();
		getBattle().setDuntype(DungeonMailType.XQHX);
		
	}
}
