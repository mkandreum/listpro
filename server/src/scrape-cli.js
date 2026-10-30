// Actualización manual del catálogo: `npm run scrape` (todos) o `npm run scrape -- consum`
import { scrapeAll, scrapeCatalog } from './scraper.js';
import { isStore } from './stores.js';

try {
  const store = process.argv[2];
  const r = isStore(store) ? await scrapeCatalog(store) : await scrapeAll();
  console.log(JSON.stringify(r));
} catch (e) {
  console.error(e);
  process.exit(1);
}
