package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;

import org.hawk.log.HawkLog;

import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;

public abstract class IYQZZAchievParser {
	
	/**
	 * 成就任务类型
	 * @return
	 */
	public abstract YQZZAchieveType getYQZZAchieveType();
	
	
	/**
	 * 定时检查推动任务更新
	 * @param achieve
	 * @param conditionValueList
	 * @return
	 */
	public abstract boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue);
	
	
	
	/**
	 * 定时检查推动任务更新
	 * @param achieve
	 * @param conditionValueList
	 * @return
	 */
	public boolean processAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue){
		if(achieve.getState() != YQZZAchieveState.PROGRESS.getValue()){
			return false;
		}
		boolean upate = this.updateAchieveOnUpdate(achieve,conditionValueList,targetValue);
		boolean finish = this.achieveFinish(achieve,conditionValueList,targetValue);
		return upate || finish;
	}
	
	
	
	/**
	 * 是否完成
	 * @param achieve
	 * @param conditionValueList
	 * @return
	 */
	public boolean achieveFinish(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue){
		long curValue = achieve.getValue();
		if(curValue >= targetValue){
			achieve.setValue(targetValue);
			achieve.setState(YQZZAchieveState.FINISH.getValue());
			HawkLog.logPrintln("{} achieve finish,playerId:{},achieveId:{},curValue:{},lastValue:{}",
					this.getClass().getSimpleName(),
					achieve.getParent().getParent().getPlayerId(),
					achieve.getParent().getAchieveId(),curValue,targetValue);
			return true;
		}
		return false;
	}
	
	
}
