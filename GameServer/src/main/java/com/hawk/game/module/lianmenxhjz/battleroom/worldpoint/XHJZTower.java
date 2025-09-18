package com.hawk.game.module.lianmenxhjz.battleroom.worldpoint;

import java.util.List;
import java.util.Objects;

import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.lianmengstarwars.worldpoint.SWBuildState;
import com.hawk.game.lianmengstarwars.worldpoint.SWHeadQuarters;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.chat.ChatParames;

/**
 * 3. 1个功能型（4级）岛屿A（岛上带建筑）
  1. 效果1：控制后，获得控制积分，并且控制期间持续产生积分。丢失控制权时，扣除对应控制积分
  2. 效果2：周期性对5级岛屿造成伤害，每X秒造成一定比例的伤兵。
  3. 效果3：出征时可携带更多燃油。
 *
 */
public class XHJZTower extends IXHJZBuilding {
	private long lastTickTime;
	public XHJZTower(XHJZBattleRoom parent) {
		super(parent);
	}
	@Override
	public boolean onTick() {
		super.onTick();
		long currTime = getParent().getCurTimeMil();
		long tickPeriod = getBuildTypeCfg().getTowerAtkPeriod();
		if (getGuildCamp() != XHJZ_CAMP.NONE && getState() == XHJZBuildState.ZHAN_LING && currTime > (lastTickTime + tickPeriod)) {
			XHJZHeadQuarter headQuarters = (XHJZHeadQuarter) getParent().getWorldPointService().getBuildingByType(XHJZBuildType.QI).get(0);
			if (!Objects.equals(getGuildId(), headQuarters.getGuildId())) {
				List<IXHJZWorldMarch> stayMarches = getParent().getPointMarches(headQuarters.getPointId(),
						WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
				int towerAtk = getBuildTypeCfg().getTowerAtk();
				for (IXHJZWorldMarch headMarch : stayMarches) {
					// 可攻击则进行一次攻击,并记录攻击结果,添加数据
					List<ArmyInfo> armys = headMarch.getMarchEntity().getArmys();
					for (ArmyInfo armyInfo : armys) {
						armyInfo.killByTower(towerAtk);
					}
					headMarch.setLastBuilder(null);
				}
//				System.out.println("****"+getClass().getSimpleName() + "ATTACK ");
			}
			lastTickTime = currTime;
		}
		return true;
	}
}
