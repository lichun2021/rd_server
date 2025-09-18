package com.hawk.game.entity;

import com.hawk.game.config.XQHXTalentLevelCfg;
import com.hawk.game.protocol.Talent.*;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "xqhx_talent")
public class PlayerXQHXTalentEntity extends HawkDBEntity {
    @Id
    @GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
    private String id;

    @Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
    private String playerId = "";

    @Column(name = "talentId", nullable = false)
    @IndexProp(id = 3)
    private int talentId;

    @Column(name = "level", nullable = false)
    @IndexProp(id = 4)
    private int level;

    @Column(name = "createTime", nullable = false)
    @IndexProp(id = 5)
    protected long createTime = 0;

    @Column(name = "updateTime")
    @IndexProp(id = 6)
    protected long updateTime = 0;

    @Column(name = "invalid")
    @IndexProp(id = 7)
    protected boolean invalid;

    public PlayerXQHXTalentEntity() {
    }

    public PlayerXQHXTalentEntity(String playerId, int talentId) {
        this.playerId = playerId;
        this.talentId = talentId;
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
    public String getOwnerKey() {
        return playerId;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(long updateTime) {
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

    public int getTalentId() {
        return talentId;
    }

    public void setTalentId(int talentId) {
        this.talentId = talentId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public XQHXTalentItem.Builder toPB(){
        XQHXTalentItem.Builder item =  XQHXTalentItem.newBuilder();
        item.setLevel(level);
        item.setTalentId(talentId);
        return item;
    }

    public XQHXTalentLevelCfg getCfg(){
        return XQHXTalentLevelCfg.getCfgByTalentIdAndLevel(talentId, level);
    }

    public XQHXTalentLevelCfg getNextCfg(){
        return XQHXTalentLevelCfg.getCfgByTalentIdAndLevel(talentId, level + 1);
    }
}
