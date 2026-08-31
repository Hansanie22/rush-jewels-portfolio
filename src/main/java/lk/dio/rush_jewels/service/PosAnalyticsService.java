package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.Payment;
import lk.dio.rush_jewels.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PosAnalyticsService {

    private final PaymentRepository paymentRepo;

    public PosAnalyticsService(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    public Map<String, Object> getSalesSplitReport() {
        List<Payment> allPayments = paymentRepo.findAll();
        
        // Online vs POS Sales
        double posTotal = 0;
        double onlineTotal = 0;
        
        // Payment Methods (Cash vs Card vs Bank) for POS
        double posCash = 0;
        double posCard = 0;
        double posBank = 0;

        for (Payment p : allPayments) {
            String source = p.getOrders().getOrderSource(); // "ONLINE" or "POS"
            String method = p.getPaymentsMethod().getMethod(); // "Cash", "Credit / Debit Card", "Bank Transfer"
            
            if ("POS".equalsIgnoreCase(source)) {
                posTotal += p.getFinalTotal();
                if (method.contains("Cash")) posCash += p.getFinalTotal();
                else if (method.contains("Card")) posCard += p.getFinalTotal();
                else if (method.contains("Bank")) posBank += p.getFinalTotal();
            } else {
                onlineTotal += p.getFinalTotal();
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", posTotal + onlineTotal);
        
        Map<String, Double> sourceSplit = new HashMap<>();
        sourceSplit.put("POS", posTotal);
        sourceSplit.put("ONLINE", onlineTotal);
        report.put("sourceSplit", sourceSplit);
        
        Map<String, Double> posMethodSplit = new HashMap<>();
        posMethodSplit.put("Cash", posCash);
        posMethodSplit.put("Card", posCard);
        posMethodSplit.put("BankTransfer", posBank);
        report.put("posPaymentMethods", posMethodSplit);
        
        return report;
    }
}
