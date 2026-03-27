#!/usr/bin/env python3
"""
Fix double-nested package declarations in Java files.
Replaces patterns like:
  package com.pisystem.core.auth.auth.service;
with:
  package com.pisystem.core.auth.service;
"""

import os
import re
from pathlib import Path

# Patterns to fix (old_pattern -> new_pattern)
PATTERNS = [
    # Core modules
    (r'package com\.pisystem\.core\.auth\.auth\.', 'package com.pisystem.core.auth.'),
    (r'package com\.pisystem\.core\.users\.users\.', 'package com.pisystem.core.users.'),
    (r'package com\.pisystem\.core\.admin\.admin\.', 'package com.pisystem.core.admin.'),
    
    # Business modules
    (r'package com\.pisystem\.modules\.budget\.budget\.', 'package com.pisystem.modules.budget.'),
    (r'package com\.pisystem\.modules\.tax\.tax\.', 'package com.pisystem.modules.tax.'),
    (r'package com\.pisystem\.modules\.lending\.lending\.', 'package com.pisystem.modules.lending.'),
    (r'package com\.pisystem\.modules\.savings\.savings\.', 'package com.pisystem.modules.savings.'),
    (r'package com\.pisystem\.modules\.etf\.etf\.', 'package com.pisystem.modules.etf.'),
    (r'package com\.pisystem\.modules\.mutualfunds\.mutualfund\.', 'package com.pisystem.modules.mutualfunds.'),
    (r'package com\.pisystem\.modules\.loans\.loan\.', 'package com.pisystem.modules.loans.'),
    (r'package com\.pisystem\.modules\.insurance\.protection\.', 'package com.pisystem.modules.insurance.'),
    (r'package com\.pisystem\.modules\.insurance\.insurance\.', 'package com.pisystem.modules.insurance.'),
    (r'package com\.pisystem\.modules\.upi\.upi\.', 'package com.pisystem.modules.upi.'),
    (r'package com\.pisystem\.modules\.portfolio\.portfolio\.', 'package com.pisystem.modules.portfolio.'),
    (r'package com\.pisystem\.modules\.stocks\.stocks\.', 'package com.pisystem.modules.stocks.'),
    (r'package com\.pisystem\.modules\.sms\.sms\.', 'package com.pisystem.modules.sms.'),
    (r'package com\.pisystem\.modules\.ai\.ai\.', 'package com.pisystem.modules.ai.'),
    
    # Integrations/Infrastructure/Shared
    (r'package com\.pisystem\.integrations\.accountaggregator\.aa\.', 'package com.pisystem.integrations.accountaggregator.'),
    (r'package com\.pisystem\.shared\.audit\.audit\.', 'package com.pisystem.shared.audit.'),
    (r'package com\.pisystem\.infrastructure\.websocket\.websocket\.', 'package com.pisystem.infrastructure.websocket.'),
]

def fix_file(file_path):
    """Fix package declaration in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Apply all patterns
        for old_pattern, new_pattern in PATTERNS:
            content = re.sub(old_pattern, new_pattern, content)
        
        # Only write if content changed
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        
        return False
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False

def main():
    """Fix all Java files in the pisystem directory"""
    base_dir = Path(__file__).parent.parent.parent / 'src' / 'main' / 'java' / 'com' / 'pisystem'
    
    if not base_dir.exists():
        print(f"Directory not found: {base_dir}")
        return 1
    
    fixed_count = 0
    total_count = 0
    
    # Find all Java files
    for java_file in base_dir.rglob('*.java'):
        total_count += 1
        if fix_file(java_file):
            fixed_count += 1
            print(f"Fixed: {java_file.relative_to(base_dir)}")
    
    print(f"\nProcessed {total_count} files")
    print(f"Fixed {fixed_count} files")
    
    return 0

if __name__ == '__main__':
    exit(main())
