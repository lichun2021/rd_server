package com.hawk.game.player.manhattan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PlantWeaponGetEvent;
import com.hawk.activity.type.impl.plantweapon.PlantWeaponActivity;
import com.hawk.game.config.ManhattanBaseLevelCfg;
import com.hawk.game.config.ManhattanBaseStageCfg;
import com.hawk.game.config.ManhattanSWCfg;
import com.hawk.game.config.ManhattanSWLevelCfg;
import com.hawk.game.config.ManhattanSWStageCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.ManhattanEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Manhattan.ManhattanBaseLevelUpReq;
import com.hawk.game.protocol.Manhattan.ManhattanInfoSync;
import com.hawk.game.protocol.Manhattan.ManhattanSWDeployReq;
import com.hawk.game.protocol.Manhattan.ManhattanSWLevelUpReq;
import com.hawk.game.protocol.Manhattan.ManhattanSWShowReq;
import com.hawk.game.protocol.Manhattan.ManhattanSWStageUpReq;
import com.hawk.game.protocol.Manhattan.ManhattanSWUnDeployReq;
import com.hawk.game.protocol.Manhattan.ManhattanSWUnlockReq;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.Manhattan.PBManhattanInfo;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.PresetMarchManhattan;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 超级武器
 * @author lating
 * @date 2024年8月26日
 */
public class PlayerManhattanModule extends PlayerModule {
	/**
	 * 上一次tick
	 */
	private long lastCheckTime = 0;
	
	/**
	 * 功能是否已解锁
	 */
	private boolean funcUnlocked = false;
	
	public PlayerManhattanModule(Player player) {
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
		checkFuncUnlock();
		if (funcUnlocked && player.getManhattanBase() == null) {
			PlayerManhattan manhattan = unLockManhattanBase();
			syncManhattanInfo();
			manhattan.notifyChange();
			synActivityInfo();
		}
		
		return true;
	}
	
