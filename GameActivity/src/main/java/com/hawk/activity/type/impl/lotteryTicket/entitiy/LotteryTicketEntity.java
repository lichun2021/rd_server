package com.hawk.activity.type.impl.lotteryTicket.entitiy;

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
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_lottery_ticket")
public class LotteryTicketEntity extends HawkDBEntity implements IActivityDataEntity {
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
	@Column(name = "initTime", nullable = false)
	private long initTime;
    
    @IndexProp(id = 5)
    @Column(name = "buyMsg", nullable = false)
    private String buyMsg;
    
    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private Map<Integer, Integer> buyNumMap = new ConcurrentHashMap<>();
	
	
    public LotteryTicketEntity(){
    	
    	
    }

    public LotteryTicketEntity(String playerId, int termId) {
    	this.playerId = playerId;
		this.termId = termId;
    }
    
    @Override
    public void beforeWrite() {
    	this.buyMsg = SerializeHelper.mapToString(buyNumMap);
    }
    
    @Override
    public void afterRead() {
    	this.buyNumMap = SerializeHelper.stringToMap(this.buyMsg, Integer.class, Integer.class);
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
    public long getCreateTime() {
        return this.createTime;
    }

    @Override
    protected void setCreateTime(long createTime) {
    	this.createTime = createTime;
    }

    @Override
    public long getUpdateTime() {
        return this.updateTime;
    }

    @Override
    protected void setUpdateTime(long updateTime) {
    	this.updateTime = updateTime;
    }

    @Override
    public boolean isInvalid() {
        return this.invalid;
    }

    @Override
    protected void setInvalid(boolean invalid) {
    	this.invalid = invalid;
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
	
	public long getInitTime() {
		return initTime;
	}

    public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	public Map<Integer, Integer> getBuyNumMap() {
	    return buyNumMap;
	}


	public int getBuyCount(int buyId) {
	   return this.buyNumMap.getOrDefault(buyId, 0);
	}

	 
	public void addBuyCount(int eid, int count) {
       if (count <= 0) {
           return;
       }
       count += this.getBuyCount(eid);
       this.buyNumMap.put(eid, count);
       this.notifyUpdate();
	}
	

	
    
}
