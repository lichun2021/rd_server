package com.hawk.game.module.college.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.MilitaryCollege.CollegeAuth;
import com.hawk.game.service.college.CollegeService;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 军事学院成员信息
 *
 * @author Jesse
 *
 */
@Entity
@Table(name = "college_member")
public class CollegeMemberEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String playerId;

	@Column(name = "collegeId")
	@IndexProp(id = 3)
	private String collegeId;

	@Column(name = "auth")
	@IndexProp(id = 4)
	private int auth;

	@Column(name = "crossResetTime", nullable = false)
	@IndexProp(id = 5)
	private long crossResetTime;

	@Column(name = "quitTime")
	@IndexProp(id = 6)
	private long quitTime = 0;

	@Column(name = "joinTime")
	@IndexProp(id = 7)
	private long joinTime = 0;

	/** 上次被提醒上线的时间 */
	@Column(name = "lastNotifyedTime")
	@IndexProp(id = 8)
	private long lastNotifyedTime = 0;

	/** 今日在线时长 -db记录值,可能存在未刷新的情况*/
	@Column(name = "todayOnlineTime")
	@IndexProp(id = 9)
	private long todayOnlineTime = 0;

	/** 今日已领取在线时长奖励领取信息 */
	@Column(name = "onlineTookInfo")
	@IndexProp(id = 10)
	private String onlineTookInfo = "";

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 11)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 12)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 13)
	protected boolean invalid;
	
	/** 积分信息 */
	@Column(name = "scoreInfo")
	@IndexProp(id = 14)
	private String scoreInfo = "";

	/** 兑换商店-商品信息 */
	@Column(name = "shopInfo")
	@IndexProp(id = 15)
	private String shopInfo = "";
	
	/** 今日已领取在线时长奖励领取信息 */
	@Column(name = "vitInfo")
	@IndexProp(id = 16)
	private String vitInfo = "";
	
	@Column(name = "missionInfo")
	@IndexProp(id = 17)
	private String missionInfo = "";
	
	/** 直购商店-礼包信息 */
	@Column(name = "giftInfo")
	@IndexProp(id = 18)
	private String giftInfo = "";

	/**
	 * 在线时长奖励领取信息
	 */
	@Transient
	private Map<String, List<Integer>> onlineTookMap = new HashMap<>();
	
	/** 商店信息*/
	@Transient
	private CollegeMemberScoreEntity scoreData = new CollegeMemberScoreEntity();
	
	/** 兑换商店-商品信息*/
	@Transient
	private Map<Integer, CollegeMemberShopEntity> shopDataMap = new HashMap<>();
	/** 直购商店-礼包信息*/
	@Transient
	CollegeMemberGiftEntity giftData = new CollegeMemberGiftEntity();
	/** 商店商品刷新时间 */
	@Transient
	private Map<Integer, Long> refreshTimeMap = new HashMap<>();
	
	@Transient
	private List<CollegeMissionEntityItem> missionList = new ArrayList<>();
	
	//@Transient 这个拿到Redis去存了 为了保证跨服的时候  也能发放体力
	//private CollegeMemberVitEntity vitData = new CollegeMemberVitEntity();
	

	public CollegeMemberEntity() {
	}

	@Override
	public void beforeWrite() {
		if (onlineTookMap.isEmpty()) {
			this.onlineTookInfo = "";
		}else{
			JSONObject jsonObject = new JSONObject();
			for(Entry<String, List<Integer>> entry : onlineTookMap.entrySet()){
				jsonObject.put(entry.getKey(), entry.getValue());
			}
			this.onlineTookInfo = jsonObject.toJSONString();
		}
		//积分数据
		this.scoreInfo = this.scoreData.serializ();
		//兑换商店-商品信息序列化
		this.shopInfo = SerializeHelper.collectionToString(this.shopDataMap.values(), SerializeHelper.ELEMENT_DELIMITER);
		//直购商店-礼包信息序列化
		this.giftInfo  = this.giftData.serializ();
		//体力发放数据
		//this.vitInfo = this.vitData.serializ();
		//任务数据
		this.missionInfo = SerializeHelper.collectionToString(this.missionList, SerializeHelper.ELEMENT_DELIMITER);
		super.beforeWrite();
	}
	
	@Override
	public void afterRead() {
		onlineTookMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(this.onlineTookInfo)) {
			JSONObject jsonObject = JSONObject.parseObject(this.onlineTookInfo);
			for (Entry<String, Object> entry : jsonObject.entrySet()) {
				JSONArray array = (JSONArray) entry.getValue();
				List<Integer> list = new ArrayList<>();
				for (Object obj : array) {
					list.add((Integer) obj);
				}
				onlineTookMap.put(entry.getKey(), list);
			}
		}
		
		//积分数据
		CollegeMemberScoreEntity scoreDataTemp = new CollegeMemberScoreEntity();
		if (!HawkOSOperator.isEmptyString(this.scoreInfo)) {
			scoreDataTemp.mergeFrom(this.scoreInfo);
		}
		this.scoreData = scoreDataTemp;
		
		//兑换商店-商品信息反序列化
		if (!HawkOSOperator.isEmptyString(this.shopInfo)) {
			this.shopDataMap = new HashMap<>();
			List<CollegeMemberShopEntity> shopItemList = new ArrayList<>();
			SerializeHelper.stringToList(CollegeMemberShopEntity.class, this.shopInfo, shopItemList);
			shopItemList.forEach(e -> shopDataMap.put(e.getId(), e));
		}
		
		//直购商店-礼包信息反序列化
		CollegeMemberGiftEntity giftDataTemp = new CollegeMemberGiftEntity(); 
		if (!HawkOSOperator.isEmptyString(this.giftInfo)) {
			giftDataTemp.mergeFrom(this.giftInfo);
		}
		this.giftData = giftDataTemp;
