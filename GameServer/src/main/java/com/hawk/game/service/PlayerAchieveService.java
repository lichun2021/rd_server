package com.hawk.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.PlayerAchieveCfg;
import com.hawk.game.entity.PlayerAchieveEntity;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.PlayerAchieveItem;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlayerAchieve.AchieveData;
import com.hawk.game.protocol.PlayerAchieve.AchieveState;
import com.hawk.game.protocol.PlayerAchieve.PlayerAchieveInfo;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.type.IMission;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGPlayerAchieveChangeMsg;
import com.hawk.log.Action;

/**
 * 玩家成就服务类
 * @author golden
 *
 */
public class PlayerAchieveService {
	
	public static Logger logger = LoggerFactory.getLogger("Server");
	
	private static PlayerAchieveService instance;
	
	/**
	 * 唯一成就
	 */
	private static Map<Integer, String> soleAchieve;
	
	public static PlayerAchieveService getInstance() {
		if (instance == null) {
			instance = new PlayerAchieveService();
		}
		return instance;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		
		// 唯一成就
		soleAchieve = new ConcurrentHashMap<Integer, String>();
		Map<String, String> allSoleAchieve = LocalRedis.getInstance().getAllSoleAchieve();
		if (allSoleAchieve != null) {
			for (Entry<String, String> entry : allSoleAchieve.entrySet()) {
				soleAchieve.put(Integer.valueOf(entry.getKey()), entry.getValue());
			}
		}
		
		return true;
	}
	
	/**
	 * 初始化成就
	 */
	public PlayerAchieveEntity initPlayerAchieve(Player player) {
		
		PlayerAchieveEntity playerAchieveEntity = player.getData().getPlayerAchieveEntity();
		List<PlayerAchieveItem> achieves = new ArrayList<>();
		Set<Integer> groups = AssembleDataManager.getInstance().getPlayerAchieveGroups();
		for (int groupId : groups) {
			
			PlayerAchieveItem achieve  = new PlayerAchieveItem(groupId, 0, 0);
			
			PlayerAchieveCfg achieveCfg = AssembleDataManager.getInstance().getPlayerAchieve(groupId);
			IMission iAchieve = MissionContext.getInstance().getMissions(MissionType.valueOf(achieveCfg.getAchieveType()));
			iAchieve.initMission(player.getData(), achieve, achieveCfg.getMissionCfgItem());
			
			achieves.add(achieve);
			
		}
		playerAchieveEntity.updateMissionItems(achieves);
		
		return null;
	}
	
	/**
	 * 检测新的成就项
	 * @param player
	 */
	public void checkNewAchieve(Player player) {
		PlayerAchieveEntity playerAchieveEntity = player.getData().getPlayerAchieveEntity();
		List<PlayerAchieveItem> achieves = playerAchieveEntity.getMissionItems();
		
		Set<Integer> groups = AssembleDataManager.getInstance().getPlayerAchieveGroups();
		for (int groupId : groups) {
			PlayerAchieveItem achieveItem = playerAchieveEntity.getAchieveItem(groupId);
			if (achieveItem != null) {
				continue;
			}
			
			PlayerAchieveItem achieve  = new PlayerAchieveItem(groupId, 0, 0);
			
			PlayerAchieveCfg achieveCfg = AssembleDataManager.getInstance().getPlayerAchieve(groupId);
			IMission iAchieve = MissionContext.getInstance().getMissions(MissionType.valueOf(achieveCfg.getAchieveType()));
			iAchieve.initMission(player.getData(), achieve, achieveCfg.getMissionCfgItem());
			
			achieves.add(achieve);
		}
		
		playerAchieveEntity.updateMissionItems(achieves);
	}
	
