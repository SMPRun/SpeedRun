# SMPRun Speedrun Plugin

A simple, server‑wide speedrun mode: start a run, race to defeat the Ender Dragon, celebrate the winner, and automatically reset into a fresh world for the next race.

## Important: Not Standalone
- This module depends on internal/closed components (e.g., the Common plugin).
- It’s published for reference and learning; not intended for third‑party use as‑is.

## How It Works
- Start a run; the timer begins for everyone.
- Players race to kill the Ender Dragon.
- The winner and their time are announced.
- After a short countdown, the server restarts with a brand‑new seed for the next run.

## Notes
- The plugin handles world cleanup and seed rotation automatically after each win.
- Keep this alongside the Common dependency for functionality.

