package com.hawk.game.player.supersoldier;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import com.hawk.game.entity.*;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.SuperSoldierBuildTaskCfg;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.SuperSoldierOfficeCfg;
import com.hawk.game.config.SuperSoldierSkillLevelCfg;
import com.hawk.game.config.SuperSoldierSkinCfg;
import com.hawk.game.config.SuperSoldierStarLevelCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.PlayerHeroModule;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.supersoldier.energy.ISuperSoldierEnergy;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.PBGundamStartUp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperSoldier.HPEvolutionSuperSoldierReq;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierBatchChangeSkinReq;
import com.hawk.game.protocol.SuperSoldier.HPSuperSoldierChangeSkinReq;
import com.hawk.game.protocol.SuperSoldier.HPSuperSoldierUseSkinItemReq;
import com.hawk.game.protocol.SuperSoldier.HPSupersoldierUnlockAnyWhereReq;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierEnergyReq;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierItem;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierOffice;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierOfficeAppointRequest;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierStarUpRequest;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;
import com.hawk.game.protocol.SuperSoldier.PBSupersoldierTaskInfo;
import com.hawk.game.protocol.SuperSoldier.PBSupersoldierTaskPush;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierUnlockSkinReq;
import com.hawk.game.protocol.SuperSoldier.PBUnlockEnergyReq;
import com.hawk.game.protocol.SuperSoldier.PBUnlockSuperSoldierRequest;
import com.hawk.game.protocol.SuperSoldier.PBUseSuperSoldierExpItemRequest;
import com.hawk.game.protocol.SuperSoldier.PBUseSuperSoldierSkillExpItemRequest;
import com.hawk.game.protocol.SuperSoldier.SuperSoldierPartRepairReq;
import com.hawk.game.protocol.SuperSoldier.SuperSoliderStarUpExchange;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGainItem;
import com.hawk.game.service.mssion.event.EventMechaAdvance;
import com.hawk.game.service.mssion.event.EventMechaPartLvUp;
import com.hawk.game.service.mssion.event.EventMechaPartRepair;
import com.hawk.game.service.mssion.event.EventMechaUnlock;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 机甲模块
 * 
 * @author lwt
 * @date 2017年7月25日
 */
public class PlayerSuperSoldierModule extends PlayerModule {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	private long lastCheckTime = 0;
	private final int NOMORSKILLEXPITEMMAXID = 15210004;
	
