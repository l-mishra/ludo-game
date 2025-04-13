# Ludo Game

A Java-based implementation of the classic Ludo board game using Spring Boot.

## Overview

This project implements a complete Ludo game with the following features:
- Multiplayer support (2-4 players)
- Standard Ludo rules implementation
- Piece movement and capture mechanics
- Home path entry and victory conditions
- REST API for game management

## Tech Stack

- Java 21
- Spring Boot 3.4.4
- Maven
- Lombok

## Project Structure

```
src/main/java/com/lkm/ludogame/
├── board/           # Board and path management
├── model/           # Game entities (Cell, Player, etc.)
├── piece/           # Piece-related classes
├── repository/      # Game state persistence
├── service/         # Business logic
├── validator/       # Move validation
├── LudoGame.java    # Main game logic
└── LudoGameApplication.java
```

## Key Features

1. **Game Board**
   - Main path with 52 cells
   - Color-specific home paths
   - Safe cells and home entry points

2. **Game Mechanics**
   - Dice rolling
   - Piece movement
   - Piece capture
   - Home path entry
   - Victory conditions

3. **Player Management**
   - Turn-based gameplay
   - Multiple player support
   - Piece selection logic

## Getting Started

### Prerequisites

- Java 21
- Maven

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/ludogame.git
cd ludogame
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

## Game Rules

1. Each player has 4 pieces
2. Players take turns rolling a die
3. A piece can only start moving when a 6 is rolled
4. Pieces move clockwise around the board
5. Landing on an opponent's piece captures it
6. Safe cells prevent capture
7. Pieces must enter their home path to win
8. First player to get all pieces home wins

## API Endpoints

The game provides REST endpoints for:
- Creating a new game
- Making moves
- Getting game state
- Managing players

## Testing

Run the test suite:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot team for the amazing framework
- All contributors who have helped improve this project 