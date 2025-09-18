package com.hawk.activity.type.impl.achieve.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hawk.game.protocol.Reward;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.log.Action;

/**
 * <pre>
 * 成就数据提供者
 * 注：请将该提供者注册到AchieveContext中
 * 强制约定：成就数据提供者的achieveId需要保证在所有配置中是唯一的，如 活动的achieveId配置可以使用活动id为achieveId的前缀
 * </pre>
 * @author PhilChen
 *
 */
public interface AchieveProvider {
	
	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 该提供者是否处于激活状态
	 * @return
	 */
	boolean isProviderActive(String playerId);
	
	/**
	 * 该提供者的成就列表是否需要同步
	 * @param playerId
	 * @return
	 */
	boolean isProviderNeedSync(String playerId);

	default boolean isProviderNeedUpdate(String playerId, int achieveId) {
		return true;
	}

	/**
	 * 获取成就项数据
	 * @param playerId
	 * @return
	 */
	Optional<AchieveItems> getAchieveItems(String playerId);

	/**
	 * 获取成就配置
	 * @param achieveId
	 * @return
	 */
	AchieveConfig getAchieveCfg(int achieveId);
	
	/**
	 * 完成成就时回调
	 * @param playerId
	 * @param achieveItem
	 * @return
	 */
	default Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		return Result.success();
	}

	/**
	 * 获取成就奖励时回调
	 * @param playerId
	 * @param achieveId
	 * @return
	 */
	default Result<?> onTakeReward(String playerId, int achieveId) {
		return Result.success();
	}
	
	/**
	 * 成功领取成就奖励时回调
	 * @param playerId
	 * @return
	 */
	default void onTakeRewardSuccess(String playerId) {

	}

	/**
	 * 成功领取成就奖励后时回调
	 * @param playerId
	 * @return
	 */
	default void onTakeRewardSuccessAfter(String playerId, List<Reward.RewardItem.Builder> reweardList, int achieveId) {

	}

	default boolean isNeedPush(String playerId){
		return true;
	}
	
	/**
	 * 获取奖励的action
	 * 
	 * @return
	 */
	Action takeRewardAction();
	
	/**
	 * 获取成就奖励
	 * @param playerId
	 * @param achieveConfig
	 * @return
	 */
	default List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		return achieveConfig.getRewardList();
	}
	
	/**
	 * 获取对应的活动Id
	 * @return
	 */
	int providerActivityId();
	
	/**
	 * 检查成就配置文件变更
	 * <pre>
	 * 检查规则
	 * 1.新增配置id，增加数据
	 * 2.配置id已删除，删除数据
	 * </pre>
	 * @param playerId
	 * @param itemList
	 * @param configMap
	 * @return
	 */
	default boolean checkAchieveConfig(String playerId, List<AchieveItem> itemList, Map<Object, ? extends AchieveConfig> configMap) {
		boolean update = false;
		// 登录时检测是否存在成就项的配置更新
		Map<Integer, AchieveItem> itemMap = new HashMap<Integer, AchieveItem>();
		List<AchieveItem> removeItems = new ArrayList<AchieveItem>();
		for (AchieveItem achieveItem : itemList) {
			itemMap.put(achieveItem.getAchieveId(), achieveItem);
			// 配置id已删除，删除数据
			if (configMap.containsKey(achieveItem.getAchieveId()) == false) {
				removeItems.add(achieveItem);
			}
		}
		for (AchieveItem achieveItem : removeItems) {
			itemList.remove(achieveItem);
			if (logger.isDebugEnabled()) {
				logger.debug("[tavern] remove achieve item. playerId: {},  achieveId: {}", playerId, achieveItem.getAchieveId());
			}
			update = true;
		}
		
		for (AchieveConfig config : configMap.values()) {
			if (config.getAchieveType() == AchieveType.NONE) {
				continue;
			}
			AchieveItem item = itemMap.get(config.getAchieveId());
			// 新增配置id，增加数据
			if (item == null) {
				item = AchieveItem.valueOf(config.getAchieveId());
				itemList.add(item);
				update = true;
				if (logger.isDebugEnabled()) {
					logger.debug("[tavern] add achieve item. playerId: {}, achieveId: {}", playerId, config.getAchieveId());
				}
			}
		}
		return update;
	}

}
