package com.hawk.game.script;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.GlobalMail;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

/**
 * 发送全服带奖励邮件
 *
 * localhost:8080/script/globalMail?title=测试&content=邮件发钻测试&channel=&platform=&award=a_b_c,a_b_c&count=10&weblink=www.baidu.com
 *
 * @param channel    渠道(默认全)
 * @param platform   平台(默认全)
 * @param title      邮件标题
 * @param content    邮件内容
 * @param weblink    跳转链接, 如果有的话
 * @param award      奖励物品
 * @param count      一次发送count封邮件
 * 
 * @author lating
 */
public class SendGlobalMailHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String title = params.get("title");
			String content = params.get("content");
			String weblink = params.get("weblink");
			String channel = params.get("channel");
			String platform = params.get("platform");
			long expire = NumberUtils.toLong(params.get("expire"),TimeUnit.DAYS.toSeconds(7)) * 1000;
			int count = NumberUtils.toInt(params.get("count"));
			if (count <= 0) {
				count = 1;
			}
			
			try {
				title = new String(title.getBytes(), "utf-8");
				content = new String(content.getBytes(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				HawkException.catchException(e);
			}

			String awardStr = params.get("award");
			List<ItemInfo> items = ItemInfo.valueListOf(awardStr);
			
			long currTime = HawkTime.getMillisecond();
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<count; i++) {
				MailParames.Builder parBul = MailParames.newBuilder()
						.setMailId(MailId.REWARD_MAIL)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addSubTitles(title)
						.addContents(content)
						.addRewards(items);
				if (StringUtils.isNotEmpty(weblink)) {
					parBul.addContents("weblink=" + weblink);
					parBul.setAwardStatus(MailRewardStatus.GET);
				}
				GlobalMail mail = SystemMailService.getInstance().addGlobalMail(parBul.build()
						,currTime
						, currTime + expire,channel,platform);
				sb.append(mail.getUuid()).append(",");
			}
			
			String uuids = sb.deleteCharAt(sb.length() - 1).toString();
			logger.info("send global reward mail, mailUuid: {}, award: {}", uuids, awardStr);
			return HawkScript.successResponse(uuids);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
