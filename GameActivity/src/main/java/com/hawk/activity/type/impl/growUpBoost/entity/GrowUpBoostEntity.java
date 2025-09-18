package com.hawk.activity.type.impl.growUpBoost.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_grow_up_boost")
public class GrowUpBoostEntity extends ActivityDataEntity implements IExchangeTipEntity{
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
   	@Column(name = "useConfig", nullable = false)
    private int useConfig;
    
    
    @IndexProp(id = 5)
	@Column(name = "scoreItemDetailString", nullable = false)
    private String scoreItemDetailString;
    
    @IndexProp(id = 6)
	@Column(name = "scoreString", nullable = false)
    private String scoreString;
    
	/** 活动成就项数据 */
    @IndexProp(id = 7)
	@Column(name = "achieveItemsDay", nullable = false)
	private String achieveItemsDay;
    
	/** 活动成就项数据 */
    @IndexProp(id = 8)
	@Column(name = "achieveItemsScore", nullable = false)
	private String achieveItemsScore;
    
    @IndexProp(id = 9)
	@Column(name = "tips", nullable = false)
	private String tips;
    
    @IndexProp(id = 10)
    @Column(name = "exchangeMsg", nullable = false)
    private String exchangeMsg;
    
    @IndexProp(id = 11)
    @Column(name = "buyMsg", nullable = false)
    private String buyMsg;
    
