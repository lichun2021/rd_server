package com.hawk.activity.type.impl.hongfugift.entity;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 红福利包
 * @author hf
 */
@Entity
@Table(name = "activity_hongfu_gift")
public class HongFuGiftEntity extends ActivityDataEntity {

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
    @Column(name = "loginDays", nullable = false)
    private String loginDays;

    /**礼包信息*/
    @IndexProp(id = 5)
    @Column(name = "hongFuInfo", unique = true, nullable = false)
    private String hongFuInfo;

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
    private List<HongFuInfo> hongFuInfoList = new ArrayList<>();

    public HongFuGiftEntity(){
        
    }
    public HongFuGiftEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.hongFuInfo = "";
        this.loginDays = "";
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

    @Override
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

    /**
     * 根据礼包id获取礼包信息,没有则创建
     * @param id
     * @return
     */
    public HongFuInfo getHongFuInfoById(int id){
        Optional<HongFuInfo> optional = hongFuInfoList.stream().filter(data -> data.getId() == id).findAny();
        if (!optional.isPresent()){
            HongFuInfo info = HongFuInfo.valueOf(id);
            hongFuInfoList.add(info);
            notifyUpdate();
            return info;
        }
        return optional.get();
    }

    @Override
    public void beforeWrite() {
        this.hongFuInfo = SerializeHelper.collectionToString(this.hongFuInfoList, SerializeHelper.ELEMENT_DELIMITER);
    }

    @Override
    public void afterRead() {
    	this.hongFuInfoList.clear();
        SerializeHelper.stringToList(HongFuInfo.class, this.hongFuInfo, this.hongFuInfoList);
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
    public void setLoginDaysStr(String loginDays) {
        this.loginDays = loginDays;
    }

    @Override
    public String getLoginDaysStr() {
        return loginDays;
    }


}
