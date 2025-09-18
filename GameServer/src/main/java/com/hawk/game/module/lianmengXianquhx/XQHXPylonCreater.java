package com.hawk.game.module.lianmengXianquhx;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengXianquhx.cfg.XQHXPylonCfg;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXPylon;


/***
 * 负责刷新一波野怪
 * @author lwt
 * @date 2021年11月25日
 */
class XQHXPylonCreater {
	private XQHXPylonRefesh parent;
	private int monsterCount;
	private XQHXPylonCfg monstercfg;

	private XQHXPylonCreater() {
	}

	static XQHXPylonCreater create(XQHXPylonRefesh parent, XQHXPylonCfg monstercfg) {
		int count = 0;
		for (IXQHXWorldPoint point : parent.getParent().getViewPoints()) {
			if (point instanceof XQHXPylon) {
				XQHXPylon monster = (XQHXPylon) point;
				if (monster.getCfgId() == monstercfg.getId()) {
					count++;
				}
			}
		}

		XQHXPylonCreater result = new XQHXPylonCreater();
		result.parent = parent;
		result.monsterCount = monstercfg.getRefreshCount() - count;
		result.monstercfg = monstercfg;
		return result;
	}

	public void onTick() {
		if (monsterCount > 0) {
			monsterCount--;
			try {
				XQHXPylon monster = XQHXPylon.create(parent.getParent(), monstercfg);
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