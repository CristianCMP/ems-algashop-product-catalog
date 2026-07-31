package com.algaworks.algashop.product.catalog.infrastructure.storage.s3;

import com.algaworks.algashop.product.catalog.application.storage.FileReference;
import com.algaworks.algashop.product.catalog.application.storage.StorageProvider;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@RequiredArgsConstructor
public class StorageProviderAwsS3Impl implements StorageProvider {

    private final S3Template s3Template;
    private final StorageProviderAwsS3Properties properties;

    @Override
    public boolean healthCheck() {
        try {
            return s3Template.bucketExists(properties.getBucketName());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @SneakyThrows
    public URL requestUploadUrl(FileReference fileReference) {
        String bucketName = properties.getBucketName();
        String fileName = fileReference.getFileName();

        if (fileExists(fileName)) {
            throw new StorageProviderException(String.format("Remote file %s already exists", fileName));
        }

        ObjectMetadata.Builder metadataBuilder = ObjectMetadata.builder();

        if (fileReference.isAllowPublicRead()) {
            metadataBuilder.acl("public-read");
        }

        try {
           return s3Template.createSignedPutURL(
                    bucketName,
                    fileName,
                    fileReference.getExpiresIn(),
                    metadataBuilder.build(),
                    fileReference.getContentType().toString()
            );
        } catch (S3Exception e) {
            throw new StorageProviderException(String.format("Unknown error when tried to create presigned URL for file %s", fileName), e);
        }
    }

    @Override
    public void deleteFile(String remoteFileName) {
        if (!fileExists(remoteFileName)) {
            throw new StorageProviderException(String.format("Remote file %s was not found", remoteFileName));
        }

        try {
            s3Template.deleteObject(properties.getBucketName(), remoteFileName);
        } catch (S3Exception e) {
            throw new StorageProviderException(String.format("Unknown error when tried to remove the file %s", remoteFileName), e);
        }
    }

    @Override
    public boolean fileExists(String remoteFileName) {
        return s3Template.objectExists(properties.getBucketName(), remoteFileName);
    }
}
