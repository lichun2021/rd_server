package com.hawk.game.module.soldierExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SoldierExchangePage1Event;
import com.hawk.activity.event.impl.SoldierExchangePage2Event;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backSoldierExchange.BackSoldierExchangeActivity;
import com.hawk.activity.type.impl.backSoldierExchange.cfg.BackSoldierExchangeKVCfg;
import com.hawk.activity.type.impl.backSoldierExchange.cfg.BackSoldierExchangeShopCfg;
import com.hawk.activity.type.impl.backSoldierExchange.entity.BackSoldierExchangeEntity;
import com.hawk.activity.type.impl.soldierExchange.SoldierExchangeActivity;
import com.hawk.activity.type.impl.soldierExchange.cfg.SoldierExchangeKVCfg;
import com.hawk.activity.type.impl.soldierExchange.cfg.SoldierExchangeShopCfg;
import com.hawk.activity.type.impl.soldierExchange.entity.SoldierExchangeActivityEntity;
import com.hawk.game.GsApp;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZTeamRoom;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SoldierExchange.PBSEHistoryReq;
import com.hawk.game.protocol.SoldierExchange.PBSEOpenPageReq;
import com.hawk.game.protocol.SoldierExchange.PBSEReq;
import com.hawk.game.protocol.SoldierExchange.PBSEResp;
import com.hawk.game.protocol.SoldierExchange.PBSEShopBuyReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.robot.WORPlayer;
import com.hawk.game.world.robot.cache.WORPlayerDataCache;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.log.Action;

public class PlayerSoldierExchangeModule extends PlayerModule {
	private long coolTime;
	
