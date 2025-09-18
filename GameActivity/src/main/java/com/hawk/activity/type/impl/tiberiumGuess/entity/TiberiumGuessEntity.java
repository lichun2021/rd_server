package com.hawk.activity.type.impl.tiberiumGuess.entity;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;

/* 泰伯利亚竞猜
 * @author Winder
 *
 */
@Entity
@Table(name="activity_tiberium_guess")
public class TiberiumGuessEntity extends AchieveActivityEntity implements IActivityDataEntity{
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
	@Column(name = "createTime", nullable = false)
	private long createTime;
	
    @IndexProp(id = 5)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 6)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	
	public TiberiumGuessEntity() {
	}
	
	public TiberiumGuessEntity(String playerId) {
		this.playerId = playerId;
	}
	public TiberiumGuessEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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
		return this.invalid;
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

}
