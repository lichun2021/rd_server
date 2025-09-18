package com.hawk.activity.type.impl.allianceCarnival.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

/**
 * 联盟总动员
 * @author golden
 *
 */
@Entity
@Table(name="activity_alliance_carnival")
public class AllianceCarnivalEntity extends HawkDBEntity implements IActivityDataEntity{

	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;
	
    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false, length=50)
	private String playerId;

    @IndexProp(id = 3)
	@Column(name="termId", nullable = false, length=10)
	private int termId;

    @IndexProp(id = 4)
	@Column(name="receiveTimes", nullable = false, length=10)
	private int receiveTimes;
	
    @IndexProp(id = 5)
	@Column(name="initGuildId", nullable = false, length=50)
	private String initGuildId;

    @IndexProp(id = 6)
	@Column(name="initGuildName", nullable = false, length=50)
	private String initGuildName;
	
    @IndexProp(id = 7)
	@Column(name="initCityLevel", nullable = false, length=10)
	private int initCityLevel;
	
    @IndexProp(id = 8)
	@Column(name="buyTimes", nullable = false, length=10)
	private int buyTimes;
	
    @IndexProp(id = 9)
	@Column(name="buyTime", nullable = false, length=10)
	private long buyTime;
	
    @IndexProp(id = 10)
	@Column(name="dayBuyNumber", nullable = false, length=10)
	private int dayBuyNumber;
	
    @IndexProp(id = 11)
	@Column(name="achieveItems", nullable = false, length=1024)
	private String achieveItems;
	
    @IndexProp(id = 12)
	@Column(name="receiveMissionTime", nullable = false, length=19)
	private long receiveMissionTime;
	
    @IndexProp(id = 13)
	@Column(name="finishTimes", nullable = false, length=10)
	private int finishTimes;
	
    @IndexProp(id = 14)
	@Column(name="exp", nullable = false, length=10)
	private int exp;
	
    @IndexProp(id = 15)
	@Column(name="exchangeNumber", nullable = false, length=10)
	private int exchangeNumber;
	
    @IndexProp(id = 16)
	@Column(name="createTime", nullable = false, length=19)
	private long createTime;

    @IndexProp(id = 17)
	@Column(name="updateTime", nullable = false, length=19)
	private long updateTime;

    @IndexProp(id = 18)
	@Column(name="invalid", nullable = false, length=0)
	private boolean invalid;
	
	/**
	 * 已经发过的进阶奖励.
	 */
    @IndexProp(id = 19)
	@Column(name="sendAdvLevel", nullable = false)
	private int sendAdvLevel;
	
	/**
	 * 购买典藏宝箱礼包的时间
	 */
    @IndexProp(id = 20)
	@Column(name="payGiftTime", nullable = false, length=19)
	private long payGiftTime;

	@Transient
	private List<AchieveItem> achieve = new CopyOnWriteArrayList<AchieveItem>();
	
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

	public int getReceiveTimes() {
		return receiveTimes;
	}

	public void setReceiveTimes(int receiveTimes) {
		this.receiveTimes = receiveTimes;
	}

	public String getInitGuildId() {
		return initGuildId;
	}

	public void setInitGuildId(String initGuildId) {
		this.initGuildId = initGuildId;
	}

	public String getAchieveItem() {
		return achieveItems;
	}

	public void setAchieveItem(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public long getReceiveMissionTime() {
		return receiveMissionTime;
	}

	public void setReceiveMissionTime(long receiveMissionTime) {
		this.receiveMissionTime = receiveMissionTime;
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

	public String getInitGuildName() {
		return initGuildName;
	}

	public void setInitGuildName(String initGuildName) {
		this.initGuildName = initGuildName;
	}

	public int getInitCityLevel() {
		return initCityLevel;
	}

	public void setInitCityLevel(int initCityLevel) {
		this.initCityLevel = initCityLevel;
	}

	public int getFinishTimes() {
		return finishTimes;
	}

	public void setFinishTimes(int finishTimes) {
		this.finishTimes = finishTimes;
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	public List<AchieveItem> getAchieve() {
		return achieve;
	}

	public void setAchieve(List<AchieveItem> achieve) {
		this.achieve = achieve;
	}

	public int getBuyTimes() {
		return buyTimes;
	}

	public void setBuyTimes(int buyTimes) {
		this.buyTimes = buyTimes;
	}

	public long getBuyTime() {
		return buyTime;
	}

	public void setBuyTime(long buyTime) {
		this.buyTime = buyTime;
	}

	public int getDayBuyNumber() {
		return dayBuyNumber;
	}

	public void setDayBuyNumber(int dayBuyNumber) {
		this.dayBuyNumber = dayBuyNumber;
	}

	public void addDayBuyNumber(){
		this.dayBuyNumber++;
		notifyUpdate();
	}
	
	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public void addExp(int exp) {
		this.exp += exp;
		notifyUpdate();
	}
	
	/**
	 * 添加成就项
	 */
	public void addAchieve(AchieveItem achieve) {
		this.achieve.add(achieve);
		notifyUpdate();
	}
	
	/**
	 * 清除成就项
	 */
	public void clearAchieve() {
		achieve.clear();
		notifyUpdate();
	}
	
	/**
	 * 增加购买次数
	 */
	public void addBuyTimes() {
		buyTimes++;
		notifyUpdate();
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.achieve, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.achieve.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.achieve);
	}

	public int getSendAdvLevel() {
		return sendAdvLevel;
	}

	public void setSendAdvLevel(int sendAdvLevel) {
		this.sendAdvLevel = sendAdvLevel;
	}

	public int getExchangeNumber() {
		return exchangeNumber;
	}

	public void setExchangeNumber(int exchangeNumber) {
		this.exchangeNumber = exchangeNumber;
	}
	
	public void addExchangeNumber(int value){
		this.exchangeNumber += value;
	}
	
	public long getPayGiftTime() {
		return payGiftTime;
	}

	public void setPayGiftTime(long payGiftTime) {
		this.payGiftTime = payGiftTime;
	}
}
