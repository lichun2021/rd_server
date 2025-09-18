package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZNuclearShootAble;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.state.IDYZZTowerState;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;

/**
 *
 */
public abstract class IDYZZTower extends IDYZZNuclearShootAble {

	/**出生归属*/
	private DYZZCAMP bornCamp;
	private DYZZCAMP onwerCamp; // 当前归属联盟id
	private IDYZZTowerState stateObj;

	public IDYZZTower(DYZZBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		onShootTick();

		stateObj.onTick();
		return true;
	}
	
	public int destroyCountDownMil(IDYZZPlayer leader){
		return 0; 
	}

	@Override
	public DYZZBuildState getState() {
		return stateObj.getState();
	}

	@Override
	public String getGuildId() {
		return stateObj.getGuildId();
	}

	@Override
	public String getGuildTag() {
		return stateObj.getGuildTag();
	}

	@Override
	public int getGuildFlag() {
		return stateObj.getGuildFlag();
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.DYZZ_TOWER;
	}

	public DYZZCAMP getBornCamp() {
		return bornCamp;
	}

	public void setBornCamp(DYZZCAMP bornCamp) {
		this.bornCamp = bornCamp;
	}

	public DYZZCAMP getOnwerCamp() {
		return onwerCamp;
	}

	public void setOnwerCamp(DYZZCAMP onwerCamp) {
		this.onwerCamp = onwerCamp;
	}

	public IDYZZTowerState getStateObj() {
		return stateObj;
	}

	public void setStateObj(IDYZZTowerState stateObj) {
		this.stateObj = stateObj;
		this.stateObj.init();
		getParent().worldPointUpdate(this);
	}

	public abstract int getEffVal(EffType effType);

	@Override
	public WorldPointPB.Builder toBuilder(IDYZZPlayer viewer) {
		WorldPointPB.Builder builder = super.toBuilder(viewer);
		this.stateObj.fillBuilder(builder);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewer);
		this.stateObj.fillDetailBuilder(builder);
		return builder;
	}

}
