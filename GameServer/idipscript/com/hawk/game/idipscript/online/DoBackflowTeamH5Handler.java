package com.hawk.game.idipscript.online;

import java.util.ArrayList;
import java.util.List;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PlayerTeamBackH5Event;
import com.hawk.activity.type.impl.playerteamback.entity.MemberInfo;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 回流组队H5活动 -- 10282136
 *
 * localhost:8081/idip/4415
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4415")
public class DoBackflowTeamH5Handler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		// 鉴定星数量（0 ：未鉴定； 其他：鉴定星数量）
		int starNum = request.getJSONObject("body").getIntValue("StartNum"); 
		// 组队ID（0 未组队； 其他：组队ID- 代表有组队）
		int teamId = request.getJSONObject("body").getIntValue("TeamId");     
		// 组队成员信息列表: OpenId、PlatId、Partition、Back（类型：0非回流， 1回流）
		JSONArray memberList = request.getJSONObject("body").getJSONArray("MemberInfoList");
		// 奖励情况列表（1：活跃礼包奖励（个人连续登录奖励）；2：幸运抽次数奖励；3：组队礼包奖励；4：任务礼包奖励）
		JSONArray rewardInfoList = request.getJSONObject("body").getJSONArray("RewardInfoList");  
		
		PlayerTeamBackH5Event event = new PlayerTeamBackH5Event(player.getId());
		event.setStarNum(starNum);
		event.setTeamId(teamId);
		event.setRewardList(rewardInfoList.toJavaList(Integer.class));
		List<MemberInfo> memberInfoList = new ArrayList<MemberInfo>();
		for (int i=0; i<memberList.size(); i++) {
			JSONObject json = memberList.getJSONObject(i);
			MemberInfo memberInfo = MemberInfo.valueOf(json.getString("OpenId"), json.getIntValue("PlatId"), 
					String.valueOf(json.getIntValue("Partition")), json.getIntValue("Back"), json.getIntValue("GroupTs"));
			memberInfoList.add(memberInfo);
		}
		event.setMemberList(memberInfoList);
		ActivityManager.getInstance().postEvent(event);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}


