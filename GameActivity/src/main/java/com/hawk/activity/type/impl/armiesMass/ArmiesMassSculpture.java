package com.hawk.activity.type.impl.armiesMass;

import java.util.List;

import com.hawk.game.protocol.Activity.PBArmiesMassSculpture;
import com.hawk.game.protocol.Activity.PBSculptureQuality;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 时空之门
 * 
 * @author che
 *
 */
public class ArmiesMassSculpture implements SplitEntity {
	
	/** 位置*/
	private int postion;
	
	/** 品质*/
	private int quality;
	
	/** 是否打开*/
	private int state;

	
	
	public ArmiesMassSculpture() {
		
	}
	
	public static ArmiesMassSculpture valueOf(int quality,int state) {
		ArmiesMassSculpture sculpture = new ArmiesMassSculpture();
		sculpture.quality = quality;
		sculpture.state = state;
		return sculpture;
	}
	
	public static ArmiesMassSculpture valueOf(int postion,int quality,int state) {
		ArmiesMassSculpture sculpture = new ArmiesMassSculpture();
		sculpture.postion = postion;
		sculpture.quality = quality;
		sculpture.state = state;
		return sculpture;
	}
	
	
	
	public PBArmiesMassSculpture.Builder createBuilder(PBSculptureQuality defaultValue){
		PBArmiesMassSculpture.Builder builder = PBArmiesMassSculpture.newBuilder();
		builder.setIndex(this.postion);
		if(this.state == 0){
			builder.setQuality(defaultValue);
		}else{
			builder.setQuality(PBSculptureQuality.valueOf(this.quality));
		}
		builder.setState(this.state);
		return builder;
	}
	
	
	
	public int getPostion() {
		return postion;
	}



	public void setPostion(int postion) {
		this.postion = postion;
	}



	public int getQuality() {
		return quality;
	}



	public void setQuality(int quality) {
		this.quality = quality;
	}



	public int getState() {
		return state;
	}



	public void setState(int state) {
		this.state = state;
	}



	@Override
	public SplitEntity newInstance() {
		return new ArmiesMassSculpture();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.postion);
		dataList.add(this.quality);
		dataList.add(this.state);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		this.postion = dataArray.getInt();
		this.quality = dataArray.getInt();
		this.state = dataArray.getInt();
	}

	@Override
	public String toString() {
		return "[postion=" + postion + "quality=" + quality+ ", state=" + state + "]";
	}
	
	
	
}
