# Actions Tree

Like the built-in IntelliJ Quick Lists, for defining a list of actions to show in a popup, an action in the list can be other action list to show when invoked. The main difference between this plugin and Quick Lists is that this plugin allows each action in the list to be assigned a keyboard shortcut that is local to the list, doesn't conflict with other global shortcuts. This allows simulation of Emacs like prefix keys (n-level deep), so you are not restricted to IntelliJ keymap's maximumn of two key strokes.

## Configuration

Configuration is done via a JSON file, set it under *Preferences | Keymap | Actions Tree*.

The following is an example Emacs like configuration:

```json
{"items": [
    {"keys": ["ctrl X"],
     "items": [
         {"keys": ["ctrl F"], "id": "GotoFile"},
         {"keys": ["ctrl B"], "id": "RecentFiles"},
         {"keys": [     "B"], "id": "RecentChangedFiles"},
         {"separator-above": "",
          "keys": ["K", "0"], "id": "CloseContent"},
         {"keys": [     "1"], "id": "UnsplitAll"},
         {"keys": [     "2"], "id": "SplitHorizontally"},
         {"keys": [     "3"], "id": "SplitVertically"},
         {"keys": [     "O"], "id": "NextSplitter"},
         {"keys": [     "ctrl N"], "id": "NextSplitter"},
         {"separator-above": "",
          "keys": [     "H"], "id": "$SelectAll"},
         {"keys": ["ctrl X"], "id": "EditorSwapSelectionBoundaries"},
         {"separator-above": "",
          "keys": [     "E"], "id": "PlaybackLastMacro"},
         {"separator-above": "",
          "keys": ["ctrl C"], "id": "Exit"}
     ]},
    {"keys": ["ctrl C"],
     "items": [
         {"keys": ["released P"],
          "name": "Project...",
          "items": [
              {"keys": [         "C"], "id": "CompileDirty"},
              {"separator-above": "",
               "keys": [         "K"], "id": "CloseProject"},
              {"keys": ["released P"], "id": "RecentProjectListGroup"}
          ]}
     ]},
    {"keys": ["alt G"],
     "items": [
         {"keys": ["alt N", "N"], "id": "GotoNextError"},
         {"keys": ["alt P", "P"], "id": "GotoPreviousError"},
         {"separator-above": "",
          "keys": ["released G"], "id": "GotoLine"}
     ]},
    {"keys": ["alt S"],
     "items": [
         {"keys": [     "PERIOD"], "id": "Find"},
         {"keys": ["shift alt 5"], "id": "Replace"},
         {"separator-above": "",   "id": "FindInPath"},
         {                         "id": "ReplaceInPath"},
         {"keys": ["H"],
          "separator-above": "",
          "name": "Highlight...",
          "items": [
              {"keys": ["PERIOD"], "id": "HighlightUsagesInFile"},
              {"keys": [     "N"], "id": "GotoNextElementUnderCaretUsage"},
              {"keys": [     "P"], "id": "GotoPrevElementUnderCaretUsage"}
          ]}
     ]}
]}
```

With the above example configuration, typing `ctrl X` will show a popup containing its items, then typing `H` will invoke the `$SelectAll` action.

The basic structures are:

1. Action Group - defines a custom group of actions:
    ```json
    {
      "keys": [...],
      "name": "...",
      "separator-above": "...",
      "items": [...]
    }
    ```
    - `keys`: (optional) typing any of these key strokes while the parent popup is showing will invoke this action, for the format see [JDK's KeyStroke class](https://docs.oracle.com/javase/8/docs/api/javax/swing/KeyStroke.html#getKeyStroke-java.lang.String-)
    - `name`: (optional) name to display in the popup.
    - `separator-above`: (optional) inserts this string separator above this item in the popup, empty for a line separator
    - `items`: (required) sub-actions to show in a popup when this action is invoked, each item can be an action group or an actin reference.
2. Action Reference - references an existing IDE action:
    ```json
    {
      "keys": [...],
      "separator-above": "...",
      "id": "..."
    }
    ```
    - `keys`: same as above
    - `separator-above`: same as above
    - `id`: (required) the ID of an existing action to invoke. To find the ID of an action, use *Tools | Actions Tree | Export IDE Actions* to export the ID to name mappings of all your IDE actions.

The top level actions will be registered as global actions in the keymap (can be seen under *Preferences | Keymap | Plug-ins | Actions Tree*) so they can be invoked anywhere, so be sure to check your keymap to make sure their keys don't conflict with other actions.

Any change to the configuration file can be reloaded via *Tools | Actions Tree | Reload*.
