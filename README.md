# EZBattleMap

**Version 1.25.12**

A dual-screen battlemap display tool for tabletop RPGs with built-in image library management and token support. Control what your players see with pixel-precise grid masking, place and manage tokens, and organize your entire battlemap collection across two monitors.

## Perfect for Game Masters

EZBattleMap lets you control exactly what portions of your battlemap your players see on their screen, perfect for fog of war, revealing areas as players explore, or hiding secret rooms and traps. Manage character and monster tokens with drag-and-drop placement and flexible sizing. The built-in image library keeps all your battlemaps and tokens organized and ready to load with a single double-click or drag.

## Features

- **Dual Screen Support**: Automatically detects and uses two monitors (DM screen + player display)
- **Any Image Format**: Load battlemaps and tokens in JPG, PNG, GIF, or BMP format
- **Dual-Mode Operation**:
  - **Map Mode**: Control fog of war and revealed areas
  - **Token Mode**: Place, move, resize, and manage character/monster tokens
- **Image Library**: Built-in library system for managing your entire collection
  - Separate libraries for Maps and Tokens
  - Visual thumbnail grid for easy browsing
  - Organize with categories (Dungeons, Characters, Monsters, etc.)
  - Add searchable tags and notes to images
  - Rename images with friendly display names
  - Search across names, categories, tags, and notes
  - Double-click map thumbnails to instantly load maps
  - Drag tokens from library directly onto the map
  - **Mass Import**: Import multiple images at once to quickly build your library
  - Images stored safely in your home directory
  - Grid size saved per map and auto-restored
- **GM Controller Screen**:
  - View full battlemap with pixel-based square grid overlay
  - Adjust grid size (1-2000 pixels, 1-pixel increments) to match your map
  - **Map Mode**: Click or drag to reveal/hide areas - perfect for fog of war
  - **Token Mode**: Place, select, move, and manage tokens
  - Independent pan and zoom with mouse controls
  - Green overlay shows revealed areas
  - Red border shows exactly what players see
  - Visual token selection with green/red borders
- **Token Management**:
  - Drag tokens from library directly onto map
  - Right-click context menu for token options
  - Resize tokens: 1x1 (Small), 2x2 (Medium), 3x3 (Large)
  - Move tokens by dragging in Token Mode
  - Delete tokens with confirmation
  - Tokens persist on map and update in real-time
- **Player Display Screen**:
  - Shows only the selected/revealed portions of the map
  - Hidden areas covered with black masks (fog of war)
  - Tokens displayed without borders for clean view
  - Independent zoom and pan controls
  - Real-time updates as you reveal new areas and move tokens

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

### Initial Setup

1. **Launch**: Two windows will open - GM Controller and Player Display
   - Place the Player Display on your second monitor/TV for players to see
   - Keep the GM Controller on your screen

2. **Build Your Library**:
   - Click **"Mass Import"** in the menu bar to quickly add multiple maps and tokens
   - Or use **"Add Image"** button in the library panel for individual images
   - Switch between Maps and Tokens libraries using the "Library" dropdown
   - Maps stored in `~/.ezbattlemap/maps/`, Tokens in `~/.ezbattlemap/tokens/`

### Managing Your Image Library

The left panel contains your image library with full organization tools:

- **Switch Libraries**: Use the "Library" dropdown to switch between Maps and Tokens
- **Mass Import** (Menu Bar): Import multiple images at once to quickly populate your library
- **Add Images**: Click "Add Image" to upload single images to the current library
- **Organize**: Click "Edit" to set metadata for any image:
  - Display name (e.g., "Tavern Level 1" or "Goblin Warrior")
  - Category (e.g., "Dungeons", "Characters", "Monsters")
  - Tags (e.g., "indoor", "combat", "level-5", "player-character")
  - Notes (e.g., "Use for Chapter 3 encounter" or "HP: 15, AC: 13")
- **Search**: Use the search bar to find images by any metadata
- **Filter**: Select a category from the dropdown to filter the current library
- **Load Maps**: Double-click a map thumbnail to load it onto the battlemap
- **Place Tokens**: Drag token thumbnails onto the map (in Token Mode)
- **Delete**: Select an image and click "Delete" to remove it from your library

### Working with Maps (Map Mode)

