package com.hawk.game.lianmengstarwars.player;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.xid.HawkXID;

public class SWVideoPlayer extends ISWPlayer {

	public SWVideoPlayer(HawkXID xid) {
		super(xid);
		// TODO Auto-generated constructor stub
	}

	/** 场景构建信息 */
	public boolean sendHeadVideoProtocol(HawkProtocol protocol) {
		// System.out.println("sendHeadVideoProtocol "+ HP.code.valueOf(protocol.getType()));
		return sendVideoProtocol(protocol, 1);
	}

	/** 记录场景内广播信息 */
	public boolean sendBroadVideoProtocol(HawkProtocol protocol) {
		// System.out.println("sendBroadVideoProtocol "+ HP.code.valueOf(protocol.getType()));
		return sendVideoProtocol(protocol, 2);
	}

	private boolean sendVideoProtocol(HawkProtocol protocol, int arg) {
		// 录像观看需求取消. 先注掉. 注意. 代码是新测可用的.
		// try {
		//
		// PBSWCmd.Builder cmd = PBSWCmd.newBuilder();
		// cmd.setTimestemp(HawkTime.getMillisecond());
		// cmd.setCmd(protocol.getType());
		// if (protocol.getData() != null) {
		// cmd.setCmdBytes(ByteString.copyFrom(protocol.getData()));
		// }
		//
		// if (arg == 1) {
		// getParent().getVideoPackage().addHead(cmd);
		// } else if (arg == 2) {
		// getParent().getVideoPackage().addCmdList(cmd);
		//
		// }
		// } catch (Exception e) {
		// HawkException.catchException(e);
		// }
		return true;
	}

	@Override
	public String getName() {
		return "录像机";
	}

	@Override
	public String getId() {
		return "sdfsdf";
	}

	@Override
	public int getIcon() {
		return 0;
	}

	@Override
	public String getPfIcon() {
		return "";
	}

	@Override
	public String getGuildId() {
		return "";
	}

	@Override
	public String getGuildTag() {
		return "";
	}

	@Override
	public int getGuildFlag() {
		return 0;
	}

	@Override
	public String getGuildName() {
		return "";
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeWorldPoint() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public double getMarchSpeedUp() {
		// TODO Auto-generated method stub
		return 0;
	}

}
