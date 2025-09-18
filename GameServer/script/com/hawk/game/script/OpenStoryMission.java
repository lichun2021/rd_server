package com.hawk.game.script;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsConfig;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.util.GsConst;

/**
 * 开启剧情任务
 * localhost:8080/script/openStoryMission?playerName=golden&chapterId=1
 * @author golden
 *
 */
public class OpenStoryMission extends HawkScript {

	@SuppressWarnings("unchecked")
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "");
		}
		
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "player not found");
			}
			
			if (!params.containsKey("chapterId")) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "chapterId not found");
			}
			
			int chapterId = Integer.valueOf(params.get("chapterId"));
			StoryMissionEntity entity = player.getData().getStoryMissionEntity();
			entity.setChapterId(chapterId);
			
			try {
				Class<StoryMissionService> clz = StoryMissionService.class;
				Method method = clz.getDeclaredMethod("generateChapterMission", Player.class, int.class);
				method.setAccessible(true);
				List<MissionEntityItem> items = (List<MissionEntityItem>)method.invoke(StoryMissionService.getInstance(), player, chapterId);
				entity.setMissionItems(items);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			// 判断新章节完成状态
			boolean chapterComp = true;
			for (MissionEntityItem mission : entity.getMissionItems()) {
				if (mission.getState() == GsConst.MissionState.STATE_NOT_FINISH) {
					chapterComp = false;
				}
			}
			
			int chapterState = chapterComp ? GsConst.MissionState.STATE_FINISH : GsConst.MissionState.STATE_NOT_FINISH;
			entity.setChapterState(chapterState);
			player.getPush().syncStoryMissionInfo();
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}

}
