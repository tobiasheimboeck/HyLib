# HyLib Message WebUI

Live preview for HyLib format tags – same format as `HyMessages.parse()` and `MessageParserImpl` in HyLib.

## Usage

```bash
npm install
npm run dev    # http://localhost:5173
npm run build  # Output: dist/
npm run preview # Serve dist/
```

## Supported Tags

| Tag | Example |
|-----|---------|
| `color`, `c`, `colour` | `<color:#ff0000>` or `<red>` |
| `gradient`, `grnt` | `<gradient:red:blue>text</gradient>` |
| `bold`, `b` | `<bold>text</bold>` |
| `italic`, `i`, `em` | `<italic>text</italic>` |
| `underline`, `u` | `<underline>text</underline>` |
| `monospace`, `mono` | `<monospace>text</monospace>` |
| `link`, `url` | `<link:https://...>text</link>` |
| `reset`, `r` | `<reset>` – clears all styles |

**Named colors:** black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white

## Share via URL

The input can be encoded in the URL hash: `#m=encoded_text`. Use the Copy button to copy the raw tag string.
