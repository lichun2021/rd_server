package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.GsConfig;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.KVResource(file = "xml/star_wars_const.xml")
public class StarWarsConstCfg extends HawkConfigBase {
	
	/**
	 * 总开关
	 */
	private final int isSystemClose;
	
	/**
	 * 匹配准备时间(单位:秒)
	 */
	private final int matchPrepareTime;
	
	/**
	 *  战场开启时间(单位:秒)
	 */
	private final int warOpenTime;
	
	/**
	 * 提前发送邀请及跑马灯时间(距离报名结束的小时数)
	 */
	private final int inviteFromtHour;
	
	/**
	 * 匹配竞争锁有效期(单位:秒)
	 */
	private final int matchLockExpire;
	
	/**
	 * 联赛排行数量
	 */
	private final int powerRankSize;
	
	/**
	 * 赛区入围联盟数量限制
	 */
	private final int joinGuildCnt;
	
	/**
	 * 每个联盟同时入场人数限制
	 */
	private final int memberCntLimit;
	
	/**
	 * 战场服信息
	 */
	private final String roomServer;
	
	/**
	 * 战场开启前邮件通知时间(秒)
	 */
	private final String warOpenNoticeTimes;
	
	/**
	 * 国王记录
	 */
	private final int maxHistoryCount;
	/**
	 * 礼包的最大记录
	 */
	private final int maxGiftHistoryCount;
	/**
	 * 祈祷的效果
	 */
	private final String blessEffect;
	/**
	 * 玩家通过请求官职的信息获取战争是否打完.
	 */
	private final int loadOfficerCdTime;
	/**
	 * 当前等待登陆队列有多少人就提示等待.
	 */
	private final int maxWaitLoginingPlayer;	
	/**
	 * 祈祷的效果map
	 */
	private Map<Integer, Integer> blessEffectMap = new HashMap<>();
	/**
	 * 效果数组.
	 */
	private EffType[] effTypes;
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
	 * 加持服务器的cd时间.单位(秒)
	 */
	private final int blessTime;
	/**
	 * 跨服玩家结构过期时间.
	 */
	private final int crossPlayerStructExpireTime;
	/**
	 * 最小的等待时间
	 */
	private final int minWaitTime;
	/**
	 * 最大的等待时间.
	 */
	private final int maxWaitTime;
	/**
	 * 强制签回时间.
	 */
	private final int forceMoveBackTime;
	/**
	 * 周期时间.
	 */
	private final int perioTime;
	/***
	 * 任命有效时间.
	 */
	private final int appointTime; 	
	/**
	 * 第1轮胜利奖励
	 */
	private final String swWinReward1;
	/**
	 * 第1轮失败奖励
	 */
	private final String swLostReward1;
	/**
	 * 第2轮胜利奖励
	 */
	private final String swWinReward2;
	/**
	 * 第2轮失败奖励
	 */
	private final String swLostReward2;
	/**
	 * 第3轮胜利奖励
	 */
	private final String swWinReward3;
	/**
	 * 第3轮失败奖励
	 */
	private final String swLostReward3;
	/**
	 * 霸主专属奖励
	 */
	private final String swKingReward;
	/**
	 * 三军统帅专属奖励
	 */
	private final String swGenerReward;
	/**
	 * 清理时间.
	 */
	private final int swClearTime;	
	
	/**
	 * 计算结果的服务器ID
	 */
	private final String calServer;
	

	private List<ItemInfo> swWinReward1List;
	
	private List<ItemInfo> swLostReward1List;
	
	private List<ItemInfo> swWinReward2List;
	
	private List<ItemInfo> swLostReward2List;
	
	private List<ItemInfo> swWinReward3List;
	
	private List<ItemInfo> swLostReward3List;
	
	private List<ItemInfo> swKingRewardList;
	
	private List<ItemInfo> swGenerRewardList;
	
	private List<Long> warOpenNoticeTimeList;
	

	/**
	 * 各大区战场服服务器id
	 */
	private Map<String, String> roomServerMap = new HashMap<>();
	
	private Map<String, String> calServerMap = new HashMap<>();
	
	private static StarWarsConstCfg instance = null;
	
	public static StarWarsConstCfg getInstance() {
		return instance;
	}
	
	public StarWarsConstCfg() {
		this.isSystemClose = 0;
		this.matchPrepareTime = 300;
		this.warOpenTime = 3600;
		this.inviteFromtHour = 12;
		this.matchLockExpire = 120;
		this.powerRankSize = 100;
		this.joinGuildCnt = 10;
		this.memberCntLimit = 60;
		this.maxHistoryCount = 100;
		maxGiftHistoryCount = 100;
		this.blessEffect = "";
		this.manifestoCost = "";
		this.manifestoLength = 200;
		this.blessTime = 0;
		this.crossPlayerStructExpireTime = 86400;
		this.minWaitTime = 1000;
		this.maxWaitTime = 5000;
		this.forceMoveBackTime = 30000;
		this.roomServer = "";
		this.perioTime = 10_000;
		this.appointTime = 4 * 3600;
		this.swWinReward1 = "";
		this.swLostReward1 = "";
		this.swWinReward2 = "";
		this.swLostReward2 = "";
		this.swWinReward3 = "";
		this.swLostReward3 = "";
		this.swKingReward = "";
		this.swGenerReward = "";
		this.swClearTime = 3 * 86400;
		this.warOpenNoticeTimes = "";
		this.loadOfficerCdTime = 300;
		this.maxWaitLoginingPlayer = 200;
		this.calServer = "";
		instance = this;
	}

