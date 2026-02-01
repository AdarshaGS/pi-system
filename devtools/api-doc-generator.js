#!/usr/bin/env node

/**
 * API Documentation Dashboard Generator
 * 
 * Scans Java controllers and generates:
 * 1. Complete API documentation dashboard (API_DASHBOARD.md)
 * 2. Endpoint inventory with metadata
 * 3. API status tracker (implemented/in-progress/planned)
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const PROJECT_ROOT = path.join(__dirname, '..');
const SRC_DIR = path.join(PROJECT_ROOT, 'src', 'main', 'java');
const OUTPUT_FILE = path.join(PROJECT_ROOT, 'API_DASHBOARD.md');

// API Domains
const DOMAINS = {
  'Authentication & Users': ['/api/auth', '/api/v1/admin'],
  'Budget Management': ['/api/v1/budget'],
  'Investments - Stocks': ['/api/v1/stocks', '/api/v1/portfolio', '/api/v1/net-worth'],
  'Investments - Mutual Funds': ['/api/v1/mutual-funds'],
  'Investments - ETF': ['/api/v1/etf'],
  'Savings': ['/api/v1/savings', '/api/v1/fixed-deposit', '/api/v1/recurring-deposit'],
  'Insurance': ['/api/v1/insurance'],
  'Lending': ['/api/v1/lending'],
  'Tax': ['/api/v1/tax'],
  'External Services': ['/api/v1/external-services'],
  'Dev Tools': ['/api/dev', '/api/migration'],
  'Health & Monitoring': ['/health', '/actuator']
};

class APIDocGenerator {
  constructor() {
    this.endpoints = [];
    this.controllers = [];
    this.domains = {};
  }

  // Find all controller files
  findControllers(dir = SRC_DIR, fileList = []) {
    try {
      const files = fs.readdirSync(dir);

      files.forEach(file => {
        const filePath = path.join(dir, file);
        const stat = fs.statSync(filePath);

        if (stat.isDirectory()) {
          this.findControllers(filePath, fileList);
        } else if (file.endsWith('Controller.java')) {
          fileList.push(filePath);
        }
      });
    } catch (error) {
      // Directory might not exist
    }

    return fileList;
  }

  // Parse a controller file
  parseController(filePath) {
    const content = fs.readFileSync(filePath, 'utf-8');
    const lines = content.split('\n');
    
    const controller = {
      path: filePath,
      relativePath: path.relative(PROJECT_ROOT, filePath),
      name: path.basename(filePath, '.java'),
      baseMapping: '',
      endpoints: [],
      isRestController: false
    };

    // Check if it's a REST controller
    controller.isRestController = /@RestController/.test(content);
    if (!controller.isRestController && !/@Controller/.test(content)) {
      return null;
    }

    // Get base request mapping
    const baseMappingMatch = content.match(/@RequestMapping\(["']([^"']+)["']\)/);
    if (baseMappingMatch) {
      controller.baseMapping = baseMappingMatch[1];
    }

    // Parse endpoints
    let currentMethod = null;
    let currentDescription = [];
    let inJavadoc = false;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();

      // Track JavaDoc comments
      if (line.startsWith('/**')) {
        inJavadoc = true;
        currentDescription = [];
      } else if (inJavadoc) {
        if (line.startsWith('*/')) {
          inJavadoc = false;
        } else if (line.startsWith('*')) {
          const desc = line.replace(/^\*\s*/, '').trim();
          if (desc && !desc.startsWith('@')) {
            currentDescription.push(desc);
          }
        }
      }

      // Parse endpoint annotations
      const mappingRegex = /@(Get|Post|Put|Delete|Patch|Request)Mapping(?:\(["']([^"']+)["']|(?:\([^)]*value\s*=\s*["']([^"']+)["'])?)/;
      const match = line.match(mappingRegex);

      if (match) {
        const method = match[1].replace('Mapping', '').toUpperCase();
        if (method === 'REQUEST') {
          // RequestMapping defaults to GET but supports multiple methods
          currentMethod = 'GET/POST/PUT/DELETE';
        } else {
          currentMethod = method;
        }
        
        let path = match[2] || match[3] || '';
        
        // Look ahead for method name
        let methodName = '';
        for (let j = i + 1; j < Math.min(i + 5, lines.length); j++) {
          const nextLine = lines[j].trim();
          const methodMatch = nextLine.match(/(?:public|private|protected)\s+\w+(?:<[^>]+>)?\s+(\w+)\s*\(/);
          if (methodMatch) {
            methodName = methodMatch[1];
            break;
          }
        }

        // Parse path variables and request params
        const pathVariables = [];
        const requestParams = [];
        
        for (let j = i + 1; j < Math.min(i + 10, lines.length); j++) {
          const nextLine = lines[j];
          if (nextLine.includes('{')) break; // Method body started
          
          const pathVarMatch = nextLine.match(/@PathVariable[^)]*["']([^"']+)["']/g);
          if (pathVarMatch) {
            pathVarMatch.forEach(m => {
              const varMatch = m.match(/["']([^"']+)["']/);
              if (varMatch) pathVariables.push(varMatch[1]);
            });
          }
          
          const paramMatch = nextLine.match(/@RequestParam[^)]*["']([^"']+)["']/g);
          if (paramMatch) {
            paramMatch.forEach(m => {
              const varMatch = m.match(/["']([^"']+)["']/);
              if (varMatch) requestParams.push(varMatch[1]);
            });
          }
        }

        const endpoint = {
          method: currentMethod,
          path: path,
          fullPath: controller.baseMapping + path,
          methodName: methodName,
          description: currentDescription.join(' ') || '',
          pathVariables: pathVariables,
          requestParams: requestParams,
          controller: controller.name
        };

        controller.endpoints.push(endpoint);
        this.endpoints.push(endpoint);
        
        currentDescription = [];
      }
    }

    return controller;
  }

  // Categorize endpoint by domain
  categorizeEndpoint(endpoint) {
    for (const [domain, patterns] of Object.entries(DOMAINS)) {
      for (const pattern of patterns) {
        if (endpoint.fullPath.startsWith(pattern)) {
          return domain;
        }
      }
    }
    return 'Other';
  }

  // Generate the dashboard
  generate() {
    console.log('🔍 Scanning for controllers...');
    const controllerFiles = this.findControllers();
    console.log(`📄 Found ${controllerFiles.length} controller files`);

    // Parse each controller
    controllerFiles.forEach(filePath => {
      try {
        const controller = this.parseController(filePath);
        if (controller && controller.endpoints.length > 0) {
          this.controllers.push(controller);
        }
      } catch (error) {
        console.error(`Error parsing ${filePath}:`, error.message);
      }
    });

    // Group endpoints by domain
    this.endpoints.forEach(endpoint => {
      const domain = this.categorizeEndpoint(endpoint);
      if (!this.domains[domain]) {
        this.domains[domain] = [];
      }
      this.domains[domain].push(endpoint);
    });

    console.log('📝 Generating API dashboard...');
    this.generateDashboard();
    console.log(`✅ API dashboard generated: ${OUTPUT_FILE}`);
  }

  // Generate the dashboard markdown
  generateDashboard() {
    let content = `# 🚀 API Documentation Dashboard

*Last generated: ${new Date().toLocaleString()}*

## Overview

This dashboard provides a complete inventory of all REST APIs in the project.

---

## 📊 Statistics

- **Total Endpoints**: ${this.endpoints.length}
- **Controllers**: ${this.controllers.length}
- **Domains**: ${Object.keys(this.domains).length}

### HTTP Methods Distribution

`;

    // Count by HTTP method
    const methodCounts = {};
    this.endpoints.forEach(ep => {
      const methods = ep.method.includes('/') ? ep.method.split('/') : [ep.method];
      methods.forEach(m => {
        methodCounts[m] = (methodCounts[m] || 0) + 1;
      });
    });

    Object.entries(methodCounts).sort((a, b) => b[1] - a[1]).forEach(([method, count]) => {
      const emoji = { GET: '🔍', POST: '➕', PUT: '📝', DELETE: '🗑️', PATCH: '🔧' }[method] || '📡';
      content += `- ${emoji} **${method}**: ${count}\n`;
    });

    content += `\n---\n\n`;

    // Quick navigation
    content += `## 🗺️ Quick Navigation\n\n`;
    const sortedDomains = Object.keys(this.domains).sort();
    sortedDomains.forEach(domain => {
      const count = this.domains[domain].length;
      const anchor = domain.toLowerCase().replace(/[^a-z0-9]+/g, '-');
      content += `- [${domain}](#${anchor}) (${count} endpoints)\n`;
    });

    content += `\n---\n\n`;

    // Generate sections for each domain
    sortedDomains.forEach(domain => {
      content += `## ${domain}\n\n`;
      content += `**Total Endpoints**: ${this.domains[domain].length}\n\n`;

      // Group by base path
      const byBasePath = {};
      this.domains[domain].forEach(ep => {
        const basePath = ep.fullPath.split('/').slice(0, 4).join('/') || ep.fullPath;
        if (!byBasePath[basePath]) {
          byBasePath[basePath] = [];
        }
        byBasePath[basePath].push(ep);
      });

      Object.keys(byBasePath).sort().forEach(basePath => {
        const endpoints = byBasePath[basePath];
        content += `### \`${basePath}\`\n\n`;
        content += `| Method | Endpoint | Description | Controller |\n`;
        content += `|--------|----------|-------------|------------|\n`;

        endpoints.forEach(ep => {
          const method = ep.method;
          const emoji = { GET: '🔍', POST: '➕', PUT: '📝', DELETE: '🗑️', PATCH: '🔧' }[method.split('/')[0]] || '📡';
          const endpoint = ep.fullPath;
          const description = ep.description || ep.methodName || '-';
          const controller = `\`${ep.controller}\``;
          
          content += `| ${emoji} ${method} | \`${endpoint}\` | ${description} | ${controller} |\n`;
        });

        content += `\n`;
      });

      content += `\n`;
    });

    // Add detailed endpoint reference
    content += `---\n\n## 📖 Detailed Endpoint Reference\n\n`;
    
    sortedDomains.forEach(domain => {
      content += `### ${domain}\n\n`;
      
      this.domains[domain].forEach(ep => {
        content += `#### \`${ep.method} ${ep.fullPath}\`\n\n`;
        
        if (ep.description) {
          content += `**Description**: ${ep.description}\n\n`;
        }
        
        if (ep.pathVariables.length > 0) {
          content += `**Path Variables**:\n`;
          ep.pathVariables.forEach(pv => {
            content += `- \`${pv}\`\n`;
          });
          content += `\n`;
        }
        
        if (ep.requestParams.length > 0) {
          content += `**Request Parameters**:\n`;
          ep.requestParams.forEach(rp => {
            content += `- \`${rp}\`\n`;
          });
          content += `\n`;
        }
        
        content += `**Controller**: [\`${ep.controller}\`](${this.getControllerPath(ep.controller)})\n`;
        content += `**Method**: \`${ep.methodName}\`\n\n`;
        content += `---\n\n`;
      });
    });

    // Add controller index
    content += `## 🎛️ Controller Index\n\n`;
    content += `| Controller | Endpoints | Base Path | Location |\n`;
    content += `|------------|-----------|-----------|----------|\n`;

    this.controllers.sort((a, b) => a.name.localeCompare(b.name)).forEach(ctrl => {
      const name = `\`${ctrl.name}\``;
      const count = ctrl.endpoints.length;
      const basePath = ctrl.baseMapping || '-';
      const location = `[View](${ctrl.relativePath})`;
      
      content += `| ${name} | ${count} | \`${basePath}\` | ${location} |\n`;
    });

    fs.writeFileSync(OUTPUT_FILE, content);
  }

  // Get relative path to controller
  getControllerPath(controllerName) {
    const controller = this.controllers.find(c => c.name === controllerName);
    return controller ? controller.relativePath : '#';
  }
}

// Run the generator
if (require.main === module) {
  const generator = new APIDocGenerator();
  generator.generate();
}

module.exports = APIDocGenerator;
