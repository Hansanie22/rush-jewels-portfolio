package lk.dio.rush_jewels.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "gemstone")
public class Gemstone implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "gem_stone", length = 45, nullable = false)
    private String gemStone;

    public Gemstone() {
    }

    public Gemstone(int id, String gemStone) {
        this.id = id;
        this.gemStone = gemStone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGemStone() {
        return gemStone;
    }

    public void setGemStone(String gemStone) {
        this.gemStone = gemStone;
    }
}
