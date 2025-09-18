package com.hawk.game.yuriStrikes;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkTime;

import com.hawk.game.config.YuristrikeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

@YuriStrikeState(pbState = YuriState.LOCK)
public class YuriStrikeStateLock implements IYuriStrikeState {

	private long nextTick;

	@Override
	public void tick(Player player, YuriStrike obj) {
		long now = HawkTime.getMillisecond();
		if (now < nextTick) {
			return;
		}
		YuristrikeCfg cfg = obj.getCfg();
		if (cfg.getTriggerCityLevel() > player.getCityLevel()) {
			return;
		}
		if (cfg.getTriggerPower() > player.getPower()) {
			return;
		}
		if (cfg.getTriggerPlayerLevel() > player.getLevel()) {
			return;
		}
		if (!StoryMissionService.getInstance().isChapterComplete(player, cfg.getTriggerPlot())) {
			return;
		}
		if (cfg.getTriggerMilitaryRank() > player.getMilitaryRankLevel()) {
			return;
		}

		IWorldMarch march = WorldMarchService.getInstance().startYuriStrikeMarch(player, obj.getCfg());
		if (Objects.isNull(march)) {
			nextTick = now + TimeUnit.MINUTES.toMillis(1);
			return;
		}
		obj.getDbEntity().setMarchId(march.getMarchId());
		obj.setState(player, IYuriStrikeState.valueOf(YuriState.MARCH));
	}

}
