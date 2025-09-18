package com.hawk.game.idipscript.fourth;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 活动查询
 *
 * localhost:8080/script/idip/4251?PageNo=&StartTime=&EndTime=
 *
 * @param StartTime  时间段（时间段内，仍然开启的活动）
 * @param EndTime   时间段（时间段内，仍然开启的活动）
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4251")
public class QueryActivityHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
//		long startTime = request.getJSONObject("body").getLongValue("StartTime");
//		long endTime = request.getJSONObject("body").getLongValue("EndTime");
		
		int pageNum = request.getJSONObject("body").getIntValue("PageNo");
		int indexStart = pageNum > 1 ? (pageNum - 1) * IdipUtil.PAGE_SHOW_COUNT : 0;
		int indexEnd = indexStart + IdipUtil.PAGE_SHOW_COUNT;
		indexStart += 1;
		
		ConfigIterator<ActivityCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCfg.class);
		List<ActivityCfg> list = configIterator.stream()
				.filter(e -> !e.isInvalid() && !SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ACTIVITY, e.getActivityId()))
				.collect(Collectors.toList());
		JSONArray activityArray = new JSONArray();
		int count = 0;
		for (ActivityCfg activityCfg : list) {
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
//			String beginTimeStr = activityCfg.getFirstStartTime();
//			long beginDate = HawkTime.parseTime(beginTimeStr);
//			long endDate = beginDate + activityCfg.getOpenDuration();
//			if (endDate < startTime || beginDate > endTime) {
//				continue;
//			}
			
			JSONObject activity = new JSONObject();
			String title = activityCfg.getActivityName();
			
//			activity.put("BeginDate", beginDate);
//			activity.put("EndDate", endDate);
			activity.put("ActivityTitle", IdipUtil.encode(title));
			activity.put("ActivityId", activityCfg.getActivityId());
			activityArray.add(activity);
		}
		
		result.getBody().put("TotalPageNo", (int)Math.ceil(count * 1.0d /IdipUtil.PAGE_SHOW_COUNT));
		result.getBody().put("ActivityList_count", activityArray.size());
		result.getBody().put("ActivityList", activityArray);
		
		return result;
	}
}
