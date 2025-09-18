package com.hawk.game.module.lianmengtaiboliya.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tbly_npc.xml")
public class TBLYNpcCfg extends HawkConfigBase {
	// <data id="1" playerName="小帅1" guildName="联盟一" guildTag="联盟一" icon="10" foggyId="9001" effectList="100_0|102_0|136_0" buildAtkBuff="100_0|102_0|136_0" />
	@Id
	protected final int id;
	protected final String playerName;
	protected final String guildName;
	protected final String guildTag;
	protected final int icon;
	protected final int mirror;
	protected final String buildAtkBuff;
	protected final int atkBuffTime;
//	private Map<EffType, Integer> effectListMap;
	private Map<EffType, Integer> buildAtkBuffMap;

//	protected final int foggyId;
//	protected final String effectList;
	public TBLYNpcCfg() {
		id = 0;
		mirror = 3000;
		playerName = "";
		atkBuffTime = 0;
		guildName = "";
		guildTag = "";
		icon = 0;
//		foggyId = 0;
//		effectList = "";
		buildAtkBuff = "";
	}

	@Override
	protected boolean assemble() {
//		{
//			effectListMap = new HashMap<>();
//			if (!HawkOSOperator.isEmptyString(effectList)) {
//				String[] array = effectList.split(",");
//				for (String val : array) {
//					String[] info = val.split("_");
//					effectListMap.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
//				}
//			}
//			effectListMap = ImmutableMap.copyOf(effectListMap);
//		}
		{
			buildAtkBuffMap = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(buildAtkBuff)) {
				String[] array = buildAtkBuff.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					buildAtkBuffMap.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			buildAtkBuffMap = ImmutableMap.copyOf(buildAtkBuffMap);
		}
		return true;
	}

//	public Map<EffType, Integer> getEffectListMap() {
//		return effectListMap;
//	}
//
//	public void setEffectListMap(Map<EffType, Integer> effectListMap) {
//		this.effectListMap = effectListMap;
//	}

	public Map<EffType, Integer> getBuildAtkBuffMap() {
		return buildAtkBuffMap;
	}

	public void setBuildAtkBuffMap(Map<EffType, Integer> buildAtkBuffMap) {
		this.buildAtkBuffMap = buildAtkBuffMap;
	}

	public int getId() {
		return id;
	}

	public String getPlayerName() {
		return playerName;
	}

	public String getGuildName() {
		return guildName;
	}

	public String getGuildTag() {
		return guildTag;
	}

	public int getIcon() {
		return icon;
	}

//	public int getFoggyId() {
//		return foggyId;
//	}
//
//	public String getEffectList() {
//		return effectList;
//	}

	public String getBuildAtkBuff() {
		return buildAtkBuff;
	}

	public int getAtkBuffTime() {
		return atkBuffTime;
	}

	public int getMirror() {
		return mirror;
	}

	
}
