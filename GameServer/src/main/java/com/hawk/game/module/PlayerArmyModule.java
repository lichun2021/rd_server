package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ArmyHurtDeathEvent;
import com.hawk.activity.event.impl.MakeTrapEvent;
import com.hawk.activity.event.impl.SoldierNumChangeEvent;
import com.hawk.activity.event.impl.TrainSoldierStartEvent;
import com.hawk.activity.event.impl.TreatArmyEvent;
import com.hawk.game.GsApp;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.data.ProtectSoldierInfo;
import com.hawk.game.data.RevengeInfo;
import com.hawk.game.data.RevengeInfo.RevengeState;
import com.hawk.game.data.RevengeSoldierInfo;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.CalcDeadArmy;
import com.hawk.game.msg.CurePlantQueueFinishMsg;
import com.hawk.game.msg.CureQueueCancelMsg;
import com.hawk.game.msg.CureQueueFinishMsg;
import com.hawk.game.msg.TrainQueueCancelMsg;
import com.hawk.game.msg.TrainQueueFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Army.HPAddSoldierReq;
import com.hawk.game.protocol.Army.HPAdvanceSoldierReq;
import com.hawk.game.protocol.Army.HPCollectSoldierReq;
import com.hawk.game.protocol.Army.HPCollectSoldierResp;
import com.hawk.game.protocol.Army.HPCureSoldierReq;
import com.hawk.game.protocol.Army.HPFireSoldierReq;
import com.hawk.game.protocol.Army.OpenSoldierPageNotice;
import com.hawk.game.protocol.Army.ProtectSoldierReceiveReq;
import com.hawk.game.protocol.Army.RevengeInfoPB;
import com.hawk.game.protocol.Army.RevengeShopItemInfo;
import com.hawk.game.protocol.Army.SoldierFirstAidReq;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Newly.NewlyDataType;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EvenntTreatArmy;
import com.hawk.game.service.mssion.event.EventSoldierAdd;
import com.hawk.game.service.mssion.event.EventSoldierTrainStart;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGPlayerSoldierNumChangeMsg;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.game.util.GsConst.MissionFunType;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.LogConst.SoldierProtectEventType;
import com.hawk.log.Source;

/**
 * 军队模块
 *
 * @author lating
 */
public class PlayerArmyModule extends PlayerModule {

    static Logger logger = LoggerFactory.getLogger("Server");
    
    /**
     * 构造函数
     *
     * @param player
     */
    public PlayerArmyModule(Player player) {
        super(player);
    }

    /**
     * 更新
     *
     * @return
     */
    @Override
    public boolean onTick() {
    	RevengeInfo revengeInfo = player.getRevengeInfo(false);
    	if (revengeInfo != null) {
    		checkRevengeShopEnd(revengeInfo);
    	}
    	
        return super.onTick();
    }

