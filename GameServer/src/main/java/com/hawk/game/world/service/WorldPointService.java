package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.filter.HawkFilter;
import org.hawk.log.HawkLog;
import org.hawk.net.HawkNetworkManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreKVCfg;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrossFortressConstCfg;
import com.hawk.game.config.DressCfg;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.ResTreasureCfg;
import com.hawk.game.config.SuperWeaponConstCfg;
import com.hawk.game.config.WorldChristmasWarBossCfg;
import com.hawk.game.config.WorldEnemyRefreshCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldNianBoxCfg;
import com.hawk.game.config.WorldNianCfg;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.config.XZQPointCfg;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.GenerateResTreasureMsgInvoker;
import com.hawk.game.item.BaseShowItem;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpaceMechaGrid;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Dress.DressEditData;
import com.hawk.game.protocol.Dress.DressEditDataSync;
import com.hawk.game.protocol.Dress.DressEditType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.PBGenResTreasureSuccess;
import com.hawk.game.protocol.World.SignatureState;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointPB.Builder;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.PresidentTowerPointId;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.WorldScene;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.AroundPoints;
import com.hawk.game.world.object.CapitalAreaObject;
import com.hawk.game.world.object.DistancePoint;
import com.hawk.game.world.object.MapBlock;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.object.YuriFactoryPoint;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 世界基类
 * 
 * 主要执行一些初始化信息, 存储全局信息, 不执行逻辑处理
 * 
 * @author zhenyu.shang
 * @since 2017年8月15日
 */
public class WorldPointService extends HawkAppObj {

	public static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 资源区域块 <块id,块数据>
	 */
	private Map<Integer, AreaObject> areas;
	
	/**
	 * 黑土地区域
	 */
	private CapitalAreaObject captialArea;

	/**
	 * 所有已被（资源怪物城堡联盟建筑等）占用的点的信息
	 */
	private Map<Integer, WorldPoint> points;
	/**
	 * 黑土地区域
	 */
	private Set<Integer> capitalAreaIds;
	/**
	 * 世界场景对象
	 */
	private WorldScene worldScene;
	/**
	 * 同步状态信息标记
	 */
	private int worldSyncFlag;
	/**
	 * 玩家签名
	 */
	private Map<String, String> signatureMap = new ConcurrentHashMap<>();
	/**
	 * 玩家装扮
	 */
	private ConcurrentHashMap<String, Map<Integer, DressItem>> showDressTable = new ConcurrentHashMap<>();
	
	/**
	 * 装备科技外显
	 */
	private Map<String, Integer> showEquipTech = new ConcurrentHashMap<>();
	
	/**
	 * 泰能装备外显(装备星级外显)
	 */
	public Map<String, Integer> showEquipStar = new ConcurrentHashMap<>();

	/**
	 * 星能探索外显
	 */
	public Map<String, Integer> showStarExplore = new ConcurrentHashMap<>();

	/**
	 * 地图坐标索引
	 */
	private Integer[][] worldIndex;

	/**
	 * 周年庆烟花效果
	 */
	private Map<String, String> fireWorksMap = new ConcurrentHashMap<>();
	
	/**
	 * 装扮称号显示类型
	 */
	private Map<String, Integer> dressTitleTypeMap = new ConcurrentHashMap<>();
	
	/**
	 * 学院名称展示
	 */
	private Map<String, String> collegeNameMap = new ConcurrentHashMap<>();
	
	
	
	
	
	private static WorldPointService instance = null;

	public static WorldPointService getInstance() {
		return instance;
	}

	public WorldPointService(HawkXID xid) {
		super(xid);
		instance = this;
		// 设置同步为所有
		worldSyncFlag = GsConst.WorldSyncFlag.SYNC_ALL;
	}

	/**
	 * 初始化地图管理器
	 * 
	 * @return
	 */
	public boolean init() {
		areas = new ConcurrentHashMap<Integer, AreaObject>();
		points = new ConcurrentHashMap<Integer, WorldPoint>();
		
		HawkLog.logPrintln("world point service init start");
		
		// 代理初始化
		WorldPointProxy.getInstance().init();
		
		// 初始化地图坐标索引
		loadWorldIndex();
		
		// 加载地图的阻挡信息
		if (!MapBlock.getInstance().init()) {
			return false;
		}
		// 加载特殊点
		loadSpecialPoints();

		// 初始化世界场景
		initWorldScene();

		// 加载世界区域对象
		loadWorldAreas();

		// 加载世界点对象, 并计算城点周边占用信息
		loadWorldPoints();

		// 初始化世界区域
		initWorldAreas();

		// 记录世界点的信息
		if (GsConfig.getInstance().isDebug()) {
			logWorldPointInfo();
		}

		// 通知场景初始化完毕
		worldScene.notifyInitOK();
		
		// 日志记录
		HawkLog.logPrintln("world point service init success");
		return true;
	}

	/**
	 * 初始化地图坐标索引
	 */
	private void loadWorldIndex() {
		// 世界地图最大坐标
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		
		worldIndex = new Integer[worldMaxX + 1][worldMaxY + 1];
		for (int x = 0; x <= worldMaxX; x++) {
			for (int y = 0; y <= worldMaxY; y++) {
				worldIndex[x][y] = (y << 16) | x;
			}
		}
	}
	
	/**
	 * 初始化世界的特殊点
	 * @return
	 */
	private void loadSpecialPoints() {
		// 世界地图核心点坐标
		int centerX = WorldMapConstProperty.getInstance().getWorldCenterX();
		int centerY = WorldMapConstProperty.getInstance().getWorldCenterY();

		// 王座区域
		int[] kingPalaceRange = WorldMapConstProperty.getInstance().getKingPalaceRange();
		Collection<Integer> kingPalace = WorldUtil.getRangePointIds(centerX, centerY, kingPalaceRange[0], kingPalaceRange[1]);

		// 黑土地
		int[] capitalCoreRange = WorldMapConstProperty.getInstance().getCapitalCoreRange();
		capitalAreaIds = new ConcurrentHashSet<Integer>();
		capitalAreaIds.addAll(WorldUtil.getRangePointIds(centerX, centerY, capitalCoreRange[0], capitalCoreRange[1]));

		// 把国王宫殿的点加入阻挡信息表
		for (Integer pointId : kingPalace) {
			MapBlock.getInstance().addStopPoint(pointId);
		}
		
		// 超级武器加入阻挡点列表
		int superWeaponRadius = SuperWeaponConstCfg.getInstance().getRadius();
		for (int centerPoint : SuperWeaponService.getInstance().getSuperWeaponPoints()) {
			int[] centerPos = GameUtil.splitXAndY(centerPoint);
			Collection<Integer> superWeaponPalace = WorldUtil.getRangePointIds(centerPos[0], centerPos[1], superWeaponRadius, superWeaponRadius);
			for (Integer pointId : superWeaponPalace) {
				MapBlock.getInstance().addStopPoint(pointId);
			}
		}
		
		// 航海要塞点加入阻挡点列表
		int crossFortressRadius = CrossFortressConstCfg.getInstance().getRadius();
		for (int[] centerPos : CrossFortressConstCfg.getInstance().getPosList()) {
			Collection<Integer> superWeaponPalace = WorldUtil.getRangePointIds(centerPos[0], centerPos[1], crossFortressRadius, crossFortressRadius);
			for (Integer pointId : superWeaponPalace) {
				MapBlock.getInstance().addStopPoint(pointId);
			}
		}
		
		// 小战区阻挡
		if(XZQConstCfg.getInstance().isOpen()){
			ConfigIterator<XZQPointCfg> pit = HawkConfigManager.getInstance().getConfigIterator(XZQPointCfg.class);
			for(XZQPointCfg pcfg : pit){
				Collection<Integer> xzqPalace = WorldUtil.getRangePointIds(pcfg.getX(), pcfg.getY(), pcfg.getGridCnt(), pcfg.getGridCnt());
				for (Integer pointId : xzqPalace) {
					MapBlock.getInstance().addStopPoint(pointId);
				}
			}
		}
		
//		// 国家建筑加入阻挡点
//		int nbRadius = NationConstCfg.getInstance().getRadius();
//		for (Integer point : NationService.getInstance().getAllNationalPoint().keySet()) {
//			int[] centerPos = GameUtil.splitXAndY(point);
//			Collection<Integer> nbPalace = WorldUtil.getRangePointIds(centerPos[0], centerPos[1], nbRadius, nbRadius);
//			for (Integer pointId : nbPalace) {
//				MapBlock.getInstance().addStopPoint(pointId);
//			}
//		}
	}

	/**
	 * 初始化世界场景
	 */
	private void initWorldScene() {
		// 世界地图最大坐标
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();

		// 玩家视野半径
		int viewXRadius = GameConstCfg.getInstance().getViewXRadius();
		int viewYRadius = GameConstCfg.getInstance().getViewYRadius();
		int searchRadius = GameConstCfg.getInstance().getSyncSearchRadius();
		searchRadius = Math.max(searchRadius, Math.max(viewXRadius, viewYRadius));

		// 初始化世界场景
		worldScene = new WorldScene(worldMaxX, worldMaxY, searchRadius);
	}

	/**
	 * 加载世界区域数据
	 */
	private void loadWorldAreas() {
		logger.info("start load world areas ...");
		long startTime = HawkTime.getMillisecond();

		// 创建区域对象列表
		List<AreaObject> areaList = createWorldAreas();
		for (AreaObject areaObj : areaList) {
			// 创建区域内所有点对象
			if (areaObj.initAreaPoints()) {
				areas.put(areaObj.getId(), areaObj);
			}
		}
		
		captialArea = new CapitalAreaObject();
		logger.info("world areas load finish, count: {}, costtime: {}", areas.size(), HawkTime.getMillisecond() - startTime);
	}

	/**
	 * 创建世界区域对象
	 * @return
	 */
	private List<AreaObject> createWorldAreas() {
		List<AreaObject> areaList = new LinkedList<AreaObject>();
		// 分区数据
		int areaCols = WorldMapConstProperty.getInstance().getWorldMaxX() / WorldMapConstProperty.getInstance().getWorldResRefreshWidth();
		areaCols = WorldMapConstProperty.getInstance().getWorldMaxX() % WorldMapConstProperty.getInstance().getWorldResRefreshWidth() == 0 ? areaCols : areaCols + 1;

		int areaRows = WorldMapConstProperty.getInstance().getWorldMaxY() / WorldMapConstProperty.getInstance().getWorldResRefreshHeight();
		areaRows = WorldMapConstProperty.getInstance().getWorldMaxY() % WorldMapConstProperty.getInstance().getWorldResRefreshHeight() == 0 ? areaRows : areaRows + 1;

		// 根据资源分块信息进行分块对象创建
		for (int row = 0; row < areaRows; row++) {
			for (int col = 0; col < areaCols; col++) {
				int beginX = col * WorldMapConstProperty.getInstance().getWorldResRefreshWidth();
				int beginY = row * WorldMapConstProperty.getInstance().getWorldResRefreshHeight();
				int endX = (col + 1) * WorldMapConstProperty.getInstance().getWorldResRefreshWidth() - 1;
				int endY = (row + 1) * WorldMapConstProperty.getInstance().getWorldResRefreshHeight() - 1;

				// 最大区域限制
				if (endX > WorldMapConstProperty.getInstance().getWorldMaxX()) {
					endX = WorldMapConstProperty.getInstance().getWorldMaxX();
				}
				if (endY > WorldMapConstProperty.getInstance().getWorldMaxY()) {
					endY = WorldMapConstProperty.getInstance().getWorldMaxY();
				}

				// 构建地图区块
				int areaId = row * areaCols + col + 1;
				AreaObject areaObj = new AreaObject(areaId, beginX, beginY, endX, endY);
				areaList.add(areaObj);
			}
		}
		
		return areaList;
	}

