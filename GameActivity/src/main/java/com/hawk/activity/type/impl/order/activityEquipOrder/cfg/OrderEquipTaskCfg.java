package com.hawk.activity.type.impl.order.activityEquipOrder.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.task.OrderTaskType;
import com.hawk.serialize.string.SerializeHelper;


/**
 * 战令任务配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/equip_order/equip_order_achieve.xml")
public class OrderEquipTaskCfg extends HawkConfigBase implements IOrderTaskCfg {
	/** 成就id */
	@Id
	private final int id;
	/** 周期数 */
	private final int cycle;
	/** 任务类型 */
	private final int orderType;
	/** 条件值 */
	private final String conditionVal;
	/** 可完成次数 */
	private final int repeatVal;
	/** 每次完成奖励经验 */
	private final int exp;
	
	private final int isClose;

	private OrderTaskType taskType;

	private List<Integer> conditionList;
	
	private int conditionValue;

	private static int continueWeeks = 0;
	
	public OrderEquipTaskCfg() {
		id = 0;
		cycle = 0;
		orderType = 0;
		conditionVal = "";
		repeatVal = 0;
		exp = 0;
		isClose = 0;
	}
	
	public static int getContinueWeeks(){
		return continueWeeks;
	}

	public int getId() {
		return id;
	}

	public int getCycle() {
		return cycle;
	}

	public int getRepeatVal() {
		return repeatVal;
	}

	public int getExp() {
		return exp;
	}

	public OrderTaskType getTaskType() {
		return taskType;
	}

	public List<Integer> getConditionList() {
		return conditionList;
	}

	public int getConditionValue() {
		return conditionValue;
	}
	
	public final boolean isClose() {
		return isClose != 0;
	}

	@Override
	protected boolean assemble() {
		taskType = OrderTaskType.getType(orderType);
		if (taskType == null) {
			HawkLog.errPrintln("taskType type not found! type: {}", orderType);
			return false;
		}
		List<Integer> conditionValueList = SerializeHelper.stringToList(Integer.class, conditionVal, SerializeHelper.ATTRIBUTE_SPLIT);
		conditionList = conditionValueList.subList(0, conditionValueList.size() - 1);
		conditionValue = conditionValueList.get(conditionValueList.size() - 1);
		if(this.cycle > continueWeeks){
			continueWeeks = this.cycle;
		}
		return true;
	}

}
