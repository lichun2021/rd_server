package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.city.CityManager;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.GuildManorCfg;
import com.hawk.game.config.GuildManorMineCfg;
import com.hawk.game.config.GuildManorTowerCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildManorEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.manor.AbstractBuildable;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.ManorBastionStat;
import com.hawk.game.guild.manor.building.GuildDragonTrap;
import com.hawk.game.guild.manor.building.GuildManorSuperMine;
import com.hawk.game.guild.manor.building.GuildManorTower;
import com.hawk.game.guild.manor.building.GuildManorWarehouse;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManor.GuildBuildingNorStat;
import com.hawk.game.protocol.GuildManor.GuildManorBase;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.GuildManor.GuildManorStat;
import com.hawk.game.protocol.GuildManor.GuildSuperMineType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.LogConst.GuildAction;

/**
 * 联盟领地整体服务类
 * 
 * @author zhenyu.shang
 * @since 2017年7月5日
 */
public class GuildManorService extends HawkAppObj {
	/**
	 * 日志对象
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 所有领地信息:key--manorId, value--manorEntity
	 */
	private Map<String, GuildManorObj> allManors;
	
	/**
	 * 联盟领地列表 guildId --- List
	 */
	private Map<String, List<GuildManorObj>> guildManors;
	
	/**
	 * 联盟建筑列表
	 */
	private Map<String, List<IGuildBuilding>> guildBuildings;
	
	/**
	 * 所有联盟建筑
	 */
	private Map<String, IGuildBuilding> allBuildings;
	
	/**
	 * 单例对象
	 */
	private static GuildManorService instance = null;
	
	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static GuildManorService getInstance() {
		return instance;
	}

	public GuildManorService(HawkXID xid) {
		super(xid);
		instance = this;
		
		allManors = new ConcurrentHashMap<String, GuildManorObj>();
		guildManors = new ConcurrentHashMap<String, List<GuildManorObj>>();
		guildBuildings = new ConcurrentHashMap<String, List<IGuildBuilding>>();
		allBuildings = new ConcurrentHashMap<String, IGuildBuilding>();
		
	}

	public boolean init() {
		long startTime = HawkTime.getMillisecond();
		//从数据库中加载已有的联盟领地
		List<GuildManorEntity> dbManorList = HawkDBManager.getInstance().query("from GuildManorEntity where invalid = 0");
		if (dbManorList != null && !dbManorList.isEmpty()) {
			for (GuildManorEntity entity : dbManorList) {
				GuildManorObj obj = new GuildManorObj(entity);
				allManors.put(entity.getManorId(), obj);//存入全局对应关系
				String guildId = entity.getGuildId();
				if (HawkOSOperator.isEmptyString(guildId)) {
					logger.warn("manor has not guildId, manorId={}", entity.getManorId());
					continue;
				}
				//公会对应的列表
				List<GuildManorObj> list = guildManors.get(guildId);
				if(list == null){
					list = new ArrayList<GuildManorObj>();
					guildManors.put(guildId, list);
				}
				list.add(obj);
			}
			logger.info("load guild manor info success, num: {},  costtime: {} ", dbManorList.size(), HawkTime.getMillisecond() - startTime);
		}
		
		startTime = HawkTime.getMillisecond();
		
		//从数据库中加载已有的联盟建筑
		List<GuildBuildingEntity> dbManorBuildList = HawkDBManager.getInstance().query("from GuildBuildingEntity where invalid = 0");
		if (dbManorBuildList != null && !dbManorBuildList.isEmpty()) {
			for (GuildBuildingEntity entity : dbManorBuildList) {
				IGuildBuilding building = loadGuildBuild(entity);
				if(building != null){
					allBuildings.put(entity.getId(), building);//存入全局对应关系
					String guildId = entity.getGuildId();
					if (HawkOSOperator.isEmptyString(guildId)) {
						logger.warn("building has not guildId, building={}", entity.getId());
						continue;
					}
					List<IGuildBuilding> list = guildBuildings.get(guildId);
					if(list == null){
						list = new ArrayList<IGuildBuilding>();
						guildBuildings.put(guildId, list);
					}
					list.add(building);
				}
			}
			
			long buildCostTime = HawkTime.getMillisecond() - startTime;
			logger.info("load guild building info success, num: {},  costtime: {} ", dbManorBuildList.size(), buildCostTime);
		}
		//检查一下巨龙陷阱
		for(Map.Entry<String, List<IGuildBuilding>> entry : this.guildBuildings.entrySet()){
			String guildId = entry.getKey();
			List<IGuildBuilding> blist = entry.getValue();
			boolean create = true;
			for(IGuildBuilding build : blist){
				if(build.getBuildType() == TerritoryType.GUILD_DRAGON_TRAP){
					create = false;
					break;
				}
			}
			if(create){
				GuildBuildingEntity trapEntity = this.createBuildEntity(guildId, 1, GuildBuildingNorStat.LOCKED_N_VALUE, TerritoryType.GUILD_DRAGON_TRAP_VALUE);
				if (!HawkDBManager.getInstance().create(trapEntity)) {
					logger.error("manor create guild trapEntity building failed, guildId: {}", guildId);
					continue;
				}
				
				IGuildBuilding trapBuilding = loadGuildBuild(trapEntity);
				blist.add(trapBuilding);
				allBuildings.put(trapEntity.getId(), trapBuilding);//存入全局对应关系
				
			}
		}
		//添加所有建筑的tick
		addTickable(new HawkPeriodTickable(1000) {
			@Override
			public void onPeriodTick() {
				buildingTick();
			}
		});
		
		return true;
	}
	
	/**
	 * 根据entity创建联盟建筑实例
	 * @param entity
	 * @return
	 */
	public IGuildBuilding loadGuildBuild(GuildBuildingEntity entity){
		TerritoryType type = TerritoryType.valueOf(entity.getBuildType());
		IGuildBuilding building = null;
		switch (type) {
		case GUILD_BARTIZAN:
			building = new GuildManorTower(entity, type);
			break;
		case GUILD_MINE:
			building = new GuildManorSuperMine(entity, type, GuildSuperMineType.valueOf(entity.getBuildingId()));
			break;
		case GUILD_STOREHOUSE:
			building = new GuildManorWarehouse(entity, type);
			break;
		case GUILD_DRAGON_TRAP:
			building = new GuildDragonTrap(entity, type);
			break;
		default:
			logger.error("building type error, buildingId={}, type={}", entity.getId(), entity.getBuildType());
			break;
		}
		if(building != null){
			entity.storeBuilding(building);
			building.parseBuildingParam(entity.getBuildParam());
		}
		return building;
	}
	
	/**
	 * 初始化联盟领地和建筑
	 * @param guildId
	 */
	public void initNewGuildManor(String guildId){
		this.createGuildBastion(guildId);
		this.createGuildBuilding(guildId);
	}
	
