package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "delivery_address")
public class DeliveryAddress implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "line_1", length = 200, nullable = false)
    private String line1;

    @Column(name = "line_2", length = 200,nullable = true)
    private String line2;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @ManyToOne
    @JoinColumn(name = "city_id", referencedColumnName = "id", nullable = true)
    private City city;

    @ManyToOne
    @JoinColumn(name = "province_id", referencedColumnName = "id", nullable = true)
    private Province province;

    @ManyToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id", nullable = false)
    private Country country;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "fname", length = 100)
    private String firstName;

    @Column(name = "lname", length = 100)
    private String lastName;

    @Column(name = "contact_no", length = 15)
    private String contactNo;

    @Column(name = "state_text", length = 45)
    private String stateText;

    @Column(name = "city_text", length = 45)
    private String cityText;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    private AddressType addressType; // NEW

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress = true; // Optional: mark default address

    public DeliveryAddress() {}

    // --- Getters & Setters ---
    public int getId() { return id; }
    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }
    public String getLine2() { return line2; }
    public void setLine2(String line2) { this.line2 = line2; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }
    public Province getProvince() { return province; }
    public void setProvince(Province province) { this.province = province; }
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public String getStateText() { return stateText; }
    public void setStateText(String stateText) { this.stateText = stateText; }
    public String getCityText() { return cityText; }
    public void setCityText(String cityText) { this.cityText = cityText; }
    public AddressType getAddressType() { return addressType; }
    public void setAddressType(AddressType addressType) { this.addressType = addressType; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
}
