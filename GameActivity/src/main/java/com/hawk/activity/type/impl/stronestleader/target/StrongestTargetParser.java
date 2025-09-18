package com.hawk.activity.type.impl.stronestleader.target;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularCfg;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;

public interface StrongestTargetParser<T extends StrongestEvent> {
	static Logger logger = LoggerFactory.getLogger("Server");
	
	StrongestTargetType getTargetType();
	
	boolean isOverrideScore();
	
	boolean onEvent(ActivityStrongestLeaderEntity entity, ActivityCircularCfg circularCfg, T event);
	
	/** 检测积分是否超过上限 **/
	default long checkScoreLimit(ActivityCircularCfg circularCfg, long score){
		if(circularCfg.getScoreLimit() > 0){
			if(score > circularCfg.getScoreLimit()){ //超过积分上限，拦截一下
				score = circularCfg.getScoreLimit();
			}
		}
		return score;
	}

	void recordPlayerData(ActivityDataProxy dataGeter, String playerId, ActivityStrongestLeaderEntity entity, T event);
}
