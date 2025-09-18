package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;

public class YuriRevengeFinishMsgInvoker extends HawkMsgInvoker {
	
	private String guildId;
	
	public YuriRevengeFinishMsgInvoker(String guildId) {
		this.guildId = guildId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		YuriRevengeService.getInstance().onGuildActivityFinish(guildId);
		return true;
	}

	public String getGuildId() {
		return guildId;
	}
}
