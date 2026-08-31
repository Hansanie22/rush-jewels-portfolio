package lk.dio.rush_jewels.dto;

public class CheckoutAddressDTO {

    private String firstName;
    private String lastName;
    private String contactNo;
    private String addressLine1; // Will be (No + Line 1)
    private String addressLine2; // Will be (Line 2)
    private String postalCode;
    private Integer countryId;
    private Integer provinceId;
    private Integer cityId;
    private String stateText; // Fallback for text input
    private String cityText;  // Fallback for text input
    private boolean defaultAddress;

    // --- Constructors ---
    public CheckoutAddressDTO() {}

    // --- Getters & Setters ---

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public Integer getCountryId() { return countryId; }
    public void setCountryId(Integer countryId) { this.countryId = countryId; }

    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getStateText() { return stateText; }
    public void setStateText(String stateText) { this.stateText = stateText; }

    public String getCityText() { return cityText; }
    public void setCityText(String cityText) { this.cityText = cityText; }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    // <-- ADDED GETTER METHOD -->
    public boolean isDefaultAddress() {
        return defaultAddress;
    }
}