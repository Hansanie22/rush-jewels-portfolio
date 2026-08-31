package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "system_setting")
public class SystemSetting implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "setting_key", unique = true, nullable = false)
    private String key; // e.g., "TAX_RATE"

    @Column(name = "setting_value")
    private String value; // e.g., "10.0"

    public SystemSetting() {}

    public SystemSetting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}