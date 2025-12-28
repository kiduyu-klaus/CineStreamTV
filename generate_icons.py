#!/usr/bin/env python3
"""
CineStream TV Icon Generator
This script generates Android app icons from SVG/vector sources
"""

import os
import subprocess
import sys

def check_requirements():
    """Check if required tools are available"""
    try:
        subprocess.run(['rsvg-convert', ' --version'], 
                      capture_output=True, check=True)
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False

def create_directories():
    """Create required icon directories"""
    dirs = [
        'app/src/main/res/mipmap-mdpi',
        'app/src/main/res/mipmap-hdpi', 
        'app/src/main/res/mipmap-xhdpi',
        'app/src/main/res/mipmap-xxhdpi',
        'app/src/main/res/mipmap-xxxhdpi'
    ]
    
    for dir_path in dirs:
        os.makedirs(dir_path, exist_ok=True)
        print(f"📁 Created: {dir_path}")

def generate_icon(source_file, output_file, size):
    """Generate an icon of specified size"""
    try:
        cmd = [
            'rsvg-convert', '-w', str(size), '-h', str(size),
            source_file, '-o', output_file
        ]
        subprocess.run(cmd, check=True, capture_output=True)
        print(f"✅ Generated: {output_file} ({size}x{size})")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ Failed to generate {output_file}: {e}")
        return False

def main():
    print("🎬 CineStream TV Icon Generator")
    print("=" * 40)
    
    # Check requirements
    if not check_requirements():
        print("❌ rsvg-convert not found!")
        print("Install with:")
        print("  Ubuntu/Debian: sudo apt-get install librsvg2-bin")
        print("  macOS: brew install librsvg")
        print("  Windows: Install from https://github.com/behdad/rsvg/releases")
        sys.exit(1)
    
    # Create directories
    print("\n📁 Creating directories...")
    create_directories()
    
    # Define icon sizes for different densities
    densities = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }
    
    print("\n🎨 Generating app icons...")
    
    # Generate launcher icons for each density
    for density, size in densities.items():
        # Regular launcher icon
        source = 'app/src/main/res/drawable/ic_launcher_foreground.xml'
        output = f'app/src/main/res/mipmap-{density}/ic_launcher.png'
        generate_icon(source, output, size)
        
        # Round launcher icon
        output_round = f'app/src/main/res/mipmap-{density}/ic_launcher_round.png'
        generate_icon(source, output_round, size)
    
    print("\n🖼️  Generating banner icons...")
    
    # Generate banner icons
    banner_source = 'app/src/main/res/drawable/ic_cinestream_banner.xml'
    
    # Standard banner (320x180)
    banner_output = 'app/src/main/res/drawable/cinestream_banner.png'
    generate_icon(banner_source, banner_output, 320)
    
    # High-res banner (1280x720)
    banner_hd_output = 'app/src/main/res/drawable/cinestream_banner_hd.png'
    generate_icon(banner_source, banner_hd_output, 1280)
    
    print("\n🎉 Icon generation complete!")
    print("\n📋 Generated files:")
    print("   • Adaptive icons: Already in mipmap-anydpi-v26/")
    print("   • Legacy PNG icons: In respective density folders")
    print("   • Banner PNG icons: In drawable/ folder")
    
    print("\n🔧 Next steps:")
    print("   1. Verify icon appearance and quality")
    print("   2. Test on different device densities") 
    print("   3. Build and deploy the Android app")
    print("   4. Check icon display on Android TV home screen")

if __name__ == '__main__':
    main()
    
    
    
    
C:\Users\Administrator>C:/Users/Administrator/AppData/Local/Programs/Python/Python310/python.exe c:/Users/Administrator/AndroidStudioProjects/CineStreamTV/generate_icons.py
🎬 CineStream TV Icon Generator
========================================

📁 Creating directories...
📁 Created: app/src/main/res/mipmap-mdpi
📁 Created: app/src/main/res/mipmap-hdpi
📁 Created: app/src/main/res/mipmap-xhdpi
📁 Created: app/src/main/res/mipmap-xxhdpi
📁 Created: app/src/main/res/mipmap-xxxhdpi

🎨 Generating app icons...
✅ Generated: app/src/main/res/mipmap-mdpi/ic_launcher.png (48x48)
✅ Generated: app/src/main/res/mipmap-mdpi/ic_launcher_round.png (48x48)
✅ Generated: app/src/main/res/mipmap-hdpi/ic_launcher.png (72x72)
✅ Generated: app/src/main/res/mipmap-hdpi/ic_launcher_round.png (72x72)
✅ Generated: app/src/main/res/mipmap-xhdpi/ic_launcher.png (96x96)
✅ Generated: app/src/main/res/mipmap-xhdpi/ic_launcher_round.png (96x96)
✅ Generated: app/src/main/res/mipmap-xxhdpi/ic_launcher.png (144x144)
✅ Generated: app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png (144x144)
✅ Generated: app/src/main/res/mipmap-xxxhdpi/ic_launcher.png (192x192)
✅ Generated: app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png (192x192)

🖼️  Generating banner icons...
❌ Failed to generate app/src/main/res/drawable/cinestream_banner.png: Command '['rsvg-convert', '-w', '320', '-h', '320', 'app/src/main/res/drawable/ic_cinestream_banner.xml', '-o', 'app/src/main/res/drawable/cinestream_banner.png']' returned non-zero exit status 1.
❌ Failed to generate app/src/main/res/drawable/cinestream_banner_hd.png: Command '['rsvg-convert', '-w', '1280', '-h', '1280', 'app/src/main/res/drawable/ic_cinestream_banner.xml', '-o', 'app/src/main/res/drawable/cinestream_banner_hd.png']' returned non-zero exit status 1.