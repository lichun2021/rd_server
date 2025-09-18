package com.hawk.game.module;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.google.protobuf.ByteString;
import com.hawk.game.msg.TsssdkInvokeMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SecuritySdk.HPAntiAddUser;
import com.hawk.game.protocol.SecuritySdk.HPAntiDelUser;
import com.hawk.game.protocol.SecuritySdk.HPAntiRecvDataInfo;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.tsssdk.invoker.TsssdkInvoker;
import com.hawk.game.util.GameUtil;
import com.hawk.tsssdk.manager.TssSdkManager;
import com.hawk.tsssdk.util.AntiAddUserInfo;
import com.hawk.tsssdk.util.AntiDelUserInfo;
import com.hawk.tsssdk.util.AntiRecvDataInfo;

import tsssdk.jni.TssAccountPlatId;
import tsssdk.jni.TssAccountType;

/**
 * 腾讯安全SDK
 * 
 * @author Nannan.Gao
 * @date 2017-4-10 10:34:06
 */
public class PlayerTssSdkModule extends PlayerModule {
	public PlayerTssSdkModule(Player player) {
		super(player);
	}
	
	/**
	 * 安全SDK用户登入
	 *
	 * @param protocol
	 * @return
	 */
//	@ProtocolHandler(code = HP.code.TSS_SDK_ADD_USER_C_VALUE)
//	private boolean onAntiAddUser(HawkProtocol protocol) {
//		HPAntiAddUser request = protocol.parseProtocol(HPAntiAddUser.getDefaultInstance());
//		
//		long clientIp = 0L;
//		String ip = player.getClientIp();
//		if (!HawkOSOperator.isEmptyString(ip)) {
//			String ips[] = ip.split("\\.");
//			int index = 0;
//			for (String _ip : ips) {
//				long ipValue = Long.parseLong(_ip);
//				clientIp |= ipValue << (24 - index * 8);
//				index ++;
//			}
//		}
//		
//		byte[] extData = player.getId().getBytes();
//		
//		AntiAddUserInfo userInfo = new AntiAddUserInfo(request.getOpenid(), request.getPlatid(), request.getClientVer(), clientIp, 
//				player.getName(), GameUtil.getWorldId(), 0, extData);
//		TssSdkManager.getInstance().antiAddMessage(userInfo);
//		return true;
//	}
	
	@ProtocolHandler(code = HP.code.TSS_SDK_ADD_USER_C_VALUE)
	private boolean onAntiAddUser(HawkProtocol protocol) {
		HPAntiAddUser request = protocol.parseProtocol(HPAntiAddUser.getDefaultInstance());
		
		long clientIp = 0L;
		String ip = player.getClientIp();
		if (!HawkOSOperator.isEmptyString(ip)) {
			String ips[] = ip.split("\\.");
			int index = 0;
			for (String _ip : ips) {
				long ipValue = Long.parseLong(_ip);
				clientIp |= ipValue << (24 - index * 8);
				index ++;
			}
		}
		
		TssAccountType accountType = "wx".equalsIgnoreCase(player.getChannel()) ? TssAccountType.TSSACCOUNT_TYPE_WECHAT : TssAccountType.TSSACCOUNT_TYPE_QQ;
		TssAccountPlatId platId = GameUtil.isAndroidAccount(player) ? TssAccountPlatId.TSSPLAT_ID_ANDROID : TssAccountPlatId.TSSPLAT_ID_IOS;
		AntiAddUserInfo userInfo = new AntiAddUserInfo(accountType, request.getOpenid(), player.getId(), platId, clientIp, player.getName(), GameUtil.getServerId());
		TssSdkManager.getInstance().antiAddMessage(userInfo);
		return true;
	}
	
