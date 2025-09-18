package com.hawk.game.lianmengjunyan.entity;

import org.hawk.db.HawkDBEntity;

import com.hawk.game.entity.ArmyEntity;

public final class LMJYDbEntityCopyUtil {
	@SuppressWarnings("unchecked")
	public static <T> T copyOf(HawkDBEntity entity) {
		HawkDBEntity copyResult = null;
		if (entity instanceof ArmyEntity) {
			ArmyEntity army = (ArmyEntity) entity;
			ArmyEntity result = new ArmyEntity();

			result.setId(army.getId());
			result.setPlayerId(army.getPlayerId());
			result.setArmyId(army.getArmyId());
			result.addFree(army.getFree() + army.getWoundedCount() + army.getCureCount() + army.getMarch() +army.getCureFinishCount());

			copyResult = result;
		}

		if (copyResult != null) {
			/************** 数据不落地 *************/
			/************** 数据不落地 *************/
			/************** 数据不落地 *************/
			copyResult.setPersistable(false);
			/************** 数据不落地 *************/
			/************** 数据不落地 *************/
			/************** 数据不落地 *************/
			return (T) copyResult;
		}
		throw new RuntimeException("未实现的: " + entity.getClass().getSimpleName());
	}
}
