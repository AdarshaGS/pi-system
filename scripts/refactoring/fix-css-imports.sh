#!/bin/bash
# Fix all CSS imports in renamed page files

cd "$(dirname "$0")/../../frontend/src/features"

# Function to fix CSS import in a JSX file
fix_css_import() {
    local file=$1
    local old_name=$2
    local new_name=$3
    
    if [ -f "$file" ] && grep -q "import.*$old_name\.css" "$file"; then
        sed -i '' "s|['\"]\./$old_name\.css['\"]|'./$new_name.css'|g" "$file"
        echo "Fixed CSS import in $file"
    fi
}

# Fix each page
fix_css_import "budget/pages/CashFlowPage.jsx" "CashFlow" "CashFlowPage"
fix_css_import "budget/pages/RecurringTransactionsPage.jsx" "RecurringTransactions" "RecurringTransactionsPage"
fix_css_import "tax/pages/TaxPage.jsx" "Tax" "TaxPage"
fix_css_import "loans/pages/LoansPage.jsx" "Loans" "LoansPage"
fix_css_import "banking/pages/BankingPage.jsx" "Banking" "BankingPage"
fix_css_import "creditScore/pages/CreditScorePage.jsx" "CreditScore" "CreditScorePage"
fix_css_import "documents/pages/DocumentsPage.jsx" "Documents" "DocumentsPage"
fix_css_import "goals/pages/FinancialGoalsPage.jsx" "FinancialGoals" "FinancialGoalsPage"
fix_css_import "goals/pages/GoalDetailsPage.jsx" "GoalDetails" "GoalDetailsPage"
fix_css_import "goals/pages/RetirementPlanningPage.jsx" "RetirementPlanning" "RetirementPlanningPage"
fix_css_import "portfolio/pages/PortfolioRebalancingPage.jsx" "PortfolioRebalancing" "PortfolioRebalancingPage"
fix_css_import "payments/pages/UPIDashboardPage.jsx" "UPIDashboard" "UPIDashboardPage"

echo ""
echo "✓ All CSS imports fixed"
