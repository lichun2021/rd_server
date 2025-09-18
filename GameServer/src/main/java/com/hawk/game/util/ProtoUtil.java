package com.hawk.game.util;

import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.protocol.HawkProtocolCodec;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSON;
import com.hawk.game.GsConfig;

public class ProtoUtil {
	/**
	 * 协议压缩标记
	 */
	private static int RESERVE_ZLIB_COMPRESS = 1;
	/**
	 * 协议加密秘钥
	 */
	private static int RESERVE_XOR_MIN_VALUE = 9;
	private static int RESERVE_XOR_MAX_VALUE = 99;

	/**
	 * 协议压缩
	 *
	 * @return
	 */
	public static HawkProtocol compressProtocol(HawkProtocol protocol) {
		try {
			return HawkProtocolCodec.zlibCompress(protocol, ProtoUtil.RESERVE_ZLIB_COMPRESS);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 协议解密
	 *
	 * @return
	 */
	public static HawkProtocol decryptionProtocol(HawkSession session, HawkProtocol protocol) {
		List<Integer> xorTokens = session.getUserObject("xor");

		int lowMask = 0x000000FF & protocol.getReserve();
		int highMask = (0xFFFF0000 & protocol.getReserve()) >> 16;
		if (protocol.getReserve() != 0) {
			// 协议秘钥范围判断
			if (lowMask < ProtoUtil.RESERVE_XOR_MIN_VALUE || lowMask > ProtoUtil.RESERVE_XOR_MAX_VALUE) {
				session.close();
				return null;
			}

			// 协议序号判断
			if (GsConfig.getInstance().isProtocolOrder() && highMask <= session.getProtocolOrder()) {
				session.close();
				return null;
			}

			session.setProtocolOrder(highMask);
		} else {
			// 协议安全要求
			if (GsConfig.getInstance().isProtocolSecure()) {
				throw new RuntimeException("protocol secure reserve check failed");
			}
		}

		// 秘钥令牌校验
		if (xorTokens != null && xorTokens.size() > 0 && !xorTokens.contains(lowMask)) {
			HawkLog.errPrintln("session closed by xor token check, clientToken: {}, sessionToken: {}", 
					lowMask, JSON.toJSONString(xorTokens));

			session.close();
			return null;
		}

		if (lowMask >= ProtoUtil.RESERVE_XOR_MIN_VALUE && lowMask <= ProtoUtil.RESERVE_XOR_MAX_VALUE) {
			if (protocol.getSize() > 0) {
				byte[] bytes = protocol.getData();
				for (int i = 0; i < protocol.getSize(); i++) {
					bytes[i] ^= lowMask;
				}
				protocol.setCrc(HawkOSOperator.calcCrc(bytes));
			}
		} else {
			// 协议安全要求
			if (GsConfig.getInstance().isProtocolSecure()) {
				throw new RuntimeException("protocol secure lowMask check failed");
			}
		}
		
		return protocol;
	}
}
