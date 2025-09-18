package com.hawk.game.module.dayazhizhan.battleroom.worldpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailStatus;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.DYZZ.PBDYZZNuclearSync;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.HPListMailResp;
import com.hawk.game.protocol.Mail.HPTypeMail;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterMarch;
import com.hawk.game.protocol.World.DYZZAtkBaseRec;
import com.hawk.game.protocol.World.DYZZBaseDefRec;
import com.hawk.game.protocol.World.DYZZHoldRec;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 
 *
 */
public abstract class IDYZZBuilding implements IDYZZWorldPoint {
	private final DYZZBattleRoom parent;
	private int index; // 刷的序号
	private int x;
	private int y;
	private int redis;
	private boolean firstControl = true;
	private long lastTick;
	private IDYZZWorldMarch leaderMarch;
	private List<String> defMailList = new LinkedList<>();
	private List<String> spyMailList = new LinkedList<>();
	private List<DYZZHoldRec> holdRecList = new ArrayList<>();
	private List<DYZZAtkBaseRec> nuclearShotRecList = new ArrayList<>();
	private List<DYZZBaseDefRec> nuclearDefRecList = new ArrayList<>();
	/** 联盟控制时间 */
	private AtomicLongMap<String> controlGuildTimeMap = AtomicLongMap.create();

	public IDYZZBuilding(DYZZBattleRoom parent) {
		this.parent = parent;
	}

	public boolean underGuildControl(String guildId) {
		return getState() == DYZZBuildState.ZHAN_LING && Objects.equals(this.getGuildId(), guildId);
	}

	@Override
	public boolean onTick() {

		return true;
	}

	/** 占领倒计时 /秒 */
	public abstract int getControlCountDown();

	public final int controlTimeMil(IDYZZPlayer leader) {
		int controlCountDown = getControlCountDown() * 1000;
		controlCountDown = (int) (controlCountDown * (1 - leader.getEffect().getEffVal(EffType.DYZZ_9001) * GsConst.EFF_PER));
		return controlCountDown;
	}

	public int getControlBuff(EffType effType) {
		return 0;
	}

	public void onPlayerLogin(IDYZZPlayer gamer) {
	}

	public void anchorJoin(IDYZZPlayer gamer) {
	}
	
	/** 导弹攻击记录*/
	public void incNuclearShotRec(PBDYZZNuclearSync sendRecord){
		DYZZAtkBaseRec.Builder builder = DYZZAtkBaseRec.newBuilder();
		builder.setFromX(sendRecord.getFromX());
		builder.setFromY(sendRecord.getFromY());
		builder.setToX(sendRecord.getTarX());
		builder.setToY(sendRecord.getTarY());
		builder.setType(sendRecord.getShotType());
		builder.setHurt(sendRecord.getAtkVal());
		builder.setHurtTime(getParent().getCurTimeMil());
		this.nuclearShotRecList.add(builder.build());
	}
	
	/** 导弹受到攻击记录*/
	public void incNuclearDefRec(PBDYZZNuclearSync sendRecord){
		DYZZBaseDefRec.Builder builder = DYZZBaseDefRec.newBuilder();
		builder.setFromX(sendRecord.getFromX());
		builder.setFromY(sendRecord.getFromY());
		builder.setToX(sendRecord.getTarX());
		builder.setToY(sendRecord.getTarY());
		builder.setType(sendRecord.getShotType());
		builder.setHurt(sendRecord.getAtkVal());
		builder.setHurtTime(getParent().getCurTimeMil());
		this.nuclearDefRecList.add(builder.build());
	}
	
	/**
	 * 增加一条占领记录
	 */
	public void incHoldrec() {
		// 占领记录
		if (Objects.isNull(leaderMarch)) {
			return;
		}
		IDYZZPlayer leader = leaderMarch.getParent();
		DYZZHoldRec hrec = DYZZHoldRec.newBuilder()
				.setHoldTime(getParent().getCurTimeMil())
				.setPlayerName(leader.getName())
				.setGuildTag(leader.getGuildTag())
				.setPtype(getPointType())
				.setX(getX())
				.setY(getY())
				.setFlagView(leader.getCamp().intValue())
				.setBuildState(getState().getNumber())
				.build();
		getHoldRecList().add(hrec);

	}

