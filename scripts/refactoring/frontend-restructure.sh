#!/bin/bash
# Phase 2: Frontend Restructuring - Complete Migration
# Moves all pages and components to feature-based structure

set -e

cd "$(dirname "$0")/../../frontend/src"

echo "=== Phase 2: Frontend Restructuring ==="
echo ""

# Tax Feature
echo "Moving Tax Feature..."
[ -f pages/Tax.jsx ] && git mv pages/Tax.jsx features/tax/pages/TaxPage.jsx
[ -f pages/Tax.css ] && git mv pages/Tax.css features/tax/pages/TaxPage.css
[ -f components/CapitalGainsModule.jsx ] && git mv components/CapitalGainsModule.jsx features/tax/components/
[ -f components/CapitalGainsModule.css ] && git mv components/CapitalGainsModule.css features/tax/components/
[ -f components/CapitalGainsTracker.jsx ] && git mv components/CapitalGainsTracker.jsx features/tax/components/
[ -f components/DeductionsTracker.jsx ] && git mv components/DeductionsTracker.jsx features/tax/components/
[ -f components/DeductionsTracker.css ] && git mv components/DeductionsTracker.css features/tax/components/
[ -f components/ITRFilingAssistant.jsx ] && git mv components/ITRFilingAssistant.jsx features/tax/components/
[ -f components/ITRFilingAssistant.css ] && git mv components/ITRFilingAssistant.css features/tax/components/
[ -f components/IncomeEntryForms.jsx ] && git mv components/IncomeEntryForms.jsx features/tax/components/
[ -f components/IncomeEntryForms.css ] && git mv components/IncomeEntryForms.css features/tax/components/
[ -f components/RegimeComparison.jsx ] && git mv components/RegimeComparison.jsx features/tax/components/
[ -f components/TaxDashboard.jsx ] && git mv components/TaxDashboard.jsx features/tax/components/
[ -f components/TaxDashboard.css ] && git mv components/TaxDashboard.css features/tax/components/
[ -f components/TaxDetailsForm.jsx ] && git mv components/TaxDetailsForm.jsx features/tax/components/
[ -f components/TaxPlanningTools.jsx ] && git mv components/TaxPlanningTools.jsx features/tax/components/
[ -f components/TaxPlanningTools.css ] && git mv components/TaxPlanningTools.css features/tax/components/
[ -f components/TaxProjection.jsx ] && git mv components/TaxProjection.jsx features/tax/components/
[ -f components/TDSManagement.jsx ] && git mv components/TDSManagement.jsx features/tax/components/
[ -f components/TDSManagement.css ] && git mv components/TDSManagement.css features/tax/components/
[ -f services/taxApi.js ] && git mv services/taxApi.js features/tax/services/
echo "  ✓ Tax"

# Portfolio Feature
echo "Moving Portfolio Feature..."
[ -f pages/Portfolio.jsx ] && git mv pages/Portfolio.jsx features/portfolio/pages/PortfolioPage.jsx
[ -f pages/NetWorth.jsx ] && git mv pages/NetWorth.jsx features/portfolio/pages/NetWorthPage.jsx
[ -f pages/PortfolioRebalancing.jsx ] && git mv pages/PortfolioRebalancing.jsx features/portfolio/pages/PortfolioRebalancingPage.jsx
[ -f pages/PortfolioRebalancing.css ] && git mv pages/PortfolioRebalancing.css features/portfolio/pages/PortfolioRebalancingPage.css
echo "  ✓ Portfolio"

