package com.hawk.game.module.college.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

/**
 * 军事学院实体
 *
 * @author shadow
 *
 */
/**
 * @author LENOVO
 *
 */
@Entity
@Table(name = "college_info")
public class CollegeEntity extends HawkDBEntity {
	@Id
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "coachId", nullable = false)
    @IndexProp(id = 2)
	private String coachId;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 3)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 4)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 5)
	protected boolean invalid;
	
	
	/** 学院名称*/
	@Column(name = "collegeName")
    @IndexProp(id = 6)
	protected String collegeName= "";
	
	/** 经验*/
	@Column(name = "expTotal")
    @IndexProp(id = 7)
	protected int expTotal;
	
	/** 经验*/
	@Column(name = "level")
    @IndexProp(id = 8)
	protected int level;
	
	/** 经验*/
	@Column(name = "exp")
    @IndexProp(id = 9)
	protected int exp;
	
	/** 体力可分配*/
	@Column(name = "vitality")
    @IndexProp(id = 10)
	protected double vitality;
	
	/** 是否自由加入 0需要申请  1可以自由加入*/
	@Column(name = "joinFree")
    @IndexProp(id = 11)
	protected int joinFree;
	
	@Column(name = "reNameCount")
    @IndexProp(id = 12)
	protected int reNameCount;
	
	/** 统计数据*/
	@Column(name = "statisticsData")
    @IndexProp(id = 13)
	private String statisticsData= "";
	
	
	@Transient
	private CollegeStatisticsEntity statisticsEntity = new CollegeStatisticsEntity();
	

	@Transient
	private Map<String,Long> kickOutTimeMap = new ConcurrentHashMap<>();
	@Transient
	private Map<String,Long> guildApplyMap = new ConcurrentHashMap<>();
	@Transient
	private long guildApplyTime;
	@Transient
	private Map<String,Long> inviteMailSendMap = new ConcurrentHashMap<>();
	
	public CollegeEntity() {
	}
	
	@Override
	public void beforeWrite() {
		this.statisticsData = this.statisticsEntity.serializ();
		super.beforeWrite();
	}
	
	@Override
	public void afterRead() {
		CollegeStatisticsEntity statisticsEntityTemp = new CollegeStatisticsEntity();
		statisticsEntityTemp.mergeFrom(this.statisticsData);
		this.statisticsEntity = statisticsEntityTemp;
		super.afterRead();
	}
	public int getExp() {
		return exp;
	}
	
	public void setExp(int exp) {
		this.exp = exp;
	}
	
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	
	public void setCollegeName(String collegeName) {
		this.collegeName = collegeName;
	}
	
	public String getCollegeName() {
		return collegeName;
	}
	
	public double getVitality() {
		return vitality;
	}
	
	public void setVitality(double vitality) {
		this.vitality = vitality;
	}
	
	
	public int getCanVitalitySendValue(){
		int value = (int) (this.vitality);
		return value;
	}
	
	
	public int getJoinFree() {
		return joinFree;
	}
	
	public void setJoinFree(int joinFree) {
		this.joinFree = joinFree;
	}
	
	public int getReNameCount() {
		return reNameCount;
	}
	
	public void setReNameCount(int reNameCount) {
		this.reNameCount = reNameCount;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCoachId() {
		return coachId;
	}

	public void setCoachId(String coachId) {
		this.coachId = coachId;
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

	
	public CollegeStatisticsEntity getStatisticsEntity() {
		return statisticsEntity;
	}
	
	public void setStatisticsEntity(CollegeStatisticsEntity statisticsEntity) {
		this.statisticsEntity = statisticsEntity;
	}
	
	
	public int getExpTotal() {
		return expTotal;
	}
	
	public void setExpTotal(int expTotal) {
		this.expTotal = expTotal;
	}
	
	
	
	public Map<String, Long> getKickOutTimeMap() {
		return kickOutTimeMap;
	}
	
	public Map<String, Long> getGuildApplyMap() {
		return guildApplyMap;
	}
	
	public long getGuildApplyTime() {
		return guildApplyTime;
	}
	
	public void resetGuildApplyTime(long guildApplyTime) {
		this.guildApplyTime = guildApplyTime;
	}
	
	public void addKickoutTime(String playerId,long time){
		this.kickOutTimeMap.put(playerId, time);
	}
	
	public Map<String, Long> getInviteMailSendMap() {
		return inviteMailSendMap;
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
