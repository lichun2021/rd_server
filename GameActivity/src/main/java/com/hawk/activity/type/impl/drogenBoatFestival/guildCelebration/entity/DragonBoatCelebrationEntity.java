package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_dragon_boat_celebration")
public class DragonBoatCelebrationEntity  extends AchieveActivityEntity implements IActivityDataEntity {

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
	
    @IndexProp(id = 4)
	@Column(name = "exchangeItems", nullable = false)
	private String exchangeItems;
	
	/**野外采集上次累计的时间*/
    @IndexProp(id = 5)
	@Column(name="wolrdCollectRemainTime", nullable = false)
	private int wolrdCollectRemainTime;
	/**
	 * 世界资源收集的次数
	 */
    @IndexProp(id = 6)
	@Column(name="wolrdCollectTimes", nullable = false)
	private int wolrdCollectTimes;
	
	
	/**击败尤里的次数*/
    @IndexProp(id = 7)
	@Column(name="beatYuriTimes", nullable = false)
	private int beatYuriTimes;
	
	/**击败尤里的总次数*/
    @IndexProp(id = 8)
	@Column(name="beatYuriTotalTimes", nullable = false)
	private int beatYuriTotalTimes;
	
	
	
	/**联盟捐献的次数*/
    @IndexProp(id = 9)
	@Column(name="guildDonateTimes", nullable = false)
	private int guildDonateTimes;
	
	
	/**联盟捐献的次数*/
    @IndexProp(id = 10)
	@Column(name="guildDonateTotalTimes", nullable = false)
	private int guildDonateTotalTimes;
	
	
    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	/** 兑换信息 **/
	@Transient
	private Map<Integer, Integer> exchanges = new ConcurrentHashMap<Integer, Integer>();

	public DragonBoatCelebrationEntity() {
	}
	
	
	
	public DragonBoatCelebrationEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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
	
	

	public int getWolrdCollectTimes() {
		return wolrdCollectTimes;
	}


	public void setWolrdCollectTimes(int wolrdCollectTimes) {
		this.wolrdCollectTimes = wolrdCollectTimes;
	}

	
	


	public int getWolrdCollectRemainTime() {
		return wolrdCollectRemainTime;
	}



	public void setWolrdCollectRemainTime(int wolrdCollectRemainTime) {
		this.wolrdCollectRemainTime = wolrdCollectRemainTime;
	}



	public int getBeatYuriTimes() {
		return beatYuriTimes;
	}



	public void setBeatYuriTimes(int beatYuriTimes) {
		this.beatYuriTimes = beatYuriTimes;
	}

	
	

	

	public int getBeatYuriTotalTimes() {
		return beatYuriTotalTimes;
	}



	public void setBeatYuriTotalTimes(int beatYuriTotalTimes) {
		this.beatYuriTotalTimes = beatYuriTotalTimes;
	}



	public int getGuildDonateTimes() {
		return guildDonateTimes;
	}



	public void setGuildDonateTimes(int guildDonateTimes) {
		this.guildDonateTimes = guildDonateTimes;
	}
	
	
	

	

	public int getGuildDonateTotalTimes() {
		return guildDonateTotalTimes;
	}



	public void setGuildDonateTotalTimes(int guildDonateTotalTimes) {
		this.guildDonateTotalTimes = guildDonateTotalTimes;
	}



	public Map<Integer, Integer> getExchanges() {
		return exchanges;
	}



	public void setExchanges(Map<Integer, Integer> exchanges) {
		this.exchanges = exchanges;
	}



	public int getExchangeCount(int eid){
		if(this.exchanges.containsKey(eid)){
			return this.exchanges.get(eid);
		}
		return 0;
	}
	
	
	public void addExchangeCount(int eid,int count){
		if(count <=0){
			return;
		}
		count += this.getExchangeCount(eid);
		this.exchanges.put(eid, count);
		this.notifyUpdate();
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
		this.exchangeItems = SerializeHelper.mapToString(this.exchanges);
	}
	
	@Override
	public void afterRead() {
		SerializeHelper.stringToMap(this.exchangeItems, Integer.class, Integer.class,this.exchanges);
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
