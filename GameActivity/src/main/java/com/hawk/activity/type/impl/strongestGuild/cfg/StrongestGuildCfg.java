package com.hawk.activity.type.impl.strongestGuild.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.serialize.string.SerializeHelper;


/**
 * 王者联盟配置
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/strongest_alliance/activity_strongest_allian.xml")
public class StrongestGuildCfg extends HawkConfigBase {
	
	/** 阶段id*/
	@Id
	private final int stageId;
	
	/** 阶段名称*/
	private final String name;
	
	/** 循环顺序 服务器用不到 */
	private final int order;
	
	/** 条件类型*/
	private final int funcType;
	
	/** 积分值*/
	private final String scoreCof;
	
	/** 阶段积分权重系数*/
	private final int scoreWeightCof;
	
	/** 准备时间xx秒*/
	private final int prepareTime;
	
	/** 持续时间xx秒*/
	private final int continueTime;
	
	/** 目标奖励id，格式：id_id_id*/
	private final String targetId;
	
	/** 阶段排名奖励id*/
	private final int rankId;
	
	/** 积分上限（如果为0，则无积分上限） **/
	private final String scoreLimit;
	
	/** 下一个阶段id **/
	private final int nextStageId;
	
	/** 上一个阶段id **/
	private final int beforeStageId;
	
	/** 联盟的排行奖励rankid **/
	private final int allianceRankId;
	
	private List<Integer> targetIdList;
	
	private List<Integer> indexScoreList;
	
	private Map<Integer, Integer> indexScoreMap;
	
	private Map<Integer, List<Integer>> indexScoreListMap;
	
	private long killScoreLimit;
	
	private long hurtScoreLimit;
	
	private long stageScoreLimit;
	
	private StrongestTargetType targetType;
	
	public StrongestGuildCfg() {
		stageId = 0;
		name = "";
		order = 0;
		funcType = 0;
		scoreCof = "";
		scoreWeightCof = 0;
		prepareTime = 0;
		continueTime = 0;
		targetId = "";
		rankId = 0;
		scoreLimit = "";
		nextStageId = 0;
		beforeStageId = 0;
		allianceRankId = 0;
	}
	
	@Override
	protected boolean assemble() {
		targetType = StrongestTargetType.getType(funcType);
		if (targetType == null) {
			HawkLog.errPrintln("StrongestTargetType not exist! funcType: {}", funcType);
			return false;
		}
		targetIdList = SerializeHelper.stringToList(Integer.class, targetId, SerializeHelper.ATTRIBUTE_SPLIT);
		if (targetType.getConfigType() == StrongestTargetType.ConfigType.LIST) {
			indexScoreList = SerializeHelper.stringToList(Integer.class, scoreCof, SerializeHelper.ATTRIBUTE_SPLIT);
		}
		if (targetType.getConfigType() == StrongestTargetType.ConfigType.MAP) {
			indexScoreMap = SerializeHelper.stringToMap(scoreCof, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
		}
		if(targetType.getConfigType() == StrongestTargetType.ConfigType.OTHER){
			String src[] = scoreCof.split(SerializeHelper.BETWEEN_ITEMS);
			indexScoreListMap = new HashMap<Integer, List<Integer>>();
			if(src.length != 2){
				throw new RuntimeException("ActivityCircularCfg error, BREAK_WAR config scoreCof error :" + scoreCof);
			}
			/** 0为击杀积分系数列表    1为击伤积分系数列表 **/
			indexScoreListMap.put(0, SerializeHelper.stringToList(Integer.class, src[0], SerializeHelper.ATTRIBUTE_SPLIT));
			indexScoreListMap.put(1, SerializeHelper.stringToList(Integer.class, src[1], SerializeHelper.ATTRIBUTE_SPLIT));
			if(scoreLimit != null && !scoreLimit.trim().equals("")){
				String s[] = scoreLimit.split(SerializeHelper.ATTRIBUTE_SPLIT);
				killScoreLimit = Long.parseLong(s[0]);
				hurtScoreLimit = Long.parseLong(s[1]);
			}
		}else{
			if(scoreLimit != null && !scoreLimit.trim().equals("")){
				stageScoreLimit = Integer.parseInt(scoreLimit);
			}
		}
		
		return true;
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

	public int getStageId() {
		return stageId;
	}
	
	public String getName() {
		return name;
	}

	public int getOrder() {
		return order;
	}

	public int getFuncType() {
		return funcType;
	}

	public String getScoreCof() {
		return scoreCof;
	}

	public int getScoreWeightCof() {
		return scoreWeightCof;
	}

	public long getPrepareTime() {
		return prepareTime * 1000l;
	}

	public long getContinueTime() {
		return continueTime * 1000l;
	}

	public String getTargetId() {
		return targetId;
	}

	public int getRankId() {
		return rankId;
	}
	
	public int getNextStageId() {
		return nextStageId;
	}

	public long getScoreLimit() {
		return stageScoreLimit;
	}

	public long getKillScoreLimit() {
		return killScoreLimit;
	}

	public long getHurtScoreLimit() {
		return hurtScoreLimit;
	}

	public List<Integer> getTargetIdList() {
		return targetIdList;
	}
	
	public List<Integer> getIndexScoreList() {
		return indexScoreList;
	}
	
	public Map<Integer, Integer> getIndexScoreMap() {
		return indexScoreMap;
	}
	
	public StrongestTargetType getTargetType() {
		return targetType;
	}

	public Map<Integer, List<Integer>> getIndexScoreListMap() {
		return indexScoreListMap;
	}

	public int getBeforeStageId() {
		return beforeStageId;
	}

	public int getAllianceRankId() {
		return allianceRankId;
	}
}
