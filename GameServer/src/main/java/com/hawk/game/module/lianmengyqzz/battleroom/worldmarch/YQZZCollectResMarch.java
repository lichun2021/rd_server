package com.hawk.game.module.lianmengyqzz.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZResourceCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.entity.YQZZMarchEntity;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZResourceHonor;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZReportPushMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZResource;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;

public class YQZZCollectResMarch extends IYQZZWorldMarch implements IYQZZReportPushMarch, IYQZZPassiveAlarmTriggerMarch {
	private double accumulation; // 累积值, 每超过一个就算做采集成功
	private int collected;
	private int resType;
	private YQZZResource resPoint;
	private YQZZResourceCfg yqcfg;
	double lastcollectSpeed;

	public YQZZCollectResMarch(IYQZZPlayer parent) {
		super(parent);
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.COLLECT_RESOURCE;
	}

	@Override
	public WorldMarchPB.Builder toBuilder(WorldMarchRelation relation) {
		WorldMarchPB.Builder result = super.toBuilder(relation);
		result.setTargetId(getMarchEntity().getTargetId());
		result.setResEndTime(getMarchEntity().getResEndTime());

		result.setResStartTime(getMarchEntity().getResStartTime());
		return result;
	}

	private void calResCollectTime() {
		if (resPoint == null) {
			return;
		}
		long curTimeMil = getParent().getParent().getCurTimeMil();
		if (getMarchEntity().getResStartTime() == 0) { // 采集开始
			resType = resPoint.getResourceCfg().getResType();
			yqcfg = resPoint.getCfg();
			getMarchEntity().setResStartTime(curTimeMil);
			getMarchEntity().setLastExploreTime(curTimeMil);
			getMarchEntity().setCollectBaseSpeed(WorldUtil.getCollectBaseSpeed(getPlayer(), resPoint.getResourceCfg().getResType(), getMarchEntity().getEffectParams()));
			AwardItems items = AwardItems.valueOf();
			items.addNewItem(Const.ItemType.PLAYER_ATTR_VALUE, resType, 0);
			getMarchEntity().setAwardItems(items);
		}
		lastcollectSpeed = getCollectSpeed();
		long resEndTime = curTimeMil + (long) (Math.ceil(resPoint.getRemainResNum() * 1000) / getCollectSpeed()) + 1000;
		getMarchEntity().setResEndTime(resEndTime);
		getMarchEntity().setCollectSpeed(lastcollectSpeed);
	}

