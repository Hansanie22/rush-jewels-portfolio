package lk.dio.rush_jewels.dto;

public class CheckoutDetailsDTO {

    private String email;
    private String userFirstName;
    private String userLastName;
    private CheckoutAddressDTO address;
    private boolean subscribed; // <--- NEW FIELD ADDED
    private boolean defaultAddress;

    // --- Constructors ---
    // MODIFIED: Constructor now accepts 'subscribed'
    public CheckoutDetailsDTO(String email, String userFirstName, String userLastName, CheckoutAddressDTO address, boolean subscribed) {
        this.email = email;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.address = address;
        this.subscribed = subscribed; // <--- MAPPED HERE
    }

    // --- Getters & Setters ---

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserFirstName() { return userFirstName; }
    public void setUserFirstName(String userFirstName) { this.userFirstName = userFirstName; }

    public String getUserLastName() { return userLastName; }
    public void setUserLastName(String userLastName) { this.userLastName = userLastName; }

    public CheckoutAddressDTO getAddress() { return address; }
    public void setAddress(CheckoutAddressDTO address) { this.address = address; }

    public boolean isSubscribed() { return subscribed; } // <--- NEW GETTER
    public void setSubscribed(boolean subscribed) { this.subscribed = subscribed; }

}