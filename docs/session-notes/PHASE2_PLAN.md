# Phase 2: Frontend Restructuring Plan

## Current Structure Analysis

### Frontend Metrics
- **Total Pages**: ~31 JSX files
- **Total Components**: ~36 JSX files
- **Services**: 3 API services
- **Current Organization**: Pages + Components (flat structure)

### Current Structure
```
frontend/src/
├── pages/
│   ├── Login.jsx, Register.jsx, ForgotPassword.jsx
│   ├── Dashboard.jsx
│   ├── Budget.jsx
│   ├── Tax.jsx, Tax.css
│   ├── Portfolio.jsx
│   ├── NetWorth.jsx
│   ├── Banking.jsx, Banking.css
│   ├── CashFlow.jsx, CashFlow.css
│   ├── CreditScore.jsx, CreditScore.css
│   ├── Documents.jsx, Documents.css
│   ├── FinancialGoals.jsx, FinancialGoals.css
│   ├── GoalDetails.jsx, GoalDetails.css
│   ├── Insights.jsx
│   ├── Insurance.jsx
│   ├── Lending.jsx
│   ├── Loans.jsx, Loans.css
│   ├── PortfolioRebalancing.jsx, PortfolioRebalancing.css
│   ├── RecurringTransactions.jsx, RecurringTransactions.css
│   ├── RetirementPlanning.jsx, RetirementPlanning.css
│   ├── Settings.jsx
│   ├── admin/
│   │   ├── AdminDashboard.jsx
│   │   ├── AdminUsers.jsx
│   │   ├── AdminFeatures.jsx
│   │   ├── AdminJobs.jsx
│   │   ├── AdminActivityLogs.jsx
│   │   ├── AdminCriticalLogs.jsx
│   │   └── AdminExternalServices.jsx
│   └── payments/
│       ├── UPIDashboard.jsx, UPIDashboard.css
│       └── UPIPayment.jsx
│
├── components/
│   ├── AiAssistant.jsx, AiAssistant.css
│   ├── BulkActionsToolbar.jsx, BulkActionsToolbar.css
│   ├── CapitalGainsModule.jsx, CapitalGainsModule.css
│   ├── CapitalGainsTracker.jsx
│   ├── CreateGoalModal.jsx, CreateGoalModal.css
│   ├── CreateTemplateModal.jsx
│   ├── DeductionsTracker.jsx, DeductionsTracker.css
│   ├── DocumentCard.jsx, DocumentCard.css
│   ├── ExportModal.jsx, ExportModal.css
│   ├── FeatureGate.jsx
│   ├── GoalCard.jsx, GoalCard.css
│   ├── ITRFilingAssistant.jsx, ITRFilingAssistant.css
│   ├── IncomeEntryForms.jsx, IncomeEntryForms.css
│   ├── Lending.jsx, Lending.css
│   ├── AddLendingModal.jsx
│   ├── LendingDetailModal.jsx
│   ├── LendingForm.jsx, LendingForm.css
│   ├── RegimeComparison.jsx
│   ├── RepaymentTracker.jsx, RepaymentTracker.css
│   ├── AddRepaymentModal.jsx
│   ├── RecurringTemplateCard.jsx, RecurringTemplateCard.css
│   ├── TagSelector.jsx, TagSelector.css
│   ├── TagManagementModal.jsx, TagManagementModal.css
│   ├── TaxDashboard.jsx, TaxDashboard.css
│   ├── TaxDetailsForm.jsx
│   ├── TaxPlanningTools.jsx, TaxPlanningTools.css
│   ├── TaxProjection.jsx
│   ├── TDSManagement.jsx, TDSManagement.css
│   ├── TierBadge.jsx, TierBadge.css
│   ├── TierLimitIndicator.jsx, TierLimitIndicator.css
│   ├── TransactionModal.jsx, TransactionModal.css
│   ├── UpgradePrompt.jsx, UpgradePrompt.css
│   └── insurance/
│       ├── ClaimsManagement.jsx
│       ├── PolicyForm.jsx
│       ├── PolicyList.jsx
│       └── PremiumPayment.jsx
│
├── services/
│   ├── taxApi.js
│   ├── lendingApi.js
│   └── stockPriceWebSocket.js
│
├── contexts/
├── layouts/
├── api/
├── utils/
└── websocket/
```

## Problems with Current Structure

