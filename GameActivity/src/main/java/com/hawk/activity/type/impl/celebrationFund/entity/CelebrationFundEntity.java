package com.hawk.activity.type.impl.celebrationFund.entity;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.celebrationFund.cfg.CelebrationFundBuyCfg;

@Entity
@Table(name = "activity_celebration_fund")
public class CelebrationFundEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	//档位
    @IndexProp(id = 4)
	@Column(name = "fundLevel", nullable = false)
	private int fundLevel;
	//档位积分
    @IndexProp(id = 5)
	@Column(name = "levelScore", nullable = false)
	private int levelScore;
	
    @IndexProp(id = 6)
	@Column(name = "buyOver", nullable = false)
	private int buyOver;
    
    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	
	public int getBuyOver() {
		return buyOver;
	}

	public void setBuyOver(int buyOver) {
		this.buyOver = buyOver;
	}

	public CelebrationFundEntity() {
	}
	
	public CelebrationFundEntity(String playerId, int termId) {
		this.playerId = playerId;
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public int getFundLevel() {
		if (fundLevel <= 0) {
			CelebrationFundBuyCfg cfg = CelebrationFundBuyCfg.getFirstLevelConfig();
			this.setFundLevel(cfg.getId());
		}
		return fundLevel;
	}

	public void setFundLevel(int fundLevel) {
		this.fundLevel = fundLevel;
	}

	public int getLevelScore() {
		return levelScore;
	}

	public void setLevelScore(int levelScore) {
		this.levelScore = levelScore;
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

	
}
