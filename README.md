# MP-cmsc-12

Final CMSC 12 Java/Swing game project.

## Project Structure

- `src/main` - application launcher and top-level window flow.
- `src/engine` - gameplay panel, level definitions, camera, game mode, and level enemy setup.
- `src/entity` - player, enemies, projectiles, lasers, and shared entity behavior.
- `src/systems` - input, collision, combat resolution, timer, save-file helpers, and future state model.
- `src/tile` - tile data types and tile-map loading/rendering.
- `src/ui` and `src/panels` - reusable UI helpers, HUD, pause menu, loading/menu/cutscene panels.
- `src/util` - constants, resource cache, cooldowns, image helpers, and Swing utility components.
- `res` - images, fonts, map files, and other runtime assets.
- `bin` - compiled `.class` output.

## Compile

Run these from the project root in PowerShell:

```powershell
Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName.Substring((Get-Location).Path.Length + 1).Replace('\', '/') } | Set-Content -Path sources.txt
cmd /c "javac -d bin @sources.txt"
```

If package names or class names were renamed, do a clean compile first:

```powershell
if (Test-Path bin) { Remove-Item -Recurse -Force bin }
New-Item -ItemType Directory bin | Out-Null
Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName.Substring((Get-Location).Path.Length + 1).Replace('\', '/') } | Set-Content -Path sources.txt
cmd /c "javac -d bin @sources.txt"
```

PowerShell treats `@sources.txt` specially unless the `javac` command is passed through `cmd /c`.

## Run

```powershell
java -cp bin main.GameLauncher
```

Keep the `res` folder beside the project root when running from compiled classes because development builds load assets from `res/...`.

## Build the JAR

`manifest.txt` points the JAR to `main.GameLauncher`.

```powershell
jar cfm HawakKoAngBit.jar manifest.txt -C bin . -C . res
java -jar HawakKoAngBit.jar
```

The resource cache now supports both development filesystem paths and classpath resources bundled in the JAR.
