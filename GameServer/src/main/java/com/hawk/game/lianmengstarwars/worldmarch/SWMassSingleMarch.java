package com.hawk.game.lianmengstarwars.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.entity.SWMarchEntity;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.submarch.ISWMassMarch;
import com.hawk.game.lianmengstarwars.worldmarch.submarch.ISWReportPushMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;

/** 集结攻打单人基地
 * 
 * @author lwt
 * @date 2019年1月2日 */
public class SWMassSingleMarch extends ISWMassMarch implements ISWReportPushMarch {

	public SWMassSingleMarch(ISWPlayer parent) {
		super(parent);
	}

	@Override
	public void heartBeats() {
		// 集结等待中
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			waitingStatusMarchProcess();
			return;
		}
		// 当前时间
		long currTime = getParent().getParent().getCurTimeMil();
		// 行军或者回程时间未结束
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
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MASS;
	}

	public WorldMarchType getJoinMassType() {
		return WorldMarchType.MASS_JOIN;
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
	public void onMarchReach(Player parent) {
		// 行军
		SWMarchEntity atkLeaderMarch = getMarchEntity();
		Set<ISWWorldMarch> massJoinMarchs = getMassJoinMarchs(true);
		// 目标点
		ISWWorldPoint targetPoint = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		if (Objects.isNull(targetPoint)) {// 烧出去了
			onMarchReturn(atkLeaderMarch.getTerminalId(), getParent().getPointId(), this.getArmys());
			// 队员行军返回
			for (ISWWorldMarch tmpMarch : massJoinMarchs) {
				tmpMarch.onMarchReturn(atkLeaderMarch.getTerminalId(), tmpMarch.getParent().getPointId(), tmpMarch.getArmys());
			}
			return;
		}
		// 防守方玩家
		ISWPlayer defplayer = (ISWPlayer) targetPoint;

		/********************** 战斗数据组装及战斗 ***************************/
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(getParent());

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);

		// 填充参与集结信息
		for (ISWWorldMarch massJoinMarch : massJoinMarchs) {
			atkPlayers.add(massJoinMarch.getParent());
			atkMarchs.add(massJoinMarch);
		}

		// 防御方玩家
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(defplayer);

		// 防御方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		// 防守方援军
		List<ISWWorldMarch> helpMarchList = defplayer.assisReachMarches();
		for (ISWWorldMarch iWorldMarch : helpMarchList) {
			defMarchs.add(iWorldMarch);
			defPlayers.add(iWorldMarch.getParent());
		}

		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(
				BattleConst.BattleType.ATTACK_CITY,
				targetPoint.getPointId(), atkPlayers, defPlayers, atkMarchs,
				defMarchs, BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setSWMail(getParent().getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.SW);
		FightMailService.getInstance().sendFightMail(targetPoint.getPointType().getNumber(), battleIncome, battleOutcome, null);
		// 战斗胜利
		boolean isAtkWin = battleOutcome.isAtkWin();

		/********************** 战斗数据组装及战斗 ***************************/

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		List<ArmyInfo> atkArmyList = WorldUtil.mergAllPlayerArmy(atkArmyLeftMap);

		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);

		// 防御者战后部队结算
		onArmyBack(defplayer, battleOutcome.getBattleArmyMapDef().get(defplayer.getId()), Collections.emptyList(), 0, null);
		updateDefMarchAfterWar(helpMarchList, defArmyLeftMap);

		// 防守方失败了 , 城墙着火
		if (isAtkWin) {
			defplayer.setOnFireEndTime(GameUtil.getOnFireEndTime(0));
		}
		getParent().getParent().worldPointUpdate(defplayer);
		// 播放战斗动画
		this.sendBattleResultInfo(isAtkWin, atkArmyList, defArmyList);

		onMarchReturn(targetPoint.getPointId(), getParent().getPointId(), atkArmyLeftMap.get(atkLeaderMarch.getPlayerId()));
		// 队员行军返回
		for (ISWWorldMarch tmpMarch : massJoinMarchs) {
			tmpMarch.onMarchReturn(targetPoint.getPointId(), tmpMarch.getParent().getPointId(), atkArmyLeftMap.get(tmpMarch.getPlayerId()));
		}
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	/** 获取被动方联盟战争界面信息 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		// 队长id
		String leaderId = this.getMarchEntity().getTargetId();
		ISWPlayer leader = SWRoomManager.getInstance().makesurePlayer(leaderId);
		// 队长位置
		int[] pos = leader.getPos();
		// 队长
		builder.setPointType(WorldPointType.PLAYER);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setLeaderArmyLimit(leader.getMaxAssistSoldier());
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = leader.getGuildTag();
			builder.setGuildTag(guildTag);
		}

		// 队长信息
		GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
		leaderInfo.setPlayerId(leader.getId());
		leaderInfo.setPlayerName(leader.getName());
		leaderInfo.setIconId(leader.getIcon());
		leaderInfo.setPfIcon(leader.getPfIcon());
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING);
		builder.setLeaderMarch(leaderInfo);

		// 已经到达的士兵数量
		int reachArmyCount = 0;
		List<ISWWorldMarch> assistandMarchs = getParent().getParent().getPointMarches(
				leader.getPointId(),
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE);
		for (ISWWorldMarch assistandMarch : assistandMarchs) {
			builder.addJoinMarchs(assistandMarch.getGuildWarSingleInfo());
			reachArmyCount += WorldUtil.calcSoldierCnt(assistandMarch.getMarchEntity().getArmys());
		}
		builder.setCityLevel(leader.getCityLv());
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}

	@Override
	public boolean isMassMarch() {
		return true;
	}

	@Override
	public boolean isEvident() {
		return ISWReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<ISWWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(),
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE);
		Set<String> result = new HashSet<>();
		result.add(getMarchEntity().getTargetId());
		for (ISWWorldMarch march : helpMarchList) {
			result.add(march.getPlayerId());
		}
		return result;
	}

}
