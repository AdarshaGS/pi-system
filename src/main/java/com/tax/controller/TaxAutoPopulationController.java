package com.tax.controller;

import com.tax.data.CapitalGainsTransaction;
import com.tax.data.TaxSavingInvestment;
import com.tax.service.TaxAutoPopulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for auto-populating tax data from other modules
 * Provides endpoints to automatically fetch and link portfolio, FD, insurance, and income data
 */
@RestController
@Tag(name = "Tax Auto-Population", description = "Auto-populate tax data from portfolio, investments, and income modules")
@RequestMapping("api/v1/tax/auto-populate")
@RequiredArgsConstructor
public class TaxAutoPopulationController {

    private final TaxAutoPopulationService autoPopulationService;

    // ========== Capital Gains Auto-Population ==========

    @PostMapping("/{userId}/capital-gains")
    @Operation(
        summary = "Auto-populate capital gains",
        description = "Automatically calculate capital gains from portfolio sell transactions for the financial year"
    )
    public ResponseEntity<List<CapitalGainsTransaction>> autoPopulateCapitalGains(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        List<CapitalGainsTransaction> transactions = autoPopulationService.autoPopulateCapitalGains(userId, financialYear);
        return ResponseEntity.ok(transactions);
    }

    // ========== Income Auto-Population ==========

    @PostMapping("/{userId}/salary-income")
    @Operation(
        summary = "Auto-populate salary income",
        description = "Automatically fetch salary income from income/payroll module for the financial year"
    )
    public ResponseEntity<Map<String, String>> autoPopulateSalaryIncome(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        autoPopulationService.autoPopulateSalaryIncome(userId, financialYear);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Salary income auto-populated successfully"
        ));
    }

    @PostMapping("/{userId}/interest-income")
    @Operation(
        summary = "Auto-populate interest income",
        description = "Automatically calculate interest income from FD and savings accounts for the financial year"
    )
    public ResponseEntity<Map<String, String>> autoPopulateInterestIncome(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        autoPopulationService.autoPopulateInterestIncome(userId, financialYear);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Interest income auto-populated successfully"
        ));
    }

    @PostMapping("/{userId}/dividend-income")
    @Operation(
        summary = "Auto-populate dividend income",
        description = "Automatically calculate dividend income from stock holdings for the financial year"
    )
    public ResponseEntity<Map<String, String>> autoPopulateDividendIncome(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        autoPopulationService.autoPopulateDividendIncome(userId, financialYear);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Dividend income auto-populated successfully"
        ));
    }

    // ========== Tax Saving Investments Auto-Population ==========

    @PostMapping("/{userId}/80c-investments")
    @Operation(
        summary = "Auto-populate 80C investments",
        description = "Automatically fetch 80C investments from FD, insurance, PPF, ELSS, and home loan principal"
    )
    public ResponseEntity<List<TaxSavingInvestment>> autoPopulate80CInvestments(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        List<TaxSavingInvestment> investments = autoPopulationService.autoPopulate80CInvestments(userId, financialYear);
        return ResponseEntity.ok(investments);
    }

    @PostMapping("/{userId}/80d-investments")
    @Operation(
        summary = "Auto-populate 80D investments",
        description = "Automatically fetch health insurance premiums for 80D deduction"
    )
    public ResponseEntity<List<TaxSavingInvestment>> autoPopulate80DInvestments(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        List<TaxSavingInvestment> investments = autoPopulationService.autoPopulate80DInvestments(userId, financialYear);
        return ResponseEntity.ok(investments);
    }

    @PostMapping("/{userId}/home-loan-interest")
    @Operation(
        summary = "Auto-populate home loan interest",
        description = "Automatically calculate home loan interest for 24B and 80EEA deductions"
    )
    public ResponseEntity<Map<String, String>> autoPopulateHomeLoanInterest(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        autoPopulationService.autoPopulateHomeLoanInterest(userId, financialYear);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Home loan interest auto-populated successfully"
        ));
    }

    // ========== Bulk Auto-Population ==========

    @PostMapping("/{userId}/all")
    @Operation(
        summary = "Auto-populate all tax data",
        description = "Automatically populate all tax-related data (capital gains, income, investments) for the financial year"
    )
    public ResponseEntity<Map<String, Object>> autoPopulateAllTaxData(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        
        // Auto-populate capital gains
        List<CapitalGainsTransaction> capitalGains = autoPopulationService.autoPopulateCapitalGains(userId, financialYear);
        
        // Auto-populate incomes
        autoPopulationService.autoPopulateSalaryIncome(userId, financialYear);
        autoPopulationService.autoPopulateInterestIncome(userId, financialYear);
        autoPopulationService.autoPopulateDividendIncome(userId, financialYear);
        
        // Auto-populate tax saving investments
        List<TaxSavingInvestment> investments80C = autoPopulationService.autoPopulate80CInvestments(userId, financialYear);
        List<TaxSavingInvestment> investments80D = autoPopulationService.autoPopulate80DInvestments(userId, financialYear);
        
        // Auto-populate home loan interest
        autoPopulationService.autoPopulateHomeLoanInterest(userId, financialYear);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "All tax data auto-populated successfully",
            "capitalGainsCount", capitalGains.size(),
            "80CInvestmentsCount", investments80C.size(),
            "80DInvestmentsCount", investments80D.size()
        ));
    }
}
