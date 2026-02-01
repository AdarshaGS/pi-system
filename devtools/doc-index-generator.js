#!/usr/bin/env node

/**
 * Documentation Index Generator
 * 
 * Scans all markdown files in the project and generates:
 * 1. A searchable index (DOC_INDEX.md)
 * 2. A navigation sidebar (DOC_NAVIGATION.md)
 * 3. Metadata about each document (last modified, size, links)
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const PROJECT_ROOT = path.join(__dirname, '..');
const OUTPUT_FILE = path.join(PROJECT_ROOT, 'DOC_INDEX.md');
const NAV_FILE = path.join(PROJECT_ROOT, 'DOC_NAVIGATION.md');

// Categories for organizing docs
const CATEGORIES = {
  'API Documentation': ['API', 'INTEGRATION', 'REFERENCE'],
  'Testing': ['TEST', 'TESTING'],
  'Deployment': ['DEPLOY', 'DOCKER'],
  'Product & Features': ['PRODUCT', 'FEATURE', 'DESIGN'],
  'Planning & Strategy': ['ROADMAP', 'BACKLOG', 'VISION', 'SCOPE'],
  'Budget Domain': ['BUDGET', 'SPRINT'],
  'Implementation': ['IMPLEMENTATION', 'SUMMARY', 'COMPLETE'],
  'Guides': ['GUIDE', 'QUICK', 'START'],
  'Problems & Risks': ['PROBLEM', 'RISK', 'CONSTRAINT'],
  'Architecture': ['ARCHITECTURE', 'DESIGN'],
  'Admin': ['ADMIN', 'PORTAL'],
  'Other': []
};

class DocIndexGenerator {
  constructor() {
    this.docs = [];
    this.categories = {};
  }

  // Find all markdown files
  findMarkdownFiles(dir, fileList = []) {
    const files = fs.readdirSync(dir);

    files.forEach(file => {
      const filePath = path.join(dir, file);
      const stat = fs.statSync(filePath);

      if (stat.isDirectory()) {
        // Skip node_modules, build, .git
        if (!['node_modules', 'build', '.git', 'gradle', 'bin'].includes(file)) {
          this.findMarkdownFiles(filePath, fileList);
        }
      } else if (file.endsWith('.md')) {
        fileList.push(filePath);
      }
    });

    return fileList;
  }

  // Get last modified date from git
  getLastModified(filePath) {
    try {
      const relativePath = path.relative(PROJECT_ROOT, filePath);
      const timestamp = execSync(`git log -1 --format=%ai "${relativePath}"`, {
        cwd: PROJECT_ROOT,
        encoding: 'utf-8'
      }).trim();
      return timestamp || 'Unknown';
    } catch (error) {
      return 'Unknown';
    }
  }

  // Get file size
  getFileSize(filePath) {
    const stats = fs.statSync(filePath);
    const bytes = stats.size;
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  }

  // Extract first heading as title
  getTitle(filePath) {
    try {
      const content = fs.readFileSync(filePath, 'utf-8');
      const match = content.match(/^#\s+(.+)$/m);
      return match ? match[1] : path.basename(filePath, '.md');
    } catch (error) {
      return path.basename(filePath, '.md');
    }
  }

  // Count headings in document
  countHeadings(filePath) {
    try {
      const content = fs.readFileSync(filePath, 'utf-8');
      return (content.match(/^#{1,6}\s+.+$/gm) || []).length;
    } catch (error) {
      return 0;
    }
  }

  // Categorize document
  categorizeDoc(filePath) {
    const fileName = path.basename(filePath).toUpperCase();
    
    for (const [category, keywords] of Object.entries(CATEGORIES)) {
      if (category === 'Other') continue;
      for (const keyword of keywords) {
        if (fileName.includes(keyword)) {
          return category;
        }
      }
    }
    return 'Other';
  }

  // Get relative path from project root
  getRelativePath(filePath) {
    return path.relative(PROJECT_ROOT, filePath);
  }

  // Extract key info from document
  extractKeyInfo(filePath) {
    try {
      const content = fs.readFileSync(filePath, 'utf-8');
      const lines = content.split('\n').slice(0, 20).join('\n');
      
      // Look for key phrases
      const hasEndpoints = /endpoint|api|route|@RestController/i.test(content);
      const hasCode = /```/.test(content);
      const hasTables = /\|.*\|.*\|/.test(content);
      
      const tags = [];
      if (hasEndpoints) tags.push('📡 APIs');
      if (hasCode) tags.push('💻 Code');
      if (hasTables) tags.push('📊 Tables');
      
      return tags.join(' ');
    } catch (error) {
      return '';
    }
  }

  // Check if document is outdated (not modified in 90 days)
  isOutdated(filePath) {
    try {
      const relativePath = path.relative(PROJECT_ROOT, filePath);
      const timestamp = execSync(`git log -1 --format=%at "${relativePath}"`, {
        cwd: PROJECT_ROOT,
        encoding: 'utf-8'
      }).trim();
      
      if (!timestamp) return false;
      
      const lastModified = new Date(parseInt(timestamp) * 1000);
      const now = new Date();
      const daysDiff = (now - lastModified) / (1000 * 60 * 60 * 24);
      
      return daysDiff > 90;
    } catch (error) {
      return false;
    }
  }

  // Generate the index
  generate() {
    console.log('🔍 Scanning for markdown files...');
    const files = this.findMarkdownFiles(PROJECT_ROOT);
    console.log(`📄 Found ${files.length} markdown files`);

    // Process each file
    files.forEach(filePath => {
      const doc = {
        path: filePath,
        relativePath: this.getRelativePath(filePath),
        title: this.getTitle(filePath),
        category: this.categorizeDoc(filePath),
        lastModified: this.getLastModified(filePath),
        size: this.getFileSize(filePath),
        headings: this.countHeadings(filePath),
        tags: this.extractKeyInfo(filePath),
        outdated: this.isOutdated(filePath)
      };

      this.docs.push(doc);

      // Group by category
      if (!this.categories[doc.category]) {
        this.categories[doc.category] = [];
      }
      this.categories[doc.category].push(doc);
    });

    // Sort categories
    Object.keys(this.categories).forEach(category => {
      this.categories[category].sort((a, b) => a.title.localeCompare(b.title));
    });

    console.log('📝 Generating index...');
    this.generateIndexFile();
    this.generateNavigationFile();
    console.log(`✅ Documentation index generated: ${OUTPUT_FILE}`);
    console.log(`✅ Navigation generated: ${NAV_FILE}`);
  }

  // Generate main index file
  generateIndexFile() {
    let content = `# 📚 Documentation Index

*Last generated: ${new Date().toLocaleString()}*

## Overview

This index contains all ${this.docs.length} markdown documents in the project, organized by category.

🔴 Red flag = Document hasn't been updated in 90+ days  
📡 = Contains API/endpoint documentation  
💻 = Contains code examples  
📊 = Contains tables/data

---

## 📊 Statistics

`;

    // Add statistics
    const stats = {
      total: this.docs.length,
      outdated: this.docs.filter(d => d.outdated).length,
      withAPIs: this.docs.filter(d => d.tags.includes('📡')).length,
      withCode: this.docs.filter(d => d.tags.includes('💻')).length
    };

    content += `- **Total Documents**: ${stats.total}\n`;
    content += `- **Outdated** (90+ days): ${stats.outdated}\n`;
    content += `- **With API Documentation**: ${stats.withAPIs}\n`;
    content += `- **With Code Examples**: ${stats.withCode}\n`;
    content += `\n---\n\n`;

    // Add categories
    const sortedCategories = Object.keys(this.categories).sort();
    
    content += `## 📂 Quick Navigation\n\n`;
    sortedCategories.forEach(category => {
      const count = this.categories[category].length;
      const anchor = category.toLowerCase().replace(/[^a-z0-9]+/g, '-');
      content += `- [${category}](#${anchor}) (${count})\n`;
    });
    content += `\n---\n\n`;

    // Add each category
    sortedCategories.forEach(category => {
      content += `## ${category}\n\n`;
      content += `| Document | Location | Last Modified | Size | Tags |\n`;
      content += `|----------|----------|---------------|------|------|\n`;

      this.categories[category].forEach(doc => {
        const outdatedFlag = doc.outdated ? ' 🔴' : '';
        const title = `${doc.title}${outdatedFlag}`;
        const link = `[${title}](${doc.relativePath})`;
        const location = path.dirname(doc.relativePath);
        const lastMod = doc.lastModified.split(' ')[0] || 'Unknown';
        
        content += `| ${link} | \`${location}\` | ${lastMod} | ${doc.size} | ${doc.tags} |\n`;
      });

      content += `\n`;
    });

    // Add outdated documents section if any
    const outdatedDocs = this.docs.filter(d => d.outdated);
    if (outdatedDocs.length > 0) {
      content += `\n---\n\n## 🔴 Outdated Documents\n\n`;
      content += `These documents haven't been updated in 90+ days:\n\n`;
      outdatedDocs.forEach(doc => {
        content += `- [${doc.title}](${doc.relativePath}) - Last modified: ${doc.lastModified.split(' ')[0]}\n`;
      });
    }

    fs.writeFileSync(OUTPUT_FILE, content);
  }

  // Generate navigation file
  generateNavigationFile() {
    let content = `# 🧭 Documentation Navigation

*Quick links to all documentation*

`;

    const sortedCategories = Object.keys(this.categories).sort();
    
    sortedCategories.forEach(category => {
      content += `\n## ${category}\n\n`;
      this.categories[category].forEach(doc => {
        const indent = doc.relativePath.includes('docs/') ? '  ' : '';
        content += `${indent}- [${doc.title}](${doc.relativePath})\n`;
      });
    });

    fs.writeFileSync(NAV_FILE, content);
  }
}

// Run the generator
if (require.main === module) {
  const generator = new DocIndexGenerator();
  generator.generate();
}

module.exports = DocIndexGenerator;