1. **Poor Feature Cohesion**: Budget components scattered between pages/ and components/
2. **Difficult Navigation**: Must search multiple directories to find all code for a feature
3. **Unclear Ownership**: Hard to tell which components belong to which features
4. **No Co-location**: Pages, components, services, and styles for same feature are separated
5. **Flat Scalability Issues**: Adding new features clutters existing directories

## Target Structure (Feature-Based)

```
frontend/src/
├── features/
│   ├── auth/
│   │   ├── pages/
│   │   │   ├── LoginPage.jsx
│   │   │   ├── RegisterPage.jsx
│   │   │   └── ForgotPasswordPage.jsx
│   │   ├── components/
│   │   ├── hooks/
│   │   └── index.js (re-exports)
│   │
│   ├── dashboard/
│   │   ├── pages/
│   │   │   └── DashboardPage.jsx
│   │   ├── components/
│   │   │   ├── OverviewCard.jsx
│   │   │   └── QuickActions.jsx
│   │   └── index.js
│   │
│   ├── budget/
│   │   ├── pages/
│   │   │   ├── BudgetPage.jsx
│   │   │   ├── CashFlowPage.jsx
│   │   │   └── RecurringTransactionsPage.jsx
│   │   ├── components/
│   │   │   ├── BulkActionsToolbar.jsx
│   │   │   ├── CreateTemplateModal.jsx
│   │   │   ├── ExportModal.jsx
│   │   │   ├── RecurringTemplateCard.jsx
│   │   │   ├── TagManagementModal.jsx
│   │   │   ├── TagSelector.jsx
│   │   │   └── TransactionModal.jsx
│   │   ├── services/
│   │   │   └── budgetApi.js
│   │   └── index.js
│   │
│   ├── tax/
│   │   ├── pages/
│   │   │   └── TaxPage.jsx
│   │   ├── components/
│   │   │   ├── CapitalGainsModule.jsx
│   │   │   ├── CapitalGainsTracker.jsx
│   │   │   ├── DeductionsTracker.jsx
│   │   │   ├── ITRFilingAssistant.jsx
│   │   │   ├── IncomeEntryForms.jsx
│   │   │   ├── RegimeComparison.jsx
│   │   │   ├── TaxDashboard.jsx
│   │   │   ├── TaxDetailsForm.jsx
│   │   │   ├── TaxPlanningTools.jsx
│   │   │   ├── TaxProjection.jsx
│   │   │   └── TDSManagement.jsx
│   │   ├── services/
│   │   │   └── taxApi.js
│   │   └── index.js
│   │
│   ├── portfolio/
│   │   ├── pages/
│   │   │   ├── PortfolioPage.jsx
│   │   │   ├── NetWorthPage.jsx
│   │   │   └── PortfolioRebalancingPage.jsx
│   │   ├── components/
│   │   ├── services/
│   │   └── index.js
│   │
│   ├── insurance/
│   │   ├── pages/
│   │   │   └── InsurancePage.jsx
│   │   ├── components/
│   │   │   ├── ClaimsManagement.jsx
│   │   │   ├── PolicyForm.jsx
│   │   │   ├── PolicyList.jsx
│   │   │   └── PremiumPayment.jsx
│   │   └── index.js
│   │
│   ├── lending/
│   │   ├── pages/
│   │   │   └── LendingPage.jsx
│   │   ├── components/
│   │   │   ├── AddLendingModal.jsx
│   │   │   ├── AddRepaymentModal.jsx
│   │   │   ├── Lending.jsx
│   │   │   ├── LendingDetailModal.jsx
│   │   │   ├── LendingForm.jsx
│   │   │   └── RepaymentTracker.jsx
│   │   ├── services/
│   │   │   └── lendingApi.js
│   │   └── index.js
│   │
│   ├── loans/
│   │   ├── pages/
│   │   │   └── LoansPage.jsx
│   │   └── index.js
│   │
│   ├── banking/
│   │   ├── pages/
│   │   │   └── BankingPage.jsx
│   │   └── index.js
│   │
│   ├── payments/
│   │   ├── pages/
│   │   │   ├── UPIDashboardPage.jsx
│   │   │   └── UPIPaymentPage.jsx
│   │   └── index.js
│   │
│   ├── documents/
│   │   ├── pages/
│   │   │   └── DocumentsPage.jsx
│   │   ├── components/
│   │   │   └── DocumentCard.jsx
│   │   └── index.js
│   │
│   ├── goals/
│   │   ├── pages/
│   │   │   ├── FinancialGoalsPage.jsx
│   │   │   ├── GoalDetailsPage.jsx
│   │   │   └── RetirementPlanningPage.jsx
│   │   ├── components/
│   │   │   ├── CreateGoalModal.jsx
│   │   │   └── GoalCard.jsx
│   │   └── index.js
│   │
│   ├── insights/
│   │   ├── pages/
│   │   │   └── InsightsPage.jsx
│   │   └── index.js
│   │
│   ├── creditScore/
│   │   ├── pages/
│   │   │   └── CreditScorePage.jsx
│   │   └── index.js
│   │
│   ├── settings/
│   │   ├── pages/
│   │   │   └── SettingsPage.jsx
│   │   └── index.js
│   │
│   └── admin/
│       ├── pages/
│       │   ├── AdminDashboardPage.jsx
│       │   ├── AdminUsersPage.jsx
│       │   ├── AdminFeaturesPage.jsx
│       │   ├── AdminJobsPage.jsx
│       │   ├── AdminActivityLogsPage.jsx
│       │   ├── AdminCriticalLogsPage.jsx
│       │   └── AdminExternalServicesPage.jsx
│       └── index.js
│
├── shared/
│   ├── components/
│   │   ├── AiAssistant.jsx
│   │   ├── FeatureGate.jsx
│   │   ├── TierBadge.jsx
│   │   ├── TierLimitIndicator.jsx
│   │   └── UpgradePrompt.jsx
│   ├── layouts/ (from current layouts/)
│   └── utils/ (from current utils/)
│
├── core/
│   ├── api/ (from current api/)
│   ├── services/ (global services)
│   ├── contexts/ (from current contexts/)
│   └── websocket/ (from current websocket/)
│
├── App.jsx
├── main.jsx
└── index.css
```

