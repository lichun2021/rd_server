package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.StoryMissionCfg;
import com.hawk.game.config.StoryMissionChaptCfg;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.mission.ChapterMissionItem;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.log.Action;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.type.IMission;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.ChapterMissionOperType;
import com.hawk.log.LogConst.TaskType;

/**
 * 剧情任务操作类
 * 
 * @author golden
 *
 */
public class StoryMissionService {
	public static Logger logger = LoggerFactory.getLogger("Server");

	private static StoryMissionService instance;

	public static StoryMissionService getInstance() {
		if (instance == null) {
			instance = new StoryMissionService();
		}
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		return true;
	}

	/**
	 * 初始化剧情任务
	 */
	public StoryMissionEntity initStroyMission(Player player) {
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		if (entity.getChapterId() != 0) {
			return entity;
		}
		
		// 初始章节
		int initChapterId = 1;
		entity.setPlayerId(player.getId());
		entity.setChapterId(initChapterId);
		entity.setChapterState(GsConst.MissionState.STATE_NOT_FINISH);
		entity.setMissionItems(generateChapterMission(player, initChapterId));
		LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, initChapterId, ChapterMissionOperType.MISSION_REFRESH);
		checkChapterComplete(player, entity);
		return entity;
	}

	/**
	 * 剧情奖励领取反馈
	 * 
	 * @param isChapterAward:是否是章节奖励
	 * @param missionId:任务id
	 */
	public boolean syncStoryMissionReward(Player player, boolean isChapterAward, int missionId, int chapterId) {
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();

		// 参数错误，没有此任务
		if (!isChapterAward && entity.getStoryMissionItem(missionId) == null) {
			player.sendError(HP.code.STORY_MISSION_REWARD_S_VALUE, Status.SysError.PARAMS_INVALID_VALUE, 0);
			return false;
		}
		
		// 章节id
		if (chapterId <= 0) {
			chapterId = entity.getChapterId();
		}

		// 奖励不可领取
		if (!rewardCanReceive(entity, isChapterAward, missionId, chapterId)) {
			player.sendError(HP.code.STORY_MISSION_REWARD_S_VALUE, Status.Error.LEVEL_TAKE_AWARD_ALREADY_VALUE, 0);
			return false;
		}

		if (isChapterAward) {
			pushChapterReward(player, chapterId);
			// 领取章节任务奖励打点记录
			LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, chapterId, ChapterMissionOperType.COMPLETE_AWARD_TAKEN);
			if (entity.isMainChapter(chapterId)) {
				entity.setChapterState(GsConst.MissionState.STATE_BONUS);
			} else if (entity.getParalleledChapterMission() != null && entity.getParalleledChapterMission().getChapterId() == chapterId) {
				entity.getParalleledChapterMission().setChapterState(GsConst.MissionState.STATE_BONUS);
			}
			refreshChapterMission(player);
		} else {
			pushMissionReward(player, chapterId, missionId);
			entity.changeMissionState(missionId, GsConst.MissionState.STATE_BONUS);
			StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId, missionId);
			logTaskFlow(player, cfg, MissionState.STATE_BONUS);
		}

		player.getPush().syncStoryMissionInfo();
		player.getPush().syncStoryMissionAward(isChapterAward, missionId, chapterId);
		return true;
	}

	/**
	 * 大本升级检测新的章节任务
	 * 
	 * @param player
	 */
	public void upLevelRefresh(Player player) {
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		boolean mainChapter = entity.getChapterState() == GsConst.MissionState.STATE_BONUS || entity.getChapterState() == GsConst.MissionState.STORY_MISSION_COMPLETE;
		boolean paralleledChapter = entity.getParalleledChapterMission() != null && entity.getParalleledChapterMission().getChapterState() == GsConst.MissionState.STATE_BONUS;
		if (mainChapter || paralleledChapter) {
			refreshChapterMission(player);
			player.getPush().syncStoryMissionInfo();
		}
	}

	/**
	 * 检测任务配置更新
	 * 
	 * @param player
	 */
	public void checkMissionCfgUpdate(Player player) {
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		if (entity.getChapterId() == 0) {
			initStroyMission(player);
			logger.error("checkMissionCfgUpdate, init story mission, playerId:{}", player.getId());
		}
		
		if (isMissionCfgUpdate(entity, entity.getChapterId())) {
			refreshMissionList(player, entity, entity.getChapterId());
		}
		
		ChapterMissionItem chapterMission = entity.getParalleledChapterMission();
		if (chapterMission != null && isMissionCfgUpdate(entity, chapterMission.getChapterId())) {
			refreshMissionList(player, entity, chapterMission.getChapterId());
		}
		
		boolean mainChapter = entity.getChapterState() == GsConst.MissionState.STATE_BONUS || entity.getChapterState() == GsConst.MissionState.STORY_MISSION_COMPLETE;
		boolean paralleledChapter = entity.getParalleledChapterMission() != null && entity.getParalleledChapterMission().getChapterState() == GsConst.MissionState.STATE_BONUS;
		if (mainChapter || paralleledChapter) {
			refreshChapterMission(player);
		}
	}

	/**
	 * 章节是否完成
	 * @param player
	 * @param chapterId
	 * @return
	 */
	public boolean isChapterComplete(Player player, int chapterId) {
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		if (entity.getChapterId() > chapterId) {
			return true;
		}
//		if (entity.getChapterId() < chapterId) {
//			return false;
//		}
		if (entity.getCompleteChapterSet().contains(chapterId)) {
			return true;
		}
		
		if (entity.isMainChapter(chapterId) && (entity.getChapterState() == MissionState.STATE_BONUS || entity.getChapterState() == MissionState.STORY_MISSION_COMPLETE)) {
			return true;
		}
		
		ChapterMissionItem chapterMission = entity.getParalleledChapterMission();
		if (chapterMission != null && chapterMission.getChapterId() == chapterId && chapterMission.getChapterState() == MissionState.STATE_BONUS) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 章节完成检测
	 * 
	 * @param player
	 */
	public void checkChapterComplete(Player player,StoryMissionEntity entity) {
		List<MissionEntityItem> missionItems = entity.getMissionItems();
		boolean chapterComplete = true;
		for (MissionEntityItem missionItem : missionItems) {
			if (missionItem.getState() == MissionState.STATE_NOT_FINISH) {
				chapterComplete = false;
			}
		}
		
		if (chapterComplete && entity.getChapterState() == MissionState.STATE_NOT_FINISH) {
			entity.setChapterState(MissionState.STATE_FINISH);
		}
		
		ChapterMissionItem chapterMission = entity.getParalleledChapterMission();
		if (chapterMission != null) {
			missionItems = chapterMission.getMissionItems();
			chapterComplete = true;
			for (MissionEntityItem missionItem : missionItems) {
				if (missionItem.getState() == MissionState.STATE_NOT_FINISH) {
					chapterComplete = false;
				}
			}
			
			if (chapterComplete && chapterMission.getChapterState() == MissionState.STATE_NOT_FINISH) {
				chapterMission.setChapterState(MissionState.STATE_FINISH);
			}
		}
	}

	/**
	 * 任务完成检测
	 * 
	 * @param player
	 */
	public boolean checkTaskComplete(Player player, int taskId) {
		boolean isComplete = true;
		
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		List<MissionEntityItem> missionItems = entity.getMissionItems();
		if (entity.getParalleledChapterMission() != null) {
			missionItems = new ArrayList<MissionEntityItem>();
			missionItems.addAll(entity.getMissionItems());
			missionItems.addAll(entity.getParalleledChapterMission().getMissionItems());
		}
		
		for (MissionEntityItem missionItem : missionItems) {
			if (missionItem.getCfgId() != taskId) {
				continue;
			}
			if (missionItem.getState() == MissionState.STATE_NOT_FINISH) {
				isComplete = false;
			}
		}
		
		return isComplete;
	}
	
	/**
	 * 获取任务目标
	 * @param player
	 * @param taskId
	 * @return
	 */
	public Integer getMissionTaskTarget(Player player, int taskId) {
		Integer target = null;
		StoryMissionCfg taskCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionCfg.class, taskId);
		if (taskCfg != null) {
			target = Integer.valueOf(taskCfg.getVal1().split(";")[0]);
		}
		return target;
	}
	
	/**
	 * 生成章节任务数据
	 * 
	 * @param chapterId
	 * @return
	 */
	public List<MissionEntityItem> generateChapterMission(Player player, int chapterId) {
		List<MissionEntityItem> missions = new ArrayList<>();

		Map<Integer, StoryMissionCfg> chapterCfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId);
		for (StoryMissionCfg cfg : chapterCfg.values()) {
			MissionCfgItem missionCfgItem = cfg.getMissionCfgItem();
			MissionEntityItem missionEntityItem = new MissionEntityItem(cfg.getId(), 0, 0);

			// 初始化任务数据
			IMission iMission = MissionContext.getInstance().getMissions(missionCfgItem.getType());
			if (iMission == null) {
				logger.error("iMission is null, type:{}", missionCfgItem.getType());
				continue;
			}

			iMission.initMission(player.getData(), missionEntityItem, missionCfgItem);
			missions.add(missionEntityItem);
			// 记录剧情任务日志
			logTaskFlow(player, cfg, missionEntityItem.getState());
		}
		return missions;
	}

	/**
	 * 刷新章节任务
	 * 
	 * @param player
	 * @param isLevelRef:是否是升级刷新任务
	 */
	public void refreshChapterMission(Player player) {
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		// 主干的章节任务已经完成了，才可以刷新主干任务.  
		// 这里需要判断STORY_MISSION_COMPLETE状态，是因为在配置中新添加章节时也能刷新
		if (entity.getChapterState() == GsConst.MissionState.STATE_BONUS || entity.getChapterState() == GsConst.MissionState.STORY_MISSION_COMPLETE) {
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
	public void refreshMainChapterMission(Player player, StoryMissionEntity entity) {
		int chapterId = entity.getChapterId();
		int nextChapterId = chapterId + 1;
		int maxChapterId = HawkConfigManager.getInstance().getConfigSize(StoryMissionChaptCfg.class);
		if (chapterId >= maxChapterId || (entity.getCompleteChapterSet().contains(nextChapterId) && nextChapterId >= maxChapterId)) {
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
		nextChapterId = paralleledChapterList.get(1);
		entity.addCompleteChapter(chapterId);
		entity.notifyUpdate();
		refreshParalleledChapterMission(player, entity, nextChapterId);
	}
	
	/**
	 * 开启分支任务
	 * 
	 * @param player
	 * @param entity
	 * @param nextChapterId
	 */
	public void refreshParalleledChapterMission(Player player, StoryMissionEntity entity, int nextChapterId) {
		StoryMissionChaptCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, nextChapterId);
		// 等级是否可开启下一章节
		if (nextCfg == null) {
			return;
		}

		try {
			ChapterMissionItem paralleledChapterMission = new ChapterMissionItem(nextChapterId);
			paralleledChapterMission.setChapterState(GsConst.MissionState.STATE_NOT_OPEN);
			paralleledChapterMission.setMissionItems(generateChapterMission(player, nextChapterId));
			entity.setParalleledChapterMission(paralleledChapterMission);
			if (nextCfg.isOpen() && player.getCityLevel() >= nextCfg.getLevel()) {
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
	
	/**
	 * 主干切换到下一个章节
	 * 
	 * @param player
	 * @param entity
	 * @param chapterId
	 */
	private void refreshToNextChapter(Player player, StoryMissionEntity entity, int chapterId, int nextChapterId) {
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
		if (nextCfg != null && nextCfg.isOpen() && player.getCityLevel() >= nextCfg.getLevel()) {
			entity.addCompleteChapter(chapterId);
			entity.setChapterId(nextChapterId);
			entity.setMissionItems(generateChapterMission(player, nextChapterId));

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
					logTaskFlow(player, cfg, missionItem.getState());
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
	private boolean mergeParalleledChapterToMain(Player player, StoryMissionEntity entity) {
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
	 * 更新分支任务
	 * 
	 * @param player
	 * @param entity
	 */
	private void refreshParalleledChapterMission(Player player, StoryMissionEntity entity) {
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
		int maxChapterId = HawkConfigManager.getInstance().getConfigSize(StoryMissionChaptCfg.class);
		int chapterId = paralleledChapterMission.getChapterId();
		// 如果当前章节是最后一章任务
		if (chapterId >= maxChapterId) {
			entity.addCompleteChapter(chapterId);
			entity.setParalleledChapterMission(null);
			// 主干章节也完成了
			if (entity.getChapterState() == GsConst.MissionState.STATE_BONUS && entity.getCompleteChapterSet().contains(entity.getChapterId() + 1)) {
				entity.setChapterState(GsConst.MissionState.STORY_MISSION_COMPLETE);
			}
			return;
		}
		
		int nextChapterId = chapterId + 1;
		StoryMissionChaptCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, nextChapterId);
		// 等级是否可开启下一章节
		if (nextCfg != null && nextCfg.isOpen() && player.getCityLevel() >= nextCfg.getLevel()) {
			entity.addCompleteChapter(chapterId);
			paralleledChapterMission.setChapterId(nextChapterId);
			paralleledChapterMission.setMissionItems(generateChapterMission(player, nextChapterId));
			
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
					logTaskFlow(player, cfg, missionItem.getState());
				}
			}
			paralleledChapterMission.setMissionItems(missionItems);
			entity.notifyUpdate();
		}
	}
	
	/**
	 * 开启分支任务
	 * 
	 * @param player
	 * @param paralleledChapterMission
	 * @return
	 */
	private boolean openParalleledChapterMission(Player player, StoryMissionEntity entity, ChapterMissionItem paralleledChapterMission) {
		StoryMissionChaptCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, paralleledChapterMission.getChapterId());
		if (currCfg != null && currCfg.isOpen() && player.getCityLevel() >= currCfg.getLevel()) {
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
	 * 判断是否过了指定章节
	 * 
	 * @param palyer
	 * @param chapterId
	 * @return
	 */
	public boolean isCrossChapterId(Player palyer, int chapterId) {
		StoryMissionEntity entity = palyer.getData().getStoryMissionEntity();

		if (entity.getChapterId() > chapterId) {
			return true;
		} else if (entity.getChapterId() == chapterId) {
			return entity.getChapterState() == MissionState.STATE_FINISH;
		} else if (entity.getCompleteChapterSet().contains(chapterId)) {
			return true;
		} else if (entity.getParalleledChapterMission() != null && entity.getParalleledChapterMission().getChapterId() == chapterId) {
			return entity.getParalleledChapterMission().getChapterState() == MissionState.STATE_FINISH;
		}

		return false;
	}

	/**
	 * 奖励是否可领取
	 * 
	 * @param stats:任务完成状态
	 * @param missionId:任务id
	 * @param isChapterAward:是否是章节任务
	 * @return
	 */
	private boolean rewardCanReceive(StoryMissionEntity entity, boolean isChapterAward, int missionCfgId, int chapterId) {
		if (isChapterAward) {
			if (entity.isMainChapter(chapterId)) {
				return entity.getChapterState() == GsConst.MissionState.STATE_FINISH;
			}
			
			ChapterMissionItem paralleledMission = entity.getParalleledChapterMission();
			return paralleledMission != null && paralleledMission.getChapterId() == chapterId && paralleledMission.getChapterState() == GsConst.MissionState.STATE_FINISH;
		}

		MissionEntityItem mission = entity.getStoryMissionItem(missionCfgId);
		return mission != null && mission.getState() == GsConst.MissionState.STATE_FINISH;
	}

	/**
	 * 推送单个任务奖励
	 * 
	 * @param player
	 * @param chapterId
	 *            章节id
	 * @param missionId
	 *            任务id
	 */
	public void pushMissionReward(Player player, int chapterId, int missionId) {
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(getMissionReward(chapterId, missionId)); // 任务奖励
		awardItems.rewardTakeAffectAndPush(player, Action.STORY_MISSION_BONUS, true,
				RewardOrginType.STORY_MISSION_REWARD); // 发奖
	}

	/**
	 * 推送全部奖励 *章节任务奖励 + 单个完成未领取的任务奖励*
	 * 
	 * @param chapterId
	 */
	public void pushChapterReward(Player player, int chapterId) {
		AwardItems awardItems = AwardItems.valueOf();
		// 章节奖励
		StoryMissionChaptCfg chapterCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, chapterId);
		awardItems.addItemInfos(chapterCfg.getRewardItem());

		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		List<MissionEntityItem> missionItems = entity.getMissionItems();
		if (entity.getParalleledChapterMission() != null && entity.getParalleledChapterMission().getChapterId() == chapterId) {
			missionItems = entity.getParalleledChapterMission().getMissionItems();
		}
		
		for (MissionEntityItem missionItem : missionItems) {
			if (missionItem.getState() == GsConst.MissionState.STATE_FINISH) {
				awardItems.addItemInfos(getMissionReward(chapterId, missionItem.getCfgId()));
				StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId, missionItem.getCfgId());
				logTaskFlow(player, cfg, MissionState.STATE_BONUS);
			}
		}
		
		// 发奖
		awardItems.rewardTakeAffectAndPush(player, Action.STORY_MISSION_BONUS, false,
				RewardOrginType.STORY_CHAPTER_REWARD);
	}

	/**
	 * 获取任务奖励
	 * 
	 * @param chapterId:章节id
	 * @param missionId:任务id
	 * @return
	 */
	public List<ItemInfo> getMissionReward(int chapterId, int missionId) {
		StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId, missionId);
		return cfg.getRewardItem();
	}

	/**
	 * 任务配置是否更新
	 * 
	 * @param player
	 * @return
	 */
	private boolean isMissionCfgUpdate(StoryMissionEntity entity, int chapterId) {
		// 章节配置
		Map<Integer, StoryMissionCfg> missionCfgs = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId);
		// 章节数据
		List<MissionEntityItem> missionItems = entity.getMissionItems();
		if (!entity.isMainChapter(chapterId)) {
			missionItems = entity.getParalleledChapterMission().getMissionItems();
		}
		
		// size不同，证明配置更新过
		if (missionItems.size() != missionCfgs.size()) {
			return true;
		}
		// 数据里存在配置没有的任务，证明配置更新过
		for (MissionEntityItem mission : missionItems) {
			if (missionCfgs.get(mission.getCfgId()) == null) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 配置更新刷新任务列表
	 * 
	 * @param player
	 */
	private void refreshMissionList(Player player, StoryMissionEntity entity, int chapterId) {
		// 章节配置
		Map<Integer, StoryMissionCfg> missionCfgItems = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId);
		// 章节任务数据
		List<MissionEntityItem> missionEntityItems = entity.getMissionItems();
		if (!entity.isMainChapter(chapterId)) {
			missionEntityItems = entity.getParalleledChapterMission().getMissionItems();
		}

		// 移除任务
		Iterator<MissionEntityItem> iter = missionEntityItems.iterator();
		while (iter.hasNext()) {
			MissionEntityItem next = iter.next();
			if (!missionCfgItems.containsKey(next.getCfgId())) {
				iter.remove();
			}
		}

		// 添加任务
		for (StoryMissionCfg cfg : missionCfgItems.values()) {
			if (entity.getStoryMissionItem(cfg.getId()) != null && cfg.getChapter() == chapterId) {
				continue;
			}

			MissionCfgItem missionCfgItem = cfg.getMissionCfgItem();
			MissionEntityItem missionEntityItem = new MissionEntityItem(cfg.getId(), 0, 0);

			// 初始化任务数据
			IMission iMission = MissionContext.getInstance().getMissions(missionCfgItem.getType());
			iMission.initMission(player.getData(), missionEntityItem, missionCfgItem);

			missionEntityItems.add(missionEntityItem);
			logTaskFlow(player, cfg, missionEntityItem.getState());
		}

		if (entity.isMainChapter(chapterId)) {
			entity.setMissionItems(missionEntityItems);
		} else {
			entity.getParalleledChapterMission().setMissionItems(missionEntityItems);
		}

		// 刷新章节完成状态
		checkChapterComplete(player, entity);
	}

	/**
	 * rts任务是否解锁
	 * 
	 * @param player
	 */
	public boolean isRtsMissionUnlock(PlayerData playerData, int rtsCfgId) {
		StoryMissionEntity entity = playerData.getStoryMissionEntity();

		boolean isUnLock = true;
		List<MissionEntityItem> missionList = entity.getMissionItems();
		ChapterMissionItem chapterMission = entity.getParalleledChapterMission();
		if (chapterMission != null) {
			missionList = new ArrayList<>();
			missionList.addAll(entity.getMissionItems());
			missionList.addAll(chapterMission.getMissionItems());
		}
		
		for (MissionEntityItem mission : missionList) {
			StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(entity.getChapterId(), mission.getCfgId());
			if (cfg == null) {
				cfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterMission.getChapterId(), mission.getCfgId());
			}
			
			if (cfg.getType() != MissionType.MISSION_PLOT_BATTLE.intValue() && mission.getState() == MissionState.STATE_NOT_FINISH) {
				isUnLock = false;
			}
			
			if (cfg.getType() == MissionType.MISSION_PLOT_BATTLE.intValue() && !String.valueOf(rtsCfgId).equals(cfg.getVal1())) {
				isUnLock = false;
			}
		}
		
		return isUnLock;
	}

	/**
	 * 记录剧情任务流水日志
	 * 
	 * @param player
	 * @param cfg
	 * @param state
	 */
	public void logTaskFlow(Player player, StoryMissionCfg cfg, int state) {
		try {
			int type = cfg.getType();
			MissionType missionType = MissionType.valueOf(type);
			if (missionType != null) {
				type = missionType.logMissionTypeVal();
			}
			
			LogUtil.logTaskFlow(player, TaskType.STORY_MISSION, type, cfg.getId(), state, cfg.getChapter());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 是否已经完成了这个章节任务
	 * @param playerId
	 * @param missionId
	 * @return
	 */
	public boolean hasFinishStoryMission(String playerId, int missionId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		StoryMissionEntity entity = player.getData().getStoryMissionEntity();
		
		StoryMissionCfg missionCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionCfg.class, missionId);
		if (missionCfg == null) {
			return false;
		}
		
//		// 任务还没做到这个章节,那这个任务肯定没完成呢
//		if (entity.getChapterId() < missionCfg.getChapter()) {
//			return false;
//		}
		
		// 任务已经超过了这个章节,那这个任务肯定完成了
		if (entity.getChapterId() > missionCfg.getChapter()) {
			return true;
		}
		
		if (entity.getCompleteChapterSet().contains(missionCfg.getChapter())) {
			return true;
		}
		
		// 当前没有这个任务
		MissionEntityItem storyMissionItem = entity.getStoryMissionItem(missionId);
		if (storyMissionItem == null) {
			return false;
		}
		
		// 任务还没完成呢
		if (storyMissionItem.getValue() == MissionState.STATE_NOT_FINISH) {
			return false;
		}
		
		return true;
	}
	
}
