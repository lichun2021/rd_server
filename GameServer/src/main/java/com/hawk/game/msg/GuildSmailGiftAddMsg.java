package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.guild.GuildSmailGift;

public class GuildSmailGiftAddMsg extends HawkMsg {
	private GuildSmailGift gift;

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static GuildSmailGiftAddMsg valueOf(GuildSmailGift gift) {
		GuildSmailGiftAddMsg msg = new GuildSmailGiftAddMsg();

		msg.gift = gift;
		return msg;
	}

	public GuildSmailGift getGift() {
		return gift;
	}

	public void setGift(GuildSmailGift gift) {
		this.gift = gift;
	}

}