	/**
	 * 安全数据处理
	 *
	 * @param protocol
	 * @return
	 */
//	@ProtocolHandler(code = HP.code.TSS_SDK_RECV_DATA_C_VALUE)
//	private boolean onRecvAntiData(HawkProtocol protocol) {
//		HPAntiRecvDataInfo request = protocol.parseProtocol(HPAntiRecvDataInfo.getDefaultInstance());
//		
//		ByteString antiData = request.getAntiData();
//		byte[] extData = player.getId().getBytes();;
//		
//		AntiRecvDataInfo dataInfo = new AntiRecvDataInfo(request.getOpenid(), request.getPlatid(), GameUtil.getWorldId(), 0, antiData.toByteArray(), extData);
//        TssSdkManager.getInstance().antiAddMessage(dataInfo);
//		return true;
//	}
	
	@ProtocolHandler(code = HP.code.TSS_SDK_RECV_DATA_C_VALUE)
	private boolean onRecvAntiData(HawkProtocol protocol) {
		HPAntiRecvDataInfo request = protocol.parseProtocol(HPAntiRecvDataInfo.getDefaultInstance());
		
		ByteString antiData = request.getAntiData();
		byte[] extData = player.getId().getBytes();;
		
		TssAccountType accountType = "wx".equalsIgnoreCase(player.getChannel()) ? TssAccountType.TSSACCOUNT_TYPE_WECHAT : TssAccountType.TSSACCOUNT_TYPE_QQ;
		TssAccountPlatId platId = GameUtil.isAndroidAccount(player) ? TssAccountPlatId.TSSPLAT_ID_ANDROID : TssAccountPlatId.TSSPLAT_ID_IOS;
		
		AntiRecvDataInfo dataInfo = new AntiRecvDataInfo(accountType, request.getOpenid(), platId, GameUtil.getServerId(), antiData.toByteArray(), extData);
        TssSdkManager.getInstance().antiAddMessage(dataInfo);
		return true;
	}
	
	/**
	 * 安全SDK用户登出
	 *
	 * @param protocol
	 * @return
	 */
//	@ProtocolHandler(code = HP.code.TSS_SDK_DEL_USER_C_VALUE)
//	private boolean onDelUser(HawkProtocol protocol) {
//		HPAntiDelUser request = protocol.parseProtocol(HPAntiDelUser.getDefaultInstance());
//		
//		byte[] extData = null;
//		if(request.hasUserExtData()) {
//			ByteString userExtData = request.getUserExtData();
//			extData = userExtData.toByteArray();
//		}
//		
//		AntiDelUserInfo userInfo = new AntiDelUserInfo(request.getOpenid(), request.getPlatid(), GameUtil.getWorldId(), 0, extData);
//		TssSdkManager.getInstance().antiAddMessage(userInfo);
//		return true;
//	}
	
	@ProtocolHandler(code = HP.code.TSS_SDK_DEL_USER_C_VALUE)
	private boolean onDelUser(HawkProtocol protocol) {
		HPAntiDelUser request = protocol.parseProtocol(HPAntiDelUser.getDefaultInstance());
		
		byte[] extData = null;
		if(request.hasUserExtData()) {
			ByteString userExtData = request.getUserExtData();
			extData = userExtData.toByteArray();
		}
		
		TssAccountType accountType = "wx".equalsIgnoreCase(player.getChannel()) ? TssAccountType.TSSACCOUNT_TYPE_WECHAT : TssAccountType.TSSACCOUNT_TYPE_QQ;
		TssAccountPlatId platId = GameUtil.isAndroidAccount(player) ? TssAccountPlatId.TSSPLAT_ID_ANDROID : TssAccountPlatId.TSSPLAT_ID_IOS;
		AntiDelUserInfo userInfo = new AntiDelUserInfo(accountType, request.getOpenid(), platId, GameUtil.getServerId(), extData);
		TssSdkManager.getInstance().antiAddMessage(userInfo);
		return true;
	}
	
	@MessageHandler
	private void onTsssdkInvoke(TsssdkInvokeMsg msg) {
		TsssdkInvoker invoker = GameTssService.getInstance().getInvoker(msg.getCategory());
		if (invoker == null) {
			HawkLog.errPrintln("tsssdk invoke failed, scene: {}", msg.getCategory());
			return;
		}
		
		int result = msg.getResultCode(), protocol = msg.getProtocol();
		String content = msg.getMsgContent(), callback = msg.getCallbackData();
		invoker.invoke(player, result, content, protocol, callback);
	}
	
}
