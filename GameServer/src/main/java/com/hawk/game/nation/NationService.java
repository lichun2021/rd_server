package com.hawk.game.nation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Table;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationConstructionLevelCfg;
import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.nation.NationalConst.NationalDiamondRecordType;
import com.hawk.game.nation.construction.model.NationalDonatModel;
import com.hawk.game.nation.ship.NationShipFactory;
import com.hawk.game.nation.wearhouse.NationalDiamondRecord;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NationalResAttr;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.ResourceZone;
import com.hawk.game.protocol.Item.HPItemTipsResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.NationBaseInfoPB;
import com.hawk.game.protocol.National.NationBuildingDetail;
import com.hawk.game.protocol.National.NationBuildingInfoPB;
import com.hawk.game.protocol.National.NationBuildingState;
import com.hawk.game.protocol.National.NationStatus;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 国家服务类
 * @author zhenyu.shang
 * @since 2022年3月22日
 */
public class NationService extends HawkAppObj {
	
	/**
	 * 日志记录器
	 */
	public static final Logger logger = LoggerFactory.getLogger("Server");
	
	/** 当前全局国家状态 */
	private NationStatus nationStatus;
	
	/** 3次盟总归属同一个联盟 */
	private int president3condition;
	
	/** 上次王战联盟id */
	private String lastPresidentGuildId;
	
	/** 攻占他国盟总 */
	private int attackOtherPresident;
	
	/** 当前开服天数 */
	private int currentOpenDays;
	
	/** 当前重建值 */
	private int rebuildingLife;
	
	/** 所有国家建筑 */
	private Map<Integer, NationalBuilding> allNationalBuilding;
	
	/** 阶段二捐献信息 */
	private Map<String, NationalDonatModel> allDonateInfo;
	/**
	 * 国家建筑等级缓存（针对跨服玩家）
	 */
	private Table<String, Integer, NationBuildLevelCacheObject> buildLevelCache;
	
	/**
	 * 全局实例对象
	 */
	private static NationService instance = null;
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static NationService getInstance() {
		return instance;
	}

	public NationService(HawkXID xid) {
		super(xid);
		instance = this;
		
		president3condition = 1;
		attackOtherPresident = 0;
		nationStatus = NationStatus.UNOPEN;
		allNationalBuilding = new ConcurrentHashMap<Integer, NationalBuilding>();
		allDonateInfo = new ConcurrentHashMap<String, NationalDonatModel>();
		buildLevelCache = ConcurrentHashTable.create();
	}
	
