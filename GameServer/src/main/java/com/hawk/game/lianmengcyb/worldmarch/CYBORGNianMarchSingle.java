package com.hawk.game.lianmengcyb.worldmarch;

import java.util.Optional;

import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGNian;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

public class CYBORGNianMarchSingle extends ICYBORGWorldMarch {

	public CYBORGNianMarchSingle(ICYBORGPlayer parent) {
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
		return WorldMarchType.NIAN_SINGLE;
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return;
		}
		// 当前时间
		long currTime = getParent().getParent().getCurTimeMil();
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
		Optional<ICYBORGWorldPoint> point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY());
		if (!point.isPresent() || !(point.get() instanceof CYBORGNian)) {
			this.onMarchCallback();
			return;
		}
		point.get().onMarchReach(this);
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
