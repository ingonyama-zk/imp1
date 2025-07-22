#!/bin/bash

# IMP1 v0.2.2 Release Script
# This script helps create a GitHub release with the built artifacts

set -e

VERSION="0.2.2"
RELEASE_TAG="v${VERSION}"
RELEASE_BRANCH="stas/release/v${VERSION}"

echo "🚀 Creating IMP1 v${VERSION} Release"
echo "=================================="

# Check if we're on the correct branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "$RELEASE_BRANCH" ]; then
    echo "❌ Error: Must be on branch $RELEASE_BRANCH"
    echo "Current branch: $CURRENT_BRANCH"
    exit 1
fi

# Check if artifacts exist
ANDROID_AAR="android/imp1/app/build/outputs/aar/imp1-${VERSION}.aar"
ANDROID_DEBUG_AAR="android/imp1/app/build/outputs/aar/imp1-${VERSION}-debug.aar"
IOS_XCFRAMEWORK="ios/Build/imp1.xcframework.zip"

echo "📦 Checking artifacts..."
if [ ! -f "$ANDROID_AAR" ]; then
    echo "❌ Android AAR not found: $ANDROID_AAR"
    exit 1
fi

if [ ! -f "$ANDROID_DEBUG_AAR" ]; then
    echo "❌ Android Debug AAR not found: $ANDROID_DEBUG_AAR"
    exit 1
fi

if [ ! -f "$IOS_XCFRAMEWORK" ]; then
    echo "❌ iOS XCFramework not found: $IOS_XCFRAMEWORK"
    exit 1
fi

echo "✅ All artifacts found!"

# Create release notes
RELEASE_NOTES="RELEASE_NOTES_v${VERSION}.md"
if [ ! -f "$RELEASE_NOTES" ]; then
    echo "❌ Release notes not found: $RELEASE_NOTES"
    exit 1
fi

echo "📝 Release notes found: $RELEASE_NOTES"

# Instructions for manual GitHub release
echo ""
echo "🎯 Next Steps (Manual GitHub Release):"
echo "======================================"
echo ""
echo "1. Go to GitHub: https://github.com/ingonyama-zk/imp1/releases/new"
echo ""
echo "2. Create new release with:"
echo "   - Tag: $RELEASE_TAG"
echo "   - Title: IMP1 v${VERSION} - Parallel Proof Generation"
echo "   - Branch: $RELEASE_BRANCH"
echo ""
echo "3. Copy release notes from: $RELEASE_NOTES"
echo ""
echo "4. Upload these artifacts:"
echo "   - $ANDROID_AAR"
echo "   - $ANDROID_DEBUG_AAR"
echo "   - $IOS_XCFRAMEWORK"
echo ""
echo "5. Publish the release"
echo ""
echo "📊 Artifact Sizes:"
echo "   Android AAR: $(du -h "$ANDROID_AAR" | cut -f1)"
echo "   Android Debug AAR: $(du -h "$ANDROID_DEBUG_AAR" | cut -f1)"
echo "   iOS XCFramework: $(du -h "$IOS_XCFRAMEWORK" | cut -f1)"
echo ""

# Optional: Create a draft release using GitHub CLI (if available)
if command -v gh &> /dev/null; then
    echo "🔧 GitHub CLI detected. Would you like to create a draft release? (y/n)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo "Creating draft release..."
        gh release create "$RELEASE_TAG" \
            --draft \
            --title "IMP1 v${VERSION} - Parallel Proof Generation" \
            --notes-file "$RELEASE_NOTES" \
            "$ANDROID_AAR" \
            "$ANDROID_DEBUG_AAR" \
            "$IOS_XCFRAMEWORK"
        echo "✅ Draft release created!"
    fi
else
    echo "💡 Tip: Install GitHub CLI (gh) for automated release creation"
fi

echo ""
echo "🎉 Release preparation complete!"
echo "📋 Remember to:"
echo "   - Test the artifacts in a clean environment"
echo "   - Update documentation links"
echo "   - Announce the release to the community" 