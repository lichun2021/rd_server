package com.hawk.game.script;

import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GsConst;

/**
 * 收集
 * http://localhost:8080/script/collectRes?playerId=7pt-4by1s-3&buildType=2102
 * playerName: 玩家名字
 * 
 * @author lating
 *
 */
public class CollectResourceHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			int buildType = BuildingType.OIL_WELL_VALUE;
			if (params.containsKey("buildType")) {
				int type = Integer.valueOf(params.get("buildType"));
				if (BuildingCfg.isResBuildingType(type)) {
					buildType = type;
				}
			}
			
			List<BuildingBaseEntity> buildingList = player.getData().getBuildingEntities();
			long curTime = HawkTime.getMillisecond();
			StringBuilder sb = new StringBuilder(HawkScript.HTTP_NEW_LINE);
			for (BuildingBaseEntity building : buildingList) {
				if (building.getType() != buildType) {
					continue;
				}
				
				//building.setLastResCollectTime(1517581963718L);
				BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
				resStore(player, building.getId(), buildCfg, curTime, building.getLastResCollectTime(), sb);
			}
			
			return successResponse(sb.toString());

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
	
	/**
	 * 可收取资源建筑当前储量
	 * @param player
	 * @param buidingId
	 * @param buildCfg
	 * @param timeLong
	 */
	private long resStore(Player player, String buidingId, BuildingCfg buildCfg, long timeNow, long lastTime, StringBuilder sb) {
		double allResEffect = player.getData().getEffVal(EffType.RES_OUTPUT);
		double allResEffBoost = player.getData().getEffVal(EffType.RES_OUTPUT_BOOST);
		double addPercent = 0d;
		double addBoost = 0d;
		switch (buildCfg.getBuildType()) {
			case BuildingType.ORE_REFINING_PLANT_VALUE: {
				addPercent = player.getData().getEffVal(EffType.RES_OUTPUT_GOLDORE, buidingId);
				addBoost = player.getData().getEffVal(EffType.RES_OUTPUT_GOLDORE_BOOST);
				break;
			}
			case BuildingType.OIL_WELL_VALUE: {
				addPercent = player.getData().getEffVal(EffType.RES_OUTPUT_OIL, buidingId);
				addBoost = player.getData().getEffVal(EffType.RES_OUTPUT_OIL_BOOST);
				break;
			}
			case BuildingType.STEEL_PLANT_VALUE: {
				addPercent = player.getData().getEffVal(EffType.RES_OUTPUT_STEEL, buidingId);
				addBoost = player.getData().getEffVal(EffType.RES_OUTPUT_STEEL_BOOST);
				break;
			}
			case BuildingType.RARE_EARTH_SMELTER_VALUE: {
				addPercent = player.getData().getEffVal(EffType.RES_OUTPUT_ALLOY, buidingId);
				addBoost = player.getData().getEffVal(EffType.RES_OUTPUT_ALLOY_BOOST);
				break;
			}
			default:
				break;
		}
		
 		// 每小时产量
		final int resPerHour = buildCfg.getResPerHour();
		double actualRate = resPerHour * (1 + (allResEffect + addPercent) * GsConst.EFF_PER) * (1 + GsConst.EFF_PER * (allResEffBoost + addBoost));
		// 资源建筑最大储量
		double actualLimit = buildCfg.getResLimit() * (1 + (allResEffect + addPercent) * GsConst.EFF_PER) * (1 + GsConst.EFF_PER * (allResEffBoost + addBoost));
		// 产量
		long product = (long) ((timeNow - lastTime) * 1.0D / GsConst.HOUR_MILLI_SECONDS * actualRate);
		product = (long) Math.min(actualLimit, product);
		
		HawkLog.logPrintln("resource store calc script, playerId: {}, cfgId: {}, buildingId: {}, allResEffect: {}, addPercent: {}, "
				+ "allResEffBoost: {}, addBoost: {}, product: {}, actualLimit: {}, resPerHour: {}, ResLimit: {}, lastTime: {}, timelong: {}", 
				player.getId(), buildCfg.getId(), buidingId, allResEffect, addPercent, allResEffBoost, addBoost, product, actualLimit, 
				resPerHour, buildCfg.getResLimit(), HawkTime.formatTime(lastTime), (timeNow - lastTime) * 1.0D / GsConst.HOUR_MILLI_SECONDS);
				
		sb.append("buildCfgId: ").append(buildCfg.getId())
		  .append(", buildUUid: ").append(buidingId)
		  .append(", allResEffect: ").append(allResEffect)
		  .append(", addPercent: ").append(addPercent)
		  .append(", allResEffBoost: ").append(allResEffBoost)
		  .append(", addBoost: ").append(addBoost)
		  .append(", product: ").append(product)
		  .append(", productLimit: ").append(actualLimit)
		  .append(", resPerHour: ").append(resPerHour)
		  .append(", resLimit: ").append(buildCfg.getResLimit())
		  .append(", lastTime: ").append(HawkTime.formatTime(lastTime))
		  .append(", timelong: ").append(timeNow - lastTime)
		  .append(HawkScript.HTTP_NEW_LINE);

		return product;
	}
}
