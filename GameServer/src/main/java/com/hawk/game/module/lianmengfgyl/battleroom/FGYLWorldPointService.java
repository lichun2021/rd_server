package com.hawk.game.module.lianmengfgyl.battleroom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLBattleCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLEnemyCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLBuildType;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLHeadQuarter;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLTower;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.state.FGYLBuildingStateYuri;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.FoggyInfo;
import com.hawk.game.world.object.Point;

public class FGYLWorldPointService {
	private final FGYLBattleRoom parent;
	private boolean inited;

	private FGYLWorldScene worldScene;
	/**
	 * 资源区域块 <块id,块数据>
	 */
	private Map<Integer, FGYLAreaObject> areas = new ConcurrentHashMap<>();
	public int worldMaxX;
	public int worldMaxY;
	public final int WorldResRefreshWidth = 40;
	public final int WorldResRefreshHeight = 40;
	private Multimap<FGYLBuildType, ? super IFGYLBuilding> builTypeMap = HashMultimap.create();
	private List<IFGYLBuilding> buildingList = new CopyOnWriteArrayList<>();
	private Map<Integer, IFGYLBuilding> buildIdBuildMap = new ConcurrentHashMap<>();
	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	private Map<Integer, IFGYLWorldPoint> viewPoints = new ConcurrentHashMap<>();

	public FGYLWorldPointService(FGYLBattleRoom parent) {
		this.parent = parent;
	}

	public FGYLBattleRoom getParent() {
		return parent;
	}

	public boolean onTick() {
		return true;
	}

	public void init() {
		if (inited) {
			return;
		}
		inited = true;

		FGYLBattleCfg bcfg = parent.getCfg();
		worldMaxX = bcfg.getMapX() + 1;
		worldMaxY = bcfg.getMapY() + 1;
		initWorldScene();
		loadWorldAreas();

		ConfigIterator<FGYLEnemyCfg> buildRefreshIt = HawkConfigManager.getInstance().getConfigIterator(FGYLEnemyCfg.class);
		for (FGYLEnemyCfg rfcfg : buildRefreshIt) {
			if(rfcfg.getLevel() == getParent().getDifficult()){
				refreshBuild(rfcfg);
			}
		}
		Collections.shuffle(buildingList);

		// 通知场景初始化完毕
		getWorldScene().notifyInitOK();
	}

