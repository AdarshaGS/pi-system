#!/usr/bin/env python3
"""Fix all API imports to point to correct feature services"""

import re
from pathlib import Path

# Map of APIs to their feature locations
API_MAPPINGS = {
    'insuranceApi': 'features/insurance/services',
    'taxApi': 'features/tax/services',
    'lendingApi': 'features/lending/services',
    'goalsApi': 'features/goals/services',
    'documentsApi': 'features/documents/services',
    'creditScoreApi': 'features/creditScore/services',
    'cashFlowApi': 'features/budget/services',
    'recurringTransactionsApi': 'features/budget/services',
    'rebalancingApi': 'features/portfolio/services',
    'retirementPlanningApi': 'features/goals/services',
}

def fix_api_imports(file_path):
    """Fix API imports in a file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original = content
        
        # Fix each API import
        for api_name, feature_path in API_MAPPINGS.items():
            # Fix @/core/api/xyz → @/features/.../services/xyz
            content = re.sub(
                rf"from ['\"]\@/core/api/{api_name}['\"]",
                f"from '@/{feature_path}/{api_name}'",
                content
            )
            # Fix relative paths like ../../api/xyz → ../services/xyz (for components)
            content = re.sub(
                rf"from ['\"]\.\./\.\./api/{api_name}['\"]",
                f"from '../services/{api_name}'",
                content
            )
            # Fix from @/core/api → @/core/api.js
            content = re.sub(
                r"from ['\"]@/core/api['\"]",
                "from '@/core/api.js'",
                content
            )
        
        if content != original:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"Error: {file_path}: {e}")
        return False

def main():
    base = Path('/Users/adarshgs/Documents/Stocks/App/pi-system/frontend/src')
    
    fixed = 0
    for file in base.rglob('*.jsx'):
        if 'node_modules' in str(file):
            continue
        if fix_api_imports(file):
            print(f"Fixed: {file.relative_to(base)}")
            fixed += 1
    
    for file in base.rglob('*.js'):
        if 'node_modules' in str(file) or 'vite.config' in str(file):
            continue
        if fix_api_imports(file):
            print(f"Fixed: {file.relative_to(base)}")
            fixed += 1
    
    print(f"\nFixed {fixed} files")

if __name__ == '__main__':
    main()