	private void synActivityInfo() {
		try {
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.PLANT_WEAPON_355_VALUE);
			PlantWeaponActivity activity = (PlantWeaponActivity) opActivity.get();
			activity.pushActivityInfo(player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	protected boolean onPlayerLogin() {
		lastCheckTime = HawkApp.getInstance().getCurrentTime();
		checkFuncUnlock();
		if (funcUnlocked && player.getManhattanBase() == null) {
			unLockManhattanBase();
		}
		syncManhattanInfo();
		
		if (player.isCsPlayer()) {
			return true;
		}
		WorldPoint point = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		if (point != null && (point.getAtkSwSkillId() <= 0) || point.getDefSwSkillId() <= 0) {
			PBDeployedSwInfo.Builder deployedInfo = player.getDeployedSwInfo();
			if (deployedInfo.getAtkSwSkillId() > 0 || deployedInfo.getDefSwSkillId() > 0) {
				point.setAtkSwSkillId(deployedInfo.getAtkSwSkillId());
				point.setDefSwSkillId(deployedInfo.getDefSwSkillId());
				WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
				point.notifyUpdate();
			}
		}
		return true;
	}
	
	/**
	 * 检测功能是否已解锁
	 */
	private void checkFuncUnlock() {
		if (player.checkManhattanFuncUnlock()) {
			funcUnlocked = true;
			HawkLog.logPrintln("player manhattan func unlocked, playerId: {}", player.getId());
		}
	}
	
	/**
	 * 同步超武信息
	 */
	public void syncManhattanInfo() {
		ManhattanInfoSync.Builder builder = ManhattanInfoSync.newBuilder();
		builder.setFuncUnlocked(funcUnlocked ? 1 : 0);
		if (funcUnlocked) {
			PBManhattanInfo.Builder mbuilder = player.buildManhattanInfo();
			builder.setManhattanInfo(mbuilder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MANHATTAN_INFO_SYNC_S, builder));
	}

	/**
	 * 超武聚能底座升阶
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MANHATTAN_BASE_STAGE_UP_C_VALUE)
	private void onManhattanBaseStageUp(HawkProtocol protocol) {
		if (!funcUnlocked) {
			sendError(protocol.getType(), Status.Error.MANHATTAN_LOCK_STATE_VALUE);
			return;
		}
		
		//超武建筑
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.MANHATTAN);
		if (buildingEntity == null) {
			HawkLog.errPrintln("manhattan base stage up failed, manhattan building not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.BUILDING_FRONT_NOT_EXISIT_VALUE);
			return;
		}
				
		PlayerManhattan base = player.getManhattanBase();
		if (base == null) {
			HawkLog.errPrintln("manhattan base stage up failed, manhattan base not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MANHATTAN_BASE_NOT_EXIST_VALUE);
			return;
		}
		
		int oldStage = base.getStage();
		int newStage = oldStage + 1;
		ManhattanBaseStageCfg config = ManhattanBaseStageCfg.getConfigByStage(newStage);
		if (config == null) {
			HawkLog.errPrintln("manhattan base stage up failed, manhattan config not exist, playerId: {}, newStage: {}", player.getId(), newStage);
			sendError(protocol.getType(), Status.Error.MANHATTAN_BASE_MAX_STAGE_VALUE);
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(config.getConsumption()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			HawkLog.errPrintln("manhattan base stage up failed, consume break, playerId: {}, newStage: {}", player.getId(), newStage);
			return;
		}

		consumeItems.consumeAndPush(player, Action.MANHATTAN_BASE_STAGE_UP);
		base.stageUpgrade();
		syncManhattanInfo();
		base.notifyChange();
		player.responseSuccess(protocol.getType());
		HawkLog.logPrintln("manhattan base stage up success, playerId: {}, oldStage: {}, newStage: {}", player.getId(), oldStage, base.getStage());
		
		//tlog打点数据
		Map<String, Object> param = new HashMap<>();
        param.put("base", 1);   //1代表聚能底座升阶，0代表超武升阶
        param.put("swId", 0);   //为0时表示聚能底座，大于0表示具体的超武ID
        param.put("preStage", oldStage);          //进阶前的阶级
        param.put("afterStage", base.getStage()); //进阶后的阶级
        param.put("baseStage", base.getStage());  //聚能底座的阶级
        LogUtil.logActivityCommon(player, LogInfoType.manhattan_stage_up, param);
	}

	/**
	 * 超武聚能底座部件升级
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MANHATTAN_BASE_LEVEL_UP_C_VALUE)
	private void onManhattanBaseLevelUp(HawkProtocol protocol) {
		if (!funcUnlocked) {
			sendError(protocol.getType(), Status.Error.MANHATTAN_LOCK_STATE_VALUE);
			return;
		}
		//超武建筑
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.MANHATTAN);
		if (buildingEntity == null) {
			HawkLog.errPrintln("manhattan base level up failed, manhattan building not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.BUILDING_FRONT_NOT_EXISIT_VALUE);
			return;
		}
		
		PlayerManhattan base = player.getManhattanBase();
		if (base == null) {
			HawkLog.errPrintln("manhattan base level up failed, manhattan base not exist, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MANHATTAN_BASE_NOT_EXIST_VALUE);
			return;
		}
		
		ManhattanBaseLevelUpReq req = protocol.parseProtocol(ManhattanBaseLevelUpReq.getDefaultInstance());
		final int posId = req.getPosId();
		int oldLevel = base.getPosLevel(posId);
		int newLevel = oldLevel + 1;
		ManhattanBaseLevelCfg config = ManhattanBaseLevelCfg.getConfig(posId, newLevel);
		if (config == null) {
			HawkLog.errPrintln("manhattan base level up failed, manhattan config not exist, playerId: {}, posId: {}, newLevel: {}", player.getId(), posId, newLevel);
			sendError(protocol.getType(), Status.Error.MANHATTAN_BASE_MAX_LEVEL_VALUE);
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(config.getConsumption()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			HawkLog.errPrintln("manhattan base level up failed, consume break, playerId: {}, posId: {}, newLevel: {}", player.getId(), posId, newLevel);
			return;
		}
		
		consumeItems.consumeAndPush(player, Action.MANHATTAN_BASE_LEVEL_UP);
		base.levelUpgrade(posId);
		syncManhattanInfo();
		base.notifyChange();
		player.responseSuccess(protocol.getType());
		HawkLog.logPrintln("manhattan base level up success, playerId: {}, posId: {}, oldLevel: {}, newLevel: {}", player.getId(), posId, oldLevel, base.getPosLevel(posId));
		
		//tlog打点数据
		Map<String, Object> param = new HashMap<>();
        param.put("base", 1);       //1代表聚能底座的部件升级，0代表超武部件升级
        param.put("swId", 0);       //为0时表示聚能底座，大于0表示具体的超武ID
        param.put("posId", posId);  //部件ID
        param.put("preLevel", oldLevel); //升级前等级
        param.put("afterLevel", base.getPosLevel(posId)); //升级后等级
        param.put("stage", 0);      //超武的阶级
        param.put("baseStage", base.getStage());  //聚能底座的阶级
        LogUtil.logActivityCommon(player, LogInfoType.manhattan_level_up, param);
	}
	
	/**
	 * 超级武器解锁
	 */
	@ProtocolHandler(code = HP.code2.MANHATTAN_SW_UNLOCK_C_VALUE)
	private void onManhattanSWUnlock(HawkProtocol protocol) {
		if (!funcUnlocked) {
			sendError(protocol.getType(), Status.Error.MANHATTAN_LOCK_STATE_VALUE);
			return;
		}
		
		ManhattanSWUnlockReq req = protocol.parseProtocol(ManhattanSWUnlockReq.getDefaultInstance());
		final int swId = req.getCfgId();
		ManhattanSWCfg config = HawkConfigManager.getInstance().getConfigByKey(ManhattanSWCfg.class, swId);
		if (config == null || config.getShowTimeValue() > HawkTime.getMillisecond()) {
			HawkLog.errPrintln("manhattansw unlock failed, config not exist, playerId: {}, swId: {}", player.getId(), swId);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_NOT_EXIST_VALUE);
			return;
		}
		PlayerManhattan manhattan = player.getManhattanSWByCfgId(swId);
		if (manhattan != null) {
			HawkLog.errPrintln("manhattansw unlock failed, has unlocked history, playerId: {}, swId: {}", player.getId(), swId);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_UNLOCKED_VALUE);
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(config.getUnlockConsumption()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			HawkLog.errPrintln("manhattansw unlock failed, consume break, playerId: {}, swId: {}", player.getId(), swId);
			return;
		}
		consumeItems.consumeAndPush(player, Action.MANHATTAN_SW_UNLOCK);
		manhattan = this.unLockManhattanSW(swId);
		syncManhattanInfo();
		manhattan.notifyChange();
		player.responseSuccess(protocol.getType());
		
		ActivityManager.getInstance().postEvent(new PlantWeaponGetEvent(player.getId(), swId));

		HawkLog.logPrintln("manhattansw unlock success, playerId: {}, swId: {}", player.getId(), swId);
		//tlog打点数据
		Map<String, Object> param = new HashMap<>();
        param.put("swId", swId);
        LogUtil.logActivityCommon(player, LogInfoType.manhattan_create, param);
	}
	
	/**
	 * 解锁超武聚能底座
	 * @return
	 */
	private PlayerManhattan unLockManhattanBase() {
		return unLockManhattanSW(0);
	}
	
	/**
	 * 超级武器解锁
	 * @param swId 超武配置id（底座的swId为0）
	 * @return
	 */
	private PlayerManhattan unLockManhattanSW(int swId) {
		ManhattanEntity entity = new ManhattanEntity();
		entity.setId(HawkOSOperator.randomUUID());
		entity.setPlayerId(player.getId());
		entity.setSwId(swId);
		entity.setBase(swId > 0 ? 0 : 1);
		PlayerManhattan newManhattan = null;
		if (swId > 0) {
			newManhattan = ManhattanSW.create(entity);
		} else {
			newManhattan = ManhattanBase.create(entity);
		}
		entity.create(true);
		player.getData().getManhattanEntityList().add(entity);
		HawkLog.logPrintln("unlocked new manhattan sw, playerId: {}, swId: {}", player.getId(), swId);
		return newManhattan;
	}
	
	/**
	 * 超级武器升阶
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MANHATTAN_SW_STAGE_UP_C_VALUE)
	private void onManhattanSWStageUp(HawkProtocol protocol) {
		if (!funcUnlocked) {
			sendError(protocol.getType(), Status.Error.MANHATTAN_LOCK_STATE_VALUE);
			return;
		}
		ManhattanSWStageUpReq req = protocol.parseProtocol(ManhattanSWStageUpReq.getDefaultInstance());
		final int swId = req.getCfgId();
		PlayerManhattan manhattan = player.getManhattanSWByCfgId(swId);
		if (manhattan == null) {
			HawkLog.errPrintln("manhattansw stage up failed, manhattan null, playerId: {}, swId: {}", player.getId(), swId);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_LOCK_STATE_VALUE);
			return;
		}
		
		int oldStage = manhattan.getStage();
		int newStage = oldStage + 1;
		ManhattanSWStageCfg config = ManhattanSWStageCfg.getConfig(swId, newStage);
		if (config == null) {
			HawkLog.errPrintln("manhattansw stage up failed, manhattan config not exist, playerId: {}, swId: {}, newStage: {}", player.getId(), swId, newStage);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_MAX_STAGE_VALUE);
			return;
		}
		
		//前置条件判断
		PlayerManhattan base = player.getManhattanBase();
		if (base == null) {
			HawkLog.errPrintln("manhattansw stage up failed, manhattan base not exist, playerId: {}, swId: {}", player.getId(), swId);
			sendError(protocol.getType(), Status.Error.MANHATTAN_BASE_NOT_MATCH_VALUE);
			return;
		}
		if (base.getStage() < config.getUnlockBaseStage()) {
			HawkLog.errPrintln("manhattansw stage up failed, manhattan base not match, playerId: {}, swId: {}, newStage: {}, baseStage: {}", player.getId(), swId, newStage, base.getStage());
			sendError(protocol.getType(), Status.Error.MANHATTAN_BASE_NOT_MATCH_VALUE);
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(config.getConsumption()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			HawkLog.errPrintln("manhattansw stage up failed, consume break, playerId: {}, swId: {}, newStage: {}", player.getId(), swId, newStage);
			return;
		}

		consumeItems.consumeAndPush(player, Action.MANHATTAN_SW_STAGE_UP);
		manhattan.stageUpgrade();
		syncManhattanInfo();
		manhattan.notifyChange();
		player.responseSuccess(protocol.getType());
		HawkLog.logPrintln("manhattansw stage up success, playerId: {}, swId: {}, oldStage: {}, newStage: {}", player.getId(), swId, oldStage, manhattan.getStage());
		
		//tlog打点数据
		Map<String, Object> param = new HashMap<>();
        param.put("base", 0);      //1代表聚能底座升阶，0代表超武升阶
        param.put("swId", swId);   //为0时表示聚能底座，大于0表示具体的超武ID
        param.put("preStage", oldStage);               //进阶前的阶级
        param.put("afterStage", manhattan.getStage()); //进阶后的阶级
        param.put("baseStage", base.getStage());  //聚能底座的阶级
        LogUtil.logActivityCommon(player, LogInfoType.manhattan_stage_up, param);
	}

	/**
	 * 超级武器部件升级
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MANHATTAN_SW_LEVEL_UP_C_VALUE)
	private void onManhattanSWLevelUp(HawkProtocol protocol) {
		if (!funcUnlocked) {
			sendError(protocol.getType(), Status.Error.MANHATTAN_LOCK_STATE_VALUE);
			return;
		}
		ManhattanSWLevelUpReq req = protocol.parseProtocol(ManhattanSWLevelUpReq.getDefaultInstance());
		final int swId = req.getCfgId();
		final int posId = req.getPosId();
		PlayerManhattan manhattan = player.getManhattanSWByCfgId(swId);
		if (manhattan == null) {
			HawkLog.errPrintln("manhattansw level up failed, manhattan null, playerId: {}, swId: {}", player.getId(), swId);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_LOCK_STATE_VALUE);
			return;
		}
		
		int oldLevel = manhattan.getPosLevel(posId);
		int newLevel = oldLevel + 1;
		ManhattanSWLevelCfg config = ManhattanSWLevelCfg.getConfig(swId, posId, newLevel);
		if (config == null) {
			HawkLog.errPrintln("manhattansw level up failed, manhattan config not exist, playerId: {}, swId: {}, posId: {}, newLevel: {}", player.getId(), swId, posId, newLevel);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_MAX_LEVEL_VALUE);
			return;
		}
		
		//前置条件判断
		PlayerManhattan base = player.getManhattanBase();
		if (base == null) {
			HawkLog.errPrintln("manhattansw level up failed, manhattan base null, playerId: {}, swId: {}", player.getId(), swId);
			sendError(protocol.getType(), Status.Error.MANHATTAN_BASE_NOT_MATCH_VALUE);
			return;
		}
		if (base.getStage() < config.getUnlockBaseStage()) {
			HawkLog.errPrintln("manhattansw level up failed, manhattan base not match, playerId: {}, swId: {}, posId: {}, newLevel: {}, baseStage: {}", player.getId(), swId, posId, newLevel, base.getStage());
			sendError(protocol.getType(), Status.Error.MANHATTAN_BASE_NOT_MATCH_VALUE);
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(config.getConsumption()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			HawkLog.errPrintln("manhattansw level up failed, consume break, playerId: {}, swId: {}, posId: {}, newLevel: {}", player.getId(), swId, posId, newLevel);
			return;
		}
		
		consumeItems.consumeAndPush(player, Action.MANHATTAN_SW_LEVEL_UP);
		manhattan.levelUpgrade(posId);
		syncManhattanInfo();
		manhattan.notifyChange();
		player.responseSuccess(protocol.getType());
		HawkLog.logPrintln("manhattansw level up success, playerId: {}, swId: {}, posId: {}, oldLevel: {}, newLevel: {}", player.getId(), swId, posId, oldLevel, manhattan.getPosLevel(posId));
		
		//tlog打点数据
		Map<String, Object> param = new HashMap<>();
        param.put("base", 0);       //1代表聚能底的部件升级，0代表超武部件升级
        param.put("swId", swId);    //为0时表示聚能底座，大于0表示具体的超武ID
        param.put("posId", posId);  //部件ID
        param.put("preLevel", oldLevel);  //升级前等级
        param.put("afterLevel", manhattan.getPosLevel(posId)); //升级后等级
        param.put("stage", manhattan.getStage());      //超武的阶级
        param.put("baseStage", base.getStage());  //聚能底座的阶级
        LogUtil.logActivityCommon(player, LogInfoType.manhattan_level_up, param);
	}
	
	/**
	 * 超级武器部署
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MANHATTAN_SW_DEPLOY_C_VALUE)
	private void onManhattanDeployReq(HawkProtocol protocol) {
		if (!funcUnlocked) {
			sendError(protocol.getType(), Status.Error.MANHATTAN_LOCK_STATE_VALUE);
			return;
		}
		ManhattanSWDeployReq req = protocol.parseProtocol(ManhattanSWDeployReq.getDefaultInstance());
		int swId = req.getCfgId();
		deployChange(protocol, swId, true);
	}
	
	/**
	 * 超级武器撤下
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MANHATTAN_SW_UNDEPLOY_C_VALUE)
	private void onManhattanUnDeployReq(HawkProtocol protocol) {
		if (!funcUnlocked) {
			sendError(protocol.getType(), Status.Error.MANHATTAN_LOCK_STATE_VALUE);
			return;
		}
		ManhattanSWUnDeployReq req = protocol.parseProtocol(ManhattanSWUnDeployReq.getDefaultInstance());
		int swId = req.getCfgId();
		deployChange(protocol, swId, false);
	}
	
	/**
	 * 部署或撤下超武
	 * @param protocol
	 * @param swId
	 * @param deploy
	 */
	private void deployChange(HawkProtocol protocol, int swId, boolean deploy) {
		ManhattanSWCfg swCfg = HawkConfigManager.getInstance().getConfigByKey(ManhattanSWCfg.class, swId);
		if (swCfg == null) {
			HawkLog.errPrintln("manhattan deploy failed, swCfg config not exist, playerId: {}, swId: {}, deploy: {}", player.getId(), swId, deploy);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_NOT_EXIST_VALUE);
			return;
		}
		
		PlayerManhattan manhattan = player.getManhattanSWByCfgId(swId);
		if (manhattan == null) {
			HawkLog.errPrintln("manhattan deploy failed, manhattan null, playerId: {}, swId: {}, deploy: {}", player.getId(), swId, deploy);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_LOCK_STATE_VALUE);
			return;
		}
		