	/**
	 * 初始化机器人联盟领地和建筑
	 * @param guildId
	 */
	public void initNewRobotGuildManor(String guildId){
		//判断此联盟是否已经有领地，如果有则不执行
		if(guildManors.containsKey(guildId)){
			logger.error("guild already have manors... guildId=" + guildId);
			return;
		}
		//此处在创建联盟的同时，将所有哨塔都进行初始化，状态全部为未解锁状态
		ConfigIterator<GuildManorCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(GuildManorCfg.class);
		List<GuildManorObj> objlist = new ArrayList<GuildManorObj>();
		for (GuildManorCfg guildManorCfg : iterator) {
			GuildManorEntity entity = new GuildManorEntity();
			entity.setManorId(guildId + "_" + guildManorCfg.getId()); //ID为组合id
			entity.setGuildId(guildId);
			entity.setManorIndex(guildManorCfg.getId());
			entity.setManorName(guildManorCfg.getDefaultName());
			entity.setManorState(GuildManorStat.OPENED_M_VALUE);
			entity.setLevel(1);
			if(HawkDBManager.getInstance().create(entity)){
				GuildManorObj obj = new GuildManorObj(entity);
				//存入列表
				objlist.add(obj);
				//存入全局
				allManors.put(entity.getManorId(), obj);
			} else {
				logger.error("manor to db error , guildId={}, manorIdx={}", guildId, guildManorCfg.getId());
			}
		}
		//存入Service
		guildManors.put(guildId, objlist);
		
		
		//判断此联盟是否已经有建筑，如果有则不执行
		if(guildBuildings.containsKey(guildId)){
			logger.error("guild already have manors... guildId=" + guildId);
			return;
		}
		List<IGuildBuilding> buildList = new ArrayList<IGuildBuilding>();
		guildBuildings.put(guildId, buildList);
		//初始化联盟超级矿
		for (GuildSuperMineType mineType : GuildSuperMineType.values()) {
			//这里将矿的类型作为buildId, 4个矿初始情况下是都可以放置的
			GuildBuildingEntity mineEntity = createBuildEntity(guildId, mineType.getNumber(), GuildBuildingNorStat.OPENED_N_VALUE, TerritoryType.GUILD_MINE_VALUE);
			if(HawkDBManager.getInstance().create(mineEntity)){
				IGuildBuilding building = loadGuildBuild(mineEntity);
				buildList.add(building);
				allBuildings.put(mineEntity.getId(), building);
			} else {
				logger.error("mine build to db error , type={}, guildId={}, index={}", TerritoryType.GUILD_MINE_VALUE, guildId, mineType.getNumber());
			}
		}
		//初始化联盟仓库
		GuildBuildingEntity houseEntity = createBuildEntity(guildId, 1, GuildBuildingNorStat.OPENED_N_VALUE, TerritoryType.GUILD_STOREHOUSE_VALUE);
		if(HawkDBManager.getInstance().create(houseEntity)){
			IGuildBuilding building = loadGuildBuild(houseEntity);
			buildList.add(building);
			allBuildings.put(houseEntity.getId(), building);
		} else {
			logger.error("house build to db error , type={}, guildId={}", TerritoryType.GUILD_STOREHOUSE_VALUE, guildId);
		}
		//初始化联盟箭塔 (此处默认初始化1个哨塔时的状态)
		ConfigIterator<GuildManorTowerCfg> it = HawkConfigManager.getInstance().getConfigIterator(GuildManorTowerCfg.class);
		while (it.hasNext()) {
			GuildManorTowerCfg cfg = it.next();
			GuildManorTower towerEntity = getBuildingByTypeAndIdx(guildId, cfg.getId(), TerritoryType.GUILD_BARTIZAN);
			if(towerEntity != null){
				continue;
			}
			GuildBuildingEntity entity = createBuildEntity(guildId, cfg.getId(), GuildBuildingNorStat.OPENED_N_VALUE, TerritoryType.GUILD_BARTIZAN_VALUE);
			if(HawkDBManager.getInstance().create(entity)){
				IGuildBuilding building = loadGuildBuild(entity);
				guildBuildings.get(guildId).add(building);
				allBuildings.put(entity.getId(), building);
			} else {
				logger.error("bartizan build to db error , type={}, guildId={}, index={}", TerritoryType.GUILD_BARTIZAN_VALUE, guildId, cfg.getId());
			}
		}
	}
	
	/**
	 * 创建联盟哨塔
	 * @param guildId
	 */
	private void createGuildBastion(String guildId){
		//判断此联盟是否已经有领地，如果有则不执行
		if(guildManors.containsKey(guildId)){
			logger.error("guild already have manors... guildId=" + guildId);
			return;
		}
		//此处在创建联盟的同时，将所有哨塔都进行初始化，状态全部为未解锁状态
		List<GuildManorObj> objlist = new ArrayList<GuildManorObj>();
		ConfigIterator<GuildManorCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(GuildManorCfg.class);
		for (GuildManorCfg guildManorCfg : iterator) {
			GuildManorEntity entity = new GuildManorEntity();
			entity.setManorId(guildId + "_" + guildManorCfg.getId()); //ID为组合id
			entity.setGuildId(guildId);
			entity.setManorIndex(guildManorCfg.getId());
			entity.setManorName(guildManorCfg.getDefaultName());
			entity.setManorState(GuildManorStat.LOCKED_M.getNumber());
			entity.setLevel(1);
			entity.setBuildingLife(1);
			
			if (!HawkDBManager.getInstance().create(entity)) {
				logger.error("manor create guild bastion failed, guildId: {}, manorIndex: {}", guildId, guildManorCfg.getId());
				continue;
			}
			
			GuildManorObj obj = new GuildManorObj(entity);
			//存入列表
			objlist.add(obj);
			//存入全局
			allManors.put(entity.getManorId(), obj);
		}
		
		//存入Service
		guildManors.put(guildId, objlist);
	}
	
	/**
	 * 创建联盟建筑
	 * @param guildId
	 * @param first 初次创建
	 */
	private void createGuildBuilding(String guildId){
		//判断此联盟是否已经有建筑，如果有则不执行
		if(guildBuildings.containsKey(guildId)){
			logger.error("guild already have manors, guildId: {}", guildId);
			return;
		}
		
		List<IGuildBuilding> buildList = new ArrayList<IGuildBuilding>();
		guildBuildings.put(guildId, buildList);
		//初始化联盟超级矿
		for (GuildSuperMineType mineType : GuildSuperMineType.values()) {
			//这里将矿的类型作为buildId, 4个矿初始情况下是都可以放置的
			GuildBuildingEntity mineEntity = createBuildEntity(guildId, mineType.getNumber(), GuildBuildingNorStat.LOCKED_N_VALUE, TerritoryType.GUILD_MINE_VALUE);
			
			if (!HawkDBManager.getInstance().create(mineEntity)) {
				logger.error("manor create guild mine building failed, guildId: {}, mineType: {}", guildId, mineType);
				continue;
			}
			
			IGuildBuilding building = loadGuildBuild(mineEntity);
			buildList.add(building);
			allBuildings.put(mineEntity.getId(), building);
		}
		
		//初始化联盟仓库
		GuildBuildingEntity houseEntity = createBuildEntity(guildId, 1, GuildBuildingNorStat.LOCKED_N_VALUE, TerritoryType.GUILD_STOREHOUSE_VALUE);
		if(HawkDBManager.getInstance().create(houseEntity)){
			IGuildBuilding building = loadGuildBuild(houseEntity);
			buildList.add(building);
			allBuildings.put(houseEntity.getId(), building);
		} else {
			logger.error("house build to db error , type={}, guildId={}", TerritoryType.GUILD_STOREHOUSE_VALUE, guildId);
		}
		
		//初始化联盟箭塔 (此处默认初始化1个哨塔时的状态)
		initGuildManorTower(1, guildId);
		
		//巨龙陷阱
		GuildBuildingEntity trapEntity = this.createBuildEntity(guildId, 1, GuildBuildingNorStat.LOCKED_N_VALUE, TerritoryType.GUILD_DRAGON_TRAP_VALUE);
		if (HawkDBManager.getInstance().create(trapEntity)) {
			IGuildBuilding trapBuilding = loadGuildBuild(trapEntity);
			buildList.add(trapBuilding);
			allBuildings.put(trapEntity.getId(), trapBuilding);
		}else{
			logger.error("manor create guild trapEntity building failed, guildId: {}", guildId);
			
		}
		
		
	}
	
