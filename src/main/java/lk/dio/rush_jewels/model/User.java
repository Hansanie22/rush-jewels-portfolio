package lk.dio.rush_jewels.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String fname;

    @Column(length = 45, nullable = false)
    private String lname;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 10,nullable = true)
    private String mobile;

    @Column(length = 225)
    private String verification;

    @JsonIgnore  // Don't serialize password
    @Column(length = 100)
    private String password;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "verification_expiry")
    @Temporal(TemporalType.TIMESTAMP)
    private Date verificationExpiry;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Column(name = "login_provider", length = 20, nullable = false)
    private String loginProvider = "LOCAL";

    @Column(name = "provider_id", length = 100, unique = true)
    private String providerId;

    @Column(name = "subscribed", nullable = false)
    private boolean subscribed = false;

    @Column(name = "type", length = 10, nullable = false)
    private String type = "USER";

    @Column(name = "image_path", length = 500)
    private String imagePath;

    // ✅ Getter & Setter
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    // --- Cart relationship ---
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Cart> carts = new HashSet<>();

    // --- Address relationship ---
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DeliveryAddress> addresses = new HashSet<>();

    public User() {}

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }
    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // NEW: Getter & Setter for Mobile
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getVerification() { return verification; }
    public void setVerification(String verification) { this.verification = verification; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getVerificationExpiry() { return verificationExpiry; }
    public void setVerificationExpiry(Date verificationExpiry) { this.verificationExpiry = verificationExpiry; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getLoginProvider() { return loginProvider; }
    public void setLoginProvider(String loginProvider) { this.loginProvider = loginProvider; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public Set<Cart> getCarts() { return carts; }
    public void setCarts(Set<Cart> carts) { this.carts = carts; }
    public Set<DeliveryAddress> getAddresses() { return addresses; }
    public void setAddresses(Set<DeliveryAddress> addresses) { this.addresses = addresses; }
    public boolean isSubscribed() { return subscribed; }
    public void setSubscribed(boolean subscribed) { this.subscribed = subscribed; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}