	/**
	 * 初始化国家系统 （需要放在国王战初始化完成之后）
	 * @return
	 */
	public boolean init() {
		// 判断国家系统是否对外开放
		if(NationConstCfg.getInstance().getNationalOpen() == 0) {
			return true;
		}
		//1. 计算国家当前状态是否开启
		// 先判断开服时间 如果开服时间到120天，那么不需要再去判断，直接进入阶段2
		this.currentOpenDays = GameUtil.getServerOpenDays();
		if (currentOpenDays >= NationConstCfg.getInstance().getNationOpenDelay()) {
			this.nationStatus = NationStatus.INITING;
		} else {
			// 判断是否已经打过国王战
			if(PresidentFightService.getInstance().getPresidentCity().isOpenedPresident()) {
				this.nationStatus = NationStatus.OPEN;
				// 这里计算开启条件
				//1. 本国家 连续三次盟军总部归属同一个联盟（系统上线的时候，默认计数为1/3）
				//2. 本国家 航海远征活动，攻占他国盟总（系统上线的时候，默认计数为0/1）
				//3. 本国家（物理主服）开启时间120+（系统上线的时候，默认计数为：当前服务器开启天数/120）
				
				// 读取上次盟总联盟id
				String lastPresidentGuildId = LocalRedis.getInstance().getNationalDataByKey("lastPresidentGuildId", String.class);
				if(lastPresidentGuildId != null) {
					this.lastPresidentGuildId = lastPresidentGuildId;
				} else {
					// 首次直接存入上次盟总id
					this.setLastPresidentGuildId(PresidentFightService.getInstance().getPresidentGuildId());
				}
				
				// 从redis加载次数信息
				Integer p3 = LocalRedis.getInstance().getNationalDataByKey("president3condition", Integer.class);
				if(p3 != null) {
					this.president3condition = p3;
				} else {
					p3 = 1;
				}
				
				Integer aop = LocalRedis.getInstance().getNationalDataByKey("attackOtherPresident", Integer.class);
				if(aop != null) {
					this.attackOtherPresident = aop;
				} else {
					aop = 0;
				}
				// 两个条件达成任何一个，直接改变状态
				if(p3 >= 3 || aop > 0){
					this.nationStatus = NationStatus.INITING;
				}
			}
		}
		
		// 这里判断一下是否是INITING状态，如果是INIT状态，则需要加载捐献信息
		if(this.nationStatus == NationStatus.INITING) {
			allDonateInfo = LocalRedis.getInstance().getAllNationalDonateInfo();
		}
		
		// 读取重建值
		Integer rbf = LocalRedis.getInstance().getNationalDataByKey("rebuildingLife", Integer.class);
		if(rbf != null) {
			this.rebuildingLife = rbf;
			if(this.rebuildingLife >= NationConstCfg.getInstance().getRebuildingLimit()){
				// 修改状态为重建中，（这里不走升级逻辑，因为建设处本身初始化的时候就已经加载了状态）
				this.nationStatus = NationStatus.REBUILDING;
			}
		}
		
		//2. 加载各个建筑建筑值，并且初始化
		List<NationConstructionEntity> dbNationBuildingList = HawkDBManager.getInstance().query("from NationConstructionEntity where invalid = 0");
		// 首次没有建筑entity
		if(dbNationBuildingList == null || dbNationBuildingList.isEmpty()){
			dbNationBuildingList = new ArrayList<NationConstructionEntity>();
			// 遍历8个建筑
			for (NationbuildingType nationType : NationbuildingType.values()) {
				NationConstructionEntity entity = createNewBuildEntity(nationType);
				dbNationBuildingList.add(entity);
			}
		} 
		for (NationConstructionEntity nationConstructionEntity : dbNationBuildingList) {
			// 初始化
			NationalBuilding building = NationalBuilding.createNationalBuilding(nationConstructionEntity, NationbuildingType.valueOf(nationConstructionEntity.getBuildingId()));
			building.init();
			
			// 存入redis，用于跨服
			RedisProxy.getInstance().updateNationBuildLvl(building.getBuildType().getNumber(), building.getLevel());
			
			allNationalBuilding.put(nationConstructionEntity.getBuildingId(), building);
		}
		
		// 只要进入了第二阶段就添加世界点
		if(this.nationStatus != NationStatus.UNOPEN && this.nationStatus != NationStatus.OPEN) {
			initNationalBuildingPoint(true);
		}
		
		// 启动后第一次记录一下日志
		LogUtil.logNationStatusChange(this.nationStatus.getNumber());
		
		//3. 初始化tick
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(1000) {
			@Override
			public void onPeriodTick() {
				for (NationalBuilding nb : allNationalBuilding.values()) {
					try {
						nb.tick();
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			}
		});
		
		logger.info("nation server init over, current status : {}", this.nationStatus);
		return true;
	}
	
	/**
	 * 状态开启后，添加世界点
	 */
	public void initNationalBuildingPoint(boolean init){
		// 国家建筑加入世界点
		for (Entry<Integer, Integer> pointEntry : getAllNationalPoint().entrySet()) {
			int[] pos = GameUtil.splitXAndY(pointEntry.getKey());
			
			WorldPoint current = WorldPointService.getInstance().getWorldPoint(pos[0], pos[1]);
			if(current == null) {
				WorldPoint point = new WorldPoint(pos[0], pos[1], WorldUtil.getAreaId(pos[0], pos[1]), ResourceZone.ZONE_BLACK_VALUE, WorldPointType.NATIONAL_BUILDING_POINT_VALUE);
				WorldPointService.getInstance().addPoint(point);
			}
			// 通知更新
			if(!init){
				WorldPointService.getInstance().notifyPointUpdate(pos[0], pos[1]);
			}
		}
	}
	
	/**
	 * 检查开服时间
	 */
	public void checkOpenTime() {
		if(this.nationStatus == NationStatus.OPEN){
			int thisdays = GameUtil.getServerOpenDays();
			if(thisdays != currentOpenDays){
				this.currentOpenDays = thisdays;
				logger.info("check nation OpenTime, current open days:{}", thisdays);
				// 如果当前开服时间大于要求时间，通知状态改变
				if (currentOpenDays >= NationConstCfg.getInstance().getNationOpenDelay()) {
					changeStatusNotify(NationStatus.INITING, "Open server time : " + currentOpenDays);
					return;
				}
				// 跨天通知状态改变
				boardcastNationalStatus();
			}
		}
	}

	public void changeStatusNotify(NationStatus status, String reason) {
		this.nationStatus = status;
		// 首次开启后，添加世界点
		if(this.nationStatus == NationStatus.INITING){
			this.initNationalBuildingPoint(false);
		}
		// 全服通知状态改变
		boardcastNationalStatus();
		
		LogUtil.logNationStatusChange(this.nationStatus.getNumber());
		
		logger.info("nation server change over, current status : {}, reason : {}", this.nationStatus, reason);
	}
	
	/**
	 * 广播整体状态
	 */
	public void boardcastNationalStatus() {
		NationBaseInfoPB.Builder builder = makeNationalStatusPB();
		// 全服通知状态改变
		Set<Player> playerSet = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : playerSet) {
			if(player.isCsPlayer()) {
				builder.setCrossNation(true);
			}
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_INFO_SYNC_VALUE, makeNationalStatusPB()));
		}
	}
	
	
	private NationConstructionEntity createNewBuildEntity(NationbuildingType nationType) {
		NationConstructionEntity entity = new NationConstructionEntity();
		entity.setBuildingId(nationType.getNumber());
		entity.setBuildingStatus(NationBuildingState.INCOMPLETE_VALUE);
		entity.setBuildVal(0);
		entity.setLevel(0);
		entity.setTotalVal(0);
		entity.setBuildTime(0L);
		
		if (!HawkDBManager.getInstance().create(entity)) {
			throw new RuntimeException("create nation building error, type=" + nationType);
		}
		return entity;
	}
	
	/**
	 * 增加捐献值
	 * @param addRebuilding
	 */
	public void addAndCheckRebuilding(){
		this.rebuildingLife += NationConstCfg.getInstance().getRebuildVal();
		// 重建状态时并且建设值够了，直接进入建设处重建
		if(this.nationStatus == NationStatus.INITING && this.rebuildingLife >= NationConstCfg.getInstance().getRebuildingLimit()){
			// 修改状态为重建中
			this.nationStatus = NationStatus.REBUILDING;
			
			LogUtil.logNationStatusChange(this.nationStatus.getNumber());
			
			logger.info("nation server change over, current status : {}, reason : {}", this.nationStatus, "addAndCheckRebuilding");
			// 建设处开始建设
			NationalBuilding construction = allNationalBuilding.get(NationbuildingType.NATION_BUILDING_CENTER_VALUE);
			construction.startlevelup();
		}
		// 更新数据
		LocalRedis.getInstance().updateNationalDataByKey("rebuildingLife", rebuildingLife);
	}
	
	/**
	 * 重置每日建设值
	 */
	public void resetNationBuildVal(){
		// 如果没有重建完成不需要执行
		if(this.nationStatus != NationStatus.COMPLETE){
			return;
		}
		for (NationalBuilding building : allNationalBuilding.values()) {
			building.resetBuildVal();
		}
		// 主动推送给前端
		for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
			NationBuildingInfoPB.Builder builder = NationBuildingInfoPB.newBuilder();
			builder.addAllBuildings(NationService.getInstance().makeAllBuildingPBBuilder());
			// 发送建筑列表信息
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_BUILDING_INFO_SYNC_VALUE, builder));
		}
	}
	
	/**
	 * 重置飞船每日上限
	 */
	public void resetShipAssistLimit() {
		// 如果没有重建完成不需要执行
		if(this.nationStatus != NationStatus.COMPLETE){
			return;
		}
		NationShipFactory shipFactory = (NationShipFactory) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_SHIP_FACTORY);
		shipFactory.setDayAssistTotalTime(0);
	}
	
	/**
	 * 增加建设值 （世界线程调用）
	 */
	public void addBuildQuestVal(int buildId, int val, boolean addDayVal){
		NationalBuilding building = this.allNationalBuilding.get(buildId);
		if(building != null){
			building.addBuildingVal(val, addDayVal);
		}
	}
	
	/**
	 * 攻占他服盟总后，条件达成
	 */
	public void checkAndUpdateNationStatus(){
		logger.info("start change cross president win over..., current status:{}", this.nationStatus);
		if(this.nationStatus == NationStatus.OPEN){
			changeStatusNotify(NationStatus.INITING, "attack other server president");
			this.setAttackOtherPresident(1);
		}
	}
	
	/**
	 * 更新盟总信息
	 */
	public void checkAndUpatePresident3(String guildId) {
		// 判断国家系统是否对外开放
		if(NationConstCfg.getInstance().getNationalOpen() == 0) {
			return;
		}
		boolean change = false;
		String reason = "";
		// 如果当前是未开启状态，全服通知开启国家系统
		if(this.nationStatus == NationStatus.UNOPEN){
			this.nationStatus = NationStatus.OPEN;
			this.setLastPresidentGuildId(guildId); // 存入上次联盟id
			change = true;
			
			reason = "first president over";
		} else if(this.nationStatus == NationStatus.OPEN) {
			if(guildId.equals(this.lastPresidentGuildId)){
				this.setPresident3condition(president3condition + 1);
				if(this.president3condition >= 3){
					this.nationStatus = NationStatus.INITING;
					this.initNationalBuildingPoint(false);
					change = true;
				}
				reason = "continue president over";
			} else {
				// 降为1次重新计算
				reason = "break president over, lastGuildId=" + lastPresidentGuildId;
				this.setPresident3condition(1);
				this.setLastPresidentGuildId(guildId); // 重新存入
			}
		}
		
		if(change) {
			LogUtil.logNationStatusChange(this.nationStatus.getNumber());
		}
		// 全服通知状态改变
		boardcastNationalStatus();
		
		logger.info("nation server change over, current status : {}, reason : {}, notify : {}, thisGuildId:{}, president3condition:{}", 
				this.nationStatus, reason, change, guildId, president3condition);
	}
	
	
	/**
	 * 获取玩家的捐献信息
	 * @param playerId
	 * @return
	 */
	public NationalDonatModel getPlayerDonatInfo(String playerId){
		NationalDonatModel model = allDonateInfo.get(playerId);
		// 首次捐献
		if(model == null){
			model = new NationalDonatModel();
			model.setPlayerId(playerId);
			model.setLeftcount(NationConstCfg.getInstance().getRebuildingCountLimit());
			model.setResumeTime(0);
			
			allDonateInfo.put(playerId, model);
		}
		return model;
	}

	public NationStatus getNationStatus() {
		return nationStatus;
	}

	public void setNationStatus(NationStatus nationStatus, String reason) {
		this.nationStatus = nationStatus;
		LogUtil.logNationStatusChange(this.nationStatus.getNumber());
		
		logger.info("nation server change over, current status : {}, reason : {}", this.nationStatus, reason);
	}

	public int getRebuildingLife() {
		return rebuildingLife;
	}

	public void setRebuildingLife(int rebuildingLife) {
		this.rebuildingLife = rebuildingLife;
	}

	public void setPresident3condition(int president3condition) {
		this.president3condition = president3condition;
		LocalRedis.getInstance().updateNationalDataByKey("president3condition", president3condition);
	}

	public void setAttackOtherPresident(int attackOtherPresident) {
		this.attackOtherPresident = attackOtherPresident;
		LocalRedis.getInstance().updateNationalDataByKey("attackOtherPresident", attackOtherPresident);
	}

	public String getLastPresidentGuildId() {
		return lastPresidentGuildId;
	}

	public void setLastPresidentGuildId(String lastPresidentGuildId) {
		this.lastPresidentGuildId = lastPresidentGuildId;
		if(lastPresidentGuildId != null) {
			LocalRedis.getInstance().updateNationalDataByKey("lastPresidentGuildId", lastPresidentGuildId);
		}
	}

	public int getPresident3condition() {
		return president3condition;
	}

	public int getAttackOtherPresident() {
		return attackOtherPresident;
	}

	public Map<Integer, NationalBuilding> getAllNationalBuilding() {
		return allNationalBuilding;
	}
	
	public NationalBuilding getNationBuildingByType(NationbuildingType type){
		return allNationalBuilding.get(type.getNumber());
	}
	
	public NationalBuilding getNationBuildingByTypeId(int type){
		return allNationalBuilding.get(type);
	}
	
	public NationBaseInfoPB.Builder makeNationalStatusPB() {
		NationBaseInfoPB.Builder builder = NationBaseInfoPB.newBuilder();
		builder.setNationStatus(nationStatus);
		// 只有这个阶段需要这三个参数
		if(nationStatus == NationStatus.OPEN) {
			builder.setCond1(president3condition);
			builder.setCond2(attackOtherPresident);
			builder.setCond3(currentOpenDays);
		}
		
		NationalBuilding warehouse = this.allNationalBuilding.get(NationbuildingType.NATION_WEARHOUSE_VALUE);
		builder.setWarehouseOver(warehouse.getLevel() > 0);
		
		return builder;
	}
	
	/**
	 * 获取建筑详细信息列表
	 * @return
	 */
	public List<NationBuildingDetail> makeAllBuildingPBBuilder(){
		List<NationBuildingDetail> list = new ArrayList<>();
		for (NationalBuilding building : allNationalBuilding.values()) {
			list.add(building.toBuilder().build());
		}
		return list;
	}
	
	/**
	 * 根据坐标获取建筑类型 
	 * @param point
	 * @return
	 */
	public NationalBuilding getNationalBuildingByPoint(int point) {
		Integer buildType = AssembleDataManager.getInstance().getNationalBuildingPoint().get(point);
		if(buildType != null){
			return this.allNationalBuilding.get(buildType);
		}
		return null;
	}
	
	/**
	 * 获取所有建筑点列表
	 * @param point
	 * @return
	 */
	public Map<Integer, Integer> getAllNationalPoint() {
		return AssembleDataManager.getInstance().getNationalBuildingPoint();
	}
	
	/**
	 * 停服处理
	 */
	public void onShutdown() {
		for (NationalBuilding nb : allNationalBuilding.values()) {
			try {
				nb.onShutdown();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 获取国家仓库中特定种类的资源数量
	 * 
	 * @param resourceItemId 资源ID（如果是库存金条，对应PlayerAttr.DIAMOND_VALUE）
	 * @return
	 */
	public long getNationalWarehouseResourse(int resourceItemId, String serverId) {
		 String redisKey = NationService.getInstance().getDonateRedisKey(serverId);
		 String count = RedisProxy.getInstance().getRedisSession().hGet(redisKey, String.valueOf(resourceItemId));
		 if (HawkOSOperator.isEmptyString(count)) {
			 return 0;
		 }
		 
		 return Long.parseLong(count);
	}
	
	/**
	 * 获取国家仓库中所有资源的数量（包括国家库存金条的数量）
	 * 
	 * @return
	 */
	public Map<Integer, Long> getNationalWarehouseResourse(String serverId) {
		 String redisKey = NationService.getInstance().getDonateRedisKey(serverId);
		 Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(redisKey);
		 if (map.isEmpty()) {
			 return Collections.emptyMap();
		 }
		 
		 Map<Integer, Long> returnMap = new HashMap<Integer, Long>();
		 for (Entry<String, String> entry : map.entrySet()) {
			 returnMap.put(Integer.parseInt(entry.getKey()), Long.parseLong(entry.getValue()));
		 }
		 
		 return returnMap;
	}
	
	/**
	 * 消耗国家仓库中的资源（包括国家库存金条）, 需要在世界线程调用
	 * 
	 * @param resourceItemId 
	 * @param count
	 * @param buildType 消耗在哪个建筑上
	 * @return
	 */
	public boolean nationalWarehouseResourceConsume(int resourceItemId, long count, NationbuildingType buildType, String playerId, String playerName, String serverId) {
		boolean success = nationalWarehouseResourceConsume(resourceItemId, count, serverId);
		if (success && resourceItemId == PlayerAttr.DIAMOND_VALUE) {
			addNationalDiamondRecord(NationalDiamondRecordType.CONSUME, count, buildType.getNumber(), playerId, playerName, serverId);
		}
		return success;
	}
	
	/**
	 * 消耗国家仓库中的资源（包括国家库存金条）, 需要在世界线程调用
	 * 
	 * @param resourceItemId
	 * @param count
	 * @return
	 */
	public boolean nationalWarehouseResourceConsume(int resourceItemId, long count, String serverId) {
		long oldCount = getNationalWarehouseResourse(resourceItemId, serverId);
		if (oldCount < count) {
			return false;
		}
		
		 String redisKey = NationService.getInstance().getDonateRedisKey(serverId);
		 if (count <= Integer.MAX_VALUE) {
			 RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(resourceItemId), (int) (0 - count));
		 } else {
			 RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(resourceItemId), (int) (0 - Integer.MAX_VALUE));
			 RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(resourceItemId), (int) (Integer.MAX_VALUE - count));
		 }
		 
		 return true;
	}
	
	/**
	 * 消耗国家仓库中的资源（包括国家库存金条）, 需要在世界线程调用
	 * 
	 * @param resourceItems
	 * @return
	 */
	public boolean nationalWarehouseResourceConsume(List<ItemInfo> resourceItems, NationbuildingType buildType, String playerId, String playerName, String serverId) {
		boolean success = nationalWarehouseResourceConsume(resourceItems, serverId);
		if (success) {
			 for (ItemInfo itemInfo : resourceItems) {
				 if (itemInfo.getItemId() == PlayerAttr.DIAMOND_VALUE) {
					 addNationalDiamondRecord(NationalDiamondRecordType.CONSUME, itemInfo.getCount(), buildType.getNumber(), playerId, playerName, serverId);
				 }
			 }
		}
		
		return success;
	}
	
	/**
	 * 消耗国家仓库中的资源（包括国家库存金条）, 需要在世界线程调用
	 * 
	 * @param resourceItems
	 * @return
	 */
	public boolean nationalWarehouseResourceConsume(List<ItemInfo> resourceItems, String serverId) {
		 // 所传参数不对
		 if (resourceItems == null) {
			 return true;
		 } 
		 
		 // 表示没有消耗
		 if (resourceItems.isEmpty()) {
			 return true;
		 }
		
		 Map<Integer, Long> map = getNationalWarehouseResourse(serverId);
		 String redisKey = NationService.getInstance().getDonateRedisKey(serverId);
		 for (ItemInfo itemInfo : resourceItems) {
			 if (itemInfo.getCount() > map.getOrDefault(itemInfo.getItemId(), 0L)) {
				 return false;
			 }
		 }
		 
		 for (ItemInfo itemInfo : resourceItems) {
			 if (itemInfo.getCount() <= Integer.MAX_VALUE) {
				 RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(itemInfo.getItemId()), (int) (0 - itemInfo.getCount()));
				 continue;
			 }
			 
			 RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(itemInfo.getItemId()), (int) (0 - Integer.MAX_VALUE));
			 RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(itemInfo.getItemId()), (int) (Integer.MAX_VALUE - itemInfo.getCount()));
		 }
		 
		 return true;
	}
	
	/**
	 * 往国家仓库添加资源（包括国家金条）, 需要在世界线程调用
	 * 
	 * @param resourceItemId
	 * @param count
	 * @return
	 */
	public boolean nationalWarehouseResourceIncrease(int resourceItemId, long count, String serverId) {
		// 不是国家仓库资源类型
		if (resourceItemId != PlayerAttr.DIAMOND_VALUE && NationalResAttr.valueOf(resourceItemId) == null) {
			return false;
		}
	
		String redisKey = NationService.getInstance().getDonateRedisKey(serverId);
		if (count <= Integer.MAX_VALUE) {
			RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(resourceItemId), (int)count);
		} else {
			 RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(resourceItemId), Integer.MAX_VALUE);
			 RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(resourceItemId), (int) (count - Integer.MAX_VALUE));
		}
		
		return true;
	}
	
	/**
	 * 国家仓库资源被掠夺 ，需要在世界线程调用
	 * 
	 * @return 返回的是掠夺方从被掠夺方那拉取的资源数量
	 */
	public Map<Integer, Long> warehouseResourcePlunder(String serverId) {
		if (HawkOSOperator.isEmptyString(serverId)) {
			HawkLog.errPrintln("warehouseResourcePlunder failed, serverId: {}", serverId);
			return Collections.emptyMap();
		}
		
		serverId = GlobalData.getInstance().getMainServerId(serverId);
		NationbuildingType buildType = NationbuildingType.NATION_WEARHOUSE;
		NationConstructionLevelCfg cfg = null;
		if (GsConfig.getInstance().getServerId().equals(serverId)) {
			NationalBuilding building = getNationBuildingByType(buildType);
			if (building != null) {
				cfg = building.getCurrentLevelCfg();
			}
		} else {
			int level = getBuildLevel(serverId, buildType.getNumber());
			int baseId = buildType.getNumber() * 100 + level;
			cfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionLevelCfg.class, baseId);
		}
		
		int safeResource = 0;
		if (cfg != null) {
			safeResource = cfg.getSafeResource();
		} else {
			HawkLog.errPrintln("warehouseResourcePlunder failed, nation warehouse building null, serverId: {}", serverId);
		}
    	
    	Map<Integer, Long> plunderResourceMap = new HashMap<Integer, Long>();
    	Map<Integer, Long> totalResourceMap = getNationalWarehouseResourse(serverId);
    	for (Entry<Integer, Long> entry : totalResourceMap.entrySet()) {
    		if (entry.getKey() == PlayerAttr.DIAMOND_VALUE) {
    			continue;
    		}
    		
    		long plunderNum = (long) Math.floor(entry.getValue() * (1 - safeResource * 1D / 10000));
    		plunderResourceMap.put(entry.getKey(), plunderNum);
    	}
    	
		return plunderResourceMap;
	}
	
    /**
	  * 添加国家库存金条数量变动记录, 需要在世界线程调用
	  * 
	  * @param count 捐赠或支出金条数量
	  * @param build 捐赠或支出针对的建筑类型
	  * @param playerId 完成捐赠或支出的玩家ID
	  * @param playerName 完成捐赠或支出的玩家名字
	  */
	 public void addNationalDiamondRecord(NationalDiamondRecordType type, long count, int build, String playerId, String playerName, String serverId) {
		 String redisKey = getDonateRecordRedisKey(serverId);
		 NationalDiamondRecord record = NationalDiamondRecord.valueOf(type.intVal(), HawkTime.getMillisecond(), playerId, playerName, count, build);
		 long size = RedisProxy.getInstance().getRedisSession().lPush(redisKey, 0, JSONObject.toJSONString(record));
		 // 加个10是避免trim太频繁
		 if (size > getRecordLimit() + 10) {
			 RedisProxy.getInstance().getRedisSession().lTrim(redisKey, 0, getRecordLimit());
		 }
	 }
	 
	 /**
	  * 获取国家仓库金条捐献记录存储条数上限
	  * 
	  * @return
	  */
	 public int getRecordLimit() {
		 return NationConstCfg.getInstance().warehouseListLimit();
	 }
	 
	 /**
	  * 获取国家仓库资源存储得rediskey
	  * @return
	  */
     public String getDonateRedisKey(String serverId) {
    	return NationalConst.NATIONAL_WAREHOUSE_RESOURCE + ":" + serverId;
     }
    
     /**
      * 获取国家仓库金条变动记录存储的rediskey
      * @return
      */
     public String getDonateRecordRedisKey(String serverId) {
		return NationalConst.NATIONAL_WAREHOUSE_DONATE + ":" + serverId;
	 }
     
     /**
      * 获取国家建筑的等级
      * 
      * @param serverId 区服ID
      * @param buildId 建筑类型ID
      * @return
      */
 	public int getBuildLevel(String serverId, int buildId) {
 		if (HawkOSOperator.isEmptyString(serverId)) {
 			HawkLog.errPrintln("nation service to getBuildlevel failed, serverId: {}, buildId: {}", serverId, buildId);
 			return 0;
 		}
 		
 		NationbuildingType buildType = NationbuildingType.valueOf(buildId);
 		if (buildType == null) {
 			HawkLog.errPrintln("nation service to getBuildlevel failed, serverId: {}, buildId: {}", serverId, buildId);
 			return 0;
 		}
 		
 		try {
 			serverId = GlobalData.getInstance().getMainServerId(serverId);
 			if (GsConfig.getInstance().getServerId().equals(serverId)) {
 				NationalBuilding building = getNationBuildingByType(buildType);
 				if (building != null && building.getCurrentLevelCfg() != null) {
 					return building.getCurrentLevelCfg().getLevel();
 				} 
 				return 0;
 			}
 			
 			NationBuildLevelCacheObject obj = buildLevelCache.get(serverId, buildId);
 			// 缓存数据设置2分钟的过期时间
 			if (obj == null || GsApp.getInstance().getCurrentTime() - obj.getRefreshTime() > 120000) {
 				int level = RedisProxy.getInstance().getServerNationBuildLvl(serverId, buildId);
 				obj = new NationBuildLevelCacheObject(buildId, level);
 				buildLevelCache.put(serverId, buildId, obj);
 			}
 			
 			return buildLevelCache.get(serverId, buildId).getLevel();
 		} catch (Exception e) {
 			HawkException.catchException(e);
 		}
 		
 		return 0;
     }
 	
 	/**
 	 * 使用道具效果实施
 	 * @param player
 	 * @param itemCfg
 	 * @param itemCount
 	 * @return
 	 */
 	public boolean nationBuildValItemUseEffect(Player player, ItemCfg itemCfg, int itemCount) {
 		int buildId = itemCfg.getNationBuild();
 		NationalBuilding building = NationService.getInstance().getNationBuildingByTypeId(buildId);
		if(building == null){
			HawkLog.errPrintln("item cfg nation build id error, can not find nation build, buildId = {}", buildId);
			return false;
		}
		
		// 增加建设值，世界线程执行
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_BUILD_USE_ITEM) {
			@Override
			public boolean onInvoke() {
				HPItemTipsResp.Builder builder = HPItemTipsResp.newBuilder();
				builder.setItemId(itemCfg.getId());
				
				NationConstructionEntity entity = building.getEntity();
				int current = entity.getBuildVal();
				int totoalVal = entity.getTotalVal();
				int limit = building.getBuildDayLimit();
				// 计算添加量
				int allVal = itemCfg.getBuildValue() * itemCount;
				// 先算出剩余量
				int leftVal = limit - current;
				// 如果还够加，就直接加上
				if(leftVal >= allVal) {
					entity.setBuildVal(current + allVal);
					builder.setItemCount(itemCount);
				} else {
					// 如果溢出了，计算出需要返还的道具数量
					// 算出剩余的数量 再加1个
					int needCount = (leftVal / itemCfg.getBuildValue());
					if(leftVal % itemCfg.getBuildValue() != 0){
						needCount++; // 有余数就多加一个
					}
					allVal = itemCfg.getBuildValue() * needCount;
					
					int backCount = itemCount - needCount;
					// 这里直接加满就行
					entity.setBuildVal(limit);
					
					ItemInfo itemInfo = new ItemInfo();
					itemInfo.setType(ItemType.TOOL_VALUE);
					itemInfo.setItemId(itemCfg.getId());
					itemInfo.setCount(backCount);
					// 发送返还邮件
					SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.NATIONAL_BUILD_BACK_ITEM)
						.addReward(itemInfo)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
					
					builder.setItemCount(needCount);
				}
				// 增加总值
				entity.setTotalVal(entity.getTotalVal() + allVal);
				// 发送使用成功消息
				player.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_USE_TIPS_VALUE, builder));
				
				LogUtil.logNationbuildItemUse(player, buildId, totoalVal, entity.getTotalVal(), entity.getBuildVal() + "_" + limit);
				return true;
			}
		});
		return true;
 	}
 	
}
