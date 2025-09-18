package com.hawk.game.module.homeland.map;

import com.hawk.game.module.homeland.cfg.HomeLandConstKVCfg;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import org.hawk.config.HawkConfigManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class HomeLandMap {
    public int worldMaxX;
    public int worldMaxY;
    public int WorldResRefreshWidth = 30;
    public int WorldResRefreshHeight = 30;
    public int theme;
    /**
     * 资源区域块 <块id,块数据>
     */
    private Map<Integer, HomeLandAreaObject> areaMap = new ConcurrentHashMap<>();
    private Map<Integer, IHomeLandPoint> viewMap = new ConcurrentHashMap<>();

    public HomeLandMap() {

    }

    public void init(int theme) {
        this.theme = theme;
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        String rangeStr = cfg.getMapRange();
        String[] range = rangeStr.split(",");
        worldMaxX = Integer.parseInt(range[0]) - 1;
        worldMaxY = Integer.parseInt(range[1]) - 1;
        WorldResRefreshWidth = Integer.parseInt(range[0]);
        WorldResRefreshHeight = Integer.parseInt(range[1]);
        loadMapAreas();
    }


    public Map<Integer, HomeLandAreaObject> getAreas() {
        return areaMap;
    }

    public int getAreaId(int x, int y) {
        int areaCols = worldMaxX / WorldResRefreshWidth;
        areaCols = worldMaxX % WorldResRefreshWidth == 0 ? areaCols : areaCols + 1;
        // 计算行列
        int col = x / WorldResRefreshWidth;
        int row = y / WorldResRefreshHeight;
        // 构建地图区块
        return row * areaCols + col + 1;
    }

    /**
     * 获取点所在区域
     *
     * @param x
     * @param y
     * @return
     */
    public HomeLandAreaObject getArea(int x, int y) {
        return getAreas().get(getAreaId(x, y));
    }

    /**
     * 获取点所在区域
     *
     * @param areaId
     * @return
     */
    public HomeLandAreaObject getArea(int areaId) {
        return getAreas().get(areaId);
    }

    public boolean tryOccupied(int x, int y, int width, int height) {
        HomeLandAreaObject areaObj = getArea(x, y);
        if (areaObj == null) {
            return false;
        }
        Point centerPoint = areaObj.getFreePoint(x, y);
        if (Objects.isNull(centerPoint)) {
            return false;
        }
        // 距离边界的长度
        int boundary = 0;
        // 边界坐标点不可用的检测
        if (centerPoint.getX() <= boundary || centerPoint.getY() < boundary || centerPoint.getX() >= (worldMaxX - boundary)
                || centerPoint.getY() >= worldMaxY - boundary) {
            return false;
        }
        // 中心点是否被占用
        if (viewMap.containsKey(centerPoint.getId())) {
            return false;
        }
        // 城点距离限制范围内的所有点(1距离为4个点, 2距离为12个点)
        List<Point> freeAroundPoints = getRhoAroundPointsDetail(centerPoint.getX(), centerPoint.getY(), width, height, true);
        // 必须为4个, 否则就是有阻挡点存在
        return freeAroundPoints.size() == width * height - 1;
    }

    /**
     * 获取地图上的点
     *
     * @param x
     * @param y
     * @return
     */
    public Point getAreaPoint(int x, int y, boolean needFree) {
        HomeLandAreaObject areaObj = getArea(x, y);
        if (areaObj == null) {
            return null;
        }
        return needFree ? areaObj.getFreePoint(x, y) : areaObj.getAreaPoint(x, y);
    }

    public boolean canMove(int posX, int posY, IHomeLandPoint point) {
        if (!tryOccupied(posX, posY, point.getWidth(), point.getHeight())) {
            addToAreaFreePoint(point);
            if (!tryOccupied(posX, posY, point.getWidth(), point.getHeight())) {
                rmFromAreaFreePoint(point);
                return false;
            }
        }
        return true;
    }

    public void addViewPoint(IHomeLandPoint point) {
        viewMap.put(point.getPointId(), point);
        rmFromAreaFreePoint(point);
    }

    public boolean removeViewPoint(IHomeLandPoint vp) {
        boolean result = Objects.nonNull(viewMap.remove(vp.getPointId()));
        addToAreaFreePoint(vp);
        return result;
    }

    /**
     * 区域中添加空闲点
     */
    public void addToAreaFreePoint(IHomeLandPoint building) {
        // 自身点从AreaObject移除
        AreaObject areaObject = getArea(building.getX(), building.getY());
        areaObject.addFreePoint(building.getX(), building.getY());
        int width = building.getWidth();
        int height = building.getHeight();
        // 周围占用点从AreaObject移除
        List<Point> aroundPoints = getRhoAroundPointsDetail(building.getX(), building.getY(), width, height, false);
        for (Point point : aroundPoints) {
            AreaObject aroundAreaObject = getArea(point.getAreaId());
            aroundAreaObject.addFreePoint(point.getX(), point.getY());
        }
    }

    /**
     * 计算特定中心点周围的有效点, 不包含中心点本身(菱形区域)
     *
     * @param centerX
     * @param centerY
     * @param radiusX
     * @param radiusY
     * @return
     */
    public List<Point> getRhoAroundPointsDetail(int centerX, int centerY, int radiusX, int radiusY, boolean needFree) {
        List<Point> aroundPoints = new LinkedList<>();
        if (radiusX <= 0 || radiusY <= 0) {
            return aroundPoints;
        }
        for (int i = 0; i < radiusX; i++) {
            for (int j = 0; j < radiusY; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                int x1 = centerX - i;
                int y1 = centerY - j;
                Point p1 = getAreaPoint(x1, y1, needFree);
                if (p1 != null) {
                    aroundPoints.add(p1);
                }
            }
        }
        return aroundPoints;
    }

    private void loadMapAreas() {
        List<HomeLandAreaObject> areaList = createMapAreas();
        for (HomeLandAreaObject areaObj : areaList) {
            // 创建区域内所有点对象
            if (areaObj.initAreaPoints()) {
                areaMap.put(areaObj.getId(), areaObj);
            }
        }
    }

    private List<HomeLandAreaObject> createMapAreas() {
        List<HomeLandAreaObject> areaList = new LinkedList<>();

        int areaCols = worldMaxX / WorldResRefreshWidth;
        areaCols = worldMaxX % WorldResRefreshWidth == 0 ? areaCols : areaCols + 1;

        int areaRows = worldMaxY / WorldResRefreshHeight;
        areaRows = worldMaxY % WorldResRefreshHeight == 0 ? areaRows : areaRows + 1;

        // 根据资源分块信息进行分块对象创建
        for (int row = 0; row <= areaRows; row++) {
            for (int col = 0; col <= areaCols; col++) {
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
                HomeLandAreaObject areaObj = new HomeLandAreaObject(theme, areaId, beginX, beginY, endX, endY);
                areaList.add(areaObj);
            }
        }
        return areaList;
    }

    /**
     * 区域中移除空闲点
     */
    public void rmFromAreaFreePoint(IHomeLandPoint building) {
        // 自身点从AreaObject移除
        HomeLandAreaObject areaObject = getArea(building.getX(), building.getY());
        areaObject.removeFreePoint(building.getX(), building.getY());
        int width = building.getWidth();
        int height = building.getHeight();
        // 周围占用点从AreaObject移除
        List<Point> aroundPoints = getRhoAroundPointsDetail(building.getX(), building.getY(), width, height, true);
        for (Point point : aroundPoints) {
            HomeLandAreaObject aroundAreaObject = getArea(point.getAreaId());
            aroundAreaObject.removeFreePoint(point.getX(), point.getY());
        }
    }
}
