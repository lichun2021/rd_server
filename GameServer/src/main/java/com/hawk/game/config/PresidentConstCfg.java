package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 系统基础配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.KVResource(file = "xml/president_const.xml")
public class PresidentConstCfg extends HawkConfigBase {
	/**
	 * tick周期(ms)
	 */
	protected final long tickPeriod;
	/**
	 * 初始和平时间(s)
	 */
	protected final int initPeaceTime;
	/**
	 * 初始开始的星期数，只能配1-7
	 */
	protected final int initday;
	/**
	 * 初始开始的时间点，只能配0-23
	 */
	protected final int initTime;
	/**
	 * 初始分钟. 0-59
	 */
	protected final int initMinute;
	/**
	 * 占领首都到成为国王过度时间(s)
	 */
	protected final int occupationTime;
	/**
	 * 战争时期周期(s)
	 */
	protected final int warfareTime;
	/**
	 * 搜索玩家推送个数
	 */
	protected final int searchMaxCount;
	/**
	 * 最大事件数
	 */
	protected final int maxEventCount;
	/**
	 * 历届国王最大记录数
	 */
	protected final int maxHistoryCount;
	/**
	 * 国王禁言时间（秒）
	 */
	protected final int broadCastBanTime;
	/**
	 * 国王征税比例（万分比）
	 */
	protected final int taxPercent;
	/**
	 * 国王起名字长度限制
	 */
	protected final int nameLengthLimit;
	/**
	 * 王国名字和旗帜修改的次数限制
	 */
	protected final int countryModifyTimes;
	/**
	 * 资源设置时间
	 */
	protected final int changeResCd;

	protected final String mailBeforeTime;
	protected int[] mailBeforeTimeArray;

	protected final String mailAfterTime;
	protected int[] mailAfterTimeArray;

	/**
	 * 箭塔伤害
	 */
	protected final int towerAtk;
	
	/**
	 * 资源调整比例
	 */
	protected final float changeResCoe;
	/**
	 * 可任命时间
	 */
	protected final int appointTime;
	/**
	 * buff;
	 */
	private final String globalBuff;
	/**
	 * {buffId, List<ItemInfo>>
	 */
	private Map<Integer, List<ItemInfo>> globalBuffMap;
	/**
	 * 宣言消耗.
	 */
	private final String manifestoCost;
	/**
	 * 宣言消耗
	 */
	private List<ItemInfo> manifestoCostList;
	/**
	 * 宣言的长度.
	 */
	private final int manifestoLength;
	/**
	 * 跨服国王的redis 过期时间.
	 */
	private final int crossKingExpireTime;
	/**
	 * 公告cd时间.
	 */
	private final int noticeCdTime;
	/**
	 * 全局静态对象
	 */
	private static PresidentConstCfg instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static PresidentConstCfg getInstance() {
		return instance;
	}

	public PresidentConstCfg() {

		instance = this;
		tickPeriod = 2000;
		// 8小时
		occupationTime = 0;
		// 一周
		initPeaceTime = 0;
		//
		initday = 0;
		//
		initTime = 0;
		// 一周
		warfareTime = 0;
		// 搜索玩家推送个数
		searchMaxCount = 0;
		// 最大事件数
		maxEventCount = 0;
		// 最大历届国王数
		maxHistoryCount = 0;
		//国王禁言时间（秒）
		broadCastBanTime = 0;
		mailBeforeTime = "";
		mailAfterTime = "";
		//国王征税比例（万分比）
		taxPercent = 0;
		nameLengthLimit = 0;
		countryModifyTimes = 0;
		towerAtk = 0;
		changeResCd = 0;
		changeResCoe = 0f;
		this.initMinute = 0;
		appointTime = 1800;
		globalBuff = "";
		manifestoCost = "";
		manifestoLength = 256;
		this.crossKingExpireTime = 15 * 86400;
		this.noticeCdTime = 3600;
	}

	@Override
	protected boolean assemble() {
		if(initday < 1 || initday > 7){
			return false;
		}
		
		if(initTime < 0 || initTime > 23){
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(mailBeforeTime)) {
			String[] arr = mailBeforeTime.split("_");
			mailBeforeTimeArray = new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				mailBeforeTimeArray[i] = Integer.valueOf(arr[i]);
			}
		}
		if (!HawkOSOperator.isEmptyString(mailAfterTime)) {
			String[] arr = mailAfterTime.split("_");
			mailAfterTimeArray = new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				mailAfterTimeArray[i] = Integer.valueOf(arr[i]);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(globalBuff)) {
			Map<Integer, List<ItemInfo>> buffMap = new HashMap<>();
			String[] itemList = globalBuff.split(SerializeHelper.SEMICOLON_ITEMS);
			for (String item : itemList) {
				String[] buffList = item.split(SerializeHelper.BETWEEN_ITEMS);
				List<ItemInfo> itemInfoList = new ArrayList<>();
				int buffId = 0;
				for (int i = 0; i < buffList.length; i++) {
					if (i == 0) {
						buffId = Integer.parseInt(buffList[i]);						
					} else {
						itemInfoList.add(ItemInfo.valueOf(buffList[i]));
					}
				}
				
				buffMap.put(buffId, itemInfoList);
			}
			
			this.globalBuffMap = buffMap;
		} else {
			this.globalBuffMap = new HashMap<>();
		}
		
		if(!HawkOSOperator.isEmptyString(manifestoCost)) {
			manifestoCostList = ItemInfo.valueListOf(manifestoCost);
		} else {
			manifestoCostList = new ArrayList<>();
		}
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		for (Integer buffId : globalBuffMap.keySet()) {
			if (HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId) == null) {
				throw new InvalidParameterException(String.format("在president_const.xml中配置的buffId%s 在buff.xml中找不到", buffId));
			}
		}
		
		return true;
	}

	public long getTickPeriod() {
		return tickPeriod;
	}

	public int getOccupationTime() {
		return occupationTime;
	}

	public int getInitPeaceTime() {
		return initPeaceTime;
	}

	public int getWarfareTime() {
		return warfareTime;
	}

	public int getSearchMaxCount() {
		return searchMaxCount;
	}

	public int getTaxPercent() {
		return taxPercent;
	}

	public int getMaxEventCount() {
		return maxEventCount;
	}

	public int getMaxHistoryCount() {
		return maxHistoryCount;
	}

	public int getBroadCastBanTime() {
		return broadCastBanTime;
	}

	public int[] getMailBeforeTimeArray() {
		return mailBeforeTimeArray;
	}

	public int[] getMailAfterTimeArray() {
		return mailAfterTimeArray;
	}

	public int getNameLengthLimit() {
		return nameLengthLimit;
	}

	public int getCountryModifyTimes() {
		return countryModifyTimes;
	}

	public int getInitday() {
		return initday;
	}

	public int getInitTime() {
		return initTime;
	}

	public int getTowerAtk() {
		return towerAtk;
	}

	public int getChangeResCd() {
		return changeResCd;
	}

	public float getChangeResCoe() {
		return changeResCoe;
	}

	public int getInitMinute() {
		return initMinute;
	}

	public long getAppointTime() {
		return appointTime * 1000l;
	}

	public Map<Integer, List<ItemInfo>> getGlobalBuffMap() {
		return globalBuffMap;
	}

	public List<ItemInfo> getManifestoCostList() {
		return manifestoCostList;
	}

	public int getManifestoLength() {
		return manifestoLength;
	}

	public int getCrossKingExpireTime() {
		return crossKingExpireTime;
	}

	public int getNoticeCdTime() {
		return noticeCdTime;
	}		
}
