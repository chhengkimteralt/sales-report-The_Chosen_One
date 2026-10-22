package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.ProductTotal;
import edu.itc.salesreport.model.SaleTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class DefaultSalesAggregator implements SalesAggregator {

    @Override
    public BranchSummary summarize(String branch, List<SaleTransaction> rows) {
        BigDecimal totalRevenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDiscounts = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        Set<String> distinctReceipts = new HashSet<>();
        Map<String, BigDecimal> revByCategory = new TreeMap<>();
        Map<PaymentMethod, BigDecimal> revByPayment = new EnumMap<>(PaymentMethod.class);
        Map<String, ProductAgg> productAggMap = new HashMap<>();

        for (SaleTransaction tx : rows) {
            totalRevenue = totalRevenue.add(tx.revenue());
            totalDiscounts = totalDiscounts.add(tx.discount());
            distinctReceipts.add(tx.receiptNo());

            revByCategory.merge(tx.category(), tx.revenue(), BigDecimal::add);
            revByPayment.merge(tx.paymentMethod(), tx.revenue(), BigDecimal::add);

            productAggMap.compute(tx.sku(), (sku, existing) -> {
                if (existing == null) {
                    return new ProductAgg(sku, tx.productName(), tx.revenue(), tx.quantity());
                } else {
                    return new ProductAgg(
                            sku,
                            tx.productName(),
                            existing.revenue().add(tx.revenue()),
                            existing.quantity() + tx.quantity());
                }
            });
        }

        long receipts = distinctReceipts.size();

        List<ProductTotal> topProducts = productAggMap.values().stream()
                .sorted(Comparator.comparing(ProductAgg::revenue).reversed()
                        .thenComparing(ProductAgg::sku))
                .limit(10)
                .map(pa -> new ProductTotal(pa.sku(), pa.name(), pa.revenue(), pa.quantity()))
                .toList();

        return new BranchSummary(
                branch,
                totalRevenue,
                totalDiscounts,
                receipts,
                revByCategory,
                topProducts,
                revByPayment);
    }

    private record ProductAgg(String sku, String name, BigDecimal revenue, long quantity) {
    }
}