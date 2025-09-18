package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.state;

import java.util.List;
import java.util.Objects;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZShotType;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZBuilding;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZNuclearShootAble;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.DYZZEnergyWell;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelog.LogParam;
import com.hawk.log.LogConst;
import org.hawk.os.HawkException;

public class DYZZEnergyWellStateZhanLingZhong extends IDYZZEnergyWellState {
	private String lastGuildid;
	private long zhanlingStart; // 占领开始
	private long zhanlingJieshu;

	public DYZZEnergyWellStateZhanLingZhong(DYZZEnergyWell parent) {
		super(parent);
	}

	@Override
	public void init() {
		this.zhanlingStart = getParent().getParent().getCurTimeMil();
		IDYZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		this.zhanlingJieshu = getParent().controlTimeMil(leaderMarch.getParent()) + zhanlingStart;

		lastGuildid = leaderMarch.getParent().getDYZZGuildId();
	}

	@Override
	public boolean onTick() {
		IDYZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if (leaderMarch == null) {
			lastGuildid = null;
			getParent().setStateObj(new DYZZEnergyWellStateZhongLi(getParent()));
			return true;
		}

		if (!Objects.equals(leaderMarch.getParent().getDYZZGuildId(), lastGuildid)) { // 如果占领者变了
			getParent().setStateObj(new DYZZEnergyWellStateZhanLingZhong(getParent()));
			return true;
		}

		if (getParent().getParent().getCurTimeMil() > zhanlingJieshu) {
			try {
				LogParam logParam = LogUtil.getPersonalLogParam(leaderMarch.getPlayer(), LogConst.LogInfoType.dyzz_occupy);
				if (logParam != null) {
					logParam.put("type", World.WorldPointType.DYZZ_ENERGY_WELL_VALUE);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			nuclearShot();
			// 占领记录
			getParent().incHoldrec();

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.DYZZ_334)
					.addParms(getParent().getX())
					.addParms(getParent().getY())
					.addParms(leaderMarch.getParent().getCamp().intValue())
					.addParms(leaderMarch.getParent().getGuildTag())
					.addParms(leaderMarch.getParent().getName())
					.build();
			getParent().getParent().addWorldBroadcastMsg(parames);
			
			getParent().setStateObj(new DYZZEnergyWellStateProtected(getParent()));
		}

		return true;
	}

	private void nuclearShot() {
		List<IDYZZBuilding> allBuilds = getParent().getParent().getDYZZBuildingList();
		for (IDYZZBuilding build : allBuilds) {
			boolean bfasle = build instanceof IDYZZNuclearShootAble;
			if (!bfasle) {
				continue;
			}
			IDYZZNuclearShootAble shotbuild = (IDYZZNuclearShootAble) build;
			if (shotbuild.getState() != DYZZBuildState.ZHAN_LING || !Objects.equals(shotbuild.getGuildId(), lastGuildid)) {
				continue;
			}
			int atkVal = shotbuild.getWellAtkVal();
			if (getParent().getParent().isHotBloodModel()) {
				atkVal = (int) (atkVal * getParent().getParent().getCfg().getDamage());
			}
			if (atkVal <= 0) {
				continue;
			}
			shotbuild.onNuclearShoot(atkVal, DYZZShotType.EnergyWell);
		}

		// 行军返回
		List<IDYZZWorldMarch> pms = getParent().getParent().getPointMarches(getParent().getPointId());
		for (IDYZZWorldMarch march : pms) {
			if (march.isMassMarch() && march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				march.getMassJoinMarchs(true).forEach(jm -> jm.onMarchCallback());
				march.onMarchBack();
			} else {
				march.onMarchCallback();
			}
		}
	}

	@Override
	public DYZZBuildState getState() {
		return DYZZBuildState.ZHAN_LING_ZHONG;
	}

	@Override
	public void fillBuilder(WorldPointPB.Builder builder) {
		builder.setManorBuildTime(zhanlingStart); // 占领开始时间
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}

	@Override
	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {
		builder.setManorBuildTime(zhanlingStart); // 占领开始时间
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间

	}

}
