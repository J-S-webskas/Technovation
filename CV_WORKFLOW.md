# Computer Vision Task Flow Diagram

## Issue Dependencies and Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                     COMPUTER VISION PIPELINE                        │
└─────────────────────────────────────────────────────────────────────┘

PHASE 1: FOUNDATION (Critical - Must Complete First)
═══════════════════════════════════════════════════════════════════════

┌─────────────────────┐
│   Issue #38, #39    │
│  Python → Java      │ ──┐
│   Translation       │   │
└─────────────────────┘   │
                          │
┌─────────────────────┐   ├──► ┌─────────────────────┐
│   Issue #40         │   │    │   Issue #28         │
│  Connect MediaPipe  │ ──┘    │  Implement CV       │
│   to Camera App     │        │  Formulas/Algos     │
└─────────────────────┘        └─────────────────────┘
         │                              │
         │                              │
         └──────────┬───────────────────┘
                    │
                    ▼

PHASE 2: DATA INTEGRATION (High Priority)
═══════════════════════════════════════════════════════════════════════

         ┌──────────────────────┐
         │   Issue #32          │
         │  Connect Functions   │
         │   to Camera          │
         └──────────────────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │   Issue #33          │
         │  Connect Output      │
         │   to Graph           │
         └──────────────────────┘
                    │
          ┌─────────┴─────────┐
          │                   │
          ▼                   ▼
┌──────────────────┐  ┌──────────────────┐
│   Issue #3       │  │   Issue #36      │
│  Link Coordinate │  │  Connect Balance │
│  /Angle Data     │  │  Speed, Endur.   │
│  to Graph        │  │  to Graph        │
└──────────────────┘  └──────────────────┘
          │                   │
          └─────────┬─────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │   Issue #41          │
         │  Connect ALL Model   │
         │  Data to Graphs      │
         │  (Integration Task)  │
         └──────────────────────┘

PHASE 3: UI & DISPLAY (Medium Priority)
═══════════════════════════════════════════════════════════════════════

         ┌──────────────────────┐
         │   Issue #35          │
         │  Complete Record UI  │
         │  + Test Buttons      │
         └──────────────────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │   Issue #29          │
         │  Show Live Body      │
         │  Metrics             │
         └──────────────────────┘

PHASE 4: ENHANCEMENT (Lower Priority)
═══════════════════════════════════════════════════════════════════════

         ┌──────────────────────┐
         │   Issue #30          │
         │  Better Trend        │
         │  Detection Algorithm │
         └──────────────────────┘


PLANNING ISSUES (Ongoing)
═══════════════════════════════════════════════════════════════════════

┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│   Issue #13     │   │   Issue #14     │   │   Issue #15     │
│  Dec: Code CV   │   │  Jan: Anomaly   │   │  Feb: Reports   │
│                 │   │   Detection     │   │                 │
└─────────────────┘   └─────────────────┘   └─────────────────┘
```

## Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Android App                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐         ┌──────────────────┐            │
│  │  RecordFragment  │◄────────│   CameraX        │            │
│  │  (Issue #35)     │         │   (Issue #40)    │            │
│  └────────┬─────────┘         └──────────────────┘            │
│           │                                                     │
│           │ uses                                                │
│           ▼                                                     │
│  ┌──────────────────┐         ┌──────────────────┐            │
│  │  MediaPipe Pose  │────────►│   OverlayView    │            │
│  │  Landmarker      │  draws  │   (Issue #29)    │            │
│  │  (Issue #40)     │         └──────────────────┘            │
│  └────────┬─────────┘                                          │
│           │                                                     │
│           │ outputs landmarks                                  │
│           ▼                                                     │
│  ┌──────────────────┐                                          │
│  │  CV Algorithms   │ ◄─── Issue #28, #38, #39               │
│  │  (Python→Java)   │                                          │
│  │                  │                                          │
│  │  • Balance       │                                          │
│  │  • Spasticity    │                                          │
│  │  • Walking Speed │                                          │
│  │  • Joint Angles  │                                          │
│  └────────┬─────────┘                                          │
│           │                                                     │
│           │ generates metrics                                  │
│           ▼                                                     │
│  ┌──────────────────┐         ┌──────────────────┐            │
│  │ AssessmentData   │────────►│  Chart Classes   │            │
│  │ (Issues #3,#32,  │  feeds  │  (Issues #33,    │            │
│  │  #33, #36, #41)  │         │   #36, #41)      │            │
│  └──────────────────┘         └──────────────────┘            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Key File Relationships

```
Main.ipynb (Python)
    │
    │ Translation needed (Issues #38, #39)
    │
    ▼
RecordFragment.java ◄─────────── Issues #28, #32, #35, #40
    │
    ├──► OverlayView.java ◄────── Issue #29
    │
    ├──► AssessmentData.java ◄─── Issues #3, #33, #36, #41
    │
    └──► Chart classes ◄────────── Issues #33, #36, #41
         (WalkingSpeedChart.java,
          JointSpasticityChart.java)
```

## Test Type Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                   3 Test Types (Issue #35)                      │
└─────────────────────────────────────────────────────────────────┘

    ┌──────────────────┐
    │  BASELINE TEST   │  Normal walking
    │  (Issue #35)     │  → Establishes reference values
    └────────┬─────────┘
             │
    ┌────────┴─────────────────────────────────────────┐
    │                                                   │
    ▼                                                   ▼
┌─────────────────┐                          ┌─────────────────┐
│  DUAL TASK      │                          │  SENSORY ATAXIA │
│  TEST           │                          │  TEST           │
│                 │                          │                 │
│  Walking +      │                          │  Walking with   │
│  Counting       │                          │  Eyes Closed    │
│  (Issue #41)    │                          │  (Issue #41)    │
└────────┬────────┘                          └────────┬────────┘
         │                                            │
         └────────────────┬───────────────────────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │  Compare to     │
                 │  Baseline       │
                 │  (Issue #28)    │
                 └─────────────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │  Generate       │
                 │  MS Scores      │
                 │  (Issues #33,   │
                 │   #36, #41)     │
                 └─────────────────┘
```

## Issue Status at a Glance

```
COMPLETED ✅                    IN PROGRESS 🚧              TODO ⏳
─────────────────────────────────────────────────────────────────
#1  Research angles            #28  CV formulas            #3   Link to graph
#6  Python integration         #38  Python→Java           #29  Live metrics
#7  Joint calculation          #39  Python→Java           #30  Better trends
#10 Formula research           #40  MediaPipe+Camera      #13  Planning: Dec
#11 Extract angles                                         #14  Planning: Jan
#27 Camera setup                                           #15  Planning: Feb
                                                           #32  Connect funcs
                                                           #33  Output→graph
                                                           #35  Record UI
                                                           #36  Data→graph
                                                           #41  Models→graphs
```

## Priority Order

1. **Week 1-2: Foundation**
   - Issues #38, #39 (Python to Java)
   - Issue #40 (MediaPipe connection)
   - Issue #28 (CV algorithms)

2. **Week 3-4: Integration**
   - Issue #32 (Functions to camera)
   - Issue #33 (Output to graph)
   - Issues #3, #36 (Data to graphs)

3. **Week 5: Finalization**
   - Issue #41 (Complete integration)
   - Issue #35 (UI completion)
   - Issue #29 (Live display)

4. **Week 6+: Enhancement**
   - Issue #30 (Better algorithms)

---

**Legend:**
- Boxes `┌─┐` = Issues/Components
- Arrows `──►` = Dependencies/Flow
- `═══` = Phase separators
