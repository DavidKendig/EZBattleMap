# EZBattleMap

A dual-screen battlemap display tool for tabletop RPGs. Control what your players see with pixel-precise grid masking across two monitors.

## Perfect for Game Masters

EZBattleMap lets you control exactly what portions of your battlemap your players see on their screen, perfect for fog of war, revealing areas as players explore, or hiding secret rooms and traps.

## Features

- **Dual Screen Support**: Automatically detects and uses two monitors (DM screen + player display)
- **Any Image Format**: Load battlemaps in JPG, PNG, GIF, or BMP format
- **GM Controller Screen**:
  - View full battlemap with pixel-based square grid overlay
  - Adjust grid size (1-2000 pixels, 1-pixel increments) to match your map
  - Click or drag to reveal/hide areas - perfect for fog of war
  - Independent pan and zoom with mouse controls
  - Green overlay shows revealed areas
  - Red border shows exactly what players see
- **Player Display Screen**:
  - Shows only the selected/revealed portions of the map
  - Hidden areas covered with black masks
  - Independent zoom and pan controls
  - Real-time updates as you reveal new areas

## Requirements

- Java Development Kit (JDK) 8 or higher
- Two monitors (or simulated dual-screen setup)

## Building and Running

### Windows

1. Compile the application:
   ```
   build.bat
   ```

2. Run the application:
   ```
   run.bat
   ```

### Linux/Mac

1. Make scripts executable:
   ```
   chmod +x build.sh run.sh
   ```

2. Compile the application:
   ```
   ./build.sh
   ```

3. Run the application:
   ```
   ./run.sh
   ```

## Usage for Game Masters

1. **Setup**: Two windows will open - GM Controller and Player Display
   - Place the Player Display on your second monitor/TV for players to see
   - Keep the GM Controller on your screen

2. **Load your battlemap**: Click "Select Image" and choose your map file

3. **Match grid to your map**: Adjust the square size in pixels to align with your battlemap's grid
   - Default: 100px
   - Range: 1-2000px with 1-pixel precision
   - Type directly or use arrow buttons for fine control

4. **Reveal areas to players**:
   - **Click** on a grid square to reveal/hide that area
   - **Click and drag** to reveal/hide multiple areas at once
     - First click determines reveal or hide
     - Drag over more squares to apply the same action
   - Green overlay = revealed to players
   - Red border = total visible area on player screen

5. **Player view**:
   - Players see only revealed areas
   - Hidden areas show as black
   - Players can zoom and pan independently

6. **Navigation** (both screens work independently):
   - **Mouse wheel**: Zoom in/out
   - **Middle-click drag** or **Ctrl+Left-click drag**: Pan around
   - Perfect for showing details or keeping overview

7. **Quick controls**:
   - **Clear Selection**: Hide everything from players (great for scene transitions)
   - **Select Image**: Load a new map

## GM Controls Quick Reference

| Action | Control |
|--------|---------|
| Zoom In/Out | Mouse Wheel (works on both screens independently) |
| Pan Map | Middle-click + Drag OR Ctrl + Left-click + Drag |
| Reveal Single Area | Left-click on grid square |
| Reveal Multiple Areas | Left-click + Drag across squares |
| Adjust Grid Size | Square Size spinner (1-2000 pixels) |
| Load New Map | "Select Image" button |
| Hide All from Players | "Clear Selection" button |

## Use Cases

- **Fog of War**: Hide unexplored areas, reveal as players move
- **Dungeon Crawling**: Show only the rooms players have entered
- **Secret Rooms**: Keep hidden areas masked until discovered
- **Combat Encounters**: Reveal tactical maps room by room
- **Exploration**: Control what terrain/buildings players can see
- **Theater of the Mind**: Gradually reveal location details

## Tips for Game Masters

- Match the grid size to your battlemap's native grid for perfect alignment
- Use high-resolution battlemaps for best quality
- The red border on your controller shows exactly what players see
- Clear selection between scenes for dramatic reveals
- Players can zoom in/out independently - great for examining details
- Works with any battlemap image format (JPG, PNG, GIF, BMP)

## License

MIT License - Copyright (c) 2024 David Kendig

See [LICENSE](LICENSE) file for details.
