package com.hawk.game.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.StoryMissionCfg;
import com.hawk.game.config.StoryMissionChaptCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.MissionMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.StoryMission.StoryMissionKillMosterReq;
import com.hawk.game.protocol.StoryMission.StoryMissionRewReq;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventGenOldMonsterMarch;
import com.hawk.game.service.mssion.type.IMission;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.impl.AttackMonster;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;

public class PlayerStoryMissionModule extends PlayerModule {

	public PlayerStoryMissionModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		StoryMissionService.getInstance().checkMissionCfgUpdate(player);
		checkMissionFinish();
		// 先游服合并处理
		doExpensionMerge();
		
		checkParalleledChapterMission();
		
		StoryMissionService.getInstance().checkChapterComplete(player, player.getData().getStoryMissionEntity());
		
		player.getPush().syncStoryMissionInfo();
		return true;
	}

	/**
	 * 检测分支任务是否需要开启
	 */
	private void checkParalleledChapterMission() {
		try {
			boolean isNewly = false;
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
			if (accountInfo !=null && accountInfo.isNewly()) {
				isNewly = true;
			}
			
			// 判断之前是否已经处理过
			String redisKey = "story_mission_paralleled" + ":" + player.getId();
			HawkRedisSession redis = RedisProxy.getInstance().getRedisSession();
			if (isNewly) {
				redis.setString(redisKey, String.valueOf(HawkTime.getMillisecond()));
				return;
			}
			
			String redisInfo = redis.getString(redisKey);
			if (!HawkOSOperator.isEmptyString(redisInfo)) {
				return;
			}
			redis.setString(redisKey, String.valueOf(HawkTime.getMillisecond()));
			
			int paralledChapterId = StoryMissionChaptCfg.getParalledChapterId();
			if (paralledChapterId == 0) {
				return;
			}
			
			StoryMissionEntity entity = player.getData().getStoryMissionEntity();
			if (entity.getChapterId() <= paralledChapterId) {
				return;
			}
			
			if (entity.getParalleledChapterMission() != null) {
				return;
			}
			
			StoryMissionChaptCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, paralledChapterId);
			if (cfg == null || cfg.getNextChapterIds().size() <= 1) {
				return;
			}
			
			List<Integer> chapterIds = cfg.getNextChapterIds();
			if (entity.getChapterId() >= chapterIds.get(1)) {
				return;
			}
			
			// 开启平行的分支任务
			StoryMissionService.getInstance().refreshParalleledChapterMission(player, entity, chapterIds.get(1));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 领取剧情任务奖励
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.STORY_MISSION_REWARD_C_VALUE)
	private boolean getStoryMissionReward(HawkProtocol protocol) {
		StoryMissionRewReq req = protocol.parseProtocol(StoryMissionRewReq.getDefaultInstance());
		// 检测配置更新
		StoryMissionService.getInstance().checkMissionCfgUpdate(player);
		int chapterId = req.hasChapterId() ? req.getChapterId() : 0;
		return StoryMissionService.getInstance().syncStoryMissionReward(player, req.getIsChapterAward(), req.getMissionId(), chapterId);  
	}

	/**
	 * 刷新剧情任务
	 * 
	 * @param msg
	 */
	@MessageHandler
	private void onRefreshMission(MissionMsg msg) {
		MissionEvent event = msg.getEvent();
		
		// 事件触发任务列表
		List<MissionType> touchMissions = event.touchMissions();
		if (touchMissions == null || touchMissions.isEmpty()) {
			return;
		}
		
		// 章节任务配置
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		Map<Integer, StoryMissionCfg> cfgs = AssembleDataManager.getInstance().getStoryMissionCfg(entity.getChapterId());
		if (entity.getParalleledChapterMission() != null) {
			cfgs = new HashMap<>();
			cfgs.putAll(AssembleDataManager.getInstance().getStoryMissionCfg(entity.getChapterId()));
			cfgs.putAll(AssembleDataManager.getInstance().getStoryMissionCfg(entity.getParalleledChapterMission().getChapterId()));
		}

		for (StoryMissionCfg cfg : cfgs.values()) {
			// 任务类型
			MissionType missionType = MissionType.valueOf(cfg.getType());
			
			// 不触发此类型任务
			if (!touchMissions.contains(missionType)) {
				continue;
			}
			
			// 任务实体
			MissionEntityItem entityItem = entity.getStoryMissionItem(cfg.getId());
			if (entityItem == null) {
				continue;
			}
			
			if (entityItem.getState() != MissionState.STATE_NOT_FINISH) {
				continue;
			}
			
			// 刷新任务
			IMission iMission = MissionContext.getInstance().getMissions(missionType);
			iMission.refreshMission(player.getData(), event, entityItem, cfg.getMissionCfgItem());
			
			// 设置任务状态(这里要设置一下，调用下entity的set方法，不然可能不会落地)
			entity.setStoryMissionItem(entityItem);
			
			if (entityItem.getState() == MissionState.STATE_FINISH) {
				StoryMissionService.getInstance().logTaskFlow(player, cfg, entityItem.getState());
				StoryMissionService.getInstance().checkChapterComplete(player, entity);
			}			
			
			// 同步
			player.getPush().syncStoryMissionInfo();
		}
	}
	
	/**
	 * 击杀剧情野怪
	 * @param hawkProtocol
	 */
	@ProtocolHandler(code = HP.code.STORY_MISSION_KILL_MOSTER_REQ_VALUE)
	public void killStoryMoster(HawkProtocol hawkProtocol) {
		StoryMissionKillMosterReq cparam = hawkProtocol.parseProtocol(StoryMissionKillMosterReq.getDefaultInstance());
		int monsterId = cparam.getMonsterId();
		WorldEnemyCfg worldEnemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		if (worldEnemyCfg == null) {
			this.sendError(hawkProtocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		// 体力消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, worldEnemyCfg.getCostPhysicalPower());
		// 体力不足
		if (!consumeItems.checkConsume(player, hawkProtocol.getType())) {
			HawkLog.warnPrintln("world attack story monster failed, vit not enough vit:{}", player.getVit());
			return ;
		}
		
//		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
//		Map<Integer, StoryMissionCfg> cfgs = AssembleDataManager.getInstance().getStoryMissionCfg(entity.getChapterId());
//		StoryMissionCfg cfg = null;
//		boolean hasKillTask = false;
//		MissionCfgItem mci = null;
//		for (Entry<Integer, StoryMissionCfg> entry : cfgs.entrySet()) {
//			cfg = entry.getValue();
//			MissionType missionType = MissionType.valueOf(cfg.getType());
//			mci = cfg.getMissionCfgItem();
//			if (missionType == MissionType.GEN_OLD_MONSTER_MARCH && (mci.getIds().get(0) == 0 || mci.getIds().get(0) == worldEnemyCfg.getLevel())) {
//				hasKillTask = true;
//				break;
//			}
//		}
		
		// 扣除体力
		consumeItems.consumeAndPush(player, Action.STORY_MISSION_VIT_COST);
				
//		if (!hasKillTask) {
//			HawkLog.warnPrintln("storyMissionKillMonster has no task kill monster playerId:{}", player.getId());
//			this.sendError(hawkProtocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
//			return;
//		}
		
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.FAKE_MONSTER_KILL_TIMES);
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(GsConst.FAKE_MONSTER_KILL_TIMES, 0, "");
		}
		if (customData != null && customData.getValue() >= ConstProperty.getInstance().getFakeMonsterMaxKillTime()) {
			HawkLog.warnPrintln("storyMissionKillMonster touch limit, playerId: {}", player.getId());
			this.sendError(hawkProtocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		if (customData != null) {
			customData.setValue(customData.getValue() + 1);
		}
		
		killStoryMonster(worldEnemyCfg, new int[]{cparam.getPosX(), cparam.getPosY()});
	}
	
	public void killStoryMonster(WorldEnemyCfg worldEnemyCfg, int[] pos) {		
		// 组织战斗数据		
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		List<ArmyEntity> armyEntityList = player.getData().getArmyEntities();
		ArmyInfo armyInfo = null;
		for (ArmyEntity armyEntity : armyEntityList) {
			armyInfo = new ArmyInfo();
			armyInfo.setArmyId(armyEntity.getArmyId());
			armyInfo.setTotalCount(armyEntity.getTotal());
			armyList.add(armyInfo);
		}
		List<Integer> heroIdList = player.getAllHero().stream().map(playerHero->playerHero.getCfgId()).collect(Collectors.toList());		
		WorldMarch worldMarch = new WorldMarch();
		worldMarch.setMarchId(HawkUUIDGenerator.genUUID());
		worldMarch.setArmys(armyList);
		worldMarch.setHeroIdList(heroIdList);
		worldMarch.setPlayerId(player.getId());
		AttackMonster march = new AttackMonster(worldMarch);
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();			
		atkMarchs.add(march);
		atkPlayers.add(player);					
		// 战斗
		PveBattleIncome battleIncome = BattleService.getInstance().initMonsterBattleData(BattleConst.BattleType.ATTACK_MONSTER,
				GameUtil.combineXAndYCacheIndex(pos[0], pos[1]), worldEnemyCfg.getId(), atkPlayers, atkMarchs);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);

		// 战斗结果
		boolean isWin = battleOutcome.isAtkWin();
		List<ArmyInfo> afterArmyList = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		
		// 更新击杀野怪等级
		boolean isFirst = false;
		if (worldEnemyCfg.getLevel() > player.getData().getMonsterEntity().getMaxLevel()) {
			isFirst = true;
		}
		
		WorldPoint point = new WorldPoint();
		point.setX(pos[0]);
		point.setY(pos[1]);
		// 结果处理
		march.doAtkMonsterResult(point, atkPlayers.get(0), battleOutcome, heroIdList, worldEnemyCfg, false);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.STORY_MISSION_KILL_MONSTER, Params.valueOf("marchData", march),
				Params.valueOf("atkLeftArmyList", afterArmyList), Params.valueOf("isWin", isWin));
		
		LogUtil.logAttackMonster(player, point.getX(), point.getY(), worldEnemyCfg.getType(), worldEnemyCfg.getId(), worldEnemyCfg.getLevel(), 1, 1, 0, 0, isWin, isFirst, true);
		
		GuildRankMgr.getInstance().onPlayerKillMonster(player.getId(), player.getGuildId(),1 );
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.WARFARE_ATK);
		//这个杀怪只能在这里手动调用
		this.onRefreshMission(MissionMsg.valueOf(new EventGenOldMonsterMarch(worldEnemyCfg.getLevel())));
	}
	
	/**
	 * 先游版本合并
	 */
	private void doExpensionMerge() {
		// 2022-03-01 00:00:00之后注册的玩家就不处理了，因为从那以后先游版本内容已经合并过来了。
		if (player.getEntity().getCreateTime() > 1646064000000L) {
			return;
		}
		
		try {
			boolean isNewly = false;
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
			if (accountInfo !=null && accountInfo.isNewly()) {
				isNewly = true;
			}
			
			// 判断之前是否已经处理过
			String redisKey = "expension_merge_story" + ":" + player.getId();
			HawkRedisSession redis = RedisProxy.getInstance().getRedisSession();
			if (isNewly) {
				redis.setString(redisKey, String.valueOf(HawkTime.getMillisecond()));
				return;
			}
			
			String redisInfo = redis.getString(redisKey);
			if (!HawkOSOperator.isEmptyString(redisInfo)) {
				return;
			}
			redis.setString(redisKey, String.valueOf(HawkTime.getMillisecond()));
			
			// 已完成的，重置为第13章完成
			StoryMissionEntity entity = player.getData().getStoryMissionEntity();
			if (entity.getChapterState() == MissionState.STORY_MISSION_COMPLETE) {
				entity.setChapterId(13);
				return;
			}
			
			// 10章以前特殊处理
			if (entity.getChapterId() <= 10) {
				// 刷新章节任务
				entity.setChapterId(entity.getChapterId() - 1);
				if (entity.getChapterId() <= 0) {
					StoryMissionService.getInstance().initStroyMission(player);
				} else {
					StoryMissionService.getInstance().refreshMainChapterMission(player, entity);
				}
				
				// 如果刷新以后是第四章节,重置4010和4040两个任务为已完成
				if (entity.getChapterId() == 4) {
					for (MissionEntityItem mission : entity.getMissionItems()) {
						if (mission.getCfgId() == 4010 || mission.getCfgId() == 4040) {
							mission.setState(MissionState.STATE_FINISH);
						}
					}
				}
				return;
			}
			
			// 如果是第11章,直接重置为第13章
			if (entity.getChapterId() == 11) {
				entity.setChapterId(13);
				StoryMissionService.getInstance().checkMissionCfgUpdate(player);
				return;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void checkMissionFinish() {
		// 章节任务配置
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		Map<Integer, StoryMissionCfg> cfgs = AssembleDataManager.getInstance().getStoryMissionCfg(entity.getChapterId());
		if (entity.getParalleledChapterMission() != null) {
			cfgs = new HashMap<>();
			cfgs.putAll(AssembleDataManager.getInstance().getStoryMissionCfg(entity.getChapterId()));
			cfgs.putAll(AssembleDataManager.getInstance().getStoryMissionCfg(entity.getParalleledChapterMission().getChapterId()));
		}

		for (StoryMissionCfg cfg : cfgs.values()) {
			// 任务类型
			MissionType missionType = MissionType.valueOf(cfg.getType());
			
			// 任务实体
			MissionEntityItem entityItem = entity.getStoryMissionItem(cfg.getId());
			if (entityItem == null) {
				continue;
			}
			
			if (entityItem.getState() != MissionState.STATE_NOT_FINISH) {
				continue;
			}
			
			// 刷新任务
			IMission iMission = MissionContext.getInstance().getMissions(missionType);
			iMission.checkMissionFinish(entityItem, cfg.getMissionCfgItem());
			
			if (entityItem.getState() == MissionState.STATE_FINISH) {
				// 设置任务状态(这里要设置一下，调用下entity的set方法，不然可能不会落地)
				entity.setStoryMissionItem(entityItem);
				StoryMissionService.getInstance().logTaskFlow(player, cfg, entityItem.getState());
				StoryMissionService.getInstance().checkChapterComplete(player, entity);
			}			
		}
	}
}
