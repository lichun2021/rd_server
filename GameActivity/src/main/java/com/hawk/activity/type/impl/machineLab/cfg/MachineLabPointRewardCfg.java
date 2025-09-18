package com.hawk.activity.type.impl.machineLab.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;

@HawkConfigManager.XmlResource(file = "activity/machine_lab/machine_lab_points_get.xml")
public class MachineLabPointRewardCfg extends HawkConfigBase {
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	
	private final String conditionValue;
	
	private final int dropProb;
	
	private final int dropNum;
	
	private final int dropLimit;
	
	private List<Integer> conditionList;

	public MachineLabPointRewardCfg() {
		id = 0;
		conditionValue = "";
		dropProb = 0;
		dropNum = 0;
		dropLimit = 0;
	}

	public int getId() {
		return id;
	}
	
	public int getDropProb() {
		return dropProb;
	}
	
	public int getDropNum() {
		return dropNum;
	}
	
	public int getDropLimit() {
		return dropLimit;
	}
	
	public List<Integer> getConditionList() {
		return conditionList;
	}
	
	@Override
	protected boolean assemble() {
		List<Integer> conditionTemp = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.conditionValue)){
			String[] arr = this.conditionValue.split("_");
			for(String str : arr){
				conditionTemp.add(Integer.parseInt(str));
			}
		}
		this.conditionList = ImmutableList.copyOf(conditionTemp);
		return super.assemble();
	}
	
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}

}
