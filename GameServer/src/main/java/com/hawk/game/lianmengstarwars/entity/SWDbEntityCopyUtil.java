package com.hawk.game.lianmengstarwars.entity;

import org.hawk.db.HawkDBEntity;
import org.hawk.db.entifytype.EntityType;

import com.hawk.game.entity.ArmyEntity;

public final class SWDbEntityCopyUtil {
	@SuppressWarnings("unchecked")
	public static <T> T copyOf(HawkDBEntity entity) {
		HawkDBEntity copyResult = null;
		if (entity instanceof ArmyEntity) {
			ArmyEntity army = (ArmyEntity) entity;
			SWArmyEntity result = new SWArmyEntity();

			result.setId(army.getId());
			result.setPlayerId(army.getPlayerId());
			result.setArmyId(army.getArmyId());
			result.addFree(army.getFree() + army.getWoundedCount() + army.getCureCount());
			result.setMaxFree(result.getFree());
			copyResult = result;
		}

		if (copyResult != null) {
			/************** 数据不落地 *************/
			/************** 数据不落地 *************/
			/************** 数据不落地 *************/
			copyResult.setPersistable(false);
			copyResult.setEntityType(EntityType.TEMPORARY);
			/************** 数据不落地 *************/
			/************** 数据不落地 *************/
			/************** 数据不落地 *************/
			return (T) copyResult;
		}
		throw new RuntimeException("未实现的: " + entity.getClass().getSimpleName());
	}
}
