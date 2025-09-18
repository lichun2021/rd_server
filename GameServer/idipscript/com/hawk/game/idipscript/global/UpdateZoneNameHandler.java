package com.hawk.game.idipscript.global;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改普通区服与专服名称请求  -- 10282161
 *
 * localhost:8080/script/idip/4471
 * 
    <entry name="Partition" type="uint32" desc="小区号：小区id" test="1" isverify="true" isnull="true"/>
    <entry name="PlatId" type="uint8" desc="平台：IOS（0），安卓（1）" test="1" isverify="true" isnull="true"/>
    <entry name="NameType" type="uint32" desc="名称类型:1服务器名称2专服标识" test="1" isverify="true" isnull="true"/>
    <entry name="ServerColor" type="string" size="MAX_SERVERCOLOR_LEN" desc="服务器/专服名称颜色" test="test" isverify="false" isnull="true"/>
    <entry name="ServerPic" type="uint32" desc="专服背景图：1-10的整数数字" test="1" isverify="true" isnull="true"/>
    <entry name="Name" type="string" size="MAX_NAME_LEN" desc="修改后名称" test="test" isverify="false" isnull="true"/>
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4471")
public class UpdateZoneNameHandler extends IdipScriptHandler {
	
	static final String SERVER_NAME = "SERVER_NAME";
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		String serverId = request.getJSONObject("body").getString("Partition");
		int type = request.getJSONObject("body").getIntValue("NameType");  // 名称类型:1服务器名称2专服标识
		String serverColor = request.getJSONObject("body").getString("ServerColor"); // 服务器/专服名称颜色
		int serverPic = request.getJSONObject("body").getIntValue("ServerPic");  // 专服背景图：1-10的整数数字
		String name = request.getJSONObject("body").getString("Name"); // 修改后名称
		JSONObject json = new JSONObject();
		if(type == 1){
			json.put("n", name == null ? "" : name);
			json.put("c", serverColor == null ? "" : serverColor);
		}
		if(type == 2){
			json.put("pty", "1");
			json.put("pty_n", name == null ? "" : name);
			json.put("pty_c", serverColor == null ? "" : serverColor);
			json.put("pty_b", String.valueOf(serverPic));
		}
		
		//已经在GM中转服写过了，这里可以不用再往微信redis写了
		RedisProxy.getInstance().getRedisSession().hSet(SERVER_NAME, serverId, json.toJSONString());
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
