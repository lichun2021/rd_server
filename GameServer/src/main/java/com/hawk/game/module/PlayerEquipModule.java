package com.hawk.game.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.EquipChangeEvent;
import com.hawk.activity.event.impl.EquipForgeEvent;
import com.hawk.activity.event.impl.EquipMaterialMergeEvent;
import com.hawk.activity.event.impl.EquipResolveEvent;
import com.hawk.activity.event.impl.EquipUpEvent;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.EquipMaterialCfg;
import com.hawk.game.config.EquipmentCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.EquipQueueCancelMsg;
import com.hawk.game.msg.EquipQueueFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.player.equip.EquipSlot;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.Equip.EquipForgeOrQualityUpReq;
import com.hawk.game.protocol.Equip.EquipQueueType;
import com.hawk.game.protocol.Equip.EquipState;
import com.hawk.game.protocol.Equip.ForgeResolveReq;
import com.hawk.game.protocol.Equip.MaterialOperationReq;
import com.hawk.game.protocol.Equip.PutOnEquipReq;
import com.hawk.game.protocol.Equip.TakeOffEquipReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventCommanderPutDownEquip;
import com.hawk.game.service.mssion.event.EventCommanderPutOnEquip;
import com.hawk.game.service.mssion.event.EventEquipQualityUp;
import com.hawk.game.service.mssion.event.EventForgeEquip;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;

/**
 * 装备系统模块
 * 
 * @author Jesse
 */
