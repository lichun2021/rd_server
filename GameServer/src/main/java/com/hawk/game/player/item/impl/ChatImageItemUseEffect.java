package com.hawk.game.player.item.impl;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.config.ChatImgCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.HPCustomDataSync;
import com.hawk.game.protocol.SysProtocol.KVData;
import com.hawk.game.util.GsConst;

public class ChatImageItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.CHAT_EMOTION_ITEM_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		int chatImgId = ChatImgCfg.getCfgId(itemCfg.getId());
		if (chatImgId <= 0) {
			return false;
		}
		
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.CHAT_IMAGE_UNLOCKED);
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(GsConst.CHAT_IMAGE_UNLOCKED, 0, String.valueOf(chatImgId));
		} else {
			String[] images = customData.getArg().split(",");
			for (String image : images) {
				if (HawkOSOperator.isEmptyString(image.trim())) {
					continue;
				}
				if (chatImgId == Integer.parseInt(image.trim())) {
					HawkLog.logPrintln("ChatImageItem use repeated, playerId: {}, itemId: {}, chatImgId: {}, customData: {}", player.getId(), itemCfg.getId(), chatImgId, customData.getArg());
					syncChatImg(player, customData);
					return true;
				}
			}
			customData.setArg(String.format("%s,%d", customData.getArg(), chatImgId));
		}
		
		//解锁同步
		syncChatImg(player, customData);
		return true;
	}
	
	private void syncChatImg(Player player, CustomDataEntity customData) {
		HPCustomDataSync.Builder builder = HPCustomDataSync.newBuilder();
		KVData.Builder kvData = KVData.newBuilder();
		kvData.setKey(customData.getType());
		kvData.setVal(customData.getValue());
		kvData.setArg(customData.getArg());
		builder.addData(kvData);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CHAT_IMG_UNLOCK_SYNC, builder));	
	}

}
