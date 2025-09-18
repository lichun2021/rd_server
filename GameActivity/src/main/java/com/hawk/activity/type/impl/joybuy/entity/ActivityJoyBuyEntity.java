package com.hawk.activity.type.impl.joybuy.entity;

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
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 累计登录活动数据存储
 * @author PhilChen
 *
 */
@Entity
@Table(name = "activity_joy_buy")
public class ActivityJoyBuyEntity extends HawkDBEntity implements IActivityDataEntity{
	
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

	//兑换刷新次数
    @IndexProp(id = 4)
	@Column(name = "exchangeRefreshNum", nullable = false)
	private int exchangeRefreshNum;
	
	//兑换刷新下次时间点
    @IndexProp(id = 5)
	@Column(name = "exchangeNextTime", nullable = false)
	private long exchangeNextTime;
	
	//兑换列表
    @IndexProp(id = 6)
	@Column(name = "exchangeList", nullable = false)
	private String exchangeList;
	
	//兑换列表
    @IndexProp(id = 7)
	@Column(name = "exchangeNumber", nullable = false)
	private int exchangeNumber;
	
	/** 活动成就项数据 */
    @IndexProp(id = 8)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 9)
	@Column(name = "loginRefreshTime", nullable = false)
	private long loginRefreshTime;

    @IndexProp(id = 10)
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	@Transient
	private List<JoyBuyExchangeItem> exchangeObjectList = new CopyOnWriteArrayList<JoyBuyExchangeItem>();

	public ActivityJoyBuyEntity() {
	}
	
	public ActivityJoyBuyEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.exchangeRefreshNum = 0;
		this.exchangeNextTime = 0;
		this.exchangeList="";
		this.achieveItems = "";
		this.loginRefreshTime= HawkTime.getMillisecond();
		this.loginDays=1;
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


	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
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
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	public int getExchangeNumber() {
		return exchangeNumber;
	}

	public void setExchangeNumber(int exchangeNumber) {
		this.exchangeNumber = exchangeNumber;
	}

	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public int getExchangeRefreshNum() {
		return exchangeRefreshNum;
	}

	public void setExchangeRefreshNum(int exchangeRefreshNum) {
		this.exchangeRefreshNum = exchangeRefreshNum;
	}

	public long getExchangeNextTime() {
		return exchangeNextTime;
	}

	public void setExchangeNextTime(long exchangeNextTime) {
		this.exchangeNextTime = exchangeNextTime;
	}

	public String getExchangeList() {
		return exchangeList;
	}

	public void setExchangeList(String exchangeList) {
		this.exchangeList = exchangeList;
	}

	public List<JoyBuyExchangeItem> getExchangeObjectList() {
		return exchangeObjectList;
	}

	public void setExchangeObjectList(List<JoyBuyExchangeItem> exchangeObjectList) {
		this.exchangeObjectList = exchangeObjectList;
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.exchangeList = SerializeHelper.collectionToString(this.exchangeObjectList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.exchangeObjectList.clear();
		SerializeHelper.stringToList(JoyBuyExchangeItem.class, this.exchangeList, this.exchangeObjectList);
	}
	
	public long getLoginRefreshTime() {
		return loginRefreshTime;
	}

	public void setLoginRefreshTime(long loginRefreshTime) {
		this.loginRefreshTime = loginRefreshTime;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
