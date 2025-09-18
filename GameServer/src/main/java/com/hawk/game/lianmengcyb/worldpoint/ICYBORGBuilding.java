package com.hawk.game.lianmengcyb.worldpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple4;
import org.hawk.tuple.HawkTuples;

import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.CYBORGBuildTreeCfg;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGRoomManager.CYBORG_CAMP;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.HPListMailResp;
import com.hawk.game.protocol.Mail.HPTypeMail;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterMarch;
import com.hawk.game.protocol.World.CYBORGHoldRec;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 
 *
 */
public abstract class ICYBORGBuilding implements ICYBORGWorldPoint {
	private final CYBORGBattleRoom parent;
	private CYBORGBuildState state = CYBORGBuildState.ZHONG_LI;
	/** 占领开始 */
	private long zhanLingKaiShi;
	private long zhanLingJieShu;
	private int index; // 刷的序号
	private int x;
	private int y;
	private boolean firstControl = true;
	private long lastTick;
	private ICYBORGWorldMarch leaderMarch;
	private List<CYBORGHoldRec> holdRecList = new ArrayList<>();
	private List<String> defMailList = new LinkedList<>();
	private List<String> spyMailList = new LinkedList<>();

	private HawkTuple4<String, String, Long, Long> controlLog; // guildId, guildName 取得控制 , 占领

	/** 联盟控制时间 */
	private AtomicLongMap<String> controlGuildTimeMap = AtomicLongMap.create();
	/** 联盟控制取得积分 */
	private Map<String, Double> controlGuildHonorMap = new HashMap<>();

	private CYBORGBuildTreeCfg treeCfg;
	private EnumSet<CYBORG_CAMP> canAttackTamp;
	private boolean leaderMarchChanged;

	public ICYBORGBuilding(CYBORGBattleRoom parent) {
		this.parent = parent;
	}

	public boolean underGuildControl(String guildId) {
		boolean b = getState() == CYBORGBuildState.ZHAN_LING || isRoot();
		return b && Objects.equals(this.getGuildId(), guildId);
	}

	@Override
	public boolean onTick() {
		long timePass = getParent().getCurTimeMil() - lastTick;
		if (timePass < 1000) {
			return true;
		}

		if (this.isRoot()) {
			state = CYBORGBuildState.ZHAN_LING;
			return true;
		}
		if(Objects.nonNull(controlLog) && state == CYBORGBuildState.ZHONG_LI){
			LogUtil.logCYBORGBuildControl(getParent().getId(), controlLog.first, controlLog.second, controlLog.third, controlLog.fourth, getParent().getCurTimeMil());
			controlLog = null;
		}

		lastTick = getParent().getCurTimeMil();

		getLeaderMarch();
		if (leaderMarchChanged) {
			getParent().worldPointUpdate(this);
			leaderMarchChanged = false;
		}
		
		List<ICYBORGWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		if (stayMarches.isEmpty()) {
			if (state != CYBORGBuildState.ZHONG_LI) {
				state = CYBORGBuildState.ZHONG_LI;
			}
			return true;
		}

		if (state != CYBORGBuildState.ZHONG_LI && !Objects.equals(getGuildId(), leaderMarch.getParent().getGuildId())) {
			state = CYBORGBuildState.ZHONG_LI;
			getParent().worldPointUpdate(this);
			return true;
		}

		// 有行军
		if (state == CYBORGBuildState.ZHAN_LING) {
			controlGuildTimeMap.addAndGet(getGuildId(), timePass); // 联盟占领时间++
			if (canBeAttack(leaderMarch.getParent().getCamp())) {
				controlGuildHonorMap.merge(getGuildId(), getGuildHonorPerSecond(), (v1, v2) -> v1 + v2);
				for (ICYBORGWorldMarch march : stayMarches) {
					// 个人积分++ pers
					double pers = Math.min(getPlayerHonorPerSecond() * 1D / getCollectArmyMin() * march.getMarchEntity().getArmyCount(), getPlayerHonorPerSecond());
					march.getParent().incrementPlayerHonor(pers);
				}
			}
			return true;
		}

		if (state == CYBORGBuildState.ZHONG_LI) {
			state = CYBORGBuildState.ZHAN_LING_ZHONG;
			zhanLingKaiShi = getParent().getCurTimeMil();

			int controlCountDown = getControlCountDown();
			for (CYBORGWeatherController w : getParent().getCYBORGBuildingByClass(CYBORGWeatherController.class)) {
				if (w.underGuildControl(getGuildId())) {
					controlCountDown = (int) (controlCountDown * 0.01 * CYBORGWeatherController.getCfg().getCoolDownReducePercentage());
				}
			}

			zhanLingJieShu = zhanLingKaiShi + controlCountDown * 1000;
			getParent().worldPointUpdate(this);

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_122)
					.addParms(getX())
					.addParms(getY())
					.addParms(leaderMarch.getParent().getCamp().intValue())
					.addParms(getGuildTag())
					.addParms(getPlayerName())
					.build();
			getParent().addWorldBroadcastMsg(parames);
			
			controlLog = HawkTuples.tuple(getGuildId(),getGuildTag(), zhanLingKaiShi, 0L);
			return true;
		}

