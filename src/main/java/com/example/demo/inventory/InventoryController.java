package com.example.demo.inventory;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    public static class AdjustRequest {
        public Long storeId;
        public Long productId;
        public double qty;
        public String refType;
        public String refId;
    }

    @PostMapping("/inbound")
    public void inbound(@RequestBody AdjustRequest req) {
        service.inbound(req.storeId, req.productId, req.qty, req.refType, req.refId);
    }

    @PostMapping("/outbound")
    public void outbound(@RequestBody AdjustRequest req) {
        service.outbound(req.storeId, req.productId, req.qty, req.refType, req.refId);
    }

    @GetMapping("/{storeId}/{productId}")
    public InventoryRecord get(@PathVariable Long storeId, @PathVariable Long productId) {
        return service.get(storeId, productId);
    }

    @GetMapping("/{storeId}/{productId}/ledger")
    public List<InventoryLedgerItem> ledger(@PathVariable Long storeId, @PathVariable Long productId,
                                            @RequestParam(defaultValue = "50") int limit) {
        return service.ledger(storeId, productId, limit);
    }
}
