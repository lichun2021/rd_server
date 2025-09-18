package com.hawk.game.player.tick.impl;

import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.mechacoreexplore.CoreExploreActivity;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.Activity.ActivityType;

public class CoreExlopreActivityTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long lastTick = player.getTickTimeLine().getActivity369Tick();
		if (currentTime - lastTick < 5000L) {
			return;
		}
		
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(ActivityType.MECHA_CORE_EXPLORE_VALUE);
		if (activityOp.isPresent()) {
			try {
				CoreExploreActivity activity = (CoreExploreActivity) activityOp.get();
				activity.playerTick(player.getId());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		player.getTickTimeLine().setActivity369Tick(currentTime);
	}

}
