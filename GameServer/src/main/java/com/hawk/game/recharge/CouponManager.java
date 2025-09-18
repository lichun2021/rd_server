package com.hawk.game.recharge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.ControlProperty;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Recharge.CouponItemSync;
import com.hawk.game.protocol.Recharge.HPCouponAppInfo;
import com.hawk.game.protocol.Recharge.HPCouponCondition;
import com.hawk.game.protocol.Recharge.HPCouponItem;
import com.hawk.game.util.GameUtil;
import com.hawk.sdk.SDKManager;
import com.hawk.sdk.SDKConst.UserType;

/**
 * 微信券管理
 * 
 * @author lating
 *
 */
public class CouponManager {
	
	/**
	 * 查询所有的微信券，包括可领取和已领取的
	 * 
	 * @param player
	 */
	public static void queryAllCoupon(Player player) {
		// 开关关闭
		if (ControlProperty.getInstance().getCouponSwitch() == 0) {
			return;
		}
		
 		if (GameUtil.isWin32Platform(player) || UserType.getByChannel(player.getChannel()) != UserType.WX) {
			return;
		}
		
 		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					List<HPCouponItem> couponList = queryCoupon(player);
					List<HPCouponItem> userCouponList = queryUserCoupon(player);
					CouponItemSync.Builder builder = CouponItemSync.newBuilder();
					builder.addAllCouponItem(couponList);
					builder.addAllUserCouponItem(userCouponList);
					player.sendProtocol(HawkProtocol.valueOf(HP.code.COUPON_INFO_PUSH_S, builder));
					return null;
				}
			};
			
			task.setPriority(1);
			task.setTypeName("queryCoupon");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
		} else {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					List<HPCouponItem> couponList = queryCoupon(player);
					List<HPCouponItem> userCouponList = queryUserCoupon(player);
					CouponItemSync.Builder builder = CouponItemSync.newBuilder();
					builder.addAllCouponItem(couponList);
					builder.addAllUserCouponItem(userCouponList);
					player.sendProtocol(HawkProtocol.valueOf(HP.code.COUPON_INFO_PUSH_S, builder));
					return null;
				}
			});
		}
		
	}
	
	/**
	 * 查询可领取的微信券
	 * 
	 * @param player
	 * @return
	 */
	private static List<HPCouponItem> queryCoupon(Player player) {
		String result = SDKManager.getInstance().wxQueryGameCoupon(player.getChannel(), HawkTime.getSeconds(), player.getOpenId(), player.getPfTokenJson());
		if (result == null) {
			HawkLog.logPrintln("queryCoupon failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			return Collections.emptyList();
		}
		
		JSONObject obj = JSONObject.parseObject(result);
		if (obj.getIntValue("ret") != 0) {
			HawkLog.logPrintln("queryCoupon failed, playerId: {}, openid: {}, errCode: {}, msg: {}", player.getId(), player.getOpenId(), 
					obj.getIntValue("ret"), obj.getString("msg"));
			return Collections.emptyList();
		}
		
		JSONObject dataObj = obj.getJSONObject("data");
		if (dataObj == null) {
			HawkLog.logPrintln("queryCoupon failed, playerId: {}, openid: {}, data is null", player.getId(), player.getOpenId());
			return Collections.emptyList();
		}
		
		List<HPCouponItem> couponList = new ArrayList<HPCouponItem>();
		JSONArray myListArray = dataObj.getJSONArray("my_list");
		if (myListArray != null && !myListArray.isEmpty()) {
			for (int i = 0; i < myListArray.size(); i++) {
				JSONObject listObj = myListArray.getJSONObject(i);
				couponList.add(buildCouponItem(listObj));
			}
		}
		
		JSONArray downloadArray = dataObj.getJSONArray("download_list");
		if (downloadArray != null && !downloadArray.isEmpty()) {
			for (int i = 0; i < downloadArray.size(); i++) {
				JSONObject listObj = downloadArray.getJSONObject(i);
				couponList.add(buildCouponItem(listObj));
			}
		}
		
		return couponList;
	}
	
	/**
	 * 查询已领取的微信券
	 * 
	 * @param player
	 * @return
	 */
	private static List<HPCouponItem> queryUserCoupon(Player player) {
		String result = SDKManager.getInstance().wxQuerySelfGameCoupon(player.getChannel(), HawkTime.getSeconds(), player.getOpenId(), player.getPfTokenJson());
		if (result == null) {
			HawkLog.logPrintln("queryUserCoupon failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			return Collections.emptyList();
		}
		
		JSONObject obj = JSONObject.parseObject(result);
		if (obj.getIntValue("ret") != 0) {
			HawkLog.logPrintln("queryUserCoupon failed, playerId: {}, openid: {}, errCode: {}, msg: {}", player.getId(), player.getOpenId(), 
					obj.getIntValue("ret"), obj.getString("msg"));
			return Collections.emptyList();
		}
		
		JSONObject dataObj = obj.getJSONObject("data");
		if (dataObj == null) {
			HawkLog.logPrintln("queryUserCoupon failed, playerId: {}, openid: {}, data is null", player.getId(), player.getOpenId());
			return Collections.emptyList();
		}
		
		List<HPCouponItem> couponList = new ArrayList<HPCouponItem>();
		JSONArray myListArray = dataObj.getJSONArray("list");
		if (myListArray != null && !myListArray.isEmpty()) {
			for (int i = 0; i < myListArray.size(); i++) {
				JSONObject listObj = myListArray.getJSONObject(i);
				couponList.add(buildCouponItem(listObj));
			}
		}
		
		return couponList;
	}

	/**
	 * 领取微券
	 * 
	 * @param player
	 */
	public static String sendCoupon(Player player, String couponId, String actId) {
		if (ControlProperty.getInstance().getCouponSwitch() == 0) {
			return "";
		}
		
		String result = SDKManager.getInstance().wxSendGameCoupon(player.getChannel(), HawkTime.getSeconds(), player.getOpenId(), player.getPfTokenJson(), actId, couponId);
		if (result == null) {
			HawkLog.logPrintln("queryUserCoupon failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			return "error";
		}
		
		JSONObject obj = JSONObject.parseObject(result);
		if (obj.getIntValue("ret") != 0) {
			HawkLog.logPrintln("queryUserCoupon failed, playerId: {}, openid: {}, errCode: {}, msg: {}", player.getId(), player.getOpenId(), 
					obj.getIntValue("ret"), obj.getString("msg"));
			return obj.getString("msg");
		}
		
		return "";
	}
	
	/**
	 * 构建builder
	 * 
	 * @param json
	 * @return
	 */
	private static HPCouponItem buildCouponItem(JSONObject json) {
		HPCouponItem.Builder builder = HPCouponItem.newBuilder();
		if (json.containsKey("title")) {
			builder.setTitle(json.getString("title"));
		}
		if (json.containsKey("coupon_value")) {
			builder.setCouponValue(json.getIntValue("coupon_value"));
		}
		if (json.containsKey("coupon_threshold")) {
			builder.setCouponThreshold(json.getIntValue("coupon_threshold"));
		}
		if (json.containsKey("expiry_time_type")) {
			builder.setExpiryTimeType(json.getString("expiry_time_type"));
		}
		if (json.containsKey("float_time_value")) {
			builder.setFloatTimeValue(json.getIntValue("float_time_value"));
		}
		if (json.containsKey("float_time_unit")) {
			builder.setFloatTimeUnit(json.getString("float_time_unit"));
		}
		if (json.containsKey("fix_expiry_time_begin")) {
			String beginTime = json.getString("fix_expiry_time_begin");
			builder.setFixExpiryTimeBegin((int) (HawkTime.parseTime(beginTime)/1000));
		}
		if (json.containsKey("fix_expiry_time_end")) {
			String endTime = json.getString("fix_expiry_time_end");
			builder.setFixExpiryTimeEnd((int) (HawkTime.parseTime(endTime)/1000));
		}
		if (json.containsKey("coupon_id")) {
			builder.setCouponId(json.getString("coupon_id"));
		}
		if (json.containsKey("act_id")) {
			builder.setActId(json.getString("act_id"));
		}
		if (json.containsKey("title")) {
			builder.setBatchId(json.getString("batch_id"));
		}
		if (json.containsKey("status")) {
			builder.setStatus(json.getIntValue("status"));
		}
		if (json.containsKey("order")) {
			builder.setOrder(json.getIntValue("order"));
		}
		if (json.containsKey("hide")) {
			builder.setHide(json.getBooleanValue("hide"));
		}
		if (json.containsKey("pool")) {
			builder.setPool(json.getIntValue("pool"));
		}
		if (json.containsKey("show_range")) {
			builder.setShowRange(json.getIntValue("show_range"));
		}
		if (json.containsKey("begintime")) {
			builder.setBegintime(json.getIntValue("begintime"));
		}
		if (json.containsKey("endtime")) {
			builder.setEndtime(json.getIntValue("endtime"));
		}
		if (json.containsKey("present_time")) {
			builder.setPresentTime(json.getIntValue("present_time"));
		}
		if (json.containsKey("appinfo")) {
			JSONObject obj = json.getJSONObject("appinfo");
			HPCouponAppInfo.Builder appInfoBuilder = HPCouponAppInfo.newBuilder();
			appInfoBuilder.setAppid(obj.getString("appid"));
			appInfoBuilder.setAppuin(String.valueOf(obj.getLong("appuin")));
			appInfoBuilder.setAppname(obj.getString("appname"));
			appInfoBuilder.setAppicon(obj.getString("appicon"));
			builder.setAppInfo(appInfoBuilder);
		}
		
		if (json.containsKey("condition")) {
			JSONArray array = json.getJSONArray("condition");
			for (int i = 0; i < array.size(); i++) {
				HPCouponCondition.Builder condBuilder = HPCouponCondition.newBuilder();
				condBuilder.setType(array.getJSONObject(i).getIntValue("type"));
				builder.addCondition(condBuilder);
			}
		}
		
		return builder.build();
	}
}
