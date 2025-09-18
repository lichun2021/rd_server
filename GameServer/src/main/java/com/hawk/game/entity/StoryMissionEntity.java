package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.config.StoryMissionChaptCfg;
import com.hawk.game.item.mission.ChapterMissionItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.protocol.StoryMission.MissionData;
import com.hawk.game.protocol.StoryMission.ParalleledStoryMission;
import com.hawk.game.protocol.StoryMission.StoryMissionPage;
import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 剧情任务实体
 * 
 * @author golden
 *
 */
@Entity
@Table(name = "story_mission")
public class StoryMissionEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	// 章节id
	@Column(name = "chapterId", nullable = false)
    @IndexProp(id = 3)
	private int chapterId;

	// 章节状态 (0:未完成 1:未领取 2:已领取)
	@Column(name = "chapterState", nullable = false)
    @IndexProp(id = 4)
	private int chapterState;

	// 任务列表 结构: type_id_count_state_cfgId
	@Column(name = "missions", nullable = false)
    @IndexProp(id = 5)
	private String missions = "";

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 6)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 7)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 8)
	protected boolean invalid;
	
	// 已完成的章节任务
	@Column(name = "completeChapters", nullable = false)
    @IndexProp(id = 9)
	private String completeChapters = "";
	// 平行的章节任务
	@Column(name = "paralleledChapterMission", nullable = false)
    @IndexProp(id = 10)
	private String paralleledChapterMission = "";

	@Transient
	private List<MissionEntityItem> missionItems;
	
	@Transient
	private Set<Integer> completeChapterSet = new HashSet<>();
	
	@Transient
	ChapterMissionItem paralleledChapterMissionItem;

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

	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId(int chapterId) {
		this.chapterId = chapterId;
	}
	
	public boolean isMainChapter(int chapterId) {
		return this.chapterId == chapterId;
	}
	
	public void setParalleledChapterMission(ChapterMissionItem chapterMission) {
		paralleledChapterMissionItem = chapterMission;
	}
	
	public ChapterMissionItem getParalleledChapterMission() {
		return paralleledChapterMissionItem;
	}

	public int getChapterState() {
		return chapterState;
	}

	public void setChapterState(int chapterState) {
		this.chapterState = chapterState;
	}

	public String getMissions() {
		return missions;
	}

	public void setMissions(String missions) {
		this.missions = missions;
	}

	public List<MissionEntityItem> getMissionItems() {
		return missionItems;
	}

	public void setMissionItems(List<MissionEntityItem> missionItems) {
		this.missionItems = missionItems;
	}
	
	public Set<Integer> getCompleteChapterSet() {
		return completeChapterSet;
	}
	
	public void addCompleteChapter(int chapterId) {
		this.completeChapterSet.add(chapterId);
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
	
	/**
	 * 设置任务状态
	 * 
	 * @param chapterId
	 * @param missionId
	 * @param satate
	 */
	public void changeMissionState(int missionId, int state) {
		boolean changed = false;
		for (MissionEntityItem mission : getMissionItems()) {
			if (mission.getCfgId() == missionId) {
				changed = true;
				mission.setState(state);
			}
		}
		
		if (!changed && paralleledChapterMissionItem != null) {
			for (MissionEntityItem mission : paralleledChapterMissionItem.getMissionItems()) {
				if (mission.getCfgId() == missionId) {
					mission.setState(state);
				}
			}
		}
		
		notifyUpdate();
	}

	/**
	 * 转协议
	 * 
	 * @return
	 */
	public StoryMissionPage.Builder toBuilder() {
		StoryMissionPage.Builder builder = StoryMissionPage.newBuilder();
		builder.setChapterId(chapterId);
		builder.setChapterState(chapterState);

		MissionData.Builder missionData = MissionData.newBuilder();
		for (MissionEntityItem missionItem : missionItems) {
			missionData.setMissionId(missionItem.getCfgId());
			missionData.setState(missionItem.getState());
			missionData.setNum((int) Math.min(Integer.MAX_VALUE - 1, missionItem.getValue()));
			builder.addData(missionData);
		}
		
		if (paralleledChapterMissionItem != null && paralleledChapterMissionItem.getChapterState() != GsConst.MissionState.STATE_NOT_OPEN) {
			ParalleledStoryMission.Builder paralleledMissionBuilder = paralleledChapterMissionItem.toBuilder();
			builder.setParalleledMission(paralleledMissionBuilder);
		}
		
		builder.addAllCompleteChapterId(completeChapterSet);
		
		return builder;
	}

	/**
	 * 获取指定配置的任务item
	 * 
	 * @param cfgId
	 * @return
	 */
	public MissionEntityItem getStoryMissionItem(int cfgId) {
		for (int i = 0; i < missionItems.size(); i++) {
			MissionEntityItem missionItem = missionItems.get(i);
			if (missionItem.getCfgId() == cfgId) {
				return missionItem;
			}
		}
		
		if (paralleledChapterMissionItem != null) {
			for (int i = 0; i < paralleledChapterMissionItem.getMissionItems().size(); i++) {
				MissionEntityItem missionItem = paralleledChapterMissionItem.getMissionItems().get(i);
				if (missionItem.getCfgId() == cfgId) {
					return missionItem;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 获取指定配置的任务item
	 * 
	 * @param cfgId
	 * @return
	 */
	public void setStoryMissionItem(MissionEntityItem item) {
		boolean setSucc = false;
		for (int i = 0; i < missionItems.size(); i++) {
			MissionEntityItem missionItem = missionItems.get(i);
			if (missionItem.getCfgId() == item.getCfgId()) {
				setSucc = true;
				missionItem = item;
			}
		}
		
		if (!setSucc && paralleledChapterMissionItem != null) {
			for (int i = 0; i < paralleledChapterMissionItem.getMissionItems().size(); i++) {
				MissionEntityItem missionItem = paralleledChapterMissionItem.getMissionItems().get(i);
				if (missionItem.getCfgId() == item.getCfgId()) {
					missionItem = item;
				}
			}
		}
	}


	public void setStoryMissionItem2(MissionEntityItem item){
		boolean setSucc = false;
		for (int i = 0; i < missionItems.size(); i++) {
			MissionEntityItem missionItem = missionItems.get(i);
			if (missionItem.getCfgId() == item.getCfgId()) {
				setSucc = true;
				missionItem.setValue(item.getValue());
				missionItem.setState(item.getState());
			}
		}

		if (!setSucc && paralleledChapterMissionItem != null) {
			for (int i = 0; i < paralleledChapterMissionItem.getMissionItems().size(); i++) {
				MissionEntityItem missionItem = paralleledChapterMissionItem.getMissionItems().get(i);
				if (missionItem.getCfgId() == item.getCfgId()) {
					missionItem.setValue(item.getValue());
					missionItem.setState(item.getState());
				}
			}
		}
	}


	@Override
	public void afterRead() {
		missionItems = new ArrayList<MissionEntityItem>();
		if (!HawkOSOperator.isEmptyString(missions)) {
			String[] missionArr = missions.split(",");
			for (String missionStr : missionArr) {
				String[] mission = missionStr.split("_");
				MissionEntityItem missionItem = new MissionEntityItem(Integer.parseInt(mission[0]), Integer.parseInt(mission[1]), Integer.parseInt(mission[2]));
				missionItems.add(missionItem);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(paralleledChapterMission)) {
			ChapterMissionItem itemObj = ChapterMissionItem.parseObject(paralleledChapterMission);
			if (itemObj != null) {
				paralleledChapterMissionItem = itemObj;
			}
		}
		
		this.completeChapterSet = SerializeHelper.stringToSet(Integer.class, completeChapters, ",");
		ConfigIterator<StoryMissionChaptCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(StoryMissionChaptCfg.class);
		while (iterator.hasNext()) {
			StoryMissionChaptCfg cfg = iterator.next();
			if (cfg.getId() < this.chapterId) {
				this.completeChapterSet.add(cfg.getId());
			}
		}
	}

	@Override
	public void beforeWrite() {
		if (missionItems != null) {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < missionItems.size(); i++) {
				if (i > 0) {
					result.append(",");
				}
				result.append(missionItems.get(i).toString());
			}
			missions = result.toString();
		}

		if (paralleledChapterMissionItem != null) {
			paralleledChapterMission = paralleledChapterMissionItem.toString();
		} else {
			paralleledChapterMission = "";
		}
		
		this.completeChapters = SerializeHelper.collectionToString(completeChapterSet, ",");
	}
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
