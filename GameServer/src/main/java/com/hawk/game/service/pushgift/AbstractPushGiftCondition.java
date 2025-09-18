package com.hawk.game.service.pushgift;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import com.hawk.game.config.PushGiftGroupCfg;
import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.PushGift.PushGiftOper;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MapUtil;


/**
 * 
 * @author jm
 *
 * @param <T>
 */
public abstract class AbstractPushGiftCondition {
	/**
	 * 比较
	 * @param cfgParam
	 * @param param
	 * @return
	 */
	public abstract boolean isReach(List<Integer> cfgParam, List<Integer> param);
	
	/**
	 * 需求改了 只能加个方法.
	 * @param playerData
	 * @param cfgParam
	 * @param param
	 * @return
	 */
	public boolean isReach(PlayerData playerData, List<Integer> cfgParam, List<Integer> param){
		return isReach(cfgParam, param);
	}
	
	/**
	 * 加个方法，支持从配置中读取 cfgParam 之外的其它参数
	 * 
	 * @param playerData
	 * @param cfg
	 * @param param
	 * @return
	 */
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param){
		return isReach(cfg.getParamList(), param);
	}
	
	/**
	 * 获取condition类型
	 * @return
	 */
	public abstract int getConditionType();

	/**
	 * 返回Entity方便做批量推送处理
	 * @param params 根据场景的不一样参数的含义不一样, 
	 * @return
	 */
	public  boolean handle(PlayerData playerData, List<Integer>param, boolean online) {		
		AssembleDataManager assembleDataManager = AssembleDataManager.getInstance();
		int conditionType = this.getConditionType();
		//groupList 有可能不存在这里拦截一下
		List<PushGiftGroupCfg> groupList = assembleDataManager.getPushGiftGroupCfgList(conditionType);
		if (groupList == null) {
			HawkLog.debugPrintln("null gruopList", conditionType);
			return false;
		}
		
		PushGiftEntity pushGiftEntity = playerData.getPushGiftEntity();
		List<PushGiftGroupCfg> validGroupList = new ArrayList<>();
		int num = 0;
		Map<Integer, Integer> groupRefreshCountMap = pushGiftEntity.getGroupRefreshCountMap();
		Set<Integer> existsGroupIds = this.getExistGroupId(pushGiftEntity);
		for (PushGiftGroupCfg groupCfg : groupList) {
			if (groupCfg.getIsSale() != GsConst.PushGiftConst.SALE) {
				continue;				
			}
			//已经有同一个group的物品则不刷新
			if (existsGroupIds.contains(groupCfg.getGroupId())) {
				continue;
			}
			
			// 判断终身触发次数
			int touchGiftTimes = PushGiftManager.getInstance().getTouchGiftTimes(playerData.getPlayerId(), groupCfg.getGroupId());
			if (groupCfg.getAllInterval() > 0 && touchGiftTimes >= groupCfg.getAllInterval()) {
				continue;	
			}
			
			num = MapUtil.getIntValue(groupRefreshCountMap, groupCfg.getGroupId());
			if (groupCfg.getTimeInterval() > 0 && num >= groupCfg.getTimeInterval()) {
				continue;
			}
			validGroupList.add(groupCfg);
		}
		
		if (validGroupList.isEmpty()) {
			return false;
		}
		
		List<Integer> synList = new ArrayList<>();		
		List<PushGiftLevelCfg> giftLevelList = null;
		List<Integer> refreshList = null;		
		for (PushGiftGroupCfg groupCfg : validGroupList) {			
			giftLevelList = assembleDataManager.getPushGiftLevelCfgList(groupCfg.getGroupId());			
			refreshList = this.handle(playerData, groupCfg, giftLevelList, param, online);
			
			if (refreshList != null && !refreshList.isEmpty()) {
				// 添加推送礼包触发日志 
				Player player  = GlobalData.getInstance().makesurePlayer(playerData.getPlayerEntity().getId());
				for (Integer giftId : refreshList) {
					LogUtil.logPushGiftRefresh(player, conditionType, groupCfg.getGroupId(), giftId);
				}
				//这个enttiy的add方法生成的不太好.下次改改
				int oldNum = MapUtil.getIntValue(pushGiftEntity.getGroupRefreshCountMap(), groupCfg.getGroupId());
				pushGiftEntity.addGroupRefreshCount(groupCfg.getGroupId(), oldNum + 1);
				synList.addAll(refreshList);
				
				// 添加终身触发次数
				if (groupCfg.getAllInterval() > 0) {
					PushGiftManager.getInstance().addTouchGiftTimes(playerData.getPlayerId(), groupCfg.getGroupId());
				}
			}
		}
		
		if (!synList.isEmpty()) {
			Player.logger.info("playerId:{} trigger push gift id:{}", playerData.getPlayerEntity().getId(), synList);
			PushGiftManager.getInstance().updatePushGiftList(playerData.getPlayerEntity().getId(), synList, PushGiftOper.ADD);
			return true;
		}
		return false;
	}
	
	private Set<Integer> getExistGroupId(PushGiftEntity pushGiftEntity) {
		PushGiftLevelCfg giftCfg = null;
		Set<Integer> existGroupIds = new HashSet<>();
		for (Integer giftId : pushGiftEntity.getGiftIdTimeMap().keySet()) {
			giftCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftLevelCfg.class, giftId);
			existGroupIds.add(giftCfg.getGroupId());
		}
		
		return existGroupIds;
	}
	
	private  List<Integer> handle(PlayerData playerData, PushGiftGroupCfg groupCfg, List<PushGiftLevelCfg> levelCfgList, List<Integer> param, boolean online){
		PushGiftLevelCfg cfg = null;
		boolean multi = false;
		if (groupCfg.getGroupType() != GsConst.PushGiftConst.TRIGGER_TYPE_SINGLE) {
			multi = true;
		}		
		boolean isTrigger = false;
		List<Integer> readchIdList = new ArrayList<>();
		int curTime = HawkTime.getSeconds();
		PushGiftEntity pushGiftEntity = playerData.getPushGiftEntity();
		//倒序从最大的找起
		for (int i = levelCfgList.size() - 1; i >= 0; i--) {
			cfg = levelCfgList.get(i);
			if (this.isReach(playerData, cfg, param)){
				Integer endTime = null;			
				if (online) {
					endTime = curTime + groupCfg.getLimitTime();
				} else {
					//结束时间为0是一个特殊值，下次该玩家上线的时候重置.
					endTime = Integer.valueOf(0);
				}
				
				pushGiftEntity.addGiftIdTime(cfg.getId(), endTime);
				readchIdList.add(cfg.getId());
				isTrigger = true;
			}
			
			if(!multi && isTrigger) {
				break;
			}
		}
		
		return readchIdList;
						
	}
	
	public List<Integer> createPushGiftEntity(PlayerData playerData, PushGiftGroupCfg groupCfg, List<PushGiftLevelCfg> giftList,  boolean online) {		
		 
		PushGiftEntity pushGiftEntity = playerData.getPushGiftEntity();
		int curTime = HawkTime.getSeconds();
		List<Integer> newGift = new ArrayList<>();
		for (PushGiftLevelCfg pushGiftLevelCfg : giftList) {
			Integer endTime = null;			
			if (online) {
				endTime = curTime + groupCfg.getLimitTime();
			} else {
				//结束时间为0是一个特殊值，下次该玩家上线的时候重置.
				endTime = Integer.valueOf(0);
			}
			
			pushGiftEntity.addGiftIdTime(pushGiftLevelCfg.getId(), endTime);
			newGift.add(pushGiftLevelCfg.getId());
		}
		
		return newGift;
	}
}
