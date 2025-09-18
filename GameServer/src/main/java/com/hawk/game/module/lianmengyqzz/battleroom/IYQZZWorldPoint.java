package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.Set;

import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZPointUtil;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 世界点
 * 
 * @author lwt
 * @date 2018年10月30日
 */
public interface IYQZZWorldPoint {

	YQZZBattleRoom getParent();
	int getX();

	int getY();

	int getPointId();

	String getGuildId();
	
	int getAoiObjId();

	void setAoiObjId(int aoiObjId);
	
	int getGridCnt();
	
	default void add2ViewPoint(){}
	
	default int getAreaId() {
		return getParent().getWorldPointService().getAreaId(getX(), getY());
	}

	default void worldPointUpdate() {
		getParent().getWorldPointService().getWorldScene().update(this.getAoiObjId());
	}
	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	WorldPointPB.Builder toBuilder(IYQZZPlayer viewer);
	
	default WorldPointPB.Builder toSecondMapBuilder(){
		throw new UnsupportedOperationException();
	}

	default void onMarchCome(IYQZZWorldMarch march) {

	}
	
	default void fillWithOcuPointId(Set<Integer> set) {
		YQZZPointUtil.getOcuPointId(getX(), getY(), getGridCnt(), set);
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
	WorldPointDetailPB.Builder toDetailBuilder(IYQZZPlayer viewer);

	WorldPointType getPointType();

	boolean onTick();

	boolean needJoinGuild();

	void removeWorldPoint();

	default int getHashThread(int threadNum) {
		return Math.abs(getPointId() % threadNum);
	}

}
