package com.hawk.game.module.spacemecha.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleLogHelper;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaCabinCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.MonsterMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 进攻联盟机甲舱体的野怪行军
 * 
 */
public class SpaceMechaMonsterAttackMarch extends MonsterMarch implements BasedMarch, IReportPushMarch {

	public SpaceMechaMonsterAttackMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH;
	}

	@Override
	public Player getPlayer() {
		WorldMarch march = getMarchEntity();
		if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
			SpaceWorldPoint spacePoint = (SpaceWorldPoint) WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
			return spacePoint.getNpcPlayer(march.getOrigionId());
		} else {
			throw new RuntimeException("spaceMecha monster attack space march getPlayer error, marchId: " + march.getMarchId());
		}
	}

	@Override
	public void onMarchReach(Player player) {
		// 消除警报
		removeAttackReport();
		// 行军
		WorldMarch atkMarch = getMarchEntity();
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(atkMarch.getTerminalId());
		// 舱体信息
		String guildId = this.getTargetId();
		MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(guildId);

		// 战斗前检查条件
		if (!this.checkBeforeWar(targetPoint, spaceObject)) {
			// 直接删除行军
			spaceObject.removeEnemyMarch(getMarchId());
			return;
		}

		SpaceWorldPoint spacePoint = (SpaceWorldPoint) targetPoint;
		// 舱体已经被攻破了
		if (spacePoint.getSpaceBlood() <= 0) {
			HawkLog.logPrintln("spaceMecha monster attack space march reach, space broken, guildId: {}, spaceIndex: {}, monster marchId: {}, origionId: {}", spacePoint.getGuildId(), spacePoint.getSpaceIndex(), getMarchId(), atkMarch.getOrigionId());
			spaceObject.removeEnemyMarch(getMarchId());
			spacePoint.removeNpcPlayer(atkMarch.getOrigionId());
			return;
		}
		
		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		// 防守方驻军
		List<IWorldMarch> spaceMarchList = spaceObject.getSpaceMarchs(spacePoint.getSpaceIndex());
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		for (IWorldMarch march : spaceMarchList) {
			Player defplayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			defPlayers.add(defplayer);
			defMarchs.add(march);
		}
		
		List<ArmyInfo> monsterArmys = this.getMarchEntity().getArmyCopy();
		int totalMonster = monsterArmys.stream().mapToInt(e -> e.getTotalCount()).sum();
		if (defPlayers.isEmpty()) {
			int oldBlood = spacePoint.getSpaceBlood();
			defPlayerEmptyHandle(spacePoint, spaceObject, totalMonster, atkMarch);
			SpaceMechaService.getInstance().logSpaceMechaDefWar(spaceObject, spacePoint, oldBlood - spacePoint.getSpaceBlood());
			return;
		}
				
		List<Integer> heroIds = this.getMarchEntity().getEffectParams().getHeroIds();
		int enemyId = spaceObject.getMarchEnemyMap().get(atkMarch.getMarchId());
		try {
			// 战斗
			PveBattleIncome battleIncom = BattleService.getInstance().initGuildSpaceBattleData(BattleConst.BattleType.SPACE_MECHA_PVE, targetPoint.getId(), monsterArmys, defPlayers, defMarchs, heroIds, atkMarch.getOrigionId(), enemyId);
			
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncom);
			boolean atkWin = battleOutcome.isAtkWin();
			// 战后移除野怪行军
			spaceObject.removeEnemyMarch(getMarchId());
			
			// 防守方剩余兵力
			Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
			
			int oldBlood = spacePoint.getSpaceBlood();
			// 防守战斗结算
			calcResult(battleIncom, battleOutcome, totalMonster, spacePoint, spaceObject, atkMarch.getMarchId(), enemyId);
			
			spacePoint.storeAtkEnemyId(enemyId);
			FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.SPACE_MECHA_PVE, battleIncom, battleOutcome, null);
			
			// 战斗动画
			List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);
			WorldMarchService.getInstance().sendBattleResultInfo(this, atkWin, monsterArmys, defArmyList, atkWin);
			
			// 记录战斗日志
			BattleLogHelper battleLogHelper = new BattleLogHelper(battleIncom, battleOutcome, atkWin);
			battleLogHelper.logBattleFlow();
			SpaceMechaService.getInstance().logSpaceMechaDefWar(spaceObject, spacePoint, oldBlood - spacePoint.getSpaceBlood());
			
			// 更新防守玩家行军的部队
			updateDefMarchAfterWar(spaceMarchList, defArmyLeftMap);
			// 从联盟战争界面移除
			WorldMarchService.getInstance().rmGuildMarch(this.getMarchId());
			// 刷新战力
