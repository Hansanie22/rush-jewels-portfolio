package lk.dio.rush_jewels.dto;

import java.util.List;

public class ChartDataDTO {
    private List<String> labels; // e.g., "Mon", "Tue" or "Jan", "Feb"
    private List<Double> data;   // e.g., 15000.0, 20000.0

    public ChartDataDTO(List<String> labels, List<Double> data) {
        this.labels = labels;
        this.data = data;
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
}