    @IndexProp(id = 12)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;
    
    
    @IndexProp(id = 13)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 14)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 15)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
    
    
    
	@Transient
	private List<AchieveItem> itemListScore = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private List<AchieveItem> itemListDay = new CopyOnWriteArrayList<AchieveItem>();
    
	@Transient
    private Map<Long,GrowUpBoostScore> scores = new ConcurrentHashMap<>();
	
	@Transient
    private Map<Integer,GrowUpBoostItemScoreDetail> itemScores = new ConcurrentHashMap<>();
	
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();
	
	@Transient
	private Map<Integer, Integer> buyNumMap = new ConcurrentHashMap<>();
	
	
	
    public GrowUpBoostEntity(){
    	
    }

    public GrowUpBoostEntity(String playerId, int termId) {
    	this.playerId = playerId;
		this.termId = termId;
		
		this.achieveItemsDay = "";
		this.achieveItemsScore = "";
		this.scoreString = "";
		this.scoreItemDetailString = "";
		this.exchangeMsg = "";
		this.buyMsg = "";
		this.loginDays = "";
    }
    
    @Override
    public void beforeWrite() {
    	
       
    	this.achieveItemsScore = SerializeHelper.collectionToString(this.itemListScore, SerializeHelper.ELEMENT_DELIMITER);
    	this.achieveItemsDay = SerializeHelper.collectionToString(this.itemListDay, SerializeHelper.ELEMENT_DELIMITER);
		
    	this.scoreString = SerializeHelper.mapToString(this.scores);
    	this.scoreItemDetailString = SerializeHelper.mapToString(this.itemScores);
    	
    	this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
    	this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
    	this.buyMsg = SerializeHelper.mapToString(buyNumMap);
    	
    }
    
    @Override
    public void afterRead() {
    	this.itemListScore = SerializeHelper.stringToList(AchieveItem.class, this.achieveItemsScore);
		this.itemListDay = SerializeHelper.stringToList(AchieveItem.class, this.achieveItemsDay);
    	
    	SerializeHelper.stringToMap(this.scoreString, Long.class, GrowUpBoostScore.class,this.scores);
    	SerializeHelper.stringToMap(this.scoreItemDetailString, Integer.class, GrowUpBoostItemScoreDetail.class,this.itemScores);
		
    	SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
    	this.exchangeNumMap = SerializeHelper.stringToMap(this.exchangeMsg, Integer.class, Integer.class);
    	this.buyNumMap = SerializeHelper.stringToMap(this.buyMsg, Integer.class, Integer.class);

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
	
	

	public int getUseConfig() {
		return useConfig;
	}

	public void setUseConfig(int useConfig) {
		this.useConfig = useConfig;
	}

	public int getScoreTotal() {
		int total = 0;
		for(Map.Entry<Long, GrowUpBoostScore> entry : this.scores.entrySet()){
			GrowUpBoostScore score = entry.getValue();
			total += score.getAchieveScore();
			total += score.getItemScore();
        }
		return total;
	}

	

	public int getScoreItem() {
		int total = 0;
		for(Map.Entry<Long, GrowUpBoostScore> entry : this.scores.entrySet()){
			GrowUpBoostScore score = entry.getValue();
			total += score.getItemScore();
        }
		return total;
	}
	
	
	public int getScoreAchieveTotal() {
		int total = 0;
		for(Map.Entry<Long, GrowUpBoostScore> entry : this.scores.entrySet()){
			GrowUpBoostScore score = entry.getValue();
			total += score.getAchieveScore();
        }
		return total;
	}
	

	
	public int getScoreTotalTody(){
    	long time = HawkTime.getAM0Date().getTime();
    	GrowUpBoostScore record = this.scores.get(time);
    	if(Objects.isNull(record)){
    		return 0;
    	}
    	return record.getAchieveScore() + record.getItemScore();
    }
	
	
	public int getScoreAchieveToday(){
    	long time = HawkTime.getAM0Date().getTime();
    	GrowUpBoostScore record = this.scores.get(time);
    	if(Objects.isNull(record)){
    		return 0;
    	}
    	return record.getAchieveScore();
    }
	
	

	
	
	
	public Map<Long, GrowUpBoostScore> getScores() {
		return scores;
	}
	
	

	public Map<Integer, GrowUpBoostItemScoreDetail> getItemScores() {
		return itemScores;
	}

	
	public List<AchieveItem> getItemListScore() {
		return itemListScore;
	}

	public void setItemListScore(List<AchieveItem> itemListScore) {
		this.itemListScore = itemListScore;
	}

	public List<AchieveItem> getItemListDay() {
		return itemListDay;
	}

	public void setItemListDay(List<AchieveItem> itemListDay) {
		this.itemListDay = itemListDay;
	}

	
	
	public List<AchieveItem> getAchieveList(){
		List<AchieveItem> list = new ArrayList<>();
		list.addAll(this.itemListDay);
		list.addAll(this.itemListScore);
		return list;
	}
    
	
    
	public void resetItemListDay(List<AchieveItem> itemList){
		this.itemListDay = itemList;
		this.notifyUpdate();
	}
	
	public void resetItemListScore(List<AchieveItem> itemList){
		this.itemListScore = itemList;
		this.notifyUpdate();
	}

	public void addItemScoreRecord(int score){
		long time = HawkTime.getAM0Date().getTime();
		GrowUpBoostScore record = this.scores.get(time);
    	if(Objects.isNull(record)){
    		record = new GrowUpBoostScore();
    		record.setDayZero(time);
    		record.setItemScore(score);
    		this.scores.put(record.getDayZero(), record);
    		this.notifyUpdate();
    		return;
    	}
    	
    	int scoreAft = record.getItemScore() + score;
    	record.setItemScore(scoreAft);
		this.notifyUpdate();
	}
	
	
	public void addAchieveScoreRecord(int score){
		long time = HawkTime.getAM0Date().getTime();
		GrowUpBoostScore record = this.scores.get(time);
    	if(Objects.isNull(record)){
    		record = new GrowUpBoostScore();
    		record.setDayZero(time);
    		record.setAchieveScore(score);
    		this.scores.put(record.getDayZero(), record);
    		this.notifyUpdate();
    		return;
    	}
    	int scoreAft = record.getAchieveScore() + score;
    	record.setAchieveScore(scoreAft);
		this.notifyUpdate();
	}
	
	
    public void addItemConsumeScoreDetail(int itemId,int itemNum,int score){
    	GrowUpBoostItemScoreDetail detail = this.itemScores.get(itemId);
    	if(Objects.isNull(detail)){
    		detail = new GrowUpBoostItemScoreDetail();
    		detail.setItemId(itemId);
    		detail.setItemNum(itemNum);
    		detail.setScore(score);
    		this.itemScores.put(itemId, detail);
    		this.notifyUpdate();
    		return;
    	}
    	
    	int scoreAft = detail.getScore() + score;
    	int numAft = detail.getItemNum() + itemNum;
    	detail.setItemNum(numAft);
		detail.setScore(scoreAft);
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
    
    
    
    
    public Map<Integer, Integer> getBuyNumMap() {
        return buyNumMap;
    }


    public int getBuyCount(int buyId) {
        return this.buyNumMap.getOrDefault(buyId, 0);
    }

    public void addBuyCount(int eid, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getBuyCount(eid);
        this.buyNumMap.put(eid, count);
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