## Migration Strategy

### Phase 2.1: Create Feature Structure (15 min)
1. Create `features/` directory with all feature subdirectories
2. Create `shared/` directory for common components
3. Create `core/` directory for infrastructure

### Phase 2.2: Move Auth Pages (10 min)
- Move Login.jsx, Register.jsx, ForgotPassword.jsx to features/auth/pages/

### Phase 2.3: Move Budget Feature (20 min)
- Move Budget.jsx → features/budget/pages/BudgetPage.jsx
- Move CashFlow.jsx → features/budget/pages/CashFlowPage.jsx
- Move RecurringTransactions.jsx → features/budget/pages/RecurringTransactionsPage.jsx
- Move related components to features/budget/components/

### Phase 2.4: Move Tax Feature (20 min)
- Move Tax.jsx → features/tax/pages/TaxPage.jsx
- Move all tax components to features/tax/components/
- Move taxApi.js → features/tax/services/taxApi.js

### Phase 2.5: Move Remaining Features (45 min)
- Portfolio (portfolio, networth, rebalancing)
- Insurance (page + components)
- Lending (page + components + API)
- Loans, Banking, Payments/UPI
- Documents, Goals, Insights, CreditScore, Settings
- Admin (7 pages)

### Phase 2.6: Move Shared Components (15 min)
- AiAssistant, FeatureGate, TierBadge, etc. → shared/components/
- layouts/ → shared/layouts/
- utils/ → shared/utils/

### Phase 2.7: Move Core Infrastructure (10 min)
- api/ → core/api/
- contexts/ → core/contexts/
- websocket/ → core/websocket/
- services/ → core/services/

### Phase 2.8: Update Imports (30 min)
- Update all imports across entire frontend
- Update routing in App.jsx
- Update any absolute path imports

### Phase 2.9: Verification (15 min)
- Run npm run build
- Fix any remaining import errors
- Test that app starts correctly

## Estimated Time
**Total: 3-4 hours**

## Benefits

1. **Clear Feature Boundaries**: All budget code in features/budget/
2. **Better Scalability**: Each feature is self-contained
3. **Easier Testing**: Can test entire feature in isolation
4. **Improved Navigation**: Developers know exactly where to find code
5. **Co-location**: Pages, components, services for same feature are together
6. **Better Code Splitting**: Can lazy-load entire features

## Next Steps

Execute the migration following the phases above.