	public boolean isSystemClose() {
		return isSystemClose == 1;
	}
	
	public long getMatchPrepareTime() {
		return matchPrepareTime * 1000l;
	}

	public final long getWarOpenTime() {
		return warOpenTime * 1000l;
	}
	
	public int getInviteFromtHour() {
		return inviteFromtHour;
	}

	public int getMatchLockExpire() {
		return matchLockExpire;
	}
	
	public int getPowerRankSize() {
		return powerRankSize;
	}

	public int getJoinGuildCnt() {
		return joinGuildCnt;
	}

	public int getMemberCntLimit() {
		return memberCntLimit;
	}

	public int getMaxHistoryCount() {
		return maxHistoryCount;
	}

	public int getMaxGiftHistoryCount() {
		return maxGiftHistoryCount;
	}
	
	public String getRoomServer(String area){
		return roomServerMap.get(area);
	}
	

	public String getCalServer(String area){
		return calServerMap.get(area);
	}
	
	/**
	 * 
	 */
	public boolean assemble() {
		effTypes = new EffType[0];
		Map<Integer, Integer> blessEffectMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(blessEffect)) {
			String[] array = blessEffect.split(",");
			for (String val : array) {
				String[] eff = val.split("_");
				if (eff == null || eff.length != 2) {
					return false;
				}
				blessEffectMap.put(Integer.valueOf(eff[0]), Integer.valueOf(eff[1]));
			}

			int i = 0;
			Set<Integer> effIdSet = blessEffectMap.keySet();
			effTypes = new EffType[blessEffectMap.size()];
			for (int effId : effIdSet) {
				effTypes[i++] = EffType.valueOf(effId);
			}
			
			this.blessEffectMap = blessEffectMap;
		}
		
		if(!HawkOSOperator.isEmptyString(manifestoCost)) {
			manifestoCostList = ItemInfo.valueListOf(manifestoCost);
		} else {
			manifestoCostList = new ArrayList<>();
		}
		Map<String, String> roomMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(roomServer)) {
			String[] areaStr = roomServer.split(",");
			for (String str : areaStr) {
				String[] serverStr = str.split("_");
				roomMap.put(serverStr[0], serverStr[1]);
			}
			this.roomServerMap = roomMap; 
		}
		this.swWinReward1List = ItemInfo.valueListOf(swWinReward1);
		this.swLostReward1List = ItemInfo.valueListOf(swLostReward1);
		this.swWinReward2List = ItemInfo.valueListOf(swWinReward2);
		this.swLostReward2List = ItemInfo.valueListOf(swLostReward2);
		this.swWinReward3List = ItemInfo.valueListOf(swWinReward3);
		this.swLostReward3List = ItemInfo.valueListOf(swLostReward3);
		this.swKingRewardList = ItemInfo.valueListOf(swKingReward);
		this.swGenerRewardList = ItemInfo.valueListOf(swGenerReward);
		List<Long> noticeTimeList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(warOpenNoticeTimes)) {
			String[] timesStr = warOpenNoticeTimes.split(",");
			for (String timeStr : timesStr) {
				noticeTimeList.add(Long.valueOf(timeStr) * 1000);
			}
		}
		this.warOpenNoticeTimeList = noticeTimeList;
		
		Map<String, String> calMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(calServer)) {
			String[] areaStr = calServer.split(",");
			for (String str : areaStr) {
				String[] serverStr = str.split("_");
				calMap.put(serverStr[0], serverStr[1]);
			}
			this.calServerMap = calMap; 
		}
		return true;
	}
	
	
	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}
	
	
	public List<ItemInfo> getManifestoCostList() {
		return manifestoCostList;
	}

	public int getManifestoLength() {
		return manifestoLength;
	}

	public Map<Integer, Integer> getBlessEffectMap() {
		return blessEffectMap;
	}

	public EffType[] getEffTypes() {
		return effTypes;
	}

	public int getCrossPlayerStructExpireTime() {
		return crossPlayerStructExpireTime;
	}

	public int getMinWaitTime() {
		return minWaitTime;
	}

	public int getMaxWaitTime() {
		return maxWaitTime;
	}

	public int getForceMoveBackTime() {
		return forceMoveBackTime;
	}

	public int getBlessTime() {
		return blessTime;
	}

	public int getPerioTime() {
		return perioTime;
	}
	
	public int getAppointTime() {
		return appointTime;
	}

	public List<ItemInfo> getSwWinReward1List() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : swWinReward1List) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public List<ItemInfo> getSwLostReward1List() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : swLostReward1List) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public List<ItemInfo> getSwWinReward2List() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : swWinReward2List) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public List<ItemInfo> getSwLostReward2List() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : swLostReward2List) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public List<ItemInfo> getSwWinReward3List() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : swWinReward3List) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public List<ItemInfo> getSwLostReward3List() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : swLostReward3List) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public List<ItemInfo> getSwKingRewardList() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : swKingRewardList) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public List<ItemInfo> getSwGenerRewardList() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : swGenerRewardList) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public Map<String, String> getRoomServerMap() {
		return roomServerMap;
	}
	
	public int getSwClearTime() {
		return swClearTime;
	}

	public List<Long> getWarOpenNoticeTimeList() {
		List<Long> copy = new ArrayList<>();
		copy.addAll(warOpenNoticeTimeList);
		return copy;
	}
	public int getLoadOfficerCdTime() {
		return loadOfficerCdTime;
	}
	
	public int getMaxWaitLoginingPlayer() {
		return maxWaitLoginingPlayer;
	}

	public Map<String, String> getCalServerMap() {
		return calServerMap;
	}

	
	
}	
