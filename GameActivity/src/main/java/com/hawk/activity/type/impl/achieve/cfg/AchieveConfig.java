package com.hawk.activity.type.impl.achieve.cfg;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.config.HawkConfigReloadListener;
import org.hawk.config.HawkReloadable;

import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * <pre>
 * 成就配置
 * 强制约定：成就配置数据的achieveId需要保证在所有配置中是唯一的
 * </pre>
 * @author PhilChen
 *
 */
public abstract class AchieveConfig extends HawkConfigBase {
	
	static Map<Class<?>, Set<Integer>> achieveIdMap = new ConcurrentHashMap<>();
	
	static HawkConfigReloadListener reloadListener = new HawkConfigReloadListener() {

		@Override
		public void beforReload(Class<? extends HawkReloadable> clazz) {
			Set<Integer> set = achieveIdMap.get(clazz);
			if (set != null) {
				set.clear();
			}
		}

		@Override
		public void afterReload(Class<? extends HawkReloadable> clazz) {
			
		}

		
	};

	/**
	 * 获取成就id
	 * @return
	 */
	public int getAchieveId() {
		throw new RuntimeException(getClass().getSimpleName() + " invalid call...");
	}
	
	/**
	 * 成就类型
	 * @return
	 */
	public AchieveType getAchieveType(){
		throw new RuntimeException(getClass().getSimpleName() + "invalid call...");
	}

	/**
	 * 成就条件值列表
	 * @return
	 */
	public List<Integer> getConditionValues(){
		throw new RuntimeException(getClass().getSimpleName() + " invalid call...");
	}

	/**
	 * 成就奖励列表
	 * @return
	 */
	public List<RewardItem.Builder> getRewardList(){
		throw new RuntimeException(getClass().getSimpleName() + " invalid call...");
	}
	
	public String getReward(){
		throw new RuntimeException(getClass().getSimpleName() + " invalid call...");
	}
	
	public int getConditionValue(int index) {
		List<Integer> conditionValues = getConditionValues();
		if (conditionValues.size() <= index) {
			return 0;
		}
		return conditionValues.get(index);
	}
	
	/**
	 * 判断是否存在相同的achieveId
	 * @return
	 */
	public boolean existConfigId() {
		int achieveId = getAchieveId();
		Class<? extends AchieveConfig> clazz = getClass();
		for (Entry<Class<?>, Set<Integer>> entry : achieveIdMap.entrySet()) {
			if (entry.getValue().contains(achieveId)) {
				XmlResource annotation1 = entry.getKey().getAnnotation(HawkConfigManager.XmlResource.class);
				String file1;
				String file2;
				if (annotation1 != null) {
					file1 = annotation1.file();
				} else {
					file1 = entry.getKey().getSimpleName();
				}
				XmlResource annotation2 = clazz.getAnnotation(HawkConfigManager.XmlResource.class);
				if (annotation2 != null) {
					file2 = annotation2.file();
				} else {
					file2 = clazz.getSimpleName();
				}
				throw new InvalidParameterException(String.format("repetitive achieveId! file1: %s, file2: %s, achieveId: %d", file1, file2, achieveId));
			}
		}
		Set<Integer> set = achieveIdMap.get(clazz);
		if (set == null) {
			set = new HashSet<>();
			achieveIdMap.put(clazz, set);
			HawkConfigManager.getInstance().registerReloadListener(clazz, reloadListener);
		}
		set.add(achieveId);
		return false;
	}

	@Override
	protected boolean checkValid() {
		if (existConfigId()) {
			return false;
		}
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(getReward());
		if (!valid) {
			throw new InvalidParameterException(String.format("achieve item reward error, achieveId: %d, Class name: %s ", getAchieveId(), getClass().getName()));
		}
		return super.checkValid();
	}
	
}
