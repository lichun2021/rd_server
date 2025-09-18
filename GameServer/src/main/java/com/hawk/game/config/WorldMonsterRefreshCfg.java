package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 普通野怪刷新配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_monster_refresh.xml")
public class WorldMonsterRefreshCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int openServiceTimeLowerLimit;
	protected final int openServiceTimeUpLimit;
	
	/**
	 * 普通野怪刷新数量
	 */
	protected final String monsterCommonNum;
	protected final String monsterSpecialNum;
	protected final String monsterCapitalNum;
	
	/**
	 * 活动野怪刷新数量
	 */
	protected final String actiMonsterCommonNum;
	protected final String actiMonsterSpecialNum;
	protected final String actiMonsterCapitalNum;
	
	/**
	 * 活动期间普通野怪刷新数量
	 */
	protected final String actiNormalMonsterCommonNum;
	protected final String actiNormalMonsterSpecialNum;
	protected final String actiNormalMonsterCapitalNum;
	
	/**
	 * 185活动野怪刷新
	 */
	protected final String acti185MonsterCommonNum;
	protected final String acti185MonsterSpecialNum;
	protected final String acti185MonsterCapitalNum;
	
	// 普通区块刷新：普通怪
	private Map<Integer, Integer> refreshCommon;
	// 特殊区块刷新：普通怪
	private Map<Integer, Integer> refreshSpecial;
	// 首都(黑土地)区块刷新：普通怪
	private Map<Integer, Integer> refreshCapital;
	
	// 普通区块刷新：普通怪
	private Map<Integer, Integer> refreshActiCommon;
	// 特殊区块刷新：普通怪
	private Map<Integer, Integer> refreshActiSpecial;
	// 首都(黑土地)区块刷新：普通怪
	private Map<Integer, Integer> refreshActiCapital;

	// 普通区块刷新：活动期间普通怪
	private Map<Integer, Integer> refreshActiNormalCommon;
	// 特殊区块刷新：活动期间普通怪
	private Map<Integer, Integer> refreshActiNormalSpecial;
	// 首都(黑土地)区块刷新：活动期间普通怪
	private Map<Integer, Integer> refreshActiNormalCapital;
	
	
	
	
	// 普通区块刷新：普通怪
	private Map<Integer, Integer> refreshActiCommon185;
	// 特殊区块刷新：普通怪
	private Map<Integer, Integer> refreshActiSpecial185;
	// 首都(黑土地)区块刷新：普通怪
	private Map<Integer, Integer> refreshActiCapital185;
	
	
	public WorldMonsterRefreshCfg() {
		id = 0;
		openServiceTimeLowerLimit = 0;
		openServiceTimeUpLimit = 0;
		monsterCommonNum = "";
		monsterSpecialNum = "";
		monsterCapitalNum = "";
		actiMonsterCommonNum = "";
		actiMonsterSpecialNum = "";
		actiMonsterCapitalNum = "";
		actiNormalMonsterCommonNum = "";
		actiNormalMonsterSpecialNum = "";
		actiNormalMonsterCapitalNum = "";
		acti185MonsterCommonNum = "";
		acti185MonsterSpecialNum = "";
		acti185MonsterCapitalNum = "";
	}

	public Map<Integer, Integer> getRefreshCommon() {
		return refreshCommon;
	}

	public Map<Integer, Integer> getRefreshSpecial() {
		return refreshSpecial;
	}

	public Map<Integer, Integer> getRefreshCapital() {
		return refreshCapital;
	}

	public Map<Integer, Integer> getRefreshActiCommon() {
		return refreshActiCommon;
	}

	public Map<Integer, Integer> getRefreshActiSpecial() {
		return refreshActiSpecial;
	}

	public Map<Integer, Integer> getRefreshActiCapital() {
		return refreshActiCapital;
	}

	public Map<Integer, Integer> getRefreshActiNormalCommon() {
		return refreshActiNormalCommon;
	}

	public Map<Integer, Integer> getRefreshActiNormalSpecial() {
		return refreshActiNormalSpecial;
	}

	public Map<Integer, Integer> getRefreshActiNormalCapital() {
		return refreshActiNormalCapital;
	}

	public int getId() {
		return id;
	}

	public int getOpenServiceTimeLowerLimit() {
		return openServiceTimeLowerLimit;
	}

	public int getOpenServiceTimeUpLimit() {
		return openServiceTimeUpLimit;
	}

	public String getMonsterCommonNum() {
		return monsterCommonNum;
	}

	public String getMonsterSpecialNum() {
		return monsterSpecialNum;
	}

	public String getMonsterCapitalNum() {
		return monsterCapitalNum;
	}

	public String getActiMonsterCommonNum() {
		return actiMonsterCommonNum;
	}

	public String getActiMonsterSpecialNum() {
		return actiMonsterSpecialNum;
	}

	public String getActiMonsterCapitalNum() {
		return actiMonsterCapitalNum;
	}

	public String getActiNormalMonsterCommonNum() {
		return actiNormalMonsterCommonNum;
	}

	public String getActiNormalMonsterSpecialNum() {
		return actiNormalMonsterSpecialNum;
	}

	public String getActiNormalMonsterCapitalNum() {
		return actiNormalMonsterCapitalNum;
	}
	
	
	public Map<Integer, Integer> getRefreshActiCapital185() {
		return refreshActiCapital185;
	}
	
	public Map<Integer, Integer> getRefreshActiCommon185() {
		return refreshActiCommon185;
	}
	
	public Map<Integer, Integer> getRefreshActiSpecial185() {
		return refreshActiSpecial185;
	}
	
	
	public boolean activityMonster185(int monsterId){
		return this.refreshActiCapital185.containsKey(monsterId) ||
				this.refreshActiCommon185.containsKey(monsterId) ||
				this.refreshActiSpecial185.containsKey(monsterId);
	}
	
	
	public boolean activityMonsterOld(int monsterId){
		return this.refreshActiCapital.containsKey(monsterId) ||
				this.refreshActiCommon.containsKey(monsterId) ||
				this.refreshActiSpecial.containsKey(monsterId);
	}

	@Override
	protected boolean assemble() {
		Map<Integer, Integer> refreshCommon = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(monsterCommonNum)) {
			String[] array = monsterCommonNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshCommon.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshCommon = refreshCommon;
		
		Map<Integer, Integer> refreshSpecial = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(monsterSpecialNum)) {
			String[] array = monsterSpecialNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshSpecial.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshSpecial = refreshSpecial;
		
		Map<Integer, Integer> refreshCapital = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(monsterCapitalNum)) {
			String[] array = monsterCapitalNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshCapital.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshCapital = refreshCapital;
		
		Map<Integer, Integer> refreshActiCommon = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(actiMonsterCommonNum)) {
			String[] array = actiMonsterCommonNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiCommon.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiCommon = refreshActiCommon;
		
		Map<Integer, Integer> refreshActiSpecial = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(actiMonsterSpecialNum)) {
			String[] array = actiMonsterSpecialNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiSpecial.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiSpecial = refreshActiSpecial;
		
		Map<Integer, Integer> refreshActiCapital = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(actiMonsterCapitalNum)) {
			String[] array = actiMonsterCapitalNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiCapital.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiCapital = refreshActiCapital;
		
		Map<Integer, Integer> refreshActiNormalCommon = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(actiNormalMonsterCommonNum)) {
			String[] array = actiNormalMonsterCommonNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiNormalCommon.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiNormalCommon = refreshActiNormalCommon;
		
		Map<Integer, Integer> refreshActiNormalSpecial = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(actiNormalMonsterSpecialNum)) {
			String[] array = actiNormalMonsterSpecialNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiNormalSpecial.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiNormalSpecial = refreshActiNormalSpecial;
		
		Map<Integer, Integer> refreshActiCapitalCommon = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(actiNormalMonsterCapitalNum)) {
			String[] array = actiNormalMonsterCapitalNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiCapitalCommon.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiNormalCapital = refreshActiCapitalCommon;
		
		
		
		
		Map<Integer, Integer> refreshActiCommon185 = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(acti185MonsterCommonNum)) {
			String[] array = acti185MonsterCommonNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiCommon185.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiCommon185 = refreshActiCommon185;
		
		Map<Integer, Integer> refreshActiSpecial185 = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(acti185MonsterSpecialNum)) {
			String[] array = acti185MonsterSpecialNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiSpecial185.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiSpecial185 = refreshActiSpecial185;
		
		Map<Integer, Integer> refreshActiCapital185 = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(acti185MonsterCapitalNum)) {
			String[] array = acti185MonsterCapitalNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshActiCapital185.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshActiCapital185 = refreshActiCapital185;
		
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		return true;
	}
}