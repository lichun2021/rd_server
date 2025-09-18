package com.hawk.game.script;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.game.global.GlobalData;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.EffectParams;

/**
 * 打印玩家作用号属性
 * localhost:8080/script/effect?playerName=l0001&heroId=11
 * playerName: 玩家名字
 * 
 * @author Jesse
 *
 */
public class PrintEffectHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			int heroId = NumberUtils.toInt(params.get("heroId"));
			int superSoldierId = NumberUtils.toInt(params.get("superSoldierId"));
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			StringBuilder builder = new StringBuilder();
			builder.append(HawkScript.HTTP_NEW_LINE);
			builder.append(HawkScript.HTTP_NEW_LINE);
			builder.append("Player:");
			builder.append(HawkScript.HTTP_NEW_LINE);
			for (EffType type : EffType.values()) {
				int val = player.getEffect().getEffVal(type);
				if (val > 0) {
					builder.append(type.getNumber()).append(" : ").append(val).append(HawkScript.HTTP_NEW_LINE);
				}
			}
			builder.append(HawkScript.HTTP_NEW_LINE);
			builder.append(HawkScript.HTTP_NEW_LINE);

			NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
			if (center != null && center.getLevel() > 0) {
				builder.append("NationTech:");
				builder.append(HawkScript.HTTP_NEW_LINE);
				for (EffType type : EffType.values()) {
					int val = center.getEffValue(type.getNumber());
					if (val > 0) {
						builder.append(type.getNumber()).append(" : ").append(val).append(HawkScript.HTTP_NEW_LINE);
					}
				}
			}
			
			PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
			if(Objects.isNull(hero)){
				return successResponse(builder.toString());
			}
			
			builder.append("Hero: (march effect only)");
			builder.append(HawkScript.HTTP_NEW_LINE);
			for (EffType type : EffType.values()) {
				int val = hero.getBattleEffect(type,EffectParams.getDefaultVal());
				if (val > 0) {
					builder.append(type.getNumber()).append(" : ").append(val).append(HawkScript.HTTP_NEW_LINE);
				}
			}
			
			builder.append(HawkScript.HTTP_NEW_LINE);
			builder.append(HawkScript.HTTP_NEW_LINE);
			builder.append("Player And Hero:");
			builder.append(HawkScript.HTTP_NEW_LINE);
			
			EffectParams effParams = new EffectParams();
			effParams.setHeroIds(Arrays.asList(heroId));
			effParams.setSuperSoliderId(superSoldierId);
			
			for (EffType type : EffType.values()) {
				int val = player.getEffect().getEffVal(type, effParams);
				if (val > 0) {
					builder.append(type.getNumber()).append(" : ").append(val).append(HawkScript.HTTP_NEW_LINE);
				}
			}
			builder.append(HawkScript.HTTP_NEW_LINE);
			builder.append("Hero Info");
			builder.append(HawkScript.HTTP_NEW_LINE);
			builder.append(JsonFormat.printToString(hero.toPBobj()));
			
			return successResponse(builder.toString());

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
