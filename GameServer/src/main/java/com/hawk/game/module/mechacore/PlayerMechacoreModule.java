package com.hawk.game.module.mechacore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.mechacore.cfg.MechaCoreConstCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleCfg;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.MechaCore.MechaCoreModuleAttrReplaceReq;
import com.hawk.game.protocol.MechaCore.MechaCoreModuleLoadReq;
import com.hawk.game.protocol.MechaCore.MechaCoreModuleLockReq;
import com.hawk.game.protocol.MechaCore.MechaCoreModuleResolveReq;
import com.hawk.game.protocol.MechaCore.MechaCoreModuleResolveResp;
import com.hawk.game.protocol.MechaCore.MechaCoreModuleUnloadReq;
import com.hawk.game.protocol.MechaCore.MechaCoreModuleUnlockReq;
import com.hawk.game.protocol.MechaCore.MechaCoreSlotUnlockReq;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitResp;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.MechaCore.MechaCoreTechUpReq;
import com.hawk.game.protocol.MechaCore.SuitChangeNameReq;
import com.hawk.game.protocol.MechaCore.SuitSwitchReq;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 机甲核心
 * @author lating
 * @date 2025年2月12日
 */
public class PlayerMechacoreModule extends PlayerModule {
	/**
	 * 上一次tick
	 */
	private long lastCheckTime = 0;
	
	/**
	 * 功能是否已解锁
	 */
	private boolean funcUnlocked = false;
	private boolean moduleGacheUnlocked = false;
	private boolean moduleLoadUnlocked = false;
	
	private int errCode = 0;
	
