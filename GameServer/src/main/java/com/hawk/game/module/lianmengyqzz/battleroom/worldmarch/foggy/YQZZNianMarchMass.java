package com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.foggy;

import java.util.Optional;

import org.hawk.app.HawkApp;

import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZMassMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZFoggyFortress;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;

/**
 * 集结
 */
public class YQZZNianMarchMass extends IYQZZMassMarch {

	public YQZZNianMarchMass(IYQZZPlayer parent) {
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
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.FOGGY_FORTRESS_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.FOGGY_FORTRESS_MASS_JOIN;
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
		Optional<IYQZZWorldPoint> point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY());
		if (!point.isPresent() || point.get().getPointType() != WorldPointType.FOGGY_FORTRESS) {
			onMarchReturn(getMarchEntity().getTerminalId(), getParent().getPointId(), this.getArmys());
			// 队员行军返回
			for (IYQZZWorldMarch tmpMarch : getMassJoinMarchs(true)) {
				tmpMarch.onMarchReturn(getMarchEntity().getTerminalId(), tmpMarch.getParent().getPointId(), tmpMarch.getArmys());
			}
			return;
		}
		YQZZFoggyFortress foggy = (YQZZFoggyFortress) point.get();
		foggy.onMarchReach(this);
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
		builder.setPointType(WorldPointType.FOGGY_FORTRESS);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setGuildId(getParent().getGuildId());
		int targetId = Integer.parseInt(this.getMarchEntity().getTargetId());
		builder.setMonsterId(targetId);
		return builder;
	}

	@Override
	public boolean isMassMarch() {
		return true;
	}
}
