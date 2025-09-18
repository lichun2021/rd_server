package com.hawk.activity.type.impl.exchangeDecorate.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.db.HawkDBEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.protocol.Activity.ExchangeCommonType;

/**
 * 通用奖励处理
 */
public interface IExchangeCommon {
	static final Logger logger = LoggerFactory.getLogger("Server");
	void exchangeCommon(HawkDBEntity baseEntity,List<Integer> params);
	
	static Map<ExchangeCommonType,IExchangeCommon> commonMap = new ConcurrentHashMap<>();
	
	public static void register(ExchangeCommonType act,IExchangeCommon iExchangeCommon){
		commonMap.put(act, iExchangeCommon);
	}
	
	public static IExchangeCommon getExchangeType(ExchangeCommonType act){
		if(!commonMap.containsKey(act)){
			logger.info("exchange decorate act impl not find = "+act.getNumber());
			return null;
		}
		return commonMap.get(act);
	}
}
