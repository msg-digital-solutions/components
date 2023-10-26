package org.talend.components.simplefileio.runtime.s3;

import com.talend.shaded.com.amazonaws.ClientConfiguration;
import com.talend.shaded.com.amazonaws.auth.AWSCredentialsProvider;
import com.talend.shaded.com.amazonaws.services.s3.AmazonS3;
import com.talend.shaded.com.amazonaws.services.s3.AmazonS3Client;
import com.talend.shaded.org.apache.hadoop.fs.s3a.DefaultS3ClientFactory;

public class S3ClientFactory extends DefaultS3ClientFactory {

        static String USER_AGENT = "fs.s3a.user.agent";

        public AmazonS3 newAmazonS3Client(AWSCredentialsProvider credentials, ClientConfiguration awsConf) {
            String userAgent = this.getConf().getTrimmed(USER_AGENT, "");
            if (!userAgent.isEmpty()) {
                awsConf.setUserAgent(userAgent);
            }
            return new AmazonS3Client(credentials, awsConf);
        }
}
