package com.hawk.activity.type.impl.groupBuy.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_group_buy")
public class GroupBuyEntity extends HawkDBEntity implements IActivityDataEntity{

	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/** 活动成就项数据 */
    @IndexProp(id = 4)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	/** 团购购买礼包记录数据 */
    @IndexProp(id = 5)
	@Column(name = "buyRecord", nullable = false)
	private String buyRecord;
	
	/** 团购购买礼包次数数据 */
    @IndexProp(id = 6)
	@Column(name = "buyTimes", nullable = false)
	private String buyTimes;
	
	/**购买积分数 */
    @IndexProp(id = 7)
	@Column(name = "buyScore", nullable = false)
	private int buyScore;
	
    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	
    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
    
    //TODO 已领取免费积分的热销商品
    @IndexProp(id = 11)
	@Column(name = "hotSellFreeGot", nullable = false)
	private String hotSellFreeGot="";
    
    //TODO 每个商品的购买次数奖励领取情况
    @IndexProp(id = 12)
	@Column(name = "topDiscountRewardGot", nullable = false)
	private String topDiscountRewardGot="";
    
    //已经达到最高折扣的礼包
    @IndexProp(id = 13)
   	@Column(name = "topDiscountGifts", nullable = false)
   	private String topDiscountGifts="";
	
	@Transient
	private List<AchieveItem> itemList;
	
	@Transient
	private List<GroupBuyRecord> buyRecordList = new ArrayList<>();
	
	//TODO 每个商品的购买次数
	@Transient
	private Map<Integer, Integer> buyTimesMap = new HashMap<>();
	
	//已领取免费积分的热销商品
	@Transient
	private List<Integer> hotSellFreeGotList = new ArrayList<>();
	
	//每个商品的购买次数奖励领取情况
	@Transient
	private Map<Integer, List<Integer>> topDiscountRewardGotMap = new HashMap<>();
	
	//已经达到最高折扣的礼包
	@Transient
	private List<Integer> topDiscountGiftList = new ArrayList<>();
	
	
	public GroupBuyEntity() {
		this.itemList = new ArrayList<>();
	}
	
	public GroupBuyEntity(String playerId) {
		this.playerId = playerId;
		this.itemList = new ArrayList<>();
	}
	
	public GroupBuyEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.itemList = new ArrayList<>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.buyRecord = SerializeHelper.collectionToString(this.buyRecordList, SerializeHelper.ELEMENT_DELIMITER);
		this.buyTimes = SerializeHelper.mapToString(this.buyTimesMap);
		
		this.hotSellFreeGot = SerializeHelper.collectionToString(hotSellFreeGotList, "_");
		this.topDiscountGifts = SerializeHelper.collectionToString(topDiscountGiftList, "_");
		StringJoiner sj = new StringJoiner("|");
		for(Entry<Integer, List<Integer>> entry : topDiscountRewardGotMap.entrySet()) {
			String value = SerializeHelper.collectionToString(entry.getValue(), "_");
			sj.add(String.format("%d,%s", entry.getKey(), value));
		}
		this.topDiscountRewardGot = sj.toString();
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.buyRecordList.clear();
		SerializeHelper.stringToList(GroupBuyRecord.class, this.buyRecord, this.buyRecordList);
		this.buyTimesMap = SerializeHelper.stringToMap(buyTimes);
		
		this.hotSellFreeGotList = SerializeHelper.stringToList(Integer.class, this.hotSellFreeGot, "_");
		this.topDiscountGiftList = SerializeHelper.stringToList(Integer.class, this.topDiscountGifts, "_");
		this.topDiscountRewardGotMap = new ConcurrentHashMap<>();
		List<String> list = SerializeHelper.stringToList(String.class, this.topDiscountRewardGot, "\\|");
		for (String str : list) {
			String[] arr = str.split(",");
			topDiscountRewardGotMap.put(Integer.parseInt(arr[0]), SerializeHelper.stringToList(Integer.class, arr[1], "_"));
		}
	}
	
	public List<Integer> getHotSellFreeGotList() {
		return hotSellFreeGotList;
	}

	public Map<Integer, List<Integer>> getTopDiscountRewardGotMap() {
		return topDiscountRewardGotMap;
	}
	
	public List<Integer> getTopDiscountRewardGotList(int giftId) {
		return topDiscountRewardGotMap.getOrDefault(giftId, Collections.emptyList());
	}
	
	public void addTopDiscountRewardGot(int giftId, int buyTimes) {
		List<Integer> gotList = topDiscountRewardGotMap.get(giftId);
		if (gotList == null) {
			gotList = new ArrayList<>();
			topDiscountRewardGotMap.put(giftId, gotList);
		}
		gotList.add(buyTimes);
		this.notifyUpdate();
	}

	public List<Integer> getTopDiscountGiftList() {
		return topDiscountGiftList;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}

	
	public String getBuyRecord() {
		return buyRecord;
	}

	public List<GroupBuyRecord> getBuyRecordList() {
		return buyRecordList;
	}
	
	public void addBuyRecord(GroupBuyRecord groupBuyRecord) {
		if (buyRecordList == null) {
			buyRecordList = new ArrayList<>();
		}
		buyRecordList.add(groupBuyRecord);
		this.notifyUpdate();
	}

	public void setBuyRecordList(List<GroupBuyRecord> buyRecordList) {
		this.buyRecordList = buyRecordList;
	}

	public Map<Integer, Integer> getBuyTimesMap() {
		return buyTimesMap;
	}

	public void setBuyTimesMap(Map<Integer, Integer> buyTimesMap) {
		this.buyTimesMap = buyTimesMap;
	}

	public int getBuyScore() {
		return buyScore;
	}

	public void setBuyScore(int buyScore) {
		this.buyScore = buyScore;
	}
	
	public void buyScoreAdd(int buyScore) {
		this.buyScore += buyScore;
		this.notifyUpdate();
	}

	@Override
	public boolean isInvalid() {
		return this.invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;		
	}		
}
