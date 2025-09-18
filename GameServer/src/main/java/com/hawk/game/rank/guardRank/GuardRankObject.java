package com.hawk.game.rank.guardRank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hawk.game.config.GuardianConstConfig;
import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Rank.GuardRankInfo;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.BuilderUtil;


/**
 * 守护排行榜, 这个榜单和其它的榜单不一样，所以单独写了.
 * @author jm
 *
 */
public class GuardRankObject {
	
	List<GuardRankInfo> guardRankInfo = new ArrayList<>();
	List<GuardRank> sortList = new ArrayList<>();
	
	
	public void refreshRank() {
		List<GuardRankInfo> newBuilderList = new ArrayList<>();
		GuardRankInfo.Builder builder = null;
		Player player = null;  
		int maxShow = this.getMaxShow();
		int index = 0;
		GlobalData globalData = GlobalData.getInstance();
		for (GuardRank guardRank : sortList) {
			if (index >= maxShow) {
				break;
			}
			index++;
			builder = GuardRankInfo.newBuilder();
			builder.setRankNo(index);
			player = globalData.makesurePlayer(guardRank.getFirstPlayerId());
			if (player == null) {
				continue;
			}
			builder.setFirstPlayerName(player.getName());
			builder.setFirstPlayerId(player.getId());
			builder.setFirstPlayerGuildTag(player.getGuildTag());
			builder.setFirstCommon(BuilderUtil.genPlayerCommonBuilder(player.getData()));
			player = globalData.makesurePlayer(guardRank.getSecondPlayerId());
			if (player == null) {
				continue;
			}
			builder.setSecondPlayerName(player.getName());
			builder.setSecondPlayerId(player.getId());
			builder.setSecondPlayerGuildTag(player.getGuildTag());
			builder.setSecondCommon(BuilderUtil.genPlayerCommonBuilder(player.getData()));
			builder.setGuardValue(guardRank.getGuardValue());
			newBuilderList.add(builder.build());
		}
		
		this.guardRankInfo = newBuilderList;
	}
	
	
	//只有100个，不方.
	public int getRank(String playerId1, String playerId2) {
		String[] idArray = this.sortPlayerId(playerId1, playerId2);
		GuardRank guardRank = null;
		for (int i = 0; i < sortList.size(); i++) {
			guardRank = sortList.get(i);
			if (guardRank.getFirstPlayerId().equals(idArray[0]) && guardRank.getSecondPlayerId().equals(idArray[1])) {
				return i + 1;
			}
		}
		
		return 0;
	}
	
	
	public String getKey(String playerId1, String playerId2) {
		String[] idArray = sortPlayerId(playerId1, playerId2);
		
		return getKey(idArray);
	}
	
	public String getKey(String[] idArray) {
		return idArray[0] + ":" + idArray[1];
	}
	
	public GuardRank getGuardRank(String playerId1, String playerId2) {
		String[] idArray = this.sortPlayerId(playerId1, playerId2);
		GuardRank guardRank = null;
		for (int i = 0; i < sortList.size(); i++) {
			guardRank = sortList.get(i);
			if (guardRank.getFirstPlayerId().equals(idArray[0]) && guardRank.getSecondPlayerId().equals(idArray[1])) {
				return guardRank;
			}
		}
		
		return null;
	}
	
	/**
	 * 这里的数据来源于RelationService,所以在这个初始化之前必须保证relationService已经初始化了.
	 */
	public void init() {
		Map<String, PlayerRelationEntity> guardMap = RelationService.getInstance().getPlayerGuardMap();
		Set<String> distinctSet = new HashSet<>();
		List<GuardRank> rankList = new ArrayList<>();
		for (Entry<String, PlayerRelationEntity> entry : guardMap.entrySet()) {
			PlayerRelationEntity relaiotnEntity = entry.getValue();
			String[] idArray = this.sortPlayerId(entry.getKey(), relaiotnEntity.getTargetPlayerId());
			String key = this.getKey(entry.getKey(), relaiotnEntity.getTargetPlayerId());
			if (distinctSet.contains(key)) {
				continue;
			}
			distinctSet.add(key);
			GuardRank guardRank = new GuardRank(idArray[0], idArray[1], relaiotnEntity.getGuardValue(), relaiotnEntity.getOperationTime());
			rankList.add(guardRank);
		}
		
		Collections.sort(rankList);
		int end = rankList.size() > this.getMaxRank() ? this.getMaxRank() : rankList.size();
		this.sortList.addAll(rankList.subList(0, end));
		this.refreshRank();
	}
	
	/**
	 *  从排行榜里面删除.
	 * @param playerId1
	 * @param playerId2
	 */
	public void deleteRank(String playerId1, String playerId2) {
		GuardRank guardRank = this.getGuardRank(playerId1, playerId2);
		if (guardRank != null) {
			this.sortList.remove(guardRank);
		}
	} 
	/**
	 * 该函数的调用必须保证会在同一个线程,这里没有做处理.
	 * @param playerId1
	 * @param playerId2
	 * @param value
	 */
	public void addRank(String playerId1, String playerId2, int value, long operationTime) {			
		GuardRank guardRank = this.getGuardRank(playerId1, playerId2);
		if (guardRank == null) {
			String[] idArray = this.sortPlayerId(playerId1, playerId2);
			guardRank = new GuardRank(idArray[0], idArray[1], value, operationTime);
			this.sortList.add(guardRank);
		} else {
			guardRank.setGuardValue(value);
			guardRank.setOperationTime(operationTime);
		}
		Collections.sort(sortList);
		//删除最后一个
		if (this.sortList.size() > this.getMaxRank()) {
			this.sortList.remove(sortList.size() - 1); 
		}				
	}
	
	/**
	 * 根据玩家ID的ascii码比较,
	 * 玩家ID是不可能一样的.
	 * @param playerId1
	 * @param playerId2
	 * @return
	 */
	public String[] sortPlayerId(String playerId1, String playerId2) {
		int compareValue = playerId1.compareTo(playerId2);
		if (compareValue > 0) {
			return new String[]{playerId1, playerId2};
		} else {
			return new String[]{playerId2, playerId1};
		}
	}
	
	/**
	 * 最多排多少人
	 * @return
	 */
	public int getMaxRank() {
		return GuardianConstConfig.getInstance().getRankMaxNum(); 
	}
	
	/**
	 * 
	 * 显示在榜上的人
	 * @return
	 */
	public int getMaxShow() {
		return GuardianConstConfig.getInstance().getRankCondition();
	}


	public List<GuardRankInfo> getGuardRankInfo() {
		return guardRankInfo;
	}
}
