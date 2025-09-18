package com.hawk.game.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.common.base.Joiner;
import com.google.common.collect.Table;
import com.hawk.game.config.GuildManorWareHouseCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.manor.building.GuildManorWarehouse;
import com.hawk.game.invoker.StoreMarchBackMsgInvoker;
import com.hawk.game.invoker.StoreMarchReachMsgInvoker;
import com.hawk.game.invoker.StoreMarchTakeReachMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildWarehouse.HPRandomBoxCooling;
import com.hawk.game.protocol.GuildWarehouse.HPStoreItem;
import com.hawk.game.protocol.GuildWarehouse.HPWareHouseSync;
import com.hawk.game.protocol.GuildWarehouse.HPYuriMonsterQueryHelpReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.Predicates;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 联盟仓库
 * 
 * @author lwt
 *
 */
public class PlayerManorWarehouseModule extends PlayerModule {

	final int WARE_INDEX = 1;

	public PlayerManorWarehouseModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		pushBoxCooling();
		return super.onPlayerLogin();
	}

	@Override
	public boolean onTick() {
		return super.onTick();
	}

	/**
	 * 请求同步
	 */
	@ProtocolHandler(code = HP.code.WARE_HOUSE_SYNC_C_VALUE)
	private void onSyncReq(HawkProtocol protocol) {
		this.syncHouseInfo();
		player.responseSuccess(protocol.getType());
	}

	public void syncHouseInfo() {
		GuildManorWarehouse wareHouse = GuildManorService.getInstance().getBuildingByTypeAndIdx(player.getGuildId(), WARE_INDEX, TerritoryType.GUILD_STOREHOUSE);
		HPWareHouseSync.Builder resp = HPWareHouseSync.newBuilder();
		Table<String, String, Integer> table = wareHouse.getPlayerDeposit();
		for (Entry<String, Map<String, Integer>> ent : table.rowMap().entrySet()) {
			String playerId = ent.getKey();
			Map<String, Integer> storeMap = ent.getValue();
			Player tarPlayer = GlobalData.getInstance().makesurePlayer(playerId);
			if (null == tarPlayer) {
				continue;
			}
			String itemStr = storeMap.entrySet().stream()
					.filter(e -> e.getValue() > 0)
					.map(e -> e.getKey() + "_" + e.getValue()).collect(Collectors.joining(","));
			if (HawkOSOperator.isEmptyString(itemStr)) {
				continue;
			}
			HPStoreItem help = HPStoreItem.newBuilder()
					.setPlayerId(tarPlayer.getId())
					.setLevel(tarPlayer.getLevel())
					.setName(tarPlayer.getName())
					.setOffice(GameUtil.getOfficerId(tarPlayer.getId()))
					.setVip(tarPlayer.getVipLevel())
					.setVipActive(tarPlayer.getData().getVipActivated())
					.setIcon(tarPlayer.getIcon())
					.setPfIcon(tarPlayer.getPfIcon() == null ? "" : tarPlayer.getPfIcon())
					.setItems(itemStr)
					.setCommon(BuilderUtil.genPlayerCommonBuilder(tarPlayer))
					.build();
			resp.addItems(help);
			if (Objects.equals(playerId, player.getId())) {
				resp.setPlayerStore(help);
			}
		}

		if (!resp.hasPlayerStore()) {
			HPStoreItem help = HPStoreItem.newBuilder()
					.setPlayerId(player.getId())
					.setLevel(player.getLevel())
					.setName(player.getName())
					.setOffice(PresidentOfficier.getInstance().getOfficerId(player.getId()))
					.setVip(player.getVipLevel())
					.setVipActive(player.getData().getVipActivated())
					.setIcon(player.getIcon())
					.setPfIcon(player.getPfIcon() == null ? "" : player.getPfIcon())
					.build();
			resp.setPlayerStore(help);
		}

		String itemStr = table.row(player.getId()).entrySet().stream()
				.map(e -> Joiner.on("_").join(e.getKey(), e.getValue()))
				.collect(Collectors.joining(","));
		List<ItemInfo> allStore = ItemInfo.valueListOf(itemStr);
		int totalWeight = (int)allStore.stream().mapToLong(ItemInfo::weight).sum();
		int todayWeight = LocalRedis.getInstance().warehouseStoreCount(player.getId());
		GuildManorWareHouseCfg cfg = wareHouse.getCfg();
		int effPer = player.getEffect().getEffVal(EffType.GUILD_WEARHOUSE_LIMIT);
		int effVal = player.getEffect().getEffVal(EffType.GUILD_WEARHOUSE_LIMIT_VAL);
		int daymax = (int) (cfg.getEverydayUpLimit() * (1 + effPer * GsConst.EFF_PER) + effVal);
		resp.setDayMax(daymax)
				.setTodayStore(todayWeight)
				.setStoreMax(cfg.getSaveUpLimit())
				.setTotalStore(totalWeight);

		player.sendProtocol(HawkProtocol.valueOf(HP.code.WARE_HOUSE_SYNC_S, resp));

	}

	/**
	 * 发起资源保存行军
	 */
	@ProtocolHandler(code = HP.code.WARE_HOUSE_STORE_MARCH_C_VALUE)
	private void onStoreRes(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());

		GuildManorWarehouse wareHouse = GuildManorService.getInstance().getBuildingByTypeAndIdx(player.getGuildId(), WARE_INDEX, TerritoryType.GUILD_STOREHOUSE);
		if(Objects.isNull(wareHouse)){ //还没有仓库
			return;
		}
		Predicate<RewardItem> isUnsafe = Predicates.of((RewardItem item) -> item.getItemId() == PlayerAttr.GOLDORE_UNSAFE_VALUE)
				.or(item -> item.getItemId() == PlayerAttr.OIL_UNSAFE_VALUE)
				.or(item -> item.getItemId() == PlayerAttr.STEEL_UNSAFE_VALUE)
				.or(item -> item.getItemId() == PlayerAttr.TOMBARTHITE_UNSAFE_VALUE);
		List<ItemInfo> itemInfos = req.getAssistantList().stream()
				.filter(isUnsafe)
				.map(this::rewardItem2ItemInfo)
				.collect(Collectors.toList());

		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(itemInfos, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}

		consumeItems.consumeAndPush(player, Action.WAREHOUSE_STORE_RES);

		// 回复协议
		player.responseSuccess(protocol.getType());

		WorldPoint point = wareHouse.getPoint();
		if (point == null) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_MANOR_VALUE);
			return;
		}

		List<ArmyInfo> armys = new ArrayList<>();
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.WAREHOUSE_STORE_VALUE, point.getId(), wareHouse.getEntity().getId(), new EffectParams(req, armys));
		
		if (march == null) {
			return;
		}

		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(itemInfos);
		march.getMarchEntity().setAwardItems(awardItems);

		BehaviorLogger.log4Service(player, Source.GUILD_MANOR,
				Action.WARE_HOUSE_STORE_MARCH,
				Params.valueOf("march", march), Params.valueOf("resToStore", awardItems.getAwardItems()));
	}

	/**
	 * 存款行军到达
	 */
	public void msgStoreMarchReach(IWorldMarch march) {
		GuildManorWarehouse wareHouse = GuildManorService.getInstance().getBuildingByTypeAndIdx(player.getGuildId(), WARE_INDEX, TerritoryType.GUILD_STOREHOUSE);
		if (null == wareHouse) {
			WorldMarchService.getInstance().onPlayerNoneAction(march, march.getMarchEntity().getReachTime());
			return;
		}
		StoreMarchReachMsgInvoker invoker = new StoreMarchReachMsgInvoker(wareHouse, march, player, this);
		GuildManorService.getInstance().dealMsg(MsgId.WARE_HOUSE_STORE_MARCH_REACH, invoker);

		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(march, march.getMarchEntity().getReachTime());
	}

	/**
	 * 存款行军返回
	 */
	public void msgStoreMarchBack(WorldMarch march) {
		AwardItems awardItems = march.getAwardItems();

		boolean isCallBack = march.getCallBackTime() > 0;
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION,
				Action.WARE_HOUSE_STORE_MARCH_BACK,
				Params.valueOf("march", march), Params.valueOf("takeBack", awardItems.getAwardItems()),
				Params.valueOf("isCallBack", isCallBack));

		StoreMarchBackMsgInvoker invoker = new StoreMarchBackMsgInvoker(awardItems, player);
		player.dealMsg(MsgId.WARE_HOUSE_STORE_MARCH_BACK, invoker);

	}

	/**
	 * 发起资源提取行军
	 */
	@ProtocolHandler(code = HP.code.WARE_HOUSE_TAKE_MARCH_C_VALUE)
	private void onTakeRes(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());

		GuildManorWarehouse wareHouse = GuildManorService.getInstance().getBuildingByTypeAndIdx(player.getGuildId(), WARE_INDEX, TerritoryType.GUILD_STOREHOUSE);

		// TODO 资源类型要策划配置
		List<ItemInfo> itemInfos = req.getAssistantList().stream()
				.map(this::rewardItem2ItemInfo)
				.collect(Collectors.toList());

		WorldPoint point = wareHouse.getPoint();
		if (point == null) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_MANOR_VALUE);
			return;
		}

		List<ArmyInfo> armys = new ArrayList<>();
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.WAREHOUSE_GET_VALUE, point.getId(), wareHouse.getEntity().getId(), new EffectParams(req, armys));
		if (march == null) {
			return;
		}

		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(itemInfos);
		march.getMarchEntity().setAwardItems(awardItems);

		// 回复协议
		player.responseSuccess(protocol.getType());

		BehaviorLogger.log4Service(player, Source.GUILD_MANOR,
				Action.WARE_HOUSE_TAKE_MARCH,
				Params.valueOf("march", march), Params.valueOf("resToTake", itemInfos));

	}

	/**
	 * 取款行军到达
	 */
	public void msgTakeMarchReach(IWorldMarch march) {
		GuildManorWarehouse wareHouse = GuildManorService.getInstance().getBuildingByTypeAndIdx(player.getGuildId(), WARE_INDEX, TerritoryType.GUILD_STOREHOUSE);
		if(Objects.isNull(wareHouse)){//退盟了
			march.getMarchEntity().setAwardItems(AwardItems.valueOf());
			// 行军返回
			WorldMarchService.getInstance().onPlayerNoneAction(march, march.getMarchEntity().getReachTime());
			return;
		}
		
		StoreMarchTakeReachMsgInvoker invoker = new StoreMarchTakeReachMsgInvoker(march, wareHouse, player, this);
		GuildManorService.getInstance().dealMsg(MsgId.WARE_HOUSE_TAKE_MARCH_REACH, invoker);

		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(march, march.getMarchEntity().getReachTime());
	}

	/**
	 * 取款行军返回
	 */
	public void msgTakeMarchBack(WorldMarch march) {
		AwardItems awardItems = march.getAwardItems();
		boolean isCallBack = march.getCallBackTime() > 0;
		if (isCallBack || march.getReachTime() == 0) {// 中途召回
			return;
		}

		awardItems.rewardTakeAffectAndPush(player, Action.WARE_HOUSE_TAKE_MARCH_BACK);

		BehaviorLogger.log4Service(player, Source.WORLD_ACTION,
				Action.WARE_HOUSE_TAKE_MARCH_BACK,
				Params.valueOf("march", march), Params.valueOf("restBack", awardItems.getAwardItems()),
				Params.valueOf("isCallBack", isCallBack));
	}

	private ItemInfo rewardItem2ItemInfo(RewardItem rewardItem) {
		ItemInfo itemInfo = new ItemInfo(rewardItem.getItemType(), rewardItem.getItemId(),
				(int) rewardItem.getItemCount());
		return itemInfo;
	}

	/**
	 * 随机宝箱冷却
	 */
	@ProtocolHandler(code = HP.code.RANDOM_BOX_CD_C_VALUE)
	private void onQueryBoxCooling(HawkProtocol protocol) {
		pushBoxCooling();
	}

	public void pushBoxCooling() {
		long cooling = LocalRedis.getInstance().lastOpenRandomChest(player.getId()) + WorldMapConstProperty.getInstance().getRandomBoxGetCd() * 1000;
		HPRandomBoxCooling.Builder resp = HPRandomBoxCooling.newBuilder().setCooldown(cooling);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RANDOM_BOX_CD_S, resp));
	}

	/**
	 * 随机宝箱
	 */
	@ProtocolHandler(code = HP.code.WORLD_RANDOM_BOX_MARCH_C_VALUE)
	private boolean onWorldRandomChestStart(HawkProtocol protocol) {

		long lastOpen = LocalRedis.getInstance().lastOpenRandomChest(player.getId());
		if (HawkTime.getMillisecond() - lastOpen < WorldMapConstProperty.getInstance().getRandomBoxGetCd() * 1000) {
			return false;
		}

		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}

		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		// 验证点信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		if (point == null || (point.getPointType() != WorldPointType.BOX_VALUE)) {
			return false;
		}

		// 怪物配置错误
		WorldEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
		if (cfg == null) {
			return false;
		}

		// 玩家等级不满足
		if (player.getLevel() < cfg.getLowerLimit()) {
			return false;
		}

		WorldMarchType marchType = WorldMarchType.RANDOM_BOX;

		// 开启行军,目标放怪物ID
		String targetId = String.valueOf(point.getMonsterId());
		
		List<ArmyInfo> armys = new ArrayList<>();
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType.getNumber(), point.getId(), targetId, null, 0, new EffectParams(req, armys));

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_FIGHT_MONSTER,
				Params.valueOf("marchData", march));

		// 回复协议
		player.responseSuccess(protocol.getType());

		return true;
	}

	/** 请求行军支援 */
	@ProtocolHandler(code = HP.code.YURI_MONSTER_QUERY_HELP_VALUE)
	private void onYuriMonsterQueryHelp(HawkProtocol protocol) {

		HPYuriMonsterQueryHelpReq req = protocol.parseProtocol(HPYuriMonsterQueryHelpReq.getDefaultInstance());
		// 行军已不存在
		IWorldMarch worldMarch = WorldMarchService.getInstance().getMarch(req.getMarhcId());
		if (worldMarch == null) {
			return;
		}
		
		WorldMarchType marchType = worldMarch.getMarchType();
		NoticeCfgId noticeId = marchType == WorldMarchType.YURI_MONSTER ? Const.NoticeCfgId.YURI_MONSTER_QUERY_HELP : Const.NoticeCfgId.MARCH_QUERY_HELP;
		
		int tx = worldMarch.getTerminalX();
		int ty = worldMarch.getTerminalY();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(tx, ty);
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, noticeId,
				player, tx, ty, point.getPointType());
		
		player.responseSuccess(protocol.getType());

	}

}
