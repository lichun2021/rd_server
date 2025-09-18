package com.hawk.robot;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

public class GameRobotMain {
	
	/** 
	 * log4j配置文件路径
	 * */
	static String LOG4J_XML_PATH = "cfg/log4j2.xml";
	
	public static void main(String[] args) {
		// 设置log4j配置文件路径
		HawkOSOperator.setupLog4jCfg(LOG4J_XML_PATH);
		
		try {
			// 创建应用
			GameRobotApp app = new GameRobotApp();
			if (app.init(System.getProperty("user.dir") + "/cfg/config.xml")) {
				app.run();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