//		//体力发放数据
//		CollegeMemberVitEntity vitDataTemp = new CollegeMemberVitEntity();
//		if (!HawkOSOperator.isEmptyString(vitInfo)) {
//			vitDataTemp.mergeFrom(this.vitInfo);
//		}
//		this.vitData = vitDataTemp;
		//任务数据
		this.missionList.clear();
		SerializeHelper.stringToList(CollegeMissionEntityItem.class, this.missionInfo, this.missionList);
		super.afterRead();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getCollegeId() {
		return collegeId;
	}

	public void setCollegeId(String collegeId) {
		this.collegeId = collegeId;
	}

	public int getAuth() {
		return auth;
	}

	public void setAuth(int auth) {
		this.auth = auth;
	}

	public long getCrossResetTime() {
		return crossResetTime;
	}

	public void setCrossResetTime(long crossResetTime) {
		this.crossResetTime = crossResetTime;
	}

	public long getQuitTime() {
		return quitTime;
	}

	public void setQuitTime(long quitTime) {
		this.quitTime = quitTime;
	}

	public long getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(long joinTime) {
		this.joinTime = joinTime;
	}

	public long getLastNotifyedTime() {
		return lastNotifyedTime;
	}

	public void setLastNotifyedTime(long lastNotifyedTime) {
		this.lastNotifyedTime = lastNotifyedTime;
	}

	/**
	 * 获取今日在线时长(DB记录值,可能存在未重置的情况)
	 * 
	 * @return
	 */
	public long getTodayOnlineTime() {
		return this.todayOnlineTime;
	}

	public void setTodayOnlineTime(long todayOnlineTime) {
		this.todayOnlineTime = todayOnlineTime;
	}

	public String getOnlineTookInfo() {
		return onlineTookInfo;
	}

	public void setOnlineTookInfo(String onlineTookInfo) {
		this.onlineTookInfo = onlineTookInfo;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public Map<String, List<Integer>> getOnlineTookMap() {
		return onlineTookMap;
	}

	public void setOnlineTookMap(Map<String, List<Integer>> onlineTookMap) {
		this.onlineTookMap = onlineTookMap;
	}
	
	public CollegeMemberScoreEntity getScoreData() {
		return scoreData;
	}
	
	public Map<Integer, CollegeMemberShopEntity> getShopDataMap() {
		return shopDataMap;
	}
	
	public CollegeMemberGiftEntity getGiftData() {
		return giftData;
	}
	
	public Map<Integer, Long> getRefreshTimeMap() {
		return refreshTimeMap;
	}
	
	public List<CollegeMissionEntityItem> getMissionList() {
		return missionList;
	}
	
//	public CollegeMemberVitEntity getVitData() {
//		return vitData;
//	}
	
	/**
	 * 获取今日在线时长(实际值)
	 * @return
	 */
	public long getOnlineTimeToday() {
		if (HawkTime.isSameDay(HawkTime.getMillisecond(), this.crossResetTime)) {
			return this.todayOnlineTime;
		}
		return 0;
	}
	
	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}
	
	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}

	public String getOwnerKey() {
		return playerId;
	}

	/**
	 * 遍历添加在线时长
	 */
	public void increaceOnlineTime() {
		this.todayOnlineTime += 10000;
		notifyUpdate();
	}

	/**
	 * 跨天重置奖励领取信息以及在线时长
	 */
	public void resetOnlineTime() {
		this.onlineTookInfo = "";
		this.onlineTookMap = new HashMap<>();
		this.todayOnlineTime = 0;
		this.crossResetTime = HawkTime.getMillisecond();
		notifyUpdate();
	}

	/**
	 * 退出学院
	 */
	public void quit() {
		this.auth = CollegeAuth.NOJOIN_VALUE;
		this.collegeId = "";
		long now = HawkTime.getMillisecond();
		this.todayOnlineTime = 0;
		this.quitTime = now;
		this.crossResetTime = now;
		this.scoreData.setWeekScore(0);
		notifyUpdate();
	}

	/**
	 * 加入学院
	 * 
	 * @param collegeId
	 * @param isCreate
	 */
	public void join(String collegeId, boolean isCreate) {
		if (isCreate) {
			this.auth = CollegeAuth.COACH_VALUE;
		} else {
			this.auth = CollegeAuth.TRAINEE_VALUE;
		}
		this.collegeId = collegeId;
		long now = HawkTime.getMillisecond();
		this.todayOnlineTime = 0;
		this.joinTime = now;
		this.crossResetTime = now;
		
		//初始化兑换商店的商品
		CollegeService.getInstance().updateExchangeShopData(this, now);
		//初始化直购商店数据
		CollegeService.getInstance().updateGiftShopData(this, now);
		//更新
		notifyUpdate();
	}
	
	public CollegeMissionEntityItem getMission(int cfgId) {
		for (CollegeMissionEntityItem mission : missionList) {
			if (mission.getCfgId() == cfgId) {
				return mission;
			}
		}
		return null;
	}
}
