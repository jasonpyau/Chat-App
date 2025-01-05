package com.jasonpyau.chatapp.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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

    private String bucket;

    private Cache<String, byte[]> cache;
    
    public AmazonS3Service(@Value("${com.jasonpyau.chatapp.aws.access-key-id}") String accessKeyId,
                            @Value("${com.jasonpyau.chatapp.aws.secret-access-key}") String secretAccessKey, 
                            @Value("${com.jasonpyau.chatapp.aws.endpoint}") String endpoint,
                            @Value("${com.jasonpyau.chatapp.aws.bucket}") String bucket) throws URISyntaxException {
        this.bucket = bucket;
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                                .endpointOverride(new URI(endpoint))
                                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                                .region(Region.of("auto"))
                                .forcePathStyle(true)
                                .build();
        this.cache = CacheBuilder.newBuilder()
                                .maximumSize(200)
                                .expireAfterAccess(Duration.ofMinutes(30))
                                .build();

    }

    // This could be sped up using a MultipartUpload.
    public void putObject(byte[] bytes, String key, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                                                    .bucket(bucket)
                                                    .contentType(contentType)
                                                    .key(key)
                                                    .ifNoneMatch(key)
                                                    .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    public byte[] getObject(String key) {
        byte[] bytes = cache.getIfPresent(key);
        if (bytes != null) {
            return bytes;
        }
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
        cache.put(key, bytes);
        return bytes;
    }


}
