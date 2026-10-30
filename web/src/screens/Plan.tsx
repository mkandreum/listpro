import { useEffect, useMemo, useState } from 'react';
import {
  AlertTriangle, Apple, Check, ChefHat, Clock3, Coffee, Flame, Minus, Moon, Plus, RefreshCw, ShoppingBasket, SlidersHorizontal,
  Soup, Sparkles, Store, Trash2, Wallet,
} from 'lucide-react';
import { api } from '../api';
import { money } from '../lib';
import { href, navigate } from '../router';
import { addPlanToBasket, clearPlan, generatePlan, getState, loadCatalog, storeInfo, swapRecipe, toast, useStore } from '../store';
import { StoreChoice } from '../components/Stores';
import type { MealPlan, MealType, PlanPrefs, PlanRecipe, PlannerOptions } from '../types';
import { haptic } from '../fx/haptics';
import { useTweened } from '../fx/motion';
import { Pager, type PillItem } from '../components/Pills';
import { Price } from '../components/ProductTag';

export const DEFAULT_PREFS: PlanPrefs = {
  budget: 45, people: 2, days: 5, meals: ['cena'], maxMinutes: 40, diets: ['omnivora'], appliances: ['fuego', 'microondas'],
  likes: '', dislikes: '', allergies: '', pantry: '', store: 'mercadona',
};

// Color, icono y coste orientativo por ración de cada comida (para sugerir presupuesto). Igual en Android.
export const MEALS: Record<MealType, { label: string; plural: string; icon: typeof Coffee; hue: string; perServing: number }> = {
  desayuno: { label: 'Desayuno', plural: 'Desayunos', icon: Coffee, hue: '#F59E0B', perServing: 0.9 },
  comida: { label: 'Comida', plural: 'Comidas', icon: Soup, hue: '#F2711C', perServing: 2.6 },
  merienda: { label: 'Merienda', plural: 'Meriendas', icon: Apple, hue: '#E8508E', perServing: 0.7 },
  cena: { label: 'Cena', plural: 'Cenas', icon: Moon, hue: '#6366F1', perServing: 2.1 },
};
const MEAL_ORDER = Object.keys(MEALS) as MealType[];

const APPLIANCE_SHORT: Record<string, string> = {
  fuego: 'Fuego', horno: 'Horno', microondas: 'Micro', freidora_aire: 'Freidora de aire', olla_rapida: 'Olla rápida',
  robot: 'Robot', sandwichera: 'Plancha', batidora: 'Batidora',
};
const DIET_SHORT: Record<string, string> = {
  omnivora: 'De todo', vegetariana: 'Vegetariana', vegana: 'Vegana', pescetariana: 'Pescetariana', sin_gluten: 'Sin gluten',
  sin_lactosa: 'Sin lactosa', alta_proteina: 'Alta en proteína', baja_carbohidratos: 'Baja en hidratos', ligera: 'Ligera', halal: 'Halal',
};
const LOADING = ['Mirando los precios de hoy…', 'Repartiendo la semana…', 'Cuadrando tu presupuesto…', 'Eligiendo platos que no se repitan…', 'Reaprovechando ingredientes…', 'Calculando la nutrición…', 'Montando tu lista de la compra…'];

/** Los planes de antes de las recetas solo tenían cenas y `nights`. */
const mealOf = (r: PlanRecipe): MealType => r.meal ?? 'cena';
const withDefaults = (p: Partial<PlanPrefs> | null | undefined): PlanPrefs => ({
  ...DEFAULT_PREFS, store: getState().store, ...p, days: p?.days ?? p?.nights ?? DEFAULT_PREFS.days, meals: p?.meals?.length ? p.meals : ['cena'],
});
const round5 = (n: number) => Math.max(15, Math.round(n / 5) * 5);
export const suggestedBudget = (p: PlanPrefs) => round5(p.meals.reduce((a, m) => a + MEALS[m].perServing, 0) * p.people * p.days * 1.15);

