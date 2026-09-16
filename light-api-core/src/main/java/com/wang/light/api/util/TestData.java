package com.wang.light.api.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 轻量造数工具（并发安全）。数据工厂模式中用于生成基础随机值；
 * 业务对象级的工厂请写在业务测试工程里。
 */
public final class TestData {

    private TestData() {
    }

    /** 形如 1[3-9] + 9 位数字的 11 位手机号（唯一性不保证，需要唯一请自行加时间戳后缀） */
    public static String phone() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder("1").append(r.nextInt(3, 10));
        for (int i = 0; i < 9; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    public static String digits(int length) {
        return random(length, "0123456789");
    }

    public static String letters(int length) {
        return random(length, "abcdefghijklmnopqrstuvwxyz");
    }

    public static String mixed(int length) {
        return random(length, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }

    /** 形如 qa_ab3xK9p2 的随机名称 */
    public static String name() {
        return "qa_" + mixed(8);
    }

    public static String email() {
        return name() + "@light-test.local";
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static long timestamp() {
        return System.currentTimeMillis();
    }

    /** [min, max] 闭区间随机整数 */
    public static int intBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static String random(int length, String alphabet) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
