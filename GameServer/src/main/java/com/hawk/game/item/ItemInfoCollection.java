package com.hawk.game.item;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.google.common.base.Joiner;
import com.hawk.game.util.GsConst;

/**
 * 物品自定义结构集合
 * @author Golden
 *
 */
public class ItemInfoCollection {

	private List<ItemInfo> itemInfoList = new ArrayList<>();
	
	public ItemInfoCollection() {
		
	}
	
	/**
	 * 构造
	 * @param info
	 */
	public ItemInfoCollection(String info) {
		if(HawkOSOperator.isEmptyString(info)){
			return;
		}
		
		String[] itemArray = info.split(",");
		for (String item : itemArray) {
			ItemInfo itemInfo = ItemInfo.valueOf(item);
			if (itemInfo != null) {
				itemInfoList.add(itemInfo);
			}
		}
	}
	
	/**
	 * 获取ItemInfoCollection对象
	 * @param info
	 * @return
	 */
	public static ItemInfoCollection valueOf(String info) {
		ItemInfoCollection itemCollection = new ItemInfoCollection(info);
		return itemCollection;
	}
	
	/**
	 * 添加物品
	 * @param itemInfo
	 */
	public void add(String itemInfoStr) {
		List<ItemInfo> itemInfos = valueListOf(itemInfoStr);
		add(itemInfos);
	}
	
	/**
	 * 添加物品
	 * @param itemInfo
	 */
	public void add(List<ItemInfo> itemInfos) {
		for (ItemInfo itemInfo : itemInfos) {
			add(itemInfo);
		}
	}
	
	/**
	 * 添加物品
	 * @param itemInfo
	 */
	public void add(ItemInfo addItemInfo) {
		for (ItemInfo itemInfo : itemInfoList) {
			if (addItemInfo.getItemType() == itemInfo.getItemType() && addItemInfo.getItemId() == itemInfo.getItemId()) {
				itemInfo.setCount(itemInfo.getCount() + addItemInfo.getCount());
				return;
			}
		}
		itemInfoList.add(addItemInfo.clone());
	}
	
	/**
	 * 转化String类型为List
	 * @param info
	 * @return
	 */
	private static List<ItemInfo> valueListOf(String info) {
		if(HawkOSOperator.isEmptyString(info)){
			return new ArrayList<>();
		}
		List<ItemInfo> itemList = new ArrayList<>();
		String[] itemArray = info.split(",");
		for (String item : itemArray) {
			ItemInfo itemInfo = ItemInfo.valueOf(item);
			if (itemInfo != null) {
				itemList.add(itemInfo);
			}
		}
		return itemList;
	}

	/**
	 * 获取itemInfos克隆对象
	 * @return
	 */
	public List<ItemInfo> getItemInfosClon() {
		return new ArrayList<>(itemInfoList);
	}
	
	/**
	 * 以比率获取itemInfos克隆对象
	 * @param rate 万分比
	 * @return
	 */
	public List<ItemInfo> getItemInfosClon(int rate) {
		List<ItemInfo> itemInfos = new ArrayList<>(itemInfoList);
		for (ItemInfo itemInfo : itemInfos) {
			long beforeCount = itemInfo.getCount();
			// 用此方法,请注意越界和取整
			long afterCount = (long) (beforeCount * rate * GsConst.EFF_PER);
			itemInfo.setCount(afterCount);
		}
		return itemInfos;
	}
	
	/**
	 * 是否是空的
	 * @return
	 */
	public boolean isEmpty() {
		return itemInfoList.isEmpty();
	}
	
	public String toString() {
		return Joiner.on(",").join(itemInfoList);
	}
}