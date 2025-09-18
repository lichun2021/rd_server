package com.hawk.game.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.config.GachaCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.gacha.GachaOprator;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 招募不扣费
 * 
 * http://localhost:8080/script/gacha?playerName=LLLLLL&type=101&count=100000
 * 
 * @author lwt
 */
public class GachaTestHandler extends HawkScript {
    int COUNT = 10000;
    final String newLine = "<br/>";

    @Override
    public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
        String typeStr = params.get("type");
        COUNT = NumberUtils.toInt(params.get("count"), 10000); 
        final GachaType gachaType = GachaType.valueOf(Integer.parseInt(typeStr));
        if (Objects.isNull(gachaType)) {
            return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "All types allowed :101 110 201 210 301 310");
        }
        Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
        PlayerGachaEntity gachaEntity = player.getData().getGachaEntityByType(gachaType);
        GachaCfg gachaCfg = HawkConfigManager.getInstance().getConfigByKey(GachaCfg.class, gachaType.getNumber());

        GachaOprator gachaOprator = GachaOprator.of(gachaType);
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < COUNT; i++) {
            List<String> result = gachaOprator.gacha(gachaCfg, gachaEntity, player);
            result.stream()
                    .map(str -> str.split("_"))
                    .map(arr -> new HawkTuple2<>(arr[1], Integer.parseInt(arr[2])))
                    .forEach(h -> map.merge(h.first, h.second, (v1, v2) -> v1 + h.second));
        }

        final double total = map.values().stream().mapToInt(Integer::intValue).sum();
        StringBuilder sb = new StringBuilder();
        sb.append("****************************************************************************")
                .append(newLine)
                .append("type : " + gachaType.getNumber() + " 抽取次数 : " + COUNT)
                .append(newLine)
                .append("ItemId   ,     count  ,  %")
                .append(newLine);
        map.forEach((k, v) -> {
            sb.append(k + "   ,  " + v + " , " + String.format("%.2f", v * 100 / total) + "%").append(newLine);
        });

        return HawkScript.successResponse(sb.toString());
    }

    static final Map<Integer, String> MAP = new HashMap<>();
}
