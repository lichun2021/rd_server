package com.hawk.activity.type.impl.redblueticket.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 红蓝对决翻牌活动数据
 * 
 * @author lating
 *
 */
@Entity
@Table(name = "activity_redblue_ticket")
public class RedBlueTicketActivityEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	
	// A池子已翻出的牌及奖励
    @IndexProp(id = 4)
	@Column(name = "ticketsA", nullable = false)
	private String ticketsA;
	
	// B池子已翻出的牌及奖励
    @IndexProp(id = 5)
	@Column(name = "ticketsB", nullable = false)
	private String ticketsB;
	
	// 奖池刷新次数
    @IndexProp(id = 6)
	@Column(name = "poolRefreshTimes", nullable = false)
	private int poolRefreshTimes;
	
    @IndexProp(id = 7)
	@Column(name = "started", nullable = false)
	private int started;
	
    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	/** A池子已翻出的牌 */
	@Transient
	private Map<Integer, Integer> poolATicketMap = new ConcurrentHashMap<Integer, Integer>();
	
	/** B池子已翻出的牌 */
	@Transient
	private Map<Integer, Integer> poolBTicketMap = new ConcurrentHashMap<Integer, Integer>();
	
	
	public RedBlueTicketActivityEntity() {
		
	}
	
	public RedBlueTicketActivityEntity(String playerId, int termId) {
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
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	@Override
	public void beforeWrite() {
		this.ticketsA = SerializeHelper.mapToString(this.poolATicketMap);
		this.ticketsB = SerializeHelper.mapToString(this.poolBTicketMap);
	}
	
	@Override
	public void afterRead() {
		SerializeHelper.stringToMap(this.ticketsA, Integer.class, Integer.class, this.poolATicketMap);
		SerializeHelper.stringToMap(this.ticketsB, Integer.class, Integer.class, this.poolBTicketMap);
	}

	public void addTicketToPoolA(int ticketId, int rewardId){
		poolATicketMap.put(ticketId, rewardId);
		notifyUpdate();
	}
	
	public Map<Integer, Integer> getPoolATickets() {
		return poolATicketMap;
	}
	
	public void addTicketToPoolB(int ticketId, int rewardId){
		poolBTicketMap.put(ticketId, rewardId);
		notifyUpdate();
	}
	
	public Map<Integer, Integer> getPoolBTickets() {
		return poolBTicketMap;
	}

	public int getPoolRefreshTimes() {
		return poolRefreshTimes;
	}

	public void setPoolRefreshTimes(int poolRefreshTimes) {
		this.poolRefreshTimes = poolRefreshTimes;
	}
	
	public int getStarted() {
		return started;
	}

	public void setStarted(int started) {
		this.started = started;
	}
}