	public PlayerMechacoreModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		if (funcUnlocked || player.isInDungeonMap()) {
			return true;
		}
		
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime - lastCheckTime < 2000) {
			return true;
		}
		lastCheckTime = currentTime;
		funcUnlocked = player.checkMechacoreFuncUnlock();
		if (funcUnlocked) {
			player.getPlayerMechaCore().syncMechaCoreInfo(true);
		}
		if (!moduleGacheUnlocked && player.getPlayerMechaCore().isModuleGachaUnlock()) {
			moduleGacheUnlocked = true;
			player.getPlayerMechaCore().syncMechaCoreInfo(true);
		}
		if (!moduleLoadUnlocked && player.getPlayerMechaCore().isModuleLoadUnlock()) {
			moduleLoadUnlocked = true;
			player.getPlayerMechaCore().syncMechaCoreInfo(true);
		}
		return true;
	}
	
	@Override
	protected boolean onPlayerLogin() {
		if (!funcUnlocked) {
			funcUnlocked = player.checkMechacoreFuncUnlock();
		}
		if (funcUnlocked) {
			player.getPlayerMechaCore().loginFix();
		}
		moduleGacheUnlocked = player.getPlayerMechaCore().isModuleGachaUnlock();
		moduleLoadUnlocked = player.getPlayerMechaCore().isModuleLoadUnlock();
		player.getPlayerMechaCore().syncMechaCoreInfo(funcUnlocked);
		player.getPlayerMechaCore().syncAllModuleInfo();
		lastCheckTime = HawkApp.getInstance().getCurrentTime();
		
		if (player.isCsPlayer() || !funcUnlocked) {
			return true;
		}
		WorldPoint point = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		if (point != null && HawkOSOperator.isEmptyString(point.getMechaCoreShow())) {
			String showInfo = player.getPlayerMechaCore().serializeUnlockedCityShow();
			if (!HawkOSOperator.isEmptyString(showInfo)) {
				point.setMechaCoreShow(showInfo);
				WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
				point.notifyUpdate();
			}
		}
		
		return true;
	}
	
	/**
	 * 检测功能是否已开启
	 * @return
	 */
	private boolean checkFuncOpen() {
		if (!MechaCoreConstCfg.getInstance().isMechaCoreOpen()) {
			errCode = Status.Error.MECHA_CORE_FUNC_CLOSE_VALUE;
		} else {
			errCode = funcUnlocked ? 0 : Status.Error.MECHA_CORE_FUNC_LOCK_VALUE;
		}
		return errCode == 0;
	}
	
	/**
	 * 机甲核心科技突破
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_TECH_BREAKTHROUGH_C_VALUE)
	private void onTechBreakThrough(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore breakthrough failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		int result = mechaCore.breakthrough(protocol.getType());
		if (result > 0) {
			sendError(protocol.getType(), result);
			return;
		}
		if (result == 0) {
			mechaCore.syncMechaCoreInfo(true);
			mechaCore.notifyChange(PowerChangeReason.MECHA_CORE_TECH);
			player.responseSuccess(protocol.getType());
		}
	}
	
	/**
	 * 机甲核心科技升级
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_TECH_UP_C_VALUE)
	private void onTechUp(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore tech levelup failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		MechaCoreTechUpReq req = protocol.parseProtocol(MechaCoreTechUpReq.getDefaultInstance());
		int result = mechaCore.techLevelUp(req.getType(), protocol.getType());
		if (result > 0) {
			sendError(protocol.getType(), result);
			return;
		}
		if (result == 0) {
			mechaCore.syncMechaCoreInfo(true);
			mechaCore.notifyChange(PowerChangeReason.MECHA_CORE_TECH);
			player.responseSuccess(protocol.getType());
		}
	}
	
	/**
	 * 槽位解锁（槽位解锁方式为自动解锁）
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_SLOT_UNLOCK_C_VALUE)
	private void onSlotUnlock(HawkProtocol protocol) {
	}
	
	/**
	 * 槽位升级
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_SLOT_UP_C_VALUE)
	private void onSlotUp(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore slot levelup failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		MechaCoreSlotUnlockReq req = protocol.parseProtocol(MechaCoreSlotUnlockReq.getDefaultInstance());
		int result = mechaCore.slotLevelup(req.getSlotType(), protocol.getType());
		if (result > 0) {
			sendError(protocol.getType(), result);
			return;
		}
		if (result == 0) {
			mechaCore.syncMechaCoreInfo(true);
			mechaCore.notifyChange(PowerChangeReason.MECHA_CORE_MODULE);
			player.responseSuccess(protocol.getType());
		}
	}
	
	/**
	 * 模块装载（模块替换也是走的装载流程）
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_MODULE_LOAD_C_VALUE)
	private void onModuleLoad(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore load module failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		if (!mechaCore.isModuleLoadUnlock()) {
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_CLOSE_VALUE);
			return;
		}
		
		MechaCoreModuleLoadReq req = protocol.parseProtocol(MechaCoreModuleLoadReq.getDefaultInstance());
		int result = mechaCore.loadModule(req.getSuit().getNumber(), req.getSlotType(), req.getModuleId());
		if (result != 0) {
			sendError(protocol.getType(), result);
			return;
		}
		mechaCore.syncMechaCoreInfo(true);
		mechaCore.notifyChange(PowerChangeReason.MECHA_CORE_MODULE);
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 模块卸载
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_MODULE_UNLOAD_C_VALUE)
	private void onModuleUnload(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore module unload failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		MechaCoreModuleUnloadReq req = protocol.parseProtocol(MechaCoreModuleUnloadReq.getDefaultInstance());
		int slotType = req.getSlotType();		
		int result = mechaCore.unloadModule(req.getSuit().getNumber(), slotType);
		if (result != 0) {
			sendError(protocol.getType(), result);
			return;
		}
		
		mechaCore.syncMechaCoreInfo(true);
		mechaCore.notifyChange(PowerChangeReason.MECHA_CORE_MODULE);
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 模块分解
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_MODULE_RESOLVE_C_VALUE)
	private void onModuleResolve(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore module resolve failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		MechaCoreModuleResolveReq req = protocol.parseProtocol(MechaCoreModuleResolveReq.getDefaultInstance());
		List<String> moduleIds = req.getModuleIdList();
		List<Integer> qualityList = req.getQualityList();
		if (moduleIds.isEmpty() && qualityList.isEmpty()) {
			HawkLog.errPrintln("mechaCore module resolve param error, playerId: {}", player.getId());
			return;
		}
		Set<String> moduleIdSet = new HashSet<>(moduleIds);
		Set<Integer> moduleQualitySet = new HashSet<>(qualityList);
		List<MechaCoreModuleEntity> removeList = new ArrayList<>();
		AwardItems awardItems = AwardItems.valueOf();
		List<MechaCoreModuleEntity> moduleList = player.getData().getMechaCoreModuleEntityList();
		int count = 0;
		for (MechaCoreModuleEntity module : moduleList) {
			if (!moduleIdSet.contains(module.getId()) && !moduleQualitySet.contains(module.getQuality())) {
				continue;
			}
			if (module.isLocked()) {
				HawkLog.logPrintln("mechaCore module resolve skip locked, playerId: {}, module cfgId: {}, uuid: {}", player.getId(), module.getCfgId(), module.getId());
				continue;
			}
			//已经装载到槽位上的模块不能分解
			if (module.isLoaded()) {
				HawkLog.logPrintln("mechaCore module resolve skip loaded, playerId: {}, module cfgId: {}, uuid: {}", player.getId(), module.getCfgId(), module.getId());
				continue;
			}
			
			count++;
			removeList.add(module);
			//发奖
			MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, module.getCfgId());
			if (moduleCfg != null) {
				awardItems.addItemInfos(ItemInfo.valueListOf(moduleCfg.getBreakDownGetItem()));
			}
		}
		
		if (!removeList.isEmpty()) {
			moduleList.removeAll(removeList);
			removeList.forEach(e -> e.delete(true));
			awardItems.rewardTakeAffectAndPush(player, Action.MECHA_CORE_MODULE_RESOLVE, true);
			MechaCoreModuleResolveResp.Builder builder = MechaCoreModuleResolveResp.newBuilder();
			removeList.forEach(e -> builder.addModuleId(e.getId()));
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.MECHACORE_MODULE_RESOLVE_S, builder));
		}
		
//		if (count > 0) {
//			mechaCore.syncAllModuleInfo();
//		}
		player.responseSuccess(protocol.getType());
		
		mechaCore.logMechcoreModuleFlow(player, 1, removeList);
		HawkLog.logPrintln("mechaCore module resolve success, playerId: {}, count: {} {}, moduleQuality: {}", player.getId(), moduleIdSet.size(), count, moduleQualitySet);
	}
	
	/**
	 * 模块属性词条更换
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_ATTR_REPLACE_C_VALUE)
	private void onModuleAttrReplace(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore moduleAttr replace failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		MechaCoreModuleAttrReplaceReq req = protocol.parseProtocol(MechaCoreModuleAttrReplaceReq.getDefaultInstance());
		String fromModuleId = req.getFromModuleId(), toModuleId = req.getToModuleId();
		int fromAttrId = req.getFromAttrId(), toAttrId = req.getToAttrId();
		int result = mechaCore.replaceModuleAttr(fromModuleId, toModuleId, fromAttrId, toAttrId, protocol.getType());
		if (result > 0) {
			sendError(protocol.getType(), result);
			return;
		}
		if (result == 0) {
			mechaCore.syncMechaCoreInfo(true);
			mechaCore.notifyChange(PowerChangeReason.MECHA_CORE_MODULE);
			player.responseSuccess(protocol.getType());
			HawkLog.logPrintln("mechaCore moduleAttr replace success, playerId: {}, module: {} {}, attrId: {} {}", player.getId(), fromModuleId, toModuleId, fromAttrId, toAttrId);
		}
	}
	
	/**
	 * 套装解锁
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_SUIT_UNLOCK_C_VALUE)
	private void suitUnlock(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore suitUnlock failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		int result = mechaCore.suitUnlock(protocol.getType());
		if (result > 0) {
			sendError(protocol.getType(), result);
			return;
		}
		if (result == 0) {
			mechaCore.syncMechaCoreInfo(true);
			mechaCore.notifyChange(null);
			player.responseSuccess(protocol.getType());
		}
	}
	
	/**
	 * 套装切换
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_SUIT_SWICH_C_VALUE)
	private void suitSwitch(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore suitSwitch failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		SuitSwitchReq req = protocol.parseProtocol(SuitSwitchReq.getDefaultInstance());
		int result = mechaCore.suitSwitch(req.getSuit().getNumber());
		if (result > 0) {
			sendError(protocol.getType(), result);
			return;
		}
		if (result == 0) {
			mechaCore.syncMechaCoreInfo(true);
			mechaCore.notifyChange(PowerChangeReason.MECHA_CORE_MODULE);
			player.responseSuccess(protocol.getType());
		}
	}
	
	/**
	 * 套装改名
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_SUIT_CHANGENAME_C_VALUE)
	private void changeSuitName(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore suitSwitch failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		SuitChangeNameReq req = protocol.parseProtocol(SuitChangeNameReq.getDefaultInstance());
		MechaCoreSuitType suitType = req.getSuit();
		if (mechaCore.getSuitObj(suitType.getNumber()) == null) {
			sendError(protocol.getType(), Status.Error.MECHA_CORE_SUIT_LOCK_ERR_VALUE);
			return;
		}
		
		String name = req.getName();
		if (name.indexOf(",") >= 0 || name.indexOf("_") >= 0 || !GameUtil.canArmourSuitNameUse(name)) {
			sendError(protocol.getType(), Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE);
			return;
		}
		
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", suitType.getNumber());
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", String.valueOf(suitType.getNumber()));
		GameTssService.getInstance().wordUicChatFilter(player, name, 
				MsgCategory.ARMOUR_GROUP_NAME.getNumber(), GameMsgCategory.MECHACORE_SUIT_CHANGE_NAME, 
				String.valueOf(suitType.getNumber()), json, protocol.getType());
	}
	

	/**
	 * 模块锁定
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_MODULE_LOCK_C_VALUE)
	private void onModuleLock(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore module lock failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		MechaCoreModuleLockReq req = protocol.parseProtocol(MechaCoreModuleLockReq.getDefaultInstance());
		int lockAll = req.getLockAll();
		List<String> moduleIds = req.getModuleIdList();
		if (lockAll <= 0 && moduleIds.isEmpty()) {
			HawkLog.errPrintln("mechaCore module lock param error, playerId: {}", player.getId());
			return;
		}
		
		List<MechaCoreModuleEntity> moduleList = player.getData().getMechaCoreModuleEntityList();
		List<MechaCoreModuleEntity> changeList = new ArrayList<>();
		int count = 0;
		for (MechaCoreModuleEntity module : moduleList) {
			if (lockAll <= 0 && !moduleIds.contains(module.getId())) {
				continue;
			}
			if (module.isLocked()) {
				if (lockAll <= 0) {
					HawkLog.logPrintln("mechaCore module lock skip, playerId: {}, module cfgId: {}, uuid: {}", player.getId(), module.getCfgId(), module.getId());
				}
				continue;
			}
			
			count++;
			module.setLocked(true);
			changeList.add(module);
		}
		
		if (count > 0) {
			mechaCore.syncModuleInfo(changeList);
			mechaCore.syncMechaCoreInfo(true);
		}
		player.responseSuccess(protocol.getType());
		HawkLog.logPrintln("mechaCore module lock success, playerId: {}, count: {} {}, lockAll: {}", player.getId(), moduleIds.size(), count, lockAll);
	}
	
	/**
	 * 模块解锁
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_MODULE_UNLOCK_C_VALUE)
	private void onModuleUnlock(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore module unlock failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		MechaCoreModuleUnlockReq req = protocol.parseProtocol(MechaCoreModuleUnlockReq.getDefaultInstance());
		int unlockAll = req.getUnlockAll();
		List<String> moduleIds = req.getModuleIdList();
		if (unlockAll <= 0 && moduleIds.isEmpty()) {
			HawkLog.errPrintln("mechaCore module unlock param error, playerId: {}", player.getId());
			return;
		}
		
		List<MechaCoreModuleEntity> moduleList = player.getData().getMechaCoreModuleEntityList();
		List<MechaCoreModuleEntity> changeList = new ArrayList<>();
		int count = 0;
		for (MechaCoreModuleEntity module : moduleList) {
			if (unlockAll <= 0 && !moduleIds.contains(module.getId())) {
				continue;
			}
			if (!module.isLocked()) {
				if (unlockAll <= 0) {
					HawkLog.logPrintln("mechaCore module unlock skip, playerId: {}, module cfgId: {}, uuid: {}", player.getId(), module.getCfgId(), module.getId());
				}
				continue;
			}
			
			count++;
			module.setLocked(false);
			changeList.add(module);
		}
		if (count > 0) {
			mechaCore.syncModuleInfo(changeList);
			mechaCore.syncMechaCoreInfo(true);
		}
		player.responseSuccess(protocol.getType());
		HawkLog.logPrintln("mechaCore module unlock success, playerId: {}, count: {} {}, unlockAll: {}", player.getId(), moduleIds.size(), count, unlockAll);
	}
	
	/**
	 * 请求套装详细信息
	 */
	@ProtocolHandler(code = HP.code2.MECHACORE_SUIT_INFO_C_VALUE)
	private void onSuitDetailReq(HawkProtocol protocol) {
		if (!checkFuncOpen()) {
			sendError(protocol.getType(), errCode);
			return;
		}
		PlayerMechaCore mechaCore = player.getPlayerMechaCore();
		if (mechaCore == null) {
			HawkLog.errPrintln("mechaCore suit detail failed, mechaCore not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MECHA_CORE_FUNC_LOCK_VALUE);
			return;
		}
		
		MechaCoreSuitResp.Builder builder = MechaCoreSuitResp.newBuilder();
		mechaCore.genSuitDetailBuilder(builder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MECHACORE_SUIT_INFO_S, builder));
	}
	
}
