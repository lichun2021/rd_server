package com.hawk.game.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 装备科技
 * @author golden
 *
 */
@Entity
@Table(name = "equip_research")
public class EquipResearchEntity extends HawkDBEntity {

	/**
	 * 唯一id，uuid
	 */
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;
	
	/**
	 * 玩家id
	 */
	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId;
	
	/**
	 * 研究id
	 */
	@Column(name = "researchId", nullable = false)
    @IndexProp(id = 3)
	private int researchId;
	
	/**
	 * 研究等级
	 */
	@Column(name = "researchLevel", nullable = false)
    @IndexProp(id = 4)
	private int researchLevel;
	
	/**
	 * 已经领取的宝箱id
	 */
	@Column(name = "receiveBox", nullable = false)
    @IndexProp(id = 5)
	private String receiveBox;
	
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
	 * 已经领取的宝箱id
	 */
	@Transient
	private Set<Integer> receiveBoxSet = new ConcurrentHashSet<>();
	
	
	@Override
	public void beforeWrite() {
		receiveBox = SerializeHelper.collectionToString(receiveBoxSet, SerializeHelper.BETWEEN_ITEMS);
	}

	@Override
	public void afterRead() {
		if (!HawkOSOperator.isEmptyString(receiveBox)) {
			receiveBoxSet = SerializeHelper.stringToSet(Integer.class, receiveBox, SerializeHelper.BETWEEN_ITEMS, null, new ConcurrentHashSet<>());
		}
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

	public int getResearchId() {
		return researchId;
	}

	public void setResearchId(int researchId) {
		this.researchId = researchId;
	}

	public int getResearchLevel() {
		return researchLevel;
	}

	public void setResearchLevel(int researchLevel) {
		this.researchLevel = researchLevel;
	}

	public String getReceiveBox() {
		return receiveBox;
	}

	public void setReceiveBox(String receiveBox) {
		this.receiveBox = receiveBox;
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

	public Set<Integer> getReceiveBoxSet() {
		return receiveBoxSet;
	}
	
	public void addReceiveBox(int level) {
		receiveBoxSet.add(level);
		notifyUpdate();
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
