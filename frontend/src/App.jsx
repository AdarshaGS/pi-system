import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import Dashboard from './pages/Dashboard';
import Budget from './pages/Budget';
import CashFlow from './pages/CashFlow';
import Portfolio from './pages/Portfolio';
import Banking from './pages/Banking';
import Loans from './pages/Loans';
import Insurance from './pages/Insurance';
import Insights from './pages/Insights';
import Settings from './pages/Settings';
import RecurringTransactions from './pages/RecurringTransactions';
import Layout from './layouts/Layout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminUsers from './pages/admin/AdminUsers';
import AdminCriticalLogs from './pages/admin/AdminCriticalLogs';
import AdminExternalServices from './pages/admin/AdminExternalServices';
import AdminActivityLogs from './pages/admin/AdminActivityLogs';
import AdminFeatures from './pages/admin/AdminFeatures';
import AdminJobs from './pages/admin/AdminJobs';
import { FeatureProvider } from './contexts/FeatureContext';
import { TierProvider } from './contexts/TierContext';
import FeatureGate from './components/FeatureGate';
import TaxDashboard from './components/TaxDashboard';
import IncomeEntryForms from './components/IncomeEntryForms';
import DeductionsTracker from './components/DeductionsTracker';
import CapitalGainsModule from './components/CapitalGainsModule';
import TDSManagement from './components/TDSManagement';
import TaxPlanningTools from './components/TaxPlanningTools';
import ITRFilingAssistant from './components/ITRFilingAssistant';
import Lending from './pages/Lending';

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
      </Router>
      </TierProvider>
    </FeatureProvider>
  );
}

export default App;
