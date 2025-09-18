package com.hawk.game.world.march.impl;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.OverlordBlessingMarchEvent;
import com.hawk.activity.type.impl.overlordBlessing.cfg.OverlordBlessingKVCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;

/**
 * 霸主膜拜行军
 * 
 * @author lating
 *
 */
public class OverlordBlessingMarch extends PlayerMarch implements BasedMarch {

	public OverlordBlessingMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.OVERLORD_BLESSING_MARCH;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
		// 通知活动处理
		ActivityManager.getInstance().postEvent(new OverlordBlessingMarchEvent(player.getId()));
	}
	
	@Override
	public long getMarchNeedTime() {
		long needTime = super.getMarchNeedTime();
		OverlordBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OverlordBlessingKVCfg.class);
		return Math.min(needTime, cfg.getMaxMarchTime());
	}
}
