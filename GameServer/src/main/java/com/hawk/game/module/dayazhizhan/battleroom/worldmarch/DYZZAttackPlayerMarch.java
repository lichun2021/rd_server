package com.hawk.game.module.dayazhizhan.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZReportPushMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.skill.talent.ITalentSkill;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;

public class DYZZAttackPlayerMarch extends IDYZZWorldMarch implements IDYZZReportPushMarch {

	public DYZZAttackPlayerMarch(IDYZZPlayer parent) {
		super(parent);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ATTACK_PLAYER;
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	@Override
	public void heartBeats() {
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
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReach(Player parent) {
		// 删除行军报告
		removeAttackReport();
		IDYZZWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		// 路点为空
		if (point == null || !(point instanceof IDYZZPlayer)) {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), getArmys());
			return;
		}
		// 去程到达目标点，变成停留状态
		getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE);
		getMarchEntity().setReachTime(getMarchEntity().getEndTime());

		IDYZZPlayer defplayer = (IDYZZPlayer) point;

		/************************* fight!!!!!!!!!!!! */
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(getParent());

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(defplayer);

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		// 防守方援军
		List<IDYZZWorldMarch> helpMarchList = helpMarches();
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		for (IDYZZWorldMarch iWorldMarch : helpMarchList) {
			defMarchs.add(iWorldMarch);
			defPlayers.add(iWorldMarch.getParent());
		}
		// 判断触发技能
		BattleSkillType skillType = getTouchSkillType(getPlayer(), defplayer, getMarchEntity());
		if (skillType.equals(BattleSkillType.BATTLE_SKILL_DUEL)) {
			helpMarchList = new ArrayList<>();
		}
		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_CITY,
				point.getPointId(), atkPlayers, defPlayers, atkMarchs, defMarchs, BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setDYZZMail(getParent().getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.DYZZ);
		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		List<ArmyInfo> atkArmyLeft = atkArmyLeftMap.get(getParent().getId());

		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);

		// 防御者战后部队结算
		onArmyBack(defplayer, battleOutcome.getBattleArmyMapDef().get(defplayer.getId()), Collections.emptyList(), 0, null);
		updateDefMarchAfterWar(helpMarchList, defArmyLeftMap);

		if (isAtkWin) {
			defplayer.setOnFireEndTime(GameUtil.getOnFireEndTime(0));
		}

		FightMailService.getInstance().sendFightMail(point.getPointType().getNumber(), battleIncome, battleOutcome, null);

		/************************* fight!!!!!!!!!!!! */
		getParent().getParent().worldPointUpdate(defplayer);
		// 发送战斗结果给前台播放动画
		this.sendBattleResultInfo(isAtkWin, atkArmyLeft, defArmyList);

		onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), atkArmyLeft);

		// 刷新战力
		// refreshPowerAfterWar(atkPlayers, defPlayers);
		getParent().setLastAttacker(defplayer);
		for (Player defer : defPlayers) {
			((IDYZZPlayer) defer).setLastAttacker(getParent());
		}
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

	/** 触发技能类型
	 * 
	 * @param atkPlayer攻击方玩家
	 * @param defPlayer防守方玩家
	 * @param atkMarch攻击方行军
	 * @return */
	private BattleSkillType getTouchSkillType(Player atkPlayer, Player defPlayer, WorldMarch atkMarch) {
		// 触发技能判断: 救援
		BattleSkillType skillType = BattleSkillType.BATTLE_SKILL_NONE;
		ITalentSkill talentSkill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10103);
		if (talentSkill.touchSkill(atkPlayer, null)) {
			skillType = BattleSkillType.BATTLE_SKILL_LIFESAVING;
		}
		// 触发技能判断: 决斗
		talentSkill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10104);
		if (talentSkill.touchSkill(atkPlayer, defPlayer.getId(),
				ArmyService.getInstance().getArmysCount(atkMarch.getArmys()))) {
			skillType = BattleSkillType.BATTLE_SKILL_DUEL;
		}
		return skillType;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<IDYZZWorldMarch> helpMarchList = helpMarches();
		Set<String> result = new HashSet<>();
		result.add(getMarchEntity().getTargetId());
		for (IDYZZWorldMarch march : helpMarchList) {
			result.add(march.getPlayerId());
		}
		return result;
	}

	private List<IDYZZWorldMarch> helpMarches() {
		return getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(),
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE);
	}
}
