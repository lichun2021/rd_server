package com.hawk.game.module.lianmengyqzz.battleroom;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZResourceCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZResource;

/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class YQZZResourceCreater {
	private YQZZResourceRefesh parent;
	private int resourceCount;
	private YQZZResourceCfg resourcecfg;

	private YQZZResourceCreater() {
	}

	static YQZZResourceCreater create(YQZZResourceRefesh parent, YQZZResourceCfg monstercfg) {
		int count = 0;
		for (IYQZZWorldPoint point : parent.getParent().getViewPoints()) {
			if (point instanceof YQZZResource) {
				YQZZResource monster = (YQZZResource) point;
				if (monster.getCfgId() == monstercfg.getId()) {
					count++;
				}
			}
		}
		YQZZResourceCreater result = new YQZZResourceCreater();
		result.parent = parent;
		result.resourceCount = monstercfg.getRefreshCount() - count;
		result.resourcecfg = monstercfg;
		return result;
	}

	public void onTick() {
		if (resourceCount > 0) {
			resourceCount--;
			try {
				YQZZResource monster = YQZZResource.create(parent.getParent(), resourcecfg);
				int[] xy = popWorldPoint(monster.getGridCnt());
				if (xy != null) {
					monster.setX(xy[0]);
					monster.setY(xy[1]);
					parent.getParent().getWorldPointService().addViewPoint(monster);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	public int[] popWorldPoint(int gridCnt) {
		IYQZZBuilding build = parent.getParent().getWorldPointService().randomBuildByType(resourcecfg.randomBoinPoint());
		return parent.getParent().getWorldPointService().randomSubareaPoint(build.getSubarea(), gridCnt);
	}
}