	public PlayerSuperSoldierModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		if (!player.isInDungeonMap()) {
			long currentTime = HawkApp.getInstance().getCurrentTime();
			if (currentTime - lastCheckTime > 10_000) {
				superSoldierCheckAndFix();
				lastCheckTime = currentTime;
			}
		}
		return super.onTick();
	}

	@Override
	protected boolean onPlayerLogin() {
		newFunc2OldPlayer();
		fixSkin();
		player.getPush().pushSuperSoldier();
		// 优化后的机甲功能，在还没有解锁出机甲的情况下，需要推送机甲建筑任务数据
		List<SuperSoldier> soldiers = player.getAllSuperSoldier();
		if (soldiers.isEmpty()) {
			syncSueprsoldierTaskInfo();
		}
		player.getPush().pushSuperSoldierSkin();
		return super.onPlayerLogin();
	}

	private void fixSkin(){
		try {
			CommanderEntity entity = player.getData().getCommanderEntity();
			boolean needUpdate = false;
			for(SuperSoldier ssoldier : player.getAllSuperSoldier()){
				if(ssoldier == null || ssoldier.getUnlockSkinSet().isEmpty()){
					continue;
				}
				SuperSoldierCfg cfg = ssoldier.getConfig();
				ssoldier.getDBEntity().setAnyWhereUnlock(0);
				if(ssoldier.getUnlockSkinSet().contains(cfg.getUnlockAnyWhereGetSkin())){
					ssoldier.getUnlockSkinSet().remove(cfg.getUnlockAnyWhereGetSkin());
					ssoldier.getDBEntity().setAnyWhereUnlock(1);
				}
				if(ssoldier.getUnlockSkinSet().isEmpty()){
					continue;
				}
				needUpdate = true;
				for(int skin : ssoldier.getUnlockSkinSet()){
					entity.getSuperSoldierSkins().add(skin);
					ssoldier.getUnlockSkinSet().remove(skin);
					if(ssoldier.getSkin() == skin){
						ssoldier.changeSkin(0);
					}
				}
				ssoldier.getDBEntity().notifyUpdate();
			}
			if(needUpdate){
				entity.notifyUpdate();
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 优化后的机甲功能针对老号的特殊处理
	 */
	private void newFunc2OldPlayer() {
		// 新玩家跳过
		if (player.getCreateTime() >= ConstProperty.getInstance().getNewbieVersionTimeValue()) {
			return;
		}
		
		try {
			final String key = "supersoldierNewVersion";
			CustomDataEntity entity = player.getData().getCustomDataEntity(key);
			if (entity == null) {
				//这里不调player.getData().createCustomDataEntity接口，是为了防止db容错的一个缺陷（数据落地失败但又触发了容错机制，导致内存跟db不一致）
				entity = new CustomDataEntity();
				entity.setPlayerId(player.getId());
				entity.setType(key);
				entity.setArg("");
				entity.setId(HawkOSOperator.randomUUID());
				entity.create(true);
				player.getData().getCustomDataEntities().add(entity);
			}
			
			// 老玩家已经处理过了，跳过
			if (entity.getValue() > 0) {
				return;
			}
			
			entity.setValue(HawkTime.getSeconds());
			HawkLog.logPrintln("supersoldierNewVersion to old player, playerId: {}", player.getId());
			
			List<SuperSoldier> soldiers = player.getAllSuperSoldier();
			if (soldiers.isEmpty()) {
				HawkLog.logPrintln("supersoldierNewVersion to old player unlock supersoldier, playerId: {}", player.getId());
				this.unLockSuperSoldier(GameConstCfg.getInstance().getSuperSoldierId());
			}
			
			updateCustomData();
			
			// 关联的任务自动完成：任务类型 = 270（获取机甲图纸）
			for (int itemId : ConstProperty.getInstance().getRecordItemList()) {
				RedisProxy.getInstance().getRedisSession().hIncrBy("GainItemTotal:" + player.getId(), String.valueOf(itemId), 100);
				MissionManager.getInstance().postMsg(player, new EventGainItem(itemId, 100));
			}
			
			// 任务类型 = 273（解锁机甲部件）
			ConfigIterator<SuperSoldierBuildTaskCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierBuildTaskCfg.class);
			while (iterator.hasNext()) {
				SuperSoldierBuildTaskCfg taskCfg = iterator.next();
				RedisProxy.getInstance().updateSupersoldierTaskTerminal(player.getId(), taskCfg.getId(), -1);
				MissionManager.getInstance().postMsg(player, new EventMechaPartRepair(taskCfg.getId()));
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 同步机甲任务信息
	 */
	private void syncSueprsoldierTaskInfo() {
		try {
			PBSupersoldierTaskPush.Builder taskList = PBSupersoldierTaskPush.newBuilder();
			Map<Integer, Integer> map = RedisProxy.getInstance().getSupersoldierTaskInfo(player.getId());
			for (Entry<Integer, Integer> entry : map.entrySet()) {
				int partId = entry.getKey();
				int value = entry.getValue();
				SuperSoldierBuildTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierBuildTaskCfg.class, partId);
				PBSupersoldierTaskInfo.Builder builder = PBSupersoldierTaskInfo.newBuilder();
				builder.setUnitId(partId);
				builder.setTaskType(SupersoldierTaskType.valueOf(cfg.getTaskId()));
				builder.setNum(value);
				builder.setFinished(value < 0);
				taskList.addTaskInfo(builder);
			}
			
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_SOLDIER_TASK_PUSH, taskList));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 使用英雄皮肤道具
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_USE_SKIN_ITEM_VALUE)
	private void onUseSkinItem(HawkProtocol protocol) {
		HPSuperSoldierUseSkinItemReq req = protocol.parseProtocol(HPSuperSoldierUseSkinItemReq.getDefaultInstance());
		ItemInfo item = ItemInfo.valueOf(req.getSkinItem());
		final int itemId = item.getItemId();
		ItemCfg skinItemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		if (Objects.isNull(skinItemCfg) || skinItemCfg.getItemType() != ToolType.SUPER_SOLDIER_SKIN_VALUE) {
			return;
		}
		BuffCfg bufCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, skinItemCfg.getBuffId());
		StatusDataEntity entity = player.getData().getStatusById(bufCfg.getEffect());
		if (entity != null && entity.getEndTime() > HawkTime.getMillisecond()) {
			return;
		}

		SuperSoldierSkinCfg skinCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierSkinCfg.class, bufCfg.getEffect());
		if (Objects.isNull(skinCfg)) {
			return;
		}
		int ssoldierId = skinCfg.getSupersoldierId();
		SuperSoldier ssoldier = player.getSuperSoldierByCfgId(ssoldierId).orElse(null);
		if (ssoldier == null) {
			return;
		}
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addItemConsume(itemId, 1);
		if (!consum.checkConsume(player, protocol.getType())) {
			return;
		}
		consum.consumeAndPush(player, Action.SUPER_SOLDIER_SKIN_USE);

		GameUtil.addBuff(player, skinItemCfg, "");
		ssoldier.changeSkin(skinCfg.getSkinId());
		player.responseSuccess(protocol.getType());
	}
	
	
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_UNLOCK_ANYWHERE_VALUE)
	public void onUnlockAnyWhere(HawkProtocol protocol) {
		HPSupersoldierUnlockAnyWhereReq req = protocol.parseProtocol(HPSupersoldierUnlockAnyWhereReq.getDefaultInstance());
		int soldierId = req.getSuperSoldierId();
		SuperSoldier ssoldier = player.getSuperSoldierByCfgId(soldierId).orElse(null);
		if (ssoldier == null || ssoldier.isAnyWhereUnlock(false)) {// 已解锁
			return;
		}
		if (ssoldier.getConfig().getUnlockAnyWhereGetSkin() <= 0) { // 没得解锁
			return;
		}

		List<ItemInfo> itemList = ItemInfo.valueListOf(ssoldier.getConfig().getUnlockAnyWhereCost());
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(itemList);
		if (!consum.checkConsume(player, protocol.getType())) {
			return;
		}
		consum.consumeAndPush(player, Action.SUPER_SOLDIER_ANYWHERE);
		ssoldier.getDBEntity().setAnyWhereUnlock(1);

		ssoldier.changeSkin(ssoldier.getConfig().getUnlockAnyWhereGetSkin());
		player.responseSuccess(protocol.getType());
		
		// 机甲TLOG
		LogUtil.logMechaAttrChange(player, Action.SUPER_SOLDIER_ANYWHERE, ssoldier);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.SUPER_SOLDIER_ANYWHERE,
				Params.valueOf("superSoldierId", soldierId));
	}

	/**
	 * 更换皮肤
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_CHANGE_SKIN_VALUE)
	public void onChangeSkin(HawkProtocol protocol) {
		HPSuperSoldierChangeSkinReq req = protocol.parseProtocol(HPSuperSoldierChangeSkinReq.getDefaultInstance());
		int skinId = req.getSkinId();
		int soldierId = req.getSuperSoldierId();

		SuperSoldier ssoldier = player.getSuperSoldierByCfgId(soldierId).orElse(null);
		if (ssoldier == null) {
			return;
		}

		if (skinId == 0) { // 还原原始皮肤, 不需要验证
			ssoldier.changeSkin(skinId);
		} else if (ssoldier.isAnyWhereUnlock(false) && ssoldier.getConfig().getUnlockAnyWhereGetSkin() == skinId) {
			// 使用无处不在形象
			ssoldier.changeSkin(skinId);
		}

		player.responseSuccess(protocol.getType());
		// 机甲TLOG
		LogUtil.logMechaAttrChange(player, Action.SUPER_SOLDIER_CSKIN, ssoldier);
	}

	/**
	 * 升星（进阶）
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_STAR_UP_C_VALUE)
	private void onSuperSoldierStarUp(HawkProtocol protocol) {
		PBSuperSoldierStarUpRequest req = protocol.parseProtocol(PBSuperSoldierStarUpRequest.getDefaultInstance());
		final int soldierId = req.getSuperSoldierId();
		final int toStep = req.getToStep();

		Optional<SuperSoldier> soldierOp = player.getSuperSoldierByCfgId(soldierId);
		if (!soldierOp.isPresent()) {
			return;
		}
		
		// 兑换
		for (SuperSoliderStarUpExchange exchange : req.getExchangesList()) {
			int toItemId = exchange.getToItemId();
			int toItemCount = exchange.getToItemCount();
			HawkAssert.checkNonNegative(toItemCount);
			PlayerHeroModule heroModule = player.getModule(ModuleType.HERO);
			heroModule.exchangeItem(protocol.getType(), req.getItemId(), toItemCount, toItemId, 0);
		}
		
		SuperSoldier superSoldier = soldierOp.get();
		int toStar = superSoldier.getStar() + 1;

		SuperSoldierStarLevelCfg toStarLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, soldierId, toStar, toStep);
		if (Objects.isNull(toStarLevelCfg)) {
			return;
		}
		SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, soldierId, superSoldier.getStar(),
				superSoldier.getStep());
		if (toStarLevelCfg.getId() <= starLevelCfg.getId()) {
			return;
		}

//		SuperSoldierCfg superSoldierCfg = superSoldier.getConfig();
//		SuperSoldierColorQualityCfg colorQualityCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierColorQualityCfg.class, superSoldierCfg.getQualityColor());
//		if (superSoldier.getStar() > colorQualityCfg.getMaxStarLevel()) {// 最高星级
//			return;
//		}
		for (SuperSoldierSkillSlot slot : superSoldier.getSkillSlots()) {
			if (!slot.getSkill().isMaxLevel()) {
				return;
			}
		}

		ConsumeItems consumeItems = ConsumeItems.valueOf();
		ConfigIterator<SuperSoldierStarLevelCfg> slit = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierStarLevelCfg.class);
		slit.stream()
				.filter(st -> st.getSupersoldierId() == soldierId)
				.filter(st -> st.getId() >= starLevelCfg.getId())
				.filter(st -> st.getId() < toStarLevelCfg.getId())
				.forEach(st -> {
					consumeItems.addConsumeInfo(ItemInfo.valueListOf(st.getPiecesForNextLevel()));
				});

		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}

		superSoldier.starUp(toStar, toStep);
		consumeItems.consumeAndPush(player, Action.SUPER_SOLDIER_STAR_UP);

		player.responseSuccess(protocol.getType());
		
		MissionManager.getInstance().postMsg(player, new EventMechaAdvance(soldierId, toStep));

		GuildMailService.getInstance().sendMail(MailParames.newBuilder()
                .setPlayerId(player.getId())
                .setMailId(MailId.GUNDAM_START_UP)
                .addContents(PBGundamStartUp.newBuilder().setSsoldier(superSoldier.toPBobj()))
                .addSubTitles(soldierId,superSoldier.getStar())
                .addTips(soldierId,superSoldier.getStar())
                .build());
		
		// 机甲 TLOG
		LogUtil.logMechaAttrChange(player, Action.SUPER_SOLDIER_STAR_UP, superSoldier);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.SUPER_SOLDIER_STAR_UP,
				Params.valueOf("star", superSoldier.getStar()));
	}

	/**
	 * 使用经验道具
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_ADD_EXP_C_VALUE)
	private void onUseSuperSoldierExpItem(HawkProtocol protocol) {
		PBUseSuperSoldierExpItemRequest req = protocol.parseProtocol(PBUseSuperSoldierExpItemRequest.getDefaultInstance());
		final int soldierId = req.getSuperSoldierId();
		Optional<SuperSoldier> soldierOp = player.getSuperSoldierByCfgId(soldierId);
		if (!soldierOp.isPresent()) {
			return;
		}
		SuperSoldier superSoldier = soldierOp.get();

		int totalExpAdd = 0;
		ConsumeItems consume = ConsumeItems.valueOf();
		for (PBSuperSoldierItem item : req.getItemUseList()) {
			ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
			if (Objects.isNull(cfg) || cfg.getItemType() != Const.ToolType.SUPER_SOLDIER_EXP_VALUE) { // 只能使用英雄加经验道具.业务失败
				return;
			}
			consume.addItemConsume(item.getItemId(), item.getCount());
			totalExpAdd = totalExpAdd + cfg.getHeroExp() * item.getCount();
		}
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.USE_SUPER_SOLDIER_EXP);
		superSoldier.addExp(totalExpAdd);

		player.responseSuccess(protocol.getType());

		LogUtil.logMechaAttrChange(player, Action.USE_SUPER_SOLDIER_EXP, superSoldier);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.USE_SUPER_SOLDIER_EXP,
				Params.valueOf("items", req.getItemUseList()));
	}

	/**
	 * 使用技能经验道具(改装升级)
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_SKILL_ADD_EXP_C_VALUE)
	private void onUseSkillExpItem(HawkProtocol protocol) {
		PBUseSuperSoldierSkillExpItemRequest req = protocol.parseProtocol(PBUseSuperSoldierSkillExpItemRequest.getDefaultInstance());
		int soldierId = req.getSuperSoldierId();
		int index = req.getIndex();
		int type = req.getType(); // 1 自带技能, 2 学习技能
		Optional<SuperSoldier> soldierOp = player.getSuperSoldierByCfgId(soldierId);
		if (!soldierOp.isPresent()) {
			return;
		}

		SuperSoldier ssoldier = soldierOp.get();
		ImmutableList<SuperSoldierSkillSlot> skillSlots = type == 1 ? ssoldier.getPassiveSkillSlots() : ssoldier.getSkillSlots();
		Optional<SuperSoldierSkillSlot> skillSlot = skillSlots.stream().filter(slot -> slot.getIndex() == index).findAny();
		if (!skillSlot.isPresent() || skillSlot.get().getSkill() == null|| skillSlot.get().getSkill().isMaxLevel()) {
			return;
		}

		int totalExpAdd = 0;
		int preSupersoldierId = ssoldier.getConfig().getPreSupersoldierId();
		ConsumeItems consume = ConsumeItems.valueOf();
		for (PBSuperSoldierItem item : req.getItemUseList()) {
			ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
			if (Objects.isNull(cfg)
					|| cfg.getItemType() != Const.ToolType.SUPER_SOLDIER_SKILL_EXP_VALUE
					|| cfg.getPartType() != index) { // 只能使用英雄技能加经验道具.业务失败
				return;
			}
			if (cfg.getId() > NOMORSKILLEXPITEMMAXID  && preSupersoldierId == 0) {
				return;
			}
			// 高阶机甲用15210004以上的材料
			if (cfg.getId() <= NOMORSKILLEXPITEMMAXID && preSupersoldierId > 0) {
				return;
			}
			consume.addItemConsume(item.getItemId(), item.getCount());
			totalExpAdd = totalExpAdd + cfg.getSkillExp() * item.getCount();
		}

		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}

		consume.consumeAndPush(player, Action.USE_SUPER_SOLDIER_SKILL_EXP);
		skillSlot.get().getSkill().addExp(totalExpAdd);
		ssoldier.notifyChange();
		player.responseSuccess(protocol.getType());

		MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(soldierId));
		// 机甲TLOG
		LogUtil.logMechaAttrChange(player, Action.USE_SUPER_SOLDIER_SKILL_EXP, ssoldier);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.USE_SUPER_SOLDIER_SKILL_EXP,
				Params.valueOf("soldierId", soldierId),
				Params.valueOf("slotIndex", index),
				Params.valueOf("items", req.getItemUseList().toString()));
	}

	/**
	 * 委任官员
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_OFFICE_APPOINT_C_VALUE)
	private void onSuperSoldierOfficeAppoint(HawkProtocol protocol) {
		PBSuperSoldierOfficeAppointRequest req = protocol.parseProtocol(PBSuperSoldierOfficeAppointRequest.getDefaultInstance());

		for (PBSuperSoldierOffice officePB : req.getOfficeList()) {
			int officeId = officePB.getOffice();
			int ssoldierId = officePB.getSuperSoldierId();
			SuperSoldierOfficeCfg officeCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierOfficeCfg.class, officeId);
			if (Objects.nonNull(officeCfg) && !officeCfg.checkOfficeBuilding(player.getData().getBuildingEntities())) { // 官没解锁
				continue;
			}

			Optional<SuperSoldier> soldierOp = player.getSuperSoldierByCfgId(ssoldierId);
			if (!soldierOp.isPresent()) {
				continue;
			}
			SuperSoldier ssoldier = soldierOp.get();
			if (ssoldier.getConfig().getSupersoldierClass() == 1) {
				continue;
			}

			if (ssoldier.getOffice() > 0) { // 当前英雄是否空闲
				SuperSoldierOfficeCfg soldierOfficeCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierOfficeCfg.class, ssoldier.getOffice());
				if (Objects.nonNull(soldierOfficeCfg) && hasBusyQueue(soldierOfficeCfg)) {
					continue;
				}
			}
			SuperSoldier oldOfficeSoldier = officeId > 0 ? getSuperSoldierByOfficeId(officeId) : null;
			if (Objects.equals(ssoldier, oldOfficeSoldier)) {
				continue;
			}

			if (Objects.nonNull(oldOfficeSoldier) && Objects.nonNull(officeCfg)) {
				if (hasBusyQueue(officeCfg)) {
					continue;
				}
				oldOfficeSoldier.officeAppoint(0);
			}

			ssoldier.officeAppoint(officeId); // 只能委任非守城

			// 机甲TLOG
			LogUtil.logMechaAttrChange(player, Action.USE_SUPER_OFFICE_APPOINT, ssoldier);
			// 行为日志
			BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.USE_SUPER_OFFICE_APPOINT,
					Params.valueOf("soldierId", ssoldier.getCfgId()),
					Params.valueOf("officeId", officeId));

		}

		player.responseSuccess(protocol.getType());
	}

	private boolean hasBusyQueue(SuperSoldierOfficeCfg officeCfg) {
		QueueEntity queue = player.getData().getQueueByBuildingType(officeCfg.getUnlockBuildingType());
		if (Objects.nonNull(queue)
				&& queue.getEnableEndTime() == 0
				&& officeCfg.getQueueUsedList().contains(queue.getQueueType())
				&& queue.getReusage() != QueueReusage.FREE.intValue()) {
			return true;
		}
		return false;
	}

	public SuperSoldier getSuperSoldierByOfficeId(int officeId) {
		for (SuperSoldier ssoldier : player.getAllSuperSoldier()) {
			if (ssoldier.getOffice() == officeId) {
				return ssoldier;
			}
		}
		return null;
	}

	/**
	 * 超级兵驻防
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_CITY_DEF_C_VALUE)
	private void onSuperSoldierCityDef(HawkProtocol protocol) {
		PBSuperSoldierOfficeAppointRequest req = protocol.parseProtocol(PBSuperSoldierOfficeAppointRequest.getDefaultInstance());
		List<SuperSoldier> sodliers = player.getAllSuperSoldier();
		HashBiMap<Integer, SuperSoldier> soldierOffice = HashBiMap.create(); // 原官职-英雄
		for (SuperSoldier ssoldier : sodliers) {
			if (ssoldier.getCityDefense() > 0) {
				soldierOffice.put(ssoldier.getCityDefense(), ssoldier);
			}
		}

		for (PBSuperSoldierOffice officePB : req.getOfficeList()) {
			SuperSoldier oldOfficeSoldier = soldierOffice.get(officePB.getOffice());
			if (oldOfficeSoldier != null && oldOfficeSoldier.getCfgId() != officePB.getSuperSoldierId() && oldOfficeSoldier.getCityDefense() == officePB.getOffice()) { // 老官卸任
				oldOfficeSoldier.cityDef(0);
			}
			this.cityDef(officePB);
		}

		player.responseSuccess(protocol.getType());

	}

	/**
	 * 英雄驻防
	 * 
	 * @param office
	 * @return
	 */
	private boolean cityDef(PBSuperSoldierOffice office) {
		Optional<SuperSoldier> ssoldierOp = player.getSuperSoldierByCfgId(office.getSuperSoldierId());
		if (!ssoldierOp.isPresent()) {
			return false;
		}
		SuperSoldier ssoldier = ssoldierOp.get();
		if (ssoldier.getConfig().getSupersoldierClass() == 2) {
			return false;
		}
		ssoldier.cityDef(office.getOffice()); // 只能委任守城

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.SUPER_SOLDIER_OFFICE_APPOINT,
				Params.valueOf("soldier", ssoldier.getCfgId()),
				Params.valueOf("officeId", office.getOffice()));

		return true;
	}

	/**
	 * 出征超级兵异常检测及修复
	 */
	private void superSoldierCheckAndFix() {
		// 出征中的英雄列表
		List<Integer> marchSoldiers = new ArrayList<>();

		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch march : marchs) {
			marchSoldiers.add(march.getSuperSoldierId());
		}

		for (SuperSoldier ssoldier : player.getAllSuperSoldier()) {
			// 超级兵状态为出征中 && 出征超级兵列表没有此
			int soldierId = ssoldier.getCfgId();
			if(ssoldier.isAnyWhereUnlock(true)){
				continue;
			}
			if (ssoldier.getState() == PBSuperSoldierState.SUPER_SOLDIER_STATE_MARCH && !marchSoldiers.contains(soldierId)) {
				ssoldier.backFromMarch(null);
			}
			if (ssoldier.getState() == PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE && marchSoldiers.contains(soldierId)) {
				ssoldier.goMarch(null);
			}
		}
	}

	/**
	 * 解锁
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_UNLOCK_C_VALUE)
	private void onUnlockSuperSoldier(HawkProtocol protocol) {
		PBUnlockSuperSoldierRequest req = protocol.parseProtocol(PBUnlockSuperSoldierRequest.getDefaultInstance());
		final int ssId = req.getSuperSoldierId();
		if (player.getSuperSoldierByCfgId(ssId).isPresent()) {// 已解锁
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("unlock supersoldier failed, already unlocked, playerId: {}, soliderId: {}", player.getId(), ssId);
			return;
		}
		
		SuperSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierCfg.class, ssId);
		if (cfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("unlock supersoldier failed, config error, playerId: {}, soliderId: {}", player.getId(), ssId);
			return;
		}
		
		boolean empty = player.getAllSuperSoldier().isEmpty();
		// 第一次解锁机甲，需要判断“收集图纸”的任务是否完成
		if (empty) {
			Map<Integer, Integer> map = RedisProxy.getInstance().getSupersoldierTaskInfo(player.getId());
			ConfigIterator<SuperSoldierBuildTaskCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierBuildTaskCfg.class);
			while (iterator.hasNext()) {
				SuperSoldierBuildTaskCfg taskCfg = iterator.next();
				int num = map.getOrDefault(taskCfg.getId(), 0);
				if (num >= 0) {
					sendError(protocol.getType(), Status.Error.MECHA_PART_NEED_REPAIR_VALUE);
					HawkLog.errPrintln("unlock supersoldier failed, task unfinish, playerId: {}, soliderId: {}, taskId: {}, num: {}", player.getId(), ssId, taskCfg.getId(), num);
					return;
				}
			}
		}

		if (player.getCityLevel() < cfg.getUnlockLevel()) {
			ItemInfo costItem = new ItemInfo(cfg.getUnlockCost());
			ConsumeItems consumeItems = ConsumeItems.valueOf();
			consumeItems.addConsumeInfo(costItem, false);
			if (!consumeItems.checkConsume(player, protocol.getType())) {
				return;
			}
			consumeItems.consumeAndPush(player, Action.SUPER_SOLDIER_UNLOCK);
		}
		SuperSoldier ssoldier = this.unLockSuperSoldier(ssId);
		ssoldier.notifyChange();
		player.responseSuccess(protocol.getType());
		
		MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(ssId));
		
		if (empty) {
			RedisProxy.getInstance().deleteSupersoldierTaskInfo(player.getId());
		}

		// 首次解锁机甲发放道具
		CustomDataEntity unlockRewardState = player.getData().getCustomDataEntity(GsConst.SUPER_SOLDIER_UNLOCK_REWARD_KEY);
		if (unlockRewardState == null || HawkOSOperator.isEmptyString(unlockRewardState.getArg())) {
			if (unlockRewardState == null) {
				unlockRewardState = player.getData().createCustomDataEntity(GsConst.SUPER_SOLDIER_UNLOCK_REWARD_KEY, 0, "1");
			} else {
				unlockRewardState.setArg("1");
			}

			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItemInfos(ConstProperty.getInstance().getGrantItemForUnlockMechaList());
			awardItems.rewardTakeAffectAndPush(player, Action.SUPER_SOLDIER_UNLOCK_AWARD);
		}

		// 机甲TLOG
		LogUtil.logMechaAttrChange(player, Action.SUPER_SOLDIER_UNLOCK, ssoldier);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.SUPER_SOLDIER_UNLOCK,
				Params.valueOf("superSoldierId", ssId));
	}
	
	/**
	 * 进化
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_EVOLUTION_VALUE)
	private void onEvolutionSuperSoldier(HawkProtocol protocol) {
		HPEvolutionSuperSoldierReq req = protocol.parseProtocol(HPEvolutionSuperSoldierReq.getDefaultInstance());
		final int evolutionId =req.getEvolutionSoldierId();
		if (player.getSuperSoldierByCfgId(evolutionId).isPresent()) {// 已解锁
			return;
		}
		SuperSoldierCfg evolutionIdCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierCfg.class, evolutionId);
		if (evolutionIdCfg == null ) {
			return;
		}
		SuperSoldier needSoldier = player.getSuperSoldierByCfgId(evolutionIdCfg.getPreSupersoldierId()).orElse(null);
		if(needSoldier == null){
			return;
		}
		for(SuperSoldierSkillSlot skillSlot : needSoldier.getSkillSlots()){
			if(!skillSlot.getSkill().isMaxLevel()){
				return;
			}
		}
		if (needSoldier.getStar() < 5) {// 最高星级
			return;
		}
		
		////////// 解锁
		ItemInfo costItem = new ItemInfo(evolutionIdCfg.getUnlockCost());
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(costItem, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.SUPER_SOLDIER_UNLOCK);
		SuperSoldier ssoldier = this.unLockSuperSoldier(evolutionIdCfg.getSupersoldierId());
		ConfigIterator<SuperSoldierSkillLevelCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierSkillLevelCfg.class);
		for (SuperSoldierSkillSlot slot : ssoldier.getSkillSlots()) {
			if (slot.getSkill().getCfg().getIniLevel() > 1) {
				int exp = configIterator.stream().filter(cfg -> cfg.getSkillQuality() == slot.getSkill().getCfg().getSkillQuality())
						.filter(cfg -> cfg.getSkillLevel() == slot.getSkill().getCfg().getIniLevel()-1).findFirst().get().getSkillExp();
				slot.getSkill().setExp(exp);
			}
		}
		ssoldier.notifyChange();
		player.responseSuccess(protocol.getType());
	}
	

	public SuperSoldier unLockSuperSoldier(int soldierId) {
		SuperSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierCfg.class, soldierId);
		if (cfg == null) {
			return null;
		}

		SuperSoldierEntity newSso = new SuperSoldierEntity();
		newSso.setId(HawkUUIDGenerator.genUUID());
		newSso.setSoldierId(soldierId);
		newSso.setPlayerId(player.getId());
		newSso.setStar(cfg.getIniStar());
		newSso.setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE_VALUE);
		SuperSoldier hero = SuperSoldier.create(newSso);
		newSso.create(true);

		player.getData().getSuperSoldierEntityList().add(newSso);
		MissionManager.getInstance().postMsg(player, new EventMechaUnlock(soldierId));

		return hero;

	}
	
	/**
	 * 触发任务
	 * @param msg
	 */
	@MessageHandler
	private void onTriggeTask(SuperSoldierTriggeTaskMsg msg) {
		if (msg.getTaskType() == null || msg.getNum() <= 0) {
			return;
		}

		if (!player.getAllSuperSoldier().isEmpty()) {
			return;
		}
		
		Set<Integer> partIdSet = SuperSoldierBuildTaskCfg.getUnitByTaskId(msg.getTaskType().getNumber());
		Map<Integer, Integer> map = RedisProxy.getInstance().getSupersoldierTaskInfo(player.getId());
		int oldNum = -1;
		for (int partId : partIdSet) {
			int value = map.getOrDefault(partId, 0);
			// -1 表示已修复
			if (value != -1) {
				oldNum = value;
				RedisProxy.getInstance().updateSupersoldierTask(player.getId(), partId, msg.getNum());
			}
		}
		
		// -1表示已经修复了，没有待修复的数据不再改变
		if (oldNum != -1) {
			syncSueprsoldierTaskInfo();
		}
	}
	
	/**
	 * 修复机甲部件
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_PART_REPAIR_C_VALUE)
	private void onSupersoldierPartRepair(HawkProtocol protocol) {
		SuperSoldierPartRepairReq req = protocol.parseProtocol(SuperSoldierPartRepairReq.getDefaultInstance());
		int partId = req.getUnitId();
		SuperSoldierBuildTaskCfg taskCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierBuildTaskCfg.class, partId);
		if (taskCfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR_VALUE);
			HawkLog.errPrintln("repair supersoldier part failed, config error, playerId: {}, partId: {}", player.getId(), partId);
			return;
		}
		
		int chapterId = player.getData().getStoryMissionEntity().getChapterId();
		if (chapterId < taskCfg.getUnlockDramaLevel()) {
			sendError(protocol.getType(), Status.Error.MACHA_REPAIR_CHAPTER_UNREACH_VALUE);
			HawkLog.errPrintln("repair supersoldier part failed, playerId: {}, partId: {}, dramaLevel: {}", player.getId(), partId, chapterId);
			return;
		}
		
		Map<Integer, Integer> map = RedisProxy.getInstance().getSupersoldierTaskInfo(player.getId());
		int num = map.getOrDefault(taskCfg.getId(), 0);
		// -1表示已经修复了
		if (num < 0) {
			sendError(protocol.getType(), Status.Error.MACHA_PART_REPAIRED_VALUE);
			HawkLog.errPrintln("repair supersoldier part failed, playerId: {}, partId: {}, num: {}", player.getId(), partId, num);
			return;
		}
		
		if (num < Integer.parseInt(taskCfg.getTaskParam())) {
			sendError(protocol.getType(), Status.Error.MECHA_REPAIR_COND_UNREACH_VALUE);
			HawkLog.errPrintln("repair supersoldier part failed, playerId: {}, partId: {}, num: {}", player.getId(), partId, num);
			return;
		}
		
		MissionManager.getInstance().postMsg(player, new EventMechaPartRepair(taskCfg.getId()));
		
		// 用-1表示已修复
		RedisProxy.getInstance().updateSupersoldierTaskTerminal(player.getId(), taskCfg.getId(), -1);
		syncSueprsoldierTaskInfo();
		player.responseSuccess(protocol.getType());
	}

	/**
	 * 修复机甲前消耗图纸道具
	 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_INIT_CONSUME_C_VALUE)
	private void onSupersoldierInitConsume(HawkProtocol protocol) {
		if (!player.getAllSuperSoldier().isEmpty()) {
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		
		for (ItemInfo item : ConstProperty.getInstance().getMechaRepairItemList()) {
			int num = player.getData().getItemNumByItemId(item.getItemId());
			item.setCount(num);
			consumeItems.addConsumeInfo(item, false);
		}
		if (!consumeItems.checkConsume(player)) {
			HawkLog.logPrintln("supersoldier init consume failed, playerId: {}", player.getId());
			return;
		}
		
		consumeItems.consumeAndPush(player, Action.SUPER_SOLDIER_UNLOCK);
		// 设置图纸消耗的状态数据
		updateCustomData();
		HawkLog.logPrintln("supersoldier init consume success, playerId: {}", player.getId());
	}
	
	/**
	 * 更新特定key的custom数据
	 */
	private void updateCustomData() {
		String customKey = GameConstCfg.getInstance().getSuperSoldierTutorialKey();
		CustomDataEntity entity = player.getData().getCustomDataEntity(customKey);
		if (entity == null) {
			// 这里不调player.getData().createCustomDataEntity接口，是为了防止db容错的一个缺陷（数据落地失败但又触发了容错机制，导致内存跟db不一致）而客户端又强依赖这个数据
			entity = new CustomDataEntity();
			entity.setPlayerId(player.getId());
			entity.setType(customKey);
			entity.setValue(1);
			entity.setArg("");
			entity.setId(HawkOSOperator.randomUUID());
			entity.create(true);
			player.getData().getCustomDataEntities().add(entity);
		} else {
			entity.setValue(1);
		}
		
		// 和前端对齐后，两种情况下调用这个接口都不用同步了
		//player.getPush().syncCustomData();
	}
	
	/**
	 * 赋能
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SUPERSOLDIER_ENERGY_LEVEL_UP_VALUE)
	private void onSuperSoldierEnergy(HawkProtocol protocol) {
		PBSuperSoldierEnergyReq req = protocol.parseProtocol(PBSuperSoldierEnergyReq.getDefaultInstance());

		// 机甲
		Optional<SuperSoldier> soldierOp = player.getSuperSoldierByCfgId(req.getSuperSoldierId());
		if (!soldierOp.isPresent()) {
			return;
		}
		SuperSoldier superSoldier = soldierOp.get();
		
		// 未解锁赋能
		if (!superSoldier.getSoldierEnergy().isUnlockEnergy()) {
			logger.info("onSuperSoldierEnergy, not unlock energy, playerId:{}, superSoldierId:{}", player.getId(), superSoldier.getCfgId());
			return;
		}
		
		// 赋能部件
		ISuperSoldierEnergy energy = superSoldier.getSoldierEnergy().getEnergy(req.getEnergy());
		if (energy == null) {
			logger.info("onSuperSoldierEnergy, energy not found, playerId:{}, energy:{}", player.getId(), req.getEnergy());
			return;
		}
		
		// 已经达到最大等级
		if (energy.isMaxLevel()) {
			logger.info("onSuperSoldierEnergy, energy is max level, playerId:{}, level:{}", player.getId(), energy.getCfgId());
			return;
		}
		
		// 位置升级限制
		if (energy.positionUpLevelLimit()) {
			logger.info("onSuperSoldierEnergy, position level limit, playerId:{}, energy:{}, level:{}", player.getId(), energy.getCfgId(), superSoldier.getSoldierEnergy().getLevel());
			return;
		}
		
		// 检测消耗
		List<ItemInfo> resItems = energy.getLevelConsume();
		final int FUJIEJING = 9990032; // ，赋能结晶id
		GameUtil.reduceByEffect(resItems, FUJIEJING, player.getEffect().getEffValArr(EffType.EFF_367815));
		
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(resItems);
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.SUPER_SOLDIER_ENERGY_LEVEL_UP);
		
		// 升级
		energy.upLevel();
		superSoldier.notifyChange();
		
		player.responseSuccess(protocol.getType());
		
		// 打点
		LogUtil.logSuperSoldierEnergy(player, superSoldier);
	}
	
	/**
	 * 解锁赋能
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SUPERSOLDIER_ENERGY_UNLOCK_VALUE)
	private void onUnlockSupersoldierEnergy(HawkProtocol protocol) {
		PBUnlockEnergyReq req = protocol.parseProtocol(PBUnlockEnergyReq.getDefaultInstance());
		
		// 机甲
		Optional<SuperSoldier> soldierOp = player.getSuperSoldierByCfgId(req.getSuperSoldierId());
		if (!soldierOp.isPresent()) {
			return;
		}
		SuperSoldier superSoldier = soldierOp.get();
		
		SuperSoldierEnergy soldierEnergy = superSoldier.getSoldierEnergy();
		
		// 已经解锁机甲赋能
		if (soldierEnergy.isUnlockEnergy()) {
			logger.info("onUnlockSupersoldierEnergy, unlock energy already, playerId:{}, superSoldierId:{}", player.getId(), superSoldier.getCfgId());
			return;
		}
		
		// 检测消耗
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(soldierEnergy.getUnlockConsume());
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.SUPER_SOLDIER_ENERGY_UNLOCK);
		
		// 解锁机甲赋能
		soldierEnergy.unlockEnergy();
		CommanderEntity entity = player.getData().getCommanderEntity();
		superSoldier.notifyChange();
		player.responseSuccess(protocol.getType());
		
		// 打点
		LogUtil.logSuperSoldierEnergy(player, superSoldier);
	}

	@ProtocolHandler(code = HP.code2.SUPER_SOLDIER_SKIN_INFO_REQ_VALUE)
	private void skinInfo(HawkProtocol protocol) {
		player.getPush().pushSuperSoldierSkin();
		player.responseSuccess(protocol.getType());
	}

	@ProtocolHandler(code = HP.code2.SUPER_SOLDIER_BATCH_CHANGE_SKIN_REQ_VALUE)
	private void batchChangeSkin(HawkProtocol protocol) {
		PBSuperSoldierBatchChangeSkinReq req = protocol.parseProtocol(PBSuperSoldierBatchChangeSkinReq.getDefaultInstance());
		int skinId = req.getSkinId();
		CommanderEntity entity = player.getData().getCommanderEntity();
		if(!entity.getSuperSoldierSkins().contains(skinId)){
			return;
		}
		Set<Integer> ssoldierIds = new HashSet<>();
		ssoldierIds.addAll(req.getSuperSoldierIdsList());
		for(SuperSoldier ssoldier : player.getAllSuperSoldier()) {
			if (ssoldier == null) {
				continue;
			}
			if(ssoldierIds.contains(ssoldier.getCfgId())){
				ssoldier.changeSkin(skinId);
			}else {
				if(ssoldier.getSkin() == skinId){
					ssoldier.changeSkin(0);
				}
			}
		}
		player.responseSuccess(protocol.getType());
	}


	@ProtocolHandler(code = HP.code2.SUPER_SOLDIER_UNLOCK_SKIN_REQ_VALUE)
	private void unlockSkin(HawkProtocol protocol) {
		PBSuperSoldierUnlockSkinReq req = protocol.parseProtocol(PBSuperSoldierUnlockSkinReq.getDefaultInstance());
		int skinId = req.getSkinId();
		CommanderEntity entity = player.getData().getCommanderEntity();
		SuperSoldierSkinCfg skinCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierSkinCfg.class, skinId);
		if(skinCfg == null){
			return;
		}
		if(entity.getSuperSoldierSkins().contains(skinId)){
			return;
		}
		List<ItemInfo> itemList = ItemInfo.valueListOf(skinCfg.cost());
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(itemList);
		if (!consum.checkConsume(player, protocol.getType())) {
			return;
		}
		consum.consumeAndPush(player, Action.SUPER_SOLDIER_ANYWHERE);
		entity.getSuperSoldierSkins().add(req.getSkinId());
		entity.notifyUpdate();
		player.getPush().pushSuperSoldierSkin();
		player.responseSuccess(protocol.getType());
		for(SuperSoldier ssoldier : player.getAllSuperSoldier()) {
			ssoldier.notifyChange();
		}
	}
}
