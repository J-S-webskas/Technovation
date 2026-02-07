# Computer Vision Task Organization

## 📋 Overview

This directory contains documentation to help organize and identify computer vision-related tasks for the Technovation MS Tracking App project.

## 📚 Documentation Files

### 1. **COMPUTER_VISION_TASKS.md** - Comprehensive Analysis
The main document providing:
- Complete list of all CV-related issues (open and closed)
- Detailed descriptions and context for each issue
- Current implementation status
- Key files and algorithms
- Recommendations for next steps

👉 **Start here** for a deep dive into the project's computer vision work.

### 2. **CV_ISSUE_LABELS.md** - Quick Reference
A condensed guide for quick lookups:
- Table format listing all CV issues
- Priority levels (Critical, High, Medium)
- How to identify CV vs non-CV issues
- Keyword filtering guide
- Recommended labeling actions

👉 **Use this** when you need to quickly check if an issue is CV-related.

### 3. **CV_WORKFLOW.md** - Visual Diagrams
Visual representations showing:
- Issue dependencies and workflow phases
- Component architecture diagram
- File relationship maps
- Test type flow
- Timeline and priority ordering

👉 **Reference this** to understand how issues connect and what order to tackle them.

## 🎯 Computer Vision Issues Summary

### Statistics
- **Total Open Issues Related to CV**: 14
- **Total Closed CV Issues**: 6
- **Critical Priority**: 4 issues (#28, #38, #39, #40)
- **High Priority**: 5 issues (#3, #32, #33, #36, #41)

### Quick List of CV Issues

**🔴 Critical (Foundation Work)**
- #38, #39: Translate Python code to Java
- #40: Connect MediaPipe to camera
- #28: Implement CV formulas/algorithms

**🟡 High Priority (Data Integration)**
- #41: Connect models' data to graphs
- #36: Connect balance/speed/endurance to graph
- #33: Connect output to graph
- #32: Connect functions to camera
- #3: Link coordinate/angle data to graph

**🟢 Medium Priority (UI & Features)**
- #35: Finish Record UI + test buttons
- #29: Show live body metrics
- #30: Find better trend detection algorithm

**📋 Planning Issues**
- #13: December CV coding plan
- #14: January anomaly detection plan
- #15: February report plan

## 🚀 Quick Start Guide

### For Team Members

1. **Want to know if an issue is CV-related?**
   → Check `CV_ISSUE_LABELS.md` (Section: "Quick Filter Guide")

2. **Need to understand what work is pending?**
   → See `COMPUTER_VISION_TASKS.md` (Section: "Current Implementation Status")

3. **Planning your sprint/week?**
   → Review `CV_WORKFLOW.md` (Section: "Priority Order")

4. **Looking for specific files to edit?**
   → Check `COMPUTER_VISION_TASKS.md` (Section: "Key Files for Computer Vision")

### For New Contributors

1. Read `COMPUTER_VISION_TASKS.md` - "Overview" section
2. Look at `CV_WORKFLOW.md` - "Component Architecture" diagram
3. Review `CV_ISSUE_LABELS.md` - "Quick Filter Guide"
4. Pick a task from the Critical or High Priority list
5. Check dependencies in `CV_WORKFLOW.md` before starting

## 🏗️ Technology Stack

The app uses these technologies for computer vision:

- **MediaPipe Tasks Vision 0.10.11** - Pose detection
- **CameraX 1.5.2** - Camera integration
- **Android Studio** - Development environment
- **Java** - Primary language (translating from Python)

Key concepts:
- 33 body landmarks per frame
- Real-time pose detection
- 3 test types: Baseline, Dual Task, Sensory Ataxia
- 6 metrics: Balance, Spasticity, Walking Speed, Endurance, Joint Angles, Asymmetry

## 📝 Recommended Actions

### Immediate Actions
1. Add `computer-vision` label to issues: #3, #13, #14, #15, #28, #29, #30, #32, #33, #35, #36, #38, #39, #40, #41

### Optional Labels
- `mediapipe` → #40, #28
- `camera-integration` → #40, #32
- `python-to-java` → #38, #39
- `data-pipeline` → #41, #36, #33, #3
- `algorithms` → #28, #30

### Milestones to Create
1. **CV Core Implementation** (Critical)
   - Issues: #28, #38, #39, #40
   
2. **Data Integration** (High Priority)
   - Issues: #3, #32, #33, #36, #41
   
3. **CV UI/Display** (Medium Priority)
   - Issues: #29, #35

## 🤝 How This Helps

This organization helps your team:
- ✅ Quickly identify which issues are CV-related
- ✅ Understand dependencies between tasks
- ✅ Prioritize work effectively
- ✅ See the big picture of the CV implementation
- ✅ Onboard new team members faster
- ✅ Track progress through the CV pipeline

## 📞 Questions?

If you have questions about:
- **Specific algorithms**: See `COMPUTER_VISION_TASKS.md` → "Algorithms Needed"
- **File locations**: See `COMPUTER_VISION_TASKS.md` → "Key Files"
- **Task dependencies**: See `CV_WORKFLOW.md` → diagrams
- **Priority ordering**: See `CV_ISSUE_LABELS.md` → "Priority Grouping"

## 📅 Created

- Date: February 7, 2026
- Purpose: Help organize GitHub issues for Technovation competition
- For: J-S-webskas and team members

---

**Good luck with your project! You're doing great work helping people with MS track their disease progression. 🎯💪**
