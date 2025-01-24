package com.jasonpyau.chatapp.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.Getter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class AmazonS3Service {

    private S3Client s3Client;

    @Getter
    private String bucket;

    @Getter
    private String publicBucketUrl;
    
    public AmazonS3Service(@Value("${com.jasonpyau.chatapp.aws.access-key-id}") String accessKeyId,
                            @Value("${com.jasonpyau.chatapp.aws.secret-access-key}") String secretAccessKey, 
                            @Value("${com.jasonpyau.chatapp.aws.endpoint}") String endpoint,
                            @Value("${com.jasonpyau.chatapp.aws.bucket}") String bucket,
                            @Value("${com.jasonpyau.chatapp.aws.public-bucket-url}") String publicBucketUrl) throws URISyntaxException {
        this.bucket = bucket;
        if (StringUtils.hasText(publicBucketUrl) && publicBucketUrl.endsWith("/")) {
            publicBucketUrl = publicBucketUrl.substring(0, publicBucketUrl.length()-1);
        }
        this.publicBucketUrl = publicBucketUrl;
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                                .endpointOverride(new URI(endpoint))
                                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                                .region(Region.of("auto"))
                                .forcePathStyle(true)
                                .build();
    }

    // This could be sped up using a MultipartUpload.
    public void putObject(byte[] bytes, String key, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                                                    .bucket(bucket)
                                                    .contentType(contentType)
                                                    .key(key)
                                                    .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    public byte[] getObject(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                                                    .bucket(bucket)
                                                    .key(key)
                                                    .build();
        ResponseInputStream<GetObjectResponse> objectStream;
        try {
            objectStream = s3Client.getObject(request);
        } catch (NoSuchKeyException e) {
            return null;
        }
        byte[] bytes = null;
        try {
            bytes = objectStream.readAllBytes();
            objectStream.close();
        } catch (IOException e1) {
            try {
                objectStream.close();
            } catch (IOException e2) {
                System.out.println(e2.getMessage());
            }
        }
        return bytes;
    }

}
