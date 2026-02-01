-- Add missing columns to tax_details table to match Tax entity

ALTER TABLE tax_details 
    ADD COLUMN gross_salary DECIMAL(10,2),
    ADD COLUMN standard_deduction DECIMAL(10,2),
    ADD COLUMN section_80c_deductions DECIMAL(10,2),
    ADD COLUMN section_80d_deductions DECIMAL(10,2),
    ADD COLUMN other_deductions DECIMAL(10,2),
    ADD COLUMN house_property_income DECIMAL(10,2),
    ADD COLUMN business_income DECIMAL(10,2),
    ADD COLUMN other_income DECIMAL(10,2),
    ADD COLUMN tds_deducted DECIMAL(10,2),
    ADD COLUMN advance_tax_paid DECIMAL(10,2),
    ADD COLUMN self_assessment_tax DECIMAL(10,2),
    ADD COLUMN selected_regime VARCHAR(50),
    ADD COLUMN updated_date DATE;
