package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.DeliveryAddress;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.DeliveryAddressRepository;
import lk.dio.rush_jewels.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final DeliveryAddressRepository addressRepository;
    private final CloudinaryService cloudinaryService; // ✅ Cloudinary Service එක සම්බන්ධ කළා

    // ✅ Removed: Path profileImageBaseDir (Local file saving logic removed)

    public ProfileService(UserRepository userRepository,
                          DeliveryAddressRepository addressRepository,
                          CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User updateProfile(String email, String fname, String lname, String mobile) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFname(fname);
        user.setLname(lname);
        user.setMobile(mobile);
        userRepository.save(user);

        List<DeliveryAddress> addresses = addressRepository.findByUser(user);
        if (!addresses.isEmpty() && mobile != null && !mobile.isBlank()) {
            DeliveryAddress addr = addresses.get(0);
            addr.setContactNo(mobile);
            addressRepository.save(addr);
        }

        return user;
    }

    // ✅ Updated: Upload to Cloudinary & Save URL to User Entity
    public String saveProfileImage(Integer userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IOException("Empty file uploaded");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Upload to Cloudinary
        String imageUrl = cloudinaryService.uploadImage(file);

        // 2. Save URL to User Entity
        user.setImagePath(imageUrl); // ⚠️ User Entity එකේ imagePath කියලා field එකක් තියෙන්න ඕන (පහල බලන්න)
        userRepository.save(user);

        return imageUrl;
    }

    // ❌ Removed: getProfileImage(byte[]) method.
    // Frontend එක දැන් කෙලින්ම user.getImagePath() හරහා URL එක ගන්නවා.

    public List<DeliveryAddress> getUserAddresses(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();
        return addressRepository.findByUser(user);
    }
}