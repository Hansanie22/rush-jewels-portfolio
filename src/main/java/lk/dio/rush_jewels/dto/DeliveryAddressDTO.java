package lk.dio.rush_jewels.dto;

import lk.dio.rush_jewels.model.DeliveryAddress;

public class DeliveryAddressDTO {

    private int id;
    private String line1;
    private String line2;
    private String postalCode;
    private String contactNo;
    private String firstName;
    private String lastName;

    private String stateText; // Corresponds to provinceOther
    private String cityText;  // Corresponds to cityOther

    private boolean defaultAddress; // Added to expose the status to the frontend

    private CityDTO city;
    private ProvinceDTO province;
    private CountryDTO country;

    // Constructor to easily convert an Entity to a DTO
    public DeliveryAddressDTO(DeliveryAddress address) {
        this.id = address.getId();
        this.line1 = address.getLine1();
        this.line2 = address.getLine2();
        this.postalCode = address.getPostalCode();
        this.contactNo = address.getContactNo();
        this.firstName = address.getFirstName();
        this.lastName = address.getLastName();

        this.stateText = address.getStateText();
        this.cityText = address.getCityText();
        this.defaultAddress = address.isDefaultAddress(); // MAPPED HERE

        // Convert nested entities to their DTOs
        if (address.getCity() != null) {
            this.city = new CityDTO(address.getCity());
        }
        if (address.getProvince() != null) {
            this.province = new ProvinceDTO(address.getProvince());
        }
        if (address.getCountry() != null) {
            this.country = new CountryDTO(address.getCountry());
        }
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getLine1() { return line1; }
    public String getLine2() { return line2; }
    public String getPostalCode() { return postalCode; }
    public String getContactNo() { return contactNo; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public CityDTO getCity() { return city; }
    public ProvinceDTO getProvince() { return province; }
    public CountryDTO getCountry() { return country; }

    public String getStateText() { return stateText; }
    public String getCityText() { return cityText; }

    public boolean isDefaultAddress() { return defaultAddress; } // NEW GETTER
}