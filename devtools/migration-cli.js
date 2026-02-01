#!/usr/bin/env node

/**
 * Migration Generator CLI Tool
 * 
 * Interacts with the Spring Boot backend to:
 * 1. Detect schema drift
 * 2. Generate migrations from entity changes
 * 3. Save schema snapshots
 * 4. View current schema
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const BASE_URL = process.env.API_URL || 'http://localhost:8082';
const API_PREFIX = '/open/dev/migration';

// ANSI color codes
const colors = {
    reset: '\x1b[0m',
    bright: '\x1b[1m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    cyan: '\x1b[36m'
};

function colorize(text, color) {
    return `${colors[color]}${text}${colors.reset}`;
}

// HTTP request helper
function makeRequest(endpoint, method = 'GET', data = null) {
    return new Promise((resolve, reject) => {
        const url = new URL(`${API_PREFIX}${endpoint}`, BASE_URL);
        
        const options = {
            hostname: url.hostname,
            port: url.port || 8080,
            path: url.pathname + url.search,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                try {
                    const response = JSON.parse(body);
                    if (res.statusCode >= 200 && res.statusCode < 300) {
                        resolve(response);
                    } else {
                        reject(new Error(response.message || 'Request failed'));
                    }
                } catch (e) {
                    reject(new Error('Invalid JSON response'));
                }
            });
        });

        req.on('error', reject);

        if (data) {
            req.write(JSON.stringify(data));
        }

        req.end();
    });
}

// Commands

async function detectDrift() {
    console.log(colorize('\n🔍 Detecting schema drift...', 'cyan'));
    
    try {
        const drift = await makeRequest('/detect-drift');
        
        if (!drift.hasChanges) {
            console.log(colorize('✅ No schema changes detected', 'green'));
            console.log('Database schema is in sync with JPA entities.');
            return;
        }

        console.log(colorize(`\n⚠️  Detected ${drift.changeCount} schema changes:`, 'yellow'));
        
        const changesByType = {};
        drift.changes.forEach(change => {
            if (!changesByType[change.type]) {
                changesByType[change.type] = [];
            }
            changesByType[change.type].push(change);
        });

        Object.entries(changesByType).forEach(([type, changes]) => {
            console.log(colorize(`\n${type}:`, 'bright'));
            changes.forEach(change => {
                console.log(`  • ${change.description}`);
            });
        });

        console.log(colorize('\n💡 Run "npm run migrate:generate" to create migration', 'cyan'));
    } catch (error) {
        console.error(colorize(`❌ Error: ${error.message}`, 'red'));
        console.log('\n📝 Make sure your Spring Boot backend is running on', BASE_URL);
    }
}

async function generateMigration(description) {
    console.log(colorize('\n🚀 Generating migration from entities...', 'cyan'));
    
    try {
        const response = await makeRequest(
            `/generate-from-entities?description=${encodeURIComponent(description)}`,
            'POST'
        );
        
        if (!response.schemaChanges || response.schemaChanges.length === 0) {
            console.log(colorize('✅ ' + response.message, 'green'));
            return;
        }

        console.log(colorize(`\n✅ Migration generated: ${response.fileName}`, 'green'));
        console.log(colorize(`📁 Location: ${response.fullPath}`, 'blue'));
        
        console.log(colorize('\n📋 Changes included:', 'cyan'));
        response.schemaChanges.forEach(change => {
            console.log(`  • ${change}`);
        });

        console.log(colorize('\n📄 Migration SQL:', 'cyan'));
        console.log('─'.repeat(60));
        console.log(response.content);
        console.log('─'.repeat(60));
    } catch (error) {
        console.error(colorize(`❌ Error: ${error.message}`, 'red'));
    }
}

async function saveSnapshot() {
    console.log(colorize('\n💾 Saving schema snapshot...', 'cyan'));
    
    try {
        const response = await makeRequest('/schema/snapshot', 'POST');
        console.log(colorize('✅ ' + (response.message || JSON.stringify(response)), 'green'));
    } catch (error) {
        console.error(colorize(`❌ Error: ${error.message}`, 'red'));
    }
}

async function viewSchema() {
    console.log(colorize('\n📊 Current Schema from JPA Entities:', 'cyan'));
    
    try {
        const schema = await makeRequest('/schema/current');
        
        console.log(colorize(`\nFound ${schema.length} tables:\n`, 'bright'));
        
        schema.forEach(table => {
            console.log(colorize(`📋 ${table.tableName}`, 'green'));
            console.log(`   Class: ${table.className}`);
            console.log(`   Columns: ${table.columns.length}`);
            
            table.columns.forEach(col => {
                let info = `   • ${col.columnName} (${col.sqlType})`;
                if (col.primaryKey) info += ' PK';
                if (!col.nullable) info += ' NOT NULL';
                if (col.autoIncrement) info += ' AUTO_INCREMENT';
                console.log(info);
            });
            
            if (table.indexes.length > 0) {
                console.log(colorize(`   Indexes: ${table.indexes.length}`, 'yellow'));
                table.indexes.forEach(idx => {
                    console.log(`   • ${idx.name} (${idx.columnList})`);
                });
            }
            
            console.log('');
        });
    } catch (error) {
        console.error(colorize(`❌ Error: ${error.message}`, 'red'));
    }
}

function showHelp() {
    console.log(`
${colorize('🔧 Migration Generator CLI', 'cyan')}

${colorize('Usage:', 'bright')}
  npm run migrate:drift              Detect schema drift
  npm run migrate:generate [desc]    Generate migration from entities
  npm run migrate:snapshot           Save current schema snapshot
  npm run migrate:schema             View current schema

${colorize('Examples:', 'bright')}
  npm run migrate:drift
  npm run migrate:generate "Add user preferences table"
  npm run migrate:snapshot
  npm run migrate:schema

${colorize('Environment:', 'bright')}
  API_URL=${BASE_URL}

${colorize('Note:', 'yellow')}
  Make sure your Spring Boot backend is running before using these commands.
`);
}

// Main CLI logic
async function main() {
    const args = process.argv.slice(2);
    const command = args[0];

    if (!command || command === 'help' || command === '--help' || command === '-h') {
        showHelp();
        return;
    }

    switch (command) {
        case 'drift':
        case 'detect':
            await detectDrift();
            break;
        
        case 'generate':
        case 'create':
            const description = args.slice(1).join(' ') || 'Auto_Generated_Migration';
            await generateMigration(description);
            break;
        
        case 'snapshot':
        case 'save':
            await saveSnapshot();
            break;
        
        case 'schema':
        case 'view':
            await viewSchema();
            break;
        
        default:
            console.error(colorize(`❌ Unknown command: ${command}`, 'red'));
            console.log('Run "npm run migrate:help" for usage information');
            process.exit(1);
    }
}

// Run CLI
if (require.main === module) {
    main().catch(error => {
        console.error(colorize('❌ Unexpected error:', 'red'), error);
        process.exit(1);
    });
}

module.exports = { detectDrift, generateMigration, saveSnapshot, viewSchema };
