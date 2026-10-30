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
  /** Súper del producto. Los catálogos guardados antes de haber varios súper no lo traen. */
  store?: string;
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
  store?: string;
  storeName?: string;
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
  /** Plan de recetas activo y preferencias: así el plan también se ve en los otros dispositivos. */
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

// ───────────── Planes de recetas ─────────────

export type MealType = 'desayuno' | 'comida' | 'merienda' | 'cena';

export interface PlanPrefs {
  budget: number;
  people: number;
  /** Días de la semana a planificar. */
  days: number;
  /** Nombre antiguo de `days`, de cuando solo había cenas. */
  nights?: number;
  meals: MealType[];
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
  /** Los planes antiguos no lo traen: eran todo cenas. */
  meal?: MealType;
  base?: string;
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
  usedFor?: { day: string; meal: MealType }[];
}

export interface MealPlan {
  id: string;
  createdAt: string;
  prefs: PlanPrefs;
  title: string;
  summary: string;
  sections?: Partial<Record<MealType, { summary: string; count: number; kcalAvg: number; costPerServing: number }>>;
  recipes: PlanRecipe[];
  shopping: ShoppingItem[];
  totals: { shopping: number; consumed: number; perServing: number; kcalAvg: number; kcalDay?: number; proteinAvg: number; budget: number; overBudget: boolean };
  warnings: string[];
}

export interface PlannerOptions {
  enabled: boolean;
  diets: { id: string; label: string }[];
  appliances: { id: string; label: string }[];
  meals?: { id: MealType; label: string }[];
  stores: { id: string; label: string }[];
}

// ───────────── Súper y comparador ─────────────

export interface StoreInfo {
  id: string;
  name: string;
  hue: string;
  count: number;
  updatedAt: string | null;
}

export interface ComparedProduct {
  id: string;
  store: string;
  name: string;
  size: string;
  price: number;
  refPrice: number | null;
  refFormat: string | null;
  image: string | null;
  /** Precio por kilo, litro, unidad o lavado calculado igual para todos los súper. */
  per?: { value: number; label: string } | null;
  match?: MatchKind;
  /** Envases que hacen falta para la misma cantidad que llevas. */
  packs?: number;
}

/** "same": mismo código de barras. "equivalent": el mismo producto de otra marca. "similar": del mismo tipo, con diferencias. */
export type MatchKind = 'same' | 'equivalent' | 'similar';

/** Una alternativa de un producto de la cesta, con lo que costaría para la misma cantidad. */
export interface Alternative extends ComparedProduct {
  match: MatchKind;
  packs: number;
  cost: number;
  saving: number;
  /** El ahorro se ha calculado por kilo o litro porque los envases no cuadran. */
  approx: boolean;
  comparable: boolean;
}

export type Alternatives = Record<string, { per: ComparedProduct['per']; options: Alternative[] }>;

export interface StoreMatch {
  store: string;
  match: MatchKind;
  product: ComparedProduct;
  alternatives: ComparedProduct[];
}

export interface Comparison {
  id: string;
  store: string;
  per?: ComparedProduct['per'];
  matches: StoreMatch[];
}

export type Cheaper = Record<string, StoreMatch & { saving: number }>;

export interface BasketStoreCost {
  id: string;
  name: string;
  hue: string;
  total: number;
  matched: number;
  exact: number;
  similar: number;
  equivalent?: number;
  /** Lo que cuestan en tu súper solo los productos encontrados en este. */
  comparedTotal?: number;
  missing: string[];
  missingCount: number;
  coverage: number;
}

export interface BasketCost {
  totalItems: number;
  stores: BasketStoreCost[];
}

// ───────────── Cuentas ─────────────

export interface User {
  id: string;
  name: string;
  email: string;
  listCode: string | null;
  /** Súper preferido: el que se ve al entrar. */
  store?: string | null;
  createdAt: string;
  plan?: 'pro' | 'free';
  planStartedAt?: string | null;
  inviteCode?: string | null;
}

export interface Session {
  token: string;
  user: User;
}
