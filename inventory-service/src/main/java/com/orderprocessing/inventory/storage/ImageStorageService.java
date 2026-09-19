package com.orderprocessing.inventory.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    String upload(String key, MultipartFile file);

    void delete(String key);

    String getUrl(String key);
}