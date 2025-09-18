package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.game.GsConfig;
import com.hawk.game.player.manhattan.ManhattanBase;
import com.hawk.game.player.manhattan.ManhattanSW;
import com.hawk.game.player.manhattan.PlayerManhattan;

@Entity
@Table(name = "manhattan")
public class ManhattanEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;
	
	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 2)
	private String playerId = "";
	
	/**
	 * 超武配置武id
	 */
	@Column(name = "swId", nullable = false)
	@IndexProp(id = 3)
	private int swId;
	
	/**
	 * 品阶
	 */
	@Column(name = "stage")
	@IndexProp(id = 4)
	private int stage;
	
	/**
	 * 部件等级
	 */
	@Column(name = "posLevel")
	@IndexProp(id = 5)
	private String posLevel = "";
	
	/**
	 * 是否已部署
	 */
	@Column(name = "deployed")
	@IndexProp(id = 6)
	private int deployed;
	
	/**
	 * 是否内城展示
	 */
	@Column(name = "cityShow")
	@IndexProp(id = 7)
	private int cityShow;
	
	/**
	 * 是否是聚能底座（聚能底座也被抽象为一个超武）
	 */
	@Column(name = "base")
	@IndexProp(id = 8)
	private int base;

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 9)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 10)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 11)
	protected boolean invalid;

	@Transient
	private PlayerManhattan playerManhattan;

	public ManhattanEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	@Override
	public void beforeWrite() {
		if (playerManhattan != null) {
			posLevel = playerManhattan.serializePosLevel();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		if (base > 0) {
			ManhattanBase.create(this);
		} else {
			ManhattanSW.create(this);
		}
		super.afterRead();
	}

	public void recordManhattanObj(PlayerManhattan manhattanObj) {
		this.playerManhattan = manhattanObj;
	}
	
	public PlayerManhattan getManhattanObj() {
		return playerManhattan;
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

	public int getSwId() {
		return swId;
	}

	public void setSwId(int swId) {
		this.swId = swId;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public String getPosLevel() {
		return posLevel;
	}

	public void setPosLevel(String posLevel) {
		this.posLevel = posLevel;
	}

	public int getDeployed() {
		return deployed;
	}

	public void setDeployed(int deployed) {
		this.deployed = deployed;
	}
	
	public int getCityShow() {
		return cityShow;
	}

	public void setCityShow(int show) {
		this.cityShow = show;
	}

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		this.base = base;
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

	public String getOwnerKey() {
		return playerId;
	}
}
