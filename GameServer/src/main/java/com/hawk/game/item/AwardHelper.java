package com.hawk.game.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import java.util.Set;

import com.hawk.game.entity.EquipEntity;
import com.hawk.game.invoker.PlayerLevelUpRewardInvoker;
import com.hawk.game.invoker.VipLevelUpRewardInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Equip.HPEquipInfo;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;

public class AwardHelper {
	/**
	 * 道具ID集合
	 */
	private Set<String> itemUuids;
	/**
	 * 各兵种数量
	 */
	private Map<Integer, Integer> soldierCountMap;
	/**
	 * 装备列表
	 */
	private List<EquipEntity> equipList;
	/**
	 * 增加的vip经验值总数
	 */
	private int changeVipExp;
	
	/**
	 * 添加道具
	 * @param uuid
	 */
	public void addItem(String uuid) {
		if (itemUuids == null) {
			itemUuids = new HashSet<>();
		}
		
		itemUuids.add(uuid);
	}
	
	/**
	 * 增加vip经验值
	 * @param vipExp
	 */
	public void addVipExp(int vipExp) {
		changeVipExp += vipExp;
	}
	
	/**
	 * 添加士兵
	 * @param armyId
	 * @param count
	 */
	public void addSoldier(int armyId, int count) {
		if (soldierCountMap == null) {
			soldierCountMap = new HashMap<>();
		}
		
		if (soldierCountMap.containsKey(armyId)) {
			soldierCountMap.put(armyId, soldierCountMap.get(armyId) + count);
		} else {
			soldierCountMap.put(armyId, count);
		}
	}
	
	/**
	 * 添加装备
	 * @param entity
	 */
	public void addEquip(List<EquipEntity> equipEntities) {
		if (equipList == null) {
			equipList = new ArrayList<>();
		}
		
		equipList.addAll(equipEntities);
	}
	
	/**
	 * 非玩家基本属性类奖励信息同步
	 * 
	 * @param player
	 */
	public void syncAward(Player player) {
		if (itemUuids != null) {
			player.getPush().syncItemInfo(itemUuids.toArray(new String[itemUuids.size()]));
		}
		
		if (soldierCountMap != null) {
			player.refreshPowerElectric(PowerChangeReason.AWARD_SOLDIER);
	     	Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	     	for (Entry<Integer, Integer> entry : soldierCountMap.entrySet()) {
	     		map.put(entry.getKey(), entry.getValue());
	     	}
			player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT, map);
		}
		
		if (equipList != null) {
			HPEquipInfo.Builder builder = HPEquipInfo.newBuilder();
			for (EquipEntity equipEntity : equipList) {
				builder.addEquipInfo(BuilderUtil.genEquipInfoBuilder(equipEntity));
			}
			player.sendProtocol(HawkProtocol.valueOf(HP.code.EQUIP_ADD_SYNC_S, builder));
		}
	}
	
	/**
	 * 玩家升级检测奖励
	 * 
	 * @param player
	 * @param oldLevel
	 */
	protected void playerLevelUpReward(Player player, int oldLevel) {
		if (player.getLevel() <= oldLevel) {
			return;
		}

		player.dealMsg(MsgId.PLAYER_LEVELUP_REWARD, new PlayerLevelUpRewardInvoker(player, oldLevel));
	}

	/**
	 * vip经验值变化
	 * 
	 * @param player
	 * @param oldLevel
	 * @param action 
	 * @param beforeVipExp 
	 */
	protected void vipLevelUpAward(Player player, int oldLevel, int beforeVipExp, Action action) {
		if (changeVipExp == 0) {
			return;
		}
		
		// vip等级提升后，vip礼包发放，解锁英雄
		if (player.getVipLevel() > oldLevel) {
			player.dealMsg(MsgId.VIP_LEVELUP_REWARD, new VipLevelUpRewardInvoker(player, oldLevel));
		}
		
		try {
			player.getSuperVipObject().superVipLevelUp();
			player.getSuperVipObject().addMonthSuperVipScore(changeVipExp);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// vip变更log
		LogUtil.logVipExpFlow(player, oldLevel, beforeVipExp, player.getVipExp(), changeVipExp, action);
		
		// 防止重复调用产生重复记录
		changeVipExp = 0;
	}
}
