package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.state;

import java.util.List;
import java.util.Objects;

import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZAreaCfg;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.IDYZZTower;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.service.chat.ChatParames;

/**
 * 正在摧毁  
 * @author lwt
 * @date 2022年3月29日
 */
public class DYZZTowerStateCuiHuiZhong extends IDYZZTowerState {
	private long zhanlingStart; // 摧毁开始
	private long zhanlingJieshu;

	public DYZZTowerStateCuiHuiZhong(IDYZZTower parent) {
		super(parent);
	}

	@Override
	public void init() {
		this.zhanlingStart = getParent().getParent().getCurTimeMil();
		IDYZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		this.zhanlingJieshu = getParent().destroyCountDownMil(leaderMarch.getParent()) + zhanlingStart;
	}

	@Override
	public boolean onTick() {
		IDYZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if (Objects.isNull(leaderMarch)) {
			getParent().setStateObj(new DYZZTowerStateZhanLing(getParent()));
			return true;
		}

		if (leaderMarch.getParent().getCamp() == getParent().getBornCamp()) {
			getParent().setStateObj(new DYZZTowerStateZhanLingZhong(getParent()));
			return true;
		}

		if (getParent().getParent().getCurTimeMil() > zhanlingJieshu) {
			getParent().setOnwerCamp(leaderMarch.getParent().getCamp());
			
			getParent().setStateObj(new DYZZTowerStateCuiHui(getParent()));
			// 占领记录
			getParent().incHoldrec();

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

			
			DYZZAreaCfg acfg = DYZZAreaCfg.getPointArea(getParent().getPointId());
			if (Objects.nonNull(acfg)) {
				getParent().getParent().addNobuffArea(acfg.getArea());
			}
			
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.DYZZ_358)
					.addParms(getParent().getX())
					.addParms(getParent().getY())
					.addParms(leaderMarch.getParent().getCamp().intValue())
					.addParms(leaderMarch.getParent().getGuildTag())
					.addParms(leaderMarch.getParent().getName())
					.build();
			getParent().getParent().addWorldBroadcastMsg(parames);
			
			return true;
		}

		return true;
	}

	@Override
	public DYZZBuildState getState() {
		return DYZZBuildState.CUI_HUI;
	}

	@Override
	public String getGuildId() {
		if (getParent().getLeaderMarch() == null) {
			return "";
		}
		return getParent().getLeaderMarch().getParent().getDYZZGuildId();
	}

	@Override
	public String getGuildTag() {
		if (getParent().getLeaderMarch() == null) {
			return "";
		}
		return getParent().getLeaderMarch().getParent().getGuildTag();
	}

	@Override
	public int getGuildFlag() {
		if (getParent().getLeaderMarch() == null) {
			return 0;
		}
		return getParent().getLeaderMarch().getParent().getGuildFlag();
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
