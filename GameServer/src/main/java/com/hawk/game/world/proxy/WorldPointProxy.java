package com.hawk.game.world.proxy;

import java.util.List;
import java.util.Map;

import com.hawk.game.GsConfig;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;

public abstract class WorldPointProxy {
	/**
	 * 世界点代理模式
	 */
	private static WorldPointProxy instance = null;

	/**
	 * 获取世界点代码模式实例
	 * 
	 * @return
	 */
	public static WorldPointProxy getInstance() {
		if (instance == null) {
			if (GsConfig.getInstance().getWorldPointProxy() == 0) {
				instance = new WorldPointDBProxy();
			} else {
				instance = new WorldPointRedisProxy();
			}
		}
		return instance;
	}

	/**
	 * 加载所有世界点
	 * 
	 * @return
	 */
	public abstract List<WorldPoint> loadAllPoints(Map<Integer, AreaObject> areas);

	/**
	 * 创建世界点
	 * 
	 * @param worldPoint
	 * @return
	 */
	public abstract boolean create(WorldPoint worldPoint);

	/**
	 * 更新世界点
	 * 
	 * @param worldPoint
	 * @return
	 */
	public abstract boolean update(WorldPoint worldPoint);

	/**
	 * 删除世界点
	 * 
	 * @param worldPoint
	 * @return
	 */
	public abstract void delete(WorldPoint worldPoint);

	/**
	 * 批量创建世界点
	 * 
	 * @param worldPoints
	 * @return
	 */
	public abstract boolean batchCreate(List<WorldPoint> worldPoints);

	/**
	 * 批量删除世界点
	 * 
	 * @param worldPoints
	 * @return
	 */
	public abstract boolean batchDelete(List<WorldPoint> worldPoints);
	
	/**
	 * 代理初始化
	 * 
	 * @return
	 */
	public boolean init() {
		return true;
	}
	
	/**
	 * flush出去代理数据
	 */
	public void flush() {
		
	}
}
