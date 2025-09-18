package com.hawk.activity.type.impl.seasonActivity.entity;

import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonGradeLevelCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;

@Entity
@Table(name = "activity_season_guild_grade")
public class SeasonActivityGuildGradeEntity extends HawkDBEntity {
    @Id
    @GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    //联盟id
    @IndexProp(id = 2)
    @Column(name = "guildId", nullable = false)
    private String guildId = null;

    //期数
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

    //等级
    @IndexProp(id = 7)
    @Column(name = "level", nullable = false)
    private int level;

    //经验
    @IndexProp(id = 8)
    @Column(name = "exp", nullable = false)
    private int exp;

    @IndexProp(id = 9)
    @Column(name = "isReward", nullable = false)
    private boolean isReward;

    public SeasonActivityGuildGradeEntity(){

    }

    public SeasonActivityGuildGradeEntity(String guildId, int termId){
        this.guildId = guildId;
        this.termId = termId;
        this.level = 1;
    }


    public int getTermId() {
        return termId;
    }

    public String getGuildId() {
        return guildId;
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

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public boolean isReward() {
        return isReward;
    }

    public void setReward(boolean reward) {
        isReward = reward;
    }

    /**
     * 加经验
     * @param addExp 经验增加值
     */
    public void addExp(int addExp){
        //加经验
        this.exp += addExp;
        //算等级
        this.level = calLevel();
        //入库
        notifyUpdate();
    }

    //算等级
    public int calLevel(){
        //初始等级为1
        int level = 1;
        //找到当前配置
        ConfigIterator<SeasonGradeLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonGradeLevelCfg.class);
        for(SeasonGradeLevelCfg cfg : iterator){
            if(this.exp >= cfg.getLevelUpExp() && cfg.getGradeLevel() > level){
                level = cfg.getGradeLevel();
            }
        }
        //返回等级
        return level;
    }
}
