package com.hawk.game.script;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tuple.HawkTuple3;

import com.hawk.game.config.CyborgSeasonInitCfg;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.util.GsConst;

/**
 * 打印玩家作用号属性
 * localhost:8080/script/clwtest?cnt=100
 * playerName: 玩家名字
 * 
 * @author Jesse
 *
 */
public class CyborgSeasonInitTestHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			int cnt = NumberUtils.toInt(params.get("cnt"));
			calcStars(cnt);
			return HawkScript.failedResponse(ScriptError.SUCCESS_OK_VALUE, "");
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
	/**
	 * 计算战队初始额外星级
	 */
	private void calcStars(int count) {
		List<HawkTuple3<CyborgSeasonInitCfg, Long, Long>> list = CyborgWarService.getInstance().getInitRange(count);
		String output = System.getProperty("user.dir") + "/logs/CLWStar.txt";
		File file = new File(output);
		if (file.exists()) {
			file.delete();
		}
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(output, true);
			fileWriter.write("********************************************战队数量:" + count + "*****************************************************");
			fileWriter.write("排名\t" + "额外星数\t" + "领先比例\t");
			fileWriter.write("\r\n");
			fileWriter.write("\r\n");
			fileWriter.write("\r\n");
			for (int i = 0; i < count; i++) {
				CyborgSeasonInitCfg cfg = CyborgWarService.getInstance().getCyborgSeasonInitCfgByRank(i, list);
				StringBuilder sb = new StringBuilder();
				sb.append(i + 1).append("\t");
				if (cfg != null) {
					sb.append(cfg.getExtraStar()).append("\t").append(cfg.getTopeRate() / 100).append("%");
				} else {
					sb.append(0).append("\t").append(100);
				}
				fileWriter.write(sb.toString());
				fileWriter.write("\r\n");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e1) {
					HawkException.catchException(e1);
				}
			}
		}

	}

	public List<HawkTuple3<CyborgSeasonInitCfg, Long, Long>> getInitRange(int size) {
		ConfigIterator<CyborgSeasonInitCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonInitCfg.class);
		List<HawkTuple3<CyborgSeasonInitCfg, Long, Long>> rangeList = new ArrayList<>();
		long endIndex = 0;
		for (CyborgSeasonInitCfg cfg : its) {
			int topRate = cfg.getTopeRate();
			long indexCnt = (long) Math.ceil(1d * size * topRate / GsConst.RANDOM_MYRIABIT_BASE);
			if (endIndex != 0 && indexCnt < endIndex) {
				HawkLog.logPrintln("CyborgWarService getInitRange pass, cfgId:{}, size:{}, endIndex:{}, indexCnt:{}", cfg.getId(), size, endIndex, indexCnt);
				continue;
			}
			HawkTuple3<CyborgSeasonInitCfg, Long, Long> tuple = new HawkTuple3<CyborgSeasonInitCfg, Long, Long>(cfg, endIndex, indexCnt);
			rangeList.add(tuple);
			HawkLog.logPrintln("CyborgWarService getInitRange, cfgId:{}, size:{}, endIndex:{}, indexCnt:{}", cfg.getId(), size, endIndex, indexCnt);
			endIndex = indexCnt + 1;
		}
		return rangeList;
	}
	
	
	public CyborgSeasonInitCfg getCyborgSeasonInitCfgByRank(int index, List<HawkTuple3<CyborgSeasonInitCfg, Long, Long>> tupleList) {
		for (HawkTuple3<CyborgSeasonInitCfg, Long, Long> tuple : tupleList) {
			if (index >= tuple.second && index <= tuple.third) {
				return tuple.first;
			}
		}
		return null;
	}
}
