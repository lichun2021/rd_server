package com.hawk.game.guild.manor.building;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.hawk.game.config.GuildManorWareHouseCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.invoker.WarhouseSendBackMsgInvoker;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManor.GuildBuildingNorStat;
import com.hawk.game.protocol.GuildManor.GuildManorList.Builder;
import com.hawk.game.protocol.GuildManor.GuildWarehouseBase;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.LogConst.GuildAction;

/**
 * 联盟仓库
 * 
 * @author zhenyu.shang
 * @since 2017年7月7日
 */
public class GuildManorWarehouse extends AbstractGuildBuilding {
	public final static int RADIUS = 2;

	public final static String CELL_SPLITER = "|||||";
	public final static String CELL_VALUE_SPLITER = "|";
	/** 玩家存款 */
	Table<String, String, Integer> playerDeposit;

	/**
	 * 总存量
	 * 
	 * @return
	 */
	public long totalDeposit() {
		String itemStr = this.playerDeposit.cellSet().stream()
				.collect(Collectors.groupingBy(Cell::getColumnKey, Collectors.summarizingInt(Cell::getValue)))
				.entrySet()
				.stream()
				.map(e -> Joiner.on("_").join(e.getKey(), e.getValue().getSum()))
				.collect(Collectors.joining(","));
		List<ItemInfo> allStore = ItemInfo.valueListOf(itemStr);
		// 总存量
		long totalWeight = allStore.stream().mapToLong(ItemInfo::weight).sum();
		return totalWeight;
	}
	
