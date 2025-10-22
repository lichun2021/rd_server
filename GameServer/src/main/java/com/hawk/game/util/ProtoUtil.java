package com.hawk.game.util;

import java.util.List;
import java.util.Arrays;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.protocol.HawkProtocolCodec;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.util.HawkZlib;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSON;
import com.hawk.game.GsConfig;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.HP.code;
import com.hawk.game.protocol.HP.code2;

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

	public static String bytesToHex(byte[] data, int lengthLimit) {
		if (data == null || data.length == 0) {
			return "";
		}
		int limit = Math.min(data.length, Math.max(0, lengthLimit));
		StringBuilder sb = new StringBuilder(limit * 2 + 16);
		for (int i = 0; i < limit; i++) {
			int v = data[i] & 0xFF;
			if (v < 16) sb.append('0');
			sb.append(Integer.toHexString(v));
			if (i + 1 < limit) sb.append(' ');
		}
		if (limit < data.length) {
			sb.append(" ...(").append(data.length - limit).append(" more bytes)");
		}
		return sb.toString();
	}

	public static String getProtocolName(int type) {
		try {
			HP.code c = null;
			HP.code2 c2 = null;
			try { c = HP.code.valueOf(type); } catch (Exception ignore) {}
			try { c2 = HP.code2.valueOf(type); } catch (Exception ignore) {}
			if (c != null) return c.name();
			if (c2 != null) return c2.name();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return String.valueOf(type);
	}

	// 协议反序列化注册表
	public interface ProtocolDecoder {
		String decode(byte[] data) throws Exception;
	}

	private static final java.util.concurrent.ConcurrentHashMap<Integer, ProtocolDecoder> DECODER_MAP = new java.util.concurrent.ConcurrentHashMap<Integer, ProtocolDecoder>();

    // 通过配置动态注册，无需编译
    private static final String PROTOCOL_LOG_CFG_PATH = "cfg/protocolLog.cfg";
    private static volatile long lastCfgMtime = -1L;

    private static void registerTextDecoderReflect(final int type, final String className) {
        try {
            final Class<?> clazz = Class.forName(className);
            final java.lang.reflect.Method newBuilder = clazz.getMethod("newBuilder");
            DECODER_MAP.put(type, new ProtocolDecoder() {
                @Override
                public String decode(byte[] data) throws Exception {
                    Object builder = newBuilder.invoke(null);
                    // builder.mergeFrom(byte[])
                    java.lang.reflect.Method mergeFrom = builder.getClass().getMethod("mergeFrom", byte[].class);
                    mergeFrom.invoke(builder, new Object[] { data });
                    // buildPartial()
                    java.lang.reflect.Method buildPartial = builder.getClass().getMethod("buildPartial");
                    Object msg = buildPartial.invoke(builder);
                    return TextFormat.printToString((Message) msg);
                }
            });
        } catch (Throwable e) {
            HawkException.catchException(e);
        }
    }

    private static void loadDecodersFromConfigIfNeeded() {
        try {
            java.io.File file = new java.io.File(PROTOCOL_LOG_CFG_PATH);
            if (!file.exists()) {
                return;
            }
            long mtime = file.lastModified();
            if (mtime == lastCfgMtime) {
                return;
            }
            lastCfgMtime = mtime;
            java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            for (String line : lines) {
                String trim = line == null ? null : line.trim();
                if (trim == null || trim.isEmpty()) continue;
                if (trim.startsWith("#")) continue;
                int idx = trim.indexOf('=');
                if (idx <= 0) continue;
                String codeStr = trim.substring(0, idx).trim();
                String className = trim.substring(idx + 1).trim();
                try {
                    int type = Integer.parseInt(codeStr);
                    registerTextDecoderReflect(type, className);
                } catch (NumberFormatException ignore) {}
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

	public static String tryDecodeProtocol(int type, byte[] data) {
        try {
            loadDecodersFromConfigIfNeeded();
			ProtocolDecoder decoder = DECODER_MAP.get(type);
			if (decoder == null) {
				return null;
			}
			return decoder.decode(data);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

    /**
     * 粗略判断是否为 zlib 包头：
     * - 第 1 字节通常为 0x78（deflate + 窗口大小）
     * - (CMF<<8 | FLG) % 31 == 0 为合法 zlib 头校验
     */
    private static boolean hasZlibHeader(byte[] data) {
        if (data == null || data.length < 2) {
            return false;
        }
        int cmf = data[0] & 0xFF;
        int flg = data[1] & 0xFF;
        if (cmf != 0x78) {
            return false;
        }
        int header = (cmf << 8) | flg;
        return (header % 31) == 0;
    }

    /**
     * 打印前按 reserve 判断是否需要 zlib 解压；未压缩则直接返回原始数据。
     * 约定：reserve 低位等于 1 表示 zlib 压缩（见 ProtoUtil.RESERVE_ZLIB_COMPRESS）。
     */
    public static byte[] preferInflated(int reserve, byte[] data) {
        if (data == null || data.length == 0) {
            return data;
        }
        int lowMask = reserve & 0xFF;
        if (lowMask != RESERVE_ZLIB_COMPRESS) {
            return data;
        }
        // 进一步用 zlib 头判断，避免对非压缩数据误解压导致错误日志
        if (!hasZlibHeader(data)) {
            return data;
        }
        try {
            byte[] inflated = HawkZlib.zlibInflate(data);
            if (inflated != null && inflated.length > 0) {
                return inflated;
            }
        } catch (Throwable ignore) {}
        return data;
    }
}
