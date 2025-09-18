package com.hawk.game.script;

import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.timer.HawkTimerManager;

/**
 * 修改时针偏移
 * @author golden
 * http://localhost:8080/script/changeTimeOffset?offset=
 */
public class ChangeTimeOffsetHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!params.containsKey("offset")) {
			return HawkScript.successResponse("have no param:{offset}");
		} 
		
		String offset = params.get("offset");
		long offsetLong = 0;
		//你可以猜一下.
		if (offset.length() == 14) {
			long expectTime = HawkTime.parseTime(offset, "yyyyMMddHHmmss");
			offsetLong = expectTime - System.currentTimeMillis();
		} else {
			offsetLong = Long.parseLong(offset);
		}
		
		if (offsetLong < 0l) {
			return HawkScript.successResponse("offset time must > 0 ");
		}
		
		HawkTime.setMsOffset(offsetLong);
				
		
		String systemTime = HawkTime.formatTime(System.currentTimeMillis()); 
		String gameTime = HawkTime.formatNowTime();
		HawkLog.logPrintln("change time offset, offset:{}, systemTime:{}, gameTime:{}", offset, systemTime, gameTime);
		
		HawkTimerManager.getInstance().refreshAlarm();
		
		return HawkScript.successResponse("change offset success! systemTime: " + systemTime + ", gameTime: " + gameTime);
	}

}
