package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;

import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Status.IdipMsgCode;

public class SilentTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long silentTime = player.getEntity().getSilentTime();
		if (silentTime > 0 && silentTime < HawkApp.getInstance().getCurrentTime()) {
			player.getEntity().setSilentTime(0);
			player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, 0, IdipMsgCode.IDIP_BAN_MSG_RELEASE_VALUE);
		}
	}

}
