package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "integration")
public class Integration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "provider_key", nullable = false)
    private String providerKey;

    @Column(name = "is_connected", nullable = false)
    private boolean connected;

    // ✅ FIX: Explicitly allow NULL values
    @Column(name = "api_key", columnDefinition = "TEXT", nullable = true)
    private String apiKey;

    @Column(name = "icon_class")
    private String iconClass;

    public Integration() {}

    public Integration(String name, String providerKey, boolean connected, String iconClass) {
        this.name = name;
        this.providerKey = providerKey;
        this.connected = connected;
        this.iconClass = iconClass;
        this.apiKey = null; // Allow null on creation
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProviderKey() { return providerKey; }
    public void setProviderKey(String providerKey) { this.providerKey = providerKey; }
    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
}