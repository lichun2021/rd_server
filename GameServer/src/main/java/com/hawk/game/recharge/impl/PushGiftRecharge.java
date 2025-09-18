package com.hawk.game.recharge.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import com.hawk.game.GsApp;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.PushGiftGroupCfg;
import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.PushGiftDeliverMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.PushGift.PushGiftOper;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.service.pushgift.PushGiftManager;
import com.hawk.game.util.GsConst;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 推送礼包
 * 
 * @author lating
 *
 */
public class PushGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		return pushGiftBuyCheck(player, giftCfg.getId(), protocol);
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		GsApp.getInstance().postMsg(player, new PushGiftDeliverMsg(giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.PUSH_GIFT;
	}

	private boolean pushGiftBuyCheck(Player player, String id, int protoType) {
		PushGiftEntity giftEntity = player.getData().getPushGiftEntity();
		List<Integer> giftIdList = AssembleDataManager.getInstance().getPushIdListByPayId(id);
		if (giftIdList == null) {
			HawkLog.errPrintln("playerId:{}  push gift fail can not find giftId payGiftId:{}", player.getId(), id);
			return false;
		}
		Integer giftId = null;
		Optional<Integer> op = giftIdList.stream().filter(i->{
			PushGiftLevelCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftLevelCfg.class, i);
			if (giftCfg == null) {
				return false;
			}
			
			return giftEntity.getGiftIdTimeMap().containsKey(giftCfg.getId());
			
		}).findFirst();
		
		if (!op.isPresent()) {
			HawkLog.errPrintln("playerId:{} check fail can not find push gift in player group list payGiftId:{}, giftIdList", player.getId(), id, giftIdList);			
			return false;
		}
		
		giftId = op.get();
		Integer endTime = giftEntity.getGiftIdTimeMap().get(giftId);
		if (endTime == null || endTime < HawkTime.getSeconds()) {
			HawkLog.warnPrintln("pushGift time over playerId{}, giftId: {}", player.getId(), giftId);
			player.sendError(protoType,  Status.Error.PUSH_GIFT_SOLD_OUT, 0);
			giftEntity.getGiftIdTimeMap().remove(giftId);
			PushGiftManager.getInstance().updatePushGiftList(player, Arrays.asList(giftId), PushGiftOper.DELETE);
		}
		
		PushGiftLevelCfg pushGiftLevelCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftLevelCfg.class, giftId);
		PushGiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftGroupCfg.class, pushGiftLevelCfg.getGroupId());
		if (groupCfg.getIsSale() != GsConst.PushGiftConst.SALE) {
			player.sendError(protoType, Status.SysError.CONFIG_ERROR_VALUE, 0);
			giftEntity.getGiftIdTimeMap().remove(giftId);			
			HawkLog.warnPrintln("pushGift config invliad playerId: {}, groupId: {}, giftId: {} ", player.getId(), groupCfg.getGroupId(), pushGiftLevelCfg.getLevel());			
			PushGiftManager.getInstance().updatePushGiftList(player, Arrays.asList(giftId), PushGiftOper.DELETE);			
			return false;
		}
														
		return true;		
	}
	
}
