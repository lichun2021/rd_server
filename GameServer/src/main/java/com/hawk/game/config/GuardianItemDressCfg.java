package com.hawk.game.config;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.serialize.string.SerializeHelper;



@HawkConfigManager.XmlResource(file = "xml/guardian_item_dress.xml")
public class GuardianItemDressCfg extends HawkConfigBase {
	@Id
	private final int itemId;
	/**
	 * 有效时间.
	 */
	private final int validTime;
	/**
	 * 可供选择的穿戴ID
	 */
	private final String dressIds;
	/**
	 * 外观列表list
	 */
	private List<Integer> dressIdList = new ArrayList<>();
	
	public GuardianItemDressCfg() {
		this.itemId = 0;
		this.validTime = 0;
		this.dressIds = "";
	}
	public int getItemId() {
		return itemId;
	}	
	public String getDressIds() {
		return dressIds;
	}
	
	public List<Integer> getDressIdList() {
		return dressIdList;
	}
	
	@Override
	public boolean assemble() {
		if (HawkOSOperator.isEmptyString(dressIds)) {
			this.dressIdList = Collections.synchronizedList(new ArrayList<>()); 
		} else {
			this.dressIdList = Collections.synchronizedList(SerializeHelper.cfgStr2List(dressIds));
		}
		
		return true;
	}
	
	public int getValidTime() {
		return validTime;
	}
	
	public int dressIdIndex(int dressId) {
		return dressIdList.indexOf(dressId);
	}
	
	/**
	 * 获取单人.
	 * @return
	 */
	public Integer getSingleDressId() {
		return dressIdList.get(0);
	}
}
