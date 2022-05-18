package com.weichai.awsdemo;

import com.weichai.awsdemo.service.DemoService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import sun.security.krb5.Credentials;

/**
 * @author wangshihai
 * @date 3/28/22
 */
@RestController
@RequestMapping(value="/aws/")
public class TestController {
    @Autowired DemoService demoService;

    @PostMapping("/page")
    public Credentials listTmStation() {
        String[] args = {};
        final String USAGE = "\n" +
                "Usage:\n" +
                "    <bucketName> <keyName> \n\n" +
                "Where:\n" +
                "    bucketName - the name of the Amazon S3 bucket. \n\n" +
                "    keyName - a key name that represents a text file. \n" ;

        if (args.length != 2) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String bucketName = args[0];
        String keyName = args[1];
        Region region = Region.US_EAST_1;
        S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .build();

        demoService.signBucket(presigner, bucketName, keyName);
        presigner.close();
        return null;
    }
}
