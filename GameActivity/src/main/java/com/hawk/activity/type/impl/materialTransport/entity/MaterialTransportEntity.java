package com.hawk.activity.type.impl.materialTransport.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_material_transport")
public class MaterialTransportEntity extends HawkDBEntity implements IActivityDataEntity {
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

	// # 每日普通货车发车次数
	@IndexProp(id = 4)
	@Column(name = "truckNumber", nullable = false)
	private String truckNumber;

	// # 个人每日联盟列车参与次数
	@IndexProp(id = 5)
	@Column(name = "trainNumber", nullable = false)
	private String trainNumber;
	// # 每日普通货车抢夺次数
	@IndexProp(id = 6)
	@Column(name = "truckRobNumber", nullable = false)
	private String truckRobNumber;
	// # 每日联盟列车抢夺次数
	@IndexProp(id = 7)
	@Column(name = "trainRobNumber", nullable = false)
	private String trainRobNumber;

	@IndexProp(id = 8)
	@Column(name = "specialTrainNumber", nullable = false)
	private String specialTrainNumber;

	@IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;

	@IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

	@IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	// # 每日普通货车发车次数
	@Transient
	private Map<Integer, Integer> truckNumberMap = new HashMap<>();
	// # 个人每日联盟列车参与次数
	@Transient
	private Map<Integer, Integer> trainNumberMap = new HashMap<>();
	// # 每日普通货车抢夺次数
	@Transient
	private Map<Integer, Integer> truckRobNumberMap = new HashMap<>();
	// # 每日联盟列车抢夺次数
	@Transient
	private Map<Integer, Integer> trainRobNumberMap = new HashMap<>();
	@Transient
	private Map<Integer, Integer> specialTrainNumberMap = new HashMap<>();

	public MaterialTransportEntity() {
	}

	public MaterialTransportEntity(String playerId) {
		this.playerId = playerId;
	}

	public MaterialTransportEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	public int getNumber(NumberType type) {
		int yearDay = HawkTime.getYearDay();
		switch (type) {
		case truckNumberMap:
			return truckNumberMap.getOrDefault(yearDay, 0);
		case trainNumberMap:
			return trainNumberMap.getOrDefault(yearDay, 0);
		case specialTrainNumberMap:
			return specialTrainNumberMap.getOrDefault(yearDay, 0);
		case truckRobNumberMap:
			return truckRobNumberMap.getOrDefault(yearDay, 0);

		case trainRobNumberMap:
			return trainRobNumberMap.getOrDefault(yearDay, 0);

		default:
			break;
		}
		return 0;
	}

	public int incNumber(NumberType type) {
		int yearDay = HawkTime.getYearDay();
		notifyUpdate();
		switch (type) {
		case truckNumberMap:
			return truckNumberMap.merge(yearDay, 1, (v1, v2) -> v1 + v2);
		case trainNumberMap:
			return trainNumberMap.merge(yearDay, 1, (v1, v2) -> v1 + v2);
		case specialTrainNumberMap:
			return specialTrainNumberMap.merge(yearDay, 1, (v1, v2) -> v1 + v2);

		case truckRobNumberMap:
			return truckRobNumberMap.merge(yearDay, 1, (v1, v2) -> v1 + v2);

		case trainRobNumberMap:
			return trainRobNumberMap.merge(yearDay, 1, (v1, v2) -> v1 + v2);

		default:
			break;
		}
		return 0;
	}

	@Override
	public void beforeWrite() {
		truckNumber = SerializeHelper.mapToString(truckNumberMap);
		trainNumber = SerializeHelper.mapToString(trainNumberMap);
		specialTrainNumber = SerializeHelper.mapToString(specialTrainNumberMap);
		truckRobNumber = SerializeHelper.mapToString(truckRobNumberMap);
		trainRobNumber = SerializeHelper.mapToString(trainRobNumberMap);
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		truckNumberMap = SerializeHelper.stringToMap(truckNumber);
		trainNumberMap = SerializeHelper.stringToMap(trainNumber);
		specialTrainNumberMap = SerializeHelper.stringToMap(specialTrainNumber);
		truckRobNumberMap = SerializeHelper.stringToMap(truckRobNumber);
		trainRobNumberMap = SerializeHelper.stringToMap(trainRobNumber);
		super.afterRead();
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
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

	public String getTruckNumber() {
		return truckNumber;
	}

	public void setTruckNumber(String truckNumber) {
		this.truckNumber = truckNumber;
	}

	public String getTrainNumber() {
		return trainNumber;
	}

	public void setTrainNumber(String trainNumber) {
		this.trainNumber = trainNumber;
	}

	public String getTruckRobNumber() {
		return truckRobNumber;
	}

	public void setTruckRobNumber(String truckRobNumber) {
		this.truckRobNumber = truckRobNumber;
	}

	public String getTrainRobNumber() {
		return trainRobNumber;
	}

	public void setTrainRobNumber(String trainRobNumber) {
		this.trainRobNumber = trainRobNumber;
	}

	public Map<Integer, Integer> getTruckNumberMap() {
		return truckNumberMap;
	}

	public void setTruckNumberMap(Map<Integer, Integer> truckNumberMap) {
		this.truckNumberMap = truckNumberMap;
	}

	public Map<Integer, Integer> getTrainNumberMap() {
		return trainNumberMap;
	}

	public void setTrainNumberMap(Map<Integer, Integer> trainNumberMap) {
		this.trainNumberMap = trainNumberMap;
	}

	public Map<Integer, Integer> getTruckRobNumberMap() {
		return truckRobNumberMap;
	}

	public void setTruckRobNumberMap(Map<Integer, Integer> truckRobNumberMap) {
		this.truckRobNumberMap = truckRobNumberMap;
	}

	public Map<Integer, Integer> getTrainRobNumberMap() {
		return trainRobNumberMap;
	}

	public void setTrainRobNumberMap(Map<Integer, Integer> trainRobNumberMap) {
		this.trainRobNumberMap = trainRobNumberMap;
	}

}
