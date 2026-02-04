// convert-px-to-rem.js
const fs = require("fs");
const path = require("path");

const BASE_FONT_SIZE = 16;
console.log("BASE_FONT_SIZE:", BASE_FONT_SIZE);

const SRC_DIR = path.join(__dirname, "src");
console.log("SRC_DIR:", SRC_DIR);

const INDEX_HTML = path.join(SRC_DIR, "index.html");
console.log("INDEX_HTML path:", INDEX_HTML);

const TARGET_FOLDERS = ["@theme", "auth", "pages", "pipes", "services"].map(f => path.join(SRC_DIR, "app", f));
console.log("TARGET_FOLDERS:", TARGET_FOLDERS);

function pxToRem(content, filePath) {
  console.log("Running pxToRem for:", filePath);

  const lines = content.split("\n");
  console.log("Number of lines:", lines.length);

  const updatedLines = lines.map((line, idx) => {
    let matches = [...line.matchAll(/(\d+(\.\d+)?)px/g)];
    console.log(`Line ${idx + 1}:`, line);
    console.log(`Matches found:`, matches.length);

    if (matches.length === 0) return line;

    const changes = matches.map(match => {
      const pxValue = parseFloat(match[1]);
      const remValue = +(pxValue / BASE_FONT_SIZE).toFixed(4);
      console.log(`Converting ${match[0]} to ${remValue}rem`);
      return `${match[0]}:${remValue}rem`;
    });

    const newLine = line.replace(/(\d+(\.\d+)?)px/g, (_, num) => {
      const converted = `${(parseFloat(num) / BASE_FONT_SIZE).toFixed(4)}rem`;
      console.log(`Replacing ${num}px with ${converted}`);
      return converted;
    });

    console.log(`[${path.relative(SRC_DIR, filePath)}] ${changes.join(" && ")}`);
    return newLine;
  });

  return updatedLines.join("\n");
}

function processFile(filePath) {
  console.log("Processing file:", filePath);

  const ext = path.extname(filePath).toLowerCase();
  console.log("File extension:", ext);

  const allowed = [".html", ".ts", ".css", ".scss", ".js"];
  if (!allowed.includes(ext)) {
    console.log("Skipping file (not allowed extension):", filePath);
    return;
  }

  const original = fs.readFileSync(filePath, "utf-8");
  console.log("Original file content read");

  const updated = pxToRem(original, filePath);
  if (original !== updated) {
    fs.writeFileSync(filePath, updated, "utf-8");
    console.log("File updated:", filePath);
  } else {
    console.log("No changes made to file:", filePath);
  }
}

function processDirectory(dir) {
  console.log("Processing directory:", dir);

  const entries = fs.readdirSync(dir);
  console.log("Entries found:", entries.length);

  entries.forEach(entry => {
    const fullPath = path.join(dir, entry);
    const stat = fs.statSync(fullPath);

    if (stat.isDirectory()) {
      console.log("Entering directory:", fullPath);
      processDirectory(fullPath);
    } else {
      console.log("Found file:", fullPath);
      processFile(fullPath);
    }
  });
}

// 1. Convert index.html
console.log("1. Convert index.html");
if (fs.existsSync(INDEX_HTML)) {
  console.log("index.html exists");
  processFile(INDEX_HTML);
} else {
  console.log("index.html does not exist:", INDEX_HTML);
}

// 2. Convert all files in target folders
console.log("2. Convert all files in target folders");
for (const folder of TARGET_FOLDERS) {
  console.log("Checking folder:", folder);
  if (fs.existsSync(folder)) {
    console.log("Folder exists:", folder);
    processDirectory(folder);
  } else {
    console.log("Folder does not exist:", folder);
  }
}
