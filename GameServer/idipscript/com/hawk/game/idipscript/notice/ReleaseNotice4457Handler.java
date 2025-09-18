package com.hawk.game.idipscript.notice;

import org.hawk.cryption.HawkMd5;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.BillboardInfo;
import com.hawk.common.BillboardInfo.BillboardScene;
import com.hawk.common.BillboardInfo.BillboardType;
import com.hawk.common.service.BillboardService;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发布公告 -- 10282154
 *
 * localhost:8080/script/idip/4457?NoticeType=&NoticeTitle=&Content=&UrlLink=&UrlLinkJump=&ButtonType=&StartTime=&EndTime=&Channel
 *
 * @param NoticeType     公告类型（登录前0、登录后1）
 * @param NoticeTitle    公告标题
 * @param Content        公告内容
 * @param UrlLink        拍脸图的url
 * @param UrlLinkJump    跳转url链接
 * @param ButtonType   按键类型，1：红色  2：蓝色 3：其它待扩展
 * @param StartTime    公告开始生效时间
 * @param EndTime      公告结束生效时间
 * @param Channel      渠道（0，为所有渠道显示，非0指定渠道显示）
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4457")
public class ReleaseNotice4457Handler extends IdipScriptHandler {
	
	protected static final int BEFORE_LOGIN = 0;
	protected static final int AFTER_LOGIN = 1;
	protected static final int AFTER_LOGIN_BOX = 2;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int noticeType = request.getJSONObject("body").getIntValue("NoticeType");
		String title = request.getJSONObject("body").getString("NoticeTitle");
		String content = request.getJSONObject("body").getString("Content");
		String urlLink = request.getJSONObject("body").getString("UrlLink");
		String urlLinkJump = request.getJSONObject("body").getString("UrlLinkJump");
		int buttonType = request.getJSONObject("body").getIntValue("ButtonType");
		long startTime = request.getJSONObject("body").getLongValue("StartTime");
		long endTime = request.getJSONObject("body").getLongValue("EndTime");
		int channelId = request.getJSONObject("body").getIntValue("Channel");
		
		int billboardScene = noticeType == BEFORE_LOGIN ?  BillboardScene.BEFORE_LOGIN : BillboardScene.AFTER_LOGIN;
		int billboardType = BillboardType.JUMP;
		content = urlLink;
		
		BillboardInfo billboardInfo = new BillboardInfo(billboardScene, billboardType);
		billboardInfo.setId(String.format("%d-%s", billboardScene, HawkOSOperator.randomUUID()));
		
		billboardInfo.setTitle(IdipUtil.decode(title));
		billboardInfo.setContent(IdipUtil.decode(content));
		billboardInfo.setStartTime((int)startTime);
		billboardInfo.setEndTime((int)endTime);
		billboardInfo.setStatus(BillboardInfo.BillboardStatus.PUBLISHED);
		billboardInfo.setCondPlatform("*");
		billboardInfo.setCondChannelId(String.valueOf(channelId));
		billboardInfo.setName(IdipUtil.decode(title));
		billboardInfo.setRedirect(IdipUtil.decode(urlLinkJump));
		billboardInfo.setButtonType(buttonType);
		
		{
			String calcMd5 = HawkMd5.makeMD5(String.format("%d-%s-%s", billboardScene, billboardInfo.getContent(), billboardInfo.getCondChannelId()));
			String key = "billboardMd5:" + calcMd5;
			boolean setSucc = RedisProxy.getInstance().getRedisSession().setNx(key, billboardInfo.getId());
			if (!setSucc) {
				String billboardId = RedisProxy.getInstance().getRedisSession().getString(key);
				billboardInfo.setId(billboardId != null ? billboardId : billboardInfo.getId());
			}
		}
		
		BillboardService.getInstance().updateBillboard(billboardInfo, GsConfig.getInstance().getServerId());
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		// 添加敏感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		
		return result;
	} 
	
}
