package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月4日
 */
@Entity
@Table( name = "guild_science")
public class GuildScienceEntity extends HawkDBEntity{
	
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;
	
	@Column(name = "guildId")
    @IndexProp(id = 2)
	private String guildId;
	
	@Column(name = "scienceId")
    @IndexProp(id = 3)
	private Integer scienceId;
	
	@Column(name = "level")
    @IndexProp(id = 4)
	private int level;
	
	@Column(name = "star")
    @IndexProp(id = 5)
	private int star;
	
	/** 科技值*/
	@Column(name = "donate")
    @IndexProp(id = 6)
	private int donate;
	
	/** 开始研究时间*/
	@Column(name = "finishTime")
    @IndexProp(id = 7)
	private long finishTime;
	
	/** 是否推荐科技*/
	@Column(name = "recommend")
    @IndexProp(id = 8)
	private boolean recommend;
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 9)
	private long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 10)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 11)
	private boolean invalid;
	
	
	@Column(name = "openLimitTime")
    @IndexProp(id = 12)
	private long openLimitTime;
	
	public GuildScienceEntity() {
		this.level = 0;
		this.star = 0;
		this.donate = 0;
		this.recommend = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public Integer getScienceId() {
		return scienceId;
	}

	public void setScienceId(Integer scienceId) {
		this.scienceId = scienceId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public int getDonate() {
		return donate;
	}

	public void setDonate(int donate) {
		this.donate = donate;
	}
	
	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public boolean isRecommend() {
		return recommend;
	}

	public void setRecommend(boolean recommend) {
		this.recommend = recommend;
	}
	
	public long getOpenLimitTime() {
		return openLimitTime;
	}

	public void setOpenLimitTime(long openLimitTime) {
		this.openLimitTime = openLimitTime;
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
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

}
