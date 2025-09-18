package com.hawk.activity.type.impl.stronestleader.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.stronestleader.rank.StrongestRank;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 最强指挥官活动数据存储
 * @author PhilChen
 *
 */
@Entity
@Table(name = "activity_strongest_leader")
public class ActivityStrongestLeaderEntity extends HawkDBEntity implements IActivityDataEntity{
	
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
	@Column(name = "activityStage", nullable = false)
	private int activityStage;
	
    @IndexProp(id = 5)
	@Column(name = "stageId", nullable = false)
	private int stageId;
	
    @IndexProp(id = 6)
	@Column(name = "score", nullable = false)
	private long score; //如果为发动战争阶段，此值存储杀死部队积分，否则为总积分
	
    @IndexProp(id = 7)
	@Column(name = "hurtScore", nullable = false)
	private long hurtScore;
	
    @IndexProp(id = 8)
	@Column(name = "initFightPoint", nullable = false)
	private long initFightPoint;
	
    @IndexProp(id = 9)
	@Column(name = "buildBattlePoint", nullable = false)
	private long buildBattlePoint;
	
    @IndexProp(id = 10)
	@Column(name = "techBattlePoint", nullable = false)
	private long techBattlePoint;
	
    @IndexProp(id = 11)
	@Column(name = "targetIds", nullable = false)
	private String targetIds;
	
    @IndexProp(id = 12)
	@Column(name = "achieveTargets", nullable = false)
	private String achieveTargets;

    @IndexProp(id = 13)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 14)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 15)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<Integer> achieveTargetList = new CopyOnWriteArrayList<>();
	
	@Transient
	private List<Integer> targetList = new CopyOnWriteArrayList<>();
	
	public ActivityStrongestLeaderEntity() {
	}
	
	public ActivityStrongestLeaderEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public ActivityStrongestLeaderEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		targetIds = SerializeHelper.collectionToString(targetList);
		achieveTargets = SerializeHelper.collectionToString(achieveTargetList);
	}
	public static void main(String[] args) {
		Map<Integer, StrongestRank> historyStageInfo = new ConcurrentHashMap<>();
		historyStageInfo.put(1, StrongestRank.valueOf("11111", 1));
		historyStageInfo.put(2, StrongestRank.valueOf("22222", 2));
		String mapToString = SerializeHelper.mapToString(historyStageInfo);
		System.out.println(mapToString);
		Map<Integer, StrongestRank> stringToMap = SerializeHelper.stringToMap("1:11111,1,0|2:22222,2,0", Integer.class, StrongestRank.class);
		System.out.println(stringToMap);
		
		List<Integer> targetList = new ArrayList<Integer>();
		targetList.add(1);
		targetList.add(2);
		targetList.add(3);
		String collectionToSplitString = SerializeHelper.collectionToString(targetList);
		System.out.println(collectionToSplitString);
		List<Integer> stringSplitToList = SerializeHelper.stringToList(Integer.class, "1|2|3");
		System.out.println(stringSplitToList);
	}

	@Override
	public void afterRead() {
		this.targetList.clear();
		this.achieveTargetList.clear();
		SerializeHelper.stringToList(Integer.class, targetIds, targetList);
		SerializeHelper.stringToList(Integer.class, achieveTargets, achieveTargetList);
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public long getScore() {
		return score;
	}
	
	public long getTotalScore(){
		return score + hurtScore;
	}
	
	public void setScore(long score) {
		this.score = score;
	}

	public long getHurtScore() {
		return hurtScore;
	}

	public void setHurtScore(long hurtScore) {
		this.hurtScore = hurtScore;
	}

	public int getStageId() {
		return stageId;
	}
	
	public void setActivityStage(int activityStage) {
		this.activityStage = activityStage;
	}
	
	public int getActivityStage() {
		return activityStage;
	}
	
	public void setStageId(int stageId) {
		this.stageId = stageId;
	}
	
	public void setInitBattlePoint(long initFightPoint) {
		this.initFightPoint = initFightPoint;
	}

	public long getInitBattlePoint() {
		return initFightPoint;
	}
	
	public long getBuildBattlePoint() {
		return buildBattlePoint;
	}
	
	public void setBuildBattlePoint(long buildBattlePoint) {
		this.buildBattlePoint = buildBattlePoint;
	}
	
	public long getTechBattlePoint() {
		return techBattlePoint;
	}
	
	public void setTechBattlePoint(long techBattlePoint) {
		this.techBattlePoint = techBattlePoint;
	}

	public void addScore(long score) {
		setScore(this.score + score);
	}
	
	public boolean isAchieveTarget(Integer targetId) {
		return achieveTargetList.contains(targetId);
	}
	
	public void addAchieveTarget(Integer targetId) {
		achieveTargetList.add(targetId);
		this.notifyUpdate();
	}
	
	public void addTargetId(int targetId) {
		targetList.add(targetId);
		this.notifyUpdate();
	}
	
	public void cleanTarget() {
		targetList.clear();
		achieveTargetList.clear();
	}
	
	public List<Integer> getTargetList() {
		return targetList;
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
