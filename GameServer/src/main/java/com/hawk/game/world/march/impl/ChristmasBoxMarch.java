package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.WorldChristmasWarBoxCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
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

/**
 * 圣诞宝箱行军
 * @author copy from golden
 * 	
 * 		? , it's none of my business  -- golden
 *
 */
public class ChristmasBoxMarch extends PlayerMarch implements BasedMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	private static Logger logger = LoggerFactory.getLogger("Server");

	public ChristmasBoxMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.CHRISTMAS_BOX_MARCH;
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
		this.pullAttackReport();
	}

	@Override
	public boolean isNeedCalcTickMarch() {
		return true;
	}

	@Override
	public void onMarchReach(Player player) {
		int christmasBox = player.getReceivedChristmasBoxNumber();
		this.getMarchEntity().setEndTime(Long.MAX_VALUE);

		if (christmasBox >= WorldMarchConstProperty.getInstance().getChristmasBoxReceiveLimit()) {
			player.sendError(HP.code.WORLD_CHRISTMAS_BOX_MARCH_C_VALUE, Status.Error.CHRISTMAS_BOX_TIMES_LIMIT_VALUE, 0);
			WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());

			return;
		}

		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(this.getTerminalId());
		if (worldPoint == null || worldPoint.getPointType() != WorldPointType.CHRISTMAS_BOX_VALUE) {
			sendDisapperaMail();

			WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
		} else {
			// 无人占领.
			if (HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				marchReachNoOwner(player, worldPoint);
			} else {
				marchReachHasOwner(player, worldPoint);
			}
		}
		removeAttackReport();
		pullAttackReport();
	}

	/**
	 * 有人占领的情况下到达.
	 * @param player
	 * @param worldPoint
	 */
	private void marchReachHasOwner(Player player, WorldPoint worldPoint) {
		String occPlayerId = worldPoint.getPlayerId();
		// 如果是自己占领的，或者联盟的人占领的。
		if (occPlayerId.equals(player.getId()) || GuildService.getInstance().isInTheSameGuild(player.getId(), occPlayerId)) {
			logger.info("occupy playerId:{}, atkPlayerId:{} return back", occPlayerId, player.getId());

			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			// 发送邮件---采集失败：已被同盟玩家占领
			CollectMail.Builder builder = MailBuilderUtil.createCollectMail(this.getMarchEntity(),
					MailId.COLLECT_FAILED_ALLIED_OCCUPY_VALUE, false);
			CollectMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(player.getId())
							.setMailId(MailId.COLLECT_FAILED_ALLIED_OCCUPY).addContents(builder).build());
			return;
		}

		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(player);

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		Player defPlayer = GlobalData.getInstance().makesurePlayer(worldPoint.getPlayerId());
		defPlayers.add(defPlayer);

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);

		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		IWorldMarch defMarch = WorldMarchService.getInstance().getMarch(worldPoint.getMarchId());
		defMarchs.add(defMarch);

		// 战斗
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_TREASURE_HUNT_RES, worldPoint.getId(), atkPlayers, defPlayers,
				atkMarchs, defMarchs, null);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);

		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();
		// 发送战斗邮件
		FightMailService.getInstance().sendFightMail(worldPoint.getPointType(), battleIncome, battleOutcome, null);
		BattleService.getInstance().dealWithPvpBattleEvent(battleIncome, battleOutcome, isMassMarch(), this.getMarchType());

		// 双方战后剩余部队
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> atkArmyLeft = atkArmyLeftMap.get(player.getId());
		List<ArmyInfo> defArmyLeft = defArmyLeftMap.get(defPlayer.getId());

		// 发送战斗结果，用于前端播放动画
		WorldMarchService.getInstance().sendBattleResultInfo(this, isAtkWin, atkArmyLeft, defArmyLeft, isAtkWin);

		// 处理任务、统计
		sendMsgUpdateDefPlayerAfterWar(defPlayer, battleOutcome, null);
		sendMsgUpdateAtkPlayerAfterWar(isAtkWin, atkArmyLeft, defPlayer.getCityLevel(), player, battleOutcome);

		if (!isAtkWin) {
			logger.info("christmas box attack failed, marchId:{}", this.getMarchId());
			WorldMarchService.getInstance().onMarchReturn(this, atkArmyLeft, worldPoint.getId());
			// 刷新防御方的兵力信息
			WorldMarchService.getInstance().resetMarchArmys(defMarch, defArmyLeft);
		} else {
			logger.info("christmas box attack win, marchId:{}", this.getMarchId());

			// 防守行军奖励计算级返回
			defMarch.getMarchEntity().setResEndTime(0);
			WorldMarchService.getInstance().onMarchReturn(defMarch, defArmyLeft, 0);

			String ownerId = worldPoint.getOwnerId();

			if (!HawkOSOperator.isEmptyString(ownerId) && player.getGuildId().equals(ownerId)) {
				if (atkArmyLeft != null) {
					WorldMarch march = getMarchEntity();
					if (march != null) {
						march.setArmys(atkArmyLeft);
					}					
				}
				this.getBox(player, worldPoint);				
				LogUtil.logChristmasBox(player, worldPoint.getMonsterId(), GsConst.ChristmasConst.GET_BOX_WAR);
			} else {
				// 世界点处理
				worldPoint.initPlayerInfo(player.getData());
				worldPoint.setMarchId(this.getMarchId());
				notifyPointUpdate(worldPoint);

				// 行军处理
				onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, atkArmyLeft, worldPoint);
			}
		}

		// 宝箱战斗.
		LogUtil.logChristmasBox(this.getPlayer(), worldPoint.getMonsterId(), GsConst.ChristmasConst.BOX_WAR);
	}

	private void notifyPointUpdate(WorldPoint point) {
		// 点行军数据刷新
		Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		for (IWorldMarch march : worldPointMarchs) {
			if (march.isReturnBackMarch() || march.getMarchId().equals(this.getMarchId())) {
				continue;
			}
			march.updateMarch();
			WorldMarchService.getInstance().addGuildMarch(march);
		}

		// 点刷新
		WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
	}

	/**
	 * 该点无人占领.
	 * @param player
	 * @param worldPoint
	 */
	private void marchReachNoOwner(Player player, WorldPoint worldPoint) {
		String ownerId = worldPoint.getOwnerId();

		// 是工会的。
		if (!HawkOSOperator.isEmptyString(ownerId) && player.getGuildId().equals(ownerId)) {
			getBox(player, worldPoint);
			LogUtil.logChristmasBox(player, worldPoint.getMonsterId(), GsConst.ChristmasConst.GET_BOX_NORMAL);
		} else {
			// 走到这里要么是没有归属权的箱子， 要么是非自己联盟的箱子.
			// 转换为占领.
			worldPoint.initPlayerInfo(player.getData());
			worldPoint.setMarchId(this.getMarchId());
			notifyPointUpdate(worldPoint);
			// 行军处理
			onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, this.getMarchEntity().getArmys(), worldPoint);
		}
	}

	private void getBox(Player player, WorldPoint worldPoint) {
		try {
			sendAwardMail(worldPoint);
			WorldChristmasWarBoxCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldChristmasWarBoxCfg.class, worldPoint.getMonsterId());
			if (cfg != null && cfg.isNeedNotice()) {
				Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_CHRISTMAS_BOX;
				ChatParames.Builder builder = ChatParames.newBuilder();
				builder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
				builder.setKey(noticeId);
				builder.addParms(this.getPlayer().getName()).addParms(worldPoint.getX()).addParms(worldPoint.getY());

				ChatService.getInstance().addWorldBroadcastMsg(builder.build());
			}

			player.receiveChristmasBox(1);
			WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		// 删除城点.
		WorldPointService.getInstance().removeWorldPoint(worldPoint.getId(), true);
	}

	/**
	 * 宝箱消失
	 */
	public void sendDisapperaMail() {
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.CHRISTMAS_BOX_MISS)
				.setPlayerId(this.getPlayerId())
				.addContents(this.getMarchEntity().getTargetId())
				.build());
	}

	/**
	 * 奖励
	 */
	public void sendAwardMail(WorldPoint wp) {
		WorldChristmasWarBoxCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldChristmasWarBoxCfg.class, wp.getMonsterId());
		if (cfg == null) {
			return;
		}

		AwardItems award = AwardItems.valueOf();
		award.addAwards(cfg.getAwards());

		Object[] title = new Object[1];
		Object[] subTitle = new Object[1];
		title[0] = cfg.getId();
		subTitle[0] = cfg.getId();

		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.CHRISTMAS_BOX_AWARD)
				.setPlayerId(this.getPlayerId())
				.setRewards(award.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addContents(cfg.getId())
				.addSubTitles(subTitle)
				.addTitles(title)
				.build());
	}

	@Override
	public void pullAttackReport() {
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
	public Set<String> attackReportRecipients() {
		Set<String> result = alarmPointEnemyMarches().stream()
				.filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE)
				.map(IWorldMarch::getPlayerId)
				.filter(tid -> !Objects.equals(tid, this.getMarchEntity().getPlayerId()))
				.collect(Collectors.toSet());
		return result;
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		removeAttackReport();
		// 处发其他行军重推报告
		this.pullAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		this.removeAttackReport();
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
		if (point != null) {
			this.getMarchEntity().setResEndTime(HawkTime.getMillisecond());
			getBox(this.getPlayer(), point);

			LogUtil.logChristmasBox(this.getPlayer(), point.getMonsterId(), GsConst.ChristmasConst.GET_BOX_OCCUY);
		} else {
			logger.error("point miss:{}", pointId);
		}
		return true;
	}

	@Override
	public void detailMarchStop(WorldPoint point) {
		long currentTime = HawkTime.getMillisecond();
		this.getMarchEntity().setResStartTime(currentTime);

		WorldChristmasWarBoxCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldChristmasWarBoxCfg.class, point.getMonsterId());
		this.getMarchEntity().setResEndTime(currentTime + cfg.getTime() * 1000L);
		logger.info("christmas box march, detailMarchStop, resStartTime:{}, resEndTime:{}, playerId:{}", this.getMarchEntity().getResStartTime(),
				this.getMarchEntity().getResEndTime(), this.getMarchEntity().getPlayerId());
	}

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
			if (pointMarch.isReturnBackMarch() || pointMarch.getMarchId().equals(this.getMarchId())) {
				continue;
			}
			// 更新行军信息(更新行军线颜色)
			pointMarch.updateMarch();
			// 删除联盟战争显示
			WorldMarchService.getInstance().rmGuildMarch(pointMarch.getMarchId());
		}

		// 通知场景点数据更新
		WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
	}

	@Override
	public boolean needShowInGuildWar() {
		int terminalId = this.getMarchEntity().getTerminalId();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		if (worldPoint == null || worldPoint.getPointType() != WorldPointType.CHRISTMAS_BOX_VALUE) {
			return false;
		}

		String targerPlayerId = worldPoint.getPlayerId();
		if (HawkOSOperator.isEmptyString(targerPlayerId)) {
			return false;
		}

		return !GuildService.getInstance().isInTheSameGuild(this.getPlayerId(), targerPlayerId);
	}

	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		return true;
	}

	@Override
	public void moveCityProcess(long curTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		if (this.isReturnBackMarch()) {
			return;
		}

		if (this.isMarchState()) {
			WorldMarchService.getInstance().onMarchReturn(this, getMarchEntity().getArmys(), 0);
			return;
		}

		WorldMarch march = this.getMarchEntity();
		// 据点
		int pointId = this.getMarchEntity().getTerminalId();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		if (point != null) {
			// 行军返回
			march.setResEndTime(0);
			point.setPlayerId("");
			point.setPlayerName("");
			point.setPlayerIcon(0);
			point.setMarchId("");

			// 点行军数据刷新
			Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
			for (IWorldMarch pointMarch : worldPointMarchs) {
				if (pointMarch.isReturnBackMarch() || pointMarch.getMarchId().equals(this.getMarchId())) {
					continue;
				}
				// 更新行军信息(更新行军线颜色)
				pointMarch.updateMarch();
				// 删除联盟战争显示
				WorldMarchService.getInstance().rmGuildMarch(pointMarch.getMarchId());
			}

			int[] pos = GameUtil.splitXAndY(pointId);

			// 资源收益报告邮件
			/*
			 * MailId mailId = MailId.CHRISTMAS_BOX_AWARD; CollectMail.Builder builder = MailBuilderUtil.createCollectMail(this.getMarchEntity(),
			 * mailId.getNumber(), false); builder.setEndTime(HawkTime.getMillisecond()); builder.setCollectTime((int)(HawkTime.getMillisecond() -
			 * march.getResStartTime())); builder.setX(pos[0]); builder.setY(pos[1]);
			 * CollectMailService.getInstance().sendMail(MailParames.newBuilder() .setPlayerId(this.getPlayerId()) .setMailId(mailId)
			 * .addContents(builder) .build());
			 */

			// 通知场景点数据更新
			WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
		} else {
			logger.error("point is null, pointId:{}, marchInfo:{}", pointId, this.getMarchEntity());

			return;
		}
	}

	/**
	 * 获取被动方联盟战争界面信息
	 */
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		// 队长位置
		int terminalId = this.getMarchEntity().getTerminalId();
		int[] pos = GameUtil.splitXAndY(terminalId);

		builder.setPointType(WorldPointType.CHRISTMAS_BOX);
		builder.setX(pos[0]);
		builder.setY(pos[1]);

		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);

		if (worldPoint != null && !HawkOSOperator.isEmptyString(worldPoint.getMarchId()) && worldPoint.getPointType() == WorldPointType.CHRISTMAS_BOX_VALUE) {
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
}
