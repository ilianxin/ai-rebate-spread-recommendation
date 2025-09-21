package com.airebate.model;

/**
 * 货币枚举类
 * 支持主要的国际货币
 */
public enum Currency {
    USD("美元", "US Dollar"),
    EUR("欧元", "Euro"),
    JPY("日元", "Japanese Yen"),
    GBP("英镑", "British Pound"),
    CNY("人民币", "Chinese Yuan"),
    CAD("加拿大元", "Canadian Dollar"),
    AUD("澳元", "Australian Dollar"),
    CHF("瑞士法郎", "Swiss Franc"),
    HKD("港币", "Hong Kong Dollar"),
    SGD("新加坡元", "Singapore Dollar");

    private final String chineseName;
    private final String englishName;

    Currency(String chineseName, String englishName) {
        this.chineseName = chineseName;
        this.englishName = englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getCode() {
        return this.name();
    }
}
