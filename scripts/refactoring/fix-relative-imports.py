#!/usr/bin/env python3
"""
Fix all remaining relative imports in frontend after restructuring.
Converts relative imports to @ alias imports.
"""

import os
import re
from pathlib import Path

def fix_imports_in_file(file_path, base_dir):
    """Fix relative imports in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Fix common relative imports
        fixes = [
            # API imports - now in core/api or features/*/services
            (r"from ['\"]\.\.?/\.\.?/api\.js['\"]", "from '@/core/api.js'"),
            (r"from ['\"]\.\.?/api\.js['\"]", "from '@/core/api.js'"),
            (r"from ['\"]\.\.?/\.\.?/api['\"]", "from '@/core/api'"),
            (r"from ['\"]\.\.?/api['\"]", "from '@/core/api'"),
            
            # Contexts
            (r"from ['\"]\.\.?/\.\.?/contexts/", "from '@/core/contexts/"),
            (r"from ['\"]\.\.?/contexts/", "from '@/core/contexts/"),
            
            # Utils
            (r"from ['\"]\.\.?/\.\.?/utils/", "from '@/shared/utils/"),
            (r"from ['\"]\.\.?/utils/", "from '@/shared/utils/"),
            
            # Websocket
            (r"from ['\"]\.\.?/\.\.?/websocket/", "from '@/core/websocket/"),
            (r"from ['\"]\.\.?/websocket/", "from '@/core/websocket/"),
            
            # Services
            (r"from ['\"]\.\.?/\.\.?/services/", "from '@/core/services/"),
            (r"from ['\"]\.\.?/services/", "from '@/core/services/"),
            
            # Layouts
            (r"from ['\"]\.\.?/\.\.?/layouts/", "from '@/shared/layouts/"),
            (r"from ['\"]\.\.?/layouts/", "from '@/shared/layouts/"),
        ]
        
        for pattern, replacement in fixes:
            content = re.sub(pattern, replacement, content)
        
        # Write if changed
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        
        return False
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False

def main():
    """Fix all frontend files"""
    base_dir = Path('/Users/adarshgs/Documents/Stocks/App/pi-system/frontend/src')
    
    if not base_dir.exists():
        print(f"Directory not found: {base_dir}")
        return 1
    
    updated_count = 0
    total_count = 0
    
    # Find all JSX/JS files (excluding node_modules)
    for ext in ['jsx', 'js']:
        for file in base_dir.rglob(f'*.{ext}'):
            if 'node_modules' in str(file) or 'dist' in str(file):
                continue
            
            total_count += 1
            if fix_imports_in_file(file, base_dir):
                updated_count += 1
                print(f"Fixed: {file.relative_to(base_dir)}")
    
    print(f"\nProcessed {total_count} files")
    print(f"Fixed {updated_count} files")
    
    return 0

if __name__ == '__main__':
    exit(main())
