package com.hawk.activity.type.impl.gratefulBenefits.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.alliesWishing.entity.WishMember;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Table(name = "activity_grateful_benefits")
public class GratefulBenefitsEntity extends HawkDBEntity implements IActivityDataEntity {

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
     * 签到次数
     */
    @IndexProp(id = 7)
    @Column(name = "punchCount", nullable = false)
    private int punchCount;

    /**
     * 签到时间
     */
    @IndexProp(id = 8)
    @Column(name = "lastPunchTime", nullable = false)
    private long lastPunchTime;

    /**
     * 每日分享次数
     */
    @IndexProp(id = 9)
    @Column(name = "shareCount", nullable = false)
    private int shareCount;

    /**
     * 分享次数刷新时间
     */
    @IndexProp(id = 10)
    @Column(name = "shareRefreshTime", nullable = false)
    private long shareRefreshTime;

    /**
     * 邀请冷却时间
     */
    @IndexProp(id = 11)
    @Column(name = "inviteCDTime", nullable = false)
    private long inviteCDTime;

    /**
     * 帮助成员列表
     */
    @IndexProp(id = 12)
    @Column(name = "wishMembers", nullable = false)
    private String wishMembers;

    /**
     * 是否领奖
     */
    @IndexProp(id = 13)
    @Column(name = "award", nullable = false)
    private boolean award;

    /**
     * 是否第一次打开页面
     */
    @IndexProp(id = 14)
    @Column(name = "first", nullable = false)
    private boolean first;

    @Transient
    private List<WishMember> wishMemberList = new CopyOnWriteArrayList<WishMember>();

    public GratefulBenefitsEntity(){

    }

    public GratefulBenefitsEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
        this.wishMembers = "";
    }

    @Override
    public void beforeWrite() {
        this.wishMembers = WishMember.serializList(this.wishMemberList);
    }

    @Override
    public void afterRead() {
        this.wishMemberList = WishMember.mergeFromList(this.wishMembers);
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

    public int getPunchCount() {
        return punchCount;
    }

    public void setPunchCount(int punchCount) {
        this.punchCount = punchCount;
    }

    public long getLastPunchTime() {
        return lastPunchTime;
    }

    public void setLastPunchTime(long lastPunchTime) {
        this.lastPunchTime = lastPunchTime;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public long getShareRefreshTime() {
        return shareRefreshTime;
    }

    public void setShareRefreshTime(long shareRefreshTime) {
        this.shareRefreshTime = shareRefreshTime;
    }

    public long getInviteCDTime() {
        return inviteCDTime;
    }

    public void setInviteCDTime(long inviteCDTime) {
        this.inviteCDTime = inviteCDTime;
    }

    public String getWishMembers() {
        return wishMembers;
    }

    public void setWishMembers(String wishMembers) {
        this.wishMembers = wishMembers;
    }

    public boolean isAward() {
        return award;
    }

    public void setAward(boolean award) {
        this.award = award;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public List<WishMember> getWishMemberList() {
        return wishMemberList;
    }

    public void addGuildWishMember(WishMember member){
        this.wishMemberList.add(member);
        this.notifyUpdate();
    }
}
