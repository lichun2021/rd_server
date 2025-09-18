package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 建筑队列完成消息
 * 
 * @author lating
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class BuildingQueueFinishMsg extends HawkMsg {
	/**
	 * 建筑Id
	 */
	String itemId;
	/**
	 * 队列状态
	 */
	int status;
	/**
	 * 建筑改建时新建筑的建筑id
	 */
	int newBuildCfgId;
	/**
	 * 队列类型
	 */
	int queueType;
	
	/**
	 * 连续升级
	 */
	int multi;
	
	public int getQueueType() {
		return queueType;
	}

	public void setQueueType(int queueType) {
		this.queueType = queueType;
	}

	public int getNewBuildCfgId() {
		return newBuildCfgId;
	}

	public void setNewBuildCfgId(int newBuildCfgId) {
		this.newBuildCfgId = newBuildCfgId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
	public void setMulti(int multi) {
		this.multi = multi;
	}
	
	
	public int getMulti() {
		return multi;
	}

	public BuildingQueueFinishMsg() {
		super(MsgId.BUILDING_QUEUE_FINISH);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static BuildingQueueFinishMsg valueOf(String itemId, int queueType, int status,int multi) {
		BuildingQueueFinishMsg msg = HawkObjectPool.getInstance().borrowObject(BuildingQueueFinishMsg.class);
		if(itemId.indexOf("_") > 0) {
			String[] items = itemId.split("_");
			msg.itemId = items[0];
			if(QueueStatus.QUEUE_STATUS_REBUILD_VALUE == status) {
				msg.newBuildCfgId = Integer.valueOf(items[1]);
			}
		} else {
			msg.itemId = itemId;
		}
		msg.status = status;
		msg.queueType = queueType;
		msg.multi = multi;
		return msg;
	}
}
