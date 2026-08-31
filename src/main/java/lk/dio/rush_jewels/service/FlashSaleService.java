package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.FlashSaleDTO;
import lk.dio.rush_jewels.model.Sale;
import lk.dio.rush_jewels.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FlashSaleService {

    @Autowired
    private SaleRepository saleRepository;

    public FlashSaleDTO getLatestFlashSale() {

        Sale sale = saleRepository.findLatestActiveSale();

        if (sale == null) {
            return null;
        }

        String name = null;
        Double discount = null;

        if (sale.getProductVariance() != null) {
            name = sale.getProductVariance().getProduct().getName();
            discount = sale.getProductVariance().getDiscountPercentage();
        }

        if (sale.getCollection() != null) {
            name = sale.getCollection().getName();
            discount = sale.getCollection().getDiscountPercentage();
        }

        return new FlashSaleDTO(
                name,
                discount,
                sale.getEndDate(),
                sale.getDescription()
        );
    }
}
