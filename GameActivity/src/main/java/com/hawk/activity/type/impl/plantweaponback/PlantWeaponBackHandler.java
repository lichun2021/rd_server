package com.hawk.activity.type.impl.plantweaponback;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.PlantWeaponBackDrawReq;
import com.hawk.game.protocol.Activity.PlantWeaponBackShopBuyReq;

/**
 * 泰能超武返场活动
 * 
 * @author lating
 */
public class PlantWeaponBackHandler extends ActivityProtocolHandler {
	
	/**
	 * 活动信息请求
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.PLANT_WEAPON_BACK_INFO_REQ_VALUE)
	public void onActivityInfoReq(HawkProtocol protocol, String playerId){
		PlantWeaponBackActivity activity = getActivity(ActivityType.PLANT_WEAPON_BACK_360);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		activity.syncActivityInfo(playerId);
	}
	
	/**
	 * 商店购买
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.PLANT_WEAPON_BACK_SHOP_BUY_C_VALUE)
	public void onShopBuy(HawkProtocol protocol, String playerId){
		PlantWeaponBackActivity activity = getActivity(ActivityType.PLANT_WEAPON_BACK_360);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		
		PlantWeaponBackShopBuyReq req = protocol.parseProtocol(PlantWeaponBackShopBuyReq.getDefaultInstance());
		int result = activity.onShopBuy(playerId, req.getShopId(), req.getCount());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 抽奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.PLANT_WEAPON_BACK_DRAW_C_VALUE)
	public void onDraw(HawkProtocol protocol, String playerId){
		PlantWeaponBackActivity activity = getActivity(ActivityType.PLANT_WEAPON_BACK_360);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		
		PlantWeaponBackDrawReq req = protocol.parseProtocol(PlantWeaponBackDrawReq.getDefaultInstance());
		int result = activity.onDraw(playerId, req.getType());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
}
