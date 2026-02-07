# Computer Vision Tasks Analysis

## Overview
This document identifies all tasks and issues related to the computer vision portion of the Technovation MS tracking application. The app uses MediaPipe pose detection to analyze exercise movements and track MS symptoms over time.

## Computer Vision Stack
- **Library**: Google MediaPipe (tasks-vision 0.10.11)
- **Features**: Pose Landmarker for body tracking
- **Camera**: CameraX for Android camera integration
- **Processing**: Real-time pose detection and analysis

## Computer Vision Related Issues

### Active Development (Open Issues)

#### High Priority - Core Computer Vision

**Issue #40: Connect Mediapipe model to camera app using the GitHub repo from the mediapipe website**
- Status: OPEN
- Category: Integration
- Description: Core task to integrate MediaPipe pose detection with the camera
- Related Files: `RecordFragment.java`, `OverlayView.java`
- Notes: This is currently partially implemented with PoseLandmarker setup

**Issue #39: Translate Python Code to Java**
- Status: OPEN
- Category: Code Translation
- Description: Translate balance detection code from Python to Java for Android
- Related Files: `app/src/Main.ipynb` (Python), needs Java implementation
- Notes: Duplicate of #38

**Issue #38: Translate python code to java**
- Status: OPEN
- Category: Code Translation
- Description: Convert Python computer vision algorithms to Java
- Notes: Duplicate of #39

**Issue #28: Coding the formula/computer vision**
- Status: OPEN
- Category: Algorithm Implementation
- Description: Implement the core computer vision formulas and algorithms
- Related to: Balance, spasticity, and gait analysis calculations

#### Medium Priority - Data Integration

**Issue #41: Connect the models' data to the graphs**
- Status: OPEN
- Category: Data Pipeline
- Description: Connect computer vision output (dual task, sensory ataxia, spasticity) to graph visualization
- Assigned to: Jenny (dual task, sensory ataxia, spasticity), Manreen (balance, speed, endurance)
- Related Files: Chart classes (WalkingSpeedChart.java, JointSpasticityChart.java)

**Issue #36: Connect data for balance, walking speed + endurance to graph**
- Status: OPEN
- Category: Data Visualization
- Description: Integrate balance, walking speed, and endurance metrics from CV to graphs
- Related Files: Chart implementations

**Issue #33: Connect output to graph**
- Status: OPEN
- Category: Data Visualization
- Description: Connect computer vision analysis output to graphing system

**Issue #32: Connect the functions to camera**
- Status: OPEN
- Category: Integration
- Description: Connect analysis functions to camera feed

**Issue #3: Link the coordinate/angle data to graph**
- Status: OPEN
- Category: Data Pipeline
- Description: Connect landmark coordinates and calculated joint angles to visualization

#### Lower Priority - Enhancement

**Issue #35: Finish Record UI + the buttons for doing tests**
- Status: OPEN
- Category: UI/Recording
- Description: Complete recording interface including baseline test before treatment
- Related Files: `fragment_record.xml`, `RecordFragment.java`
- Notes: Baseline test must be taken before treatment data starts (user input)

**Issue #30: Find another algorithm for detecting trends**
- Status: OPEN
- Category: Algorithm
- Description: Research and implement improved trend detection algorithm
- Notes: May involve CV data analysis

**Issue #29: Making live body metrics show**
- Status: OPEN
- Category: Real-time Display
- Description: Display live body metrics during pose detection
- Related Files: `OverlayView.java`, `RecordFragment.java`

#### Planning Issues

**Issue #13: December: Code computer vision**
- Status: OPEN
- Category: Planning
- Description: High-level planning for computer vision implementation
- Subtasks include connecting CV to phone camera, storing data, connecting data to graphs

**Issue #14: January: anomaly detection**
- Status: OPEN
- Category: Planning
- Description: Planning for anomaly detection in CV data

**Issue #15: February: Code report**
- Status: OPEN
- Category: Planning
- Description: Planning for report generation from CV analysis

### Completed (Closed Issues)

**Issue #11: Extract the angles and motion data we need from mediapipe** ✅
- Status: CLOSED
- Category: Data Extraction
- Description: Successfully extracted angles and motion data from MediaPipe
- Related Files: `AssessmentData.java` (JointAngleData class)

**Issue #10: Figure out the formulas we need for implementing the graphs** ✅
- Status: CLOSED
- Category: Algorithm Research
- Description: Researched formulas for knee angle, balance, muscular stiffness
- Reference: Google Doc with formulas
- Notes: Knee angle and balance formulas completed, muscular stiffness pending

