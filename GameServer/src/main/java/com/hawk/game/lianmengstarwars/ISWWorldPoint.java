package com.hawk.game.lianmengstarwars;

import java.util.Set;

import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.lianmengstarwars.worldpoint.SWPointUtil;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 世界点
 * 
 * @author lwt
 * @date 2018年10月30日
 */
public interface ISWWorldPoint {

	SWBattleRoom getParent();
	int getX();

	int getY();

	int getPointId();

	String getGuildId();
	
	int getAoiObjId();

	void setAoiObjId(int aoiObjId);
	
	int getWorldPointRadius();
	
	default int getAreaId() {
		return getParent().getWorldPointService().getAreaId(getX(), getY());
	}
	
	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	WorldPointPB.Builder toBuilder(ISWPlayer viewer);

	default void onMarchCome(ISWWorldMarch march) {

	}
	
	default void fillWithOcuPointId(Set<Integer> set) {
		SWPointUtil.getOcuPointId(getX(), getY(), getWorldPointRadius(), set);
	}

	default long getProtectedEndTime() {
		return 0L;
	}

	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	WorldPointDetailPB.Builder toDetailBuilder(ISWPlayer viewer);

	WorldPointType getPointType();

	boolean onTick();

	boolean needJoinGuild();

	void removeWorldPoint();

}
