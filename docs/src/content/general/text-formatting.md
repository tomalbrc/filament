# Text Formatting

Filament comes with its own MiniMessage-style text-formatter.

## Basics

Write tags in angle brackets:

```text
<green>Hello</green>
<bold>Important</bold>
```

Tags are case-insensitive, so these all work the same:

```text
<green>
<GREEN>
<Green>
```

Most tags can be closed with an end tag like `</bold>`. If you forget to close tags, the parser will close them automatically at the end of the input.

## Escaping

Use backslashes to print special characters literally when needed:

```text
\<    prints <
\>    prints >
\\    prints \
```

Quoted tag arguments can use either single or double quotes.

## Supported tags

### Color

Set the text color using either named colors or hex colors.

Examples:

```text
<green>Green text</green>
<c:green>Green text</c>
<#55ff55>Green text</#55ff55>
<color:#ff0000>Red text</color>
<#324>Short hex (becomes #332244)</#324>
<color #ff0000>Space as separator</color>
```

Supported named colors are the usual Minecraft color names such as:

```text
black
dark_blue
dark_green
dark_aqua
dark_red
dark_purple
gold
gray / grey
dark_gray / dark_grey
blue
green
aqua
red
light_purple
yellow
white
```

Hex colors support `#RRGGBB`, `0xRRGGBB`, and short `#RGB`.

### Decorations

These styles can be turned on and off:

```text
<bold>bold</bold>
<italic>italic</italic>
<underlined>underlined</underlined>
<strikethrough>struck</strikethrough>
<obfuscated>random glyphs</obfuscated>
```

Short aliases are supported for most tags:

```text
<b>bold</b>
<i>italic</i> or <em>italic</em>
<u>underlined</u> or <underline>underlined</underline>
<st>struck</st> or <strike>struck</strike>
<obf>random glyphs</obf> or <magic>random glyphs</magic>
```

You can also negate them with `!`. This is useful for turning off a style within a nested tag:

```text
<bold>This is bold <!bold>this is not</!bold></bold>
```

### Keybind

Insert a keybind:

```text
Press <key:key.jump> to jump!
```

### Translatable

Insert translatable text (with optional arguments):

```text
<lang:block.minecraft.diamond_block>
<lang:commands.give.success.single:'1':'<red>Stone'>
```

### Selector

Insert an entity selector (runs as console at 0,0,0):

```text
<selector:@r>
```

### Score

Display a scoreboard value (placeholder not supported):

```text
Your score is <score:PlayerName:ObjectiveName>
```

### Reset

`<reset>` clears the active formatting.

Example:

```text
<green>Green <bold>bold</bold> <reset>plain again
```

### New line

Insert a new line with either:

```text
<newline>
<br>
```

### Font

Change the font used for the text:

```text
<font:minecraft:uniform>Uniform font</font>
<font:mynamespace:myfont>Custom font</font>
<font mynamespace:myfont>Custom font 2</font>
```

### Insertion

Add insertion text:

```text
<insertion:some text>Shown text</insertion>
```

### Shadow color

Set the shadow color for the styled text:

```text
<shadow>Uses the current text color as the shadow</shadow>
<shadow:#000000>Black shadow</shadow>
<shadow:#ff0000:0.5>Red shadow with 50% alpha</shadow>
<shadow:off>No shadow</shadow>

<shadow #000000>Black shadow</shadow>
```

The `0.5` part is optional alpha. If omitted, the formatter uses a default shadow alpha.

### Gradient

Create a gradient across the text inside the tag:

```text
<gradient:#ff0000:#00ff00>Red to green</gradient>
<gradient:#ff0000:#00ff00:#0000ff>Three-color gradient</gradient>
<gradient #ff0000 #00ff00 #0000ff>Three-color gradient</gradient>
```

A trailing number can be used as a phase offset:

```text
<gradient:#ff0000:#00ff00:0.25>Shifted gradient</gradient>
```

### Transition

Transition gives the whole enclosed text a single blended color sampled from the listed colors:

```text
<transition:#ff0000:#00ff00>Transition text</transition>
<transition:#ff0000:#00ff00:0.5>Shifted transition</transition>
```

### Rainbow

Rainbow colors the text with a rainbow sweep:

```text
<rainbow>Rainbow text</rainbow>
<rainbow:0.25>Phase shifted rainbow</rainbow>
<rainbow:!0.25>Reverse rainbow</rainbow>
```

### Click

Clickable text is supported through `<click:...>`.

Supported actions:

```text
<click:open_url:https://example.com>Open link</click>
<click:url:https://example.com>Alias for open_url</click>
<click:run_command:/say hello>Run command</click>
<click:cmd:/say hello>Alias for run_command</click>
<click:suggest_command:/msg Steve hello>Suggest command</click>
<click:change_page:2>Change page</click>
<click:copy_to_clipboard:Hello>Copy text</click>
<click:copy:Hello>Alias for copy_to_clipboard</click>
```

### Hover

Hover text is supported through `<hover:...>`.

#### Show text

This shows formatted text on hover:

```text
<hover:show_text:<yellow>Hello there>>Hover me</hover>
```

Hover text can use the same formatting tags again:

```text
<hover:show_text:<green><bold>Nested formatting</bold></green>>Hover me</hover>
```

#### Show item

This shows an item tooltip on hover:

```text
<hover:show_item:minecraft:stone>Stone</hover>
<hover:show_item:minecraft:stone:64>Stack of stone</hover>
```

#### Show entity

This shows an entity tooltip on hover:

```text
<hover:show_entity:minecraft:zombie:123e4567-e89b-12d3-a456-426614174000:Zombie>Hover me</hover>
```

### Sprite

Insert a custom sprite font character

```text
<sprite:atlas:minecraft:block/diamond_block>
<sprite:player:Steve>
```

## Closing tags

Most tags should be closed in the normal way:

```text
<green>Hello</green>
<bold>Important</bold>
<hover:show_text:'<yellow>Tip'>Hover me</hover>
```

## Nesting

Tags can be nested:

```text
<green>Hello <bold>world</bold></green>
```

Formatting is applied from the outside in.

## Plain text behavior

If the formatter does not recognize a tag, it leaves it as normal text.

Example:

```text
<not_a_tag>This is printed literally
```

## Examples

### Fancy message

```text
<gradient:#ff5555:#55ffff><bold>Welcome</bold></gradient>
<hover:show_text:'<gray>Click to join the website'><click:url:https://example.com>Visit our site</click></hover>
```

### Translatable with color

```text
You found a <green><lang:block.minecraft.diamond_block></green>!
```

## Summary of supported tags

- Colors: `<green>`, `<c:green>`, `<#55ff55>`, `<#RGB>`
- Decorations: `<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`, `<obfuscated>`, `<!style>`
- Components: `<key:...>`, `<lang:...>`, `<selector:...>`, `<score:...>`, `<sprite:...>`
- Reset: `<reset>`
- New line: `<newline>`, `<br>`
- Font: `<font:...>`
- Insertion: `<insertion:...>`
- Shadow: `<shadow>`, `<shadow:#rrggbb>`, `<shadow:#rrggbb:alpha>`, `<shadow:off>`
- Gradient: `<gradient:...>`
- Transition: `<transition:...>`
- Rainbow: `<rainbow>`
- Click: `<click:...>` (actions: `open_url`, `run_command`, `suggest_command`, `change_page`, `copy_to_clipboard`)
- Hover: `<hover:...>` (actions: `show_text`, `show_item`, `show_entity`)