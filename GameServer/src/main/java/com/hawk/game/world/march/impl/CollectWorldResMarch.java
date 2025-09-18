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
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.HeroResourceCollectEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.WorldFieldCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventAttackCollectTimes;
import com.hawk.game.service.mssion.event.EventResourceCollectBegin;
import com.hawk.game.service.mssion.event.EventWorldCollectStart;
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
import com.hawk.game.world.march.submarch.CollectMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResourceService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.DefenderIdentity;
import com.hawk.log.Source;

/**
 * 采集行军
 * 
 * @author zhenyu.shang
 * @since 2017年8月25日
 */
public class CollectWorldResMarch extends PlayerMarch implements CollectMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public CollectWorldResMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public boolean marchHeartBeats(long currTime) {
		// 采集资源结束
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			// 采集结束的处理
			if (getMarchEntity().getMarchType() == WorldMarchType.COLLECT_RESOURCE_VALUE) {
				// 采集结算
				if (getMarchEntity().getResEndTime() != 0 && getMarchEntity().getResEndTime() <= currTime) {
					doCollectRes(false);
					return true;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.COLLECT_RESOURCE;
	}

	/**
	 * 对当前行军拥有的信息进行资源采集结算
	 * 
	 * @return true代表已采集完成并开始行军回程
	 */
	public boolean doCollectRes(boolean changeSpeed) {
		long currentTime = HawkTime.getMillisecond();
		if (this.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			WorldMarchService.logger.warn("doCollectRes error, march status error, march:{}", this.getMarchEntity().toString());
			return false;
		}
		
		// 没有采集时间差
		if (getMarchEntity().getResEndTime() - getMarchEntity().getResStartTime() < 0) {
			return false;
		}
		Player player = GlobalData.getInstance().makesurePlayer(getPlayerId());
		if (player == null) {
			return false;
		}
		// 计算
		WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, Integer.parseInt(getMarchEntity().getTargetId()));
		if (cfg == null) {
			return false;
		}
		int resType = cfg.getResType();

		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getTerminalId());
		if (worldPoint == null) {
			WorldMarchService.logger.error("collect resource march error, point is null, marchData: {}", getMarchEntity());
			WorldMarchService.getInstance().onMarchReturn(this, currentTime, 0);
			return false;
		}

		// 结算额外奖励tick时间
		int extraAwardTickTime = player.getEffect().getEffVal(Const.EffType.RES_EXTRA_AWARD, getMarchEntity().getEffectParams());
		if (extraAwardTickTime > 0) {
			// 采集时长
			long collectTime = currentTime - getMarchEntity().getResStartTime();
			// tick次数
			int times = (int) ((collectTime / 1000) / extraAwardTickTime);
			// 额外奖励id
			int extraAwardId = ConstProperty.getInstance().getEffect366LinkToAwardId();
			AwardItems extraAward = AwardItems.valueOf();
			for (int i = 0; i < times; i++) {
				extraAward.addAward(extraAwardId);
			}
			// 366作用号，采集每x时间给一份奖励。
			getMarchEntity().setAwardExtraStr(extraAward.toString());
		}
		
		// 设置最终采集的时长
		if (getMarchEntity().getResStartTime() > 0) {
			getMarchEntity().setMassReadyTime(currentTime - getMarchEntity().getResStartTime());
		}
		
		//计算物品产出
		//先计算出tick周期
		WorldFieldCfg worldFieldCfg = HawkConfigManager.getInstance().getCombineConfig(WorldFieldCfg.class, resType, player.getCityLevel());
		long offsetTime = currentTime - getMarchEntity().getLastExploreTime();
		
		int count = (int) (offsetTime / (worldFieldCfg.getTicktime() * 1000));
		int yu = (int) (offsetTime % (worldFieldCfg.getTicktime() * 1000));
		if (worldFieldCfg.getAward() != 0) {
			if(count > 0){
				for (int i = 0; i < count; i++) {
					getMarchEntity().getAwardItems().addAward(worldFieldCfg.getAward());
				}
			}
			//按余数计算概率
			if(yu > HawkRand.randInt(0, worldFieldCfg.getTicktime() * 1000)) {
				//再给一次奖励
				getMarchEntity().getAwardItems().addAward(worldFieldCfg.getAward());
			}
		}
		//重置时间
		getMarchEntity().setLastExploreTime(currentTime);
		
