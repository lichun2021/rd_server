package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.state;

import java.util.Objects;

import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.IDYZZTower;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;

/**
 * 控制权中  
 * @author lwt
 * @date 2022年3月29日
 */
public class DYZZTowerStateZhanLing extends IDYZZTowerState {
	private IDYZZWorldMarch lastLeaderMarch;
	private long zhanlingJieshu;
	public DYZZTowerStateZhanLing(IDYZZTower parent) {
		super(parent);
	}

	@Override
	public void init() {
		this.zhanlingJieshu = getParent().getParent().getCurTimeMil();
	}
	
	@Override
	public boolean onTick() {
		IDYZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if(lastLeaderMarch != leaderMarch){
			getParent().getParent().worldPointUpdate(getParent());
			lastLeaderMarch = leaderMarch;
		}
		if (Objects.isNull(leaderMarch)) {
			return true;
		}

		if (Objects.equals(leaderMarch.getParent().getCamp(), getParent().getOnwerCamp())) {
			return true;
		}

		// 切换到争夺中
		getParent().setStateObj(new DYZZTowerStateZhanLingZhong(getParent()));

		return true;
	}

	@Override
	public DYZZBuildState getState() {
		return DYZZBuildState.ZHAN_LING;
	}

	@Override
	public String getGuildId() {
		return getParent().getParent().getCampGuild(getParent().getOnwerCamp());
	}

	@Override
	public String getGuildTag() {
		return getParent().getParent().getCampGuildTag(getParent().getOnwerCamp());
	}

	@Override
	public int getGuildFlag() {
		return getParent().getParent().getCampGuildFlag(getParent().getOnwerCamp());
	}
	
	@Override
	public void fillBuilder(WorldPointPB.Builder builder) {
		builder.setLastActiveTime(getParent().getNextShot()); // 下次发射
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}

	@Override
	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {
		builder.setLastActiveTime(getParent().getNextShot()); // 下次发射
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}
}
