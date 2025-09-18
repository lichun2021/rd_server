package com.hawk.activity.type.impl.honourHeroBefell.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

@Entity
@Table(name="activity_honour_hero_befell")
public class HonourHeroBefellEntity extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "freeLotteryCount", nullable = false)
	private String freeLotteryCount;
	
    @IndexProp(id = 5)
	@Column(name = "oneLotteryCount", nullable = false)
	private String oneLotteryCount;
	
    @IndexProp(id = 6)
	@Column(name = "tenLotteryCount", nullable = false)
	private String tenLotteryCount;
	
	
	/** 关注的兑换id列表 **/
    @IndexProp(id = 7)
	@Column(name = "playerPoint", nullable = false)
	private String playerPoint;
	
    @IndexProp(id = 8)
	@Column(name = "exchangeMsg", nullable = false)
	private String exchangeMsg;
	
	/** 活动成就项数据 */
    @IndexProp(id = 9)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
	
	@Transient
	private Map<Integer, Integer> freeLotteryCountMap = new ConcurrentHashMap<>();
	
	@Transient
	private Map<Integer, Integer> oneLotteryCountMap = new ConcurrentHashMap<>();
	
	@Transient
	private Map<Integer, Integer> tenLotteryCountMap = new ConcurrentHashMap<>();
	
	@Transient
	private List<Integer> playerPoints = new CopyOnWriteArrayList<Integer>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();
	
	
	public HonourHeroBefellEntity() {
	}
	
	public HonourHeroBefellEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.freeLotteryCount = "";
		this.oneLotteryCount = "";
		this.tenLotteryCount = "";
		this.achieveItems = "";
		this.playerPoint = "";
		this.exchangeMsg= "";
		
	}
	
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.playerPoint = SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT);
		this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
		this.freeLotteryCount = SerializeHelper.mapToString(freeLotteryCountMap);
		this.oneLotteryCount = SerializeHelper.mapToString(oneLotteryCountMap);
		this.tenLotteryCount = SerializeHelper.mapToString(tenLotteryCountMap);
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		playerPoints = SerializeHelper.cfgStr2List(playerPoint, SerializeHelper.ATTRIBUTE_SPLIT);
		exchangeNumMap = SerializeHelper.stringToMap(exchangeMsg, Integer.class, Integer.class);
		freeLotteryCountMap = SerializeHelper.stringToMap(freeLotteryCount, Integer.class, Integer.class);
		oneLotteryCountMap = SerializeHelper.stringToMap(oneLotteryCount, Integer.class, Integer.class);
		tenLotteryCountMap = SerializeHelper.stringToMap(tenLotteryCount, Integer.class, Integer.class);
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
	
	
	public int getUseFreeLotteryCountToday(){
		int day = HawkTime.getYearDay();
		return this.freeLotteryCountMap.getOrDefault(day, 0);
	}
	
	
	public void addUseFreeLotteryCountToday(){
		int day = HawkTime.getYearDay();
		int count = this.freeLotteryCountMap.getOrDefault(day, 0);
		count ++;
		this.freeLotteryCountMap.put(day, count);
		this.notifyUpdate();
	}
	
	public int getOneLotteryCountToday(){
		int day = HawkTime.getYearDay();
		return this.oneLotteryCountMap.getOrDefault(day, 0);
	}
	
	public void addOneLotteryCount(){
		int day = HawkTime.getYearDay();
		int count = this.oneLotteryCountMap.getOrDefault(day, 0);
		count ++;
		this.oneLotteryCountMap.put(day, count);
		this.notifyUpdate();
	}
	
	public void addTenLotteryCount(){
		int day = HawkTime.getYearDay();
		int count = this.tenLotteryCountMap.getOrDefault(day, 0);
		count ++;
		this.tenLotteryCountMap.put(day, count);
		this.notifyUpdate();
	}
	
	public int getTotalLotteryCount(){
		int count = 0;
		for(Map.Entry<Integer, Integer > entry: this.oneLotteryCountMap.entrySet()) {
			count += entry.getValue();
        }
		for(Map.Entry<Integer, Integer > entry: this.tenLotteryCountMap.entrySet()) {
			count += entry.getValue() *10;
        }
		return count;
	}
	
	public int getTotalFreeLotteryCount(){
		int count = 0;
		for(Map.Entry<Integer, Integer > entry: this.freeLotteryCountMap.entrySet()) {
			count += entry.getValue();
        }
		return count;
	}
	
	
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}

	
	public List<Integer> getPlayerPoints() {
		return playerPoints;
	}
	
	
	public void addTips(int id){
		if(!playerPoints.contains(id)){
			playerPoints.add(id);
		}
		this.notifyUpdate();
	}
	
	public void removeTips(int id){
		playerPoints.remove(new Integer(id));
		this.notifyUpdate();
	}
	
	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}
	
	
	public int getExchangeCount(int exchangeId){
		return this.exchangeNumMap.getOrDefault(exchangeId,0);
	}
	
	public void addExchangeCount(int eid,int count){
		if(count <=0){
			return;
		}
		count += this.getExchangeCount(eid);
		this.exchangeNumMap.put(eid, count);
		this.notifyUpdate();
	}

}