	private long coolTime356;
	
	
	public PlayerSoldierExchangeModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		return true;
	}

	@ProtocolHandler(code = HP.code2.SOLDIER_EXCHANGE_PRE_VIEW_C_VALUE)
	private void onSoldierExchangePreView(HawkProtocol protocol) {
		PBSEReq req = protocol.parseProtocol(PBSEReq.getDefaultInstance());
		SoldierType fromType = req.getFromType();
		SoldierType toType = req.getToType();
		if (fromType == toType) {
			return;
		}

		WORPlayer target = (WORPlayer) WorldRobotService.getInstance().createRobotPlayer(player.getId(), 0);
		WORPlayerDataCache cache = (WORPlayerDataCache) target.getData().getDataCache();
		cache.getDataLoader().setRealArmy(true);

		SoldierExchangeUtil util = SoldierExchangeUtil.create(target, fromType, toType);
		util.zhuanArmy(true);
		util.zhuanBuild(true);
		util.zhuanPlantSchool(true);
		util.zhuanSuperSoldier();
		util.zhuanArmour();
		util.zhuanMechaCore(player);
		util.zhuanPlantTech();
		util.zhuanBuild(false);
		util.zhuanPlantSchool(false);
		util.zhuanArmy(false);
		PBSEResp.Builder resp = util.getResp();
		List<Integer> errset = new ArrayList<>();
		exchangeCheck(protocol, errset,this.coolTime);
		resp.addAllCheckErr(errset);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SOLDIER_EXCHANGE_PRE_VIEW_S, resp));
		WorldRobotService.getInstance().invalidate(target);

	}

	@ProtocolHandler(code = HP.code2.SOLDIER_EXCHANGE_C_VALUE)
	private void onSoldierExchange(HawkProtocol protocol) {

		List<Integer> errset = new ArrayList<>();
		if (!exchangeCheck(protocol, errset,this.coolTime)) {
			sendError(protocol.getType(), errset.get(0));
			return;
		}

		Optional<SoldierExchangeActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SOLDIER_EXCHANGE.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		SoldierExchangeActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return;
		}
		Optional<SoldierExchangeActivityEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		if (!opEntity.isPresent()) {
			return;
		}
		SoldierExchangeActivityEntity entity = opEntity.get();
		SoldierExchangeKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(SoldierExchangeKVCfg.class);
		if (entity.getHistorList().size() >= kvcfg.getExchangeTotalTime()) {
			return;
		}

		PBSEReq req = protocol.parseProtocol(PBSEReq.getDefaultInstance());
		SoldierType fromType = req.getFromType();
		SoldierType toType = req.getToType();
		if (fromType == toType || fromType.getNumber() > 8 || toType.getNumber() > 8) {
			return;
		}

		// 扣费
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(ItemInfo.valueListOf(kvcfg.getExchangeCost()));
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.BINZHONGZHUANHUAN);

		SoldierExchangeUtil util = SoldierExchangeUtil.create(player, fromType, toType);
		util.zhuanArmy(true);
		util.zhuanBuild(true);
		util.zhuanPlantSchool(true);
		util.zhuanSuperSoldier();
		util.zhuanArmour();
		util.zhuanMechaCore(player);
		util.zhuanPlantTech();
		util.zhuanBuild(false);
		util.zhuanPlantSchool(false);
		util.zhuanArmy(false);

		String uuid = HawkUUIDGenerator.genUUID();
		RedisProxy.getInstance().getRedisSession().setBytes(uuid, util.getResp().build().toByteArray());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SOLDIER_EXCHANGE_S, util.getResp()));
		coolTime = GsApp.getInstance().getCurrentTime() + kvcfg.getExchangeCD() * 1000;

		entity.getHistorList().add(uuid);
		entity.getShopItems().clear();
		entity.setExchangeType(toType.getNumber());
		entity.getItemList().clear();
		entity.setCoolTime(coolTime  );
		activity.initAchieveInfo(player.getId());
		activity.syncActivityDataInfo(player.getId());
		
	}

	private boolean exchangeCheck(HawkProtocol protocol, List<Integer> errList,long coolTime) {
		if (coolTime > GsApp.getInstance().getCurrentTime()) {
			errList.add(Status.Error.SE_exchangeCD_NOT_VALUE);
		}
		
		if (player.isCsPlayer()) {
			errList.add(Status.Error.SE_CS_NOT_VALUE);
		}

		DYZZTeamRoom team = DYZZService.getInstance().getPlayerTeamRoom(player.getId());
		if (player.isInDungeonMap() || player.getLmjyState() != null || team != null || WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
			errList.add(Status.Error.SE_Dungeon_NOT_VALUE);
		}

		for (ArmyEntity army : player.getData().getArmyEntities()) { // 国家医院有伤兵
			if (army.getTrainCount() > 0) {
				errList.add(Status.Error.SE_TrainCount_NOT_VALUE);
			}
			if (army.getTrainFinishCount() > 0) {
				errList.add(Status.Error.SE_TrainFinishCount_NOT_VALUE);
			}
			if (army.getWoundedCount() > 0 || army.getCureFinishCount() > 0) {
				errList.add(Status.Error.SE_WoundedCount_NOT_VALUE);
			}
			if (army.getMarch() > 0) {
				errList.add(Status.Error.SE_March_NOT_VALUE);
			}
		}
		
		if (!WorldMarchService.getInstance().getPlayerMarch(player.getId()).isEmpty()) {
			errList.add(Status.Error.SE_March_NOT_VALUE);
		}

		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(player.getPlayerPos());
		if (targetPoint.getShowProtectedEndTime() < HawkTime.getMillisecond()) { // 不开罩不行
			errList.add(Status.Error.SE_CityShieldTime_NOT_VALUE);
		}

		// 城内有援助行军，不能进入泰伯利亚
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			errList.add(Status.Error.SE_Asst_NOT_VALUE);
		}

		// 有被动行军
		BlockingQueue<IWorldMarch> passiveMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
		if (!CollectionUtils.isEmpty(passiveMarchs)) {
			int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
			for (IWorldMarch march : passiveMarchs) {
				if (march == null || march.getMarchEntity() == null || march.getMarchEntity().isInvalid()) {
					continue;
				}
				if (march.getTerminalId() != playerPos) {
					continue;
				}
				if (march.getMarchType().getNumber() == WorldMarchType.ASSISTANCE_VALUE) {
					continue;
				}
				errList.add(Status.Error.SE_PassiveMarch_NOT_VALUE);
			}
		}

		Optional<QueueEntity> buildop = getPlayerData().getQueueEntities().stream()
				.filter(queue -> queue.getQueueType() == QueueType.BUILDING_QUEUE_VALUE)
				.filter(queue -> queue.getReusage() != QueueReusage.FREE.intValue())
				.findAny();
		// 有建筑在升级
		if (buildop.isPresent()) {
			errList.add(Status.Error.SE_BuildUPLevel_NOT_VALUE);
		}

		// 当前有正在研究的装备科技
		for (QueueEntity queueEntity : player.getData().getQueueEntities()) {
			if (queueEntity.getEnableEndTime() != 0 || queueEntity.getReusage() == QueueReusage.FREE.intValue()) {
				continue;
			}
			switch (queueEntity.getQueueType()) {
			case QueueType.SCIENCE_QUEUE_VALUE: //科技升级
				errList.add(Status.Error.SE_TECH_RESEARCH_NOT_VALUE);
				break;
			case QueueType.GUILD_HOSPICE_QUEUE_VALUE: //联盟关怀
				errList.add(Status.Error.SE_GUILD_HOSPICE_NOT_VALUE);
				break;
			case QueueType.CROSS_TECH_QUEUE_VALUE: //远征科技研究
				errList.add(Status.Error.SE_CROSS_TECH_RESEARCH_NOT_VALUE);
				break;
			case QueueType.EQUIP_RESEARCH_QUEUE_VALUE: //装备科技研究
				errList.add(Status.Error.SE_EQUIP_RESEARCH_NOT_VALUE);
				break;
			case QueueType.PLANT_SCIENCE_QUEUE_VALUE: //泰能科技研究
				errList.add(Status.Error.SE_PLANT_SCIENCE_RESEARCH_NOT_VALUE);
				break;
			}
		}
		
