package com.hawk.activity.type.impl.heavenBlessing.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_heaven_blessing")
public class HeavenBlessingEntity extends HawkDBEntity implements IActivityDataEntity {

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

    /** 付费组Id */
    @IndexProp(id = 7)
    @Column(name = "groupId", nullable = false)
    private int groupId;

    /** 付费次数，用于记录已经充值到当前付费组的第几个档位 */
    @IndexProp(id = 8)
    @Column(name = "payCount", nullable = false)
    private int payCount;

    /** 当前激活的档位，其实可以不用存库，为了代码简洁存库 */
    @IndexProp(id = 9)
    @Column(name = "level", nullable = false)
    private int level;

    /** 自定义奖励选择了第几个 */
    @IndexProp(id = 10)
    @Column(name = "choose", nullable = false)
    private int choose;

    /** 自定义奖励状态 */
    @IndexProp(id = 11)
    @Column(name = "customState", nullable = false)
    private int customState;

    /** 活动激活状态 */
    @IndexProp(id = 12)
    @Column(name = "activeState", nullable = false)
    private boolean activeState;

    /** 激活时间戳 */
    @IndexProp(id = 13)
    @Column(name = "activeTime", nullable = false)
    private long activeTime;

    /** 活动成就项数据 */
    @IndexProp(id = 14)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    public HeavenBlessingEntity(){

    }

    public HeavenBlessingEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.groupId = 0;
        this.payCount = 0;
        this.level = 0;
        this.choose = 0;
        this.customState = 0;
        this.activeState = false;
        this.activeTime = 0;
        this.achieveItems = "";
    }

    @Override
    public void beforeWrite() {
        //成就数据转换成字符串
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
    }

    @Override
    public void afterRead() {
    	this.itemList.clear();
        //字符串转换成成就数据
        SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
    }

    @Override
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
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

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getPayCount() {
        return payCount;
    }

    public void setPayCount(int payCount) {
        this.payCount = payCount;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getChoose() {
        return choose;
    }

    public void setChoose(int choose) {
        this.choose = choose;
    }

    public int getCustomState() {
        return customState;
    }

    public void setCustomState(int customState) {
        this.customState = customState;
    }

    public boolean isActiveState() {
        return activeState;
    }

    public void setActiveState(boolean activeState) {
        this.activeState = activeState;
    }

    public long getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(long activeTime) {
        this.activeTime = activeTime;
    }

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }
}
