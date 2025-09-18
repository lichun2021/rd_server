package com.hawk.game.lianmengjunyan;

import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 世界点
 * 
 * @author lwt
 * @date 2018年10月30日
 */
public interface ILMJYWorldPoint {
	
	int getX();
	int getY();
	int getPointId();
	
	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	WorldPointPB.Builder toBuilder(String viewerId);
	
	default void onMarchCome(ILMJYWorldMarch march){
		
	}

	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	WorldPointDetailPB.Builder toDetailBuilder(String viewerId);

	WorldPointType getPointType();
	boolean onTick();
	boolean needJoinGuild();
}
