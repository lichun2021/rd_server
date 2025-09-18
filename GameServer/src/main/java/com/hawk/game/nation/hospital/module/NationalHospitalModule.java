package com.hawk.game.nation.hospital.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.HotBloodWarArmyCureEvent;
import com.hawk.activity.event.impl.QueueSpeedUpEvent;
import com.hawk.activity.event.impl.UseItemSpeedUpEvent;
import com.hawk.activity.type.ActivityState;
import com.hawk.game.GsConfig;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationConstructionLevelCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.msg.CalcDeadArmy;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.NationalConst;
import com.hawk.game.nation.hospital.NationalHospitalInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.National.DeleteSoldierPB;
import com.hawk.game.protocol.National.DeleteSoldierSetPB;
import com.hawk.game.protocol.National.FirstRecoverSetPB;
import com.hawk.game.protocol.National.NationDeleteDeadSoldierReq;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.National.NationalHospitalArmyPB;
import com.hawk.game.protocol.National.NationalHospitalPB;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.National.RecoverSpeedItemPB;
import com.hawk.game.protocol.National.RecoverSpeedReq;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventUseItemSpeed;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.Mail.CureMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 国家医院
 * 
 * @author lating
 *
 */
public class NationalHospitalModule extends PlayerModule {
	
	// 1. 接收死兵后，2. 删除死兵后， 3. 加速复活后， 5. 登录修复
	public static final int ACCEPT_DEAD_SOLDIER = 1;
	public static final int DELETE_DEAD_SOLDIER = 2;
	public static final int SPEED_RECOVER = 3;
	public static final int COLLECT_RECOVERD = 4;
	public static final int LOGIN_FIX = 5;
	
	/**
	 * 国家医院信息
	 */
	private NationalHospitalInfo nationalHospitalInfo;
	/**
	 * 	国家医院计算死兵恢复的最小间隔
	 */
	private static final int HOSPITAL_CALC_PERIOD = 5000;
	/**
	 * 玩家在线时自动恢复死兵的计算时间间隔
	 */
	private static final long HOSPITAL_CALC_TICK_PERIOD = HawkTime.MINUTE_MILLI_SECONDS * 3;
	/**
	 * 上一次tick的时间 
	 */
	private long lastTickTime;
	
	public NationalHospitalModule(Player player) {
		super(player);
	}
	
	public boolean onPlayerLogin() {
		if (!isNationalHospitalExist()) {
			return true;
		}
		
		// 为了保证在跨服时数据一致性，登录的时候，不管nationalHospitalInfo是否为null，都需要重新赋值
		nationalHospitalInfo = getNationalHospitalInfo();
		
		lastTickTime = HawkTime.getMillisecond();
		
		if (player.isInDungeonMap() && player.getYQZZState() != YQZZState.GAMEING) {
			return true;
		}
		
		if (nationalHospitalInfo.getLastCalcTime() > 0) {
			calcHospitalDeadSoldier("login");
			syncNationalHospitalState();
		} else {
			loginFix();
		}
		
		syncNationalHospitalInfo();
		return true;
	}
	
