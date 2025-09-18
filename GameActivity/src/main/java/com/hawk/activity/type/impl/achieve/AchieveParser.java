package com.hawk.activity.type.impl.achieve;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.extend.KeyValue;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.AchieveData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.game.protocol.Activity.AchieveState;

/**
 * 成就解析器
 * @author PhilChen
 *
 * @param <T>
 */
public abstract class AchieveParser<T extends ActivityEvent> {

	protected static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 成就类型
	 * @return
	 */
	public abstract AchieveType geAchieveType();

	/**
	 * 开启时初始填充成就数据
	 * @param playerId
	 * @param achieveItem
	 * @param achieveConfig
	 * @return	是否更新数据
	 */
	public abstract boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig);

	/**
	 * 更新成就数据
	 * @param achieveData
	 * @param achieveConfig
	 * @param event
	 * @return
	 */
	protected abstract boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, T event);

	/**
	 * 事件驱动更新成就数据
	 * @param achieveData
	 * @param achieveId
	 * @param event
	 * @return
	 */
	public boolean updateAchieveData(AchieveItem achieveData, AchieveConfig achieveConfig, ActivityEvent event, List<AchieveItem> needPush) {
		if (achieveData.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
			return false;
		}
		T e = event.convert();
		boolean update = updateAchieve(achieveData, achieveConfig, e);
		if (update) {
			logger.debug(" achieve data update, playerId: {}, achieveId: {}, data: {}", event.getPlayerId(), achieveConfig.getAchieveId(), achieveData.getDataList());
			boolean finish = false;
			// 存在数据更新，向客户端推送同步消息
			if (isFinish(achieveData, achieveConfig)) {
				// 成就条件达成
				achieveData.setState(AchieveState.NOT_REWARD_VALUE);
				finish = true;
				logger.info("achieve finish. playerId: {}, achieveId: {}", event.getPlayerId(), achieveConfig.getAchieveId());
			}
			
			needPush.add(achieveData);
			
			if (finish) {
				KeyValue<AchieveConfig, AchieveProvider> configAndProvider = AchieveManager.getInstance().getAchieveConfigAndProvider(achieveConfig.getAchieveId());
				if (configAndProvider != null) {
					configAndProvider.getValue().onAchieveFinished(event.getPlayerId(), achieveData);
				}
			}
		}
		return update;
	}

	/**
	 * 判断成就是否完成
	 * @param achieveItem
	 * @param AchieveConfig
	 * @return
	 */
	public boolean isFinish(AchieveItem achieveItem, AchieveConfig achieveConfig) {
		AchieveData achieveData = geAchieveType().getAchieveData();
		if (achieveData.isGreaterOrEqual(achieveItem, achieveConfig)) {
			return true;
		}
		return false;
	}
}
