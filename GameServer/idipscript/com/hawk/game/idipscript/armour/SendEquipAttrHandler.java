package com.hawk.game.idipscript.armour;

import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.module.PlayerArmourModule;
import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发送装备带属性请求 -- 10282163
 *
 * localhost:8080/script/idip/4475
 * 
    <entry name="EquipId" type="uint64" desc="装备配置ID" test="100" isverify="true" isnull="true"/>
    <entry name="Lv" type="uint32" desc="装备等级" test="1" isverify="true" isnull="true"/>
    <entry name="Quality" type="uint32" desc="装备品质" test="1" isverify="true" isnull="true"/>
    <entry name="Star" type="uint32" desc="装备星级" test="1" isverify="true" isnull="true"/>
    <entry name="ExtraAttrList1" type="string" size="MAX_EXTRAATTRLIST1_LEN" desc="装备额外属性1：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="ExtraAttrList2" type="string" size="MAX_EXTRAATTRLIST2_LEN" desc="装备额外属性2：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="ExtraAttrList3" type="string" size="MAX_EXTRAATTRLIST3_LEN" desc="装备额外属性3：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="ExtraAttrList4" type="string" size="MAX_EXTRAATTRLIST4_LEN" desc="装备额外属性4：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="ExtraAttrList5" type="string" size="MAX_EXTRAATTRLIST5_LEN" desc="装备额外属性5：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="ExtraAttrList6" type="string" size="MAX_EXTRAATTRLIST6_LEN" desc="装备额外属性6（预留）：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="StuntAttrList" type="string" size="MAX_STUNTATTRLIST_LEN" desc="装备特技属性" test="test" isverify="false" isnull="true"/>
    <entry name="StarAttr1" type="string" size="MAX_STARATTR1_LEN" desc="装备星级属性1：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="StarAttr2" type="string" size="MAX_STARATTR2_LEN" desc="装备星级属性2：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="StarAttr3" type="string" size="MAX_STARATTR3_LEN" desc="装备星级属性3：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="StarAttr4" type="string" size="MAX_STARATTR4_LEN" desc="装备星级属性4（预留）：需要支持同时输入6组数字" test="test" isverify="false" isnull="true"/>
    <entry name="QuantumLv" type="uint64" desc="装备量子等级" test="100" isverify="true" isnull="true"/>
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4475")
public class SendEquipAttrHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		long equipId = request.getJSONObject("body").getLongValue("EquipId");
		// 对参数进行检测
		if (equipId < 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "EquipId param invalid");
			return result;
		}
		
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getTaskExecutor();
		int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				updateEquipAttr(player, equipId, request);
				return null;
			}
			
		}, threadIndex);

		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	private void updateEquipAttr(Player player, long equipId, JSONObject request) {
		// 等级 "Lv":60,
		int lv = request.getJSONObject("body").getIntValue("Lv");
		// 品质 "Quality":4,
		int quality = request.getJSONObject("body").getIntValue("Quality");
		// 星级 "Star":40,
		int star = request.getJSONObject("body").getIntValue("Star");
		// 量子槽位 "QuantumLv":60
		int quantumLv = request.getJSONObject("body").getIntValue("QuantumLv");

		//设置装备参数
		ArmourEntity entity = new ArmourEntity();
		entity.setPlayerId(player.getId());
		// 装备配置id
		entity.setArmourId(((int) equipId));
		// 等级
		entity.setLevel(lv);
		// 品质
		entity.setQuality(quality);
		// 星级
		for (int i = 0; i < star; i++) {
			entity.addStar();
		}
		// 量子槽位
		for (int i = 0; i < quantumLv; i++){
			entity.addQuantum();
		}
		
		// 装备额外属性 "ExtraAttrList1":"738_1024_1200_0_0_0","ExtraAttrList2":"846_107_800_0_0_0","ExtraAttrList3":"726_1008_1200_0_0_0","ExtraAttrList4":"834_105_800_0_0_0","ExtraAttrList5":"732_1016_1200_0_0_0","ExtraAttrList6":"0",
		for (int i = 1; i <= 6; i++){
			String extraAttrConst =  "ExtraAttrList" + i;
			String extraAttr = request.getJSONObject("body").getString(extraAttrConst);
			if (HawkOSOperator.isEmptyString(extraAttr) || "0".equals(extraAttr)){
				continue;
			}
			ArmourEffObject extraAttrEff = SerializeHelper.getValue(ArmourEffObject.class, extraAttr,  SerializeHelper.ATTRIBUTE_SPLIT);
			entity.addExtraAttrEff(extraAttrEff);
		}

		// 特技属性  "StuntAttrList":"0",
		String skillAttr =  request.getJSONObject("body").getString("StuntAttrList");
		if (!HawkOSOperator.isEmptyString(skillAttr) && !"0".equals(skillAttr)){
			ArmourEffObject skillEff = SerializeHelper.getValue(ArmourEffObject.class, skillAttr,  SerializeHelper.ATTRIBUTE_SPLIT);
			entity.addSkillAttrEff(skillEff);
		}
		
		// 星级属性  "StarAttr1":"8_1008_1800_22500_0_2","StarAttr2":"24_1024_1800_22500_12_2","StarAttr3":"1_100_1200_22500_0_2","StarAttr4":"3_102_1200_22500_0_2",
		for (int i = 1; i <= 4; i++){
			String starAttrConst =  "StarAttr" + i;
			String starAttr = request.getJSONObject("body").getString(starAttrConst);
			if (HawkOSOperator.isEmptyString(starAttr) || "0".equals(starAttr)){
				continue;
			}
			ArmourEffObject starEff = SerializeHelper.getValue(ArmourEffObject.class, starAttr,  SerializeHelper.ATTRIBUTE_SPLIT);
			entity.addStarEff(starEff);
		}

		// 创建实体
		entity.create();
		// 加入到缓存
		player.getData().getArmourEntityList().add(entity);
		// 玩家如果在线,推送给客户端
		if (player.isActiveOnline()) {
			player.getPush().syncArmourInfo(entity);
		}
		PlayerArmourModule module = player.getModule(GsConst.ModuleType.ARMOUR_MODULE);
		module.logArmour(entity, GsConst.ArmourChangeReason.ARMOUR_CHANGE_17);
	}

	
}