	/**
	 * 初始化联盟箭塔
	 * @param manorIdx
	 * @param guildId
	 * @param buildList
	 */
	public void initGuildManorTower(int manorIdx, String guildId){
		List<GuildManorTowerCfg> list = getTowerCfgByManorIdx(manorIdx);
		for (GuildManorTowerCfg cfg : list) {
			GuildManorTower towerEntity = getBuildingByTypeAndIdx(guildId, cfg.getId(), TerritoryType.GUILD_BARTIZAN);
			if(towerEntity != null){
				continue;
			}
			GuildBuildingEntity entity = createBuildEntity(guildId, cfg.getId(), GuildBuildingNorStat.LOCKED_N_VALUE, TerritoryType.GUILD_BARTIZAN_VALUE);

			if (!HawkDBManager.getInstance().create(entity)) {
				logger.error("manor create guild bartizan failed, guildId: {}, manorIndex: {}, cfgId: {}", guildId, manorIdx, cfg.getId());
				continue;
			}
			
			IGuildBuilding building = loadGuildBuild(entity);
			guildBuildings.get(guildId).add(building);
			allBuildings.put(entity.getId(), building);
		}
	}
	
	/**
	 * 获取联盟领地对应箭塔
	 * @param index
	 * @return
	 */
	public List<GuildManorTowerCfg> getTowerCfgByManorIdx(int index){
		List<GuildManorTowerCfg> list = new ArrayList<GuildManorTowerCfg>();
		ConfigIterator<GuildManorTowerCfg> it = HawkConfigManager.getInstance().getConfigIterator(GuildManorTowerCfg.class);
		while (it.hasNext()) {
			GuildManorTowerCfg guildManorTowerCfg = it.next();
			if(guildManorTowerCfg.getManorCount() == index){
				list.add(guildManorTowerCfg);
			}
		}
		return list;
	}
	
	/**
	 * 获取联盟领地列表
	 * @param guildId
	 * @return
	 */
	public List<GuildManorObj> getGuildManors(String guildId){
		return guildManors.get(guildId);
	}
	
