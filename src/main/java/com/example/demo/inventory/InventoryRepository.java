package com.example.demo.inventory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public InventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<InventoryRecord> mapper = (rs, i) -> {
        InventoryRecord r = new InventoryRecord();
        r.setStoreId(rs.getLong("store_id"));
        r.setProductId(rs.getLong("product_id"));
        r.setOnHand(rs.getDouble("on_hand"));
        r.setReserved(rs.getDouble("reserved"));
        return r;
    };

    public InventoryRecord get(Long storeId, Long productId) {
        List<InventoryRecord> list = jdbcTemplate.query(
                "select store_id, product_id, on_hand, reserved from inventory where store_id=? and product_id=?",
                mapper, storeId, productId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void upsert(Long storeId, Long productId, double delta) {
        jdbcTemplate.update(
                "insert into inventory(store_id, product_id, on_hand, reserved) values(?,?,?,0) " +
                        "on conflict(store_id, product_id) do update set on_hand = inventory.on_hand + excluded.on_hand, updated_at=now()",
                storeId, productId, delta);
    }

    public void addLedger(Long storeId, Long productId, double delta, String type, String refType, String refId) {
        jdbcTemplate.update(
                "insert into inventory_ledger(store_id, product_id, qty_change, type, ref_type, ref_id) values(?,?,?,?,?,?)",
                storeId, productId, delta, type, refType, refId);
    }

    public List<InventoryLedgerItem> ledger(Long storeId, Long productId, int limit) {
        return jdbcTemplate.query(
                "select id, store_id, product_id, qty_change, type, ref_type, ref_id, occurred_at from inventory_ledger " +
                        "where store_id=? and product_id=? order by id desc limit ?",
                (rs, i) -> {
                    InventoryLedgerItem it = new InventoryLedgerItem();
                    it.setId(rs.getLong("id"));
                    it.setStoreId(rs.getLong("store_id"));
                    it.setProductId(rs.getLong("product_id"));
                    it.setQtyChange(rs.getDouble("qty_change"));
                    it.setType(rs.getString("type"));
                    it.setRefType(rs.getString("ref_type"));
                    it.setRefId(rs.getString("ref_id"));
                    it.setOccurredAt(rs.getTimestamp("occurred_at").toInstant());
                    return it;
                }, storeId, productId, limit
        );
    }
}
