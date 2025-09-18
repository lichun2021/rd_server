package com.hawk.game.lianmengstarwars.player;

import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.protocol.Const.EffType;

public class SWPlayerData extends ISWPlayerData {
	private ISWPlayerEffect playerEffect;

	private SWPlayerData(SWPlayer parent) {
		super(parent);
	}

	public static SWPlayerData valueOf(SWPlayer player) {
		PlayerData source = player.getSource().getData();
		SWPlayerData result = new SWPlayerData(player);
		result.playerId = source.getPlayerId();
		result.dataCache = source.getDataCache();
		// effect 对象
		result.playerEffect = new SWPlayerEffect(result);
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
	public SWPlayer getParent() {
		return (SWPlayer) super.getParent();
	}

	

}
