#!/usr/bin/env python3
"""
Update frontend imports after restructuring to feature-based architecture.
Handles:
- pages/ → features/*/pages/*Page.jsx
- components/ → features/*/components/* or shared/components/*
- layouts/ → shared/layouts/
- utils/ → shared/utils/
- api/ → core/api/
- contexts/ → core/contexts/
- websocket/ → core/websocket/
- services/ → features/*/services/ or core/services/
"""

import os
import re
from pathlib import Path

# Mapping of old imports to new imports
IMPORT_MAPPINGS = {
    # Auth pages
    r"from ['\"]\.\.?/pages/Login['\"]": "from '@/features/auth/pages/LoginPage'",
    r"from ['\"]\.\.?/pages/Register['\"]": "from '@/features/auth/pages/RegisterPage'",
    r"from ['\"]\.\.?/pages/ForgotPassword['\"]": "from '@/features/auth/pages/ForgotPasswordPage'",
    
    # Dashboard
    r"from ['\"]\.\.?/pages/Dashboard['\"]": "from '@/features/dashboard/pages/DashboardPage'",
    
    # Budget pages
    r"from ['\"]\.\.?/pages/Budget['\"]": "from '@/features/budget/pages/BudgetPage'",
    r"from ['\"]\.\.?/pages/CashFlow['\"]": "from '@/features/budget/pages/CashFlowPage'",
    r"from ['\"]\.\.?/pages/RecurringTransactions['\"]": "from '@/features/budget/pages/RecurringTransactionsPage'",
    
    # Tax pages
    r"from ['\"]\.\.?/pages/Tax['\"]": "from '@/features/tax/pages/TaxPage'",
    
    # Portfolio pages
    r"from ['\"]\.\.?/pages/Portfolio['\"]": "from '@/features/portfolio/pages/PortfolioPage'",
    r"from ['\"]\.\.?/pages/NetWorth['\"]": "from '@/features/portfolio/pages/NetWorthPage'",
    r"from ['\"]\.\.?/pages/PortfolioRebalancing['\"]": "from '@/features/portfolio/pages/PortfolioRebalancingPage'",
    
    # Insurance
    r"from ['\"]\.\.?/pages/Insurance['\"]": "from '@/features/insurance/pages/InsurancePage'",
    
    # Lending
    r"from ['\"]\.\.?/pages/Lending['\"]": "from '@/features/lending/pages/LendingPage'",
    
    # Loans
    r"from ['\"]\.\.?/pages/Loans['\"]": "from '@/features/loans/pages/LoansPage'",
    
    # Banking
    r"from ['\"]\.\.?/pages/Banking['\"]": "from '@/features/banking/pages/BankingPage'",
    
    # Documents
    r"from ['\"]\.\.?/pages/Documents['\"]": "from '@/features/documents/pages/DocumentsPage'",
    
    # Goals
    r"from ['\"]\.\.?/pages/FinancialGoals['\"]": "from '@/features/goals/pages/FinancialGoalsPage'",
    r"from ['\"]\.\.?/pages/GoalDetails['\"]": "from '@/features/goals/pages/GoalDetailsPage'",
    r"from ['\"]\.\.?/pages/RetirementPlanning['\"]": "from '@/features/goals/pages/RetirementPlanningPage'",
    
    # Insights
    r"from ['\"]\.\.?/pages/Insights['\"]": "from '@/features/insights/pages/InsightsPage'",
    
    # Credit Score
    r"from ['\"]\.\.?/pages/CreditScore['\"]": "from '@/features/creditScore/pages/CreditScorePage'",
    
    # Settings
    r"from ['\"]\.\.?/pages/Settings['\"]": "from '@/features/settings/pages/SettingsPage'",
    
    # Admin pages
    r"from ['\"]\.\.?/pages/admin/AdminDashboard['\"]": "from '@/features/admin/pages/AdminDashboardPage'",
    r"from ['\"]\.\.?/pages/admin/AdminUsers['\"]": "from '@/features/admin/pages/AdminUsersPage'",
    r"from ['\"]\.\.?/pages/admin/AdminFeatures['\"]": "from '@/features/admin/pages/AdminFeaturesPage'",
    r"from ['\"]\.\.?/pages/admin/AdminJobs['\"]": "from '@/features/admin/pages/AdminJobsPage'",
    r"from ['\"]\.\.?/pages/admin/AdminActivityLogs['\"]": "from '@/features/admin/pages/AdminActivityLogsPage'",
    r"from ['\"]\.\.?/pages/admin/AdminCriticalLogs['\"]": "from '@/features/admin/pages/AdminCriticalLogsPage'",
    r"from ['\"]\.\.?/pages/admin/AdminExternalServices['\"]": "from '@/features/admin/pages/AdminExternalServicesPage'",
    
    # Payments/UPI
    r"from ['\"]\.\.?/pages/payments/UPIDashboard['\"]": "from '@/features/payments/pages/UPIDashboardPage'",
    r"from ['\"]\.\.?/pages/payments/UPIPayment['\"]": "from '@/features/payments/pages/UPIPaymentPage'",
    
    # Layouts
    r"from ['\"]\.\.?/layouts/": "from '@/shared/layouts/",
    
    # Utils
    r"from ['\"]\.\.?/utils/": "from '@/shared/utils/",
    
    # Api
    r"from ['\"]\.\.?/api/": "from '@/core/api/",
    r"from ['\"]\.\.?/api\.js['\"]": "from '@/core/api.js'",
    
    # Contexts
    r"from ['\"]\.\.?/contexts/": "from '@/core/contexts/",
    
    # Websocket
    r"from ['\"]\.\.?/websocket/": "from '@/core/websocket/",
    
    # Services
    r"from ['\"]\.\.?/services/taxApi['\"]": "from '@/features/tax/services/taxApi'",
    r"from ['\"]\.\.?/services/lendingApi['\"]": "from '@/features/lending/services/lendingApi'",
    r"from ['\"]\.\.?/services/stockPriceWebSocket['\"]": "from '@/core/services/stockPriceWebSocket'",
    r"from ['\"]\.\.?/services/": "from '@/core/services/",
    
    # Budget components
    r"from ['\"]\.\.?/components/BulkActionsToolbar['\"]": "from '@/features/budget/components/BulkActionsToolbar'",
    r"from ['\"]\.\.?/components/CreateTemplateModal['\"]": "from '@/features/budget/components/CreateTemplateModal'",
    r"from ['\"]\.\.?/components/ExportModal['\"]": "from '@/features/budget/components/ExportModal'",
    r"from ['\"]\.\.?/components/RecurringTemplateCard['\"]": "from '@/features/budget/components/RecurringTemplateCard'",
    r"from ['\"]\.\.?/components/TagManagementModal['\"]": "from '@/features/budget/components/TagManagementModal'",
    r"from ['\"]\.\.?/components/TagSelector['\"]": "from '@/features/budget/components/TagSelector'",
    r"from ['\"]\.\.?/components/TransactionModal['\"]": "from '@/features/budget/components/TransactionModal'",
    
    # Tax components
    r"from ['\"]\.\.?/components/CapitalGainsModule['\"]": "from '@/features/tax/components/CapitalGainsModule'",
    r"from ['\"]\.\.?/components/CapitalGainsTracker['\"]": "from '@/features/tax/components/CapitalGainsTracker'",
    r"from ['\"]\.\.?/components/DeductionsTracker['\"]": "from '@/features/tax/components/DeductionsTracker'",
    r"from ['\"]\.\.?/components/ITRFilingAssistant['\"]": "from '@/features/tax/components/ITRFilingAssistant'",
    r"from ['\"]\.\.?/components/IncomeEntryForms['\"]": "from '@/features/tax/components/IncomeEntryForms'",
    r"from ['\"]\.\.?/components/RegimeComparison['\"]": "from '@/features/tax/components/RegimeComparison'",
    r"from ['\"]\.\.?/components/TaxDashboard['\"]": "from '@/features/tax/components/TaxDashboard'",
    r"from ['\"]\.\.?/components/TaxDetailsForm['\"]": "from '@/features/tax/components/TaxDetailsForm'",
    r"from ['\"]\.\.?/components/TaxPlanningTools['\"]": "from '@/features/tax/components/TaxPlanningTools'",
    r"from ['\"]\.\.?/components/TaxProjection['\"]": "from '@/features/tax/components/TaxProjection'",
    r"from ['\"]\.\.?/components/TDSManagement['\"]": "from '@/features/tax/components/TDSManagement'",
    
    # Lending components
    r"from ['\"]\.\.?/components/Lending['\"]": "from '@/features/lending/components/Lending'",
    r"from ['\"]\.\.?/components/AddLendingModal['\"]": "from '@/features/lending/components/AddLendingModal'",
    r"from ['\"]\.\.?/components/AddRepaymentModal['\"]": "from '@/features/lending/components/AddRepaymentModal'",
    r"from ['\"]\.\.?/components/LendingDetailModal['\"]": "from '@/features/lending/components/LendingDetailModal'",
    r"from ['\"]\.\.?/components/LendingForm['\"]": "from '@/features/lending/components/LendingForm'",
    r"from ['\"]\.\.?/components/RepaymentTracker['\"]": "from '@/features/lending/components/RepaymentTracker'",
    
    # Insurance components
    r"from ['\"]\.\.?/components/insurance/ClaimsManagement['\"]": "from '@/features/insurance/components/ClaimsManagement'",
    r"from ['\"]\.\.?/components/insurance/PolicyForm['\"]": "from '@/features/insurance/components/PolicyForm'",
    r"from ['\"]\.\.?/components/insurance/PolicyList['\"]": "from '@/features/insurance/components/PolicyList'",
    r"from ['\"]\.\.?/components/insurance/PremiumPayment['\"]": "from '@/features/insurance/components/PremiumPayment'",
    
    # Documents components
    r"from ['\"]\.\.?/components/DocumentCard['\"]": "from '@/features/documents/components/DocumentCard'",
    
    # Goals components
    r"from ['\"]\.\.?/components/CreateGoalModal['\"]": "from '@/features/goals/components/CreateGoalModal'",
    r"from ['\"]\.\.?/components/GoalCard['\"]": "from '@/features/goals/components/GoalCard'",
    
    # Shared components
    r"from ['\"]\.\.?/components/AiAssistant['\"]": "from '@/shared/components/AiAssistant'",
    r"from ['\"]\.\.?/components/FeatureGate['\"]": "from '@/shared/components/FeatureGate'",
    r"from ['\"]\.\.?/components/TierBadge['\"]": "from '@/shared/components/TierBadge'",
    r"from ['\"]\.\.?/components/TierLimitIndicator['\"]": "from '@/shared/components/TierLimitIndicator'",
    r"from ['\"]\.\.?/components/UpgradePrompt['\"]": "from '@/shared/components/UpgradePrompt'",
}

def update_imports_in_file(file_path):
    """Update imports in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Apply all mappings
        for old_pattern, new_import in IMPORT_MAPPINGS.items():
            content = re.sub(old_pattern, new_import, content)
        
        # Only write if changed
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        
        return False
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False

def main():
    """Update all frontend files"""
    base_dir = Path(__file__).parent.parent.parent / 'frontend' / 'src'
    
    if not base_dir.exists():
        print(f"Directory not found: {base_dir}")
        return 1
    
    updated_count = 0
    total_count = 0
    
    # Find all JSX/JS files
    for ext in ['jsx', 'js', 'tsx', 'ts']:
        for file in base_dir.rglob(f'*.{ext}'):
            # Skip node_modules
            if 'node_modules' in file.parts:
                continue
            
            total_count += 1
            if update_imports_in_file(file):
                updated_count += 1
                print(f"Updated: {file.relative_to(base_dir)}")
    
    print(f"\nProcessed {total_count} files")
    print(f"Updated {updated_count} files")
    
    return 0

if __name__ == '__main__':
    exit(main())
