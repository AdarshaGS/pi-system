#!/usr/bin/env python3
"""Fix insurance module imports and old loan references"""

import re
from pathlib import Path

def fix_insurance_imports():
    """Fix all insurance imports removing .insurance.insurance. pattern"""
    base_dir = Path('src/main/java/com/pisystem')
    
    # Find all Java files
    fixed_count = 0
    for java_file in base_dir.rglob('*.java'):
        try:
            with open(java_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            
            # Fix insurance imports
            content = re.sub(
                r'import com\.pisystem\.modules\.insurance\.insurance\.',
                'import com.pisystem.modules.insurance.',
                content
            )
            
            # Fix old loan reference
            content = re.sub(
                r'\bcom\.loan\.data\.Loan\b',
                'com.pisystem.modules.loans.data.Loan',
                content
            )
            
            # Write if changed
            if content != original_content:
                with open(java_file, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Fixed: {java_file.relative_to(base_dir.parent.parent.parent.parent)}")
                fixed_count += 1
        
        except Exception as e:
            print(f"Error processing {java_file}: {e}")
    
    print(f"\nFixed {fixed_count} files")

if __name__ == '__main__':
    fix_insurance_imports()
