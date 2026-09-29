# Android controller mapping

| Control | Action |
| --- | --- |
| Left stick | Move the Avatar through Exult's existing controller path |
| Right stick | Move the Exult mouse cursor |
| R2 or A | Left click; hold while moving the right stick to drag |
| R1 | Double left click (talk, open, use) |
| L2 | Right click |
| Hold L1 | Precision cursor speed |
| B or Start | Escape / close / menu |
| X | Inventory (`I`) |
| Y | Toggle combat (`C`) |
| D-pad up | Map (`M`) |
| D-pad right | Spellbook (`B`) |
| D-pad down | Statistics (`Z`) |
| D-pad left or Select | Save/restore (`S`) |
| L3 | Pause combat (Space) |
| R3 | Center the cursor |

Shortcuts retain the original game's conditions. For example, an item shortcut
does nothing when the required item is unavailable.

## Diagnostics

Android writes controller diagnostics under
`Download/Exult-Korean/diagnostics/`:

- `pad-v9-0.txt` through `pad-v9-2.txt`: three rotating files, at most 256 KiB
  each.
- `pad-v9-exit-0.bin` and `pad-v9-exit-1.bin`: two rotating historical crash
  traces, at most 1 MiB each.

`INPUT` records the physical transition, `ACTION` the generated mouse or key
event, and `ENGINE` the receiving Exult path. Stick motion is logged only when
movement starts or stops, not every frame. A dispatch record proves that code
ran; it does not by itself prove that the requested visible result was allowed
by the current game state.
