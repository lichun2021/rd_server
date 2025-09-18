package com.hawk.game.module.homeland.entity;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;


@Entity
@Table(name = "player_homeland")
public class PlayerHomeLandEntity extends HawkDBEntity {
    @Id
    @GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
    private String id;
    @Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
    private String playerId;

    @Column(name = "theme_id", nullable = false)
    @IndexProp(id = 3)
    private int themeId = 0; // 当前主题ID，默认为1

    @Column(name = "prosperity", nullable = false)
    @IndexProp(id = 4)
    private long prosperity = 0; // 历史最高繁荣度

    @Column(name = "buildingData", nullable = false)
    @IndexProp(id = 5)
    private String buildingData = ""; // 所有建筑数据，以JSON字符串存储

    @Column(name = "warehouseData", nullable = false)
    @IndexProp(id = 6)
    private String warehouseData = ""; // 仓库中的建筑，JSON字符串
    //图鉴
    @Column(name = "buildingCollect", nullable = false)
    @IndexProp(id = 7)
    private String buildingCollect = ""; // 图鉴

    @Column(name = "createTime", nullable = false)
    @IndexProp(id = 8)
    private long createTime = 0;

    @Column(name = "updateTime", nullable = false)
    @IndexProp(id = 9)
    private long updateTime;

    @Column(name = "invalid")
    @IndexProp(id = 10)
    private boolean invalid;
    //被点赞数
    @Column(name = "likes")
    @IndexProp(id = 11)
    private int likes;
    //点赞玩家ID
    @Column(name = "dailyLike", nullable = false)
    @IndexProp(id = 12)
    private String dailyLikes = "";
    //每日重置点赞时间戳
    @Column(name = "lastDailyLikeTime")
    @IndexProp(id = 13)
    private long lastDailyLikeTime;
    //解锁的主题
    @Column(name = "themes", nullable = false)
    @IndexProp(id = 14)
    private String themes = "";
    //已激活繁荣度属性
    @Column(name = "activeProsperityAttr", nullable = false)
    @IndexProp(id = 15)
    private String activeProsperityAttr = "";
    //上次分享时间
    @Column(name = "shareTime")
    @IndexProp(id = 16)
    private long shareTime;
    //商店
    @Column(name = "shopInfo")
    @IndexProp(id = 17)
    private String shopInfo;
    @Transient
    HomeLandComponent component;

    @Override
    public void beforeWrite() {
        if (component != null) {
            this.buildingData = component.getMapBuildComp().serializ();
            this.warehouseData = component.getWareHouseComp().serializ();
            this.dailyLikes = component.getLikeComp().serializ();
            this.buildingCollect = component.getCollectComp().serializ();
            this.themes = component.getThemeComp().serializ();
            this.activeProsperityAttr = component.getAttrComp().serializ();
            this.shopInfo = component.getShopComp().serializ();
        }
        super.beforeWrite();
    }

    @Override
    public void afterRead() {
        HomeLandComponent.create(this);
        super.afterRead();
    }

    public void recordHomeLandObj(HomeLandComponent component) {
        this.component = component;
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

    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    //历史最高
    public long getProsperity() {
        return prosperity;
    }

    public void setProsperity(long prosperity) {
        this.prosperity = prosperity;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    @Override
    public String getPrimaryKey() {
        return id;
    }

    @Override
    public void setPrimaryKey(String primaryKey) {
        this.id = primaryKey;

    }

    public String getOwnerKey() {
        return playerId;
    }


    public long getLastDailyLikeTime() {
        return lastDailyLikeTime;
    }

    public void setLastDailyLikeTime(long lastDailyLikeTime) {
        this.lastDailyLikeTime = lastDailyLikeTime;
    }

    public void setShareTime(long shareTime) {
        this.shareTime = shareTime;
    }

    public long getShareTime() {
        return shareTime;
    }

    public String getBuildingData() {
        return buildingData;
    }

    public String getWarehouseData() {
        return warehouseData;
    }

    public String getBuildingCollect() {
        return buildingCollect;
    }

    public String getDailyLikes() {
        return dailyLikes;
    }

    public String getThemes() {
        return themes;
    }

    public String getActiveProsperityAttr() {
        return activeProsperityAttr;
    }

    public HomeLandComponent getComponent() {
        if (component == null) {
            throw new RuntimeException("HomeLandComponent init failed");
        }
        return component;
    }

    public String getShopInfo() {
        return shopInfo;
    }
}
