package com.hawk.ms;

import java.nio.charset.Charset;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

public class MergeServerMain {
	
	/** 
	 * log4j配置文件路径
	 * */
	static final String LOG4J_XML_PATH = "log4j/log4j2.xml";
	
	/**
	 * debug的log4j配置
	 */
	static final String DEBUG_LOG4J_XML_PATH = "log4j/log4j2-debug.xml";
	static final String CONTINUE = "continue="; 
	/**
	 * 入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// 设置log4j配置文件路径
			if(isExistDebugLog4jFile()) {
				HawkOSOperator.setupLog4jCfg(DEBUG_LOG4J_XML_PATH);
			} else {
				HawkOSOperator.setupLog4jCfg(LOG4J_XML_PATH);
			}
			
			boolean isContinue = false;
			// 打印启动参数
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith(CONTINUE)) {
					String value = args[i].substring(CONTINUE.length());
					isContinue = Boolean.valueOf(value);
					
				}
				HawkLog.logPrintln(args[i]);
			}
		    HawkLog.logPrintln("mege serve start");
		    HawkLog.logPrintln("encode = {}", Charset.defaultCharset());
			// 创建应用
			MergeServerApp app = new MergeServerApp();
			boolean executeResult = false;
			if (app.init("cfg/app.cfg", isContinue)) {
				executeResult = app.run();
			}

			// 退出
			HawkLog.logPrintln("merge server exit executeResult :{}", executeResult ? "success" : "fail");
			System.exit(0);

		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("merge server exit by exception");
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
}