//			refreshPowerAfterWar(null, defPlayers);
			
		} catch (Exception e) {
			spaceObject.removeEnemyMarch(getMarchId());
			throw e;
		} finally {
			spacePoint.removeNpcPlayer(atkMarch.getOrigionId());
		}
		
		// 防守成功处理（按血量来说）
		if (spacePoint.getSpaceBlood() <= 0) {
			sendDefenceFailedNotice(spacePoint);
		}
	}
	
	/**
	 * 舱体内没有防守玩家时的处理
	 * 
	 */
	private void defPlayerEmptyHandle(SpaceWorldPoint spacePoint, MechaSpaceInfo spaceObject, int totalMonster, WorldMarch atkMarch) {
		int oldBlood = spacePoint.getSpaceBlood();
		spacePoint.setSpaceBlood(Math.max(0, oldBlood - totalMonster));
		HawkLog.logPrintln("spaceMecha monster attack space march reach, no defPlayers on space, guildId: {}, spaceIndex: {}, marchId: {}, posX: {}, poxY: {}, old blood: {}, remain blood: {}", 
				spacePoint.getGuildId(), spacePoint.getSpaceIndex(), atkMarch.getMarchId(), spacePoint.getX(), spacePoint.getY(), oldBlood, spacePoint.getSpaceBlood());
		
		spaceObject.removeEnemyMarch(getMarchId());
		spacePoint.removeNpcPlayer(atkMarch.getOrigionId());
		if (spacePoint.getSpaceBlood() <= 0) {
			sendDefenceFailedNotice(spacePoint);
		}
	}
	
	/**
	 * 防守失败(被打爆)通告
	 * 
	 * @param spacePoint
	 */
	private void sendDefenceFailedNotice(SpaceWorldPoint spacePoint) {
		HawkLog.logPrintln("spaceMecha broken, guildId: {}, spaceIndex: {}, monster marchId: {}, origionId: {}", spacePoint.getGuildId(), spacePoint.getSpaceIndex(), this.getMarchId());
		// 血量降到0了失败了
		if (spacePoint.getSpaceIndex() == SpacePointIndex.MAIN_SPACE) {
			SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(spacePoint.getSpaceLevel());
			Object[] object = new Object[] { spacePoint.getX(), spacePoint.getY(), cfg.getId() };
			ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_MAIN_DEF_FAIL).setGuildId(spacePoint.getGuildId()).addParms(object).build());
		} else {
			Object[] object = new Object[] { spacePoint.getX(), spacePoint.getY() };
			ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_SLAVE_DEF_FAIL).setGuildId(spacePoint.getGuildId()).addParms(object).build());
		}
		
		WorldPointService.getInstance().notifyPointUpdate(spacePoint.getX(), spacePoint.getY());
	}
	
	/**
	 * 战斗结束后的结算处理
	 * 
	 * @return
	 */
	private void calcResult(PveBattleIncome battleIncome ,BattleOutcome battleOutcome, int totalMonster, SpaceWorldPoint spacePoint, MechaSpaceInfo spaceObj, String atkMarchId, int enemyId) {
		int totalKill = 0;
		Map<String, List<ArmyInfo>> battleArmyLeftMap = battleOutcome.getBattleArmyMapDef();
		for (Entry<String, List<ArmyInfo>> entry : battleArmyLeftMap.entrySet()) {
			List<ArmyInfo> armyList = entry.getValue();
			int killCnt = armyList.stream().mapToInt(e -> e.getKillCount()).sum();
			totalKill += killCnt;
		}

		// 计算剩余血量
		int oldBlood = spacePoint.getSpaceBlood();
		int leftMonsterArmy = Math.max(0, totalMonster - totalKill);
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		double ratio = spacePoint.getSpaceIndex() == SpacePointIndex.MAIN_SPACE ? cfg.getMainSpaceDamageRatio() : cfg.getSubSpaceDamageRatio();
		leftMonsterArmy = (int) (leftMonsterArmy * ratio); 
		spacePoint.setSpaceBlood(Math.max(0, oldBlood - leftMonsterArmy));
		spaceObj.getStage().updateRecord();
		
		HawkLog.logPrintln("spaceMecha monster attack space march reach, guildId: {}, spaceIndex: {}, marchId: {}, posX: {}, poxY: {}, old blood: {}, remain blood: {}", 
				spacePoint.getGuildId(), spacePoint.getSpaceIndex(), atkMarchId, spacePoint.getX(), spacePoint.getY(), oldBlood, spacePoint.getSpaceBlood());
		
		// 添加参与战斗的防守成员
		battleIncome.getDefPlayers().forEach(e -> spacePoint.addDefenceMember(e.getId()));
	}

	@Override
	public long getMarchNeedTime() {
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		return cfg.getEnemyMarchTime();
	}

	private boolean checkBeforeWar(WorldPoint targetPoint, MechaSpaceInfo spaceObject) {
		// 行军信息
		WorldMarch march = getMarchEntity();
		String guildId = march.getTargetId();
		// 找不到目标
		if (spaceObject == null || targetPoint == null) {
			WorldMarchService.logger.info("spaceMecha attack space march check, point null, marchId: {}, targetX: {}, targetY: {}, guildId: {}", march.getMarchId(), march.getTerminalX(), march.getTerminalY(), guildId);
			return false;
		}

		// 目标点类型错误
		if (targetPoint.getPointType() != WorldPointType.SPACE_MECHA_MAIN_VALUE && targetPoint.getPointType() != WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
			WorldMarchService.logger.info("spaceMecha attack space march check, point type error, marchId: {}, targetX: {}, targetY: {}, guildId: {}", march.getMarchId(), march.getTerminalX(), march.getTerminalY(), guildId);
			return false;
		}
		
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) targetPoint;
		if (!spaceObject.getGuildId().equals(guildId) || march.getTerminalId() != spacePoint.getId()) {
			WorldMarchService.logger.info("spaceMecha attack space march check, targetPoint owner error, marchId: {}, targetX: {}, targetY: {}, guildId: {}", march.getMarchId(), march.getTerminalX(), march.getTerminalY(), guildId);
			return false;
		}
		
		return true;
	}

	/**
	 * 获取进攻方联盟战争界面信息
	 * @return
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarInitiativeInfo() {
		int origionId = this.getMarchEntity().getOrigionId();
		int pos[] = GameUtil.splitXAndY(origionId);
		String guildId = this.getMarchEntity().getTargetId();
		MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(guildId);
		WorldPointType pointType = WorldPointType.SPACE_MECHA_MONSTER;
		Integer monsterId = spaceObject.getMarchEnemyMap().get(this.getMarchId());
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		builder.setPointType(pointType);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setMonsterId(monsterId);
		return builder;
	}

	/**
	 * 获取防守方联盟战争界面信息
	 */
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) point;
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		builder.setPointType(WorldPointType.valueOf(point.getPointType()));  // -- 1
		builder.setX(spacePoint.getX());  // -- 2
		builder.setY(spacePoint.getY());  // -- 3

		String guildId = this.getMarchEntity().getTargetId();
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(spacePoint.getSpaceLevel());
		builder.setMonsterId(cfg.getId());
		// 队长id
		Player leader = spaceObj.getSpaceLeader(spacePoint.getSpaceIndex());
		if (leader == null) {
			return builder;
		}
		builder.setGridCount(leader.getMaxMassJoinMarchNum());  // -- 5
		String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());  // -- 6
		builder.setGuildTag(guildTag);

		// 队长信息
		GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
		leaderInfo.setPlayerId(leader.getId());
		leaderInfo.setPlayerName(leader.getName());
		leaderInfo.setIconId(leader.getIcon());
		leaderInfo.setPfIcon(leader.getPfIcon());
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		String leaderMarchId = spaceObj.getSpaceLeaderMarchId(spacePoint.getSpaceIndex());
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		for (ArmyInfo army : leaderMarch.getMarchEntity().getArmys()) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		leaderInfo.setMarchId(leaderMarchId);
		builder.setLeaderMarch(leaderInfo);
		
		builder.setLeaderArmyLimit(leaderMarch.getMaxMassJoinSoldierNum(leader));
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(leaderMarch.getMarchEntity().getArmys());
		
		List<IWorldMarch> marchs = spaceObj.getSpaceMarchs(spacePoint.getSpaceIndex());
		for (IWorldMarch stayMarch : marchs) {
			if (stayMarch.getMarchId().equals(leaderMarchId)) {
				continue;
			}
			builder.addJoinMarchs(getGuildWarSingleInfo(stayMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(stayMarch.getMarchEntity().getArmys());
		}
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}

	/**
	 * 联盟战争界面里单人信息
	 * @param worldMarch
	 * @return
	 */
	@Override
	public GuildWarSingleInfo.Builder getGuildWarSingleInfo(WorldMarch worldMarch) {
		Player player = GlobalData.getInstance().makesurePlayer(worldMarch.getPlayerId());
		GuildWarSingleInfo.Builder builder = GuildWarSingleInfo.newBuilder();
		builder.setPlayerId(worldMarch.getPlayerId());
		builder.setPlayerName(player.getName());
		builder.setIconId(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		List<PlayerHero> heros = player.getHeroByCfgId(worldMarch.getHeroIdList());
		if (heros != null && !heros.isEmpty()) {
			for (PlayerHero hero : heros) {
				builder.addHeroInfo(hero.toPBobj());
			}
		}
		List<ArmyInfo> armys = worldMarch.getArmys();
		for (ArmyInfo army : armys) {
			builder.addArmys(army.toArmySoldierPB(player));
		}
		builder.setEndTime(worldMarch.getEndTime());
		return builder;
	}

	@Override
	public void onMarchStart() {		
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public Set<String> attackReportRecipients() {
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(this.getMarchEntity().getTerminalId());
		if (worldPoint == null) {
			return Collections.emptySet();
		}
		
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) worldPoint;
		try {
			Set<String> defPlayerIds = spacePoint.getDefMarchList().stream().map(e -> e.getPlayerId()).collect(Collectors.toSet());
			return defPlayerIds;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Collections.emptySet();
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

}
