package com.budget.service;

import com.budget.data.BudgetReportDTO;
import com.budget.data.Expense;
import com.budget.data.ExpenseCategory;
import com.budget.data.Income;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating PDF reports
 */
@Service
public class ReportGenerationService {

    @Autowired
    private BudgetService budgetService;

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    /**
     * Generate monthly budget report in PDF format
     */
    public byte[] generateMonthlyReport(Long userId, String monthYear) {
        try {
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // Parse month and year
            YearMonth yearMonth = parseMonthYear(monthYear);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            // Add title
            Paragraph title = new Paragraph("Monthly Budget Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Add report period
            Paragraph period = new Paragraph(startDate.format(MONTH_FORMATTER), HEADING_FONT);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(20);
            document.add(period);

            // Get report data
            BudgetReportDTO report = budgetService.getMonthlyReport(userId, monthYear);

            // Add summary section
            addSummarySection(document, report);

            // Add budget vs actual section
            addBudgetVsActualSection(document, report);

            // Add expenses breakdown
            addExpensesBreakdownSection(document, report);

            // Add income breakdown
            addIncomeBreakdownSection(document, report);

            // Add footer
            addFooter(document);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Add summary section with key metrics
     */
    private void addSummarySection(Document document, BudgetReportDTO report) throws DocumentException {
        Paragraph summaryTitle = new Paragraph("Summary", HEADING_FONT);
        summaryTitle.setSpacingBefore(10);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);

        addSummaryRow(table, "Total Budget", "$" + report.getTotalBudget());
        addSummaryRow(table, "Total Expenses", "$" + report.getTotalExpenses());
        addSummaryRow(table, "Total Income", "$" + report.getTotalIncome());
        addSummaryRow(table, "Remaining Budget", "$" + report.getRemainingBudget());
        addSummaryRow(table, "Savings", "$" + report.getSavings());
        addSummaryRow(table, "Budget Usage", report.getBudgetUsagePercentage() + "%");

        document.add(table);
    }

    /**
     * Add budget vs actual by category
     */
    private void addBudgetVsActualSection(Document document, BudgetReportDTO report) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Budget vs Actual by Category", HEADING_FONT);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);
        table.setWidths(new float[]{3, 2, 2, 2});

        // Header
        addTableHeader(table, "Category");
        addTableHeader(table, "Budget");
        addTableHeader(table, "Actual");
        addTableHeader(table, "Difference");

        // Data rows
        if (report.getCategoryBreakdown() != null) {
            for (Map.Entry<ExpenseCategory, BudgetReportDTO.CategorySummary> entry : report.getCategoryBreakdown().entrySet()) {
                String category = entry.getKey().name();
                BudgetReportDTO.CategorySummary summary = entry.getValue();
                BigDecimal budget = summary.getLimit();
                BigDecimal actual = summary.getSpent();
                BigDecimal difference = summary.getRemaining();

                addTableCell(table, category);
                addTableCell(table, "$" + budget);
                addTableCell(table, "$" + actual);
                addTableCell(table, "$" + difference);
            }
        }

        document.add(table);
    }

    /**
     * Add expenses breakdown section
     */
    private void addExpensesBreakdownSection(Document document, BudgetReportDTO report) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Recent Expenses", HEADING_FONT);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        if (report.getRecentExpenses() != null && !report.getRecentExpenses().isEmpty()) {
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingAfter(20);
            table.setWidths(new float[]{2, 3, 2, 2});

            // Header
            addTableHeader(table, "Date");
            addTableHeader(table, "Description");
            addTableHeader(table, "Category");
            addTableHeader(table, "Amount");

            // Data rows (top 10 recent expenses)
            List<Expense> recentExpenses = report.getRecentExpenses().stream()
                .limit(10)
                .collect(Collectors.toList());

            for (Expense expense : recentExpenses) {
                addTableCell(table, expense.getExpenseDate().format(DATE_FORMATTER));
                addTableCell(table, expense.getDescription() != null ? expense.getDescription() : "");
                addTableCell(table, expense.getEffectiveCategoryName());
                addTableCell(table, "$" + expense.getAmount());
            }

            document.add(table);
        } else {
            Paragraph noData = new Paragraph("No expenses recorded for this period.", SMALL_FONT);
            noData.setSpacingAfter(20);
            document.add(noData);
        }
    }

    /**
     * Add income breakdown section
     */
    private void addIncomeBreakdownSection(Document document, BudgetReportDTO report) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Recent Income", HEADING_FONT);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        if (report.getRecentIncomes() != null && !report.getRecentIncomes().isEmpty()) {
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingAfter(20);
            table.setWidths(new float[]{2, 4, 2});

            // Header
            addTableHeader(table, "Date");
            addTableHeader(table, "Source");
            addTableHeader(table, "Amount");

            // Data rows (top 10 recent incomes)
            List<Income> recentIncomes = report.getRecentIncomes().stream()
                .limit(10)
                .collect(Collectors.toList());

            for (Income income : recentIncomes) {
                addTableCell(table, income.getDate().format(DATE_FORMATTER));
                addTableCell(table, income.getSource() != null ? income.getSource() : "");
                addTableCell(table, "$" + income.getAmount());
            }

            document.add(table);
        } else {
            Paragraph noData = new Paragraph("No income recorded for this period.", SMALL_FONT);
            noData.setSpacingAfter(20);
            document.add(noData);
        }
    }

    /**
     * Add footer with generation timestamp
     */
    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
            "Generated on: " + LocalDate.now().format(DATE_FORMATTER), 
            SMALL_FONT
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
    }

    /**
     * Helper method to add summary row
     */
    private void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    /**
     * Helper method to add table header cell
     */
    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADING_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    /**
     * Helper method to add table data cell
     */
    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * Parse month-year string to YearMonth
     */
    private YearMonth parseMonthYear(String monthYear) {
        if (monthYear == null || monthYear.isEmpty()) {
            return YearMonth.now();
        }
        
        // Try parsing "2026-01" format
        if (monthYear.matches("\\d{4}-\\d{2}")) {
            return YearMonth.parse(monthYear);
        }
        
        // Default to current month
        return YearMonth.now();
    }
}
