import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import Dashboard from './pages/Dashboard';
import Budget from './pages/Budget';
import CashFlow from './pages/CashFlow';
import Portfolio from './pages/Portfolio';
import Loans from './pages/Loans';
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
import { FeatureProvider } from './contexts/FeatureContext';
import FeatureGate from './components/FeatureGate';

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
              <FeatureGate feature="RECURRING_TRANSACTIONS" showDisabledMessage>
                <RecurringTransactions />
              </FeatureGate>
            } />
            <Route path="portfolio" element={
              <FeatureGate feature="PORTFOLIO" showDisabledMessage>
                <Portfolio />
              </FeatureGate>
            } />
            <Route path="loans" element={
              <FeatureGate feature="LOANS" showDisabledMessage>
                <Loans />
              </FeatureGate>
            } />
            <Route path="insights" element={<Insights />} />
            <Route path="settings" element={<Settings />} />
            
            {/* Admin Routes */}
            <Route path="admin" element={<AdminDashboard />} />
            <Route path="admin/users" element={<AdminUsers />} />
            <Route path="admin/utilities" element={<AdminCriticalLogs />} />
            <Route path="admin/external-services" element={<AdminExternalServices />} />
            <Route path="admin/activity-logs" element={<AdminActivityLogs />} />
            <Route path="admin/features" element={<AdminFeatures />} />
          </Route>
        </Routes>
      </Router>
    </FeatureProvider>
  );
}

export default App;
