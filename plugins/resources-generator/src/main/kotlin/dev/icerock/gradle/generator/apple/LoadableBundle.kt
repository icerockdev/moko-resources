/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import java.io.File

class LoadableBundle(
    directory: File,
    bundleName: String,
    private val developmentRegion: String,
    private val identifier: String
) {
    val bundleDir: File = File(directory, "$bundleName.bundle")
    val contentsDir: File = File(bundleDir, "Contents")
    val infoPListFile: File = File(contentsDir, "Info.plist")
    val resourcesDir: File = File(contentsDir, "Resources")

    fun write() {
        bundleDir.mkdir()
        contentsDir.mkdir()
        resourcesDir.mkdir()

        writeInfoPList()
    }

    private fun writeInfoPList() {
        infoPListFile.writeText(
            """<?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0">
    <dict>
        <key>CFBundleDevelopmentRegion</key>
        <string>$developmentRegion</string>
        <key>CFBundleIdentifier</key>
        <string>$identifier</string>
        <key>CFBundleVersion</key>
        <string>1</string>
        <key>CFBundlePackageType</key>
        <string>BNDL</string>
    </dict>
    </plist>"""
        )
    }
}
