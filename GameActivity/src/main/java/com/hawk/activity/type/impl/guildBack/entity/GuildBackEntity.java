package com.hawk.activity.type.impl.guildBack.entity;

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
@Table(name = "activity_guild_back")
public class GuildBackEntity extends HawkDBEntity implements IActivityDataEntity, IExchangeTipEntity {
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

    @IndexProp(id = 8)
    @Column(name = "resetTime", nullable = false)
    private long resetTime;

    /** 购买数量信息 */
    @IndexProp(id = 9)
    @Column(name = "buyInfo", nullable = false)
    private String buyInfo;

    /** 兑换提醒信息 */
    @IndexProp(id = 10)
    @Column(name = "tips", nullable = false)
    private String tips;

    @IndexProp(id = 11)
    @Column(name = "getBox", nullable = false)
    private int getBox;

    @IndexProp(id = 12)
    @Column(name = "useBox", nullable = false)
    private int useBox;

    @IndexProp(id = 13)
    @Column(name = "dayBoxTime", nullable = false)
    private long dayBoxTime;

    @IndexProp(id = 14)
    @Column(name = "dropCount", nullable = false)
    private int dropCount;

    @IndexProp(id = 15)
    @Column(name = "dayPoolCount", nullable = false)
    private int dayPoolCount;

    @IndexProp(id = 16)
    @Column(name = "goldNum", nullable = false)
    private int goldNum;

    @IndexProp(id = 17)
    @Column(name = "vitNum", nullable = false)
    private int vitNum;

    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    /** 兑换数量 */
    @Transient
    private Map<Integer, Integer> buyNumMap = new HashMap<>();

    /** 兑换提醒 */
    @Transient
    private Set<Integer> tipSet = new HashSet<>();

    public GuildBackEntity(){

    }

    public GuildBackEntity(String playerId, int termId) {
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
        this.tipSet = SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null, null);
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

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }

    public long getResetTime() {
        return resetTime;
    }

    public void setResetTime(long resetTime) {
        this.resetTime = resetTime;
    }

    public Map<Integer, Integer> getBuyNumMap() {
        return buyNumMap;
    }

    @Override
    public Set<Integer> getTipSet() {
        return tipSet;
    }

    @Override
    public void setTipSet(Set<Integer> tipSet) {
        this.tipSet = tipSet;
    }

    public int getGetBox() {
        return getBox;
    }

    public void setGetBox(int getBox) {
        this.getBox = getBox;
    }

    public int getUseBox() {
        return useBox;
    }

    public void setUseBox(int useBox) {
        this.useBox = useBox;
    }

    public long getDayBoxTime() {
        return dayBoxTime;
    }

    public void setDayBoxTime(long dayBoxTime) {
        this.dayBoxTime = dayBoxTime;
    }

    public int getDropCount() {
        return dropCount;
    }

    public void setDropCount(int dropCount) {
        this.dropCount = dropCount;
    }

    public int getDayPoolCount() {
        return dayPoolCount;
    }

    public void setDayPoolCount(int dayPoolCount) {
        this.dayPoolCount = dayPoolCount;
    }

    public int getGoldNum() {
        return goldNum;
    }

    public void setGoldNum(int goldNum) {
        this.goldNum = goldNum;
    }

    public int getVitNum() {
        return vitNum;
    }

    public void setVitNum(int vitNum) {
        this.vitNum = vitNum;
    }
}