	@Override
	public WorldPointPB.Builder toBuilder(IDYZZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setPlayerId(getPlayerId());
		builder.setPlayerName(getPlayerName());
		builder.setGuildId(getGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());

		builder.setManorState(getState().getNumber()); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
		int stayArmycount = 0;
		List<IDYZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IDYZZWorldMarch march : stayMarches) {
			if (viewer.isAnchor()) {
				stayArmycount += ArmyService.getInstance().getArmysCount(march.getArmys());
			}
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				break;
			}
		}
		if (StringUtils.isNotEmpty(getGuildId())) {
			builder.setFlagView(getParent().getGuildCamp(getGuildId()).intValue()); // 1 红 ,2 蓝
		}
		builder.setStayArmycount(stayArmycount);
		if (getLeaderMarch() != null) {
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}

		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		if (!Objects.equals(viewer.getDYZZGuildId(), getGuildId())) {// 核弹攻击
			builder.setCounterAttack(Objects.equals(viewer.getDYZZGuildId(), getParent().getNuclearReadGuild()));
		}
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		if (getLeaderMarch() != null) {
			IDYZZPlayer leader = leaderMarch.getParent();
			builder.setPlayerId(leader.getId());
			builder.setPlayerName(leader.getName());
			builder.setPlayerIcon(leader.getIcon());
			builder.setPlayerPfIcon(leader.getPfIcon());
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}
		builder.setGuildId(getGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());

		builder.setManorState(getState().getNumber());// /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
		int stayArmycount = 0;
		List<IDYZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IDYZZWorldMarch march : stayMarches) {
			if (viewer.isAnchor()) {
				stayArmycount += ArmyService.getInstance().getArmysCount(march.getArmys());
			}
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				break;
			}
		}
		if (StringUtils.isNotEmpty(getGuildId())) {
			builder.setFlagView(getParent().getGuildCamp(getGuildId()).intValue()); // 1 红 ,2 蓝
		}
		builder.setStayArmycount(stayArmycount);

		builder.addAllDyzzHoldRec(holdRecList);
		builder.addAllDyzzAtkBaseRec(nuclearShotRecList);
		builder.addAllDyzzBasedefRec(nuclearDefRecList);
		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		if (!Objects.equals(viewer.getDYZZGuildId(), getGuildId())) {// 核弹攻击
			builder.setCounterAttack(Objects.equals(viewer.getDYZZGuildId(), getParent().getNuclearReadGuild()));
		}
		return builder;
	}

	@Override
	public long getProtectedEndTime() {
		return getParent().getCollectStartTime();
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
		IDYZZPlayer leader = getLeaderMarch().getParent();
		String leaderId = leader.getId();
		// 队长
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getDYZZGuildId())) {
			String guildTag = leader.getGuildTag();
			builder.setGuildTag(guildTag);
			builder.setGuildId(leader.getDYZZGuildId());
		}

		// 已经到达的士兵数量
		int reachArmyCount = 0;
		List<IDYZZWorldMarch> assistandMarchs = getParent().getPointMarches(this.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IDYZZWorldMarch stayMarch : assistandMarchs) {
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
	public void onMarchReach(IDYZZWorldMarch leaderMarch) {
		IDYZZPlayer player = leaderMarch.getParent();
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<IDYZZWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));

		if (getParent().getCurTimeMil() < getProtectedEndTime() || getState() == DYZZBuildState.YI_CUI_HUI) {
			for (IDYZZWorldMarch defM : atkMarchList) {
				defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
			}
			return;
		}

		for (IDYZZWorldMarch iWorldMarch : atkMarchList) {
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
		List<IDYZZWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		IDYZZWorldMarch enemyLeadmarch = getLeaderMarch();
		if (Objects.nonNull(enemyLeadmarch)) {// 队长排第一
			defMarchList.remove(enemyLeadmarch);
			defMarchList.add(0, enemyLeadmarch);
		}
		for (IDYZZWorldMarch iWorldMarch : defMarchList) {
			defMarchs.add(iWorldMarch);
			defPlayers.add(iWorldMarch.getParent());
		}

		if (defMarchs.isEmpty()) {
			this.leaderMarch = leaderMarch;
			for (IDYZZWorldMarch iWorldMarch : atkMarchList) {
				iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				iWorldMarch.updateMarch();
			}
			return;
		}

		if (Objects.equals(player.getDYZZGuildId(), enemyLeadmarch.getParent().getDYZZGuildId())) { // 同阵营
			try {
				assitenceWarPoint(atkMarchList, defMarchList);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			// for (IDYZZWorldMarch iWorldMarch : atkMarchList) {
			// iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			// iWorldMarch.updateMarch();
			// }
			return;
		}

		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_TBLY_BUILD, this.getPointId(), atkPlayers, defPlayers, atkMarchs,
				defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setDYZZMail(getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.DYZZ);
		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();
		/********* 击杀/击伤部队数据 *********/
		getParent().calcKillAndHurtPower(battleOutcome, atkPlayers, defPlayers);
		/********* 击杀/击伤部队数据 *********/

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		List<ArmyInfo> atkArmyLeft = WorldUtil.mergAllPlayerArmy(atkArmyLeftMap);
		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);

		// 发送战斗结果给前台播放动画
		leaderMarch.sendBattleResultInfo(isAtkWin, atkArmyLeft, defArmyList);

		for (IDYZZWorldMarch iWorldMarch : atkMarchList) {
			iWorldMarch.notifyMarchEvent(MarchEvent.MARCH_DELETE);
		}

		leaderMarch.updateDefMarchAfterWar(atkMarchList, atkArmyLeftMap);
		leaderMarch.updateDefMarchAfterWar(defMarchList, defArmyLeftMap);

		List<IDYZZWorldMarch> winMarches = null;
		List<IDYZZWorldMarch> losMarches = null;

		if (isAtkWin) {
			this.leaderMarch = leaderMarch;
			winMarches = atkMarchList;
			losMarches = defMarchList;
		} else {
			winMarches = defMarchList;
			losMarches = atkMarchList;
		}

		for (IDYZZWorldMarch atkM : winMarches) {
			atkM.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			atkM.updateMarch();
		}
		for (IDYZZWorldMarch defM : losMarches) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}

		FightMailService.getInstance().sendFightMail(this.getPointType().getNumber(), battleIncome, battleOutcome, null);

		defMailList.add(0, battleOutcome.getDefMail(defPlayers.get(0).getId()));
	}

	public void listMail(IDYZZPlayer player, int type) {
		HPListMailResp.Builder resp = HPListMailResp.newBuilder();
		List<String> mailIds;
		if (type == 0) {
			mailIds = defMailList.stream().limit(666).collect(Collectors.toList());
		} else {
			mailIds = spyMailList.stream().limit(666).collect(Collectors.toList());
		}
		List<MailLiteInfo.Builder> list = MailService.getInstance().listMailEntity(mailIds);
		HPTypeMail.Builder bul = HPTypeMail.newBuilder().setType(5).setHasNext(false);
		for (MailLiteInfo.Builder mail : list) {
			mail.setStatus(MailStatus.READ_VALUE);
			bul.addMail(mail);
		}

		resp.addList(bul);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_ANCHOR_LIST_MAIL_S, resp));
	}

	private boolean assitenceWarPoint(List<IDYZZWorldMarch> atkMarchList, List<IDYZZWorldMarch> stayMarchList) {
		// 队长
		IDYZZPlayer leader = leaderMarch.getParent();

		// 8.21新增：检查当前自己的行军，如果行军中已有英雄，则将自己队列中的英雄撤回
		for (IDYZZWorldMarch worldMarch : atkMarchList) {
			if (worldMarch.getMarchEntity().getHeroIdList().isEmpty()) {// 如果当前行军中没有英雄直接跳过
				continue;
			}
			// 如果有英雄，则判断当前据点停留的行军有没有自己的
			for (IDYZZWorldMarch stayMarch : stayMarchList) {
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

		// // 先处理过来行军中的队长行军，队长行军处理完毕后删除
		// if (stayMarchList != null && !stayMarchList.isEmpty()) {
		// int count = stayMarchList.size();// 加上队长
		// if (count > leader.getMaxMassJoinMarchNum() + leaderMarch.getMarchEntity().getBuyItemTimes()) {
		// return returnMarchList(atkMarchList);
		// }
		// }

		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);

		List<WorldMarch> ppList = new ArrayList<>();
		for (IDYZZWorldMarch worldMarch : stayMarchList) {
			ppList.add(worldMarch.getMarchEntity());
		}

		int curPopulationCnt = WorldUtil.calcMarchsSoldierCnt(ppList); // 已驻扎士兵人口
		// 剩余人口<0部队返回
		int remainArmyPopu = maxMassSoldierNum - curPopulationCnt;
		if (remainArmyPopu <= 0) {
			return returnMarchList(atkMarchList);
		}

		// 优先加入已在玩家
		for (IDYZZWorldMarch stayMarch : stayMarchList) {
			Iterator<IDYZZWorldMarch> it = atkMarchList.iterator();
			while (it.hasNext()) {
				IDYZZWorldMarch massMarch = it.next();
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
					IDYZZWorldMarch back = getParent().startMarch(massMarch.getParent(), this, massMarch.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
					back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
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
		Iterator<IDYZZWorldMarch> it = atkMarchList.iterator();

		while (it.hasNext()) {
			IDYZZWorldMarch march = it.next();
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
				IDYZZWorldMarch back = getParent().startMarch(march.getParent(), this, march.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
				back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
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

	private boolean returnMarchList(List<IDYZZWorldMarch> atkMarchList) {
		for (IDYZZWorldMarch defM : atkMarchList) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}
		return true;
	}

	/** 遣返 */
	public boolean repatriateMarch(IDYZZPlayer comdPlayer, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!Objects.equals(getGuildId(), comdPlayer.getDYZZGuildId())) {
			return false;
		}

		// 队长
		IDYZZPlayer leader = leaderMarch.getParent();

		// R4盟主队长可以遣返
		boolean guildAuthority = true;// GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<IDYZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IDYZZWorldMarch iWorldMarch : stayMarches) {
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
	public boolean cheangeQuarterLeader(IDYZZPlayer comdPlayer, String targetPlayerId) {

		// 不是本盟的没有权限操作
		if (!comdPlayer.getDYZZGuildId().equals(getGuildId())) {
			return false;
		}

		// R4盟主队长可以遣返
		boolean guildAuthority = true;// GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leaderMarch.getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<IDYZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IDYZZWorldMarch march : stayMarches) {
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			leaderMarch = march;
			getParent().worldPointUpdate(this);
			break;
		}
		return true;
	}

	public void syncQuarterInfo(IDYZZPlayer player) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		if (!player.isAnchor() && !Objects.equals(player.getDYZZGuildId(), getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
			return;
		}

		List<IDYZZWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		IDYZZWorldMarch leaderMarch = getLeaderMarch();
		if (leaderMarch != null) {
			builder.addQuarterMarch(getSuperWeaponQuarterMarch(leaderMarch));
			builder.setMassSoldierNum(leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getParent()));
		}
		for (IDYZZWorldMarch march : defMarchList) {
			if (march != leaderMarch) {
				builder.addQuarterMarch(getSuperWeaponQuarterMarch(march));
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
	}

	public SuperWeaponQuarterMarch.Builder getSuperWeaponQuarterMarch(IDYZZWorldMarch march) {
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

	public abstract DYZZBuildState getState();

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public DYZZBattleRoom getParent() {
		return parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public abstract String getGuildTag();

	public abstract int getGuildFlag();

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

	public long getLastTick() {
		return lastTick;
	}

	public void setLastTick(long lastTick) {
		this.lastTick = lastTick;
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

	public IDYZZWorldMarch getLeaderMarch() {
		if (leaderMarch == null || leaderMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			List<IDYZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			if (stayMarches.isEmpty()) {
				leaderMarch = null;
				return null;
			}

			leaderMarch = stayMarches.get(0);
		}
		return leaderMarch;
	}

	public void setLeaderMarch(IDYZZWorldMarch leaderMarch) {
		this.leaderMarch = leaderMarch;
	}

	public List<String> getSpyMailList() {
		return spyMailList;
	}

	public void setSpyMailList(List<String> spyMailList) {
		this.spyMailList = spyMailList;
	}

	@Override
	public int getRedis() {
		return redis;
	}

	public void setRedis(int redis) {
		this.redis = redis;
	}

	public List<DYZZHoldRec> getHoldRecList() {
		return holdRecList;
	}
}
