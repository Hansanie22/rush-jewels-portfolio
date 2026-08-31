package lk.dio.rush_jewels.dto;

import java.io.Serializable;
import java.util.List;

public class ReturnRequestDTO implements Serializable {
    private String reason;
    private List<String> selectedItemNames; // To store in description

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<String> getSelectedItemNames() { return selectedItemNames; }
    public void setSelectedItemNames(List<String> selectedItemNames) { this.selectedItemNames = selectedItemNames; }
}