package com.hawk.activity.type.impl.strongestGuild.target;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildEntity;

public interface StrongestGuildTargetParser<T extends StrongestEvent> {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	StrongestTargetType getTargetType();
	
	boolean isOverrideScore();
	
	/***
	 * 事件回调
	 * @param entity
	 * @param circularCfg
	 * @param event
	 * @return 增加的积分
	 */
	long onEvent(StrongestGuildEntity entity, StrongestGuildCfg circularCfg, T event);
	
	boolean march(StrongestEvent event);
	
	/** 检测积分是否超过上限 **/
	default long checkScoreLimit(StrongestGuildCfg circularCfg, long score){
		if(circularCfg.getScoreLimit() > 0){
			if(score > circularCfg.getScoreLimit()){ //超过积分上限，拦截一下
				score = circularCfg.getScoreLimit();
			}
		}
		return score;
	}

	void recordPlayerData(ActivityDataProxy dataGeter, String playerId, StrongestGuildEntity entity, T event);
}
