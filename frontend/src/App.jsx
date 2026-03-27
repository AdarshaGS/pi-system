import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from '@/features/auth/pages/LoginPage';
import Register from '@/features/auth/pages/RegisterPage';
import ForgotPassword from '@/features/auth/pages/ForgotPasswordPage';
import Dashboard from '@/features/dashboard/pages/DashboardPage';
import Budget from '@/features/budget/pages/BudgetPage';
import CashFlow from '@/features/budget/pages/CashFlowPage';
import Portfolio from '@/features/portfolio/pages/PortfolioPage';
import Banking from '@/features/banking/pages/BankingPage';
import Loans from '@/features/loans/pages/LoansPage';
import Insurance from '@/features/insurance/pages/InsurancePage';
import Insights from '@/features/insights/pages/InsightsPage';
import Settings from '@/features/settings/pages/SettingsPage';
import RecurringTransactions from '@/features/budget/pages/RecurringTransactionsPage';
import Layout from '@/shared/layouts/Layout';
import AdminDashboard from '@/features/admin/pages/AdminDashboardPage';
import AdminUsers from '@/features/admin/pages/AdminUsersPage';
import AdminCriticalLogs from '@/features/admin/pages/AdminCriticalLogsPage';
import AdminExternalServices from '@/features/admin/pages/AdminExternalServicesPage';
import AdminActivityLogs from '@/features/admin/pages/AdminActivityLogsPage';
import AdminFeatures from '@/features/admin/pages/AdminFeaturesPage';
import AdminJobs from '@/features/admin/pages/AdminJobsPage';
import { FeatureProvider } from '@/core/contexts/FeatureContext';
import { TierProvider } from '@/core/contexts/TierContext';
import FeatureGate from '@/shared/components/FeatureGate';
import TaxDashboard from '@/features/tax/components/TaxDashboard';
import IncomeEntryForms from '@/features/tax/components/IncomeEntryForms';
import DeductionsTracker from '@/features/tax/components/DeductionsTracker';
import CapitalGainsModule from '@/features/tax/components/CapitalGainsModule';
import TDSManagement from '@/features/tax/components/TDSManagement';
import TaxPlanningTools from '@/features/tax/components/TaxPlanningTools';
import ITRFilingAssistant from '@/features/tax/components/ITRFilingAssistant';
import Lending from '@/features/lending/pages/LendingPage';
import AiAssistant from '@/shared/components/AiAssistant';
import FinancialGoals from '@/features/goals/pages/FinancialGoalsPage';
import GoalDetails from '@/features/goals/pages/GoalDetailsPage';

const ProtectedRoute = ({ children }) => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (!user || !user.token) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

function App() {
  return (
    <FeatureProvider>
      <TierProvider>
        <Router>
          <Routes>
            {/* Auth Routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />

            {/* Protected Routes */}
            <Route path="/" element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="budget" element={
                <FeatureGate feature="BUDGET_MODULE" showDisabledMessage>
                  <Budget />
                </FeatureGate>
              } />
              <Route path="cashflow" element={
                <FeatureGate feature="BUDGET_MODULE" showDisabledMessage>
                  <CashFlow />
                </FeatureGate>
              } />
              <Route path="recurring" element={
                <FeatureGate feature="BUDGET_MODULE" showDisabledMessage>
                  <RecurringTransactions />
                </FeatureGate>
              } />
              <Route path="portfolio" element={
                <FeatureGate feature="INVESTMENTS_MODULE" showDisabledMessage>
                  <Portfolio />
                </FeatureGate>
              } />
              <Route path="financial-goals" element={
                <FeatureGate feature="INVESTMENTS_MODULE" showDisabledMessage>
                  <FinancialGoals />
                </FeatureGate>
              } />
              <Route path="goals/:goalId" element={
                <FeatureGate feature="INVESTMENTS_MODULE" showDisabledMessage>
                  <GoalDetails />
                </FeatureGate>
              } />
              <Route path="banking" element={
                <FeatureGate feature="BANKING_MODULE" showDisabledMessage>
                  <Banking />
                </FeatureGate>
              } />
              <Route path="loans" element={
                <FeatureGate feature="BANKING_MODULE" showDisabledMessage>
                  <Loans />
                </FeatureGate>
              } />
              <Route path="insurance" element={
                <FeatureGate feature="INSURANCE_MODULE" showDisabledMessage>
                  <Insurance />
                </FeatureGate>
              } />
              <Route path="insights" element={<Insights />} />
              <Route path="settings" element={<Settings />} />

              {/* Tax Routes
            <Route path="tax" element={
              <FeatureGate feature="TAX_MANAGEMENT" showDisabledMessage>
                <TaxDashboard />
              </FeatureGate>
            } />
            <Route path="tax/income" element={
              <FeatureGate feature="TAX_MANAGEMENT" showDisabledMessage>
                <IncomeEntryForms />
              </FeatureGate>
            } />
            <Route path="tax/deductions" element={
              <FeatureGate feature="TAX_MANAGEMENT" showDisabledMessage>
                <DeductionsTracker />
              </FeatureGate>
            } />
            <Route path="tax/capital-gains" element={
              <FeatureGate feature="TAX_MANAGEMENT" showDisabledMessage>
                <CapitalGainsModule />
              </FeatureGate>
            } />
            <Route path="tax/tds" element={
              <FeatureGate feature="TAX_MANAGEMENT" showDisabledMessage>
                <TDSManagement />
              </FeatureGate>
            } />
            <Route path="tax/planning" element={
              <FeatureGate feature="TAX_MANAGEMENT" showDisabledMessage>
                <TaxPlanningTools />
              </FeatureGate>
            } />
            <Route path="tax/itr" element={
              <FeatureGate feature="TAX_MANAGEMENT" showDisabledMessage>
                <ITRFilingAssistant />
              </FeatureGate>
            } /> */}

              <Route path="tax" element={<TaxDashboard />} />
              <Route path="tax/income" element={<IncomeEntryForms />} />
              <Route path="tax/deductions" element={<DeductionsTracker />} />
              <Route path="tax/capital-gains" element={<CapitalGainsModule />} />
              <Route path="tax/tds" element={<TDSManagement />} />
              <Route path="tax/planning" element={<TaxPlanningTools />} />
              <Route path="tax/itr" element={<ITRFilingAssistant />} />

              {/* Lending Routes */}
              <Route path="lending" element={<Lending />} />

              {/* Admin Routes */}
              <Route path="admin" element={<AdminDashboard />} />
              <Route path="admin/users" element={<AdminUsers />} />
              <Route path="admin/utilities" element={<AdminCriticalLogs />} />
              <Route path="admin/external-services" element={<AdminExternalServices />} />
              <Route path="admin/activity-logs" element={<AdminActivityLogs />} />
              <Route path="admin/features" element={<AdminFeatures />} />
              <Route path="admin/jobs" element={<AdminJobs />} />
              <Route path="admin/jobs" element={<AdminJobs />} />
            </Route>
          </Routes>
          <AiAssistant />
        </Router>
      </TierProvider>
    </FeatureProvider>
  );
}

export default App;
