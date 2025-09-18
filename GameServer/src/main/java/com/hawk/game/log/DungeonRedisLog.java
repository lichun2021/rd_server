package com.hawk.game.log;

import java.util.Calendar;

import org.apache.logging.log4j.message.FormattedMessage;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;

import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;

/**
 * 查询 lrange dungeon_log_flow:anyId:month 0 100
 * @author lwt
 * @date 2022年5月19日
 */
public class DungeonRedisLog {
	final static String DYZZ_PLAYER_KEY = "dungeon_log_flow:";

	public static void log(String anyId, String messagePattern, final Object... arguments) {
		try {
			HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();

			FormattedMessage msg = new FormattedMessage(messagePattern, arguments);

			String stackStr = stackTraceStr();

			String logStr = HawkTime.formatNowTime() + " " + GsConfig.getInstance().getServerId() + "," + stackStr + msg.getFormattedMessage();

			int month = HawkTime.getCalendar(false).get(Calendar.MONTH) + 1;
			final String key = DYZZ_PLAYER_KEY + anyId + ":" + month;
			redisSession.lPush(key, 30 * 24 * 60 * 60, logStr);
			HawkLog.logPrintln(msg.getFormattedMessage());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private static String stackTraceStr() {
		try {
			StackTraceElement st = Thread.currentThread().getStackTrace()[3];
			return "[" + Thread.currentThread().getName() + "]" + "[" + st.getFileName().split("\\.")[0] + ":" + st.getMethodName() + ":" + st.getLineNumber() + "]";

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return "";
	}
}
