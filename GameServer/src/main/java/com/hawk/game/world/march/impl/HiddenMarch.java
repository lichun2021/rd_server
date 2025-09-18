package com.hawk.game.world.march.impl;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;

/**
 * 尤里探索行军
 * @author zhenyu.shang
 * @since 2017年9月18日
 */
public class HiddenMarch extends PlayerMarch implements BasedMarch{

	public HiddenMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.HIDDEN_MARCH;
	}
	
	@Override
	public boolean marchHeartBeats(long time) {
		// 开始探索
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_HIDDEN_VALUE) {
			if(time > getMarchEntity().getResEndTime()){
				//判断探索时间是否已经到了
				WorldMarchService.getInstance().onMarchReturn(this, time, 0);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void onMarchReach(Player player) {
		//行军到达
		this.onMarchStop(WorldMarchStatus.MARCH_STATUS_HIDDEN_VALUE, null, null);
	}
	
	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		WorldMarch march = getMarchEntity();
		
		long time = march.getResStartTime();
		long now = HawkApp.getInstance().getCurrentTime();
		
		// 探索的开始时间和结束时间计算
		march.setResStartTime(now);
		march.setResEndTime(now + time);
		
		WorldMarchService.logger.info("world march stop to explore, marchData: {}", march);
	}

	@Override
	public long getMarchNeedTime() {
		return 0L;
	}
	
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		WorldMarchService.getInstance().onPlayerNoneAction(this, callbackTime);
		try {
			Player player = this.getPlayer();
			LogUtil.logTibetanArmyHole(player, 0, player.getData().getPowerElectric().getArmyBattlePoint(), 0, true);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
