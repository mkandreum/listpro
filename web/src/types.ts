export interface Subcategory {
  id: number;
  name: string;
  emoji: string;
  count: number;
  image: string | null;
}

export interface Category {
  id: number;
  name: string;
  emoji: string;
  count: number;
  images: string[];
  subcategories: Subcategory[];
}

export interface Section {
  id: string;
  name: string;
  emoji: string;
  hue: string;
  icon: string;
  blurb: string;
  count: number;
  images: string[];
  categories: Category[];
}

export interface Product {
  id: string;
  name: string;
  size: string;
  section: string;
  category: number;
  subcategory: number;
  group: string;
  price: number;
  refPrice: number | null;
  refFormat: string | null;
  previousPrice: number | null;
  priceChangedAt: string | null;
  image: string | null;
  url: string | null;
  isNew: boolean;
}

export interface Catalog {
  version: string;
  updatedAt: string | null;
  warehouse: string | null;
  count: number;
  sections: Section[];
  products: Product[];
}

export interface SnapshotItem {
  name: string;
  price: number;
  section?: string;
  category?: number;
}

export interface SavedBasket {
  id: string;
  name: string;
  date: string;
  total: number;
  items: Record<string, number>;
  snapshot?: Record<string, SnapshotItem>;
}

/** Lo que se sincroniza entre dispositivos (mismo formato en web y Android). */
export interface ListData {
  basket: Record<string, number>;
  addedPrices: Record<string, number>;
  inCart?: string[];
  savedBaskets: SavedBasket[];
}

export interface PriceHistory {
  id: string;
  name: string;
  price: number;
  history: { price: number; at: string }[];
}
