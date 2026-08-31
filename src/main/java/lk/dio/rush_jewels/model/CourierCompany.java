package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "courier_company")
public class CourierCompany implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String name;

    // Status: 1 = Active, 0 = Deleted
    @Column(name = "status", nullable = false, columnDefinition = "INT DEFAULT 1")
    private int status = 1;

    @OneToMany(mappedBy = "courierCompany", cascade = CascadeType.ALL)
    private List<CourierService> courierServices;

    public CourierCompany() {}

    public CourierCompany(String name, int status) {
        this.name = name;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public List<CourierService> getCourierServices() { return courierServices; }
    public void setCourierServices(List<CourierService> courierServices) {
        this.courierServices = courierServices;
    }
}