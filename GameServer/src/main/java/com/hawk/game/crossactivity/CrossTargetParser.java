package com.hawk.game.crossactivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.event.speciality.CrossActivityEvent;
import com.hawk.game.config.CrossIntegralCfg;

public interface CrossTargetParser<T extends CrossActivityEvent> {
	static Logger logger = LoggerFactory.getLogger("Server");
	
	CrossTargetType getTargetType();
	
	boolean calcScore(int termId, CrossIntegralCfg circularCfg, T event);
}
