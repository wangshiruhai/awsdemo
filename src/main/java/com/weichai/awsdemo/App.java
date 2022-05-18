package com.weichai.awsdemo;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * @author wangshihai
 * @date 3/25/22
 */
public class App {


    public static void main(String[] args) throws IOException {
        Region region = Region.CN_NORTH_1;
        //profile方式
//        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
        //静态变量方式
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(DemoConstants.WSH_ACCES_SKEY, DemoConstants.WSH_SECRET_KEY);
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
        S3Client s3 = S3Client.builder().credentialsProvider(staticCredentialsProvider).region(region).build();

        //测试创建存储桶
//        String bucket = "bucket" + System.currentTimeMillis();
//        testCreateBucket(s3,bucket,region);
        String bucketName = "bucket1648190995234";
        String key = "car_picture_".concat(UUID.randomUUID().toString().replaceAll("-", ""));

        //测试上传存储桶
//        PutObjectRequest objectKey = PutObjectRequest.builder().bucket(bucketName).key(key)
//                .build();
//        s3.putObject(objectKey,
//                RequestBody.fromString("test upload s3"));
//        Path localFilePath = Paths.get("/Users/wangshihai/Pictures/1481208274341.jpg");
//        s3.putObject(objectKey,localFilePath);

        //查看存储桶列表
//        listBucketObjects(s3,bucketName);

        //获取下载地址
        S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(region)
                .build();
//        String presignedUrl = getPresignedUrl(presigner, bucketName, "car_picture01522619d0d4445f9fa8e4ea032f4acd");
//        System.out.println("Presigned URL: " + presignedUrl);
        presigner.close();
        s3.close();
    }

   /* public static String getPresignedUrl(S3Presigner presigner, String bucketName, String keyName) {
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(keyName)
                        .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        // Generate the presigned request
        PresignedGetObjectRequest presignedGetObjectRequest =
                presigner.presignGetObject(getObjectPresignRequest);

        // Log the presigned URL
        System.out.println("Presigned URL: " + presignedGetObjectRequest.url());
        //打印请求头
        presignedGetObjectRequest.httpRequest().headers().forEach((header, values) -> {
            values.forEach(value -> {
                System.out.println(header + ":" + values);

            });
        });
        String path = "";
        try {
            path = presignedGetObjectRequest.url().toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return path;
    }*/


    public static void testCreateBucket(S3Client s3Client, String bucketName, Region region) {
        try {
            s3Client.createBucket(CreateBucketRequest
                    .builder()
                    .bucket(bucketName)
                    .createBucketConfiguration(
                            CreateBucketConfiguration.builder()
                                    .locationConstraint(region.id())
                                    .build())
                    .build());
            System.out.println("Creating bucket: " + bucketName);
            s3Client.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            System.out.println(bucketName + " is ready.");
            System.out.printf("%n");
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /*public static void cleanUp(S3Client s3Client, String bucketName, String keyName) {
        System.out.println("Cleaning up...");
        try {
            System.out.println("Deleting object: " + keyName);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(keyName).build();
            s3Client.deleteObject(deleteObjectRequest);
            System.out.println(keyName +" has been deleted.");
            System.out.println("Deleting bucket: " + bucketName);
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
            s3Client.deleteBucket(deleteBucketRequest);
            System.out.println(bucketName +" has been deleted.");
            System.out.printf("%n");
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        System.out.println("Cleanup complete");
        System.out.printf("%n");
    }*/

    public static void listBucketObjects(S3Client s3, String bucketName) {
        String logInfo = "key = {};object size = {} KBs; owner = {};";
        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsResponse res = s3.listObjects(listObjects);
            List<S3Object> objects = res.contents();

            for (ListIterator iterVals = objects.listIterator(); iterVals.hasNext(); ) {
                S3Object myValue = (S3Object) iterVals.next();
                System.out.print("\n the object name is :" + myValue.key());
                System.out.println();
            }

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
