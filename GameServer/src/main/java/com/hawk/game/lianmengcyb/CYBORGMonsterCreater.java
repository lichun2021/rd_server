package com.hawk.game.lianmengcyb;

import org.hawk.os.HawkException;

import com.hawk.game.config.CYBORGMonsterCfg;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGMonster;

/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class CYBORGMonsterCreater {
	private CYBORGMonsterRefesh parent;
	private int monsterCount;
	private CYBORGMonsterCfg monstercfg;

	private CYBORGMonsterCreater() {
	}

	static CYBORGMonsterCreater create(CYBORGMonsterRefesh parent, CYBORGMonsterCfg monstercfg) {
		CYBORGMonsterCreater result = new CYBORGMonsterCreater();
		result.parent = parent;
		result.monsterCount = monstercfg.getRefreshCount();
		result.monstercfg = monstercfg;
		return result;
	}

	public void onTick() {
		if (monsterCount > 0) {
			monsterCount--;
			try {
				CYBORGMonster monster = CYBORGMonster.create(parent.getParent(), monstercfg.getId());
				int[] xy = parent.getParent().randomFreePoint(popMonsterPoint(), monster.getPointType());
				if (xy != null) {
					monster.setX(xy[0]);
					monster.setY(xy[1]);
					parent.getParent().addViewPoint(monster);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	public int[] popMonsterPoint() {
		return monstercfg.randomBoinPoint();
	}
}