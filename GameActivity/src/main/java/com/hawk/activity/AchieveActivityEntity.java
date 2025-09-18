package com.hawk.activity;

import java.util.List;
import org.hawk.db.HawkDBEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/***
 * 带成就的活动数据库实体
 * 
 * @author yang.rao
 *
 */
public abstract class AchieveActivityEntity extends HawkDBEntity {
	protected List<AchieveItem> getItemList() {
		throw new RuntimeException("invalid call...");
	};

	protected int getTermId() {
		throw new RuntimeException("invalid call...");
	}
}