# Insurance Feature
echo "Moving Insurance Feature..."
[ -f pages/Insurance.jsx ] && git mv pages/Insurance.jsx features/insurance/pages/InsurancePage.jsx
[ -d components/insurance ] && git mv components/insurance/* features/insurance/components/ && rmdir components/insurance
echo "  ✓ Insurance"

# Lending Feature
echo "Moving Lending Feature..."
[ -f pages/Lending.jsx ] && git mv pages/Lending.jsx features/lending/pages/LendingPage.jsx
[ -f components/Lending.jsx ] && git mv components/Lending.jsx features/lending/components/
[ -f components/Lending.css ] && git mv components/Lending.css features/lending/components/
[ -f components/AddLendingModal.jsx ] && git mv components/AddLendingModal.jsx features/lending/components/
[ -f components/AddRepaymentModal.jsx ] && git mv components/AddRepaymentModal.jsx features/lending/components/
[ -f components/LendingDetailModal.jsx ] && git mv components/LendingDetailModal.jsx features/lending/components/
[ -f components/LendingForm.jsx ] && git mv components/LendingForm.jsx features/lending/components/
[ -f components/LendingForm.css ] && git mv components/LendingForm.css features/lending/components/
[ -f components/RepaymentTracker.jsx ] && git mv components/RepaymentTracker.jsx features/lending/components/
[ -f components/RepaymentTracker.css ] && git mv components/RepaymentTracker.css features/lending/components/
[ -f services/lendingApi.js ] && git mv services/lendingApi.js features/lending/services/
echo "  ✓ Lending"

# Loans Feature
echo "Moving Loans Feature..."
[ -f pages/Loans.jsx ] && git mv pages/Loans.jsx features/loans/pages/LoansPage.jsx
[ -f pages/Loans.css ] && git mv pages/Loans.css features/loans/pages/LoansPage.css
echo "  ✓ Loans"

# Banking Feature
echo "Moving Banking Feature..."
[ -f pages/Banking.jsx ] && git mv pages/Banking.jsx features/banking/pages/BankingPage.jsx
[ -f pages/Banking.css ] && git mv pages/Banking.css features/banking/pages/BankingPage.css
echo "  ✓ Banking"

# Payments/UPI Feature
echo "Moving Payments Feature..."
[ -d pages/payments ] && find pages/payments -name "*.jsx" -o -name "*.css" | while read f; do
    basename_file=$(basename "$f")
    git mv "$f" "features/payments/pages/${basename_file/UPI/UPI}"
done
[ -d pages/payments ] && rmdir pages/payments 2>/dev/null || true
echo "  ✓ Payments/UPI"

# Documents Feature
echo "Moving Documents Feature..."
[ -f pages/Documents.jsx ] && git mv pages/Documents.jsx features/documents/pages/DocumentsPage.jsx
[ -f pages/Documents.css ] && git mv pages/Documents.css features/documents/pages/DocumentsPage.css
[ -f components/DocumentCard.jsx ] && git mv components/DocumentCard.jsx features/documents/components/
[ -f components/DocumentCard.css ] && git mv components/DocumentCard.css features/documents/components/
echo "  ✓ Documents"

# Goals Feature
echo "Moving Goals Feature..."
[ -f pages/FinancialGoals.jsx ] && git mv pages/FinancialGoals.jsx features/goals/pages/FinancialGoalsPage.jsx
[ -f pages/FinancialGoals.css ] && git mv pages/FinancialGoals.css features/goals/pages/FinancialGoalsPage.css
[ -f pages/GoalDetails.jsx ] && git mv pages/GoalDetails.jsx features/goals/pages/GoalDetailsPage.jsx
[ -f pages/GoalDetails.css ] && git mv pages/GoalDetails.css features/goals/pages/GoalDetailsPage.css
[ -f pages/RetirementPlanning.jsx ] && git mv pages/RetirementPlanning.jsx features/goals/pages/RetirementPlanningPage.jsx
[ -f pages/RetirementPlanning.css ] && git mv pages/RetirementPlanning.css features/goals/pages/RetirementPlanningPage.css
[ -f components/CreateGoalModal.jsx ] && git mv components/CreateGoalModal.jsx features/goals/components/
[ -f components/CreateGoalModal.css ] && git mv components/CreateGoalModal.css features/goals/components/
[ -f components/GoalCard.jsx ] && git mv components/GoalCard.jsx features/goals/components/
[ -f components/GoalCard.css ] && git mv components/GoalCard.css features/goals/components/
echo "  ✓ Goals"

# Insights Feature
echo "Moving Insights Feature..."
[ -f pages/Insights.jsx ] && git mv pages/Insights.jsx features/insights/pages/InsightsPage.jsx
echo "  ✓ Insights"

# Credit Score Feature
echo "Moving Credit Score Feature..."
[ -f pages/CreditScore.jsx ] && git mv pages/CreditScore.jsx features/creditScore/pages/CreditScorePage.jsx
[ -f pages/CreditScore.css ] && git mv pages/CreditScore.css features/creditScore/pages/CreditScorePage.css
echo "  ✓ CreditScore"

# Settings Feature
echo "Moving Settings Feature..."
[ -f pages/Settings.jsx ] && git mv pages/Settings.jsx features/settings/pages/SettingsPage.jsx
echo "  ✓ Settings"

# Admin Feature
echo "Moving Admin Feature..."
[ -d pages/admin ] && find pages/admin -name "*.jsx" | while read f; do
    basename_file=$(basename "$f")
    git mv "$f" "features/admin/pages/${basename_file/Admin/Admin}"
done
[ -d pages/admin ] && rmdir pages/admin 2>/dev/null || true
echo "  ✓ Admin"

# Shared Components
echo "Moving Shared Components..."
[ -f components/AiAssistant.jsx ] && git mv components/AiAssistant.jsx shared/components/
[ -f components/AiAssistant.css ] && git mv components/AiAssistant.css shared/components/
[ -f components/FeatureGate.jsx ] && git mv components/FeatureGate.jsx shared/components/
[ -f components/TierBadge.jsx ] && git mv components/TierBadge.jsx shared/components/
[ -f components/TierBadge.css ] && git mv components/TierBadge.css shared/components/
[ -f components/TierLimitIndicator.jsx ] && git mv components/TierLimitIndicator.jsx shared/components/
[ -f components/TierLimitIndicator.css ] && git mv components/TierLimitIndicator.css shared/components/
[ -f components/UpgradePrompt.jsx ] && git mv components/UpgradePrompt.jsx shared/components/
[ -f components/UpgradePrompt.css ] && git mv components/UpgradePrompt.css shared/components/
echo "  ✓ Shared Components"

# Shared Layouts
echo "Moving Shared Infrastructure..."
[ -d layouts ] && git mv layouts shared/
[ -d utils ] && git mv utils shared/
echo "  ✓ Layouts & Utils"

# Core Infrastructure
echo "Moving Core Infrastructure..."
[ -d api ] && git mv api core/
[ -d contexts ] && git mv contexts core/
[ -d websocket ] && git mv websocket core/
[ -d services ] && [ "$(ls -A services)" ] && git mv services core/ || ([ -d services ] && rmdir services)
echo "  ✓ Core Infrastructure"

# Cleanup empty directories
echo ""
echo "Cleaning up..."
rmdir pages 2>/dev/null || true
rmdir components 2>/dev/null || true

echo ""
echo "=== Frontend Restructuring Complete ==="
echo "✓ All features organized"
echo "✓ Shared components co-located"
echo "✓ Core infrastructure separated"
echo ""
echo "Next: Update imports in all files"
