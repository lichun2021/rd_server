package com.hawk.game.nation.tech.skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.NationTechCfg;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.CapitalAreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 丰饶之地
 * @author Golden
 *
 */
public class NationTechSkill107 {

	public static void touchSkill() {
		int techId = 10701;
		
		NationTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationTechCfg.class, techId);
		
		// param1生成资源数量
		int genCount = Integer.parseInt(cfg.getParam1());
		// param2覆盖半径
		int radius = Integer.parseInt(cfg.getParam2());
		// param3资源点id_权重,资源点id_权重
		String resourceIds = cfg.getParam3(); // "300102_5"
		// param4采集加速329buff值
		int eff329 = Integer.parseInt(cfg.getParam4().split("_")[1]);
		
		Map<Integer, Integer> resourceWeightMap = SerializeHelper.cfgStr2Map(resourceIds);
		
		// 取8个战区范围内的点
		List<Point> allPoints = new ArrayList<>();
		List<Integer> pointIds = SuperWeaponService.getInstance().getSuperWeaponPoints();
		for (Integer pointId : pointIds) {
			List<Point> points = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, radius);
			for (Point point : points) {
				if ((point.getX() + point.getY()) % 2 != 0) {
					continue;
				}
				allPoints.add(point);
			}
		}
		Collections.shuffle(allPoints);
	
		// 生成点
		for (int i = 0; i < genCount; i++) {
			Point point = allPoints.get(i);
			int resourceId = HawkRand.randomWeightObject(resourceWeightMap);
			genResourcePoint(point.getX(), point.getY(), resourceId, eff329);
		}
	}
	
	public static void genResourcePoint(int x, int y, int resourceId, int eff329) {
		int pointId = GameUtil.combineXAndY(x, y);
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		if (point != null) {
			return;
		}
		WorldResourceCfg resCfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, resourceId);
		if (resCfg == null) {
			return;
		}
		AreaObject areaObj = WorldPointService.getInstance().getArea(x, y);
		Point bornPoint = areaObj.getFreePoint(x, y);
		if (bornPoint == null) {
			return;
		}
		// 创建世界点对象
		WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.RESOURCE_VALUE);
		worldPoint.setResourceId(resCfg.getId());
		int resNum = resCfg.getResNum();
		worldPoint.setRemainResNum(resNum);
		worldPoint.setLifeStartTime(HawkTime.getMillisecond());
		
		EffectObject eff = new EffectObject(EffType.SKILL_RESOURCE_COLLECT_SPEED_UP_VALUE, eff329);
		worldPoint.setShowEffect(eff.toString());
		
		WorldPointProxy.getInstance().create(worldPoint);
		if (WorldPointService.getInstance().isInCapitalArea(bornPoint.getId())) {
			CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
			captialArea.addResourcePoint(resCfg.getResType(), pointId);
		} else {
			areaObj.addResourcePoint(resCfg.getResType(), pointId);
		}
		// 放入世界点列表信息中
		WorldPointService.getInstance().addPoint(worldPoint);
		
		WorldPointService.logger.info("NationTechSkill1001 gen point, x:{}, y:{}, resourceId:{}", x, y, resourceId);
	}
}
