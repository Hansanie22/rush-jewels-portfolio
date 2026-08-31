package lk.dio.rush_jewels.dto;

import lk.dio.rush_jewels.model.AddressType;

public class DeliveryAddressRequestDTO {

    // --- FIELDS ---
    private String firstName;
    private String lastName;
    private String contactNo;

    // --- FIX: Renamed to match entity for clarity ---
    private AddressType addressType = AddressType.SHIPPING;
    // --- FIX: Renamed to match entity for clarity ---
    private boolean defaultAddress = true;

    private String line1;
    private String line2;
    private String postalCode;
    private Integer countryId;
    private Integer provinceId;
    private Integer cityId;
    private String provinceOther;
    private String cityOther;

    // --- GETTERS (Corrected) ---
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getContactNo() { return contactNo; }
    public AddressType getAddressType() { return addressType; } // <-- FIX
    public boolean isDefaultAddress() { return defaultAddress; } // <-- FIX
    public String getLine1() { return line1; }
    public String getLine2() { return line2; }
    public String getPostalCode() { return postalCode; }
    public Integer getCountryId() { return countryId; }
    public Integer getProvinceId() { return provinceId; }
    public Integer getCityId() { return cityId; }
    public String getProvinceOther() { return provinceOther; }
    public String getCityOther() { return cityOther; }

    // --- SETTERS (Corrected) ---
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public void setAddressType(AddressType addressType) { this.addressType = addressType; } // <-- FIX
    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; } // <-- FIX
    public void setLine1(String line1) { this.line1 = line1; }
    public void setLine2(String line2) { this.line2 = line2; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setCountryId(Integer countryId) { this.countryId = countryId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }
    public void setProvinceOther(String provinceOther) { this.provinceOther = provinceOther; }
    public void setCityOther(String cityOther) { this.cityOther = cityOther; }
}