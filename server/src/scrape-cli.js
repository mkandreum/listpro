// Actualización manual del catálogo: `npm run scrape`
import { scrapeCatalog } from './scraper.js';

try {
  const r = await scrapeCatalog();
  console.log(JSON.stringify(r));
} catch (e) {
  console.error(e);
  process.exit(1);
}
