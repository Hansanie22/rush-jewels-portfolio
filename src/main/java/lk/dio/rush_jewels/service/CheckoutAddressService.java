package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.CheckoutAddressDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CheckoutAddressService {

    private final DeliveryAddressRepository addressRepo;
    private final CountryRepository countryRepo;
    private final ProvinceRepository provinceRepo;
    private final CityRepository cityRepo;

    public CheckoutAddressService(DeliveryAddressRepository addressRepo,
                                  CountryRepository countryRepo,
                                  ProvinceRepository provinceRepo,
                                  CityRepository cityRepo) {
        this.addressRepo = addressRepo;
        this.countryRepo = countryRepo;
        this.provinceRepo = provinceRepo;
        this.cityRepo = cityRepo;
    }

    public DeliveryAddress getAddressById(Integer id) {
        return addressRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
    }

    /**
     * Smart Save: Shipping Address
     * Checks Default -> Checks Existing (Any) -> Creates New only if no match found.
     */
    @Transactional
    public DeliveryAddress saveShippingAddressDuringCheckout(User user, CheckoutAddressDTO dto) {
        // 1. Try to find an exact match in existing DB records (Default or Non-Default)
        Optional<DeliveryAddress> existingMatch = findExistingAddress(user, dto, AddressType.SHIPPING);

        if (existingMatch.isPresent()) {
            return existingMatch.get();
        }

        // 2. No match found, create NEW address
        DeliveryAddress newAddr = createAddressFromDTO(user, dto, AddressType.SHIPPING);

        // Handle Default Logic: If user has no shipping address, make this default, or if DTO requested it
        boolean hasDefault = addressRepo.existsByUserAndAddressTypeAndDefaultAddressTrue(user, AddressType.SHIPPING);
        boolean shouldBeDefault = !hasDefault || dto.isDefaultAddress();

        newAddr.setDefaultAddress(shouldBeDefault);

        if (shouldBeDefault && hasDefault) {
            Optional<DeliveryAddress> oldDefault = addressRepo.findByUserAndAddressTypeAndDefaultAddressTrue(user, AddressType.SHIPPING);
            if (oldDefault.isPresent()) {
                DeliveryAddress old = oldDefault.get();
                old.setDefaultAddress(false);
                addressRepo.save(old);
            }
        }

        return addressRepo.save(newAddr);
    }

    /**
     * Smart Save: Billing Address
     * Checks Default -> Checks Existing -> Creates New only if no match found.
     */
    @Transactional
    public DeliveryAddress saveBillingAddressIfNeeded(User user, CheckoutAddressDTO billingDto,
                                                      DeliveryAddress shippingAddress, boolean useDifferentBilling) {

        if (!useDifferentBilling) {
            // --- CASE 1: BILLING SAME AS SHIPPING ---
            Optional<DeliveryAddress> existingBillingMatch = findExistingAddressMatchingEntity(user, shippingAddress, AddressType.BILLING);

            if (existingBillingMatch.isPresent()) {
                return existingBillingMatch.get();
            }

            // No match found, Copy shipping to new Billing
            DeliveryAddress billingFromShipping = copyAddressWithType(shippingAddress, AddressType.BILLING);
            boolean hasDefault = addressRepo.existsByUserAndAddressTypeAndDefaultAddressTrue(user, AddressType.BILLING);
            billingFromShipping.setDefaultAddress(!hasDefault);
            return addressRepo.save(billingFromShipping);

        } else {
            // --- CASE 2: DIFFERENT BILLING ADDRESS ---
            Optional<DeliveryAddress> existingMatch = findExistingAddress(user, billingDto, AddressType.BILLING);

            if (existingMatch.isPresent()) {
                return existingMatch.get();
            }

            // No match, create new
            DeliveryAddress newAddr = createAddressFromDTO(user, billingDto, AddressType.BILLING);
            boolean hasDefault = addressRepo.existsByUserAndAddressTypeAndDefaultAddressTrue(user, AddressType.BILLING);
            newAddr.setDefaultAddress(!hasDefault);
            return addressRepo.save(newAddr);
        }
    }

    // ====================================================================
    // HELPER METHODS
    // ====================================================================

    /**
     * Searches for an existing address (Default first, then others) matching the DTO.
     */
    private Optional<DeliveryAddress> findExistingAddress(User user, CheckoutAddressDTO dto, AddressType type) {
        // 1. Check DEFAULT address first
        Optional<DeliveryAddress> defaultAddr = addressRepo.findByUserAndAddressTypeAndDefaultAddressTrue(user, type);
        if (defaultAddr.isPresent() && isSameAddress(defaultAddr.get(), dto)) {
            return defaultAddr;
        }

        // 2. Check ALL addresses of this type (reuse if exists)
        List<DeliveryAddress> allAddresses = addressRepo.findByUser(user);
        return allAddresses.stream()
                .filter(addr -> addr.getAddressType() == type)
                .filter(addr -> isSameAddress(addr, dto))
                .findFirst();
    }

    private Optional<DeliveryAddress> findExistingAddressMatchingEntity(User user, DeliveryAddress source, AddressType targetType) {
        Optional<DeliveryAddress> defaultAddr = addressRepo.findByUserAndAddressTypeAndDefaultAddressTrue(user, targetType);
        if (defaultAddr.isPresent() && addressesMatch(defaultAddr.get(), source)) {
            return defaultAddr;
        }

        List<DeliveryAddress> allAddresses = addressRepo.findByUser(user);
        return allAddresses.stream()
                .filter(addr -> addr.getAddressType() == targetType)
                .filter(addr -> addressesMatch(addr, source))
                .findFirst();
    }

    private DeliveryAddress createAddressFromDTO(User user, CheckoutAddressDTO dto, AddressType type) {
        Country country = countryRepo.findById(dto.getCountryId())
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));

        DeliveryAddress addr = new DeliveryAddress();
        addr.setUser(user);
        addr.setAddressType(type);
        addr.setFirstName(dto.getFirstName());
        addr.setLastName(dto.getLastName());
        addr.setContactNo(dto.getContactNo());
        addr.setPostalCode(dto.getPostalCode());
        addr.setLine1(dto.getAddressLine1());
        addr.setLine2(dto.getAddressLine2());
        addr.setCountry(country);

        if (dto.getProvinceId() != null) {
            addr.setProvince(provinceRepo.findById(dto.getProvinceId()).orElse(null));
        } else {
            addr.setStateText(dto.getStateText());
        }

        if (dto.getCityId() != null) {
            addr.setCity(cityRepo.findById(dto.getCityId()).orElse(null));
        } else {
            addr.setCityText(dto.getCityText());
        }
        return addr;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private boolean isSameAddress(DeliveryAddress a, CheckoutAddressDTO b) {
        if (!normalize(a.getLine1()).equals(normalize(b.getAddressLine1()))) return false;
        if (!normalize(a.getLine2()).equals(normalize(b.getAddressLine2()))) return false;
        if (!normalize(a.getPostalCode()).equals(normalize(b.getPostalCode()))) return false;

        // Compare Country
        if (a.getCountry().getId() != b.getCountryId()) return false;

        // Check Province (ID vs Text)
        // If DB has Province ID, DTO must have matching ID
        if (a.getProvince() != null) {
            if (b.getProvinceId() == null || a.getProvince().getId() != b.getProvinceId()) return false;
        }
        // If DB has State Text, DTO must have matching Text (ignoring case)
        else if (a.getStateText() != null) {
            if (!normalize(a.getStateText()).equals(normalize(b.getStateText()))) return false;
        }
        // If DB has neither, DTO must have neither
        else {
            if (b.getProvinceId() != null || !normalize(b.getStateText()).isEmpty()) return false;
        }

        // Check City (ID vs Text)
        if (a.getCity() != null) {
            if (b.getCityId() == null || a.getCity().getId() != b.getCityId()) return false;
        }
        else if (a.getCityText() != null) {
            if (!normalize(a.getCityText()).equals(normalize(b.getCityText()))) return false;
        }
        else {
            if (b.getCityId() != null || !normalize(b.getCityText()).isEmpty()) return false;
        }

        return true;
    }

    private boolean addressesMatch(DeliveryAddress a, DeliveryAddress b) {
        if (!normalize(a.getLine1()).equals(normalize(b.getLine1()))) return false;
        if (!normalize(a.getLine2()).equals(normalize(b.getLine2()))) return false;
        if (!normalize(a.getPostalCode()).equals(normalize(b.getPostalCode()))) return false;

        if (a.getCountry().getId() != b.getCountry().getId()) return false;

        // Province Match
        if (a.getProvince() != null && b.getProvince() != null) {
            if (a.getProvince().getId() != b.getProvince().getId()) return false;
        } else if (!normalize(a.getStateText()).equals(normalize(b.getStateText()))) {
            return false;
        }

        // City Match
        if (a.getCity() != null && b.getCity() != null) {
            if (a.getCity().getId() != b.getCity().getId()) return false;
        } else if (!normalize(a.getCityText()).equals(normalize(b.getCityText()))) {
            return false;
        }

        return true;
    }

    private DeliveryAddress copyAddressWithType(DeliveryAddress source, AddressType newType) {
        DeliveryAddress copy = new DeliveryAddress();
        copy.setUser(source.getUser());
        copy.setAddressType(newType);
        copy.setFirstName(source.getFirstName());
        copy.setLastName(source.getLastName());
        copy.setContactNo(source.getContactNo());
        copy.setLine1(source.getLine1());
        copy.setLine2(source.getLine2());
        copy.setPostalCode(source.getPostalCode());
        copy.setCountry(source.getCountry());
        copy.setProvince(source.getProvince());
        copy.setCity(source.getCity());
        copy.setStateText(source.getStateText());
        copy.setCityText(source.getCityText());
        return copy;
    }
}