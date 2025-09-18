package com.hawk.activity.type.impl.exchangeDecorate.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

public class ExchangeDecorateLimitInfo  implements SplitEntity {
	//id
	private int levelId;
	
	//开启列表
	private List<ExchangeDecorateInfo> infos = new CopyOnWriteArrayList<ExchangeDecorateInfo>();
	
	public int getLevelId() {
		return levelId;
	}

	public void setLevelId(int levelId) {
		this.levelId = levelId;
	}

	public ExchangeDecorateLimitInfo(){
	}
	
	@Override
	public SplitEntity newInstance() {
		return new ExchangeDecorateLimitInfo();
	}

	public List<ExchangeDecorateInfo> getInfos() {
		return infos;
	}

	public void setInfos(List<ExchangeDecorateInfo> infos) {
		this.infos = infos;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(levelId);
		dataList.add(SerializeHelper.collectionToString(this.infos, SerializeHelper.BETWEEN_ITEMS));
	}

	@Override
	public void fullData(DataArray dataArray) {
		this.levelId = dataArray.getInt();
		this.infos = SerializeHelper.stringToList(ExchangeDecorateInfo.class, dataArray.getString(), SerializeHelper.BETWEEN_ITEMS);
	}

	@Override
	public boolean equals(Object arg0) {
		ExchangeDecorateLimitInfo info = (ExchangeDecorateLimitInfo) arg0;
		if(info.getLevelId() == getLevelId())
			return true;
		return false;
	}

}