    /**
     * 玩家登陆处理(数据同步)
     */
    @Override
    protected boolean onPlayerLogin() {
    	AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
    	if (account != null && !account.isArmyFixed()) {
    		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.ARMY_FIX) {
    			@Override
    			public boolean onInvoke() {
    				WorldMarchService.getInstance().checkAndFixArmy(player, true);
    				account.setArmyFixed(true);
    				return true;
    			}
    		});
    	}
        player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
        syncProtectSoldierInfo();
        syncRevengeInfo();
        return true;
    }
    
    /**
     * 退出游戏处理
     */
    protected boolean onPlayerLogout() {
    	// 考虑到跨服，玩家下线时需要将数据重置
    	player.setProtectSoldierInfo(null);
    	player.resetRevengeInfo();
    	return super.onPlayerLogout();
    }
    
    /**
     * 同步新兵保护信息
     */
    private void syncProtectSoldierInfo() {
    	long timeLong = ConstProperty.getInstance().getRescueDuration() * 1000L;
    	if (HawkApp.getInstance().getCurrentTime() - player.getCreateTime() >= timeLong) {
     		return;
     	}
    	
    	int oldReceiveDayCount = 0;
    	ProtectSoldierInfo protectSoldierInfo = player.getProtectSoldierInfo(true);
    	if (!HawkTime.isSameDay(HawkTime.getMillisecond(), protectSoldierInfo.getLastReceiveTime())) {
    		oldReceiveDayCount = protectSoldierInfo.getReceiveCountDay();
    		protectSoldierInfo.setReceiveCountDay(0);
		}
    	
    	player.getPush().pushProtectSoldierInfo();
    	if (oldReceiveDayCount > 0) {
    		RedisProxy.getInstance().updateProtectSoldierInfo(protectSoldierInfo, ConstProperty.getInstance().getRescueDuration());
    	}
    }
    
    /**
     * 同步大R复仇死兵信息 
     */
    private void syncRevengeInfo() {
    	if (ConstProperty.getInstance().getRevengeShopOpen() <= 0) {
    		return;
    	}
    	
    	if (player.getData().getUnlockedSoldierMaxLevel() < ConstProperty.getInstance().getRevengeShopTroopsLevel()) {
    		return;
    	}
    	
    	try {
    		RevengeInfo revengeInfo = checkAndGetRevengeInfo();
			if (revengeInfo.getState() == RevengeState.PREPARE) {
				revengeInfo.setShopStartTime(HawkTime.getMillisecond());
				revengeInfo.setState(RevengeState.ON);   // 上线触发
				RedisProxy.getInstance().updateRevengeInfo(revengeInfo);
				LogUtil.logTriggerRevengeShop(player, revengeInfo.getDeadSoldierTotal(), revengeInfo.getStartTime(), true);
			}
			
    		pushRevengeInfo();
    		
    	} catch (Exception e) {
    		HawkException.catchException(e);
    	}
    }
    
    /**
     * 打开造兵界面通知（只有有新解锁出兵种后第一次进入页面才发送）
     */
    @ProtocolHandler(code = HP.code.OPEN_SOLDIER_PAGE_VALUE)
    private boolean onOpenSoldierPage(HawkProtocol protocol) {
    	OpenSoldierPageNotice noticeData = protocol.parseProtocol(OpenSoldierPageNotice.getDefaultInstance());
    	String uuid = noticeData.getBuildingUUID();
    	BuildingBaseEntity building = player.getData().getBuildingBaseEntity(uuid);
    	if (building == null) {
    		logger.error("open soldier page, entity error, playerId: {}, uuid: {}", player.getId(), uuid);
    		return false;
    	}
    	
    	int armyId = noticeData.getArmyId();
    	BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
    	if (!GameUtil.isSoldierUnlocked(player, buildingCfg, armyId)) {
            logger.error("open soldier page, soldier not unlocked error, playerId: {}, armyId: {}", player.getId(), armyId);
            sendError(protocol.getType(), Status.Error.SOLDIER_NOT_UNLOCKED);
            return false;
        }
    	
    	ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
    	if (armyEntity != null) {
    		logger.error("open soldier page, armyEntity exist, playerId: {}, armyId: {}", player.getId(), armyId);
    		return false;
    	}
    	
    	armyEntity = new ArmyEntity();
    	armyEntity.setPlayerId(player.getId());
    	armyEntity.setArmyId(armyId);
    	if (!HawkDBManager.getInstance().create(armyEntity)) {
    		logger.error("open soldier page, armyEntity create failed, playerId: {}, armyId: {}", player.getId(), armyId);
    		return false;
    	}
    	
    	getPlayerData().addArmyEntity(armyEntity);
        player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT, armyId);
        player.responseSuccess(protocol.getType());

    	return true;
    }
    
	/**
	 * 士兵晋升
	 */
	@ProtocolHandler(code = HP.code.SOLDIER_ADVANCE_C_VALUE)
	private boolean onAdvanceSoldier(HawkProtocol protocol) {
		HPAdvanceSoldierReq req = protocol.parseProtocol(HPAdvanceSoldierReq.getDefaultInstance());
		final int armyId = req.getArmyId();// = 1; //消耗兵种
		final int toArmyId = req.getToArmyId();// = 2; // 目标兵种
		final int soldierCount = req.getSoldierCount(); // = 3;
		final String buildingUUID = req.getBuildingUUID();// = 4;
		final boolean isImmediate = req.getIsImmediate();// = 5;
		final boolean useGold = req.getUseGold();// = 6; // 非立即训练时，是否用水晶替补不足的资源

		if (soldierCount <= 0) {
			HawkLog.errPrintln("advance soldier failed, playerId: {}, soldierCount: {}", player.getId(), soldierCount);
			return false;
		}
		// 造兵条件验证
		if (!trainArmyCheck(protocol.getType(), buildingUUID, toArmyId)) {
			return false;
		}
		
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		BattleSoldierCfg toarmyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, toArmyId);
		// 兵种配置不存在
		if (Objects.isNull(armyCfg) || Objects.isNull(toarmyCfg) || toarmyCfg.isPlantSoldier()) {
			return false;
		}
		ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
		if (Objects.isNull(armyEntity) || armyEntity.getFree() < soldierCount) {
			return false;
		}
		if(!armyCfg.getPromotionList().contains(toArmyId)){
			return false;
		}

		double disvariate = ConstProperty.getInstance().getPromotionVariate();
		double timedisvariate = ConstProperty.getInstance().getPromotionTimeVariate();
		// 计算造兵速度加成
		int speedup = calTrainSpeedUp(true, armyId, soldierCount, protocol.getType(), false);
		if (speedup < 0) {
            logger.error("soldier advance error, playerId: {}, armyId: {}, speedup: {}", player.getId(), armyId, speedup);
            return false;
        }
		
		// 造兵需要的时间：单位秒
		long oldtrainTime = GameUtil.calTrainTime(player, armyCfg, soldierCount, speedup,true);
		// 造兵需要的时间：单位秒
		long trainTime = GameUtil.calTrainTime(player, toarmyCfg, soldierCount, speedup,true) - (long) (oldtrainTime * timedisvariate);

		// 原造兵资源打折回收
		List<ItemInfo> itemInfos = GameUtil.trainCost(player, armyCfg, soldierCount).stream()
				.peek(item -> item.setCount((int) (item.getCount() * disvariate)))
				.collect(Collectors.toList());
		// 正常扣除造兵资源
		List<ItemInfo> toitemInfos = GameUtil.trainCost(player, toarmyCfg, soldierCount);
		for (ItemInfo info : toitemInfos) {
			int itemId = info.getItemId();
			Optional<ItemInfo> disCountItem = itemInfos.stream().filter(d -> d.getItemId() == itemId).findAny();
			if (disCountItem.isPresent()) {
				int disCount = (int) disCountItem.get().getCount();
				info.setCount(Math.max(0, info.getCount() - disCount));
			}
		}
		
		List<ItemInfo> consumeRes = GameUtil.trainConsume(player, armyCfg, toitemInfos, soldierCount, trainTime, isImmediate, useGold, protocol.getType());
		if (consumeRes == null) {
			return false;
		}
		// 消耗的兵也要做为返还资源的一部分
		consumeRes.add(new ItemInfo(ItemType.SOLDIER_VALUE, armyId, soldierCount * 2));

		advanceSoldier(buildingUUID, toarmyCfg, armyId, soldierCount, trainTime, consumeRes, isImmediate);

		pushCreateSoldierInfo(buildingUUID, toArmyId, soldierCount, isImmediate);
		// 扣兵
		armyEntity.addFree(-soldierCount);
		player.getPush().syncArmyInfo(ArmyChangeCause.FIRE, armyId);
		player.refreshPowerElectric(PowerChangeReason.TRAIN_SOLDIER);
		player.responseSuccess(protocol.getType());
		
		// 单次征兵令，用过之后失效
		{
			CustomDataEntity customDataEntity = player.getData().getCustomDataEntity(CustomKeyCfg.getTrainQuantityAddKey());
			if (customDataEntity != null && customDataEntity.getValue() > 0) {
				customDataEntity.setValue(customDataEntity.getValue() - 1);
				player.getPush().syncCustomData();
			}
		}
		// 单次晋升令，用过之后失效
		{
			CustomDataEntity customDataEntity = player.getData().getCustomDataEntity(CustomKeyCfg.getAdvanceQuantityAddKey());
			if (customDataEntity != null && customDataEntity.getValue() > 0) {
				customDataEntity.setValue(customDataEntity.getValue() - 1);
				player.getPush().syncCustomData();
			}
		}
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.SOLDIER_ADVANCE,
				Params.valueOf("armyId", armyId),
				Params.valueOf("toArmyId", toArmyId),
				Params.valueOf("count", soldierCount),
				Params.valueOf("buildingId", buildingUUID),
				Params.valueOf("immediate", isImmediate));
		return true;
	}
    
    /**
     * 训练士兵
     */
    @ProtocolHandler(code = HP.code.ADD_SOLDIER_C_VALUE)
    public boolean onCreateSoldier(HawkProtocol protocol) {
        HPAddSoldierReq req = protocol.parseProtocol(HPAddSoldierReq.getDefaultInstance());
        final boolean immediate = req.getIsImmediate();
        if (immediate && player.isZeroEarningState()) {
        	logger.error("train failed, player on zero earning status, playerId: {}", player.getId());
        	sendError(protocol.getType(), Status.SysError.ZERO_EARNING_STATE);
        	return false;
        }
        
        HawkAssert.checkPositive(req.getSoldierCount());
        BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(req.getBuildingUUID());
        if (null == buildingEntity) {
            logger.error("soldier related building cant not find, playerId: {}, buildingId: {}, armyId: {}", player.getId(), req.getBuildingUUID(), req.getArmyId());
            sendError(protocol.getType(), Status.Error.BUILDING_NOT_EXISIT_VALUE);
            return false;
        }
        
        int armyId = req.getArmyId();
        BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
        // 兵种配置不存在或泰能兵
        if (armyCfg == null || armyCfg.isPlantSoldier()) {
            logger.error("config error, playerId: {}, armyId: {}", player.getId(), req.getArmyId());
            sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
            return false;
        }
        
        // 陷阱类型单独处理
        if(armyCfg.isDefWeapon()) {
        	return createTrap(req, protocol.getType());
        }
        
        // 是否新手特殊处理
        boolean newlyOp = false;
        int newlyFlag = req.hasFlag() ? req.getFlag() : 0;
        CustomDataEntity entity = player.getData().getCustomDataEntity(CustomKeyCfg.getNewlyDataStateKey());
        int newlyDataState = entity == null ? 0 : entity.getValue();
		
        if (newlyFlag > 0 && newlyDataState < newlyFlag) {
        	newlyOp = true;
		} else if (newlyFlag > 0) {
			logger.error("train soldier newlyFlag error, playerId: {}, armyId: {}, newlyFlag: {}, newlyDataState:{}", player.getId(), armyId, newlyFlag, newlyDataState);
		}
        
        // 造兵条件验证
        if (!trainArmyCheck(protocol.getType(), req.getBuildingUUID(), req.getArmyId())) {
            return false;
        }
        
        final int soldierCount = req.getSoldierCount();
        final String buildingUuid = req.getBuildingUUID();
        // 计算造兵速度加成
        int speedup = calTrainSpeedUp(false, armyId, soldierCount, protocol.getType(), newlyOp);
        if (speedup < 0) {
            logger.error("train soldier error, playerId: {}, armyId: {}, speedup: {}", player.getId(), armyId, speedup);
            return false;
        }
        
 		// 造兵需要的时间：单位秒
 		long trainTime = 0;
        // 新手造兵时间特殊处理
        if (newlyOp) {
        	trainTime = getNewlyOpTrainTime(newlyFlag, immediate);
        } else {
        	trainTime = GameUtil.calTrainTime(player, armyCfg, soldierCount, speedup);
        }

        // 造兵消耗资源: 新手阶段造兵不需要消耗资源
        List<ItemInfo> consumeRes = null;
        if (!newlyOp) {
        	List<ItemInfo> itemInfos = GameUtil.trainCost(player, armyCfg, soldierCount);
        	consumeRes = GameUtil.trainConsume(player, armyCfg, itemInfos, soldierCount, trainTime, immediate, req.hasUseGold() ? req.getUseGold() : false, protocol.getType());
            if (consumeRes == null) {
                return false;
            }
        }

        ArmyEntity armyEntity = createSoldier(buildingUuid, armyCfg, soldierCount, trainTime, consumeRes, immediate);
        if(armyEntity == null) {
        	logger.error("create soldier failed, playerId: {}, armyId: {}, soldierCount: {}, armyEntity: {}", player.getId(), armyCfg.getId(), soldierCount, armyEntity);
 			sendError(protocol.getType(), Status.Error.CREATE_SOLDIER_FAILED_VALUE);
        	return false;
        }
       
        // 训练兵种操作打点
        LogUtil.logTrainOperation(player, buildingEntity.getType(), armyId, armyCfg.getLevel(), soldierCount, immediate);
        
        pushCreateSoldierInfo(buildingUuid, armyId, soldierCount, immediate);
        player.refreshPowerElectric(PowerChangeReason.TRAIN_SOLDIER);
        player.responseSuccess(protocol.getType());
        // 更新新手数据状态
        try {
        	// 单次征兵令，用过之后失效
        	CustomDataEntity customDataEntity = player.getData().getCustomDataEntity(CustomKeyCfg.getTrainQuantityAddKey());
    		if (customDataEntity != null && customDataEntity.getValue() > 0) {
    			customDataEntity.setValue(customDataEntity.getValue() - 1);
    			player.getPush().syncCustomData();
    		}
    		
            if (entity == null) {
    			player.getData().createCustomDataEntity(CustomKeyCfg.getNewlyDataStateKey(), newlyFlag, CustomKeyCfg.getNewlyDataStateKey());
    		} else {
    			entity.setValue(newlyFlag);
    		}
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        
        logger.debug("train soldier, playerId: {}, armyId: {}, count: {}, buildingId: {}, immediate: {}, newlyOp: {}, hasNewlyFlag: {}, newlyDataState: {}, newlyFlag: {}", 
        		player.getId(), armyId, soldierCount, buildingUuid, immediate, newlyOp, req.hasFlag(), newlyDataState, newlyFlag);
        return true;
    }

    /**
     * 制造陷阱
     * @param req
     * @return
     */
    private boolean createTrap(HPAddSoldierReq req, int hpCode) {
        // 制造陷阱前置条件判断
        if(!makeTrapCheck(req, hpCode)) {
        	return false;
        }
        
        int trapId = req.getArmyId();
        BattleSoldierCfg trapCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, trapId);
        BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.WAR_FORTS);
        BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
        int trapCount = req.getSoldierCount();
        // 制造陷阱需要的时间
        long costTime = GameUtil.calTrainTime(player, trapCfg, trapCount, buildingCfg.getTrainSpeed());
        boolean immediate = req.getIsImmediate();
        List<ItemInfo> resCost = makeTrapConsume(player, trapCfg, trapCount, immediate, req.hasUseGold() ? req.getUseGold() : false, costTime, hpCode);
        if(resCost == null) {
        	return false;
        }
        
        ArmyEntity trapEntity = getPlayerData().getArmyEntity(trapId);
        if (trapEntity == null) {
        	trapEntity = new ArmyEntity();
        	trapEntity.setPlayerId(player.getId());
        	trapEntity.setArmyId(trapId);
            if (!HawkDBManager.getInstance().create(trapEntity)) {
                return false;
            }
            getPlayerData().addArmyEntity(trapEntity);
        }
        
        MissionManager.getInstance().postMsg(player, new EventSoldierTrainStart(trapId, trapCount));
        if (immediate) {
        	trapEntity.addFree(trapCount);
        	ActivityManager.getInstance().postEvent(new MakeTrapEvent(player.getId(), trapId, trapCount));
        	MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_MAKE_TRAP, trapId, trapCount);
        	MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_MAKE_TRAP, 0, trapCount);
        	GameUtil.soldierAddRefresh(player, trapEntity.getArmyId(), trapCount);
        	
        	LogUtil.logArmyChange(player, trapEntity, trapCount, ArmySection.FREE, ArmyChangeReason.TRAIN);
        } else {
        	trapEntity.setTrainCount(trapCount);
            QueueService.getInstance().addReusableQueue(player, QueueType.TRAP_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
            		String.valueOf(trapId), trapCfg.getBuilding(), costTime * 1000d, resCost, GsConst.QueueReusage.MAKE_TRAP);
            LogUtil.logArmyChange(player, trapEntity, trapCount, ArmySection.TRAIN, ArmyChangeReason.TRAIN);
        }
        
        trapEntity.setLastTrainTime(GsApp.getInstance().getCurrentTime());
        updateLatestTrain(buildingEntity.getId(), trapId);

        pushMakeTrapInfo(trapId, trapCount, req.getBuildingUUID(), immediate);
        player.refreshPowerElectric(PowerChangeReason.MAKE_TRAP);
        player.responseSuccess(hpCode);
        // 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.MAKE_TRAP,
     				Params.valueOf("trapId", trapId),
     				Params.valueOf("count", trapCount),
     				Params.valueOf("immediate", immediate));
        return true;
	}
    
    /**
     * 同步制造陷阱的结果
     * @param trapId
     * @param trapCount
     * @param buildingId
     * @param immediate
     */
    private void pushMakeTrapInfo(int trapId, int trapCount, String buildingId, boolean immediate) {
    	// 同步陷阱数据
    	 if (immediate) {
 	     	 pushCollectSoldierInfo(trapId, trapCount, buildingId);
         } else {
        	 player.getPush().syncArmyInfo(ArmyChangeCause.TRAIN, trapId);
         }
    }
    
    /**
     * 制造陷阱条件检测
     * @param trapId
     * @param trapCount
     * @param hpCode
     * @return
     */
    private boolean makeTrapCheck(HPAddSoldierReq req, int hpCode) {
        BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(req.getBuildingUUID());
        if (buildingEntity.getType() != BuildingType.WAR_FORTS_VALUE) {
        	logger.error("warforts building type error, playerId: {}, uuid: {}, type: {}", player.getId(), req.getBuildingUUID(), buildingEntity.getType());
        	sendError(hpCode, Status.SysError.PARAMS_INVALID);
        	return false;
        }
        
        BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
        // 配置错误
        if(buildingCfg == null) {
        	logger.error("warforts building config error, playerId: {}, cfgId: {}", player.getId(), buildingEntity.getBuildingCfgId());
        	sendError(hpCode, Status.SysError.CONFIG_ERROR_VALUE);
        	return false;
        }
        
        QueueEntity buildingQueue = player.getData().getQueueEntityByItemId(buildingEntity.getId());
        // 战争堡垒正在升级
        if(buildingQueue != null) {
        	logger.error("warforts building upgrading, playerId: {}, queueId: {}", player.getId(), buildingQueue.getId());
        	sendError(hpCode, Status.Error.BUILDING_STATUS_UPGRADE_VALUE);
        	return false;
        }
        
        int trapId = req.getArmyId();
        BattleSoldierCfg trapCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, trapId);
        Optional<QueueEntity> optional = getPlayerData().getQueueEntities().stream()
                .filter(queue -> queue.getQueueType() == QueueType.TRAP_QUEUE_VALUE)
                .filter(queue -> queue.getReusage() != QueueReusage.FREE.intValue())
                .filter(queue -> queue.getBuildingType() == trapCfg.getBuilding())
                .findAny();
        // 正在建造陷阱
        if(optional.isPresent()) {
        	logger.error("warforts is working, playerId: {}, queueId: {}", player.getId(), optional.get().getId());
        	sendError(hpCode, Status.Error.BUILDING_STATUS_MAKING_TRAP_VALUE);
        	return false;
        }
        
        // 有造完的陷阱未收走
        if (buildingEntity.getStatus() == Const.BuildingStatus.TRAP_HARVEST_VALUE) {
            logger.error("warforts building need harvest first, playerId: {}, trapId: {}, buildingStatus: {}", player.getId(), trapId, buildingEntity.getStatus());
            if (player.isZeroEarningState()) {
            	logger.error("create warforts harvest failed, player is in zero earning state, playerId: {}", player.getId());
            	sendError(hpCode, Status.SysError.ZERO_EARNING_STATE);
            	return false;
            }
            collectBeforeTrain(buildingEntity);
        }
        
        // 陷阱类型未解锁
        if (!GameUtil.isSoldierUnlocked(player, buildingCfg, trapId)) {
            logger.error("trap not unlocked, playerId: {}, trapId: {}", player.getId(), trapId);
            sendError(hpCode, Status.Error.TRAP_NOT_UNLOCKED_VALUE);
            return false;
        }
        
        int trapCount = req.getSoldierCount();
        int trapCapacity = player.getData().getTrapCapacity();
        int trapTotalCount = player.getData().getTrapCount();
        // 已造出的陷阱数量已达陷阱容量上限
        if(trapTotalCount >= trapCapacity || trapTotalCount + trapCount > trapCapacity) {
        	logger.error("trapTotalCount exceed the capacity, playerId: {}, trapId: {}, trapTotalCount: {}, trapCount: {}, trapCapacity: {}", player.getId(), trapId, trapTotalCount, trapCount, trapCapacity);
        	sendError(hpCode, Status.Error.TRAP_TOTAL_EXCEED_CAPACITY_VALUE);
        	return false;
        }
        
        // 陷阱数量超过单次造陷阱的数量上限
        int trainQuantity = buildingCfg.getTrainQuantity() + player.getData().getEffVal(EffType.TRAP_CREATE_ADD_ONCE);
        BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, trapId);
		switch (cfg.getType()) {
		case SoldierType.WEAPON_LANDMINE_101_VALUE:
			trainQuantity += player.getData().getEffVal(Const.EffType.WEAPON_BUILD_TYPE101_ADD_NUM);
			break;
		case SoldierType.WEAPON_ACKACK_102_VALUE:
			trainQuantity += player.getData().getEffVal(Const.EffType.WEAPON_BUILD_TYPE102_ADD_NUM);
			break;
		case SoldierType.WEAPON_ANTI_TANK_103_VALUE:
			trainQuantity += player.getData().getEffVal(Const.EffType.WEAPON_BUILD_TYPE103_ADD_NUM);
			break;
		default:
			break;
		}
		
        if(trapCount <= 0 || trapCount > trainQuantity) {
        	logger.error("trapCount exceed the limit, playerId: {}, trapId: {}, trapCount: {}, uplimit: {}", player.getId(), trapId, trapCount, buildingCfg.getTrainQuantity());
        	sendError(hpCode, Status.Error.TRAP_COUNT_EXCEED_LIMIT_VALUE);
        	return false;
        }
        
        return true;
    }
    
    /**
     * 造兵前先收取已训练完成的兵
     * 
     * @param buildingEntity
     */
    private void collectBeforeTrain(BuildingBaseEntity buildingEntity) {
        for (ArmyEntity entity : player.getData().getArmyEntities()) {
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
			if (armyCfg.getBuilding() == buildingEntity.getType() && entity.getTrainFinishCount() > 0 && !armyCfg.isPlantSoldier()) {
				collectSoldier(buildingEntity, entity);
			}
		}
    }
    
    /**
     * 制造陷阱消耗资源
     * 
     * @param player
     * @param trapCfg
     * @param count
     * @param immediate
     * @param useGold
     * @param costTime
     * @param hpCode
     * @return
     */
    private List<ItemInfo> makeTrapConsume(Player player, BattleSoldierCfg trapCfg, int count, boolean immediate, boolean useGold, long costTime, int hpCode) {
		ConsumeItems consume = ConsumeItems.valueOf();
		List<ItemInfo> resList = trapCfg.getResList();
		List<ItemInfo> itemInfos = new ArrayList<ItemInfo>();

		for (ItemInfo itemInfo : resList) {
			int itemCount = (int) (itemInfo.getCount() * count);
			itemInfos.add(new ItemInfo(itemInfo.getType(), itemInfo.getItemId(), itemCount));
		}
		// 制造陷阱需要消耗的资源
		consume.addConsumeInfo(itemInfos, immediate || useGold);

		// 立即完成 
		if (immediate) {
			// 造兵和造陷阱共用一个SpeedUpTimeWeightType
			consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(costTime, SpeedUpTimeWeightType.TIME_WEIGHT_TRAINSOLDIER));
		}

		if (!consume.checkConsume(player, hpCode)) {
			return null;
		}
		
		AwardItems realCostItems = consume.consumeAndPush(player, Action.MAKE_TRAP);
		return realCostItems.getAwardItems();
	}

	/**
     * 造兵条件校验
     * @param buildingEntity
     * @param count
     * @param hpCode
     * @param armyId
     * @param newly
     * @param isImmediate
     * @return
     */
	public boolean trainArmyCheck(int hpCode, String buildingUUID, int armyId) {
		BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(buildingUUID);
        BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
        // 非造兵建筑
        if(buildingCfg == null || !buildingCfg.isSoldierProductBuilding()) {
        	 logger.error("soldier produce building error, playerId: {}, buildingId: {}, buildCfgId: {}", player.getId(), buildingUUID, buildingCfg == null ? 0 : buildingCfg.getId());
             sendError(hpCode, Status.Error.BUILDING_NOT_EXISIT_VALUE);
             return false;
        }
        
        QueueEntity buildingQueue = player.getData().getQueueEntityByItemId(buildingEntity.getId());
        if(buildingQueue != null) {
        	logger.error("building upgrading, train soldier req break, playerId: {}, buildId: {}, queueId: {}", player.getId(), buildingEntity.getId(), buildingQueue.getId());
        	sendError(hpCode, Status.Error.BUILDING_STATUS_UPGRADE_VALUE);
        	return false;
        }

        // 判断是否有造完未领取的兵
		if (buildingEntity.getStatus() == Const.BuildingStatus.SOILDER_HARVEST_VALUE) {
            logger.error("building status soldier harvest error, playerId: {}, armyId: {}", player.getId(), armyId);
            if (player.isZeroEarningState()) {
            	logger.error("train harvest failed, player on zero earning status, playerId: {}", player.getId());
            	sendError(hpCode, Status.SysError.ZERO_EARNING_STATE);
		        return false;
            }
            collectBeforeTrain(buildingEntity);
        }
        
        Optional<QueueEntity> op = getPlayerData().getQueueEntities().stream()
                .filter(queue -> queue.getQueueType() == QueueType.SOILDER_QUEUE_VALUE || queue.getQueueType() == QueueType.SOLDIER_ADVANCE_QUEUE_VALUE)
                .filter(queue -> queue.getReusage() != QueueReusage.FREE.intValue())
                .filter(queue -> queue.getBuildingType() == buildingEntity.getType())
                .findAny();
        
        // 正在造兵
        if (op.isPresent()) {
            logger.error("army in trainning error, playerId: {}, armyId: {}", player.getId(), armyId);
            sendError(hpCode, Status.Error.ARMY_IN_TRAINNING);
            return false;
        }

        // 兵种还未解锁
        if (!GameUtil.isSoldierUnlocked(player, buildingCfg, armyId)) {
            logger.error("soldier not unlocked error, playerId: {}, armyId: {}", player.getId(), armyId);
            sendError(hpCode, Status.Error.SOLDIER_NOT_UNLOCKED);
            return false;
        }

        return true;
    }
    
    /**
     * 计算新手阶段造兵所需时间
     * @param newlyFlag
     * @param isImmediate
     * @return
     */
    private long getNewlyOpTrainTime(int newlyFlag, boolean isImmediate) {
    	long trainTime = 0;
        // 普通步兵
        if (newlyFlag == NewlyDataType.COMMON_INFANTRY_VALUE) {
        	trainTime = ConstProperty.getInstance().getGuideFirstTrainSoldierTime();
        }
        // 普通坦克
        if (newlyFlag == NewlyDataType.COMMON_TANK_VALUE) {
        	trainTime = ConstProperty.getInstance().getGuideFirstTrainSoldierTime();
        }
        // 普通飞机
        if (newlyFlag == NewlyDataType.COMMON_PLANE_VALUE) {
        	trainTime = ConstProperty.getInstance().getGuideFirstTrainSoldierTime();
        }
        // 立即步兵
        if (newlyFlag == NewlyDataType.IMMEDIATE_INFANTRY_VALUE && isImmediate) {
        	trainTime = 0;
        }
        
        return trainTime;
    }

    /**
     * 计算造兵加速百分比和造兵上限：两者均由复制中心建筑来控制
     * @param armyId
     * @param soldierCount
     * @param hpCode
     * @param newly
     * @return
     */
    private int calTrainSpeedUp(boolean advance,int armyId, int soldierCount, int hpCode, boolean newly) {
    	int speed = 0, trainUpLimit = 0;  // 造兵速度加成百分比，造兵上限
    	List<BuildingBaseEntity> buildingList = player.getData().getBuildingListByType(BuildingType.SOLDIER_CONTROL_HALL);
    	for(BuildingBaseEntity building : buildingList) {
    		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
    		if (buildingCfg == null) {
    			logger.error("soldier building config error, playerId: {}, buildCfgId: {}", player.getId(), building.getBuildingCfgId());
    			sendError(hpCode, Status.SysError.CONFIG_ERROR);
    			return -1;
    		}
    		
    		speed += buildingCfg.getTrainSpeed();
    		trainUpLimit += buildingCfg.getTrainQuantity();
    	}
		
    	if (newly) {
			trainUpLimit = ConstProperty.getInstance().getNewTrainSafeNum();
		} else {
			// 该作用号只对基础数量生效
			trainUpLimit = (int) Math.ceil(trainUpLimit * (1 + player.getData().getEffVal(Const.EffType.TRAIN_COUNT_ADD_PER) * GsConst.EFF_PER));
			trainUpLimit += ConstProperty.getInstance().getNewTrainQuantity();
			trainUpLimit += getTrainCountEffVal(armyId);
		}
    	
    	// 时效征兵令
    	StatusDataEntity statusDataEntity = player.getData().getStatusById(EffType.TRAIN_QUANTITY_ADD_NUM_VALUE, "");
		if (statusDataEntity != null && statusDataEntity.getEndTime() > HawkTime.getMillisecond()) {
			trainUpLimit += statusDataEntity.getVal();
		}
		
		// 单次征兵令
		{
			CustomDataEntity customDataEntity = player.getData().getCustomDataEntity(CustomKeyCfg.getTrainQuantityAddKey());
			if (customDataEntity != null && customDataEntity.getValue() > 0) {
				if (HawkOSOperator.isEmptyString(customDataEntity.getArg())) {
					customDataEntity.setArg(String.valueOf(customDataEntity.getValue()));
				}

				int onceAddNum = Integer.parseInt(customDataEntity.getArg());
				trainUpLimit += onceAddNum;
			}
		}
		// 单次晋升令
		if (advance) {
			CustomDataEntity customDataEntity = player.getData().getCustomDataEntity(CustomKeyCfg.getAdvanceQuantityAddKey());
			if (customDataEntity != null && customDataEntity.getValue() > 0) {
				if (HawkOSOperator.isEmptyString(customDataEntity.getArg())) {
					customDataEntity.setArg(String.valueOf(customDataEntity.getValue()));
				}

				int onceAddNum = Integer.parseInt(customDataEntity.getArg());
				trainUpLimit += onceAddNum;
			}
		}
    	
 		// 造兵数量超过上限
 		if (soldierCount > trainUpLimit || soldierCount <= 0) {
 			logger.error("playerId: {}, armyId: {}, soldierCount: {}, server maxCount: {}, newly: {}", 
 					player.getId(), armyId, soldierCount, trainUpLimit, newly);
 			sendError(hpCode, Status.Error.ARMY_COUNT_ERROR);
 			return -1;
 		}
 		
        return speed;
    }
    
    /**
     * 获取兵种训练单次训练数量增加作用号的值
     * 
     * @param soldierId
     * @return
     */
    private int getTrainCountEffVal(int soldierId) {
    	int trainCountAdd = player.getData().getEffVal(Const.EffType.CITY_ARMY_TRAIN_NUM);
		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, soldierId);
		switch (cfg.getBuilding()) {
		case BuildingType.BARRACKS_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.FOOT_TRAIN_ADD_ONCE);
			break;
		case BuildingType.WAR_FACTORY_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TANK_TRAIN_ADD_ONCE);
			break;
		case BuildingType.REMOTE_FIRE_FACTORY_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.CANNON_TRAIN_ADD_ONCE);
			break;
		case BuildingType.AIR_FORCE_COMMAND_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.PLANE_TRAIN_ADD_ONCE);
			break;
		default:
			break;
		}
		
		switch (cfg.getType()) {
		case SoldierType.TANK_SOLDIER_1_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TRAIN_COUNT_TYPE1_ADD_NUM);
			break;
		case SoldierType.TANK_SOLDIER_2_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TRAIN_COUNT_TYPE2_ADD_NUM);
			break;
		case SoldierType.PLANE_SOLDIER_3_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TRAIN_COUNT_TYPE3_ADD_NUM);
			break;
		case SoldierType.PLANE_SOLDIER_4_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TRAIN_COUNT_TYPE4_ADD_NUM);
			break;
		case SoldierType.FOOT_SOLDIER_5_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TRAIN_COUNT_TYPE5_ADD_NUM);
			break;
		case SoldierType.FOOT_SOLDIER_6_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TRAIN_COUNT_TYPE6_ADD_NUM);
			break;
		case SoldierType.CANNON_SOLDIER_7_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TRAIN_COUNT_TYPE7_ADD_NUM);
			break;
		case SoldierType.CANNON_SOLDIER_8_VALUE:
			trainCountAdd += player.getData().getEffVal(Const.EffType.TRAIN_COUNT_TYPE8_ADD_NUM);
			break;
		default:
			break;
		}
		
		return trainCountAdd;
    }

    /**
     * 开始造兵
     * @param buildingUuid
     * @param armyEntity
     * @param armyCfg
     * @param count
     * @param isImmediate
     */
    private ArmyEntity createSoldier(String buildingUuid, BattleSoldierCfg armyCfg, int count, long trainTime, List<ItemInfo> consumeRes, boolean immediate) {
        int armyId = armyCfg.getId();
        // 启动造兵统计任务
        MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_TRAIN_SOLDIER_START_NUMBER, armyId, count);
        ActivityManager.getInstance().postEvent(new TrainSoldierStartEvent(player.getId(), armyCfg.getType(), armyId, count));
        MissionManager.getInstance().postMsg(player, new EventSoldierTrainStart(armyId, count));
        
        ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
        if (armyEntity == null) {
        	armyEntity = new ArmyEntity();
        	armyEntity.setPlayerId(player.getId());
        	armyEntity.setArmyId(armyCfg.getId());
        	if (!HawkDBManager.getInstance().create(armyEntity)) {
        		return null;
        	}
        	getPlayerData().addArmyEntity(armyEntity);
        }
        
        if (immediate) {
        	RedisProxy.getInstance().idipDailyStatisAdd(player.getId(), IDIPDailyStatisType.TRAIN_ARMY, count);
        	finishTrainImmediate(armyEntity, count);
        	sendExtraSoldier(armyCfg, count);
        	LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.TRAIN);
        } else {
        	armyEntity.setTrainCount(count);
        	// 此处队列的itemId拼接3项信息只是为了方便前端，事实上itemId最好对应单项信息
        	final String itemId = Joiner.on("_").join(buildingUuid, armyId, count);
        	QueueService.getInstance().addReusableQueue(player, QueueType.SOILDER_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
        			itemId, armyCfg.getBuilding(), trainTime * 1000d, consumeRes, GsConst.QueueReusage.ARMY_TRAIN);
        	LogUtil.logArmyChange(player, armyEntity, count, ArmySection.TRAIN, ArmyChangeReason.TRAIN);
        }
        
        armyEntity.setLastTrainTime(GsApp.getInstance().getCurrentTime());
        updateLatestTrain(buildingUuid, armyId);
        
        return armyEntity;
    }
    
    /**
     * 开始晋升
     * @param buildingUuid
     * @param armyEntity
     * @param armyCfg
     * @param count
     * @param isImmediate
     */
    private ArmyEntity advanceSoldier(String buildingUuid, BattleSoldierCfg toarmyCfg,int armyId, int count, long trainTime, List<ItemInfo> consumeRes, boolean immediate) {
    	int toarmyId = toarmyCfg.getId();
        ArmyEntity armyEntity = getPlayerData().getArmyEntity(toarmyId);
        if (armyEntity == null) {
        	armyEntity = new ArmyEntity();
        	armyEntity.setPlayerId(player.getId());
        	armyEntity.setArmyId(toarmyId);
        	if (!HawkDBManager.getInstance().create(armyEntity)) {
        		return null;
        	}
        	getPlayerData().addArmyEntity(armyEntity);
        }
        
        
        if (immediate) {
        	armyEntity.addFree(count);
        	GameUtil.soldierAddRefresh(player, toarmyId, count);
        	LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.ADVANCE);
        } else {
        	BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
        	armyEntity.setAdvancePower(cfg.getPower() * count);
        	armyEntity.setTrainCount(count);
        	final String itemId = Joiner.on("_").join(buildingUuid, toarmyId, count,armyId);
        	QueueService.getInstance().addReusableQueue(player, QueueType.SOLDIER_ADVANCE_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
        			itemId, toarmyCfg.getBuilding(), trainTime * 1000d, consumeRes, GsConst.QueueReusage.ARMY_TRAIN);
        	LogUtil.logArmyChange(player, armyEntity, count, count, 0, ArmySection.ADVANCE, ArmyChangeReason.ADVANCE);
        }
        
        updateLatestTrain(buildingUuid, toarmyId);
        
        return armyEntity;
    }
    
    /**
     * 更新兵种训练最新数据
     * @param buildingUuid
     * @param armyId
     */
    private void updateLatestTrain(String buildingUuid, int armyId) {
    	BuildingBaseEntity building = player.getData().getBuildingBaseEntity(buildingUuid);
    	List<ArmyEntity> armyList = player.getData().getArmyEntities();
    	for (ArmyEntity army : armyList) {
    		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
    		if (cfg == null || cfg.getBuilding() != building.getType()) {
    			continue;
    		}
    		
    		if (army.getArmyId() == armyId) {
    			army.setTrainLatest(true);
    		} else {
    			army.setTrainLatest(false);
    		}
    	}
    }
    
    /**
     * 同步造兵信息
     * @param buildingId
     * @param armyId
     * @param count
     * @param immediate
     */
    private void pushCreateSoldierInfo(String buildingId, int armyId, int count, boolean immediate) {
		 if (immediate) {
			 pushCollectSoldierInfo(armyId, count, buildingId);
	     } else {
	     	player.getPush().syncArmyInfo(ArmyChangeCause.TRAIN, armyId);
	     }
    }
    
    /**
     * 推送收兵信息
     * @param armyId
     * @param count
     * @param buildingId
     */
    private void pushCollectSoldierInfo(int armyId, int count, String buildingId) {
    	// 同步兵种数量变化信息
     	Map<Integer, Integer> map = new HashMap<Integer, Integer>();
     	map.put(armyId, count);
     	player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_COLLECT, map);
     	// 向客户端返回造兵应答信息
     	HPCollectSoldierResp.Builder resp = HPCollectSoldierResp.newBuilder();
     	resp.setArmyId(armyId);
     	resp.setBuildingUUID(buildingId);
     	resp.setCollectNum(count);
     	resp.setResult(true);
     	player.sendProtocol(HawkProtocol.valueOf(HP.code.COLLECT_SOLDIER_S, resp));
    }

    /**
     * 制造陷阱完成处理
     * @param buildingEntity 
     * @param trapEntity
     * @param trapCount
     * @param immediate
     * @return
     */
  	private boolean makeTrapComplete(BuildingBaseEntity buildingEntity, ArmyEntity trapEntity, int trapCount, boolean immediate) {
  		if(immediate){
  			trapEntity.addFree(trapCount);
  			pushCollectSoldierInfo(trapEntity.getArmyId(), trapCount, buildingEntity.getId());
  			ActivityManager.getInstance().postEvent(new MakeTrapEvent(player.getId(), trapEntity.getArmyId(), trapCount));
  			MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_MAKE_TRAP, trapEntity.getArmyId(), trapCount);
  			MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_MAKE_TRAP, 0, trapCount);
  			GameUtil.soldierAddRefresh(player, trapEntity.getArmyId(), trapCount);
  			LogUtil.logArmyChange(player, trapEntity, trapCount, ArmySection.FREE, ArmyChangeReason.TRAIN_FINISH);
		} else {
			trapEntity.setTrainFinishCount(trapCount);
			// 改变建筑状态
			player.getPush().pushBuildingStatus(buildingEntity, Const.BuildingStatus.TRAP_HARVEST);
			// 同步陷阱数据
			player.getPush().syncArmyInfo(ArmyChangeCause.TRAIN_FINISH, trapEntity.getArmyId());
			LogUtil.logArmyChange(player, trapEntity, trapCount, ArmySection.TRAIN_FINISH, ArmyChangeReason.TRAIN_FINISH);
		}
  		
        player.refreshPowerElectric(PowerChangeReason.MAKE_TRAP);
        return true;
    }
    
  	/**
  	 * 制造陷阱取消处理
  	 * @param trapEntity
  	 * @param cancelBackRes
  	 * @return
  	 */
  	private boolean makeTrapCancel(ArmyEntity trapEntity, String cancelBackRes) {
    	if (!HawkOSOperator.isEmptyString(cancelBackRes)) {
             AwardItems awardItem = AwardItems.valueOf(cancelBackRes);
             // 取消制造陷阱的资源返还比例
             awardItem.scale(ConstProperty.getInstance().getMakeTrapCancelReclaimRate() / 10000d);
             awardItem.rewardTakeAffectAndPush(player, Action.MAKE_TRAP_CANCEL);
        }
    	
    	player.refreshPowerElectric(PowerChangeReason.CANCEL_TRAP);
    	// 同步陷阱数据
    	player.getPush().syncArmyInfo(ArmyChangeCause.TRAIN_CANCEL, trapEntity.getArmyId());
    	
    	return true;
    }
    
    /**
     * 士兵训练结束
     * @param msg
     * @return
     */
    @MessageHandler
	private boolean onTrainComplete(TrainQueueFinishMsg msg) {
		String itemId = msg.getItemId();
		boolean isImmediate = msg.isImmediate();
		int armyId = Integer.parseInt(itemId);
		ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
		if (armyEntity == null) {
			logger.error("train complete error, playerId: {}, armyId: {}, armyEntity: {}", player.getId(), armyId, armyEntity);
			return false;
		}

		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, Integer.valueOf(armyId));
		if (armyCfg == null) {
			logger.error("train complete error, playerId: {}, armyId: {}, armyCfg: {}", player.getId(), armyId, armyCfg);
			return false;
		}
		
		int count = armyEntity.getTrainCount();
		if(count <= 0) {
			logger.error("train complete error, playerId: {}, armyId: {}, count: {}", player.getId(), armyId,  count);
			return false;
		}
		
		armyEntity.setTrainCount(0);
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.valueOf(armyCfg.getBuilding()));
		if(armyCfg.isDefWeapon()) {
			makeTrapComplete(buildingEntity, armyEntity, count, isImmediate);
			return true;
		}
		
		int advancePower = (int) armyEntity.getAdvancePower();
		if(isImmediate){
			armyEntity.setAdvancePower(0);
			finishTrainImmediate(armyEntity, count);
			pushCollectSoldierInfo(armyId, count, buildingEntity.getId());
			if (advancePower > 0) {
				LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.ADVANCE_FINISH);
			} else {
				sendExtraSoldier(armyCfg, count);
				LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.TRAIN_FINISH);
				RedisProxy.getInstance().idipDailyStatisAdd(player.getId(), IDIPDailyStatisType.TRAIN_ARMY, count);
			}
		} else {
			armyEntity.setTrainFinishCount(count);
			player.getPush().pushBuildingStatus(buildingEntity, Const.BuildingStatus.SOILDER_HARVEST);
			player.getPush().syncArmyInfo(ArmyChangeCause.TRAIN_FINISH, armyEntity.getArmyId());
			if (advancePower > 0) {
				LogUtil.logArmyChange(player, armyEntity, count, 0, count, ArmySection.ADVANCE_FINISH, ArmyChangeReason.ADVANCE_FINISH);
			} else {
				LogUtil.logArmyChange(player, armyEntity, count, ArmySection.TRAIN_FINISH, ArmyChangeReason.TRAIN_FINISH);
				RedisProxy.getInstance().idipDailyStatisAdd(player.getId(), IDIPDailyStatisType.TRAIN_ARMY, count);
			}
			
			logger.debug("train soldier complete, playerId: {}, armyId: {}, count: {}, after freeCount: {}, total: {}",  
					player.getId(), armyId, count, armyEntity.getFree(), armyEntity.getTotal());
		}		
		
		player.refreshPowerElectric(PowerChangeReason.TRAIN_SOLDIER);
		
		return true;
	}
    
    /**
     * 快速完成造兵
     * @param armyEntity
     * @param count
     */
    private void finishTrainImmediate(ArmyEntity armyEntity, int count) {
    	armyEntity.addFree(count);
    	GameUtil.soldierAddRefresh(player, armyEntity.getArmyId(), count);
    	
    	logger.debug("train soldier finish immedidatly, playerId: {}, armyId: {}, count: {}, after freeCount: {}, total: {}",  
				player.getId(), armyEntity.getArmyId(), count, armyEntity.getFree(), armyEntity.getTotal());
    }

    /**
     * 取消训练
     * @param msg
     * @return
     */
    @MessageHandler
    private boolean onTrainCancel(TrainQueueCancelMsg msg) {
        int hpCode = msg.getProtoType();
        String cancelBackRes = msg.getCancelBackRes();
        int armyId = Integer.parseInt(msg.getItemId());
        ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
        if (armyEntity == null) {
            logger.error("add cancel armyEntity error, playerId: {}, armyId: {}", player.getId(), armyId);
            sendError(hpCode, Status.Error.ARMY_ENTITY_ERROR);
            return false;
        }
        BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, Integer.valueOf(armyId));
		if (armyCfg == null) {
			logger.error("train complete error, playerId: {}, armyId: {}, armyCfg: {}", player.getId(), armyId, armyCfg);
			return false;
		}
		
		int advancePower = (int) armyEntity.getAdvancePower();
		int trainCount = armyEntity.getTrainCount();
		armyEntity.setTrainCount(0);
		armyEntity.setAdvancePower(0);

		// 士兵晋升取消时，士兵返还在awardItems中处理
		if (advancePower <= 0) {
			LogUtil.logArmyChange(player, armyEntity, trainCount, ArmySection.TRAIN, ArmyChangeReason.TRAIN_CANCEL);
		}
		
        if(armyCfg.isDefWeapon()) {
        	makeTrapCancel(armyEntity, cancelBackRes);
			return true;
		}
        
        // 返还资源
        if (!HawkOSOperator.isEmptyString(cancelBackRes)) {
            AwardItems awardItem = AwardItems.valueOf(cancelBackRes);
            awardItem.scale(ConstProperty.getInstance().getTrainCancelReclaimRate() / 10000d);
            awardItem.rewardTakeAffectAndPush(player, Action.TRAIN_SOLDIER_CANCEL);
        }
        
        // 异步推送消息
        player.getPush().syncArmyInfo(ArmyChangeCause.TRAIN_CANCEL, armyEntity.getArmyId());
        player.refreshPowerElectric(PowerChangeReason.CANCEL_TRAP);
        
        return true;
    }

    /**
     * 训练士兵结束后领取士兵
     */
    @ProtocolHandler(code = HP.code.COLLECT_SOLDIER_C_VALUE)
	private boolean onCollectSoldier(HawkProtocol protocol) {
		HPCollectSoldierReq req = protocol.parseProtocol(HPCollectSoldierReq.getDefaultInstance());
		final String buildingUuid = req.getBuildingUUID();
		BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(buildingUuid);
		if (buildingEntity == null) {
			logger.error("collect soldier, buildingEntity not exist, playerId: {}, uuid: {}", player.getId(), buildingUuid);
			sendError(protocol.getType(), Status.Error.BUILDING_NOT_EXISIT_VALUE);
			return false;
		}
		
		ArmyEntity armyEntity = null;
		for (ArmyEntity entity : player.getData().getArmyEntities()) {
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
			if (armyCfg.getBuilding() == buildingEntity.getType() && entity.getTrainFinishCount() > 0) {
				armyEntity = entity;
				break;
			}
		}

		if (armyEntity == null) {
			logger.error("collect soldier armyEntity error, playerId: {}, armyId: {}", player.getId(), 0);
			//sendError(protocol.getType(), Status.Error.ARMY_ENTITY_ERROR);
			return false;
		}

		if (armyEntity.getTrainCount() > 0) {
			logger.error("collect soldier error, playerId: {}, armyId: {}, train count: {}", player.getId(), armyEntity.getArmyId(), armyEntity.getTrainCount());
			sendError(protocol.getType(), Status.Error.ARMY_IN_TRAINNING);
			return false;
		}

		int count = armyEntity.getTrainFinishCount();
		if (count <= 0) {
			//前端连续点击不返回错误码，直接return
			logger.error("collect soldier error, playerId: {}, armyId: {}, finish train count: {}", player.getId(), armyEntity.getArmyId(), count);
			sendError(protocol.getType(), Status.Error.ARMY_ENTITY_ERROR);
			return false;
		}
		
		// 具体收兵逻辑
		collectSoldier(buildingEntity, armyEntity);
        
		return true;
	}
    
    /**
     * 收兵逻辑
     * @param buildingEntity
     * @param armyEntity
     */
    private void collectSoldier(BuildingBaseEntity buildingEntity, ArmyEntity armyEntity) {
    	BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
		if(armyCfg.isDefWeapon()) {
			collectTrap(buildingEntity, armyEntity);
			return;
		}
		
		int advancePower = (int) armyEntity.getAdvancePower();
		int count = armyEntity.getTrainFinishCount();
    	armyEntity.setTrainFinishCount(0);
    	armyEntity.setAdvancePower(0);
		armyEntity.addFree(count);
		GameUtil.soldierAddRefresh(player, armyEntity.getArmyId(), count);
		// 士兵收取完成,重置建筑状态
		player.getPush().pushBuildingStatus(buildingEntity, Const.BuildingStatus.COMMON);
		// 同步收兵信息
		pushCollectSoldierInfo(armyEntity.getArmyId(), count, buildingEntity.getId());
		player.refreshPowerElectric(PowerChangeReason.TRAIN_SOLDIER);
		
		if (advancePower > 0) {
			LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.ADVANCE_COLLECT);
		} else {
			sendExtraSoldier(armyCfg, count);
			LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.TRAIN_COLLECT);
		}
		
		// 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.COLLECT_SOLDIER_TRAINED,
     				Params.valueOf("armyId", armyEntity.getArmyId()),
     				Params.valueOf("count", count),
     				Params.valueOf("collectCount", count),
     				Params.valueOf("buildCfgId", buildingEntity.getBuildingCfgId()));
    }
    
    /**
     * 收取已制造出的陷阱
     * @param buildingEntity
     * @param trapEntity
     */
    private void collectTrap(BuildingBaseEntity buildingEntity, ArmyEntity trapEntity) {
    	int count = trapEntity.getTrainFinishCount();
    	trapEntity.addFree(count);
    	trapEntity.setTrainFinishCount(0);
    	// 改变建筑状态
    	player.getPush().pushBuildingStatus(buildingEntity, Const.BuildingStatus.COMMON);
    	player.refreshPowerElectric(PowerChangeReason.MAKE_TRAP);
    	// 向前端同步陷阱数据
    	pushCollectSoldierInfo(trapEntity.getArmyId(), count, buildingEntity.getId());
    	ActivityManager.getInstance().postEvent(new MakeTrapEvent(player.getId(), trapEntity.getArmyId(), count));
    	MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_MAKE_TRAP, trapEntity.getArmyId(), count);
    	MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_MAKE_TRAP, 0, count);
    	GameUtil.soldierAddRefresh(player, trapEntity.getArmyId(), count);
    	
    	LogUtil.logArmyChange(player, trapEntity, count, ArmySection.FREE, ArmyChangeReason.TRAIN_COLLECT);
    	
		// 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.COLLECT_TRAP,
     				Params.valueOf("trapId", trapEntity.getArmyId()),
     				Params.valueOf("count", count),
     				Params.valueOf("collectCount", count),
     				Params.valueOf("buildCfgId", buildingEntity.getBuildingCfgId()));
    }

    /**
     * 造兵收兵时根据 4306~4309 作用号发放额外数量的当前训练部队
     * @param armyCfg
     */
    private void sendExtraSoldier(BattleSoldierCfg armyCfg, int armyCount) {
		// 训练加速作用加成
		int effectValue = 0;
		int cfgParam = 0;
		// 黑科技
		switch (armyCfg.getType()) {
		case Const.SoldierType.TANK_SOLDIER_1_VALUE:
		case Const.SoldierType.TANK_SOLDIER_2_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.EXTRA_ARMY_1_2_PER);
			cfgParam = ConstProperty.getInstance().getEffect4036Param();
			break;
		case Const.SoldierType.PLANE_SOLDIER_3_VALUE:
		case Const.SoldierType.PLANE_SOLDIER_4_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.EXTRA_ARMY_3_4_PER);
			cfgParam = ConstProperty.getInstance().getEffect4037Param();
			break;
		case Const.SoldierType.FOOT_SOLDIER_5_VALUE:
		case Const.SoldierType.FOOT_SOLDIER_6_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.EXTRA_ARMY_5_6_PER);
			cfgParam = ConstProperty.getInstance().getEffect4038Param();
			break;
		case Const.SoldierType.CANNON_SOLDIER_7_VALUE:
		case Const.SoldierType.CANNON_SOLDIER_8_VALUE:
			effectValue += player.getData().getEffVal(Const.EffType.EXTRA_ARMY_7_8_PER);
			cfgParam = ConstProperty.getInstance().getEffect4039Param();
			break;
		default:
			break;
		}
		
		if (effectValue > 0 && cfgParam > 0) {
			int randInt = HawkRand.randInt(1, 10000);
			int count = (int) Math.floor(GsConst.EFF_PER * armyCount * cfgParam);
			if (randInt <= effectValue && count > 0) {
				ItemInfo reward = new ItemInfo(ItemType.SOLDIER_VALUE * GsConst.ITEM_TYPE_BASE, armyCfg.getId(), count);
				MailParames parames = MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.EXTRA_COLLECT_ARMY)
						.addReward(reward)
						.addContents(count, armyCfg.getId())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build();
				
				MailService.getInstance().sendMail(parames);
			}
		}
    }
    
    /**
     * 治疗伤兵
     * @return
     */
    @ProtocolHandler(code = HP.code.CURE_SOLDIER_C_VALUE)
    public boolean onCureSoldier(HawkProtocol protocol) {
        HPCureSoldierReq req = protocol.parseProtocol(HPCureSoldierReq.getDefaultInstance());
        final List<ArmySoldierPB> cureList = req.getSoldiersList();
        final boolean immediate = req.getIsImmediate();
        // 治疗伤兵条件验证
        if (!cureCheck(cureList, req.getBuildingUUID(), protocol.getType())) {
            return false;
        }

        // 治疗伤兵需要的时间
		double recoverTime = GameUtil.recoverTime(player, cureList);
        // 治疗伤兵消耗资源
        List<ItemInfo> resCost = cureConsume(cureList, (int) Math.ceil(recoverTime), immediate, req.hasUseGold() ? req.getUseGold() : false, protocol.getType());
        if (resCost == null) {
            return false;
        }

        // 治疗伤兵
        Map<Integer, Integer> armyIds = cureSoldier(cureList, immediate);
        if (immediate) {
            player.getPush().syncArmyInfo(ArmyChangeCause.CURE_FINISH_COLLECT, armyIds);
            
            int totalCount = 0;
            for (int count : armyIds.values()) {
            	totalCount += count;
    		}
            ActivityManager.getInstance().postEvent(new TreatArmyEvent(player.getId(), totalCount));
            MissionManager.getInstance().postMsg(player, new EvenntTreatArmy(totalCount));
            
        } else {
        	QueueService.getInstance().addReusableQueue(player, QueueType.CURE_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
        			req.getBuildingUUID(), Const.BuildingType.HOSPITAL_STATION_VALUE, recoverTime * 1000, resCost, GsConst.QueueReusage.SOLDIER_CURE);
        	player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_CURE, armyIds.keySet().toArray(new Integer[armyIds.size()]));
        }
        
        if (ArmyService.getInstance().getWoundedCount(player) <= 0) {
        	GameUtil.changeBuildingStatus(player, Const.BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.COMMON);
        }

        player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
        player.responseSuccess(protocol.getType());
        // 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.SOLDIER_TREATMENT,
    				Params.valueOf("immediate", immediate),
    				Params.valueOf("army", armyIds));
        return true;
    }

    /**
     * 医疗伤兵条件判断
     * @param cureList
     * @param buildingUUID
     * @param immediate
     * @param hasGold
     * @param hpCode
     * @return
     */
    private boolean cureCheck(List<ArmySoldierPB> cureList, String buildingUUid, int hpCode) {
        if (cureList == null || cureList.size() == 0) {
            logger.error("cure params invalid, playerId: {}, cureList size: {}", player.getId(), cureList == null ? 0 : cureList.size());
            sendError(hpCode, Status.SysError.PARAMS_INVALID);
            return false;
        }
        
        List<QueueEntity> queueEntities = getPlayerData().getBusyCommonQueue(QueueType.CURE_QUEUE_VALUE);
        if (queueEntities.size() > 0) {
            // 判断时间是否结束 ，这里不用做任何处理，系统会定时处理过期的队列
            logger.error("army under treatment, playerId: {}", player.getId());
            sendError(hpCode, Status.Error.ARMY_UNDER_TREATMENT);
            return false;
        }
        
        // 非立即治疗时，需要判断当前建筑是否正在升级
        QueueEntity queuEntity = getPlayerData().getQueueEntityByItemId(buildingUUid);
        if (queuEntity != null) {
        	sendError(hpCode, Status.Error.BUILDING_STATUS_UPGRADE);
        	logger.error("cure failed, building is upgrading, playerId: {}, buildingUUid: {}, queue: {}", player.getId(), buildingUUid, queuEntity);
        	return false;
        }

        // 判断医院是否有治疗完成未领取的兵
        for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
            if (armyEntity.getCureFinishCount() > 0 && !armyEntity.isPlantSoldier()) {
                logger.error("cure finish soldier not none, playerId: {}, armyId: {}", player.getId(), armyEntity.getArmyId());
                sendError(hpCode, Status.Error.CURE_FINISH_SOLDIER_NOT_NONE_VALUE);
                GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.CURE_FINISH_HARVEST, true);
                return false;
            }
        }
        
        for (ArmySoldierPB army : cureList) {
            int armyId = army.getArmyId();
            int count = army.getCount();
            HawkAssert.checkPositive(count);
            // 相关兵种配置是否存在
            BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
            if (armyCfg == null || armyCfg.isPlantSoldier()) {
                logger.error("cure soldier config error, playerId: {}, armyId: {}", player.getId(), armyId);
                sendError(hpCode, Status.SysError.CONFIG_ERROR);
                return false;
            }

            ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
            if (armyEntity.getWoundedCount() < count) {
                logger.error("cure soldier params invalid, playerId: {}, armyId: {}, wounded count: {}, cure count: {}", player.getId(), 
                		armyId, armyEntity.getWoundedCount(), count);
                sendError(hpCode, Status.Error.EXCEED_WOUNED_SOLDIER);
                return false;
            }
        }

        return true;
    }

    /**
     * 治疗伤兵消耗资源
     * @param cureList 
     * @param coverResMap
     * @param recoverTime
     * @param immediate
     * @param useGold 普通治疗资源不足时是否用水晶购买
     * @param hpCode
     * @return
     */
	private List<ItemInfo> cureConsume(List<ArmySoldierPB> cureList, int recoverTime, boolean immediate, boolean useGold, int hpCode) {
		List<ItemInfo> itemInfos = GameUtil.cureItems(player, cureList);
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(itemInfos, immediate || useGold);
		// 立即治疗
		if (immediate) {
			consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(recoverTime, SpeedUpTimeWeightType.TIME_WEIGHT_CURESOLDIER));
		}
		
		if (!consume.checkConsume(player, hpCode)) {
			logger.error("cure soldier consume error, playerId: {}, consume: {}", player.getId(), consume);
			return null;
		}

		AwardItems realCostItems = consume.consumeAndPush(player, Action.SOLDIER_TREATMENT);
		return realCostItems.getAwardItems();
	}

	/**
	 * 超时空急救站急救消耗
	 * 
	 * @param soldierList
	 * @param useGold
	 * @param hpCode
	 * @return
	 */
	private boolean firstAidConsume(List<ArmySoldierPB> soldierList, boolean useGold, int hpCode) {
		List<ItemInfo> itemInfos = GameUtil.soldierRecoverConsume(player, soldierList, true);
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(itemInfos, useGold);
		if (!consume.checkConsume(player, hpCode)) {
			logger.error("firstAidConsume consume error, playerId: {}, consume: {}", player.getId(), consume);
			return false;
		}

		consume.consumeAndPush(player, Action.SOLDIER_FIRST_AID);
		return true;
	}

    /**
     * 开始治疗伤兵
     * @param cureList
     * @param immediate
     * @return
     */
    private Map<Integer, Integer> cureSoldier(List<ArmySoldierPB> cureList, boolean immediate) {
        Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
        // 立即治疗士兵数量
        int totalImmediateCureCnt = 0;
        for (ArmySoldierPB army : cureList) {
            int armyId = army.getArmyId();
            int count = army.getCount();
            armyIds.put(armyId, count);
            ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
            ArmySection section = ArmySection.CURE;
            if (immediate) {
            	totalImmediateCureCnt += count;
                armyEntity.addWoundedCount(-count);
                // 立即治疗或道具加速完成不用等待领取，直接回到兵营
                armyEntity.addFree(count);
                section = ArmySection.FREE;
            } else {
                armyEntity.addWoundedCount(-count);
                armyEntity.immSetCureCountWithoutSync(count);
            }
            
            LogUtil.logArmyChange(player, armyEntity, count, section, ArmyChangeReason.CURE);
        }
        
        // 伤兵治疗统计
        if (totalImmediateCureCnt > 0) {
        	player.getData().getStatisticsEntity().addArmyCureCnt(totalImmediateCureCnt);
        	MissionService.getInstance().missionRefresh(player, GsConst.MissionFunType.FUN_TREAT_ARMY, 0, totalImmediateCureCnt);
        }
        
        return armyIds;
    }
    
    
    /**
     * 治疗伤兵完成后收取
     * @return
     */
    @ProtocolHandler(code = HP.code2.COLLECT_CURE_PLANT_FINISH_SOLDIER_VALUE)
    private boolean onCollectPlantCureFinishSoldier(HawkProtocol protocol) {
        Map<String, QueueEntity> queueEntities = player.getData().getQueueEntitiesByType(QueueType.CURE_PLANT_QUEUE_VALUE);
        // 正在治疗中...
        if (queueEntities != null && queueEntities.size() > 0) {
            logger.error("collect plant failed, army under treatment, playerId: {}", player.getId());
            sendError(protocol.getType(), Status.Error.ARMY_UNDER_TREATMENT);
            return false;
        }

       if (!ArmyService.getInstance().collectCurePlantFinishSoldier(player)) {
    	   sendError(protocol.getType(), Status.Error.CURE_FINISH_SOLDIER_NONE_VALUE);
    	   return false;
       }
       player.responseSuccess(protocol.getType());
        return true;
    }
    
    /**
     * 治疗泰能伤兵
     * @return
     */
    @ProtocolHandler(code = HP.code2.CURE_PLANT_SOLDIER_C_VALUE)
    public boolean onCurePlantSoldier(HawkProtocol protocol) {
        HPCureSoldierReq req = protocol.parseProtocol(HPCureSoldierReq.getDefaultInstance());
        final List<ArmySoldierPB> cureList = req.getSoldiersList();
        final boolean immediate = req.getIsImmediate();
        // 治疗伤兵条件验证
        if (!curePlantCheck(cureList, req.getBuildingUUID(), protocol.getType())) {
            return false;
        }
        // 治疗伤兵需要的时间
		double recoverTime = GameUtil.plantRecoverTime(player, cureList);
        // 治疗伤兵消耗资源
        List<ItemInfo> resCost = curePlantConsume(cureList, (int) Math.ceil(recoverTime), immediate, req.hasUseGold() ? req.getUseGold() : false, protocol.getType());
        if (resCost == null) {
            return false;
        }

        // 治疗伤兵
        Map<Integer, Integer> armyIds = curePlantSoldier(cureList, immediate);
        if (immediate) {
            player.getPush().syncArmyInfo(ArmyChangeCause.CURE_FINISH_COLLECT, armyIds);
            int totalCount = 0;
            for (int count : armyIds.values()) {
            	totalCount += count;
    		}
            ActivityManager.getInstance().postEvent(new TreatArmyEvent(player.getId(), totalCount));
            MissionManager.getInstance().postMsg(player, new EvenntTreatArmy(totalCount));
            
        } else {
        	QueueService.getInstance().addReusableQueue(player, QueueType.CURE_PLANT_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
        			req.getBuildingUUID(), Const.BuildingType.PLANT_HOSPITAL_VALUE, recoverTime * 1000, resCost, GsConst.QueueReusage.PLANT_SOLDIER_CURE);
        	player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_CURE, armyIds.keySet().toArray(new Integer[armyIds.size()]));
        }
        
        if (ArmyService.getInstance().getPlantWoundedCount(player) <= 0) {
        	GameUtil.changeBuildingStatus(player, Const.BuildingType.PLANT_HOSPITAL_VALUE, Const.BuildingStatus.COMMON);
        }

        player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
        player.responseSuccess(protocol.getType());
        // 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.SOLDIER_TREATMENT,
    				Params.valueOf("immediate", immediate),
    				Params.valueOf("army", armyIds));
        return true;
    }
    
    
    /**
     * 医疗伤兵条件判断
     * @param cureList
     * @param buildingUUID
     * @param immediate
     * @param hasGold
     * @param hpCode
     * @return
     */
    private boolean curePlantCheck(List<ArmySoldierPB> cureList, String buildingUUid, int hpCode) {
        if (cureList == null || cureList.size() == 0) {
            logger.error("cure plant params invalid, playerId: {}, cureList size: {}", player.getId(), cureList == null ? 0 : cureList.size());
            sendError(hpCode, Status.SysError.PARAMS_INVALID);
            return false;
        }
        //是否已经解锁泰能医院
        BuildingBaseEntity entity= getPlayerData().getBuildingBaseEntity(buildingUUid);
        if(entity== null || entity.getType() != BuildingType.PLANT_HOSPITAL_VALUE){
        	  logger.error("cure plant build invalid, playerId: {}, cureList size: {}", player.getId(), cureList == null ? 0 : cureList.size());
        	sendError(hpCode, Status.SysError.PARAMS_INVALID);
        	return false;
        }
        List<QueueEntity> queueEntities = getPlayerData().getBusyCommonQueue(QueueType.CURE_PLANT_QUEUE_VALUE);
        if (queueEntities.size() > 0) {
            // 判断时间是否结束 ，这里不用做任何处理，系统会定时处理过期的队列
            logger.error("army plant under treatment, playerId: {}", player.getId());
            sendError(hpCode, Status.Error.ARMY_UNDER_TREATMENT);
            return false;
        }

        // 判断医院是否有治疗完成未领取的兵
        for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
            if (armyEntity.isPlantSoldier() && armyEntity.getCureFinishCount() > 0) {
                logger.error("cure plant finish soldier not none, playerId: {}, armyId: {}", player.getId(), armyEntity.getArmyId());
                sendError(hpCode, Status.Error.CURE_FINISH_SOLDIER_NOT_NONE_VALUE);
                GameUtil.changeBuildingStatus(player, BuildingType.PLANT_HOSPITAL_VALUE, BuildingStatus.PLANT_CURE_FINISH_HARVEST, true);
                return false;
            }
        }
        
        for (ArmySoldierPB army : cureList) {
            int armyId = army.getArmyId();
            int count = army.getCount();
            HawkAssert.checkPositive(count);
            // 相关兵种配置是否存在
            BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
            if (armyCfg == null || !armyCfg.isPlantSoldier()) {
                logger.error("cure plant soldier config error, playerId: {}, armyId: {}", player.getId(), armyId);
                sendError(hpCode, Status.SysError.CONFIG_ERROR);
                return false;
            }

            ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
            if (armyEntity.getWoundedCount() < count) {
                logger.error("cure plant soldier params invalid, playerId: {}, armyId: {}, wounded count: {}, cure count: {}", player.getId(), 
                		armyId, armyEntity.getWoundedCount(), count);
                sendError(hpCode, Status.Error.EXCEED_WOUNED_SOLDIER);
                return false;
            }
        }
        return true;
    }
    
    
    
    /**
     * 治疗伤兵消耗资源
     * @param cureList 
     * @param coverResMap
     * @param recoverTime
     * @param immediate
     * @param useGold 普通治疗资源不足时是否用水晶购买
     * @param hpCode
     * @return
     */
	private List<ItemInfo> curePlantConsume(List<ArmySoldierPB> cureList, int recoverTime, boolean immediate, boolean useGold, int hpCode) {
		List<ItemInfo> itemInfos = GameUtil.curePlantItems(player, cureList);
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(itemInfos, immediate || useGold);
		// 立即治疗
		if (immediate) {
			consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(recoverTime, SpeedUpTimeWeightType.TIME_WEIGHT_CURESOLDIER));
		}
		
		if (!consume.checkConsume(player, hpCode)) {
			logger.error("cure soldier consume error, playerId: {}, consume: {}", player.getId(), consume);
			return null;
		}

		AwardItems realCostItems = consume.consumeAndPush(player, Action.SOLDIER_TREATMENT);
		return realCostItems.getAwardItems();
	}

	  /**
     * 开始治疗伤兵
     * @param cureList
     * @param immediate
     * @return
     */
    private Map<Integer, Integer> curePlantSoldier(List<ArmySoldierPB> cureList, boolean immediate) {
        Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
        // 立即治疗士兵数量
        int totalImmediateCureCnt = 0;
        for (ArmySoldierPB army : cureList) {
            int armyId = army.getArmyId();
            int count = army.getCount();
            armyIds.put(armyId, count);
            ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
            ArmySection section = ArmySection.CURE;
            if (immediate) {
            	totalImmediateCureCnt += count;
                armyEntity.addWoundedCount(-count);
                // 立即治疗或道具加速完成不用等待领取，直接回到兵营
                armyEntity.addFree(count);
                section = ArmySection.FREE;
            } else {
                armyEntity.addWoundedCount(-count);
                armyEntity.immSetCureCountWithoutSync(count);
            }
            
            LogUtil.logArmyChange(player, armyEntity, count, section, ArmyChangeReason.CURE);
        }
        
        // 伤兵治疗统计
        if (totalImmediateCureCnt > 0) {
        	player.getData().getStatisticsEntity().addArmyCureCnt(totalImmediateCureCnt);
        	MissionService.getInstance().missionRefresh(player, GsConst.MissionFunType.FUN_TREAT_ARMY, 0, totalImmediateCureCnt);
        }
        
        return armyIds;
    }
    /**
     * 治疗伤兵结束
     * @param msg
     * @return
     */
    @MessageHandler
    private boolean onCureComplete(CureQueueFinishMsg msg) {
        final int immediateFlag = msg.getImmediate();
        Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
        int totalCureCount = 0;
        for (ArmyEntity armyEntity : getPlayerData().getArmyEntities()) {
        	if(armyEntity.isPlantSoldier()){
        		continue;
        	}
            int cureCount = armyEntity.getCureCount();
            if (cureCount <= 0) {
            	continue;
            }
            
            totalCureCount += cureCount;
            armyIds.put(armyEntity.getArmyId(), cureCount);
            armyEntity.immSetCureCountWithoutSync(0);
            ArmySection section = ArmySection.CURE_FINISH;
            if (immediateFlag >= 0) {
            	armyEntity.addFree(cureCount);
            	section = ArmySection.FREE;
            	ActivityManager.getInstance().postEvent(new TreatArmyEvent(player.getId(), cureCount));
            	MissionManager.getInstance().postMsg(player, new EvenntTreatArmy(cureCount));
            } else {
            	armyEntity.setCureFinishCount(cureCount);
            }
            
            LogUtil.logArmyChange(player, armyEntity, cureCount, section, ArmyChangeReason.CURE_FINISH);
        }
        
        player.getData().getStatisticsEntity().addArmyCureCnt(totalCureCount);
        MissionService.getInstance().missionRefresh(player, GsConst.MissionFunType.FUN_TREAT_ARMY, 0, totalCureCount);
        logger.info("soldier cure complete, playerId: {}, army: {}, totalCnt: {}", player.getId(), armyIds, totalCureCount);

        if (immediateFlag < 0) {
            GameUtil.changeBuildingStatus(player, Const.BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.CURE_FINISH_HARVEST);
            // 异步推送消息
            player.getPush().syncArmyInfo(ArmyChangeCause.CURE_FINISH, armyIds.keySet().toArray(new Integer[armyIds.size()]));
        } else {
            if (ArmyService.getInstance().getWoundedCount(player) <= 0) {
                GameUtil.changeBuildingStatus(player, Const.BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.COMMON);
            }
            // 异步推送消息
            player.getPush().syncArmyInfo(ArmyChangeCause.CURE_FINISH_COLLECT, armyIds);
        }

        player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
        return true;
    }

    
    
    
    /**
     * 治疗伤兵结束
     * @param msg
     * @return
     */
    @MessageHandler
    private boolean onCurePlantComplete(CurePlantQueueFinishMsg msg) {
        final int immediateFlag = msg.getImmediate();
        Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
        int totalCureCount = 0;
        for (ArmyEntity armyEntity : getPlayerData().getArmyEntities()) {
        	if(!armyEntity.isPlantSoldier()){
        		continue;
        	}
            int cureCount = armyEntity.getCureCount();
            if (cureCount <= 0) {
            	continue;
            }
            
            totalCureCount += cureCount;
            armyIds.put(armyEntity.getArmyId(), cureCount);
            armyEntity.immSetCureCountWithoutSync(0);
            ArmySection section = ArmySection.CURE_FINISH;
            if (immediateFlag >= 0) {
            	armyEntity.addFree(cureCount);
            	section = ArmySection.FREE;
            	ActivityManager.getInstance().postEvent(new TreatArmyEvent(player.getId(), cureCount));
            	MissionManager.getInstance().postMsg(player, new EvenntTreatArmy(cureCount));
            } else {
            	armyEntity.setCureFinishCount(cureCount);
            }
            
            LogUtil.logArmyChange(player, armyEntity, cureCount, section, ArmyChangeReason.CURE_FINISH);
        }
        
        player.getData().getStatisticsEntity().addArmyCureCnt(totalCureCount);
        MissionService.getInstance().missionRefresh(player, GsConst.MissionFunType.FUN_TREAT_ARMY, 0, totalCureCount);
        logger.info("soldier cure complete, playerId: {}, army: {}, totalCnt: {}", player.getId(), armyIds, totalCureCount);

        if (immediateFlag < 0) {
            GameUtil.changeBuildingStatus(player, Const.BuildingType.PLANT_HOSPITAL_VALUE, Const.BuildingStatus.PLANT_CURE_FINISH_HARVEST);
            // 异步推送消息
            player.getPush().syncArmyInfo(ArmyChangeCause.CURE_FINISH, armyIds.keySet().toArray(new Integer[armyIds.size()]));
        } else {
            if (ArmyService.getInstance().getPlantWoundedCount(player) <= 0) {
                GameUtil.changeBuildingStatus(player, Const.BuildingType.PLANT_HOSPITAL_VALUE, Const.BuildingStatus.COMMON);
            }
            // 异步推送消息
            player.getPush().syncArmyInfo(ArmyChangeCause.CURE_FINISH_COLLECT, armyIds);
        }

        player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
        return true;
    }
    /**
     * 取消治疗
     * @param msg
     * @return
     */
    @MessageHandler
    private boolean onCureCancel(CureQueueCancelMsg msg) {
        final String cancelBackRes = msg.getCancelBackRes();
        List<Integer> armyIds = new ArrayList<Integer>();
        for (ArmyEntity armyEntity : getPlayerData().getArmyEntities()) {
        	if(armyEntity.isPlantSoldier()){
        		continue;
        	}
            int cureCount = armyEntity.getCureCount();
            if (cureCount <= 0) {
                continue;
            }
            armyIds.add(armyEntity.getArmyId());

            // 返还资源
            BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
            if (armyCfg == null || armyCfg.isPlantSoldier()) {
                logger.error("cure cancle config error, playerId: {}, armyId: {}", player.getId(), armyEntity.getArmyId());
                return false;
            }

            armyEntity.immSetCureCountWithoutSync(0);
            armyEntity.addWoundedCount(cureCount);
            
            LogUtil.logArmyChange(player, armyEntity, cureCount, ArmySection.WOUNDED, ArmyChangeReason.CURE_CANCEL);
        }

        if (!HawkOSOperator.isEmptyString(cancelBackRes)) {
            AwardItems awardItem = AwardItems.valueOf(cancelBackRes);
            awardItem.scale(ConstProperty.getInstance().getRecoverCancelReclaimRate() / 10000d);
            awardItem.rewardTakeAffectAndPush(player, Action.SOLDIER_TREATMENT_CANCEL);
        }
        
        GameUtil.changeBuildingStatus(player, Const.BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.SOLDIER_WOUNDED);
        // 异步推送消息
        player.getPush().syncArmyInfo(ArmyChangeCause.CURE_CANCEL, armyIds.toArray(new Integer[armyIds.size()]));
        player.refreshPowerElectric(PowerChangeReason.CANCEL_CURE);
        return true;
    }

    /**
     * 治疗伤兵完成后收取
     * @return
     */
    @ProtocolHandler(code = HP.code.COLLECT_CURE_FINISH_SOLDIER_VALUE)
    private boolean onCollectCureFinishSoldier(HawkProtocol protocol) {
        Map<String, QueueEntity> queueEntities = player.getData().getQueueEntitiesByType(QueueType.CURE_QUEUE_VALUE);
        // 正在治疗中...
        if (queueEntities != null && queueEntities.size() > 0) {
            logger.error("collect failed, army under treatment, playerId: {}", player.getId());
            sendError(protocol.getType(), Status.Error.ARMY_UNDER_TREATMENT);
            return false;
        }

       if (!ArmyService.getInstance().collectCureFinishSoldier(player)) {
    	   sendError(protocol.getType(), Status.Error.CURE_FINISH_SOLDIER_NONE_VALUE);
    	   return false;
       }
       
       player.responseSuccess(protocol.getType());

        return true;
    }
    
    /**
     * 超时空急救站死兵急救
     * 
     * @return
     */
    @ProtocolHandler(code = HP.code.SOLDIER_FIRST_AID_VALUE)
    private boolean onSoldierFirstAid(HawkProtocol protocol) {
    	SoldierFirstAidReq req = protocol.parseProtocol(SoldierFirstAidReq.getDefaultInstance());
    	String buildingId = req.getBuildingUUID();
    	BuildingBaseEntity building = player.getData().getBuildingBaseEntity(buildingId);
    	// 超时空急救站建筑不存在
    	if (building == null) {
    		 logger.error("soldierFirstAid station not exist, playerId: {}", player.getId());
             sendError(protocol.getType(), Status.Error.FIRST_AID_STATION_NOT_EXIST_VALUE);
    		return false;
    	}
    	
    	final List<ArmySoldierPB> soldierList = req.getSoldiersList();
    	 if (soldierList.isEmpty()) {
             logger.error("soldierFirstAid params invalid, playerId: {}, soldierList size: {}", player.getId(), soldierList.size());
             sendError(protocol.getType(), Status.Error.FIRST_AID_SOLDIER_COUNT_ERR);
             return false;
         }
         
         List<QueueEntity> queueEntities = getPlayerData().getBusyCommonQueue(QueueType.BUILDING_RECOVER_QUEUE_VALUE);
         // 超时空急救站冷却中
         if (queueEntities.size() > 0) {
             logger.error("soldierFirstAid station is recovering, playerId: {}, queue entity: {}", player.getId(), queueEntities.get(0));
             sendError(protocol.getType(), Status.Error.FIRST_AID_STATION_RECOVERING);
             return false;
         }
         
         // 兵的数量检测
         for (ArmySoldierPB army : soldierList) {
             int armyId = army.getArmyId();
             int count = army.getCount();
             HawkAssert.checkPositive(count);
             ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
             if (armyEntity.getTaralabsCount() < count) {
                 logger.error("soldierFirstAid params invalid, playerId: {}, armyId: {}, army count: {}, firstAid count: {}", 
                		 player.getId(), armyId, armyEntity.getTaralabsCount(), count);
                 sendError(protocol.getType(), Status.Error.FIRST_AID_SOLDIER_COUNT_ERR);
                 return false;
             }
         }
         
         // 死兵急救消耗资源
         if (!firstAidConsume(soldierList, req.hasUseGold() ? req.getUseGold() : false, protocol.getType())) {
        	 return false;
         }
         
         // 回收死兵
         Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
         for (ArmySoldierPB army : soldierList) {
             int armyId = army.getArmyId();
             int count = army.getCount();
             armyIds.put(armyId, count);
             ArmyEntity armyEntity = getPlayerData().getArmyEntity(armyId);
             armyEntity.addTaralabsCount(-count);
             armyEntity.addFree(count);
             
             LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.TARALABS_COLLECT);
         }
         
         player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_FIRST_AID, armyIds);
         
         // 改变建筑状态
         GameUtil.changeBuildingStatus(player, Const.BuildingType.FIRST_AID_STATION_VALUE, Const.BuildingStatus.FIRST_AID_BUILDING_RECOVER);
         
         // 产生超时空急救站建筑冷却恢复时间队列
         HawkTuple2<Integer, Integer> tuple = GameUtil.getSuperTimeRescueCd(building.getRescueCd());
         
         QueueService.getInstance().addReusableQueue(player, QueueType.BUILDING_RECOVER_QUEUE_VALUE, QueueStatus.QUEUE_BUILDING_RECOVER_VALUE,
     			req.getBuildingUUID(), BuildingType.FIRST_AID_STATION_VALUE, tuple.first * 1000, 
     			Collections.emptyList(), GsConst.QueueReusage.FIRST_AIT_STAION_RECOVER);
         
         building.setRescueCd(tuple.second);
         
         // 刷新战力
         player.refreshPowerElectric(PowerChangeReason.SOLDIER_FIRST_AID);
         // 操作成功返回
         player.responseSuccess(protocol.getType());
         // 行为日志
         BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.SOLDIER_FIRST_AID,
     				Params.valueOf("army", armyIds));
         
    	return true;
    }

    /**
     * 解雇士兵
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code.FIRE_SOLDIER_C_VALUE)
    private boolean onFireSoldier(HawkProtocol protocol) {
        HPFireSoldierReq req = protocol.parseProtocol(HPFireSoldierReq.getDefaultInstance());
        final List<ArmySoldierPB> soldierList = req.getSoldiersList();
        boolean isWounded = req.getIsWounded();
        if (soldierList == null) {
            logger.error("fire soldier soldierList null, playerId: {}", player.getId());
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }

        Map<Integer, Integer> fireArmy = new HashMap<>();
        if (isWounded) {
        	for (ArmySoldierPB soldier : soldierList) {
        		if (soldier.getCount() <= 0) {
        			HawkLog.errPrintln("fire soldier failed wounded, playerId: {}, armyId: {}, count: {}", player.getId(), soldier.getArmyId(), soldier.getCount());
        			continue;
        		}
        		ArmyEntity armyEntity = player.getData().getArmyEntity(soldier.getArmyId());
        		int soldierHave = armyEntity.getWoundedCount();
        		if (!fireSoldier(soldier, soldierHave, fireArmy, protocol.getType())) {
        			return false;
        		}
        		
        		armyEntity.addWoundedCount(-soldier.getCount());
        		
        		LogUtil.logArmyChange(player, armyEntity, soldier.getCount(), ArmySection.WOUNDED, ArmyChangeReason.FIRE);
        	}
        	
        	int woundedCount = ArmyService.getInstance().getWoundedCount(player);
        	if (woundedCount <= 0) {
        		int cureFinishCount = ArmyService.getInstance().getCureFinishCount(player);
        		if (cureFinishCount <= 0) {
        			GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.COMMON);
        		} else {
        			GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.CURE_FINISH_HARVEST);
        		}
        	}
        } else {
        	for (ArmySoldierPB soldier : soldierList) {
        		if (soldier.getCount() <= 0) {
        			HawkLog.errPrintln("fire soldier failed, playerId: {}, armyId: {}, count: {}", player.getId(), soldier.getArmyId(), soldier.getCount());
        			continue;
        		}
        		ArmyEntity armyEntity = player.getData().getArmyEntity(soldier.getArmyId());
        		int soldierHave = armyEntity.getFree();
        		if (!fireSoldier(soldier, soldierHave, fireArmy, protocol.getType())) {
        			return false;
        		}
        
        		armyEntity.addFree(-soldier.getCount());
        		LogUtil.logArmyChange(player, armyEntity, soldier.getCount(), ArmySection.FREE, ArmyChangeReason.FIRE);
        	}
        }

        List<Integer> armyIds = fireArmy.keySet().stream().collect(Collectors.toList());
        // 异步推送消息
        player.getPush().syncArmyInfo(ArmyChangeCause.FIRE, armyIds.toArray(new Integer[armyIds.size()]));
        player.refreshPowerElectric(PowerChangeReason.FIRE_SOLDIER);

        //向客户端返回信息
        player.responseSuccess(protocol.getType());
        // 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.FIRE_SOLDIER,
     				Params.valueOf("armys", fireArmy));
        return true;
    }
    
    /**
     * 解雇士兵
     * @param soldier
     * @param soldierHave
     * @param fireArmy
     * @param protoType
     * @return
     */
    private boolean fireSoldier(ArmySoldierPB soldier, int soldierHave, Map<Integer, Integer> fireArmy, int protoType) {
    	if (soldierHave < soldier.getCount()) {
			logger.error("fire soldier, playerId: {}, soldierHave: {}, fire count: {}", player.getId(), soldierHave, soldier.getCount());
			sendError(protoType, Status.Error.EXCEED_SOLDIER);
			return false;
		}
		
		int fireCnt = soldier.getCount();
		int armyId = soldier.getArmyId();
		fireArmy.put(armyId, fireCnt);
		// 刷新任务
		ActivityManager.getInstance().postEvent(new SoldierNumChangeEvent(player.getId(), armyId, 0 - fireCnt));
		MissionManager.getInstance().postMsg(player, new EventSoldierAdd(armyId, soldierHave, soldierHave - fireCnt));
        // 我要变强士兵数量变更
        StrengthenGuideManager.getInstance().postMsg( new SGPlayerSoldierNumChangeMsg(player) );
		
		return true;
    }
    
    /**
     * 行军死兵处理
     * @param msg
     * @return
     */
    @MessageHandler
    private boolean onCalcDeadArmy(CalcDeadArmy msg) {
        final List<ArmyInfo> armyList = msg.getArmyDeadList();
        if(armyList == null || armyList.size() == 0){
        	return true;
        }
        
        // 国家医院功能上线后，原来的跨服医院直接去掉
        //calcCrossHospital(armyList);
        calcSoldierProtect(armyList);
        calcRevengeSoldier(armyList);
        
        // 对于回到家时触发的死兵计算，不需要执行以下逻辑
        if (msg.isBackHome()) {
        	return true;
        }
        ArmyHurtDeathEvent armyHurtDeathEvent = new ArmyHurtDeathEvent(this.player.getId(),this.player.getDungeonMap(), this.player.isCsPlayer());
        List<Integer> armyIds = new ArrayList<>();
        for(ArmyInfo info : armyList){
        	if(info.getDeadCount() == 0){
        		continue;
        	}
        	int armyId = info.getArmyId();
        	ArmyEntity entity = player.getData().getArmyEntity(armyId);
        	if(entity == null){
        		continue;
        	}
        	int deadCnt = info.getDeadCount();
    		
        	entity.addMarch(-deadCnt);
        	armyIds.add(armyId);
        	MissionManager.getInstance().postMsg(player, new EventSoldierAdd(armyId, entity.getMarch() + deadCnt, entity.getMarch()));
        	ActivityManager.getInstance().postEvent(new SoldierNumChangeEvent(player.getId(), armyId, deadCnt));
            // 我要变强士兵数量变更
            StrengthenGuideManager.getInstance().postMsg( new SGPlayerSoldierNumChangeMsg(player) );
        	
        	LogUtil.logArmyChange(player, entity, deadCnt, deadCnt, ArmySection.TARALABS, ArmyChangeReason.MARCH_DIE);
        	armyHurtDeathEvent.addDeath(armyId, deadCnt);
        }
        
        player.getPush().syncArmyInfo(ArmyChangeCause.WOUNDED, armyIds.toArray(new Integer[armyIds.size()]));
        player.refreshPowerElectric(PowerChangeReason.SOLDIER_DIE);
        //活动医院
        ActivityManager.getInstance().postEvent(armyHurtDeathEvent);
        return true;
    }
    
    /**
     * 新兵救援
     * 
     * @param armyList
     */
    private void calcSoldierProtect(List<ArmyInfo> armyList) {
		if (player.isInDungeonMap()) {
			return;
		}
    	long now = HawkTime.getMillisecond();
    	long timeLong = ConstProperty.getInstance().getRescueDuration() * 1000L;
    	if (now - player.getEntity().getCreateTime() >= timeLong) {
    		HawkLog.logPrintln("calcSoldierProtect break, player register time too old, playerId: {}", player.getId());
    		return;
    	}
    	
    	try {
    		ProtectSoldierInfo protectSoldierInfo = player.getProtectSoldierInfo(true);
    		if (protectSoldierInfo.getReceiveTotalCount() >= ConstProperty.getInstance().getTotalclaimLimt()) {
    			HawkLog.logPrintln("calcSoldierProtect break, player receive total count exceed, playerId: {}", player.getId());
    			return;
    		}
    		
    		boolean change = false;
    		
    		Map<Integer, Integer> deadSoldierMap = protectSoldierInfo.getDeadSoldier();
    		 for(ArmyInfo info : armyList) {
	        	if(info.getDeadCount() <= 0) {
	        		HawkLog.logPrintln("calcSoldierProtect, playerId: {}, armyId: {}, deadCount: {}", player.getId(), info.getArmyId(), info.getDeadCount());
	        		continue;
	        	}
	        	
	        	BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.getArmyId());
	        	if (armyCfg == null || armyCfg.isDefWeapon()) {
	        		HawkLog.logPrintln("calcSoldierProtect, defWeapon exclude, playerId: {}, armyId: {}", player.getId(), info.getArmyId());
	        		continue;
	        	}
	        	
	        	if (armyCfg.getLevel() > ConstProperty.getInstance().getRecruitslevelLimit()) {
	        		HawkLog.logPrintln("calcSoldierProtect, playerId: {}, armyId: {}, level: {}", player.getId(), info.getArmyId(), armyCfg.getLevel());
	        		continue;
	        	}
	        	
	        	change = true;
	        	Integer count = deadSoldierMap.get(info.getArmyId());
	        	if (count == null) {
	        		deadSoldierMap.put(info.getArmyId(), info.getDeadCount());
	        	} else {
	        		deadSoldierMap.put(info.getArmyId(), info.getDeadCount() + count);
	        	}
    		 }
    		 
    		 if (change) {
    			 // 给客户端同步救援信息
    			 player.getPush().pushProtectSoldierInfo();
    			 RedisProxy.getInstance().updateProtectSoldierInfo(protectSoldierInfo, ConstProperty.getInstance().getRescueDuration());
    			 LogUtil.logProtectSoldierInfo(player, SoldierProtectEventType.TRIGGER_PROTECT);  // 触发新兵救援
    		 }
    		 
    		 HawkLog.logPrintln("calcSoldierProtect success, playerId: {}, armyList: {}", player.getId(), armyList);
    		 
    	} catch (Exception e) {
    		HawkException.catchException(e);
    	}
    }
    
    /**
     * 领取救援新兵
     * 
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code.PROTECT_SOLDIER_RECEIVE_C_VALUE)
    private boolean onReceiveProtectSoldier(HawkProtocol protocol) {
        long now = HawkTime.getMillisecond();
        long timeLong = ConstProperty.getInstance().getRescueDuration() * 1000L;
    	if (now - player.getEntity().getCreateTime() > timeLong) {
    		sendError(protocol.getType(), Status.Error.SOLDIER_RECEIVE_OVER_TIME);
    		player.sendProtocol(HawkProtocol.valueOf(HP.code.PROTECT_SOLDIER_PUSH));
    		HawkLog.logPrintln("receiveProtectSoldier break, player register time too old, playerId: {}", player.getId());
    		return false;
    	}
    	
    	ProtectSoldierInfo protectSoldierInfo = player.getProtectSoldierInfo(false);
    	if (protectSoldierInfo == null) {
    		sendError(protocol.getType(), Status.Error.PROTECT_SOLDIER_NOT_EXIST);
    		HawkLog.logPrintln("receiveProtectSoldier break, protectSoldierInfo not exist, playerId: {}", player.getId());
    		return false;
    	}
		
    	ProtectSoldierReceiveReq req = protocol.parseProtocol(ProtectSoldierReceiveReq.getDefaultInstance());
    	List<ArmySoldierPB> soldierList = req.getReceiveSoldierList();
    	Map<Integer, Integer> deadSoldierMap = protectSoldierInfo.getDeadSoldier();
    	
    	int receiveTotal = 0;
    	for (ArmySoldierPB soldier : soldierList) {
    		int armyId = soldier.getArmyId();
    		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
        	if (cfg == null || cfg.getLevel() > ConstProperty.getInstance().getRecruitslevelLimit()) {
        		sendError(protocol.getType(), Status.Error.PROTECT_SOLDIER_LEVEL_ERROR);
        		HawkLog.logPrintln("receiveProtectSoldier break, param error, playerId: {}, armyId: {}, level: {}", player.getId(), armyId, cfg == null ? 0 : cfg.getLevel());
        		return false;
        	}
        	
        	if (!deadSoldierMap.containsKey(armyId) || deadSoldierMap.get(armyId) < soldier.getCount()) {
        		sendError(protocol.getType(), Status.Error.PROTECT_SOLDIER_COUNT_ERROR);
        		HawkLog.logPrintln("receiveProtectSoldier break, param error, playerId: {}, armyId: {}, count: {}, receive count: {}", 
        				player.getId(), armyId, deadSoldierMap.containsKey(armyId) ? deadSoldierMap.get(armyId) : 0, soldier.getCount());
        		return false;
        	}
        	
        	receiveTotal += soldier.getCount();
    	}
    	
    	if (!HawkTime.isSameDay(now, protectSoldierInfo.getLastReceiveTime())) {
			protectSoldierInfo.setReceiveCountDay(0);
		}
    	
    	protectSoldierInfo.setLastReceiveTime(now);
		
    	int newReceiveCountDay = protectSoldierInfo.getReceiveCountDay() + receiveTotal;
    	int newReceiveTotalCount = protectSoldierInfo.getReceiveTotalCount() + receiveTotal;
		if (newReceiveCountDay > ConstProperty.getInstance().getDayclaimLimt() || newReceiveTotalCount > ConstProperty.getInstance().getTotalclaimLimt()) {
			sendError(protocol.getType(), newReceiveTotalCount > ConstProperty.getInstance().getTotalclaimLimt() ? Status.Error.SOLDIER_RECEIVE_TOTAL_EXCEED : Status.Error.SOLDIER_RECEIVE_DAY_EXCEED);
			HawkLog.logPrintln("receiveProtectSoldier break, count error, playerId: {}, day count: {}, total count: {}", 
					player.getId(), newReceiveCountDay, newReceiveTotalCount);
			return false;
		}
		
		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmySoldierPB soldier : soldierList) {
			int armyId = soldier.getArmyId(); 
			armyIds.add(armyId);
			ArmyEntity entity = player.getData().getArmyEntity(armyId);
			if (entity != null) {
				entity.addFree(soldier.getCount());
				deadSoldierMap.put(armyId, deadSoldierMap.get(armyId) - soldier.getCount());
				LogUtil.logArmyChange(player, entity, soldier.getCount(), ArmySection.FREE, ArmyChangeReason.RECEIVE_PROTECT_SOLDIER);
			} else {
				HawkLog.errPrintln("receiveProtectSoldier error, armyEntity not exist, playerId: {}, armyId: {}", player.getId(), armyId);
			}
			
			LogUtil.logReceiveRevengeSoldier(player, armyId, soldier.getCount(), 2);
		}
		
		protectSoldierInfo.setReceiveCountDay(newReceiveCountDay);
		protectSoldierInfo.setReceiveTotalCount(newReceiveTotalCount);
		
		// 给客户端同步救援信息
		player.getPush().pushProtectSoldierInfo();
		RedisProxy.getInstance().updateProtectSoldierInfo(protectSoldierInfo, ConstProperty.getInstance().getRescueDuration());
		
        // 异步推送消息
        player.getPush().syncArmyInfo(ArmyChangeCause.RECEIVE_PROTECT_SOLDIER, armyIds.toArray(new Integer[armyIds.size()]));
        player.refreshPowerElectric(PowerChangeReason.RECEIVE_PROTECT_SOLDIER);

        //向客户端返回信息
        player.responseSuccess(protocol.getType());
        
        if (newReceiveCountDay >= ConstProperty.getInstance().getDayclaimLimt()) {
        	LogUtil.logProtectSoldierInfo(player, SoldierProtectEventType.RECEIVE_UPLIMIT_DAY); // 达到单日领取上限
        }
        
        if (newReceiveTotalCount >= ConstProperty.getInstance().getTotalclaimLimt()) {
        	LogUtil.logProtectSoldierInfo(player, SoldierProtectEventType.RECEIVE_UPLIMIT_TOTAL); // 达到总领取上限
        }
        
        HawkLog.logPrintln("receiveProtectSoldier success, playerId: {}, armyIds: {}", player.getId(), armyIds);
         
    	return true;
    }
    
    /**
     * 结算大R复仇死兵信息
     * 
     * @param armyList
     */
    protected void calcRevengeSoldier(List<ArmyInfo> armyList) {
    	if (ConstProperty.getInstance().getRevengeShopOpen() <= 0) {
    		return;
    	}
    	
    	if (player.getData().getUnlockedSoldierMaxLevel() < ConstProperty.getInstance().getRevengeShopTroopsLevel()) {
    		HawkLog.logPrintln("calcRevengeSoldier break, playerId: {}, unlocked soldier maxLevel not match", player.getId());
    		return;
    	}
    	
    	try {
    		RevengeInfo revengeInfo = checkAndGetRevengeInfo();
    		if (revengeInfo.getState() == RevengeState.END) {
    			HawkLog.logPrintln("calcRevengeSoldier break, playerId: {}, revenge state： {}", player.getId(), RevengeState.END);
    			return;
    		}
    		
    		int totalCount = 0;
    		Map<Integer, Integer> deadSoldierMap = new HashMap<Integer, Integer>();
    		for (ArmyInfo armyInfo : armyList) {
    			if (armyInfo.getDeadCount() <= 0) {
    				HawkLog.logPrintln("calcRevengeSoldier armyInfo deadCount zero, playerId: {}, armyId: {}", player.getId(), armyInfo.getArmyId());
    				continue;
    			}
    			
    			int armyId = armyInfo.getArmyId();
    			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
    			if (armyCfg == null || armyCfg.isDefWeapon()) {
    				HawkLog.logPrintln("calcRevengeSoldier, defWeapon exclude, playerId: {}, armyId: {}", player.getId(), armyId);
    				continue;
    			}
    			
    			if (armyCfg.getLevel() < ConstProperty.getInstance().getRevengeShopTroopsLevel()) {
    				HawkLog.logPrintln("calcRevengeSoldier soldierLevel not match, playerId: {}, soldierLevel: {}", player.getId(), armyCfg.getLevel());
    				continue;
    			}
    			
    			totalCount += armyInfo.getDeadCount();
    			deadSoldierMap.put(armyInfo.getArmyId(), armyInfo.getDeadCount());
    		}
    		
    		if (totalCount > 0) {
    			// 保险起见，这里还是判断一下时间
    			long timeNow = HawkTime.getMillisecond();
    			if (revengeInfo.getStartTime() == 0) {
    				revengeInfo.setStartTime(timeNow);
    			}
    			
    			RevengeSoldierInfo deadSoldierInfo = new RevengeSoldierInfo(player.getId());
    			deadSoldierInfo.setSoldierDeadTime(timeNow);
    			deadSoldierInfo.setTotalCount(totalCount);
    			deadSoldierInfo.setDeadSoldier(deadSoldierMap);
    			player.getLossTroopInfoList().add(deadSoldierInfo);
    			
    			revengeInfo.setDeadSoldierTotal(revengeInfo.getDeadSoldierTotal() + totalCount);
    			int triggerNum = ConstProperty.getInstance().getRevengeShopTriggerNum();
    			if (revengeInfo.getState() == RevengeState.INIT && revengeInfo.getDeadSoldierTotal() >= triggerNum) {
    				revengeInfo.setStartTime(timeNow);
    				if (player.isActiveOnline()) {
    					revengeInfo.setShopStartTime(timeNow);
    					revengeInfo.setState(RevengeState.ON); 
    					LogUtil.logTriggerRevengeShop(player, revengeInfo.getDeadSoldierTotal(), timeNow, false);
    				} else {
    					revengeInfo.setState(RevengeState.PREPARE); // 不在线的情况下不触发
    				}
    			} 
    			
    			long expireTime = revengeInfo.getStartTime() + ConstProperty.getInstance().getRevengeShopRefresh() - timeNow;
    			RedisProxy.getInstance().addRevengeDeadSoldierInfo(deadSoldierInfo, (int) (expireTime/1000) - 1);

    			pushRevengeInfo();
    			// 更新redis
    			RedisProxy.getInstance().updateRevengeInfo(revengeInfo);
    		}
    		
    		HawkLog.logPrintln("calcRevengeSoldier success, playerId: {}, count: {}, deadSoldierTotal: {}", player.getId(), totalCount, revengeInfo.getDeadSoldierTotal());
    		
    	} catch (Exception e) {
    		HawkException.catchException(e);
    	}
    }
    
    /**
     * 检测大R复仇商店的状态
     * 
     * @param time
     */
    private RevengeInfo checkAndGetRevengeInfo() {
    	long time = HawkTime.getMillisecond();
    	RevengeInfo revengeInfo = player.getRevengeInfo(true);
    	// 条件校验
		if (revengeInfo.getState() == RevengeState.END) {
			long shopRefreshTime = ConstProperty.getInstance().getRevengeShopRefresh();
			if (time - revengeInfo.getStartTime() < shopRefreshTime) {
				HawkLog.logPrintln("calcRevengeSoldier break, on end state, playerId: {}, startTime: {}", player.getId(), revengeInfo.getStartTime());
				return revengeInfo;
			} else {
				revengeInfo = new RevengeInfo(player.getId());
				player.setRevengeInfo(revengeInfo);
				player.getRevengeShopBuyInfo().clear();
				player.getLossTroopInfoList().clear();
				
				RedisProxy.getInstance().updateRevengeInfo(revengeInfo);  // 后面会执行，所以这里可以不用执行
				RedisProxy.getInstance().removeAllRevengeDeadSoldierInfo(player.getId());
				RedisProxy.getInstance().removeRevengeShopBuyInfo(player.getId());
				HawkLog.logPrintln("calcRevengeSoldier reopen, start init state, playerId: {}", player.getId());
			}
		} else if (checkRevengeShopEnd(revengeInfo)) {
			HawkLog.logPrintln("calcRevengeSoldier break, just into end state, playerId: {}", player.getId());
			return revengeInfo;
		}
		
		long shopTriggerTime = ConstProperty.getInstance().getRevengeShopTriggerTime() * 1000L;
		// 第一次触发，getStartTime结果为0，必然走这个逻辑
		if (revengeInfo.getState() == RevengeState.INIT && time - revengeInfo.getStartTime() > shopTriggerTime) {
			int totalCount = 0;
			long startTime = time;
			Iterator<RevengeSoldierInfo> it = player.getLossTroopInfoList().iterator();
			while (it.hasNext()) {
				RevengeSoldierInfo deadInfo = it.next();
				if (time - deadInfo.getSoldierDeadTime() >= shopTriggerTime) {
					it.remove();
					RedisProxy.getInstance().removeRevengeDeadSoldierInfo(deadInfo);
				} else {
					totalCount += deadInfo.getTotalCount();
					if (deadInfo.getSoldierDeadTime() < startTime) {
						startTime = deadInfo.getSoldierDeadTime();
					}
				}
			}
			
			// 更新信息
			revengeInfo.setDeadSoldierTotal(totalCount);
			revengeInfo.setStartTime(startTime);
			RedisProxy.getInstance().updateRevengeInfo(revengeInfo);
			HawkLog.logPrintln("calcRevengeSoldier init state, playerId: {}", player.getId());
		}
		
		// 为了兼容之前的数据
		if (revengeInfo.getState() == RevengeState.ON && revengeInfo.getShopStartTime() == 0) {
			revengeInfo.setShopStartTime(revengeInfo.getStartTime());
			RedisProxy.getInstance().updateRevengeInfo(revengeInfo);
		}
		
		return revengeInfo;
    }
    
    /**
     * 复仇商店结束处理
     */
    private boolean checkRevengeShopEnd(RevengeInfo revengeInfo) {
    	long shopDurationTime = ConstProperty.getInstance().getRevengeShopDuration();
    	long shopRefreshTime = ConstProperty.getInstance().getRevengeShopRefresh();
    	long now = HawkApp.getInstance().getCurrentTime();
    	if (revengeInfo.getState() == RevengeState.ON && 
    			(now - revengeInfo.getShopStartTime() >= shopDurationTime || now - revengeInfo.getStartTime() >= shopRefreshTime)) {
    		
    		revengeInfo.setState(RevengeState.END);
    		sendProtocol(HawkProtocol.valueOf(HP.code.REVENGE_SHOP_END_S));
    		RedisProxy.getInstance().updateRevengeInfo(revengeInfo);
    		// 复仇商店结束邮件
    		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
    				.setPlayerId(player.getId())
    				.setMailId(MailId.REVENGE_SHOP_END)
    				.build());
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * 推送大R死兵复仇信息
     */
    private void pushRevengeInfo() {
    	if (ConstProperty.getInstance().getRevengeShopOpen() <= 0) {
    		return;
    	}
    	
    	RevengeInfo revengeInfo = player.getRevengeInfo(true);
    	HawkLog.logPrintln("calcRevengeSoldier pushRevengeInfo, playerId: {}, state: {}", player.getId(), revengeInfo.getState());
    	if (revengeInfo.getState() != RevengeState.ON) {
    		return;
    	}
    	
    	RevengeInfoPB.Builder builder = RevengeInfoPB.newBuilder();
    	builder.setStartTime(revengeInfo.getShopStartTime());
    	builder.setDeadSoldierTotal(revengeInfo.getDeadSoldierTotal());

    	long shopDurationEndTime = revengeInfo.getShopStartTime() + ConstProperty.getInstance().getRevengeShopDuration();
    	long shopRefreshEndTime = revengeInfo.getStartTime() + ConstProperty.getInstance().getRevengeShopRefresh();
    	builder.setEndTime(Math.min(shopDurationEndTime, shopRefreshEndTime));
    	
    	Map<Integer, Integer> deadSoldierMap = new HashMap<Integer, Integer>();
    	List<RevengeSoldierInfo> deadSoldierList = player.getLossTroopInfoList();
    	for (RevengeSoldierInfo soldierInfo : deadSoldierList) {
    		for (Entry<Integer, Integer> entry : soldierInfo.getDeadSoldier().entrySet()) {
    			if (!deadSoldierMap.containsKey(entry.getKey())) {
    				deadSoldierMap.put(entry.getKey(), entry.getValue());
    			} else {
    				deadSoldierMap.put(entry.getKey(), deadSoldierMap.get(entry.getKey()) + entry.getValue());
    			}
    		}
    	}
    	
    	for (Entry<Integer, Integer> entry : deadSoldierMap.entrySet()) {
    		ArmySoldierPB.Builder deadSoldier = ArmySoldierPB.newBuilder();
    		deadSoldier.setArmyId(entry.getKey());
    		deadSoldier.setCount(entry.getValue());
    		builder.addDeadSoldierInfo(deadSoldier);
    	}
    	
    	for (Entry<Integer, Integer> entry : player.getRevengeShopBuyInfo().entrySet()) {
    		RevengeShopItemInfo.Builder shopBuilder = RevengeShopItemInfo.newBuilder();
    		shopBuilder.setShopId(entry.getKey());
    		shopBuilder.setCount(entry.getValue());
    		builder.addBoughtShopItem(shopBuilder);
    	}
    	
    	player.sendProtocol(HawkProtocol.valueOf(HP.code.REVENGE_INFO_PUSH, builder));
    }
    
}
