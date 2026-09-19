package com.orderprocessing.inventory.storage;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/test/r2")
public class R2TestController {

    private final ImageStorageService imageStorageService;

    public R2TestController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String upload(@RequestPart("image") MultipartFile image) {

        String key = "product-images/" + UUID.randomUUID() + "-" + image.getOriginalFilename();

        return imageStorageService.upload(key, image);
    }
}