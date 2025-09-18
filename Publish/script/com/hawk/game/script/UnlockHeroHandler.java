package com.hawk.game.script;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.log.Action;

/**
 * localhost:8080/script/unlockHero?playerName=
 * @author lwt
 * @date 2018年7月24日
 */
public class UnlockHeroHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			ConfigIterator<HeroCfg>  heroCfgs = HawkConfigManager.getInstance().getConfigIterator(HeroCfg.class);
			AwardItems award = AwardItems.valueOf();
			for(HeroCfg cfg:heroCfgs){
				ItemInfo piceItem = ItemInfo.valueOf(cfg.getUnlockPieces());
				piceItem.setCount(1000);
				award.addItem(piceItem);
				award.addItem(ItemInfo.valueOf(cfg.getUnlockPieces()));
			}
			
			List<ItemInfo> skills =  HawkConfigManager.getInstance().getConfigIterator(ItemCfg.class).stream()
					.filter(cfg -> cfg.getItemType() == Const.ToolType.HERO_SKILL_VALUE)
					.map(cfg -> new ItemInfo(ItemType.TOOL_VALUE, cfg.getId(), 100))
					.collect(Collectors.toList());
			
			award.addItemInfos(skills);
			
			award.rewardTakeAffectAndPush(player, Action.GM_AWARD);
			
			return HawkScript.successResponse("SUCC");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
