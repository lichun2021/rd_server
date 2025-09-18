package com.hawk.game.idipscript.query;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.game.util.GsConst.StatisticDataType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询玩家角色信息二请求  -- 10282139
 *
 * localhost:8081/idip/4421
 *
 * @param OpenId     
 * @param PlatId     
 * @param Partition  
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4421")
public class QueryPlayerRoleInfoHandler extends IdipScriptHandler {

	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		if (!player.isActiveOnline()) {
			boolean isCrossDay = !(HawkTime.isSameDay(HawkTime.getMillisecond(), player.getEntity().getResetTime()));
			if (isCrossDay) {
				StatisticsEntity statisticsEntity = player.getData().getStatisticsEntity();
				statisticsEntity.setCommonStatisData(StatisticDataType.GROUP_PVP_TOTAL_TODAY, 0);
				statisticsEntity.setCommonStatisData(StatisticDataType.GROUP_TOTAL_TODAY, 0);
				statisticsEntity.setCommonStatisData(StatisticDataType.COLLECT_RES_TODAY, 0);
				statisticsEntity.setCommonStatisData(StatisticDataType.ATK_GHOST_TOTAL_TODAY, 0);
				statisticsEntity.setCommonStatisData(StatisticDataType.PVE_TOTAL_TODAY, 0);
			}
		}
		
		// 角色ID
		result.getBody().put("RoleId", player.getId());      
		// 角色名称
		result.getBody().put("RoleName", player.getNameEncoded());  
		// 当前等级 
		result.getBody().put("Level", player.getLevel());   
		// 角色经验值
		result.getBody().put("Exp", player.getExp());    
		// 钻石数量
		result.getBody().put("Diamond", player.getDiamonds());
		// 黄金数量
		result.getBody().put("GoldCount", player.getGoldore());   
		// 铀矿资源
		result.getBody().put("UraniumCount", player.getSteel());  
		// 石油数量
		result.getBody().put("OilCount", player.getOil());              
		// 合金数量
		result.getBody().put("TombarthiteCount", player.getTombarthite());  
		// 燃料
		result.getBody().put("Fuel", 0);     
		// 非保护黄金数量
		result.getBody().put("UnsafeGoldCount", player.getGoldoreUnsafe());           
		// 非保护铀矿资源
		result.getBody().put("UnsafeUraniumCount", player.getSteelUnsafe());        
		// 非保护石油数量
		result.getBody().put("UnsafeOilCount", player.getOilUnsafe());            
		// 非保护合金数量
		result.getBody().put("UnsafeTombarthiteCount", player.getTombarthiteUnsafe());    
		// VIP等级
		result.getBody().put("VipLevel", player.getVipLevel());           
		// VIP经验值
		result.getBody().put("VipExp", player.getVipExp());               
		// 注册时间
		result.getBody().put("RegisterTime", player.getCreateTime()/1000);    
		// 累计登陆时长
		long onlineTime = player.getOnlineTimeHistory();
		result.getBody().put("TotalLoginTime", onlineTime > 0 ? onlineTime : (HawkTime.getMillisecond() - player.getLoginTime()) / 1000);
		// 最近登录时间
		result.getBody().put("LastLoginTime", player.getLoginTime()/1000);    
		// 最近登出时间
		result.getBody().put("LastLogoutTime", player.getLogoutTime()/1000); 
		// 账号是否在线：在线（0），离线（1）
		result.getBody().put("IsOnline", GlobalData.getInstance().isOnline(player.getId()) ? 0 : 1);          
		// 账号状态：正常（0），封停（1）
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(player.getPuid(), player.getServerId());
		result.getBody().put("Status", HawkTime.getMillisecond() < accountInfo.getForbidenTime() ? 1 : 0);  
		
		StatisticsEntity statisticsEntity = player.getData().getStatisticsEntity();
		 // 累计登录天数
		result.getBody().put("TotalLoginDay", statisticsEntity.getLoginDay());    
		// 累计采集资源数量  
		result.getBody().put("TotalCollect", statisticsEntity.getStatisData(StatisticDataType.COLLECT_RES_TODAY));
		// 累计集结次数（PVP集结+PVE集结，发起、参与都算）
		result.getBody().put("TotalGroup", statisticsEntity.getStatisData(StatisticDataType.GROUP_TOTAL_TODAY));
		// 累计打野次数（单人）
		result.getBody().put("TotalPve", statisticsEntity.getStatisData(StatisticDataType.PVE_TOTAL_TODAY));          
		// 累计集结进攻发生战斗次数（发起、参与集结都算）
		result.getBody().put("TotalGroupPvp", statisticsEntity.getStatisData(StatisticDataType.GROUP_PVP_TOTAL_TODAY)); 
		// 累计战胜幽灵基地（单打、集结都算）
		result.getBody().put("TotalGroupGhost", statisticsEntity.getStatisData(StatisticDataType.ATK_GHOST_TOTAL_TODAY)); 
		// 回流时间:回流过（时间非0），普通（时间为0）
		result.getBody().put("BackTime", 0);  // 这个是备用字段， 如果游戏内有回流时间的话就写上， 没有就是
		// 注册区服
		result.getBody().put("RegPartition", Integer.parseInt(player.getServerId()));
		
		// 当日消费金条
		result.getBody().put("DayUseDiamond", RedisProxy.getInstance().getIdipDailyStatis(player.getId(), IDIPDailyStatisType.DIAMOND_CONSUME));
		// 当日消费金币
		result.getBody().put("DayUseCoin", RedisProxy.getInstance().getIdipDailyStatis(player.getId(), IDIPDailyStatisType.GOLD_CONSUME));
		// 当日总充值（元）
		result.getBody().put("DayRecharge", RedisProxy.getInstance().getIdipDailyStatis(player.getId(), IDIPDailyStatisType.TOTAL_RECHARGE) / 10);
		
		try {
			String channelId = RedisProxy.getInstance().getRedisSession().getString("PlayerRegChannel:" + player.getOpenId());
			if (!HawkOSOperator.isEmptyString(channelId)) {
				result.getBody().put("RegisterChannel", Integer.parseInt(channelId));
			} else {
				result.getBody().put("RegisterChannel", 0);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("RegisterChannel", 0);
		}
		
		return result;
	}
}