1. **Load a battlemap**:
   - Double-click a thumbnail from your Maps library, OR
   - Click "Select Image" to load a file directly (doesn't add to library)

2. **Match grid to your map**:
   - Adjust the square size in pixels to align with your battlemap's grid
   - Default: 100px, Range: 1-2000px with 1-pixel precision
   - Grid size is automatically saved per map and restored when reloaded

3. **Reveal areas to players** (in Map Mode):
   - **Click** on a grid square to reveal/hide that area
   - **Click and drag** to reveal/hide multiple areas at once
     - First click determines reveal or hide
     - Drag over more squares to apply the same action
   - Green overlay = revealed to players
   - Red border = total visible area on player screen

4. **Quick controls**:
   - **Clear Selection**: Hide everything from players (great for scene transitions)

### Working with Tokens (Token Mode)

1. **Switch to Token Mode**: Use the "Mode" dropdown to select "Token Mode"

2. **Place tokens**:
   - Drag token images from the library panel onto the map
   - Tokens snap to grid squares

3. **Select and move tokens**:
   - **Left-click** a token to select it (green border)
   - **Drag** selected token to move it to a new position

4. **Resize tokens** (right-click menu):
   - **Right-click** on any token to open context menu
   - Select **"Change Size"** and choose:
     - **1x1 (Small)**: Single grid square (default)
     - **2x2 (Medium)**: 2x2 grid area (Large creatures)
     - **3x3 (Large)**: 3x3 grid area (Huge creatures)

5. **Delete tokens**:
   - **Right-click** token and select **"Delete Token"**
   - Confirmation dialog prevents accidents

### Navigation (Both Screens)

Both GM Controller and Player Display support independent navigation:
- **Mouse wheel**: Zoom in/out
- **Middle-click drag** or **Ctrl+Left-click drag**: Pan around
- Perfect for showing details or maintaining overview

### Player View

- Players see only revealed areas (fog of war in Map Mode)
- Hidden areas appear as black masks
- Tokens are displayed cleanly without borders
- Players can zoom and pan independently
- Real-time updates as you reveal areas and move tokens

## GM Controls Quick Reference

### Menu Bar
| Action | Control |
|--------|---------|
| Mass Import Images | "Mass Import" button |
| View About/Version | "About" button |
| Open Help Dialog | "Help" button |

### Library Management
| Action | Control |
|--------|---------|
| Switch Library Type | "Library" dropdown (Maps/Tokens) |
| Mass Import Images | "Mass Import" button (menu bar) |
| Add Single Image | "Add Image" button (library panel) |
| Edit Image Metadata | Select thumbnail, click "Edit" |
| Delete Image | Select thumbnail, click "Delete" |
| Search Library | Search bar (library panel) |
| Filter by Category | Category dropdown (library panel) |

### Map Mode Controls
| Action | Control |
|--------|---------|
| Switch to Map Mode | Mode dropdown: "Map Mode" |
| Load Map from Library | Double-click map thumbnail |
| Load Map from File | "Select Image" button |
| Adjust Grid Size | Square Size spinner (1-2000 pixels) |
| Reveal Single Area | Left-click on grid square |
| Reveal Multiple Areas | Left-click + Drag across squares |
| Hide All from Players | "Clear Selection" button |

### Token Mode Controls
| Action | Control |
|--------|---------|
| Switch to Token Mode | Mode dropdown: "Token Mode" |
| Place Token on Map | Drag token from library to map |
| Select Token | Left-click on token |
| Move Token | Drag selected token |
| Resize Token | Right-click token → "Change Size" → Select size |
| Delete Token | Right-click token → "Delete Token" |

### Navigation (Both Screens)
| Action | Control |
|--------|---------|
| Zoom In/Out | Mouse Wheel |
| Pan View | Middle-click + Drag OR Ctrl + Left-click + Drag |

## Use Cases

- **Fog of War**: Hide unexplored areas, reveal as players move
- **Dungeon Crawling**: Show only the rooms players have entered, place monster tokens as encounters begin
- **Secret Rooms**: Keep hidden areas masked until discovered
- **Combat Encounters**: Reveal tactical maps room by room, manage initiative with token placement
- **Token Management**: Place character tokens, resize for different creature sizes (Small/Medium/Large/Huge)
- **Dynamic Battles**: Move tokens in real-time as combat unfolds
- **Exploration**: Control what terrain/buildings players can see
- **Theater of the Mind**: Gradually reveal location details with token placement for key NPCs

## Tips for Game Masters

- **Use Mass Import** to quickly build your library before the session - add all maps and tokens you might need
- **Organize libraries separately**: Keep Maps library for battlemaps, Tokens library for characters/monsters
- **Use categories** to organize by campaign, location type, or session (e.g., "Campaign 1", "Dungeons", "NPCs")
- **Add tags** for quick searching (e.g., "boss-fight", "urban", "outdoor", "party", "enemies")
- **Match grid size** to your battlemap's native grid for perfect alignment (grid size saves per map)
- **Use high-resolution images** for battlemaps and tokens for best quality
- **Red border on controller** shows exactly what players see on their screen
- **Switch modes efficiently**: Use Map Mode for fog of war, Token Mode for character management
- **Token sizing**: Use 1x1 for Medium creatures, 2x2 for Large, 3x3 for Huge
- **Clear selection** between scenes for dramatic reveals
- **Independent zoom**: Players can zoom in/out on their screen - great for examining token details
- **Metadata is powerful**: Add notes to tokens (HP, AC, abilities) for quick reference during play
- **Works with any image format**: JPG, PNG, GIF, BMP supported for both maps and tokens

## Image Library Storage

Your battlemap and token libraries are stored permanently in your user home directory:

**Location**: `~/.ezbattlemap/` (or `C:\Users\YourName\.ezbattlemap\` on Windows)

**Directory Structure**:
```
~/.ezbattlemap/
├── maps/             # Full-size battlemap images
├── tokens/           # Token/character images
├── thumbnails/       # Auto-generated 150x150px thumbnails (shared)
└── library.dat       # Metadata for both libraries (names, categories, tags, notes)
```

**Library Types**:
- **Maps Library**: Store your battlemaps, dungeon tiles, and scene backgrounds
- **Tokens Library**: Store character tokens, monster tokens, and markers
- Switch between libraries using the "Library" dropdown in the left panel
- Each library has independent categories and organization

**Important Notes**:
- Images are automatically copied to the appropriate library folder when you click "Add Image"
- Original files are not modified or moved
- Deleting from library removes images from `.ezbattlemap/` only
- To backup your library, copy the entire `.ezbattlemap/` folder
- To share your library across computers, copy `.ezbattlemap/` to the new machine
- Library persists between sessions and program updates
- Maps and tokens can have the same filenames (stored in separate directories)

## License

MIT License - Copyright (c) 2025 David Kendig

See [LICENSE](LICENSE) file for details.
