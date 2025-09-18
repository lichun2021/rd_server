package com.hawk.game.player.item.impl;

import org.hawk.os.HawkRand;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PersonSendGreetingsEvent;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.LogUtil;
import com.hawk.serialize.string.SerializeHelper;

public class ActivityGreetingItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.ACTIVITY_GREETINGS_ITEM_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		//随机一个祝福语图片名字
		String blessingImgs = ConstProperty.getInstance().getBlessingImg();
		String[] blessingImgArr = blessingImgs.split(SerializeHelper.BETWEEN_ITEMS);
		int index = HawkRand.randInt(blessingImgArr.length - 1);
		String img = blessingImgArr[index];
		//广播
		ChatParames.Builder chatBuilder = ChatParames.newBuilder();
		chatBuilder.setPlayer(player);
		chatBuilder.setKey(NoticeCfgId.ACTIVITY_GREETINGS);
		chatBuilder.setChatType(ChatType.CHAT_WORLD);
		chatBuilder.addParms(img);
		ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());
		//活动事件
		ActivityManager.getInstance().postEvent(new PersonSendGreetingsEvent(player.getId(), 1));
		//打点
		LogUtil.logGreetUseItemImage(player, index);
		return true;
	}

}
