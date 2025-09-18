package com.hawk.game.idipscript.fourth;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.BillboardInfo;
import com.hawk.common.BillboardInfo.BillboardScene;
import com.hawk.common.service.BillboardService;
import com.hawk.game.GsConfig;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询公告
 *
 * localhost:8080/script/idip/4179?NoticeType=&PageNo=&StartTime=&EndTime=
 *
 * @param NoticeType 公告类型（登录前1、登录后2）
 * @param PageNo     页码
 * @param StartTime  时间段（时间段内，所有发送的公告）
 * @param EndTime    时间段（时间段内，所有发送的公告）
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4179")
public class QueryNoticeHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int noticeType = request.getJSONObject("body").getIntValue("NoticeType");
		if (noticeType != BillboardScene.BEFORE_LOGIN && noticeType != BillboardScene.AFTER_LOGIN) {
			result.getHead().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getHead().put("RetErrMsg", "NoticeType param error, NoticeType should be one of 1 or 2");
			return result;
		}
		
		long startTime = request.getJSONObject("body").getLongValue("StartTime");
		long endTime = request.getJSONObject("body").getLongValue("EndTime");
		
		int pageNum = request.getJSONObject("body").getIntValue("PageNo");
		int indexStart = pageNum > 1 ? (pageNum - 1) * IdipUtil.PAGE_SHOW_COUNT : 0;
		int indexEnd = indexStart + IdipUtil.PAGE_SHOW_COUNT;
		indexStart += 1;
		
		List<BillboardInfo> list = BillboardService.getInstance().getBillboardList(noticeType, GsConfig.getInstance().getServerId()).stream()
				.filter(e -> e.getCreateTime() >= startTime && e.getCreateTime() <= endTime).collect(Collectors.toList());
		JSONArray noticeArray = new JSONArray();
		int count = 0;
		for (BillboardInfo noticeInfo : list) {
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
			JSONObject notice = new JSONObject();
			notice.put("Partition", GsConfig.getInstance().getServerId());
			notice.put("NoticeType", noticeType);
			notice.put("NoticeStart", noticeInfo.getStartTime());
			notice.put("NoticeEnd", noticeInfo.getEndTime());
			notice.put("NoticeId", noticeInfo.getId());
			notice.put("NoticeTitle", IdipUtil.encode(noticeInfo.getTitle()));
			notice.put("NoticeContent",  IdipUtil.encode(noticeInfo.getContent()));
			noticeArray.add(notice);
		}
		
		result.getBody().put("TotalPageNo", (int)Math.ceil(count * 1.0d /IdipUtil.PAGE_SHOW_COUNT));
		result.getBody().put("NoticeList1_count", noticeArray.size());
		result.getBody().put("NoticeList1", noticeArray);
		
		return result;
	}
}
