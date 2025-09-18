package com.hawk.match;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

/**
 * 匹配服务器
 * 
 * @author hawk
 *
 */
public class MatchMain {
	/**
	 * 入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// 打印启动参数
			for (int i = 0; i < args.length; i++) {
				HawkLog.logPrintln(args[i]);
			}

			// 创建应用
			MatchApp app = new MatchApp();
			if (app.init("cfg/app.cfg")) {
				app.run();
			}

			// 退出
			HawkLog.logPrintln("match server exit");
			System.exit(0);

		} catch (Exception e) {
			HawkException.catchException(e);
			System.exit(-1);
		}
	}
}
