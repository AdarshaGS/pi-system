package com.budget.service;

import com.budget.data.Expense;
import com.budget.data.ExpenseCategory;
import com.budget.data.Income;
import com.budget.repo.ExpenseRepository;
import com.budget.repo.IncomeRepository;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting budget data to various formats (CSV, Excel)
 */
@Service
public class ExportService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Export expenses to CSV format
     */
    public byte[] exportExpensesToCSV(Long userId, LocalDate startDate, LocalDate endDate, String category) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header
            String[] header = { "Date", "Description", "Category", "Amount", "Notes" };
            csvWriter.writeNext(header);

            // Get filtered expenses
            List<Expense> expenses = getFilteredExpenses(userId, startDate, endDate, category);

            // Write data rows
            for (Expense expense : expenses) {
                String[] row = {
                        expense.getExpenseDate().format(DATE_FORMATTER),
                        expense.getDescription() != null ? expense.getDescription() : "",
                        expense.getEffectiveCategoryName(),
                        expense.getAmount().toString(),
                        expense.getNotes() != null ? expense.getNotes() : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export expenses to CSV", e);
        }
    }

    /**
     * Export incomes to CSV format
     */
    public byte[] exportIncomesToCSV(Long userId, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header
            String[] header = { "Date", "Source", "Amount", "Notes" };
            csvWriter.writeNext(header);

            // Get filtered incomes
            List<Income> incomes = getFilteredIncomes(userId, startDate, endDate);

            // Write data rows
            for (Income income : incomes) {
                String[] row = {
                        income.getDate().format(DATE_FORMATTER),
                        income.getSource() != null ? income.getSource() : "",
                        income.getAmount().toString(),
                        income.getNotes() != null ? income.getNotes() : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export incomes to CSV", e);
        }
    }

    /**
     * Export expenses to Excel format
     */
    public byte[] exportExpensesToExcel(Long userId, LocalDate startDate, LocalDate endDate, String category) {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expenses");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "Date", "Description", "Category", "Amount", "Notes" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Get filtered expenses
            List<Expense> expenses = getFilteredExpenses(userId, startDate, endDate, category);

            // Create data rows
            int rowNum = 1;
            BigDecimal total = BigDecimal.ZERO;
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(expense.getExpenseDate().format(DATE_FORMATTER));
                row.createCell(1).setCellValue(expense.getDescription() != null ? expense.getDescription() : "");
                row.createCell(2).setCellValue(expense.getEffectiveCategoryName());
                row.createCell(3).setCellValue(expense.getAmount().doubleValue());
                row.createCell(4).setCellValue(expense.getNotes() != null ? expense.getNotes() : "");
                total = total.add(expense.getAmount());
            }

            // Add total row
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(2);
            totalLabelCell.setCellValue("TOTAL:");
            totalLabelCell.setCellStyle(headerStyle);
            Cell totalAmountCell = totalRow.createCell(3);
            totalAmountCell.setCellValue(total.doubleValue());
            totalAmountCell.setCellStyle(headerStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export expenses to Excel", e);
        }
    }

    /**
     * Export incomes to Excel format
     */
    public byte[] exportIncomesToExcel(Long userId, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Incomes");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "Date", "Source", "Amount", "Notes" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Get filtered incomes
            List<Income> incomes = getFilteredIncomes(userId, startDate, endDate);

            // Create data rows
            int rowNum = 1;
            BigDecimal total = BigDecimal.ZERO;
            for (Income income : incomes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(income.getDate().format(DATE_FORMATTER));
                row.createCell(1).setCellValue(income.getSource() != null ? income.getSource() : "");
                row.createCell(2).setCellValue(income.getAmount().doubleValue());
                row.createCell(3).setCellValue(income.getNotes() != null ? income.getNotes() : "");
                total = total.add(income.getAmount());
            }

            // Add total row
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(1);
            totalLabelCell.setCellValue("TOTAL:");
            totalLabelCell.setCellStyle(headerStyle);
            Cell totalAmountCell = totalRow.createCell(2);
            totalAmountCell.setCellValue(total.doubleValue());
            totalAmountCell.setCellStyle(headerStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export incomes to Excel", e);
        }
    }

    /**
     * Helper method to get filtered expenses
     */
    private List<Expense> getFilteredExpenses(Long userId, LocalDate startDate, LocalDate endDate, String category) {
        if (startDate != null && endDate != null && category != null && !category.isEmpty()) {
            ExpenseCategory expenseCategory = ExpenseCategory.valueOf(category);
            return expenseRepository.findByUserIdAndCategoryAndExpenseDateBetween(
                    userId, expenseCategory, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate);
        } else if (category != null && !category.isEmpty()) {
            ExpenseCategory expenseCategory = ExpenseCategory.valueOf(category);
            return expenseRepository.findByUserIdAndCategory(userId, expenseCategory);
        } else {
            return expenseRepository.findByUserId(userId);
        }
    }

    /**
     * Helper method to get filtered incomes
     */
    private List<Income> getFilteredIncomes(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        } else {
            return incomeRepository.findByUserId(userId);
        }
    }
}
