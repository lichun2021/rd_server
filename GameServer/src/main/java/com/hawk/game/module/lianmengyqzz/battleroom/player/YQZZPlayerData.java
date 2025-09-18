package com.hawk.game.module.lianmengyqzz.battleroom.player;

import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.protocol.Const.EffType;

public class YQZZPlayerData extends IYQZZPlayerData {
	private IYQZZPlayerEffect playerEffect;

	private YQZZPlayerData(YQZZPlayer parent) {
		super(parent);
	}

	public static YQZZPlayerData valueOf(YQZZPlayer player) {
		PlayerData source = player.getSource().getData();
		YQZZPlayerData result = new YQZZPlayerData(player);
		result.playerId = source.getPlayerId();
		result.dataCache = source.getDataCache();
		// effect 对象
		result.playerEffect = new YQZZPlayerEffect(result);
		result.playerEffect.setParent(player);
		result.lockOriginalData();
		return result;
	}
	
	@Override
	public StaffOfficerSkillCollection getStaffOffic(){
		return getSource().getStaffOffic();
	}
	
	@Override
	public int getEffVal(EffType effType) {
		return getPlayerEffect().getEffVal(effType);
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {
		return getPlayerEffect().getEffVal(effType, targetId);
	}

	public void lockOriginalData() {
		// 锁定原始数据
		// source.getDataCache().lockKey(PlayerDataKey.ArmyEntities);
		// source.getDataCache().lockKey(PlayerDataKey.QueueEntities);
		// source.getDataCache().lockKey(PlayerDataKey.StatusDataEntities);
	}

	public void unLockOriginalData() {
		// 锁定原始数据
		// source.getDataCache().unLockKey(PlayerDataKey.ArmyEntities);
		// source.getDataCache().unLockKey(PlayerDataKey.QueueEntities);
		// source.getDataCache().unLockKey(PlayerDataKey.StatusDataEntities);
	}

	public PlayerData getSource() {
		return getParent().getSource().getData();
	}

	@Override
	public PlayerEffect getPlayerEffect() {
		return playerEffect;
	}
	
	@Override
	public PlayerDataCache getDataCache() {
		return getSource().getDataCache();
	}

	@Override
	public YQZZPlayer getParent() {
		return (YQZZPlayer) super.getParent();
	}

	

}
