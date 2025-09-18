package com.hawk.activity.type.impl.playerComeBack.helper;


import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.entity.ActivityAccountRoleInfo;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.impl.playerComeBack.cfg.PlayerComeBackParamsConfig;

/***
 * 老玩家回归帮助类
 * @author yang.rao
 *
 */
public class ComeBackPlayerHelper {
	
	final static Logger logger = LoggerFactory.getLogger("Server");
	
	/***
	 * 是否是回归玩家
	 * @param openId
	 * @return
	 */
	public static ActivityAccountRoleInfo isComeBackPlayer(String openId){
		List<String> list = ActivityGlobalRedis.getInstance().getAccountRoleList(openId);
		if(list.isEmpty()){
			logger.error("check come back player error. can't find AccountRoleInfo from redis:{}", openId);
			return null;
		}
		List<ActivityAccountRoleInfo> infos = new ArrayList<>();
		for(String value : list){
			infos.add(JsonUtils.String2Object(value, ActivityAccountRoleInfo.class));
		}
		boolean fitParams = false; //有角色满足所有的配置条件
		long time = HawkTime.getMillisecond(); //当前系统时间
		long constTime = getLostTimeConst();
		ActivityAccountRoleInfo lostRole = null;
		ActivityAccountRoleInfo identifyPlayer = isIdentifyPlayer(infos, openId);
		if(identifyPlayer != null){
			return identifyPlayer;
		}
		//判断是不是老玩家回归
		for(ActivityAccountRoleInfo info : infos){
			if(info.getLogoutTime() == 0L){
//				ActivityAccountRoleInfo inherit = ActivityManager.getInstance().getDataGeter().getRoleInfoInheritIdentifyAndRemove(openId);
//				if(inherit != null){
//					return inherit;
//				}
				continue; //新注册的帐号进来
			}
			if(time - info.getLogoutTime() < constTime){
				logger.info("check come back player, find role hasn't lost, serverId:{}, playerId:{}, openId:{}", info.getServerId(), info.getPlayerId(), info.getOpenId());
				return null;
			}
			//判断info是否满足所有条件
			if(checkFitLostParams(info)){
				fitParams = true;
				if(lostRole == null){
					lostRole = info;
				}else{
					if(info.getLogoutTime() > lostRole.getLogoutTime()){ //下线时间更大，表示流失更近
						lostRole = info;
					}
				}
			}
		}
		if(fitParams){
			//把这个帐号信息，存到GlobalRedis
			ActivityManager.getInstance().getDataGeter().saveRoleInfoAfterOldServerComeBack(lostRole);
			return lostRole;
		}
		return null;
	}
	
	private static ActivityAccountRoleInfo isIdentifyPlayer(List<ActivityAccountRoleInfo> infos, String openId){
		if(infos == null || infos.isEmpty()){
			return null;
		}
		boolean isCreateNewPlayer = false;
		for(ActivityAccountRoleInfo info : infos){
			if(info.getLogoutTime() == 0L){
				isCreateNewPlayer = true;
			}
		}
		if(isCreateNewPlayer){
			ActivityAccountRoleInfo inherit = ActivityManager.getInstance().getDataGeter().getRoleInfoInheritIdentifyAndRemove(openId);
			if(inherit != null){
				return inherit;
			}
		}
		return null;
	}
	
	/***
	 * 获取流失时间长度(ms)
	 * @return
	 */
	private static long getLostTimeConst(){
		PlayerComeBackParamsConfig config = HawkConfigManager.getInstance().getKVInstance(PlayerComeBackParamsConfig.class);
		return config.getLostTime() * 1000l;
	}
	
	/***
	 * 判定一个角色是否满足所有的匹配条件() 
	 * @param info
	 * @return
	 */
	private static boolean checkFitLostParams(ActivityAccountRoleInfo info){
		PlayerComeBackParamsConfig config = HawkConfigManager.getInstance().getKVInstance(PlayerComeBackParamsConfig.class);
		if(info.getVipLevel() >= config.getVip() && info.getPlayerLevel() >= config.getLevel() && info.getBattlePoint() >= config.getBattlePoint() && info.getCityLevel() >= config.getBuildLevel()){
			return true;
		}
		return false;
	}
}
