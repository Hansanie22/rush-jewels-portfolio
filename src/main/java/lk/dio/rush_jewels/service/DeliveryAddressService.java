package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.CheckoutAddressDTO;
import lk.dio.rush_jewels.dto.CheckoutDetailsDTO;
import lk.dio.rush_jewels.dto.DeliveryAddressDTO;
import lk.dio.rush_jewels.dto.DeliveryAddressRequestDTO;
import lk.dio.rush_jewels.model.AddressType;
import lk.dio.rush_jewels.model.City;
import lk.dio.rush_jewels.model.Country;
import lk.dio.rush_jewels.model.DeliveryAddress;
import lk.dio.rush_jewels.model.Province;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.CityRepository;
import lk.dio.rush_jewels.repository.CountryRepository;
import lk.dio.rush_jewels.repository.DeliveryAddressRepository;
import lk.dio.rush_jewels.repository.ProvinceRepository;
import lk.dio.rush_jewels.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeliveryAddressService {

    private final DeliveryAddressRepository addressRepo;
    private final CityRepository cityRepo;
    private final ProvinceRepository provinceRepo;
    private final CountryRepository countryRepo;
    private final UserRepository userRepo;

    public DeliveryAddressService(DeliveryAddressRepository addressRepo,
                                  CityRepository cityRepo,
                                  ProvinceRepository provinceRepo,
                                  CountryRepository countryRepo,
                                  UserRepository userRepo) {
        this.addressRepo = addressRepo;
        this.cityRepo = cityRepo;
        this.provinceRepo = provinceRepo;
        this.countryRepo = countryRepo;
        this.userRepo = userRepo;
    }

    public List<DeliveryAddressDTO> getAddressesByUser(User user) {
        return addressRepo.findByUser(user)
                .stream()
                .map(DeliveryAddressDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryAddressDTO saveOrUpdateAddress(User user, DeliveryAddressRequestDTO request) {

        // 1. Fetch the freshest user data to ensure we have the correct mobile number from the profile
        User freshUser = userRepo.findById(user.getId()).orElse(user);

        Country country = countryRepo.findById(request.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found"));

        // If firstName is missing, we assume this is an Account Profile update
        boolean isCheckoutSave = StringUtils.hasText(request.getFirstName());
        DeliveryAddress addressToSave;

        if (!isCheckoutSave) {
            // --- ACCOUNT LOGIC: Always update the existing default SHIPPING row if it exists ---
            Optional<DeliveryAddress> existingDefault = addressRepo
                    .findByUserAndAddressTypeAndDefaultAddressTrue(freshUser, AddressType.SHIPPING);

            addressToSave = existingDefault.orElseGet(DeliveryAddress::new);

            addressToSave.setUser(freshUser);
            addressToSave.setAddressType(AddressType.SHIPPING);
            addressToSave.setDefaultAddress(true);

            // AUTO-SYNC: Take names and mobile from the Profile
            addressToSave.setFirstName(freshUser.getFname());
            addressToSave.setLastName(freshUser.getLname());
            addressToSave.setContactNo(freshUser.getMobile());

        } else {
            // --- CHECKOUT LOGIC: New entry or duplicate check ---
            Optional<DeliveryAddress> duplicateAddress = findMatchingAddress(freshUser, request, country);

            if (duplicateAddress.isPresent()) {
                addressToSave = duplicateAddress.get();
                if (request.isDefaultAddress()) {
                    addressRepo.updateAllDefaultToFalse(freshUser, request.getAddressType());
                    addressToSave.setDefaultAddress(true);
                }
            } else {
                if (request.isDefaultAddress()) {
                    addressRepo.updateAllDefaultToFalse(freshUser, request.getAddressType());
                }
                addressToSave = new DeliveryAddress();
                addressToSave.setUser(freshUser);
                addressToSave.setAddressType(request.getAddressType());
                addressToSave.setDefaultAddress(request.isDefaultAddress());
            }

            addressToSave.setFirstName(request.getFirstName());
            addressToSave.setLastName(request.getLastName());

            // In Checkout, use provided contactNo, but fallback to Profile Mobile if empty
            String contact = StringUtils.hasText(request.getContactNo()) ? request.getContactNo() : freshUser.getMobile();
            addressToSave.setContactNo(contact);
        }

        // --- COMMON LOGIC: Set address fields for both flows ---

        // Final sanity check: if contactNo is STILL null/empty (could happen if both are empty),
        // ensure we attempt to pull from freshUser one last time before saving.
        if (!StringUtils.hasText(addressToSave.getContactNo())) {
            addressToSave.setContactNo(freshUser.getMobile());
        }

        addressToSave.setCountry(country);
        addressToSave.setLine1(request.getLine1() != null ? request.getLine1().trim() : null);
        addressToSave.setLine2(
                (request.getLine2() != null && !request.getLine2().trim().isEmpty())
                        ? request.getLine2().trim()
                        : null
        );
        addressToSave.setPostalCode(request.getPostalCode());

        setProvinceLogic(addressToSave, request.getProvinceId(), request.getProvinceOther());
        setCityLogic(addressToSave, request.getCityId(), request.getCityOther());

        DeliveryAddress saved = addressRepo.save(addressToSave);
        return new DeliveryAddressDTO(saved);
    }

    private Optional<DeliveryAddress> findMatchingAddress(User user, DeliveryAddressRequestDTO request, Country country) {
        String reqLine1 = Optional.ofNullable(request.getLine1()).map(String::trim).orElse("");
        String reqLine2 = Optional.ofNullable(request.getLine2()).map(String::trim).orElse("");
        String reqPostal = Optional.ofNullable(request.getPostalCode()).map(String::trim).orElse("");
        String reqStateText = Optional.ofNullable(request.getProvinceOther()).map(String::trim).orElse("");
        String reqCityText = Optional.ofNullable(request.getCityOther()).map(String::trim).orElse("");

        java.util.function.BiPredicate<String, String> equalsIgnoreCaseSafe =
                (s1, s2) -> s1 != null && s2 != null && s1.trim().equalsIgnoreCase(s2.trim());

        return addressRepo.findByUser(user).stream()
                .filter(a -> a.getCountry() != null && java.util.Objects.equals(a.getCountry().getId(), country.getId()))
                .filter(a -> equalsIgnoreCaseSafe.test(a.getLine1(), reqLine1))
                .filter(a -> equalsIgnoreCaseSafe.test(a.getLine2(), reqLine2))
                .filter(a -> equalsIgnoreCaseSafe.test(a.getPostalCode(), reqPostal))
                .filter(a -> {
                    if (request.getProvinceId() != null) {
                        return a.getProvince() != null && java.util.Objects.equals(a.getProvince().getId(), request.getProvinceId());
                    } else {
                        return equalsIgnoreCaseSafe.test(a.getStateText(), reqStateText);
                    }
                })
                .filter(a -> {
                    if (request.getCityId() != null) {
                        return a.getCity() != null && java.util.Objects.equals(a.getCity().getId(), request.getCityId());
                    } else {
                        return equalsIgnoreCaseSafe.test(a.getCityText(), reqCityText);
                    }
                })
                .findFirst();
    }

    private void setProvinceLogic(DeliveryAddress address, Integer provinceId, String provinceOther) {
        if (provinceId != null) {
            Province province = provinceRepo.findById(provinceId)
                    .orElseThrow(() -> new RuntimeException("Province not found"));
            address.setProvince(province);
            address.setStateText(null);
        } else if (provinceOther != null && !provinceOther.trim().isEmpty()) {
            address.setProvince(null);
            address.setStateText(provinceOther);
        } else {
            address.setProvince(null);
            address.setStateText(null);
        }
    }

    private void setCityLogic(DeliveryAddress address, Integer cityId, String cityOther) {
        if (cityId != null) {
            City city = cityRepo.findById(cityId)
                    .orElseThrow(() -> new RuntimeException("City not found"));
            address.setCity(city);
            address.setCityText(null);
        } else if (cityOther != null && !cityOther.trim().isEmpty()) {
            address.setCity(null);
            address.setCityText(cityOther);
        } else {
            address.setCity(null);
            address.setCityText(null);
        }
    }

    public CheckoutDetailsDTO getCheckoutDetails(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Fetch fresh user to get the latest mobile number from DB
        User freshUser = userRepo.findById(user.getId()).orElse(user);

        String email = freshUser.getEmail();
        String userFirstName = freshUser.getFname();
        String userLastName = freshUser.getLname();
        String userMobile = freshUser.getMobile();

        Optional<DeliveryAddress> addressOpt = addressRepo
                .findByUserAndAddressTypeAndDefaultAddressTrue(freshUser, AddressType.SHIPPING);

        if (addressOpt.isEmpty()) {
            addressOpt = addressRepo.findTopByUserAndAddressTypeOrderByIdDesc(freshUser, AddressType.SHIPPING);
        }

        CheckoutAddressDTO addressDTO = null;

        if (addressOpt.isPresent()) {
            DeliveryAddress address = addressOpt.get();
            addressDTO = new CheckoutAddressDTO();

            addressDTO.setFirstName(StringUtils.hasText(address.getFirstName()) ? address.getFirstName() : userFirstName);
            addressDTO.setLastName(StringUtils.hasText(address.getLastName()) ? address.getLastName() : userLastName);

            // Critical: fallback to user mobile if the address record is missing a contact number
            addressDTO.setContactNo(StringUtils.hasText(address.getContactNo()) ? address.getContactNo() : userMobile);

            addressDTO.setPostalCode(address.getPostalCode());
            addressDTO.setDefaultAddress(address.isDefaultAddress());

            addressDTO.setAddressLine1(address.getLine1() != null ? address.getLine1() : "");
            addressDTO.setAddressLine2(address.getLine2());

            if (address.getCountry() != null) {
                addressDTO.setCountryId(address.getCountry().getId());
            }
            if (address.getProvince() != null) {
                addressDTO.setProvinceId(address.getProvince().getId());
            }
            if (address.getCity() != null) {
                addressDTO.setCityId(address.getCity().getId());
            }

            addressDTO.setStateText(address.getStateText());
            addressDTO.setCityText(address.getCityText());
        }

        boolean isSubscribed = freshUser.isSubscribed();

        return new CheckoutDetailsDTO(email, userFirstName, userLastName, addressDTO, isSubscribed);
    }
}