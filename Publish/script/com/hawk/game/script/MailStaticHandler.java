package com.hawk.game.script;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.config.MailsysCfg;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.service.MailService;

import redis.clients.jedis.Jedis;

/**
 * 邮件统计 http://localhost:8080/script/mailstatic?playerId=7pu-ovdn-1
 * 
 * @author lwt
 * @date 2017年11月23日
 */
public class MailStaticHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		DecimalFormat df = new DecimalFormat("#.##");
		StringBuilder sb = new StringBuilder();
		try (Jedis jedis = LocalRedis.getInstance().getRedisSession().getJedis()) {
			final String newLine = "<br/>";
			int[] types = allMailTypes();
			String playerId = params.get("playerId");
			double totalByte = 0;
			double maxbyte = 0;
			for (int type : types) {
				if (type == 0 || type == 1) {
					continue;
				}
				String keySort = MailService.getInstance().keySort(playerId, type);
				Set<String> mailIds = jedis.zrange(keySort, 0, -1);
				sb.append("邮件类型:" + type2String(type))
						.append("			总数:" + mailIds.size())
						.append(newLine);
				if (mailIds.isEmpty()) {
					continue;
				}

				double total = 0;
				for (String mailId : mailIds) {
					double entityLen = jedis.strlen(MailService.getInstance().keyEntity(mailId));
					double contenLen = jedis.strlen(MailService.getInstance().keyContent(mailId));
					sb.append("Entity:" + entityLen)
							.append("   Content:" + contenLen)
							.append("   Mail size:" + (entityLen + contenLen))
							.append(newLine);
					total = total + entityLen + contenLen;
					totalByte = totalByte + entityLen + contenLen;
				}

				maxbyte = maxbyte + total / mailIds.size() * 999;

				sb.append("平均:" + df.format(total / mailIds.size()) + " byte")
						.append("___合计:")
						.append(df.format(total / 1024 / 1024) + "M      ")
						.append("    999封占用:" + df.format(total / 1024 / 1024 / mailIds.size() * 999) + " M")
						.append(newLine)
						.append(newLine);
			}
			sb.append("总计:" + df.format(totalByte / 1024 / 1024) + "M      ");
			sb.append("极限情况全999总计:" + df.format(maxbyte / 1024 / 1024) + "M      ");
		}
		return sb.toString();
	}

	private String type2String(int type) {
		switch (type) {
		case 2:
			return "联盟信息";
		case 3:
			return "活动消息";
		case 4:
			return "系统消息";
		case 5:
			return "战斗报告";
		case 6:
			return "采集报告";
		case 7:
			return "尤里残部/野怪战报";
		case 8:
			return "尤里复仇";

		default:
			break;
		}
		return "艹";
	}

	/**
	 * 所有已知的邮件类型
	 * 
	 * @return
	 */
	private int[] allMailTypes() {
		return HawkConfigManager.getInstance().getConfigIterator(MailsysCfg.class).stream().mapToInt(MailsysCfg::getNewPageType).distinct().sorted().toArray();
	}
}
