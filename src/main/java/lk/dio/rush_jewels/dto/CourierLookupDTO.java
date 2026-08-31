package lk.dio.rush_jewels.dto;

import java.util.List;

public class CourierLookupDTO {
    private int id;
    private String name;
    private List<BranchDTO> branches;

    public CourierLookupDTO(int id, String name, List<BranchDTO> branches) {
        this.id = id;
        this.name = name;
        this.branches = branches;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public List<BranchDTO> getBranches() { return branches; }

    public static class BranchDTO {
        private int id;
        private String name; // This is the 'branch' field

        public BranchDTO(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public int getId() { return id; }
        public String getName() { return name; }
    }
}