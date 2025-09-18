package com.hawk.game.module.lianmengyqzz.battleroom;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZPylonCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZPylon;

/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class YQZZPylonCreater {
	private YQZZPylonRefesh parent;
	private int monsterCount;
	private YQZZPylonCfg monstercfg;

	private YQZZPylonCreater() {
	}

	static YQZZPylonCreater create(YQZZPylonRefesh parent, YQZZPylonCfg monstercfg) {
		int count = 0;
		for (IYQZZWorldPoint point : parent.getParent().getViewPoints()) {
			if (point instanceof YQZZPylon) {
				YQZZPylon monster = (YQZZPylon) point;
				if (monster.getCfgId() == monstercfg.getId()) {
					count++;
				}
			}
		}

		YQZZPylonCreater result = new YQZZPylonCreater();
		result.parent = parent;
		result.monsterCount = monstercfg.getRefreshCount() - count;
		result.monstercfg = monstercfg;
		return result;
	}

	public void onTick() {
		if (monsterCount > 0) {
			monsterCount--;
			try {
				YQZZPylon monster = YQZZPylon.create(parent.getParent(), monstercfg);
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
		IYQZZBuilding build = parent.getParent().getWorldPointService().randomBuildByType(monstercfg.randomBoinPoint());
		return parent.getParent().getWorldPointService().randomSubareaPoint(build.getSubarea(), gridCnt);
	}
}