package com.hawk.activity.type.impl.shareprosperity.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.shareprosperity.cfg.ShareProsperityKVCfg;
import com.hawk.common.AccountRoleInfo;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

@Entity
@Table(name = "activity_share_prosperity")
public class ShareProsperityEntity extends HawkDBEntity implements IActivityDataEntity {
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
    
    /**
     * 触发活动开始时间
     */
    @IndexProp(id = 4)
    @Column(name = "startTime", nullable = false)
    private long startTime;
    /**
     * 返利数量
     */
    @IndexProp(id = 5)
    @Column(name = "rebateCount", nullable = false)
    private int rebateCount;
    /**
     * 绑定的老玩家
     */
    @IndexProp(id = 6)
    @Column(name = "bindOldPlayer", nullable = false)
    private String bindOldPlayer = "";

	@IndexProp(id = 7)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 8)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 9)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;
    
    @Transient
    private Map<String, AccountRoleInfo> roleInfoMap = new HashMap<>();
    @Transient
    private long roleInfoRefreshTime;
    

    public ShareProsperityEntity(){
    }

    public ShareProsperityEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
    }

    @Override
    public void beforeWrite() {
    }

    @Override
    public void afterRead() {
    }

    @Override
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public String getPrimaryKey() {
        return id;
    }

    @Override
    public void setPrimaryKey(String primaryKey) {
        this.id = primaryKey;
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
    protected void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public long getStartTime() {
        return startTime;
    }
    
    public long getNextAM0Time() {
    	return HawkTime.getAM0Date(new Date(startTime)).getTime() + HawkTime.DAY_MILLI_SECONDS;
    }
    
    public long getEndTime() {
    	long lastTimeLong = ShareProsperityKVCfg.getInstance().getLastTimeLong();
    	return this.getNextAM0Time() + lastTimeLong;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getRebateCount() {
		return rebateCount;
	}

	public void setRebateCount(int rebateCount) {
		this.rebateCount = rebateCount;
	}
	
	public String getBindOldPlayer() {
		return bindOldPlayer;
	}

	public void setBindOldPlayer(String bindOldPlayer) {
		this.bindOldPlayer = bindOldPlayer;
	}

	public long getRoleInfoRefreshTime() {
		return roleInfoRefreshTime;
	}

	public void setRoleInfoRefreshTime(long roleInfoRefreshTime) {
		this.roleInfoRefreshTime = roleInfoRefreshTime;
	}

	public Map<String, AccountRoleInfo> getRoleInfoMap() {
		return roleInfoMap;
	}
	
}
