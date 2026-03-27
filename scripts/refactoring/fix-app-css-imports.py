#!/usr/bin/env python3
"""Fix all CSS imports in frontend files"""

import re
from pathlib import Path

def fix_app_css_imports(file_path):
    """Fix App.css imports based on file location"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original = content
        
        # Replace any ../../App.css or ../App.css with @/App.css
        content = re.sub(
            r"import ['\"]\.\.?/\.\.?/?App\.css['\"];?",
            "import '@/App.css';",
            content
        )
        content = re.sub(
            r"import ['\"]\.\.?/\.\.?/\.\.?/?App\.css['\"];?",
            "import '@/App.css';",
            content
        )
        
        # Fix index.css imports as well
        content = re.sub(
            r"import ['\"]\.\.?/\.\.?/?index\.css['\"];?",
            "import '@/index.css';",
            content
        )
        
        if content != original:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False

def main():
    base = Path('/Users/adarshgs/Documents/Stocks/App/pi-system/frontend/src')
    
    fixed = 0
    for file in base.rglob('*.jsx'):
        if 'node_modules' in str(file):
            continue
        if fix_app_css_imports(file):
            print(f"Fixed: {file.relative_to(base)}")
            fixed += 1
    
    print(f"\nFixed {fixed} files")

if __name__ == '__main__':
    main()
