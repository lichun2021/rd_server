package com.hawk.activity.type.impl.heroTrial.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.heroTrial.temp.HeroTrialTemplate;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 英雄试炼
 * @author golden
 *
 */
@Entity
@Table(name="activity_hero_trial")
public class HeroTrialActivityEntity extends HawkDBEntity implements IActivityDataEntity {

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
	 * 任务
	 */
    @IndexProp(id = 4)
	@Column(name = "mission", nullable = false)
	private String mission;
	
	/**
	 * 上次刷新时间
	 */
    @IndexProp(id = 5)
	@Column(name = "lastRefreshTime", nullable = false)
	private long lastRefreshTime;
	
	/**
	 * 接受任务次数
	 */
    @IndexProp(id = 6)
	@Column(name = "acceptTimes", nullable = false)
	private int acceptTimes;
	
	/**
	 * 刷新次数
	 */
    @IndexProp(id = 7)
	@Column(name = "refreshTimes", nullable = false)
	private int refreshTimes;
	
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
	private Set<HeroTrialTemplate> missionSet = new ConcurrentHashSet<>();
	
	public HeroTrialActivityEntity() {
		
	}
	
	public HeroTrialActivityEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		mission = SerializeHelper.collectionToString(missionSet, SerializeHelper.SEMICOLON_ITEMS);
	}

	@Override
	public void afterRead() {
		missionSet = SerializeHelper.stringToSet(HeroTrialTemplate.class, mission, SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT);
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

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	public int getAcceptTimes() {
		return acceptTimes;
	}

	public void setAcceptTimes(int acceptTimes) {
		this.acceptTimes = acceptTimes;
	}

	public long getLastRefreshTime() {
		return lastRefreshTime;
	}

	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}

	public Set<HeroTrialTemplate> getMissionSet() {
		return missionSet;
	}
	
	public int getRefreshTimes() {
		return refreshTimes;
	}

	public void setRefreshTimes(int refreshTimes) {
		this.refreshTimes = refreshTimes;
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}
	
	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	/**
	 * 删除任务
	 */
	public void removeMission(String missionUUid) {
		Iterator<HeroTrialTemplate> iterator = missionSet.iterator();
		while(iterator.hasNext()) {
			HeroTrialTemplate mission = iterator.next();
			if (!mission.getUuid().equals(missionUUid)) {
				continue;
			}
			iterator.remove();
		}
		notifyUpdate();
	}
	
	public List<Integer> getAllTrialHeroIds() {
		List<Integer> heroIds = new ArrayList<>();
		for (HeroTrialTemplate mission : missionSet) {
			heroIds.addAll(mission.getHeroList());
		}
		return heroIds;
	}
}
