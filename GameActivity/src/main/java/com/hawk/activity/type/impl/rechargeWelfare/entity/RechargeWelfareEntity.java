package com.hawk.activity.type.impl.rechargeWelfare.entity;

import java.util.ArrayList;
import java.util.List;
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
@Table(name="activity_recharge_welfare")
public class RechargeWelfareEntity extends HawkDBEntity implements IActivityDataEntity{

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
	
    @IndexProp(id = 5)
	@Column(name = "freeTimes", nullable = false)
	private int freeTimes;
	
    @IndexProp(id = 6)
	@Column(name = "itemset", nullable = false)
	private String itemset;
	
	//总的点券数量
    @IndexProp(id = 7)
	@Column(name = "totalCoupon", nullable = false)
	private int totalCoupon;
	//已领取点券数量
    @IndexProp(id = 8)
	@Column(name = "receiveCoupon", nullable = false)
	private int receiveCoupon;
	
	//每日任务积分数
    @IndexProp(id = 9)
	@Column(name = "dailyScore", nullable = false)
	private int dailyScore;
	
	//当日满足积分是否已经免费领取
    @IndexProp(id = 10)
	@Column(name = "isFreeRec", nullable = false)
	private boolean isFreeRec;
	
	//抽奖次数
    @IndexProp(id = 11)
	@Column(name = "lotteryTimes", nullable = false)
	private int lotteryTimes;
	
	//抽奖次数
    @IndexProp(id = 12)
	@Column(name = "dailyLotteryTimes", nullable = false)
	private int dailyLotteryTimes;
	
	//总的充值金条数量
    @IndexProp(id = 13)
	@Column(name = "totalDiamond", nullable = false)
	private int totalDiamond;
	//剩余的可累计的充值钻石数
    @IndexProp(id = 14)
	@Column(name = "receiveDiamond", nullable = false)
	private int receiveDiamond;
	
    @IndexProp(id = 15)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 16)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	
    @IndexProp(id = 17)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<Integer> itemsetList = new ArrayList<>();
	
	@Transient
	private List<AchieveItem> itemList;
	
	public RechargeWelfareEntity() {
		this.itemList = new ArrayList<>();
	}
	
	public RechargeWelfareEntity(String playerId) {
		this.playerId = playerId;
		this.itemset = "";
		this.itemList = new ArrayList<>();
	}
	
	public RechargeWelfareEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.itemset = "";
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

	public String getStoreInfo() {
		return itemset;
	}

	public void setStoreInfo(String storeInfo) {
		this.itemset = storeInfo;
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
	
	public void setFreeTimes(int val){
		this.freeTimes = val;
	}

	public int getFreeTimes() {
		return freeTimes;
	}

	public int getTotalCoupon() {
		return totalCoupon;
	}

	public void setTotalCoupon(int totalCoupon) {
		this.totalCoupon = totalCoupon;
	}

	public int getReceiveCoupon() {
		return receiveCoupon;
	}

	public void setReceiveCoupon(int receiveCoupon) {
		this.receiveCoupon = receiveCoupon;
	}

	public int getDailyScore() {
		return dailyScore;
	}

	public void setDailyScore(int dailyScore) {
		this.dailyScore = dailyScore;
	}

	public boolean isFreeRec() {
		return isFreeRec;
	}

	public void setFreeRec(boolean isFreeRec) {
		this.isFreeRec = isFreeRec;
	}


	public int getLotteryTimes() {
		return lotteryTimes;
	}

	public void setLotteryTimes(int lotteryTimes) {
		this.lotteryTimes = lotteryTimes;
	}


	public int getDailyLotteryTimes() {
		return dailyLotteryTimes;
	}

	public void setDailyLotteryTimes(int dailyLotteryTimes) {
		this.dailyLotteryTimes = dailyLotteryTimes;
	}

	public int getTotalDiamond() {
		return totalDiamond;
	}

	public void setTotalDiamond(int totalDiamond) {
		this.totalDiamond = totalDiamond;
	}

	public int getReceiveDiamond() {
		return receiveDiamond;
	}

	public void setReceiveDiamond(int receiveDiamond) {
		this.receiveDiamond = receiveDiamond;
	}

	@Override
	public void beforeWrite() {
		this.itemset = SerializeHelper.collectionToString(itemsetList);
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		this.itemsetList = SerializeHelper.stringToList(Integer.class, itemset);
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
	}
	
	
	public String getItemset() {
		return itemset;
	}

	public void setItemset(String itemset) {
		this.itemset = itemset;
	}

	public List<Integer> getItemsetList() {
		return itemsetList;
	}

	public void setItemsetList(List<Integer> itemsetList) {
		this.itemsetList = itemsetList;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
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
