package lk.dio.rush_jewels.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // application.properties එකේ තියෙන නම් වලට ගැලපෙන විදිහට @Value වෙනස් කළා
    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("cloud_name", cloudName);
        valuesMap.put("api_key", apiKey);
        valuesMap.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(valuesMap);
    }

    public String uploadImage(MultipartFile file) throws IOException {
        // "resource_type", "auto" දැම්මම Image, Video, PDF ඕනෑම දෙයක් Auto Detect වෙනවා
        Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "auto"
        ));

        // secure_url මගින් හැමවිටම https ලින්ක් එකක් ලැබෙනවා
        return (String) result.get("secure_url");
    }
}