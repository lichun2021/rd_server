package com.hawk.game.module.mechacore.entity;

import java.util.List;

import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.protocol.MechaCore.MechaCoreSlotPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 机甲核心槽位信息
 * @author lating
 *
 */
public class MechaCoreSlotObject implements SplitEntity {
	
	private int slotType;
	
	private int slotId;
	
	private int unlocked;
	
	private int level;
	
	private String moduleUuid = ""; //当前work的套装下装配的模块
	
	public MechaCoreSlotObject() {
	}
	
	public MechaCoreSlotObject(int slotType) {
		this.slotType = slotType;
	}
	
	public static MechaCoreSlotObject valueOf(int type) {
		return new MechaCoreSlotObject(type);
	}
	
	@Override
	public SplitEntity newInstance() {
		return new MechaCoreSlotObject();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(slotType);
		dataList.add(slotId);
		dataList.add(unlocked);
		dataList.add(level);
		dataList.add(moduleUuid);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		slotType = dataArray.getInt();
		slotId = dataArray.getInt();
		unlocked = dataArray.getInt();
		level = dataArray.getInt();
		moduleUuid = dataArray.getString();
	}
	
	@Override
	public String toString() {
		return slotType + "_" + slotId + "_" + unlocked + "_" + level + "_" + moduleUuid;
	}
	
	public int getSlotType() {
		return slotType;
	}

	public void setSlotType(int slotType) {
		this.slotType = slotType;
	}

	public int getSlotId() {
		return slotId;
	}

	public void setSlotId(int slotId) {
		this.slotId = slotId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getModuleUuid() {
		return moduleUuid;
	}

	public void setModuleUuid(String moduleUuid) {
		this.moduleUuid = moduleUuid;
	}

	public int getUnlocked() {
		return unlocked;
	}

	public void setUnlocked(int unlocked) {
		this.unlocked = unlocked;
	}
	
	public MechaCoreSlotPB.Builder toBuilder(PlayerMechaCore mechacore) {
		return toBuilder(mechacore, this.getModuleUuid());
	}
	
	public MechaCoreSlotPB.Builder toBuilder(PlayerMechaCore mechacore, String moduleUuid) {
		MechaCoreSlotPB.Builder slotBuilder = MechaCoreSlotPB.newBuilder();
		slotBuilder.setSlotType(this.getSlotType());
		slotBuilder.setUnlocked(mechacore.slotAutoUnlock(this));
		slotBuilder.setSlotId(this.getSlotId());
		slotBuilder.setLevel(this.getLevel());
		MechaCoreModuleEntity moduleEntity = mechacore.getParent().getMechaCoreModuleEntity(moduleUuid);
		if (moduleEntity != null) {
			slotBuilder.setModule(moduleEntity.toBuilder());
		}
		return slotBuilder;
	}
	
}
