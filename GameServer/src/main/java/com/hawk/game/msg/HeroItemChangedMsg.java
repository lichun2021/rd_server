package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.gamelib.GameConst.MsgId;

public class HeroItemChangedMsg extends HawkMsg {
	private ItemInfo itemAdd;
	private ItemCfg itemCfg;

	private HeroItemChangedMsg() {
		super(MsgId.HERO_ITEMCHANGED);
	}

	public static HeroItemChangedMsg valueOf(ItemInfo itemAdd, ItemCfg itemCfg) {
		HeroItemChangedMsg msg = new HeroItemChangedMsg();
		msg.itemAdd = itemAdd;
		msg.itemCfg = itemCfg;
		return msg;
	}

	public ItemInfo getItemAdd() {
		return itemAdd;
	}

	public void setItemAdd(ItemInfo itemAdd) {
		this.itemAdd = itemAdd;
	}

	public ItemCfg getItemCfg() {
		return itemCfg;
	}

	public void setItemCfg(ItemCfg itemCfg) {
		this.itemCfg = itemCfg;
	}

}
