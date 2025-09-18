package com.hawk.game.script;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.entity.PlotBattleEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.PlayerPlotBattleModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.PlotBattle.LevelState;
import com.hawk.game.util.GsConst;

/**
 * 开发时，使用的一些指令
 * @author jm
 *
 */
public class DevCammondHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String param = params.get("params");
		String[] paramArray = param.split(";");
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		String rlt = null;
		try {
			switch	(paramArray[0]) {
			case "101":
				rlt = setPlotBattleInfo(player, paramArray[1]);
				break;
			}
		} catch (Exception e) {
			return failedResponse(0, e.getMessage());
		}
		
		return successResponse(rlt);
		
	}

	private String setPlotBattleInfo(Player player, String string) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		PlayerPlotBattleModule module = player.getModule(GsConst.ModuleType.PLOT_BATTLE);
		PlotBattleEntity plotBattleEntity = player.getData().getPlotBattleEntity();
		plotBattleEntity.setLevelId(Integer.parseInt(string));
		plotBattleEntity.setStatus(LevelState.OPEN_VALUE);
		
		Method[] methods = module.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().contains("syn")) {
				method.setAccessible(true);
				method.invoke(module);
			}
		}
		
		return "player:"+player.getName()+"  levelId:"+string;
	}

}
