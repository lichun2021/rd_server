package com.hawk.game.lianmengstarwars.worldpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterMarch;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.SWHoldRec;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 
 *
 */
public abstract class ISWBuilding implements ISWWorldPoint {
	private final SWBattleRoom parent;
	private SWBuildState state = SWBuildState.ZHONG_LI;
	/** 占领开始 */
	private long zhanLingKaiShi;
	private long zhanLingJieShu;
	private int index; // 刷的序号
	private int x;
	private int y;
	private boolean firstControl = true;
	private long lastTick;
	private int aoiObjId = 0;
	private ISWWorldMarch leaderMarch;
	private List<SWHoldRec.Builder> holdRecList = new ArrayList<>();

	private String lastControlGuild = "";
	private long lastControlTime;

	/** 联盟控制时间 */
	private AtomicLongMap<String> controlGuildTimeMap = AtomicLongMap.create();
	/** 联盟控制取得积分 */
	private Map<String, Double> controlGuildHonorMap = new HashMap<>();

	public ISWBuilding(SWBattleRoom parent) {
		this.parent = parent;
	}

	public boolean underGuildControl(String guildId) {
		return getState() == SWBuildState.ZHAN_LING && Objects.equals(this.getGuildId(), guildId);
	}

	@Override
	public boolean onTick() {
		long timePass = getParent().getCurTimeMil() - lastTick;
		if (timePass < 1000) {
			return true;
		}
		lastTick = getParent().getCurTimeMil();
		
		if(state != SWBuildState.ZHAN_LING){
			lastControlGuild = "";
			lastControlTime = 0;
		}
		
		List<ISWWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		if (stayMarches.isEmpty()) {
			if (state != SWBuildState.ZHONG_LI) {
				state = SWBuildState.ZHONG_LI;
				getParent().worldPointUpdate(this);
			}
			return true;
		}

		if (leaderMarch == null || !stayMarches.contains(leaderMarch)) {
			leaderMarch = stayMarches.get(0);
			getParent().worldPointUpdate(this);
		}

		String cguildId = getGuildId();
		if (state != SWBuildState.ZHONG_LI && !Objects.equals(cguildId, leaderMarch.getParent().getGuildId())) {
			state = SWBuildState.ZHONG_LI;
			getParent().worldPointUpdate(this);
			return true;
		}

		// 有行军
		if (state == SWBuildState.ZHAN_LING) {
			controlGuildTimeMap.addAndGet(cguildId, timePass); // 联盟占领时间++
			controlGuildHonorMap.merge(cguildId, getGuildHonorPerSecond(), (v1, v2) -> v1 + v2);
			lastControlGuild = cguildId;
			lastControlTime += timePass;
			for (ISWWorldMarch march : stayMarches) {
				// 个人积分++ pers
				double pers = Math.min(getPlayerHonorPerSecond() * 1D / getCollectArmyMin() * march.getMarchEntity().getArmyCount(), getPlayerHonorPerSecond());
				march.getParent().incrementPlayerHonor(pers);
			}
			//System.out.println(getPointType() + "累计占领"+ lastControlGuild + "   " + lastControlTime + "cguildId  : "+ cguildId);
			return true;
		}

		if (state == SWBuildState.ZHONG_LI) {
			state = SWBuildState.ZHAN_LING_ZHONG;
			zhanLingKaiShi = getParent().getCurTimeMil();

			int controlCountDown = getControlCountDown();

			zhanLingJieShu = zhanLingKaiShi + controlCountDown * 1000;
			getParent().worldPointUpdate(this);

			// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.SPECIAL_BROADCAST).setKey(NoticeCfgId.SW_176)
			// .addParms(getX())
			// .addParms(getY())
			// .addParms(cguildId)
			// .addParms(getGuildTag())
			// .addParms(getPlayerName())
			// .build();
			// getParent().addWorldBroadcastMsg(parames);
			return true;
		}

		if (state == SWBuildState.ZHAN_LING_ZHONG) {
			if (getParent().getCurTimeMil() > zhanLingJieShu) {
				state = SWBuildState.ZHAN_LING;
				if (firstControl) {// 首控
					firstControl = false;
					controlGuildHonorMap.put(cguildId, getFirstControlGuildHonor());
					for (ISWWorldMarch march : stayMarches) {
						// 个人积分++
						march.getParent().incrementPlayerHonor(getFirstControlPlayerHonor());
					}
				}

				SWHoldRec.Builder hrec = SWHoldRec.newBuilder().setHoldTime(zhanLingJieShu).setPlayerName(getPlayerName()).setGuildTag(getGuildTag()).setPtype(getPointType()).setX(x)
						.setY(y)
						.setGuildId(cguildId)
						.setServerId(getGuildServerId());
				holdRecList.add(hrec);
				getParent().worldPointUpdate(this);

				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.SW_176)
						.addParms(getX())
						.addParms(getY())
						.addParms(cguildId)
						.addParms(getGuildTag())
						.addParms(getPlayerName())
						.addParms(getParent().getExtParm().getWarType().getNumber())
						.build();
				getParent().addWorldBroadcastMsg(parames);
				return true;
			}
		}

