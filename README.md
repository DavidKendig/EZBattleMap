# EZBattleMap

A dual-screen battlemap display tool for tabletop RPGs with built-in image library management. Control what your players see with pixel-precise grid masking across two monitors.

## Perfect for Game Masters

EZBattleMap lets you control exactly what portions of your battlemap your players see on their screen, perfect for fog of war, revealing areas as players explore, or hiding secret rooms and traps. The built-in image library keeps all your battlemaps organized and ready to load with a single double-click.

## Features

- **Dual Screen Support**: Automatically detects and uses two monitors (DM screen + player display)
- **Any Image Format**: Load battlemaps in JPG, PNG, GIF, or BMP format
- **Image Library**: Built-in library for managing your battlemap collection
  - Upload and store battlemaps permanently
  - Visual thumbnail grid for easy browsing
  - Organize maps with categories (Maps, Tokens, Dungeons, etc.)
  - Add searchable tags and notes to images
  - Rename images with friendly display names
  - Search across names, categories, tags, and notes
  - Double-click thumbnails to instantly load maps
  - Images stored safely in your home directory
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

2. **Manage your map library** (left panel on GM Controller):
   - **Add Images**: Click "Add Image" to upload battlemaps to your library
     - Images are copied to `~/.ezbattlemap/images/` (safe permanent storage)
     - Thumbnails are automatically generated for easy browsing
   - **Organize**: Click "Edit" to set:
     - Display name (e.g., "Tavern Level 1")
     - Category (e.g., "Dungeons", "Cities", "Wilderness")
     - Tags (e.g., "indoor", "combat", "level-5")
     - Notes (e.g., "Use for Chapter 3 encounter")
   - **Search**: Use the search bar to find maps by any metadata
   - **Filter**: Select a category from the dropdown to show only those maps
   - **Load**: Double-click any thumbnail to load it onto the battlemap
   - **Delete**: Select a map and click "Delete" to remove it from your library

3. **Load your battlemap**:
   - Double-click a thumbnail from your library, OR
   - Click "Select Image" to load a file directly (doesn't add to library)

4. **Match grid to your map**: Adjust the square size in pixels to align with your battlemap's grid
   - Default: 100px
   - Range: 1-2000px with 1-pixel precision
   - Type directly or use arrow buttons for fine control

5. **Reveal areas to players**:
   - **Click** on a grid square to reveal/hide that area
   - **Click and drag** to reveal/hide multiple areas at once
     - First click determines reveal or hide
     - Drag over more squares to apply the same action
   - Green overlay = revealed to players
   - Red border = total visible area on player screen

6. **Player view**:
   - Players see only revealed areas
   - Hidden areas show as black
   - Players can zoom and pan independently

7. **Navigation** (both screens work independently):
   - **Mouse wheel**: Zoom in/out
   - **Middle-click drag** or **Ctrl+Left-click drag**: Pan around
   - Perfect for showing details or keeping overview

8. **Quick controls**:
   - **Clear Selection**: Hide everything from players (great for scene transitions)
   - **Select Image**: Load a new map directly from filesystem

## GM Controls Quick Reference

| Action | Control |
|--------|---------|
| Add Map to Library | "Add Image" button (left panel) |
| Load Map from Library | Double-click thumbnail (left panel) |
| Edit Map Metadata | Select thumbnail, click "Edit" |
| Search Library | Search bar or category filter (left panel) |
| Zoom In/Out | Mouse Wheel (works on both screens independently) |
| Pan Map | Middle-click + Drag OR Ctrl + Left-click + Drag |
| Reveal Single Area | Left-click on grid square |
| Reveal Multiple Areas | Left-click + Drag across squares |
| Adjust Grid Size | Square Size spinner (1-2000 pixels) |
| Load Map from File | "Select Image" button |
| Hide All from Players | "Clear Selection" button |

## Use Cases

- **Fog of War**: Hide unexplored areas, reveal as players move
- **Dungeon Crawling**: Show only the rooms players have entered
- **Secret Rooms**: Keep hidden areas masked until discovered
- **Combat Encounters**: Reveal tactical maps room by room
- **Exploration**: Control what terrain/buildings players can see
- **Theater of the Mind**: Gradually reveal location details

## Tips for Game Masters

- Build your library before the session - add all maps you might need
- Use categories to organize maps by campaign, location type, or session
- Add tags for quick searching (e.g., "boss-fight", "urban", "outdoor")
- Match the grid size to your battlemap's native grid for perfect alignment
- Use high-resolution battlemaps for best quality
- The red border on your controller shows exactly what players see
- Clear selection between scenes for dramatic reveals
- Players can zoom in/out independently - great for examining details
- Works with any battlemap image format (JPG, PNG, GIF, BMP)

## Image Library Storage

Your battlemap library is stored permanently in your user home directory:

**Location**: `~/.ezbattlemap/` (or `C:\Users\YourName\.ezbattlemap\` on Windows)

**Directory Structure**:
```
~/.ezbattlemap/
├── images/           # Full-size battlemap images
├── thumbnails/       # Auto-generated 150x150px thumbnails
└── library.dat       # Metadata (names, categories, tags, notes)
```

**Important Notes**:
- Images are automatically copied to the library when you click "Add Image"
- Original files are not modified or moved
- Deleting from library removes images from `.ezbattlemap/` only
- To backup your library, copy the entire `.ezbattlemap/` folder
- To share your library across computers, copy `.ezbattlemap/` to the new machine
- Library persists between sessions and program updates

## License

MIT License - Copyright (c) 2025 David Kendig

See [LICENSE](LICENSE) file for details.
