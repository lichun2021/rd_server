package com.hawk.activity.type.impl.strongestGuild.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hawk.util.JsonUtils;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.game.protocol.Activity.StrongestGuildInfo;
import com.hawk.game.protocol.Activity.StrongestGuildTarget;

/***
 * 王者联盟活动实体表
 * @author yang.rao
 *
 */
@Entity
@Table(name = "activity_strongest_guild")
public class StrongestGuildEntity extends HawkDBEntity implements IActivityDataEntity {

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
	
	/** 本期活动当前阶段 **/
    @IndexProp(id = 4)
	@Column(name = "stageId", nullable = false)
	private int stageId;
	
	/** 已经领过奖的集合，用下划线分割 **/
    @IndexProp(id = 5)
	@Column(name = "targetInfo", nullable = false)
	private String targetInfo;
	
    @IndexProp(id = 6)
	@Column(name = "score", nullable = false)
	private long score;
	
    @IndexProp(id = 7)
	@Column(name = "killScore", nullable = false)
	private long killScore;
	
    @IndexProp(id = 8)
	@Column(name = "hurtScore", nullable = false)
	private long hurtScore;
	
    @IndexProp(id = 9)
	@Column(name = "buildBattlePoint", nullable = false)
	private long buildBattlePoint;
	
    @IndexProp(id = 10)
	@Column(name = "techBattlePoint", nullable = false)
	private long techBattlePoint;

    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	/** 已经领过奖的集合列表 **/
	@Transient
	private List<TargetInfo> targetList = new ArrayList<>();
	
	/***
	 * 一个临时的战力值，每次解析战力分数都要赋值
	 */
	@Transient
	private long tempBattlePoint;
	
	public StrongestGuildEntity(){}
	
	public StrongestGuildEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		targetInfo = JsonUtils.Object2Json(targetList);
	}

	@Override
	public void afterRead() {
		targetList = JsonUtils.String2List(targetInfo, TargetInfo.class);
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
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

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public int getTermId() {
		return termId;
	}
	
	public void addScore(long score){
		this.score += score;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public long getKillScore() {
		return killScore;
	}

	public long getHurtScore() {
		return hurtScore;
	}
	
	public void addKillScore(long score){
		this.killScore += score;
	}
	
	public void addHurtScore(long score){
		this.hurtScore += score;
	}

	public long getTempBattlePoint() {
		return tempBattlePoint;
	}

	/****
	 * 重新设置战力值
	 * @param tempBattlePoint
	 */
	public void resetTempBattlePoint(long tempBattlePoint) {
		this.tempBattlePoint = tempBattlePoint;
	}
	
	
	
	public void changeStage(){
		targetInfo = "";
		targetList.clear();
		score = 0;
		killScore = 0;
		hurtScore = 0;
		notifyUpdate();
	}
	
	//初始化添加target
	public void addTarget(TargetInfo target){
		targetList.add(target);
	}
	
	public void buildTarget(StrongestGuildInfo.Builder builder){
		for(TargetInfo tar : targetList){
			StrongestGuildTarget.Builder targetBuilder = StrongestGuildTarget.newBuilder();
			targetBuilder.setTargetId(tar.getTargetId());
			targetBuilder.setFinish(tar.isFinish());
			targetBuilder.setRecieveReward(tar.isAchieve());
			targetBuilder.setTargetScore(tar.getTargetScore());
			builder.addTarget(targetBuilder);
		}
	}
	
	public void checkTarget(){
		for(TargetInfo tar : targetList){
			if(score >= tar.getTargetScore()){
				tar.setFinish(true);
			}
		}
	}
	
	/***
	 * 判断有没有奖励可以领取
	 * 如果可以领直接设置
	 * @param targetId
	 * @return
	 */
	public boolean canAchieve(int targetId){
		boolean hasReward = false;
		for(TargetInfo tar : targetList){
			if(tar.getTargetId() == targetId && tar.isFinish() && !tar.isAchieve()){
				tar.setAchieve(true);
				hasReward = true;
			}
		}
		return hasReward;
	}
	
	public int getBuildLevel(int targetId){
		for(TargetInfo tar : targetList){
			if(tar.getTargetId() == targetId){
				return tar.getBuildLevel();
			}
		}
		return 0;
	}

	public void setKillScore(long killScore) {
		this.killScore = killScore;
	}

	public void setHurtScore(long hurtScore) {
		this.hurtScore = hurtScore;
	}

	public long getBuildBattlePoint() {
		return buildBattlePoint;
	}

	public long getTechBattlePoint() {
		return techBattlePoint;
	}

	public void setTechBattlePoint(long techBattlePoint) {
		this.techBattlePoint = techBattlePoint;
	}

	public void setTempBattlePoint(long tempBattlePoint) {
		this.tempBattlePoint = tempBattlePoint;
	}

	@Override
	public String toString() {
		return "StrongestGuildEntity [id=" + id + ", playerId=" + playerId + ", termId=" + termId + ", stageId="
				+ stageId + ", targetInfo=" + targetInfo + ", score=" + score + ", killScore=" + killScore
				+ ", hurtScore=" + hurtScore + ", buildBattlePoint=" + buildBattlePoint + ", techBattlePoint="
				+ techBattlePoint + ", createTime=" + createTime + ", updateTime=" + updateTime + ", invalid=" + invalid
				+ ", targetList=" + targetList + ", tempBattlePoint=" + tempBattlePoint + "]";
	
	}
}
