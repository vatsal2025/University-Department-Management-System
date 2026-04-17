package com.udiims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class DepartmentFundService {

    @Autowired
    private SupabaseService supabase;

    // ── Fund Sources (incoming money) ─────────────────────────────────────────

    public List<Map<String, Object>> getFundSources(String departmentId) throws Exception {
        if (departmentId == null || departmentId.isBlank()) throw new RuntimeException("departmentId is required.");
        validateDepartment(departmentId);
        return supabase.getList("department_fund_sources",
                "department_id=eq." + departmentId + "&order=received_date.desc");
    }

    public Map<String, Object> addFundSource(Map<String, Object> body) throws Exception {
        String departmentId = (String) body.get("department_id");
        String sourceName   = (String) body.get("source_name");
        if (departmentId == null || departmentId.isBlank()) throw new RuntimeException("department_id is required.");
        if (sourceName == null || sourceName.isBlank())     throw new RuntimeException("source_name is required.");
        validateDepartment(departmentId);

        double amount = body.get("amount") instanceof Number n ? n.doubleValue() : 0;
        if (amount <= 0) throw new RuntimeException("Amount must be positive.");

        Map<String, Object> record = new HashMap<>();
        record.put("source_id",    "DFS-" + departmentId + "-" + Instant.now().toEpochMilli());
        record.put("department_id", departmentId);
        record.put("source_name",   sourceName);
        record.put("amount",        amount);
        record.put("received_date", body.getOrDefault("received_date", Instant.now().toString()));
        record.put("description",   body.getOrDefault("description", ""));

        List<Map<String, Object>> result = supabase.post("department_fund_sources", record);
        return result.isEmpty() ? record : result.get(0);
    }

    // ── Fund Usage (expenditures) ─────────────────────────────────────────────

    public List<Map<String, Object>> getFundUsage(String departmentId) throws Exception {
        if (departmentId == null || departmentId.isBlank()) throw new RuntimeException("departmentId is required.");
        validateDepartment(departmentId);
        return supabase.getList("department_fund_usage",
                "department_id=eq." + departmentId + "&order=usage_date.desc");
    }

    public Map<String, Object> addFundUsage(Map<String, Object> body) throws Exception {
        String departmentId = (String) body.get("department_id");
        String purpose      = (String) body.get("purpose");
        if (departmentId == null || departmentId.isBlank()) throw new RuntimeException("department_id is required.");
        if (purpose == null || purpose.isBlank())           throw new RuntimeException("purpose is required.");
        validateDepartment(departmentId);

        double amount = body.get("amount") instanceof Number n ? n.doubleValue() : 0;
        if (amount <= 0) throw new RuntimeException("Amount must be positive.");

        // Overspend guard: available balance must cover new expenditure
        double available = computeAvailableBalance(departmentId);
        if (amount > available) {
            throw new RuntimeException(String.format(
                    "Insufficient funds. Available balance: %.2f, Requested: %.2f", available, amount));
        }

        Map<String, Object> record = new HashMap<>();
        record.put("usage_id",     "DFU-" + departmentId + "-" + Instant.now().toEpochMilli());
        record.put("department_id", departmentId);
        record.put("amount",        amount);
        record.put("usage_date",    body.getOrDefault("usage_date", Instant.now().toString()));
        record.put("purpose",       purpose);
        record.put("description",   body.getOrDefault("description", ""));

        List<Map<String, Object>> result = supabase.post("department_fund_usage", record);
        return result.isEmpty() ? record : result.get(0);
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    public Map<String, Object> getDepartmentFundSummary(String departmentId) throws Exception {
        if (departmentId == null || departmentId.isBlank()) throw new RuntimeException("departmentId is required.");
        validateDepartment(departmentId);

        List<Map<String, Object>> sources = supabase.getList("department_fund_sources",
                "department_id=eq." + departmentId + "&order=received_date.desc");
        List<Map<String, Object>> usages  = supabase.getList("department_fund_usage",
                "department_id=eq." + departmentId + "&order=usage_date.desc");

        double totalIncoming = sources.stream()
                .mapToDouble(s -> s.get("amount") instanceof Number n ? n.doubleValue() : 0).sum();
        double totalSpent = usages.stream()
                .mapToDouble(u -> u.get("amount") instanceof Number n ? n.doubleValue() : 0).sum();
        double balance = totalIncoming - totalSpent;

        Map<String, Object> result = new HashMap<>();
        result.put("department_id",    departmentId);
        result.put("total_incoming",   totalIncoming);
        result.put("total_spent",      totalSpent);
        result.put("available_balance", balance);
        result.put("fund_sources",     sources);
        result.put("fund_usage",       usages);
        if (balance < 0) result.put("warning", "Available balance is negative. Immediate review required.");
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double computeAvailableBalance(String departmentId) throws Exception {
        List<Map<String, Object>> sources = supabase.getList("department_fund_sources",
                "department_id=eq." + departmentId);
        List<Map<String, Object>> usages  = supabase.getList("department_fund_usage",
                "department_id=eq." + departmentId);
        double totalIn  = sources.stream().mapToDouble(s -> s.get("amount") instanceof Number n ? n.doubleValue() : 0).sum();
        double totalOut = usages.stream().mapToDouble(u -> u.get("amount") instanceof Number n ? n.doubleValue() : 0).sum();
        return totalIn - totalOut;
    }

    private void validateDepartment(String departmentId) throws Exception {
        Map<String, Object> dept = supabase.getSingle("departments", "department_id=eq." + departmentId);
        if (dept == null) throw new RuntimeException("Department not found: " + departmentId);
    }
}
