package com.hawk.game.lianmengcyb.worldmarch;

import java.util.Optional;

import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGMassMarch;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGNian;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;

/**
 * 集结
 */
public class CYBORGNianMarchMass extends ICYBORGMassMarch {

	public CYBORGNianMarchMass(ICYBORGPlayer parent) {
		super(parent);
	}

	@Override
	public void heartBeats() {

		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return;
		}

		// 集结等待中
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			waitingStatusMarchProcess();
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
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.NIAN_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.NIAN_MASS_JOIN;
	}

	@Override
	public void onMarchStart() {
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
	}

	@Override
	public void onMarchReach(Player player) {
		Optional<ICYBORGWorldPoint> point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY());
		if (!point.isPresent() || !(point.get() instanceof CYBORGNian)) {
			onMarchReturn(getMarchEntity().getTerminalId(), getParent().getPointId(), this.getArmys());
			// 队员行军返回
			for (ICYBORGWorldMarch tmpMarch : getMassJoinMarchs(true)) {
				tmpMarch.onMarchReturn(getMarchEntity().getTerminalId(), tmpMarch.getParent().getPointId(), tmpMarch.getArmys());
			}
			return;
		}
		point.get().onMarchReach(this);
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	/** 获取被动方联盟战争界面信息 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		int terminalId = this.getMarchEntity().getTerminalId();
		int[] pos = GameUtil.splitXAndY(terminalId);
		builder.setPointType(WorldPointType.NIAN);
		builder.setX(pos[0]);
		builder.setY(pos[1]);

		int targetId = Integer.parseInt(this.getMarchEntity().getTargetId());
		builder.setMonsterId(targetId);
		return builder;
	}

	@Override
	public boolean isMassMarch() {
		return true;
	}

}
