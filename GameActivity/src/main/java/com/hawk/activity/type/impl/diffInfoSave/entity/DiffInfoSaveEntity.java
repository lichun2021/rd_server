package com.hawk.activity.type.impl.diffInfoSave.entity;

import com.hawk.activity.type.IActivityDataEntity;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;

@Entity
@Table(name = "activity_diff_info_save")
public class DiffInfoSaveEntity extends HawkDBEntity implements IActivityDataEntity {
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

    @IndexProp(id = 7)
    @Column(name = "type", nullable = false)
    private int type;

    @IndexProp(id = 8)
    @Column(name = "score", nullable = false)
    private int score;

    @IndexProp(id = 9)
    @Column(name = "popCnt", nullable = false)
    private int popCnt;

    @IndexProp(id = 10)
    @Column(name = "isEnd", nullable = false)
    private boolean isEnd;

    @IndexProp(id = 11)
    @Column(name = "clickTime", nullable = false)
    private long clickTime;

    @IndexProp(id = 12)
    @Column(name = "dotCnt", nullable = false)
    private int dotCnt;
    public DiffInfoSaveEntity(){

    }

    public DiffInfoSaveEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.type = 1;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPopCnt() {
        return popCnt;
    }

    public void setPopCnt(int popCnt) {
        this.popCnt = popCnt;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public long getClickTime() {
        return clickTime;
    }

    public void setClickTime(long clickTime) {
        this.clickTime = clickTime;
    }

    public int getDotCnt() {
        return dotCnt;
    }

    public void setDotCnt(int dotCnt) {
        this.dotCnt = dotCnt;
    }
}
