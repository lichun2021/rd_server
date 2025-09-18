package com.hawk.activity.type.impl.backToNewFly.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "activity_back_to_new_fly")
public class BackToNewFlyEntity extends HawkDBEntity implements IActivityDataEntity, IExchangeTipEntity {
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
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 5)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 6)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;

    /** 活动成就项数据 */
    @IndexProp(id = 7)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    /** 购买数量信息 */
    @IndexProp(id = 8)
    @Column(name = "buyInfo", nullable = false)
    private String buyInfo;

    /** 兑换提醒信息 */
    @IndexProp(id = 9)
    @Column(name = "tips", nullable = false)
    private String tips;

    /**
     * 当前期开始时间
     */
    @IndexProp(id = 10)
    @Column(name = "startTime", nullable = false)
    private long startTime;

    /**
     * 当前期开始时间
     */
    @IndexProp(id = 11)
    @Column(name = "overTime", nullable = false)
    private long overTime;

    /**
     * 回流次数
     */
    @IndexProp(id = 12)
    @Column(name = "backCount", nullable = false)
    private int backCount;

    @IndexProp(id = 13)
    @Column(name = "baseLevel", nullable = false)
    private int baseLevel;

    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    /** 兑换数量 */
    @Transient
    private Map<Integer, Integer> buyNumMap = new HashMap<>();

    /** 兑换提醒 */
    @Transient
    private Set<Integer> tipSet = new HashSet<>();

    public BackToNewFlyEntity(){

    }

    public BackToNewFlyEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.achieveItems = "";
    }

    @Override
    public void beforeWrite() {
        //成就数据转换成字符串
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        //兑换数据转字符串
        this.buyInfo = SerializeHelper.mapToString(this.buyNumMap);
        this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
    }

    @Override
    public void afterRead() {
        //字符串转换成成就数据
        this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
        //字符串转兑换数据
        this.buyNumMap = SerializeHelper.stringToMap(this.buyInfo, Integer.class, Integer.class);
        this.tipSet = SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null);
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

    @Override
    public Set<Integer> getTipSet() {
        return tipSet;
    }

    @Override
    public void setTipSet(Set<Integer> tipSet) {
        this.tipSet = tipSet;
    }

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }

    public Map<Integer, Integer> getBuyNumMap() {
        return buyNumMap;
    }

    public long getOverTime() {
        return overTime;
    }

    public void setOverTime(long overTime) {
        this.overTime = overTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getBackCount() {
        return backCount;
    }

    public void setBackCount(int backCount) {
        this.backCount = backCount;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public void setBaseLevel(int baseLevel) {
        this.baseLevel = baseLevel;
    }
}