	/**
	 * 同步成就信息
	 */
	public void syncAchieveInfo(Player player) {
		PlayerAchieveEntity entity = player.getData().getPlayerAchieveEntity();
		
		PlayerAchieveInfo.Builder builder = PlayerAchieveInfo.newBuilder();
		
		List<PlayerAchieveItem> achieves = entity.getMissionItems();
		for (PlayerAchieveItem achieve : achieves) {
			builder.addData(genSingleAchieveBuilder(achieve));
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_ACHIEVE_INFO_PUSH, builder));
	}

	/**
	 * 同步成就更新
	 * @param player
	 * @param achieveItem
	 */
	public void syncAchieveUpdate(Player player, PlayerAchieveItem achieveItem) {
		PlayerAchieveInfo.Builder builder = PlayerAchieveInfo.newBuilder();
		builder.addData(genSingleAchieveBuilder(achieveItem));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_ACHIEVE_INFO_UPDATE, builder));
	}
	
	/**
	 * 单条成就信息builder
	 * @param achieve
	 * @return
	 */
	private AchieveData.Builder genSingleAchieveBuilder(PlayerAchieveItem achieve) {
		AchieveData.Builder achieveBuilder = AchieveData.newBuilder();
		achieveBuilder.setGroupId(achieve.getCfgId());

		// guoup成就最大等级
		int playerAchieveMaxLvl = AssembleDataManager.getInstance().getPlayerAchieveMaxLvl(achieve.getCfgId());
		// 领奖次数
		int rewardTimes = achieve.getState();
		// 当前所在等级
		int currentLevel = rewardTimes >= playerAchieveMaxLvl ? playerAchieveMaxLvl : rewardTimes + 1;
		
		PlayerAchieveCfg cfg = AssembleDataManager.getInstance().getPlayerAchieve(achieve.getCfgId(), currentLevel);
		
		AchieveState state = AchieveState.ACHIEVE_NOT_FINISH;
		
		if (achieve.getValue() >= cfg.getTarget()) {
			state = AchieveState.ACHIEVE_NOT_REWARD;
		}
		
		if (achieve.getValue() >= cfg.getTarget() && rewardTimes >= currentLevel) {
			state = AchieveState.ACHIEVE_REWARD;
		}
		
		achieveBuilder.setCurrentLvl(currentLevel);
		achieveBuilder.setValue((int) Math.min(Integer.MAX_VALUE - 1, achieve.getValue()));
		achieveBuilder.setState(state);
		achieveBuilder.setCompleteTime(achieve.getCompleteTime());
		return achieveBuilder;
	}
	
	/**
	 * 领取奖励
	 * @param player
	 * @param groupId
	 */
	public void onTakeAward(Player player, int groupId) {
		PlayerAchieveEntity entity = player.getData().getPlayerAchieveEntity();
		PlayerAchieveItem achieveItem = entity.getAchieveItem(groupId);
		// 已经领取的等级
		int receivedLvl = achieveItem.getState();
		// guoup成就最大等级
		int playerAchieveMaxLvl = AssembleDataManager.getInstance().getPlayerAchieveMaxLvl(achieveItem.getCfgId());
		// 当前所在等级
		int currentLevel = receivedLvl >= playerAchieveMaxLvl ? playerAchieveMaxLvl : receivedLvl + 1;
		// 当前等级配置
		PlayerAchieveCfg achieveCfg = AssembleDataManager.getInstance().getPlayerAchieve(groupId, currentLevel);
		if (receivedLvl >= currentLevel || achieveItem.getValue() < achieveCfg.getTarget()) {
			return;
		}
		
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(achieveCfg.getRewards());
		awardItems.rewardTakeAffectAndPush(player, Action.PLAYER_ACHIEVE_REWARD, false, null);
		
		achieveItem.setState(currentLevel);
		
		// 刷新成就
		achieveRefresh(player, achieveItem, currentLevel);
		
		achieveItem.setCompleteTime(HawkTime.getSeconds());
		entity.updateBattleMissionItem(achieveItem);
		syncAchieveUpdate(player, achieveItem);
		
		// 我要变强 领取成就点 计算积分
		StrengthenGuideManager.getInstance().postMsg( new SGPlayerAchieveChangeMsg(player));
		logger.info("take achieve award, playerId:{}, award:{}, afterAchieve:{}", player.getId(), awardItems.toString(), achieveItem.toString());
	}
	
	/**
	 * 刷新成就 (针对档位提升，成就类型改变)
	 * @param player
	 * @param achieveItem
	 * @param currentLevel
	 */
	private void achieveRefresh(Player player, PlayerAchieveItem achieveItem, int currentLevel) {
		int groupId = achieveItem.getCfgId();
		PlayerAchieveCfg beforeCfg = AssembleDataManager.getInstance().getPlayerAchieve(groupId, currentLevel);
		PlayerAchieveCfg afterCfg = AssembleDataManager.getInstance().getPlayerAchieve(groupId, currentLevel + 1);
		if (beforeCfg == null || afterCfg == null) {
			return;
		}
		if (beforeCfg.getAchieveType() == afterCfg.getAchieveType() && beforeCfg.getCondition().equals(afterCfg.getCondition())) {
			return;
		}
		
		achieveItem.setValue(0);
		IMission iAchieve = MissionContext.getInstance().getMissions(MissionType.valueOf(afterCfg.getAchieveType()));
		iAchieve.initMission(player.getData(), achieveItem, afterCfg.getMissionCfgItem());
	}
	
	/**
	 * 是否全服成就已经达成
	 * @return
	 */
	public boolean isSoleAchieveAlreadyConclude(int achieveId) {
		String achieve = soleAchieve.get(achieveId);
		return achieve != null;
	}
	
	/**
	 * 检测全服成就完成
	 * @param player
	 * @param cfg
	 */
	public boolean soleAchieveConclude(String putId, int achieveId) {
		// 全服成就已经有人达成
		if (isSoleAchieveAlreadyConclude(achieveId)) {
			return false;
		}
		
		String retId = soleAchieve.putIfAbsent(achieveId, putId);
		if (retId != null) {
			return false;
		}
		
		LocalRedis.getInstance().updateSoleAchieve(String.valueOf(achieveId), putId);
		return true;
	}
}