		if (state == CYBORGBuildState.ZHAN_LING_ZHONG && getParent().getCurTimeMil() > zhanLingJieShu) {
			state = CYBORGBuildState.ZHAN_LING;
			if (firstControl) {// 首控
				firstControl = false;
				controlGuildHonorMap.put(getGuildId(), getFirstControlGuildHonor());
				for (ICYBORGWorldMarch march : stayMarches) {
					// 个人积分++
					march.getParent().incrementPlayerHonor(getFirstControlPlayerHonor());
				}
			}

			CYBORGHoldRec hrec = CYBORGHoldRec.newBuilder().setHoldTime(zhanLingJieShu).setPlayerName(getPlayerName()).setGuildTag(getGuildTag()).setPtype(getPointType())
					.setX(x)
					.setY(y)
					.setFlagView(stayMarches.get(0).getParent().getCamp().intValue()).build();
			holdRecList.add(hrec);
			getParent().worldPointUpdate(this);

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_BUILD_CONTROL)
					.addParms(getX())
					.addParms(getY())
					.addParms(leaderMarch.getParent().getCamp().intValue())
					.addParms(getGuildTag())
					.addParms(getPlayerName())
					.build();
			getParent().addWorldBroadcastMsg(parames);
			
			controlLog = HawkTuples.tuple(getGuildId(), getGuildTag(), zhanLingKaiShi, zhanLingJieShu);
			return true;
		}

		return true;
	}

	public boolean checkAttackCampChanged() {
		EnumSet<CYBORG_CAMP> tamp = EnumSet.noneOf(CYBORG_CAMP.class);
		for (CYBORG_CAMP camp : CYBORG_CAMP.values()) {
			// if(canBeAttack(camp)!=canBeAttack2(camp)){
			// System.out.println("@###################");
			// }

			if (checkCanBeAttack2(camp)) {
				tamp.add(camp);
			}
		}

		if (canAttackTamp == null) {
			canAttackTamp = tamp;
			return true;
		}

		if (canAttackTamp.size() == tamp.size() && canAttackTamp.containsAll(tamp)) {
			return false;
		}

		canAttackTamp = tamp;
		return true;
	}

	public CYBORGBuildTreeCfg getTreeCfg() {
		if (Objects.isNull(treeCfg)) {
			treeCfg = HawkConfigManager.getInstance().getConfigByKey(CYBORGBuildTreeCfg.class, x + "_" + y);
		}
		return treeCfg;
	}
	
	/** 出生归属*/
	public CYBORG_CAMP bornCamp(){
		CYBORGBuildTreeCfg treeCfg = getTreeCfg();
		if (Objects.isNull(treeCfg)) {
			return null;
		}
		CYBORG_CAMP camp = CYBORG_CAMP.valueOf(treeCfg.getCamp());
		return camp;
		
	}
	
	/** 出生归属*/
	public String bornGuild(){
		return getParent().getCampBase(bornCamp()).campGuild;
		
	}

	public boolean isRoot() {
		CYBORGBuildTreeCfg treeCfg = getTreeCfg();
		if (Objects.isNull(treeCfg)) {
			return false;
		}
		return treeCfg.getRoot() == 1;
	}

	public boolean isLinkBuild(ICYBORGBuilding other) {
		if (getTreeCfg() == null || other.getTreeCfg() == null) {
			return false;
		}
		return getTreeCfg().isLinkedNode(other.getTreeCfg().getId());
	}

	public boolean canBeAttack(CYBORG_CAMP camp) {
		return canAttackTamp.contains(camp);
	}

	private boolean checkCanBeAttack2(CYBORG_CAMP camp) {
		if (this.isRoot()) {
			return false;
		}
		String campGuild = getParent().getCampBase(camp).campGuild;
		Set<ICYBORGBuilding> controlGuild = getParent().getCYBORGBuildingList().stream().filter(build -> build.underGuildControl(campGuild)).collect(Collectors.toSet());

		List<ICYBORGBuilding> allLinkBuild = new ArrayList<>();
		allLinkBuild.add(this);
		controlGuild.remove(this);
		while (true) {
			List<ICYBORGBuilding> canLinkBuild = new ArrayList<>();
			for (ICYBORGBuilding cbuild : controlGuild) {

				for (ICYBORGBuilding canLink : allLinkBuild) {
					if (canLink.isLinkBuild(cbuild)) {
						if (cbuild.isRoot()) {
							return true;
						}
						canLinkBuild.add(cbuild);
						break;
					}
				}
			}
			if (canLinkBuild.isEmpty()) {
				return false;
			}
			controlGuild.removeAll(canLinkBuild);
			allLinkBuild.addAll(canLinkBuild);
		}
	}

	private boolean checkCanBeAttack(CYBORG_CAMP camp) {
		if (this.isRoot()) {
			return false;
		}
		String campGuild = getParent().getCampBase(camp).campGuild;
		Set<ICYBORGBuilding> controlGuild = getParent().getCYBORGBuildingList().stream().filter(build -> build.underGuildControl(campGuild)).collect(Collectors.toSet());
		Set<ICYBORGBuilding> linkNode = new HashSet<>();
		linkNode.add(this);
		Set<ICYBORGBuilding> checkedNode = new HashSet<>();
		while (!linkNode.isEmpty()) {
			HashSet<ICYBORGBuilding> tocheckNode = new HashSet<>(linkNode);
			linkNode.clear();
			for (ICYBORGBuilding node : tocheckNode) {
				for (ICYBORGBuilding build : controlGuild) {
					if (checkedNode.contains(build)) {
						continue;
					}
					if (tocheckNode.contains(build)) {
						continue;
					}
					if (build.isLinkBuild(node)) {
						if (build.isRoot()) {
							return true;
						}
						linkNode.add(build);
					}
				}
				checkedNode.add(node);
			}
		}

		return false;

	}

	/** 占领倒计时 /秒 */
	public abstract int getControlCountDown();

	public abstract double getGuildHonorPerSecond();

	public abstract double getPlayerHonorPerSecond();

	public abstract double getFirstControlGuildHonor();

	public abstract double getFirstControlPlayerHonor();

	public abstract int getProtectTime();

	public abstract int getCollectArmyMin();

	public void onPlayerLogin(ICYBORGPlayer gamer) {
	}

	public void anchorJoin(ICYBORGPlayer gamer) {
	}

	@Override
	public WorldPointPB.Builder toBuilder(ICYBORGPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setPlayerId(getPlayerId());
		builder.setPlayerName(getPlayerName());
		builder.setGuildId(getGuildId());
		builder.setManorState(state.intValue()); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
		List<ICYBORGWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ICYBORGWorldMarch march : stayMarches) {
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 ,2 蓝
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				break;
			}
		}
		if (leaderMarch != null) {
			BuilderUtil.buildMarchEmotion(builder, leaderMarch.getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}

		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());
		if (state != CYBORGBuildState.ZHONG_LI) {
			builder.setManorBuildTime(zhanLingKaiShi); // 占领开始时间
			builder.setManorComTime(zhanLingJieShu); // 占领结束时间
		}

		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		if (!Objects.equals(viewer.getGuildId(), getGuildId())) {// 核弹攻击
			for (CYBORGNuclearMissileSilo nuc : getParent().getNuclearBuildList()) {
				if (Objects.equals(viewer.getGuildId(), nuc.getNuclearReadyGuild())) {
					builder.setCounterAttack(true);
					break;
				}
			}
		}
		canAttackTamp.forEach(c -> builder.addCyborgCanMarchCamp(c.intValue()));

		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ICYBORGPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setGuildId(getGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());
		if (leaderMarch != null) {
			ICYBORGPlayer leader = leaderMarch.getParent();
			builder.setPlayerId(leader.getId());
			builder.setPlayerName(leader.getName());
			builder.setPlayerIcon(leader.getIcon());
			builder.setPlayerPfIcon(leader.getPfIcon());
			BuilderUtil.buildMarchEmotion(builder, leaderMarch.getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}
		builder.setManorState(state.intValue());// /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
		List<ICYBORGWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ICYBORGWorldMarch march : stayMarches) {
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 2 蓝
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				break;
			}
		}
		if (state != CYBORGBuildState.ZHONG_LI) {
			builder.setManorBuildTime(zhanLingKaiShi);// 占领开始时间
			builder.setManorComTime(zhanLingJieShu);// 占领结束时间
		}
		builder.addAllSyborgHoldRec(holdRecList);

		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		if (!Objects.equals(viewer.getGuildId(), getGuildId())) {// 核弹攻击
			for (CYBORGNuclearMissileSilo nuc : getParent().getNuclearBuildList()) {
				if (Objects.equals(viewer.getGuildId(), nuc.getNuclearReadyGuild())) {
					builder.setCounterAttack(true);
					break;
				}
			}
		}
		canAttackTamp.forEach(c -> builder.addCyborgCanMarchCamp(c.intValue()));
		return builder;
	}

	@Override
	public long getProtectedEndTime() {
		return getParent().getCreateTime() + getProtectTime() * 1000;
	}

	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		builder.setPointType(this.getPointType());
		builder.setX(this.getX());
		builder.setY(this.getY());
		// 队长id
		if (getLeaderMarch() == null) {
			return builder;
		}
		ICYBORGPlayer leader = getLeaderMarch().getParent();
		String leaderId = leader.getId();
		// 队长
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = leader.getGuildTag();
			builder.setGuildTag(guildTag);
		}

		// 已经到达的士兵数量
		int reachArmyCount = 0;
		List<ICYBORGWorldMarch> assistandMarchs = getParent().getPointMarches(this.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ICYBORGWorldMarch stayMarch : assistandMarchs) {
			if (Objects.equals(stayMarch.getPlayerId(), leaderId)) {
				// 队长信息
				builder.setLeaderArmyLimit(stayMarch.getMaxMassJoinSoldierNum(leader));
				builder.setLeaderMarch(stayMarch.getGuildWarSingleInfo());
				continue;
			}
			builder.addJoinMarchs(stayMarch.getGuildWarSingleInfo());
			reachArmyCount += WorldUtil.calcSoldierCnt(stayMarch.getMarchEntity().getArmys());
		}
		builder.setCityLevel(leader.getCityLv());
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}

	@Override
	public void onMarchReach(ICYBORGWorldMarch leaderMarch) {
		ICYBORGPlayer player = leaderMarch.getParent();
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<ICYBORGWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (ICYBORGWorldMarch iWorldMarch : atkMarchList) {
			// 去程到达目标点，变成停留状态
			iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE);
			iWorldMarch.getMarchEntity().setReachTime(leaderMarch.getMarchEntity().getEndTime());
			iWorldMarch.updateMarch();
			atkMarchs.add(iWorldMarch);
			atkPlayers.add(iWorldMarch.getParent());
		}

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		List<IWorldMarch> defMarchs = new ArrayList<>();
		// 防守方行军
		List<ICYBORGWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		ICYBORGWorldMarch enemyLeadmarch = getLeaderMarch();
		if (Objects.nonNull(enemyLeadmarch)) {// 队长排第一
			defMarchList.remove(enemyLeadmarch);
			defMarchList.add(0, enemyLeadmarch);
		}
		for (ICYBORGWorldMarch iWorldMarch : defMarchList) {
			defMarchs.add(iWorldMarch);
			defPlayers.add(iWorldMarch.getParent());
		}

		if (defMarchs.isEmpty()) {
			state = CYBORGBuildState.ZHONG_LI;
			this.leaderMarch = leaderMarch;
			for (ICYBORGWorldMarch iWorldMarch : atkMarchList) {
				iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				iWorldMarch.updateMarch();
			}
			return;
		}

		if (Objects.equals(player.getGuildId(), defPlayers.get(0).getGuildId())) { // 同阵营
			try {
				assitenceWarPoint(atkMarchList, defMarchList);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			// for (ICYBORGWorldMarch iWorldMarch : atkMarchList) {
			// iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			// iWorldMarch.updateMarch();
			// }
			return;
		}

		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_TBLY_BUILD, this.getPointId(), atkPlayers, defPlayers,
				atkMarchs,
				defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setCYBORGMail(getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.CYBORG);
		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();
		/********* 击杀/击伤部队数据 *********/
		getParent().calcKillAndHurtPower(battleOutcome, atkPlayers, defPlayers);
		/********* 击杀/击伤部队数据 *********/

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		// 计算损失兵力
		List<ArmyInfo> atkArmyLeft = WorldUtil.mergAllPlayerArmy(atkArmyLeftMap);
		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		// 计算损失兵力
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);

		// 发送战斗结果给前台播放动画
		leaderMarch.sendBattleResultInfo(isAtkWin, atkArmyLeft, defArmyList);

		for (ICYBORGWorldMarch iWorldMarch : atkMarchList) {
			iWorldMarch.notifyMarchEvent(MarchEvent.MARCH_DELETE);
		}

		leaderMarch.updateDefMarchAfterWar(atkMarchList, atkArmyLeftMap);
		leaderMarch.updateDefMarchAfterWar(defMarchList, defArmyLeftMap);

		List<ICYBORGWorldMarch> winMarches = null;
		List<ICYBORGWorldMarch> losMarches = null;

		if (isAtkWin) {
			state = CYBORGBuildState.ZHONG_LI;
			this.leaderMarch = leaderMarch;
			winMarches = atkMarchList;
			losMarches = defMarchList;
		} else {
			winMarches = defMarchList;
			losMarches = atkMarchList;
		}

		for (ICYBORGWorldMarch atkM : winMarches) {
			atkM.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			atkM.updateMarch();
		}
		for (ICYBORGWorldMarch defM : losMarches) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}

		FightMailService.getInstance().sendFightMail(this.getPointType().getNumber(), battleIncome, battleOutcome, null);

		defMailList.add(0, battleOutcome.getDefMail(defPlayers.get(0).getId()));
	}

	public void listMail(ICYBORGPlayer player, int type) {
		HPListMailResp.Builder resp = HPListMailResp.newBuilder();
		List<String> mailIds;
		if (type == 0) {
			mailIds = defMailList.stream().limit(30).collect(Collectors.toList());
		} else {
			mailIds = spyMailList.stream().limit(30).collect(Collectors.toList());
		}
		List<MailLiteInfo.Builder> list = MailService.getInstance().listMailEntity(mailIds);
		HPTypeMail.Builder bul = HPTypeMail.newBuilder().setType(5).setHasNext(false);
		for (MailLiteInfo.Builder mail : list) {
			mail.setStatus(MailStatus.READ_VALUE);
			bul.addMail(mail);
		}

		resp.addList(bul);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_ANCHOR_LIST_MAIL_S, resp));
	}

	

	private boolean assitenceWarPoint(List<ICYBORGWorldMarch> atkMarchList, List<ICYBORGWorldMarch> stayMarchList) {
		// 队长
		ICYBORGPlayer leader = leaderMarch.getParent();

		// 8.21新增：检查当前自己的行军，如果行军中已有英雄，则将自己队列中的英雄撤回
		for (ICYBORGWorldMarch worldMarch : atkMarchList) {
			if (worldMarch.getMarchEntity().getHeroIdList().isEmpty()) {// 如果当前行军中没有英雄直接跳过
				continue;
			}
			// 如果有英雄，则判断当前据点停留的行军有没有自己的
			for (ICYBORGWorldMarch stayMarch : stayMarchList) {
				try {
					if (stayMarch.getPlayerId().equals(worldMarch.getPlayerId())) {
						List<Integer> heroId = worldMarch.getMarchEntity().getHeroIdList();
						if (stayMarch.getMarchEntity().getHeroIdList().size() > 0) {
							// 如果停留的行军有自己的并且已经带有英雄，将当前的英雄瞬间返回
							List<PlayerHero> OpHero = stayMarch.getParent().getHeroByCfgId(heroId);
							for (PlayerHero hero : OpHero) {
								hero.backFromMarch(stayMarch);
							}
							worldMarch.getMarchEntity().setHeroIdList(Collections.emptyList());
							Optional<SuperSoldier> marchSsOp = stayMarch.getParent().getSuperSoldierByCfgId(worldMarch.getSuperSoldierId());
							if (marchSsOp.isPresent()) {
								marchSsOp.get().backFromMarch(stayMarch);
								worldMarch.getMarchEntity().setSuperSoldierId(0);
							}
						} else if (heroId.size() > 0) {
							// 停留行军没有英雄则直接加入
							stayMarch.getMarchEntity().setHeroIdList(heroId);
							worldMarch.getMarchEntity().setHeroIdList(Collections.emptyList());
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}

		// 先处理过来行军中的队长行军，队长行军处理完毕后删除
//		if (stayMarchList != null && !stayMarchList.isEmpty()) {
//			int count = stayMarchList.size();// 加上队长
//			if (count > leader.getMaxMassJoinMarchNum() + leaderMarch.getMarchEntity().getBuyItemTimes()) {
//				return returnMarchList(atkMarchList);
//			}
//		}

		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);

		List<WorldMarch> ppList = new ArrayList<>();
		for (ICYBORGWorldMarch worldMarch : stayMarchList) {
			ppList.add(worldMarch.getMarchEntity());
		}

		int curPopulationCnt = WorldUtil.calcMarchsSoldierCnt(ppList); // 已驻扎士兵人口
		// 剩余人口<0部队返回
		int remainArmyPopu = maxMassSoldierNum - curPopulationCnt;
		if (remainArmyPopu <= 0) {
			return returnMarchList(atkMarchList);
		}

		// 优先加入已在玩家
		for (ICYBORGWorldMarch stayMarch : stayMarchList) {
			Iterator<ICYBORGWorldMarch> it = atkMarchList.iterator();
			while (it.hasNext()) {
				ICYBORGWorldMarch massMarch = it.next();
				if (!stayMarch.getPlayerId().equals(massMarch.getPlayerId())) {
					continue;
				}

				List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
				List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
				int stayCnt = WorldUtil.calcStayArmy(massMarch.getMarchEntity(), remainArmyPopu, stayList, backList);

				// 回家的士兵生成新行军
				if (backList.size() > 0) {
					EffectParams effParams = new EffectParams();
					effParams.setArmys(backList);
					ICYBORGWorldMarch back = getParent().startMarch(massMarch.getParent(), this, massMarch.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
					back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
					back.updateMarch();
				}
				massMarch.remove();

				List<List<ArmyInfo>> lists = new ArrayList<List<ArmyInfo>>();
				lists.add(stayMarch.getMarchEntity().getArmys());
				lists.add(stayList);
				stayMarch.getMarchEntity().setArmys(WorldUtil.mergMultArmyList(lists));

				remainArmyPopu -= stayCnt;
				it.remove();
				break;
			}
		}

		// 一轮援助检查后是否所有行军已处理
		if (atkMarchList.isEmpty()) {
			return true;
		}

		// 加入要留驻的行军
		Iterator<ICYBORGWorldMarch> it = atkMarchList.iterator();

		while (it.hasNext()) {
			ICYBORGWorldMarch march = it.next();
			int eff1546 = march.getPlayer().getEffect().getEffVal(EffType.HERO_1546, march.getMarchEntity().getEffectParams());
			if (remainArmyPopu + eff1546 <= 0) {
				continue;
			}
			remainArmyPopu += eff1546;
			List<ArmyInfo> stayArmyList = new ArrayList<ArmyInfo>();
			List<ArmyInfo> backArmyList = new ArrayList<ArmyInfo>();
			int stayCnt = WorldUtil.calcStayArmy(march.getMarchEntity(), remainArmyPopu, stayArmyList, backArmyList);
			// 回家的士兵生成新行军
			if (backArmyList.size() > 0) {
				EffectParams effParams = new EffectParams();
				effParams.setArmys(backArmyList);
				ICYBORGWorldMarch back = getParent().startMarch(march.getParent(), this, march.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
				back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
				back.updateMarch();
			}
			march.getMarchEntity().setArmys(stayArmyList);
			march.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			march.updateMarch();
			remainArmyPopu -= stayCnt;
			it.remove();
		}

		// 有多余的行军就原路返回&&通知行军提示变更
		if (atkMarchList.size() > 0) {
			returnMarchList(atkMarchList);
		}
		return true;
	}

	private boolean returnMarchList(List<ICYBORGWorldMarch> atkMarchList) {
		for (ICYBORGWorldMarch defM : atkMarchList) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}
		return true;
	}

	/** 遣返 */
	public boolean repatriateMarch(ICYBORGPlayer comdPlayer, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!Objects.equals(getGuildId(), comdPlayer.getGuildId())) {
			return false;
		}

		// 队长
		ICYBORGPlayer leader = leaderMarch.getParent();

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<ICYBORGWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ICYBORGWorldMarch iWorldMarch : stayMarches) {
			if (iWorldMarch.isReturnBackMarch()) {
				continue;
			}
			if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
				continue;
			}
			iWorldMarch.onMarchReturn(this.getPointId(), iWorldMarch.getParent().getPointId(), iWorldMarch.getArmys());
		}
		return true;
	}

	/**
	 * 任命队长
	 * 
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public boolean cheangeQuarterLeader(ICYBORGPlayer comdPlayer, String targetPlayerId) {

		// 不是本盟的没有权限操作
		if (!comdPlayer.getGuildId().equals(getGuildId())) {
			return false;
		}

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leaderMarch.getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<ICYBORGWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ICYBORGWorldMarch march : stayMarches) {
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			leaderMarch = march;
			getParent().worldPointUpdate(this);
			break;
		}
		return true;
	}

	public void syncQuarterInfo(ICYBORGPlayer player) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		if (!player.isAnchor() && !Objects.equals(player.getGuildId(), getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
			return;
		}

		List<ICYBORGWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		ICYBORGWorldMarch leaderMarch = getLeaderMarch();
		if (leaderMarch != null) {
			builder.addQuarterMarch(getSuperWeaponQuarterMarch(leaderMarch));
			builder.setMassSoldierNum(leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getParent()));
		}
		for (ICYBORGWorldMarch march : defMarchList) {
			if (march != leaderMarch) {
				builder.addQuarterMarch(getSuperWeaponQuarterMarch(march));
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
	}

	public SuperWeaponQuarterMarch.Builder getSuperWeaponQuarterMarch(ICYBORGWorldMarch march) {
		SuperWeaponQuarterMarch.Builder builder = SuperWeaponQuarterMarch.newBuilder();

		Player snapshot = march.getParent();

		builder.setPlayerId(snapshot.getId());
		builder.setName(snapshot.getName());
		builder.setIcon(snapshot.getIcon());
		builder.setPfIcon(snapshot.getPfIcon());
		builder.setGuildTag(snapshot.getGuildTag());
		builder.setMarchId(march.getMarchId());

		List<ArmyInfo> armys = march.getMarchEntity().getArmys();
		for (ArmyInfo army : armys) {
			if (army.getFreeCnt() <= 0) {
				continue;
			}
			builder.addArmy(army.toArmySoldierPB(snapshot).build());
		}
		for (PlayerHero hero : march.getHeros()) {
			builder.addHeroId(hero.getCfgId());
		}
		List<PlayerHero> heroList = snapshot.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
		for (PlayerHero hero : heroList) {
			builder.addHero(hero.toPBobj());
		}
		SuperSoldier ssoldier = snapshot.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId()).orElse(null);
		if (Objects.nonNull(ssoldier)) {
			builder.setSsoldier(ssoldier.toPBobj());
		}
		return builder;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getPointId() {
		return GameUtil.combineXAndY(x, y);
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeWorldPoint() {
		// TODO Auto-generated method stub

	}

	public CYBORGBuildState getState() {
		return state;
	}

	public void setState(CYBORGBuildState state) {
		this.state = state;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public CYBORGBattleRoom getParent() {
		return parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public CYBORG_CAMP getCamp() {
		if (this.isRoot()) {
			CYBORG_CAMP camp = CYBORG_CAMP.valueOf(getTreeCfg().getCamp());
			return getParent().getCampBase(camp).camp;
		}
		if (getLeaderMarch() == null) {
			return null;
		}
		return leaderMarch.getParent().getCamp();
	}

	@Override
	public String getGuildId() {
		if (this.isRoot()) {
			CYBORG_CAMP camp = CYBORG_CAMP.valueOf(getTreeCfg().getCamp());
			return getParent().getCampBase(camp).campGuild;
		}
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getGuildId();
	}

	public String getGuildTag() {
		if (this.isRoot()) {
			CYBORG_CAMP camp = CYBORG_CAMP.valueOf(getTreeCfg().getCamp());
			return getParent().getCampBase(camp).campGuildTag;
		}
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getGuildTag();
	}

	public String getPlayerId() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getId();
	}

	public String getPlayerName() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getName();
	}

	public long getZhanLingKaiShi() {
		return zhanLingKaiShi;
	}

	public void setZhanLingKaiShi(long zhanLingKaiShi) {
		this.zhanLingKaiShi = zhanLingKaiShi;
	}

	public long getZhanLingJieShu() {
		return zhanLingJieShu;
	}

	public void setZhanLingJieShu(long zhanLingJieShu) {
		this.zhanLingJieShu = zhanLingJieShu;
	}

	public long getLastTick() {
		return lastTick;
	}

	public void setLastTick(long lastTick) {
		this.lastTick = lastTick;
	}

	public List<CYBORGHoldRec> getHoldRecList() {
		return holdRecList;
	}

	public void setHoldRecList(List<CYBORGHoldRec> holdRecList) {
		this.holdRecList = holdRecList;
	}

	public int getGuildFlag() {
		if (this.isRoot()) {
			CYBORG_CAMP camp = CYBORG_CAMP.valueOf(getTreeCfg().getCamp());
			return getParent().getCampBase(camp).campguildFlag;
		}
		if (getLeaderMarch() == null) {
			return 0;
		}
		return leaderMarch.getParent().getGuildFlag();
	}

	public boolean isFirstControl() {
		return firstControl;
	}

	public void setFirstControl(boolean firstControl) {
		this.firstControl = firstControl;
	}

	public AtomicLongMap<String> getControlGuildTimeMap() {
		return controlGuildTimeMap;
	}

	public void setControlGuildTimeMap(AtomicLongMap<String> controlGuildTimeMap) {
		this.controlGuildTimeMap = controlGuildTimeMap;
	}

	public Map<String, Double> getControlGuildHonorMap() {
		return controlGuildHonorMap;
	}

	public ICYBORGWorldMarch getLeaderMarch() {
		if (leaderMarch == null || leaderMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			ICYBORGWorldMarch leaderMarchNew = null;
			List<ICYBORGWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			if (!stayMarches.isEmpty()) {
				leaderMarchNew = stayMarches.get(0);
			}
			if(leaderMarchNew != leaderMarch){
				leaderMarchChanged = true;
				leaderMarch = leaderMarchNew;
			}

		}
		if (leaderMarch == null && !isRoot()) {
			state = CYBORGBuildState.ZHONG_LI;
		}
		return leaderMarch;
	}

	public void setLeaderMarch(ICYBORGWorldMarch leaderMarch) {
		this.leaderMarch = leaderMarch;
	}

	public List<String> getSpyMailList() {
		return spyMailList;
	}

	public void setSpyMailList(List<String> spyMailList) {
		this.spyMailList = spyMailList;
	}

}
