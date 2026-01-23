# 💰 PI SYSTEM — Stock & Personal Finance Intelligence System

PI SYSTEM is a Spring Boot–based financial intelligence platform designed to help users track their investments, analyze diversification, compute net worth, and make data-driven financial decisions.  

It integrates with external stock market APIs, performs sector-based diversification scoring, and is structured to support future modules like budgeting, account aggregators, alerts, and AI-driven insights.

---

## 🌟 Key Capabilities

✔ Stock price retrieval from external APIs  
✔ Portfolio tracking & profit/loss analysis  
✔ Sector-based diversification scoring  
✔ Token-based authentication using JWT  
✔ User password encryption using BCrypt  
✔ Caching logic for unavailable external providers  
✔ Expandable architecture for future finance modules  

---

## 🧭 Product Workflow Diagram

> The full system workflow diagram is available here:  
🔗 **https://www.mermaidchart.com/app/projects/ef11d05b-42d4-47ba-a46f-4b0a68ac58f3/diagrams/bf2087c9-56e3-4bd7-b604-812953cd9be5/version/v0.1/edit**

### Simplified System Flow (Mermaid)

```mermaid
flowchart TD

User[[User]] -->|Register/Login| Auth[JWT Auth Service]
Auth -->|Valid Credentials| JWT[Generate JWT Token]
JWT -->|Bearer Token| API[Protected API Layer]

API -->|Portfolio Requests| PortfolioService
API -->|Stock Lookup| StockService
API -->|Net Worth Requests| NetWorthService
API -->|User Asset Insert| AssetService
API -->|Future: Alerts & Analytics| InsightsService

StockService -->|Check DB| StockDB[(Stocks DB)]
StockDB -->|Found| ReturnStock
StockDB -->|Not Found| ThirdPartyCheck{External Stock API?}

ThirdPartyCheck -->|Yes| ExternalAPI[External Market API]
ExternalAPI --> Parse[Parse & Store]
Parse --> ReturnStock

ThirdPartyCheck -->|No| Cached[Use Last Known Price]

PortfolioService --> PortfolioDB[(Portfolio Holdings DB)]
PortfolioDB --> Calculate[Calculate Current Value]

Calculate --> Diversify[Sector Mapping & Score]

Diversify --> SectorDB[(Sectors DB)]
SectorDB --> Diversify

Diversify --> Insight[Generate Recommendations]
Insight --> ReturnSummary

NetWorthService --> UserAssets[(Assets DB)]
NetWorthService --> UserLiabilities[(Liabilities DB)]
UserAssets --> ComputeNW[Compute Net Worth]
UserLiabilities --> ComputeNW
ComputeNW --> ReturnNW[Return Net Worth Summary]

---

## 🖥️ Frontend (React + Vite)

Located in the `/frontend` directory, the UI is built for speed and minimalism.

### Tech Stack
- **Core**: React 18 + Vite
- **Routing**: React Router Dom
- **Styling**: Vanilla CSS (Custom Design System)
- **Icons**: Lucide React
- **Charts**: Recharts (Portfolio Allocation)

### Key Features
- **Dashboard**: One-glance financial awareness with Net Worth Hero Card.
- **Auth Flow**: Under 20-second entry with minimal distractions.
- **Portfolio**: Detailed XIRR and asset allocation visualization.
- **Insights**: Computed observations on portfolio concentration and risk.

### How to Run
```bash
cd frontend
npm install
npm run dev
```
Default URL: `http://localhost:5173` or `http://localhost:5175`

---

## 🛠️ Getting Started (Backend)

1. **Prerequisites**: Java 17, MySQL 8, Redis.
2. **Configure**: Update `src/main/resources/application.yml` with your DB and Redis credentials.
3. **Run**:
   ```bash
   ./gradlew bootRun
   ```
4. **API Port**: `8082`
5. **Swagger UI**: `http://localhost:8082/swagger-ui.html`
