package com.hawk.game.module.lianmengXianquhx;

import java.util.Set;

import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.worldmarch.IXQHXWorldMarch;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXPointUtil;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 世界点
 * 
 * @author lwt
 * @date 2018年10月30日
 */
public interface IXQHXWorldPoint {

	XQHXBattleRoom getParent();
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
	WorldPointPB.Builder toBuilder(IXQHXPlayer viewer);

	default void onMarchCome(IXQHXWorldMarch march) {

	}
	
	default void onMarchReach(IXQHXWorldMarch leaderMarch) {
	}
	int getGridCnt();
//	default void fillWithOcuPointId(Set<Integer> set,boolean lite) {
//		WorldPointType pt = this.getPointType();
//		int k = XQHXPointUtil.pointRedis(pt);
//		int xishu = 0;
//		if(lite){
//			xishu = 1;
//		}
//		for (int i = -k + xishu; i <= k; i++) {
//			for (int j = -k; j <= k - xishu; j++) {
//				if (i == k && j == -k) {
//					continue;
//				}
//				int x = this.getX() + i;
//				int y = this.getY() + j;
//				set.add(GameUtil.combineXAndY(x, y));
//			}
//		}
//	}
	
	default void fillWithOcuPointId(Set<Integer> set) {
		XQHXPointUtil.getOcuPointId(getX(), getY(), getGridCnt(), set);
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
	WorldPointDetailPB.Builder toDetailBuilder(IXQHXPlayer viewer);

	WorldPointType getPointType();

	boolean onTick();

	boolean needJoinGuild();

	void removeWorldPoint();
	default int getHashThread(int threadNum) {
		return Math.abs(getPointId() % threadNum);
	}
}
