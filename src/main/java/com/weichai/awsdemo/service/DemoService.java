package com.weichai.awsdemo.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sts.StsClient;
import sun.security.krb5.Credentials;

/**
 * @author wangshihai
 * @date 3/28/22
 */
public interface DemoService {

   StsClient getTempCredentials(String accessKey,String secretKey);

   void signBucket(S3Presigner presigner, String bucketName, String keyName);

   void getPresignedUrl(S3Presigner presigner, String bucketName, String keyName);

   void getObjectBytes (S3Client s3, String bucketName, String keyName, String path );
}
