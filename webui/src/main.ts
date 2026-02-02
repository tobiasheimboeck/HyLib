/**
 * HyLib Message WebUI – Live preview for HyLib format tags.
 * Uses same format as MessageParserImpl / HyMessages.parse().
 */

import "./style.css";
import { createInputSection } from "./ui/input.js";
import { createPreviewSection } from "./ui/preview.js";
import { createDocsSection } from "./ui/docs.js";

// Optional: restore from URL hash (#m=...)
function getInitialFromHash(): string {
  const hash = window.location.hash.slice(1);
  if (!hash.startsWith("m=")) return "";
  try {
    return decodeURIComponent(hash.slice(2));
  } catch {
    return "";
  }
}

function updateHash(value: string): void {
  if (!value) {
    history.replaceState(null, "", window.location.pathname + window.location.search);
    return;
  }
  history.replaceState(null, "", `#m=${encodeURIComponent(value)}`);
}

const initial = getInitialFromHash() || "Hello <bold>World</bold>! Try <gradient:red:blue>rainbow</gradient> or <link:https://hytale.com>Hytale</link>.";

const app = document.getElementById("app")!;
app.innerHTML = "";

const headerRow = document.createElement("div");
headerRow.className = "header-row";
const h1 = document.createElement("h1");
h1.textContent = "HyLib Message Preview";

const copyBtn = document.createElement("button");
copyBtn.className = "copy-btn mainBar__cta-btn";
copyBtn.innerHTML = `<span class="mainBar__cta-btn__label">Copy</span>`;

headerRow.appendChild(h1);
headerRow.appendChild(copyBtn);
app.appendChild(headerRow);

const { container: preview, update } = createPreviewSection();

const inputSection = createInputSection(initial, (value) => {
  update(value);
  updateHash(value);
});

app.appendChild(inputSection);
app.appendChild(preview);
app.appendChild(createDocsSection());

// Initial render
update(initial);

// Copy button
const copyBtnLabel = () => copyBtn.querySelector(".mainBar__cta-btn__label")!;
copyBtn.addEventListener("click", async () => {
  const textarea = inputSection.querySelector("textarea") as HTMLTextAreaElement;
  const text = textarea?.value ?? "";
  try {
    await navigator.clipboard.writeText(text);
    copyBtnLabel().textContent = "Copied!";
    setTimeout(() => { copyBtnLabel().textContent = "Copy"; }, 1500);
  } catch {
    copyBtnLabel().textContent = "Failed";
    setTimeout(() => { copyBtnLabel().textContent = "Copy"; }, 1500);
  }
});