	/**
	 * 登录修复
	 */
	private void loginFix() {
		int totalCount = player.getData().getArmyEntities().stream().mapToInt(e -> e.getNationalHospitalDeadCount()).sum();
		if (totalCount <= 0) {
			return;
		}
		
		try {
			HawkLog.logPrintln("player login fix national hospital, playerId: {}, info: {}", player.getId(), JSONObject.toJSONString(nationalHospitalInfo));
			long lastTime = player.getLogoutTime();
			if (lastTime <= 0) {
				lastTime = HawkTime.getMillisecond();
			}
			Map<Integer, Integer> oldMap = nationalHospitalInfo.getPresetDeleteSoldier();
			List<Integer> oldList = nationalHospitalInfo.getFirstRecoverArmy();
			nationalHospitalInfo = new NationalHospitalInfo(player.getId());
			nationalHospitalInfo.setLastCalcTime(lastTime);
			nationalHospitalInfo.setQueueStartTime(lastTime);
			nationalHospitalInfo.getPresetDeleteSoldier().putAll(oldMap);
			nationalHospitalInfo.getFirstRecoverArmy().addAll(oldList);
			updateRecoverEndTime(LOGIN_FIX);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public boolean onTick() {
		long now = HawkApp.getInstance().getCurrentTime();
		if (now - lastTickTime < 5000) {
			return true;
		}
		
		lastTickTime = now;
		if (!isNationalHospitalExist()) {
			return true;
		}
		
		if (nationalHospitalInfo == null) {
			nationalHospitalInfo = getNationalHospitalInfo();
			syncNationalHospitalInfo();
		}
		
		if (player.isInDungeonMap() && player.getYQZZState() != YQZZState.GAMEING) {
			return true;
		}
		
		// 服务器正常结算
		if (nationalHospitalInfo.getLastCalcTime() > 0 && now - nationalHospitalInfo.getLastCalcTime() >= HOSPITAL_CALC_TICK_PERIOD) {
			calcHospitalDeadSoldier("normal-tick");
		}
		
		// 服务器最后一次结算
		if (nationalHospitalInfo.getLastCalcTime() > 0 && nationalHospitalInfo.getRecoverEndTime() <= now) {
			endCalc();
			HawkLog.logPrintln("national hospital info reset, playerId: {}, recoverEndTime: {}, lastCalcTime: {}", player.getId(), nationalHospitalInfo.getRecoverEndTime(), nationalHospitalInfo.getLastCalcTime());
			stateToIdle();
			syncNationalHospitalInfo();
		}
		
		return true;
	}
	
	/**
	 * 队列走完前最后一次结算
	 */
	private void endCalc() {
		try {
			int totalCount = player.getData().getArmyEntities().stream().mapToInt(e -> e.getNationalHospitalDeadCount()).sum();
			if (totalCount <= 0) {
				return;
			}
			
			calcHospitalDeadSoldier("end-tick");
			totalCount = player.getData().getArmyEntities().stream().mapToInt(e -> e.getNationalHospitalDeadCount()).sum();
			if (totalCount <= 0) {
				return;
			}
			
			List<ArmyEntity> entityList = player.getData().getArmyEntities();
			for (ArmyEntity entity : entityList) {
				int deadCount = entity.getNationalHospitalDeadCount();
				if (deadCount > 0) {
					entity.setNationalHospitalDeadCount(0);
					entity.setNationalHospitalRecoveredCount(entity.getNationalHospitalRecoveredCount() + deadCount);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 国家医院中的死兵已恢复完成
	 */
	private void stateToIdle() {
		pushQueue(HP.code.QUEUE_DELETE_PUSH_VALUE);
		Map<Integer, Integer> oldMap = nationalHospitalInfo.getPresetDeleteSoldier();
		List<Integer> oldList = nationalHospitalInfo.getFirstRecoverArmy();
		nationalHospitalInfo = new NationalHospitalInfo(player.getId());
		nationalHospitalInfo.getPresetDeleteSoldier().putAll(oldMap);
		nationalHospitalInfo.getFirstRecoverArmy().addAll(oldList);
		syncNationalHospitalState();
	}
	
	/**
	 * 同步国家医院状态
	 */
	public void syncNationalHospitalState() {
		// 普通死兵未恢复完
		int totalCount = player.getData().getArmyEntities().stream().mapToInt(e -> e.getNationalHospitalDeadCount()).sum();
		if (totalCount > 0) {
			return;
		}
		
		// 特殊死兵未恢复完
		totalCount = player.getData().getArmyEntities().stream().mapToInt(e -> e.getTszzDeadCount()).sum();
		if (totalCount > 0) {
			return;
		}
		
		int recoveredCount1 = player.getData().getArmyEntities().stream().mapToInt(e -> e.getNationalHospitalRecoveredCount()).sum();
		int recoveredCount2 = player.getData().getArmyEntities().stream().mapToInt(e -> e.getTszzRecoveredCount()).sum();
		// 还有已恢复完成待收取的普通兵或特殊兵
		if (recoveredCount1 > 0 || recoveredCount2 > 0) {
			player.updateNationRDAndNotify(NationRedDot.HOSPITAL_IDLE);
		} else {
			player.rmNationRDAndNotify(NationRedDot.HOSPITAL_IDLE);
		}
	}
	
	/**
	 *  领取复活的兵
	 * @param protocol
	 * @return
	 */
	 @ProtocolHandler(code = HP.code2.NATIONAL_HOSPITAL_COLLECT_RECOVERD_VALUE)
	 public boolean onCollectRecoveredSoldier(HawkProtocol protocol) {
		 if (!isNationalHospitalExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_HOSPITAL_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 if (player.isInDungeonMap() && player.getYQZZState() != YQZZState.GAMEING) {
			 HawkLog.logPrintln("national hospital collect failed, player in dungeonMap, playerId: {}, dungeonMap: {}", player.getId(), player.getDungeonMap());
			 return false;
		 }
		 
		 // 先计算一次复活
		 calcHospitalDeadSoldier("before-collect");
		 
		 Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
		 for (ArmyEntity entity : player.getData().getArmyEntities()) {
			 int collectCount = entity.getNationalHospitalRecoveredCount();
			 if (collectCount <= 0) {
				 continue;
			 }
			 entity.addFree(collectCount);
			 entity.setNationalHospitalRecoveredCount(0);
			 armyIds.put(entity.getArmyId(), collectCount);
			 LogUtil.logArmyChange(player, entity, collectCount, ArmySection.FREE, ArmyChangeReason.NATION_HOSPITAL_COLLECT);
		 }
		 
		 if (nationalHospitalInfo.getRecoverEndTime() > 0 && nationalHospitalInfo.getRecoverEndTime() <= HawkTime.getMillisecond()) {
			 endCalc();
			 stateToIdle();
		 } else {
			 syncNationalHospitalState();
		 }
		 
		 HawkLog.logPrintln("national hospital collect recovered, playerId: {}, soldier: {}", player.getId(), armyIds);
		 player.getPush().syncArmyInfo(ArmyChangeCause.NATION_HOSPITAL_COLLECT, armyIds);
		 player.refreshPowerElectric(true, PowerChangeReason.NATION_HOSPITAL_COLLECT);
	     player.responseSuccess(protocol.getType());
	     syncNationalHospitalInfo();
		 return true;
	 }
	
	
	 /**
	  *  删除死兵
	  * @param protocol
	  * @return
	  */
	 @ProtocolHandler(code = HP.code2.NATIONAL_HOSPITAL_DELETE_SOLDIER_VALUE)
	 public boolean onDeleteSoldier(HawkProtocol protocol) {
		 if (!isNationalHospitalExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_HOSPITAL_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 if (player.isInDungeonMap() && player.getYQZZState() != YQZZState.GAMEING) {
			 HawkLog.logPrintln("national hospital delete failed, player in dungeonMap, playerId: {}, dungeonMap: {}", player.getId(), player.getDungeonMap());
			 return false;
		 }
		 
		 NationDeleteDeadSoldierReq req = protocol.parseProtocol(NationDeleteDeadSoldierReq.getDefaultInstance());
		 List<DeleteSoldierSetPB> deleteSetInfos = req.getPresetDeleteList();
		 if (!deleteSetInfos.isEmpty()) {
			 return presetDelete(deleteSetInfos, protocol.getType());
		 }
		 
		 calcHospitalDeadSoldier("before-remove");
		 
		 List<DeleteSoldierPB> list = req.getDeleteDeadList();
		 for (DeleteSoldierPB army : list) {
			 if (army.getDeleteCount() <= 0) {
				 continue;
			 }
			 
			 ArmyEntity entity = player.getData().getArmyEntity(army.getArmyId());
			 if (entity == null) {
				 continue;
			 }
			 
			 int oldCount = entity.getNationalHospitalDeadCount();
			 if (oldCount <= 0) {
				 continue;
			 }
			 int count = Math.min(oldCount, army.getDeleteCount());
			 entity.setNationalHospitalDeadCount(oldCount - count);
			 LogUtil.logNationalHospitalRemoveSolider(player, 2, 0, 0, army.getArmyId(), count, oldCount, NationalConst.NATION_HOSPITAL_SOLDIER);
			 HawkLog.logPrintln("national hospital deleteSoldier, playerId: {}, armyId: {}, oldCount: {}, deleteCount: {}", player.getId(), army.getArmyId(), oldCount, army.getDeleteCount());
		 }
		 
		 player.responseSuccess(protocol.getType());
		 
		 // 重新计算死兵复活endTime
		 updateRecoverEndTime(DELETE_DEAD_SOLDIER);
		 if (nationalHospitalInfo.getRecoverEndTime() > 0 && nationalHospitalInfo.getRecoverEndTime() <= HawkTime.getMillisecond()) {
			 endCalc();
			 stateToIdle();
		 } else {
			 pushQueue(HP.code.QUEUE_UPDATE_PUSH_VALUE);
		 }
		 
		 // 给客户端同步数据
		 syncNationalHospitalInfo();
		 
		 return true;
	 }
	 
	 /**
	  * 预设死兵接收信息
	  * 
	  * @param deleteSetInfos
	  * @param protocol
	  * @return
	  */
	 private boolean presetDelete(List<DeleteSoldierSetPB> deleteSetInfos, int protocol) {
		 Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		 // 先去重
		 for (DeleteSoldierSetPB setInfo : deleteSetInfos) {
			 if (setInfo.getLevel() < 0) {
				 sendError(protocol, Status.SysError.PARAMS_INVALID_VALUE);
				 return false;
			 }
			 
			 if (setInfo.getSoldierType() != 0) {
				 SoldierType type = SoldierType.valueOf(setInfo.getSoldierType());
				 if (type == null) {
					 sendError(protocol, Status.SysError.PARAMS_INVALID_VALUE);
					 return false;
				 }
			 }
			 
			 if (map.containsKey(setInfo.getSoldierType())) {
				 sendError(protocol, Status.SysError.PARAMS_INVALID_VALUE);
				 return false; 
			 }
			 
			 map.put(setInfo.getSoldierType(), setInfo.getLevel());
		 }
		 
		 Map<Integer, Integer> finalMap = new HashMap<>();
		 for (Entry<Integer, Integer> entry : map.entrySet()) {
			 int soldierType = entry.getKey(), soldierLevel = entry.getValue();
			 finalMap.put(soldierType, soldierLevel);
			 LogUtil.logNationalHospitalRemoveSolider(player, 1, soldierType, soldierLevel, 0, 0, 0, NationalConst.NATION_HOSPITAL_SOLDIER);
			 // 全兵种取消设置
			 if (soldierType == 0 && soldierLevel == 0) {
				 finalMap.clear();
				 break;
			 } else if(soldierLevel == 0) {  // 单兵种取消设置
				 finalMap.remove(soldierType);
			 }
		 }
		 
		 // soldierType==0表示针对所有的兵种
		 if (finalMap.containsKey(0) && finalMap.size() > 1) {
			 int level = finalMap.get(0);
			 finalMap.clear();
			 finalMap.put(0, level);
		 }
		 
		 nationalHospitalInfo.getPresetDeleteSoldier().clear();
		 nationalHospitalInfo.getPresetDeleteSoldier().putAll(finalMap);
		 
		 HawkLog.logPrintln("national hospital preset deleteSoldier, playerId: {}, deleteSoldier: {} -> {}", player.getId(), map, finalMap);
		 
		 updateNationalHospitalInfo();
		 syncNationalHospitalInfo();
		 return true;
	 }
	 
	 
	 /**
	  * 设置优先恢复的兵
	  * @param protocol
	  * @return
	  */
	 @ProtocolHandler(code = HP.code2.NATIONAL_HOSPITAL_RECOVER_SET_C_VALUE)
	 public boolean setFirstRecover(HawkProtocol protocol) {
		 if (!isNationalHospitalExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_HOSPITAL_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 if (player.isInDungeonMap() && player.getYQZZState() != YQZZState.GAMEING) {
			 HawkLog.logPrintln("national-hospital setFirstRecover failed, player in dungeonMap, playerId: {}, dungeonMap: {}", player.getId(), player.getDungeonMap());
			 return false;
		 }
		 
		 calcHospitalDeadSoldier("before-setFirstRecover");
		 FirstRecoverSetPB req = protocol.parseProtocol(FirstRecoverSetPB.getDefaultInstance());
		 List<Integer> armyTypes = req.getArmyTypeList();
		 nationalHospitalInfo.clearFirstRecoverArmy();
		 for (int armyType : armyTypes) {
			 nationalHospitalInfo.addFirstRecoverArmy(armyType);
		 }
		 
		 HawkLog.logPrintln("national-hospital setFirstRecover, playerId: {}, armyTypes: {}", player.getId(), armyTypes);		 
		 updateNationalHospitalInfo();
		 syncNationalHospitalInfo();
		 return true; 
	 }
	 
	 
	/**
	 *  加速复活
	 * @param protocol
	 * @return
	 */
	 @ProtocolHandler(code = HP.code2.NATIONAL_HOSPITAL_RECOVER_SPEED_VALUE)
	 public boolean onSpeedRecover(HawkProtocol protocol) {
		 if (!isNationalHospitalExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_HOSPITAL_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 if (player.isInDungeonMap() && player.getYQZZState() != YQZZState.GAMEING) {
			 HawkLog.logPrintln("national hospital speed failed, player in dungeonMap, playerId: {}, dungeonMap: {}", player.getId(), player.getDungeonMap());
			 return false;
		 }
		 
		 RecoverSpeedReq req = protocol.parseProtocol(RecoverSpeedReq.getDefaultInstance());
		 List<RecoverSpeedItemPB> itemList = req.getSpeedItemList();
		 if (itemList.isEmpty()) {
			 sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			 return false;
		 }
		 
		 ConsumeItems consume = ConsumeItems.valueOf();
		 long now = HawkTime.getMillisecond();
		 long speedTimeLong = 0, itemCount = 0;
		 for (RecoverSpeedItemPB item : itemList) {
			 ItemEntity entity = player.getData().getItemById(item.getItemUuid());
			 if (entity == null) {
				 sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				 return false;
			 }
			 
			 ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, entity.getItemId());
			 if (itemCfg == null || itemCfg.getItemType() != ToolType.NATIONAL_HOSPITAL_RECOVER_SPEED_VALUE) {
				 sendError(protocol.getType(), Status.Error.NATION_SPEED_ITEM_ERROR_VALUE);
				 return false; 
			 }
			 
			 speedTimeLong += itemCfg.getSpeedUpTime() * 1000L * item.getItemCount();
			 itemCount += item.getItemCount();
			 consume.addConsumeInfo(ItemType.TOOL, item.getItemUuid(), itemCfg.getId(), item.getItemCount());
		 }
		 
		 long gap = ConstProperty.getInstance().getItemSpeedUpTimeThresholdValue() * 1000L;
		 long remainTime = nationalHospitalInfo.getRecoverEndTime() - now;
		 if (itemCount > 1 && speedTimeLong - remainTime > gap) {
			 sendError(protocol.getType(), Status.Error.SPEEDUP_ITEM_COUNT_ERROR);
			 return false;
		 }
		 
		 if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		 }
		 consume.consumeAndPush(player, Action.NATIONAL_HOSPITAL_SPEED_CONSUME);
		 
		 HawkLog.logPrintln("national hospital recover speed, playerId: {}, lastCalcTime: {}, speedTime: {}, history totalReduce: {}", player.getId(), nationalHospitalInfo.getLastCalcTime(), speedTimeLong, nationalHospitalInfo.getTotalReduceTime());
		 
		 recoverSpeed(speedTimeLong);
		 
		 ActivityManager.getInstance().postEvent(new QueueSpeedUpEvent(player.getId(), QueueType.NATIONAL_HOSPITAL_RECOVER_VALUE, speedTimeLong));
		 ActivityManager.getInstance().postEvent(new UseItemSpeedUpEvent(player.getId(), (int)(speedTimeLong / 1000 / 60)));
		 MissionManager.getInstance().postMsg(player, new EventUseItemSpeed((int)(speedTimeLong / 1000 / 60)));
		
		 return true;
	 }
	 
	 /**
	  * 加速
	  * @param time
	  */
	 public void recoverSpeed(long time) {
		 Map<Integer, Integer> oldMap = new HashMap<Integer, Integer>();
		 player.getData().getArmyEntities().stream().forEach(e -> oldMap.put(e.getArmyId(), e.getNationalHospitalDeadCount()));
		 
		 nationalHospitalInfo.setTotalReduceTime(nationalHospitalInfo.getTotalReduceTime() + time);
		 long passTime = 0;
		 if (nationalHospitalInfo.getLastCalcTime() > 0) {
			 passTime = HawkTime.getMillisecond() - nationalHospitalInfo.getLastCalcTime();
			 time += passTime;
		 }
		 HawkLog.debugPrintln("national hospital soldier recover speed-start, playerId: {}, passTime: {}, time: {}, endTime: {}-{}, lastCalcTime: {}-{}", 
				 player.getId(), passTime, time, nationalHospitalInfo.getRecoverEndTime(), HawkTime.formatTime(nationalHospitalInfo.getRecoverEndTime()),
				 nationalHospitalInfo.getLastCalcTime(), HawkTime.formatTime(nationalHospitalInfo.getLastCalcTime()));
		 calcHospitalDeadSoldier(time, true, "speed-recover");
		 updateRecoverEndTime(SPEED_RECOVER);
		 
		 HawkLog.debugPrintln("national hospital soldier recover speed-end, playerId: {}, passTime: {}, time: {}, endTime: {}-{}, lastCalcTime: {}-{}", 
				 player.getId(), passTime, time, nationalHospitalInfo.getRecoverEndTime(), HawkTime.formatTime(nationalHospitalInfo.getRecoverEndTime()),
				 nationalHospitalInfo.getLastCalcTime(), HawkTime.formatTime(nationalHospitalInfo.getLastCalcTime()));
		 
		 if (nationalHospitalInfo.getRecoverEndTime() > 0 && nationalHospitalInfo.getRecoverEndTime() <= HawkTime.getMillisecond()) {
			 endCalc();
			 stateToIdle();
		 } else {
			 pushQueue(HP.code.QUEUE_UPDATE_PUSH_VALUE);
		 }
		 syncNationalHospitalInfo();
		 
		 Map<Integer, Integer> newMap = new HashMap<Integer, Integer>();
		 player.getData().getArmyEntities().stream().forEach(e -> newMap.put(e.getArmyId(), e.getNationalHospitalDeadCount()));
		 LogUtil.logNationalHospitalSpeedRecover(player, oldMap, newMap, NationalConst.NATION_HOSPITAL_SOLDIER);
	 }
	 
	 /**
	  * 客户端请求国家医院信息
	  * @param protocol
	  * @return
	  */
	 @ProtocolHandler(code = HP.code2.NATIONAL_HOSPITAL_INFO_REQ_VALUE)
	 public boolean onNationalHospitalInfoReq(HawkProtocol protocol) {
		 if (!isNationalHospitalExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_HOSPITAL_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 if (player.isInDungeonMap() && player.getYQZZState() != YQZZState.GAMEING) {
			return false;
		 }
		 
		 calcHospitalDeadSoldier("client-request");
		 if (nationalHospitalInfo.getRecoverEndTime() > 0 && nationalHospitalInfo.getRecoverEndTime() <= HawkTime.getMillisecond()) {
			 endCalc();
			 stateToIdle();
			 syncNationalHospitalInfo();
		 } else {
			 syncNationalHospitalInfo();
			 pushQueue(HP.code.QUEUE_UPDATE_PUSH_VALUE);
		 }
		 return true;
	 }
	
	
	/**
	 * 医院接收死兵
	 * 
	 * @param msg
	 */
    @MessageHandler
    private void acceptDeadSoldier(CalcDeadArmy msg) {
    	List<ArmyInfo> armyInfos = msg.getArmyDeadList();
    	if (armyInfos == null || armyInfos.isEmpty()) {
    		return;
    	}
    	//活动医院
    	Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.HOT_BLOOD_WAR_VALUE);
		boolean activiytOpen = activity.isPresent() && activity.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
		if(!player.isCsPlayer() && !player.isInDungeonMap() && activiytOpen){
    		HotBloodWarArmyCureEvent event = new HotBloodWarArmyCureEvent(player.getId());
    		armyInfos.forEach(army->event.addDeath(army.getArmyId(), army.getDeadCount()));
    		ActivityManager.getInstance().postEvent(event);
    		return;
    	}
		if (player.getYQZZState() != YQZZState.GAMEING) {
			if (!CrossActivityService.getInstance().isOpen() && !msg.isGmTrigger()) {
				return;
			}
			if (player.isInDungeonMap()) {
				return;
			}
		}
		
    	HawkLog.logPrintln("national hospital accept dead soldier, playerId: {}, gmTrigger: {}", player.getId(), msg.isGmTrigger());
    	if (!isNationalHospitalExist()) {
    		return;
    	}
    	
    	if (nationalHospitalInfo == null) {
    		nationalHospitalInfo = getNationalHospitalInfo();
    	}
    	
    	int alreadyExist = player.getData().getArmyEntities().stream().mapToInt(e -> e.getNationalHospitalDeadCount()).sum();
    	// 先算一次复活
    	if (alreadyExist > 0) {
    		calcHospitalDeadSoldier("before-accept");
    		alreadyExist = player.getData().getArmyEntities().stream().mapToInt(e -> e.getNationalHospitalDeadCount()).sum();
    	}
    	
    	List<ArmyInfo> actualAcceptList = new ArrayList<>();
    	long totalCount = 0;
    	Map<Integer, Integer> comeinMap = new HashMap<>();
    	for (ArmyInfo army : armyInfos) {
    		if (army.getDeadCount() <= 0) {
    			continue;
    		}
    		
    		comeinMap.put(army.getArmyId(), army.getDeadCount());
    		if (deleteSoldierPreset(army.getArmyId())) {
    			HawkLog.logPrintln("national hospital delete solider while accept, playerId: {}, armyId: {}", player.getId(), army.getArmyId());
    			continue;
    		}
    		
    		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
    		if (cfg == null || cfg.isDefWeapon()) {
    			continue;
    		}
    		
    		actualAcceptList.add(army);
    		totalCount += army.getDeadCount();
    	}
    	
    	if (totalCount <= 0) {
    		HawkLog.logPrintln("national hospital accept no solider, playerId: {}", player.getId());
    		return;
    	}
    	
    	// 国家医院可容纳死兵上限
    	int hospitalAcceptLimit = getHospitalAcceptDeadLimit();
    	if (alreadyExist >= hospitalAcceptLimit) {
			sendMail(totalCount, 0, armyInfos);
    		HawkLog.logPrintln("national hospital accept solider failed, has no capacity, playerId: {}", player.getId());
    		return;
    	}
    	
    	if (alreadyExist + totalCount > hospitalAcceptLimit) {
    		actualAcceptList = actualAcceptList.stream().sorted(new Comparator<ArmyInfo>() {
    			@Override
    			public int compare(ArmyInfo arg0, ArmyInfo arg1) {
    				BattleSoldierCfg cfg0 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg0.getArmyId());
    				BattleSoldierCfg cfg1 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg1.getArmyId());
    				if (cfg0.getLevel() != cfg1.getLevel()) {
    					return cfg1.getLevel() - cfg0.getLevel();
    				} else {
    					return cfg1.getId() - cfg0.getId();
    				}
    			}
    		}).collect(Collectors.toList());
    	}
    	
    	List<ArmyInfo> wound2DeadList = new ArrayList<>();
    	int remainCount = hospitalAcceptLimit - alreadyExist;
    	int acceptCount = 0;
    	NationConstructionLevelCfg hospitalCfg = getHospitalBuildCfg();
    	if (hospitalCfg == null) {
    		hospitalCfg = HawkConfigManager.getInstance().getConfigByIndex(NationConstructionLevelCfg.class, 0);
    	}
    	Map<Integer, Integer> acceptSoldierMap = new HashMap<Integer, Integer>();
    	for (int i = 0; i < actualAcceptList.size(); i++) {
    		if (remainCount <= 0) {
    			break;
    		}
    		
    		ArmyInfo armyInfo = actualAcceptList.get(i);
    		ArmyInfo mailArmy = armyInfo.getCopy();
			mailArmy.setWoundedCount(0);
			mailArmy.setDeadCount(0);
    		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
    		if (cfg == null || cfg.isDefWeapon()) {
    			continue;
    		}
    		int count = Math.min(remainCount, armyInfo.getDeadCount());
    		remainCount -= count;
    		acceptCount += count;
			mailArmy.setDeadCount(armyInfo.getDeadCount() - count);
			if(mailArmy.getDeadCount()>0){
				wound2DeadList.add(mailArmy);
			}
    		acceptSoldierMap.put(armyInfo.getArmyId(), count);
    		ArmyEntity entity = player.getData().getArmyEntity(armyInfo.getArmyId());
    		int oldCount = entity.getNationalHospitalDeadCount();
    		entity.setNationalHospitalDeadCount(oldCount + count);
    		LogUtil.logNationalHospitalAcceptDeadSoldier(player, armyInfo.getArmyId(), oldCount, comeinMap.get(armyInfo.getArmyId()), count, 
    				NationalConst.NATION_HOSPITAL_SOLDIER, hospitalCfg.getLevel(), hospitalCfg.getHospitalAccelerate());
    		comeinMap.remove(armyInfo.getArmyId());
    	}
    	
    	for (Entry<Integer, Integer> entry : comeinMap.entrySet()) {
    		ArmyEntity entity = player.getData().getArmyEntity(entry.getKey());
    		LogUtil.logNationalHospitalAcceptDeadSoldier(player, entry.getKey(), entity == null ? 0 : entity.getNationalHospitalDeadCount(), entry.getValue(), 0, NationalConst.NATION_HOSPITAL_SOLDIER, hospitalCfg.getLevel(), hospitalCfg.getHospitalAccelerate());
    	}
    	
    	if(nationalHospitalInfo.getLastCalcTime() == 0) {
    		nationalHospitalInfo.setLastCalcTime(HawkTime.getMillisecond());
    		HawkLog.logPrintln("national hospital deadsoldier calcTime update, playerId: {}, scene: {}, alreadyExist: {}", player.getId(), "accept-dead-entrance", alreadyExist);
    		updateNationalHospitalInfo();
    	}
    	
    	sendMail(totalCount, acceptCount, wound2DeadList);
    	HawkLog.logPrintln("national hospital accept dead solider, playerId: {}, already count: {}, acceptCount: {}, totalCount: {}, detail: {}", player.getId(), alreadyExist, acceptCount, totalCount, acceptSoldierMap);
    	
    	boolean add = false;
    	if (nationalHospitalInfo.getQueueStartTime() <= 0 || alreadyExist <= 0) {
    		add = true;
    		nationalHospitalInfo.setQueueStartTime(HawkTime.getMillisecond());
    	}
    	
    	// 重新计算结束时间
    	updateRecoverEndTime(ACCEPT_DEAD_SOLDIER);
    	
    	// 给客户端同步信息
    	syncNationalHospitalInfo();
    	
    	pushQueue(add ? HP.code.QUEUE_ADD_PUSH_VALUE : HP.code.QUEUE_UPDATE_PUSH_VALUE);
    }
    
    /**
     * 发送邮件
     * 
     * @param totalCount
     * @param acceptCount
     */
    private void sendMail(long totalCount, int acceptCount,List<ArmyInfo> wound2DeadList) {
    	int actualDeadCnt = (int) (totalCount - acceptCount);
    	if (actualDeadCnt > 0) {
    		try {
    			CureMail.Builder builder = MailBuilderUtil.createCureMail(acceptCount, actualDeadCnt, wound2DeadList);
    			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.NATION_HOSPITAL_SOLDIER_DEAD).addContents(builder).build());
    		} catch (Exception e) {
    			HawkException.catchException(e);
    		}
    	}
    }
    
    /**
     * 判断一个兵种是否属于预设删除的兵种
     * 
     * @param armyId
     * @return
     */
    private boolean deleteSoldierPreset(int armyId) {
    	BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
    	Map<Integer, Integer> presetMap = nationalHospitalInfo.getPresetDeleteSoldier();
    	int level = presetMap.getOrDefault(cfg.getType(), 0);
    	if (level <= 0) {
    		level = presetMap.getOrDefault(0, 0);
    	}
    	return cfg.getLevel() < level;  // 策划说这里按小于算，而非按小于等于算
    }
    
    /**
     * 死兵复活计算
     */
    private void calcHospitalDeadSoldier(String scene) {
    	if (nationalHospitalInfo.getLastCalcTime() <= 0) {
    		return;
    	}
    	
    	long time = HawkTime.getMillisecond() - nationalHospitalInfo.getLastCalcTime();
    	calcHospitalDeadSoldier(time, false, scene);
    }
    
    /**
     * 死兵复活计算
     */
    private boolean calcHospitalDeadSoldier(long time, boolean speed, String scene) {
    	if (nationalHospitalInfo.getLastCalcTime() == 0) {
    		return false;
    	}

    	// 小于30s，暂且不计算
    	if (time < HOSPITAL_CALC_PERIOD) {
    		long remainTime = nationalHospitalInfo.getRecoverEndTime() - HawkTime.getMillisecond();
    		if (remainTime > HOSPITAL_CALC_PERIOD) {
    			HawkLog.logPrintln("calc hospital dead soldier time invalid, playerId: {}, time: {}, remainTime: {}, scene: {}", player.getId(), time, remainTime, scene);
    			return false;
    		}
    	}
    	
    	long nowTime = HawkTime.getMillisecond();
    	int lastArmyId = nationalHospitalInfo.getArmyId();
    	List<ArmyEntity> entityList = player.getData().getArmyEntities().stream()
    			.filter(e -> e.getNationalHospitalDeadCount() > 0 || e.getArmyId() == lastArmyId)
    			.sorted(new Comparator<ArmyEntity>() {
        			@Override
        			public int compare(ArmyEntity arg0, ArmyEntity arg1) {
        				BattleSoldierCfg cfg0 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg0.getArmyId());
        				BattleSoldierCfg cfg1 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg1.getArmyId());
        				if (cfg0.getLevel() != cfg1.getLevel()) {
        					return cfg1.getLevel() - cfg0.getLevel();
        				} else {
        					return cfg1.getId() - cfg0.getId();
        				}
        			}
        		}).collect(Collectors.toList());
    	
    	if (entityList.isEmpty()) {
    		HawkLog.logPrintln("calc hospital dead soldier failed, has no dead soldier, playerId: {}", player.getId());
    		return false;
    	}
    	
    	int total = entityList.stream().mapToInt(e -> e.getNationalHospitalDeadCount()).sum();
    	if (total <= 0) {
    		HawkLog.logPrintln("national hospital deadsoldier calcTime update, playerId: {}, scene: {}, deadCount: 0", player.getId(), "trigger-alreadydead-zero");
    		return false;
    	}
    	
    	int armyIndex = lastArmyId;
    	if (lastArmyId == entityList.get(entityList.size() - 1).getArmyId()) {
    		armyIndex = 0;
    	}
    	
    	if (armyIndex != 0) {
    		for (int i = 0; i < entityList.size(); i++) {
    			if (lastArmyId == entityList.get(i).getArmyId()) {
    				armyIndex = i + 1;
    				break;
    			}
    		}
    		
    		if (armyIndex >= entityList.size()) {
    			armyIndex = 0;
    		}
    	}
    	
    	double doubleTime = time * 1D / 1000;
    	calcDeadCircle(doubleTime, armyIndex, entityList, nowTime, speed, scene);

    	// 说明时间更新了，要存redis
    	if (nationalHospitalInfo.getLastCalcTime() == nowTime) {
    		updateNationalHospitalInfo();
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * 按轮结算
     * 
     * @param totalTime   以秒为单位的时长
     * @param startIndex  
     * @param entityList
     * @return
     */
    private int calcDeadCircle(double totalTime, int startIndex, List<ArmyEntity> entityList, long nowTime, boolean speed, String scene) {
    	List<ArmyEntity> oldEntityList = new ArrayList<>();
    	oldEntityList.addAll(entityList);
    	List<ArmyEntity> firstRecoverArmy = getFirstRecoverArmy();
    	if (!firstRecoverArmy.isEmpty()) {
    		entityList = firstRecoverArmy;
    		startIndex = 0;
    	}
    	
    	int minCount = Integer.MAX_VALUE; 
    	double circleTime = 0D;
    	for (int i = startIndex; i < entityList.size(); i++) {
    		ArmyEntity entity = entityList.get(i);
    		int deadCount = entity.getNationalHospitalDeadCount();
    		if (deadCount <= 0) {
    			continue;
    		}
    		
    		minCount = Math.min(minCount, deadCount);
    		double recoverTime = getSoldierRecoverTimeSingle(entity.getArmyId());
    		circleTime += recoverTime;
    	}
    	
    	// 没有待恢复的死兵
    	if (minCount == Integer.MAX_VALUE) {
    		return 0;
    	} 
    	
    	final double totalTimeFinal = totalTime;
    	boolean notEnoughCircle = totalTime < circleTime;
    	// 时间不够恢复一轮, 恢复完直接返回；或是第一轮结算，先把后续的算完
    	if (notEnoughCircle || startIndex > 0) {
    		for (int i = startIndex; i < entityList.size(); i++) {
    			ArmyEntity entity = entityList.get(i);
    			int deadCount = entity.getNationalHospitalDeadCount();
    			if (deadCount <= 0) {
    				continue;
    			}
    			
    			double recoverTime = getSoldierRecoverTimeSingle(entity.getArmyId());
    			// 第一轮结算的情况，其实可以不需要这个判断
    			if (totalTime < recoverTime) {
    				if (totalTime > 0 && notEnoughCircle && speed) {
    					totalTime -= recoverTime;
    	    			entity.setNationalHospitalDeadCount(deadCount - 1);
    	    			entity.setNationalHospitalRecoveredCount(entity.getNationalHospitalRecoveredCount() + 1);
    	    			nationalHospitalInfo.setArmyId(entity.getArmyId());
    	    			HawkLog.debugPrintln("national hospital soldier recover, playerId: {}, armyId: {}, recoverTime: {}, totalTime: {}, circleTime: {}", player.getId(), entity.getArmyId(), recoverTime, totalTime, circleTime);
    				}
    				break;
    			}
    			
    			totalTime -= recoverTime;
    			entity.setNationalHospitalDeadCount(deadCount - 1);
    			entity.setNationalHospitalRecoveredCount(entity.getNationalHospitalRecoveredCount() + 1);
    			nationalHospitalInfo.setArmyId(entity.getArmyId());
    			HawkLog.debugPrintln("national hospital soldier recover, playerId: {}, armyId: {}, recoverTime: {}, totalTime: {}, circleTime: {}", player.getId(), entity.getArmyId(), recoverTime, totalTime, circleTime);
    		}
    		
    		nationalHospitalInfo.setLastCalcTime(nowTime);
    		HawkLog.logPrintln("national hospital deadsoldier calcTime update, playerId: {}, scene: {}, notEnoughCircle: {}, startIndex: {}, "
    				+ "totalTime: {}, circleTime: {}, armyId: {}, speed: {}", player.getId(), scene, notEnoughCircle, startIndex, totalTimeFinal, 
    				circleTime, nationalHospitalInfo.getArmyId(), speed);
    		if (notEnoughCircle) {
    			return 0;
    		}
    		
    		return calcDeadCircle(totalTime, 0, oldEntityList, nowTime, speed, scene);
    	}
    	
    	int recoverCount = minCount; 
    	// 死兵数量最少的兵种，结算完这么多轮耗时要大于传入的时间
    	if (circleTime * minCount > totalTime) {
    		recoverCount = (int)Math.floor(totalTime / circleTime);
    	}
    	
    	for (int i = 0; i < entityList.size(); i++) {
    		ArmyEntity entity = entityList.get(i);
    		int deadCount = entity.getNationalHospitalDeadCount();
    		if (deadCount <= 0) {
    			continue;
    		}
    		
    		double recoverTime = getSoldierRecoverTimeSingle(entity.getArmyId());
    		totalTime -= recoverTime * recoverCount;
    		entity.setNationalHospitalDeadCount(deadCount - recoverCount);
    		entity.setNationalHospitalRecoveredCount(entity.getNationalHospitalRecoveredCount() + recoverCount);
    		HawkLog.debugPrintln("national hospital soldier recover, playerId: {}, armyId: {}, recoverTime: {}, recoverCount: {}, totalTime: {}, circleTime: {}", player.getId(), entity.getArmyId(), recoverTime, recoverCount, totalTime, circleTime);
    	}
    	nationalHospitalInfo.setLastCalcTime(nowTime);
    	HawkLog.logPrintln("national hospital deadsoldier calcTime update, playerId: {}, scene: {}, recoverCount: {}, minCount: {}, totalTime: {}, circleTime: {}", 
    			player.getId(), scene, recoverCount, minCount, totalTimeFinal, circleTime);
    	
    	return calcDeadCircle(totalTime, 0, oldEntityList, nowTime, speed, scene);
    }
    
    /**
     * 获取优先恢复的兵种数据
     * @return
     */
    private List<ArmyEntity> getFirstRecoverArmy() {
    	if (nationalHospitalInfo.getFirstRecoverArmy().isEmpty()) {
    		return Collections.emptyList();
    	}

    	for (int armyType : nationalHospitalInfo.getFirstRecoverArmy()) {
    		ArmyEntity armyEntity = null;
    		int level = 0;
    		for (ArmyEntity entity : player.getData().getArmyEntities()) {
    			if (entity.getNationalHospitalDeadCount() <= 0) {
    				continue;
    			}
    			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
    			if (cfg != null && cfg.getType() == armyType && cfg.getLevel() > level) {
    				level = cfg.getLevel();
    				armyEntity = entity;
    			}
    		}
    		
    		if (armyEntity != null) {
    			return Arrays.asList(armyEntity);
    		}
    	}
    	
    	return Collections.emptyList();
    }
    
    /**
     * 从redis获取国家医院的相关数据
     * 
     * @return
     */
    public NationalHospitalInfo getNationalHospitalInfo() {
    	String info = RedisProxy.getInstance().getRedisSession().getString(getNationalHospitalRedisKey());
    	if (!HawkOSOperator.isEmptyString(info)) {
    		try {
    			return JSONObject.parseObject(info, NationalHospitalInfo.class);
    		} catch (Exception e) {
    			HawkLog.errPrintln("getNationalHospitalInfo failed, playerId: {}, info: {}", player.getId(), info);
    			HawkException.catchException(e);
    		}
    	}
    	
    	HawkLog.logPrintln("national hospital info reset, playerId: {}, recoverEndTime: {}", player.getId(), 0);
    	return new NationalHospitalInfo(player.getId());
    }
    
    /**
     * 将国家医院的相关的数据更新到redis
     * 
     * @param info
     */
    private void updateNationalHospitalInfo() {
    	try {
    		String value = JSONObject.toJSONString(nationalHospitalInfo);
    		RedisProxy.getInstance().getRedisSession().setString(getNationalHospitalRedisKey(), value);
    	} catch (Exception e) {
    		HawkException.catchException(e);
    	}
    }
    
    /**
     * 获取国家医院相关信息存储的redisKey
     * 
     * @return
     */
    private String getNationalHospitalRedisKey() {
    	return NationalConst.NATION_HOSPITAL_SETTING + ":" + player.getId();
    }
    
    /**
     * 判断国家医院建筑是否存在
     * 
     * @return
     */
    private boolean isNationalHospitalExist() {
    	return getHospitalBuildCfg() != null;
    }
    
    /**
     * 获取国家医院建筑配置
     * 
     * @return
     */
	@SuppressWarnings("deprecation")
	private NationConstructionLevelCfg getHospitalBuildCfg() {
    	NationbuildingType buildType = NationbuildingType.NATION_HOSPITAL;
    	if (player.isCsPlayer()) {
    		int level = NationService.getInstance().getBuildLevel(player.getServerId(), buildType.getNumber());
    		if (level <= 0) {
    			return null;
    		}
    		int baseId = buildType.getNumber() * 100 + level;
    		return HawkConfigManager.getInstance().getConfigByKey(NationConstructionLevelCfg.class, baseId);
    	}
    	
    	NationalBuilding building = NationService.getInstance().getNationBuildingByType(buildType);
    	if (building == null) {
    		return null;
    	}

    	if ("60005".equals(GsConfig.getInstance().getServerId())) {
    		return building.getNextLevelCfg();
    	}
    	
    	return building.getCurrentLevelCfg();
    }
    
    /**
     * 获取单个兵恢复的时长
     * 
     * @param armyId
     * @return
     */
    private double getSoldierRecoverTimeSingle(int armyId) {
    	BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		double basicTime = cfg.getTime();
		int specialTime = NationConstCfg.getInstance().getSpecialSoldierTime(cfg.getLevel());
		if (specialTime > 0) {
			basicTime = specialTime * 1D;
		}
		NationConstructionLevelCfg hospitalCfg = getHospitalBuildCfg();
		if (hospitalCfg == null) {
			return basicTime;
		}
		
		return basicTime * (hospitalCfg.getHospitalAccelerate() * 1D / 10000);
    }
    
    /**
     * 获取国家医院容纳死兵上限
     * 
     * @return
     */
    private int getHospitalAcceptDeadLimit() {
    	NationConstructionLevelCfg hospitalCfg = getHospitalBuildCfg();
    	if (hospitalCfg == null) {
    		return 0;
    	}
    	
    	return hospitalCfg.getHospitalLimit();
    }
    
    /**
     * 更新死兵复活结束时间，分4中情形：1. 接收死兵后，2. 删除死兵后， 3. 加速复活后，外加一个登录修复
     */
    private void updateRecoverEndTime(int changeFrom) {
    	List<ArmyEntity> entityList = player.getData().getArmyEntities();
    	double needTime = 0;
    	for (ArmyEntity entity : entityList) {
    		int deadCount = entity.getNationalHospitalDeadCount();
    		if (deadCount <= 0) {
    			continue;
    		}
    		
    		double recoverTime = getSoldierRecoverTimeSingle(entity.getArmyId());
    		needTime += recoverTime * deadCount;
    	}
    	
    	needTime *= 1000;
    	long oldTime = nationalHospitalInfo.getRecoverEndTime();
    	nationalHospitalInfo.setRecoverEndTime(nationalHospitalInfo.getLastCalcTime() + (long)needTime);
    	updateNationalHospitalInfo();
    	int alreadyExist = player.getData().getArmyEntities().stream().mapToInt(e -> e.getNationalHospitalDeadCount()).sum();
    	int hospitalAcceptLimit = getHospitalAcceptDeadLimit();
    	LogUtil.logNationalHospitalDeadRecoverQueue(player, changeFrom, nationalHospitalInfo.getQueueStartTime(), oldTime, nationalHospitalInfo.getRecoverEndTime(), alreadyExist, hospitalAcceptLimit - alreadyExist, NationalConst.NATION_HOSPITAL_SOLDIER);
    }
    
    /**
     * 同步恢复队列
     */
    public void pushQueue(int protocol) {
    	try {
    		QueueEntity queueEntity = new QueueEntity();
    		queueEntity.setId(nationalHospitalInfo.getQueueUuid());
    		queueEntity.setEndTime(nationalHospitalInfo.getRecoverEndTime());
    		queueEntity.setItemId("");
    		queueEntity.setPlayerId(player.getId());
    		queueEntity.setQueueType(QueueType.NATIONAL_HOSPITAL_RECOVER_VALUE);
    		queueEntity.setStartTime(nationalHospitalInfo.getQueueStartTime());
    		queueEntity.setTotalQueueTime(queueEntity.getEndTime() - queueEntity.getStartTime() + nationalHospitalInfo.getTotalReduceTime());
    		queueEntity.setBuildingType(0);
    		queueEntity.setTotalReduceTime(nationalHospitalInfo.getTotalReduceTime());
    		queueEntity.setStatus(QueueStatus.QUEUE_STATUS_COMMON_VALUE);
    		queueEntity.setReusage(-1);  // 可重用队列
    		
    		if (protocol == HP.code.QUEUE_DELETE_PUSH_VALUE) {
    			QueueService.getInstance().deleteQueueNotice(player, queueEntity);
    			return;
    		}
    		
    		if (nationalHospitalInfo.getRecoverEndTime() > 0 && nationalHospitalInfo.getRecoverEndTime() <= HawkTime.getMillisecond()) {
    			QueueService.getInstance().deleteQueueNotice(player, queueEntity);
    		} else {
    			QueuePB.Builder pushQueue = BuilderUtil.genQueueBuilder(queueEntity);
    			player.sendProtocol(HawkProtocol.valueOf(protocol, pushQueue));
    		}
    	} catch (Exception e) {
    		HawkException.catchException(e);
    	}
	}
    
    
    
    /**
     * 同步国家医院信息
     */
    private void syncNationalHospitalInfo() {
    	NationalHospitalPB.Builder builder = NationalHospitalPB.newBuilder();
    	builder.setAcceptDeadLimit(getHospitalAcceptDeadLimit());
    	builder.setRecoverEndTime(nationalHospitalInfo.getRecoverEndTime());
    	builder.setRecoverStartTime(nationalHospitalInfo.getQueueStartTime());
    	builder.setTotalSpeedTime(nationalHospitalInfo.getTotalReduceTime());
    	builder.setArmyId(getNextRecoverArmy());
    	builder.setCalcTime(nationalHospitalInfo.getLastCalcTime());
    	for (Entry<Integer, Integer> entry : nationalHospitalInfo.getPresetDeleteSoldier().entrySet()) {
    		DeleteSoldierSetPB.Builder presetBuilder = DeleteSoldierSetPB.newBuilder();
    		presetBuilder.setSoldierType(entry.getKey());
    		presetBuilder.setLevel(entry.getValue());
    		builder.addPresetDelete(presetBuilder);
    	}
    	
    	for (ArmyEntity entity : player.getData().getArmyEntities()) {
    		int deadCount = entity.getNationalHospitalDeadCount();
    		int recoveredCount = entity.getNationalHospitalRecoveredCount();
    		if (deadCount <= 0 && recoveredCount <= 0) {
    			continue;
    		}
    		
    		NationalHospitalArmyPB.Builder armyBuilder = NationalHospitalArmyPB.newBuilder();
    		armyBuilder.setArmyId(entity.getArmyId());
    		armyBuilder.setDeadSoldierCount(deadCount);
    		armyBuilder.setRecoveredCount(recoveredCount);
    		builder.addArmy(armyBuilder);
    	}
    	
    	NationConstructionLevelCfg hospitalCfg = getHospitalBuildCfg();
    	builder.setBuildLevel(hospitalCfg.getLevel());
    	if (!nationalHospitalInfo.getFirstRecoverArmy().isEmpty()) {
    		builder.addAllFirstRecoverArmy(nationalHospitalInfo.getFirstRecoverArmy());
    	}
    	
    	player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_HOSPITAL_INFO_SYNC, builder));
    }
    
    private int getNextRecoverArmy() {
    	int lastArmyId = nationalHospitalInfo.getArmyId();
    	List<ArmyEntity> entityList = player.getData().getArmyEntities().stream()
    			.filter(e -> e.getNationalHospitalDeadCount() > 0 || e.getArmyId() == lastArmyId)
    			.sorted(new Comparator<ArmyEntity>() {
        			@Override
        			public int compare(ArmyEntity arg0, ArmyEntity arg1) {
        				BattleSoldierCfg cfg0 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg0.getArmyId());
        				BattleSoldierCfg cfg1 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg1.getArmyId());
        				if (cfg0.getLevel() != cfg1.getLevel()) {
        					return cfg1.getLevel() - cfg0.getLevel();
        				} else {
        					return cfg1.getId() - cfg0.getId();
        				}
        			}
        		}).collect(Collectors.toList());
    	if (entityList.isEmpty()) {
    		return 0;
    	}
    	
    	if (entityList.size() == 1 && entityList.get(0).getNationalHospitalDeadCount() == 0) {
    		return 0;
    	}
    	
    	int armyIndex = lastArmyId;
    	if (lastArmyId == entityList.get(entityList.size() - 1).getArmyId()) {
    		armyIndex = 0;
    	}
    	
    	if (armyIndex != 0) {
    		for (int i = 0; i < entityList.size(); i++) {
    			if (lastArmyId == entityList.get(i).getArmyId()) {
    				armyIndex = i + 1;
    				break;
    			}
    		}
    		
    		if (armyIndex >= entityList.size()) {
    			armyIndex = 0;
    		}
    	}
    	
    	return entityList.get(armyIndex).getArmyId();
    }
    
}
