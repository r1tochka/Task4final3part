# Task4final3part / Simple3DViewer

JavaFX 3D Viewer (software rasterizer) for CGVSU Task 4.

## Features

- Load OBJ models (`File -> Open Model...`)
- Multiple models in a scene + active model selection
- Model transforms (move/rotate/scale by axes) + reset
- Delete active model / delete vertex or polygon by index
- Rendering modes:
  - wireframe
  - texture
  - lighting
- Multiple cameras (add/remove/next/previous) + simple camera markers
- Save models to OBJ:
  - `Save Active Model...`
  - `Save Active Model (Original)...`
  - `Save Active Model (Transformed)...`

## Requirements

- Java 17
- Maven (optional; you can run via IntelliJ)
- JavaFX 17

## How to run (IntelliJ IDEA)

1. Open project folder `Task4final3part`.
2. Open Maven tool window, reload project.
3. Run `com.cgvsu.Main`.

## How to run tests

- In IntelliJ: Maven -> `Simple3DViewer` -> Lifecycle -> `test`.

## Controls

- **Active model**:
  - Select in the left panel (ComboBox)
  - Transform buttons in the left panel
- **Keyboard shortcuts (model transform)**:
  - `W/S/A/D` + `Space/C` - translate
  - `Q/E/R/F` - rotate
  - `+/-` - scale
  - `Backspace` - reset transform
- **Camera**:
  - LMB drag - rotate
  - RMB drag - pan
  - Mouse wheel - zoom

## Project structure

- `Simple3DViewer/` - main JavaFX application module
- `src/main/java/com/cgvsu/` - UI controller, math, model, OBJ IO, render engine
- `src/test/java/` - unit tests
