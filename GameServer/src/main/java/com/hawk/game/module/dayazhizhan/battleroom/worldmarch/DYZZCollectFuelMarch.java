package com.hawk.game.module.dayazhizhan.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.entity.DYZZMarchEntity;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZReportPushMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZFuelBank;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.march.IWorldMarch;

public class DYZZCollectFuelMarch extends IDYZZWorldMarch implements IDYZZReportPushMarch, IDYZZPassiveAlarmTriggerMarch {
	private long lastCollect;
	private double accumulation; // 累积值, 每超过一个就算做采集成功
	private IDYZZFuelBank resPoint;

	private long resEndTime;
	private long resStartTime;
	double lastcollectSpeed;

	public DYZZCollectFuelMarch(IDYZZPlayer parent) {
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
		result.setTargetId("79");
		if (resEndTime > 0) {
			result.setResEndTime(resEndTime);
		}

		if (resStartTime > 0) {
			result.setResStartTime(resStartTime);
		}
		return result;
	}

	private void calResCollectTime() {
		if (resPoint == null) {
			resEndTime = 0;
			resStartTime = 0;
			return;
		}
		lastcollectSpeed = getCollectSpeed();
		if (resStartTime == 0) {
			resStartTime = getParent().getParent().getCurTimeMil();
		}
		resEndTime = getParent().getParent().getCurTimeMil() + (long) (Math.ceil(resPoint.getRemainResNum() * 1000) / getCollectSpeed()) + 1000;
	}

	/**已经采了几分钟*/
	private int hasCollectMins() {
		if (resStartTime == 0) {
			return 0;
		}
		return (int) ((getParent().getParent().getCurTimeMil() - resStartTime) / 60000);
	}

	@Override
	public Set<String> attackReportRecipients() {
		IDYZZWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof IDYZZFuelBank)) {
			return Collections.emptySet();
		}
		IDYZZFuelBank fub = (IDYZZFuelBank) ttt;
		if (fub.getMarch() != null && fub.getMarch().getParent().getCamp() != getParent().getCamp()) {
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
			}
			resPoint.setMarch(this);
			doCollectRes(resPoint);
			if (resPoint.getRemainResNum() == 0) {
				resPoint.setMarch(null);
				this.onMarchCallback();
			}
			return;
		}

		// 当前时间
		long currTime = getParent().getParent().getCurTimeMil();
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
		DYZZMarchEntity march = getMarchEntity();
		IDYZZWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof IDYZZFuelBank)) {
			this.onMarchCallback();
			return;
		}
		IDYZZFuelBank point = (IDYZZFuelBank) ttt;
		DYZZCollectFuelMarch oldMarch = point.getMarch();
		if (oldMarch == null) {
			resPoint = point;
			resPoint.setMarch(this);
			march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE);
			notifyMarchEvent(MarchEvent.MARCH_DELETE);
			calResCollectTime();
			updateMarch();
			getParent().getParent().worldPointUpdate(point);
			return;
		}
		// 点已经被自己占领
		IDYZZPlayer defplayer = oldMarch.getParent();
		if (defplayer == getParent() || getParent().getCamp() == defplayer.getCamp()) {
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
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_TBLY_RES, point.getPointId(), atkPlayers, defPlayers, atkMarchs,
				defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setDYZZMail(getParent().getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.DYZZ);
		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();
		/********* 击杀/击伤部队数据 *********/
		getParent().getParent().calcKillAndHurtPower(battleOutcome, atkPlayers, defPlayers);
		/********* 击杀/击伤部队数据 *********/

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
		getParent().getParent().worldPointUpdate(point);

		getParent().setLastAttacker(defplayer);

		defplayer.setLastAttacker(getParent());

	}

	private void doCollectRes(IDYZZFuelBank resPoint) {
		long now = getParent().getParent().getCurTimeMil();
		if (lastCollect == 0) {
			lastCollect = now;
		}
		if (getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			return;
		}
		long passTime = now - lastCollect;
		if (passTime < 1000) { // 1秒一算
			return;
		}

		accumulation += lastcollectSpeed * passTime / 1000; // 增加一秒采集量

		if (accumulation > 1) {
			int col = (int) accumulation;
			col = resPoint.doCollect(col);
			getParent().incrementCollectHonor(col);
			if (getParent().getCamp() == DYZZCAMP.A) {
				getParent().getParent().campAOrder += col;
			} else {
				getParent().getParent().campBOrder += col;
			}
			accumulation -= col;
		}

		lastCollect = now;

		if (lastcollectSpeed != getCollectSpeed()) {
			calResCollectTime();
			updateMarch();
			return;
		}
	}

	@Override
	public void onMarchCallback() {
		if (resPoint != null) {
			resPoint.setMarch(null);
			getParent().getParent().worldPointUpdate(resPoint);
		}
		super.onMarchCallback();
	}

	public double getCollectSpeed() {
		if (Objects.isNull(resPoint)) {
			return 0;
		}
		ImmutableList<Double> collectSpeed = resPoint.getCollectSpeed();
		int index = Math.min(hasCollectMins(), collectSpeed.size() - 1);
		double speed = collectSpeed.get(index);
		speed = speed * GsConst.EFF_PER * (GsConst.EFF_RATE + getParent().getEffect().getEffVal(EffType.DYZZ_9002));
		return speed;
	}

	public long getLastCollect() {
		return lastCollect;
	}

	private void rePushPointReport() {
		// 删除行军报告
		removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void pullAttackReport() {
		List<IDYZZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IDYZZWorldMarch march : marchList) {
			if (march instanceof IDYZZReportPushMarch && march != this) {
				((IDYZZReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<IDYZZWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (IDYZZWorldMarch march : marchList) {
			if (march instanceof IDYZZReportPushMarch && march != this) {
				((IDYZZReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

	@Override
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}
}
