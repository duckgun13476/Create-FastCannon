
<img width="1024" height="290" alt="model" src="https://github.com/user-attachments/assets/fe7fffdd-f077-4021-aac6-f4e5c06b2e2c" />



## Create: Fast Schematic Cannon

---

Another simple add-on for Mechanical Power that brings configuration modifications to the Blueprint Cannon!

1. Allows customization of the number of prints the Blueprint Cannon performs per tick, resulting in the final effect being the Mechanical Power's tick delay multiplied by this print count.
2. Allows customization of the blocks that the Blueprint Cannon is forbidden to print. Once added to the list, the corresponding blocks will be unbreakable by the Blueprint Cannon, effectively fixing the void boiler issue.

---

# Configuration File Description

### Enable Blueprint Cannon Speed Up
- **EnableCannonSpeedUp**:
    - This is a boolean value indicating whether to enable the Blueprint Cannon speed-up feature. Set to `true` to enable, and `false` to disable.

### Prints Per Tick
- **SpeedupPerTick**:
    - This variable represents the number of prints the Blueprint Cannon performs per tick, which is multiplicative with the print delay within the Blueprint Cannon.
    - Calculation formula: Print Speed = Number of Prints per Tick (this configuration value) × Game Ticks (default is 20) / [Blueprint Cannon Print Delay] (default is 10).
    - Range: 1 ~ 400

### Experimental Game Tick Parameter
- **lazyTick**:
    - This is an experimental parameter used to reduce the game ticks of the Blueprint Cannon, specifying how many ticks should pass before executing the Blueprint Cannon code.
    - The default value is 1, but you can increase it to reduce lag; however, this will make the Blueprint Cannon's response slower.
    - A suggested value is 5, which is four times slower than the default, but the difference may not be noticeable.
    - Range: 1 ~ 200

### Unbreakable Blocks List
- **blocks_unbreak**:
    - This is a list of blocks that cannot be destroyed by the Blueprint Cannon, preventing the Blueprint Cannon from causing the endless boiler bug.
    - Example: `["create:blaze_burner"]`

### Enable Debug Messages
- **debug**:
    - This is a boolean value indicating whether to notify the console when the Blueprint Cannon attempts to break a forbidden block. Set to `true` to enable notifications, and `false` to disable.