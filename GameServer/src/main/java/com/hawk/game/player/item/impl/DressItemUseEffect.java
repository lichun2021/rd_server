package com.hawk.game.player.item.impl;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ActiveSkinEvent;
import com.hawk.activity.event.impl.DressActiveEvent;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.DressCfg;
import com.hawk.game.config.DressToolCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Dress;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;

public class DressItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.DRESS_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		int dressToolId = itemCfg.getDressId();
		DressToolCfg dressToolCfg = HawkConfigManager.getInstance().getConfigByKey(DressToolCfg.class, dressToolId);
		if (dressToolCfg == null) {
			HawkLog.errPrintln("useDressItem error, dressToolCfg null, playerId: {}, dressToolId: {}", player.getId(), dressToolId);
			return false;
		}
		
		DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressToolCfg.getDressId());
		if (dressCfg == null) {
			HawkLog.errPrintln("useDressItem error, dressCfg null, playerId: {}, dressId: {}", player.getId(), dressToolCfg.getDressId());
			return false;
		}
		
		DressEntity dressEntity = player.getData().getDressEntity();
		DressItem dressInfo = dressEntity.getDressInfo(dressCfg.getDressType(), dressCfg.getModelType());
		if (dressInfo != null && dressInfo.getContinueTime() >= GsConst.PERPETUAL_MILL_SECOND) {
			// 已经存在永久效果了，再用就是浪费，直接补对应的东西
			if (GameUtil.itemPropExchangeAward(player, itemCfg.getId(), 1)) {
				return true;
			}
			if(dressItemExchange(player, dressToolCfg)){
				return true;
			}
		}
		
		dressEntity.addOrUpdateDressInfo(dressCfg.getDressType(), dressCfg.getModelType(), dressToolCfg.getContinueTime() * 1000L);
		WorldPointService.getInstance().updateShowDress(player.getId(), dressCfg.getDressType(), dressEntity.getDressInfo(dressCfg.getDressType(), dressCfg.getModelType()));
		ActivityManager.getInstance().postEvent(new ActiveSkinEvent(player.getId(), dressCfg.getDressType(), dressCfg.getModelType(), dressToolCfg.getContinueTime()));
		ActivityManager.getInstance().postEvent(new DressActiveEvent(player.getId(), dressCfg.getDressId()));
		if(ConstProperty.getInstance().isDressGodOpen() && dressCfg.getIsShowMyth() > 0){
			CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.DRESS_GOD_ACTIVE);
			if(customData == null){
				player.getData().createCustomDataEntity(GsConst.DRESS_GOD_ACTIVE, 0, "");
			}
		}
		player.getPush().syncDressInfo();
		player.getEffect().resetEffectDress(player);
		notifyDressShow(player);
		HawkLog.debugPrintln("useDressItem do dress, playerId: {}, dressToolId: {}, dressType: {}, modelType: {}", player.getId(), dressToolId, dressCfg.getDressType(), dressCfg.getModelType());
		Dress.DressSuccessResp.Builder builder = Dress.DressSuccessResp.newBuilder();
		builder.setDressType(Dress.DressType.valueOf(dressCfg.getDressType()));
		builder.setModelType(dressCfg.getModelType());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.DRESS_SUCCESS_RESP, builder));
		return true;
	}
	
	/**
	 * 通知装扮显示更新
	 */
	private void notifyDressShow(Player player) {
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		if (worldPoint == null) {
			return;
		}
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	private boolean dressItemExchange(Player player, DressToolCfg dressToolCfg){
		if(!dressToolCfg.canExchange()){
			return false;
		}
		ItemInfo itemInfo = ItemInfo.valueOf(dressToolCfg.getExchangeItem());
		if (itemInfo.getItemType() == Const.ItemType.PLAYER_ATTR && itemInfo.getItemId() == PlayerAttr.DIAMOND_VALUE) {
			player.increaseDiamond((int)itemInfo.getCount(), Action.PROP_EXCHANGE, null, DiamondPresentReason.GAMEPLAY);
		} else {
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(itemInfo);
			awardItems.rewardTakeAffectAndPush(player, Action.PROP_EXCHANGE, true);
		}
		return true;
	}

}
