package com.hawk.game.nation.wearhouse.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationConstructionLevelCfg;
import com.hawk.game.config.NationWarehouseShopCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.NationBuildSupportInvoker;
import com.hawk.game.invoker.NationWarehouseDonateInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.NationalConst;
import com.hawk.game.nation.NationalConst.NationalDiamondRecordType;
import com.hawk.game.nation.wearhouse.NationShopRealtimeInfo;
import com.hawk.game.nation.wearhouse.NationalDiamondRecord;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.NationalBuildSupportInfo;
import com.hawk.game.protocol.National.NationalBuildSupportPB;
import com.hawk.game.protocol.National.NationalDiamondRecordData;
import com.hawk.game.protocol.National.NationalDiamondRecordPB;
import com.hawk.game.protocol.National.NationalShopBuyGoodsReq;
import com.hawk.game.protocol.National.NationalStorehouseDonateReq;
import com.hawk.game.protocol.National.NationalSupportDiamondReq;
import com.hawk.game.protocol.National.NationalWareHouseShopItemPB;
import com.hawk.game.protocol.National.NationalWareHouseShopPB;
import com.hawk.game.protocol.National.NationalWarehouseResItemPB;
import com.hawk.game.protocol.National.NationalWarehouseResourcePB;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 国家仓库
 * 
 * @author lating
 *
 */
public class NationalWarehouseModule extends PlayerModule {
	/**
	 * 个人在国家商店购买数据的内存缓存
	 */
	NationShopRealtimeInfo nationShopInfo;
	/**
	 * 上一次tick的时间
	 */
	private long lastTickTime;
	/**
	 * 过期时长:秒
	 */
	static final int NATIONAL_SHOP_EXPIRE_SECOND = 86400 * 7;
	/**
	 * 上一次捐献的时间
	 */
	private long lastDonateTime;
	private int donateTimes;
	
	public NationalWarehouseModule(Player player) {
		super(player);
	}
	
	public boolean onPlayerLogin() {
		if (isNationalWareHouseExist()) {
			reloadNationalShopInfo();
			Map<Integer, Integer> supportTimes = getNationBuildSupportTimes();
			syncNationBuildSupportInfo(supportTimes);
		}
		return true;
	}
	
	public boolean onTick() {
		long now = HawkApp.getInstance().getCurrentTime();
		if (now - lastTickTime < 5000) {
			return true;
		}
		
		long lastTime = lastTickTime;
		lastTickTime = now;
		if (!isNationalWareHouseExist()) {
			return true;
		}
		
		if (nationShopInfo == null) {
			nationShopInfo = NationShopRealtimeInfo.valueOf(now);
			String redisKey = getNationalWarehouseShopKey();
			RedisProxy.getInstance().getRedisSession().setString(redisKey, JSONObject.toJSONString(nationShopInfo), NATIONAL_SHOP_EXPIRE_SECOND);
			syncNationalShopInfo();
		} else {
			if (refreshNationalShopInfo(now)) {
				 syncNationalShopInfo();
			}
		}
		
		// 在线跨天时推送
		if (HawkTime.isCrossDay(lastTime, now, 0)) {
			RedisProxy.getInstance().getRedisSession().del(getNationBuildSupportTimesKey());
			syncNationBuildSupportInfo(Collections.emptyMap());
		}
		
		return true;
	}
	