	private void refreshBuild(FGYLEnemyCfg bcfg) {
		try {
			int index = 0;
			IFGYLBuilding icd = null;
			switch (bcfg.getType()) {
			case 1:
				icd = new FGYLHeadQuarter(parent);
				break;
			case 2:
				icd = new FGYLTower(parent);
				break;
			default:
				break;
			}
			icd.setCfgId(bcfg.getEnemyId());
			icd.setIndex(index);
			icd.setX(bcfg.getCoordinateX());
			icd.setY(bcfg.getCoordinateY());
			icd.setBuildTypeId(bcfg.getType());
			icd.setProtectedEndTime(bcfg.getPeaceTime()*1000 + getParent().getCreateTime());

			FoggyFortressCfg foggyFortressCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, bcfg.getFoggyFortress());
			FoggyInfo foggyInfo = new FoggyInfo();
			foggyInfo.setTrapInfo(foggyFortressCfg.getRandTrapInfo());
			foggyInfo.setSoliderInfo(foggyFortressCfg.getRandSoldierInfo());
			foggyInfo.setHeroIds(foggyFortressCfg.getRandHeroId(2));
			icd.setFoggyFortressId(bcfg.getFoggyFortress());
			icd.setFoggyInfoObj(foggyInfo);
			icd.setStateObj(new FGYLBuildingStateYuri(icd));
			viewPoints.put(icd.getPointId(), icd);
			buildingList.add(icd);
			buildIdBuildMap.put(icd.getCfgId(), icd);
			builTypeMap.put(icd.getBuildType(), icd);
			index++;
			addViewPoint(icd);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 添加世界点 */
	public void addViewPoint(IFGYLWorldPoint... vp) {
		for (IFGYLWorldPoint point : vp) {
			if (point == null) {
				continue;
			}
			viewPoints.put(point.getPointId(), point);
			int type = point instanceof IFGYLPlayer ? GsConst.WorldObjType.CITY : point.getPointType().getNumber();
			int aoiObjId = getWorldScene().add(type, 0, point.getX(), point.getY(), point);
			point.setAoiObjId(aoiObjId);
			rmFromAreaFreePoint(point);

		}

	}

	public boolean removeViewPoint(IFGYLWorldPoint vp) {
		boolean result = Objects.nonNull(viewPoints.remove(vp.getPointId()));
		// resetOccupationPoint();
		addToAreaFreePoint(vp);
		// 移除世界场景
		getWorldScene().leave(vp.getAoiObjId());

		return result;
	}

	/**
	 * 初始化世界场景
	 */
	private void initWorldScene() {

		// 玩家视野半径
		int viewXRadius = GameConstCfg.getInstance().getViewXRadius();
		int viewYRadius = GameConstCfg.getInstance().getViewYRadius();
		int searchRadius = GameConstCfg.getInstance().getSyncSearchRadius();
		searchRadius = Math.max(searchRadius, Math.max(viewXRadius, viewYRadius));

		// 初始化世界场景
		worldScene = new FGYLWorldScene(worldMaxX, worldMaxY, searchRadius);
	}

	private void loadWorldAreas() {
		// 创建区域对象列表
		List<FGYLAreaObject> areaList = createWorldAreas();
		for (FGYLAreaObject areaObj : areaList) {
			// 创建区域内所有点对象
			if (areaObj.initAreaPoints(parent)) {
				areas.put(areaObj.getId(), areaObj);
			}
		}

	}

	private List<FGYLAreaObject> createWorldAreas() {
		List<FGYLAreaObject> areaList = new LinkedList<>();

		int areaCols = worldMaxX / WorldResRefreshWidth;
		areaCols = worldMaxX % WorldResRefreshWidth == 0 ? areaCols : areaCols + 1;

		int areaRows = worldMaxY / WorldResRefreshHeight;
		areaRows = worldMaxY % WorldResRefreshHeight == 0 ? areaRows : areaRows + 1;

		// 根据资源分块信息进行分块对象创建
		for (int row = 0; row < areaRows; row++) {
			for (int col = 0; col < areaCols; col++) {
				int beginX = col * WorldResRefreshWidth;
				int beginY = row * WorldResRefreshHeight;
				int endX = (col + 1) * WorldResRefreshWidth - 1;
				int endY = (row + 1) * WorldResRefreshHeight - 1;

				// 最大区域限制
				if (endX > worldMaxX) {
					endX = worldMaxX;
				}
				if (endY > worldMaxY) {
					endY = worldMaxY;
				}

				// 构建地图区块
				int areaId = row * areaCols + col + 1;
				FGYLAreaObject areaObj = new FGYLAreaObject(areaId, beginX, beginY, endX, endY);
				areaList.add(areaObj);
			}
		}

		return areaList;
	}

	public int getAreaId(int x, int y) {

		int areaCols = worldMaxX / WorldResRefreshWidth;
		areaCols = worldMaxX % WorldResRefreshWidth == 0 ? areaCols : areaCols + 1;

		int areaRows = worldMaxY / WorldResRefreshHeight;
		areaRows = worldMaxY % WorldResRefreshHeight == 0 ? areaRows : areaRows + 1;

		// 计算行列
		int col = x / WorldResRefreshWidth;
		int row = y / WorldResRefreshHeight;

		// 构建地图区块
		int areaId = row * areaCols + col + 1;
		return areaId;
	}

	public FGYLWorldScene getWorldScene() {
		return worldScene;
	}

	public Map<Integer, FGYLAreaObject> getAreas() {
		return areas;
	}

	/**
	 * 检查点是否可用
	 * @param areaObj
	 * @param centerPoint
	 * @param distance
	 * @return
	 */
	public boolean tryOccupied(int x, int y, int distance) {
		FGYLAreaObject areaObj = getArea(x, y);
		Point centerPoint = areaObj.getFreePoint(x, y);
		if (Objects.isNull(centerPoint)) {
			return false;
		}
		// 距离边界的长度
		int boundary = GsConst.WORLD_BOUNDARY_SIZE;
		// 边界坐标点不可用的检测
		if (centerPoint.getX() <= boundary || centerPoint.getY() < boundary || centerPoint.getX() >= (worldMaxX - boundary)
				|| centerPoint.getY() >= worldMaxY - boundary) {
			return false;
		}
		// 中心点是否被占用
		Optional<IFGYLWorldPoint> currPoint = parent.getWorldPoint(centerPoint.getX(), centerPoint.getY());
		if (currPoint.isPresent()) {
			return false;
		}
		// 城点距离限制范围内的所有点(1距离为4个点, 2距离为12个点)
		List<Point> freeAroundPoints = getRhoAroundPointsDetail(centerPoint.getX(), centerPoint.getY(), distance, distance, true);
		// 必须为4个, 否则就是有阻挡点存在
		if (freeAroundPoints.size() != 2 * distance * (distance - 1)) {
			return false;
		}
		return true;
	}

	/**
	 * 计算特定中心点周围的有效点, 不包含中心点本身(菱形区域)
	 * @param centerX
	 * @param centerY
	 * @param radiusX
	 * @param radiusY
	 * @param recNum 所属第几圈
	 * @return
	 */
	public List<Point> getRhoAroundPointsDetail(int centerX, int centerY, int radiusX, int radiusY, boolean needFree) {
		List<Point> aroundPoints = new LinkedList<>();
		if (radiusX <= 0 || radiusY <= 0) {
			return aroundPoints;
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
	 * 获得周围指定type的点
	 * 
	 * @param centerX
	 * @param centerY
	 * @param radiusX
	 * @param radiusY
	 * @return
	 */
	public List<IFGYLWorldPoint> getAroundWorldPointsWithType(int centerX, int centerY, int radiusX, int radiusY, int pointType) {
		List<Point> points = getRhoAroundPointsAll(centerX, centerY, radiusX, radiusY);
		List<IFGYLWorldPoint> aroundWorldPoints = new ArrayList<>(points.size());
		for (Point point : points) {
			IFGYLWorldPoint worldPoint = getWorldPoint(point.getX(), point.getY());
			if (worldPoint != null && worldPoint.getPointType().getNumber() == pointType) {
				aroundWorldPoints.add(worldPoint);
			}
		}
		return aroundWorldPoints;
	}

	/**
	 * 获取中心点以及占用点，不分是否空闲
	 */
	public List<Point> getRhoAroundPointsAll(int centerX, int centerY, int radiusX, int radiusY) {
		return getRhoAroundPointsDetail(centerX, centerY, radiusX, radiusY, false);
	}

	/**
	 * 根据坐标获得世界点信息，若是未被占用的点就返回空
	 * @param x
	 * @param y
	 * @return
	 */
	public IFGYLWorldPoint getWorldPoint(int x, int y) {
		return this.getWorldPoint(GameUtil.combineXAndY(x, y));
	}

	/**
	 * 获取实体点ID
	 * @param pointId
	 * @return
	 */
	public IFGYLWorldPoint getWorldPoint(int pointId) {
		return viewPoints.get(pointId);
	}

	/**
	 * 获取地图上的点
	 * @param x
	 * @param y
	 * @return
	 */
	public Point getAreaPoint(int x, int y, boolean needFree) {
		FGYLAreaObject areaObj = getArea(x, y);
		if (areaObj == null) {
			return null;
		}
		return needFree ? areaObj.getFreePoint(x, y) : areaObj.getAreaPoint(x, y);
	}

	/**
	 * 获取点所在区域
	 * @param areaId
	 * @return
	 */
	public FGYLAreaObject getArea(int areaId) {
		return getAreas().get(areaId);
	}

	public Collection<FGYLAreaObject> getAreaVales() {
		return Collections.unmodifiableCollection(getAreas().values());
	}

	/**
	 * 获取点所在区域
	 * @param x
	 * @param y
	 * @return
	 */
	public FGYLAreaObject getArea(int x, int y) {
		return getAreas().get(getAreaId(x, y));
	}

	/**
	 * 区域中添加空闲点
	 * @param worldPoint
	 */
	public void addToAreaFreePoint(IFGYLWorldPoint worldPoint) {
		// 自身点从AreaObject移除
		AreaObject areaObject = getArea(worldPoint.getAreaId());
		areaObject.addFreePoint(worldPoint.getX(), worldPoint.getY());

		// 周围占用点从AreaObject移除
		List<Point> aroundPoints = getRhoAroundPointsDetail(worldPoint.getX(), worldPoint.getY(), worldPoint.getGridCnt(), worldPoint.getGridCnt(), false);
		for (Point point : aroundPoints) {
			AreaObject aroundAreaObject = getArea(point.getAreaId());
			aroundAreaObject.addFreePoint(point.getX(), point.getY());
		}
	}

	/**
	 * 区域中移除空闲点
	 * @param worldPoint
	 */
	public void rmFromAreaFreePoint(IFGYLWorldPoint worldPoint) {
		// 自身点从AreaObject移除
		FGYLAreaObject areaObject = getArea(worldPoint.getAreaId());
		areaObject.removeFreePoint(worldPoint.getX(), worldPoint.getY());
		// 周围占用点从AreaObject移除
		List<Point> aroundPoints = getRhoAroundPointsDetail(worldPoint.getX(), worldPoint.getY(), worldPoint.getGridCnt(), worldPoint.getGridCnt(), true);
		for (Point point : aroundPoints) {
			FGYLAreaObject aroundAreaObject = getArea(point.getAreaId());
			aroundAreaObject.removeFreePoint(point.getX(), point.getY());
		}
	}

	// public int[] randomBornPoint(IFGYLPlayer player) {
	// FGYLBase base = getBaseByCamp(player.getCamp());
	// return randomSubareaPoint(base.getSubarea(), player.getGridCnt());
	// }

	// public int[] randomSubareaPoint(int subareaNum, int redis) {
	// try {
	// FGYLRandPointSeed pointSeed = FGYLMapBlock.getInstance().getRandPointSeedBySubareaNum(subareaNum);
	// for (int i = 0; i < pointSeed.size(); i++) {
	// int[] xy = pointSeed.nextPoint();
	// if ((xy[0] + xy[1]) % 2 == redis % 2) {
	// continue;
	// }
	// // 检查是否能被占用
	// if (!tryOccupied(xy[0], xy[1], redis)) {
	// continue;
	// }
	// return new int[] { xy[0], xy[1] };
	// }
	// } catch (Exception e) {
	// HawkException.catchException(e);
	// }
	// IFGYLBuilding build = HawkRand.randomObject(getBuildingByType(FGYLBuildType.BA));
	// return randomFreePoint(GameUtil.splitXAndY(build.getPointId()), redis);
	// }

	public int[] randomFreePoint(int[] center, int redis) {

		List<Point> pointList = getRhoAroundPointsDetail(center[0], center[1], 40, 40, true);
		// 乱序
		Collections.shuffle(pointList);
		// 生成所需要的点
		for (Point point : pointList) {
			if ((point.getX() + point.getY()) % 2 == redis % 2) {
				continue;
			}
			// 检查是否能被占用
			if (!tryOccupied(point.getX(), point.getY(), redis)) {
				continue;
			}
			return new int[] { point.getX(), point.getY() };
		}
		return new int[2];

	}

	public List<IFGYLBuilding> getBuildingList() {
		return buildingList;
	}

	@SuppressWarnings("unchecked")
	public <T extends IFGYLBuilding> List<T> getBuildingByType(FGYLBuildType type) {
		return new ArrayList<>((Collection<T>) builTypeMap.get(type));
	}

	/**通过建筑id取得建筑*/
	@SuppressWarnings("unchecked")
	public <T extends IFGYLBuilding> T getBuildingByCfgId(int buildId) {
		return (T) buildIdBuildMap.get(buildId);
	}

	public IFGYLBuilding randomBuildByType(FGYLBuildType type) {
		List<IFGYLBuilding> list = getBuildingByType(type);
		if (list.isEmpty()) {
			return null;
		}
		int randomPointIndex = HawkRand.randInt();
		return list.get(randomPointIndex % list.size());
	}

	public List<IFGYLWorldPoint> getViewPointsList() {
		return new ArrayList<>(viewPoints.values());
	}

	@SuppressWarnings("unchecked")
	public <T extends IFGYLBuilding> List<T> getFGYLBuildingByClass(Class<T> type) {
		return (List<T>) buildingList.stream().filter(b -> b.getClass() == type).collect(Collectors.toList());
	}

}
