package com.hawk.game.log;

import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.player.Player;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 行为日志
 *
 * @author hawk
 * @date 2014-7-24
 */
public class BehaviorLogger {
	/**
	 * 日志参数
	 *
	 * @author hawk
	 * @date 2014-7-24
	 */
	public static class Params {
		private String name;
		private Object value;

		public static Params valueOf(String name, Object value) {
			Params params = new Params();
			params.setName(name);
			params.setValue(value);
			return params;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Object getValue() {
			return value;
		}

		public String getStringValue() {
			if (value == null) {
				return "null";
			}
			return value.toString();
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}

	/**
	 * 行为日志，数据流变化日志记录器
	 */
	private static final Logger ACTION_LOGGER = LoggerFactory.getLogger("Action");

	/**
	 * 记录行为日志
	 *
	 * @param playerId
	 * @param source
	 * @param action
	 * @param params
	 */
	public static void log4Service(String msg, boolean serverIdentify) {
		try {
			if (serverIdentify) {
				ACTION_LOGGER.info("[{}] {}", GsConfig.getInstance().getServerId(), msg);
			} else {
				ACTION_LOGGER.info(msg);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 以玩家id为key统计
	 *
	 * @param playerId
	 * @param source
	 * @param action
	 * @param params
	 */
	public static void log4Player(String playerId, Source source, Action action, Params... params) {
		try {
			JSONObject jsonObject = new JSONObject();
			// 行为时间
			jsonObject.put("playerId", playerId);
			jsonObject.put("source", source.name());
			jsonObject.put("action", action.name());

			JSONObject paramsJson = new JSONObject();
			for (Params param : params) {
				paramsJson.put(param.getName(), param.getStringValue());
			}
			jsonObject.put("data", paramsJson);
			// 记录日志信息
			log4Service(jsonObject.toString(), false);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 数据流统计，主要用户客服问题查询
	 *
	 * @param player
	 * @param source
	 * @param action
	 */
	public static void log4Service(Player player, Source source, Action action, Params... params) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("source", source.name());
			jsonObject.put("action", action.name());

			JSONObject paramsJson = new JSONObject();
			for (Params param : params) {
				paramsJson.put(param.getName(), param.getStringValue());
			}
			jsonObject.put("data", paramsJson);

			if (player != null) {
				// 行为日志参数
				jsonObject.put("playerId", player.getId());
				jsonObject.put("playerName", player.getName());
				jsonObject.put("puid", player.getPuid());
				jsonObject.put("deviceId", player.getDeviceId());
				jsonObject.put("channel", player.getChannel());
				jsonObject.put("playerLevel", player.getLevel());
				jsonObject.put("vipLevel", player.getVipLevel());
				jsonObject.put("cityLevel",player.getCityLevel());
				jsonObject.put("guildId", player.getGuildId());
			}

			// 记录日志信息
			log4Service(jsonObject.toString(), false);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
