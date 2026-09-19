package com.orderprocessing.inventory.storage;

import com.orderprocessing.inventory.config.R2Properties;
import com.orderprocessing.inventory.exception.ImageStorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

@Service
public class R2ImageStorageService implements ImageStorageService {

    private final S3Client s3Client;
    private final R2Properties r2Properties;
    private final S3Presigner s3Presigner;

    public R2ImageStorageService(
            S3Client s3Client,
            R2Properties r2Properties, S3Presigner s3Presigner) {

        this.s3Client = s3Client;
        this.r2Properties = r2Properties;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public String upload(String key, MultipartFile file) {

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Properties.bucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            return key;

        } catch (IOException | SdkException exception) {

            throw new ImageStorageException(
                    "Failed to upload image to R2",
                    exception
            );
        }
    }

    @Override
    public void delete(String key) {

        try {
            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(r2Properties.bucket())
                            .key(key)
                            .build();

            s3Client.deleteObject(request);

        } catch (SdkException exception) {

            throw new ImageStorageException(
                    "Failed to delete image from R2: " + key,
                    exception
            );
        }
    }

    @Override
    public String getUrl(String key) {

        try {
            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder()
                            .bucket(r2Properties.bucket())
                            .key(key)
                            .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(
                                    Duration.ofMinutes(15)
                            )
                            .getObjectRequest(getObjectRequest)
                            .build();

            return s3Presigner
                    .presignGetObject(presignRequest)
                    .url()
                    .toString();

        } catch (SdkException exception) {

            throw new ImageStorageException(
                    "Failed to generate image URL",
                    exception
            );
        }
    }
}