export function PlanScreen({ recipe }: { recipe?: number }) {
  const plan = useStore((s) => s.plan);
  const savedPrefs = useStore((s) => s.planPrefs);
  const [editing, setEditing] = useState(false);
  const [busy, setBusy] = useState<MealType[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [options, setOptions] = useState<PlannerOptions | null>(null);

  useEffect(() => { api.plannerOptions().then(setOptions).catch(() => setOptions(null)); }, []);

  const run = async (prefs: PlanPrefs, avoid: string[] = []) => {
    setBusy(prefs.meals);
    setError(null);
    haptic.tap();
    try {
      await generatePlan(prefs, avoid);
      haptic.success();
      setEditing(false);
      window.scrollTo({ top: 0 });
    } catch (e) {
      haptic.warn();
      setError((e as Error).message || 'No se ha podido crear el plan. Prueba otra vez.');
    } finally {
      setBusy(null);
    }
  };

  if (busy) return <Cooking meals={busy} />;
  if (!plan || editing) {
    return <PrefsForm initial={withDefaults(savedPrefs ?? plan?.prefs)} options={options} error={error} onSubmit={(p) => run(p)} onCancel={plan ? () => setEditing(false) : undefined} />;
  }
  const open = recipe != null ? plan.recipes[recipe] : undefined;
  return (
    <>
      <PlanView plan={plan} openMeal={open ? mealOf(open) : undefined} onRedo={() => run(withDefaults(plan.prefs), plan.recipes.map((r) => r.name))} onEdit={() => setEditing(true)} />
      {open && <RecipeSheet key={`${plan.id}-${recipe}`} r={open} index={recipe!} onClose={() => history.length > 1 ? history.back() : navigate({ name: 'plan' }, true)} />}
    </>
  );
}

function Title({ children }: { children?: React.ReactNode }) {
  return (
    <div className="row-head" style={{ marginTop: 6 }}>
      <h1 className="h-section"><span className="h-icon"><ChefHat size={22} strokeWidth={2.2} /></span>Recetas</h1>
      {children}
    </div>
  );
}

// ───────────── Formulario de preferencias ─────────────

function PrefsForm({ initial, options, error, onSubmit, onCancel }: {
  initial: PlanPrefs; options: PlannerOptions | null; error: string | null; onSubmit: (p: PlanPrefs) => void; onCancel?: () => void;
}) {
  const [p, setP] = useState<PlanPrefs>(initial);
  const up = <K extends keyof PlanPrefs>(k: K, v: PlanPrefs[K]) => setP((x) => ({ ...x, [k]: v }));
  const toggle = (k: 'diets' | 'appliances', id: string) => {
    haptic.tick();
    setP((x) => {
      let list = x[k].includes(id) ? x[k].filter((v) => v !== id) : [...x[k], id];
      if (k === 'diets') list = id === 'omnivora' ? ['omnivora'] : list.filter((v) => v !== 'omnivora');
      if (!list.length) list = k === 'diets' ? ['omnivora'] : ['fuego'];
      return { ...x, [k]: list };
    });
  };
  const toggleMeal = (m: MealType) => {
    haptic.tick();
    setP((x) => {
      const on = x.meals.includes(m);
      if (on && x.meals.length === 1) return x;
      const meals = MEAL_ORDER.filter((k) => (k === m ? !on : x.meals.includes(k)));
      // Si el presupuesto iba con la sugerencia, la sigue
      const next = { ...x, meals };
      return x.budget === suggestedBudget(x) ? { ...next, budget: suggestedBudget(next) } : next;
    });
  };
  const disabled = options != null && !options.enabled;
  const budget = useTweened(p.budget, 200);
  const suggested = suggestedBudget(p);
  const max = Math.max(150, round5(suggested * 2));
  const what = p.meals.length === 1 ? MEALS[p.meals[0]].plural.toLowerCase() : `${p.meals.length} comidas al día`;

  return (
    <form className="plan-form" onSubmit={(e) => { e.preventDefault(); onSubmit({ ...p, nights: p.days }); }}>
      <Title>{onCancel && <button type="button" className="btn" onClick={onCancel}>Cancelar</button>}</Title>
      <p className="muted lead">Desayunos, comidas, meriendas y cenas de la semana con productos y precios reales de tu súper, a tu gusto y con lo que tienes en la cocina.</p>

      <section className="panel">
        <div className="field-head"><ChefHat size={20} /><h2 className="h-block">¿Qué planificamos?</h2></div>
        <div className="meal-picker" role="group" aria-label="Comidas del día">
          {MEAL_ORDER.map((m) => {
            const M = MEALS[m];
            const on = p.meals.includes(m);
            return (
              <button type="button" key={m} className="meal-tile" aria-pressed={on} style={{ '--meal': M.hue } as React.CSSProperties} onClick={() => toggleMeal(m)}>
                <span className="meal-ico"><M.icon size={22} strokeWidth={2.2} /></span>
                <b>{M.plural}</b>
                <span className="tick" aria-hidden><Check size={14} strokeWidth={3} /></span>
              </button>
            );
          })}
        </div>
      </section>

      <section className="panel">
        <div className="field-head"><Wallet size={20} /><h2 className="h-block">Presupuesto de la semana</h2></div>
        <div className="budget"><Price value={budget} className="price big-price" /><span className="muted">para toda la compra</span></div>
        <input className="range" type="range" min={15} max={max} step={5} value={Math.min(p.budget, max)} aria-label="Presupuesto semanal en euros"
          onChange={(e) => { haptic.tick(); up('budget', Number(e.target.value)); }} />
        <div className={`suggest${p.budget < suggested * 0.8 ? ' low' : ''}`}>
          <Sparkles size={16} />
          <span>Para {p.people} {p.people === 1 ? 'persona' : 'personas'}, {p.days} {p.days === 1 ? 'día' : 'días'} y {what}, calcula unos <b className="num">{money(suggested)}</b>{p.budget < suggested * 0.8 ? '. Con menos, tocará tirar de lo más básico.' : '.'}</span>
          {p.budget !== suggested && <button type="button" className="link" onClick={() => { haptic.tick(); up('budget', suggested); }}>Usar</button>}
        </div>
        <div className="two">
          <Counter label="Personas" value={p.people} min={1} max={8} onChange={(v) => setP((x) => ({ ...x, people: v, budget: x.budget === suggestedBudget(x) ? suggestedBudget({ ...x, people: v }) : x.budget }))} />
          <Counter label="Días" value={p.days} min={1} max={7} onChange={(v) => setP((x) => ({ ...x, days: v, budget: x.budget === suggestedBudget(x) ? suggestedBudget({ ...x, days: v }) : x.budget }))} />
        </div>
      </section>

      <section className="panel">
        <div className="field-head"><Store size={20} /><h2 className="h-block">Tu súper</h2></div>
        <StoreChoice value={p.store} onChange={(id) => up('store', id)} />
        <p className="muted small" style={{ marginTop: 8 }}>Las recetas se hacen con los productos y precios de hoy de {storeInfo(p.store).name}.</p>
      </section>

      <section className="panel">
        <div className="field-head"><Sparkles size={20} /><h2 className="h-block">Tu dieta</h2></div>
        <div className="chips wrap">
          {Object.keys(DIET_SHORT).map((id) => (
            <button type="button" key={id} className="chip text-only" aria-pressed={p.diets.includes(id)} onClick={() => toggle('diets', id)}>{DIET_SHORT[id]}</button>
          ))}
        </div>
        <TextField label="Alergias o intolerancias" placeholder="frutos secos, marisco…" value={p.allergies} onChange={(v) => up('allergies', v)} />
      </section>

      <section className="panel">
        <div className="field-head"><Flame size={20} /><h2 className="h-block">Tu cocina</h2></div>
        <div className="chips wrap">
          {Object.keys(APPLIANCE_SHORT).map((id) => (
            <button type="button" key={id} className="chip text-only" aria-pressed={p.appliances.includes(id)} onClick={() => toggle('appliances', id)}>{APPLIANCE_SHORT[id]}</button>
          ))}
        </div>
        <div className="field-head" style={{ marginTop: 18 }}><Clock3 size={18} /><b>Tiempo máximo para cocinar</b></div>
        <div className="segmented four" role="group" aria-label="Tiempo máximo">
          {[15, 30, 45, 60].map((m) => (
            <button type="button" key={m} aria-pressed={p.maxMinutes === m} onClick={() => { haptic.tick(); up('maxMinutes', m); }}>{m} min</button>
          ))}
        </div>
        {p.meals.some((m) => m === 'desayuno' || m === 'merienda') && <p className="muted small">Desayunos y meriendas siempre van rápidos: 10 o 15 minutos como mucho.</p>}
      </section>

      <section className="panel">
        <div className="field-head"><ChefHat size={20} /><h2 className="h-block">Tus gustos</h2></div>
        <TextField label="Te gusta" placeholder="pasta, platos de cuchara, cocina asiática…" value={p.likes} onChange={(v) => up('likes', v)} />
        <TextField label="No te gusta" placeholder="coliflor, picante…" value={p.dislikes} onChange={(v) => up('dislikes', v)} />
        <TextField label="Ya tienes en casa" placeholder="arroz, cebollas, medio paquete de pasta…" value={p.pantry} onChange={(v) => up('pantry', v)} />
      </section>

      {error && <div className="notice up" role="alert"><AlertTriangle size={20} /><span>{error}</span></div>}
      {disabled && <div className="notice" role="status"><AlertTriangle size={20} /><span>El planificador no está disponible ahora mismo. Vuelve a intentarlo más tarde.</span></div>}
      <button className="btn primary block cta" type="submit" disabled={disabled}><Sparkles size={20} /> Crear mi plan</button>
      <p className="muted small center">Tarda entre 20 y 60 segundos. No es un servicio de reparto: te ayudamos a organizar la compra y la cocina.</p>
    </form>
  );
}

function Counter({ label, value, min, max, onChange }: { label: string; value: number; min: number; max: number; onChange: (v: number) => void }) {
  return (
    <div className="counter">
      <span>{label}</span>
      <div className="stepper" role="group" aria-label={label}>
        <button type="button" aria-label={`Menos ${label.toLowerCase()}`} disabled={value <= min} onClick={() => { haptic.remove(); onChange(Math.max(min, value - 1)); }}><Minus size={18} /></button>
        <output key={value} className="roll">{value}</output>
        <button type="button" aria-label={`Más ${label.toLowerCase()}`} disabled={value >= max} onClick={() => { haptic.add(); onChange(Math.min(max, value + 1)); }}><Plus size={18} /></button>
      </div>
    </div>
  );
}

function TextField({ label, placeholder, value, onChange }: { label: string; placeholder: string; value: string; onChange: (v: string) => void }) {
  return (
    <label className="text-field">
      <span>{label}</span>
      <input className="input" value={value} placeholder={placeholder} maxLength={200} onChange={(e) => onChange(e.target.value)} />
    </label>
  );
}

// ───────────── Mientras cocina ─────────────

function Cooking({ meals }: { meals: MealType[] }) {
  const [i, setI] = useState(0);
  useEffect(() => { const t = setInterval(() => setI((x) => (x + 1) % LOADING.length), 2600); return () => clearInterval(t); }, []);
  return (
    <div className="cooking" role="status" aria-live="polite">
      <div className="pot"><ChefHat size={46} strokeWidth={1.8} /><span /><span /><span /></div>
      <h2 className="h-block">Preparando tu plan</h2>
      <p key={i} className="muted roll-text">{LOADING[i]}</p>
      <div className="cooking-meals" aria-hidden>
        {meals.map((m, k) => {
          const M = MEALS[m];
          return <span key={m} style={{ '--meal': M.hue, '--d': k } as React.CSSProperties}><M.icon size={16} /> {M.plural}</span>;
        })}
      </div>
    </div>
  );
}

// ───────────── Plan ─────────────

function PlanView({ plan, openMeal, onRedo, onEdit }: { plan: MealPlan; openMeal?: MealType; onRedo: () => void; onEdit: () => void }) {
  const idx = useStore((s) => s.idx);
  const t = plan.totals;
  // El plan puede ser de otro súper: su catálogo hace falta para meter la compra en la cesta
  const planStore = plan.prefs.store ?? 'mercadona';
  useEffect(() => { if (!getState().catalogs[planStore]) loadCatalog(planStore); }, [planStore]);
  const pct = Math.min(100, (t.shopping / t.budget) * 100);
  const shopping = useMemo(() => plan.shopping.filter((s) => idx.products.size === 0 || idx.products.has(s.productId)), [plan, idx]);
  const meals = useMemo(() => MEAL_ORDER.filter((m) => plan.recipes.some((r) => mealOf(r) === m)), [plan]);
  const tabs: PillItem[] = [
    ...meals.map((m) => ({ key: m, label: MEALS[m].label, icon: MEALS[m].icon, hue: MEALS[m].hue })),
    { key: 'compra', label: 'Compra', icon: ShoppingBasket, hue: '#16A34A' },
  ];
  const multi = meals.length > 1;

  return (
    <>
      <Title><button className="btn" onClick={onEdit}><SlidersHorizontal size={18} /> Ajustar</button></Title>

      <section className="total-block plan-hero" aria-label="Resumen del plan">
        <div className="label">{plan.title}</div>
        <div className="big num"><Price value={t.shopping} className="" /></div>
        <div className="progress" role="progressbar" aria-valuemin={0} aria-valuemax={t.budget} aria-valuenow={t.shopping} aria-label="Gasto frente al presupuesto">
          <span style={{ width: `${pct}%`, background: t.overBudget ? '#FFD6D6' : '#fff' }} />
        </div>
        <div className="small num" style={{ fontWeight: 600, marginTop: 8 }}>
          {t.overBudget ? `Te pasas ${money(t.shopping - t.budget)} de tus ${money(t.budget)}` : `De tus ${money(t.budget)}: te sobran ${money(t.budget - t.shopping)}`}
        </div>
        <div className="hero-figures">
          <div><b className="num">{money(t.perServing)}</b><span>por ración</span></div>
          {multi && t.kcalDay ? <div><b className="num">{t.kcalDay}</b><span>kcal al día</span></div> : <div><b className="num">{t.kcalAvg}</b><span>kcal de media</span></div>}
          <div><b className="num">{plan.recipes.length}</b><span>recetas</span></div>
        </div>
        <div className="total-actions">
          <button className="btn white" onClick={() => {
            haptic.success();
            toast(`${addPlanToBasket(shopping)} productos en la cesta`);
          }}><ShoppingBasket size={18} /> Añadir la compra a la cesta</button>
          <button className="btn ghost-white" onClick={onRedo}><RefreshCw size={18} /> Otro plan</button>
        </div>
      </section>

      {plan.warnings.map((w) => <div key={w} className="notice up"><AlertTriangle size={20} /><span>{w}</span></div>)}

      <Pager tabs={tabs} initial={openMeal} label="Secciones del plan">
        {meals.map((m) => <MealPage key={m} plan={plan} meal={m} />)}
        <ShoppingPage plan={plan} shopping={shopping} />
      </Pager>
    </>
  );
}

function MealPage({ plan, meal }: { plan: MealPlan; meal: MealType }) {
  const M = MEALS[meal];
  const list = plan.recipes.map((r, i) => ({ r, i })).filter(({ r }) => mealOf(r) === meal);
  const sec = plan.sections?.[meal];
  const kcal = sec?.kcalAvg ?? Math.round(list.reduce((a, { r }) => a + r.nutrition.kcal, 0) / (list.length || 1));
  const cost = sec?.costPerServing ?? list.reduce((a, { r }) => a + r.costPerServing, 0) / (list.length || 1);
  const summary = sec?.summary ?? (plan.sections ? '' : plan.summary);

  return (
    <div style={{ '--meal': M.hue } as React.CSSProperties}>
      <div className="meal-head">
        <span className="meal-ico"><M.icon size={22} strokeWidth={2.2} /></span>
        <div>
          <h2 className="h-section">{M.plural}</h2>
          <span className="muted small num">{list.length} {list.length === 1 ? 'receta' : 'recetas'} · {kcal} kcal · {money(cost)}/ración</span>
        </div>
      </div>
      {summary && <p className="muted" style={{ margin: '0 2px 14px' }}>{summary}</p>}
      <div className="dinners">
        {list.map(({ r, i }, k) => (
          <a key={`${r.day}-${i}`} href={href({ name: 'plan', recipe: i })} className="dinner" style={{ '--d': k } as React.CSSProperties} onClick={() => haptic.tap()}>
            <div className="dinner-pics" aria-hidden>
              {r.ingredients.filter((x) => x.image).slice(0, 3).map((x) => <img key={x.productId} src={x.image!} alt="" loading="lazy" />)}
            </div>
            <div className="dinner-body">
              <span className="day">{r.day}</span>
              <b>{r.name}</b>
              <p>{r.description}</p>
              <div className="facts num">
                <span><Clock3 size={14} /> {r.minutes} min</span>
                <span><Flame size={14} /> {r.nutrition.kcal} kcal</span>
                <span>{money(r.costPerServing)}/ración</span>
              </div>
            </div>
          </a>
        ))}
      </div>
    </div>
  );
}

function ShoppingPage({ plan, shopping }: { plan: MealPlan; shopping: MealPlan['shopping'] }) {
  const t = plan.totals;
  const label = (s: MealPlan['shopping'][number]) => {
    const uses = s.usedFor ?? s.usedIn.map((day) => ({ day, meal: 'cena' as MealType }));
    const multi = new Set(plan.recipes.map(mealOf)).size > 1;
    return uses.slice(0, 4).map((u) => `${u.day.slice(0, 3)}${multi ? ` ${MEALS[u.meal]?.label.toLowerCase() ?? ''}` : ''}`).join(', ') + (uses.length > 4 ? ` y ${uses.length - 4} más` : '');
  };
  return (
    <div>
      <div className="meal-head" style={{ '--meal': 'var(--green)' } as React.CSSProperties}>
        <span className="meal-ico"><ShoppingBasket size={22} strokeWidth={2.2} /></span>
        <div>
          <h2 className="h-section">Lista de la compra</h2>
          <span className="muted small num">{shopping.length} productos · {money(t.shopping)}</span>
        </div>
      </div>
      <div className="panel" style={{ marginTop: 0, padding: '6px 16px' }}>
        {shopping.map((s) => (
          <div key={s.productId} className="shop-row">
            {s.image ? <img src={s.image} alt="" loading="lazy" /> : <span className="ph" />}
            <div className="info">
              <b>{s.name}</b>
              <span className="muted small">{s.size} · {label(s)}</span>
            </div>
            <div className="right num"><b>{money(s.cost)}</b><span className="muted small">{s.packs} × {money(s.price)}</span></div>
          </div>
        ))}
        <div className="shop-total num"><span>Total</span><b>{money(t.shopping)}</b></div>
      </div>
      <button className="btn primary block" style={{ marginTop: 14 }} onClick={() => {
        haptic.success();
        toast(`${addPlanToBasket(shopping)} productos en la cesta`);
      }}><ShoppingBasket size={18} /> Añadir todo a la cesta</button>
      <p className="muted small" style={{ marginTop: 12 }}>
        Precios reales de hoy. La nutrición es una estimación hecha por IA: si tienes alergias, revisa siempre la etiqueta del producto.
      </p>
      <button className="btn danger block" style={{ marginTop: 18 }} onClick={() => { clearPlan(); toast('Plan borrado'); }}><Trash2 size={18} /> Borrar el plan</button>
    </div>
  );
}

// ───────────── Receta ─────────────

function RecipeSheet({ r, index, onClose }: { r: PlanRecipe; index: number; onClose: () => void }) {
  const [done, setDone] = useState<Set<number>>(new Set());
  const [swapping, setSwapping] = useState(false);
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { removeEventListener('keydown', onKey); document.body.style.overflow = prev; };
  }, [onClose]);
  const n = r.nutrition;
  const M = MEALS[mealOf(r)];
  const bought = r.ingredients.filter((i) => i.productId).map((i) => ({ productId: i.productId!, packs: Math.max(1, Math.ceil(i.fraction - 0.05)) }));
  const swap = async () => {
    setSwapping(true);
    haptic.tap();
    try {
      await swapRecipe(index);
      haptic.success();
      toast('Receta cambiada');
    } catch (e) {
      haptic.warn();
      toast((e as Error).message || 'No se ha podido cambiar la receta');
    } finally {
      setSwapping(false);
    }
  };

  return (
    <>
      <div className="sheet-scrim" onClick={onClose} />
      <div className="sheet recipe" role="dialog" aria-modal="true" aria-labelledby="recipe-title" style={{ '--meal': M.hue } as React.CSSProperties}>
        <div className="grip" />
        <span className="stamp meal-stamp"><M.icon size={14} /> {M.label} · {r.day} · {r.servings} {r.servings === 1 ? 'ración' : 'raciones'}</span>
        <h2 id="recipe-title" className={swapping ? 'swapping' : undefined}>{r.name}</h2>
        <p className="muted" style={{ marginTop: 6 }}>{r.description}</p>
        <div className="facts num" style={{ marginTop: 12 }}>
          <span><Clock3 size={14} /> {r.minutes} min</span>
          <span>{r.difficulty}</span>
          <span>{money(r.costPerServing)}/ración</span>
          {r.appliances.map((a) => <span key={a}>{APPLIANCE_SHORT[a] ?? a}</span>)}
        </div>

        <h3 className="h-block" style={{ marginTop: 22 }}>Nutrición por ración</h3>
        <div className="nutri num">
          <div className="kcal"><b>{n.kcal}</b><span>kcal</span></div>
          <div><b>{n.protein} g</b><span>proteínas</span></div>
          <div><b>{n.carbs} g</b><span>hidratos</span></div>
          <div><b>{n.fat} g</b><span>grasas</span></div>
          <div><b>{n.fiber} g</b><span>fibra</span></div>
          <div><b>{n.salt.toLocaleString('es-ES')} g</b><span>sal</span></div>
        </div>
        <p className="muted small" style={{ marginTop: 6 }}>Estimación orientativa.</p>

        <h3 className="h-block" style={{ marginTop: 22 }}>Ingredientes</h3>
        {r.ingredients.map((i) => (
          <div key={i.name} className="shop-row">
            {i.image ? <img src={i.image} alt="" loading="lazy" /> : <span className="ph" />}
            <div className="info">
              <b>{i.name}</b>
              <span className="muted small">{i.amount}{i.fromPantry ? ' · lo tienes en casa' : ''}</span>
            </div>
            {i.price != null && <div className="right num"><span className="muted small">{money(i.price)}</span></div>}
          </div>
        ))}
        <p className="muted small" style={{ marginTop: 6 }}>Además: aceite de oliva, sal y pimienta.</p>

        <h3 className="h-block" style={{ marginTop: 22 }}>Paso a paso</h3>
        <p className="muted small">Toca cada paso para tacharlo mientras cocinas.</p>
        <ol className="steps">
          {r.steps.map((s, k) => (
            <li key={k} aria-checked={done.has(k)} role="checkbox" tabIndex={0}
              onClick={() => { haptic.tick(); setDone((d) => { const x = new Set(d); if (x.has(k)) x.delete(k); else x.add(k); return x; }); }}
              onKeyDown={(e) => { if (e.key === ' ' || e.key === 'Enter') { e.preventDefault(); (e.currentTarget as HTMLElement).click(); } }}>
              {s}
            </li>
          ))}
        </ol>
        {r.tip && <div className="notice down" style={{ marginTop: 14 }}><Sparkles size={18} /><span>{r.tip}</span></div>}

        <button className="btn primary block" style={{ marginTop: 20 }} onClick={() => {
          haptic.success();
          toast(`${addPlanToBasket(bought)} ingredientes en la cesta`);
        }}><ShoppingBasket size={18} /> Añadir ingredientes a la cesta</button>
        <div className="two" style={{ marginTop: 10 }}>
          <button className="btn" onClick={swap} disabled={swapping}><RefreshCw size={18} className={swapping ? 'spin' : undefined} /> {swapping ? 'Buscando otra…' : 'Cambiar receta'}</button>
          <button className="btn" onClick={onClose}>Cerrar</button>
        </div>
      </div>
    </>
  );
}
