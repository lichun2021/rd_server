package com.hawk.activity.type.impl.goldBabyNew.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.goldBabyNew.cfg.GoldBabyNewDailyAchieveCfg;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_gold_baby_new")
public class GoldBabyNewEntity extends HawkDBEntity implements IActivityDataEntity{
	
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
    
    /**
     * 累计抽取次数
     */
    @IndexProp(id = 4)
	@Column(name = "findTimes", nullable = false)
	private int findTimes;
   
    /**
     * 奖池项
     */
    @IndexProp(id = 5)
	@Column(name = "pools", nullable = false)
	private String pools;   
    
    /**
     * 累计登陆天数
     */
    @IndexProp(id = 6)
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
    /**
     * 活动成就项数据
     */
    @IndexProp(id = 7)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
    
    /**
     * 上次登陆时间
     */
    @IndexProp(id = 8)
	@Column(name = "lastLoginTime", nullable = false)
	private long lastLoginTime;    
    
    /**
     * 剩余购买次数
     */
    @IndexProp(id = 9)
	@Column(name = "buyTimes", nullable = false)
	private int buyTimes;    
    
	/**
	 * 跨天天数更新时间
	 */
    @IndexProp(id = 10)
	@Column(name = "refreshTime", nullable = false)
	private long refreshTime;
    
    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	/**
	 * 奖池列表
	 */
	@Transient
	private List<GoldBabyNewRewardPool> poolList = new ArrayList<>();
	
	/**
	 * 成就奖励
	 */
	@Transient
	private List<AchieveItem> achieveItemList = new CopyOnWriteArrayList<AchieveItem>();
	
	/**
	 * 默认构造
	 * -从数据库加载时会调用无参构造方法
	 */
    public GoldBabyNewEntity() {
    }
    
    public GoldBabyNewEntity (String playerId, int termId) {
    	this.playerId=playerId;
    	this.termId=termId;
    	this.loginDays=1;
    	this.refreshTime=HawkTime.getMillisecond();
    	this.pools="";
    	this.achieveItems="";
    	this.findTimes=0;
    	this.buyTimes=0;
	}
    
	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	public List<AchieveItem> getAchieveItemList() {
		return achieveItemList;
	}

	public void setAchieveItemList(List<AchieveItem> achieveItemList) {
		this.achieveItemList = achieveItemList;
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

	public int getFindTimes() {
		return findTimes;
	}

	
	public int getBuyTimes() {
		return buyTimes;
	}

	public void setBuyTimes(int buyTimes) {
		this.buyTimes = buyTimes;
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	protected void setCreateTime(long arg0) {
		this.createTime=arg0;
	}

	@Override
	protected void setInvalid(boolean arg0) {
		this.invalid=arg0;
	}

	@Override
	public void setPrimaryKey(String arg0) {
		this.id=arg0;
	}

	@Override
	protected void setUpdateTime(long arg0) {
		this.updateTime=arg0;
	}

	public String getPools() {
		return pools;
	}

	public void setPools(String pools) {
		this.pools = pools;
	}

	public void addAchieveItems(AchieveItem item) {
		this.achieveItemList.add(item);
		notifyUpdate();
	}
	
	public void addPool(GoldBabyNewRewardPool pool) {
		this.poolList.add(pool);
		notifyUpdate();
	}
	
	public void fillPoolList(List<GoldBabyNewRewardPool> poolList) {
		this.poolList.clear();
		this.poolList.addAll(poolList);
		notifyUpdate();
	}

	public List<GoldBabyNewRewardPool> getPoolList() {
		return poolList;
	}

	public void setPoolList(List<GoldBabyNewRewardPool> poolList) {
		this.poolList = poolList;
	}

	public void setFindTimes(int findTimes) {
		this.findTimes = findTimes;
	}

	
	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	@Override
	public void beforeWrite() {
		pools = SerializeHelper.collectionToString(poolList, SerializeHelper.BETWEEN_ITEMS);
		this.achieveItems = SerializeHelper.collectionToString(this.achieveItemList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		
		String[] array = SerializeHelper.split(pools, SerializeHelper.BETWEEN_ITEMS);
		List<GoldBabyNewRewardPool> pools = new ArrayList<>();
		for (String data : array) {
			pools.add(GoldBabyNewRewardPool.valueOf(data));
		}
		
		this.achieveItemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.achieveItemList);
		fillPoolList(pools);
	}
	
	/**
	 * 根据id获取奖池对象
	 * @param poolId
	 * @return
	 */
	public GoldBabyNewRewardPool getPoolById(int poolId){
		for(GoldBabyNewRewardPool pool: poolList){
			if (pool.getPoolId()==poolId) {
				return pool;
			}
		}
		return null;
	}
	
	/**
	 * 重置每日成就数据
	 */
	public void resetDailyAchieve() {
		
		for (AchieveItem achieveItem : achieveItemList){
			
			GoldBabyNewDailyAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GoldBabyNewDailyAchieveCfg.class, achieveItem.getAchieveId());
			if (cfg == null) {
				continue;
			}
			if (cfg.getResetting() == 1) {
				achieveItem.reset();
			}
		}
	}
}
