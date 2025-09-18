package com.hawk.activity.type.impl.loverMeet.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;

@HawkConfigManager.XmlResource(file = "activity/lover_meet/lover_meet_question.xml")
public class LoverMeetQuestionCfg extends HawkConfigBase{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	
	/**
	 * 数值
	 */
	private final String answer;

	
	private Map<Integer,List<Integer>> answerMap;

	public LoverMeetQuestionCfg() {
		id = 0;
		answer = "";
	}

	
	@Override
	protected boolean assemble() {
		Map<Integer,List<Integer>> answerMapTemp = new HashMap<>();
		if(!HawkOSOperator.isEmptyString(this.answer)){
			String[] arr = this.answer.split(",");
			for(String str : arr){
				List<Integer> conList = new ArrayList<>();
				String[] conArr = str.split("_");
				for(int i=0;i<conArr.length;i++){
					if(i==0){
						int ending = Integer.parseInt(conArr[i]);
						answerMapTemp.put(ending,conList);
					}else{
						conList.add(Integer.parseInt(conArr[i]));
					}
				}
			}
		}
		this.answerMap = ImmutableMap.copyOf(answerMapTemp);
		return super.assemble();
	}
	

	public int getId() {
		return id;
	}
	
	
	
	
	public List<Integer> getAnswerList(Collection<Integer> endings) {
		List<Integer> answers = new ArrayList<>();
		for(Entry<Integer,List<Integer>> entry : this.answerMap.entrySet()){
			int ending = entry.getKey();
			List<Integer> conditions = entry.getValue();
			if(conditions.isEmpty()){
				answers.add(ending);
				continue;
			}
			for(int conEnding : conditions){
				if(!endings.contains(conEnding)){
					answers.add(ending);
					break;
				}
			}
		}
		return answers;
	}
	
	
	
	
	
	
}
