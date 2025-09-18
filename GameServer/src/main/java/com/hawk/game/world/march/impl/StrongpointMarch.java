package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.OccupyStrongpointEvent;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.game.GsApp;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.WorldStrongpointCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.MarchVitReturnBackMsgInvoker;
import com.hawk.game.invoker.StrongpointAwardInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.AtkAfterPveMsg;
import com.hawk.game.msg.PlayerVitCostMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.StrongpointStatus;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.MailRewards;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventAttackPlayerStrongpoint;
import com.hawk.game.service.mssion.event.EventAttackStrongpoint;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldStrongPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 据点行军
 * 
 * @author golden
 *
 */
public class StrongpointMarch extends PlayerMarch implements BasedMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	private static Logger logger = LoggerFactory.getLogger("Server");
	
	public StrongpointMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.STRONGPOINT;
	}

	@Override
	public boolean marchHeartBeats(long currTime) {

		if (this.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return false;
		}

		if (this.getMarchEntity().getResEndTime() > HawkTime.getMillisecond()) {
			return false;
		}

		// 奖励结算
		WorldStrongPointService.getInstance().doStrongpointReturn(this, this.getMarchEntity().getArmys(), true);
		return true;
	}

	@Override
	public boolean isNeedCalcTickMarch() {
		return true;
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		removeAttackReport();
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
		this.pullAttackReport();
	}
	
	@Override
	public void onMarchReach(Player player) {
		marchReach(player);
		// 删除行军报告
		removeAttackReport();
		// 处发其他行军重推报告
		this.pullAttackReport();
	}

	@Override
	public boolean needShowInGuildWar() {
		int terminalId = this.getMarchEntity().getTerminalId();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		if (worldPoint == null) {
			return false;
		}
		
		String targerPlayerId = worldPoint.getPlayerId();
		if (HawkOSOperator.isEmptyString(targerPlayerId)) {
			return false;
		}
		
		return !GuildService.getInstance().isInTheSameGuild(this.getPlayerId(), targerPlayerId);
	}

	@Override
	public void detailMarchStop(WorldPoint point) {
		long currentTime = HawkTime.getMillisecond();
		this.getMarchEntity().setResStartTime(currentTime);
		this.getMarchEntity().setResEndTime(currentTime + point.getLastActiveTime());
		logger.info("strongpoint march, detailMarchStop, resStartTime:{}, resEndTime:{}", this.getMarchEntity().getResStartTime(), this.getMarchEntity().getResEndTime());
	}

	@Override
	public void onWorldMarchReturn(Player player) {
		doAwardCalc(player);
	}

	/**
	 * 行军召回
	 */
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		WorldStrongPointService.getInstance().doStrongpointReturn(this, this.getMarchEntity().getArmys(), false);
		logger.info("strongpoint march, onMarchCallback, marchId:{}", this.getMarchId());
	}
	
	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		// 队长位置
		int terminalId = this.getMarchEntity().getTerminalId();
		int[] pos = GameUtil.splitXAndY(terminalId);
		
		builder.setPointType(WorldPointType.STRONG_POINT);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		
		if (worldPoint != null && !HawkOSOperator.isEmptyString(worldPoint.getMarchId()) && worldPoint.getPointType() == WorldPointType.STRONG_POINT_VALUE) {
			// 队长
			String leaderId = worldPoint.getPlayerId();
			String marchId = worldPoint.getMarchId();
			Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
			
			if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
				String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());
				builder.setGuildTag(guildTag);
			}
	
			// 队长信息
			GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
			leaderInfo.setPlayerId(leader.getId());
			leaderInfo.setPlayerName(leader.getName());
			leaderInfo.setIconId(leader.getIcon());
			leaderInfo.setPfIcon(leader.getPfIcon());
			leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			for (ArmyInfo army : march.getMarchEntity().getArmys()) {
				leaderInfo.addArmys(army.toArmySoldierPB(leader));
			}
			builder.setLeaderMarch(leaderInfo);
			builder.setReachArmyCount(WorldUtil.calcSoldierCnt(this.getMarchEntity().getArmys()));
		}
		return builder;
	}
	
	@Override
	public void onMarchReturn() {
		BasedMarch.super.onMarchReturn();
		// 删除行军报告
		removeAttackReport();
		// 处发其他行军重推报告
		this.pullAttackReport();
	}

	@Override
	public Set<String> attackReportRecipients() {
		Set<String> result = alarmPointEnemyMarches().stream()
				.filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE)
				.map(IWorldMarch::getPlayerId)
				.filter(tid -> !Objects.equals(tid, this.getMarchEntity().getPlayerId()))
				.collect(Collectors.toSet());
		return result;
	}

	@Override
	public void pullAttackReport() {
		// 不是同联盟且不是回程的行军，才会处理
		for (IWorldMarch targetMarch : alarmPointEnemyMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport();
			}
		}
	}
	
	@Override
	public void pullAttackReport(String playerId) {
		// 不是同联盟且不是回程的行军，才会处理
		for (IWorldMarch targetMarch : alarmPointEnemyMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport(playerId);
			}
		}
	}
	
	@Override
	public void doQuitGuild(String guildId) {
		// 移除自己行军的联盟战争显示
		WorldMarchService.getInstance().rmGuildMarch(this.getMarchId());
		
		if (!this.isReachAndStopMarch()) {
			return;
		}
		
		// 删除向驻扎点行军的目标方联盟战争显示
		int pos[] = GameUtil.splitFromAndTo(this.getMarchEntity().getTerminalId());
		Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(pos[0], pos[1]);
		for (IWorldMarch worldPointMarch : worldPointMarchs) {
			// 更新行军信息(更新行军线颜色)
			worldPointMarch.updateMarch();
			// 删除联盟战争显示
			WorldMarchService.getInstance().rmGuildMarch(worldPointMarch.getMarchId(), guildId);
		}
	}
	
	/**
	 * 奖励结算
	 * 
	 * @param player
	 */
	public void doAwardCalc(Player player) {
		if (this.getMarchEntity().getResStartTime() <= 0 || this.getMarchEntity().getResEndTime() <= 0) {
			return;
		}
		
		if (WorldUtil.getFreeArmyCnt(this.getMarchEntity().getArmys()) <= 0) {
			return;
		}
		
		AwardItems awardItems = this.getMarchEntity().getAwardItems();
		boolean hasAwardItem = awardItems.hasAwardItem();
		MailId mailId = MailId.STRONG_AWARD_REPORT;
		
		// 发奖
		if (hasAwardItem) {
			awardItems.rewardTakeAffectAndPush(player, Action.WORLD_STRONGPOINT_MARCH, false);
		}

		// 资源收益报告邮件
		CollectMail.Builder builder = MailBuilderUtil.createCollectMail(this.getMarchEntity(), mailId.getNumber(), hasAwardItem);
		CollectMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(this.getPlayerId())
				.setMailId(mailId)
				.addContents(builder)
				.build());

		logger.info("strongpoint march, doAwardCalc, playerId:{}, marchId:{}, award:{}", player.getId(), this.getMarchId(), awardItems.toString());
		this.getMarchEntity().setAwardItems(null);
	}

	/**
	 * 行军到达处理
	 * @param player
	 */
	private void marchReach(Player player) {
		logger.info("strongpoint march, marchReach, marchId:{}", this.getMarchId());
		
		this.getMarchEntity().setEndTime(Long.MAX_VALUE);

		WorldPoint point = WorldPointService.getInstance().getWorldPoint(this.getMarchEntity().getTerminalId());

		if (point == null || point.getPointType() != WorldPointType.STRONG_POINT_VALUE) {
			logger.info("strongpoint march, marchReach, point changed, marchId:{}", this.getMarchId());
			strongpointMarchReturn(player);
			return;
		}

		switch (point.getPointStatus()) {

		// 初始状态
		case StrongpointStatus.SP_INIT_VALUE:
			initStatusReach(point, player);
			break;

		// 有部队采集中
		case StrongpointStatus.SP_COLLECT_VALUE:
			collectStatusReach(point, player);
			break;

		// 空状态
		case StrongpointStatus.SP_EMPTY_VALUE:
			emptyStatusReach(point, player);
			break;

		default:
			strongpointMarchReturn(player);
			break;
		}
	}
	
	/**
	 * 
	 * 据点初始状态行军到达
	 */
	private void initStatusReach(WorldPoint point, Player player) {
		
		try {
			ActivityManager.getInstance().postEvent(new VitCostEvent(this.getPlayerId(), getMarchEntity().getVitCost()));
			HawkApp.getInstance().postMsg(getPlayer(), PlayerVitCostMsg.valueOf(getMarchEntity().getPlayerId(), getMarchEntity().getVitCost()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 据点野怪战斗
		if (!doMonsterFight(point, player)) {
			logger.info("strongpoint march, initStatusReach, doMonsterFight failed, marchId:{}", this.getMarchId());
			return;
		}

		// 世界点处理
		point.initPlayerInfo(player.getData());
		point.setMarchId(this.getMarchId());
		point.setPointStatus(StrongpointStatus.SP_COLLECT_VALUE);
		notifyStrongpointUpdate(point);
		logger.info("strongpoint march, initStatusReach, doMonsterFight success, marchId:{}", this.getMarchId());
	}

	/**
	 * 据点采集状态行军到达
	 */
	private void collectStatusReach(WorldPoint point, Player atkPlayer) {
		if (!HawkOSOperator.isEmptyString(atkPlayer.getId())
				&& !HawkOSOperator.isEmptyString(point.getPlayerId())
				&& GuildService.getInstance().isInTheSameGuild(atkPlayer.getId(), point.getPlayerId())) {
			logger.info("strongpoint march, collectStatusReach, guild changed, marchId:{}, guildId:{}", this.getMarchId(), atkPlayer.getId());

			// 对方所属联盟改变，进攻失败
			int[] pos = GameUtil.splitXAndY(this.getMarchEntity().getOrigionId());
			CollectMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(this.getPlayerId())
					.setMailId(MailId.STRONGPOINT_GUILD_CHANGE)
					.addContents(pos[0], pos[1])
					.build());

			strongpointMarchReturn(atkPlayer);
			return;
		}

		if (point.getPlayerId().equals(atkPlayer.getId())) {
			strongpointMarchReturn(atkPlayer);
			return;
		}
		
		/********************** 战斗数据组装及战斗 ***************************/
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(atkPlayer);

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		Player defPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
		defPlayers.add(defPlayer);
		
		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);

		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		IWorldMarch defMarch = WorldMarchService.getInstance().getMarch(point.getMarchId());
		defMarchs.add(defMarch);

		// 战斗
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_STRONG_POINT_PVP, point.getId(), atkPlayers, defPlayers,
				atkMarchs, defMarchs, null);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);

		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();
		// 发送战斗邮件
		FightMailService.getInstance().sendFightMail(point.getPointType(), battleIncome, battleOutcome, null);
		BattleService.getInstance().dealWithPvpBattleEvent(battleIncome, battleOutcome, isMassMarch(), this.getMarchType());

		// 双方战后剩余部队
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> atkArmyLeft = atkArmyLeftMap.get(atkPlayer.getId());
		List<ArmyInfo> defArmyLeft = defArmyLeftMap.get(defPlayer.getId());

		// 发送战斗结果，用于前端播放动画
		WorldMarchService.getInstance().sendBattleResultInfo(this, isAtkWin, atkArmyLeft, defArmyLeft, isAtkWin);
		
		// 处理任务、统计
		sendMsgUpdateDefPlayerAfterWar(defPlayer, battleOutcome, null);
		sendMsgUpdateAtkPlayerAfterWar(isAtkWin, atkArmyLeft, defPlayer.getCityLevel(), atkPlayer, battleOutcome);
		/********************** 战斗数据组装及战斗 ***************************/

		try {
			ActivityManager.getInstance().postEvent(new VitCostEvent(this.getPlayerId(), getMarchEntity().getVitCost()));
			HawkApp.getInstance().postMsg(getPlayer(), PlayerVitCostMsg.valueOf(getMarchEntity().getPlayerId(), getMarchEntity().getVitCost()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (!isAtkWin) {
			logger.info("strongpoint march, collectStatusReach, attack failed, marchId:{}", this.getMarchId());
			WorldMarchService.getInstance().onMarchReturn(this, atkArmyLeft, point.getId());
			// 刷新防御方的兵力信息
			WorldMarchService.getInstance().resetMarchArmys(defMarch, defArmyLeft);
		} else {
			logger.info("strongpoint march, collectStatusReach, attack win, marchId:{}", this.getMarchId());
			
			// 防守行军奖励计算级返回
			boolean pointIsRemove = WorldStrongPointService.getInstance().doStrongpointReturn(defMarch, defArmyLeft, false);
			
			// 点被移除，则攻击方直接返回
			if (pointIsRemove) {
				WorldMarchService.getInstance().onMarchReturn(this, atkArmyLeft, point.getId());
				return;
			}
			
			// 世界点处理
			point.initPlayerInfo(atkPlayer.getData());
			point.setMarchId(this.getMarchId());
			point.setPointStatus(StrongpointStatus.SP_COLLECT_VALUE);
			notifyStrongpointUpdate(point);

			// 行军处理
			onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, atkArmyLeft, point);
			
			MissionManager.getInstance().postMsg(atkPlayer, new EventAttackPlayerStrongpoint(isAtkWin));
		}
		
		WorldStrongpointCfg strongPointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, point.getMonsterId());
		MissionManager.getInstance().postMsg(atkPlayer, new EventAttackStrongpoint(strongPointCfg.getLevel(), isAtkWin));
		ActivityManager.getInstance().postEvent(new OccupyStrongpointEvent(atkPlayer.getId(), point.getMonsterId(), isAtkWin));
		// 刷新战力
		refreshPowerAfterWar(atkPlayers, defPlayers);
	}

	/**
	 * 据点空状态行军到达
	 */
	private void emptyStatusReach(WorldPoint point, Player player) {
		logger.info("strongpoint march, emptyStatusReach, marchId:{}", this.getMarchId());
		
		// 世界点处理
		point.initPlayerInfo(player.getData());
		point.setMarchId(this.getMarchId());
		point.setPointStatus(StrongpointStatus.SP_COLLECT_VALUE);
		notifyStrongpointUpdate(point);

		// 成功占领空据点
		int[] pos = GameUtil.splitXAndY(this.getMarchEntity().getOrigionId());
		CollectMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(this.getPlayerId())
				.setMailId(MailId.STRONGPOINT_OCCUPY_EMPTY_SUCC)
				.addContents(pos[0], pos[1])
				.build());

		// 行军处理
		onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, this.getMarchEntity().getArmys(), point);
		
		WorldStrongpointCfg strongPointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, point.getMonsterId());
		
		MissionManager.getInstance().postMsg(player, new EventAttackStrongpoint(strongPointCfg.getLevel(), true));
		ActivityManager.getInstance().postEvent(new OccupyStrongpointEvent(player.getId(), point.getMonsterId(), true));
		
		try {
			ActivityManager.getInstance().postEvent(new VitCostEvent(this.getPlayerId(), getMarchEntity().getVitCost()));
			HawkApp.getInstance().postMsg(getPlayer(), PlayerVitCostMsg.valueOf(getMarchEntity().getPlayerId(), getMarchEntity().getVitCost()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 据点野怪战斗
	 * 
	 * @param point
	 * @param player
	 * @return
	 */
	private boolean doMonsterFight(WorldPoint point, Player player) {
		WorldStrongpointCfg strongPointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, point.getMonsterId());

		// 组织战斗数据
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		atkPlayers.add(player);

		PveBattleIncome battleIncome = BattleService.getInstance().initStrongPointPveBattleData(BattleConst.BattleType.ATTACK_STRONG_POINT_PVE, point.getId(),
				strongPointCfg.getId(), atkPlayers, atkMarchs);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);

		// 战斗结果
		boolean isWin = battleOutcome.isAtkWin();

		// 填充奖励
		AwardItems award = AwardItems.valueOf();
		if (isWin) {
			award.addAwards(strongPointCfg.getKillAwards());
		}
		
		// 发送战斗结果
		WorldMarchService.getInstance().sendBattleResultInfo(this, isWin, this.getMarchEntity().getArmys(), new ArrayList<ArmyInfo>(), isWin);
		
		MailRewards mailRewards = new MailRewards().addPublicRewards(award.getAwardItems()); 
		// 据点PVE战斗邮件发放
		FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_STRONG_POINT_PVE, battleIncome, battleOutcome, mailRewards);

		// 行军返回
		if (!isWin) {
			WorldMarchService.getInstance().onMarchReturn(this, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), 0);
		} else {
			// 行军处理
			onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), point);
			// 投递发奖
			player.dealMsg(MsgId.ATTACK_MONSTER_AWARD, new StrongpointAwardInvoker(player, award));
		}

		GsApp.getInstance().postMsg(player.getXid(), AtkAfterPveMsg.valueOf(battleOutcome, this.getMarchType(), strongPointCfg.getLevel()));
		MissionManager.getInstance().postMsg(player, new EventAttackStrongpoint(strongPointCfg.getLevel(), isWin));
		ActivityManager.getInstance().postEvent(new OccupyStrongpointEvent(player.getId(), point.getMonsterId(), isWin));
		// 刷新战力
