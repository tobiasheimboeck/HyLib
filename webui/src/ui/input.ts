/**
 * Input textarea + toolbar for inserting tags at cursor.
 */

export function createInputSection(
  initialValue: string,
  onInput: (value: string) => void
): HTMLElement {
  const section = document.createElement("div");
  section.className = "input-section";

  const label = document.createElement("label");
  label.textContent = "Message (HyLib format tags)";
  section.appendChild(label);

  const textarea = document.createElement("textarea");
  textarea.value = initialValue;
  textarea.placeholder = "e.g. <bold>Hello</bold> <red>World</red> or <gradient:red:blue>Rainbow</gradient>";
  section.appendChild(textarea);

  const toolbar = createToolbar(textarea);
  section.appendChild(toolbar);

  textarea.addEventListener("input", () => onInput(textarea.value));
  textarea.addEventListener("keyup", () => onInput(textarea.value));

  return section;
}

function createToolbar(textarea: HTMLTextAreaElement): HTMLElement {
  const bar = document.createElement("div");
  bar.className = "toolbar";

  const insert = (before: string, after: string = "") => {
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const text = textarea.value;
    const selected = text.slice(start, end);
    const newText = text.slice(0, start) + before + selected + after + text.slice(end);
    textarea.value = newText;
    textarea.focus();
    const newPos = start + before.length + selected.length;
    textarea.setSelectionRange(newPos, newPos);
    textarea.dispatchEvent(new Event("input", { bubbles: true }));
  };

  const buttons: [string, string, string][] = [
    ["Bold", "<bold>", "</bold>"],
    ["Italic", "<italic>", "</italic>"],
    ["Underline", "<underline>", "</underline>"],
    ["Mono", "<monospace>", "</monospace>"],
    ["Reset", "<reset>", ""],
  ];

  for (const [label, open, close] of buttons) {
    const btn = document.createElement("button");
    btn.textContent = label;
    btn.type = "button";
    btn.addEventListener("click", () => insert(open, close));
    bar.appendChild(btn);
  }

  const colorSelect = document.createElement("select");
  colorSelect.innerHTML = "<option value=''>Color…</option>";
  const colors = [
    "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
    "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
    "yellow", "white",
  ];
  for (const c of colors) {
    const opt = document.createElement("option");
    opt.value = c;
    opt.textContent = c.replace(/_/g, " ");
    colorSelect.appendChild(opt);
  }
  colorSelect.addEventListener("change", () => {
    const v = colorSelect.value;
    if (v) {
      insert(`<${v}>`, `</${v}>`);
      colorSelect.value = "";
    }
  });
  bar.appendChild(colorSelect);

  const gradientBtn = document.createElement("button");
  gradientBtn.textContent = "Gradient";
  gradientBtn.type = "button";
  gradientBtn.addEventListener("click", () => insert("<gradient:red:blue>", "</gradient>"));
  bar.appendChild(gradientBtn);

  const linkBtn = document.createElement("button");
  linkBtn.textContent = "Link";
  linkBtn.type = "button";
  linkBtn.addEventListener("click", () => {
    const url = prompt("URL:", "https://");
    if (url) insert(`<link:${url}>`, "</link>");
  });
  bar.appendChild(linkBtn);

  return bar;
}
