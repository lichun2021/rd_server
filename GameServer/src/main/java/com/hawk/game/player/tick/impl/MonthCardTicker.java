package com.hawk.game.player.tick.impl;

import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.monthcard.MonthCardActivity;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.Activity.ActivityType;

public class MonthCardTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long monthCardTickTime = player.getTickTimeLine().getMonthCardTickTime();
		if (monthCardTickTime == 0) {
			monthCardTickTime = currentTime;
			player.getTickTimeLine().setMonthCardTickTime(currentTime);
		}
		//周卡月卡检测
		if (currentTime - monthCardTickTime > HawkTime.MINUTE_MILLI_SECONDS * 2) {
			player.getTickTimeLine().setMonthCardTickTime(currentTime);
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.MONTHCARD_VALUE);
			MonthCardActivity monthCardActivity = (MonthCardActivity) opActivity.get();
			monthCardActivity.onTick(player.getId());
		}
	}

}
