package com.hawk.game.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.AllianceOfficialCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GuildAuthority;
import com.hawk.game.util.GsConst.GuildOffice;

public class GuildMemberObject {

	GuildMemberEntity entity = null;

	public GuildMemberObject(GuildMemberEntity entity) {
		if (entity == null) {
			this.entity = new GuildMemberEntity();
		} else {
			this.entity = entity;
		}
	}

	public String getPlayerId() {
		return entity.getPlayerId();
	}

	public String getPlayerName() {
		return entity.getPlayerName();
	}

	public long getPower() {
		return entity.getPower();
	}
	
	public long getNoArmyPower() {
		return entity.getNoArmyPower();
	}

	public long getKillCount() {
		return entity.getKillCount();
	}

	public String getGuildId() {
		return entity.getGuildId();
	}

	public int getAuthority() {
		int auth = entity.getAuthority();
		int officeId = entity.getOfficeId();
		if(officeId != GuildOffice.NONE.value() && officeId != GuildOffice.LEADER.value()){
			auth = GuildAuthority.L14_VALUE;
		}
		return auth;
	}

	public long getQuitGuildTime() {
		return entity.getQuitGuildTime();
	}
	
	public long getJoinGuildTime() {
		return entity.getJoinGuildTime();
	}

	public int getNormalDonateTimes() {
		return entity.getNormalDonateTimes();
	}
	
	public int getCrystalDonateTimes() {
		return entity.getCrystalDonateTimes();
	}
	
	public int getDonateResetTimes() {
		return entity.getDonateResetTimes();
	}
	
	public long getNextDonateAddTime() {
		return entity.getNextDonateAddTime();
	}

	public int getDonateDayOfYear() {
		return entity.getDonateDayOfYear();
	}
	
	public int getJoinGuildTimes() {
		return entity.getJoinGuildTimes();
	}

	public int getManorUnlockTimes() {
		return entity.getManorUnlockTimes();
	}
	
	/**
	 * 获取联盟官职id
	 * @return
	 */
	public int getOfficeId() {
		return entity.getOfficeId();
	}
	
	/**
	 * 获取今日已领取任务奖励列表
	 * @return
	 */
	public List<Integer> getRewardedTaskList(){
		return entity.getRewardedTaskList();
	}
	
	/**
	 * 获取上次签到时间
	 * @return
	 */
	public long getLastSingTime(){
		return entity.getLastSingTime();
	}
	
	/**
	 * 获取签到次数
	 * @return
	 */
	public int getSignTimes(){
		return entity.getSignTimes();
	}

	public synchronized boolean create(String playerId, String guildId, String playerName, int authority, long power, long noArmyPower) {

		entity.setPlayerId(playerId);
		entity.setGuildId(guildId);
		entity.setPlayerName(playerName);
		entity.setAuthority(authority);
		entity.setPower(power);
		entity.setNoArmyPower(noArmyPower);
		entity.setRewaredTaskIds("");
		entity.resetRewardedTask();
		GuildConstProperty property = GuildConstProperty.getInstance();
		int initDonateCnt = property.getFirstJoinAllianceDonateNumber();
		entity.setNormalDonateTimes(property.getResourceDonateNumber() - initDonateCnt);
		entity.setNextDonateAddTime(HawkTime.getMillisecond() + property.getResourceDonateTime() * 1000);
		// 创建联盟时不对盟主做入盟时间限制
		if(authority != GuildAuthority.L5_VALUE){
			entity.setJoinGuildTime(HawkTime.getMillisecond());
		}
		if (!HawkDBManager.getInstance().create(entity)) {
			return false;
		}
		return true;
	}

	public synchronized boolean updateMemberAuthority(int authority) {
		entity.setAuthority(authority);
		return true;
	}
	
	public void quitGuild() {
		entity.setAuthority(Const.GuildAuthority.L0_VALUE);
		entity.setOfficeId(GuildOffice.NONE.value());
		entity.setGuildId(null);
	}

	public synchronized boolean joinGuild(String guildId, String playerName, long power, long noArmyPower, int authority) {
		if (!HawkOSOperator.isEmptyString(entity.getGuildId())) {
			return false;
		}
		entity.setGuildId(guildId);
		entity.setPlayerName(playerName);
		entity.setPower(power);
		entity.setNoArmyPower(noArmyPower);
		entity.setAuthority(authority);
		// 创建联盟时不对盟主做入盟时间限制
		if (authority != GuildAuthority.L5_VALUE) {
			entity.setJoinGuildTime(HawkTime.getMillisecond());
		}
		else{
			updateOfficeId(GuildOffice.LEADER.value());
			entity.setJoinGuildTime(0);
		}
		GuildConstProperty property = GuildConstProperty.getInstance();
		if (entity.getQuitGuildTime() == 0) {
			int initDonateCnt = property.getFirstJoinAllianceDonateNumber();
			entity.setNormalDonateTimes(property.getResourceDonateNumber() - initDonateCnt);
			entity.setNextDonateAddTime(HawkTime.getMillisecond() + property.getResourceDonateTime() * 1000);
		}
//		else{
//			entity.setNormalDonateTimes(property.getResourceDonateNumber());
//		}
		
		return true;
	}

