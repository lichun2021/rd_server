package com.hawk.game.global;

import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.db.HawkDBManager.OpType;
import org.hawk.db.entifytype.EntityType;
import org.hawk.db.serializer.HawkDBChecker;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkDelayTask;
import org.hawk.thread.HawkThreadPool;

import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.cache.PlayerDataSerializer;

public class DBChecker extends HawkDBChecker {
	/**
	 * 数据操作通知
	 */
	public void onOperation(HawkDBEntity entity, int opType) {
		// 操作临时数据对象
		if (entity.getEntityType() == EntityType.TEMPORARY) {
			return;
		}
		
		String owerKey = entity.getOwnerKey();
		if (HawkOSOperator.isEmptyString(owerKey)) {
			return;
		}
		
		PlayerData playerData = GlobalData.getInstance().getPlayerData(owerKey, false);
		if (playerData == null) {
			HawkLog.errPrintln("get player data fail entity:{}, optType:{}", entity.getClass().getName(), opType);
			
			return;
		}
		
		playerData.getDataCache().entityChange(entity);
	}
	
	/**
	 * 检测是否可创建
	 * 
	 * @param entity
	 * @return 返回false就会拦截创建操作, 返回true即继续流程
	 */
	@Override
	public boolean checkCreate(HawkDBEntity entity) {
		CrossService cs = CrossService.getInstance();
		if (cs != null && cs.isCsEntity(entity)) {
			entity.setPersistable(false);
			
			// 操作临时数据对象
			if (entity.getEntityType() != EntityType.TEMPORARY) {
				addNotifyDbDelayTask(entity, HawkDBManager.OpType.INSERT);
			}
			
			return false;
		}
		
		return super.checkCreate(entity);
	}

	/**
	 * 检测是否可删除
	 * 
	 * @param entity
	 * @return 返回false就会拦截删除操作, 返回true即继续流程
	 */
	@Override
	public boolean checkDelete(HawkDBEntity entity) {
		CrossService cs = CrossService.getInstance();
		if (cs != null && cs.isCsEntity(entity)) {
			if (entity.getEntityType() != EntityType.TEMPORARY) {
				addNotifyDbDelayTask(entity, HawkDBManager.OpType.DELETE);
			}
			
			return false;
		}
		
		return super.checkDelete(entity);
	}
	
	/**
	 * 更新检测
	 * 
	 * @param hawkDBEntity
	 * @return 返回false就会拦截更新操作, 返回true即继续流程
	 */
	@Override
	public boolean checkUpdate(HawkDBEntity entity) {
		CrossService cs = CrossService.getInstance();
		if (cs != null && cs.isCsEntity(entity)) {
			if (entity.getEntityType() != EntityType.TEMPORARY) {
				addNotifyDbDelayTask(entity, OpType.UPDATE);
			}
			return false;
		}
		
		return super.checkUpdate(entity);
	}
	
	/**
	 * 通知对象变更
	 * 
	 * @param entity
	 * @param opType
	 */
	private void addNotifyDbDelayTask(HawkDBEntity entity, int opType) {
		HawkThreadPool threadPool = HawkDBManager.getInstance().getThreadPool();
		int index = Math.abs(entity.getOwnerKey().hashCode()) % threadPool.getThreadNum();
		threadPool.addTask(new HawkDelayTask(1000, 1000, 1) {
			@Override
			public Object run() {
				PlayerDataSerializer.csEntityChanged(entity, opType);
				
				return null;
			}
		}, index, false);
	}
}
