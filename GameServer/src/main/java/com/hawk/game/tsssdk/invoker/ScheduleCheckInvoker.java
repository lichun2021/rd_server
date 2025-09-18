package com.hawk.game.tsssdk.invoker;

import org.hawk.app.HawkApp;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.msg.ScheduleCheckBackMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.SCHEDULE_CHECK)
public class ScheduleCheckInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int resultCode, String title, int protocol, String callback) {
		if (resultCode != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		JSONObject json = JSONObject.parseObject(callback);
		String uuid = json.getString("uuid");
		int type = json.getIntValue("type");
		long startTime = json.getLongValue("startTime");
		int continues = json.getIntValue("continues");
		int posX = json.getIntValue("posX");
		int posY = json.getIntValue("posY");
		HawkApp.getInstance().postMsg(player.getXid(), ScheduleCheckBackMsg.valueOf(uuid, type, title, startTime, continues, posX, posY));
		
		LogUtil.logSecTalkFlow(player, null, LogMsgType.SCHEDULE_TITLE, "", title);
		return 0;
	}

}
