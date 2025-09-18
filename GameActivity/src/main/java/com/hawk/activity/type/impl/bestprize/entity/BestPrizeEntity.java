package com.hawk.activity.type.impl.bestprize.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_best_prize")
public class BestPrizeEntity extends HawkDBEntity implements IActivityDataEntity, IExchangeTipEntity{
	
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false)
	private String playerId;

    @IndexProp(id = 3)
	@Column(name="termId", nullable = false)
	private int termId;
    
    @IndexProp(id = 4)
    @Column(name="dayTime", nullable = false)
	private long dayTime;
    
    /** 成就数据 **/
    @IndexProp(id = 5)
	@Column(name="achieveItems", nullable = false)
	private String achieveItems;
    
    /** 商店已购买次数信息 */
    @IndexProp(id = 6)
   	@Column(name = "shopItems", nullable = false)
   	private String shopItems = "";
    
    /** 积分兑换次数信息 */
    @IndexProp(id = 7)
   	@Column(name = "exchangeItems", nullable = false)
   	private String exchangeItems = "";

    @IndexProp(id = 8)
   	@Column(name = "tips", nullable = false)
   	private String tips = "";
    
    @IndexProp(id = 9)
    @Column(name="drawConsume", nullable = false)
	private int drawConsume;
    
	@IndexProp(id = 10)
    @Column(name="createTime", nullable = false)
   	private long createTime;

    @IndexProp(id = 11)
    @Column(name="updateTime", nullable = false)
  	private long updateTime;
    
    @IndexProp(id = 12)
	@Column(name="invalid", nullable = false)
	private boolean invalid;
    
    /** 每个大奖池抽到最终奖或一等奖的数量 */
    @IndexProp(id = 13)
   	@Column(name = "bigPoolDrawInfo", nullable = false)
   	private String bigPoolDrawInfo = "";
    
    
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

	/** 商店商品购买数量  */
	@Transient
	private Map<Integer, Integer> shopItemMap = new HashMap<>();
	
	@Transient
	private Map<Integer, Integer> exchangeItemMap = new HashMap<>();
	
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	/** 每个大奖池抽到最终奖或一等奖的数量  */
	@Transient
	private Map<Integer, Integer> bigPoolDrawMap = new HashMap<>();
	
	public BestPrizeEntity(){}
	
	public BestPrizeEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.shopItems = SerializeHelper.mapToString(this.shopItemMap);
		this.exchangeItems = SerializeHelper.mapToString(this.exchangeItemMap);
		this.tips = SerializeHelper.collectionToString(this.tipSet, SerializeHelper.ATTRIBUTE_SPLIT);
		this.bigPoolDrawInfo = SerializeHelper.mapToString(this.bigPoolDrawMap);
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		this.tipSet.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.shopItemMap = SerializeHelper.stringToMap(this.shopItems, Integer.class, Integer.class);
		this.exchangeItemMap = SerializeHelper.stringToMap(this.exchangeItems, Integer.class, Integer.class);
		SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
		this.bigPoolDrawMap = SerializeHelper.stringToMap(this.bigPoolDrawInfo, Integer.class, Integer.class);
	}

	@Override
	public String getPrimaryKey() {
		return id;
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

	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	public Map<Integer, Integer> getShopItemMap() {
		return shopItemMap;
	}
	
	public Map<Integer, Integer> getExchangeItemMap() {
		return exchangeItemMap;
	}
	
	public String getBigPoolDrawInfo() {
		return bigPoolDrawInfo;
	}

	public void setBigPoolDrawInfo(String bigPoolDrawInfo) {
		this.bigPoolDrawInfo = bigPoolDrawInfo;
	}
	
	public Map<Integer, Integer> getBigPoolDrawMap() {
		return bigPoolDrawMap;
	}

	public void setBigPoolDrawMap(Map<Integer, Integer> bigPoolDrawMap) {
		this.bigPoolDrawMap = bigPoolDrawMap;
	}
	
	public int getBigPoolDrawCount(int bigPool) {
		return bigPoolDrawMap.getOrDefault(bigPool, 0);
	}
	
	public void addBigPoolDrawCount(int bigPool, int count) {
		int oldCount = bigPoolDrawMap.getOrDefault(bigPool, 0);
		bigPoolDrawMap.put(bigPool, oldCount+count);
		this.notifyUpdate();
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
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList.clear();
		this.itemList.addAll(itemList);
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	public void resetItemList(List<AchieveItem> newAchieves) {
		this.itemList.clear();
		this.itemList.addAll(newAchieves);
		this.notifyUpdate();
	}

	public long getDayTime() {
		return dayTime;
	}

	public void setDayTime(long dayTime) {
		this.dayTime = dayTime;
	}

	@Override
	public Set<Integer> getTipSet() {
		return this.tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tips) {
		this.tipSet = tips;
	}

	public int getDrawConsume() {
		return drawConsume;
	}

	public void setDrawConsume(int drawConsume) {
		this.drawConsume = drawConsume;
	}
}