	/**
	 * 加载世界点数据
	 * 
	 * @return
	 */
	private void loadWorldPoints() {
		logger.info("start load world points ...");
		long startTime = HawkTime.getMillisecond();
		
		// 加载世界点
		List<WorldPoint> pointList = WorldPointProxy.getInstance().loadAllPoints(areas);
		if (pointList.size() <= 0) {
			logger.info("start create world points ...");
			pointList = new ArrayList<WorldPoint>();
		}
		logger.info("load world points success, totalLoadCount: {}", pointList.size());
		
		int count = 0;
		for(WorldPoint wp : pointList) {
			if (wp.getFoggyFortressCfg() != null) {
				count++;
			}
		}
		HawkLog.logPrintln("WorldPointService load points, FoggyFortress point total count: {}", count);
		
		// 王座的世界点只放在内存中
		int centerX = WorldMapConstProperty.getInstance().getWorldCenterX();
		int centerY = WorldMapConstProperty.getInstance().getWorldCenterY();
		int areaId = WorldUtil.getAreaId(centerX, centerY);
		int resourceZone = WorldUtil.getPointResourceZone(centerX, centerY);
		WorldPoint kingPlacePoint = new WorldPoint(centerX, centerY, areaId, resourceZone, WorldPointType.KING_PALACE_VALUE);
		pointList.add(kingPlacePoint);
		
		// 总统府箭塔点放在内存中
		PresidentTowerPointId[] presidentTowerPointIds = GsConst.PresidentTowerPointId.values();
		for (int i = 0; i < presidentTowerPointIds.length; i++) {
			int pointId = presidentTowerPointIds[i].intValue();
			int[] pos = GameUtil.splitXAndY(pointId);
			WorldPoint towerPoint = new WorldPoint(pos[0], pos[1], areaId, resourceZone, WorldPointType.CAPITAL_TOWER_VALUE);
			pointList.add(towerPoint);
		}
		
		// 超级武器坐标点放在内存中
		for (int pointId : SuperWeaponService.getInstance().getSuperWeaponPoints()) {
			int[] pos = GameUtil.splitXAndY(pointId);
			WorldPoint point = new WorldPoint(pos[0], pos[1], WorldUtil.getAreaId(pos[0], pos[1]), resourceZone, WorldPointType.SUPER_WEAPON_VALUE);
			pointList.add(point);
		}
		
		// 航海要塞点放在内存中
		for (int[] pos : CrossFortressConstCfg.getInstance().getPosList()) {
			WorldPoint point = new WorldPoint(pos[0], pos[1], WorldUtil.getAreaId(pos[0], pos[1]), resourceZone, WorldPointType.CROSS_FORTRESS_VALUE);
			pointList.add(point);
		}
		
//		// 国家建筑加入世界点
//		for (Entry<Integer, Integer> pointEntry : NationService.getInstance().getAllNationalPoint().entrySet()) {
//			int[] pos = GameUtil.splitXAndY(pointEntry.getKey());
//			WorldPoint point = new WorldPoint(pos[0], pos[1], WorldUtil.getAreaId(pos[0], pos[1]), resourceZone, WorldPointType.NATIONAL_BUILDING_POINT_VALUE);
//			pointList.add(point);
//		}
		
		for (WorldPoint worldPoint : pointList) {
			// 将所有点加入到内存中
			addPoint(worldPoint, true);
		}
		
		logger.info("world point data load finish, count: {}, costtime: {}", points.size(), HawkTime.getMillisecond() - startTime);
	}

	/**
	 * 初始化世界区域
	 * 
	 * @return
	 */
	private boolean initWorldAreas() {
		long startTime = HawkTime.getMillisecond();
		for (AreaObject areaObj : getAreaVales()) {
			if (!ConstProperty.getInstance().isOpenWorldResourceUpdate()) {
				continue;
			}
			notifyAreaUpdate(areaObj, GameConstCfg.getInstance().getAreaUpdateDelayPeriod());
		}
		logger.info("init world areas finish, costtime: {}", HawkTime.getMillisecond() - startTime);
		return true;
	}

