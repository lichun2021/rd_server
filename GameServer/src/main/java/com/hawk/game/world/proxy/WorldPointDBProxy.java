package com.hawk.game.world.proxy;

import java.util.List;
import java.util.Map;

import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;

import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;

public class WorldPointDBProxy extends WorldPointProxy {
	/**
	 * 加载所有世界点
	 */
	@Override
	public List<WorldPoint> loadAllPoints(Map<Integer, AreaObject> areas) {
		return HawkDBManager.getInstance().query("from WorldPoint where invalid = 0");
	}
	
	/**
	 * 创建世界点
	 * @param worldPoint
	 * @return
	 */
	 @Override
	public boolean create(WorldPoint worldPoint) {
		return HawkDBManager.getInstance().create(worldPoint);
	}
	
	/**
	 * 更新世界点
	 */
	@Override
	public boolean update(WorldPoint worldPoint) {
		return true;
	}

	/**
	 * 删除世界点
	 */
	@Override
	public void delete(WorldPoint worldPoint) {
		worldPoint.delete(false);
	}
	
	/**
	 * 批量创建世界点
	 * 
	 * @param worldPoints
	 * @return
	 */
	@Override
	public boolean batchCreate(List<WorldPoint> worldPoints) {
		return HawkDBEntity.batchCreate(worldPoints);
	}
	
	/**
	 * 批量删除世界点
	 * 
	 * @param worldPoints
	 * @return
	 */
	@Override
	public boolean batchDelete(List<WorldPoint> worldPoints) {
		return HawkDBEntity.batchDelete(worldPoints);
	}
}
