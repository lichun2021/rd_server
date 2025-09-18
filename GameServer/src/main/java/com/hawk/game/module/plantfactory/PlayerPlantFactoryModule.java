package com.hawk.game.module.plantfactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.GsApp;
import com.hawk.game.config.PlantFactoryCfg;
import com.hawk.game.entity.PlantFactoryEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.PlayerEffectChangeMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlantFactory.PBCollectFactoryRequest;
import com.hawk.game.protocol.PlantFactory.PBPlantFactoryInfo;
import com.hawk.game.protocol.PlantFactory.PBPlantFactorySync;
import com.hawk.game.protocol.PlantFactory.PBPlantFactoryType;
import com.hawk.game.protocol.PlantFactory.PBUnlockFactoryRequest;
import com.hawk.game.protocol.PlantFactory.PBUpgradeFactoryReuest;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventPlantFactoryLvup;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class PlayerPlantFactoryModule extends PlayerModule {

	public PlayerPlantFactoryModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		syncPlantFactoryInfo();
		return true;
	}

	@Override
	public boolean onTick() {
		return true;
	}

	public void syncPlantFactoryInfo() {
		PBPlantFactorySync.Builder resp = PBPlantFactorySync.newBuilder();
		for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
			PBPlantFactoryInfo info = PBPlantFactoryInfo.newBuilder()
					.setType(PBPlantFactoryType.valueOf(factory.getFactoryType()))
					.setCfgId(factory.getPlantCfgId())
					.setLastResStoreTime(factory.getLastResStoreTime())
					.setStore(factory.getResStoreIntVal())
					.setMaxStore(maxStroe(factory))
					.setEffVal(getEffVal(factory))
					.setCollectOneUseMil(getCollectOneuseMil(factory))
					.build();
			resp.addFactories(info);
		}
		if (resp.getFactoriesCount() == 0) { // 玩家没有解锁任何工厂
			return;
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLANT_FACTORY_SYNC_S, resp));

	}

	@ProtocolHandler(code = HP.code.PLANT_FACTORY_SYNC_C_VALUE)
	private void onFactorySync(HawkProtocol protocol) {
		syncPlantFactoryInfo();
	}

	/**
	 * 解锁新的生产线
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLANT_FACTORY_UNLOCK_C_VALUE)
	private void onUnlockFactory(HawkProtocol protocol) {
		PBUnlockFactoryRequest req = protocol.parseProtocol(PBUnlockFactoryRequest.getDefaultInstance());
		final int factoryId = req.getCfgId();
		PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, factoryId);
		for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
			if (factory.getFactoryType() == cfg.getFactoryType()) {
				player.sendError(protocol.getType(), Status.PlantFactoryError.PLANT_FACTORY_HAS_UNLOCK, 0);
				return; // 已解锁
			}
		}
		if (!checkFront(cfg, protocol.getType())) {
			return;
		}

		// 消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(cfg.getBuildCost()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_FACTORY_UNKOCK);

		// 创建
		PlantFactoryEntity entity = new PlantFactoryEntity();
		entity.setPlayerId(player.getId());
		entity.setPlantCfgId(factoryId);
		entity.setFactoryType(cfg.getFactoryType());
		entity.setLastResStoreTime(GsApp.getInstance().getCurrentTime());

		HawkDBManager.getInstance().create(entity);
		player.getData().getPlantFactoryEntities().add(entity);

		syncPlantFactoryInfo();

		player.responseSuccess(protocol.getType());
		MissionManager.getInstance().postMsg(player, new EventPlantFactoryLvup(entity.getFactoryType(), cfg.getLevel()));
		LogUtil.logPlantFactoryChange(player, Action.PLANT_FACTORY_UNKOCK, 0, cfg.getId(), entity);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.PLANT_FACTORY_UNKOCK,
				Params.valueOf("cost", consumeItems.getItemsInfo()),
				Params.valueOf("factoryId", factoryId));
	}

	/**
	 * 收资源
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLANT_FACTORY_COLLECT_C_VALUE)
	private void onCollectFactory(HawkProtocol protocol) {
		PBCollectFactoryRequest req = protocol.parseProtocol(PBCollectFactoryRequest.getDefaultInstance());
		final List<PBPlantFactoryType> typeList = req.getTypesList();

		AwardItems awardItem = AwardItems.valueOf();
		for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
			if (typeList.contains(PBPlantFactoryType.valueOf(factory.getFactoryType()))) {
				ItemInfo item = collectRes(factory);
				if (Objects.nonNull(item)) {
					awardItem.addItem(item);
				}
			}
		}
		awardItem.rewardTakeAffectAndPush(player, Action.PLANT_FACTORY_COLLECT, true, RewardOrginType.PLANT_FACTORY_COLLECT);

		syncPlantFactoryInfo();
		player.responseSuccess(protocol.getType());

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.PLANT_FACTORY_COLLECT,
				Params.valueOf("awardItem", awardItem.getAwardItems()));
	}

	/**
	 * 升级
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLANT_FACTORY_UPGRADE_C_VALUE)
	private void onUpgradeFactory(HawkProtocol protocol) {
		PBUpgradeFactoryReuest req = protocol.parseProtocol(PBUpgradeFactoryReuest.getDefaultInstance());
		final PBPlantFactoryType type = req.getType();
		PlantFactoryEntity upfactory = null;
		for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
			if (type == PBPlantFactoryType.valueOf(factory.getFactoryType())) {
				upfactory = factory;
				break;
			}
		}
		PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, upfactory.getPlantCfgId());
		PlantFactoryCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, cfg.getPostStage());

		if (!checkFront(upcfg, protocol.getType())) {
			return;
		}
		// 先更新储量
		resTore(upfactory);

		// 消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(upcfg.getBuildCost()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_FACTORY_UPGRADE);

		upfactory.setPlantCfgId(upcfg.getId());

		syncPlantFactoryInfo();

		player.responseSuccess(protocol.getType());
		MissionManager.getInstance().postMsg(player, new EventPlantFactoryLvup(upfactory.getFactoryType(), upcfg.getLevel()));
		LogUtil.logPlantFactoryChange(player, Action.PLANT_FACTORY_UPGRADE, cfg.getId(), upcfg.getId(), upfactory);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.PLANT_FACTORY_UPGRADE,
				Params.valueOf("cost", consumeItems.getItemsInfo()),
				Params.valueOf("beforedCfgId", cfg.getId()),
				Params.valueOf("afterCfgId", upcfg.getId()));

	}

	private ItemInfo collectRes(PlantFactoryEntity factory) {
		PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, factory.getPlantCfgId());
		double store = resTore(factory);
		if (store < 1) {
			return null;
		}

		int take = (int) store; // 取走的量
		factory.setResStore(store - take);

		ItemInfo item = new ItemInfo(ItemType.TOOL_VALUE, cfg.getItemId(), take);
		return item;
	}

	/**计算并更新当前储量*/
	private double resTore(PlantFactoryEntity factory) {
		long passTime = GsApp.getInstance().getCurrentTime() - factory.getLastResStoreTime();

		double collectOneUseMil = getCollectOneuseMil(factory);

		double store = passTime / collectOneUseMil + factory.getResStore();

		store = Math.min(store, maxStroe(factory));
		factory.setResStore(store);
		factory.setLastResStoreTime(GsApp.getInstance().getCurrentTime());
		return factory.getResStore();
	}

	private int getCollectOneuseMil(PlantFactoryEntity factory) {
		double effrate = getEffRate(factory);
		PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, factory.getPlantCfgId());
		double collectOneUseMil = cfg.getCollectOneUseMil() / effrate; // 做用号改在这里
		return (int) collectOneUseMil;
	}

	/**储量上限*/
	private int maxStroe(PlantFactoryEntity factory) {
		PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, factory.getPlantCfgId());
		double effrate = getEffRate(factory);
		return (int) (cfg.getItemLimit() * effrate); // 做用号同样会提升上限
	}

	/** 增产做用号换算为比例*/
	private double getEffRate(PlantFactoryEntity factory) {
		return (GsConst.EFF_RATE + getEffVal(factory)) * GsConst.EFF_PER;
	}

	/** 曾产做用号值
	 * 622：勋章，type=1 623：复制晶体，type=3 624：聚能核心，type=2
	 */
	public int getEffVal(PlantFactoryEntity factory) {
		int result = 0;
		result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED);
		result += player.getEffect().getEffVal(EffType.LIFE_TIME_CARD_646);
		PBPlantFactoryType type = PBPlantFactoryType.valueOf(factory.getFactoryType());
		switch (type) {
		case LINE_MEDAL:
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_622);
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_625);
			break;
		case LINE_CORE:
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_624);
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_627);
			break;
		case LINE_CRYSTAL:
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_623);
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_626);
			break;
		case LINE_BRILLIANT_MEDAL:
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_635);
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_636);
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_637);
			break;
		case LINE_TAIKUAGNGMUTI:
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_638);
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_651);
			result += player.getEffect().getEffVal(EffType.PLANT_FACTORY_SPEED_652);
			break;
		default:
			break;
		}

		return result;
	}

	@MessageHandler
	private void onEffectChangeEvent(PlayerEffectChangeMsg event) {
		boolean repush = event.hasEffectChange(EffType.PLANT_FACTORY_SPEED)
				||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_622)
				||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_624)
				||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_623)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_625)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_626)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_627)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_635)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_636)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_637)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_638)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_651)
						||event.hasEffectChange(EffType.PLANT_FACTORY_SPEED_652)
						||event.hasEffectChange(EffType.LIFE_TIME_CARD_646);
		if (!repush) {
			return;
		}
		for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
			resTore(factory);
		}
		syncPlantFactoryInfo();
	}

	/** 检查前置建筑*/
	private boolean checkFront(PlantFactoryCfg cfg, int protoType) {
		// 判断前置建筑条件，即判断建筑是否已解锁
		if (!BuildingService.getInstance().checkFrontCondition(player, cfg.getFrontBuildIds(), null, protoType)) {
			return false;
		}

		// 检查前置工厂
		for (int cfgId : cfg.getFrontPlantIds()) {
			PlantFactoryCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, cfgId);
			if (buildingCfg == null) {
				continue;
			}

			Optional<PlantFactoryEntity> op = player.getData().getPlantFactoryEntities().stream()
					.filter(e -> e.getFactoryType() == buildingCfg.getFactoryType())
					.filter(e -> e.getPlantCfgId() >= cfgId)
					.findAny();

			if (!op.isPresent()) {
				player.sendError(protoType, Status.PlantFactoryError.FRONT_PLANT_NOT_EXIT, 0);
				return false;
			}
		}

		// 上一等级工厂是否解锁
		if (cfg.getFrontStage() != 0) {
			boolean frontStateUnlock = false;
			for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
				if (factory.getPlantCfgId() == cfg.getFrontStage()) {
					frontStateUnlock = true; // 已解锁
					break;
				}
			}
			if (!frontStateUnlock) {
				player.sendError(protoType, Status.PlantFactoryError.FRONT_PLANT_NOT_EXIT, 0);
				return false;
			}
		}

		return true;
	}
}
