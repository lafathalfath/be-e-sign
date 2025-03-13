package org.bh_foundation.e_sign.services.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.bh_foundation.e_sign.component.PathComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageService {
    
    @Value("${server.base-url}")
    private String BASE_URL;

    public String store(MultipartFile file, String subject, Integer maxSize, List<String> mimes) throws IOException {

        if (maxSize != null && maxSize > 0 && maxSize < file.getSize()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds maximum allowed size");
        if (mimes != null && !mimes.isEmpty() && !mimes.contains(file.getContentType())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type was not supported");

        String targetPath = PathComponent.STORAGE_PATH + subject + "/";
        File targetDir = new File(targetPath);
        if (!targetDir.exists()) {
            boolean createDir = targetDir.mkdirs();
            if (!createDir) return "Failed to Create Directories";
        }

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String fileUrl = BASE_URL + "/api/storage/" + subject + "/" + filename;
        fileUrl = fileUrl.replaceAll(" ", "%20");

        String filePath = targetPath + filename;
        file.transferTo(new File(filePath));

        return fileUrl;
    }

    public void deleteByUrl(String fileUrl) throws IOException {
        if (fileUrl != null) {
            String filePath = fileUrl.replace(BASE_URL + "/api/storage", PathComponent.STORAGE_PATH);
            File fileTarget = new File(filePath);
            if (fileTarget.exists()) {
                fileTarget.delete();
            }
        } 
    }

}
