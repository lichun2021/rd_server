package com.hawk.activity.type.impl.plantweapon;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.PlantWeaponChooseReq;
import com.hawk.game.protocol.Activity.PlantWeaponShopBuyReq;

/**
 * 泰能超武投放活动
 * 
 * author:lating
 */
public class PlantWeaponHandler extends ActivityProtocolHandler {
	
	/**
	 * 活动信息请求
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANT_WEAPON_INFO_REQ_VALUE)
	public void onPlayerReqInfo(HawkProtocol protocol, String playerId){
		PlantWeaponActivity activity = getActivity(ActivityType.PLANT_WEAPON_355);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		activity.syncActivityInfo(playerId);
	}
	
	/**
	 * 研究方向
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANT_WEAPON_RANDOM_C_VALUE)
	public void onAwardRanddom(HawkProtocol protocol, String playerId){
		PlantWeaponActivity activity = getActivity(ActivityType.PLANT_WEAPON_355);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		int result = activity.onAwardRandom(playerId);
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 开启研究
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANT_WEAPON_RESEARCH_C_VALUE)
	public void onResearch(HawkProtocol protocol, String playerId){
		PlantWeaponActivity activity = getActivity(ActivityType.PLANT_WEAPON_355);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		int result = activity.onResearch(playerId);
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		} else {
			responseSuccess(playerId, protocol.getType());
		}
	}
	
	/**
	 * 放弃研究
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANT_WEAPON_GIVEUP_C_VALUE)
	public void onGiveup(HawkProtocol protocol, String playerId){
		PlantWeaponActivity activity = getActivity(ActivityType.PLANT_WEAPON_355);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		int result = activity.onGiveup(playerId);
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 商店购买
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANT_WEAPON_SHOP_BUY_C_VALUE)
	public void onShopBuy(HawkProtocol protocol, String playerId){
		PlantWeaponActivity activity = getActivity(ActivityType.PLANT_WEAPON_355);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		
		PlantWeaponShopBuyReq req = protocol.parseProtocol(PlantWeaponShopBuyReq.getDefaultInstance());
		int result = activity.onShopBuy(playerId, req.getShopId(), req.getCount());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 选择超武
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANT_WEAPON_CHOOSE_C_VALUE)
	public void onChoosePlantWeapon(HawkProtocol protocol, String playerId){
		PlantWeaponActivity activity = getActivity(ActivityType.PLANT_WEAPON_355);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		
		PlantWeaponChooseReq req = protocol.parseProtocol(PlantWeaponChooseReq.getDefaultInstance());
		int result = activity.choosePlantWeapon(playerId, req.getPlantWeaponId());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 领取每日免费奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANT_WEAPON_FREE_AWARD_C_VALUE)
	public void onFreeRecieve(HawkProtocol protocol, String playerId){
		PlantWeaponActivity activity = getActivity(ActivityType.PLANT_WEAPON_355);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		int result = activity.recieveDailyReward(playerId);
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
}
