package com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.foggy;

import java.util.Optional;

import org.hawk.app.HawkApp;

import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZFoggyFortress;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;

public class YQZZNianMarchSingle extends IYQZZWorldMarch {

	public YQZZNianMarchSingle(IYQZZPlayer parent) {
		super(parent);
	}

	@Override
	public void onMarchStart() {
	}

	@Override
	public boolean needShowInGuildWar() {
		return false;
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.FOGGY_SINGLE;
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
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
	public void onMarchReach(Player player) {
		// 删除行军报告
		Optional<IYQZZWorldPoint> point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY());
		if (!point.isPresent() || point.get().getPointType() != WorldPointType.FOGGY_FORTRESS) {
			this.onMarchCallback();
			return;
		}
		YQZZFoggyFortress foggy = (YQZZFoggyFortress) point.get();
		foggy.onMarchReach(this);
	}

	@Override
	public void onMarchReturn() {
	}

	@Override
	public void onMarchCallback() {
		super.onMarchCallback();
	}

	@Override
	public void onMarchBack() {
		// TODO 加积分

		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

}
