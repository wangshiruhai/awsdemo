package com.weichai.awsdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.time.Duration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * @author wangshihai
 * @date 3/29/22
 */
public class GetObjectPresignedUrl {
    public static void main(String[] args) {
        String bucketName = DemoConstants.BUCKET_NAME;
        String keyName = "presignerTest3";
        Region region = DemoConstants.REGION;
        S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .build();

        getPresignedUrl(presigner, bucketName, keyName);
        presigner.close();

    }
    // snippet-start:[presigned.java2.getobjectpresigned.main]
    public static void getPresignedUrl(S3Presigner presigner, String bucketName, String keyName ) {

        try {
            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(keyName)
                            .build();

            GetObjectPresignRequest getObjectPresignRequest =  GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Generate the presigned request
            PresignedGetObjectRequest presignedGetObjectRequest =
                    presigner.presignGetObject(getObjectPresignRequest);

            // Log the presigned URL
            System.out.println("Presigned URL: " + presignedGetObjectRequest.url());

            HttpURLConnection connection = (HttpURLConnection) presignedGetObjectRequest.url().openConnection();
            presignedGetObjectRequest.httpRequest().headers().forEach((header, values) -> {
                values.forEach(value -> {
                    connection.addRequestProperty(header, value);
                });
            });
            connection.addRequestProperty("Host","https://s3.amazonaws.com.cn");

            // Send any request payload that the service needs (not needed when isBrowserExecutable is true)
            if (presignedGetObjectRequest.signedPayload().isPresent()) {
                connection.setDoOutput(true);
                try (InputStream signedPayload = presignedGetObjectRequest.signedPayload().get().asInputStream();
                        OutputStream httpOutputStream = connection.getOutputStream()) {
                    IoUtils.copy(signedPayload, httpOutputStream);
                }
            }

            // Download the result of executing the request
            try (InputStream content = connection.getInputStream()) {
                System.out.println("Service returned response: ");
                IoUtils.copy(content, System.out);
            }

        } catch (S3Exception e) {
            e.getStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }
}
