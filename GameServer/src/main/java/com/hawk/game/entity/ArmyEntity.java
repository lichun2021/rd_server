package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.GsConfig;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.protocol.Army.ArmyInfoPB;

/**
 * 军队实体
 *
 * @author lating
 */
@Entity
@Table(name = "army")
public class ArmyEntity extends HawkDBEntity implements Comparable<ArmyEntity> {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "armyId", nullable = false)
    @IndexProp(id = 3)
	private int armyId;

	@Column(name = "free")
    @IndexProp(id = 4)
	private int free;

	@Column(name = "trainCount")
    @IndexProp(id = 5)
	private int trainCount;

	@Column(name = "trainFinishCount")
    @IndexProp(id = 6)
	private int trainFinishCount;

	@Column(name = "march")
    @IndexProp(id = 7)
	private int march;

	@Column(name = "woundedCount")
    @IndexProp(id = 8)
	private int woundedCount;
	
	/**
	 * 超时空救援站中数量
	 */
	@Column(name = "taralabsCount")
    @IndexProp(id = 9)
	private int taralabsCount;

	@Column(name = "cureCount")
    @IndexProp(id = 10)
	private int cureCount;

	@Column(name = "cureFinishCount")
    @IndexProp(id = 11)
	private int cureFinishCount;

	@Column(name = "trainLatest")
    @IndexProp(id = 12)
	private boolean trainLatest;

	// 最近一次训练此兵种的时间
	@Column(name = "lastTrainTime")
    @IndexProp(id = 13)
	private long lastTrainTime;
	
	@Column(name = "advancePower")
    @IndexProp(id = 14)
	private double advancePower;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 15)
	private long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 16)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 17)
	private boolean invalid;
	
	// 国家医院中存有的待复活死兵
	@Column(name = "nationalHospitalDeadCount")
    @IndexProp(id = 18)
	private int nationalHospitalDeadCount;
	
	// 国家医院中已复活过来待送走的兵
	@Column(name = "nationalHospitalRecoveredCount")
    @IndexProp(id = 19)
	private int nationalHospitalRecoveredCount;

	// 国家医院中的统帅之战死兵数量
	@Column(name = "tszzDeadCount")
    @IndexProp(id = 20)
	private int tszzDeadCount;
	
	@Column(name = "tszzRecoveredCount")
    @IndexProp(id = 21)
	private int tszzRecoveredCount;
		

	public ArmyEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}
	
	@Override
	public String toString() {
		return String.format("%d_%d_%d_%d_%d_%d_%d_%d", armyId, free, march, woundedCount, cureCount, cureFinishCount, trainCount, trainFinishCount);
	}

	/**
	 * 创建军队builder
	 * 
	 * @param army
	 * @return
	 */
	public ArmyInfoPB.Builder toProtoBuilder() {
		ArmyInfoPB.Builder builder = ArmyInfoPB.newBuilder();
		builder.setId(this.getId());
		builder.setArmyId(this.getArmyId());
		builder.setFreeCount(this.getFree());
		builder.setLatest(this.isTrainLatest());
		builder.setLastTrainTime(this.getLastTrainTime());
		if (this.getMarch() > 0) {
			builder.setMarchCount(this.getMarch());
		}
		
		if (this.getWoundedCount() > 0) {
			builder.setWoundedCount(this.getWoundedCount());
		}
		
		if (this.getCureCount() > 0) {
			builder.setCureCount(this.getCureCount());
		}
		
		if (this.getCureFinishCount() > 0) {
			builder.setCureFinishCount(this.getCureFinishCount());
		}
		
		if (this.getTrainCount() > 0) {
			builder.setInTrainCount(this.getTrainCount());
		}
		
		if (this.getTrainFinishCount() > 0) {
			builder.setFinishTrainCount(this.getTrainFinishCount());
		}
		
		if (this.getTaralabsCount() > 0) {
			builder.setTaralabsCount(this.getTaralabsCount());
		}
		
		return builder;
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

	public int getArmyId() {
		return armyId;
	}

	public void setArmyId(int armyId) {
		this.armyId = armyId;
	}

	public int getFree() {
		return free;
	}

	private void setFree(int free) {
		this.free = free;
	}

	public int getMarch() {
		return march;
	}

	private void setMarch(int march) {
		this.march = march;
	}

	public int getWoundedCount() {
		return woundedCount;
	}

	private void setWoundedCount(int woundedCount) {
		this.woundedCount = woundedCount;
	}
	
	public int getTaralabsCount() {
		return taralabsCount;
	}

	private void setTaralabsCount(int taralabsCount) {
		this.taralabsCount = taralabsCount;
	}

	public int getCureCount() {
		return cureCount;
	}

	private void setCureCount(int cureCount) {
		this.cureCount = cureCount;
	}

	public int getCureFinishCount() {
		return cureFinishCount;
	}

	public void setCureFinishCount(int cureFinishCount) {
		this.cureFinishCount = cureFinishCount;
	}

	public int getTrainCount() {
		return trainCount;
	}

	public void setTrainCount(int trainCount) {
		this.trainCount = trainCount;
	}

	public int getTrainFinishCount() {
		return trainFinishCount;
	}

	public double getAdvancePower() {
		return advancePower;
	}

	public void setAdvancePower(double advancePower) {
		this.advancePower = advancePower;
	}

	public void setTrainFinishCount(int trainFinishCount) {
		this.trainFinishCount = trainFinishCount;
	}

	public boolean isTrainLatest() {
		return trainLatest;
	}

	public void setTrainLatest(boolean trainLatest) {
		this.trainLatest = trainLatest;
	}

	public long getLastTrainTime() {
		return lastTrainTime;
	}

	public void setLastTrainTime(long lastTrainTime) {
		this.lastTrainTime = lastTrainTime;
	}

	public int getTotal() {
		return free + march + woundedCount + cureCount + cureFinishCount;
	}

	public synchronized void addFree(int free) {
		int num = this.free + free;
		if (num < 0) {
			throw new RuntimeException("army entity set error id :" + id + ", armyId : " + armyId + "+, free : " + num);
		}
		this.setFree(num);
	}

	public synchronized void addFreeWithLimit(int free) {
		if (this.free > free) {
			this.setFree(free);
		}
	}

	public synchronized void clearFree() {
		this.setFree(0);
	}
	
	public synchronized void addMarch(int march) {
		int num = this.march + march;
		
		if (num < 0 && Math.abs(num) > GameConstCfg.getInstance().getArmyFixFactor()) {
			throw new RuntimeException("army entity set error, march : " + num);
		}
		
		if (num < 0) {
			this.setMarch(0);
		} else {
			this.setMarch(num);
		}
	}
	
	public synchronized void clearMarch() {
		this.setMarch(0);
	}
	
	public synchronized void addWoundedCount(int woundedCount) {
		int num = this.woundedCount + woundedCount;
		if (num < 0) {
			throw new RuntimeException("army entity set error, woundedCount : " + num);
		}
		this.setWoundedCount(num);
	}
	
	public synchronized void immSetWoundedCountWithoutSync(int woundedCount) {
		setWoundedCount(woundedCount);
	}

	public synchronized void addCureCount(int cureCount) {
		int num = this.cureCount + cureCount;
		if (num < 0) {
			throw new RuntimeException("army entity set error, cureCount : " + num);
		}
		this.setCureCount(num);
	}
	
	public synchronized void immSetCureCountWithoutSync(int cureCount) {
		setCureCount(cureCount);
	}
	
	public synchronized void addTaralabsCount(int taralabsCount) {
		int num = this.taralabsCount + taralabsCount;
		if (num < 0) {
			throw new RuntimeException("army entity set error, taralabsCount : " + num);
		}
		this.setTaralabsCount(num);
	}
	
	public synchronized void immSetTaralabsCountWithoutSync(int taralabsCount) {
		setTaralabsCount(taralabsCount);
	}

	@Override
	public int compareTo(ArmyEntity target) {
		BattleSoldierCfg thisCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		BattleSoldierCfg targetCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, target.armyId);

		if (thisCfg.getLevel() < targetCfg.getLevel()) {
			return -1;
		}

		if (thisCfg.getLevel() > targetCfg.getLevel()) {
			return 1;
		}

		if (thisCfg.getType() > targetCfg.getType()) {
			return -1;
		}

		if (thisCfg.getType() < targetCfg.getType()) {
			return 1;
		}
		return 0;
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
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
	
	public boolean isPlantSoldier(){
		BattleSoldierCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(BattleSoldierCfg.class, this.getArmyId());
    	return cfg.isPlantSoldier();
	}

	public int getNationalHospitalDeadCount() {
		return nationalHospitalDeadCount;
	}

	public void setNationalHospitalDeadCount(int nationalHospitalDeadCount) {
		this.nationalHospitalDeadCount = nationalHospitalDeadCount;
	}

	public int getNationalHospitalRecoveredCount() {
		return nationalHospitalRecoveredCount;
	}

	public void setNationalHospitalRecoveredCount(int nationalHospitalRecoveredCount) {
		this.nationalHospitalRecoveredCount = nationalHospitalRecoveredCount;
	}
	
	public int getTszzDeadCount() {
		return tszzDeadCount;
	}

	public void setTszzDeadCount(int tszzDeadCount) {
		this.tszzDeadCount = tszzDeadCount;
	}
	
	public int getTszzRecoveredCount() {
		return tszzRecoveredCount;
	}

	public void setTszzRecoveredCount(int tszzRecoveredCount) {
		this.tszzRecoveredCount = tszzRecoveredCount;
	}
	
}