	@Override
	public Set<String> attackReportRecipients() {
		IYQZZWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof YQZZResource)) {
			return Collections.emptySet();
		}
		YQZZResource fub = (YQZZResource) ttt;
		if (fub.getMarch() != null) {
			String id = fub.getMarch().getParent().getId();
			Set<String> allToNotify = new HashSet<>();
			allToNotify.add(id);
			return allToNotify;
		}
		return Collections.emptySet();
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) { // 采集中
			if (resPoint == null) {
				this.onMarchCallback();
				return;
			}
			if (resPoint.getRemainResNum() == 0) {
				resPoint.setMarch(null);
				this.onMarchCallback();
				return;
			}

			resPoint.setMarch(this);
			doCollectRes(resPoint);
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

	@Override
	public void onMarchReach(Player player) {
		marchReach(player);
		rePushPointReport();

	}

	private void marchReach(Player player) {
		YQZZMarchEntity march = getMarchEntity();
		IYQZZWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof YQZZResource)) {
			this.onMarchCallback();
			return;
		}
		YQZZResource point = (YQZZResource) ttt;
		YQZZCollectResMarch oldMarch = point.getMarch();
		if (oldMarch == null) {
			resPoint = point;
			resPoint.setMarch(this);
			march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE);
			notifyMarchEvent(MarchEvent.MARCH_DELETE);
			calResCollectTime();
			updateMarch();
			point.worldPointUpdate();
			return;
		}
		// 点已经被自己占领
		IYQZZPlayer defplayer = oldMarch.getParent();
		if (defplayer == getParent() || Objects.equals(getParent().getGuildId(), defplayer.getGuildId())) {
			this.onMarchCallback();
			return;
		}

		// 进攻方玩家 ATKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(getParent());

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(defplayer);

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		defMarchs.add(oldMarch);

		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_RES, point.getPointId(), atkPlayers, defPlayers, atkMarchs,
				defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setYQZZMail(getParent().getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.YQZZ);
		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		List<ArmyInfo> atkArmyLeft = atkArmyLeftMap.get(getParent().getId());
		getMarchEntity().setArmys(atkArmyLeft);

		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> defArmyLeft = defArmyLeftMap.get(defplayer.getId());
		oldMarch.getMarchEntity().setArmys(defArmyLeft);

		// 发送战斗结果给前台播放动画
		this.sendBattleResultInfo(isAtkWin, atkArmyLeft, defArmyLeft);
		if (isAtkWin) {
			march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE);
			resPoint = point;
			resPoint.setMarch(this);
			notifyMarchEvent(MarchEvent.MARCH_DELETE);
			calResCollectTime();
			updateMarch();

			oldMarch.resPoint = null;
			oldMarch.onMarchReturn(point.getPointId(), defplayer.getPointId(), defArmyLeft);
		} else {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), atkArmyLeft);

			oldMarch.updateMarch();
			oldMarch.calResCollectTime();
		}

		FightMailService.getInstance().sendFightMail(point.getPointType().getNumber(), battleIncome, battleOutcome, null);

		/************************* fight!!!!!!!!!!!! */
		point.worldPointUpdate();

		getParent().setLastAttacker(defplayer);

		defplayer.setLastAttacker(getParent());

	}

	private int collect(YQZZResource resPoint, long passTime, long totalLoad) {
		accumulation += lastcollectSpeed * 0.001 * passTime; // 增加一秒采集量

		if (accumulation > 1) {
			// 负重
			collected = (int) Math.min(collected, totalLoad);
			int col = (int) Math.min(totalLoad - collected, (int) accumulation);
			col = resPoint.doCollect(col);
			accumulation -= col;
			collected += col;

			return col;
		}
		return 0;
	}

	/**
	 * 对当前行军拥有的信息进行资源采集结算
	 * 
	 * @return true代表已采集完成并开始行军回程
	 */
	public void doCollectRes(YQZZResource resPoint) {
		long currentTime = HawkTime.getMillisecond();
		long lastCollect = getMarchEntity().getLastExploreTime();
		if (lastCollect == 0) {
			lastCollect = currentTime;
		}
		if (getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			return;
		}
		long passTime = currentTime - lastCollect;
		if (passTime < 1000) { // 1秒一算
			return;
		}

		// 计算
		WorldResourceCfg cfg = resPoint.getResourceCfg();
		int resType = cfg.getResType();
		// 重置时间
		getMarchEntity().setLastExploreTime(currentTime);
		// 负重
		long totalLoad = WorldMarchService.getInstance().getArmyCarryResNum(getParent(), getMarchEntity().getArmys(), resType, this.getMarchEntity().getEffectParams());
		collect(resPoint, passTime, totalLoad);

		// 剩余量
		double remain = resPoint.getRemainResNum();

		// 已采满或采集完
		if (totalLoad <= collected || remain == 0) {
			if (collected >= cfg.getResNum()) { // 完整采集完某资源点后，额外获取 XX% 的资源比率
				collected = (int) (collected * (1 + getParent().getEffect().getEffVal(EffType.EFF_335 ,getMarchEntity().getEffectParams()) * GsConst.EFF_PER));
			}
			this.onMarchCallback();
			return;
		}

		// 重置采集速度
		if (lastcollectSpeed != getCollectSpeed()) {
			calResCollectTime();
			updateMarch();
		}

	}

	@Override
	public void onMarchCallback() {
		if (resPoint != null) {
			resPoint.setMarch(null);
			resPoint.worldPointUpdate();
		}
		super.onMarchCallback();
	}

	public double getCollectSpeed() {
		if (Objects.isNull(resPoint)) {
			return 0;
		}
		WorldResourceCfg resCfg = resPoint.getResourceCfg();
		double collectSpeed = WorldUtil.getCollectSpeed(getParent(), resCfg.getResType(), resCfg.getLevel(), resPoint.getWorldPoint(), getMarchEntity().getEffectParams());
		return collectSpeed;
	}

	private void rePushPointReport() {
		// 删除行军报告
		removeAttackReport();
		this.pullAttackReport();
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
	public void onMarchBack() {
		beforeImmediatelyRemoveMarchProcess(getParent());
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		// 无兵行军不进行资源结算， 直接返回
		if (WorldUtil.getFreeArmyCnt(getMarchEntity().getArmys()) <= 0 || collected <= 0) {
			return false;
		}
//		long totalLoad = WorldMarchService.getInstance().getArmyCarryResNum(getParent(), getMarchEntity().getArmys(), resType, this.getMarchEntity().getEffectParams());
//		collected = (int) Math.min(collected, totalLoad);

		YQZZResourceHonor honor = getParent().getResourceHonorStat(resType);
		honor.setResourceCount(honor.getResourceCount() + collected);
		honor.setPlayerHonor(honor.getPlayerHonor() + yqcfg.getPlayerScore() * GsConst.EFF_PER * collected);
		honor.setGuildHonor(honor.getGuildHonor() + yqcfg.getAllianceScore() * GsConst.EFF_PER * collected);
		honor.setNationHonor(honor.getNationHonor() + yqcfg.getNationScore() * GsConst.EFF_PER * collected);

		WorldMarchService.getInstance().resetCollectResource(getMarchEntity(), collected, resType);
		// WorldMarchService.getInstance().calcExtraDrop2(this);
		// WorldMarchService.getInstance().calcExtraDrop3(this);

		AwardItems items = getMarchEntity().getAwardItems();
		AwardCfg acfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, yqcfg.getAwardId());
		if (acfg != null && yqcfg.getAwardCount() > 0) {
			int count = (int) (collected * GsConst.EFF_PER * (GsConst.EFF_RATE + awardPct()) / yqcfg.getAwardCount());
			for (int i = 0; i < count; i++) {
				items.addItemInfos(acfg.getRandomAward().getAwardItems());
			}
		}

		if (items != null && items.getAwardItems().size() > 0) {

			// 发邮件
			CollectMail.Builder builder = MailBuilderUtil.createCollectMail(getMarchEntity(), MailId.COLLECT_SUCC_VALUE, true);

			CollectMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(getMarchEntity().getPlayerId()).setMailId(MailId.COLLECT_SUCC).addContents(builder)
							.setDuntype(DungeonMailType.YQZZ).build());

			// 计算石油转化作用号(注意会改变award，在发奖前调用)
			player.calcOilChangeEff(items);

			items.rewardTakeAffectAndPush(player, Action.WORLD_COLLECT_RES, false); // 发奖
		}

		// 额外奖励发放(366作用号采集资源每X秒给一份奖励)
		if (!HawkOSOperator.isEmptyString(this.getMarchEntity().getAwardExtraStr())) {
			AwardItems extraAward = AwardItems.valueOf(this.getMarchEntity().getAwardExtraStr());
			extraAward.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_COLLECT_RETURN, false);
			WorldMarchService.logger.info("collect res extraAward push, playerId:{}, award:{}", player.getId(), extraAward.toString());
		}

		// tlog日志统计
		try {
			WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, Integer.parseInt(getMarchEntity().getTargetId()));
			if (cfg != null) {
				long resourceNum = 0;
				AwardItems awardItems = getMarchEntity().getAwardItems();
				for (long count : awardItems.getAwardItemsCount().values()) {
					resourceNum += count;
				}
				LogUtil.logWorldCollect(player, cfg.getId(), cfg.getResType(), cfg.getLevel(), resourceNum, HawkTime.getMillisecond() - getMarchEntity().getResStartTime());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	private int awardPct() {
		// YQZZ_629 = 629; //月球之巅矿石采集获得国家材料增加 629
		// YQZZ_630 = 630; //月球之巅石油采集获得国家材料增加 630
		// YQZZ_631 = 631; //月球之巅合金采集获得国家材料增加 631
		// YQZZ_632 = 632; //月球之巅铀矿采集获得国家材料增加 632
		switch (resType) {
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			return getParent().getEffect().getEffVal(EffType.YQZZ_629);
		case PlayerAttr.OIL_UNSAFE_VALUE:
			return getParent().getEffect().getEffVal(EffType.YQZZ_630);
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			return getParent().getEffect().getEffVal(EffType.YQZZ_631);
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			return getParent().getEffect().getEffVal(EffType.YQZZ_632);
		default:
			break;
		}
		return 0;
	}

	public int getCollected() {
		return collected;
	}

}