	/**
	 * 把世界点注册到场景管理器中
	 * @param worldPoint
	 */
	public void registerIntoScene(WorldPoint worldPoint) {
		int aoiObjId = 0;

		// 根据类型进行添加
		int worldPointType = worldPoint.getPointType();
		switch (worldPointType) {
		case WorldPointType.MONSTER_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.MONSTER, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;

		case WorldPointType.RESOURCE_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.RESOURCE, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;

		case WorldPointType.PLAYER_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.CITY, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;

		case WorldPointType.QUARTERED_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.ARMY, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;

		case WorldPointType.ROBOT_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.CITY, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;

		case WorldPointType.GUILD_TERRITORY_VALUE:
			int searchRadius = GuildConstProperty.getInstance().getManorRadius() * 2;
			aoiObjId = worldScene.add(GsConst.WorldObjType.GUILD_TERRITORY, 0, worldPoint.getX(), worldPoint.getY(), searchRadius, searchRadius, 0, 0, worldPoint);
			break;
			
		case WorldPointType.BOX_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.BOX, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.YURI_FACTORY_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.YURI, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.KING_PALACE_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.PRESIDENT, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.CAPITAL_TOWER_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.PRESIDENT_TOWER, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.STRONG_POINT_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.STRONGPOINT, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.FOGGY_FORTRESS_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.FOGGYFORTESS, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.SUPER_WEAPON_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.SUPER_WEAPON, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.YURI_STRIKE_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.YURI_STRIKE, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.GUNDAM_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.GOUDA, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.NIAN_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.NIAN, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		
		case WorldPointType.TH_MONSTER_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.TREASURE_MON, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;	
			
		case WorldPointType.TH_RESOURCE_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.TREASURE_RES, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		case WorldPointType.WAR_FLAG_POINT_VALUE:
			int searchRadiusA = GuildConstProperty.getInstance().getManorRadius() * 2 + 2;
			aoiObjId = worldScene.add(GsConst.WorldObjType.WAR_FLAG, 0, worldPoint.getX(), worldPoint.getY(), searchRadiusA, searchRadiusA, 0, 0, worldPoint);
			break;	
		case WorldPointType.RESOURC_TRESURE_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.RESOURC_TRESURE, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.CROSS_FORTRESS_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.CROSS_FORTRESS, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.NIAN_BOX_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.NIAN_BOX, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.PYLON_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.PYLON, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.SNOWBALL_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.SNOWBALL, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.CHRISTMAS_BOSS_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.CHRISTMAS_BOSS, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.CHRISTMAS_BOX_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.CHRISTMAS_BOX, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.DRAGON_BOAT_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.DRAGON_BOAT, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.GHOST_TOWER_MONSTER_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.GHOST_TOWER_MONSTER, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.CAKE_SHARE_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.CAKE_SHARE, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.XIAO_ZHAN_QU_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.XQZ_BUILD, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.NATIONAL_BUILDING_POINT_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.NATION_BUILD, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.RESOURCE_SPREE_BOX_VALUE:
			aoiObjId = worldScene.add(GsConst.WorldObjType.RESOURCE_SPREE_BOX, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.SPACE_MECHA_MAIN_VALUE: // 星甲召唤主舱体
			aoiObjId = worldScene.add(GsConst.WorldObjType.SPACE_MECHA_MAIN, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.SPACE_MECHA_SLAVE_VALUE: // 星甲召唤子舱体
			aoiObjId = worldScene.add(GsConst.WorldObjType.SPACE_MECHA_SLAVE, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.SPACE_MECHA_MONSTER_VALUE: // 星甲召唤怪物点
			aoiObjId = worldScene.add(GsConst.WorldObjType.SPACE_MECHA_MONSTER, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE: // 星甲召唤据点
			aoiObjId = worldScene.add(GsConst.WorldObjType.SPACE_MECHA_STRONG_HOLD, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
		case WorldPointType.SPACE_MECHA_BOX_VALUE: // 星甲召唤掉落的宝箱
			aoiObjId = worldScene.add(GsConst.WorldObjType.SPACE_MECHA_BOX, 0, worldPoint.getX(), worldPoint.getY(), worldPoint);
			break;
			
		}
		worldPoint.resetAoiObjId(aoiObjId);
	}

	/**
	 * 获取某个类型的所有点
	 * @param pointType
	 * @return
	 */
	public List<WorldPoint> getWorldPointsByType(WorldPointType pointType) {
		List<WorldPoint> points = new ArrayList<WorldPoint>();
		for (WorldPoint worldPoint : this.points.values()) {
			if (worldPoint.getPointType() == pointType.getNumber()) {
				points.add(worldPoint);
			}
		}
		return points;
	}

	/**
	 * 移除世界点
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean removeWorldPoints(Collection<Integer> pointIds, boolean deleteEntity) {
		if (pointIds == null || pointIds.isEmpty()) {
			return true;
		}
		
		long startTime = HawkTime.getMillisecond();

		List<WorldPoint> removePoints = new ArrayList<WorldPoint>(pointIds.size());
		for (int pointId : pointIds) {
			WorldPoint rmPoint = removeWorldPoint(pointId, false);
			if (rmPoint != null) {
				removePoints.add(rmPoint);
			}
		}
		
		if (pointIds.size() > 1) {
			logger.info("remove world point before db, count: {}, costtime: {}", pointIds.size(), HawkTime.getMillisecond() - startTime);
		}
		
		WorldPointProxy.getInstance().batchDelete(removePoints);
		
		logger.info("remove world point, count: {}, size: {}, costtime: {}", pointIds.size(), removePoints.size(), HawkTime.getMillisecond() - startTime);
		return true;
	}

	/**
	 * 删除世界点
	 * @param x
	 * @param y
	 * @return
	 */
	public WorldPoint removeWorldPoint(int x, int y) {
		return removeWorldPoint(GameUtil.combineXAndY(x, y), true);
	}

	/**
	 * 删除世界点
	 * @param x
	 * @param y
	 * @return
	 */
	public WorldPoint removeWorldPoint(int pointId) {
		return removeWorldPoint(pointId, true);
	}

	/**
	 * 移除世界点
	 * 
	 * @param pointId
	 * @return
	 */
	public WorldPoint removeWorldPoint(int pointId, boolean deleteEntity) {
		int[] posInfo = GameUtil.splitXAndY(pointId);
		AreaObject areaObj = getArea(posInfo[0], posInfo[1]);
		if (areaObj == null) {
			return null;
		}
		
		WorldPoint worldPoint = getWorldPoint(pointId);
		if (worldPoint == null) {
			return null;
		}
		
		// 先删关联占用点信息
		if (WorldUtil.isPlayerPoint(worldPoint)) {
			// 移除玩家的id信息
			WorldPlayerService.getInstance().rmFromPlayerPos(worldPoint.getPlayerId());
			// 删除玩家领地buff
			Player player = GlobalData.getInstance().getActivePlayer((worldPoint.getPlayerId()));
			GuildManorService.getInstance().notifyManorBuffChange(player);
			
			if (WorldRobotService.getInstance().isRobotId(worldPoint.getPlayerId())) {
				deleteEntity = false;
				WorldRobotService.getInstance().notifyRobotRemove(worldPoint.getId(), worldPoint.getPlayerId());
			}
		}
		
		// 删除本身点信息, 并计入到空闲点列表
		rmFromPoints(worldPoint.getId());
		// 移除世界场景
		worldScene.leave(worldPoint.getAoiObjId());
		// 恢复空闲点
		addToAreaFreePoint(worldPoint);
		
		// 删除数据库
		if (deleteEntity) {
			WorldPointProxy.getInstance().delete(worldPoint);
		}
		
		logger.info("pointOperation, remove, type:{}, playerId:{}, pos:{}{}, deleteEntity:{}, aoiObjId:{}", worldPoint.getPointType(), worldPoint.getPlayerId(),
				worldPoint.getX(), worldPoint.getY(), deleteEntity, worldPoint.getAoiObjId());
		
		return worldPoint;
	}

	/**
	 * 打印地图点信息
	 */
	private void logWorldPointInfo() {
		int monsterCount = 0, resourceCount = 0, playerCityCount = 0, cityOccupyCount = 0, marchArmyCount = 0;
		for (WorldPoint worldPoint : points.values()) {
			int pointType = worldPoint.getPointType();
			switch (pointType) {
			case WorldPointType.MONSTER_VALUE:
				monsterCount++;
				logger.debug("world point, type: {}, monster: {}, pos: ({}, {})", 
						pointType, worldPoint.getMonsterId(), worldPoint.getX(), worldPoint.getY());
				break;
				
			case WorldPointType.RESOURCE_VALUE:
				resourceCount++;
				logger.debug("world point, type: {}, resource: {}, player: {}, marchId: {}, pos: ({}, {})", 
						pointType, worldPoint.getResourceId(), worldPoint.getPlayerId(), worldPoint.getMarchId(), worldPoint.getX(), worldPoint.getY());
				break;
				
			case WorldPointType.PLAYER_VALUE:
				playerCityCount++;
				logger.debug("world point, type: {}, player: {}, pos: ({}, {})", 
						pointType, worldPoint.getPlayerId(), worldPoint.getX(), worldPoint.getY());
				break;
				
			case WorldPointType.OCCUPIED_VALUE:
				cityOccupyCount++;
				logger.debug("world point, type: {}, player: {}, pos: ({}, {})", 
						pointType, worldPoint.getPlayerId(), worldPoint.getX(), worldPoint.getY());
				break;
				
			case WorldPointType.QUARTERED_VALUE:
				marchArmyCount++;
				logger.debug("world point, type: {}, playerId: {}, marchId: {}, pos: ({}, {})", 
						pointType, worldPoint.getPlayerId(), worldPoint.getMarchId(), worldPoint.getX(), worldPoint.getY());
				break;
			}
		}
		
		logger.info("world points, monster: {}, resource: {}, city: {}, occupy: {}, army: {}", 
				monsterCount, resourceCount, playerCityCount, cityOccupyCount, marchArmyCount);
	}

	/**
	 * 获取中心点以及占用点，不分是否空闲
	 */
	public List<Point> getRhoAroundPointsAll(int centerX, int centerY, int radiusX, int radiusY) {
		return getRhoAroundPointsDetail(centerX, centerY, radiusX, radiusY, false, false);
	}

	/**
	 * 获取半径内的占用点，不包含中心点，不分是否空闲
	 */
	public List<Point> getRhoAroundPointsAll(int centerX, int centerY, int radius) {
		return getRhoAroundPointsDetail(centerX, centerY, radius, radius, false, true);
	}

	/**
	 * 获取中心点以及占用点，不分是否空闲
	 */
	public List<Point> getRhoAroundPointsAll(int pointId, int radius) {
		int[] pos = GameUtil.splitXAndY(pointId);
		return getRhoAroundPointsDetail(pos[0], pos[1], radius, radius, false, true);
	}

	/**
	 * 获取中心点以及占用点，只取空闲点
	 */
	public List<Point> getRhoAroundPointsFree(int centerX, int centerY, int radiusX, int radiusY) {
		return getRhoAroundPointsDetail(centerX, centerY, radiusX, radiusY, true, true);
	}

	/**
	 * 获取中心点以及占用点，只取空闲点
	 */
	public List<Point> getRhoAroundPointsFree(int centerX, int centerY, int radius) {
		return getRhoAroundPointsDetail(centerX, centerY, radius, radius, true, true);
	}

	/**
	 * 获取中心点以及占用点，只取空闲点
	 */
	public List<Point> getRhoAroundPointsFree(int pointId, int radius) {
		int[] pos = GameUtil.splitXAndY(pointId);
		return getRhoAroundPointsDetail(pos[0], pos[1], radius, radius, true, true);
	}

	/**
	 * 计算特定中心点周围的有效点, 不包含中心点本身(菱形区域)
	 * @param centerX
	 * @param centerY
	 * @param radiusX
	 * @param radiusY
	 * @param needFree
	 * @param isNewList 是否需要生成新的数据结构。默认true。
	 *         性能优化考虑，建议用false。
	 * 		      搜索范围较大，建议用false。
	 *         当使用false的时候，当前线程只能存在一个有效的List(该方法返回的)。 即下一次调用该函数，上一次调用该函数返回的List不可再使用。
	 * @return
	 */
	public List<Point> getRhoAroundPointsDetail(int centerX, int centerY, int radiusX, int radiusY, boolean needFree, boolean isNewList) {
		List<Point> aroundPoints = null;
		if (isNewList) {
			aroundPoints = new ArrayList<>();
		} else {
			aroundPoints = AroundPoints.getAroundPoints();
		}
		
		if (radiusX <= 0 || radiusY <= 0) {
			return aroundPoints;
		}

		// 取x轴上的点
		for (int i = 1; i <= radiusX - 1; i++) {
			int x1 = centerX + i;
			int x2 = centerX - i;
			Point p1 = getAreaPoint(x1, centerY, needFree);
			if (p1 != null) {
				aroundPoints.add(p1);
			}
			Point p2 = getAreaPoint(x2, centerY, needFree);
			if (p2 != null) {
				aroundPoints.add(p2);
			}
		}

		// 取y轴上的点
		for (int i = 1; i <= radiusY - 1; i++) {
			int y1 = centerY + i;
			int y2 = centerY - i;
			Point p1 = getAreaPoint(centerX, y1, needFree);
			if (p1 != null) {
				aroundPoints.add(p1);
			}
			Point p2 = getAreaPoint(centerX, y2, needFree);
			if (p2 != null) {
				aroundPoints.add(p2);
			}
		}
		
		// 取其它点
		for (int i = 0; i <= radiusX - 1; i++) {
			for (int j = 0; j <= radiusY - 1 - i; j++) {
				// 不要中心点和坐标轴点
				if (i == 0 || j == 0) {
					continue;
				}
				
				int x1 = centerX + i;
				int x2 = centerX - i;
				int y1 = centerY + j;
				int y2 = centerY - j;
				
				Point p1 = getAreaPoint(x1, y1, needFree);
				if (p1 != null) {
					aroundPoints.add(p1);
				}

				Point p2 = getAreaPoint(x1, y2, needFree);
				if (p2 != null) {
					aroundPoints.add(p2);
				}
				
				Point p3 = getAreaPoint(x2, y1, needFree);
				if (p3 != null) {
					aroundPoints.add(p3);
				}
				
				Point p4 = getAreaPoint(x2, y2, needFree);
				if (p4 != null) {
					aroundPoints.add(p4);
				}
			}
		}
		return aroundPoints;
	}

	/**
	 * 获取区域内的点(长方形区域),不包含边缘点
	 * @param centerPoint
	 * @param radiusX
	 * @param radiusY
	 * @return
	 */
	public Map<Integer, Point> getAroundPoints(int centerX, int centerY, int radiusX, int radiusY) {
		Map<Integer, Point> aroundPoints = new HashMap<Integer, Point>();
		for (int i = 0; i < radiusX; i++) {
			int x1 = centerX + i;
			int x2 = centerX - i;
			for (int j = 0; j < radiusY; j++) {
				int y1 = centerY + j;
				int y2 = centerY - j;
				Point p1 = getAreaPoint(x1, y1, false);
				if (p1 != null) {
					aroundPoints.put(p1.getId(), p1);
				}
				Point p2 = getAreaPoint(x1, y2, false);
				if (p2 != null) {
					aroundPoints.put(p2.getId(), p2);
				}
				Point p3 = getAreaPoint(x2, y1, false);
				if (p3 != null) {
					aroundPoints.put(p3.getId(), p3);
				}
				Point p4 = getAreaPoint(x2, y2, false);
				if (p4 != null) {
					aroundPoints.put(p4.getId(), p4);
				}
			}
		}
		return aroundPoints;
	}

	/**
	 * 获取地图上的点
	 * @param x
	 * @param y
	 * @return
	 */
	public Point getAreaPoint(int x, int y, boolean needFree) {
		AreaObject areaObj = getArea(x, y);
		if (areaObj == null) {
			return null;
		}
		return needFree ? areaObj.getFreePoint(x, y) : areaObj.getAreaPoint(x, y);
	}

	/**
	 * 添加世界点
	 * 
	 * @param worldPoint
	 */
	public boolean createWorldPoint(WorldPoint worldPoint) {
		
		AreaObject areaObj = getArea(worldPoint.getAreaId());
		if (areaObj == null || !isPointNull(worldPoint.getId())) {
			return false;
		}
		if (!WorldPointProxy.getInstance().create(worldPoint)) {
			return false;
		}
		addPoint(worldPoint);
		if (worldPoint.getPointType() == WorldPointType.PLAYER_VALUE && !HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
			WorldPlayerService.getInstance().addPlayerPos(worldPoint);
		}
		
		logger.info("pointOperation, create, type:{}, playerId:{}, pos:{}{}, aoiObjId:{}", worldPoint.getPointType(), worldPoint.getPlayerId(), worldPoint.getX(),
				worldPoint.getY(), worldPoint.getAoiObjId());
		
		return true;
	}
	
	/**
	 * 检查点是否可用
	 * @param areaObj
	 * @param centerPoint
	 * @param distance
	 * @return
	 */
	public boolean tryOccupied(AreaObject areaObj, Point centerPoint, int distance) {
		// 距离边界的长度
		int boundary = GsConst.WORLD_BOUNDARY_SIZE;
		// 边界坐标点不可用的检测
		if (centerPoint.getX() <= boundary || centerPoint.getY() < boundary || centerPoint.getX() >= (WorldMapConstProperty.getInstance().getWorldMaxX() - boundary)
				|| centerPoint.getY() >= WorldMapConstProperty.getInstance().getWorldMaxY() - boundary) {
			return false;
		}
		// 中心点是否被占用
		WorldPoint currPoint = getWorldPoint(centerPoint.getX(), centerPoint.getY());
		if (currPoint != null) {
			return false;
		}
		// 城点距离限制范围内的所有点(1距离为4个点, 2距离为12个点)
		List<Point> freeAroundPoints = getRhoAroundPointsFree(centerPoint.getX(), centerPoint.getY(), distance);
		// 必须为4个, 否则就是有阻挡点存在
		if (freeAroundPoints.size() != 2 * distance * (distance - 1)) {
			return false;
		}
		return true;
	}

	/**
	 * 根据坐标获得世界点信息，若是未被占用的点就返回空
	 * @param x
	 * @param y
	 * @return
	 */
	public WorldPoint getWorldPoint(int x, int y) {
		return this.getWorldPoint(GameUtil.combineXAndY(x, y));
	}

	/**
	 * 获取实体点ID
	 * @param pointId
	 * @return
	 */
	public WorldPoint getWorldPoint(int pointId) {
		int[] pos = GameUtil.splitXAndY(pointId);
		return points.get(getWorldIndex(pos[0], pos[1]));
	}

	/**
	 * 获取区域
	 * 
	 * @return
	 */
	public Collection<AreaObject> getAreaVales() {
		return Collections.unmodifiableCollection(areas.values());
	}

	
	/**
	 * 获取点所在区域
	 * @param x
	 * @param y
	 * @return
	 */
	public AreaObject getArea(int x, int y) {
		return areas.get(WorldUtil.getAreaId(x, y));
	}

	/**
	 * 获取点所在区域
	 * @param areaId
	 * @return
	 */
	public AreaObject getArea(int areaId) {
		return areas.get(areaId);
	}
	
	/**
	 * 添加世界点
	 * 并从区域空闲点中删除此点
	 * @param worldPoint
	 * @return
	 */
	public void addPoint(WorldPoint worldPoint) {
        if (worldPoint.getFoggyFortressCfg() != null) {
			HawkLog.logPrintln("WorldPointService add point, x: {}, y: {}, pointType: {}, foggy fortress type: {}", worldPoint.getX(), worldPoint.getY(), worldPoint.getPointType(), worldPoint.getFoggyFortressCfg() != null);
        }
		this.addPoint(worldPoint, false);
	}

	/**
	 * 添加世界点
	 * 并从区域空闲点中删除此点
	 * @param worldPoint
	 * @return
	 */
	public void addPoint(WorldPoint worldPoint, boolean init) {
		synchronized (this) {
			if (points.containsKey(worldPoint.getId())) {
				return;
			}
			// 加入全局
			points.put(worldPoint.getId(), worldPoint);
		}

		// 区域中移除空闲点
		rmFromAreaFreePoint(worldPoint);
		// 添加区域野怪数量
		addAreaInfoCount(worldPoint, init);
		// 注册到场景管理器中
		registerIntoScene(worldPoint);
	}

	/**
	 * 区域中移除空闲点
	 * @param worldPoint
	 */
	private void rmFromAreaFreePoint(WorldPoint worldPoint) {
		// 自身点从AreaObject移除
		AreaObject areaObject = getArea(worldPoint.getAreaId());
		areaObject.removeFreePoint(worldPoint.getX(), worldPoint.getY());
		// 周围占用点从AreaObject移除
		List<Point> aroundPoints = getRhoAroundPointsFree(worldPoint.getId(), getWorldPointRadius(worldPoint));
		for (Point point : aroundPoints) {
			AreaObject aroundAreaObject = getArea(point.getAreaId());
			aroundAreaObject.removeFreePoint(point.getX(), point.getY());
		}
	}

	/**
	 * 添加区域野怪或宝箱数量
	 * @param worldPoint
	 */
	private void addAreaInfoCount(WorldPoint worldPoint, boolean init) {
		if (worldPoint.getPointType() == WorldPointType.FOGGY_FORTRESS_VALUE){
			// 区域
			AreaObject areaObject = getArea(worldPoint.getAreaId());
			//配置
			FoggyFortressCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, worldPoint.getMonsterId());
			if(cfg == null){
				logger.error("addFoggyCount, FoggyFortressCfg null, monsterId:{}", worldPoint.getMonsterId());
				return;
			}
			// 添加区域迷雾id数量
			areaObject.addFoggyIdNum(cfg.getLevel());
		}
	}

	/**
	 * 区域中添加空闲点
	 * @param worldPoint
	 */
	public void addToAreaFreePoint(WorldPoint worldPoint) {
		// 自身点从AreaObject移除
		AreaObject areaObject = getArea(worldPoint.getAreaId());
		areaObject.addFreePoint(worldPoint.getX(), worldPoint.getY());

		// 周围占用点从AreaObject移除
		List<Point> aroundPoints = getRhoAroundPointsAll(worldPoint.getId(), getWorldPointRadius(worldPoint));
		for (Point point : aroundPoints) {
			AreaObject aroundAreaObject = getArea(point.getAreaId());
			aroundAreaObject.addFreePoint(point.getX(), point.getY());
		}
	}

	/**
	 * 获取点的占用半径
	 * @param worldPoint
	 */
	private int getWorldPointRadius(WorldPoint worldPoint) {
		int radius = 0;
		switch (worldPoint.getPointType()) {
		// 玩家城点
		case WorldPointType.PLAYER_VALUE:
		case WorldPointType.YURI_FACTORY_VALUE:
		case WorldPointType.FOGGY_FORTRESS_VALUE:
		case WorldPointType.GUNDAM_VALUE:
		case WorldPointType.NIAN_VALUE:
		case WorldPointType.TH_MONSTER_VALUE:
		case WorldPointType.TH_RESOURCE_VALUE:
		case WorldPointType.PYLON_VALUE:
		case WorldPointType.CHRISTMAS_BOSS_VALUE:
		case WorldPointType.DRAGON_BOAT_VALUE:
		case WorldPointType.RESOURC_TRESURE_VALUE:
			radius = GsConst.PLAYER_POINT_RADIUS;
			break;
		// 联盟建筑
		case WorldPointType.GUILD_TERRITORY_VALUE:
			TerritoryType type = TerritoryType.valueOf(worldPoint.getBuildingId());
			radius = GuildManorService.getInstance().getRadiusByType(type);
			break;
		case WorldPointType.MONSTER_VALUE:
			radius = WorldMonsterService.getInstance().getMonsterRadius(worldPoint.getMonsterId());
			break;
		case WorldPointType.CAKE_SHARE_VALUE:
			radius = GsConst.CAKE_SHARE_RADIUS;
			break;
		case WorldPointType.SPACE_MECHA_MAIN_VALUE:
			radius = SpaceMechaGrid.SPACE_MAIN_GRID;
			break;
		case WorldPointType.SPACE_MECHA_SLAVE_VALUE:
			radius = SpaceMechaGrid.SPACE_SLAVE_GRID;
			break;
		case WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE:
			radius = SpaceMechaGrid.STRONG_HOLD_GRID;
			break;
		case WorldPointType.SPACE_MECHA_MONSTER_VALUE:
			radius = SpaceMechaGrid.MONSTER_GRID;
			break;
		case WorldPointType.SPACE_MECHA_BOX_VALUE:
			radius = SpaceMechaGrid.MECHA_BOX;
			break;
		default:
			radius = 1;
			break;
		}
		return radius;
	}

	/**
	 * 从世界点中移除
	 * @param pointId
	 */
	public void rmFromPoints(int pointId) {
		points.remove(pointId);
	}

	/**
	 * 点是否为null
	 * @param pointId
	 * @return
	 */
	private boolean isPointNull(int pointId) {
		return !points.containsKey(pointId);
	}

	/**
	 * 是否在黑土地区域
	 * @param pointId
	 * @return
	 */
	public boolean isInCapitalArea(int pointId) {
		return capitalAreaIds.contains(pointId);
	}

	/**
	 * 获取世界场景信息
	 * @return
	 */
	public WorldScene getWorldScene() {
		return worldScene;
	}

	/**
	 * 世界点被占领
	 * @param x
	 * @param y
	 * @param marchId
	 * @param playerId
	 * @param type
	 * @return
	 */
	public WorldPoint notifyPointOccupied(int pointId, Player player, IWorldMarch march, WorldPointType type) {
		if (pointId < 0 || player == null || march == null || type == null) {
			return null;
		}
		int[] pos = GameUtil.splitXAndY(pointId);
		if (type == WorldPointType.QUARTERED) { // 驻扎
			return notifyQuarteredPointOccupy(player, march.getMarchEntity(), pos[0], pos[1]);
		}

		if (type == WorldPointType.RESOURCE) { // 采集
			return WorldResourceService.getInstance().notifyResourcePointOccupy(player, march, pos[0], pos[1]);
		}
		
		return null;
	}

	/**
	 * 通知指定点的驻扎完成
	 * 
	 * @param worldPoint
	 * @return
	 */
	public void notifyQuarteredFinish(WorldPoint worldPoint) {
		// 点为null
		if (worldPoint == null) {
			logger.warn("notify quartered finish, world point null");
			return;
		}

		// 不是驻扎点
		if (worldPoint.getPointType() != WorldPointType.QUARTERED_VALUE) {
			logger.warn("notify quartered finish, world point not quartered value");
			return;
		}

		// 移除向驻扎点行军的联盟战争界面信息
		String guildId = GuildService.getInstance().getPlayerGuildId(worldPoint.getPlayerId());
		Collection<IWorldMarch> guildMarchs = WorldMarchService.getInstance().getGuildMarchs(guildId);
		for (IWorldMarch guildMarch : guildMarchs) {
			if (guildMarch.getMarchEntity().getTerminalId() == worldPoint.getId()) {
				WorldMarchService.getInstance().rmGuildMarch(guildMarch.getMarchId());
				continue;
			}
		}

		try {
			// 删除原有的驻扎点数据
			removeWorldPoint(worldPoint.getX(), worldPoint.getY());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取视野范围内的有内容的点 [建筑物，资源，怪]
	 * 
	 * @param x
	 *            中心点坐标
	 * @param y
	 *            中心点坐标
	 * @return
	 */
	public List<WorldPointPB.Builder> getWorldPointsInView(String playerId, int x, int y, float moveSpeed) {
		long startTime = HawkTime.getMillisecond();
		List<WorldPointPB.Builder> builderList = null;

		builderList = worldScene.getPlayerViewObjs(playerId, x, y, moveSpeed);

		logger.info("fetch inview world points, pos: ({}, {}), count: {}, costtime: {}", x, y, builderList.size(), HawkTime.getMillisecond() - startTime);

		return builderList;
	}

	/**
	 * 给在地图上的玩家广播协议
	 * 
	 * @param protocol
	 */
	public void broadcastProtocol(HawkProtocol protocol) {
		HawkNetworkManager.getInstance().broadcastProtocol(protocol, new HawkFilter() {
			@Override
			public boolean doFilter(Object obj) {
				if (obj instanceof HawkSession) {
					HawkSession session = (HawkSession) obj;
					if (session == null || session.getAppObject() == null) {
						return false;
					}
					Player player = (Player) session.getAppObject();
					if (player.getAoiObjId() > 0) {
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * 刷新世界点被攻击效果
	 * 
	 * @param worldPoint
	 * @param commonHurtEndTime
	 *            被攻击伤害持续截止时间戳
	 * @param nuclearHurtEndTime
	 *            核爆伤害持续截止时间戳
	 * @param weatherHurtEndTime
	 *            雷暴伤害持续截止时间戳
	 */

	public void notifyWorldPointBeAttacked(WorldPoint worldPoint, long commonHurtEndTime) {
		if (worldPoint == null) {
			return;
		}
		if (commonHurtEndTime != 0) {
			worldPoint.setCommonHurtEndTime(commonHurtEndTime);
		}
		worldScene.update(worldPoint.getAoiObjId());
	}
	
	/**
	 * 城点灭火通知
	 * @param playerId
	 */
	public void notifyWorldPointOutFire(String playerId) {
		int[] posInfo = WorldPlayerService.getInstance().getPlayerPosXY(playerId);
		WorldPoint worldPoint = getWorldPoint(posInfo[0], posInfo[1]);
		notifyWorldPointBeAttacked(worldPoint, HawkTime.getMillisecond());
	}

	/**
	 * 获取世界同步标记
	 * 
	 * @return
	 */
	public int getWorldSyncFlag() {
		return worldSyncFlag;
	}

	/**
	 * 设置世界同步的标记状态
	 * 
	 * @param flag
	 */
	public void setWorldSyncFlag(int flag) {
		worldSyncFlag = flag;
	}

	/**
	 * 更新一个点的数据
	 * 
	 * @param x
	 * @param y
	 */
	public void notifyPointUpdate(int x, int y) {
		WorldPoint worldPoint = getWorldPoint(x, y);
		if (worldPoint == null) {
			return;
		}
		// 通知场景点的变化
		worldScene.update(worldPoint.getAoiObjId());
	}

	/**
	 * 获得视野范围内
	 * 
	 * @param playerId
	 * @param x
	 * @param y
	 * @param moveSpeed
	 * @return
	 */
	public List<Builder> getPlayerViewObjs(String playerId, int x, int y, float moveSpeed) {
		return worldScene.getPlayerViewObjs(playerId, x, y, moveSpeed);
	}

	/**
	 * 获得周围指定type的点
	 * 
	 * @param centerX
	 * @param centerY
	 * @param radiusX
	 * @param radiusY
	 * @return
	 */
	public List<WorldPoint> getAroundWorldPointsWithType(int centerX, int centerY, int radiusX, int radiusY, int pointType) {
		List<Point> points = getRhoAroundPointsAll(centerX, centerY, radiusX, radiusY);
		List<WorldPoint> aroundWorldPoints = new ArrayList<WorldPoint>(points.size());
		for (Point point : points) {
			WorldPoint worldPoint = getWorldPoint(point.getX(), point.getY());
			if (worldPoint != null && worldPoint.getPointType() == pointType) {
				aroundWorldPoints.add(worldPoint);
			}
		}
		return aroundWorldPoints;
	}

	/**
	 * 获取当前区块数量
	 * 
	 * @return
	 */
	public int getAreaSize() {
		return areas.size();
	}

	/**
	 * 获取当前世界点的数量
	 * 
	 * @return
	 */
	public int getWorldPointSize() {
		return points.size();
	}

	/**
	 * 驻扎点被占领
	 * 
	 * @param player
	 * @param march
	 * @param x
	 * @param y
	 * @return
	 */
	private WorldPoint notifyQuarteredPointOccupy(Player player, WorldMarch march, int x, int y) {
		// 先移除原有的驻扎信息
		removeWorldPoint(x, y);

		// 从区域获得对应空闲点之后进行新世界点的创建
		AreaObject areaObj = getArea(x, y);
		if (areaObj == null) {
			return null;
		}

		Point point = areaObj.getAreaPoint(x, y);

		WorldPoint worldPoint = new WorldPoint(x, y, areaObj.getId(), point.getZoneId(), WorldPointType.QUARTERED_VALUE);
		// 初始化玩家和行军信息
		worldPoint.initPlayerInfo(player.getData());
		worldPoint.setMarchId(march.getMarchId());

		// 创建新的世界点信息
		if (!createWorldPoint(worldPoint)) {
			return null;
		}
		return worldPoint;
	}

	/**
	 * 给一个点视野内的玩家推送数据
	 * 
	 * @param x
	 * @param y
	 */
	public void broadcastScene(int x, int y, HawkProtocol protocol) {
		WorldPoint worldPoint = getWorldPoint(x, y);
		if (worldPoint == null) {
			return;
		}
		// 通知场景点的变化
		worldScene.broadcastProtocol(worldPoint.getAoiObjId(), protocol);
	}

	/**
	 * 区域更新, 主要用来刷新资源和怪
	 * 
	 * @param areaObj
	 * @param isInitUpdate
	 * @param futureTime
	 */
	public void notifyAreaUpdate(AreaObject areaObj, int futureTime) {
		// 空的区块
		if (areaObj.getTotalPointCount() <= 0) {
			return;
		}
		long startTime = HawkTime.getMillisecond();
		int resCountInCapitalArea = 0; // 资源数量(黑土地中)
		int resCountNotInCapitalArea = 0; // 资源数量(不在黑土地中)
		int monster2Count = 0; // 野怪数量
		int strongpointCount = 0; //据点数量
		int foggyCountInCapitalArea = 0; //要塞数量(在黑土地中)
		int foggyCountNotInCapitalArea = 0; //要塞数量(不在黑土地中)

		List<Point> usedPoints = areaObj.getUsedPoints(); // 已经使用的点集合
		List<Integer> removePoints = new ArrayList<Integer>(); // 移除的点

		for (Point point : usedPoints) {
			WorldPoint worldPoint = getWorldPoint(point.getX(), point.getY());
			if (worldPoint == null || !worldPoint.isLifeEndDead(futureTime)) {
				continue;
			}

			if (worldPoint.getPointType() == WorldPointType.RESOURCE_VALUE) {
				continue;
			}

			if (worldPoint.getPointType() == WorldPointType.MONSTER_VALUE) {
				continue;
			}
			
			if(worldPoint.getPointType() == WorldPointType.STRONG_POINT_VALUE){
				continue;
			} 
			
			if (worldPoint.getPointType() == WorldPointType.BOX_VALUE) {
				areaObj.deleteBoxNum(1);

			} else if(worldPoint.getPointType() == WorldPointType.FOGGY_FORTRESS_VALUE){
				YuriFactoryPoint yuriFactoryPoint = WorldFoggyFortressService.getInstance().getYuriFactoryPoint(point.getId());
				if(yuriFactoryPoint != null && yuriFactoryPoint.isInActive()){
					worldPoint.setLifeStartTime(HawkTime.getMillisecond());
					continue;
				}
				if(!HawkOSOperator.isEmptyString(worldPoint.getPlayerId()) || !HawkOSOperator.isEmptyString(worldPoint.getMarchId())){
					continue;
				}
				if (WorldPointService.getInstance().isInCapitalArea(worldPoint.getId())) {
					foggyCountInCapitalArea++;
				} else {
					foggyCountNotInCapitalArea++;
				}
				//移除尤里点
				WorldFoggyFortressService.getInstance().removeYuriPoint(point.getId());
				//获取配置
				FoggyFortressCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, worldPoint.getMonsterId());
				if(enemyCfg == null){
					logger.error("addFoggyCount, FoggyFortressCfg null, monsterId:{}", worldPoint.getMonsterId());
				} else {
					//删除区域里的迷雾点
					areaObj.delFoggyIdNum(enemyCfg.getLevel());
				}
			} else {
				// 只刷新怪物和资源点，其他不刷
				continue;
			}

			// 添加到删除列表
			removePoints.add(point.getId());
		}
		// 移除过期点
		removeWorldPoints(removePoints, true);
		// 刷新迷雾要塞的点
		WorldFoggyFortressService.getInstance().bornFoggyFortressOnArea(areaObj, null, foggyCountInCapitalArea, false, true);
		WorldFoggyFortressService.getInstance().bornFoggyFortressOnArea(areaObj, null, foggyCountNotInCapitalArea, false, false);
		
		notifyBoxRefresh(areaObj);
		// 日志记录
		if (resCountInCapitalArea + monster2Count + strongpointCount > 0) {
			logger.info("update area resource and monster, areaId: {}, monsterCount: {}, resCountInCapitalArea: {}, resCountNotInCapitalArea: {}, strongpointCount: {}, foggyCountInCapitalArea: {}, foggyCountNotInCapitalArea: {}, costtime: {}", areaObj.getId(), monster2Count,
					resCountInCapitalArea, resCountNotInCapitalArea, strongpointCount, foggyCountInCapitalArea, foggyCountNotInCapitalArea, HawkTime.getMillisecond() - startTime);
		}
	}

	/**
	 * 通知宝箱刷新
	 * @param areaObject
	 */
	public void notifyBoxRefresh(AreaObject areaObject) {
		// 当期宝箱数量
		int currBoxCount = areaObject.getBoxNum();
		// 区域宝箱最大数量
		int maxBoxCount = areaObject.getTotalPointCount() * WorldMapConstProperty.getInstance().getWorldBoxRefreshMax() / 1000 / GsConst.POINT_TO_GRUD;
		if (currBoxCount >= maxBoxCount) {
			return;
		}

		// 需要生成的宝箱数量
		int genCount = maxBoxCount - currBoxCount;

		List<WorldPoint> bornBoxList = new ArrayList<WorldPoint>(genCount);

		// 找出刷新怪物的点
		List<Point> pointList = areaObject.getValidPoints(WorldPointType.BOX_VALUE, null);
		// 乱序
		Collections.shuffle(pointList);
		// 刷宝箱
		WorldEnemyRefreshCfg boxRefresh = WorldMonsterService.getInstance().getCurrentEnemyRefreshCfg();
		if (boxRefresh == null) {
			logger.error("can not find WorldEnemyRefreshCfg !!!");
			return;
		}

		// 生成所需要的点
		for (int i = 0; i < genCount && i < pointList.size(); i++) {
			Point bornPoint = pointList.get(i);
			WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.BOX_VALUE);
			worldPoint.setLifeStartTime(HawkTime.getMillisecond());
			worldPoint.setMonsterId(boxRefresh.getBoxEnemyIds().get(0));
			bornBoxList.add(worldPoint);
			logger.info("box born, monsterId: {}, pos: ({}, {}), areaId:{}", worldPoint.getMonsterId(), bornPoint.getX(),bornPoint.getY(), areaObject.getId());
		}

		// 创建所有需要占用点
		if (WorldPointProxy.getInstance().batchCreate(bornBoxList)) {
			for (WorldPoint worldPoint : bornBoxList) {
				addPoint(worldPoint);
			}
		}
	}
	
	/**
	 * 通过点获取联盟id
	 * @param worldPoint
	 */
	public String getGuildIdByPoint(WorldPoint worldPoint) {
		if (worldPoint == null) {
			return null;
		}
		
		String guildId = null;
		
		int pointType = worldPoint.getPointType();
		switch (pointType) {
		case WorldPointType.PLAYER_VALUE:
			String playerId = worldPoint.getPlayerId();
			guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			break;
		case WorldPointType.QUARTERED_VALUE:
		case WorldPointType.RESOURCE_VALUE:
		case WorldPointType.STRONG_POINT_VALUE:
		case WorldPointType.CHRISTMAS_BOX_VALUE:
			playerId = worldPoint.getPlayerId();
			if (!HawkOSOperator.isEmptyString(playerId)) {
				guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			}
			break;
		case WorldPointType.GUILD_TERRITORY_VALUE:
			guildId = worldPoint.getGuildId();
			break;
		case WorldPointType.KING_PALACE_VALUE:
			guildId = PresidentFightService.getInstance().getCurrentGuildId();
			break;
		case WorldPointType.CAPITAL_TOWER_VALUE:
			guildId = PresidentFightService.getInstance().getPresidentTowerGuild(worldPoint.getId());
			break;
		case WorldPointType.SUPER_WEAPON_VALUE:
			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(worldPoint.getId());
			guildId = weapon.getGuildId();
			break;
		case WorldPointType.WAR_FLAG_POINT_VALUE:
			IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			if (flag != null) {
				guildId = flag.getCurrentId();
			}
			break;
		case WorldPointType.CROSS_FORTRESS_VALUE:
			Player fortressLeader = WorldMarchService.getInstance().getFortressLeader(worldPoint.getId());
			if (fortressLeader != null && fortressLeader.hasGuild()) {
				guildId = fortressLeader.getGuildId();
			}
			break;
		case WorldPointType.SPACE_MECHA_MAIN_VALUE:
		case WorldPointType.SPACE_MECHA_SLAVE_VALUE:
			guildId = worldPoint.getGuildId();
			break;
		}
		
		return guildId;
	}
	
	/**
	 * 获取总统府世界点
	 * @return
	 */
	public WorldPoint getPresidentPoint() {
		int presidentPointId = WorldMapConstProperty.getInstance().getCenterPointId();
		return getWorldPoint(presidentPointId);
	}
	
	/**
	 * 获取世界坐标索引
	 * @param x
	 * @param y
	 * @return
	 */
	public Integer getWorldIndex(int x, int y) {
		// 世界地图最大坐标
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		
		if (x < 0 || x > worldMaxX || y < 0 || y > worldMaxY) {
			return -1;
		}
		
		return worldIndex[x][y];
	}
	
	public void notifySaveEntity() {
		for (WorldPoint worldPoint : this.points.values()) {
			worldPoint.notifyUpdate(); 
		}
	}
	
	/**
	 * 获取玩家签名
	 * @param playerId
	 * @return
	 */
	public String getPlayerSignature(String playerId) {
		String signature = signatureMap.get(playerId);
		if (signature != null) {
			return signature;
		}
		
		signature = RedisProxy.getInstance().getSignature(playerId);
		if (signature == null) {
			signature = GsConst.NO_SIGNATURE_SIGN;
		}
		signatureMap.put(playerId, signature);
		return signature;
	}
	
	/**
	 * 更新玩家签名
	 */
	public void updatePlayerSignature(String playerId, String signature) {
		signatureMap.put(playerId, signature);
		RedisProxy.getInstance().updateSignature(playerId, signature);
	}
	
	/**
	 * 删除玩家签名
	 */
	public void removePlayerSignature(String playerId) {
		signatureMap.remove(playerId);
	}
	
	/**
	 * 获取玩家签名状态
	 */
	public KeyValuePair<SignatureState, String> getPlayerSignatureInfo(String playerId) {
		// 签名状态
		SignatureState state;
		// 签名
		String signature;
		
		int vipLevel = 0;
		
		AccountRoleInfo ari = GlobalData.getInstance().getAccountRoleInfo(playerId);
		
		Player activityPlayer = GlobalData.getInstance().getActivePlayer(playerId);
		if (activityPlayer != null) {
			vipLevel = activityPlayer.getVipLevel();
		} else {
			if (ari != null) {
				vipLevel = ari.getVipLevel();
			}
		}
		
		if (ari == null || vipLevel < ConstProperty.getInstance().getSignatureVipLimit()) {
			state = SignatureState.CAN_NOT_SIGNATURE_NOT;// vip未到，没签名
			signature = "";
		} else {
			signature = getPlayerSignature(playerId);
			if (signature.equals(GsConst.NO_SIGNATURE_SIGN)) {
				state = SignatureState.CAN_SIGNATURE_NOT;// vip到了，没签名
				signature = "";
			} else if (signature.equals("")) {
				state = SignatureState.CAN_SIGNATURE_EMPTY;// vip到了，签名为空
			} else {
				state = SignatureState.CAN_SIGNATURE;// vip到了，有签名
			}
		}
		return new KeyValuePair<SignatureState, String>(state, signature);
	}
	
	/**
	 * 移除当前装扮(目前只有跨服迁回用)
	 * @param playerId
	 */
	public void removeShowDress(String playerId) {
		showDressTable.remove(playerId);
	}
	
	/**
	 * 获取当前装扮
	 * @param dressType
	 * @return
	 */
	public Map<Integer, DressItem> getShowDress(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return new HashMap<>();
		}
		
		Map<Integer, DressItem> show = showDressTable.get(playerId);
		if (show != null) {
			return show;
		}
		
		show = new HashMap<>();
		
		String dressShow = RedisProxy.getInstance().getDressShow(playerId);
		if (dressShow != null) {
			Map<Integer, DressItem> dressItems = SerializeHelper.stringToMap(dressShow, Integer.class, DressItem.class);
			for (Entry<Integer, DressItem> dressItem : dressItems.entrySet()) {
				show.put(dressItem.getKey(), dressItem.getValue());
			}
		}
		Map<Integer, DressItem> curVal = showDressTable.putIfAbsent(playerId, show);
		if (curVal != null) {
			show = curVal;
		}
		return show;
	}
	
	/**
	 * 获取当前装扮
	 * @param dressType
	 * @return
	 */
	public DressItem getShowDress(String playerId, Integer dressType) {
		return getShowDress(playerId).get(dressType);
	}
	
	/**
	 * 如果存在，移除当前装扮
	 * @param dressType
	 */
	public boolean rmShowDressIfExist(String playerId, Integer rmDressType, Integer rmModelType) {
		Map<Integer, DressItem> showDress = getShowDress(playerId);
		if (showDress.containsKey(rmDressType) && showDress.get(rmDressType).getModelType() == rmModelType) {
			showDress.remove(rmDressType);
			RedisProxy.getInstance().updateDressShow(playerId, showDress);
			return true;
		}
		return false;
	}
	
	/**
	 * 更新装扮
	 * @param dressType
	 * @param modelType
	 */
	public void updateShowDress(String playerId, Integer dressType, DressItem dress) {
		Map<Integer, DressItem> showDress = getShowDress(playerId);
		showDress.put(dressType, dress);
		RedisProxy.getInstance().updateDressShow(playerId, showDress);
	}
	
	/**
	 * 获取装扮点
	 */
	public int getDressPoint(PlayerData playerData) {
		DressEntity dressEntity = playerData.getDressEntity();
		if (dressEntity == null) {
			return 0;
		}
		
		int point = 0;
		
		for (DressItem dress : dressEntity.getDressInfo()) {
			DressCfg cfg = AssembleDataManager.getInstance().getDressCfg(dress.getDressType(), dress.getModelType());
			if (cfg == null) {
				continue;
			}
			point += cfg.getSkinPoint();
		}
		
		return point;
	}
	
	/**
	 * 更新世界点作用号显示
	 * @param playerId
	 * @param baseShows
	 */
	public void updateBaseShow(String playerId, Map<Integer, BaseShowItem> baseShows) {
	}
	
	/**
	 * 获取世界点作用号显示
	 * @param playerId
	 * @return
	 */
	public Map<Integer, BaseShowItem> getBaseShow(String playerId) {
		return new HashMap<>();
	}
	
	/**
	 * 获取基地作用号显示
	 * @param playerId
	 * @return
	 */
	public int getCityBaseShow(String playerId) {
		int retBuffId = 0;
		long retBuffStartTime = 0;
		
		Map<Integer, BaseShowItem> baseShowMap = getBaseShow(playerId);
		
		List<Integer> baseShowCfgs = WorldMapConstProperty.getInstance().getBaseShow();
		for (int baseShowCfg : baseShowCfgs) {
			BaseShowItem item = baseShowMap.get(baseShowCfg);
			if (item == null) {
				continue;
			}
			if (item.getEndTime() < HawkTime.getMillisecond()) {
				continue;
			}
			if (retBuffStartTime != 0 && item.getStartTime() > retBuffStartTime) {
				continue;
			}
			retBuffId = item.getBuffId();
			retBuffStartTime = item.getStartTime();
		}
		
		return retBuffId;
	}
	
	/**
	 * 创建玩家的新手世界资源
	 * 
	 * @param playerId
	 */
	public WorldPoint createYuriStrikePoint(String playerId) {
		int playerPointId = WorldPlayerService.getInstance().getPlayerPos(playerId);
		if (playerPointId <= 0) {
			return null;
		}
		
		// 如果已经有点了，就直接用这个点
		int maxViewRadius = Math.max(GameConstCfg.getInstance().getViewXRadius(), GameConstCfg.getInstance().getViewYRadius());
		List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(playerPointId, maxViewRadius);
		for (Point point : aroundPoints) {
			WorldPoint wp = WorldPointService.getInstance().getWorldPoint(point.getId());
			if (wp == null) {
				continue;
			}
			String ownerId = wp.getOwnerId();
			if (HawkOSOperator.isEmptyString(ownerId) || !ownerId.equals(playerId)) {
				continue;
			}
			if (wp.getPointType() != WorldPointType.YURI_STRIKE_VALUE) {
				continue;
			}
			return wp;
		}

		Point point = WorldPlayerService.getInstance().randomYuriStrikePoint(playerId, WorldPointType.YURI_STRIKE, 5);
		if (point == null) {
			return null;
		}

		WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.YURI_STRIKE_VALUE);
		worldPoint.setOwnerId(playerId);
		worldPoint.setLifeStartTime(HawkTime.getMillisecond());

		if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
			return null;
		}
		
		return worldPoint;
	}
	
	/**
	 * 获取幽灵来袭行军起始点
	 * 
	 * @param playerId
	 */
	public Integer createGhostStrikePoint(String playerId, int enemyDistance) {
		int playerPointId = WorldPlayerService.getInstance().getPlayerPos(playerId);
		if (playerPointId <= 0) {
			return null;
		}
		Point point = WorldPlayerService.getInstance().randomYuriStrikePoint(playerId, WorldPointType.YURI_STRIKE, enemyDistance);
		if (point == null) {
			return null;
		}
		return point.getId();
	}
	
	/**
	 * 获取玩家占领的世界点(驻扎点，采集点，资源点)
	 * @param playerId
	 * @param type
	 * @return
	 */
	public List<WorldPoint>  getOccupyPointsByType(String playerId, WorldPointType type) {
		List<WorldPoint> points = new ArrayList<>();
		
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(playerId);
		for (IWorldMarch march : marchs) {
			if (!WorldUtil.isQuarterStatus(march)) {
				continue;
			}
			WorldPoint point = getWorldPoint(march.getMarchEntity().getTerminalId());
			if (point == null || point.getPointType() != type.getNumber()) {
				continue;
			}
			points.add(point);
		}
		
		return points;
	}
	
	/**
	 * 获取玩家占领的世界点(驻扎点，采集点，资源点)
	 * @param playerId
	 * @return
	 */
	public List<WorldPoint>  getOccupyPoints(String playerId) {
		List<WorldPoint> points = new ArrayList<>();
		
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(playerId);
		for (IWorldMarch march : marchs) {
			if (!WorldUtil.isQuarterStatus(march)) {
				continue;
			}
			WorldPoint point = getWorldPoint(march.getMarchEntity().getTerminalId());
			if (point == null) {
				continue;
			}
			points.add(point);
		}
		
		return points;
	}

	public CapitalAreaObject getCaptialArea() {
		return captialArea;
	}
	
	/**
	 * 搜索空闲点
	 * @param pos
	 * @param minDis
	 * @param maxDis
	 * @author Codej
	 */
	public WorldPoint searchFreePoint(int[] pos, int minDis, int maxDis) {		
		WorldPointService worldPointService = WorldPointService.getInstance();
		List<Point> pointList = worldPointService.getRhoAroundPointsFree(pos[0], pos[1], minDis);
		
		//默认是降序
		boolean up = false;
		
		//如果最小的距离找不到
		if (pointList.isEmpty()) {
			pointList = worldPointService.getRhoAroundPointsFree(pos[0], pos[1], maxDis);
			up = true;
		}
		
		if (pointList.isEmpty()) {
			return null;
		}
		
		List<DistancePoint> distancePointList = new ArrayList<>(pointList.size());
		pointList.stream().forEach(point->{
			distancePointList.add(new DistancePoint(point, pos));
		});
		
		//默认降序
		Collections.shuffle(distancePointList);
		DistancePoint distancePoint = null;
		boolean found = false;
		if (up) {
			for (int i = distancePointList.size() - 1; i >= 0; i--) {
				distancePoint = distancePointList.get(i);
				if ((distancePoint.getPoint().getX() + distancePoint.getPoint().getY()) % 2 == 0) {
					found = true;
					break;
				} 
			}
			
		}  else {
			for (int i = 0; i < distancePointList.size(); i++) {
				distancePoint = distancePointList.get(i);
				if ((distancePoint.getPoint().getX() + distancePoint.getPoint().getY()) % 2 == 0) {
					found = true;
					break;
				} 
			} 
		}
		
		if (found) {
			WorldPoint wp = new WorldPoint();
			wp.setX(distancePoint.getPoint().getX());
			wp.setY(distancePoint.getPoint().getY());			
			return wp;
		} else {
			return null;
		}			
	}
	
	/**
	 * 是否超出地图范围
	 * @param posX
	 * @param posY
	 * @return
	 */
	public boolean isOutOfMapRange(int posX, int posY) {
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		if (posX >= worldMaxX || posX <= 0 || posY >= worldMaxY || posY <= 0) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param pointId
	 * @param cfgId
	 * @param isKill
	 * @param ownerId 工会ID。
	 */
	public void genChristmasBox(int pointId, int cfgId, boolean isKill, String ownerId) {
		logger.info("gen christmas box pointId:{}, cfgId:{}, isKill:{}, ownerId:{}", pointId, cfgId, isKill, ownerId);
		WorldChristmasWarBossCfg bossCfg = HawkConfigManager.getInstance().getConfigByKey(WorldChristmasWarBossCfg.class, cfgId);
		if (bossCfg == null) {
			return;
		}
		// 年兽刷新区域半径(以坐标为中心，半径内区域随机生成点)
		int areaRadius = WorldMapConstProperty.getInstance().getChristmasRefreshAreaRadius();
		
		List<Point> areaPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, areaRadius);
		
		Collections.shuffle(areaPoints);
		
		Map<Integer, Integer> dropMap = bossCfg.getOnceKillDropMap();
		if (isKill) {
			dropMap = bossCfg.getKillDropMap();
		}

		// 生成数量
		int genCount = 0;
		for (Entry<Integer, Integer> drop : dropMap.entrySet()) {
			genCount += drop.getValue();
		}
		
		// 找到可以生成的点
		List<Point> genPoint = new ArrayList<>();
		
		for (Point point : areaPoints) {
			
			if (genPoint.size() >= genCount) {
				break;
			}
			
			if (!point.canRMSeat()) {
				continue;
			}
			
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}
			
			genPoint.add(point);
		}
		
		genCount = genPoint.size();
		
		// 生成点
		for (Entry<Integer, Integer> drop : dropMap.entrySet()) {			
			for (int i = 0; i < drop.getValue(); i++) {				
				try {
					Point point = genPoint.get(genCount - 1);					
					genCount--;
					
					WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.CHRISTMAS_BOX_VALUE);
					worldPoint.setMonsterId(drop.getKey());
					worldPoint.setResourceId(drop.getKey());
					worldPoint.setOwnerId(ownerId);
					WorldPointService.getInstance().createWorldPoint(worldPoint);
					
					logger.info("create christmas box , x:{}, y:{}, boxId:{}", worldPoint.getX(), worldPoint.getY(), worldPoint.getMonsterId());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	/**
	 * 生成机甲宝箱
	 */
	public void genGundamBox(int pointId, int cfgId, boolean isKill) {
		
		WorldNianCfg nianCfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianCfg.class, cfgId);
		if (nianCfg == null) {
			return;
		}
		// 年兽刷新区域半径(以坐标为中心，半径内区域随机生成点)
		int areaRadius = WorldMapConstProperty.getInstance().getNianBoxRefRadius();
		
		List<Point> areaPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, areaRadius);
		
		Collections.shuffle(areaPoints);
		
		Map<Integer, Integer> dropMap = nianCfg.getOnceKillDropMap();
		if (isKill) {
			dropMap = nianCfg.getKillDropMap();
		}

		// 生成数量
		int genCount = 0;
		for (Entry<Integer, Integer> drop : dropMap.entrySet()) {
			genCount += drop.getValue();
		}
		
		// 找到可以生成的点
		List<Point> genPoint = new ArrayList<>();
		
		for (Point point : areaPoints) {
			
			if (genPoint.size() >= genCount) {
				break;
			}
			
			if (!point.canRMSeat()) {
				continue;
			}
			
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}
			
			genPoint.add(point);
		}
		
		genCount = genPoint.size();
		
		// 生成点
		for (Entry<Integer, Integer> drop : dropMap.entrySet()) {
			
			for (int i = 0; i < drop.getValue(); i++) {
				
				try {
					Point point = genPoint.get(genCount - 1);
					genCount--;
					
					WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.NIAN_BOX_VALUE);
					worldPoint.setMonsterId(drop.getKey());
					WorldPointService.getInstance().createWorldPoint(worldPoint);
					
					logger.info("genGundamBox, x:{}, y:{}, boxId:{}", worldPoint.getX(), worldPoint.getY(), worldPoint.getMonsterId());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	public List<WorldNianBoxCfg> getNianBoxCfgs() {
		List<WorldNianBoxCfg> cfgs = new ArrayList<>();
		
		ConfigIterator<WorldNianBoxCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(WorldNianBoxCfg.class);
		while(iterator.hasNext()) {
			WorldNianBoxCfg cfg = iterator.next();
			cfgs.add(cfg);
		}
		return cfgs;
	}
	
	/**
	 * 更新装备科技外显
	 */
	public void updateShowEquipTech(String playerId, int show) {
		showEquipTech.put(playerId, show);
		RedisProxy.getInstance().updateShowEquipTech(playerId, show);
		GameUtil.notifyDressShow(playerId);
	}
	
	/**
	 * 获取装备科技外显
	 */
	public int getShowEquipTech(String playerId) {

		if (showEquipTech.containsKey(playerId)) {
			return showEquipTech.get(playerId);
		}
		
		int show = RedisProxy.getInstance().getShowEquipTech(playerId);
		showEquipTech.put(playerId, show);
		return show;
	}
	
	public void removeShowEquipTech(String playerId) {
		showEquipTech.remove(playerId);
	}
	
	
	/**更新烟花效果
	 * @param playerId
	 * @param duration  持续时间
	 */
	public void updateFireWorks(String playerId, int type, long duration) {
		long nowTime = HawkTime.getMillisecond();
		long endTime = nowTime + duration;
		String fireInfo = type + "_" + endTime;
		fireWorksMap.put(playerId, fireInfo);
		GameUtil.notifyFireWorksShow(playerId);
	}
	
	/**获取烟花效果
	 * @param playerId
	 * @return
	 */
	public int getFireWorkType(String playerId) {
		if (fireWorksMap.containsKey(playerId)) {
			long nowTime = HawkTime.getMillisecond();
			String fireInfo = fireWorksMap.get(playerId);
			String[] fireStrArr = fireInfo.split("_");
			int type = Integer.valueOf(fireStrArr[0]);
			long endTime = Long.valueOf(fireStrArr[1]);
			if (nowTime > endTime) {
				fireWorksMap.remove(playerId);
				return 0;
			}else{
				return type;
			}
		}
		return 0;
	}
	
	/**
	 * 获取装扮称号显示类型
	 * @param playerId
	 * @return
	 */
	public int getDressTitleType(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		if (dressTitleTypeMap.containsKey(playerId)) {
			return dressTitleTypeMap.get(playerId);
		}
		int type = RedisProxy.getInstance().getDressTitleType(playerId);
		dressTitleTypeMap.put(playerId, type);
		return type;
	}
	
	/**
	 * 更新装扮称号显示类型
	 * @param playerId
	 * @param type
	 */
	public void updateDressTitleType(String playerId, int type) {
		dressTitleTypeMap.put(playerId, type);
		RedisProxy.getInstance().updateDressTitleType(playerId, type);
	}
	
	/**
	 * 更新泰能装备外显(主动开启/关闭)
	 * @param player
	 */
	public void updateEquipStarShow(Player player, boolean change) {
		int[] equipStarShowInfo = getEquipStarShowInfo(player.getId());
		equipStarShowInfo[0] = GameUtil.checkEquipStarShow(player);
		equipStarShowInfo[1] = (equipStarShowInfo[1] == 0 ? 1 : 0);
				
		int showInfoInt = GameUtil.combineXAndY(equipStarShowInfo[0], equipStarShowInfo[1]);
		showEquipStar.put(player.getId(), showInfoInt);
		RedisProxy.getInstance().updateEquipStarShow(player.getId(), showInfoInt);
		
		// 更新世界显示
		GameUtil.notifyDressShow(player.getId());
		
		player.getPush().syncEquipStarShow();
	}
	
	/**
	 * 更新泰能装备外显
	 * @param player
	 */
	public void updateEquipStarShow(Player player) {
		int[] equipStarShowInfo = getEquipStarShowInfo(player.getId());
		equipStarShowInfo[0] = GameUtil.checkEquipStarShow(player);
		
		int showInfoInt = GameUtil.combineXAndY(equipStarShowInfo[0], equipStarShowInfo[1]);
		showEquipStar.put(player.getId(), showInfoInt);
		RedisProxy.getInstance().updateEquipStarShow(player.getId(), showInfoInt);
		
		// 更新世界显示
		GameUtil.notifyDressShow(player.getId());
		
		player.getPush().syncEquipStarShow();
	}
	
	/**
	 * 获取泰能装备外显信息 (y << 16) | x   y:开关  x:套装
	 * @param playerId
	 * @return
	 */
	public int[] getEquipStarShowInfo(String playerId) {
		if (showEquipStar.containsKey(playerId)) {
			return GameUtil.splitXAndY(showEquipStar.get(playerId));
		}
		int equipStarShow = RedisProxy.getInstance().getEquipStarShow(playerId);
		showEquipStar.put(playerId, equipStarShow);
		return GameUtil.splitXAndY(equipStarShow);
	}
	
	/**
	 * 获取泰能装备外显(推给客户端的)
	 * @param playerId
	 * @return
	 */
	public int getEquipStarShow(String playerId) {
		int[] equipStarShowInfo = getEquipStarShowInfo(playerId);
		// 关闭外显,就推0
		if (equipStarShowInfo[1] == 1) {
			return 0;
		}
		return equipStarShowInfo[0];
	}

	/**
	 * 获取星能探索外显(推给客户端的)
	 * @param playerId
	 * @return
	 */
	public int getStarExploreShow(String playerId) {
		if(showStarExplore.containsKey(playerId)){
			return showStarExplore.get(playerId);
		}
		int starExploreShow = RedisProxy.getInstance().getStarExploreShow(playerId);
		showStarExplore.put(playerId, starExploreShow);
		return starExploreShow;
	}

	public void updateStarExploreShow(String playerId, int show) {
		showStarExplore.put(playerId, show);
		RedisProxy.getInstance().updateStarExploreShow(playerId, show);
		GameUtil.notifyDressShow(playerId);
	}

	public void refreshStarExploreShow(String playerId){
		int starExploreShow = RedisProxy.getInstance().getStarExploreShow(playerId);
		showStarExplore.put(playerId, starExploreShow);
	}
	
	/**
	 * 生成资源锅点
	 * @param posX
	 * @param posY
	 * @param resTreCfg
	 * @return
	 */
	public boolean genResourceTreasuWorldPoint(int posX, int posY, ResTreasureCfg resTreCfg, Player player, ConsumeItems consumeItems) {
		String playerId = player != null ? player.getId() : "null";
		// 目标点
		int pointId = GameUtil.combineXAndY(posX, posY);
		int restreId = resTreCfg.getId();
		// 超出地图范围
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		if (posX >= worldMaxX || posX <= 0 || posY >= worldMaxY || posY <= 0) {
			logger.error("genResourceTreasuWorldPoint failed, out or range, x:{}, y:{}, resTreasureId: {}, playerId: {}", posX, posY, restreId, playerId);
			return false;
		}

		// 请求点为阻挡点
		if (MapBlock.getInstance().isStopPoint(pointId)) {
			logger.error("genResourceTreasuWorldPoint failed, is stop point, x:{}, y:{}, resTreasureId: {}, playerId: {}", posX, posY, restreId, playerId);
			return false;
		}

		// 请求点再国王领地内
		if (WorldPointService.getInstance().isInCapitalArea(pointId)) {
			logger.error("genResourceTreasuWorldPoint failed, is int capital area, x:{}, y:{}, resTreasureId: {}, playerId: {}", posX, posY, restreId, playerId);
			return false;
		}

		// 投递世界线程执行
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.WORLD_MONSTER_POINT_GENERATE) {
			@Override
			public boolean onInvoke() {

				// 中心点不是空闲点
				Point centerPoint = WorldPointService.getInstance().getAreaPoint(posX, posY, true);
				if (centerPoint == null) {
					logger.error("genResourceTreasuWorldPoint failed, centerPoint point not free, posX:{}, posY:{}, resTreasureId: {}, playerId: {}", posX, posY, restreId, playerId);
					return false;
				}

				// 资源宝库占用半径
				int resRadius = resTreCfg.getRadius();

				// 获取周围点
				List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, resRadius);
				if (aroundPoints.size() != 2 * resRadius * (resRadius - 1)) {
					logger.error("genResourceTreasuWorldPoint failed, arround points has been occupy, posX:{}, posY:{}, resTreasureId: {}, playerId: {}", posX, posY, restreId, playerId);
					return false;
				}

				if (consumeItems != null) {
					// 投递回玩家线程：消耗道具
					player.dealMsg(MsgId.GEN_RES_TREASURE, new GenerateResTreasureMsgInvoker(player, consumeItems));
				}

				// 中心点所在区域
				AreaObject areaObj = WorldPointService.getInstance().getArea(posX, posY);
				// 中心点所在资源带
				int zoneId = WorldUtil.getPointResourceZone(posX, posY);

				// 生成资源宝库点
				WorldPoint worldPoint = new WorldPoint(posX, posY, areaObj.getId(), zoneId, WorldPointType.RESOURC_TRESURE_VALUE);
				worldPoint.setResourceId(restreId);
				worldPoint.setLifeStartTime(HawkTime.getMillisecond());
				worldPoint.setProtectedEndTime(HawkTime.getMillisecond() + resTreCfg.getLifeTime() * 1000);
				if (player != null) {
					worldPoint.setGuildId(player.getGuildId());
					worldPoint.setPlayerName(player.getName());
				}
				
				// 创建玩家使用的世界点信息
				if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
					logger.error("genResourceTreasuWorldPoint failed, createWorldPoint failed, posX:{}, posY:{}, resTreasureId: {}, playerId: {}", posX, posY, restreId, playerId);
					return false;
				}
				
				if (player != null) {
					player.sendProtocol(HawkProtocol.valueOf(HP.code.GEN_RES_TRESSRUE_SUCCES_VALUE, PBGenResTreasureSuccess.newBuilder().setX(posX).setY(posY).setCfgId(restreId)));
					ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_ALLIANCE).setGuildId(player.getGuildId())
							.setKey(Const.NoticeCfgId.RES_TREASURE_PUT).addParms(player.getName()).addParms(posX).addParms(posY).build();
					ChatService.getInstance().addWorldBroadcastMsg(parames);
				}

				// 行为日志
				logger.info("genResourceTreasuWorldPoint success, posX:{}, posY:{}, resTreasureId: {}, playerId: {}", posX, posY, restreId, playerId);
				return true;
			}
		});
		return true;
	}
	
	/**
	 * 星能探索活动刷点
	 * @param refreshCount
	 */
	public List<Integer> planetExploreRefreshPoint(int refreshCount) {
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		ResTreasureCfg resTreCfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class, cfg.getRefreshTargetId());
		if (resTreCfg == null) {
			logger.error("planetExploreRefreshPoint failed, ResTreasureCfg not exist, treasureId: {}", cfg.getRefreshTargetId());
			return new ArrayList<>();
		}
		
		List<Integer> pointList = new ArrayList<>();
		List<AreaObject> areaList = new LinkedList<AreaObject>();
		areaList.addAll(WorldPointService.getInstance().getAreaVales());
		Collections.shuffle(areaList);
		Set<Integer> currPointIds = new HashSet<Integer>();
		for (AreaObject areaObj : areaList) {
			// 区域已满
			if (areaObj.getFreePointCount() <= 0) {
				logger.error("planetExploreRefreshPoint failed, getFreePoint empty, areaId: {}", areaObj.getId());
				continue;
			}
			// 随机一个点
			Point pointInfo = WorldPlayerService.getInstance().randomFreePoint(areaObj, WorldPointType.RESOURC_TRESURE, 
					currPointIds, GameConstCfg.getInstance().getRandMinPt(), GameConstCfg.getInstance().getRandMaxPt());
			if (pointInfo == null) {
				logger.error("planetExploreRefreshPoint failed, randomFreePoint null");
				continue;
			}
			
			//坐标x+y的和要是奇数才行，偶数不行
			if ((pointInfo.getX() + pointInfo.getY()) % 2 == 0) {
				continue;
			}
			
			int pointId = pointInfo.getId();
			// 请求点为阻挡点
			if (MapBlock.getInstance().isStopPoint(pointId)) {
				logger.error("planetExploreRefreshPoint failed, is stop point, x:{}, y:{}", pointInfo.getX(), pointInfo.getY());
				continue;
			}

			// 请求点再国王领地内
			if (WorldPointService.getInstance().isInCapitalArea(pointId)) {
				logger.error("planetExploreRefreshPoint failed, is int capital area, x:{}, y:{}", pointInfo.getX(), pointInfo.getY());
				continue;
			}
			
			// 中心点不是空闲点
			Point centerPoint = WorldPointService.getInstance().getAreaPoint(pointInfo.getX(), pointInfo.getY(), true);
			if (centerPoint == null) {
				logger.error("planetExploreRefreshPoint failed, centerPoint point not free, posX:{}, posY:{}", pointInfo.getX(), pointInfo.getY());
				continue;
			}

			// 资源宝库占用半径
			int resRadius = resTreCfg.getRadius();
			// 获取周围点
			List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, resRadius);
			if (aroundPoints.size() != 2 * resRadius * (resRadius - 1)) {
				logger.error("planetExploreRefreshPoint failed, arround points has been occupy, posX:{}, posY:{}", pointInfo.getX(), pointInfo.getY());
				continue;
			}
			
			int posX = pointInfo.getX();
			int posY = pointInfo.getY();
			boolean success = genResourceTreasuWorldPoint(posX, posY, resTreCfg, null, null);
			if (!success) {
				logger.error("planetExploreRefreshPoint genResourceTreasuWorldPoint failed, posX:{}, posY:{}", posX, posY);
				continue;
			}
			
			logger.info("planetExploreRefreshPoint success, posX:{}, posY:{}", posX, posY);
			int point = posX * 10000 + posY;
			pointList.add(point);
			refreshCount--;
			if (refreshCount <= 0) {
				break;
			}
		}
		
		if (pointList.size() > 0) {
			ChatParames.Builder builder = ChatParames.newBuilder()
					.setChatType(Const.ChatType.CHAT_WORLD)
					.setKey(Const.NoticeCfgId.PLANET_TREASURE_PUT)
					.addParms(pointList.size());
			ChatParames parames = builder.build();
			ChatService.getInstance().addWorldBroadcastMsg(parames);
		}
		
		return pointList;
	}
	
	
	
	/**
	 * 获取装扮称号显示类型
	 * @param playerId
	 * @return
	 */
	public String getCollegeNameShow(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return "";
		}
		if (this.collegeNameMap.containsKey(playerId)) {
			return collegeNameMap.get(playerId);
		}
	
		String name = RedisProxy.getInstance().getCollegeNameShow(playerId);
		if (HawkOSOperator.isEmptyString(name)) {
			name = "";
		}
		this.collegeNameMap.put(playerId, name);
		return name;
	}
	
	/**
	 * 更新装扮称号显示类型
	 * @param playerId
	 * @param type
	 */
	public void updateCollegeNameShow(String playerId, String collegeName) {
		if(Objects.isNull(collegeName)){
			return;
		}
		collegeNameMap.put(playerId, collegeName);
		RedisProxy.getInstance().updateCollegeNameShow(playerId, collegeName);
		// 更新世界显示
		GameUtil.notifyDressShow(playerId);
	}
	
	
	public void removeCollegeNameShow(String playerId) {
		collegeNameMap.remove(playerId);
	}
	
	
	
	
}