	public void updateMemberPower(long power) {
		entity.setPower(power);
	}
	
	public void updateMemberNoArmyPower(long noArmyPower) {
		entity.setNoArmyPower(noArmyPower);
	}

	public void updateMemberKillCount(long killCount) {
		entity.setKillCount(killCount);
	}

	public void updateMemberPlayerName(String playerName) {
		entity.setPlayerName(playerName);
	}

	public void updateMemberQuitGuildTime(long time) {
		entity.setQuitGuildTime(time);
	}

	/**
	 * 设置普通捐献次数 
	 * @param normalDonateTimes
	 */
	public void setNormalDonateTimes(int normalDonateTimes) {
		entity.setNormalDonateTimes(normalDonateTimes);
	}
	
	/**
	 * 设置钻石捐献次数 
	 * @param normalDonateTimes
	 */
	public void setDiamondDonateTimes(int crystalDonateTimes) {
		entity.setCrystalDonateTimes(crystalDonateTimes);
	}
	
	/**
	 * 设置钻石捐献次数 
	 * @param normalDonateTimes
	 */
	public void setDonateResetTimes(int donateResetTimes) {
		entity.setDonateResetTimes(donateResetTimes);
	}
	
	/**
	 * 下次普通捐献次数恢复时间 
	 * @param normalDonateTimes
	 */
	public void setNextDonateAddTime(long nextDonateAddTime) {
		entity.setNextDonateAddTime(nextDonateAddTime);
	}

	/**
	 * 联盟捐献相关刷新日期 
	 * @param normalDonateTimes
	 */
	public void setDonateDayOfYear(int donateDayOfYear) {
		entity.setDonateDayOfYear(donateDayOfYear);
	}
	
	/** 上次刷新大礼包 */
	public long getLastRefrashBigGift() {
		return entity.getLastRefrashBigGift();
	}

	public void updateLastRefrashBigGift(long milliseconds) {
		entity.setLastRefrashBigGift(milliseconds);
	}

	public void updateJoinGuildTimes(int joinGuildTimes) {
		entity.setJoinGuildTimes(joinGuildTimes);
	}

	public void updateManorUnlockTimes(int manorUnlockTimes) {
		entity.setManorUnlockTimes(manorUnlockTimes);
	}
	
	public void updateOfficeId(int officeId) {
		int oldId = entity.getOfficeId();
		if (oldId == officeId) {
			return;
		}
		entity.setOfficeId(officeId);
		Player player = GlobalData.getInstance().getActivePlayer(entity.getPlayerId());
		// 同步官员相关作用号信息
		if (player != null) {
			AllianceOfficialCfg oldCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceOfficialCfg.class, oldId);
			AllianceOfficialCfg newCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceOfficialCfg.class, officeId);
			List<int[]> effects = new ArrayList<>();
			if(oldCfg != null){
				effects.addAll(oldCfg.getEffects());
			}
			if(newCfg != null){
				effects.addAll(newCfg.getEffects());
			}
			Set<EffType> effectSet = new HashSet<>();
			for (int[] effect : effects) {
				effectSet.add(EffType.valueOf(effect[0]));
			}
			EffType[] types = new EffType[effectSet.size()];
			player.getPush().syncPlayerEffect(effectSet.toArray(types));
		}
	}

	public long getLogoutTime() {
		return entity.getLogoutTime();
	}

	public void setLogoutTime(long logoutTime) {
		entity.setLogoutTime(logoutTime);
	}
	
	/**
	 * 添加今日已领取联盟任务奖励id
	 * @param taskIds
	 */
	public void addRewardedTask(List<Integer> taskIds) {
		if (taskIds == null || !taskIds.isEmpty()) {
			entity.addRewardedTask(taskIds);
		}
	}
	
	public void setLastSingTime(long lastSingTime){
		entity.setLastSingTime(lastSingTime);
	}
	
	public void setSignTimes(int signTimes){
		entity.setSignTimes(signTimes);
	}
	
	public long getTaskResetTime(){
		return entity.getTaskResetTime();
	}
	
	/**
	 * 重置任务已领奖列表
	 */
	public void resetRewardedTask(){
		entity.resetRewardedTask();
	}
	
	
	public long getDragonAwardTime() {
		return this.entity.getDragonAwardTime();
	}
	
	public void setDragonAwardTime(long dragonAwardTime) {
		this.entity.setDragonAwardTime(dragonAwardTime);
	}
}
