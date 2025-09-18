package com.hawk.game.module.lianmengtaiboliya;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYMonsterCfg;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYMonster;

/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class TBLYMonsterCreater {
	private TBLYMonsterRefesh parent;
	private int monsterCount;
	private TBLYMonsterCfg monstercfg;

	private TBLYMonsterCreater() {
	}

	static TBLYMonsterCreater create(TBLYMonsterRefesh parent, TBLYMonsterCfg monstercfg) {
		TBLYMonsterCreater result = new TBLYMonsterCreater();
		result.parent = parent;
		result.monsterCount = monstercfg.getRefreshCount();
		result.monstercfg = monstercfg;
		return result;
	}

	public void onTick() {
		if (monsterCount > 0) {
			monsterCount--;
			try {
				TBLYMonster monster = TBLYMonster.create(parent.getParent(), monstercfg.getId());
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