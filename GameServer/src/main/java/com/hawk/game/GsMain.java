package com.hawk.game;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.activity.ActivityConfigChecker;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * gameserver入口
 *
 * @author hawk
 *
 */
public class GsMain {
	/** 
	 * 启动服务器id
	 * */
	static final String LAUNCH_SERVER_ID = "serverId=";
	
	/** 
	 * log4j配置文件路径
	 * */
	static final String LOG4J_XML_PATH = "log4j/log4j2.xml";
	
	/**
	 * debug的log4j配置
	 */
	static final String DEBUG_LOG4J_XML_PATH = "log4j/log4j2-debug.xml";
	
	/**
	 * @param args args[0]=serverId
	 */
	public static void main(String[] args) {
		// 设置log4j配置文件路径
		if(isExistDebugLog4jFile()) {
			HawkOSOperator.setupLog4jCfg(DEBUG_LOG4J_XML_PATH);
		} else {
			HawkOSOperator.setupLog4jCfg(LOG4J_XML_PATH);
		}
		
		// 判断是否是检测配置启动模式
		if (isCheckConfigStart(args)) {
			GsApp app = new GsApp();
			ConfigChecker.setDefaultChecker(new ActivityConfigChecker());
			app.checkConfig();
			return;
		}

		// 读取启动参数
		for (int i = 0; i < args.length; i++) {				
			// 打印启动参数
			HawkLog.logPrintln(args[i]);
			
			if (args[i].startsWith(LAUNCH_SERVER_ID)) {
				String serverId = args[i].substring(LAUNCH_SERVER_ID.length());
				GsConfig.setArgServerId(serverId);
			}
		}
		HawkLog.logPrintln("gameserver start");
		
		GsApp app = new GsApp();
		try {
			boolean initOk = app.init("cfg/app.cfg"); 
			app.onInitFinish();
			
			if (initOk) {
				app.run();
			} else {
				app.onFault();
			}
			
			// 退出
			HawkLog.logPrintln("gameserver exit");
			
		} catch (Throwable e) {
			
			app.onInitFinish();
			
			HawkException.catchException(e);
			System.exit(-1);
		}
	}
	
	/**
	 * 是否存在debug的日志配置
	 * 
	 * @return
	 */
	static boolean isExistDebugLog4jFile(){
		String debugFilePath = HawkOSOperator.getWorkPath() + DEBUG_LOG4J_XML_PATH;
		return HawkOSOperator.existFile(debugFilePath);
	}
	
	/**
	 * 是否是检测配置启动模式
	 * 
	 * @param args
	 * @return
	 */
	private static boolean isCheckConfigStart(String args[]) {
		for (int i = 0; i < args.length; i++) {	
			if ("CheckConfig".equals(args[i].trim())) {
				return true;
			}
		}
		return false;
	}
}
