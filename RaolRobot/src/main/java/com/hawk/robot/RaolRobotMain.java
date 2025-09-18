package com.hawk.robot;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

public class RaolRobotMain {
	
	/** 
	 * log4j配置文件路径
	 * */
	static String LOG4J_XML_PATH = "cfg/log4j2.xml";
	
	/**
	 * 主类
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// 设置log4j配置文件路径
		HawkOSOperator.setupLog4jCfg(LOG4J_XML_PATH);
		
		try {
			// 创建应用
			RaolRobotApp app = new RaolRobotApp();
			if (app.init("cfg/robot.xml")) {
				app.run();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