		// 采集速度
		double speed = getMarchEntity().getCollectSpeed();
		if (changeSpeed) {
			speed = WorldUtil.getCollectSpeed(player, resType, cfg.getLevel(), worldPoint, getMarchEntity().getEffectParams());
		}
		// 负重
		long totalLoad = WorldMarchService.getInstance().getArmyCarryResNum(player, getMarchEntity().getArmys(), resType, this.getMarchEntity().getEffectParams());
		// 剩余量
		double remain = worldPoint.getRemainResNum();
		// 可采集量
		double canCollect = Math.max(1.0f, Math.min(totalLoad, remain));
		// 已采集数量
		double alreadyCollect = getCollectResNum(player, currentTime, resType, speed);
		
		// 已采满或采集完
		if (alreadyCollect >= canCollect) {
			int realCollect = (int) canCollect;
			if (canCollect >= cfg.getResNum()) { // 完整采集完某资源点后，额外获取 XX% 的资源比率
				realCollect = (int) (realCollect * (1 + player.getEffect().getEffVal(EffType.EFF_335,getMarchEntity().getEffectParams()) * GsConst.EFF_PER));
			}
			
			WorldMarchService.getInstance().resetCollectResource(getMarchEntity(), (long) realCollect, resType);
			WorldMarchService.getInstance().onMarchReturn(this, currentTime, 0);
			WorldResourceService.getInstance().notifyResourceGather(worldPoint, (int)realCollect);
			LogUtil.logWorldCollect(player, cfg.getId(), cfg.getResType(), cfg.getLevel(), (int)realCollect, HawkTime.getMillisecond() - this.getMarchEntity().getResStartTime());
			
			do{
				//fix bug ID58095694 采集资源量未按照资源价值(负重)进行换算 
 				int typeWeight = WorldMarchConstProperty.getInstance().getResWeightByType(cfg.getResType());
				double resWieght = realCollect * typeWeight;
				GuildRankMgr.getInstance().onPlayerGather(player.getId(), player.getGuildId(), (long)resWieght);
			}while(false);
		
			return true;
		}
		
		// 重置采集速度
		if (changeSpeed) {
			getMarchEntity().setCollectSpeed(speed);
			getMarchEntity().setCollectBaseSpeed(WorldUtil.getCollectBaseSpeed(player, resType, getMarchEntity().getEffectParams()));
		}
		// 重置采集结束时间
		long resStartTime = getMarchEntity().getResStartTime();
		long collectTime = WorldMarchService.getInstance().getTimeByResource(canCollect, speed);
		getMarchEntity().setResEndTime(resStartTime + collectTime);