	/**
	 * 加载国家商店购买数据
	 */
	private void reloadNationalShopInfo() {
		boolean isNewly = false;
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
		if (accountInfo !=null && accountInfo.isNewly()) {
			isNewly = true;
		}
		
		String redisKey = getNationalWarehouseShopKey();
		String shopInfo = null;
		if (!isNewly) {
			shopInfo = RedisProxy.getInstance().getRedisSession().getString(redisKey);
		}
		
		long nowTime = HawkTime.getMillisecond();
		lastTickTime = nowTime;
		if (HawkOSOperator.isEmptyString(shopInfo)) {
			nationShopInfo = NationShopRealtimeInfo.valueOf(nowTime);
			RedisProxy.getInstance().getRedisSession().setString(redisKey, JSONObject.toJSONString(nationShopInfo), NATIONAL_SHOP_EXPIRE_SECOND);
			syncNationalShopInfo();
			return;
		} 
		
		try {
			nationShopInfo = JSONObject.parseObject(shopInfo, NationShopRealtimeInfo.class);
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("national warehouse shop info reload failed, playerId: {}, info: {}", player.getId(), shopInfo);
			nationShopInfo = NationShopRealtimeInfo.valueOf(HawkTime.getMillisecond());
			RedisProxy.getInstance().getRedisSession().setString(redisKey, JSONObject.toJSONString(nationShopInfo), NATIONAL_SHOP_EXPIRE_SECOND);
		}
		
		refreshNationalShopInfo(nowTime);
		syncNationalShopInfo();
	}
	
	/**
	 * 刷新国家商店购买数据
	 * 
	 * @param nowTime
	 * @return
	 */
	private boolean refreshNationalShopInfo(long nowTime) {
		if (!HawkTime.isSameWeek(nationShopInfo.getRefreshTime(), nowTime)) {
			nationShopInfo.setRefreshTime(nowTime);
			nationShopInfo.getShopItemCount().clear();
			String redisKey = getNationalWarehouseShopKey();
			RedisProxy.getInstance().getRedisSession().setString(redisKey, JSONObject.toJSONString(nationShopInfo), NATIONAL_SHOP_EXPIRE_SECOND);
			return true;
		}
		
		return false;
	}
	
	/**
	 *  捐献材料
	 * @param protocol
	 * @return
	 */
	 @ProtocolHandler(code = HP.code2.NATIONAL_WAREHOUSE_DONATE_VALUE)
	 public boolean onNationWarehouseDonate(HawkProtocol protocol) {
		 if (!isNationalWareHouseExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_WAREHOUSE_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 long now = HawkTime.getMillisecond();
		 // 每秒内最多捐献一次
		 if (now - lastDonateTime < 1000) {
			 if (donateTimes >= 5) {
				 sendError(protocol.getType(), Status.Error.NATION_WAREHOUSE_DONATE_FREQUENCY);
				 return false;
			 }
			 donateTimes += 1;
		 } else {
			 lastDonateTime = now;
			 donateTimes = 1;
		 }
		 
		 NationalStorehouseDonateReq req = protocol.parseProtocol(NationalStorehouseDonateReq.getDefaultInstance());
		 int itemId = req.getItemId();
		 long count = req.getCount();
		 if (itemId <= 0 || count <= 0) {
			 sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			 return false;
		 }
		 // 超过上限按上限算
		 if(count > NationConstCfg.getInstance().getWarehouseConsumeLimit()){
			 count = NationConstCfg.getInstance().getWarehouseConsumeLimit();
		 }
		 
		 if (NationConstCfg.getInstance().getWarehouseAwardItems().isEmpty()) {
			 sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			 return false;
		 }
		 
		 // 判断所捐献的是不是那种类型的道具
		 ItemInfo itemInfo = NationConstCfg.getInstance().getWarehouseConsume(itemId);
		 if (itemInfo == null) {
			 sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			 return false;
		 }
		 
		 final long finalCount = count;
		 final int timesCount = (int) (count / itemInfo.getCount());
		 itemInfo.setCount(count);
		 ConsumeItems consume = ConsumeItems.valueOf();
		 consume.addConsumeInfo(itemInfo, false);
		 if (!consume.checkConsume(player, protocol.getType())) {
			 return false;
		 }
		 consume.consumeAndPush(player, Action.NATIONAL_WAREHOUSE_DODATE_CONSUMNE);
		 
		 // 单独的线程添加
		 WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_STOREHOUSE_DONATE) {
			@Override
			public boolean onInvoke() {
				warehosueDonate(itemId, timesCount, finalCount);
				return true;
			}
		});
		 
