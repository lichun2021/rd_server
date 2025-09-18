package com.hawk.activity.type.impl.buff.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/server_buff/activity_buff.xml")
public class ActivityBuffCfg extends HawkConfigBase {
	@Id
	private final  int termId;
	/**
	 * 阶段时间
	 */
	private final String stageTime;
	/**
	 * 阶段时间对应的buff. 一一对应.
	 */
	private final String stageBuff;
	private  List<List<Integer>> stageTimeList;
	private  List<List<Integer>> stageBuffList;
	
	public ActivityBuffCfg() {
		this.termId = 0;
		this.stageTime = "";
		this.stageBuff = "";
	}
	
	public boolean assemble() {
		stageTimeList = new ArrayList<>();
		stageBuffList = new ArrayList<>();
		
		String[] timeArray = stageTime.split("\\|");
		List<Integer> intList = null;
		for (String time : timeArray) {
			String[] startEndTime = time.split("_");
			intList = new ArrayList<>(startEndTime.length);
			intList.add(Integer.parseInt(startEndTime[0]) * 1000);
			intList.add(Integer.parseInt(startEndTime[1]) * 1000);
			
			stageTimeList.add(intList);
		}
		
		String[] bufArray = stageBuff.split("\\|");
		for (String buffStr : bufArray) {
			String[] buffIds = buffStr.split("_");
			intList = new ArrayList<>(buffIds.length);
			for (int i = 0; i < buffIds.length; i++) {
				intList.add(Integer.parseInt(buffIds[i]));
			}
			
			stageBuffList.add(intList);
		}
		
		
		return true;		
	}

	public int getTermId() {
		return termId;
	}

	public List<List<Integer>> getStageTimeList() {
		return stageTimeList;
	}

	public List<List<Integer>> getStageBuffList() {
		return stageBuffList;
	}
}