		return true;
	}

	/** 占领倒计时 /秒 */
	public abstract int getControlCountDown();

	public abstract double getGuildHonorPerSecond();

	public abstract double getPlayerHonorPerSecond();

	public abstract double getFirstControlGuildHonor();

	public abstract double getFirstControlPlayerHonor();

	public abstract int getProtectTime();

	public abstract int getCollectArmyMin();

	@Override
	public WorldPointPB.Builder toBuilder(ISWPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setPlayerId(getPlayerId());
		builder.setPlayerName(getPlayerName());
		builder.setGuildId(getGuildId());
		builder.setServerId(getGuildServerId());
		builder.setManorState(state.intValue()); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
		List<ISWWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ISWWorldMarch march : stayMarches) {
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				break;
			}
		}
		if (getLeaderMarch() != null) {
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}

		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());
		if (state != SWBuildState.ZHONG_LI) {
			builder.setManorBuildTime(zhanLingKaiShi); // 占领开始时间
			builder.setManorComTime(zhanLingJieShu); // 占领结束时间
		}

		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ISWPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		if (getLeaderMarch() != null) {
			ISWPlayer leader = leaderMarch.getParent();
			builder.setPlayerId(leader.getId());
			builder.setPlayerName(leader.getName());
			builder.setPlayerIcon(leader.getIcon());
			builder.setPlayerPfIcon(leader.getPfIcon());
			builder.setGuildId(leader.getGuildId());
			builder.setServerId(getGuildServerId());
			builder.setGuildTag(leader.getGuildTag());
			builder.setGuildFlag(leader.getGuildFlag());
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}
		builder.setManorState(state.intValue());// /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
		boolean hasMarchStop = false;
		List<ISWWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ISWWorldMarch march : stayMarches) {
			if (!march.getPlayerId().equals(viewer.getId())) {
				continue;
			}
			hasMarchStop = true;
		}
		builder.setHasMarchStop(hasMarchStop);

		if (state != SWBuildState.ZHONG_LI) {
			builder.setManorBuildTime(zhanLingKaiShi);// 占领开始时间
			builder.setManorComTime(zhanLingJieShu);// 占领结束时间
		}
		for(SWHoldRec.Builder hrec : holdRecList){
			hrec.setTotalHold(controlGuildTimeMap.get(hrec.getGuildId()));
			//System.out.println("guildId: "+ hrec.getGuildId() +"	控制时间"+  controlGuildTimeMap.get(hrec.getGuildId())+" RECC");
			builder.addSwHoldRec(hrec);
		}
		builder.setSwLastControlTime(lastControlTime);
		//System.out.println(lastControlTime);
		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
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
		ISWPlayer leader = getLeaderMarch().getParent();
		String leaderId = leader.getId();
		// 队长
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = leader.getGuildTag();
			builder.setGuildTag(guildTag);
		}

		// 已经到达的士兵数量
		int reachArmyCount = 0;
		List<ISWWorldMarch> assistandMarchs = getParent().getPointMarches(this.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ISWWorldMarch stayMarch : assistandMarchs) {
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

	public void onMarchReach(ISWWorldMarch leaderMarch) {
		ISWPlayer player = leaderMarch.getParent();
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<ISWWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (ISWWorldMarch iWorldMarch : atkMarchList) {
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
		List<ISWWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		ISWWorldMarch enemyLeadmarch = getLeaderMarch();
		if (Objects.nonNull(enemyLeadmarch)) {// 队长排第一
			defMarchList.remove(enemyLeadmarch);
			defMarchList.add(0, enemyLeadmarch);
		}
		for (ISWWorldMarch iWorldMarch : defMarchList) {
			defMarchs.add(iWorldMarch);
			defPlayers.add(iWorldMarch.getParent());
		}

		if (defMarchs.isEmpty()) {
			state = SWBuildState.ZHONG_LI;
			this.leaderMarch = leaderMarch;
			for (ISWWorldMarch iWorldMarch : atkMarchList) {
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
			// for (ISWWorldMarch iWorldMarch : atkMarchList) {
			// iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			// iWorldMarch.updateMarch();
			// }
			return;
		}

		BattleType btype = BattleConst.BattleType.ATTACK_PRESIDENT;
		if(this instanceof SWCommandCenter){
			btype = BattleConst.BattleType.ATTACK_PRESIDENT_TOWER;
		}
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(btype, this.getPointId(), atkPlayers, defPlayers, atkMarchs,
				defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setSWMail(getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.SW);
		battleOutcome.setSwWarType(getParent().getExtParm().getWarType());
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

		for (ISWWorldMarch iWorldMarch : atkMarchList) {
			iWorldMarch.notifyMarchEvent(MarchEvent.MARCH_DELETE);
		}

		leaderMarch.updateDefMarchAfterWar(atkMarchList, atkArmyLeftMap);
		leaderMarch.updateDefMarchAfterWar(defMarchList, defArmyLeftMap);

		List<ISWWorldMarch> winMarches = null;
		List<ISWWorldMarch> losMarches = null;

		if (isAtkWin) {
			state = SWBuildState.ZHONG_LI;
			this.leaderMarch = leaderMarch;
			winMarches = atkMarchList;
			losMarches = defMarchList;
		} else {
			winMarches = defMarchList;
			losMarches = atkMarchList;
		}

		for (ISWWorldMarch atkM : winMarches) {
			atkM.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			atkM.updateMarch();
		}
		for (ISWWorldMarch defM : losMarches) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}

		FightMailService.getInstance().sendFightMail(this.getPointType().getNumber(), battleIncome, battleOutcome, null);
	}

	private boolean assitenceWarPoint(List<ISWWorldMarch> atkMarchList, List<ISWWorldMarch> stayMarchList) {
		// 队长
		ISWPlayer leader = leaderMarch.getParent();

		// 8.21新增：检查当前自己的行军，如果行军中已有英雄，则将自己队列中的英雄撤回
		for (ISWWorldMarch worldMarch : atkMarchList) {
			if (worldMarch.getMarchEntity().getHeroIdList().isEmpty()) {// 如果当前行军中没有英雄直接跳过
				continue;
			}
			// 如果有英雄，则判断当前据点停留的行军有没有自己的
			for (ISWWorldMarch stayMarch : stayMarchList) {
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
		for (ISWWorldMarch worldMarch : stayMarchList) {
			ppList.add(worldMarch.getMarchEntity());
		}

		int curPopulationCnt = WorldUtil.calcMarchsSoldierCnt(ppList); // 已驻扎士兵人口
		// 剩余人口<0部队返回
		int remainArmyPopu = maxMassSoldierNum - curPopulationCnt;
		if (remainArmyPopu <= 0) {
			return returnMarchList(atkMarchList);
		}

		// 优先加入已在玩家
		for (ISWWorldMarch stayMarch : stayMarchList) {
			Iterator<ISWWorldMarch> it = atkMarchList.iterator();
			while (it.hasNext()) {
				ISWWorldMarch massMarch = it.next();
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
					ISWWorldMarch back = getParent().startMarch(massMarch.getParent(), this, massMarch.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
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
		Iterator<ISWWorldMarch> it = atkMarchList.iterator();

		while (it.hasNext()) {
			ISWWorldMarch march = it.next();
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
				ISWWorldMarch back = getParent().startMarch(march.getParent(), this, march.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
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

	private boolean returnMarchList(List<ISWWorldMarch> atkMarchList) {
		for (ISWWorldMarch defM : atkMarchList) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}
		return true;
	}

	/** 遣返 */
	public boolean repatriateMarch(ISWPlayer comdPlayer, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!Objects.equals(getGuildId(), comdPlayer.getGuildId())) {
			return false;
		}

		// 队长
		ISWPlayer leader = leaderMarch.getParent();

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<ISWWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ISWWorldMarch iWorldMarch : stayMarches) {
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
	public boolean cheangeQuarterLeader(ISWPlayer comdPlayer, String targetPlayerId) {

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

		List<ISWWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (ISWWorldMarch march : stayMarches) {
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			leaderMarch = march;
			getParent().worldPointUpdate(this);
			break;
		}
		return true;
	}

	public void syncQuarterInfo(ISWPlayer player) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		if (!player.hasGuild() || !player.getGuildId().equals(getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
			return;
		}

		List<ISWWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		ISWWorldMarch leaderMarch = getLeaderMarch();
		if (leaderMarch != null) {
			builder.addQuarterMarch(getSuperWeaponQuarterMarch(leaderMarch));
			builder.setMassSoldierNum(leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getParent()));
		}
		for (ISWWorldMarch march : defMarchList) {
			if (march != leaderMarch) {
				builder.addQuarterMarch(getSuperWeaponQuarterMarch(march));
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
	}

	public SuperWeaponQuarterMarch.Builder getSuperWeaponQuarterMarch(ISWWorldMarch march) {
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

	public SWBuildState getState() {
		return state;
	}

	public void setState(SWBuildState state) {
		this.state = state;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public SWBattleRoom getParent() {
		return parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String getGuildId() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getGuildId();
	}

	public String getGuildName() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getGuildName();
	}

	public String getGuildTag() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getGuildTag();
	}
	
	public String getGuildServerId() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getMainServerId();
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

	public int getGuildFlag() {
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

	public ISWWorldMarch getLeaderMarch() {
		if (leaderMarch == null || leaderMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			List<ISWWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			if (stayMarches.isEmpty()) {
				leaderMarch = null;
				return null;
			}

			leaderMarch = stayMarches.get(0);
		}
		return leaderMarch;
	}

	public void setLeaderMarch(ISWWorldMarch leaderMarch) {
		this.leaderMarch = leaderMarch;
	}

	public String getLastControlGuild() {
		return lastControlGuild;
	}

	public long getLastControlTime() {
		return lastControlTime;
	}

	@Override
	public int getAoiObjId() {
		return aoiObjId;
	}

	@Override
	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}
	
}
