package com.hawk.game.player.tick.impl;

import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.prestressingloss.PrestressingLossActivity;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.Activity.ActivityType;

public class PrestressLossTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long prestressLossTick = player.getTickTimeLine().getPrestressLossTick();
		if (currentTime - prestressLossTick > 5000L) {
			Optional<ActivityBase> lossActivity = ActivityManager.getInstance().getActivity(ActivityType.PRESTRESSING_LOSS_VALUE);
			if (lossActivity.isPresent()) {
				try {
					PrestressingLossActivity activity = (PrestressingLossActivity) lossActivity.get();
					activity.onTick(player.getId());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			player.getTickTimeLine().setPrestressLossTick(currentTime);
		}
	}

}
