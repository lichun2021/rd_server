package com.hawk.game.invoker;

import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.PlayerGuildGiftEntity;
import com.hawk.game.guild.GuildBigGift;
import com.hawk.game.service.GuildService;

public class GuildSmailGiftGetAwardMsgInvoker extends HawkMsgInvoker {
	String guildId;
	List<PlayerGuildGiftEntity> giftList;

	public GuildSmailGiftGetAwardMsgInvoker(String guildId, List<PlayerGuildGiftEntity> giftList) {
		super();
		this.giftList = giftList;
		this.guildId = guildId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		
		GuildBigGift bigGift = GuildService.getInstance().bigGift(guildId);
		int bigGiftLevelExp = 0;
		int bigGiftExp = 0;
		for(PlayerGuildGiftEntity gift: giftList){
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, gift.getItemId());
			bigGiftExp += itemCfg.getAddBigGiftExp();
			bigGiftLevelExp += itemCfg.getAddGiftLevelExp();
		}
		
		bigGift.incBigGiftLevelExp(bigGiftLevelExp);
		bigGift.incBigGiftExp(bigGiftExp);
		
		bigGift.notifyChanged();

		return true;
	}

	public List<PlayerGuildGiftEntity> getGiftList() {
		return giftList;
	}

	public void setGiftList(List<PlayerGuildGiftEntity> giftList) {
		this.giftList = giftList;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

}
