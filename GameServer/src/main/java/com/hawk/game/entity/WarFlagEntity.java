package com.hawk.game.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.item.WarFlagSignUpItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 战地旗帜
 * @author golden
 *
 */
@Entity
@Table(name = "war_flag")
public class WarFlagEntity extends HawkDBEntity {

	/**
	 * 旗帜id
	 */
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "flagId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String flagId;
	
	/**
	 * 创建者联盟id
	 */
	@Column(name = "ownerId", nullable = false)
    @IndexProp(id = 2)
	private String ownerId;
	
	/**
	 * 当前占联盟id
	 */
	@Column(name = "currentId", nullable = false)
    @IndexProp(id = 3)
	private String currentId;
	
	/**
	 * 放置时间
	 */
	@Column(name = "placeTime", nullable = false)
    @IndexProp(id = 4)
	private long placeTime;
	
	/**
	 * 生命值
	 */
	@Column(name = "life", nullable = false)
    @IndexProp(id = 5)
	private int life;
	
	/**
	 * 完成时间
	 */
	@Column(name = "completeTime", nullable = false)
    @IndexProp(id = 6)
	private long completeTime;
	
	/**
	 * 状态
	 */
	@Column(name = "state", nullable = false)
    @IndexProp(id = 7)
	private int state;
	
	/**
	 * 坐标
	 */
	@Column(name = "pointId", nullable = false)
	@IndexProp(id = 8)
	private int pointId;
	
	/**
	 * 速度
	 */
	@Column(name = "speed", nullable = false)
    @IndexProp(id = 9)
	private double speed;
	
	/**
	 * 上次建造/摧毁tick时间
	 */
	@Column(name = "lastBuildTick", nullable = false)
    @IndexProp(id = 10)
	private long lastBuildTick;
	
	/**
	 * 上次资源产出tick时间
	 */
	@Column(name = "lastResourceTick", nullable = false)
    @IndexProp(id = 11)
	private long lastResourceTick;
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 12)
	private long createTime;

	@Column(name = "updateTime", nullable = false)
    @IndexProp(id = 13)
	private long updateTime;

	@Column(name = "invalid", nullable = false)
    @IndexProp(id = 14)
	private boolean invalid;
	
	/**
	 * 旗帜序号(add 战旗二期)
	 */
	@Column(name = "ownIndex", nullable = false)
    @IndexProp(id = 15)
	private int ownIndex;
	
	/**
	 * 占领值(add 战旗二期)
	 */
	@Column(name = "occupyLife", nullable = false)
    @IndexProp(id = 16)
	private int occupyLife;

	/**
	 * 是否是母旗
	 */
	@Column(name = "centerFlag", nullable = false)
    @IndexProp(id = 17)
	private int centerFlag;
	
	/**
	 * 报名信息
	 */
	@Column(name = "signUp", nullable = false)
    @IndexProp(id = 18)
	private String signUp;
	
	/**
	 * 母旗下次tick时间
	 */
	@Column(name = "centerNextTickTime", nullable = false)
    @IndexProp(id = 19)
	private long centerNextTickTime;
	
	/**
	 * 母旗是否被激活
	 */
	@Column(name = "centerActive", nullable = false)
    @IndexProp(id = 20)
	private int centerActive;
	
	/**
	 * 旗子移除时间
	 */
	@Column(name = "removeTime", nullable = false)
    @IndexProp(id = 21)
	private long removeTime;
	
	/**
	 * 报名集合
	 */
	@Transient
	private Map<String, WarFlagSignUpItem> signUpInfos = new ConcurrentHashMap<>();
	
	public WarFlagEntity() {
		
	}
	
	public String getFlagId() {
		return flagId;
	}

	public void setFlagId(String flagId) {
		this.flagId = flagId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getCurrentId() {
		return currentId;
	}

	public void setCurrentId(String currentId) {
		this.currentId = currentId;
	}

	public long getPlaceTime() {
		return placeTime;
	}

	public void setPlaceTime(long placeTime) {
		this.placeTime = placeTime;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public long getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(long completeTime) {
		this.completeTime = completeTime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getLastBuildTick() {
		return lastBuildTick;
	}

	public void setLastBuildTick(long lastBuildTick) {
		this.lastBuildTick = lastBuildTick;
	}

	public long getLastResourceTick() {
		return lastResourceTick;
	}

	public void setLastResourceTick(long lastResourceTick) {
		this.lastResourceTick = lastResourceTick;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
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

	public int getOwnIndex() {
		return ownIndex;
	}

	public void setOwnIndex(int ownIndex) {
		this.ownIndex = ownIndex;
	}

	public int getOccupyLife() {
		return occupyLife;
	}

	public void setOccupyLife(int occupyLife) {
		this.occupyLife = occupyLife;
	}

	public int getCenterFlag() {
		return centerFlag;
	}

	public void setCenterFlag(int centerFlag) {
		this.centerFlag = centerFlag;
	}

	public int getCenterActive() {
		return centerActive;
	}

	public void setCenterActive(int centerActive) {
		this.centerActive = centerActive;
	}

	@Override
	public String getPrimaryKey() {
		return flagId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		flagId = primaryKey;
	}
	
	@Override
	public void beforeWrite() {
		signUp = SerializeHelper.mapToString(signUpInfos, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.COLON_ITEMS);
	}

	@Override
	public void afterRead() {
		if (!HawkOSOperator.isEmptyString(signUp)) {
			signUpInfos = SerializeHelper.stringToMap(signUp, String.class, WarFlagSignUpItem.class,
					SerializeHelper.BETWEEN_ITEMS, SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.COLON_ITEMS, new ConcurrentHashMap<>());
		}
	}

	public Map<String, WarFlagSignUpItem> getSignUpSet() {
		return signUpInfos;
	}

	public void clearSignUpInfo() {
		signUpInfos.clear();
		notifyUpdate();
	}
	
	public long getCenterNextTickTime() {
		return centerNextTickTime;
	}

	public void setCenterNextTickTime(long centerNextTickTime) {
		this.centerNextTickTime = centerNextTickTime;
	}

	public long getRemoveTime() {
		return removeTime;
	}

	public void setRemoveTime(long removeTime) {
		this.removeTime = removeTime;
	}
}