//		if (ChampionshipService.activityInfo.state.getNumber() <= GCState.WAR_VALUE) {
//			Set<String> champbattle = RedisProxy.getInstance().getGCPlayerIds(ChampionshipService.activityInfo.getTermId(), player.getGuildId());
//			if (champbattle.contains(player.getId())) { // 参加攻防不行
//				errList.add(Status.Error.SE_CHAMP_NOT_VALUE);
//			}
//		}
//
//		if (SimulateWarService.getInstance().getActivityInfo().getState().getNumber() <= SimulateWarActivityState.SW_MARCH_VALUE) {
//			Set<String> simulatebattle = RedisProxy.getInstance().getSimulateWarGuildPlayerIds(SimulateWarService.getInstance().getTermId(), player.getGuildId());
//			if (simulatebattle.contains(player.getId())) { // 参加军团不行
//				errList.add(Status.Error.SE_SimulateWar_NOT_VALUE);
//			}
//		}

		return errList.isEmpty();
	}

	@ProtocolHandler(code = HP.code2.SOLDIER_EXCHANGE_BUY_REQ_VALUE)
	private void onSoldierBuyShop(HawkProtocol protocol) {
		Optional<SoldierExchangeActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SOLDIER_EXCHANGE.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		SoldierExchangeActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return;
		}
		Optional<SoldierExchangeActivityEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		if (!opEntity.isPresent()) {
			return;
		}
		SoldierExchangeActivityEntity entity = opEntity.get();

		PBSEShopBuyReq req = protocol.parseProtocol(PBSEShopBuyReq.getDefaultInstance());
		final int shopId = req.getShopId();
		final int cnt = req.getCnt();
		SoldierExchangeShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(SoldierExchangeShopCfg.class, shopId);
		if (shopCfg == null || shopCfg.getSoldierType() != entity.getExchangeType() || shopCfg.getExchangeCount() < cnt + entity.getShopItemVal(shopId)) {
			return;
		}

		// 扣费
		ConsumeItems consume = ConsumeItems.valueOf();
		for (int i = 0; i < cnt; i++) {
			consume.addConsumeInfo(ItemInfo.valueListOf(shopCfg.getPay()));
		}
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.BINZHONGZHUANHUAN);

		AwardItems awardItem = AwardItems.valueOf();
		for (int i = 0; i < cnt; i++) {
			awardItem.addItemInfos(ItemInfo.valueListOf(shopCfg.getGain()));
		}
		awardItem.rewardTakeAffectAndPush(player, Action.BINZHONGZHUANHUAN, true);
		entity.addShopItems(shopId, cnt);
		entity.notifyUpdate();
		activity.syncActivityDataInfo(player.getId());

		player.responseSuccess(protocol.getType());

	}

	@ProtocolHandler(code = HP.code2.SOLDIER_EXCHANGE_HISTORY_C_VALUE)
	private void onSoldierExchangeHistory(HawkProtocol protocol) {
		PBSEHistoryReq req = protocol.parseProtocol(PBSEHistoryReq.getDefaultInstance());
		byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(req.getExchangeIdBytes().toByteArray());

		PBSEResp.Builder builder = PBSEResp.newBuilder();
		try {
			builder.mergeFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SOLDIER_EXCHANGE_HISTORY_S, builder));
	}
	
	@ProtocolHandler(code = HP.code2.SOLDIER_EXCHANGE_OPEN_PAGE_VALUE)
	private void onSoldierExchangeOpenPage(HawkProtocol protocol) {
		Optional<SoldierExchangeActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SOLDIER_EXCHANGE.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		SoldierExchangeActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return;
		}
		Optional<SoldierExchangeActivityEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		if (!opEntity.isPresent()) {
			return;
		}
		if (opEntity.get().getExchangeType() == 0) {
			return;
		}
		
		PBSEOpenPageReq req = protocol.parseProtocol(PBSEOpenPageReq.getDefaultInstance());
		switch (req.getPage()) {
		case 1:
			ActivityManager.getInstance().postEvent(new SoldierExchangePage1Event(player.getId()));
			break;
		case 2:
			ActivityManager.getInstance().postEvent(new SoldierExchangePage2Event(player.getId()));
			break;
		default:
			break;
		}
	}
	
	
	
	//**************************************回流转兵种活动356-复制转兵种代码********************************************************************
	
	
	@ProtocolHandler(code = HP.code2.BACK_SOLDIER_EXCHANGE_PRE_VIEW_C_VALUE)
	private void onSoldierExchangePreView356(HawkProtocol protocol) {
		String playerId = this.player.getId();
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		BackSoldierExchangeActivity activity = this.getBackSoldierExchangeActivity(playerId);
		if(Objects.isNull(activity)){
			return;
		}
		if(activity.isActivityClose(playerId)){
			return;
		}
		PBSEReq req = protocol.parseProtocol(PBSEReq.getDefaultInstance());
		SoldierType fromType = req.getFromType();
		SoldierType toType = req.getToType();
		if (fromType == toType) {
			return;
		}

		WORPlayer target = (WORPlayer) WorldRobotService.getInstance().createRobotPlayer(player.getId(), 0);
		WORPlayerDataCache cache = (WORPlayerDataCache) target.getData().getDataCache();
		cache.getDataLoader().setRealArmy(true);

		SoldierExchangeUtil util = SoldierExchangeUtil.create(target, fromType, toType);
		util.zhuanArmy(true);
		util.zhuanBuild(true);
		util.zhuanPlantSchool(true);
		util.zhuanSuperSoldier();
		util.zhuanArmour();
		util.zhuanMechaCore(player);
		util.zhuanPlantTech();
		util.zhuanBuild(false);
		util.zhuanPlantSchool(false);
		util.zhuanArmy(false);
		PBSEResp.Builder resp = util.getResp();
		List<Integer> errset = new ArrayList<>();
		exchangeCheck(protocol, errset,this.coolTime356);
		resp.addAllCheckErr(errset);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.BACK_SOLDIER_EXCHANGE_PRE_VIEW_S, resp));
		WorldRobotService.getInstance().invalidate(target);

	}

	@ProtocolHandler(code = HP.code2.BACK_SOLDIER_EXCHANGE_C_VALUE)
	private void onSoldierExchange356(HawkProtocol protocol) {
		String playerId = this.player.getId();
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		BackSoldierExchangeActivity activity = this.getBackSoldierExchangeActivity(playerId);
		if(Objects.isNull(activity)){
			return;
		}
		if(activity.isActivityClose(playerId)){
			return;
		}
		List<Integer> errset = new ArrayList<>();
		if (!exchangeCheck(protocol, errset,this.coolTime356)) {
			sendError(protocol.getType(), errset.get(0));
			return;
		}
		if (!activity.isOpening(playerId)) {
			return;
		}
		Optional<BackSoldierExchangeEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		if (!opEntity.isPresent()) {
			return;
		}
		BackSoldierExchangeEntity entity = opEntity.get();
		BackSoldierExchangeKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(BackSoldierExchangeKVCfg.class);
		if (entity.getHistorList().size() >= kvcfg.getExchangeTotalTime()) {
			return;
		}

		PBSEReq req = protocol.parseProtocol(PBSEReq.getDefaultInstance());
		SoldierType fromType = req.getFromType();
		SoldierType toType = req.getToType();
		if (fromType == toType || fromType.getNumber() > 8 || toType.getNumber() > 8) {
			return;
		}

		// 扣费
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(ItemInfo.valueListOf(kvcfg.getExchangeCost()));
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.BINZHONGZHUANHUAN);

		SoldierExchangeUtil util = SoldierExchangeUtil.create(player, fromType, toType);
		util.zhuanArmy(true);
		util.zhuanBuild(true);
		util.zhuanPlantSchool(true);
		util.zhuanSuperSoldier();
		util.zhuanArmour();
		util.zhuanMechaCore(player);
		util.zhuanPlantTech();
		util.zhuanBuild(false);
		util.zhuanPlantSchool(false);
		util.zhuanArmy(false);

		String uuid = HawkUUIDGenerator.genUUID();
		RedisProxy.getInstance().getRedisSession().setBytes(uuid, util.getResp().build().toByteArray());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.BACK_SOLDIER_EXCHANGE_S, util.getResp()));
		coolTime356 = GsApp.getInstance().getCurrentTime() + kvcfg.getExchangeCD() * 1000;

		entity.getHistorList().add(uuid);
		entity.getShopItems().clear();
		entity.setExchangeType(toType.getNumber());
		//entity.getItemList().clear();
		entity.setCoolTime(coolTime356);
		activity.syncActivityDataInfo(player.getId());
	}

	
	@ProtocolHandler(code = HP.code2.BACK_SOLDIER_EXCHANGE_BUY_REQ_VALUE)
	private void onSoldierBuyShop356(HawkProtocol protocol) {
		String playerId = this.player.getId();
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		BackSoldierExchangeActivity activity = this.getBackSoldierExchangeActivity(playerId);
		if(Objects.isNull(activity)){
			return;
		}
		if(activity.isActivityClose(playerId)){
			return;
		}
		if (!activity.isOpening(player.getId())) {
			return;
		}
		Optional<BackSoldierExchangeEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		if (!opEntity.isPresent()) {
			return;
		}
		BackSoldierExchangeEntity entity = opEntity.get();

		PBSEShopBuyReq req = protocol.parseProtocol(PBSEShopBuyReq.getDefaultInstance());
		final int shopId = req.getShopId();
		final int cnt = req.getCnt();
		BackSoldierExchangeShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(BackSoldierExchangeShopCfg.class, shopId);
		if (shopCfg == null || shopCfg.getSoldierType() != entity.getExchangeType() || shopCfg.getExchangeCount() < cnt + entity.getShopItemVal(shopId)) {
			return;
		}

		// 扣费
		ConsumeItems consume = ConsumeItems.valueOf();
		for (int i = 0; i < cnt; i++) {
			consume.addConsumeInfo(ItemInfo.valueListOf(shopCfg.getPay()));
		}
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.BINZHONGZHUANHUAN);

		AwardItems awardItem = AwardItems.valueOf();
		for (int i = 0; i < cnt; i++) {
			awardItem.addItemInfos(ItemInfo.valueListOf(shopCfg.getGain()));
		}
		awardItem.rewardTakeAffectAndPush(player, Action.BINZHONGZHUANHUAN, true);
		entity.addShopItems(shopId, cnt);
		entity.notifyUpdate();
		activity.syncActivityDataInfo(player.getId());

		player.responseSuccess(protocol.getType());

	}

	@ProtocolHandler(code = HP.code2.BACK_SOLDIER_EXCHANGE_HISTORY_C_VALUE)
	private void onSoldierExchangeHistory356(HawkProtocol protocol) {
		String playerId = this.player.getId();
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		BackSoldierExchangeActivity activity = this.getBackSoldierExchangeActivity(playerId);
		if(Objects.isNull(activity)){
			return;
		}
		if(activity.isActivityClose(playerId)){
			return;
		}
		PBSEHistoryReq req = protocol.parseProtocol(PBSEHistoryReq.getDefaultInstance());
		byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(req.getExchangeIdBytes().toByteArray());
		PBSEResp.Builder builder = PBSEResp.newBuilder();
		try {
			builder.mergeFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.BACK_SOLDIER_EXCHANGE_HISTORY_S, builder));
	}
	
	@ProtocolHandler(code = HP.code2.BACK_SOLDIER_EXCHANGE_OPEN_PAGE_VALUE)
	private void onSoldierExchangeOpenPage356(HawkProtocol protocol) {
		String playerId = this.player.getId();
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		BackSoldierExchangeActivity activity = this.getBackSoldierExchangeActivity(playerId);
		if(Objects.isNull(activity)){
			return;
		}
		if(activity.isActivityClose(playerId)){
			return;
		}
		Optional<BackSoldierExchangeEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		if (!opEntity.isPresent()) {
			return;
		}
		if (opEntity.get().getExchangeType() == 0) {
			return;
		}
		
		PBSEOpenPageReq req = protocol.parseProtocol(PBSEOpenPageReq.getDefaultInstance());
		switch (req.getPage()) {
		case 1:
			ActivityManager.getInstance().postEvent(new SoldierExchangePage1Event(player.getId()));
			break;
		case 2:
			ActivityManager.getInstance().postEvent(new SoldierExchangePage2Event(player.getId()));
			break;
		default:
			break;
		}
	}
	
	
	public BackSoldierExchangeActivity getBackSoldierExchangeActivity(String playerId) {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.BACK_SOLDIER_EXCHANGE.intValue());
		if(!activity.isPresent()){
			return null;
		}
		BackSoldierExchangeActivity backActivity = (BackSoldierExchangeActivity) activity.get();
		if(!backActivity.isShow(playerId)){
			return null;
		}
		return backActivity;
	} 
	
}
