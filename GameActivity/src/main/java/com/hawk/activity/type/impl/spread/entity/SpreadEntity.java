package com.hawk.activity.type.impl.spread.entity;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;

import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_spread")
public class SpreadEntity extends AchieveActivityEntity implements IActivityDataEntity, IExchangeTipEntity {
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
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 5)
	@Column(name = "shopItems", nullable = false)
	private String shopItems;
	
    @IndexProp(id = 6)
	@Column(name = "hiddenAchieveIds", nullable = false)
	private String hiddenAchieveIds;
	
    @IndexProp(id = 7)
	@Column(name = "canRewardTimes", nullable = false)
	private String canRewardTimes = "";
	
    @IndexProp(id = 8)
	@Column(name = "rewardedTimes", nullable = false)
	private String rewardedTimes;
	
    @IndexProp(id = 9)
	@Column(name = "friends", nullable = false)
	private String friends = "";
	
    @IndexProp(id = 10)
	@Column(name = "dayReward", nullable = false)
	private int dayReward;
	
    @IndexProp(id = 11)
	@Column(name = "isBindCode", nullable = false)
	private int isBindCode;
	
    @IndexProp(id = 12)
	@Column(name = "bindCode", nullable = false)
	private String bindCode;
	
    @IndexProp(id = 13)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 14)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 15)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	/** 兑换提醒信息 */
	@IndexProp(id = 16)
	@Column(name = "tips", nullable = false)
	private String tips;

	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private Map<Integer,Integer> shopItemsMap = new HashMap<>();
	
	@Transient
	private List<Integer> hiddenAchieveIdsList = new ArrayList<Integer>();
	
//	@Transient
//	private Map<Integer,Integer> canRewardTimesMap = new HashMap<>();

	@Transient
	private Map<Integer,Integer> rewardedTimesMap = new HashMap<>();

	/** 兑换提醒 */
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	public SpreadEntity() {
		
	}
	
	public SpreadEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		try{
			if(HawkOSOperator.isEmptyString(this.bindCode)){
				this.bindCode = "";
			}
			this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
			this.shopItems = SerializeHelper.mapToString(shopItemsMap);
//			this.canRewardTimes = SerializeHelper.mapToString(canRewardTimesMap);
			this.rewardedTimes = SerializeHelper.mapToString(rewardedTimesMap);
			this.hiddenAchieveIds = SerializeHelper.collectionToString(this.hiddenAchieveIdsList);
			this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

	@Override
	public void afterRead() {
		try {
			if(HawkOSOperator.isEmptyString(this.bindCode)){
				this.bindCode = "";
			}
			this.itemList.clear();
			SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
			this.shopItemsMap = SerializeHelper.stringToMap(shopItems, Integer.class, Integer.class);
//			this.canRewardTimesMap = SerializeHelper.stringToMap(canRewardTimes, Integer.class, Integer.class);
			this.rewardedTimesMap = SerializeHelper.stringToMap(rewardedTimes, Integer.class, Integer.class);
			this.hiddenAchieveIdsList.clear();
			SerializeHelper.stringToList(Integer.class, this.hiddenAchieveIds, this.hiddenAchieveIdsList);
			SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
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
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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
	
	@Override
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
	public void addShopItems( int id, int count ){
		if(id <=0 || count <= 0){
			return;
		}
		Integer v = this.shopItemsMap.get(id);
		if(null != v){
			v += count;
			this.shopItemsMap.put(id,v);
			return;
		}
		this.shopItemsMap.put(id,count);
	}
	
	public Map<Integer,Integer> getShopItems(){
		return this.shopItemsMap;
	}
	
	public int getShopItemVal(int id){
		Integer v = this.shopItemsMap.get(id);
		if(null == v){
			return 0;
		}
		return v;
	}
	
	public int getDayReward() {
		return dayReward;
	}

	public void setDayReward(int dayReward) {
		this.dayReward = dayReward;
	}
	
	public boolean getIsBindCode() {
		return isBindCode != 0;
	}

	public void setIsBindCode(int isBindCode) {
		this.isBindCode = isBindCode;
	}
	
	public void setBindCode(String bindCode){
		this.bindCode = bindCode;
	}
	
	public String getBindCode(){
		return this.bindCode;
	}
	
	public List<Integer> getHiddenAchieveIds(){
		return this.hiddenAchieveIdsList;
	}
	
	public void addHiddenAchieveId( Integer id ){
		if(!this.hiddenAchieveIdsList.stream().anyMatch(e -> e == id)){
			this.hiddenAchieveIdsList.add(id);
		}
	}
	
	public void addAchieveRewardedTimes( int id, int count ){
		if(id <=0 || count <= 0){
			return;
		}
		Integer v = this.rewardedTimesMap.get(id);
		if(null != v){
			v += count;
			this.rewardedTimesMap.put(id,v);
			return;
		}
		this.rewardedTimesMap.put(id,count);
	}
	
	public void decAchieveRewardedTimes( int id){
		if(id <=0 ){
			return;
		}
		Integer v = this.rewardedTimesMap.get(id);
		if(null != v){
			v = (v > 0) ? (v -1) : 0; 
			this.rewardedTimesMap.put(id,v);
			return;
		}
	}
	
	public Map<Integer,Integer> getAchieveRewardedTimes(){
		return this.rewardedTimesMap;
	}
	
	//已领取次数
	public int getAchieveRewardedTimeById(int id){
		if(id <=0 ){
			return 0;
		}
		
		Integer v = this.rewardedTimesMap.get(id);
		if(null == v){
			return 0;
		}
		return v.intValue();
	}

	public Map<Integer,Integer> getRewardedTimesMap(){
		return 	this.rewardedTimesMap;
	}

	public String getFriends() {
		return friends;
	}

	public void setFriends(String friends) {
		this.friends = friends;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	@Override
	public Set<Integer> getTipSet() {
		return tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tipSet) {
		this.tipSet = tipSet;
	}
}