**Issue #7: Code through YouTube tutorial until the end of joint calculation** ✅
- Status: CLOSED
- Category: Implementation
- Description: Completed coding joint calculation logic from tutorial

**Issue #6: Find out how to integrate python code into android studio through chaquopy** ✅
- Status: CLOSED (but changed approach)
- Category: Integration
- Description: Investigated Python integration (now translating to Java instead)

**Issue #1: Research what type of angle we need for the graphs** ✅
- Status: CLOSED
- Category: Research
- Description: Completed research on required angles for MS symptom tracking
- Reference: Google Doc

**Issue #27: Figure out how the camera will be opened** ✅
- Status: CLOSED
- Category: Camera Setup
- Description: Determined camera implementation approach using CameraX

## Current Implementation Status

### ✅ Completed
1. MediaPipe library integration (build.gradle.kts)
2. CameraX setup for real-time video capture
3. PoseLandmarker initialization with LIVE_STREAM mode
4. Basic pose detection pipeline (RecordFragment.java)
5. Overlay view for displaying pose landmarks (OverlayView.java)
6. Data structures for storing assessment data (AssessmentData.java)
7. Camera permission handling

### 🚧 In Progress
1. Python to Java code translation (Issues #38, #39)
2. Computer vision formula implementation (Issue #28)
3. MediaPipe model connection to camera (Issue #40)

### ⏳ Pending
1. Real-time metric calculations (balance, spasticity, asymmetry)
2. Data pipeline to graphs (Issues #3, #33, #36, #41)
3. Live body metrics display (Issue #29)
4. Recording UI completion (Issue #35)
5. Test type implementation (baseline, dual task, sensory ataxia)

## Key Files for Computer Vision

### Core Implementation
- `/app/src/main/java/com/example/myappication/RecordFragment.java` - Main CV controller
- `/app/src/main/java/com/example/myappication/OverlayView.java` - Pose visualization
- `/app/src/main/java/com/example/myappication/AssessmentData.java` - Data models

### Configuration
- `/app/build.gradle.kts` - MediaPipe dependencies
- `/gradle/libs.versions.toml` - Version management
- `/app/src/main/AndroidManifest.xml` - Permissions

### Resources
- `/app/src/main/res/layout/fragment_record.xml` - Recording UI
- `/app/src/Main.ipynb` - Python prototype code (needs translation)

### Analysis/Charts
- `/app/src/main/java/com/example/myappication/WalkingSpeedChart.java`
- `/app/src/main/java/com/example/myappication/JointSpasticityChart.java`

## Algorithms Needed (From Issue #10)

Based on the closed research issues, these formulas need implementation:

1. **Balance Score**
   - Center of Mass (CoM) calculation from hip landmarks
   - Base of Support (BoS) from heel landmarks
   - Balance stability metric

2. **Joint Angles** ✅ (partially done)
   - Knee angle calculation
   - Hip angle calculation
   - Ankle angle calculation
   - 12-joint gait analysis system

3. **Spasticity/Stiffness**
   - Joint range of motion analysis
   - Movement smoothness metrics
   - Asymmetry detection (left vs right)

4. **Walking Speed**
   - Distance traveled calculation from landmarks
   - Time-based velocity computation

5. **Endurance**
   - Long-term movement quality degradation
   - Fatigue detection

6. **Dual Task Analysis**
   - Performance comparison: normal vs cognitive load

7. **Sensory Ataxia**
   - Performance comparison: eyes open vs eyes closed

## Recommendations for Issue Organization

### Suggested Labels
1. `computer-vision` - Core CV tasks
2. `mediapipe` - MediaPipe specific issues
3. `camera` - Camera integration tasks
4. `data-pipeline` - Data flow from CV to storage/visualization
5. `algorithms` - Formula and calculation implementation
6. `python-to-java` - Code translation tasks

### Priority Grouping
- **P0 (Critical)**: #38, #39, #40, #28 - Core CV functionality
- **P1 (High)**: #3, #32, #33, #36, #41 - Data integration
- **P2 (Medium)**: #29, #35 - UI and display
- **P3 (Low)**: #30 - Algorithm improvements

### Suggested Next Steps
1. Complete Python to Java translation (Issues #38, #39)
2. Finish MediaPipe camera connection (Issue #40)
3. Implement core formulas (Issue #28)
4. Connect data pipeline to graphs (Issues #3, #33, #36, #41)
5. Complete recording UI (Issue #35)

## Notes
- The project uses MediaPipe Pose Landmarker which provides 33 body landmarks
- Currently implements a 12-joint simplified model for gait analysis
- Three test types planned: Baseline, Dual Task (counting), Sensory Ataxia (eyes closed)
- Target users: People with Multiple Sclerosis tracking disease progression
