package com.hawk.game.item.mission;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.StoryMission.MissionData;
import com.hawk.game.protocol.StoryMission.ParalleledStoryMission;

/**
 * 章节任务数据结构体
 * 
 * @author lating
 *
 */
public class ChapterMissionItem {

	/** 章节id */
	private int chapterId;

	/** 章节状态 (-1:未开启 0:未完成 1:未领取 2:已领取) */ 
	private int chapterState;
	
	/** 章节子任务  */
	private List<MissionEntityItem> missionItems;

	public ChapterMissionItem() {
		this.missionItems = new ArrayList<>();
	}
	
	public ChapterMissionItem(int chapterId) {
		this.chapterId = chapterId;
		this.missionItems = new ArrayList<>();
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < missionItems.size(); i++) {
			if (i > 0) {
				result.append(",");
			}
			result.append(missionItems.get(i).toString());
		}
		
		return String.format("%d|%d|%s", chapterId, chapterState, result.toString());
	}
	
	public static ChapterMissionItem parseObject(String missionItemStr) {
		if (HawkOSOperator.isEmptyString(missionItemStr)) {
			return null;
		}
		
		String[] arr = missionItemStr.split("\\|");
		if (arr.length != 3) {
			return null;
		}
		ChapterMissionItem item = new ChapterMissionItem();
		item.chapterId = Integer.parseInt(arr[0]);
		item.chapterState = Integer.parseInt(arr[1]);
		
		String[] missionArr = arr[2].split(",");
		for (String missionStr : missionArr) {
			String[] mission = missionStr.split("_");
			MissionEntityItem missionItem = new MissionEntityItem(Integer.parseInt(mission[0]), Integer.parseInt(mission[1]), Integer.parseInt(mission[2]));
			item.missionItems.add(missionItem);
		}
		
		return item;
	}

	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId(int chapterId) {
		this.chapterId = chapterId;
	}

	public int getChapterState() {
		return chapterState;
	}

	public void setChapterState(int chapterState) {
		this.chapterState = chapterState;
	}
	
	public List<MissionEntityItem> getMissionItems() {
		return missionItems;
	}

	public void setMissionItems(List<MissionEntityItem> missionItems) {
		this.missionItems = missionItems;
	}
	
	public ParalleledStoryMission.Builder toBuilder() {
		ParalleledStoryMission.Builder builder = ParalleledStoryMission.newBuilder();
		builder.setChapterId(chapterId);
		builder.setChapterState(chapterState);

		MissionData.Builder missionData = MissionData.newBuilder();
		for (MissionEntityItem missionItem : missionItems) {
			missionData.setMissionId(missionItem.getCfgId());
			missionData.setState(missionItem.getState());
			missionData.setNum((int) Math.min(Integer.MAX_VALUE - 1, missionItem.getValue()));
			builder.addData(missionData);
		}
		
		return builder;
	}

}
