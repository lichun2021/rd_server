package com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.foggy;

import org.hawk.app.HawkApp;

import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZMassJoinMarch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

public class YQZZNianMarchMassJoin extends IYQZZMassJoinMarch {

	public YQZZNianMarchMassJoin(IYQZZPlayer parent) {
		super(parent);
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return;
		}
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE) {
			return;
		}

		// 当前时间
		long currTime = HawkApp.getInstance().getCurrentTime();
		// 行军或者回程时间未结束
		if (getMarchEntity().getEndTime() > currTime) {
			return;
		}
		// 行军返回到达
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			onMarchBack();
			return;
		}

		// 行军到达
		onMarchReach(getParent());

	}

	@Override
	public void onMarchStart() {
	}

	@Override
	public void onMarchReturn() {
		leaderMarch().ifPresent(march -> march.teamMarchCallBack(this));
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.FOGGY_FORTRESS_MASS_JOIN;
	}

	@Override
	public void onMarchReach(Player parent) {
		super.onMarchReach(parent);
		leaderMarch().ifPresent(march -> march.teamMarchReached(this));
		notifyMarchEvent(MarchEvent.MARCH_DELETE);

	}

	/*** 返回家中 */
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

}
