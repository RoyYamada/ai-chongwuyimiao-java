package com.example.demo.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository repo;

    public InventoryService(InventoryRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void inbound(Long storeId, Long productId, double qty, String refType, String refId) {
        repo.upsert(storeId, productId, qty);
        repo.addLedger(storeId, productId, qty, "IN", refType, refId);
    }

    @Transactional
    public void outbound(Long storeId, Long productId, double qty, String refType, String refId) {
        repo.upsert(storeId, productId, -Math.abs(qty));
        repo.addLedger(storeId, productId, -Math.abs(qty), "OUT", refType, refId);
    }

    public InventoryRecord get(Long storeId, Long productId) {
        return repo.get(storeId, productId);
    }

    public List<InventoryLedgerItem> ledger(Long storeId, Long productId, int limit) {
        return repo.ledger(storeId, productId, limit);
    }
}
