package com.hawk.game.gmscript;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldDelayTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 清除玩家城点
 * @author golden
 *
 * curl 'localhost:8080/script/cleanPlayerPoint?registerTime=&logoutTime=&cityLevel=&delayTime=&onceRemoveCount='
 * registerTime 移除距离现在x秒之前注册的玩儿家
 * logoutTime 	移除距离现在时间x秒之前登出的玩儿家
 * cityLevel 	移除小于等于cityLevel等级大本的玩家
 * delayTime 	x秒只能执行完所有，默认十分钟
 * onceRemoveCount 分批移除，一次移除的数量
 */
public class CleanPlayerPointHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		
		int registerTime = Integer.parseInt(params.get("registerTime"));
		int logoutTime = Integer.parseInt(params.get("logoutTime"));
		int cityLevel = Integer.parseInt(params.get("cityLevel"));

		int delayTime = 600;
		if (!HawkOSOperator.isEmptyString(params.get("delayTime"))) {
			delayTime = Integer.parseInt(params.get("delayTime"));
		}
		
		int onceRemoveCount = 100;
		if (!HawkOSOperator.isEmptyString(params.get("onceRemoveCount"))) {
			delayTime = Integer.parseInt(params.get("onceRemoveCount"));
		}
		
		return cleanWorldPoint(registerTime, logoutTime, cityLevel, delayTime * 1000L, onceRemoveCount);
	}	

	/**
	 * 清理世界城点
	 * 
	 * @param params
	 * @return
	 */
	public String cleanWorldPoint(int registerTime, int logoutTime, int cityLevel, long delayTime, int onceRemoveCount) {

		List<WorldPoint> needRemove = new ArrayList<>();

		try {
			Method method = WorldPointService.class.getDeclaredMethod("getWorldPointsByType", WorldPointType.class);

			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<WorldPoint> worldPointList = (List<WorldPoint>) method.invoke(WorldPointService.getInstance(),
					WorldPointType.PLAYER);

			for (WorldPoint worldPoint : worldPointList) {
				if (HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
					continue;
				}

				if (worldPoint.getCityLevel() > cityLevel) {
					continue;
				}

				long offlineTime = HawkTime.getMillisecond() - worldPoint.getLastActiveTime();
				if (offlineTime < logoutTime * 1000L) {
					continue;
				}

				Player player = GlobalData.getInstance().scriptMakesurePlayer(worldPoint.getPlayerId());
				long offRegisterTime = HawkTime.getMillisecond() - player.getPlayerBaseEntity().getCreateTime();
				if (offRegisterTime < registerTime * 1000L) {
					continue;
				}

				needRemove.add(worldPoint);
			}

			for (List<WorldPoint> removeList : subList(needRemove, onceRemoveCount)) {
				WorldThreadScheduler.getInstance().postDelayWorldTask(
						new WorldDelayTask(GsConst.WorldTaskType.MIGRATE_OUT_PLAYER, delayTime, delayTime, 1) {
							@Override
							public boolean onInvoke() {
								for (WorldPoint point : removeList) {
									Player player = GlobalData.getInstance().scriptMakesurePlayer(point.getPlayerId());
									if (player.isActiveOnline()) {
										continue;
									}

									WorldPlayerService.getInstance().removeCity(point.getPlayerId(), true);
									AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(point.getPlayerId());
									String puid = accountInfo == null ? "" : accountInfo.getPuid();
									HawkLog.logPrintln("sysop remove player city, playerId:{}, puid:{}", point.getPlayerId(), puid);

								}
								return true;
							}
						});
			}

		} catch (Exception e) {
			HawkException.catchException(e, "clean world point:{}");
			return HawkScript.failedResponse(-1, "clean world fail");
		}

		return HawkScript.successResponse("ok size=" + needRemove.size());
	}

	public List<List<WorldPoint>> subList(List<WorldPoint> points, int size) {
		List<List<WorldPoint>> pointList = new ArrayList<List<WorldPoint>>();
		int thisSize = points.size() % size == 0 ? points.size() / size : points.size() / size + 1;
		for (int i = 0; i < thisSize; i++) {
			List<WorldPoint> sub = new ArrayList<>();
			for (int j = i * size; j <= size * (i + 1) - 1; j++) {
				if (j < points.size()) {
					sub.add(points.get(j));
				}
			}
			pointList.add(sub);
		}
		return pointList;
	}
}
