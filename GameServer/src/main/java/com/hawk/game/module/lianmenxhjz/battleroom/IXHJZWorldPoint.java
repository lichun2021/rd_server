package com.hawk.game.module.lianmenxhjz.battleroom;

import java.util.Set;

import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZPointUtil;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 世界点
 * 
 * @author lwt
 * @date 2018年10月30日
 */
public interface IXHJZWorldPoint {

	XHJZBattleRoom getParent();
	int getX();

	int getY();

	int getPointId();

	String getGuildId();
	
	int getAoiObjId();

	void setAoiObjId(int aoiObjId);
	
	int getGridCnt();
	
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
	WorldPointPB.Builder toBuilder(IXHJZPlayer viewer);
	
	default void worldPointUpdate() {
		getParent().getWorldPointService().getWorldScene().update(this.getAoiObjId());
	}
	
	default void onMarchCome(IXHJZWorldMarch march) {

	}
	
	default void onMarchReach(IXHJZWorldMarch leaderMarch) {
	}
	
//	default void fillWithOcuPointId(Set<Integer> set,boolean lite) {
//		WorldPointType pt = this.getPointType();
//		int k = XHJZPointUtil.pointRedis(pt);
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
		WorldPointType pt = this.getPointType();
		int radius = XHJZPointUtil.pointRedis(pt);

		XHJZPointUtil.getOcuPointId(getX(), getY(), radius, set);
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
	WorldPointDetailPB.Builder toDetailBuilder(IXHJZPlayer viewer);

	WorldPointType getPointType();

	boolean onTick();

	boolean needJoinGuild();

	void removeWorldPoint();

}
