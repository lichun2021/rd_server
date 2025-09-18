package com.hawk.game.lianmengcyb;

import java.util.Set;

import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGPointUtil;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 世界点
 * 
 * @author lwt
 * @date 2018年10月30日
 */
public interface ICYBORGWorldPoint {

	CYBORGBattleRoom getParent();
	int getX();

	int getY();

	int getPointId();

	String getGuildId();
	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	WorldPointPB.Builder toBuilder(ICYBORGPlayer viewer);

	default void onMarchCome(ICYBORGWorldMarch march) {

	}
	
	default void onMarchReach(ICYBORGWorldMarch leaderMarch) {
	}
	
	default void fillWithOcuPointId(Set<Integer> set) {
		WorldPointType pt = this.getPointType();
		int radius = CYBORGPointUtil.pointRedis(pt);

		CYBORGPointUtil.getOcuPointId(getX(), getY(), radius, set);
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
	WorldPointDetailPB.Builder toDetailBuilder(ICYBORGPlayer viewer);

	WorldPointType getPointType();

	boolean onTick();

	boolean needJoinGuild();

	void removeWorldPoint();
	default int getHashThread(int threadNum) {
		return Math.abs(getPointId() % threadNum);
	}
}
