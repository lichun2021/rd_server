package com.hawk.game.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 英雄档案馆
 * 
 * @author golden
 *
 */
@Entity
@Table(name = "hero_archives")
public class HeroArchivesEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId;

	/**
	 * 档案信息
	 */
	@Column(name = "archives", nullable = false)
    @IndexProp(id = 5)
	private String archives = "";

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 6)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 7)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 8)
	protected boolean invalid;

	/**
	 * 档案信息
	 */
	@Transient
	private Map<Integer, Integer> archiveInfo = new HashMap<>();
	
	public HeroArchivesEntity() {
		
	}
	
	public HeroArchivesEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getArchives() {
		return archives;
	}

	public void setArchives(String archives) {
		this.archives = archives;
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
	public void afterRead() {
		archiveInfo = SerializeHelper.stringToMap(archives);
	}

	@Override
	public void beforeWrite() {
		archives = SerializeHelper.mapToString(archiveInfo);
	}
	
	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String arg0) {
		throw new UnsupportedOperationException();		
	}
	
	public String getOwnerKey() {
		return playerId;
	}

	public Map<Integer, Integer> getArchiveInfo() {
		return archiveInfo;
	}

	/**
	 * 获取英雄档案等级
	 * @param heroId
	 * @return
	 */
	public int getArchiveLevel(int heroId) {
		return archiveInfo.getOrDefault(heroId, 0);
	}

	/**
	 * 获取解锁的英雄档案
	 * @return
	 */
	public Set<Integer> getUnlockAchives() {
		return archiveInfo.keySet();
	}

	/**
	 * 更新英雄档案
	 * @param heroId
	 */
	public void updateArchive(int heroId) {
		int afterLevel = getArchiveLevel(heroId) + 1;
		archiveInfo.put(heroId, afterLevel);
		notifyUpdate();
	}
}
