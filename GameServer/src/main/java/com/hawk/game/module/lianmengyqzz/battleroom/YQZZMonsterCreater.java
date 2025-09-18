package com.hawk.game.module.lianmengyqzz.battleroom;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZMonsterCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZMonster;

/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class YQZZMonsterCreater {
	private YQZZMonsterRefesh parent;
	private int monsterCount;
	private YQZZMonsterCfg monstercfg;

	private YQZZMonsterCreater() {
	}

	static YQZZMonsterCreater create(YQZZMonsterRefesh parent, YQZZMonsterCfg monstercfg) {
		int count = 0;
		for (IYQZZWorldPoint point : parent.getParent().getViewPoints()) {
			if (point instanceof YQZZMonster) {
				YQZZMonster monster = (YQZZMonster) point;
				if (monster.getCfgId() == monstercfg.getId()) {
					count++;
				}
			}
		}

		YQZZMonsterCreater result = new YQZZMonsterCreater();
		result.parent = parent;
		result.monsterCount = monstercfg.getRefreshCount() - count;
		result.monstercfg = monstercfg;
		return result;
	}

	public void onTick() {
		if (monsterCount > 0) {
			monsterCount--;
			try {
				YQZZMonster monster = YQZZMonster.create(parent.getParent(), monstercfg);
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