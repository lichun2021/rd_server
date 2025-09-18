package com.hawk.activity.type.impl.loginfundtwo.entity;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录基金2
 * @author hf
 */
@Entity
@Table(name = "activity_loginfund_two")
public class LoginFundTwoEntity extends ActivityDataEntity {

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
	
    @IndexProp(id = 4)
	@Column(name = "loginDays", nullable = false)
	private String loginDays;
	
	/**购买登录基金信息*/
    @IndexProp(id = 5)
	@Column(name = "buyInfo", unique = true, nullable = false)
	private String buyInfo;

	/** 开启后主堡等级记录 */
    @IndexProp(id = 6)
	@Column(name = "facLv", nullable = false)
	private int facLv;

	/** 活动成就项数据 */
    @IndexProp(id = 7)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList;

	@Transient
	private Map<Integer, Integer> buyInfoMap = new HashMap<>();

	public LoginFundTwoEntity() {
		this.itemList = new ArrayList<>();
	}
	

	public LoginFundTwoEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
		this.buyInfo = "";
		this.loginDays = "";
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

	@Override
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getFacLv() {
		return facLv;
	}

	public void setFacLv(int facLv) {
		this.facLv = facLv;
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
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.buyInfo = SerializeHelper.mapToString(this.buyInfoMap);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.buyInfoMap = SerializeHelper.stringToMap(buyInfo);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	@Override
	public void setLoginDaysStr(String loginDays) {
		this.loginDays = loginDays;
	}

	@Override
	public String getLoginDaysStr() {
		return loginDays;
	}

	public Map<Integer, Integer> getBuyInfoMap() {
		return buyInfoMap;
	}

	/**
	 * 是否购买过该类型
	 * @param type
	 * @return
	 */
	public boolean isHasBuy(int type){
		return buyInfoMap.containsKey(type);
	}

	/**
	 * 任何类型是否有购买
	 * @return
	 */
	public boolean isHasBuyAnyType(){
		Set<Integer> keySet = buyInfoMap.keySet();
		boolean isHasAny = !keySet.isEmpty();
		return isHasAny;
	}

	public boolean isHasAllBuy(){
		return buyInfoMap.keySet().size() == 3;
	}

	public void addBuyType(int type){
		int dayInt = HawkTime.getYyyyMMddIntVal();
		buyInfoMap.put(type, dayInt);
		notifyUpdate();
	}


	@Override
	public void recordLoginDay() {
		boolean isHasBuyAny = isHasBuyAnyType();
		if(isHasBuyAny){
			super.recordLoginDay();
		}
	}
	
	
}
