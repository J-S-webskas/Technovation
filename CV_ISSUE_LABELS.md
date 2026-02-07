# Computer Vision Issue Labels - Quick Reference

## Summary
This document provides a quick reference for identifying which issues are related to computer vision in the Technovation MS tracking app.

## How to Identify Computer Vision Issues

Computer vision issues involve:
- 📹 Camera integration and video capture
- 🤖 MediaPipe pose detection
- 📐 Joint angle calculations
- 📊 Extracting metrics from body landmarks (balance, spasticity, walking speed)
- 🔄 Converting Python CV code to Java
- 🎯 Real-time body tracking and analysis
- 📈 Connecting pose data to graphs

## Computer Vision Issue List

### 🔴 Critical Priority - Foundation Work

| Issue # | Title | Type | Status |
|---------|-------|------|--------|
| #38 | Translate python code to java | Code Translation | OPEN |
| #39 | Translate Python Code to Java | Code Translation | OPEN |
| #40 | Connect Mediapipe model to camera app | Integration | OPEN |
| #28 | Coding the formula/computer vision | Algorithm | OPEN |

### 🟡 High Priority - Data Flow

| Issue # | Title | Type | Status |
|---------|-------|------|--------|
| #41 | Connect the models' data to the graphs | Data Pipeline | OPEN |
| #36 | Connect data for balance, walking speed + endurance to graph | Data Pipeline | OPEN |
| #33 | Connect output to graph | Data Pipeline | OPEN |
| #32 | Connect the functions to camera | Integration | OPEN |
| #3 | Link the coordinate/angle data to graph | Data Pipeline | OPEN |

### 🟢 Medium Priority - UI & Features

| Issue # | Title | Type | Status |
|---------|-------|------|--------|
| #35 | Finish Record UI + the buttons for doing tests | UI | OPEN |
| #29 | Making live body metrics show | Display | OPEN |
| #30 | Find another algorithm for detecting trends | Enhancement | OPEN |

### 📋 Planning Issues

| Issue # | Title | Type | Status |
|---------|-------|------|--------|
| #13 | December: Code computer vision | Planning | OPEN |
| #14 | January: anomaly detection | Planning | OPEN |
| #15 | February: Code report | Planning | OPEN |

### ✅ Completed Issues

| Issue # | Title | Type | Status |
|---------|-------|------|--------|
| #11 | Extract the angles and motion data from mediapipe | Data Extraction | CLOSED |
| #10 | Figure out the formulas for implementing the graphs | Research | CLOSED |
| #7 | Code through tutorial until joint calculation | Implementation | CLOSED |
| #6 | Integrate python code via chaquopy | Research | CLOSED |
| #1 | Research what angles needed for graphs | Research | CLOSED |
| #27 | Figure out how camera will be opened | Setup | CLOSED |

## NOT Computer Vision Issues

These issues are related to other parts of the app:

### Business/Documentation (NOT CV)
- Issues #44-55: Business canvas, pitch video, market research, etc.
- Issue #37: Pitch video planning
- Issue #25: User testing

### UI/Design (NOT CV)  
- Issue #43: Trend fragment XML elevation
- Issue #42: Home fragment getting cut off
- Issue #16: Notifications/reminders
- Issue #5: Backward navigation flow in Figma

### Other Closed Issues (NOT CV)
- Issue #34: Decide how many graphs
- Issue #31: Plan video outlines
- Issue #23-26: UI/Reports/Doctor contacts
- Issue #20-22: UI refinements
- Issue #17-19: Figma and XML work
- Issue #9: Data storage (partially CV-adjacent)
- Issue #8: Email drafting
- Issue #2: Figma UI enhancements
- Issue #4: Build the app (general)

## Quick Filter Guide

### To find CV issues, look for keywords:
- ✅ "mediapipe", "camera", "vision", "pose"
- ✅ "angle", "landmark", "coordinate"
- ✅ "python to java" (CV algorithms)
- ✅ "balance", "spasticity", "walking speed", "endurance" (when about calculation/detection)
- ✅ "formula", "algorithm" (for body analysis)
- ✅ "connect to graph" (CV data pipeline)

### NOT CV issues have keywords like:
- ❌ "business canvas", "pitch", "market research"
- ❌ "UI", "XML", "figma" (unless about camera/recording screen)
- ❌ "navigation", "email", "user testing"
- ❌ "doctor", "professional feedback"

## Issue Tags by Technology

### MediaPipe Related
- #40, #28, #11, #7

### Camera Related
- #40, #32, #27

### Python/Java Translation
- #38, #39, #6

### Data Pipeline
- #41, #36, #33, #3

### Algorithm/Formula
- #28, #30, #10, #1

### UI (CV-specific)
- #35, #29

## Recommended Action Items

1. **Label these issues with `computer-vision`:** #3, #13, #14, #15, #28, #29, #30, #32, #33, #35, #36, #38, #39, #40, #41

2. **Create additional specific labels:**
   - `mediapipe` → #40, #28, #11
   - `camera-integration` → #40, #32, #27
   - `python-to-java` → #38, #39
   - `data-pipeline` → #41, #36, #33, #3
   - `algorithms` → #28, #30, #10

3. **Create milestones:**
   - "CV Core Implementation" → #38, #39, #40, #28
   - "Data Integration" → #3, #32, #33, #36, #41
   - "CV UI/Display" → #29, #35

## Contact
Created: 2026-02-07
For: J-S-webskas and team
Project: Technovation MS Tracking App
