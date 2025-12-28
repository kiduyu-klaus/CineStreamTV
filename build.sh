#!/bin/bash

# CineStream TV Player Build Script
# This script helps build and test the Android TV app

echo "🎬 CineStream TV Player Build Script"
echo "=================================="

# Check if Android SDK is set
if [ -z "$ANDROID_HOME" ]; then
    echo "❌ ANDROID_HOME not set. Please install Android SDK."
    exit 1
fi

# Build tasks
build_debug() {
    echo "🔨 Building debug APK..."
    ./gradlew assembleDebug
    if [ $? -eq 0 ]; then
        echo "✅ Debug build successful!"
        echo "📱 APK location: app/build/outputs/apk/debug/app-debug.apk"
    else
        echo "❌ Build failed!"
        exit 1
    fi
}

build_release() {
    echo "🔨 Building release APK..."
    ./gradlew assembleRelease
    if [ $? -eq 0 ]; then
        echo "✅ Release build successful!"
        echo "📱 APK location: app/build/outputs/apk/release/app-release.apk"
    else
        echo "❌ Build failed!"
        exit 1
    fi
}

run_tests() {
    echo "🧪 Running tests..."
    ./gradlew test
    if [ $? -eq 0 ]; then
        echo "✅ All tests passed!"
    else
        echo "❌ Some tests failed!"
        exit 1
    fi
}

install_debug() {
    echo "📱 Installing debug APK on connected device..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    if [ $? -eq 0 ]; then
        echo "✅ App installed successfully!"
        echo "🚀 Launching CineStream TV..."
        adb shell am start -n com.cinestream.tvplayer/.ui.main.MainActivity
    else
        echo "❌ Installation failed!"
        echo "💡 Make sure an Android TV device is connected via USB or WiFi"
    fi
}

clean_build() {
    echo "🧹 Cleaning project..."
    ./gradlew clean
    echo "🔨 Building debug APK..."
    ./gradlew assembleDebug
    if [ $? -eq 0 ]; then
        echo "✅ Clean build successful!"
    else
        echo "❌ Build failed!"
        exit 1
    fi
}

show_help() {
    echo "Usage: ./build.sh [command]"
    echo ""
    echo "Commands:"
    echo "  debug     - Build debug APK"
    echo "  release   - Build release APK"
    echo "  test      - Run unit tests"
    echo "  install   - Build and install debug APK"
    echo "  clean     - Clean and build debug APK"
    echo "  help      - Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./build.sh debug     # Build debug version"
    echo "  ./build.sh install   # Build and install on TV"
}

# Main script logic
case "$1" in
    "debug")
        build_debug
        ;;
    "release")
        build_release
        ;;
    "test")
        run_tests
        ;;
    "install")
        build_debug
        install_debug
        ;;
    "clean")
        clean_build
        ;;
    "help"|"")
        show_help
        ;;
    *)
        echo "❌ Unknown command: $1"
        show_help
        exit 1
        ;;
esac