package com.hawk.activity.type.impl.questTreasure.entity;

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
@Table(name = "activity_quest_treasure")
public class QuestTreasureEntity extends ActivityDataEntity implements IExchangeTipEntity{
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
    
    /** 游戏刷新次数*/
    @IndexProp(id = 5)
   	@Column(name = "gameRefreshCount", nullable = false)
    private int gameRefreshCount;
    
    /**商店兑换次数 */
    @IndexProp(id = 6)
	@Column(name = "buyInfo", nullable = false)
	private String buyInfo;
    
    /** 宝箱积分*/
    @IndexProp(id = 7)
	@Column(name = "boxScore", nullable = false)
    private int boxScore;
	
    /** 活动成就项数据 */
	@IndexProp(id = 8)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	@IndexProp(id = 9)
	@Column(name = "initTime", nullable = false)
	private long initTime;
	
    @IndexProp(id = 10)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;
    
    @IndexProp(id = 11)
	@Column(name = "tips", nullable = false)
	private String tips;
	
    @IndexProp(id = 12)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 13)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 14)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
    @Transient
    private QuestTreasureGame game = new QuestTreasureGame();
	
	@Transient
	private Map<Integer, Integer> buyInfoMap = new ConcurrentHashMap<>();
	
	@Transient
	private List<QuestTreasureAchieveItem> achieveList = new CopyOnWriteArrayList<>();
	
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	
	public QuestTreasureEntity() {
		
	}
	
	
	public QuestTreasureEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.gameInfo = "";
		this.buyInfo = "";
		this.achieveItems = "";
		this.loginDays = "";
		this.tips = "";
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.achieveList);
		this.buyInfo = SerializeHelper.mapToString(buyInfoMap);
		this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
		this.gameInfo = this.game.serializ();
	}
	
	@Override
	public void afterRead() {
		this.buyInfoMap = SerializeHelper.stringToMap(buyInfo);
		this.achieveList.clear();
		SerializeHelper.stringToList(QuestTreasureAchieveItem.class, this.achieveItems, this.achieveList);
		SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
		this.game.mergeFrom(this.gameInfo);
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



	public int getGameRefreshCount() {
		return gameRefreshCount;
	}
	
	public void setGameRefreshCount(int gameRefreshCount) {
		this.gameRefreshCount = gameRefreshCount;
	}

	public Map<Integer, Integer> getBuyInfoMap() {
		return buyInfoMap;
	}

	  

    public int getBuyCount(int exchangeId) {
        return this.buyInfoMap.getOrDefault(exchangeId, 0);
    }

    public void addBuyCount(int eid, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getBuyCount(eid);
        this.buyInfoMap.put(eid, count);
        this.notifyUpdate();
    }


    public List<AchieveItem> getAchieveList() {
    	List<AchieveItem> list = new ArrayList<>();
    	list.addAll(this.achieveList);
		return list;
	}
    
    public void setAchieveList(List<QuestTreasureAchieveItem> achieveList) {
		this.achieveList = achieveList;
	}
    
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	public QuestTreasureGame getGame() {
		return game;
	}
	
	public void setGame(QuestTreasureGame game) {
		this.game = game;
	}
	
	public int getBoxScore() {
		return boxScore;
	}
	
	public void setBoxScore(int boxScore) {
		this.boxScore = boxScore;
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

	
}