	public GuildManorWareHouseCfg getCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(GuildManorWareHouseCfg.class, getEntity().getBuildingId());
	}

	public GuildManorWarehouse(GuildBuildingEntity entity, TerritoryType buildType) {
		super(entity, buildType);
	}

	@Override
	public void addProtocol2Builder(Builder builder) {
		GuildWarehouseBase.Builder houseBuilder = GuildWarehouseBase.newBuilder();
		houseBuilder.setBuildLife((int) getEntity().getBuildLife());
		houseBuilder.setLevel(getEntity().getLevel());
		houseBuilder.setOverTime(getOverTime());
		houseBuilder.setStat(GuildBuildingNorStat.valueOf(getEntity().getBuildingStat()));
		houseBuilder.setX(getEntity().getPosX());
		houseBuilder.setY(getEntity().getPosY());
		houseBuilder.setResouceNum(totalDeposit());
		builder.setAllWarehouse(houseBuilder.build());
	}

	@Override
	public void parseBuildingParam(String buildParam) {
		playerDeposit = ConcurrentHashTable.create();
		if (HawkOSOperator.isEmptyString(buildParam)) {
			return;
		}
		Splitter.on(CELL_SPLITER)
				.trimResults()
				.omitEmptyStrings()
				.split(buildParam)
				.forEach(s -> {
					List<String> arr = Splitter.on(CELL_VALUE_SPLITER).splitToList(s);
					playerDeposit.put(arr.get(0), arr.get(1), Integer.valueOf(arr.get(2)));
				});

	}

	@Override
	public String genBuildingParamStr() {
		String str = playerDeposit.cellSet()
				.stream()
				.map(c -> Joiner.on(CELL_VALUE_SPLITER).join(c.getRowKey(), c.getColumnKey(), c.getValue()))
				.collect(Collectors.joining(CELL_SPLITER));

		return str;
	}

	/**
	 * 存放
	 * 
	 * @param player
	 * @param awardItems
	 * @return 剩余资源,超出存储上限的总分
	 */
	public List<ItemInfo> store(Player player, List<ItemInfo> list) {
		GuildManorWareHouseCfg cfg = getCfg();
		// 今日已存
		int todayWeight = LocalRedis.getInstance().warehouseStoreCount(player.getId());
		int effPer = player.getEffect().getEffVal(EffType.GUILD_WEARHOUSE_LIMIT);
		int effVal = player.getEffect().getEffVal(EffType.GUILD_WEARHOUSE_LIMIT_VAL);
		int daymax = (int) (cfg.getEverydayUpLimit() * (1 + effPer * GsConst.EFF_PER) + effVal);
		int max = cfg.getSaveUpLimit();

		String itemStr = playerDeposit.row(player.getId()).entrySet().stream()
				.map(e -> Joiner.on("_").join(e.getKey(), e.getValue()))
				.collect(Collectors.joining(","));
		List<ItemInfo> allStore = ItemInfo.valueListOf(itemStr);
		// 总存量
		int totalWeight = (int)allStore.stream().mapToLong(ItemInfo::weight).sum();
		// 最大可存入
		int maxStore = Math.min(daymax - todayWeight, max - totalWeight);

		// 本次存量
		int storeWeight = 0;

		String row = player.getId();
		// 退回的
		List<ItemInfo> takeBack = new ArrayList<>(list.size());

		for (ItemInfo item : list) {
			int typeWeight = WorldMarchConstProperty.getInstance().getResWeightByType(item.getItemId());
			int maxStoreCount = Math.min((maxStore - storeWeight) / typeWeight, (int)item.getCount()); // 最大可存数量
			maxStoreCount = Math.max(0, maxStoreCount);
			if (maxStoreCount < item.getCount()) {
				ItemInfo back = item.clone();
				back.setCount(item.getCount() - maxStoreCount);
				takeBack.add(back);
			}

			String col = String.format("%d_%d", item.getType(), item.getItemId());

			int historyStore = 0; // 历史存数
			if (playerDeposit.contains(row, col)) {
				historyStore = playerDeposit.get(row, col);
			}

			playerDeposit.put(row, col, historyStore + maxStoreCount);
			storeWeight += maxStoreCount * typeWeight;
		}
		// 记录今日存量
		LocalRedis.getInstance().incrementWarehouseStoreCount(player.getId(), storeWeight);

		if (storeWeight <= 0) {
			return takeBack;
		}
		this.getEntity().setChanged(true);
		if (getBuildStat().equals(GuildBuildingStat.COMPELETE)) {
			// 修改仓库状态为存储中
			tryEnterState(GuildBuildingStat.COLLECT.getIndex());
			// 广播状态变化
			WorldPointService.getInstance().notifyPointUpdate(getEntity().getPosX(), getEntity().getPosY());
			GuildManorService.getInstance().broadcastGuildBuilding(getGuildId());
		}
		return takeBack;
	}

	/**
	 * 取款
	 * 
	 * @param player
	 * @param list
	 * @return
	 */
	public List<ItemInfo> take(Player player, List<ItemInfo> list) {
		List<ItemInfo> takeList = new ArrayList<>(list.size());
		for (ItemInfo item : list) {
			String row = player.getId();
			String col = String.format("%d_%d", item.getType(), item.getItemId());
			if (playerDeposit.contains(row, col)) {
				Integer num = playerDeposit.get(row, col);
				ItemInfo take = item.clone();
				int takeOver = Math.min(num, (int)take.getCount());
				take.setCount(takeOver);
				playerDeposit.put(row, col, num - takeOver);
				takeList.add(take);
			}
		}

		return takeList;
	}

	@Override
	public int getBuildingUpLimit() {
		return getCfg().getBuildingUpLimit();
	}

	public Table<String, String, Integer> getPlayerDeposit() {
		return ImmutableTable.copyOf(playerDeposit);
	}

	public void setPlayerDeposit(Table<String, String, Integer> playerDeposit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onBuildRemove() {
		this.playerDeposit.rowKeySet().stream()
				.forEach(playerId -> sendResBack(playerId));
		this.playerDeposit.clear();
		LogUtil.logGuildAction(GuildAction.GUILD_WAREHOUSE_REMOVE, getGuildId());
		super.onBuildRemove();
		return true;
	}
	
	@Override
	public boolean onBuildComplete() {
		LogUtil.logGuildAction(GuildAction.GUILD_BUILD_WAREHOUSE_COMPLETE, getGuildId());
		return true;
	}

	@Override
	public void doQuitGuild(String playerId) {
		this.sendResBack(playerId);
	}

	/**
	 * 返还玩家全部存款
	 * 
	 * @param playerId
	 */
	private void sendResBack(String playerId) {
		if(!this.playerDeposit.containsRow(playerId)){
			return;
		}
		String res = playerDeposit(playerId);
		this.playerDeposit.row(playerId).clear();
		this.getEntity().setChanged(true);

		Player tarPlayer = GlobalData.getInstance().makesurePlayer(playerId);
		tarPlayer.dealMsg(MsgId.WAREHOUSE_SEND_BACK, new WarhouseSendBackMsgInvoker(tarPlayer, res));
	}

	/**玩家存款*/
	public String playerDeposit(String playerId) {
		String res = this.playerDeposit.row(playerId).entrySet().stream()
				.map(e -> e.getKey() + "_" + e.getValue())
				.collect(Collectors.joining(","));
		return res;
	}

	@Override
	public void heartBeats(long interval) {
		// 有存储资源的玩家数量
		long unEmptyCnt = this.playerDeposit.cellSet().stream()
				.filter(cell -> cell.getValue() > 0)
				.count();
		if (unEmptyCnt == 0 && this.getBuildStat() == GuildBuildingStat.COLLECT) {
			tryEnterState(GuildBuildingStat.COMPELETE.getIndex());
			// 广播状态变化
			WorldPointService.getInstance().notifyPointUpdate(getEntity().getPosX(), getEntity().getPosY());
			GuildManorService.getInstance().broadcastGuildBuilding(getGuildId());
			this.getEntity().setChanged(true);
		}
	}
	
	@Override
	public int getbuildLimtUp() {
		return getCfg().getBuildingUpLimit();
	}
}