public class PlayerEquipModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 * @param player
	 */
	public PlayerEquipModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		player.getPush().syncEquipInfo();
		player.getPush().syncCommanderEquipSoltInfo();
		return true;
	}

	@Override
	protected boolean onPlayerLogout() {
		return true;
	}
	
	/**
	 * 装备展示标记
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EQUIP_OPEN_PAGE_C_VALUE)
	private boolean onOpenEquipBag(HawkProtocol protocol) {
		// 清除新装备标签
		List<EquipEntity> equips = player.getData().getEquipEntities();
		for (EquipEntity equipEntity : equips) {
			if (equipEntity.isNew()) {
				equipEntity.setNew(false);
			}
		}
		return true;
	}
	
	/**
	 * 打开背包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EQUIP_OPEN_MATERIAL_BAG_C_VALUE)
	private boolean openBag(HawkProtocol protocol) {
		// 清除新装备材料标签
		List<ItemEntity> items = player.getData().getItemEntities();
		for (ItemEntity item : items) {
			if (!item.isNew()) {
				continue;
			}
			ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
			// 装备材料单独处理
			if (cfg.getItemType() == ToolType.EQUIP_MATERIAL_VALUE) {
				item.setNew(false);
			}
		}
		return true;
	}
	
	/**
	 * 打造装备
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EQUIP_FORGE_C_VALUE)
	private boolean onEquipForge(HawkProtocol protocol) {
		if (!isBuildingUnlock()) {
			sendError(protocol.getType(), Status.Error.EQUIP_BUILDING_LOCKED);
			return false;
		}
		
		EquipForgeOrQualityUpReq req = protocol.parseProtocol(EquipForgeOrQualityUpReq.getDefaultInstance());
		boolean immediately = req.getImmediately();
		// 装备队列已被占用
		if (!immediately && isQueueBusy()) {
			sendError(protocol.getType(), Status.Error.EQUIP_QUEUE_BUSY);
			return false;
		}
		
		int cfgId = req.getTargetId();
		EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, cfgId);
		if (equipCfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}
		boolean needEquip = equipCfg.getQuality() > 1;
		EquipEntity equip = null;
		if (needEquip) {
			if (!req.hasMaterialEquip()) {
				sendError(protocol.getType(), Status.SysError.DATA_ERROR);
				return false;
			}
			equip = getPlayerData().getEquipEntity(req.getMaterialEquip());
			if (equip == null) {
				sendError(protocol.getType(), Status.Error.EQUIP_NOT_EXIST);
				return false;
			}
			// 装备非空闲
			if (equip.getState() != EquipState.FREE_VALUE) {
				sendError(protocol.getType(), Status.Error.EQUIP_NOT_FREE);
				return false;
			}
		}
		long costTime = equipCfg.getForgeTime();
		int eff = player.getEffect().getEffVal(EffType.EQUIP_SPEED_UP) + player.getEffect().getEffVal(EffType.EQUIP_FORGE_SPEED_UP);
		costTime = (long) (costTime / (1 + eff * GsConst.EFF_PER)); 
		// 道具消耗
		HawkTuple2<Boolean, List<ItemInfo>> result = equipOperationConsume(protocol.getType(), immediately, equipCfg, costTime, Action.EQUIP_FORGE);
		if (!result.first) {
			return false;
		}
		if (immediately) {
			onForgeOrQualityUpFinish(cfgId, equip);
			MissionManager.getInstance().postMsg(player, new EventForgeEquip(cfgId, equipCfg.getLevel(), equipCfg.getQuality()));
		} else {
			if (equip != null) {
				equip.setState(EquipState.AS_MATERIAL_VALUE);
				player.getPush().syncEquipInfo(equip);
			}
			String itemId = serializQueueItemId(EquipQueueType.FORGE, equipCfg.getId(), equip);
			QueueService.getInstance().addReusableQueue(player, QueueType.EQUIP_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					itemId, BuildingType.EQUIP_RESEARCH_INSTITUTE_VALUE, costTime, result.second, QueueReusage.EQUIP_QUEUE);
		}
		player.responseSuccess(protocol.getType());
		ActivityManager.getInstance().postEvent(new EquipUpEvent(player.getId(), 1));
		BehaviorLogger.log4Service(player, Source.EQUIP, Action.EQUIP_UPGRADE_QUALITY,
				Params.valueOf("targetCfgId", cfgId),
				Params.valueOf("materialEquip", equip == null ? null : equip.getId()),
				Params.valueOf("immediately", immediately));
		return true;
	}
	
	/**
	 * 装备升品质
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EQUIP_QUALITY_UP_C_VALUE)
	private boolean onUpgradeEquipQuality(HawkProtocol protocol) {
		if (!isBuildingUnlock()) {
			sendError(protocol.getType(), Status.Error.EQUIP_BUILDING_LOCKED);
			return false;
		}
		EquipForgeOrQualityUpReq req = protocol.parseProtocol(EquipForgeOrQualityUpReq.getDefaultInstance());
		boolean immediately = req.getImmediately();
		// 装备队列已被占用
		if (!immediately && isQueueBusy()) {
			sendError(protocol.getType(), Status.Error.EQUIP_QUEUE_BUSY);
			return false;
		}
		String equipId = req.getMaterialEquip();
		EquipEntity equip = player.getData().getEquipEntity(equipId);
		// 装备不存在
		if (equip == null) {
			sendError(protocol.getType(), Status.Error.EQUIP_NOT_EXIST);
			return false;
		}
		// 装备非空闲
		if (equip.getState() != EquipState.FREE_VALUE) {
			sendError(protocol.getType(), Status.Error.EQUIP_NOT_FREE);
			return false;
		}
		EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, equip.getCfgId());
		if (equipCfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}
		// 最高品质不能提升
		if(equipCfg.getQuality() >= ConstProperty.getInstance().getEquipMaxQuality()){
			sendError(protocol.getType(), Status.Error.EQUIP_MAX_QUALITY);
			return false;
		}
		
		EquipmentCfg aftCfg = getEquipCfgWithOutId(equipCfg.getMouldId(), equipCfg.getQuality() + 1);
		int targetId = req.getTargetId();
		// 目标装备id错误
		if (aftCfg.getId() != targetId) {
			sendError(protocol.getType(), Status.SysError.DATA_ERROR);
			return false;
		}
		
		long costTime = aftCfg.getForgeTime();
		int eff = player.getEffect().getEffVal(EffType.EQUIP_SPEED_UP) + player.getEffect().getEffVal(EffType.EQUIP_FORGE_SPEED_UP);
		costTime = (long) (costTime / (1 + eff * GsConst.EFF_PER)); 
		// 道具&燃料消耗
		HawkTuple2<Boolean, List<ItemInfo>> result = equipOperationConsume(protocol.getType(), immediately, aftCfg, costTime, Action.EQUIP_UPGRADE_QUALITY);
		if (!result.first) {
			return false;
		}
		
		if (immediately) {
			onForgeOrQualityUpFinish(targetId, equip);
			MissionManager.getInstance().postMsg(player, new EventEquipQualityUp(equipCfg.getQuality(), aftCfg.getQuality(), targetId));
		} else {
			equip.setState(EquipState.QUALITY_UPING_VALUE);
			String itemId = serializQueueItemId(EquipQueueType.QUALITY_UP, aftCfg.getId(), equip);
			QueueService.getInstance().addReusableQueue(player, QueueType.EQUIP_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					itemId, BuildingType.EQUIP_RESEARCH_INSTITUTE_VALUE, costTime, result.second, QueueReusage.EQUIP_QUEUE);
			player.getPush().syncEquipInfo(equip);
		}
		player.responseSuccess(protocol.getType());
		BehaviorLogger.log4Service(player, Source.EQUIP, Action.EQUIP_UPGRADE_QUALITY,
				Params.valueOf("equipId", equipId),
				Params.valueOf("cfgIdBef", equipCfg.getId()),
				Params.valueOf("cfgIdAft", aftCfg.getId()),
				Params.valueOf("immediately", immediately));
				
		
		ActivityManager.getInstance().postEvent(new EquipUpEvent(player.getId(), 1));
		return true;
	}
	
	/**
	 * 装备分解
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EQUIP_RESOLVE_C_VALUE)
	private boolean onEquipResolve(HawkProtocol protocol) {
		if (!isBuildingUnlock()) {
			sendError(protocol.getType(), Status.Error.EQUIP_BUILDING_LOCKED);
			return false;
		}
		ForgeResolveReq req = protocol.parseProtocol(ForgeResolveReq.getDefaultInstance());
		boolean immediately = req.getImmediately();
		// 装备队列已被占用
		if (!immediately && isQueueBusy()) {
			sendError(protocol.getType(), Status.Error.EQUIP_QUEUE_BUSY);
			return false;
		}
		String equipId = req.getEquipId();
		EquipEntity equip = player.getData().getEquipEntity(equipId);
		// 装备不存在
		if (equip == null) {
			sendError(protocol.getType(), Status.Error.EQUIP_NOT_EXIST);
			return false;
		}
		
		// 装备非空闲
		if(equip.getState() != EquipState.FREE_VALUE){
			sendError(protocol.getType(), Status.Error.EQUIP_NOT_EXIST);
			return false;
		}
		EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, equip.getCfgId());
		
		long costTime = equipCfg.getResolveTime();
		int eff = player.getEffect().getEffVal(EffType.EQUIP_SPEED_UP) + player.getEffect().getEffVal(EffType.EQUIP_RESOLVE_SPEED_UP);
		costTime = (long) (costTime / (1 + eff * GsConst.EFF_PER)); 
		// 道具&燃料消耗
		HawkTuple2<Boolean, List<ItemInfo>> result = equipOperationConsume(protocol.getType(), immediately, equipCfg, costTime, Action.EQUIP_RESOLVE);
		
		if (!result.first) {
			return false;
		}
		if (immediately) {
			onResolveFinish(equip.getCfgId(), equip);
		} else {
			equip.setState(EquipState.RESOLVING_VALUE);
			String itemId = serializQueueItemId(EquipQueueType.RESOLVE, equipCfg.getId(), equip);
			QueueService.getInstance().addReusableQueue(player, QueueType.EQUIP_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					itemId, BuildingType.EQUIP_RESEARCH_INSTITUTE_VALUE, costTime, result.second, QueueReusage.EQUIP_QUEUE);
		}
		player.getPush().syncEquipInfo(equip);
		player.responseSuccess(protocol.getType());
		BehaviorLogger.log4Service(player, Source.EQUIP, Action.EQUIP_RESOLVE,
				Params.valueOf("equipId", equip.getId()),
				Params.valueOf("equipCfgId", equip.getCfgId()),
				Params.valueOf("immediately", immediately));
		return true;
	}
	
	/**
	 * 装备材料合成
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EQUIP_MATERIAL_MERGE_C_VALUE)
	private boolean onMaterialMerge(HawkProtocol protocol) {
		if (!isBuildingUnlock()) {
			sendError(protocol.getType(), Status.Error.EQUIP_BUILDING_LOCKED);
			return false;
		}
		MaterialOperationReq req = protocol.parseProtocol(MaterialOperationReq.getDefaultInstance());
		int materialId = req.getMaterialId();
		int count = req.getCount();
		HawkAssert.checkPositive(count);
		
		EquipMaterialCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipMaterialCfg.class, materialId);
		if (cfg == null || cfg.getConsumeList().isEmpty()) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}
		List<ItemInfo> consumeList = cfg.getConsumeList();
		List<ItemInfo> costItems = new ArrayList<>();
		for (ItemInfo item : consumeList) {
			costItems.add(new ItemInfo(item.getType(), item.getItemId(), item.getCount() * count));
		}
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(costItems, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		consume.consumeAndPush(player, Action.EQUIP_MATERIAL_MERGE);
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItem(ItemType.TOOL_VALUE, materialId, count);
		awardItems.rewardTakeAffectAndPush(player, Action.EQUIP_MATERIAL_MERGE);
		player.responseSuccess(protocol.getType());
		BehaviorLogger.log4Service(player, Source.EQUIP, Action.EQUIP_MATERIAL_MERGE,
				Params.valueOf("targetId", materialId),
				Params.valueOf("targetCnt", count));
		ActivityManager.getInstance().postEvent(new EquipMaterialMergeEvent(player.getId(), count));
		return true;
	}
	
	/**
	 * 装备材料分解
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EQUIP_MATERIAL_RESOLVE_C_VALUE)
	private boolean onMaterialResolve(HawkProtocol protocol) {
		if (!isBuildingUnlock()) {
			sendError(protocol.getType(), Status.Error.EQUIP_BUILDING_LOCKED);
			return false;
		}
		MaterialOperationReq req = protocol.parseProtocol(MaterialOperationReq.getDefaultInstance());
		int materialId = req.getMaterialId();
		int count = req.getCount();
		HawkAssert.checkPositive(count);
		
		int maxCount = ConstProperty.getInstance().getQuickBreakUpperLimit();
		count = Math.min(count, maxCount);
		EquipMaterialCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipMaterialCfg.class, materialId);
		if (cfg == null || cfg.getResolveList().isEmpty()) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addItemConsume(materialId, count);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		consume.consumeAndPush(player, Action.EQUIP_MATERIAL_RESOLVE);
		
		List<ItemInfo> rewardItemList = cfg.getResolveList();
		for (ItemInfo info : rewardItemList) {
			info.setCount(info.getCount() * count);
		}
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(rewardItemList);
		awardItems.rewardTakeAffectAndPush(player, Action.EQUIP_MATERIAL_RESOLVE);
		
		player.responseSuccess(protocol.getType());
		BehaviorLogger.log4Service(player, Source.EQUIP, Action.EQUIP_MATERIAL_RESOLVE,
				Params.valueOf("materialId", materialId),
				Params.valueOf("count", count));
		return true;
	}
	
	/**
	 * 装备穿戴
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PUT_ON_EQUIP_C_VALUE)
	private boolean onPutOnEquip(HawkProtocol protocol) {
		PutOnEquipReq req = protocol.parseProtocol(PutOnEquipReq.getDefaultInstance());
		String equipId = req.getEquipId();
		int slotId = req.getPos();
		EquipEntity equip = player.getData().getEquipEntity(equipId);
		if (equip == null) {
			sendError(protocol.getType(), Status.Error.EQUIP_NOT_EXIST);
			return false;
		}
		if (equip.getState() != EquipState.FREE_VALUE) {
			sendError(protocol.getType(), Status.Error.EQUIP_NOT_FREE);
			return false;
		}
		int result = putOnCommanderEquip(equip, slotId);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
		} else {
			sendError(protocol.getType(), result);
		}
		return true;
	}
	
	/**
	 * 卸下装备
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAKE_OFF_EQUIP_C_VALUE)
	private boolean onTakeOffEquip(HawkProtocol protocol) {
		TakeOffEquipReq req = protocol.parseProtocol(TakeOffEquipReq.getDefaultInstance());
		String equipId = req.getEquipId();
		EquipEntity equip = player.getData().getEquipEntity(equipId);
		if (equip == null) {
			sendError(protocol.getType(), Status.Error.EQUIP_NOT_EXIST);
			return false;
		}
		int result = Status.SysError.SUCCESS_OK_VALUE;
		switch (equip.getState()) {
		case EquipState.ON_COMMANDER_VALUE:
			result = takeOffCommanderEquip(equip);
			break;
		default:
			result = Status.SysError.PARAMS_INVALID_VALUE;
			break;
		}
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
		} else {
			sendError(protocol.getType(), result);
		}
		return false;
	}
	
	/**
	 * 指挥官穿戴装备
	 * @param equip
	 * @param slotId 装备位置id
	 * @return
	 */
	 
	private int putOnCommanderEquip(EquipEntity equip, int slotId) {
		EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, equip.getCfgId());
		if (equipCfg == null) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		int posType = equipCfg.getPos();
		int slotPosType = ConstProperty.getInstance().getEquipSlotPosType(slotId);
		if(posType != slotPosType){
			return Status.Error.EQUIP_POS_TYPE_ERROR_VALUE;
		}
		// 指挥官等级不足
		if (player.getLevel() < equipCfg.getLevel() || player.getLevel() < ConstProperty.getInstance().getCommanderEquipUnlockLvl(slotId)) {
			return Status.Error.PLAYER_LEVEL_NOT_ENOUGH_VALUE;
		}
		CommanderObject commander = player.getData().getCommanderObject();
		Optional<EquipSlot> opSlot = commander.getEquipSlot(slotId);
		EquipSlot equipSolt = null;
		List<EquipEntity> needPush = new ArrayList<>();
		if (!opSlot.isPresent()) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		equipSolt = opSlot.get();
		EquipEntity oldEquip = null;
		EquipmentCfg cfgBef = null;
		if(!HawkOSOperator.isEmptyString(equipSolt.getEquipId())){
			oldEquip = player.getData().getEquipEntity(equipSolt.getEquipId());
			oldEquip.setState(EquipState.FREE_VALUE);
			needPush.add(oldEquip);
			cfgBef = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, oldEquip.getCfgId());
		}
		needPush.add(equip);
		equipSolt.setEquipId(equip.getId());
		commander.notifyChange();
		equip.setState(EquipState.ON_COMMANDER_VALUE);
		equip.setNew(false);
		player.getPush().syncEquipInfo(needPush);
		player.getPush().syncCommanderEquipSoltInfo(equipSolt);
		syncCommanderEquipChange(equipCfg, cfgBef);
		BehaviorLogger.log4Service(player, Source.EQUIP, Action.EQUIP_PUT_ON,
				Params.valueOf("target", EquipState.ON_COMMANDER),
				Params.valueOf("pos", equipSolt.getPos()),
				Params.valueOf("equipId", equip.getId()),
				Params.valueOf("equipCfgId", equip.getCfgId()),
				Params.valueOf("equipIdBef", oldEquip == null ? null : equip.getId()),
				Params.valueOf("equipCfgIdBef", oldEquip == null ? null : equip.getCfgId()));
		
		MissionManager.getInstance().postMsg(player, new EventCommanderPutOnEquip(equipCfg.getLevel()));
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 卸下指挥官装备
	 * @param equip
	 * @return
	 */
	private int takeOffCommanderEquip(EquipEntity equip) {
		CommanderObject commander = player.getData().getCommanderObject();
		Optional<EquipSlot> solt = commander.getEquipSlot(equip.getId());
		if (!solt.isPresent()) {
			return Status.Error.EQUIP_NOT_EQUIPED_VALUE;
		}
		EquipSlot equipSolt = solt.get();
		EquipmentCfg cfgBef = equipSolt.getEquipCfg();
		equipSolt.takeOff();
		commander.notifyChange();
		equip.setState(EquipState.FREE_VALUE);
		player.getPush().syncEquipInfo(equip);
		player.getPush().syncCommanderEquipSoltInfo(equipSolt);
		syncCommanderEquipChange(null, cfgBef);
		BehaviorLogger.log4Service(player, Source.EQUIP, Action.EQUIP_TAKE_OFF,
				Params.valueOf("target", EquipState.ON_COMMANDER),
				Params.valueOf("pos", equipSolt.getPos()),
				Params.valueOf("equipId", equip.getId()),
				Params.valueOf("equipCfgId", equip.getCfgId()));
		//卸下装备的时候也需要触发一次任务.
		MissionManager.getInstance().postMsg(player, new EventCommanderPutDownEquip());
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	/**
	 * 同步指挥官装备属性变更(作用号,战斗力)
	 * @param cfgAft 变更后装备配置
	 * @param cfgBef 变更前装备配置
	 */
	 
	private void syncCommanderEquipChange(EquipmentCfg cfgAft, EquipmentCfg cfgBef){
		// 同步战力变化
		player.refreshPowerElectric(PowerChangeReason.OTHER);
		Set<Integer> set = new HashSet<>();
		if(cfgAft != null){
			set.addAll(cfgAft.getAttrMap().keySet());
		}
		if(cfgBef != null){
			set.addAll(cfgBef.getAttrMap().keySet());
		}
		if(set.isEmpty()){
			return;
		}
		EffType[] effTypes = new EffType[set.size()];
		int i =0;
		for(int type : set){
			effTypes[i] = EffType.valueOf(type);
			i++;
		}
		// 刷新装备作用号
		player.getEffect().resetEffectEquip(player, effTypes);
	}
	
	
	private EquipmentCfg getEquipCfgWithOutId(int mouldId, int quality) {
		Optional<EquipmentCfg> opCfg = HawkConfigManager.getInstance().getConfigIterator(EquipmentCfg.class).stream()
				.filter(e -> e.getMouldId() == mouldId && e.getQuality() == quality)
				.findAny();
		if (opCfg.isPresent()) {
			return opCfg.get();
		}
		return null;
	}
	
	/**
	 * 装备打造/升品/分解消耗
	 * @param opcode
	 * @param immediately 是否用水晶秒时间
	 * @param useGold 是否用金币替代材料
	 * @param aftCfg
	 * @param costTime
	 * @param action
	 * @return <boolean, List<ItemInfo>> <是否成功扣除,队列取消消耗返还列表>
	 */
	 
	private HawkTuple2<Boolean, List<ItemInfo>> equipOperationConsume(int opcode, boolean immediately, EquipmentCfg aftCfg, long costTime, Action action) {
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		// 道具消耗列表
		List<ItemInfo> materialList = new ArrayList<>();
		
		// 队列取消时消耗返还列表
		List<ItemInfo> consumeReturn = new ArrayList<>();
		switch (action) {
		case EQUIP_FORGE:
		case EQUIP_UPGRADE_QUALITY:
			materialList = aftCfg.getForgeMaterialList();
			break;
		case EQUIP_RESOLVE:
			break;
		default:
			break;
		}
		// 道具消耗,队列取消时全部返还
		consumeItems.addConsumeInfo(materialList, false);
		consumeReturn.addAll(materialList);
		
		if (immediately) {
			consumeItems.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold((long) Math.ceil(costTime/1000.0), SpeedUpTimeWeightType.EQUIP_OPERATION));
		}
		
		if (!consumeItems.checkConsume(player, opcode)) {
			return new HawkTuple2<Boolean, List<ItemInfo>>(false, consumeReturn);
		}
		
		consumeItems.consumeAndPush(player, action);
		return new HawkTuple2<Boolean, List<ItemInfo>>(true, consumeReturn);
	}
	
	/**
	 * 判定装备研究所是否已解锁
	 * @return
	 */
	private boolean isBuildingUnlock(){
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.EQUIP_RESEARCH_INSTITUTE);
		return buildingEntity != null;
	}
	
	/**
	 *  判定队列是否被占用
	 * @return
	 */
	private boolean isQueueBusy(){
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.EQUIP_RESEARCH_INSTITUTE);
		if(buildingEntity == null){
			return true;
		}
		
		QueueEntity buildingQueue = player.getData().getQueueEntityByItemId(buildingEntity.getId());
		// 装备研究所正在升级
		if (buildingQueue != null) {
			return true;
		}
		
		Optional<QueueEntity> optional = getPlayerData().getQueueEntities().stream()
                .filter(queue -> queue.getQueueType() == QueueType.EQUIP_QUEUE_VALUE)
                .findAny();
		return optional.isPresent();
	}
	
	/**
	 * 序列化装备队列itemId
	 * @param type
	 * @param cfgId
	 * @param entity
	 * @return
	 */
	private String serializQueueItemId(EquipQueueType type, int cfgId, EquipEntity entity) {
		StringBuilder sb = new StringBuilder();
		sb.append(type.getNumber()).append("|").append(cfgId).append("|").append(entity == null ? "" : entity.getId());
		return sb.toString();
	}
	
	/**
	 * 反序列化装备队列itemId
	 * @param itemId
	 * @return
	 */
	private HawkTuple3<EquipQueueType, Integer, String> deserializQueueItemId(String itemId) {
		String[] infoArr = itemId.split("\\|");
		EquipQueueType type = EquipQueueType.valueOf(Integer.parseInt(infoArr[0]));
		int cfgId = Integer.parseInt(infoArr[1]);
		String equipId = "";
		if (infoArr.length == 3) {
			equipId = infoArr[2];
		}
		return new HawkTuple3<EquipQueueType, Integer, String>(type, cfgId, equipId);
	}
	
	/**
	 * 装备队列取消
	 * @param msg
	 */
	@MessageHandler
	private void onEquipQueueCancel(EquipQueueCancelMsg msg){
		String itemId = msg.getItemId();
		String resReturnStr = msg.getResReturn();
		// 消耗返还
		AwardItems awardItems = AwardItems.valueOf(resReturnStr);
		HawkTuple3<EquipQueueType, Integer, String> tuple = deserializQueueItemId(itemId);
		EquipQueueType type = tuple.first;
		int cfgId = tuple.second;
		String equipId = tuple.third;
		EquipEntity equip = getPlayerData().getEquipEntity(equipId);
		switch (type) {
		case FORGE:
		case QUALITY_UP:
			onForgeOrQualityUpCancel(cfgId, equip, awardItems);
			break;
		case RESOLVE:
			onResolveCancel(cfgId, equip, awardItems);
			break;
		}
	}
	
	/**
	 * 装备打造/升品队列取消,资源返还
	 * @param cfgId
	 * @param equip
	 */
	private void onForgeOrQualityUpCancel(int cfgId, EquipEntity equip, AwardItems awardItems) {
		if(equip != null){
			equip.setState(EquipState.FREE_VALUE);
			equip.setNew(false);
			player.getPush().syncEquipInfo(equip);
		}
		awardItems.rewardTakeAffectAndPush(player, Action.EQUIP_QUEUE_CANCEL, false);
	}
	
	/**
	 * 装备分解队列取消
	 * @param cfgId
	 * @param equip
	 */
	private void onResolveCancel(int cfgId, EquipEntity equip, AwardItems awardItems) {
		equip.setState(EquipState.FREE_VALUE);
		equip.setNew(false);
		player.getPush().syncEquipInfo(equip);
		awardItems.rewardTakeAffectAndPush(player, Action.EQUIP_QUEUE_CANCEL, false);
	}
	
	/**
	 * 装备队列完成
	 * @param msg
	 */
	@MessageHandler
	private void onEquipQueueFinish(EquipQueueFinishMsg msg){
		String itemId = msg.getItemId();
		HawkTuple3<EquipQueueType, Integer, String> tuple = deserializQueueItemId(itemId);
		EquipQueueType type = tuple.first;
		int cfgId = tuple.second;
		String equipId = tuple.third;
		EquipEntity equip = getPlayerData().getEquipEntity(equipId);
		EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, cfgId);
		switch (type) {
		case FORGE:
			onForgeOrQualityUpFinish(cfgId, equip);
			MissionManager.getInstance().postMsg(player, new EventForgeEquip(cfgId, equipCfg.getLevel(), equipCfg.getQuality()));
			break;
		case QUALITY_UP:
			onForgeOrQualityUpFinish(cfgId, equip);
			MissionManager.getInstance().postMsg(player, new EventEquipQualityUp(equipCfg.getQuality() - 1, equipCfg.getQuality(), cfgId));
			break;
		case RESOLVE:
			onResolveFinish(cfgId, equip);
			break;
		}
	}
	
	/**
	 * 装备打造/升品完成
	 * @param cfgId
	 * @param equip
	 */
	private void onForgeOrQualityUpFinish(int cfgId, EquipEntity equip){
		EquipmentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, cfgId);
		if(equip != null){
			equip.setCfgId(cfgId);
			equip.setState(EquipState.FREE_VALUE);
			equip.setNew(true);
			player.getPush().syncAddEquipInfo(equip);
			LogUtil.logEquipmentAttrChange(player, equip.getId(), cfg.getId(), false, cfg.getPos(),
					cfg.getPower(), cfg.getQuality(), cfg.getLevel());
		}
		else{
			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItem(ItemType.EQUIP_VALUE, cfgId, 1);
			awardItem.rewardTakeAffectAndPush(player, Action.EQUIP_FORGE, false);
		}
		// 抛出活动事件
		ActivityManager.getInstance().postEvent(new EquipForgeEvent(player.getId(), cfgId, cfg.getLevel(), cfg.getQuality()));
		ActivityManager.getInstance().postEvent(new EquipChangeEvent(player.getId()));
	}
	
	/**
	 * 装备分解完成
	 * @param cfgId
	 * @param equip
	 */
	private void onResolveFinish(int cfgId, EquipEntity equip) {
		EquipmentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, cfgId);
		if (cfg == null) {
			return;
		}
		equip.setState(EquipState.AS_MATERIAL_VALUE);
		equip.setNew(true);
		LogUtil.logEquipmentAttrChange(player, equip.getId(), cfg.getId(), true, cfg.getPos(),
				cfg.getPower(), cfg.getQuality(), cfg.getLevel());
		player.getPush().syncEquipInfo(equip);
		player.getData().removeEquipEntity(equip);
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(cfg.getResolveMaterialList());
		awardItems.rewardTakeAffectAndPush(player, Action.EQUIP_RESOLVE, false);
		// 抛出活动事件
		ActivityManager.getInstance().postEvent(new EquipResolveEvent(player.getId(), cfgId, cfg.getLevel(), cfg.getQuality()));
	}
}
