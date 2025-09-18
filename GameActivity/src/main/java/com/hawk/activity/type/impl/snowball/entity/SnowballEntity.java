package com.hawk.activity.type.impl.snowball.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 雪球大战
 * @author golden
 *
 */
@Entity
@Table(name = "activity_snowball")
public class SnowballEntity extends AchieveActivityEntity implements IActivityDataEntity{

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
	 * 总积分
	 */
    @IndexProp(id = 4)
	@Column(name = "score", nullable = false)
	private int score;
	
	/**
	 * 场次
	 */
    @IndexProp(id = 5)
	@Column(name = "turnId", nullable = false)
	private int turnId;
	
	/**
	 * 踢球积分
	 */
    @IndexProp(id = 6)
	@Column(name = "kickScore", nullable = false)
	private int kickScore;
	
	/**
	 * 连续踢球积分
	 */
    @IndexProp(id = 7)
	@Column(name = "continueKickScore", nullable = false)
	private int continueKickScore;
	
	/**
	 * 助攻积分
	 */
    @IndexProp(id = 8)
	@Column(name = "assisScore", nullable = false)
	private int assisScore;
	
	/**
	 * 进球积分
	 */
    @IndexProp(id = 9)
	@Column(name = "goalScore", nullable = false)
	private int goalScore;
	
	/**
	 * 进球助攻积分
	 */
    @IndexProp(id = 10)
	@Column(name = "goalAssisScore", nullable = false)
	private int goalAssisScore;
	
	/**
	 * 已经领取的奖励
	 */
    @IndexProp(id = 11)
	@Column(name = "receive", nullable = false)
	private String receive;
	
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
	private Set<Integer> received = new HashSet<Integer>();
	
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

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void addScore(int score) {
		this.score += score;
		notifyUpdate();
	}
	
	public int getTurnId() {
		return turnId;
	}

	public void setTurnId(int turnId) {
		this.turnId = turnId;
	}

	public int getKickScore() {
		return kickScore;
	}

	public void setKickScore(int kickScore) {
		this.kickScore = kickScore;
	}

	public void addKickScore(int score) {
		this.kickScore += score;
		notifyUpdate();
	}
	
	public int getContinueKickScore() {
		return continueKickScore;
	}

	public void setContinueKickScore(int continueKickScore) {
		this.continueKickScore = continueKickScore;
	}

	public void addContinueKickScore(int score) {
		this.continueKickScore += score;
		notifyUpdate();
	}
	
	public int getAssisScore() {
		return assisScore;
	}

	public void setAssisScore(int assisScore) {
		this.assisScore = assisScore;
	}

	public void addAssisScore(int score) {
		this.assisScore += score;
		notifyUpdate();
	}
	
	public int getGoalScore() {
		return goalScore;
	}

	public void setGoalScore(int goalScore) {
		this.goalScore = goalScore;
	}
	
	public void addGoalScore(int score) {
		this.goalScore += score;
		notifyUpdate();
	}
	
	public int getGoalAssisScore() {
		return goalAssisScore;
	}

	public void setGoalAssisScore(int goalAssisScore) {
		this.goalAssisScore = goalAssisScore;
	}
	
	public void addGoalAssisScore(int score) {
		this.goalAssisScore += score;
		notifyUpdate();
	}
	
	public void setReceived(Set<Integer> received) {
		this.received = received;
	}

	public String getReceive() {
		return receive;
	}

	public void setReceive(String receive) {
		this.receive = receive;
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

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public Set<Integer> getReceived() {
		return received;
	}

	public void addReceived(int index) {
		this.received.add(index);
		notifyUpdate();
	}

	@Override
	public void beforeWrite() {
		this.receive = SerializeHelper.collectionToString(this.received, SerializeHelper.BETWEEN_ITEMS);
	}
	
	@Override
	public void afterRead() {
		if (!HawkOSOperator.isEmptyString(receive)) {
			this.received = SerializeHelper.stringToSet(Integer.class, this.receive, SerializeHelper.BETWEEN_ITEMS);
		}
	}
}
