package com.hawk.game.idipscript.notice;

import java.util.List;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.BillboardInfo;
import com.hawk.common.service.BillboardService;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询个人公告（查询可跳转游戏内拍脸图ID（个人）请求）-- 10282189
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4527")
public class QueryPersonalNoticeHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		List<BillboardInfo> list = BillboardService.getInstance().getPlayerBillboardList(player.getId());
		JSONArray noticeArray = new JSONArray();
		for (BillboardInfo noticeInfo : list) {
			JSONObject notice = new JSONObject();
			notice.put("TapFaceId", noticeInfo.getId());
			notice.put("TapFaceTitle", IdipUtil.encode(noticeInfo.getTitle()));
			noticeArray.add(notice);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("TapFaceIdList_count", noticeArray.size());
		result.getBody().put("TapFaceIdList", noticeArray);
		return result;
	}
	
}
