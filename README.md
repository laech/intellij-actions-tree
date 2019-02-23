# Actions Tree

Like the built-in IntelliJ Quick Lists, for defining a list of actions to show
in a popup, an action in the list can be other action list to show when invoked.
The main difference between this plugin and Quick Lists is that this plugin
allows each action in the list to be assigned a keyboard shortcut that is local
to the list, doesn't conflict with other global shortcuts. This allows
simulation of Emacs like prefix keys (n-level deep), so you are not restricted
to IntelliJ keymap's maximum of two key strokes.

## Configuration

Configuration is done via a JSON file, set it under *Preferences | Keymap | Actions Tree*.

The following is an example Emacs like configuration:

```json
{"items": [
    {"keys": ["ctrl X"],
     "items": [
         {"keys": ["ctrl F"], "id": "GotoFile"},
         {"keys": ["ctrl B", "B"], "id": "RecentFiles"},
         {"separator-above": "",
          "keys": ["K", "0"], "id": "CloseContent"},
         {"keys": ["1"], "id": "UnsplitAll"},
         {"keys": ["2"], "id": "SplitHorizontally"},
         {"keys": ["3"], "id": "SplitVertically"},
         {"keys": ["O"], "id": "NextSplitter"},
         {"keys": ["ctrl N"], "id": "NextSplitter"},
         {"separator-above": "",
          "keys": ["H"], "id": "$SelectAll"},
         {"separator-above": "",
          "keys": ["ctrl C"], "id": "Exit"}
     ]},
    {"keys": ["alt S"],
     "items": [
         {"keys": ["typed ."], "id": "Find"},
         {"keys": ["alt typed %"], "id": "Replace"},
         {"keys": ["H"],
          "separator-above": "",
          "name": "Highlight...",
          "items": [
              {"keys": ["typed ."], "id": "HighlightUsagesInFile"},
              {"keys": ["N"], "id": "GotoNextElementUnderCaretUsage"},
              {"keys": ["P"], "id": "GotoPrevElementUnderCaretUsage"}
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
    - `keys`: (optional) typing any of these key strokes while the parent popup 
       is showing will invoke this action, for the format see
       [JDK's KeyStroke class](https://docs.oracle.com/javase/8/docs/api/javax/swing/KeyStroke.html#getKeyStroke-java.lang.String-).
       
       Basic format is `modifiers <KEY>`.
       
       Where modifiers can be one or more of:
         - `shift`
         - `control`
         - `ctrl`
         - `meta`
         - `alt`
         - `altGraph`.
        
        And `<KEY>` can be one of:
         - Any upper case constants prefixed with `VK_` in the
           [KeyEvent](https://docs.oracle.com/javase/8/docs/api/java/awt/event/KeyEvent.html#field.summary)
           class. But you need to specify the name of the constant without the `VK_` prefix.
         - `typed <CHAR>` where `<CHAR>` is the unicode character to be typed, case sensitive.
    - `name`: (optional) name to display in the popup.
    - `separator-above`: (optional) inserts this string separator above this
      item in the popup, empty for a line separator
    - `items`: (required) sub-actions to show in a popup when this action is
      invoked, each item can be an action group or an action reference.
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
