package com.hawk.game.world.march.submarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.IFortress;
import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.WorldMarchReturnMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.GuildFormationModule;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.msg.AtkAfterPveMsg;
import com.hawk.game.msg.AtkPlayerAfterWarMsg;
import com.hawk.game.msg.DefPlayerAfterWarMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.GuildAssistant.AssistanceCallbackNotifyPB;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.guildtask.event.ResourceCollectCountEvent;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventResourceCollectCount;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.DefenderIdentity;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;

/**
 * 基础行军
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public interface BasedMarch extends IWorldMarch {

	@Override
	default boolean isPresidentMarch() {
		return false;
	}

	@Override
	default boolean isPresidentTowerMarch() {
		return false;
	}
	
	@Override
	default boolean isSuperWeaponMarch() {
		return false;
	}
	
	@Override
	default boolean isGuildSpaceMarch() {
		return false;
	}
	
	@Override
	default boolean isFortressMarch() {
		return false;
	}
	
	@Override
	default boolean isWarFlagMarch() {
		return false;
	}
	
	@Override
	default boolean isMassJoinMarch() {
		return false;
	}

	@Override
	default boolean isMassMarch() {
		return false;
	}

	@Override
	default boolean isAssistanceMarch() {
		return false;
	}

	@Override
	default boolean isManorMarch() {
		return false;
	}

	@Override
	default void heartBeats() {
		long beginTimeMs = HawkTime.getMillisecond();
		try {
			if (!isNeedCalcTickMarch()) {
				return;
			}
			
			// 当前时间
			long currTime = HawkApp.getInstance().getCurrentTime();
			
			// 具体心跳实现, 如果执行成功, 则不往下继续执行
			if (marchHeartBeats(currTime)) {
				return;
			}
			
			// 行军或者回程时间结束
			if (getMarchEntity().getEndTime() <= currTime) {
				marchReachProcess();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			// 时间消耗的统计信息
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > 20) {
				WorldMarchService.logger.warn("process march heartBeats tick too much time, costtime: {}, marchType: {}, marchstatus :{}, marchId:{}", costTimeMs, getMarchType(),
						getMarchEntity().getMarchStatus(), getMarchId());
			}
		}
	}

	/**
	 * 行军自身的心跳，如果有心跳需求请覆盖此方法
	 * @param time
	 * @return
	 */
	default boolean marchHeartBeats(long time) {
		return false;
	}

	/**
	 * 行军或者回程时间结束处理
	 * 
	 * @param march
	 * @return
	 */
	default void marchReachProcess() {
		// 返程的行军到达
		if (WorldUtil.isReturnBackMarch(this)) {
			marchReturn();
			return;
		}

		long currentTime = HawkTime.getMillisecond();
		
		try {
			// 被攻击方防御判断
			if (this.getMarchType() == WorldMarchType.ATTACK_PLAYER
					|| this.getMarchType() == WorldMarchType.MASS
					|| this.getMarchType() == WorldMarchType.YURI_MONSTER) {
				String targetId = this.getMarchEntity().getTargetId();
				Player defPlayer = GlobalData.getInstance().makesurePlayer(targetId);
				long beAttacked = defPlayer.getBeAttacked();
				if (beAttacked > 0 && currentTime - beAttacked < GameConstCfg.getInstance().getBeAttackedCalcTime()) {
					return;
				}
				defPlayer.setBeAttacked(currentTime);
			}
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		// 去程到达目标点，变成停留状态
		getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE);
		getMarchEntity().setReachTime(getMarchEntity().getEndTime());
		getMarchEntity().setEmoticonUseTime(0);
		// 队长到达，设置队员的到达时间
		if (isMassMarch()) {
			Set<IWorldMarch> reachSet = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
			if (reachSet != null) {
				for (IWorldMarch reachMarch : reachSet) {
					reachMarch.getMarchEntity().setReachTime(getMarchEntity().getEndTime());
					reachMarch.getMarchEntity().setEmoticonUseTime(0);
					reachMarch.updateMarch();
				}
			}
		}
		
		// 状态改变时修改加速召回信息
		getMarchEntity().setItemUseTime(0);
		getMarchEntity().setItemUseX(0.0d);
		getMarchEntity().setItemUseY(0.0d);
		getMarchEntity().setSpeedUpTimes(0);
		getMarchEntity().setCallBackX(0);
		getMarchEntity().setCallBackY(0);
		getMarchEntity().setCallBackTime(0);
		
		// 更新行军
		updateMarch();
		 
		// 行军到达
		marchReach();
	}

	@Override
	default void onMarchStop(int status, List<ArmyInfo> armys, WorldPoint targetPoint) {
		if (this.getMarchEntity().isInvalid()) {
			return;
		}
		WorldMarch march = getMarchEntity();
		WorldMarchService.logger.info("world march stop, marchData: {}", march);
		// 通用参数设置
		march.setMarchStatus(status);
		if (armys != null) {
			march.setArmys(armys);
		}
		// 不同类型自己的特殊处理
		detailMarchStop(targetPoint);

		// 更新
		this.updateMarch();

		WorldMarchService.getInstance().rmGuildMarch(this.getMarchId());

		// 刷新路点
		if (targetPoint != null) {
			WorldPointService.getInstance().notifyPointUpdate(targetPoint.getX(), targetPoint.getY());
		}
	}

	/**
	 * 根据需要子类自己实现, 如果没有,则不需要继承
	 * 这个方法执行在 更新行军之前，如果有需要同步到前端的行军信息变化，覆盖此方法执行
	 * @param targetPoint
	 */
	default void detailMarchStop(WorldPoint targetPoint) {

	}

	/**
	 * 战斗后立即更新攻方玩家的统计数据、伤兵闪回
	 */
	default void sendMsgUpdateAtkPlayerListAfterWar(boolean isAtkWin, List<Player> atkPlayers,
			Map<String, List<ArmyInfo>> atkArmyMap, List<Player> defPlayers, Player player, BattleOutcome battleOutcome) {
		int defMaxFactoryLvl = getMaxFactoryLvl(defPlayers); // 敌方最大建筑工厂等级
		for (Player tmpPlayer : atkPlayers) {
			List<ArmyInfo> leftArmyList = atkArmyMap.get(tmpPlayer.getId());
			GsApp.getInstance().postMsg(tmpPlayer, new AtkPlayerAfterWarMsg(isAtkWin, leftArmyList, defMaxFactoryLvl, battleOutcome));
		}
	}

	/**
	 * 战斗后立即更新攻方玩家的统计数据、伤兵闪回
	 * @param isAtkWin
	 * @param atkArmyLeft
	 * @param defCityLvl
	 * @param atkPlayer
	 */
	default void sendMsgUpdateAtkPlayerAfterWar(boolean isAtkWin, List<ArmyInfo> atkArmyLeft, int defCityLvl, Player atkPlayer, BattleOutcome battleOutcome) {
		GsApp.getInstance().postMsg(atkPlayer, new AtkPlayerAfterWarMsg(isAtkWin, atkArmyLeft, defCityLvl, battleOutcome));
	}
	
	/**
	 * 战斗后立即更新防守玩家的统计数据
	 */
	default void sendMsgUpdateDefPlayerListAfterWar(List<Player> defPlayers , BattleOutcome battleOutcome, ConsumeItems consumeItems) {
		for (int i = 0; i < defPlayers.size(); i++) {
			Player tmpPlayer = defPlayers.get(i);
			ConsumeItems consumeOne = i == 0 ? consumeItems : null;
			sendMsgUpdateDefPlayerAfterWar(tmpPlayer, battleOutcome, consumeOne);
		}
	}

	/**
	 * 发出消息更新防守玩家数据
	 */
	default void sendMsgUpdateDefPlayerAfterWar(Player defPlayer, BattleOutcome battleOutcome, ConsumeItems consumeItems) {
		// 战斗数据统计消息,传入copy列表
		List<ArmyInfo> leftArmyCopy = new ArrayList<>();
		List<ArmyInfo> battleLeftArmy = battleOutcome.getBattleArmyMapDef().get(defPlayer.getId());
		if (battleLeftArmy != null) {
			battleLeftArmy.stream().forEach(e -> leftArmyCopy.add(e.getCopy()));
		}
		// 让队友自己更新
		GsApp.getInstance().postMsg(defPlayer.getXid(), DefPlayerAfterWarMsg.valueOf(battleOutcome.isAtkWin(), leftArmyCopy, consumeItems));
	}

	/**
	 * pve过后抛事件更新玩家信息
	 * @param atkPlayer
	 * @param out
	 */
	default void postEventAfterPve(Player atkPlayer, BattleOutcome out, WorldMarchType marchType, int level) {
		GsApp.getInstance().postMsg(atkPlayer.getXid(), AtkAfterPveMsg.valueOf(out, marchType, level));
	}
	
	/**
	 * 获取玩家列表中最大的建筑工厂等级
	 * 
	 * @param players
	 * @return
	 */
	default int getMaxFactoryLvl(List<Player> players) {
		int maxFactoryLvl = 0;
		if (players != null) {
			for (Player player : players) {
				int factoryLvl = player.getCityLevel();
				if (factoryLvl > maxFactoryLvl) {
					maxFactoryLvl = factoryLvl;
				}
			}
		}
		return maxFactoryLvl;
	}

	/**
	 * 检查PVP战斗是否满足前置条件，不满足则发邮件通知，现在普通攻击大本，集结攻击大本
	 * 
	 * @param march
	 * @param tarPoint
	 * @param defPlayer
	 * @return
	 */
	default boolean checkPVPBeforeWar(WorldPoint tarPoint, Player defPlayer, Player player) {
		return true;
	}

	/**
	 * 单人或者集结攻打玩家基地
	 * 
	 * @param defPlayer
	 * @param battle
	 * @param defPlayers
	 * @param playerArmyTypes
	 * @return
	 */
	default Set<IWorldMarch> getDefMarch4War(Player defPlayer, List<Player> defPlayers) {
		Set<IWorldMarch> helpMarchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(defPlayer.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		for (IWorldMarch march : helpMarchList) {
			defPlayers.add(GlobalData.getInstance().makesurePlayer(march.getPlayerId()));
		}
		return helpMarchList;
	}

	default void updateDefMarchAfterWar(List<IWorldMarch> helpMarchList, Map<String, List<ArmyInfo>> defArmyMap) {
		// 更新援助防御玩家行军的部队
		if (helpMarchList != null && helpMarchList.size() > 0) {
			for (IWorldMarch tmpMarch : helpMarchList) {
				List<ArmyInfo> leftList = defArmyMap.get(tmpMarch.getPlayerId());
				if (WorldUtil.calcSoldierCnt(leftList) > 0) {
					WorldMarchService.getInstance().updateMarchArmy(tmpMarch, leftList);
					continue;
				}
				
				if (tmpMarch.getMarchType() == WorldMarchType.ASSISTANCE) {
					AssistanceCallbackNotifyPB.Builder callbackNotifyPB = AssistanceCallbackNotifyPB.newBuilder();
					callbackNotifyPB.setMarchId(tmpMarch.getMarchId());
					Player assistPlayer = GlobalData.getInstance().makesurePlayer(tmpMarch.getMarchEntity().getTargetId());
					assistPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.ASSISTANCE_MARCH_CALLBACK, callbackNotifyPB));
				}
				
				// 死光了，行军立即送死兵回家
				WorldMarchService.getInstance().onMarchReturnImmediately(tmpMarch, leftList);
				
			}
		}
	}

	/**
	 * 行军达到，获得该行军的同行人（含march本身即队长）
	 * 
	 * @param march
	 *            普通行军，集结则为队长march
	 * @return 返回一个包含march的列表，至少包含1个元素
	 */
	default List<IWorldMarch> getMassMarchList(IWorldMarch march) {
		List<IWorldMarch> massMarchList = new ArrayList<IWorldMarch>();
		massMarchList.add(march);

		Set<IWorldMarch> tmpSet = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
		if (tmpSet != null) {
			massMarchList.addAll(tmpSet);
		}
		return massMarchList;
	}

	/**
	 * 遣返战斗点行军
	 */
	default boolean returnMarchList(List<IWorldMarch> massMarchlist) {
		if (massMarchlist == null || massMarchlist.size() <= 0) {
			return true;
		}
		for (IWorldMarch march : massMarchlist) {
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
		}
		return true;
	}

	/**
	 * 援助战斗点
	 */
	default boolean assitenceWarPoint(List<IWorldMarch> marchList, WorldPoint worldPoint, Player player) {

		// 入驻的行军列表
		List<IWorldMarch> stayMarchList = null;
		// 队长行军
		IWorldMarch leaderMarch = null;
		// 队长
		Player leader = null;

		if (WorldUtil.isPresidentPoint(worldPoint)) {
			stayMarchList = WorldMarchService.getInstance().getPresidentQuarteredMarchs();
			String leaderMarchId = WorldMarchService.getInstance().getPresidentLeaderMarch();
			if (!HawkOSOperator.isEmptyString(leaderMarchId)) {
				leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
			}
		} else if (WorldUtil.isGuildPoint(worldPoint)) {
			stayMarchList = GuildManorService.getInstance().getManorBuildMarch(worldPoint.getId());
			leaderMarch = GuildManorService.getInstance().getManorLeaderMarch(worldPoint.getId());
		} else if (WorldUtil.isPresidentTowerPoint(worldPoint)) {
			stayMarchList = WorldMarchService.getInstance().getPresidentTowerStayMarchs(worldPoint.getId());
			if (!stayMarchList.isEmpty()) {
				String leaderMarchId = WorldMarchService.getInstance().getPresidentTowerLeaderMarchId(worldPoint.getId());
				leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
			}
		} else if (WorldUtil.isSuperWeaponPoint(worldPoint)) {
			stayMarchList = WorldMarchService.getInstance().getSuperWeaponStayMarchs(worldPoint.getId());
			if (!stayMarchList.isEmpty()) {
				String leaderMarchId = WorldMarchService.getInstance().getSuperWeaponLeaderMarchId(worldPoint.getId());
				leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
			}
		} else if (WorldUtil.isGuildSpacePoint(worldPoint)) {
			SpaceWorldPoint spacePoint = (SpaceWorldPoint) worldPoint;
			stayMarchList = spacePoint.getDefMarchList();
			if (!stayMarchList.isEmpty()) {
				String leaderMarchId = spacePoint.getLeaderMarchId();
				leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
			}
		} else if (WorldUtil.isXZQPoint(worldPoint)) {
			stayMarchList = WorldMarchService.getInstance().getXZQStayMarchs(worldPoint.getId());
			if (!stayMarchList.isEmpty()) {
				String leaderMarchId = WorldMarchService.getInstance().getXZQLeaderMarchId(worldPoint.getId());
				leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
			}
		} else if (WorldUtil.isFortressPoint(worldPoint)) {
			stayMarchList = WorldMarchService.getInstance().getFortressStayMarchs(worldPoint.getId());
			if (!stayMarchList.isEmpty()) {
				String leaderMarchId = WorldMarchService.getInstance().getFortressLeaderMarchId(worldPoint.getId());
				leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
			}
			
		} else if(worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			stayMarchList = new ArrayList<>();
			BlockingDeque<String> marchIds = WorldMarchService.getInstance().getFlagMarchs(worldPoint.getGuildBuildId());
			for (String marchId : marchIds) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				if (march != null) {
					stayMarchList.add(march);
				}
			}
			
			if (!stayMarchList.isEmpty()) {
				String leaderMarchId = WorldMarchService.getInstance().getFlagLeaderMarchId(worldPoint.getGuildBuildId());
				leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
			}
			
		} else {
			return returnMarchList(marchList);
		}

		if (stayMarchList != null && !stayMarchList.isEmpty()) {
			leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		} else {
			leaderMarch = marchList.get(0);
			leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		}

		// 8.21新增：检查当前自己的行军，如果行军中已有英雄，则将自己队列中的英雄撤回
		for (IWorldMarch worldMarch : marchList) {
			if (worldMarch.getMarchEntity().getHeroIdList().isEmpty()) {// 如果当前行军中没有英雄直接跳过
				continue;
			}
			// 如果有英雄，则判断当前据点停留的行军有没有自己的
			for (IWorldMarch stayMarch : stayMarchList) {
				if (stayMarch.getPlayerId().equals(worldMarch.getPlayerId())) {
					List<Integer> heroId = worldMarch.getMarchEntity().getHeroIdList();
					if (stayMarch.getMarchEntity().getHeroIdList().size() > 0) {
						// 如果停留的行军有自己的并且已经带有英雄，将当前的英雄瞬间返回
						player = stayMarch.getPlayer();
						List<PlayerHero> OpHero = player.getHeroByCfgId(heroId);
						for (PlayerHero hero: OpHero) {
							WorldMarchService.logger.info("assitenceWarPoint hero back, playerId:{}, marchId:{}, heroId:{}", player.getId(), worldMarch.getMarchId(), worldMarch.getMarchEntity().getHeroIdList());
							hero.backFromMarch(stayMarch);
							worldMarch.getMarchEntity().setHeroIdList(Collections.emptyList());
						}
						Optional<SuperSoldier> marchSsOp = player.getSuperSoldierByCfgId(worldMarch.getSuperSoldierId());
						if(marchSsOp.isPresent()){
							marchSsOp.get().backFromMarch(stayMarch);
						}
					} else if (heroId.size() > 0) {
						// 停留行军没有英雄则直接加入
						stayMarch.getMarchEntity().setHeroIdList(heroId);
						stayMarch.getMarchEntity().resetEffectParams();
						worldMarch.getMarchEntity().setHeroIdList(Collections.emptyList());
					}
				}
			}
		}

		// 先处理过来行军中的队长行军，队长行军处理完毕后删除
		if (stayMarchList != null && !stayMarchList.isEmpty() && WorldUtil.isGuildPoint(worldPoint)) {
			// 检查集结队伍是否超编
			Set<IWorldMarch> massMarchList = WorldMarchService.getInstance().getMassJoinMarchs(leaderMarch, true);
			int count = massMarchList != null ? massMarchList.size() + 1 : 1;// 加上队长
			if (count > leader.getMaxMassJoinMarchNum(leaderMarch) + leaderMarch.getMarchEntity().getBuyItemTimes()) {
				return returnMarchList(marchList);
			}
		}

		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);

		List<WorldMarch> ppList = new ArrayList<WorldMarch>();
		for (IWorldMarch worldMarch : stayMarchList) {
			ppList.add(worldMarch.getMarchEntity());
		}

		int curPopulationCnt = WorldUtil.calcMarchsSoldierCnt(ppList); // 已驻扎士兵人口
		// 剩余人口<0部队返回
		int remainArmyPopu = maxMassSoldierNum - curPopulationCnt;
		if (remainArmyPopu <= 0) {
			return returnMarchList(marchList);
		}

		// 优先加入已在玩家
		for (IWorldMarch stayMarch : stayMarchList) {
			Iterator<IWorldMarch> it = marchList.iterator();
			while (it.hasNext()) {
				IWorldMarch massMarch = it.next();
				if (!stayMarch.getPlayerId().equals(massMarch.getPlayerId())) {
					continue;
				}

				List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
				List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
				int stayCnt = WorldUtil.calcStayArmy(massMarch.getMarchEntity(), remainArmyPopu, stayList, backList);
				
				// 回家的士兵生成新行军
				if (backList.size() > 0) {
					WorldMarchService.getInstance().onNewMarchReturn(null, massMarch, backList, new ArrayList<Integer>(),0, null);
				}

				List<List<ArmyInfo>> lists = new ArrayList<List<ArmyInfo>>();
				lists.add(stayMarch.getMarchEntity().getArmys());
				lists.add(stayList);
				WorldMarchService.getInstance().updateMarchArmy(stayMarch, WorldUtil.mergMultArmyList(lists));

				// 原有行军结束
				WorldMarchService.getInstance().onWorldMarchOver(massMarch);

				remainArmyPopu -= stayCnt;
				it.remove();
				break;
			}
		}

		// 一轮援助检查后是否所有行军已处理
		if (marchList.isEmpty()) {
			return true;
		}

		// 加入要留驻的行军
		Iterator<IWorldMarch> it = marchList.iterator();
		
		while (it.hasNext() && remainArmyPopu > 0) {
			IWorldMarch march = it.next();
			
			int eff1546 = march.getPlayer().getEffect().getEffVal(EffType.HERO_1546, march.getMarchEntity().getEffectParams());
			if (remainArmyPopu + eff1546 <= 0) {
				continue;
			}
			
			List<ArmyInfo> stayArmyList = new ArrayList<ArmyInfo>();
			List<ArmyInfo> backArmyList = new ArrayList<ArmyInfo>();
			int stayCnt = WorldUtil.calcStayArmy(march.getMarchEntity(), remainArmyPopu, stayArmyList, backArmyList);
			// 回家的士兵生成新行军
			if (backArmyList.size() > 0) {
				WorldMarchService.getInstance().onNewMarchReturn(null, march, backArmyList, new ArrayList<Integer>(),0, null);
			}
			march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, stayArmyList, worldPoint);
			remainArmyPopu -= stayCnt;
			it.remove();
		}
		
		// 有多余的行军就原路返回&&通知行军提示变更
		if (marchList.size() > 0) {
			returnMarchList(marchList);
		}
		return true;
	}

	/**
	 * 进攻战斗点：堡垒、王座
	 * @param atkMarchs
	 * @param worldPoint
	 * @return isAtkWin 进攻方是否获胜
	 */

	default boolean attackWarPoint(List<IWorldMarch> atkMarchs, WorldPoint worldPoint, Player player) {
		// 攻击方玩家列表
		List<Player> atkPlayers = new ArrayList<>();
		// 防守方玩家列表
		List<Player> defPlayers = new ArrayList<>();
		// 防守方行军列表
		List<IWorldMarch> defMarchs = new ArrayList<>();
		
		if (WorldUtil.isPresidentPoint(worldPoint)) {
			defMarchs = WorldMarchService.getInstance().getPresidentQuarteredMarchs();
		} else if (WorldUtil.isGuildBuildPoint(worldPoint)) {
			defMarchs = GuildManorService.getInstance().getManorBuildMarch(worldPoint.getId());
		} else if (WorldUtil.isPresidentTowerPoint(worldPoint)) {
			defMarchs = WorldMarchService.getInstance().getPresidentTowerStayMarchs(worldPoint.getId());
		} else if (WorldUtil.isSuperWeaponPoint(worldPoint)) {
			defMarchs = WorldMarchService.getInstance().getSuperWeaponStayMarchs(worldPoint.getId());
		} else if (WorldUtil.isXZQPoint(worldPoint)) {
			defMarchs = WorldMarchService.getInstance().getXZQStayMarchs(worldPoint.getId());
		} else if (WorldUtil.isFortressPoint(worldPoint)) {
			defMarchs = WorldMarchService.getInstance().getFortressStayMarchs(worldPoint.getId());
		} else if(worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			BlockingDeque<String> marchIds = WorldMarchService.getInstance().getFlagMarchs(worldPoint.getGuildBuildId());
			for (String marchId : marchIds) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				if (march != null) {
					defMarchs.add(march);
				}
			}
		}

		List<IWorldMarch> calAtkList = new ArrayList<>();
		atkMarchs.forEach(march -> { // 填充攻击方玩家
			Player atkplayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			atkPlayers.add(atkplayer);
			calAtkList.add(march);
		});

		List<IWorldMarch> calDefList = new ArrayList<>();
		defMarchs.forEach(march -> { // 填充防守方玩家
			Player defplayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			defPlayers.add(defplayer);
			calDefList.add(march);
		});
		
		// 防守玩家身份
		int defenderIdentity = 0;
		// 战斗类型
		BattleConst.BattleType battleType = BattleConst.BattleType.OTHER;
				
		if (WorldUtil.isGuildPoint(worldPoint)) {
			battleType = worldPoint.getGuildId().equals(player.getGuildId()) ? BattleConst.BattleType.RECOVER_MANOR : BattleConst.BattleType.ATTACK_MANOR;
			defenderIdentity = DefenderIdentity.GUILD_MANOR;
			
		} else if (WorldUtil.isPresidentPoint(worldPoint)) {
			battleType = BattleConst.BattleType.ATTACK_PRESIDENT;
			defenderIdentity = DefenderIdentity.CAPITAL;
			
		} else if (WorldUtil.isPresidentTowerPoint(worldPoint)) {
			battleType = BattleConst.BattleType.ATTACK_PRESIDENT_TOWER;
			defenderIdentity = DefenderIdentity.CAPITAL;
		} else if (WorldUtil.isSuperWeaponPoint(worldPoint)) {
			battleType = BattleConst.BattleType.ATTACK_SUPER_WEAPON_PVP;
			defenderIdentity = DefenderIdentity.SUPER_WEAPON;
		} else if (WorldUtil.isXZQPoint(worldPoint)) {
			battleType = BattleConst.BattleType.ATTACK_XZQ_PVP;
			defenderIdentity = DefenderIdentity.XZQ;
		}else if (WorldUtil.isFortressPoint(worldPoint)) {
			battleType = BattleConst.BattleType.ATTACK_FORTRESS_PVP;
			defenderIdentity = DefenderIdentity.CROSS_FORTRESS;
		} else if(worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			battleType = BattleConst.BattleType.ATTACK_WAR_FLAG;
			defenderIdentity = DefenderIdentity.WAR_FLAG;
		}
		
		Player leaderPlayer = GlobalData.getInstance().makesurePlayer(atkMarchs.get(0).getPlayerId());
		Player defLeader = GlobalData.getInstance().makesurePlayer(defMarchs.get(0).getPlayerId());
		PvpBattleIncome battleIncome = null;
		if (WorldUtil.isGuildPoint(worldPoint)) {
			boolean isAtkOwner = leaderPlayer.hasGuild() && leaderPlayer.getGuildId().equals(worldPoint.getGuildId());
			boolean isDefOwner = defLeader.hasGuild() && defLeader.getGuildId().equals(worldPoint.getGuildId());
			battleIncome = BattleService.getInstance().initPVPBattleData(battleType, worldPoint.getId(), atkPlayers, defPlayers, calAtkList, calDefList, isAtkOwner, isDefOwner);
		} else {
			battleIncome = BattleService.getInstance().initPVPBattleData(battleType, worldPoint.getId(), atkPlayers, defPlayers, calAtkList, calDefList);
		}
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		final boolean isAtkWin = battleOutcome.isAtkWin();

		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();// 攻击方剩余部队
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();// 防守方剩余部队

		// 播放战斗动画
		IWorldMarch atkMarch = atkMarchs.get(0);
		List<ArmyInfo> atkArmyList = WorldUtil.mergAllPlayerArmy(atkArmyLeftMap);
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);
		WorldMarchService.getInstance().sendBattleResultInfo(atkMarch, isAtkWin, atkArmyList, defArmyList, isAtkWin);

		if (isAtkWin) {
			
			// 如果是总统府
			if (WorldUtil.isPresidentPoint(worldPoint)) {
				PresidentFightService.getInstance().doPresidentAttackWin(leaderPlayer.getId(), defLeader.getId());
			}
			
			// 防守方行军返回
			for (IWorldMarch march : defMarchs) {
				WorldMarchService.getInstance().onMarchReturn(march, defArmyLeftMap.get(march.getPlayerId()), worldPoint.getId());
			}

			// 攻方行军留下
			occupyWarPoint(atkMarchs, atkArmyLeftMap, worldPoint, player);
			
			// 如果是总统府箭塔(总统府箭塔相关要放在行军处理之后， 顺序不要乱)
			if (WorldUtil.isPresidentTowerPoint(worldPoint)) {
				PresidentFightService.getInstance().doPresidentTowerAttackWin(leaderPlayer.getId(), defLeader.getId(), worldPoint.getId());
			}
			
			if (WorldUtil.isSuperWeaponPoint(worldPoint)) {
				IWeapon weapon = SuperWeaponService.getInstance().getWeapon(worldPoint.getId());
				weapon.doSuperWeaponAttackWin(leaderPlayer, defLeader);
			}
			if (WorldUtil.isFortressPoint(worldPoint)) {
				IFortress fortress = CrossFortressService.getInstance().getFortress(worldPoint.getId());
				fortress.doFightWin(leaderPlayer, defLeader);
			}
		} else {
			// 如果是总统府
			if (WorldUtil.isPresidentPoint(worldPoint)) {
				PresidentFightService.getInstance().doPresidentAttackLose(leaderPlayer.getId(), defLeader.getId());
			}
			
			// 如果是总统府箭塔
			if (WorldUtil.isPresidentTowerPoint(worldPoint)) {
				PresidentFightService.getInstance().doPresidentTowerAttackLose(leaderPlayer.getId(), defLeader.getId(), worldPoint.getId());
			}

			// 如果是超级武器
			if (WorldUtil.isSuperWeaponPoint(worldPoint)) {
				IWeapon weapon = SuperWeaponService.getInstance().getWeapon(worldPoint.getId());
				weapon.doSuperWeaponAttackLose(leaderPlayer, defLeader);
			}
			
			// 进攻方行军返回
			for (IWorldMarch march : atkMarchs) {
				WorldMarchService.getInstance().onMarchReturn(march, atkArmyLeftMap.get(march.getPlayerId()), worldPoint.getId());
			}

			// 防方行军留下
			updateDefMarchAfterWar(defMarchs, defArmyLeftMap);
		}

		// 参战双方发送战斗邮件
		FightMailService.getInstance().sendFightMail(worldPoint.getPointType(), battleIncome, battleOutcome, null);
		BattleService.getInstance().dealWithPvpBattleEvent(battleIncome, battleOutcome, isMassMarch(), this.getMarchType());

		// 处理士兵、任务、统计等
		sendMsgUpdateAtkPlayerListAfterWar(isAtkWin, atkPlayers, atkArmyLeftMap, defPlayers, player, battleOutcome);
		sendMsgUpdateDefPlayerListAfterWar(defPlayers, battleOutcome, null);

		// 记录战斗安全日志
		if (defenderIdentity != 0) {
			LogUtil.logSecBattleFlow(atkPlayers.get(0), defPlayers.get(0), "", defenderIdentity, isAtkWin, null, null, atkArmyList, defArmyList, 0, calAtkList.get(0).getMarchEntity(), atkMarchs.get(0).isMassMarch());
		}
		
		// 记录国王战相关log
		if (defenderIdentity == DefenderIdentity.CAPITAL) {
			try {
				Map<String, List<ArmyInfo>> atkArmyMap = battleOutcome.getAftArmyMapAtk();// 攻击方剩余部队
				Map<String, List<ArmyInfo>> defArmyMap = battleOutcome.getAftArmyMapDef();// 防守方剩余部队
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("turnCount", PresidentFightService.getInstance().getPresidentCity().getTurnCount());  // 总统争夺战期数
				paramMap.put("battleId", HawkTime.getMillisecond());   // 战斗ID
				paramMap.put("allianceId", leaderPlayer.getGuildId());  // 联盟ID
				paramMap.put("identity", 1);   // 身份： 进攻方或防守方
				paramMap.put("warType", atkArmyMap.size() > 1 ? 1 : 0);  // 单人进攻或集结进攻
				paramMap.put("pointType", WorldUtil.isPresidentPoint(worldPoint) ? 1 : 0); // 点类型：总统府或箭塔
				paramMap.put("result", isAtkWin ? 1: 0);   // 战斗结果
				
				for (Entry<String, List<ArmyInfo>> entry : atkArmyMap.entrySet()) {
					Player attPlayer = GlobalData.getInstance().makesurePlayer(entry.getKey());
					if (attPlayer == null) {
						continue;
					}
					paramMap.put("armyInfo", entry.getValue());
					LogUtil.logPresidentWar(attPlayer, paramMap);
				}
				
				paramMap.put("allianceId", defLeader.getGuildId());
				paramMap.put("identity", 0);
				paramMap.put("result", isAtkWin ? 0 : 1);
				for (Entry<String, List<ArmyInfo>> entry : defArmyMap.entrySet()) {
					Player defPlayer = GlobalData.getInstance().makesurePlayer(entry.getKey());
					if (defPlayer == null) {
						continue;
					}
					paramMap.put("armyInfo", entry.getValue());
					LogUtil.logPresidentWar(defPlayer, paramMap);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 刷新战力
		refreshPowerAfterWar(atkPlayers, defPlayers);
		
		return isAtkWin;
	}

	/**
	 * 占领战斗点
	 * 
	 * @param leader
	 */
	default boolean occupyWarPoint(List<IWorldMarch> massMarchList, Map<String, List<ArmyInfo>> atkArmyMap, WorldPoint worldPoint, Player player) {

		// 已然没有兵
		if (massMarchList == null || massMarchList.size() <= 0) {
			WorldMarchService.logger.error("world occupy war point error pointType:{},massMarchList:{}", worldPoint.getPointType(), massMarchList);
			return false;
		}

		// 计算队长的最大集结数目
		IWorldMarch leaderMarch = massMarchList.get(0);
		Player leaderPlayer = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		
		// 检查集结士兵数是否超上限
		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leaderPlayer);

		WorldMarchService.logger.info("world occupy war point leaderPlayer:{}, remainSpace:{} ", leaderPlayer, maxMassSoldierNum);

		StringBuilder attackBuff = new StringBuilder();
		for (IWorldMarch tmpMarch : massMarchList) {
			attackBuff.append("[playerId:" + tmpMarch.getPlayerId() + ",army:" + tmpMarch.getMarchEntity().getArmyStr() + "]");
		}

		// 开始计算留下来的兵力
		StringBuilder defBuff = new StringBuilder();
		
		Iterator<IWorldMarch> it = massMarchList.iterator();
		while (it.hasNext()) {

			IWorldMarch tmpMarch = it.next();

			// 计算该行军剩余兵力
			List<ArmyInfo> leftList = atkArmyMap != null ? atkArmyMap.get(tmpMarch.getPlayerId()) : tmpMarch.getMarchEntity().getArmys();

			if (WorldUtil.calcSoldierCnt(leftList) > 0) {

				// 重设行军兵力
				WorldMarchService.getInstance().resetMarchArmys(tmpMarch, leftList);
				WorldMarchService.logger.info("occupy war point leftList:{}, tmpMarch:{} ", leftList, tmpMarch);

				// 计算留下和回家的兵力
				List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
				List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
				WorldUtil.calcStayArmy(tmpMarch.getMarchEntity(), maxMassSoldierNum, stayList, backList);
				
				// 回家的士兵生成新行军
				if (backList.size() > 0) {
					WorldMarchService.getInstance().onNewMarchReturn(player, tmpMarch, backList, new ArrayList<Integer>(),0, null);
				}
				WorldMarchService.logger.info("world occupy war point backList:{}, stayList:{} ", backList, stayList);

				defBuff.append("[playerId:" + tmpMarch.getPlayerId() + ",stayList:" + stayList + ",backList:" + backList
						+ "]");
				tmpMarch.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE, stayList, worldPoint);
			} else {
				defBuff.append("[playerId:" + tmpMarch.getPlayerId() + "leftList:" + leftList + "]");
				WorldMarchService.getInstance().onMarchReturn(tmpMarch, leftList, worldPoint.getId());
				it.remove();// 死光的移出，以便选出正确的队长
			}

		}

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_OCCUPY_WAR_POINT,
				Params.valueOf("pointType", worldPoint.getPointType()),
				Params.valueOf("leaderPlayer", leaderPlayer.getGuildId()),
				Params.valueOf("pointId", worldPoint.getId()), Params.valueOf("armyBefore", attackBuff.toString()),
				Params.valueOf("armyAfter", defBuff.toString()));
		return true;

	}

	/**
	 * 广播国王易主
	 */
	default void broadcastPresidentChanged(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}

		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}

		String guildName = GuildService.getInstance().getGuildName(guildId);
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.PRESIDENT_CHANGED, null, guildName);
	}

	/**
	 * 行军到达, 进行目标行为处理, 各自的行军行为各自处理
	 * 
	 * @return
	 */
	default boolean marchReach() {
		WorldMarchService.logger.info("world march reach, march:{}", toString());
		// 行军数据
		long startTime = HawkTime.getMillisecond();
		try {
			Player player = null;
			if (getPlayerId() != null) { // 非玩家行军，可能是怪物
				player = GlobalData.getInstance().makesurePlayer(this.getPlayerId());
				// 行为日志
				BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH, Params.valueOf("marchData", this));

				// 推送行军到达信息
				PushService.getInstance().pushMsg(this.getPlayerId(), PushMsgType.ARMY_ARRIVED_VALUE);
				
				// 从联盟箭塔中移除
				GuildManorService.getInstance().rmFromGuildBartizan(this);
			}
			
			// 各自行军自己处理自己的到达
			onMarchReach(player);
		} catch (Exception e) {
			WorldMarchService.logger.error("world march reach error, march:{}", this.toString());
			WorldMarchService.getInstance().onMarchReturn(this, this.getMarchEntity().getArmys(), 0);
			if (this.isMassMarch()) {
				Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
				for (IWorldMarch massJoinMarch : massJoinMarchs) {
					if (massJoinMarch.isReturnBackMarch()) {
						continue;
					}
					WorldMarchService.getInstance().onMarchReturnImmediately(massJoinMarch, massJoinMarch.getMarchEntity().getArmys());
					WorldMarchService.logger.error("world march reach error, mass join march return, march:{}", massJoinMarch.toString());
				}
			}
			HawkException.catchException(e);
		}
		
		WorldMarchService.logger.info("world march reach , marchType: {}, costtime: {}", this.getMarchType(),
				HawkTime.getMillisecond() - startTime);
		
		return true;
	}

	/**
	 * 行军返回, 进行结算
	 * 
	 * @return
	 */
	default void marchReturn() {
		// 所有行军返回投递到自己队列处理
		Player player = GlobalData.getInstance().makesurePlayer(getPlayerId());
		if (player != null) {
			player.dealMsg(MsgId.MARCH_RETURN, new WorldMarchReturnMsgInvoker(player, this));
		}
		// 删除已结束的行军信息
		WorldMarchService.getInstance().removeMarch(this);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.MARCH, Action.WORLD_MARCH_RETURN, Params.valueOf("marchData", this));
	}

	/**
	 * 行军在返回到达时调用
	 * @param player
	 */
	default void onWorldMarchReturn(Player player) {
	}

	@Override
	default void updateMarch() {
		
		if (this.getMarchEntity().isInvalid()) {
			return;
		}
		
		// 发给自己
		Player player = GlobalData.getInstance().getActivePlayer(this.getMarchEntity().getPlayerId());
		if (player != null) {
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_UPDATE_PUSH_VALUE, this.getMarchEntity().toBuilder(WorldMarchPB.newBuilder(), WorldMarchRelation.SELF));
			player.sendProtocol(protocol);
		}
		
		// 通知行军事件
		WorldMarchService.getInstance().notifyMarchEvent(MarchEvent.MARCH_UPDATE_VALUE, this.getMarchEntity());
		// 日志记录
		WorldMarchService.logger.info("world march update, marchData: {}", this);
		BehaviorLogger.log4Service(player, Source.MARCH, Action.UPDATE_MARCH,
				Params.valueOf("marchId", this.getMarchId()),
				Params.valueOf("marchType", this.getMarchType()),
				Params.valueOf("marchStatus", this.getMarchEntity().getMarchStatus()),
				Params.valueOf("targetId", this.getMarchEntity().getTargetId()),
				Params.valueOf("terminalId", this.getMarchEntity().getTerminalId()),
				Params.valueOf("callBackX", this.getMarchEntity().getCallbackX()),
				Params.valueOf("callBackY", this.getMarchEntity().getCallbackY()),
				Params.valueOf("resStartTime", this.getMarchEntity().getResStartTime()));
	}

	/**
	 * 是否需要在联盟战争界面显示
	 */
	@Override
	default boolean needShowInGuildWar() {
		return false;
	}

	/**
	 * 是否需要在国家战争界面显示
	 */
	@Override
	default boolean needShowInNationWar() {
		return false;
	}
	
	/**
	 * 检查联盟战争显示
	 */
	@Override
	default boolean checkGuildWarShow() {
		if (!needShowInGuildWar()) {
			return false;
		}

		// 返回中的和已经到达的行军不显示在联盟战争
		if (this.isReturnBackMarch() || this.isReachAndStopMarch()) {
			return false;
		}
		return true;
	}

	/**
	 * 获取主动方联盟战争界面信息
	 * @return
	 */
	default GuildWarTeamInfo.Builder getGuildWarInitiativeInfo() {
		
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		// 队长行军
		WorldMarch leaderMarch = this.getMarchEntity();
		// 队长id
		String leaderId = leaderMarch.getPlayerId();
		// 队长
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		// 联盟编队
		GuildFormationObj formationObj = GuildService.getInstance().getGuildFormation(leader.getGuildId());
		GuildFormationCell formation = null;
		if (formationObj != null) {
			formation = formationObj.getGuildFormation(this.getMarchId());
		}
		// 队长位置
		int[] leaderPos = leader.getPosXY();

		builder.setPointType(WorldPointType.PLAYER);
		builder.setX(leaderPos[0]);
		builder.setY(leaderPos[1]);
		builder.setLeaderArmyLimit(this.getMaxMassJoinSoldierNum(leader, null));
		builder.setGridCount(leader.getMaxMassJoinMarchNum(this));
		if (leader.hasGuild()) {
			builder.setGuildTag(GuildService.getInstance().getGuildTag(leader.getGuildId()));
		}
		builder.setLeaderMarch(getGuildWarSingleInfo(leaderMarch));

		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(this.getMarchEntity().getArmys());

		// 加入集结的行军
		Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
		for (IWorldMarch joinMarch : joinMarchs) {
			if (joinMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			builder.addJoinMarchs(getGuildWarSingleInfo(joinMarch.getMarchEntity(), formation));
			reachArmyCount += WorldUtil.calcSoldierCnt(joinMarch.getMarchEntity().getArmys());
		}
		builder.setReachArmyCount(reachArmyCount);
		builder.setServerId(leader.getMainServerId());
		
		// 联盟编队改动,如果需要这个人加入,他又没加入  需要通知客户端这个状态
		if (formation != null) {
			for (String fightId : formation.getFightIds()) {
				if (leaderId.equals(fightId)) {
					continue;
				}
				Optional<IWorldMarch> findAny = joinMarchs.stream().filter(m -> m.getPlayerId().equals(fightId)).findAny();
				if (findAny.isPresent()) {
					continue;
				}
				if(!GuildService.getInstance().isPlayerInGuild(leader.getGuildId(), fightId)){
					continue;
				}
				Player joinMemeber = GlobalData.getInstance().makesurePlayer(fightId);
				if (Objects.isNull(joinMemeber)) {
					continue;
				}
				GuildWarSingleInfo.Builder single = GuildWarSingleInfo.newBuilder();
				single.setPlayerId(joinMemeber.getId());
				single.setPlayerName(joinMemeber.getName());
				single.setIconId(joinMemeber.getIcon());
				single.setPfIcon(joinMemeber.getPfIcon());
				single.setNotJoin(true);
				GuildFormationModule module = joinMemeber.getModule(GsConst.ModuleType.GUILD_FORMATION);
				single.setDelFormationNotice(module.delNotice(this.getMarchId()));
				builder.addJoinMarchs(single);
			}
		}
		
		return builder;
	}

	/**
	 * 获取被动方联盟战争界面信息
	 */
	default GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		// 队长id
		String leaderId = this.getMarchEntity().getTargetId();
		// 队长
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		// 队长位置
		int[] pos = leader.getPosXY();
		builder.setPointType(WorldPointType.PLAYER);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setLeaderArmyLimit(leader.getMaxAssistSoldier());
		builder.setGridCount(leader.getMaxMassJoinMarchNum(this));
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
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING);
		List<ArmyInfo> armys = ArmyService.getInstance().getFreeArmyList(leader);
		for (ArmyInfo army : armys) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		builder.setLeaderMarch(leaderInfo);
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(armys);
		
		Set<IWorldMarch> assistandMarchs = WorldMarchService.getInstance().getPlayerPassiveMarchs(leaderId, WorldMarchType.ASSISTANCE_VALUE);
		for (IWorldMarch assistandMarch : assistandMarchs) {
			if (assistandMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			builder.addJoinMarchs(getGuildWarSingleInfo(assistandMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(assistandMarch.getMarchEntity().getArmys());
		}
		builder.setCityLevel(leader.getCityLv());
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}

	/**
	 * 联盟战争界面里单人信息
	 * @param worldMarch
	 * @return
	 */
	default GuildWarSingleInfo.Builder getGuildWarSingleInfo(WorldMarch worldMarch) {
		return getGuildWarSingleInfo(worldMarch, null);
	}
	
	default GuildWarSingleInfo.Builder getGuildWarSingleInfo(WorldMarch worldMarch, GuildFormationCell formation) {
		Player player = GlobalData.getInstance().makesurePlayer(worldMarch.getPlayerId());
		GuildWarSingleInfo.Builder builder = GuildWarSingleInfo.newBuilder();
		builder.setPlayerId(worldMarch.getPlayerId());
		builder.setPlayerName(player.getName());
		builder.setIconId(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		WorldMarchStatus marchStatus = WorldMarchStatus.valueOf(worldMarch.getMarchStatus());
		builder.setMarchStatus(marchStatus);
		builder.setMarchId(worldMarch.getMarchId());
		List<PlayerHero> heros = player.getHeroByCfgId(worldMarch.getHeroIdList());
		if (heros != null && !heros.isEmpty()) {
			for (PlayerHero hero : heros) {
				builder.addHeroInfo(hero.toPBobj());
			}
		}
		SuperSoldier ssoldier = player.getSuperSoldierByCfgId(worldMarch.getSuperSoldierId()).orElse(null);
		if(Objects.nonNull(ssoldier)){
			builder.setSsoldier(ssoldier.toPBobj());
		}
		
		List<ArmyInfo> armys = worldMarch.getArmys();
		for (ArmyInfo army : armys) {
			builder.addArmys(army.toArmySoldierPB(player));
		}
		// 行军结束时间(集结状态的行军为集结结束时间)
		if (marchStatus.equals(WorldMarchStatus.MARCH_STATUS_WAITING)) {
			builder.setEndTime(worldMarch.getStartTime());
		} else {
			builder.setEndTime(worldMarch.getEndTime());
		}
		builder.setStartTime(worldMarch.getStartTime());
		builder.setJourneyTime(worldMarch.getMarchJourneyTime());
		
		// 联盟编队,非本队
		if (formation != null && !formation.fight(player.getId())) {
			builder.setNotFormation(true);
		}
		return builder;
	}
	
	@Override
	default void onMarchStart() {}
	
	@Override
	default void onMarchReturn() {}
	
	
	@Override
	default void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		WorldMarchService.getInstance().onMarchReturn(this, callbackTime, getMarchEntity().getAwardItems(), getMarchEntity().getArmys(), 0, 0);
	}
	
	@Override
	default boolean doCollectRes(boolean changeSpeed) {
		return true;
	}
	
	/**
	 * 退出联盟行军处理
	 */
	@Override
	default void doQuitGuild(String guildId) {
		
	}
	
	@Override
	default void targetMoveCityProcess(Player targetPlayer, long currentTime) {
	}
	
	@Override
	default boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		return true;
	}
	
	@Override
	default void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
	}
	
	/**
	 * 刷新采集相关任务
	 */
	default void refreshCollectMission(Player player, AwardItems awardItems, WorldMarch worldMarch) {
		if (awardItems == null || awardItems.getAwardItems().size() <= 0) {
			return;
		}
		ResourceCollectEvent event = new ResourceCollectEvent(player.getId());
		for (ItemInfo itemInfo : awardItems.getAwardItems()) {
			final int itemId = itemInfo.getItemId();
			final int count = (int) itemInfo.getCount();
			// 刷新任务
			MissionManager.getInstance().postMsg(player, new EventResourceCollectCount(itemId, count));
			// 联盟任务
			GuildService.getInstance().postGuildTaskMsg(new ResourceCollectCountEvent(player.getGuildId(), itemId, count));
			int resWeight = WorldMarchConstProperty.getInstance().getResWeightByType(itemId);
			event.addCollectResource(itemId, count, resWeight);
		}

		// 计算此次采集的时间
		if(worldMarch.getResStartTime() != 0){
			int costTime = (int) ((HawkTime.getMillisecond() - worldMarch.getResStartTime()) / 1000);
			event.setCollectTime(costTime);
		}		
		ActivityManager.getInstance().postEvent(event);
		// 跨服消息投递-资源采集
		CrossActivityService.getInstance().postEvent(event);
	}
	
	/**
	 * 发送采集相关邮件
	 * 
	 * @param march
	 */
	default void sendCollectMail(WorldMarch march, String hasTax) {
		// 发送邮件---采集成功邮件
		if (march.getMarchType() == WorldMarchType.MANOR_COLLECT_VALUE) { // 超级矿
			CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march, MailId.COLLECT_SUPERMINE_SUCC_VALUE, true);
			CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId()).setMailId(MailId.COLLECT_SUPERMINE_SUCC).addContents(builder).build());
		} else { // 普通资源
			CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march, MailId.COLLECT_SUCC_VALUE, true);
			if (hasTax != null) {
				if (hasTax.equals(GsConfig.getInstance().getServerId())) {
					builder.setTax(CrossConstCfg.getInstance().getTaxRateOwnServer());
				} else {
					builder.setTax(CrossConstCfg.getInstance().getTaxRate());
					builder.setIsCrossTax(1);
				}
				
			}
			CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId()).setMailId(MailId.COLLECT_SUCC).addContents(builder).build());
		}
	}
	
	default void refreshPowerAfterWar(List<Player> atkPlayers, List<Player> defPlayers) {
		if (atkPlayers != null && !atkPlayers.isEmpty()) {
			for (Player atkPlayer : atkPlayers) {
				if (!atkPlayer.isActiveOnline()) {
					continue;
				}
				atkPlayer.refreshPowerElectric(PowerChangeReason.WARFARE_ATK);
			}
		}
		
		if (defPlayers != null && !defPlayers.isEmpty()) {
			for (Player defPlayer : defPlayers) {
				if (!defPlayer.isActiveOnline()) {
					continue;
				}
				defPlayer.refreshPowerElectric(PowerChangeReason.WARFARE_DEF);
			}
		}
	}
	
	@Override
	default Set<IWorldMarch> getQuarterMarch() {
		return Collections.emptySet();
	}
}
