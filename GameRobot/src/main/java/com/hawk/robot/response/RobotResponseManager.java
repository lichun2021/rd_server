package com.hawk.robot.response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.hawk.robot.annotation.RobotResponse;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.hawk.robot.GameRobotEntity;

public class RobotResponseManager {
	
	public static final String RESP_PACK_PATH = "com.hawk.robot.response";

	private Map<Integer, RobotResponsor> robotResponsorMap = new ConcurrentHashMap<>();
	
	/**
	 * 单例
	 */
	private static RobotResponseManager instance;
	
	/**
	 * 获取实例
	 * @return
	 */
	public static RobotResponseManager getInstance() {
		if(instance == null) {
			instance = new RobotResponseManager();
		}
		return instance;
	}
	
	/**
	 * 私有构造器
	 */
	private RobotResponseManager() {
		
	}
	
	/**
	 * 解析响应处理类
	 */
	public void scanRobotResponsor() {
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(RESP_PACK_PATH, RobotResponse.class);
		for (Class<?> clzz : classList) {
			try {
				RobotResponse annotion = clzz.getAnnotation(RobotResponse.class);
				if (annotion != null) {
					if (annotion.code().length > 0) {
						RobotResponsor responsor = (RobotResponsor) clzz.newInstance();
						for(Integer code : annotion.code()) {
							robotResponsorMap.put(code, responsor);
						}
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 分发响应
	 * @param robotEntity
	 * @param protocol
	 */
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		RobotResponsor responsor = robotResponsorMap.get(protocol.getType());
		if(responsor == null) {
			return;
		}
		
		responsor.response(robotEntity, protocol);
	}
	
}