		WorldMarchService.getInstance().resetCollectResource(getMarchEntity(), (int) alreadyCollect, resType);
		WorldMarchService.logger.info("world march collect resource, marchData: {}", getMarchEntity());
		return false;
	}

	/**
	 * 获得已采集的资源数量
	 * @param player
	 * @param currentTime
	 * @param resType
	 * @param speed
	 * @return
	 */
	public long getCollectResNum(Player player, long currentTime, int resType, double speed) {
		long collectTime = currentTime - getMarchEntity().getResStartTime();
		double collectCount = Math.ceil(collectTime / 1000d) * speed;
		return (long) collectCount;
	}

	@Override
	public void onMarchReach(Player player) {
		marchReach(player);
		rePushPointReport();
	}

	private void marchReach(Player player) {
		WorldMarch march = getMarchEntity();
		// check
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (point == null) {
			WorldMarchService.logger.error("collect resource error: point is null, terminalId:{}", march.getTerminalId());
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			// 发送邮件---采集失败：资源点消失
			CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march,
					MailId.COLLECT_FAILED_TARGET_DISAPPEAR_VALUE, false);
			CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
					.setMailId(MailId.COLLECT_FAILED_TARGET_DISAPPEAR).addContents(builder).build());
			return;
		}
		int pointType = point.getPointType();
		// check
		if (pointType != WorldPointType.RESOURCE_VALUE
				|| point.getResourceId() != Integer.parseInt(march.getTargetId())) {
			WorldMarchService.logger.error(
					"collect resource error: not resource point or resource id error, x:{}, y:{}, pointType:{}, pointResourceId:{}, targetId:{}",
					point.getX(), point.getY(), pointType, point.getResourceId(), march.getTargetId());
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			// 发送邮件---采集失败：资源点状态变更
			CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march,
					MailId.COLLECT_SUPERMIN_TARGET_CHANGED_VALUE, false);
			CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
					.setMailId(MailId.COLLECT_SUPERMIN_TARGET_CHANGED).addContents(builder).build());
			return;
		}

		// 点已经被自己占领
		if (point.getPlayerId() != null && point.getPlayerId().equals(player.getId())) {
			WorldMarchService.logger.error("collect resource error: point is occupy by own");
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			// 发送邮件---采集失败：资源点状态变更
			CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march,
					MailId.COLLECT_SUPERMIN_TARGET_CHANGED_VALUE, false);
			CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
					.setMailId(MailId.COLLECT_SUPERMIN_TARGET_CHANGED).addContents(builder).build());
			return;
		}

		int resourceId = point.getResourceId();
		WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, resourceId);
		MissionManager.getInstance().postMsg(player, new EventWorldCollectStart(cfg.getResType()));

		// 尝试占领世界点, 占领失败返回null
		WorldPoint pointOccupied = WorldPointService.getInstance().notifyPointOccupied(point.getId(), player, this, WorldPointType.RESOURCE);
		if (pointOccupied != null) {
			point = pointOccupied;
		}

		// 判断是否需要战斗
		boolean needFight = false;
		if (!HawkOSOperator.isEmptyString(point.getMarchId()) && !point.getPlayerId().equals(player.getId())) {
			WorldMarch targetMarch = WorldMarchService.getInstance().getWorldMarch(point.getMarchId());
			if (targetMarch == null) {
				WorldPointService.getInstance().notifyPointOccupied(point.getId(), player, this,
						WorldPointType.RESOURCE);
			} else {
				if (GuildService.getInstance().isPlayerInGuild(player.getGuildId(), targetMarch.getPlayerId())) {
					WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
					// 发送邮件---采集失败：已被同盟玩家占领
					CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march,
							MailId.COLLECT_FAILED_ALLIED_OCCUPY_VALUE, false);
					CollectMailService.getInstance()
							.sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
									.setMailId(MailId.COLLECT_FAILED_ALLIED_OCCUPY).addContents(builder).build());
					return;
				} else {
					needFight = true;
				}
			}
		}

		// 处理结果
		boolean fightResult = true;
		if (needFight) {
			// 组织战斗数据，计算战斗结果
			IWorldMarch targetMarch = WorldMarchService.getInstance().getMarch(point.getMarchId());
			Player defPlayer = GlobalData.getInstance().makesurePlayer(targetMarch.getPlayerId());
			List<Player> atkPlayers = new ArrayList<>();
			atkPlayers.add(player);
			List<Player> defPlayers = new ArrayList<>();
			defPlayers.add(defPlayer);

			List<IWorldMarch> atkMarchs = new ArrayList<>();
			atkMarchs.add(this);
			List<IWorldMarch> defMarchs = new ArrayList<>();
			defMarchs.add(targetMarch);

			BattleConst.BattleType battleType = BattleConst.BattleType.ATTACK_RES;
			PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(battleType, point.getId(), atkPlayers, defPlayers, atkMarchs, defMarchs);
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
			boolean isAtkWin = battleOutcome.isAtkWin();
			fightResult = battleOutcome.isAtkWin();

			Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
			Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();

			List<ArmyInfo> atkLeftArmyList = atkArmyLeftMap.get(player.getId());
			List<ArmyInfo> defLeftArmyList = defArmyLeftMap.get(defPlayer.getId());

			WorldMarchService.getInstance().sendBattleResultInfo(this, isAtkWin, atkLeftArmyList, defLeftArmyList, isAtkWin);
			// 记录玩家战斗安全日志
			LogUtil.logSecBattleFlow(player, defPlayer, "", DefenderIdentity.RES_FIELD, isAtkWin, null, null, atkLeftArmyList, defLeftArmyList, 0, march, false);
			Map<String, long[]> grabResMap = null;
			if (isAtkWin) {
				// 对方若无兵就直接回巢，有兵则带资源铩羽
				WorldMarchService.getInstance().onResourceMarchCallBack(targetMarch, march.getReachTime(), defLeftArmyList, 0);

				// 还有兵力存活
				if (WorldUtil.calcSoldierCnt(atkLeftArmyList) > 0 && !point.isInvalid()) {
					// 攻方胜利，留下继续采集
					this.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE, atkLeftArmyList, point);
					WorldPointService.getInstance().notifyPointOccupied(point.getId(), player, this, WorldPointType.RESOURCE);
				} else {
					// 攻方没有兵力， 直接返回
					WorldMarchService.getInstance().onMarchReturn(this, atkLeftArmyList, point.getId());
				}
				
				// 367掠夺资源作用号
				int preyRes = player.getEffect().getEffVal(EffType.PREY_RES, getMarchEntity().getEffectParams());
				if (preyRes > 0) {
					long targetCollectNum = ((CollectWorldResMarch)targetMarch).getCollectResNum(defPlayer, march.getReachTime(), cfg.getResType(), targetMarch.getMarchEntity().getCollectSpeed());
					int preyNum = (int) (targetCollectNum * preyRes / GsConst.EFF_RATE);
					long[] hasResAry = new long[GsConst.RES_TYPE.length];
					for(int i = 0;i< GsConst.RES_TYPE.length;i++){
						if(GsConst.RES_TYPE[i] == cfg.getResType()){
							hasResAry[i] = preyNum;
						}
					}
					int grabWeight = WorldUtil.calcTotalWeight(getPlayer(),atkLeftArmyList, march.getEffectParams());
					grabResMap = BattleService.getInstance().calcGrabRes(atkPlayers, new int[] { grabWeight }, hasResAry);
					
					// 直接发掉
					AwardItems awardItems = AwardItems.valueOf();
					long[] grabsResArr = grabResMap.get(player.getId());
					for (int i = 0; i < GsConst.RES_TYPE.length; i++) {
						int resCount = (int) grabsResArr[i];
						int itemId = GsConst.RES_TYPE[i];
						if (resCount > 0) {
							awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, itemId, resCount);
							int typeWeight = WorldMarchConstProperty.getInstance().getResWeightByType(itemId);
							int grebRes = resCount * typeWeight;
							LocalRedis.getInstance().incrementGrabResWeightDayCount(march.getPlayerId(),itemId, grebRes);
						}
					}
					awardItems.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_REACH_COLLECT);
				}
				
				if (cfg != null) {
					MissionManager.getInstance().postMsg(player, new EventResourceCollectBegin(resourceId, cfg.getLevel()));
				}								
				
			} else {
				// 防守方胜利。 有兵力继续采集 没有返回
				if (WorldUtil.calcSoldierCnt(defLeftArmyList) > 0) {
					onMarchFightWin(defPlayer, march.getReachTime(), targetMarch, defLeftArmyList);
				} else {
					WorldMarchService.getInstance().onMarchReturn(targetMarch, defLeftArmyList, point.getId());
				}
				// 攻方失败返回
				WorldMarchService.getInstance().onMarchReturn(this, atkLeftArmyList, point.getId());
				CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march, MailId.COLLECT_FAILED_ENEMY_OCCUPY_VALUE, false);
				CollectMailService.getInstance()
						.sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId()).setMailId(MailId.COLLECT_FAILED_ENEMY_OCCUPY).addContents(builder).build());
			}

			// 发送战斗邮件
			FightMailService.getInstance().sendFightMail(pointType, battleIncome, battleOutcome, grabResMap);
			BattleService.getInstance().dealWithPvpBattleEvent(battleIncome, battleOutcome, isMassMarch(), this.getMarchType());
			// 处理任务、统计等
			sendMsgUpdateDefPlayerAfterWar(defPlayer, battleOutcome, null);
			int constrFactorLvl = defPlayer.getCityLevel(); // 敌方大本等级
			sendMsgUpdateAtkPlayerAfterWar(isAtkWin, atkLeftArmyList, constrFactorLvl, player, battleOutcome);

			MissionManager.getInstance().postMsg(player, new EventAttackCollectTimes(point.getResourceId(), isAtkWin));
			// 行为日志
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_COLLECT,
					Params.valueOf("marchData", march),
					Params.valueOf("needFight", needFight),
					Params.valueOf("isAtkWin", isAtkWin),
					Params.valueOf("atkLeftArmyList", atkLeftArmyList),
					Params.valueOf("defLeftArmyList", defLeftArmyList));
			
			// 刷新战力
			refreshPowerAfterWar(atkPlayers, defPlayers);
			
		}
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_COLLECT,
				Params.valueOf("marchData", march), Params.valueOf("needFight", needFight));
		if (!march.getHeroIdList().isEmpty() && fightResult ) {
			ActivityManager.getInstance().postEvent(new HeroResourceCollectEvent(player.getId()));
		}
		// 查找朝向目标点行军的
		Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		for (IWorldMarch iWorldMarch : worldPointMarchs) {
			iWorldMarch.updateMarch();
		}
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		WorldMarch march = getMarchEntity();
		
		long currentTime = HawkTime.getMillisecond();
		Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
		WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, Integer.parseInt(march.getTargetId()));
		
		// 负重
		double totalLoad = WorldMarchService.getInstance().getArmyCarryResNum(player, march.getArmys(), cfg.getResType(), this.getMarchEntity().getEffectParams());
		// 剩余量
		double remain = targetPoint.getRemainResNum();
		// 可采集量
		double canCollect = Math.max(1.0f, Math.min(totalLoad, remain));
		// 采集速度
		double speed = WorldUtil.getCollectSpeed(player, cfg.getResType(), cfg.getLevel(), targetPoint, march.getEffectParams());
		// 采集时间
		long time = WorldMarchService.getInstance().getTimeByResource(canCollect, speed);
		
		march.setResStartTime(currentTime);
		march.setLastExploreTime(currentTime);
		march.setResEndTime(march.getResStartTime() + time);
		march.setCollectSpeed(speed);
		march.setCollectBaseSpeed(WorldUtil.getCollectBaseSpeed(player, cfg.getResType(), getMarchEntity().getEffectParams()));
		
		AwardItems items = AwardItems.valueOf();
		items.addNewItem(Const.ItemType.PLAYER_ATTR_VALUE, cfg.getResType(), 0);
		march.setAwardItems(items);
		
		WorldMarchService.logger.info("world march stop to collect, marchData: {}", march);
	}

	/**
	 * 战斗胜利，目前只有采集使用
	 * 
	 * @param march
	 * @param defLeftArmyList
	 */
	private void onMarchFightWin(Player player, long currentTime, IWorldMarch march, List<ArmyInfo> defLeftArmyList) {
		// 胜利方判断剩余兵力和负重,如果已采集的资源超过剩余负重,则带兵回巢,否则继续采集到上限
		march.getMarchEntity().setArmys(defLeftArmyList);
		// 采集
		if (march.getMarchEntity().getMarchType() == WorldMarchType.COLLECT_RESOURCE_VALUE) {
			if (!march.doCollectRes(false)) {
				march.updateMarch();
			}
		}
		WorldMarchService.logger.info("world march fight win, marchData: {}", march);
	}

	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		// 队长位置
		int[] pos = GameUtil.splitXAndY(this.getMarchEntity().getTerminalId());
		builder.setPointType(WorldPointType.RESOURCE);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pos[0], pos[1]);
		if (worldPoint != null && !HawkOSOperator.isEmptyString(worldPoint.getMarchId()) && worldPoint.getPointType() == WorldPointType.RESOURCE_VALUE) {
			builder.setLeaderMarch(getCollectGuildWarSingleInfo(this.getMarchEntity()));
		}
		
		// 队长
		String leaderId = worldPoint.getPlayerId();
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		if (leader != null && !HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());
			builder.setGuildTag(guildTag);
		}
		return builder;
	}

	/**
	 * 联盟战争界面里单人信息
	 * 
	 * @param worldMarch
	 * @return
	 */
	public GuildWarSingleInfo.Builder getCollectGuildWarSingleInfo(WorldMarch worldMarch) {
		// 资源点
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(worldMarch.getTerminalId());
		// 资源点上的行军
		WorldMarch resMarch = WorldMarchService.getInstance().getMarch(worldPoint.getMarchId()).getMarchEntity();
		Player player = GlobalData.getInstance().makesurePlayer(resMarch.getPlayerId());
		GuildWarSingleInfo.Builder builder = GuildWarSingleInfo.newBuilder();
		builder.setPlayerId(resMarch.getPlayerId());
		builder.setPlayerName(player.getName());
		builder.setIconId(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		List<PlayerHero> heros = player.getHeroByCfgId(resMarch.getHeroIdList());
		if (heros != null && !heros.isEmpty()) {
			for (PlayerHero hero : heros) {
				builder.addHeroInfo(hero.toPBobj());
			}
		}
		List<ArmyInfo> armys = resMarch.getArmys();
		for (ArmyInfo army : armys) {
			builder.addArmys(army.toArmySoldierPB(player));
		}
		return builder;
	}

	@Override
	public boolean needShowInGuildWar() {
		int terminalId = this.getMarchEntity().getTerminalId();
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		if (worldPoint == null) {
			return false;
		}
		String marchId = worldPoint.getMarchId();
		if (HawkOSOperator.isEmptyString(marchId) || WorldMarchService.getInstance().getWorldMarch(marchId) == null) {
			return false;
		}
		String targerPlayerId = worldPoint.getPlayerId();
		if (HawkOSOperator.isEmptyString(targerPlayerId)) {
			return false;
		}
		String tarGuildId = GuildService.getInstance().getPlayerGuildId(targerPlayerId);
		String ownGuildId = GuildService.getInstance().getPlayerGuildId(this.getPlayerId());
		if (HawkOSOperator.isEmptyString(ownGuildId) && HawkOSOperator.isEmptyString(tarGuildId)) {
			return false;
		}
		if (!HawkOSOperator.isEmptyString(ownGuildId)
				&& !HawkOSOperator.isEmptyString(tarGuildId)
				&& tarGuildId.equals(ownGuildId)) {
			return false;
		}
		return true;
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
		// 不是同联盟且不是回程的行军，才会处理
		this.pullAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		rePushPointReport();
	}

	@Override
	public void onMarchReturn() {
		rePushPointReport();
	}

	private void rePushPointReport() {
		// 删除行军报告
		removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public Set<String> attackReportRecipients() {
		Set<String> result = alarmPointEnemyMarches().stream()
				.filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE)
				.map(IWorldMarch::getPlayerId)
				.filter(tid -> !Objects.equals(tid, this.getMarchEntity().getPlayerId()))
				.collect(Collectors.toSet());
		return result;
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
		for (IWorldMarch targetMarch : alarmPointEnemyMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport(playerId);
			}
		}
	}
	
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		// 采集召回
		WorldMarchService.getInstance().onResourceMarchCallBack(this, callbackTime, getMarchEntity().getArmys(), 0);
	}
	
	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		// 无兵行军不进行资源结算， 直接返回
		if (WorldUtil.getFreeArmyCnt(getMarchEntity().getArmys()) <= 0) {
			ArmyService.getInstance().onArmyBack(player, getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(),getMarchEntity().getSuperSoldierId(), this);
			return false;
		}
		doCollectRes(false); // 采集资源结算
		
		WorldMarchService.getInstance().calcExtraDrop2(this);
		WorldMarchService.getInstance().calcExtraDrop3(this);
		
		AwardItems items = getMarchEntity().getAwardItems();
		if (items != null && items.getAwardItems().size() > 0) {
			refreshCollectMission(player, items, getMarchEntity()); // 刷新任务
			
			// 交税
			String hasTax = CrossActivityService.getInstance().addTax(player, items);
			
			// 发邮件
			int[] pos = GameUtil.splitXAndY(getMarchEntity().getTerminalId());
			CollectMail.Builder builder = MailBuilderUtil.createCollectMail(getMarchEntity(), MailId.COLLECT_SUCC_VALUE, true);
			builder.setX(pos[0]);
			builder.setY(pos[1]);
			if (hasTax != null) {
				if (hasTax.equals(GsConfig.getInstance().getServerId())) {
					builder.setTax(CrossConstCfg.getInstance().getTaxRateOwnServer());
				} else {
					builder.setTax(CrossConstCfg.getInstance().getTaxRate());
					builder.setIsCrossTax(1);
				}
				
			}
			
			CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(getMarchEntity().getPlayerId()).setMailId(MailId.COLLECT_SUCC).addContents(builder).build());
			
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

		// 通知资源采集
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getTerminalId());
		if (worldPoint != null) {
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId()) && worldPoint.getPlayerId().equals(getPlayerId())) {
				WorldResourceService.getInstance().notifyResourceGather(worldPoint, WorldMarchService.getInstance().getMarchCollectResource(getMarchEntity()));
			}
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
}
