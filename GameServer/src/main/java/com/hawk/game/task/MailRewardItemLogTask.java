package com.hawk.game.task;

import org.hawk.config.HawkConfigManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.IMoneyType;
import com.hawk.log.LogConst.LogInfoType;

public class MailRewardItemLogTask extends HawkTask {
	private int itemType;
	private int itemId;
	private long count;
	private Player player;
	private Action action;
	private int mailId;
	private String uuid;

	public MailRewardItemLogTask(int itemType, int itemId, long count, Player player, Action action, int mailId, String uuid) {
		this.itemType = itemType;
		this.itemId = itemId;
		this.count = count;
		this.player = player;
		this.action = action;
		this.mailId = mailId;
		this.uuid = uuid;
	}

	@Override
	public Object run() {
		// 如果怕添加物品失败就
		// 此处不是购买道具不花钱，所以moneyType填什么无所谓
		ItemType type = ItemType.valueOf(GameUtil.convertToStandardItemType(itemType) / GsConst.ITEM_TYPE_BASE);
		if (type == ItemType.TOOL) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
			int goodsType = itemCfg == null ? 30000 : itemCfg.getItemType();
			LogUtil.logItemFlow(player, action, mailId, LogInfoType.goods_add, goodsType, itemId, count, 0, IMoneyType.MT_GOLD, uuid);
		} else if (type == ItemType.PLAYER_ATTR) {
			if (itemId == PlayerAttr.GOLD_VALUE) {
				LogUtil.logMoneyFlow(player, action, mailId, LogInfoType.money_add, count, IMoneyType.MT_GOLD, uuid);
			} else {
				long after = player.getResByType(itemId);
				LogUtil.logResourceFlow(player, action, mailId, LogInfoType.resource_add, itemId, after, count, uuid);
			}
		}
		
		if (mailId == MailId.REVENGE_TROOP_BACK_VALUE) {
			LogUtil.logReceiveRevengeSoldier(player, itemId, (int)count, 1);
		}
		
		return null;
	}

}
