package com.hawk.activity.type.impl.guildDragonAttack.cfg;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.common.collect.ImmutableMap;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 成长基金活动全局K-V配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/alliance_boss/alliance_boss_cfg.xml")
public class GuildDragonAttackKVCfg extends HawkConfigBase {
	
	/** 建筑开启时间*/
	private final int unlockTime;
	
	/** 开启冷却*/
	private final int allianceCd;
	
	/** 个人参与冷却*/
	private final int playerCd;
	
	/** 活动开启前不可以更改预约时间*/
	private final int disableTime;

	/** 开放时间*/
	private final int duration;
	
	/** 开放时间段*/
	private final String openTime;
	
	/** 怪物组成*/
	private final String soldier;
	
	private final String damageParam;
	private final double damagePower;
	
	
	private final String playerEffect;
	private final String enemyEffect;
	
	
	private ImmutableMap<Integer, Double> damageParamMap = ImmutableMap.of(); 
	private ImmutableMap<Integer, Integer> playerEffectMap = ImmutableMap.of(); 
	private ImmutableMap<Integer, Integer> enemyEffectMap = ImmutableMap.of(); 
	
	public GuildDragonAttackKVCfg() {
		unlockTime = 0;
		allianceCd = 0;
		playerCd = 0;
		disableTime = 0;
		duration = 0;
		openTime = "";
		soldier = "";
		damageParam = "";
		playerEffect = "";
		enemyEffect = "";
		damagePower = 0d;
	}
	
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(this.damageParam)){
			Map<Integer,Double> map = new HashMap<>();
			String[] arr = this.damageParam.split(SerializeHelper.BETWEEN_ITEMS);
			for(String str : arr){
				String[] param = str.split(SerializeHelper.ATTRIBUTE_SPLIT);
				map.put(Integer.parseInt(param[0]), Double.parseDouble(param[1]));
			}
			this.damageParamMap = ImmutableMap.copyOf(map);
		}
		
		if(!HawkOSOperator.isEmptyString(this.playerEffect)){
			Map<Integer,Integer> map = new HashMap<>();
			String[] arr = this.playerEffect.split(SerializeHelper.BETWEEN_ITEMS);
			for(String str : arr){
				String[] param = str.split(SerializeHelper.ATTRIBUTE_SPLIT);
				map.put(Integer.parseInt(param[0]), Integer.parseInt(param[1]));
			}
			this.playerEffectMap = ImmutableMap.copyOf(map);
		}
		
		if(!HawkOSOperator.isEmptyString(this.enemyEffect)){
			Map<Integer,Integer> map = new HashMap<>();
			String[] arr = this.enemyEffect.split(SerializeHelper.BETWEEN_ITEMS);
			for(String str : arr){
				String[] param = str.split(SerializeHelper.ATTRIBUTE_SPLIT);
				map.put(Integer.parseInt(param[0]), Integer.parseInt(param[1]));
			}
			this.enemyEffectMap = ImmutableMap.copyOf(map);
		}
		return super.assemble();
	}
	

	

	public int getUnlockTime() {
		return unlockTime;
	}
	
	public int getAllianceCd() {
		return allianceCd;
	}
	
	public int getPlayerCd() {
		return playerCd;
	}
	
	public int getDisableTime() {
		return disableTime;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public String getSoldier() {
		return soldier;
	}
	
	public ImmutableMap<Integer, Double> getDamageParamMap() {
		return damageParamMap;
	}
	
	
	public ImmutableMap<Integer, Integer> getEnemyEffectMap() {
		return enemyEffectMap;
	}
	
	public ImmutableMap<Integer, Integer> getPlayerEffectMap() {
		return playerEffectMap;
	}
	
	public double getDamagePower() {
		return damagePower;
	}
	
	public boolean openTimeVertify(long time){
		String[] arr = this.openTime.split(SerializeHelper.ELEMENT_SPLIT);
		long zero = HawkTime.getAM0Date(new Date(time)).getTime();
		long star = zero + Integer.parseInt(arr[0]) * HawkTime.HOUR_MILLI_SECONDS;
		long end = zero + Integer.parseInt(arr[1]) * HawkTime.HOUR_MILLI_SECONDS;
		if(star <=time && time <= end){
			return true;
		}
		return false;
	
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}
}