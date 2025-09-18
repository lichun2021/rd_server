package com.hawk.activity.type.impl.rewardOrder.cfg;

import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.serialize.string.SerializeHelper;

/***
 * 悬赏令任务
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/reward_order/rewardOrderTaskCfg.xml")
public class RewardOrderTaskCfg extends AchieveConfig {
	
	@Id
	private final int id;
	
	/** 成就类型 **/
	private final int conditionType;
	
	/** 数值 **/
	private final String conditionValue;
	
	/** 所属悬赏令 **/
	private final int orderId;
	
	private AchieveType achieveType;
	private List<Integer> conditionValueList;
	
	public RewardOrderTaskCfg(){
		id = 0;
		conditionType = 0;
		conditionValue = "";
		orderId = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			achieveType = AchieveType.getType(conditionType);
			if (achieveType == null) {
				HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
				return false;
			}
			conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	

	public int getAchieveId() {
		return id;
	}

	public int getConditionType() {
		return conditionType;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public int getOrderId() {
		return orderId;
	}
	
	@Override
	public AchieveType getAchieveType() {
		return achieveType;
	}
	
	@Override
	public List<Integer> getConditionValues() {
		return conditionValueList;
	}
	
	public String getReward(){
		return null;
	}
}
