package com.vanish.common.util;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务 ID 生成器，格式：{前缀}_{毫秒时间戳}_{6位随机串}
 * 与前端生成的 b_xxx / c_xxx 格式保持一致
 */
public class IdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong SEQ = new AtomicLong();

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private IdGenerator() {
    }

    /**
     * 生成业务 ID
     *
     * @param prefix 前缀：u=用户、b=账单、c=分类
     */
    public static String next(String prefix) {
        // 序列参与拼串，避免同毫秒内随机碰撞的极端情况
        long seq = SEQ.incrementAndGet();
        String random = String.format("%04x%02x", RANDOM.nextInt(0x10000), seq & 0xff);
        return prefix + "_" + System.currentTimeMillis() + "_" + random;
    }
}
