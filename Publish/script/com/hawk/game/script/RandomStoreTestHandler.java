package com.hawk.game.script;

import java.text.NumberFormat;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.util.GameUtil;

/**
 * 随机刷新联盟宝藏 http://localhost:8080/script/randomStore?cityLevel=11&isFree=1
 * 
 * @author lwt
 * @date 2017年11月24日
 */
public class RandomStoreTestHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		final String newLine = "<br/>";
		int cityLevel = NumberUtils.toInt(params.get("cityLevel"));
		boolean isfree = NumberUtils.toInt(params.get("isFree")) == 1;
		AtomicLongMap<Integer> map = AtomicLongMap.create();
		final double MAX = 1000000;
		for (int i = 0; i < MAX; i++) {
			int store = GameUtil.randomStore(cityLevel, isfree, 0);
			map.incrementAndGet(store);
		}

		NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(2);

		StringBuilder sb = new StringBuilder();
		sb.append((int) MAX + "次").append(newLine);
		map.asMap().forEach((k, v) -> {
			sb.append(k).append(" > ").append(v).append(" 占比:").append(nt.format(v / MAX)).append(newLine);
		});

		return sb.toString();
	}

}
