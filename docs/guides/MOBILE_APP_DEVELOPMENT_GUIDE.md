# Mobile App Development Guide - Financial Management System

**Product**: Pi System - Complete Financial Management Platform  
**Date**: January 31, 2026  
**Platform**: iOS + Android (React Native / Flutter recommended)  
**Backend**: Spring Boot REST API (already built)

---

## 📱 Table of Contents

1. [Product Overview](#product-overview)
2. [Technology Stack Recommendation](#technology-stack-recommendation)
3. [Architecture & Design Patterns](#architecture--design-patterns)
4. [Feature Prioritization (MVP → Full)](#feature-prioritization-mvp--full)
5. [Screen-by-Screen Design Guide](#screen-by-screen-design-guide)
6. [API Integration Strategy](#api-integration-strategy)
7. [Design System & UI/UX Principles](#design-system--uiux-principles)
8. [Security & Authentication](#security--authentication)
9. [Implementation Phases](#implementation-phases)
10. [Testing Strategy](#testing-strategy)
11. [Performance Optimization](#performance-optimization)
12. [Deployment Checklist](#deployment-checklist)

---

## 1. Product Overview

### What We're Building

A **comprehensive financial management mobile app** for Indian users to track:
- 💰 **Investments**: Stocks, Mutual Funds, ETFs with real-time prices
- 📊 **Portfolio**: Net worth, XIRR calculations, diversification analysis
- 💵 **Savings**: Fixed Deposits, Recurring Deposits, Savings Accounts
- 📈 **Budget**: Income tracking, expense management, recurring transactions
- 🏦 **Loans**: Tracking borrowed and lent money
- 💳 **Account Aggregation**: Auto-sync via Sahamati AA framework
- 🛡️ **Insurance**: Life, health, vehicle insurance tracking
- 📑 **Tax**: Capital gains, deductions, tax planning

### Key Differentiators
- ✅ Real-time stock prices (Alpha Vantage + Indian APIs)
- ✅ Account Aggregation integration (automated data sync)
- ✅ Complete portfolio analytics with XIRR
- ✅ Indian market focus (NSE/BSE, INR currency)
- ✅ Budget with recurring transaction automation
- ✅ Comprehensive audit trail

---

## 2. Technology Stack Recommendation

### Option A: React Native (Recommended)

**Pros**:
- Single codebase for iOS + Android
- Large community, mature ecosystem
- Existing React frontend team can contribute
- Hot reload for faster development
- Near-native performance
- Strong library support (charts, authentication, forms)

**Cons**:
- Slightly larger app size
- Some performance overhead for complex animations

**Tech Stack**:
```
Framework: React Native 0.73+
Language: TypeScript
State Management: Redux Toolkit + RTK Query
Navigation: React Navigation 6
UI Library: React Native Paper / NativeBase
Charts: Victory Native / Recharts Native
Storage: AsyncStorage + Redux Persist
API Client: Axios + RTK Query
Authentication: JWT with Secure Storage
Testing: Jest + React Native Testing Library
```

### Option B: Flutter

**Pros**:
- Excellent performance (compiled to native)
- Beautiful default UI (Material Design)
- Fast development with Hot Reload
- Growing ecosystem

**Cons**:
- Different language (Dart) - learning curve
- Smaller community than React Native
- Larger app size

**Tech Stack**:
```
Framework: Flutter 3.16+
Language: Dart
State Management: Bloc / Riverpod
Navigation: GoRouter
UI: Material 3 / Custom Design System
Charts: fl_chart
Storage: Hive / SharedPreferences
API Client: Dio + Freezed
Authentication: JWT with FlutterSecureStorage
Testing: flutter_test + integration_test
```

### Recommendation: **React Native**

**Why?**
- Your frontend is already React-based (easier team transition)
- Faster time to market with existing React knowledge
- Better integration with your React web app (share types, utilities)
- Larger talent pool for React developers in India

---

## 3. Architecture & Design Patterns

### Overall Architecture

```
┌─────────────────────────────────────────────┐
│           Mobile App (React Native)         │
├─────────────────────────────────────────────┤
│  Presentation Layer                         │
│  - Screens (Login, Dashboard, Portfolio...) │
│  - Components (Cards, Charts, Forms...)     │
│  - Navigation (Stack, Tab, Drawer)          │
├─────────────────────────────────────────────┤
│  Business Logic Layer                       │
│  - Redux Store (Global State)               │
│  - RTK Query (API Caching)                  │
│  - Custom Hooks (useAuth, usePortfolio...)  │
│  - Utilities (formatters, validators...)    │
├─────────────────────────────────────────────┤
│  Data Layer                                  │
│  - API Service (Axios instances)            │
│  - Local Storage (AsyncStorage)             │
│  - Secure Storage (JWT tokens)              │
│  - Cache Management (RTK Query)             │
└─────────────────────────────────────────────┘
                    ↕ HTTP/REST
┌─────────────────────────────────────────────┐
│      Backend (Spring Boot - Already Built)  │
│  - /api/auth/** (Login, Register, Refresh)  │
│  - /api/investments/** (Stocks, MF, ETF)    │
│  - /api/portfolio/** (Holdings, Analytics)  │
│  - /api/budget/** (Expenses, Income)        │
│  - /api/savings/** (FD, RD, Savings)        │
│  - /api/aa/** (Account Aggregation)         │
└─────────────────────────────────────────────┘
```

### Design Patterns

**1. Feature-Based Folder Structure**
```
src/
├── features/
│   ├── auth/
│   │   ├── screens/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── slice.ts (Redux)
│   │   └── api.ts (RTK Query)
│   ├── portfolio/
│   ├── stocks/
│   ├── budget/
│   └── profile/
├── shared/
│   ├── components/ (Button, Card, Input...)
│   ├── hooks/ (useAsync, useDebounce...)
│   ├── utils/ (formatCurrency, validators...)
│   ├── constants/
│   └── types/
├── navigation/
├── services/ (API client, storage...)
└── store/ (Redux store config)
```

**2. Component Patterns**
- **Container/Presentational**: Separate logic from UI
- **Custom Hooks**: Reusable logic (useStockPrice, usePortfolio)
- **Compound Components**: Complex UI (Tabs, Accordions)
- **Render Props**: Flexible composition

**3. State Management**
- **Global State**: Redux Toolkit (user, auth, theme)
- **Server State**: RTK Query (API data with caching)
- **Local State**: useState (form inputs, modals)
- **Form State**: React Hook Form (validation, submission)

---

## 4. Feature Prioritization (MVP → Full)

### Phase 1: MVP (4-6 weeks) 🚀

**Must-Have Features**:
1. ✅ Authentication (Login, Register, Logout)
2. ✅ Dashboard (Net worth, quick stats)
3. ✅ Portfolio View (Holdings, current value)
4. ✅ Stock List (View stocks with real-time prices)
5. ✅ Add Stock Transaction (Buy/Sell)
6. ✅ Budget Summary (Income, Expenses, Balance)
7. ✅ Profile (User info, settings)

**Screens**: 8-10 screens  
**APIs**: Auth, Stocks, Portfolio, Budget basics  
**Timeline**: Sprint 1-3

### Phase 2: Core Features (4-6 weeks) 📈

**Add**:
1. ✅ Mutual Funds (List, transactions, SIP)
2. ✅ Fixed Deposits (Create, track maturity)
3. ✅ Recurring Deposits (Monthly tracking)
4. ✅ Budget Details (Categories, recurring transactions)
5. ✅ Expense Management (Add, edit, delete)
6. ✅ Transaction History (Filters, search)
7. ✅ Basic Analytics (Charts for portfolio breakdown)

**Screens**: +12-15 screens  
**APIs**: Investments, Savings, Budget full suite  
**Timeline**: Sprint 4-6

### Phase 3: Advanced Features (6-8 weeks) 🔥

**Add**:
1. ✅ ETF Tracking
2. ✅ Loans (Lent/Borrowed tracking)
3. ✅ Insurance (Life, health, vehicle)
4. ✅ Account Aggregation (AA integration)
5. ✅ Tax Planning (Capital gains, deductions)
6. ✅ Advanced Analytics (XIRR, diversification)
7. ✅ Notifications (Price alerts, payment reminders)
8. ✅ Export Reports (PDF, Excel)

**Screens**: +15-20 screens  
**APIs**: Full backend integration  
**Timeline**: Sprint 7-10

### Phase 4: Premium Features (4-6 weeks) 💎

**Add**:
1. ✅ Biometric Authentication
2. ✅ Dark Mode
3. ✅ Multiple Portfolios
4. ✅ Goal-Based Investing
5. ✅ AI-Powered Insights
6. ✅ Social Features (Share portfolio)
7. ✅ Offline Mode (View cached data)
8. ✅ Widget Support (iOS/Android)

**Timeline**: Sprint 11-13

---

## 5. Screen-by-Screen Design Guide

### 5.1 Authentication Flows

#### **Screen 1: Splash Screen**
```
┌─────────────────────┐
│                     │
│                     │
│      [App Logo]     │
│    Pi Financial     │
│   Management System │
│                     │
│   Loading...        │
│                     │
└─────────────────────┘
```

**Logic**:
- Check if JWT token exists in secure storage
- If valid token → Navigate to Dashboard
- If no token → Navigate to Login
- Auto-check token expiry

**Implementation**:
```typescript
// SplashScreen.tsx
useEffect(() => {
  const checkAuth = async () => {
    const token = await getSecureItem('jwt_token');
    if (token && !isTokenExpired(token)) {
      navigation.replace('MainTabs');
    } else {
      navigation.replace('Login');
    }
  };
  checkAuth();
}, []);
```

---

#### **Screen 2: Login Screen**
```
┌─────────────────────────────┐
│  ← Back                      │
│                              │
│  Welcome Back! 👋            │
│  Login to continue           │
│                              │
│  Email                       │
│  ┌────────────────────────┐ │
│  │ your@email.com         │ │
│  └────────────────────────┘ │
│                              │
│  Password                    │
│  ┌────────────────────────┐ │
│  │ ••••••••••            👁│ │
│  └────────────────────────┘ │
│                              │
│  ☐ Remember me    Forgot pwd?│
│                              │
│  ┌────────────────────────┐ │
│  │      LOGIN             │ │
│  └────────────────────────┘ │
│                              │
│  ────── OR ──────           │
│                              │
│  [🔵 Continue with Google]  │
│                              │
│  Don't have account? Sign Up │
└─────────────────────────────┘
```

**Design Guidelines**:
- Clean, minimal design with brand colors
- Large, easy-to-tap buttons (min 44px height)
- Show/hide password toggle
- Error messages below respective fields
- Loading state for button during API call
- Biometric option (Face ID/Fingerprint) after first login

**Validation**:
- Email: Valid format, required
- Password: Min 8 chars, required
- Show inline errors on blur

**API**: `POST /api/auth/login`

**State Management**:
```typescript
// Redux slice: authSlice.ts
const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null,
    token: null,
    isLoading: false,
    error: null
  },
  reducers: {
    loginStart, loginSuccess, loginFailure, logout
  }
});
```

---

#### **Screen 3: Register Screen**
```
┌─────────────────────────────┐
│  ← Back                      │
│                              │
│  Create Account 🎉           │
│  Start your financial journey│
│                              │
│  Full Name                   │
│  ┌────────────────────────┐ │
│  │ John Doe               │ │
│  └────────────────────────┘ │
│                              │
│  Email                       │
│  ┌────────────────────────┐ │
│  │ your@email.com         │ │
│  └────────────────────────┘ │
│                              │
│  Phone Number                │
│  ┌────────────────────────┐ │
│  │ +91 98765 43210        │ │
│  └────────────────────────┘ │
│                              │
│  Password                    │
│  ┌────────────────────────┐ │
│  │ ••••••••••            👁│ │
│  └────────────────────────┘ │
│  Password strength: [████--]│
│                              │
│  ☐ I agree to Terms & Privacy│
│                              │
│  ┌────────────────────────┐ │
│  │    CREATE ACCOUNT      │ │
│  └────────────────────────┘ │
│                              │
│  Already have account? Login │
└─────────────────────────────┘
```

**Validation**:
- Name: Min 2 chars, required
- Email: Unique, valid format
- Phone: 10 digits, Indian format
- Password: Min 8 chars, 1 uppercase, 1 number, 1 special char
- Terms: Must be checked

**API**: `POST /api/auth/register`

---

### 5.2 Main Dashboard

#### **Screen 4: Dashboard (Home)**
```
┌─────────────────────────────────┐
│  ☰  Pi Finance        🔔 👤    │
├─────────────────────────────────┤
│  Good morning, John! ☀️         │
│                                  │
│  ┌───────────────────────────┐ │
│  │  Net Worth                │ │
│  │  ₹12,45,680               │ │
│  │  +₹15,230 (+1.24%) ↗     │ │
│  │  Last updated: 2 mins ago │ │
│  └───────────────────────────┘ │
│                                  │
│  Quick Actions                   │
│  ┌─────┐ ┌─────┐ ┌─────┐       │
│  │ 📊  │ │ 💰  │ │ 📈  │       │
│  │Stock│ │ FD  │ │ MF  │       │
│  └─────┘ └─────┘ └─────┘       │
│                                  │
│  Portfolio Overview 📊           │
│  ┌───────────────────────────┐ │
│  │   [Pie Chart]             │ │
│  │   Stocks: 45%             │ │
│  │   MF: 30%                 │ │
│  │   FD: 25%                 │ │
│  └───────────────────────────┘ │
│                                  │
│  Recent Transactions              │
│  ┌───────────────────────────┐ │
│  │ RELIANCE    ₹2,500  BUY   │ │
│  │ Today, 10:30 AM      ↗    │ │
│  ├───────────────────────────┤ │
│  │ SBI MF      ₹5,000  SIP   │ │
│  │ Yesterday    Auto    →    │ │
│  └───────────────────────────┘ │
│                                  │
│  Budget Status 💵                │
│  ┌───────────────────────────┐ │
│  │ Jan 2026                  │ │
│  │ Spent: ₹45,000/₹60,000    │ │
│  │ [████████░░░░] 75%        │ │
│  └───────────────────────────┘ │
└─────────────────────────────────┘
```

**Components**:
1. **Header**: App logo, notifications, profile
2. **Net Worth Card**: Total value, change %, last updated
3. **Quick Actions**: Shortcuts to common tasks
4. **Portfolio Chart**: Interactive pie chart
5. **Recent Transactions**: Last 5 transactions
6. **Budget Progress**: Monthly spending tracker

**APIs**:
- `GET /api/portfolio/networth` (Net worth summary)
- `GET /api/portfolio/overview` (Asset breakdown)
- `GET /api/investments/transactions/recent` (Last 5 txns)
- `GET /api/budget/summary` (Monthly budget status)

**Refresh Logic**:
- Pull-to-refresh: Re-fetch all data
- Auto-refresh: Every 5 minutes when app is active
- Real-time updates: WebSocket for stock prices (optional)

**Design Tips**:
- Use cards with shadows for depth
- Color coding: Green (profit), Red (loss), Blue (neutral)
- Loading skeletons while fetching data
- Empty states with helpful CTAs

---

### 5.3 Portfolio Section

#### **Screen 5: Portfolio Details**
```
┌─────────────────────────────────┐
│  ← Back    Portfolio    [Filter]│
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │  Total Value: ₹12,45,680  │ │
│  │  Invested: ₹10,00,000     │ │
│  │  Returns: ₹2,45,680       │ │
│  │  XIRR: 18.5% 📈           │ │
│  └───────────────────────────┘ │
│                                  │
│  [ All | Stocks | MF | FD ]     │
│                                  │
│  Holdings                         │
│  ┌───────────────────────────┐ │
│  │ 📊 RELIANCE               │ │
│  │ 50 shares @ ₹2,500        │ │
│  │ Invested: ₹1,00,000       │ │
│  │ Current: ₹1,25,000        │ │
│  │ Returns: +₹25,000 (25%)   │ │
│  │ [View Details]            │ │
│  ├───────────────────────────┤ │
│  │ 📊 TCS                    │ │
│  │ 30 shares @ ₹3,200        │ │
│  │ Invested: ₹90,000         │ │
│  │ Current: ₹96,000          │ │
│  │ Returns: +₹6,000 (6.7%)   │ │
│  │ [View Details]            │ │
│  └───────────────────────────┘ │
│                                  │
│  [+ Add Investment]              │
└─────────────────────────────────┘
```

**Features**:
- **Summary Card**: Total value, returns, XIRR
- **Filter Tabs**: All, Stocks, MF, FD, ETF
- **Holdings List**: Each holding with current value, returns
- **Sort Options**: By value, returns %, alphabetical
- **Search**: Find specific holdings quickly

**API**: `GET /api/portfolio/holdings`

**Design Pattern**: FlatList with pull-to-refresh

---

#### **Screen 6: Stock Detail**
```
┌─────────────────────────────────┐
│  ← Back    RELIANCE    [★ Add]  │
├─────────────────────────────────┤
│  ₹2,500.50  +₹45.50 (+1.85%) ↗ │
│  Last updated: 2 mins ago        │
│                                  │
│  ┌───────────────────────────┐ │
│  │  [Line Chart - 1D/1W/1M]  │ │
│  │  Price trend              │ │
│  └───────────────────────────┘ │
│                                  │
│  Your Holdings                   │
│  ┌───────────────────────────┐ │
│  │ Quantity: 50 shares       │ │
│  │ Avg Price: ₹2,000         │ │
│  │ Invested: ₹1,00,000       │ │
│  │ Current: ₹1,25,025        │ │
│  │ Profit: +₹25,025 (25%)    │ │
│  └───────────────────────────┘ │
│                                  │
│  Key Metrics                     │
│  Open: ₹2,455    High: ₹2,520   │
│  Low: ₹2,440     Volume: 1.2M   │
│  Mkt Cap: ₹15.5L Cr             │
│                                  │
│  About                           │
│  Reliance Industries Ltd...      │
│  [Read more]                     │
│                                  │
│  Recent Transactions             │
│  ┌───────────────────────────┐ │
│  │ BUY  20 @ ₹2,500  Jan 15  │ │
│  │ BUY  30 @ ₹1,800  Dec 10  │ │
│  └───────────────────────────┘ │
│                                  │
│  [BUY] [SELL] [View All Txns]   │
└─────────────────────────────────┘
```

**Features**:
- Real-time price with auto-refresh (every 30s)
- Interactive chart (Victory Native / Recharts)
- Holdings summary
- Company info from OVERVIEW endpoint
- Transaction history
- Quick buy/sell actions

**APIs**:
- `GET /api/investments/stocks/{symbol}` (Stock details)
- `GET /api/portfolio/holdings/{symbol}` (User's holdings)
- `GET /api/investments/stocks/{symbol}/transactions` (Txn history)

---

#### **Screen 7: Add Stock Transaction**
```
┌─────────────────────────────────┐
│  ← Cancel    Add Stock    Save  │
├─────────────────────────────────┤
│  Transaction Type                │
│  [● BUY]  [○ SELL]              │
│                                  │
│  Stock Symbol                    │
│  ┌────────────────────────────┐│
│  │ Search symbol (RELIANCE)   ││
│  └────────────────────────────┘│
│  RELIANCE - Reliance Industries │
│                                  │
│  Quantity                        │
│  ┌────────────────────────────┐│
│  │ 10                         ││
│  └────────────────────────────┘│
│                                  │
│  Price per Share                 │
│  ┌────────────────────────────┐│
│  │ ₹2,500.00                  ││
│  └────────────────────────────┘│
│  Market Price: ₹2,500.50        │
│                                  │
│  Date                            │
│  ┌────────────────────────────┐│
│  │ 31 Jan 2026            📅  ││
│  └────────────────────────────┘│
│                                  │
│  Charges (optional)              │
│  ┌────────────────────────────┐│
│  │ ₹100                       ││
│  └────────────────────────────┘│
│                                  │
│  ─────────────────────────────  │
│  Total Amount: ₹25,100          │
│                                  │
│  [ADD TRANSACTION]              │
└─────────────────────────────────┘
```

**Validation**:
- Symbol: Must be valid stock symbol
- Quantity: Positive integer
- Price: Positive number
- Date: Cannot be future date
- For SELL: Quantity ≤ available shares

**API**: `POST /api/investments/stocks/transactions`

**Design Tips**:
- Auto-suggest stock symbols while typing
- Show current market price for reference
- Pre-fill price with current market price
- Calculate total automatically
- Confirmation dialog before saving

---

### 5.4 Budget Section

#### **Screen 8: Budget Dashboard**
```
┌─────────────────────────────────┐
│  ← Back    Budget    [+ Expense]│
├─────────────────────────────────┤
│  January 2026          [◀ ▶]   │
│                                  │
│  ┌───────────────────────────┐ │
│  │  Balance: ₹15,000         │ │
│  │  Income: ₹60,000          │ │
│  │  Spent: ₹45,000 (75%)     │ │
│  │  [████████████░░] 75%     │ │
│  └───────────────────────────┘ │
│                                  │
│  Spending by Category            │
│  ┌───────────────────────────┐ │
│  │ 🍕 Food        ₹15,000    │ │
│  │ [█████░░░░░] 50%          │ │
│  ├───────────────────────────┤ │
│  │ 🚗 Transport   ₹8,000     │ │
│  │ [████░░░░░░] 26.7%        │ │
│  ├───────────────────────────┤ │
│  │ 🎬 Entertainment ₹5,000   │ │
│  │ [███░░░░░░░] 16.7%        │ │
│  └───────────────────────────┘ │
│  [View All Categories]           │
│                                  │
│  Recent Expenses                 │
│  ┌───────────────────────────┐ │
│  │ 🍕 Lunch at Taj           │ │
│  │ ₹1,200    Food   Today    │ │
│  ├───────────────────────────┤ │
│  │ ⛽ Petrol                 │ │
│  │ ₹800      Transport  29th │ │
│  └───────────────────────────┘ │
│                                  │
│  Recurring Transactions 🔄       │
│  ┌───────────────────────────┐ │
│  │ Netflix Subscription      │ │
│  │ ₹199/month  Next: Feb 5   │ │
│  └───────────────────────────┘ │
└─────────────────────────────────┘
```

**Features**:
- Monthly overview with income/expense
- Category-wise breakdown
- Progress bars for each category
- Recent transactions
- Recurring transactions tracker
- Month navigation (prev/next)

**APIs**:
- `GET /api/budget/summary?month=2026-01` (Monthly summary)
- `GET /api/budget/categories` (Spending by category)
- `GET /api/budget/expenses/recent` (Last 10 expenses)
- `GET /api/budget/recurring` (Active recurring txns)

---

#### **Screen 9: Add Expense**
```
┌─────────────────────────────────┐
│  ← Cancel    Add Expense   Save │
├─────────────────────────────────┤
│  Amount                          │
│  ┌────────────────────────────┐│
│  │ ₹                          ││
│  └────────────────────────────┘│
│                                  │
│  Category                        │
│  ┌────────────────────────────┐│
│  │ Select category        ▼   ││
│  └────────────────────────────┘│
│  🍕 Food  🚗 Transport  🎬 Ent... │
│                                  │
│  Description                     │
│  ┌────────────────────────────┐│
│  │ Lunch at restaurant        ││
│  └────────────────────────────┘│
│                                  │
│  Date                            │
│  ┌────────────────────────────┐│
│  │ 31 Jan 2026            📅  ││
│  └────────────────────────────┘│
│                                  │
│  Payment Mode                    │
│  [○ Cash] [● Card] [○ UPI]      │
│                                  │
│  Recurring?                      │
│  ☐ Make this recurring           │
│                                  │
│  Attach Receipt (optional)       │
│  [📷 Take Photo] [📁 Choose]    │
│                                  │
│  [ADD EXPENSE]                  │
└─────────────────────────────────┘
```

**Features**:
- Large number pad for amount entry
- Category picker with icons
- Optional recurring setup
- Receipt upload (camera/gallery)
- Payment mode tracking

**API**: `POST /api/budget/expenses`

---

### 5.5 Savings Section

#### **Screen 10: Savings Overview**
```
┌─────────────────────────────────┐
│  ← Back    Savings    [+ Add]   │
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │  Total Savings            │ │
│  │  ₹5,45,000                │ │
│  │  Expected Interest: ₹1.2L │ │
│  └───────────────────────────┘ │
│                                  │
│  [ All | FD | RD | Savings ]    │
│                                  │
│  Fixed Deposits (3)              │
│  ┌───────────────────────────┐ │
│  │ 🏦 SBI Fixed Deposit      │ │
│  │ ₹2,00,000 @ 7.5%          │ │
│  │ Matures: 15 Dec 2026      │ │
│  │ Interest: ₹15,000 (exp)   │ │
│  │ [View Details]            │ │
│  ├───────────────────────────┤ │
│  │ 🏦 HDFC Fixed Deposit     │ │
│  │ ₹1,50,000 @ 7.2%          │ │
│  │ Matures: 20 Mar 2027      │ │
│  │ Interest: ₹16,200 (exp)   │ │
│  └───────────────────────────┘ │
│                                  │
│  Recurring Deposits (2)          │
│  ┌───────────────────────────┐ │
│  │ 🏦 ICICI RD               │ │
│  │ ₹5,000/month for 24 months│ │
│  │ Completed: 8/24           │ │
│  │ Next due: Feb 5, 2026     │ │
│  └───────────────────────────┘ │
└─────────────────────────────────┘
```

**APIs**:
- `GET /api/savings/fd` (Fixed deposits)
- `GET /api/savings/rd` (Recurring deposits)
- `GET /api/savings/accounts` (Savings accounts)

---

### 5.6 Profile & Settings

#### **Screen 11: Profile**
```
┌─────────────────────────────────┐
│  ← Back    Profile               │
├─────────────────────────────────┤
│        [Profile Photo]           │
│        John Doe                  │
│        john@example.com          │
│                                  │
│  Account                         │
│  ┌───────────────────────────┐ │
│  │ 👤 Edit Profile        >  │ │
│  │ 🔒 Change Password     >  │ │
│  │ 🔔 Notifications       >  │ │
│  │ 🌙 Dark Mode        [●○] │ │
│  │ 🇮🇳 Currency: INR (₹)  >  │ │
│  └───────────────────────────┘ │
│                                  │
│  Preferences                     │
│  ┌───────────────────────────┐ │
│  │ 📊 Default View        >  │ │
│  │ 📱 Biometric Login  [●○]  │ │
│  │ 💾 Data Sync          >  │ │
│  │ 🔗 Account Aggregation >  │ │
│  └───────────────────────────┘ │
│                                  │
│  Support                         │
│  ┌───────────────────────────┐ │
│  │ 💬 Help & FAQ          >  │ │
│  │ 📧 Contact Support     >  │ │
│  │ ⭐ Rate App            >  │ │
│  │ 📄 Privacy Policy      >  │ │
│  │ 📜 Terms of Service    >  │ │
│  └───────────────────────────┘ │
│                                  │
│  Version 1.0.0                   │
│                                  │
│  [LOGOUT]                        │
└─────────────────────────────────┘
```

**Features**:
- Profile photo upload
- Dark mode toggle
- Notification preferences
- Biometric authentication setup
- Help and support
- Version info

---

## 6. API Integration Strategy

### API Client Setup

**Base Configuration** (services/api.ts):
```typescript
import axios from 'axios';
import { getSecureItem } from './secureStorage';

const API_BASE_URL = 'https://api.yourapp.com'; // Replace with your backend URL

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Add JWT token
apiClient.interceptors.request.use(
  async (config) => {
    const token = await getSecureItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Handle errors
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expired, try refresh or logout
      await refreshToken();
    }
    return Promise.reject(error);
  }
);
```

### RTK Query Setup

**API Slices** (features/portfolio/api.ts):
```typescript
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

export const portfolioApi = createApi({
  reducerPath: 'portfolioApi',
  baseQuery: fetchBaseQuery({
    baseUrl: 'https://api.yourapp.com/api',
    prepareHeaders: async (headers) => {
      const token = await getSecureItem('jwt_token');
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ['Portfolio', 'Holdings'],
  endpoints: (builder) => ({
    getNetWorth: builder.query<NetWorthResponse, void>({
      query: () => '/portfolio/networth',
      providesTags: ['Portfolio'],
    }),
    getHoldings: builder.query<HoldingsResponse, void>({
      query: () => '/portfolio/holdings',
      providesTags: ['Holdings'],
    }),
    addStockTransaction: builder.mutation<void, StockTransaction>({
      query: (transaction) => ({
        url: '/investments/stocks/transactions',
        method: 'POST',
        body: transaction,
      }),
      invalidatesTags: ['Holdings', 'Portfolio'],
    }),
  }),
});

export const {
  useGetNetWorthQuery,
  useGetHoldingsQuery,
  useAddStockTransactionMutation,
} = portfolioApi;
```

### Usage in Components

```typescript
// In Dashboard.tsx
import { useGetNetWorthQuery } from '@/features/portfolio/api';

function Dashboard() {
  const { data: netWorth, isLoading, error, refetch } = useGetNetWorthQuery();

  if (isLoading) return <LoadingSkeleton />;
  if (error) return <ErrorState onRetry={refetch} />;

  return (
    <ScrollView refreshControl={<RefreshControl onRefresh={refetch} />}>
      <NetWorthCard data={netWorth} />
      {/* ... */}
    </ScrollView>
  );
}
```

### API Endpoints Reference

| Feature | Endpoint | Method | Description |
|---------|----------|--------|-------------|
| Auth | `/api/auth/login` | POST | User login |
| Auth | `/api/auth/register` | POST | User registration |
| Auth | `/api/auth/refresh` | POST | Refresh JWT token |
| Portfolio | `/api/portfolio/networth` | GET | Net worth summary |
| Portfolio | `/api/portfolio/holdings` | GET | All holdings |
| Portfolio | `/api/portfolio/analytics` | GET | XIRR, diversification |
| Stocks | `/api/investments/stocks` | GET | List stocks |
| Stocks | `/api/investments/stocks/{symbol}` | GET | Stock details |
| Stocks | `/api/investments/stocks/transactions` | POST | Add transaction |
| MF | `/api/investments/mf` | GET | List mutual funds |
| Budget | `/api/budget/summary` | GET | Monthly summary |
| Budget | `/api/budget/expenses` | GET/POST | List/add expenses |
| Savings | `/api/savings/fd` | GET/POST | Fixed deposits |

---

## 7. Design System & UI/UX Principles

### Color Palette

**Primary Colors**:
```
Primary: #1E88E5 (Blue - Trust, Finance)
Secondary: #43A047 (Green - Growth, Profit)
Accent: #F57C00 (Orange - Action, Warning)
```

**Semantic Colors**:
```
Success: #4CAF50 (Green)
Error: #F44336 (Red)
Warning: #FF9800 (Orange)
Info: #2196F3 (Blue)
```

**Neutrals**:
```
Background Light: #FAFAFA
Background Dark: #121212
Card Light: #FFFFFF
Card Dark: #1E1E1E
Text Primary: #212121
Text Secondary: #757575
Divider: #E0E0E0
```

### Typography

```
Headings:
  H1: 32px, Bold (Screen titles)
  H2: 24px, Semibold (Section headers)
  H3: 20px, Medium (Card titles)

Body:
  Large: 16px, Regular (Primary text)
  Medium: 14px, Regular (Secondary text)
  Small: 12px, Regular (Captions, hints)

Currency:
  Large: 28px, Bold (Net worth, totals)
  Medium: 20px, Semibold (Values in cards)
  Small: 16px, Medium (Transaction amounts)
```

### Component Library

**Key Components to Build**:

1. **Button** (Primary, Secondary, Outline, Text)
2. **Card** (Elevated, Outlined, Flat)
3. **Input** (Text, Number, Date, Dropdown)
4. **Chart** (Line, Bar, Pie, Area)
5. **List Item** (Transaction, Holding, Account)
6. **Modal** (Alert, Confirm, Bottom Sheet)
7. **Toast** (Success, Error, Info)
8. **Loading** (Spinner, Skeleton, Shimmer)
9. **Empty State** (No data, Error state)
10. **Tab Bar** (Bottom nav, Top tabs)

### UX Best Practices

1. **Financial Data Display**:
   - Always show currency symbol (₹)
   - Use green for positive, red for negative
   - Show percentage change with arrow (↗ ↘)
   - Format large numbers: ₹1,23,456.78

2. **Loading States**:
   - Skeleton screens for content
   - Shimmer effect for cards
   - Loading spinner for actions
   - Pull-to-refresh everywhere

3. **Error Handling**:
   - Toast notifications for temporary errors
   - Full screen for critical errors
   - Retry button always visible
   - Clear error messages

4. **Touch Targets**:
   - Minimum 44x44 px for buttons
   - Swipeable list items (left: delete, right: edit)
   - Long press for quick actions
   - Bottom sheet for multiple options

5. **Navigation**:
   - Bottom tab bar for main sections (4-5 tabs)
   - Stack navigation within sections
   - Drawer for settings/profile
   - Breadcrumbs for deep navigation

---

## 8. Security & Authentication

### JWT Token Management

**Storage**:
- Use `react-native-keychain` (iOS) or `EncryptedSharedPreferences` (Android)
- Store JWT in secure storage, never in AsyncStorage
- Store refresh token separately

**Refresh Logic**:
```typescript
async function refreshToken() {
  const refreshToken = await getSecureItem('refresh_token');
  const response = await axios.post('/api/auth/refresh', { refreshToken });
  
  await setSecureItem('jwt_token', response.data.accessToken);
  await setSecureItem('refresh_token', response.data.refreshToken);
}
```

### Biometric Authentication

**Setup** (react-native-biometrics):
```typescript
import ReactNativeBiometrics from 'react-native-biometrics';

async function authenticateWithBiometrics() {
  const { available, biometryType } = await ReactNativeBiometrics.isSensorAvailable();
  
  if (available) {
    const { success } = await ReactNativeBiometrics.simplePrompt({
      promptMessage: 'Authenticate to continue',
    });
    
    return success;
  }
  
  return false;
}
```

### Data Encryption

**Sensitive Data**:
- Encrypt API keys, tokens in secure storage
- Use HTTPS only (enforce SSL pinning)
- Never log sensitive data (passwords, tokens, PINs)

**Best Practices**:
- Auto-logout after 15 minutes of inactivity
- Clear cache on logout
- Require re-authentication for sensitive actions (transactions)
- Use 2FA for high-value transactions (optional)

---

## 9. Implementation Phases

### Sprint 1-2: Setup & Authentication (2 weeks)

**Week 1**:
- [x] Project setup (React Native, TypeScript)
- [x] Folder structure
- [x] Design system foundation (colors, fonts)
- [x] API client setup (Axios, RTK Query)
- [x] Secure storage implementation
- [ ] Navigation structure

**Week 2**:
- [ ] Splash screen
- [ ] Login screen
- [ ] Register screen
- [ ] JWT authentication
- [ ] Biometric setup
- [ ] Profile screen (basic)

**Deliverable**: Users can register, login, and see profile

---

### Sprint 3-4: Dashboard & Portfolio (2 weeks)

**Week 3**:
- [ ] Dashboard screen with net worth
- [ ] Quick actions
- [ ] Recent transactions
- [ ] Portfolio overview chart
- [ ] Budget summary widget

**Week 4**:
- [ ] Portfolio details screen
- [ ] Holdings list
- [ ] Stock detail screen
- [ ] Add stock transaction
- [ ] Transaction history

**Deliverable**: Users can view portfolio and add stock transactions

---

### Sprint 5-6: Budget & Savings (2 weeks)

**Week 5**:
- [ ] Budget dashboard
- [ ] Category-wise breakdown
- [ ] Add expense screen
- [ ] Recurring transactions list
- [ ] Month navigation

**Week 6**:
- [ ] Savings overview
- [ ] Add fixed deposit
- [ ] Add recurring deposit
- [ ] Maturity calculator
- [ ] Interest projections

**Deliverable**: Complete budget and savings management

---

### Sprint 7-8: Advanced Features (2 weeks)

**Week 7**:
- [ ] Mutual funds list and transactions
- [ ] ETF tracking
- [ ] Loans management
- [ ] Insurance tracking

**Week 8**:
- [ ] Analytics dashboard (XIRR, diversification)
- [ ] Charts and visualizations
- [ ] Export reports (PDF, Excel)
- [ ] Notifications setup

**Deliverable**: Full-featured app with analytics

---

### Sprint 9-10: Polish & Testing (2 weeks)

**Week 9**:
- [ ] Dark mode implementation
- [ ] Offline mode (view cached data)
- [ ] Performance optimization
- [ ] Accessibility improvements
- [ ] Error handling refinement

**Week 10**:
- [ ] Unit testing (Jest)
- [ ] Integration testing
- [ ] E2E testing (Detox)
- [ ] Bug fixes
- [ ] Beta testing with users

**Deliverable**: Production-ready app

---

## 10. Testing Strategy

### Unit Tests (Jest + React Native Testing Library)

**What to Test**:
- Utility functions (formatCurrency, validators)
- Redux reducers
- Custom hooks
- Component logic (not UI)

**Example**:
```typescript
// utils/__tests__/formatCurrency.test.ts
describe('formatCurrency', () => {
  it('formats INR correctly', () => {
    expect(formatCurrency(1234567.89)).toBe('₹12,34,567.89');
  });
});
```

### Integration Tests

**What to Test**:
- API integration (RTK Query)
- Navigation flows
- Form submissions
- State updates

### E2E Tests (Detox)

**Critical Flows**:
1. Login → Dashboard → View Portfolio
2. Add Stock Transaction → Verify in Portfolio
3. Add Expense → Verify in Budget
4. View Analytics

**Example**:
```typescript
// e2e/login.e2e.ts
describe('Login Flow', () => {
  it('should login successfully', async () => {
    await element(by.id('email-input')).typeText('test@example.com');
    await element(by.id('password-input')).typeText('password123');
    await element(by.id('login-button')).tap();
    
    await expect(element(by.id('dashboard'))).toBeVisible();
  });
});
```

### Manual Testing Checklist

- [ ] Login/Logout works
- [ ] All navigation works
- [ ] Forms validate correctly
- [ ] API errors show properly
- [ ] Offline mode works
- [ ] Biometric authentication works
- [ ] Dark mode renders correctly
- [ ] Charts display data
- [ ] Export reports work
- [ ] Notifications arrive

---

## 11. Performance Optimization

### Best Practices

1. **Images**:
   - Use WebP format for smaller size
   - Implement lazy loading
   - Cache images with `react-native-fast-image`

2. **Lists**:
   - Use `FlatList` with `getItemLayout`
   - Implement `keyExtractor`
   - Set `maxToRenderPerBatch` and `windowSize`
   - Use `memo` for list items

3. **State Management**:
   - Keep Redux store minimal
   - Use selectors with `reselect`
   - Cache API responses with RTK Query
   - Avoid deep nesting in state

4. **Bundle Size**:
   - Enable Hermes engine
   - Use dynamic imports for heavy screens
   - Remove unused dependencies
   - Optimize images and assets

5. **Network**:
   - Implement request debouncing
   - Use pagination for large lists
   - Cache static data locally
   - Compress API responses

### Monitoring

- Use **Sentry** for crash reporting
- Use **Firebase Analytics** for user behavior
- Track API response times
- Monitor app startup time
- Track memory usage

---

## 12. Deployment Checklist

### Pre-Release

- [ ] All features tested on iOS and Android
- [ ] No console warnings or errors
- [ ] App icons and splash screens added
- [ ] App name and bundle ID configured
- [ ] Privacy policy and terms added
- [ ] API endpoints point to production
- [ ] Analytics tracking enabled
- [ ] Crash reporting configured
- [ ] Push notifications tested

### iOS (App Store)

- [ ] Create App Store Connect account
- [ ] Generate provisioning profiles
- [ ] Add app screenshots (all device sizes)
- [ ] Write app description
- [ ] Set pricing (Free/Paid)
- [ ] Submit for review
- [ ] Respond to reviewer feedback

### Android (Play Store)

- [ ] Create Google Play Console account
- [ ] Generate signed APK/AAB
- [ ] Add app screenshots (phone, tablet)
- [ ] Write app description
- [ ] Set content rating
- [ ] Configure in-app purchases (if any)
- [ ] Submit for review

### Post-Launch

- [ ] Monitor crash reports daily
- [ ] Respond to user reviews
- [ ] Track key metrics (DAU, retention)
- [ ] Gather user feedback
- [ ] Plan next version features
- [ ] Fix critical bugs ASAP

---

## 📞 Next Steps

### Immediate Actions

1. **Choose Tech Stack**: React Native (recommended) or Flutter
2. **Setup Project**: 
   ```bash
   npx react-native init PiFinance --template react-native-template-typescript
   ```
3. **Install Dependencies**:
   ```bash
   npm install @reduxjs/toolkit react-redux
   npm install @react-navigation/native @react-navigation/bottom-tabs
   npm install axios react-native-keychain
   npm install react-native-paper
   npm install victory-native
   ```

4. **Create Folder Structure** (as shown in Section 3)
5. **Setup API Client** (as shown in Section 6)
6. **Start with Auth Screens** (Login, Register)

### Resources

- **Backend API Docs**: Your Spring Boot API is already built!
- **Design Inspiration**: 
  - ET Money (Indian finance app)
  - Groww (Investment app)
  - YNAB (Budget app)
- **React Native Docs**: https://reactnative.dev
- **RTK Query Docs**: https://redux-toolkit.js.org/rtk-query/overview

---

## 🎯 Success Metrics

**MVP Goals** (After Phase 1):
- [ ] 100+ beta users
- [ ] <2s app startup time
- [ ] 95%+ API success rate
- [ ] <1% crash rate
- [ ] 4+ star rating

**Full App Goals** (After Phase 3):
- [ ] 10,000+ downloads
- [ ] 70%+ D7 retention
- [ ] 4.5+ star rating
- [ ] <0.5% crash rate
- [ ] 2+ min average session time

---

**Good luck with your mobile app development! 🚀**

This guide should give you a complete roadmap from design to deployment. Focus on MVP first, get user feedback, then iterate.
