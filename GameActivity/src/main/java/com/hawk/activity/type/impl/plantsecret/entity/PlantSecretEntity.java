package com.hawk.activity.type.impl.plantsecret.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_plant_secret")
public class PlantSecretEntity extends HawkDBEntity implements IActivityDataEntity {

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

    // 宝箱累计的成功开启次数
    @IndexProp(id = 4)
    @Column(name = "openBoxCount", nullable = false)
    private int openBoxCount;

    // 当前箱子开启了多少次
    @IndexProp(id = 5)
    @Column(name = "openBoxTimes", nullable = false)
    private int openBoxTimes;

    // 购买了多少个翻牌道具
    @IndexProp(id = 6)
    @Column(name = "buyItemCount", nullable = false)
    private int buyItemCount;

    // 已翻出的牌
    @IndexProp(id = 7)
    @Column(name = "openedCards", nullable = false)
    private String openedCards;

    // 牌下面盖着的密码
    @IndexProp(id = 8)
    @Column(name = "secret", nullable = false)
    private int secret;

    @IndexProp(id = 9)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 10)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 11)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;

    //最后一次分享世界频道的时间，用于分享冷却
    @IndexProp(id = 12)
    @Column(name = "lastShareTimeWorld", nullable = false)
    private long lastShareTimeWorld;

    //最后一次分享工会频道的时间，用于分享冷却
    @IndexProp(id = 13)
    @Column(name = "lastShareTimeGuild", nullable = false)
    private long lastShareTimeGuild;

    //世界频道分享次数
    @IndexProp(id = 14)
    @Column(name = "worldshare", nullable = false)
    private int worldshare;

    //工会频道分享次数
    @IndexProp(id = 15)
    @Column(name = "allianceshare", nullable = false)
    private int allianceshare;

    //工会频道分享次数
    @IndexProp(id = 16)
    @Column(name = "daytime", nullable = false)
    private int dayTime;

    //工会频道分享次数
    @IndexProp(id = 17)
    @Column(name = "dailyopenbox", nullable = false)
    private int dailyOpenBox;

    @Transient
    private List<Integer> openedCardList = new ArrayList<Integer>();

    public PlantSecretEntity() {
    }

    public PlantSecretEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
    }


    @Override
    public void beforeWrite() {
        this.openedCards = SerializeHelper.collectionToString(this.openedCardList, SerializeHelper.BETWEEN_ITEMS);
    }

    @Override
    public void afterRead() {
        SerializeHelper.stringToList(Integer.class, this.openedCards, SerializeHelper.BETWEEN_ITEMS, this.openedCardList);
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
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
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

    public int getOpenBoxCount() {
        return openBoxCount;
    }

    public void setOpenBoxCount(int openBoxCount) {
        this.openBoxCount = openBoxCount;
    }

    public int getOpenBoxTimes() {
        return openBoxTimes;
    }

    public void setOpenBoxTimes(int openBoxTimes) {
        this.openBoxTimes = openBoxTimes;
    }

    public int getBuyItemCount() {
        return buyItemCount;
    }

    public void setBuyItemCount(int buyItemCount) {
        this.buyItemCount = buyItemCount;
    }

    public String getOpenedCards() {
        return openedCards;
    }

    public void setOpenedCards(String openedCards) {
        this.openedCards = openedCards;
    }

    public int getSecret() {
        return secret;
    }

    public void setSecret(int secret) {
        this.secret = secret;
    }

    public List<Integer> getOpenedCardList() {
        return openedCardList;
    }

    public long getLastShareTimeWorld() {
        return lastShareTimeWorld;
    }

    public void setLastShareTimeWorld(long lastShareTimeWorld) {
        this.lastShareTimeWorld = lastShareTimeWorld;
    }

    public long getLastShareTimeGuild() {
        return lastShareTimeGuild;
    }

    public void setLastShareTimeGuild(long lastShareTimeGuild) {
        this.lastShareTimeGuild = lastShareTimeGuild;
    }

    public int getWorldshare() {
        return worldshare;
    }

    public void setWorldshare(int worldshare) {
        this.worldshare = worldshare;
    }

    public int getAllianceshare() {
        return allianceshare;
    }

    public void setAllianceshare(int allianceshare) {
        this.allianceshare = allianceshare;
    }

    public int getDayTime() {
        return dayTime;
    }

    public void setDayTime(int dayTime) {
        this.dayTime = dayTime;
    }

    public int getDailyOpenBox() {
        return dailyOpenBox;
    }

    public void setDailyOpenBox(int dailyOpenBox) {
        this.dailyOpenBox = dailyOpenBox;
    }
}