	     player.responseSuccess(protocol.getType());
		 return true;
	 }
	 
	 /**
	  * 国家仓库捐献
	  * 
	  * @param itemId
	  * @param count
	  */
	 private void warehosueDonate(int itemId, int timesCount, long count) {
		 ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		 if (itemCfg != null && itemCfg.getItemType() == Const.ToolType.NATION_RESOURCE_ITEM_VALUE) {
			 itemId = itemCfg.getAttrType();
			 count = itemCfg.getAttrVal() * count;
			 String serverId = GlobalData.getInstance().getMainServerId(player.getServerId());
			 NationService.getInstance().nationalWarehouseResourceIncrease(itemId, count, serverId);
			 long totalCount = NationService.getInstance().getNationalWarehouseResourse(itemId, serverId);
			 //  材料id、捐献数量、捐献后材料库存数量
			 logNationWarehouseDonate(2, 0, itemId, count, totalCount);
		 } else {
			 HawkLog.errPrintln("nation warehouse donate config error, playerId: {}, itemId: {}, itemType: {}", player.getId(), itemId, itemCfg != null ? itemCfg.getItemType() : 0);
		 }
		 // 获得功绩奖章
		 List<ItemInfo> itemInfos = NationConstCfg.getInstance().getWarehouseAwardItems();
		 itemInfos.stream().forEach(e -> e.setCount(e.getCount() * timesCount));
		 player.dealMsg(MsgId.NATION_WAREHOUSE_DONATE_AWARD, new NationWarehouseDonateInvoker(player, itemInfos));
		 syncWarehouseResource();
	 }
	 
	 /**
	  * 国家捐献
	  * @param type   类型：1-存入国家金条，2-捐献飞船强化材料
	  * @param buildId  建筑ID
	  * @param donateId 金条或捐献材料ID
	  * @param donateCount 捐献的数量
	  * @param afterCount 捐献之后国家库存数量
	  */
	 private void logNationWarehouseDonate(int type, int buildId, int donateId, long donateCount, long afterCount) {
		 try {
			 LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_warehouse_donate);
			 if (logParam != null) {
				 logParam.put("donateType", type)
				 .put("buildId", buildId)
				 .put("donateId", donateId)
				 .put("donateCount", donateCount)
				 .put("totalCount", afterCount);
				 GameLog.getInstance().info(logParam);
			 }
		 } catch (Exception e) {
			 HawkException.catchException(e);
		 }
	 }
	 
	 /**
	  * 资助国家建筑
	  * 
	  * @param protocol
	  * @return
	  */
	 @ProtocolHandler(code = HP.code2.NATIONAL_BUILD_SUPPORT_REQ_VALUE)
	 private boolean onNationalBuildSupport(HawkProtocol protocol) {
		 if (!isNationalWareHouseExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_WAREHOUSE_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 NationalSupportDiamondReq req = protocol.parseProtocol(NationalSupportDiamondReq.getDefaultInstance());
		 int build = req.hasBuild() ? req.getBuild() : 0;
		 if (NationbuildingType.valueOf(build) == null) {
			 sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			 return false;
		 }
		 
		 // 判断当日资助次数是否达到上限
		 int expireSeconds = 0;
		 Map<Integer, Integer> supportTimes = getNationBuildSupportTimes();
		 if (supportTimes.isEmpty()) {
			 expireSeconds = (int)(HawkTime.getNextAM0Date() - HawkTime.getMillisecond()) / 1000;
		 }
		 
		 int times = supportTimes.getOrDefault(build, 0);
		 if (times >= NationConstCfg.getInstance().getSupportLimit()) {
			 sendError(protocol.getType(), Status.Error.NATION_BUILD_SUPPORT_LIMIT);
			 return false;
		 }
		 
		 ConsumeItems consume = ConsumeItems.valueOf();
		 ItemInfo itemInfo = ItemInfo.valueOf(NationConstCfg.getInstance().getSupportCost());
		 consume.addConsumeInfo(itemInfo, false);
		 if (!consume.checkConsume(player, protocol.getType())) {
			 return false;
		 }
		 consume.consumeAndPush(player, Action.NATIONAL_BUILD_SUPPORT_CONSUME);
		 
		 times += 1;
		 supportTimes.put(build, times);
		 RedisProxy.getInstance().getRedisSession().hIncrBy(getNationBuildSupportTimesKey(), String.valueOf(build), 1);
		 if (expireSeconds > 0) {
			 RedisProxy.getInstance().getRedisSession().expire(getNationBuildSupportTimesKey(), expireSeconds); 
		 }
		 
		 HawkLog.logPrintln("nation warehouse support build, playerId: {}, build: {}", player.getId(), build);
		 // 单独的线程添加
		 WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_STOREHOUSE_DONATE) {
			@Override
			public boolean onInvoke() {
				nationalBuildSupport(build, (int) itemInfo.getCount());
				
				syncNationBuildSupportInfo(supportTimes);
				
				NationalBuilding nb = NationService.getInstance().getNationBuildingByTypeId(build);
				String serverId = GlobalData.getInstance().getMainServerId(player.getServerId());
				LogUtil.logNationBuildSupport(player, build, nb.getEntity().getTotalVal(), 
						NationService.getInstance().getNationalWarehouseResourse(PlayerAttr.DIAMOND_VALUE, serverId));
				return true;
			}
		});
		 
		player.responseSuccess(protocol.getType());
		 
		return true;
	 }
	 
	 /**
	  * 获取玩家对国家建筑的当天的资助次数
	  * 
	  * @return
	  */
	 private Map<Integer, Integer> getNationBuildSupportTimes() {
		Map<String, String> supportTimes = RedisProxy.getInstance().getRedisSession().hGetAll(getNationBuildSupportTimesKey());
		if (supportTimes.isEmpty()) {
			return new HashMap<Integer, Integer>();
		}
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (Entry<String, String> entry : supportTimes.entrySet()) {
			map.put(Integer.parseInt(entry.getKey()),Integer.parseInt(entry.getValue()));
		}
		return map;
	 }
	 
	 /**
	  * 国家建筑资助
	  * 
	  * @param build
	  * @param count
	  */
	 private void nationalBuildSupport(int build, int count) {
		String serverId = GlobalData.getInstance().getMainServerId(player.getServerId());
		NationService.getInstance().nationalWarehouseResourceIncrease(PlayerAttr.DIAMOND_VALUE, NationConstCfg.getInstance().getSupportGold(), serverId);
		NationService.getInstance().addNationalDiamondRecord(NationalDiamondRecordType.DONATE, NationConstCfg.getInstance().getSupportGold(), build, player.getId(), player.getName(), serverId);
		NationService.getInstance().addBuildQuestVal(build, NationConstCfg.getInstance().getSupportBuilding(), false);
		// 给玩家发奖
		player.dealMsg(MsgId.NATION_BUILD_SUPPORT_AWARD, new NationBuildSupportInvoker(player));

		syncWarehouseResource();
		
		long totalCount = NationService.getInstance().getNationalWarehouseResourse(PlayerAttr.DIAMOND_VALUE, serverId);
		logNationWarehouseDonate(1, build, PlayerAttr.DIAMOND_VALUE, NationConstCfg.getInstance().getSupportGold(), totalCount);
	 }
	 
	 /**
	  * 请求捐献记录数据
	  * 
	  * @param protocol
	  * @return
	  */
	 @ProtocolHandler(code = HP.code2.NATIONAL_WAREHOUSE_DONATE_RECORD_REQ_VALUE)
	 public boolean onNationStorehouseDonateRecordReq(HawkProtocol protocol) {
		 if (!isNationalWareHouseExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_WAREHOUSE_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 String serverId = GlobalData.getInstance().getMainServerId(player.getServerId());
		 String redisKey = NationService.getInstance().getDonateRecordRedisKey(serverId);
		 List<String> records = RedisProxy.getInstance().getRedisSession().lRange(redisKey, 0, NationService.getInstance().getRecordLimit(), 0);
		 NationalDiamondRecordData.Builder resultList = NationalDiamondRecordData.newBuilder();
		 for (String record : records) {
			 NationalDiamondRecord obj = JSONObject.parseObject(record, NationalDiamondRecord.class);		
			 NationalDiamondRecordPB.Builder builder = obj.toBuilder();
			 resultList.addRecord(builder);
		 }
		 
		 player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_WAREHOUSE_DONATE_RECORD_RESP, resultList));
		 return true;
	 }
	 
	 
	 /**
	  * 国家商店购买道具
	  * 
	  * @param protocol
	  * @return
	  */
	 @ProtocolHandler(code = HP.code2.NATIONAL_WAREHOUSE_SHOP_BUY_VALUE)
	 public boolean onGoodsExchange(HawkProtocol protocol) {
		 if (!isNationalWareHouseExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_WAREHOUSE_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 long nowTime = HawkTime.getMillisecond();
		 if (refreshNationalShopInfo(nowTime)) {
			 sendError(protocol.getType(), Status.Error.NATION_SHOP_PASS_WEEK_VALUE);
			 syncNationalShopInfo();
			 return false;
		 }
		 
		 NationalShopBuyGoodsReq req = protocol.parseProtocol(NationalShopBuyGoodsReq.getDefaultInstance());
		 int shopId = req.getShopId();
		 int count = req.getCount();
		 
		 NationWarehouseShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationWarehouseShopCfg.class, shopId);
		 if (cfg == null || count <= 0) {
			 sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			 return false;
		 }
		 
		 NationMilitaryEntity nationMilitaryEntity = player.getData().getNationMilitaryEntity();
		 int militaryLevel = nationMilitaryEntity.getNationMilitarLlevel();
		 // 判断军衔等级是否达到
		 if (militaryLevel < cfg.getMilitaryLevel()) {
			 sendError(protocol.getType(), Status.Error.NATION_MILITARY_LEVEL_NOT_MATCH);
			 return false;
		 }
		 
		 int alreadyCount = nationShopInfo.getShopItemCount().getOrDefault(shopId, 0);
		 if (alreadyCount + count > cfg.getNum()) {
			 sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			 return false;
		 }
		 
		 List<ItemInfo> itemList = ItemInfo.valueListOf(cfg.getExchangeItem());
		 for (ItemInfo item : itemList) {
			 item.setCount(item.getCount() * count);
		 }
		 ConsumeItems consume = ConsumeItems.valueOf();
		 consume.addConsumeInfo(itemList, false);
		 if (!consume.checkConsume(player, protocol.getType())) {
			 return false;
		 }
		 
		 consume.consumeAndPush(player, Action.NATIONAL_WAREHOUSE_SHOP_COMSUME);
		 HawkLog.logPrintln("nation warehouse shop buy goods, playerId: {}, shopId: {}, count: {}", player.getId(), shopId, count);
		 
		 ItemInfo itemInfo = ItemInfo.valueOf(cfg.getItemId());
		 itemInfo.setCount(itemInfo.getCount() * count);
		 AwardItems awardItem = AwardItems.valueOf();
		 awardItem.addItem(itemInfo);
		 awardItem.rewardTakeAffectAndPush(player, Action.NATIONAL_WAREHOUSE_SHOP_AWARD, true);
		 
		 nationShopInfo.getShopItemCount().put(shopId, alreadyCount + count);
		 String redisKey = getNationalWarehouseShopKey();
		 RedisProxy.getInstance().getRedisSession().setString(redisKey, JSONObject.toJSONString(nationShopInfo));
		 syncNationalShopInfo();
		 
		 LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_shop_exchange);
   		 if (logParam != null) {
   			 logParam.put("militaryLevel", militaryLevel)
   			 .put("goodsId", shopId)
   			 .put("itemId", itemInfo.getItemId())
   			 .put("count", count);
   			 GameLog.getInstance().info(logParam);
   		 }
   		 
		 return true;
	 }
	 
	 
	 /**
	  * 请求国家仓库资源数据
	  * 
	  * @param protocol
	  * @return
	  */
	 @ProtocolHandler(code = HP.code2.NATIONAL_WAREHOUSE_RESOURCE_REQ_VALUE)
	 public boolean onResourceReq(HawkProtocol protocol) {
		 if (!isNationalWareHouseExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_WAREHOUSE_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 syncWarehouseResource();
		 
		 return true;
	 }
	 
	 /**
	  * 请求国家商店信息（正常情况下是用不到的，这里以防万一登录的时候没有推送）
	  * @param protocol
	  * @return
	  */
	 @ProtocolHandler(code = HP.code2.NATIONAL_WAREHOUSE_SHOP_INFO_REQ_VALUE)
	 public boolean onNationalShopInfoReq(HawkProtocol protocol) {
		 if (!isNationalWareHouseExist()) {
			 sendError(protocol.getType(), Status.Error.NATION_WAREHOUSE_NOT_EXIST_VALUE);
			 return false;
		 }
		 
		 syncNationalShopInfo();
		 
		 return true;
	 }
	 
	 
	 /**
	  * 判断国家仓库建筑是否存在
	  * 
	  * @return
	  */
	 private boolean isNationalWareHouseExist() {
		 return getWarehouseBuildCfg() != null;
	 }
	 
	 /**
	  * 获取国家仓库建筑配置信息
	  * 
	  * @return
	  */
	 @SuppressWarnings("deprecation")
	private NationConstructionLevelCfg getWarehouseBuildCfg() {
		NationbuildingType buildType = NationbuildingType.NATION_WEARHOUSE;
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
	  * 同步国家商店信息
	  */
	 private void syncNationalShopInfo() {
		 NationalWareHouseShopPB.Builder builder = NationalWareHouseShopPB.newBuilder();
		 for (Entry<Integer, Integer> entry : nationShopInfo.getShopItemCount().entrySet()) {
			 NationalWareHouseShopItemPB.Builder shopItemBuilder = NationalWareHouseShopItemPB.newBuilder();
			 shopItemBuilder.setShopId(entry.getKey());
			 shopItemBuilder.setCount(entry.getValue());
			 builder.addShopItem(shopItemBuilder);
		 }
		 
		 builder.setRefreshTime(GameUtil.getNextWeekStarttime());
		 player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_WAREHOUSE_SHOP_INFO_SYNC, builder));
	 }
	 
	 /**
	  * 同步国家建筑资助信息
	  * 
	  * @param supportTimes
	  */
	 private void syncNationBuildSupportInfo(Map<Integer, Integer> supportTimes) {
		 NationalBuildSupportInfo.Builder builder = NationalBuildSupportInfo.newBuilder();
		 for (Entry<Integer, Integer> entry : supportTimes.entrySet()) {
			 NationalBuildSupportPB.Builder singleBuilder = NationalBuildSupportPB.newBuilder();
			 singleBuilder.setBuild(entry.getKey());
			 singleBuilder.setTimes(entry.getValue());
			 builder.addSupportInfo(singleBuilder);
		 }
		 player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_BUILD_SUPPORT_INFO_SYNC, builder));
	 }
	 
	 /**
	  * 同步国家仓库中的资源数量
	  */
	 private void syncWarehouseResource() {
		 NationalWarehouseResourcePB.Builder builder = NationalWarehouseResourcePB.newBuilder();
		 builder.setNationalGold(0);
		 String serverId = GlobalData.getInstance().getMainServerId(player.getServerId());
		 Map<Integer, Long> map = NationService.getInstance().getNationalWarehouseResourse(serverId);
		 for (Entry<Integer, Long> entry : map.entrySet()) {
			 if (entry.getKey() == PlayerAttr.DIAMOND_VALUE) {
				 builder.setNationalGold(entry.getValue());
				 continue;
			 }
			 NationalWarehouseResItemPB.Builder resItem = NationalWarehouseResItemPB.newBuilder();
			 resItem.setResourceId(entry.getKey());
			 resItem.setCount(entry.getValue());
			 builder.addResource(resItem);
		 }
		 
		 player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_WAREHOUSE_RESOURCE_SYNC, builder));
	 }
	 
	 private String getNationalWarehouseShopKey() {
		 return NationalConst.NATIONAL_WAREHOUSE_SHOP + ":" + player.getId();
	 }
	 
	 private String getNationBuildSupportTimesKey() {
		 return NationalConst.NATION_BUILD_SUPPORT + ":" + player.getId();
	 }
}
