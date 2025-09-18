package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;

import com.hawk.common.IDIPBanInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.util.GsConst.IDIPBanType;

public class HealthCareTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long careBanStartTime = player.getTickTimeLine().getCareBanStartTime();
		// 成长守护平台封禁
		if (careBanStartTime > 0 && careBanStartTime < currentTime) {
			try {
				IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getOpenId(), IDIPBanType.CARE_BAN_ACCOUNT);
				if (banInfo == null || banInfo.getEndTime() <= currentTime) {
					player.getTickTimeLine().setCareBanStartTime(0);
				} else {
					player.sendIdipNotice(NoticeType.KICKOUT, NoticeMode.MSG_BOX, banInfo.getEndTime(), banInfo.getBanMsg());
					player.kickout(0, false, null);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

}