//		refreshPowerAfterWar(atkPlayers, null);
		return isWin;
	}

	/**
	 * 据点行军返回
	 */
	private void strongpointMarchReturn(Player player) {
		logger.info("strongpoint march, strongpointMarchReturn, marchId:{}", this.getMarchId());
		WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
		// 返还体力
		player.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(player, this));
	}

	/**
	 * 据点数据推送刷新
	 * @param point
	 */
	private void notifyStrongpointUpdate(WorldPoint point) {
		logger.info("strongpoint march, notifyStrongpointUpdate, marchId:{}", this.getMarchId());
		// 点行军数据刷新
		Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		for (IWorldMarch march : worldPointMarchs) {
			if(march.isReturnBackMarch() || march.getMarchId().equals(this.getMarchId())) {
				continue;
			}
			march.updateMarch();
			WorldMarchService.getInstance().addGuildMarch(march);
		}
		
		// 点刷新
		WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
	}
	
	@Override
	public void moveCityProcess(long currentTime) {
		// 返还体力
		if (this.getMarchEntity().getReachTime() <= 0) {
			Player player = this.getPlayer();
			player.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(player, this));
		}
		
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		WorldStrongPointService.getInstance().doStrongpointReturn(this, getMarchEntity().getArmys(), false);
	}
	
	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		doAwardCalc(player);
		return true;
	}
}
