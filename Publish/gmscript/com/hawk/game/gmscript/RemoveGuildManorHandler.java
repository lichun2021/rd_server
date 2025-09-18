package com.hawk.game.gmscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.game.aoi.HawkAOIObj;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.WorldScene;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;

/**
 * 移除联盟领地
 * @author golden
 *
 */
public class RemoveGuildManorHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {

		if (HawkOSOperator.isEmptyString(params.get("serverId"))) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "have no param: serverId");
		}
		
		if (HawkOSOperator.isEmptyString(params.get("guildId"))) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "have no param: guildId");
		}
		
		if (HawkOSOperator.isEmptyString(params.get("manorId"))) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "have no param: manorId");
		}
		
		if (HawkOSOperator.isEmptyString(params.get("posX"))) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "have no param: posX");
		}

		if (HawkOSOperator.isEmptyString(params.get("posY"))) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "have no param: posY");
		}
		
		if (HawkOSOperator.isEmptyString(params.get("index"))) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "have no param: index");
		}
		
		String serverId = params.get("serverId");
		String guildId = params.get("guildId");
		String manorId = params.get("manorId");
		int posX = Integer.parseInt(params.get("posX"));
		int posY = Integer.parseInt(params.get("posY"));
		int index = Integer.parseInt(params.get("index"));
		
		removeGuildManor(serverId, guildId, manorId, posX, posY, index);
		
		return HawkScript.successResponse(null);
	}
	
	
	private void removeGuildManor(String serverId, String guildId, String manorId, int posX, int posY, int index) {
		
		// 移除其它建筑
		List<IGuildBuilding> builds = GuildManorService.getInstance().getGuildBuildings(guildId);
		for (IGuildBuilding build : builds) {
			if (build.isPlaceGround() && isGuildBuildCanRmove(build, guildId, manorId)) {
				GuildManorService.getInstance().removeManorBuilding(build);
			}
		}

		// 移除联盟大本
		GuildManorObj obj = GuildManorService.getInstance().getManorByIdx(guildId, index);
		rmGuildManor(manorId, posX, posY);
		obj.onMonorRemove();
		
	}
	
	/**
	 * 移除联盟领地
	 * 
	 * @param guildId
	 * @param manorId
	 */
	private void rmGuildManor(String rmManorId, int x, int y) {
		int radius = GuildManorService.getInstance().getRadiusByType(TerritoryType.GUILD_BASTION);

		// 找可能和要移除领地范围重合的领地中心点
		Set<HawkAOIObj> inviewObjs = WorldScene.getInstance().getRangeObjs(x, y, 4 * radius, 4 * radius);
		if (inviewObjs == null || inviewObjs.size() <= 0) {
			return;
		}
		
		// 需要重新计算联盟领地落座的列表
		List<String> calcManorIds = new ArrayList<>();
		calcManorIds.add(rmManorId);

		for (HawkAOIObj aoiObj : inviewObjs) {
			if (aoiObj.getType() != GsConst.WorldObjType.GUILD_TERRITORY) {
				continue;
			}
			WorldPoint terriToryPoint = WorldPointService.getInstance().getWorldPoint(aoiObj.getX(), aoiObj.getY());
			if (WorldUtil.isGuildBastion(terriToryPoint)) {
				calcManorIds.add(terriToryPoint.getGuildBuildId());
			}
		}

		// 领地按照完成时间排序
		GuildManorService.getInstance().sortManorIdsByTime(calcManorIds);

		// 列表移除要删除的领地
		calcManorIds.remove(rmManorId);
	}
	
	/**
	 * 联盟建筑是否可移除
	 * TODO zhenyu.shang 此方法有问题，目前移除领地之后，如果领地范围有重叠，则重叠范围内的建筑仍然会被移除掉
	 * 		 （与策划讨论，目前先按照优先建成原则，如果属于之前建成的领地，则无论重叠与否，都进行删除）
	 * @param build
	 * @param guildId
	 * @return
	 */
	private boolean isGuildBuildCanRmove(IGuildBuilding build, String guildId, String manorId) {
		TerritoryType type = build.getBuildType();
		//不可能是大本
		HawkAssert.isTrue(!WorldUtil.isGuildBastion(type));
		// 是否可移除
		boolean canRmove = true;
		int radius = GuildManorService.getInstance().getRadiusByType(type);
		List<Point> arroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(build.getEntity().getPosX(), build.getEntity().getPosY(), radius);
		
		// 添加中心点
		Point centerPoint = WorldPointService.getInstance().getAreaPoint(build.getEntity().getPosX(), build.getEntity().getPosY(), false);
		if (centerPoint != null) {
			arroundPoints.add(centerPoint);
		}
		
		for (Point point : arroundPoints) {
			// 建筑点全部在将要移除的领地上，移除建筑
			if (!GuildManorService.getInstance().isInManor(guildId, manorId, point.getId())) {
				canRmove = false;
				break;
			}
		}
		return canRmove;
	}
}