package com.hawk.activity.type.impl.newStart.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "activity_new_start")
public class NewStartEntity extends HawkDBEntity implements IActivityDataEntity {
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

    /**
     * 当前期开始时间
     */
    @IndexProp(id = 7)
    @Column(name = "startTime", nullable = false)
    private long startTime;

    /**
     * 当前期开始时间
     */
    @IndexProp(id = 8)
    @Column(name = "overTime", nullable = false)
    private long overTime;

    @IndexProp(id = 9)
    @Column(name = "isActive", nullable = false)
    private boolean isActive;

    @IndexProp(id = 10)
    @Column(name = "isBind", nullable = false)
    private boolean isBind;

    @IndexProp(id = 11)
    @Column(name = "playerLevel", nullable = false)
    private int playerLevel;

    @IndexProp(id = 12)
    @Column(name = "vipLevel", nullable = false)
    private int vipLevel;

    @IndexProp(id = 13)
    @Column(name = "baseLevel", nullable = false)
    private int baseLevel;

    @IndexProp(id = 14)
    @Column(name = "heroCount", nullable = false)
    private int heroCount;

    @IndexProp(id = 15)
    @Column(name = "equipTechLevel", nullable = false)
    private int equipTechLevel;

    @IndexProp(id = 16)
    @Column(name = "jijiaLevel", nullable = false)
    private int jijiaLevel;

    @IndexProp(id = 17)
    @Column(name = "name", nullable = false)
    private String name = "";

    @IndexProp(id = 18)
    @Column(name = "icon", nullable = false)
    private int icon;

    @IndexProp(id = 19)
    @Column(name = "pfIcon", nullable = false)
    private String pfIcon = "";

    @IndexProp(id = 20)
    @Column(name = "oldPlayerId", nullable = false)
    private String oldPlayerId = "";

    @IndexProp(id = 21)
    @Column(name = "oldServerId", nullable = false)
    private String oldServerId = "";

    @IndexProp(id = 22)
    @Column(name = "cfgInfo", nullable = false)
    private String cfgInfo = "";

    @IndexProp(id = 23)
    @Column(name = "awardInfo", nullable = false)
    private String awardInfo = "";

    @Transient
    private Map<Integer, Integer> cfgMap = new HashMap<>();

    @Transient
    private Map<Integer, Integer> awardMap = new HashMap<>();

    public NewStartEntity(){

    }

    public NewStartEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
    }

    @Override
    public void beforeWrite() {
        this.cfgInfo = SerializeHelper.mapToString(this.cfgMap);
        this.awardInfo = SerializeHelper.mapToString(this.awardMap);
    }

    @Override
    public void afterRead() {
        this.cfgMap = SerializeHelper.stringToMap(this.cfgInfo, Integer.class, Integer.class);
        this.awardMap = SerializeHelper.stringToMap(this.awardInfo, Integer.class, Integer.class);
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

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getOverTime() {
        return overTime;
    }

    public void setOverTime(long overTime) {
        this.overTime = overTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public void setBaseLevel(int baseLevel) {
        this.baseLevel = baseLevel;
    }

    public int getHeroCount() {
        return heroCount;
    }

    public void setHeroCount(int heroCount) {
        this.heroCount = heroCount;
    }

    public int getEquipTechLevel() {
        return equipTechLevel;
    }

    public void setEquipTechLevel(int equipTechLevel) {
        this.equipTechLevel = equipTechLevel;
    }

    public int getJijiaLevel() {
        return jijiaLevel;
    }

    public void setJijiaLevel(int jijiaLevel) {
        this.jijiaLevel = jijiaLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getPfIcon() {
        return pfIcon;
    }

    public void setPfIcon(String pfIcon) {
        this.pfIcon = pfIcon;
    }

    public String getOldPlayerId() {
        return oldPlayerId;
    }

    public void setOldPlayerId(String oldPlayerId) {
        this.oldPlayerId = oldPlayerId;
    }

    public String getOldServerId() {
        return oldServerId;
    }

    public void setOldServerId(String oldServerId) {
        this.oldServerId = oldServerId;
    }

    public Map<Integer, Integer> getCfgMap() {
        return cfgMap;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }
}
