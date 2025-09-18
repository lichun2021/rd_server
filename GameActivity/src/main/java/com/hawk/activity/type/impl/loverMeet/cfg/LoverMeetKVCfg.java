package com.hawk.activity.type.impl.loverMeet.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;

/**
 * 装扮投放系列活动四:硝烟再起
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/lover_meet/lover_meet_cfg.xml")
public class  LoverMeetKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	
	private final String startQuestions;
	
	private final int maxCount;
	
	private final int endingScore;
	
	private List<Integer> startQuestionList;
	
	
	public LoverMeetKVCfg(){
		serverDelay =0;
		maxCount=0;
		endingScore = 100;
		startQuestions= "";
	}
	
	
	
	@Override
	protected boolean assemble() {
		List<Integer> startQuestionListTemp = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.startQuestions)){
			String[] arr = this.startQuestions.split(",");
			for(String str : arr){
				int id = Integer.parseInt(str);
				startQuestionListTemp.add(id);
			}
		}
		this.startQuestionList = ImmutableList.copyOf(startQuestionListTemp);
		return super.assemble();
	}
	
	
	
	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}


	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
	
	public List<Integer> getStartQuestionList() {
		return startQuestionList;
	}
	
	public int getMaxCount() {
		return maxCount;
	}
	
	public int getEndingScore() {
		return endingScore;
	}
}