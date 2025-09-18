package com.hawk.game.recharge.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import com.hawk.game.GsApp;
import com.hawk.game.config.GiftCfg;
import com.hawk.game.config.GiftGroupCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.PlayerGiftModule;
import com.hawk.game.msg.SuperGiftDirectBuyMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.util.GsConst;
import com.hawk.game.recharge.RechargeType;

/**
 * 商城超值礼包
 * 
 * @author lating
 *
 */
public class SuperGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		return superGiftBuyCheck(player, giftCfg.getId(), protocol);
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		GsApp.getInstance().postMsg(player, new SuperGiftDirectBuyMsg(giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.SUPER_GIFT;
	}

	private boolean superGiftBuyCheck(Player player, String id, int protoType) {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		List<Integer> giftIdList = AssembleDataManager.getInstance().getSuperGiftIdListByPayGiftId(id);
		if (giftIdList == null) {
			HawkLog.errPrintln("playerId:{} deliver gift fail can not find giftId payGiftId:{}", player.getId(), id);
			return false;
		}
		Integer giftId = null;
		Optional<Integer> op = giftIdList.stream().filter(i->{
			GiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GiftCfg.class, i);
			if (giftCfg == null) {
				return false;
			}
			return giftEntity.getGiftGroupIdsList().contains(giftCfg.getGroupId()) && giftEntity.getBuyNum(i) == 0;
		}).findFirst();
		
		if (!op.isPresent()) {
			HawkLog.errPrintln("playerId:{} check fail can not find giftCfg in player group list payGiftId:{}, giftIdList", player.getId(), id, giftIdList);
			return false;
		}
		
		giftId = op.get();
		PlayerGiftModule giftModule = player.getModule(GsConst.ModuleType.GIFT_MOUDLE);
		if (giftId == null) {
			player.sendError(protoType, Status.Error.SUPER_GIFT_PAY_ID_NOT_FOUND_VALUE, 0);
			return false;
		}
		
		GiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GiftCfg.class, giftId);
		if (Objects.isNull(giftCfg)) {
			player.sendError(protoType, Status.Error.ITEM_TYPE_ERROR, 0);
			HawkLog.warnPrintln("giftModule playerId: {}, giftId: {} is invalid", player.getId(), giftId);
			return false;
		}
				
		GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, giftCfg.getGroupId());
		if (groupCfg.getOnSale() != GsConst.INT_TRUE) {
			HawkLog.warnPrintln("giftModule gift not sell  giftId:{}, sell:{}", player.getId(), giftCfg.getId(), groupCfg.getOnSale());
			player.sendError(protoType, Status.SysError.GIFT_NOT_SALE_VALUE, 0);
			giftEntity.removeGiftGroupId(groupCfg.getId());
			giftEntity.removeGiftAdvice(groupCfg.getId());
			giftModule.syncGiftInfo();
			return false;
		}
		
		if (!giftModule.checkSell(giftCfg.getGroupId(), giftId)) {
			HawkLog.warnPrintln("giftModule gift  limit buy num playerId:{}, giftId:{}, sell:{}", player.getId(), giftCfg.getId(), groupCfg.getOnSale());
			player.sendError(protoType, Status.Error.SUPER_GIFT_LIMIT_VALUE, 0);
			return false;
		}
							
		if (!giftEntity.getGiftGroupIdsList().contains(groupCfg.getId())){
			HawkLog.warnPrintln("giftModul groupId not in playerGroupIdList playerId:{}. buyGroupId:{}, playerGroupIdList: {}", player.getId(), groupCfg.getId(), giftEntity.getGiftGroupIdsList());
			player.sendError(protoType, Status.Error.SUPER_GIFT_NOT_IN_LIST_VALUE, 0);
			return false;
		}
		
		int curLevel = giftEntity.getBuyLevel(giftCfg.getGroupId());
		if (curLevel + 1 != giftCfg.getLevel()) {			  
			HawkLog.warnPrintln("giftModul buy sequence incorrect. playerId:{}, buyGroupId:{}, curLevel:{}, buyLevel:{}", player.getId(), groupCfg.getId(), curLevel, giftCfg.getLevel());
			player.sendError(protoType, Status.Error.SUPER_GIFT_NOT_IN_LIST_VALUE, 0);
			return false;
		}
		
		return true;
	}
	
}
