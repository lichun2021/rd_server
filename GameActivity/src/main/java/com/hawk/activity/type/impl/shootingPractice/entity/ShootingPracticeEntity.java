package com.hawk.activity.type.impl.shootingPractice.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_shooting_practice")
public class ShootingPracticeEntity extends ActivityDataEntity implements IExchangeTipEntity{
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
	@Column(name = "buyCount", nullable = false)
    private int buyCount;
    
    @IndexProp(id = 5)
	@Column(name = "buyCountDaily", nullable = false)
    private int buyCountDaily;
    
    @IndexProp(id = 6)
    @Column(name = "freeCount", nullable = false)
    private int freeCount;
    
    @IndexProp(id = 7)
	@Column(name = "scoreMax", nullable = false)
    private int scoreMax;
    
    @IndexProp(id = 8)
	@Column(name = "scoreTotal", nullable = false)
    private int scoreTotal;
    
    @IndexProp(id = 9)
	@Column(name = "lastOverTime", nullable = false)
    private long lastOverTime;
    
	/** 活动成就项数据 */
    @IndexProp(id = 10)
	@Column(name = "achieveItemsDay", nullable = false)
	private String achieveItemsDay;
    
	/** 活动成就项数据 */
    @IndexProp(id = 11)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
    
    @IndexProp(id = 12)
	@Column(name = "tips", nullable = false)
	private String tips;
    
    @IndexProp(id = 13)
    @Column(name = "exchangeMsg", nullable = false)
    private String exchangeMsg;
    
    
    @IndexProp(id = 14)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;
    
    @IndexProp(id = 15)
    @Column(name = "initTime", nullable = false)
    private long initTime;
    
    
    @IndexProp(id = 16)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 17)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 18)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
    
    
    
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private List<AchieveItem> itemListDay = new CopyOnWriteArrayList<AchieveItem>();
    
	
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();
	
	
	
    public ShootingPracticeEntity(){
    	
    	
    }

    public ShootingPracticeEntity(String playerId, int termId) {
    	this.playerId = playerId;
		this.termId = termId;
		
		this.achieveItemsDay = "";
		this.achieveItems = "";
		this.exchangeMsg = "";
		this.loginDays = "";
    }
    
    @Override
    public void beforeWrite() {
    	this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
    	this.achieveItemsDay = SerializeHelper.collectionToString(this.itemListDay, SerializeHelper.ELEMENT_DELIMITER);
		
    	this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
    	this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
    	
    }
    
    @Override
    public void afterRead() {
    	this.itemList.clear();
    	this.itemListDay.clear();
    	SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
    	SerializeHelper.stringToList(AchieveItem.class, this.achieveItemsDay, this.itemListDay);
    	
    	SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
    	this.exchangeNumMap = SerializeHelper.stringToMap(this.exchangeMsg, Integer.class, Integer.class);

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
    public long getCreateTime() {
        return this.createTime;
    }

    @Override
    protected void setCreateTime(long createTime) {
    	this.createTime = createTime;
    }

    @Override
    public long getUpdateTime() {
        return this.updateTime;
    }

    @Override
    protected void setUpdateTime(long updateTime) {
    	this.updateTime = updateTime;
    }

    @Override
    public boolean isInvalid() {
        return this.invalid;
    }

    @Override
    protected void setInvalid(boolean invalid) {
    	this.invalid = invalid;
    }
    
    
    @Override
    public void setLoginDaysStr(String loginDays) {
        this.loginDays = loginDays;
    }
    
    @Override
	public String getLoginDaysStr() {
		return this.loginDays;
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
	
	public int getFreeCount() {
		return freeCount;
	}
	
	public void setFreeCount(int freeCount) {
		this.freeCount = freeCount;
	}
	
	
	public int getBuyCount() {
		return buyCount;
	}
	
	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}
	
	
	public int getBuyCountDaily() {
		return buyCountDaily;
	}
	
	public void setBuyCountDaily(int buyCountDaily) {
		this.buyCountDaily = buyCountDaily;
	}
	
	
	public int getScoreMax() {
		return scoreMax;
	}
	public void setScoreMax(int scoreMax) {
		this.scoreMax = scoreMax;
	}
	
	public int getScoreTotal() {
		return scoreTotal;
	}
	
	public void setScoreTotal(int scoreTotal) {
		this.scoreTotal = scoreTotal;
	}
	
	public long getLastOverTime() {
		return lastOverTime;
	}
	
	public void setLastOverTime(long lastOverTime) {
		this.lastOverTime = lastOverTime;
	}
	
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}

	public List<AchieveItem> getItemListDay() {
		return itemListDay;
	}
	
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}

	public void setItemListDay(List<AchieveItem> itemListDay) {
		this.itemListDay = itemListDay;
	}

	public List<AchieveItem> getAllAchieveList(){
		List<AchieveItem> list = new ArrayList<>();
		list.addAll(this.itemListDay);
		list.addAll(this.itemList);
		return list;
	}
    
	public void resetItemListDay(List<AchieveItem> itemList){
		this.itemListDay = itemList;
		this.notifyUpdate();
	}
	
	public void resetItemList(List<AchieveItem> itemList){
		this.itemList = itemList;
		this.notifyUpdate();
	}


    
    public Map<Integer, Integer> getExchangeNumMap() {
        return exchangeNumMap;
    }


    public int getExchangeCount(int exchangeId) {
        return this.exchangeNumMap.getOrDefault(exchangeId, 0);
    }

    public void addExchangeCount(int eid, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getExchangeCount(eid);
        this.exchangeNumMap.put(eid, count);
        this.notifyUpdate();
    }
    
    

	@Override
	public Set<Integer> getTipSet() {
		return this.tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tips) {
		this.tipSet = tips;
	}
    
}
