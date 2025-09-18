package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.state;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.IDYZZTower;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;

/**
 * 已摧毁  
 * @author lwt
 * @date 2022年3月29日
 */
public class DYZZTowerStateCuiHui extends IDYZZTowerState {
	private long zhanlingJieshu;
	public DYZZTowerStateCuiHui(IDYZZTower parent) {
		super(parent);
	}

	@Override
	public void init() {
		this.zhanlingJieshu = getParent().getParent().getCurTimeMil();
	}
	
	@Override
	public boolean onTick() {

		return true;
	}

	@Override
	public DYZZBuildState getState() {
		return DYZZBuildState.YI_CUI_HUI;
	}

	@Override
	public String getGuildId() {
		return "";
	}

	@Override
	public String getGuildTag() {
		return "";
	}

	@Override
	public int getGuildFlag() {
		return 0;
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
