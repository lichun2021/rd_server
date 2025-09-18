package com.hawk.game.lianmengstarwars;

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

import org.hawk.tuple.HawkTuple2;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.SWBattleCfg;
import com.hawk.game.config.SWCommandCenterCfg;
import com.hawk.game.config.SWHeadQuartersCfg;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldpoint.ISWBuilding;
import com.hawk.game.lianmengstarwars.worldpoint.SWCommandCenter;
import com.hawk.game.lianmengstarwars.worldpoint.SWHeadQuarters;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;

public class SWWorldPointService {
	private final SWBattleRoom parent;
	private boolean inited;

	private SWWorldScene worldScene;
	/**
	 * 资源区域块 <块id,块数据>
	 */
	private Map<Integer, SWAreaObject> areas = new ConcurrentHashMap<>();
	public int worldMaxX;
	public int worldMaxY;
	public final int WorldResRefreshWidth = 40;
	public final int WorldResRefreshHeight = 40;
	private Multimap<Class<? extends ISWBuilding>, ? super ISWBuilding> swBuildingMap = HashMultimap.create();
	private List<ISWBuilding> buildingList = new CopyOnWriteArrayList<>();
	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	private Map<Integer, ISWWorldPoint> viewPoints = new ConcurrentHashMap<>();
	private List<int[]> fuelBankPointList = new LinkedList<>();

	public SWWorldPointService(SWBattleRoom parent) {
		this.parent = parent;
	}

	public SWBattleRoom getParent() {
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

		SWBattleCfg bcfg = parent.getCfg();
		worldMaxX = bcfg.getMapX() + 1;
		worldMaxY = bcfg.getMapY() + 1;
		initWorldScene();
		loadWorldAreas();

		SWBattleCfg cfg = parent.getCfg();
		List<int[]> bornlist = cfg.copyOfRefreshPointList();
		Collections.shuffle(bornlist);
		while (fuelBankPointList.size() < 200) {
			fuelBankPointList.addAll(bornlist);
		}

		{
			SWHeadQuartersCfg buildcfg = SWHeadQuarters.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				SWHeadQuarters icd = new SWHeadQuarters(parent);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				swBuildingMap.put(icd.getClass(), icd);
				parent.setCenterX(icd.getX());
				parent.setCenterY(icd.getY());
				index++;
				addViewPoint(icd);
			}
		}

		{
			SWCommandCenterCfg buildcfg = SWCommandCenter.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				SWCommandCenter icd = new SWCommandCenter(parent);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				swBuildingMap.put(icd.getClass(), icd);
				index++;
				addViewPoint(icd);
			}
		}

		// 通知场景初始化完毕
		getWorldScene().notifyInitOK();
	}

	/** 添加世界点 */
	public void addViewPoint(ISWWorldPoint... vp) {
		List<ISWWorldPoint> list = new ArrayList<>(vp.length);
		for (ISWWorldPoint point : vp) {
			if (point != null) {
				list.add(point);
			}
		}

		for (ISWWorldPoint point : list) {
			viewPoints.put(point.getPointId(), point);
			int type = point instanceof ISWPlayer ? GsConst.WorldObjType.CITY : point.getPointType().getNumber();
			int aoiObjId = getWorldScene().add(type, 0, point.getX(), point.getY(), point);
			point.setAoiObjId(aoiObjId);
			rmFromAreaFreePoint(point);
		}

	}

	public boolean removeViewPoint(ISWWorldPoint vp) {
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
		worldScene = new SWWorldScene(worldMaxX, worldMaxY, searchRadius);
	}

	private void loadWorldAreas() {
		// 创建区域对象列表
		List<SWAreaObject> areaList = createWorldAreas();
		for (SWAreaObject areaObj : areaList) {
			// 创建区域内所有点对象
			if (areaObj.initAreaPoints(parent)) {
				areas.put(areaObj.getId(), areaObj);
			}
		}

	}

	private List<SWAreaObject> createWorldAreas() {
		List<SWAreaObject> areaList = new LinkedList<>();

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
				SWAreaObject areaObj = new SWAreaObject(areaId, beginX, beginY, endX, endY);
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

	public SWWorldScene getWorldScene() {
		return worldScene;
	}

	public Map<Integer, SWAreaObject> getAreas() {
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
		SWAreaObject areaObj = getArea(x, y);
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
		Optional<ISWWorldPoint> currPoint = parent.getWorldPoint(centerPoint.getX(), centerPoint.getY());
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
	 * 获取地图上的点
	 * @param x
	 * @param y
	 * @return
	 */
	public Point getAreaPoint(int x, int y, boolean needFree) {
		SWAreaObject areaObj = getArea(x, y);
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
	public SWAreaObject getArea(int areaId) {
		return getAreas().get(areaId);
	}

	public Collection<SWAreaObject> getAreaVales() {
		return Collections.unmodifiableCollection(getAreas().values());
	}

	/**
	 * 获取点所在区域
	 * @param x
	 * @param y
	 * @return
	 */
	public SWAreaObject getArea(int x, int y) {
		return getAreas().get(getAreaId(x, y));
	}

	/**
	 * 区域中添加空闲点
	 * @param worldPoint
	 */
	public void addToAreaFreePoint(ISWWorldPoint worldPoint) {
		// 自身点从AreaObject移除
		AreaObject areaObject = getArea(worldPoint.getAreaId());
		areaObject.addFreePoint(worldPoint.getX(), worldPoint.getY());

		// 周围占用点从AreaObject移除
		List<Point> aroundPoints = getRhoAroundPointsDetail(worldPoint.getX(), worldPoint.getY(), worldPoint.getWorldPointRadius(), worldPoint.getWorldPointRadius(), false);
		for (Point point : aroundPoints) {
			AreaObject aroundAreaObject = getArea(point.getAreaId());
			aroundAreaObject.addFreePoint(point.getX(), point.getY());
		}
	}

	/**
	 * 区域中移除空闲点
	 * @param worldPoint
	 */
	public void rmFromAreaFreePoint(ISWWorldPoint worldPoint) {
		// 自身点从AreaObject移除
		SWAreaObject areaObject = getArea(worldPoint.getAreaId());
		areaObject.removeFreePoint(worldPoint.getX(), worldPoint.getY());
		// 周围占用点从AreaObject移除
		List<Point> aroundPoints = getRhoAroundPointsDetail(worldPoint.getX(), worldPoint.getY(), worldPoint.getWorldPointRadius(), worldPoint.getWorldPointRadius(), true);
		for (Point point : aroundPoints) {
			SWAreaObject aroundAreaObject = getArea(point.getAreaId());
			aroundAreaObject.removeFreePoint(point.getX(), point.getY());
		}
	}

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
		return null;

	}

	public List<ISWBuilding> getBuildingList() {
		return buildingList;
	}

	@SuppressWarnings("unchecked")
	public <T extends ISWBuilding> List<T> getSWBuildingByClass(Class<T> type) {
		return new ArrayList<>((Collection<T>) swBuildingMap.get(type));
	}

	public Map<Integer, ISWWorldPoint> getViewPoints() {
		return viewPoints;
	}

}
