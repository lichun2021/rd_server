package com.hawk.game.module.nationMilitary.entity;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

/**
 * 指挥官实体对象
 *
 * @author Jesse
 */
@Entity
@Table(name = "nation_military")
public class NationMilitaryEntity extends HawkDBEntity {

	@Id
	@Column(name = "playerId", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String playerId;

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 3)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 4)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 5)
	protected boolean invalid;

	// 国家军功经验
	@Column(name = "nationMilitaryExp")
	@IndexProp(id = 10)
	private int nationMilitaryExp;

	// 国家军功重置期
	@Column(name = "nationMilitaryResetTerm")
	@IndexProp(id = 11)
	private int nationMilitaryResetTerm;

	// 国家军功等级
	@Column(name = "nationMilitarLlevel")
	@IndexProp(id = 12)
	private int nationMilitarLlevel;

	// 国家军功跨服期
	@Column(name = "crossTermId")
	@IndexProp(id = 13)
	private int crossTermId;

	// 国家军功跨服期战斗获取军工
	@Column(name = "nationMilitaryBattleExp")
	@IndexProp(id = 14)
	private int nationMilitaryBattleExp;

	// 国家军功跨服期战斗获取军工 奖励获日
	@Column(name = "nationMilitaryRewardDay")
	@IndexProp(id = 15)
	private int nationMilitaryRewardDay;

	// 国家军功跨服期战斗获取军工 奖励获日nationMilitarLlevel id
	@Column(name = "nationMilitaryReward")
	@IndexProp(id = 16)
	private int nationMilitaryReward;
	
	// 国家军功定榜期
	@Column(name = "nationMilitaryRankTerm")
	@IndexProp(id = 17)
	private int nationMilitaryRankTerm;

	@Transient
	private final AtomicBoolean needCheck = new AtomicBoolean();

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
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
	public void beforeWrite() {
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		super.afterRead();
	}

	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}

	public String getOwnerKey() {
		return playerId;
	}

	public int getNationMilitaryExp() {
		return nationMilitaryExp;
	}

	public void setNationMilitaryExp(int nationMilitaryExp) {
		this.nationMilitaryExp = nationMilitaryExp;
	}

	public int getNationMilitaryResetTerm() {
		return nationMilitaryResetTerm;
	}

	public void setNationMilitaryResetTerm(int nationMilitaryResetTerm) {
		this.nationMilitaryResetTerm = nationMilitaryResetTerm;
	}

	public int getNationMilitarLlevel() {
		return nationMilitarLlevel;
	}

	public void setNationMilitarLlevel(int nationMilitarLlevel) {
		this.nationMilitarLlevel = nationMilitarLlevel;
	}

	public int getCrossTermId() {
		return crossTermId;
	}

	public void setCrossTermId(int crossTermId) {
		this.crossTermId = crossTermId;
	}

	public int getNationMilitaryBattleExp() {
		return nationMilitaryBattleExp;
	}

	public void setNationMilitaryBattleExp(int nationMilitaryBattleExp) {
		this.nationMilitaryBattleExp = nationMilitaryBattleExp;
	}

	public int getNationMilitaryRewardDay() {
		return nationMilitaryRewardDay;
	}

	public void setNationMilitaryRewardDay(int nationMilitaryRewardDay) {
		this.nationMilitaryRewardDay = nationMilitaryRewardDay;
	}

	public int getNationMilitaryReward() {
		return nationMilitaryReward;
	}

	public void setNationMilitaryReward(int nationMilitaryReward) {
		this.nationMilitaryReward = nationMilitaryReward;
	}

	@Override
	public void notifyUpdate() {
		needCheck.set(true);
		super.notifyUpdate();
	}

	public AtomicBoolean getNeedCheck() {
		return needCheck;
	}

	public int getNationMilitaryRankTerm() {
		return nationMilitaryRankTerm;
	}

	public void setNationMilitaryRankTerm(int nationMilitaryRankTerm) {
		this.nationMilitaryRankTerm = nationMilitaryRankTerm;
	}

}
