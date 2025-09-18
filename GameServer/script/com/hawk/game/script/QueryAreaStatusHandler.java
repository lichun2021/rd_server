package com.hawk.game.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.CapitalAreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;

/**
 * 
 * @author golden
 *
 * localhost:8080/script/areaStatus?queryAreaMonster=1&areaId=1
 * localhost:8080/script/areaStatus?queryAreaResource=1&areaId=0
 * localhost:8080/script/areaStatus?queryAreaStrongpoint=1&areaId=0
 */
public class QueryAreaStatusHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		
		// 查询野怪
		if (params.containsKey("queryAreaMonster")) {
			if (!params.containsKey("areaId")) {
				return HawkScript.successResponse("missing require param areaId !");
			}
			
			int areaId = Integer.valueOf(params.get("areaId"));
			return areaId == 0 ?  HawkScript.successResponse(queryAreaMonster()) : HawkScript.successResponse(queryAreaMonster(areaId));
		}
		
		// 查询资源
		if (params.containsKey("queryAreaResource")) {
			if (!params.containsKey("areaId")) {
				return HawkScript.successResponse("missing require param areaId !");
			}
			int areaId = Integer.valueOf(params.get("areaId"));
			return areaId == 0 ?  HawkScript.successResponse(queryAreaaResource()) : HawkScript.successResponse(queryAreaaResource(areaId));
		}
		
		// 查询据点
		if (params.containsKey("queryAreaStrongpoint")) {
			if (!params.containsKey("areaId")) {
				return HawkScript.successResponse("missing require param areaId !");
			}
			int areaId = Integer.valueOf(params.get("areaId"));
			return areaId == 0 ?  HawkScript.successResponse(queryAreaStrongpoint()) : HawkScript.successResponse(queryAreaStrongpoint(areaId));
		}
		
		return HawkScript.successResponse("no params !");
	}
	
	public static String queryAreaStrongpoint() {
		StringBuilder sb = new StringBuilder().append("<br>");
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		sb.append("黑土据点数量：").append(captialArea.getStrongpointNum()).append("<br>");
		sb.append("<br>");
		
		Collection<AreaObject> areaVales = WorldPointService.getInstance().getAreaVales();
		for (AreaObject area : areaVales) {
			sb.append("区域：").append(area.getId()).append(", 据点数量：").append(area.getStrongpointNum()).append("<br>");
		}
		
		return HawkScript.successResponse(sb.toString());
	}
	
	public static String queryAreaStrongpoint(int areaId) {
		AreaObject area = WorldPointService.getInstance().getArea(areaId);
		if (area == null) {
			return "area nul";
		}
		
		Map<Integer, Set<WorldPoint>> strongpoints = new HashMap<>();
		for (Point p : area.getAllPointList()) {
			if (area.isFreePoint(p.getX(), p.getY())) {
				continue;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(p.getId());
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.STRONG_POINT_VALUE) {
				continue;
			}
			Set<WorldPoint> points = strongpoints.get(worldPoint.getMonsterId());
			if (points == null) {
				points = new HashSet<>();
				strongpoints.put(worldPoint.getMonsterId(), points);
			}
			points.add(worldPoint);
		}
		
		StringBuilder sb = new StringBuilder();
		StringBuilder totalSb = new StringBuilder();
		
		int totalCount = 0;
		for (Entry<Integer, Set<WorldPoint>> entry : strongpoints.entrySet()) {
			totalCount += entry.getValue().size();
			
			sb.append("据点id: ").append(entry.getKey()).append(", ").append(" 数量:").append(entry.getValue().size()).append("<br>");
			for (WorldPoint point :  entry.getValue()) {
				sb.append("x: ").append(point.getX()).append(", ")
					.append(" y: ").append(point.getY()).append(", ")
					.append(" zoneId: ").append(point.getZoneId())
					.append("<br>");
			}
			sb.append("<br>");
		}
		
		totalSb.append("总数：").append(totalCount).append("<br>").append("<br>");
		return HawkScript.successResponse(totalSb.toString() + sb.toString());
	}
	
	
	
	public String queryAreaaResource() {
		StringBuilder sb = new StringBuilder().append("<br>");
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		sb.append("黑土地资源数量：").append(captialArea.getResourceNum()).append("<br>");
		for (Entry<Integer, Set<Integer>> entry : captialArea.getResourcePoints().entrySet()) {
			sb.append("资源类型：").append(entry.getKey()).append(", 资源数量：").append(entry.getValue().size()).append("<br>");
		}
		sb.append("<br>");
		
		Collection<AreaObject> areaVales = WorldPointService.getInstance().getAreaVales();
		for (AreaObject area : areaVales) {
			sb.append("区域：").append(area.getId()).append(", 资源数量：").append(area.getResourceNum()).append("<br>");
			for (Entry<Integer, Set<Integer>> entry : area.getResourcePoints().entrySet()) {
				sb.append("资源类型：").append(entry.getKey()).append(", 资源数量：").append(entry.getValue().size()).append("<br>");
			}
		}
		
		return HawkScript.successResponse(sb.toString());
	}
	
	public String queryAreaaResource(int areaId) {
		AreaObject area = WorldPointService.getInstance().getArea(areaId);
		if (area == null) {
			return "area nul";
		}
		
		Map<Integer, List<WorldPoint>> points = new HashMap<>();
		Map<Integer, List<WorldPoint>> typePoints = new HashMap<>();
		Map<Integer, List<WorldPoint>> levelPoints = new HashMap<>();
		
		for (Point p : area.getAllPointList()) {
			if (area.isFreePoint(p.getX(), p.getY())) {
				continue;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(p.getId());
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE) {
				continue;
			}
			List<WorldPoint> resources = points.get(worldPoint.getResourceId());
			if (resources == null) {
				resources = new ArrayList<>();
				points.put(worldPoint.getResourceId(), resources);
			}
			resources.add(worldPoint);
			
			WorldResourceCfg config = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, worldPoint.getResourceId());
			
			List<WorldPoint> typeResources = typePoints.get(config.getResType());
			if (typeResources == null) {
				typeResources = new ArrayList<>();
				typePoints.put(config.getResType(), typeResources);
			}
			typeResources.add(worldPoint);
			
			List<WorldPoint> levelResources = levelPoints.get(config.getLevel());
			if (levelResources == null) {
				levelResources = new ArrayList<>();
				levelPoints.put(config.getLevel(), levelResources);
			}
			levelResources.add(worldPoint);
		}
		
		StringBuilder sb = new StringBuilder().append("<br>");
		int count = 0;
		for (Entry<Integer, List<WorldPoint>> entry : points.entrySet()) {
			sb.append("资源id: ").append(entry.getKey()).append(", ").append(" 数量:").append(entry.getValue().size()).append("<br>");
			count += entry.getValue().size();
		}
		sb.append("资源总数: ").append(count).append("<br>");
		sb.append("<br>");
		
		count = 0;
		for (Entry<Integer, List<WorldPoint>> entry : typePoints.entrySet()) {
			sb.append("资源类型: ").append(entry.getKey()).append(", ").append(" 数量:").append(entry.getValue().size()).append("<br>");
			count += entry.getValue().size();
		}
		sb.append("资源总数: ").append(count).append("<br>");
		sb.append("<br>");
		
		count = 0;
		for (Entry<Integer, List<WorldPoint>> entry : levelPoints.entrySet()) {
			sb.append("资源等级: ").append(entry.getKey()).append(", ").append(" 数量:").append(entry.getValue().size()).append("<br>");
			count += entry.getValue().size();
		}
		sb.append("资源总数: ").append(count).append("<br>");
		sb.append("<br>");
		
		for (List<WorldPoint> pointList : points.values()) {
			for (WorldPoint point : pointList) {
				WorldResourceCfg config = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, point.getResourceId());
				
				sb.append("资源id:").append(config.getId()).append(", ")
					.append("资源类型:").append(config.getResType()).append(", ")
					.append("资源等级:").append(config.getLevel()).append(", ")
					.append("x: ").append(point.getX()).append(", ")
					.append("y: ").append(point.getY())
					.append("<br>");
				
			}
			sb.append("<br>");
		}
		
		return sb.toString();
	}
	
	public String queryAreaMonster() {
		StringBuilder sbTotal = new StringBuilder().append("<br>");
		StringBuilder sb = new StringBuilder().append("<br>");
		
		int capitalCount = 0;
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		ConfigIterator<WorldEnemyCfg> captialAreaIter = HawkConfigManager.getInstance().getConfigIterator(WorldEnemyCfg.class);
		while (captialAreaIter.hasNext()) {
			WorldEnemyCfg enemyCfg = captialAreaIter.next();
			int monsterNum = captialArea.getCommonMonsterNum(enemyCfg.getId());
			int activityMonsterNum = captialArea.getActivityMonsterNum(enemyCfg.getId());
			if (monsterNum > 0 || activityMonsterNum > 0) {
				sb.append("captialArea: ").append(", ");
				sb.append("monsterId: ").append(enemyCfg.getId()).append(", ");
				if (monsterNum > 0) {
					sb.append("monsterNum: ").append(monsterNum).append("<br>");
				} else {
					sb.append("monsterNum: ").append(activityMonsterNum).append("<br>");
				}
				capitalCount += monsterNum;
				capitalCount += activityMonsterNum;
			}
		}
		sbTotal.append("captialNumber: ").append(capitalCount).append("<br>");
		
		Collection<AreaObject> areas = WorldPointService.getInstance().getAreaVales();
		for (AreaObject area : areas) {
			int areaTotalCount = 0;
			ConfigIterator<WorldEnemyCfg> enemyCfgIter = HawkConfigManager.getInstance().getConfigIterator(WorldEnemyCfg.class);
			while (enemyCfgIter.hasNext()) {
				WorldEnemyCfg enemyCfg = enemyCfgIter.next();
				int monsterNum = area.getCommonMonsterNum(enemyCfg.getId());
				int activityMonsterNum = area.getActivityMonsterNum(enemyCfg.getId());
				if (monsterNum > 0 || activityMonsterNum > 0) {
					sb.append("areaId: ").append(area.getId()).append(", ");
					sb.append("monsterId: ").append(enemyCfg.getId()).append(", ");
					if (monsterNum > 0) {
						sb.append("monsterNum: ").append(monsterNum).append("<br>");
					} else {
						sb.append("monsterNum: ").append(activityMonsterNum).append("<br>");
					}
					areaTotalCount += monsterNum;
					areaTotalCount += activityMonsterNum;
				}
			}
			sbTotal.append("areaNum, areaId: ").append(area.getId()).append(", number: ").append(areaTotalCount).append("<br>");
		}
		
		return HawkScript.successResponse(sbTotal.toString() + sb.toString());
	}
	
	public String queryAreaMonster(int areaId) {
		AreaObject area = WorldPointService.getInstance().getArea(areaId);
		if (area == null) {
			return "area nul";
		}
		
		Map<Integer, List<WorldPoint>> points = new HashMap<>();
		for (Point p : area.getAllPointList()) {
			if (area.isFreePoint(p.getX(), p.getY())) {
				continue;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(p.getId());
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.MONSTER_VALUE) {
				continue;
			}
			List<WorldPoint> monster = points.get(worldPoint.getMonsterId());
			if (monster == null) {
				monster = new ArrayList<>();
				points.put(worldPoint.getMonsterId(), monster);
			}
			monster.add(worldPoint);
		}
		
		StringBuilder sb = new StringBuilder().append("<br>");
		for (Entry<Integer, List<WorldPoint>> entry : points.entrySet()) {
			sb.append("monster: ").append(entry.getKey()).append(", ").append(" count:").append(entry.getValue().size()).append("<br>").append("<br>");
		}
		
		for (List<WorldPoint> pointList : points.values()) {
			for (WorldPoint point : pointList) {
				sb.append("monster:").append(point.getMonsterId()).append(", ")
					.append("x: ").append(point.getX()).append(", ")
					.append("y: ").append(point.getY())
					.append("<br>");
				
			}
			sb.append("<br>");
		}
		
		return sb.toString();
	}
}
