package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import this!
import java.io.Serializable;

@Entity
@Table(name = "courier_service")
public class CourierService implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 50, nullable = false)
    private String branch;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(name = "contact_no", length = 12, nullable = false)
    private String contactNo;

    // --- THE FIX IS HERE ---
    @ManyToOne
    @JoinColumn(name = "courier_company_id", nullable = false)
    @JsonIgnore  // <--- Add this annotation
    private CourierCompany courierCompany;

    public CourierService() {}

    public CourierService(String branch, String address, String contactNo, CourierCompany courierCompany) {
        this.branch = branch;
        this.address = address;
        this.contactNo = contactNo;
        this.courierCompany = courierCompany;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public CourierCompany getCourierCompany() { return courierCompany; }
    public void setCourierCompany(CourierCompany courierCompany) {
        this.courierCompany = courierCompany;
    }
}