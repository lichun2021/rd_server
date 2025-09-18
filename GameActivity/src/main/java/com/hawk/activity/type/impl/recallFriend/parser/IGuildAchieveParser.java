package com.hawk.activity.type.impl.recallFriend.parser;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.AchieveData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.recallFriend.RecallFriendActivity;
import com.hawk.game.protocol.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * @author hf
 */
public abstract class IGuildAchieveParser<T extends ActivityEvent> {

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
	 * @param event
	 * @return
	 */
	public boolean updateAchieveData(AchieveItem achieveData, AchieveConfig achieveConfig, ActivityEvent event, List<AchieveItem> needPush) {
		T e = event.convert();
		boolean update = updateAchieve(achieveData, achieveConfig, e);
		if (update) {
			needPush.add(achieveData);
		}
		if (achieveData.getState() != Activity.AchieveState.NOT_ACHIEVE_VALUE) {
			return false;
		}

		if (update) {
			logger.debug("IGuildAchieveParser  achieve data update, playerId: {}, achieveId: {}, data: {}", event.getPlayerId(), achieveConfig.getAchieveId(), achieveData.getDataList());
			boolean finish = false;
			// 存在数据更新，向客户端推送同步消息
			if (isFinish(achieveData, achieveConfig)) {
				// 成就条件达成
				achieveData.setState(Activity.AchieveState.NOT_REWARD_VALUE);
				finish = true;
				logger.info("IGuildAchieveParser achieve finish. playerId: {}, achieveId: {}", event.getPlayerId(), achieveConfig.getAchieveId());
			}

			if (finish) {
				Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.RECALL_FRIEND_VALUE);
				if (optional.isPresent()){
					RecallFriendActivity recallFriendActivity = (RecallFriendActivity) optional.get();
					recallFriendActivity.onGuildAchieveFinished(event.getPlayerId(), achieveData);
				}
			}
		}
		return update;
	}

	/**
	 * 判断成就是否完成
	 * @param achieveItem
	 * @return
	 */
	protected boolean isFinish(AchieveItem achieveItem, AchieveConfig achieveConfig) {
		AchieveData achieveData = geAchieveType().getAchieveData();
		if (achieveData.isGreaterOrEqual(achieveItem, achieveConfig)) {
			return true;
		}
		return false;
	}

}
