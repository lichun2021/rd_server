package com.hawk.game.module.dayazhizhan.battleroom;

import java.util.Set;

import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZPointUtil;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 世界点
 * 
 * @author lwt
 * @date 2018年10月30日
 */
public interface IDYZZWorldPoint {

	DYZZBattleRoom getParent();
	int getX();

	int getY();
	int getRedis();
	int getPointId();

	String getGuildId();
	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	WorldPointPB.Builder toBuilder(IDYZZPlayer viewer);

	default void onMarchCome(IDYZZWorldMarch march) {

	}
	
	default void onMarchReach(IDYZZWorldMarch leaderMarch) {
	}
	
//	default void fillWithOcuPointId(Set<Integer> set,boolean lite) {
//		WorldPointType pt = this.getPointType();
//		int k = DYZZPointUtil.pointRedis(pt);
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
		int radius = getRedis();

		DYZZPointUtil.getOcuPointId(getX(), getY(), radius, set);
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
	WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer);

	WorldPointType getPointType();

	boolean onTick();

	boolean needJoinGuild();

	void removeWorldPoint();

}