	/**
	 * 获取第一个联盟领地的坐标
	 * @param guildId
	 * @return
	 */
	public Integer getGuildManorPointId(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return null;
		}
		List<GuildManorObj> manors = getGuildManors(guildId);
		if (manors == null || manors.isEmpty()) {
			return null;
		}
		for (GuildManorObj manor : manors) {
			if (manor.getBastionStat() == ManorBastionStat.LOCKED || manor.getBastionStat() == ManorBastionStat.OPENED) {
				continue;
			}
			return manor.getPositionId();
		}
		return null;
	}
	
	/**
	 * 构建一个建筑实体
	 * @param guildId
	 * @param buildId
	 * @param buildStat
	 * @param buildType
	 * @return
	 */
	public GuildBuildingEntity createBuildEntity(String guildId, int buildId, int buildStat, int buildType){
		GuildBuildingEntity entity = new GuildBuildingEntity();
		entity.setId(guildId + "_" + buildType + "_" + buildId); //ID为组合id
		entity.setGuildId(guildId);
		entity.setBuildingId(buildId);
		entity.setBuildingStat(buildStat);
		entity.setBuildType(buildType);
		entity.setLevel(1);
		entity.setBuildLife(1);
		return entity;
	}
	
	
	public GuildManorList.Builder makeManorListBuilder(String guildId){
		GuildManorList.Builder builder = GuildManorList.newBuilder();
		//领地哨塔列表
		makeManorBastion(builder, guildId);
		//领地建筑列表
		makeManorBuilding(builder, guildId);
		return builder;
	}
	
	public void makeManorBastion(GuildManorList.Builder builder, String guildId){
		//领地哨塔列表
		for (GuildManorObj manor : guildManors.get(guildId)) {
			GuildManorBase.Builder manorBuilder = GuildManorBase.newBuilder();
			manorBuilder.setBuildLife((int) manor.getEntity().getBuildingLife());
			manorBuilder.setLevel(manor.getEntity().getLevel());
			manorBuilder.setManorIdx(manor.getEntity().getManorIndex());
			manorBuilder.setManorName(manor.getEntity().getManorName());
			manorBuilder.setOverTime(manor.getOverTime());
			manorBuilder.setStat(manor.getEntity().getManorStatEnum());
			manorBuilder.setX(manor.getEntity().getPosX());
			manorBuilder.setY(manor.getEntity().getPosY());
			
			builder.addAllManor(manorBuilder.build());
		}
	}
	
	public void makeManorBuilding(GuildManorList.Builder builder, String guildId){
		//领地建筑列表
		for (IGuildBuilding building : guildBuildings.get(guildId)) {
			building.addProtocol2Builder(builder);
		}
	}
	
	/**
	 * 根据索引获取大本的对象
	 * @param guildId
	 * @param idx
	 * @return
	 */
	public GuildManorObj getManorByIdx(String guildId, int idx){
		return allManors.get(guildId + "_" + idx);
	}
	
	/**
	 * 根据类型和索引查找联盟建筑
	 * @param guildId
	 * @param idx
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends IGuildBuilding> T getBuildingByTypeAndIdx(String guildId, int idx, TerritoryType type){
		return (T) allBuildings.get(guildId + "_" + type.getNumber() + "_" + idx);
	}
	
	/**
	 * 根据guildId获取联盟建筑列表
	 * @param guildId
	 * @return
	 */
	public List<IGuildBuilding> getGuildBuildings(String guildId) {
		if (!guildBuildings.containsKey(guildId)) {
			return new ArrayList<IGuildBuilding>();
		}
		return guildBuildings.get(guildId);
	}
	
	/**
	 * 获取联盟ID
	 * @param manorId
	 * @return
	 */
	public String getManorGuildId(String manorId){
		GuildManorObj obj = allManors.get(manorId);
		if(obj == null){
			return null;
		}
		return obj.getEntity().getGuildId();
	}
	
	/**
	 * 根据世界点获取领地实体对象
	 * @param point
	 * @return
	 */
	public GuildManorObj getGuildManorByPoint(WorldPoint point){
		return getAllManors().get(point.getGuildBuildId());
	}
	
	/**
	 * 根据类型获取半径
	 * @param type
	 * @return -1 为类型不正确
	 */
	public int getRadiusByType(TerritoryType type){
		int radius = 1;
		switch (type) {
		case GUILD_BASTION:
			radius = GuildManorObj.RADIUS;
			break;
		case GUILD_BARTIZAN:
			radius = GuildManorTower.RADIUS;
			break;
		case GUILD_MINE:
			radius = GuildManorSuperMine.RADIUS;
			break;
		case GUILD_STOREHOUSE:
			radius = GuildManorWarehouse.RADIUS;
			break;
		case GUILD_DRAGON_TRAP:
			radius = GuildDragonTrap.RADIUS;
		default:
			break;
		}
		return radius;
	}
	
	/**
	 * 获取联盟大本位置
	 * @param id
	 * @param resourceType
	 * @return
	 */
	public int getManorPostion(String manorId){
		GuildManorObj obj = allManors.get(manorId);
		if(obj == null){
			return 0;
		}
		return GameUtil.combineXAndY(obj.getEntity().getPosX(), obj.getEntity().getPosY());
	}

	
	public Map<String, GuildManorObj> getAllManors() {
		return allManors;
	}

	public Map<String, IGuildBuilding> getAllBuildings() {
		return allBuildings;
	}
	
	/**
	 * 建筑Tick
	 */
	public void buildingTick(){
		long start = HawkTime.getMillisecond();
		//先检查所有大本
		for (GuildManorObj manor : allManors.values()) {
			try {
				manor.tick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		long time = HawkTime.getMillisecond() - start;
		if(time > 200){
			logger.warn("guild manor tick too much time , time : {}", time);
		}
		//再检查所有建筑
		for (IGuildBuilding building : allBuildings.values()) {
			try {
				building.tick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		time = HawkTime.getMillisecond() - start;
		if(time > 200){
			logger.warn("guild manor all building tick too much time , time : {}", time);
		}
	}
	
	public int getBuildStat(int type, String guildBuildId){
		TerritoryType territoryType = TerritoryType.valueOf(type);
		int stat = -1;
		switch (territoryType) {
			case GUILD_BASTION:
				GuildManorObj obj = allManors.get(guildBuildId);
				if(obj != null){
					stat = obj.getBastionStat().getIndex();
				}
				break;
			case GUILD_BARTIZAN:
			case GUILD_MINE:
			case GUILD_STOREHOUSE:
				IGuildBuilding building = allBuildings.get(guildBuildId);
				if(building != null){
					stat = building.getBuildStat().getIndex();
				}
				break;
			default:
				break;
		}
		return stat;
	}

	public AbstractBuildable getBuildable(WorldPoint point){
		AbstractBuildable building = null;
		//判断目标类型
		TerritoryType type = TerritoryType.valueOf(point.getBuildingId());
		String guildBuildId = point.getGuildBuildId();
		switch (type) {
			case GUILD_BASTION:
				building = allManors.get(guildBuildId);
				break;
			case GUILD_BARTIZAN:
			case GUILD_MINE:
			case GUILD_STOREHOUSE:
			case GUILD_DRAGON_TRAP:
				building = (AbstractBuildable) allBuildings.get(guildBuildId);
				break;
			default:
				break;
		}
		return building;
	}
	
	public AbstractBuildable getBuildable(String guildId, TerritoryType type, int idx){
		AbstractBuildable building = null;
		//判断目标类型
		switch (type) {
			case GUILD_BASTION:
				building = getManorByIdx(guildId, idx);
				break;
			case GUILD_BARTIZAN:
			case GUILD_MINE:
			case GUILD_STOREHOUSE:
				building = getBuildingByTypeAndIdx(guildId, idx, type);
				break;
			default:
				break;
		}
		return building;
	}
	
	public List<String> sortManorIdsByTime(List<String> manorIds){
		List<GuildManorObj> manorList = new ArrayList<GuildManorObj>();
		for (String manorId : manorIds) {
			manorList.add(allManors.get(manorId));
		}
		
		//按完成时间排序
		Collections.sort(manorList, new Comparator<GuildManorObj>() {
			@Override
			public int compare(GuildManorObj o1, GuildManorObj o2) {
				return (int) (o1.getEntity().getCompleteTime() - o2.getEntity().getCompleteTime());
			}
		});
		
		manorIds.clear();
		for (GuildManorObj manorObj : manorList) {
			manorIds.add(manorObj.getEntity().getManorId());
		}
		return manorIds;
	}
	
	/**
	 * 获取可用的联盟箭塔列表
	 * 
	 * @param guildId
	 * @return
	 */
	public Map<String, Integer> getUseableTower(String guildId){
		Map<String, Integer> towerMap = new HashMap<String, Integer>();
		List<IGuildBuilding> buildings = guildBuildings.get(guildId);
		for (IGuildBuilding iGuildBuilding : buildings) {
			if(iGuildBuilding.getBuildType() == TerritoryType.GUILD_BARTIZAN && iGuildBuilding.getBuildStat() == GuildBuildingStat.COLLECT){
				towerMap.put(iGuildBuilding.getEntity().getId(), iGuildBuilding.getPositionId());
			}
		}
		return towerMap;
	}
	
	/**
	 * 添加可攻击的行军至当前箭塔列表
	 * @param march
	 * @param towerIdx
	 */
	public void addAttackMarchToTower(IWorldMarch march, List<String> retBartizanIds){
		for (String id : retBartizanIds) {
			GuildManorTower tower = (GuildManorTower) allBuildings.get(id);
			tower.addCanAttackMarch(march.getMarchId());
		}
	}
	
	/**
	 * 从箭塔中删除可攻击的行军
	 * @param march
	 * @param retBartizanIds
	 */
	public void removeAttackMarchToTower(IWorldMarch march, String guildId){
		List<IGuildBuilding> bartizans = guildBuildings.get(guildId);
		if(bartizans == null){
			return;
		}
		for (IGuildBuilding building : bartizans) {
			if(building.getBuildType() == TerritoryType.GUILD_BARTIZAN){
				GuildManorTower tower = (GuildManorTower) building;
				tower.removeAttackMarch(march.getMarchId());
			}
		}
	}
	
	/**
	 * 联盟领地堡垒点广播
	 * @param guildId
	 */
	public void broadcastManorPonit(String guildId){
		List<GuildManorObj> manorObjs = guildManors.get(guildId);
		if(manorObjs != null){
			for (GuildManorObj guildManorObj : manorObjs) {
				if(guildManorObj.getBastionStat() != ManorBastionStat.LOCKED && guildManorObj.getBastionStat() != ManorBastionStat.OPENED){
					//通知状态
					WorldPointService.getInstance().notifyPointUpdate(guildManorObj.getEntity().getPosX(), guildManorObj.getEntity().getPosY());
				}
			}
		}
		List<IGuildBuilding> buildings = guildBuildings.get(guildId);
		if(buildings != null){
			for (IGuildBuilding iGuildBuilding : buildings) {
				if(iGuildBuilding.getBuildStat() != GuildBuildingStat.LOCKED && iGuildBuilding.getBuildStat() != GuildBuildingStat.OPENED){
					//通知状态
					WorldPointService.getInstance().notifyPointUpdate(iGuildBuilding.getEntity().getPosX(), iGuildBuilding.getEntity().getPosY());
				}
			}
		}
	}
	
	/**
	 * 联盟解散,移除所有联盟领地和建筑
	 * @param guildId
	 */
	public void removeManorOnDissmiseGuild(String guildId, Player player){
		// 移除联盟领地
		List<GuildManorObj> manors = getGuildManors(guildId);
		for(GuildManorObj obj : manors){
			//先调用地图点移除
			rmGuildManor(guildId, obj.getEntity().getManorId());
			//再修改领地本身的影响
			obj.onMonorRemove();
			//通知状态
			WorldPointService.getInstance().notifyPointUpdate(obj.getEntity().getPosX(), obj.getEntity().getPosY());
			obj.delete();
		}
		//删除数据库中建筑实体
		List<IGuildBuilding> buildings = getGuildBuildings(guildId);
		for(IGuildBuilding building : buildings){
			building.onBuildDelete();
		}
	}
	
	/**
	 * 广播领地建筑信息
	 * @param guildId
	 */
	public void broadcastGuildBuilding(String guildId) {
		//推送变化消息
		GuildManorList.Builder builder = GuildManorList.newBuilder();
		//领地建筑列表
		makeManorBuilding(builder, guildId);
		//广播消息
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
	}
	
	/**
	 * 移除领地建筑
	 * @param building
	 */
	public void removeManorBuilding(IGuildBuilding building){
		//先从地图中移除建筑
		rmGuildBuild(building);
		//修改建筑影响
		building.onBuildRemove();
	}
	
	/**
	 * 处理退出联盟逻辑
	 * @param playerId
	 */
	public void doQuitGuild(String playerId, String guildId){
		for (IGuildBuilding building : allBuildings.values()) {
			if(building.getEntity().getGuildId().equals(guildId)){
				building.doQuitGuild(playerId);
			}
		}
	}
	
	/**
	 * 检查联盟建筑是否可以放置
	 * @param guildId
	 * @param territoryType
	 * @param pointId
	 * @param buildIndex
	 * @return
	 */
	public boolean checkGuildBuildCanBuild(String guildId, TerritoryType territoryType, int pointId, int buildIndex){
		if (HawkOSOperator.isEmptyString(guildId) || territoryType == null || pointId <= 0) {
			return false;
		}

		int radius = getRadiusByType(territoryType);
		if (radius < 0) {
			return false;
		}
		
		//联盟矿需要判断是否已经有了, 如果有了则不能继续放置
		if(territoryType == TerritoryType.GUILD_MINE){
			List<IGuildBuilding> buildings = GuildManorService.getInstance().getGuildBuildings(guildId);
			for (IGuildBuilding iGuildBuilding : buildings) {
				if(iGuildBuilding.getBuildType() == TerritoryType.GUILD_MINE 
						&& iGuildBuilding.getBuildStat() != GuildBuildingStat.LOCKED && iGuildBuilding.getBuildStat() != GuildBuildingStat.OPENED){
					return false;
				}
			}
		}
		
		if (!checkGuildBuildCanOccupy(guildId, buildIndex, territoryType, pointId, radius)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 世界上建造联盟建筑
	 * 
	 * @param guildId
	 *            联盟id
	 * @param territoryType
	 *            建筑类型
	 * @param pointId
	 *            点id
	 * @return
	 */
	public WorldPoint genGuildBuildInWorld(String guildId, String guildBuildId, TerritoryType territoryType, int pointId, int buildIndex) {
		//检查是否可放置
		if(!checkGuildBuildCanBuild(guildId, territoryType, pointId, buildIndex)){
			return null;
		}
		
		int[] pointXY = GameUtil.splitXAndY(pointId);

		AreaObject areaObj = WorldPointService.getInstance().getArea(pointXY[0], pointXY[1]);
		// 资源带id
		int resZoneId = WorldUtil.getPointResourceZone(pointXY[0], pointXY[1]);
		if (areaObj == null || resZoneId <= 0) {
			return null;
		}
		
		// 生成玩家城堡占用点
		WorldPoint worldPoint = new WorldPoint(pointXY[0], pointXY[1], areaObj.getId(), resZoneId, WorldPointType.GUILD_TERRITORY_VALUE);
		worldPoint.setGuildId(guildId);
		worldPoint.setGuildBuildId(guildBuildId);
		worldPoint.setBuildingId(territoryType.getNumber());

		if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
			return null;
		}
		if (territoryType == TerritoryType.GUILD_BASTION) {
			//联盟收藏红点
			GuildService.getInstance().notifyGuildFavouriteRedPoint(guildId, GsConst.GuildFavourite.TYPE_GUILD_MANOR, buildIndex);
		} else {
			GuildService.getInstance().notifyGuildFavouriteRedPoint(guildId, GsConst.GuildFavourite.TYPE_GUILD_BUILDING, territoryType.getNumber());
		}

		return worldPoint;
	}

	/**
	 * 联盟建筑大本完成
	 * 
	 * @param player
	 * @param guildId
	 * @param territoryType
	 * @return
	 */
	public void guildBuildComplete(String guildId, String manorId, TerritoryType territoryType, int pointId) {
		if (!WorldUtil.isGuildBastion(territoryType) || HawkOSOperator.isEmptyString(manorId) || HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		// 刷新玩家buff
		notifyManorBuffChange(pointId);		
		
		LogUtil.logGuildAction(GuildAction.GUILD_MANOR_COMPLETE, guildId);
	}	

	/**
	 * 移除联盟建筑
	 * 
	 * @param build
	 */
	public void rmGuildBuild(IGuildBuilding build) {
		WorldPoint wp = build.getPoint();
		if (wp == null) {
			return;
		}
		rmGuildBuild(wp);
	}

	/**
	 * 移除联盟领地
	 * 
	 * @param guildId
	 * @param manorId
	 */
	public void rmGuildManor(String guildId, String manorId) {
		// 联盟领地(大本)pointId
		int pointId = getManorPostion(manorId);

		WorldPoint wp = WorldPointService.getInstance().getWorldPoint(pointId);
		if (wp == null || !wp.getGuildId().equals(guildId)) {
			return;
		}

		if (wp.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE) {
			return;
		}
		
		List<IGuildBuilding> builds = getGuildBuildings(guildId);
		// 移除其它建筑
		for (IGuildBuilding build : builds) {
			if (build.isPlaceGround() && isGuildBuildCanRmove(build, guildId, manorId)) {
				removeManorBuilding(build);
			}
		}
		// 移除大本
		rmGuildBuild(wp);
		// 刷新玩家buff
		notifyManorBuffChange(wp.getId());
		LogUtil.logGuildAction(GuildAction.GUILD_MANOR_REMOVE, guildId);
		
		// 再做一次检测
		for (IGuildBuilding build : builds) {
			checkBuildingRemove(guildId, build);
		}
	}

	/**
	 * 检测建筑移除
	 */
	private void checkBuildingRemove(String guildId, IGuildBuilding build) {
		if (!build.isPlaceGround()) {
			return;
		}
		
		WorldPoint buildPoint = build.getPoint();
		if (buildPoint == null) {
			return;
		}

		// 建筑半径
		int radius = GuildManorService.getInstance().getRadiusByType(build.getBuildType());
		if (radius <= 1) {
			return;
		}
		
		long startTime = HawkTime.getMillisecond();
		
		// 建筑占用所有的点 周围占用点+中心点
		List<Point> arroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(buildPoint.getX(), buildPoint.getY(), radius);
		arroundPoints.add(WorldPointService.getInstance().getAreaPoint(buildPoint.getX(), buildPoint.getY(), false));
		
		// 所有领地
		List<GuildManorObj> manors = GuildManorService.getInstance().getGuildManors(guildId);
		
		// 建筑上有任何点在领地内,就不移除
		for (Point point : arroundPoints) {
			for (GuildManorObj manor : manors) {
				if (point == null || manor == null || manor.getEntity() == null) {
					HawkLog.errPrintln("checkBuildingRemove null error, point: {}, manor entity: {}", point, manor == null ? "manor null" : "manor entity null");
					continue;
				} 
				if (GuildManorService.getInstance().isInManor(guildId, manor.getEntity().getManorId(), point.getId())) {
					return;
				}
			}
		}
		
		// 移除领地建筑
		removeManorBuilding(build);

		HawkLog.logPrintln("check guild building remove, guildId:{}, pointId:{}, type:{}, radius:{}, costTime:{}",
				guildId, buildPoint.getId(), build.getBuildType(), radius, HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 移除联盟建筑
	 * 
	 * @param build
	 */
	private void rmGuildBuild(WorldPoint wp) {
		if (wp == null) {
			return;
		}
		// 先遣返行军
		List<String> marchIds = new ArrayList<String>();
		marchIds.addAll(WorldMarchService.getInstance().getManorMarchs(wp));

		for (String marchId : marchIds) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march != null && !march.getMarchEntity().isInvalid()) {
				WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
			}
		}
		
		// 联盟战争界面移除
		Collection<IWorldMarch> guildMarchs = WorldMarchService.getInstance().getGuildMarchs(wp.getGuildId());
		for (IWorldMarch guildMarch : guildMarchs) {
			if (guildMarch.getMarchEntity().getTerminalId() == wp.getId()) {
				WorldMarchService.getInstance().rmGuildMarch(guildMarch.getMarchId());
			}
		}
		
		Collection<IWorldMarch> worldPointMarch = WorldMarchService.getInstance().getWorldPointMarch(wp.getX(), wp.getY());
		for (IWorldMarch march : worldPointMarch) {
			
			// 向联盟堡垒集结类型并且等待中的行军解散
			if (march.isMassMarch() && march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
				for (IWorldMarch massJoinMarch : massJoinMarchs) {
					WorldMarchService.getInstance().onPlayerNoneAction(massJoinMarch, HawkTime.getMillisecond());
				}
				WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getMarchEntity().getArmys());
			}
			
			// 向联盟堡垒集结类型并且行军中的行军解散
			if (march.isMassMarch() && march.isMarchState()) {
				AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
				Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
				for (IWorldMarch joinMarch : joinMarchs) {
					joinMarch.getMarchEntity().setCallBackX(point.getX());
					joinMarch.getMarchEntity().setCallBackY(point.getY());
					WorldMarchService.getInstance().onMarchCallBack(joinMarch);
				}
				
				march.getMarchEntity().setCallBackX(point.getX());
				march.getMarchEntity().setCallBackY(point.getY());
				WorldMarchService.getInstance().onMarchCallBack(march);
			}
			
			if (march.isMarchState() && 
					(march.getMarchEntity().getMarchType() == WorldMarchType.MANOR_ASSISTANCE_VALUE
					|| march.getMarchEntity().getMarchType() == WorldMarchType.MANOR_SINGLE_VALUE
					|| march.getMarchEntity().getMarchType() == WorldMarchType.MANOR_BUILD_VALUE
					|| march.getMarchEntity().getMarchType() == WorldMarchType.MANOR_REPAIR_VALUE)) {
				AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
				march.getMarchEntity().setCallBackX(point.getX());
				march.getMarchEntity().setCallBackY(point.getY());
				WorldMarchService.getInstance().onMarchCallBack(march);
			}
		}
		
		// 再删除point
		int[] pos = GameUtil.splitXAndY(wp.getId());
		// 移除建筑点和周围占用
		WorldPointService.getInstance().removeWorldPoint(pos[0], pos[1]);
		
		
	}

	/**
	 * 联盟建筑是否可移除
	 * TODO zhenyu.shang 此方法有问题，目前移除领地之后，如果领地范围有重叠，则重叠范围内的建筑仍然会被移除掉
	 * 		 （与策划讨论，目前先按照优先建成原则，如果属于之前建成的领地，则无论重叠与否，都进行删除）
	 * @param build
	 * @param guildId
	 * @return
	 */
	public boolean isGuildBuildCanRmove(IGuildBuilding build, String guildId, String manorId) {
		TerritoryType type = build.getBuildType();
		//不可能是大本
		HawkAssert.isTrue(!WorldUtil.isGuildBastion(type));
		// 是否可移除
		boolean canRmove = true;
		int radius = getRadiusByType(type);
		List<Point> arroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(build.getEntity().getPosX(), build.getEntity().getPosY(), radius);
		
		// 添加中心点
		Point centerPoint = WorldPointService.getInstance().getAreaPoint(build.getEntity().getPosX(), build.getEntity().getPosY(), false);
		if (centerPoint != null) {
			arroundPoints.add(centerPoint);
		}
		
		for (Point point : arroundPoints) {
			// 建筑点全部在将要移除的领地上，移除建筑
			if (!isInManor(guildId, manorId, point.getId())) {
				canRmove = false;
				break;
			}
		}
		return canRmove;
	}

	/**
	 * 检测联盟建筑是否能落座
	 * 
	 * @param guildId
	 * @param territoryType
	 * @param pointId
	 * @param radius
	 * @return
	 */
	private boolean checkGuildBuildCanOccupy(String guildId, int buildingIdx, TerritoryType territoryType, int pointId, int radius) {
		// 请求的中心点被占用
		if (WorldPointService.getInstance().getWorldPoint(pointId) != null) {
			return false;
		}
		//不是联盟大本，并且中心点不在领地范围内
		if (!WorldUtil.isGuildBastion(territoryType) && !isInGuild(guildId, pointId)) {
			return false;
		}
		Point centerPoint = new Point(pointId);
		//判断中心坐标点是否合法
		if(!centerPoint.canTerrSeat(territoryType)){
			return false;
		}
		List<Point> arroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, radius);
		// 周围点不是2n(n+1)个代表有阻挡点
		if (arroundPoints.size() != 2 * (radius - 1) * radius) {
			return false;
		}
		
		//此处校验如果是联盟堡垒的话, 需要算上堡垒占用面积的半径, 所以需要验证占用点是否在地图内部
		if(territoryType == TerritoryType.GUILD_BASTION){
			int manorRadius = GuildConstProperty.getInstance().getManorRadius();
			// 边界坐标点不可用的检测
			if (centerPoint.getX() - manorRadius < 0 || centerPoint.getY() - manorRadius < 0 || centerPoint.getX() + manorRadius > (WorldMapConstProperty.getInstance().getWorldMaxX())
					|| centerPoint.getY() + manorRadius > WorldMapConstProperty.getInstance().getWorldMaxY()) {
				return false;
			}
		}
		
		for (Point point : arroundPoints) {
			//不是联盟大本，并且周围点不在领地范围内
			if (!WorldUtil.isGuildBastion(territoryType) && !isInGuild(guildId, point.getId())) {
				return false;
			}
		}
		//箭塔需要再检查，同联盟箭塔攻击范围不能叠加
		if (WorldUtil.isGuildBartizan(territoryType)) {
			// 箭塔攻击范围
			int bartizanRadius = HawkConfigManager.getInstance().getConfigByKey(GuildManorTowerCfg.class, buildingIdx).getTowerRadius();
			// 周围占用
			List<Point> bartizanArroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, bartizanRadius);
			// 联盟所有大本
			List<IGuildBuilding> guildBartizan = getGuildBuildByType(guildId, territoryType);
			for (IGuildBuilding build : guildBartizan) {
				// 箭塔攻击范围
				int alreadyRadius = HawkConfigManager.getInstance().getConfigByKey(GuildManorTowerCfg.class, build.getEntity().getBuildingId()).getTowerRadius();
				// 已建造箭塔攻击范围的点
				List<Point> alreadyOccupies = WorldPointService.getInstance().getRhoAroundPointsAll(build.getPositionId(), alreadyRadius);
				// 如果有重合点
				for (Point bartizanArroundPoint : bartizanArroundPoints) {
					for (Point alreadyOccupy : alreadyOccupies) {
						if (bartizanArroundPoint.getId() == alreadyOccupy.getId()) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * 获取联盟type类型的建筑列表
	 * 
	 * @param guildId
	 * @param type
	 * @return
	 */
	public List<IGuildBuilding> getGuildBuildByType(String guildId, TerritoryType type) {
		List<IGuildBuilding> guildBuilds = getGuildBuildings(guildId);
		return guildBuilds.stream().filter(build -> build.getBuildType() == type).collect(Collectors.toList());
	}

	/**
	 * 获取领地建筑上的行军
	 * 
	 * @param pointId
	 * @return
	 */
	public List<IWorldMarch> getManorBuildMarch(int pointId) {
		BlockingDeque<String> marchIds = WorldMarchService.getInstance().getManorMarchs(pointId);
		List<IWorldMarch> marchs = new ArrayList<IWorldMarch>();
		for (String marchId : marchIds) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null) {
				continue;
			}
			marchs.add(march);
		}
		return marchs;
	}
	
	/**
	 * 获取领地建筑上的行军Id
	 * 
	 * @param pointId
	 * @return
	 */
	public List<String> getManorBuildMarchId(int pointId) {
		BlockingDeque<String> marchIds = WorldMarchService.getInstance().getManorMarchs(pointId);
		List<String> marchs = new ArrayList<String>();
		for (String marchId : marchIds) {
			marchs.add(marchId);
		}
		return marchs;
	}

	/**
	 * 获取领地队长playerId
	 * 
	 * @param manorId
	 * @return
	 */
	public String getManorLeaderId(int pointId) {
		IWorldMarch march = getManorLeaderMarch(pointId);
		if (march == null || march.getMarchEntity().isInvalid()) {
			return null;
		}
		return march.getPlayerId();
	}

	/**
	 * 获取领地队长playerId
	 * 
	 * @param manorId
	 * @return
	 */
	public Player getManorLeader(int pointId) {
		String leaderId = getManorLeaderId(pointId);
		if (HawkOSOperator.isEmptyString(leaderId)) {
			return null;
		}
		return GlobalData.getInstance().makesurePlayer(leaderId);
	}

	/**
	 * 获取队长行军
	 * 
	 * @param pointId
	 * @return
	 */
	public IWorldMarch getManorLeaderMarch(int pointId) {
		BlockingDeque<String> marchIds = WorldMarchService.getInstance().getManorMarchs(pointId);
		if (marchIds == null || marchIds.isEmpty()) {
			return null;
		}
		String leaderMarchId = null;
		try {
			leaderMarchId = marchIds.getFirst();
		} catch (NoSuchElementException e) {
			logger.warn("manor march has already delete first , no is empty, size : {}", marchIds.size());
			return null;
		}

		return WorldMarchService.getInstance().getMarch(leaderMarchId);
	}

	/**
	 * 获取领地建筑上的当前人口数
	 * 
	 * @param pointId
	 * @return
	 */
	public int getManorBuildPopulation(int pointId) {
		List<IWorldMarch> marchs = getManorBuildMarch(pointId);
		int population = 0;
		for (IWorldMarch wm : marchs) {
			population += WorldUtil.calcSoldierCnt(wm.getMarchEntity().getArmys());
		}
		return population;
	}

	/**
	 * 行军加入联盟箭塔 1、行军类型(攻击 (城堡&并且本联盟) || 资源点 || 驻扎点) 2、目标在领地范围内
	 * 3、取本联盟内所有箭塔判断范围,返回和行军路线相交的箭塔
	 */
	public void addToGuildBartizan(IWorldMarch march) {
		// 行军异常检查
		if (march == null || march.getMarchEntity().isInvalid()) {
			return;
		}
		// 目标点
		int targetPointId = march.getMarchEntity().getTerminalId();

		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(targetPointId);
		// 空点不添加
		if (targetPoint == null || targetPoint.getPointType() == WorldPointType.EMPTY_VALUE) {
			return;
		}
		String targetGuildId = getTargetGuildId(targetPoint);
		// 判断目标点是否被箭塔保护
		if (!getBartizansCanBeAttacked(march.getPlayerId(), targetPoint, targetGuildId)) {
			return;
		}
		// 返回列表
		List<String> retBartizanIds = new ArrayList<String>();
		Map<String, Integer> bartizanIds = getUseableTower(targetGuildId);

		bartizanIds.forEach((k, v) -> {
			// 箭塔攻击范围
			int bartizanRadius = HawkConfigManager.getInstance().getConfigByKey(GuildManorTowerCfg.class, Integer.parseInt(k.split("_")[2])).getTowerRadius();
			// 箭塔攻击范围四个顶点id
			Point[] vertex = WorldUtil.getAreaVertex(v, bartizanRadius);
			// 行军会经过箭塔攻击范围
			if (WorldUtil.isMarchCrossArea(march.getMarchEntity().getOrigionId(), march.getMarchEntity().getTerminalId(), vertex)) {
				retBartizanIds.add(k);
			}
		});
		addAttackMarchToTower(march, retBartizanIds);
	}

	/**
	 * 点所在的领地guildId
	 * 
	 * @param targetPoint
	 * @return
	 */
	public String getTargetGuildId(WorldPoint targetPoint) {
		// 点所在的领地guildId
		String targetGuildId = getGuildId(targetPoint.getId());
		// 大本guildId(大本可能不在自己领地范围内)
		if (WorldUtil.isGuildBastion(targetPoint)) {
			targetGuildId = targetPoint.getGuildId();
		}
		return targetGuildId;
	}

	/**
	 * 目标点是否被被箭塔保护
	 * 
	 * @param march
	 * @return
	 */
	public boolean getBartizansCanBeAttacked(String playerId, WorldPoint targetPoint, String targetGuildId) {
		// 目标点不在领地范围内(联盟大本不判断)
		if (HawkOSOperator.isEmptyString(targetGuildId)) {
			return false;
		}
		// 本联盟玩家发起的行军，不攻击
		if (GuildService.getInstance().isPlayerInGuild(targetGuildId, playerId)) {
			return false;
		}
		// 如果目标是玩家基地或者驻扎点, 需要判断所属
		if (WorldUtil.isPlayerPoint(targetPoint) || targetPoint.getPointType() == WorldPointType.QUARTERED_VALUE) {
			// 判断目标玩家是否属于本联盟
			String tarplayerId = targetPoint.getPlayerId();
			// 不是本联盟则无需保护
			if (!GuildService.getInstance().isPlayerInGuild(targetGuildId, tarplayerId)) {
				return false;
			}
			// 周围占用点不在联盟领地内
			List<Point> arroundOccupy = WorldPointService.getInstance().getRhoAroundPointsAll(targetPoint.getId(), GsConst.PLAYER_POINT_RADIUS);
			for (Point point : arroundOccupy) {
				if (!isInGuild(targetGuildId, point.getId())) {
					return false;
				}
			}
		}
		// 如果目标是资源点或者据点
		if(targetPoint.getPointType() == WorldPointType.RESOURCE_VALUE || targetPoint.getPointType() == WorldPointType.STRONG_POINT_VALUE){
			//资源帝和据点需要判断目标点是否有玩家
			String tarplayerId = targetPoint.getPlayerId();
			if(tarplayerId == null || !GuildService.getInstance().isPlayerInGuild(targetGuildId, tarplayerId)){
				return false;
			}
		}
		return true;
	}

	/**
	 * 从联盟箭塔中移除
	 * @param march
	 */
	public void rmFromGuildBartizan(IWorldMarch march) {
		// 行军异常检查
		if (march == null || march.getMarchEntity().isInvalid()) {
			return;
		}

		// 是否可以被箭塔攻击的行军类型
		if (!WorldUtil.canBeAttackedByBartizan(march.getMarchEntity())) {
			return;
		}

		// 目标点
		int targetPointId = march.getMarchEntity().getTerminalId();

		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(targetPointId);
		// 世界点异常检查
		if (targetPoint == null) {
			return;
		}

		// 点所在的领地guildId
		String targetGuildId = getGuildId(targetPointId);

		// 大本guildId(大本可能不在自己领地范围内)
		if (WorldUtil.isGuildBastion(targetPoint)) {
			targetGuildId = targetPoint.getGuildId();
		}

		// 目标点不在领地范围内(联盟大本不判断)
		if (HawkOSOperator.isEmptyString(targetGuildId)) {
			return;
		}

		removeAttackMarchToTower(march, targetGuildId);
	}

	/**
	 * 行军是否在箭塔攻击范围内
	 * @param centerPointId 箭塔位置
	 * @param radius 箭塔攻击范围
	 * @param march 行军
	 * @return
	 */
	public boolean isInBartizanArea(int centerPointId, int radius, AlgorithmPoint currPoint) {
		int currPointId = currPoint.getId();
		// 当前行军所在点与箭塔重叠
		if (currPointId == centerPointId) {
			return true;
		}
		// 箭塔攻击范围
		List<Point> arroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(centerPointId, radius + 1);
		return arroundPoints.stream().anyMatch(o -> o.getId() == currPointId);
	}

	/**
	 * 通知领地变更范围内玩家刷新领地buff
	 * 
	 * @param playerIds
	 */
	public void notifyManorBuffChange(int pointId) {
		int manorRadius = GuildConstProperty.getInstance().getManorRadius();
		List<Point> mPoints = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, manorRadius + 1);
		for(Point point : mPoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(point.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.PLAYER_VALUE) {
				continue;
			}
			
			Player player = GlobalData.getInstance().makesurePlayer(worldPoint.getPlayerId());
			if (player != null) {
				notifyManorBuffChange(player);
			}
		}
	}

	/**
	 * 通知玩家刷新领地buff
	 * 
	 * @param playerIds
	 */
	public void notifyManorBuffChange(Player player) {
		if (player == null) {
			return;
		}
		EffType[] effTypes = AssembleDataManager.getInstance().getManorEffectTypes();
		player.getEffect().initEffectManor();
		
		if (player.isActiveOnline()) {
			player.getPush().syncPlayerEffect(effTypes);
		}
		
		CityManager.getInstance().cityWallFireSpeedChange(player);
	}

	/**
	 * 玩家是否在自己的联盟领地内
	 * 
	 * @param player
	 * @return
	 */
	public boolean isInOwnGuildManor(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}

		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}

		WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
		if (wp == null) {
			return false;
		}
		
		// 占用点中，只要有点在领地内，就算在领地中。
		boolean isInOwnGuildManor = false;
		List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(wp.getId(), GsConst.PLAYER_POINT_RADIUS);
		for (Point point : aroundPoints) {
			if (!isInGuild(guildId, point.getId())) {
				continue;
			}
			isInOwnGuildManor = true;
			break;
		}
		
		return isInOwnGuildManor;
	}

	/**
	 * 玩家在敌人领地debuff列表
	 * 
	 * @param playerId
	 * @return
	 */
	public List<Integer> getEnemyManorBuff(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return new ArrayList<Integer>();
		}

		WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
		if (wp == null) {
			return new ArrayList<Integer>();
		}
		List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(wp.getId(), GsConst.PLAYER_POINT_RADIUS);
		
		// 返回列表
		List<Integer> retList = new ArrayList<Integer>();
		// 自己的联盟id
		String ownGuildId = GuildService.getInstance().getPlayerGuildId(playerId);

		try {
			for (Point point : aroundPoints) {
				String underGuildId = getUnderRangeGuildId(point.getId());
				if (underGuildId == null) {
					continue;
				}
				if (underGuildId.equals(ownGuildId)) {
					continue;
				}
				retList.add(1);
				break;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return retList;
	}

	/**
	 * 获取联盟超级矿资源类型
	 * 
	 * @param buildId
	 * @return
	 */
	public int getManorBuildResType(String buildId) {
		if (HawkOSOperator.isEmptyString(buildId)) {
			return 0;
		}
		String idx = buildId.split("_")[2];
		GuildManorMineCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildManorMineCfg.class, Integer.parseInt(idx));
		if (cfg == null) {
			return 0;
		}
		return cfg.getResType();
	}

	/**
	 * 是否在所有联盟领地范围内， 包括他人的联盟领地
	 * 
	 * @param pointId
	 * @return
	 */
	public boolean isInGuildManor(int pointId) {
		return !HawkOSOperator.isEmptyString(getUnderRangeGuildId(pointId));
	}

	/**
	 * 是否在guildId联盟的领地内
	 * 
	 * @param guildId
	 * @param pointId
	 * @return
	 */
	public boolean isInGuild(String guildId, int pointId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		
		String underGuildId = getUnderRangeGuildId(pointId);
		if (underGuildId == null) {
			return false;
		}
		return underGuildId.equals(guildId);
	}

	/**
	 * 是否在guildId的manorId领地内
	 * 
	 * @param guildId
	 * @param manorId
	 * @param pointId
	 * @return
	 */
	public boolean isInManor(String guildId, String manorId, int pointId) {
		if (HawkOSOperator.isEmptyString(manorId)) {
			return false;
		}
		
		GuildManorObj manor = getUnderRangeManor(pointId);
		if (manor == null) {
			return false;
		}
		return manor.getEntity().getManorId().equals(manorId);
	}

	/**
	 * 获取点所在的领地 所属联盟id
	 * 
	 * @param pointId
	 * @return
	 */
	public String getGuildId(int pointId) {
		return getUnderRangeGuildId(pointId);
	}
	
	/**
	 * 获取点所在的领地/旗帜范围的guildId
	 */
	public String getUnderRangeGuildId(int pointId) {
		GuildManorObj manor = getUnderRangeManor(pointId);
		IFlag flag = WarFlagService.getInstance().getUnderRangeFlag(pointId);
		
		if (manor == null && flag == null) {
			return null;
		}
		
		if (manor == null) {
			return flag.getCurrentId();
		}
		
		if (flag == null) {
			return manor.getEntity().getGuildId();
		}
		
		long manorCompTime = (manor == null) ? 0L : manor.getEntity().getCompleteTime();
		long flagCompTime = (flag == null) ? 0L : flag.getCompleteTime();
		return (manorCompTime < flagCompTime) ? manor.getGuildId() : flag.getCurrentId();
	}

	/**
	 * 点所属的范围的领地
	 */
	private GuildManorObj getUnderRangeManor(int pointId) {
		GuildManorObj retManor = null;
		
		int manorRadius = GuildConstProperty.getInstance().getManorRadius();
		List<Point> points = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, manorRadius + 1);
		for (Point oPoint : points) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(oPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE) {
				continue;
			}
			AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(worldPoint);
			if (buildable == null) {
				continue;
			}
			if (buildable.getBuildType() != TerritoryType.GUILD_BASTION) {
				continue;
			}
			GuildManorObj obj = (GuildManorObj) buildable;
			if (!obj.isComplete()) {
				continue;
			}
			if (retManor == null) {
				retManor = obj;
				continue;
			}
			if (retManor.getEntity().getCompleteTime() < obj.getEntity().getCompleteTime()) {
				continue;
			}
			retManor = obj;
		}
		return retManor;
	}

	/**
	 * 服务停止前调用
	 */
	public void beforeServerShutDown(){
		//先检查所有大本
		for (GuildManorObj manor : allManors.values()) {
			try {
				if(manor.getBastionStat() != ManorBastionStat.LOCKED && manor.getBastionStat() != ManorBastionStat.OPENED){
					manor.onCloseServer();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		//再检查所有建筑
		for (IGuildBuilding building : allBuildings.values()) {
			try {
				if(building.getBuildStat() != GuildBuildingStat.LOCKED && building.getBuildStat() != GuildBuildingStat.OPENED){
					building.onCloseServer();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	public boolean hasManorComplete(String guildId){
		boolean res = false;
		List<GuildManorObj> manors = guildManors.get(guildId);
		if(manors == null || manors.isEmpty()){
			return res;
		}
		for (GuildManorObj manor : manors) {
			if(manor.isComplete()){
				res = true;
				break;
			}
		}
		return res;
	}
	
	/**
	 * 拆服后检测联盟领地建造
	 * @param guildId
	 */
	public void checkGuildManor(String guildId) {
		try {
			List<GuildManorObj> manorList = guildManors.get(guildId);
			if (manorList != null) {
				return;
			}
			createGuildBastion(guildId);
			createGuildBuilding(guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
