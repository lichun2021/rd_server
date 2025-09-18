package com.hawk.game.module.lianmenxhjz.battleroom.entity;

import java.util.List;

import javax.persistence.Transient;

import org.hawk.db.entifytype.EntityType;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;

public class XHJZMarchEntity extends WorldMarch {
	@Transient
	private int armyCount;
	private int xhjzBianduiNum;// = 103; // 星海编队号
	public XHJZMarchEntity() {
		setPersistable(false);
		setEntityType(EntityType.TEMPORARY);
	}

	@Override
	public void setArmys(List<ArmyInfo> armys) {
		this.armyCount = armys.stream().mapToInt(ArmyInfo::getFreeCnt).sum();
		super.setArmys(armys);
	}

	@Override
	public boolean isVisibleOnPos(int x, int y, int viewRadius) {
		return true;
	}

	public WorldMarchPB.Builder toBuilder(WorldMarchPB.Builder builder, WorldMarchRelation relation) {
		throw new UnsupportedOperationException("use march obj instanted");
	}

	@Override
	public IWorldMarch wrapUp() {
		return null;
	}

	public int getArmyCount() {
		return armyCount;
	}

	public void setArmyCount(int armyCount) {
		this.armyCount = armyCount;
	}

	public int getXhjzBianduiNum() {
		return xhjzBianduiNum;
	}

	public void setXhjzBianduiNum(int xhjzBianduiNum) {
		this.xhjzBianduiNum = xhjzBianduiNum;
	}

}
