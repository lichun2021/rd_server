package com.hawk.game.module.lianmengyqzz.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.WorldPylonCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZPylonHonor;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZReportPushMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZPylon;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;

/**
 * 能量塔行军
 * @author golden
 *
 */
public class YQZZPylonMarch extends IYQZZWorldMarch implements IYQZZReportPushMarch, IYQZZPassiveAlarmTriggerMarch {

	private int vitBack;

	public YQZZPylonMarch(IYQZZPlayer parent) {
		super(parent);
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) { // 采集中
			marchHeartBeats(getParent().getParent().getCurTimeMil());
			return;
		}

		// 当前时间
		long currTime = HawkApp.getInstance().getCurrentTime();
		if (getMarchEntity().getEndTime() > currTime) {
			return;
		}
		// 行军返回到达
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			onMarchBack();
			return;
		}

		// 行军到达
		onMarchReach(getParent());

	}

	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.PYLON_MARCH;
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
		List<IYQZZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IYQZZWorldMarch march : marchList) {
			if (march instanceof IYQZZReportPushMarch && march != this) {
				((IYQZZReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IYQZZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IYQZZWorldMarch march : marchList) {
			if (march instanceof IYQZZReportPushMarch && march != this) {
				((IYQZZReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		removeAttackReport();
	}

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
		YQZZPylon point = (YQZZPylon) getParent().getParent().getWorldPoint(pointId).orElse(null);

		WorldPylonCfg cfg = null;
		if (point == null) {
			cfg = HawkConfigManager.getInstance().getConfigByIndex(WorldPylonCfg.class, 0);
		} else {
			cfg = HawkConfigManager.getInstance().getConfigByKey(WorldPylonCfg.class, point.getResourceId());
		}

		if (cfg != null) {
			// 填充奖励
			AwardItems award = AwardItems.valueOf();
			award.addAwards(cfg.getFixedAwards());
			award.addAwards(cfg.getRandomAwards());
			this.getMarchEntity().setAwardItems(award);

			beforeImmediatelyRemoveMarchProcess(getParent());
			
			YQZZPylonHonor mhonor = getParent().getPylonHonorStat(cfg.getId());
			mhonor.setPylonCount(mhonor.getPylonCount() + 1);
			mhonor.setPlayerHonor(mhonor.getPlayerHonor() + point.getCfg().getPlayerScore());
			mhonor.setGuildHonor(mhonor.getGuildHonor() + point.getCfg().getAllianceScore());
			mhonor.setNationHonor(mhonor.getNationHonor() + point.getCfg().getNationScore());
		}

		this.getMarchEntity().setResEndTime(HawkTime.getMillisecond());

		onMarchReturn(pointId, getParent().getPointId(), this.getMarchEntity().getArmys());

		point.removeWorldPoint();
		
		return true;
	}

	@Override
	public boolean isNeedCalcTickMarch() {
		return true;
	}

	@Override
	public boolean needShowInGuildWar() {
		int terminalId = this.getMarchEntity().getTerminalId();
		IYQZZWorldPoint worldPoint = getParent().getParent().getWorldPoint(terminalId).orElse(null);
		if (worldPoint == null) {
			return false;
		}

		IYQZZWorldMarch oldmarch = pointMarches(terminalId).stream().filter(m -> m.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE).findFirst()
				.orElse(null);

		if (oldmarch == null || !Objects.equals(oldmarch.getParent().getMainServerId(), getParent().getMainServerId())) {
			return true;
		}

		return false;
	}

	public void detailMarchStop(YQZZPylon point) {
		getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
		this.vitBack = 0;
		long currentTime = HawkTime.getMillisecond();
		this.getMarchEntity().setResStartTime(currentTime);

		WorldPylonCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldPylonCfg.class, point.getResourceId());
		this.getMarchEntity().setResEndTime(currentTime + cfg.getTickTime() * 1000L);
		point.setCollectMarch(this);
	}

	/**
	 * 行军召回
	 */
	@Override
	public void onMarchCallback() {
		WorldMarch march = this.getMarchEntity();

		// 能量塔
		int pointId = this.getMarchEntity().getTerminalId();
		IYQZZWorldPoint point = getParent().getParent().getWorldPoint(pointId).orElse(null);

		// 行军返回
		march.setResEndTime(0);
		super.onMarchCallback();

		// // 点行军数据刷新
		// Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		// for (IWorldMarch pointMarch : worldPointMarchs) {
		// if (pointMarch.isReturnBackMarch() || pointMarch.getMarchId().equals(this.getMarchId())) {
		// continue;
		// }
		// // 更新行军信息(更新行军线颜色)
		// pointMarch.updateMarch();
		// // 删除联盟战争显示
		// WorldMarchService.getInstance().rmGuildMarch(pointMarch.getMarchId());
		// }

		int[] pos = GameUtil.splitXAndY(pointId);

		// 资源收益报告邮件
		MailId mailId = MailId.PYLON_COLLECT;
		CollectMail.Builder builder = MailBuilderUtil.createCollectMail(this.getMarchEntity(), mailId.getNumber(), false);
		builder.setEndTime(HawkTime.getMillisecond());
		builder.setCollectTime((int) (HawkTime.getMillisecond() - march.getResStartTime()));
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		CollectMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(this.getPlayerId())
				.setMailId(mailId)
				.addContents(builder)
				.setDuntype(DungeonMailType.YQZZ)
				.build());

		// 通知场景点数据更新
		point.worldPointUpdate();
	}

	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		// 返还体力
		if (this.vitBack > 0) {
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, this.vitBack);
			awardItems.rewardTakeAffectAndPush(this.getPlayer(), Action.FIGHT_MONSTER);
		}
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
		if (getParent().getCollPylon() >= getParent().getParent().getCfg().getPylonLimit()) {
			return;
		}

		if (WorldUtil.getFreeArmyCnt(this.getMarchEntity().getArmys()) <= 0) {
			return;
		}

		AwardItems awardItems = this.getMarchEntity().getAwardItems();
		boolean hasAwardItem = awardItems.hasAwardItem();
		MailId mailId = MailId.PYLON_COLLECT;

		// 发奖
		if (hasAwardItem) {
			awardItems.rewardTakeAffectAndPush(player, Action.WORLD_PYLON_MARCH_REWARD, false);
		}

		// 资源收益报告邮件
		CollectMail.Builder builder = MailBuilderUtil.createCollectMail(this.getMarchEntity(), mailId.getNumber(), hasAwardItem);
		CollectMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(this.getPlayerId())
				.setMailId(mailId)
				.addContents(builder)
				.setDuntype(DungeonMailType.YQZZ)
				.build());

		this.getMarchEntity().setAwardItems(null);

	}

	@Override
	public void onMarchReach(Player player) {
		// 删除行军报告
		removeAttackReport();
		pullAttackReport();
		this.getMarchEntity().setEndTime(Long.MAX_VALUE);

		IYQZZWorldPoint point = getParent().getParent().getWorldPoint(this.getMarchEntity().getTerminalId()).orElse(null);

		if (point == null || point.getPointType() != WorldPointType.PYLON) {
			onMarchCallback();
			// 返还体力
			if (this.vitBack > 0) {
				AwardItems awardItems = AwardItems.valueOf();
				awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, this.vitBack);
				awardItems.rewardTakeAffectAndPush(this.getPlayer(), Action.FIGHT_MONSTER);
			}

			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.PYLON_DISPARE)
					.setDuntype(DungeonMailType.YQZZ)
					.build());

			return;
		}

		YQZZPylon pylon = (YQZZPylon) point;
		if (pylon.getCollectMarch() == null) {
			// 世界点处理
			detailMarchStop(pylon);
			pylon.worldPointUpdate();
			updateMarch();
		} else {
			collectStatusReach(pylon, player);
		}
	}

	/**
	 * 采集状态行军到达
	 */
	private void collectStatusReach(YQZZPylon point, Player atkPlayer) {
		if (Objects.equals(point.getCollectMarch().getParent().getMainServerId(), atkPlayer.getMainServerId())) {

			onMarchCallback();
			// 返还体力
			if (this.vitBack > 0) {
				AwardItems awardItems = AwardItems.valueOf();
				awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, this.vitBack);
				awardItems.rewardTakeAffectAndPush(this.getPlayer(), Action.FIGHT_MONSTER);
			}
			return;
		}

		/********************** 战斗数据组装及战斗 ***************************/
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(atkPlayer);

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		IYQZZPlayer defPlayer = point.getCollectMarch().getParent();
		defPlayers.add(defPlayer);

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);

		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		IYQZZWorldMarch defMarch = point.getCollectMarch();
		defMarchs.add(defMarch);

		// 战斗
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.PYLON_WAR, point.getPointId(), atkPlayers, defPlayers,
				atkMarchs, defMarchs, null);
		battleIncome.setDuntype(DungeonMailType.YQZZ);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.YQZZ);

		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();
		
		// 发送战斗邮件
		FightMailService.getInstance().sendFightMail(point.getPointType().getNumber(), battleIncome, battleOutcome, null);
		BattleService.getInstance().dealWithPvpBattleEvent(battleIncome, battleOutcome, isMassMarch(), this.getMarchType());

		// 双方战后剩余部队
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> atkArmyLeft = atkArmyLeftMap.get(atkPlayer.getId());
		List<ArmyInfo> defArmyLeft = defArmyLeftMap.get(defPlayer.getId());
		getMarchEntity().setArmys(atkArmyLeft);
		defMarch.getMarchEntity().setArmys(defArmyLeft);

		// 发送战斗结果，用于前端播放动画
		sendBattleResultInfo(isAtkWin, atkArmyLeft, defArmyLeft);

		if (!isAtkWin) {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), atkArmyLeft);
			defMarch.updateMarch();
		} else {
			detailMarchStop(point);
			updateMarch();

			defMarch.onMarchReturn(point.getPointId(), defPlayer.getPointId(), defArmyLeft);

		}

		// // 刷新战力
		// refreshPowerAfterWar(atkPlayers, defPlayers);

		LogUtil.logPylonReach(atkPlayer);
		LogUtil.logPylonBattle(atkPlayer, atkPlayer.getPower(), defPlayer.getId(), defPlayer.getPower());

	}

	public int getVitBack() {
		return vitBack;
	}

	public void setVitBack(int vitBack) {
		this.vitBack = vitBack;
	}

}
