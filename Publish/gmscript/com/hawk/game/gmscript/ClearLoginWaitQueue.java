package com.hawk.game.gmscript;

import java.util.Map;
import java.util.Queue;

import org.hawk.log.HawkLog;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsApp;
import com.hawk.game.data.PfOnlineInfo;
import com.hawk.game.global.GlobalData;

/**
 * 清空登录排队等待session队列
 * @author golden
 *
 */
public class ClearLoginWaitQueue extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		@SuppressWarnings("unchecked")
		Queue<HawkSession> waitSessionQueue = (Queue<HawkSession>) HawkOSOperator.getFieldValue(GsApp.getInstance(), "waitSessionQueue");
		synchronized (waitSessionQueue) {
			@SuppressWarnings("unchecked")
			Map<String, PfOnlineInfo> onlineInfo = (Map<String, PfOnlineInfo>) HawkOSOperator.getFieldValue(GlobalData.getInstance(), "onlineInfo");

			PfOnlineInfo wxOnlineInfo = onlineInfo.get("wx");
			if (wxOnlineInfo != null) {
				wxOnlineInfo.waitLoginSub(wxOnlineInfo.getWaitLoginCount());
			}

			PfOnlineInfo qqOnlineInfo = onlineInfo.get("qq");
			if (qqOnlineInfo != null) {
				qqOnlineInfo.waitLoginSub(qqOnlineInfo.getWaitLoginCount());
			}

			PfOnlineInfo guestOnlineInfo = onlineInfo.get("guest");
			if (guestOnlineInfo != null) {
				guestOnlineInfo.waitLoginSub(guestOnlineInfo.getWaitLoginCount());
			}

			int waitCount = waitSessionQueue.size();
			for (HawkSession session : waitSessionQueue) {
				session.close();
			}
			waitSessionQueue.clear();
			HawkLog.logPrintln("waitSessionQueue clear: {}", waitCount);
		}
		return HawkScript.successResponse(null);
	}
}