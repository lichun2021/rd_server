package com.hawk.game.invoker;

import java.util.ArrayList;
import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;

/**
 * 世界行军加速道具返还
 */
public class MarchSpeedInvoker extends HawkMsgInvoker {
	
	private Player player;
	private int itemId;
	
	public MarchSpeedInvoker(Player player, int itemId) {
		this.player = player;
		this.itemId = itemId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int val = player.getEffect().getEffVal(EffType.MARCH_SPEED_ITEM_BACK_628);
		if (val <= 0) {
			return false;
		}
		
		String key = "march_speed_item_count:" + player.getId(), field = String.valueOf(itemId);
		long count = RedisProxy.getInstance().hIncBy(key, field, 1);
		LogUtil.logMarchSpeedItemUse(player, itemId, (int)count);
		if (count >= ConstProperty.getInstance().getEffect628Num()) {
			RedisProxy.getInstance().getRedisSession().hDel(key, field);
			// 发邮件
			ItemInfo itemInfo = new ItemInfo(30000, itemId, val);
			List<ItemInfo> items = new ArrayList<ItemInfo>(1);
			items.add(itemInfo);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			        .setPlayerId(player.getId())
			        .setMailId(MailId.WORLD_MARCH_SPPED_ITEM_BACK)
			        .setAwardStatus(MailRewardStatus.NOT_GET)
			        .addSubTitles(itemId)
			        .addContents(val, itemId, val, itemId)
			        .setRewards(items)
			        .build());
		}
		
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getItemId() {
		return itemId;
	}
	
}
