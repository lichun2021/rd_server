package com.hawk.game.module.lianmengyqzz.battleroom;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZFoggyCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZFoggyFortress;

/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class YQZZFoggyCreater {
	private YQZZFoggyRefesh parent;
	private int foggyCount;
	private YQZZFoggyCfg foggycfg;

	private YQZZFoggyCreater() {
	}

	static YQZZFoggyCreater create(YQZZFoggyRefesh parent, YQZZFoggyCfg monstercfg) {
		int count = 0;
		for (IYQZZWorldPoint point : parent.getParent().getViewPoints()) {
			if (point instanceof YQZZFoggyFortress) {
				YQZZFoggyFortress monster = (YQZZFoggyFortress) point;
				if (monster.getCfgId() == monstercfg.getId()) {
					count++;
				}
			}
		}

		YQZZFoggyCreater result = new YQZZFoggyCreater();
		result.parent = parent;
		result.foggyCount = monstercfg.getRefreshCount() - count;
		result.foggycfg = monstercfg;
		return result;
	}

	public void onTick() {
		if (foggyCount > 0) {
			foggyCount--;
			try {
				YQZZFoggyFortress foggy = YQZZFoggyFortress.create(parent.getParent(), foggycfg);
				int[] xy = popWorldPoint(foggy.getGridCnt());
				if (xy != null) {
					foggy.setX(xy[0]);
					foggy.setY(xy[1]);
					parent.getParent().getWorldPointService().addViewPoint(foggy);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	public int[] popWorldPoint(int gridCnt) {
		IYQZZBuilding build = parent.getParent().getWorldPointService().randomBuildByType(foggycfg.randomBoinPoint());
		return parent.getParent().getWorldPointService().randomSubareaPoint(build.getSubarea(), gridCnt);
	}
}