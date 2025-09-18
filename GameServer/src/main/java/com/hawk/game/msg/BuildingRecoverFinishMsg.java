package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 超时空急救站冷却恢复结束
 * 
 * @author lating
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class BuildingRecoverFinishMsg extends HawkMsg {

	public BuildingRecoverFinishMsg() {
		super(MsgId.BUILDING_RECOVER_FINISH);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static BuildingRecoverFinishMsg valueOf() {
		BuildingRecoverFinishMsg msg = HawkObjectPool.getInstance().borrowObject(BuildingRecoverFinishMsg.class);
		return msg;
	}
}
