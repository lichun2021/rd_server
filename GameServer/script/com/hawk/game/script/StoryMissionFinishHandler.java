package com.hawk.game.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.GsConfig;
import com.hawk.game.config.StoryMissionCfg;
import com.hawk.game.config.StoryMissionChaptCfg;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.mission.ChapterMissionItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.log.LogConst.ChapterMissionOperType;
import com.hawk.log.LogConst.TaskType;

/**
 * 完成指定章节的剧情任务（包括该章节之前的所有任务）
 * 
 * http://localhost:8080/script/storyMissionFinish?playerId=1aau-2ayfd6-1&chapter=9&cityControl=0
 * 
 * @author lating
 *
 */
public class StoryMissionFinishHandler extends HawkScript {
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		return doAction(params);
	}
	
	public static String doAction(Map<String, String> params) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
		}
		
		if (!params.containsKey("chapter")) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "chapter param need");
		}
		
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}
			
			int chapter = Integer.parseInt(params.get("chapter"));
			if (chapter <= 0) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "chapter param error");
			}
			
			if (player.isActiveOnline()) {
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						finishChapterTask(player, chapter, Integer.parseInt(params.getOrDefault("cityControl", "1")));
						return null;
					}
				}, threadIdx);
			} else {
				finishChapterTask(player, chapter, Integer.parseInt(params.getOrDefault("cityControl", "1")));
			}
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
	
	private static void finishChapterTask(Player player, int chapter, int cityControl) {
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		if (entity.getChapterId() > chapter) {
			HawkLog.logPrintln("storyMissionFinish handler, playerId: {}, chapter {} already finished, latest chapter: {}", player.getId(), chapter, entity.getChapterId());
			return;
		}
		
		int chapterId = entity.getChapterId();
		int maxChapterId = HawkConfigManager.getInstance().getConfigSize(StoryMissionChaptCfg.class);
		int count = 0;
		while (chapterId <= chapter && count <= maxChapterId) {
			try {
				List<MissionEntityItem> itemList = new ArrayList<MissionEntityItem>();
				if (entity.isMainChapter(chapterId)) {
					itemList.addAll(entity.getMissionItems());
				} else {
					itemList.addAll(entity.getParalleledChapterMission().getMissionItems());
				}
				
				for (MissionEntityItem item : itemList) {
					if (item.getState() == GsConst.MissionState.STATE_BONUS) {
						continue;
					}
					
					StoryMissionService.getInstance().pushMissionReward(player, chapterId, item.getCfgId());
					entity.changeMissionState(item.getCfgId(), GsConst.MissionState.STATE_BONUS);
					StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId, item.getCfgId());
					StoryMissionService.getInstance().logTaskFlow(player, cfg, MissionState.STATE_BONUS);
				}
				
				if (maxChapterId == entity.getChapterId() && entity.getChapterState() == GsConst.MissionState.STORY_MISSION_COMPLETE) {
					player.getPush().syncStoryMissionInfo();
					return;
				}
				
				StoryMissionService.getInstance().pushChapterReward(player, chapterId);
				// 领取章节任务奖励打点记录
				LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, chapterId, ChapterMissionOperType.COMPLETE_AWARD_TAKEN);
				if (entity.isMainChapter(chapterId)) {
					entity.setChapterState(GsConst.MissionState.STATE_BONUS);
				} else if (entity.getParalleledChapterMission() != null && entity.getParalleledChapterMission().getChapterId() == chapterId) {
					entity.getParalleledChapterMission().setChapterState(GsConst.MissionState.STATE_BONUS);
				}
				
				// GM命令下，不受大本等级限制
				if (cityControl == 0) {
					refreshChapterMission(player);
				} else {
					StoryMissionService.getInstance().refreshChapterMission(player);
				}
				
				if (entity.getChapterId() > chapterId) {
					chapterId = entity.getChapterId();
				} else if (entity.getParalleledChapterMission() != null && entity.getParalleledChapterMission().getChapterId() > chapterId) {
					chapterId = entity.getParalleledChapterMission().getChapterId();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				player.getPush().syncStoryMissionInfo();
				return;
			}
			
			count++;
		}
		
		player.getPush().syncStoryMissionInfo();
	}
	
	private static void refreshChapterMission(Player player) {
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		// 主干的章节任务已经完成了，才可以刷新主干任务
		if (entity.getChapterState() == GsConst.MissionState.STATE_BONUS) {
			refreshMainChapterMission(player, entity);
		}

		// 刷新分支任务（如果存在的话）
		refreshParalleledChapterMission(player, entity);
	}
	
	/**
	 * 刷新主干任务
	 * 
	 * @param player
	 * @param entity
	 */
	private static void refreshMainChapterMission(Player player, StoryMissionEntity entity) {
		int chapterId = entity.getChapterId();
		int maxChapterId = HawkConfigManager.getInstance().getConfigSize(StoryMissionChaptCfg.class);
		if (chapterId >= maxChapterId) {
			entity.setChapterState(GsConst.MissionState.STORY_MISSION_COMPLETE);
			return;
		}
		
		StoryMissionChaptCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, chapterId);
		if (curCfg.getNextChapterIds().isEmpty()) {
			refreshToNextChapter(player, entity, chapterId, chapterId + 1);
			return;
		}
		
		// 分支任务，将较小的章节分到主干上，大的章节分到分支上
		List<Integer> paralleledChapterList = curCfg.getNextChapterIds();
		refreshToNextChapter(player, entity, chapterId, paralleledChapterList.get(0));
		if (paralleledChapterList.size() <= 1) {
			return;
		}
		
		// 新增分支任务
		int nextChapterId = paralleledChapterList.get(1);
		entity.addCompleteChapter(chapterId);
		entity.notifyUpdate();
		refreshParalleledChapterMission(player, entity, nextChapterId);
	}
	
	/**
	 * 更新分支任务
	 * 
	 * @param player
	 * @param entity
	 */
	private static void refreshParalleledChapterMission(Player player, StoryMissionEntity entity) {
		ChapterMissionItem paralleledChapterMission = entity.getParalleledChapterMission();
		// 已经刷出来了但还未开启
		if (paralleledChapterMission != null && paralleledChapterMission.getChapterState() == GsConst.MissionState.STATE_NOT_OPEN) {
			openParalleledChapterMission(player, entity, paralleledChapterMission);
			return;
		}
		
		// 不存在分支任务，或分支任务还未完成的情况
		if (paralleledChapterMission == null || paralleledChapterMission.getChapterState() != GsConst.MissionState.STATE_BONUS) {
			return;
		}
		
		// 存在分支任务，且分支任务已完成的情况
		int chapterId = paralleledChapterMission.getChapterId();
		int nextChapterId = chapterId + 1;
		StoryMissionChaptCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, nextChapterId);
		// 等级是否可开启下一章节
		if (nextCfg != null && nextCfg.isOpen()) {
			entity.addCompleteChapter(chapterId);
			paralleledChapterMission.setChapterId(nextChapterId);
			paralleledChapterMission.setMissionItems(StoryMissionService.getInstance().generateChapterMission(player, nextChapterId));
			
			boolean chapterComp = true;
			for (MissionEntityItem mission : paralleledChapterMission.getMissionItems()) {
				if (mission.getState() == GsConst.MissionState.STATE_NOT_FINISH) {
					chapterComp = false;
				}
			}
			
			int chapterState = chapterComp ? GsConst.MissionState.STATE_FINISH : GsConst.MissionState.STATE_NOT_FINISH;
			paralleledChapterMission.setChapterState(chapterState);
			entity.notifyUpdate();
			LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, nextChapterId, ChapterMissionOperType.MISSION_REFRESH);
		} else {
			paralleledChapterMission.setChapterState(GsConst.MissionState.STATE_BONUS);
			List<MissionEntityItem> missionItems = paralleledChapterMission.getMissionItems();
			for (MissionEntityItem missionItem : missionItems) {
				if (missionItem.getState() != GsConst.MissionState.STATE_BONUS) {
					missionItem.setState(GsConst.MissionState.STATE_BONUS);
					StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId, missionItem.getCfgId());
					StoryMissionService.getInstance().logTaskFlow(player, cfg, missionItem.getState());
				}
			}
			paralleledChapterMission.setMissionItems(missionItems);
			entity.notifyUpdate();
		}
	}
	
	/**
	 * 主干切换到下一个章节
	 * 
	 * @param player
	 * @param entity
	 * @param chapterId
	 */
	private static void refreshToNextChapter(Player player, StoryMissionEntity entity, int chapterId, int nextChapterId) {
		// 如果存在分支，要看分支是否可以并到主干了：如果nextChapterId已经完成了，直接将分支合并到主干就完了；如果nextChapterId正是当前分支上正在进行的任务，也直接将分支合并到主干
		if (entity.getParalleledChapterMission() != null && (entity.getCompleteChapterSet().contains(nextChapterId) || entity.getParalleledChapterMission().getChapterId() == nextChapterId)) {
			// 直接将分支合并到主干
			if (mergeParalleledChapterToMain(player, entity)) {
				entity.addCompleteChapter(chapterId);
			}
			return;
		}
		
		StoryMissionChaptCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, nextChapterId);
		// 等级是否可开启下一章节
		if (nextCfg != null && nextCfg.isOpen()) {
			entity.addCompleteChapter(chapterId);
			entity.setChapterId(nextChapterId);
			entity.setMissionItems(StoryMissionService.getInstance().generateChapterMission(player, nextChapterId));

			boolean chapterComp = true;
			for (MissionEntityItem mission : entity.getMissionItems()) {
				if (mission.getState() == GsConst.MissionState.STATE_NOT_FINISH) {
					chapterComp = false;
				}
			}

			int chapterState = chapterComp ? GsConst.MissionState.STATE_FINISH : GsConst.MissionState.STATE_NOT_FINISH;
			entity.setChapterState(chapterState);
			LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, nextChapterId, ChapterMissionOperType.MISSION_REFRESH);
		} else {
			entity.setChapterState(GsConst.MissionState.STATE_BONUS);
			List<MissionEntityItem> missionItems = entity.getMissionItems();
			for (MissionEntityItem missionItem : missionItems) {
				if (missionItem.getState() != GsConst.MissionState.STATE_BONUS) {
					missionItem.setState(GsConst.MissionState.STATE_BONUS);
					StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId, missionItem.getCfgId());
					StoryMissionService.getInstance().logTaskFlow(player, cfg, missionItem.getState());
				}
			}
			entity.setMissionItems(missionItems);
		}
	}
	
	/**
	 * 将分支上的章节任务合并到主干
	 * 
	 * @param player
	 * @param entity
	 */
	private static boolean mergeParalleledChapterToMain(Player player, StoryMissionEntity entity) {
		ChapterMissionItem paralleledChapterMission = entity.getParalleledChapterMission();
		if (paralleledChapterMission == null) {
			return false;
		}
		
		// 移到主干前先判断分支任务是否开启，如果还未开启要先判断是否可开启
		if (paralleledChapterMission.getChapterState() == GsConst.MissionState.STATE_NOT_OPEN && !openParalleledChapterMission(player, entity, paralleledChapterMission)) {
			return false;
		}
		
		entity.setChapterId(paralleledChapterMission.getChapterId());
		entity.setChapterState(paralleledChapterMission.getChapterState());
		entity.setMissionItems(paralleledChapterMission.getMissionItems());
		entity.setParalleledChapterMission(null);
		return true;
	}
	
	/**
	 * 开启分支任务
	 * 
	 * @param player
	 * @param paralleledChapterMission
	 * @return
	 */
	private static boolean openParalleledChapterMission(Player player, StoryMissionEntity entity, ChapterMissionItem paralleledChapterMission) {
		StoryMissionChaptCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, paralleledChapterMission.getChapterId());
		if (currCfg != null && currCfg.isOpen()) {
			paralleledChapterMission.setChapterState(GsConst.MissionState.STATE_NOT_FINISH);
			boolean chapterComp = true;
			for (MissionEntityItem mission : paralleledChapterMission.getMissionItems()) {
				if (mission.getState() == GsConst.MissionState.STATE_NOT_FINISH) {
					chapterComp = false;
				}
			}
			
			int chapterState = chapterComp ? GsConst.MissionState.STATE_FINISH : GsConst.MissionState.STATE_NOT_FINISH;
			paralleledChapterMission.setChapterState(chapterState);
			entity.notifyUpdate();
			LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, paralleledChapterMission.getChapterId(), ChapterMissionOperType.MISSION_REFRESH);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 开启分支任务
	 * 
	 * @param player
	 * @param entity
	 * @param nextChapterId
	 */
	private static void refreshParalleledChapterMission(Player player, StoryMissionEntity entity, int nextChapterId) {
		StoryMissionChaptCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, nextChapterId);
		// 等级是否可开启下一章节
		if (nextCfg == null) {
			return;
		}

		try {
			ChapterMissionItem paralleledChapterMission = new ChapterMissionItem(nextChapterId);
			paralleledChapterMission.setChapterState(GsConst.MissionState.STATE_NOT_OPEN);
			paralleledChapterMission.setMissionItems(StoryMissionService.getInstance().generateChapterMission(player, nextChapterId));
			entity.setParalleledChapterMission(paralleledChapterMission);
			if (nextCfg.isOpen()) {
				paralleledChapterMission.setChapterState(GsConst.MissionState.STATE_NOT_FINISH);
				boolean chapterComp = true;
				for (MissionEntityItem mission : paralleledChapterMission.getMissionItems()) {
					if (mission.getState() == GsConst.MissionState.STATE_NOT_FINISH) {
						chapterComp = false;
					}
				}
				
				int chapterState = chapterComp ? GsConst.MissionState.STATE_FINISH : GsConst.MissionState.STATE_NOT_FINISH;
				paralleledChapterMission.setChapterState(chapterState);
				entity.notifyUpdate();
				LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, nextChapterId, ChapterMissionOperType.MISSION_REFRESH);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
