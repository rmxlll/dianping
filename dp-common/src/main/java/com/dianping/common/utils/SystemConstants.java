package com.dianping.common.utils;

public class SystemConstants {
//    public static final String IMAGE_UPLOAD_DIR = "D:\\heima_dianping\\nginx-1.18.0\\html\\hmdp\\imgs\\";
    public static final String IMAGE_UPLOAD_DIR = System.getProperty("user.dir")+
        "\\nginx-1.18.0\\html\\hmdp\\imgs\\";
    public static final String USER_NICK_NAME_PREFIX = "user_";
    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int MAX_PAGE_SIZE = 10;
}
