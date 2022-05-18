package com.weichai.awsdemo;

import software.amazon.awssdk.regions.Region;

/**
 * @author wangshihai
 * @date 3/29/22
 */
public class DemoConstants {

    public static String WSH_ACCES_SKEY = "ACCES_SKEY";
    public static String WSH_SECRET_KEY = "SECRET_KEY";

    public static final Region REGION = Region.CN_NORTH_1;
    public static final String BUCKET_NAME = "BUCKET_NAME";
    public static final String KEY_NAME = "KEY_NAME";

    public static final String ROLE_SESSION_NAME = "myRoleName";
    public static final String ROLE_ARN = "arn:aws-cn:s3:::BUCKET_NAME/*";
}
