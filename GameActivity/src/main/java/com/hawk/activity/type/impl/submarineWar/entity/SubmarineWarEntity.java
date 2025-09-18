package com.hawk.activity.type.impl.submarineWar.entity;

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
@Table(name = "activity_submarine_war")
public class SubmarineWarEntity extends ActivityDataEntity implements IExchangeTipEntity{
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/** 游戏数据*/
    @IndexProp(id = 4)
	@Column(name = "gameInfo", nullable = false)
	private String gameInfo;
    
    /** 游戏总积分*/
    @IndexProp(id = 5)
   	@Column(name = "gameScore", nullable = false)
    private int gameScore;
    
    /** 游戏次数*/
    @IndexProp(id = 6)
   	@Column(name = "gameCount", nullable = false)
    private int gameCount;
    
    /** 购买游戏次数*/
    @IndexProp(id = 7)
   	@Column(name = "buyGameCount", nullable = false)
    private int buyGameCount;
    
    /** 游戏通关最高等级*/
    @IndexProp(id = 8)
   	@Column(name = "gameLevelMax", nullable = false)
    private int gameLevelMax;
    
    /**游戏通关最高积分*/
    @IndexProp(id = 9)
   	@Column(name = "gameScoreMax", nullable = false)
    private int gameScoreMax;
    
    /** 游戏通关时间*/
    @IndexProp(id = 10)
   	@Column(name = "gameScoreMaxTime", nullable = false)
    private long gameScoreMaxTime;
    
    /** 技能商店兑换次数 */
    @IndexProp(id = 11)
	@Column(name = "skillItemBuyInfo", nullable = false)
	private String skillItemBuyInfo;
    
    /** 兑换商店兑换次数 */
    @IndexProp(id = 12)
	@Column(name = "buyInfo", nullable = false)
	private String buyInfo;

    /** 活动成就项数据 */
	@IndexProp(id = 13)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	@IndexProp(id = 14)
	@Column(name = "initTime", nullable = false)
	private long initTime;
	
    @IndexProp(id = 15)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;
    
    @IndexProp(id = 16)
	@Column(name = "tips", nullable = false)
	private String tips;
	
    @IndexProp(id = 17)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 18)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 19)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
    
    @IndexProp(id = 20)
  	@Column(name = "orderInfo", nullable = false)
    private String orderInfo;
    
    
    
    @Transient
    private SubmarineWarGame game = new SubmarineWarGame();
	
	@Transient
	private Map<Integer, Integer> buyInfoMap = new ConcurrentHashMap<>();
	
	@Transient
	private List<AchieveItem> achieveList = new CopyOnWriteArrayList<>();
	
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	@Transient
	private Map<Integer, Integer> skillItembuyInfoMap = new ConcurrentHashMap<>();
	
	
	@Transient
	private SubmarineWarOrder order = new SubmarineWarOrder();
	
	
	public SubmarineWarEntity() {
		
	}
	
	
	public SubmarineWarEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.gameInfo = "";
		this.buyInfo = "";
		this.achieveItems = "";
		this.loginDays = "";
		this.tips = "";
		this.orderInfo = "";
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.achieveList);
		this.buyInfo = SerializeHelper.mapToString(buyInfoMap);
		this.skillItemBuyInfo = SerializeHelper.mapToString(skillItembuyInfoMap);
		this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
		this.gameInfo = this.game.serializ();
		this.orderInfo = this.order.serializ();
	}
	
	@Override
	public void afterRead() {
		this.buyInfoMap = SerializeHelper.stringToMap(buyInfo);
		this.skillItembuyInfoMap = SerializeHelper.stringToMap(skillItemBuyInfo);
		this.achieveList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.achieveList);
		SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
		this.game.mergeFrom(this.gameInfo);
		this.order.mergeFrom(this.orderInfo);
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
	
	public Map<Integer, Integer> getShopBuyInfoMap() {
		return buyInfoMap;
	}

    public int getShopBuyCount(int exchangeId) {
        return this.buyInfoMap.getOrDefault(exchangeId, 0);
    }

    public void addShopBuyCount(int eid, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getShopBuyCount(eid);
        this.buyInfoMap.put(eid, count);
        this.notifyUpdate();
    }

    

    public List<AchieveItem> getAchieveList() {
    	List<AchieveItem> list = new ArrayList<>();
    	list.addAll(this.achieveList);
		return list;
	}
    
    public void setAchieveList(List<AchieveItem> achieveList) {
		this.achieveList = achieveList;
	}
    
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	public SubmarineWarGame getGame() {
		return game;
	}
	
	public void setGame(SubmarineWarGame game) {
		this.game = game;
	}
	
	public Map<Integer, Integer> getSkillItembuyInfoMap() {
		return skillItembuyInfoMap;
	}
	
	public void clearSkillItembuyInfoMap(){
		this.skillItembuyInfoMap = new ConcurrentHashMap<>();
		this.notifyUpdate();
	}
	
	public int getSkillItemBuyCount(int exchangeId) {
        return this.skillItembuyInfoMap.getOrDefault(exchangeId, 0);
    }

    public void addSkillItemBuyCount(int eid, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getSkillItemBuyCount(eid);
        this.skillItembuyInfoMap.put(eid, count);
        this.notifyUpdate();
    }
    
    
    public int getGameCount() {
		return gameCount;
	}
    
    public void setGameCount(int gameCount) {
		this.gameCount = gameCount;
	}
    
    public int getBuyGameCount() {
		return buyGameCount;
	}
    
    public void setBuyGameCount(int buyGameCount) {
		this.buyGameCount = buyGameCount;
	}
	

    public int getGameLevelMax() {
		return gameLevelMax;
	}
    
    public void setGameLevelMax(int gameLevelMax) {
		this.gameLevelMax = gameLevelMax;
	}
    
    public int getGameScoreMax() {
		return gameScoreMax;
	}
    
    public void setGameScoreMax(int gameScoreMax) {
		this.gameScoreMax = gameScoreMax;
	}
    
    public long getGameScoreMaxTime() {
		return gameScoreMaxTime;
	}
    
    public void setGameScoreMaxTime(long gameScoreMaxTime) {
		this.gameScoreMaxTime = gameScoreMaxTime;
	}
    
    
    public int getGameScore() {
		return gameScore;
	}
    
    public void setGameScore(int gameScore) {
		this.gameScore = gameScore;
	}
    
    
    public boolean updateMaxLevelRecord(int level,int score,int time){
    	if(score > this.gameScoreMax){
    		this.gameLevelMax = level;
    		this.gameScoreMax = score;
    		this.gameScoreMaxTime = time;
    		return true;
    	}
    	return false;
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
		return this.loginDays;
	}


	@Override
	public Set<Integer> getTipSet() {
		return this.tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tips) {
		this.tipSet = tips;
	}

	
	public SubmarineWarOrder getOrder() {
		return order;
	}
	
	public void setOrder(SubmarineWarOrder order) {
		this.order = order;
	}
	
}
