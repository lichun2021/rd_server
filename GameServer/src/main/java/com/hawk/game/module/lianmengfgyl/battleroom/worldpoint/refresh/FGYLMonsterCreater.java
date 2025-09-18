package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.refresh;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLMonsterCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLMonster;


/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class FGYLMonsterCreater {
	private FGYLMonsterRefesh parent;
	private int monsterCount;
	private FGYLMonsterCfg monstercfg;

	private FGYLMonsterCreater() {
	}

	static FGYLMonsterCreater create(FGYLMonsterRefesh parent, FGYLMonsterCfg monstercfg) {
		FGYLMonsterCreater result = new FGYLMonsterCreater();
		result.parent = parent;
		result.monsterCount = monstercfg.getRefreshCount() - parent.getParent().getLastSyncpb().getMonsterCount();
		result.monstercfg = monstercfg;
		return result;
	}

	public void onTick() {
		if (monsterCount > 0) {
			monsterCount--;
			try {
				FGYLMonster monster = FGYLMonster.create(parent.getParent(), monstercfg.getId());
				int[] xy = parent.getParent().getWorldPointService().randomFreePoint(popMonsterPoint(), monster.getGridCnt());
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

	public int[] popMonsterPoint() {
		return monstercfg.randomBoinPoint();
	}
}