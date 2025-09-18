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
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.TreasureHuntResCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.MarchVitReturnBackMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.PlayerVitCostMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
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
import com.hawk.game.world.service.WorldTreasureHuntService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 寻宝资源点行军
 * @author golden
 *
 */
public class TreasureHuntResMarch extends PlayerMarch implements BasedMarch , IReportPushMarch, IPassiveAlarmTriggerMarch{

	private static Logger logger = LoggerFactory.getLogger("Server");
	
	public TreasureHuntResMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.TREASURE_HUNT_RESOURCE;
	}
	
	@Override
	public void onMarchStart() {
		this.pushAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void onMarchReturn() {
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
	public void remove() {
		super.remove();
		// 删除行军报告
		removeAttackReport();
	}


	@Override
	public boolean marchHeartBeats(long currTime) {

		if (this.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return false;
		}

		if (this.getMarchEntity().getResEndTime() <= 0) {
			return false;
		}
		
		if (this.getMarchEntity().getResEndTime() > HawkTime.getMillisecond()) {
			return false;
		}
		
		// 据点
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		
		TreasureHuntResCfg cfg = null;
		if (point == null) {
			cfg = HawkConfigManager.getInstance().getConfigByIndex(TreasureHuntResCfg.class, 0);
		} else {
			cfg = HawkConfigManager.getInstance().getConfigByKey(TreasureHuntResCfg.class, point.getResourceId());
		}
		
		if (cfg != null) {
			// 填充奖励
			AwardItems award = AwardItems.valueOf();
			award.addAwards(cfg.getFixedAwards());
			award.addAwards(cfg.getRandomAwards());
			this.getMarchEntity().setAwardItems(award);
		}
		
		this.getMarchEntity().setResEndTime(HawkTime.getMillisecond());
		WorldMarchService.getInstance().onMarchReturn(this, this.getMarchEntity().getArmys(), 0);
		
		WorldTreasureHuntService.getInstance().notifyResRemove(pointId, this.getPlayer().getId(), this.getMarchId());
		return true;
	}
	
	@Override
	public boolean isNeedCalcTickMarch() {
		return true;
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

		TreasureHuntResCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TreasureHuntResCfg.class, point.getResourceId());
		this.getMarchEntity().setResEndTime(currentTime + cfg.getTickTime() * 1000L);
		logger.info("treasureHuntRes march, detailMarchStop, resStartTime:{}, resEndTime:{}", this.getMarchEntity().getResStartTime(), this.getMarchEntity().getResEndTime());
	}
	
	/**
	 * 行军召回
	 */
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		WorldMarch march = this.getMarchEntity();
		
		if (isReturnBackMarch()) {
			return;
		}
		
		if (this.isMarchState()) {
			WorldMarchService.getInstance().onMarchReturn(this, march.getArmys(), 0);
			return;
		}
		
		// 据点
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		
		// 行军返回
		march.setResEndTime(0);
		WorldMarchService.getInstance().onMarchReturn(this, march.getArmys(), 0);
		
		point.setPlayerId("");
		point.setPlayerName("");
		point.setPlayerIcon(0);
		point.setMarchId("");
		
		// 点行军数据刷新
		Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		for (IWorldMarch pointMarch : worldPointMarchs) {
			if(pointMarch.isReturnBackMarch() || pointMarch.getMarchId().equals(this.getMarchId())) {
				continue;
			}
			// 更新行军信息(更新行军线颜色)
			pointMarch.updateMarch();
			// 删除联盟战争显示
			WorldMarchService.getInstance().rmGuildMarch(pointMarch.getMarchId());
		}
		
		int[] pos = GameUtil.splitXAndY(pointId);
		
		// 资源收益报告邮件
		MailId mailId = MailId.TH_RESOURCE_REWARD;
		CollectMail.Builder builder = MailBuilderUtil.createCollectMail(this.getMarchEntity(), mailId.getNumber(), false);
		builder.setEndTime(HawkTime.getMillisecond());
		builder.setCollectTime((int)(HawkTime.getMillisecond() - march.getResStartTime()));
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		CollectMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(this.getPlayerId())
				.setMailId(mailId)
				.addContents(builder)
				.build());
		
		// 通知场景点数据更新
		WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
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
		
		builder.setPointType(WorldPointType.TH_RESOURCE);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		
		if (worldPoint != null && !HawkOSOperator.isEmptyString(worldPoint.getMarchId()) && worldPoint.getPointType() == WorldPointType.TH_RESOURCE_VALUE) {
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
	
	@Override
	public void onWorldMarchReturn(Player player) {
		doAwardCalc(player);
	}
	
	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		doAwardCalc(player);
		return true;
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
		MailId mailId = MailId.TH_RESOURCE_REWARD;
		
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

		this.getMarchEntity().setAwardItems(null);
	}
	
	@Override
	public void onMarchReach(Player player) {
		// 删除行军报告
		removeAttackReport();
		pullAttackReport();
		this.getMarchEntity().setEndTime(Long.MAX_VALUE);

		WorldPoint point = WorldPointService.getInstance().getWorldPoint(this.getMarchEntity().getTerminalId());

		if (point == null || point.getPointType() != WorldPointType.TH_RESOURCE_VALUE) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			// 返还体力
			player.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(player, this));
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.TH_RES_DISAPPER)
					.build());
			
			return;
		}
		
		if (HawkOSOperator.isEmptyString(point.getPlayerId())) {
			emptyStatusReach(point, player);
		} else {
			collectStatusReach(point, player);
		}
		
		try {
			ActivityManager.getInstance().postEvent(new VitCostEvent(this.getPlayerId(), getMarchEntity().getVitCost()));
			HawkApp.getInstance().postMsg(getPlayer(), PlayerVitCostMsg.valueOf(getMarchEntity().getPlayerId(), getMarchEntity().getVitCost()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 采集状态行军到达
	 */
	private void collectStatusReach(WorldPoint point, Player atkPlayer) {
		if (!HawkOSOperator.isEmptyString(atkPlayer.getId())
				&& !HawkOSOperator.isEmptyString(point.getPlayerId())
				&& GuildService.getInstance().isInTheSameGuild(atkPlayer.getId(), point.getPlayerId())) {
			logger.info("treasureHuntReach march, collectStatusReach, guild changed, marchId:{}, guildId:{}", this.getMarchId(), atkPlayer.getId());

			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			
			// 返还体力
			atkPlayer.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(atkPlayer, this));
			return;
		}

		if (point.getPlayerId().equals(atkPlayer.getId())) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			atkPlayer.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(atkPlayer, this));
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
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_TREASURE_HUNT_RES, point.getId(), atkPlayers, defPlayers,
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

		if (!isAtkWin) {
			logger.info("treasureHuntReach march, collectStatusReach, attack failed, marchId:{}", this.getMarchId());
			WorldMarchService.getInstance().onMarchReturn(this, atkArmyLeft, point.getId());
			// 刷新防御方的兵力信息
			WorldMarchService.getInstance().resetMarchArmys(defMarch, defArmyLeft);
		} else {
			logger.info("treasureHuntReach march, collectStatusReach, attack win, marchId:{}", this.getMarchId());
			
			// 防守行军奖励计算级返回
			defMarch.getMarchEntity().setResEndTime(0);
			WorldMarchService.getInstance().onMarchReturn(defMarch, defArmyLeft, 0);
			
			// 世界点处理
			point.initPlayerInfo(atkPlayer.getData());
			point.setMarchId(this.getMarchId());
			notifyPointUpdate(point);

			// 行军处理
			onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, atkArmyLeft, point);
		}
		
		// 刷新战力
		refreshPowerAfterWar(atkPlayers, defPlayers);
		
		LogUtil.logTreasureHuntCollectResource(atkPlayer);
		LogUtil.logTreasureHuntResourceBattle(atkPlayer, atkPlayer.getPower(), defPlayer.getId(), defPlayer.getPower());
	}

	/**
	 * 据点空状态行军到达
	 */
	private void emptyStatusReach(WorldPoint point, Player player) {
		logger.info("treasureHuntReach march, emptyStatusReach, marchId:{}", this.getMarchId());
		
		// 世界点处理
		point.initPlayerInfo(player.getData());
		point.setMarchId(this.getMarchId());
		notifyPointUpdate(point);

		// 行军处理
		onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, this.getMarchEntity().getArmys(), point);
		
		LogUtil.logTreasureHuntCollectResource(player);
	}
	
	private void notifyPointUpdate(WorldPoint point) {
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
		
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		if (this.isReturnBackMarch()) {
			return;
		}
		
		// 返还体力
		Player player = this.getPlayer();
		player.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(player, this));
		
		if (this.isMarchState()) {
			WorldMarchService.getInstance().onMarchReturn(this, getMarchEntity().getArmys(), 0);
			return;
		}
		
		WorldMarch march = this.getMarchEntity();
		
		
		// 据点
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		
		// 行军返回
		march.setResEndTime(0);
		point.setPlayerId("");
		point.setPlayerName("");
		point.setPlayerIcon(0);
		point.setMarchId("");
		
		// 点行军数据刷新
		Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		for (IWorldMarch pointMarch : worldPointMarchs) {
			if(pointMarch.isReturnBackMarch() || pointMarch.getMarchId().equals(this.getMarchId())) {
				continue;
			}
			// 更新行军信息(更新行军线颜色)
			pointMarch.updateMarch();
			// 删除联盟战争显示
			WorldMarchService.getInstance().rmGuildMarch(pointMarch.getMarchId());
		}
		
		int[] pos = GameUtil.splitXAndY(pointId);
		
		// 资源收益报告邮件
		MailId mailId = MailId.TH_RESOURCE_REWARD;
		CollectMail.Builder builder = MailBuilderUtil.createCollectMail(this.getMarchEntity(), mailId.getNumber(), false);
		builder.setEndTime(HawkTime.getMillisecond());
		builder.setCollectTime((int)(HawkTime.getMillisecond() - march.getResStartTime()));
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		CollectMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(this.getPlayerId())
				.setMailId(mailId)
				.addContents(builder)
				.build());
		
		// 通知场景点数据更新
		WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
	}
}
