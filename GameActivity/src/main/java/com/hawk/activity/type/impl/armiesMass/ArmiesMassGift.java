package com.hawk.activity.type.impl.armiesMass;

import java.util.List;

import com.hawk.game.protocol.Activity.PBArmiesMassGift;
import com.hawk.game.protocol.Activity.PBArmiesMassGiftType;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 时空之门
 * 
 * @author che
 *
 */
public class ArmiesMassGift implements SplitEntity {
	
	/** 位置*/
	private int type;
	
	/** 品质*/
	private int group;
	
	/** 是否打开*/
	private int level;

	
	
	public ArmiesMassGift() {
		
	}
	
	
	public static ArmiesMassGift valueOf(int type,int group,int level) {
		ArmiesMassGift sculpture = new ArmiesMassGift();
		sculpture.type = type;
		sculpture.group = group;
		sculpture.level = level;
		return sculpture;
	}
	
	
	
	public PBArmiesMassGift.Builder createBuilder(){
		PBArmiesMassGift.Builder builder = PBArmiesMassGift.newBuilder();
		builder.setType(PBArmiesMassGiftType.valueOf(this.type));
		builder.setGroupId(this.group);
		builder.setLevel(this.level);
		return builder;
	}
	
	
	
	


	



	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}


	public int getGroup() {
		return group;
	}


	public void setGroup(int group) {
		this.group = group;
	}


	public int getLevel() {
		return level;
	}


	public void setLevel(int level) {
		this.level = level;
	}


	@Override
	public SplitEntity newInstance() {
		return new ArmiesMassGift();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.type);
		dataList.add(this.group);
		dataList.add(this.level);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		this.type = dataArray.getInt();
		this.group = dataArray.getInt();
		this.level = dataArray.getInt();
	}

	@Override
	public String toString() {
		return "[type=" + type + ",group=" + group + ", level=" + level + "]";
	}
	
	
	
}
