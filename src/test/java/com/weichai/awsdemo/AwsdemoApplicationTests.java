package com.weichai.awsdemo;

import com.weichai.awsdemo.service.DemoService;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import junit.framework.Assert;
import org.assertj.core.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsGetSessionTokenCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest;
import software.amazon.awssdk.services.sts.model.GetSessionTokenResponse;
import software.amazon.awssdk.services.sts.model.StsException;

@SpringBootTest
class AwsdemoApplicationTests {

    @Autowired
    DemoService demoService;

    @Test
    void testSignBucket() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials
                .create(DemoConstants.WSH_ACCES_SKEY, DemoConstants.WSH_SECRET_KEY);
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
        S3Presigner presigner = S3Presigner.builder()
                .region(DemoConstants.REGION)
                .credentialsProvider(staticCredentialsProvider)
//                .endpointOverride(URI.create("https://s3.cn-north-1.amazonaws.com.cn"))
                .build();
        demoService.signBucket(presigner, DemoConstants.BUCKET_NAME, "presignerTest3");
        presigner.close();
    }

    @Test
    void testGetTemporaryCredentials(){
        StsClient stsClient = demoService
                .getTempCredentials(DemoConstants.WSH_ACCES_SKEY, DemoConstants.WSH_SECRET_KEY);

        GetSessionTokenRequest getSessionTokenRequest = GetSessionTokenRequest.builder().durationSeconds(7200).build();
        GetSessionTokenResponse sessionToken = stsClient.getSessionToken(getSessionTokenRequest);
        Credentials credentials = sessionToken.credentials();
        Instant expiration = credentials.expiration();
        System.out.println(credentials.expiration());
        LocalDateTime localDateTime1 = LocalDateTime.ofInstant(expiration, ZoneId.of("Asia/Shanghai"));
        String formatted = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(localDateTime1);
        System.out.println(formatted);
    }

    @Test
    void testSignBucketUseTemp() {
        StsClient tempCredentials = demoService
                .getTempCredentials(DemoConstants.WSH_ACCES_SKEY, DemoConstants.WSH_SECRET_KEY);
        Assert.assertNotNull(tempCredentials);

        System.out.println(tempCredentials.getSessionToken());
        GetSessionTokenResponse sessionToken = tempCredentials.getSessionToken();
        Credentials credentials = sessionToken.credentials();

        AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials
                .create(credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken());
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsSessionCredentials);

        S3Presigner presigner = S3Presigner.builder()
                .region(DemoConstants.REGION)
                .credentialsProvider(staticCredentialsProvider)
//                .endpointOverride(URI.create("https://s3.cn-north-1.amazonaws.com.cn"))
                .build();
        demoService.signBucket(presigner, DemoConstants.BUCKET_NAME, "presignerTest3");
        presigner.close();
    }

    /**
     * 测试获取临时凭证
     */
    @Test
    void testGetPresignedUrl() {
        StsClient tempCredentials = demoService
                .getTempCredentials(DemoConstants.WSH_ACCES_SKEY, DemoConstants.WSH_SECRET_KEY);
        Assert.assertNotNull(tempCredentials);

        System.out.println(tempCredentials.getSessionToken());
        GetSessionTokenResponse sessionToken = tempCredentials.getSessionToken();
        Credentials credentials = sessionToken.credentials();

        AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials
                .create(credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken());
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsSessionCredentials);

//        S3Configuration.builder().profileFile()
        S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(staticCredentialsProvider)
//                .serviceConfiguration()
                .region(DemoConstants.REGION)
                .build();
        demoService
                .getPresignedUrl(presigner, DemoConstants.BUCKET_NAME, "presignerTest3");

        presigner.close();
    }




    @Test
    public void assumeRole() {
        StsClient tempCredentials = demoService
                .getTempCredentials(DemoConstants.WSH_ACCES_SKEY, DemoConstants.WSH_SECRET_KEY);
        AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder().roleArn(DemoConstants.ROLE_ARN)
                .durationSeconds(900)
                .roleSessionName(DemoConstants.ROLE_SESSION_NAME).build();
        AssumeRoleResponse assumeRoleResponse = tempCredentials.assumeRole(assumeRoleRequest);
        Credentials credentials = assumeRoleResponse.credentials();
        System.out.println("credentials==========" + credentials.toString());
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withLocale(Locale.CHINA)
                        .withZone(ZoneId.systemDefault());

        formatter.format(credentials.expiration());
        System.out.println("Test 2 passed");
    }

    @Test
    public void testUrl() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials
                .create(DemoConstants.WSH_ACCES_SKEY, DemoConstants.WSH_SECRET_KEY);
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
        //获取下载地址
        S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(DemoConstants.REGION)
                .build();
        demoService
                .getPresignedUrl(presigner, DemoConstants.BUCKET_NAME, "presignerTest");

        presigner.close();
    }

    @Test
    public void testDownloadWithTemp(){
        String bucketName = DemoConstants.BUCKET_NAME;
        String keyName = DemoConstants.KEY_NAME;
        String path = "/Users/wangshihai/123.txt";

        Region region = DemoConstants.REGION;

        StsClient tempCredentials = demoService
                .getTempCredentials(DemoConstants.WSH_ACCES_SKEY, DemoConstants.WSH_SECRET_KEY);

        System.out.println(tempCredentials.getSessionToken());
        GetSessionTokenResponse sessionToken = tempCredentials.getSessionToken();
        Credentials credentials = sessionToken.credentials();

        AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials
                .create(credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken());
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsSessionCredentials);

        S3Client s3 = S3Client.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(region)
                .build();

        demoService.getObjectBytes(s3,bucketName,keyName, path);
        s3.close();
    }

}
