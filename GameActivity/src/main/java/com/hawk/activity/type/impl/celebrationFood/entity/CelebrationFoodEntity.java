package com.hawk.activity.type.impl.celebrationFood.entity;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;

@Entity
@Table(name = "activity_celebration_food")
public class CelebrationFoodEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	//美食当前等级
    @IndexProp(id = 4)
	@Column(name = "foodLevel", nullable = false)
	private int foodLevel;
	//是否购买进阶
    @IndexProp(id = 5)
	@Column(name = "buyAdvance", nullable = false)
	private boolean buyAdvance;
	
	/**野外采集上次累计的时间*/
    @IndexProp(id = 6)
	@Column(name="wolrdCollectRemainTime", nullable = false)
	private int wolrdCollectRemainTime;
	/**
	 * 世界资源收集的次数
	 */
    @IndexProp(id = 7)
	@Column(name="wolrdCollectTimes", nullable = false)
	private int wolrdCollectTimes;
	
	/**击败尤里的次数*/
    @IndexProp(id = 8)
	@Column(name="beatYuriTimes", nullable = false)
	private int beatYuriTimes;
	
	/**击败尤里的总次数*/
    @IndexProp(id = 9)
	@Column(name="beatYuriTotalTimes", nullable = false)
	private int beatYuriTotalTimes;
	
	/* 军需补给*/
    @IndexProp(id = 10)
	@Column(name="wishTimes", nullable = false, length=10)
	private int wishTimes;
	
	/* 军需补给总次数*/
    @IndexProp(id = 11)
	@Column(name="wishTotalTimes", nullable = false, length=10)
	private int wishTotalTimes;
	
    @IndexProp(id = 12)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 13)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 14)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	//是否购买进阶
    @IndexProp(id = 15)
	@Column(name = "buySuper", nullable = false)
	private boolean buySuper;
	
	
	public CelebrationFoodEntity() {
	}
	
	public CelebrationFoodEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	public boolean isBuySuper() {
		return buySuper;
	}

	public void setBuySuper(boolean buySuper) {
		this.buySuper = buySuper;
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

	public int getFoodLevel() {
		return foodLevel;
	}

	public void setFoodLevel(int foodLevel) {
		this.foodLevel = foodLevel;
	}

	public boolean isBuyAdvance() {
		return buyAdvance;
	}

	public void setBuyAdvance(boolean buyAdvance) {
		this.buyAdvance = buyAdvance;
	}

	public int getWolrdCollectRemainTime() {
		return wolrdCollectRemainTime;
	}

	public void setWolrdCollectRemainTime(int wolrdCollectRemainTime) {
		this.wolrdCollectRemainTime = wolrdCollectRemainTime;
	}

	public int getWolrdCollectTimes() {
		return wolrdCollectTimes;
	}

	public void setWolrdCollectTimes(int wolrdCollectTimes) {
		this.wolrdCollectTimes = wolrdCollectTimes;
	}

	public int getBeatYuriTimes() {
		return beatYuriTimes;
	}

	public void setBeatYuriTimes(int beatYuriTimes) {
		this.beatYuriTimes = beatYuriTimes;
	}

	public int getBeatYuriTotalTimes() {
		return beatYuriTotalTimes;
	}

	public void setBeatYuriTotalTimes(int beatYuriTotalTimes) {
		this.beatYuriTotalTimes = beatYuriTotalTimes;
	}

	public int getWishTimes() {
		return wishTimes;
	}

	public void setWishTimes(int wishTimes) {
		this.wishTimes = wishTimes;
	}

	public int getWishTotalTimes() {
		return wishTotalTimes;
	}

	public void setWishTotalTimes(int wishTotalTimes) {
		this.wishTotalTimes = wishTotalTimes;
	}

	
}
