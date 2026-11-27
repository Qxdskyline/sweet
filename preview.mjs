import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import { extname, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const host = "127.0.0.1";
const port = 8000;
const root = fileURLToPath(new URL("./src/main/assets", import.meta.url));
const contentTypes = new Map([
  [".html", "text/html"],
  [".css", "text/css"],
  [".js", "text/javascript"],
  [".json", "application/json"]
]);

const server = createServer(async (request, response) => {
  try {
    const pathname = decodeURIComponent(new URL(request.url, "http://localhost").pathname);
    const filename = pathname.endsWith("/") ? pathname + "index.html" : pathname;
    const file = resolve(root, "." + filename);
    const contentType = contentTypes.get(extname(file));
    if (!file.startsWith(root + sep) || !contentType) {
      response.writeHead(404).end();
      return;
    }

    const content = await readFile(file);
    response.writeHead(200, {
      "Content-Type": contentType + "; charset=utf-8",
      "Cache-Control": "no-store"
    }).end(content);
  } catch (error) {
    const missing = error instanceof URIError || ["ENOENT", "ENOTDIR", "EISDIR"].includes(error.code);
    response.writeHead(missing ? 404 : 500).end();
  }
});

server.listen(port, host, () => console.log(`Sweet preview: http://${host}:${port}/`));
