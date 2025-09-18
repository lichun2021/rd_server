package com.hawk.activity.type.impl.commandAcademySimplify.entity;

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

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 指挥官学院实体数据
 * @author huangfei -> lating
 *
 */
@Entity
@Table(name = "activity_cmmoand_academy_simplify")
public class CommandAcademySimplifyEntity extends AchieveActivityEntity implements IActivityDataEntity {
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
	
	/** 活动阶段 */
    @IndexProp(id = 4)
	@Column(name = "stage", nullable = false)
	private int stage;

	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	/** 礼包购买列表*/
    @IndexProp(id = 6)
	@Column(name = "giftList", nullable = false)
	private String giftList;
	
	/** 排名列表*/
    @IndexProp(id = 7)
	@Column(name = "rankIndex", nullable = false)
	private String rankIndex;
	
    @IndexProp(id = 8)
	@Column(name = "stageParam", nullable = false)
	private String stageParam;
		
    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;


	@Transient
	private Map<Integer,Integer> buyGiftMap = new ConcurrentHashMap<Integer,Integer>();
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private Map<Integer,Integer> rankIndexMap = new ConcurrentHashMap<Integer,Integer>();
	
	@Transient
	private Map<Integer,Integer> stageParamMap = new ConcurrentHashMap<Integer,Integer>();
	

	
	public CommandAcademySimplifyEntity() {
	}

	public CommandAcademySimplifyEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.stage = 0;
	}

	/**
	 * 添加礼包购买
	 * @param type
	 */
	public void addBuyGift(int giftId){
		int value = 1;
		if(this.buyGiftMap.containsKey(giftId)){
			value += this.buyGiftMap.get(giftId);
		}
		this.buyGiftMap.put(giftId,value);
		this.notifyUpdate();
	}
	
	/**
	 * 是否已经分购买过
	 * @param type
	 * @return
	 */
	public int getBuyTimes(int giftId){
		if(!this.buyGiftMap.containsKey(giftId)){
			return 0;
		}
		return this.buyGiftMap.get(giftId);
	}
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.giftList = SerializeHelper.mapToString(this.buyGiftMap);
		this.stageParam = SerializeHelper.mapToString(this.stageParamMap);
		this.rankIndex = SerializeHelper.mapToString(this.rankIndexMap);
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToMap(this.giftList, Integer.class, Integer.class, this.buyGiftMap);
		SerializeHelper.stringToMap(this.stageParam, Integer.class, Integer.class, this.stageParamMap);
		SerializeHelper.stringToMap(this.rankIndex, Integer.class, Integer.class, this.rankIndexMap);
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

	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public String getGiftList() {
		return giftList;
	}

	public void setGiftList(String giftList) {
		this.giftList = giftList;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	@Override
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public boolean isBuyGift(){
		return this.buyGiftMap.size()>0;
	}

	public void clearBuyList(){
		this.buyGiftMap.clear();
		this.notifyUpdate();
	}
	
	public int getStageParam(int stage){
		if(this.stageParamMap.containsKey(stage)){
			return stageParamMap.get(stage);
		}
		return 0;
	}
	
	public void setStageParam(int stage,int val){
		this.stageParamMap.put(stage, val);
	}

	public Map<Integer, Integer> getBuyGiftMap() {
		return buyGiftMap;
	}

	public void setBuyGiftMap(Map<Integer, Integer> buyGiftMap) {
		this.buyGiftMap = buyGiftMap;
	}
	
	public Integer getRankIndex(int stageId){
		return this.rankIndexMap.get(stageId);
	}
	
	public void setRankIndex(int stageId,int index){
		this.rankIndexMap.put(stageId, index);
	}
}