		int preSwId = 0, afterSwId = swId;
		//撤下来
		if (!deploy) {
			preSwId = swId;
			afterSwId = 0;
			manhattan.cancelDeploy(true);
		} else {
			int deployCount = 0;
			//部署上去
			for(PlayerManhattan tmp : player.getAllManhattanSW()) {
				if (!tmp.isDeployed()) {
					continue;
				}
				deployCount++;
				if (tmp.getType() == manhattan.getType()) {
					tmp.cancelDeploy(false);
					tmp.notifyChange();
					preSwId = tmp.getSWCfgId();
				}
			}
			manhattan.deploy();
			if (deployCount == 0) {
				manhattan.show();
			}
		}
		
		syncManhattanInfo();
		manhattan.notifyChange();
		player.responseSuccess(protocol.getType());
		HawkLog.logPrintln("manhattansw deploy change success, playerId: {}, swId: {}, preSwId: {}, deploy: {}", player.getId(), swId, preSwId, deploy);
		
		//tlog打点数据
		Map<String, Object> param = new HashMap<>();
        param.put("swType", swCfg.getType());  //超级武器类型（进攻或防御）
        param.put("preswId", preSwId);         //部署前的超级武器ID
        param.put("afterswId", afterSwId);     //部署后的超级武器ID
        param.put("stage", manhattan.getStage()); //部署后的超级武器品阶
        LogUtil.logActivityCommon(player, LogInfoType.manhattan_deploy, param);
	}
	
	/**
	 * 超级武器内城展示设置
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.MANHATTAN_SW_SHOW_C_VALUE)
	private void onManhattanShowChange(HawkProtocol protocol) {
		if (!funcUnlocked) {
			sendError(protocol.getType(), Status.Error.MANHATTAN_LOCK_STATE_VALUE);
			return;
		}
		
		ManhattanSWShowReq req = protocol.parseProtocol(ManhattanSWShowReq.getDefaultInstance());
		int swId = req.getCfgId();
		int show = req.getShow(); //1表示展示，0表示取消展示
		ManhattanSWCfg swCfg = HawkConfigManager.getInstance().getConfigByKey(ManhattanSWCfg.class, swId);
		if (swCfg == null) {
			HawkLog.errPrintln("manhattan show change failed, swCfg config not exist, playerId: {}, swId: {}, show: {}", player.getId(), swId, show);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_NOT_EXIST_VALUE);
			return;
		}
		
		PlayerManhattan manhattan = player.getManhattanSWByCfgId(swId);
		if (manhattan == null) {
			HawkLog.errPrintln("manhattan show change failed, manhattan null, playerId: {}, swId: {}, show: {}", player.getId(), swId, show);
			sendError(protocol.getType(), Status.Error.MANHATTAN_SW_LOCK_STATE_VALUE);
			return;
		}
		
		if (show > 0 && !manhattan.isDeployed()) {
			HawkLog.errPrintln("manhattan show change failed, manhattan not deployed, playerId: {}, swId: {}, show: {}", player.getId(), swId, show);
			sendError(protocol.getType(), Status.Error.MANHATTAN_NOT_DEPLOYED_VALUE); //未部署的超武不可展示
			return;
		}
		
		if (show > 0) {
			player.getAllManhattanSW().stream().filter(e -> e.isCityShow()).forEach(e -> e.cancelShow());
			manhattan.show();
		} else {
			manhattan.cancelShow();
		}
		
		syncManhattanInfo();
		manhattan.notifyChange();
		player.responseSuccess(protocol.getType());
		HawkLog.logPrintln("manhattansw show change success, playerId: {}, swId: {}, show: {}", player.getId(), swId, show);
	}
	public boolean checkMarchReq(PresetMarchManhattan marchManhattan){
		if (!player.checkManhattanFuncUnlock()) return false;
		Set<Integer> desiredIds = new HashSet<>();
		int defId = marchManhattan.getManhattanDefSwId();
		if (defId > 0) {
			desiredIds.add(defId);
		}
		int atkId = marchManhattan.getManhattanAtkSwId();
		if (atkId > 0) {
			desiredIds.add(atkId);
		}
		return player.manhattanSWContains(desiredIds);
    }
}
