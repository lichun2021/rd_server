package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.xid.HawkXID;

import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;
import com.hawk.game.battle.BattleLogHelper;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.config.YuriRevengeCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.CityOnFireMsgInvoker;
import com.hawk.game.invoker.PlayerArmyBackMsgInvoker;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.Mail.MailPlayerInfo;
import com.hawk.game.protocol.Mail.YuriRevengeFightMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.MonsterMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 尤里复仇活动的怪物行军
 * @author zhenyu.shang
 * @since 2017年9月21日
 */
public class YuriRevengeMonsterMarch extends MonsterMarch implements BasedMarch, IReportPushMarch {

	/** 所属公会ID */
	private String guildId;

	private int round;

	private boolean lastRound;

	/** 下一轮开始时间 */
	private long nextPushTime;

	public YuriRevengeMonsterMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.YURI_MONSTER;
	}

	@Override
	public Player getPlayer() {
		NpcPlayer player = new NpcPlayer(HawkXID.nullXid());
		return player;
	}

	@Override
	public void onMarchReach(Player player) {
		// 消除警报
		removeAttackReport();
		String targetPlayerId = this.getTargetId();
		if (!HawkOSOperator.isEmptyString(targetPlayerId)) {			
			if (CrossService.getInstance().isCrossPlayer(targetPlayerId)) {
				HawkLog.errPrintln("yuri revenge can not attack a cross player:{}", targetPlayerId);
				// 直接删除行军
				WorldFoggyFortressService.getInstance().removeCurrentMonsterMarch(getGuildId(), getMarchId());
				return;
			}
			
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetPlayerId);
			if (targetPlayer == null) {
				HawkLog.errPrintln("yuri revenge target player is null playerId:{}", targetPlayerId);
				// 直接删除行军
				WorldFoggyFortressService.getInstance().removeCurrentMonsterMarch(getGuildId(), getMarchId());
				return;
			}
			
			String mapName = targetPlayer.getDungeonMap();
			if (!HawkOSOperator.isEmptyString(mapName)) {
				HawkLog.errPrintln("yuri revenge can't attack player in dungeon:{}", targetPlayerId);
				// 直接删除行军
				WorldFoggyFortressService.getInstance().removeCurrentMonsterMarch(getGuildId(), getMarchId());
				return;
			}					
		}
		// 行军
		WorldMarch atkMarch = getMarchEntity();
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(atkMarch.getTerminalId());
		// 防守玩家
		Player defPlayer = GlobalData.getInstance().makesurePlayer(atkMarch.getTargetId());

		// 战斗前检查条件
		if (!this.checkBeforeWar(targetPoint, defPlayer)) {
			// 直接删除行军
			WorldFoggyFortressService.getInstance().removeCurrentMonsterMarch(getGuildId(), getMarchId());
			return;
		}

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(defPlayer);

		// 防守方援军
		Set<IWorldMarch> helpMarchList = getDefMarch4War(defPlayer, defPlayers);
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		for (IWorldMarch iWorldMarch : helpMarchList) {
			defMarchs.add(iWorldMarch);
		}
		List<ArmyInfo> monsterArmys = this.getMarchEntity().getArmyCopy();
		// 战斗
		PveBattleIncome battleIncom = BattleService.getInstance().initYuriRevengeBattleData(BattleConst.BattleType.YURI_YURIREVENGE, targetPoint.getId(), monsterArmys, defPlayers,
				defMarchs);

		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncom);
		// 战后移除尤里复仇行军
		WorldFoggyFortressService.getInstance().removeCurrentMonsterMarch(getGuildId(), getMarchId());

		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();

		// 防守战斗结算
		int totalMonster = monsterArmys.stream().mapToInt(e -> e.getTotalCount()).sum();
		boolean defWin = calcResult(battleIncom, battleOutcome, totalMonster, targetPoint);

		// 记录战斗日志
		BattleLogHelper battleLogHelper = new BattleLogHelper(battleIncom, battleOutcome, !defWin);
		battleLogHelper.logBattleFlow();
		
		// 防御者战后部队结算
		defPlayer.dealMsg(MsgId.ARMY_BACK, new PlayerArmyBackMsgInvoker(targetPoint.getPointType(), defPlayer, battleOutcome, battleIncom, null, false, isMassMarch(), this.getMarchType()));
		// 更新援助防御玩家行军的部队
		updateDefMarchAfterWar(new ArrayList<>(helpMarchList), defArmyLeftMap);
		// 防守方失败 , 城墙着火
		if (!defWin) {
			// 投递回玩家线程执行
			defPlayer.dealMsg(MsgId.CITY_ON_FIRE, new CityOnFireMsgInvoker(player, defPlayer, Collections.emptyList()));
		}
		// 从联盟战争界面移除
		WorldMarchService.getInstance().rmGuildMarch(this.getMarchId());
		
		// 刷新战力
		refreshPowerAfterWar(null, defPlayers);

	}

	/**
	 * 
	 * @param armyMap 战后
	 * @param totalMonster 怪物总数
	 * @param defPlayer
	 * @param point
	 * @return
	 */
	private boolean calcResult(PveBattleIncome battleIncome ,BattleOutcome battleOutcome, int totalMonster, WorldPoint point) {
		YuriRevengeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YuriRevengeCfg.class, round);
		List<Player> defPlayers = battleIncome.getDefPlayers();
		Player defLeader = defPlayers.get(0);
		long guildScore = cfg.getAllianceIntegral();
		long personScore = cfg.getPersonIntegral();
		int totalKill = 0;
		// 参与防御玩家数据汇总
		Map<String, Long> personScoreMap = new HashMap<>();
		Map<String, List<ArmyInfo>> battleArmyLeftMap = battleOutcome.getBattleArmyMapDef();
		// 击杀数据汇总
		for (Entry<String, List<ArmyInfo>> entry : battleArmyLeftMap.entrySet()) {
			String playerId = entry.getKey();
			List<ArmyInfo> armyList = entry.getValue();
			int killCnt = armyList.stream().mapToInt(e -> e.getKillCount()).sum();
			totalKill += killCnt;
			int killRate = (int) (1l * killCnt * GsConst.RANDOM_MYRIABIT_BASE / totalMonster);
			long selfScore = personScore * killRate / GsConst.RANDOM_MYRIABIT_BASE;
			personScoreMap.put(playerId, selfScore);
		}

		int totalKillRate = (int) (1l * totalKill * GsConst.RANDOM_MYRIABIT_BASE / totalMonster);
		guildScore = guildScore * totalKillRate / GsConst.RANDOM_MYRIABIT_BASE;
		boolean isWin = totalKillRate > WorldMapConstProperty.getInstance().getYuriSucceedRate();
		int loseTimes = YuriRevengeService.getInstance().getLoseCnt(defLeader.getId());
		MailId mailId;
		MailId assMailId;
		if (isWin) {
			mailId = MailId.YURI_REVENGE_FIGHT_WIN;
			assMailId = MailId.YURI_REVENGE_ASSIST_FIGHT_WIN;
		} else {
			YuriRevengeService.getInstance().addLoseCnt(defLeader.getId());
			mailId = MailId.YURI_REVENGE_FIGHT_FAILED;
			assMailId = MailId.YURI_REVENGE_ASSIST_FIGHT_FAILED;
			loseTimes += 1;
			// 失败n次 结束战斗
			if (loseTimes >= WorldMapConstProperty.getInstance().getYuriFailTimes()) {
				WorldFoggyFortressService.getInstance().removePlayerMonsterMarch(guildId, defLeader.getId(), true);
			}
		}
		// 添加尤里复仇积分
		YuriRevengeService.getInstance().onAddScore(guildId, guildScore, personScoreMap);
		// 尤里复仇战报
		YuriRevengeFightMail.Builder mailInfo = YuriRevengeFightMail.newBuilder();
		mailInfo.setTurn(round);
		mailInfo.setPosX(point.getX());
		mailInfo.setPosY(point.getY());
		mailInfo.setEnemyCnt(totalMonster);
		mailInfo.setEnemyDeadCnt(totalKill);
		mailInfo.setFailedTimes(loseTimes);
		MailPlayerInfo.Builder leaderInfo = MailBuilderUtil.createMailPlayerInfo(defLeader);
		for (int i = 0; i < defPlayers.size(); i++) {
			YuriRevengeFightMail.Builder mailInfoCopy = mailInfo.clone();
			Player defPlayer = defPlayers.get(i);
			// 玩家战斗数据统计
			int totalCnt = 0;
			int killCnt = 0;
			int woundCnt = 0;
			double disPoint = 0;
			List<ArmyInfo> selfArmy = battleArmyLeftMap.get(defPlayer.getId());
			if (selfArmy != null) {
				for (ArmyInfo army : selfArmy) {
					BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
					totalCnt += army.getTotalCount();
					killCnt += army.getKillCount();
					woundCnt += army.getWoundedCount();
					disPoint += army.getWoundedCount() * soldierCfg.getPower();
				}
			}
			int killRate = (int) (1l * killCnt * GsConst.RANDOM_MYRIABIT_BASE / totalMonster);
			// 避免因特殊英雄/作用号之类的导致杀伤数大于实际数
			killRate = Math.min(killRate, GsConst.RANDOM_MYRIABIT_BASE);
			long selfScore = personScore * killRate / GsConst.RANDOM_MYRIABIT_BASE;

			mailInfoCopy.setKillRate((int) totalKillRate);
			mailInfoCopy.setDisPoint((int) Math.ceil(disPoint));
			mailInfoCopy.setTotalCnt(totalCnt);
			mailInfoCopy.setWoundCnt(woundCnt);
			mailInfoCopy.setScore((int) selfScore);
			if(i == 0){
				FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(defLeader.getId())
						.addContents(mailInfoCopy).setMailId(mailId).addTips(cfg.getId()).build());
			}else{
				mailInfoCopy.setSelfInfo(MailBuilderUtil.createMailPlayerInfo(defPlayer));
				mailInfoCopy.setTargetInfo(leaderInfo);
				FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(defPlayer.getId())
						.addContents(mailInfoCopy).setMailId(assMailId).addTips(cfg.getId()).build());
			}
		}
		return isWin;
	}

	@Override
	public long getMarchNeedTime() {
		// 行军速度
		
		double speed = getMarchBaseSpeed();
		// 行军距离
		double distance = WorldUtil.distance(getMarchEntity().getOrigionX(), getMarchEntity().getOrigionY(), getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY());
		// 行军距离修正参数
		double param1 = WorldMarchConstProperty.getInstance().getDistanceAdjustParam();
		double param2 = WorldMarchConstProperty.getInstance().getYuriMarchCoefficient();
		double time = (Math.pow((distance), param1) * param2) / speed;
		time = time > 1 ? time : 1;
		return (long) (time * 1000L);
	}

	/**
	 * 检查PVP战斗是否满足前置条件，不满足则发邮件通知，现在普通攻击大本，集结攻击大本
	 * 
	 * @param march
	 * @param tarPoint
	 * @return
	 */
	public boolean checkBeforeWar(WorldPoint tarPoint, Player defPlayer) {
		// 行军信息
		WorldMarch march = getMarchEntity();
		int x = march.getTerminalX();
		int y = march.getTerminalY();
		String targetId = march.getTargetId();

		// 找不到目标: 攻击方发送邮件 -> 攻击玩家基地失败，被攻击方高迁或被打飞
		if (defPlayer == null || tarPoint == null) {
			WorldMarchService.logger.info("attack player march check, point null, x:{}, y{}, targetId:{}", x, y, targetId);
			return false;
		}

		// 目标非玩家，或者目标已换人，或者目标已经不属于这个联盟
		if (tarPoint.getPointType() != WorldPointType.PLAYER_VALUE
				|| !tarPoint.getPlayerId().equals(targetId)
				|| !guildId.equals(defPlayer.getGuildId())) {

			WorldMarchService.logger.info("attack player march check, point changed, x:{}, y{}, targetId:{}", x, y, targetId);
			return false;
		}
		return true;
	}

	/**
	 * 获取主动方联盟战争界面信息
	 * @return
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarInitiativeInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		int origionId = this.getMarchEntity().getOrigionId();
		int pos[] = GameUtil.splitXAndY(origionId);
		builder.setPointType(WorldPointType.YURI_FACTORY);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setYuriTimes(this.getRound());
		return builder;
	}

	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		// 队长id
		String leaderId = this.getMarchEntity().getTargetId();
		// 队长位置
		int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(leaderId);
		// 队长
		Player player = GlobalData.getInstance().makesurePlayer(leaderId);
		builder.setPointType(WorldPointType.PLAYER);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setLeaderArmyLimit(player.getMaxAssistSoldier());
		builder.setYuriTimes(this.getRound());
		builder.setGridCount(player.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(player.getGuildId())) {
			String guildTag = GuildService.getInstance().getGuildTag(player.getGuildId());
			builder.setGuildTag(guildTag);
		}

		// 队长信息
		GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
		leaderInfo.setPlayerId(player.getId());
		leaderInfo.setPlayerName(player.getName());
		leaderInfo.setIconId(player.getIcon());
		leaderInfo.setPfIcon(player.getPfIcon());
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING);
		List<ArmyInfo> armys = ArmyService.getInstance().getFreeArmyList(player);
		for (ArmyInfo army : armys) {
			leaderInfo.addArmys(army.toArmySoldierPB(player));
		}
		builder.setLeaderMarch(leaderInfo);
		
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(armys);
		
		Set<IWorldMarch> assistandMarchs = WorldMarchService.getInstance().getPlayerPassiveMarchs(leaderId, WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		for (IWorldMarch assistandMarch : assistandMarchs) {
			builder.addJoinMarchs(getGuildWarSingleInfo(assistandMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(assistandMarch.getMarchEntity().getArmys());
		}
		if (!HawkOSOperator.isEmptyString(player.getGuildId())) {
			String guildTag = GuildService.getInstance().getGuildTag(player.getGuildId());
			builder.setGuildTag(guildTag);
		}
		builder.setCityLevel(player.getCityLv());
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
		return ReportRecipients.TargetAndHisAssistance.attackReportRecipients(this);
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	public boolean isLastRound() {
		return lastRound;
	}

	public void setLastRound(boolean lastRound) {
		this.lastRound = lastRound;
	}

	public long getNextPushTime() {
		return nextPushTime;
	}

	public void setNextPushTime(long nextPushTime) {
		this.nextPushTime = nextPushTime;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}
}
