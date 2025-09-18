package com.hawk.game.module.lianmengXianquhx;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengXianquhx.cfg.XQHXMonsterCfg;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXMonster;

/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class XQHXMonsterCreater {
	private XQHXMonsterRefesh parent;
	private int monsterCount;
	private XQHXMonsterCfg monstercfg;

	private XQHXMonsterCreater() {
	}

	static XQHXMonsterCreater create(XQHXMonsterRefesh parent, XQHXMonsterCfg monstercfg) {
		XQHXMonsterCreater result = new XQHXMonsterCreater();
		result.parent = parent;
		result.monsterCount = monstercfg.getRefreshCount() - parent.getParent().worldMonsterCount();
		result.monstercfg = monstercfg;
		return result;
	} 

	public void onTick() {
		if (monsterCount > 0) {
			monsterCount--;
			try {
				XQHXMonster monster = XQHXMonster.create(parent.getParent(), monstercfg.getId());
				int[] xy = parent.getParent().randomFreePoint(popMonsterPoint(), monster.getGridCnt());
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