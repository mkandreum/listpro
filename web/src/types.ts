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
  /** Plan de cenas activo y preferencias: así el plan también se ve en los otros dispositivos. */
  planId?: string | null;
  planPrefs?: PlanPrefs | null;
  /** Lista compartida: quién está, quién pidió y quién cogió cada producto, y las cuentas. */
  members?: Member[];
  addedBy?: Record<string, string>;
  pickedBy?: Record<string, string>;
  expenses?: Expense[];
}

export interface Member {
  id: string;
  name: string;
  hue: string;
  joinedAt: string;
}

export type SplitMode = 'equal' | 'items';

/** Un gasto o un pago entre miembros. `shares` dice cuánto le toca a cada uno (en euros); siempre suma `amount`. */
export interface Expense {
  id: string;
  at: string;
  title: string;
  amount: number;
  paidBy: string;
  shares: Record<string, number>;
  kind: 'purchase' | 'settle';
  mode?: SplitMode;
  basketId?: string;
}

export interface PriceHistory {
  id: string;
  name: string;
  price: number;
  history: { price: number; at: string }[];
}

// ───────────── Planes de cenas ─────────────

export interface PlanPrefs {
  budget: number;
  people: number;
  nights: number;
  maxMinutes: number;
  diets: string[];
  appliances: string[];
  likes: string;
  dislikes: string;
  allergies: string;
  pantry: string;
  store: string;
}

export interface Nutrition { kcal: number; protein: number; carbs: number; fat: number; fiber: number; salt: number }

export interface PlanIngredient {
  productId: string | null;
  name: string;
  amount: string;
  fraction: number;
  fromPantry: boolean;
  image: string | null;
  price: number | null;
  size: string | null;
}

export interface PlanRecipe {
  day: string;
  name: string;
  description: string;
  minutes: number;
  difficulty: string;
  appliances: string[];
  servings: number;
  nutrition: Nutrition;
  ingredients: PlanIngredient[];
  steps: string[];
  tip: string;
  cost: number;
  costPerServing: number;
}

export interface ShoppingItem {
  productId: string;
  name: string;
  size: string;
  image: string | null;
  price: number;
  packs: number;
  cost: number;
  usedIn: string[];
}

export interface MealPlan {
  id: string;
  createdAt: string;
  prefs: PlanPrefs;
  title: string;
  summary: string;
  recipes: PlanRecipe[];
  shopping: ShoppingItem[];
  totals: { shopping: number; consumed: number; perServing: number; kcalAvg: number; proteinAvg: number; budget: number; overBudget: boolean };
  warnings: string[];
}

export interface PlannerOptions {
  enabled: boolean;
  diets: { id: string; label: string }[];
  appliances: { id: string; label: string }[];
  stores: { id: string; label: string }[];
}
