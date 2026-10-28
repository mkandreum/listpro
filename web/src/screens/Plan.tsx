import { useEffect, useMemo, useState } from 'react';
import {
  AlertTriangle, ChefHat, Clock3, Flame, Microwave, Minus, Plus, RefreshCw, ShoppingBasket, SlidersHorizontal, Sparkles, Store, Wallet,
} from 'lucide-react';
import { api } from '../api';
import { money } from '../lib';
import { href, navigate } from '../router';
import { addPlanToBasket, clearPlan, generatePlan, toast, useStore } from '../store';
import type { MealPlan, PlanPrefs, PlanRecipe, PlannerOptions } from '../types';
import { haptic } from '../fx/haptics';
import { useTweened } from '../fx/motion';
import { Price } from '../components/ProductTag';

export const DEFAULT_PREFS: PlanPrefs = {
  budget: 45, people: 2, nights: 5, maxMinutes: 40, diets: ['omnivora'], appliances: ['fuego', 'microondas'],
  likes: '', dislikes: '', allergies: '', pantry: '', store: 'mercadona',
};

const APPLIANCE_SHORT: Record<string, string> = {
  fuego: 'Fuego', horno: 'Horno', microondas: 'Micro', freidora_aire: 'Freidora de aire', olla_rapida: 'Olla rápida',
  robot: 'Robot', sandwichera: 'Plancha', batidora: 'Batidora',
};
const DIET_SHORT: Record<string, string> = {
  omnivora: 'De todo', vegetariana: 'Vegetariana', vegana: 'Vegana', pescetariana: 'Pescetariana', sin_gluten: 'Sin gluten',
  sin_lactosa: 'Sin lactosa', alta_proteina: 'Alta en proteína', baja_carbohidratos: 'Baja en hidratos', ligera: 'Ligera', halal: 'Halal',
};
const LOADING = ['Mirando los precios de hoy…', 'Cuadrando tu presupuesto…', 'Eligiendo cenas que te gusten…', 'Reaprovechando ingredientes…', 'Calculando la nutrición…', 'Montando tu lista de la compra…'];

export function PlanScreen({ recipe }: { recipe?: number }) {
  const plan = useStore((s) => s.plan);
  const savedPrefs = useStore((s) => s.planPrefs);
  const [editing, setEditing] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [options, setOptions] = useState<PlannerOptions | null>(null);

  useEffect(() => { api.plannerOptions().then(setOptions).catch(() => setOptions(null)); }, []);

  const run = async (prefs: PlanPrefs) => {
    setBusy(true);
    setError(null);
    haptic.tap();
    try {
      await generatePlan(prefs);
      haptic.success();
      setEditing(false);
      window.scrollTo({ top: 0 });
    } catch (e) {
      haptic.warn();
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };

  if (busy) return <Cooking />;
  if (!plan || editing) {
    return <PrefsForm initial={savedPrefs ?? plan?.prefs ?? DEFAULT_PREFS} options={options} error={error} onSubmit={run} onCancel={plan ? () => setEditing(false) : undefined} />;
  }
  const open = recipe != null ? plan.recipes[recipe] : undefined;
  return (
    <>
      <PlanView plan={plan} onRedo={() => run(plan.prefs)} onEdit={() => setEditing(true)} />
      {open && <RecipeSheet r={open} onClose={() => history.length > 1 ? history.back() : navigate({ name: 'plan' }, true)} />}
    </>
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
  const disabled = options != null && !options.enabled;
  const budget = useTweened(p.budget, 200);

  return (
    <form className="plan-form" onSubmit={(e) => { e.preventDefault(); onSubmit(p); }}>
      <div className="row-head" style={{ marginTop: 6 }}>
        <h1 className="h-section"><span className="h-icon"><ChefHat size={22} strokeWidth={2.2} /></span>Tus cenas</h1>
        {onCancel && <button type="button" className="btn" onClick={onCancel}>Cancelar</button>}
      </div>
      <p className="muted lead">Te planificamos las cenas de la semana con productos y precios reales de tu súper, a tu gusto y con lo que tienes en la cocina.</p>

      <section className="panel">
        <div className="field-head"><Wallet size={20} /><h2 className="h-block">Presupuesto de la semana</h2></div>
        <div className="budget"><Price value={budget} className="price big-price" /><span className="muted">para toda la compra</span></div>
        <input className="range" type="range" min={15} max={150} step={5} value={p.budget} aria-label="Presupuesto semanal en euros"
          onChange={(e) => { haptic.tick(); up('budget', Number(e.target.value)); }} />
        <div className="two">
          <Counter label="Personas" value={p.people} min={1} max={8} onChange={(v) => up('people', v)} />
          <Counter label="Cenas" value={p.nights} min={1} max={7} onChange={(v) => up('nights', v)} />
        </div>
      </section>

      <section className="panel">
        <div className="field-head"><Store size={20} /><h2 className="h-block">Tu súper</h2></div>
        <div className="chips wrap">
          {(options?.stores ?? [{ id: 'mercadona', label: 'Mercadona' }]).map((s) => (
            <button type="button" key={s.id} className="chip text-only" aria-pressed={p.store === s.id} onClick={() => up('store', s.id)}>{s.label}</button>
          ))}
        </div>
        <p className="muted small">Por ahora trabajamos con el catálogo de Mercadona.</p>
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
        <div className="field-head" style={{ marginTop: 18 }}><Clock3 size={18} /><b>Tiempo máximo por cena</b></div>
        <div className="segmented four" role="group" aria-label="Tiempo máximo">
          {[15, 30, 45, 60].map((m) => (
            <button type="button" key={m} aria-pressed={p.maxMinutes === m} onClick={() => { haptic.tick(); up('maxMinutes', m); }}>{m} min</button>
          ))}
        </div>
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
      <p className="muted small center">Tarda entre 20 y 60 segundos. No es un servicio de reparto: te ayudamos a organizar la compra y las cenas.</p>
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

function Cooking() {
  const [i, setI] = useState(0);
  useEffect(() => { const t = setInterval(() => setI((x) => (x + 1) % LOADING.length), 2600); return () => clearInterval(t); }, []);
  return (
    <div className="cooking" role="status" aria-live="polite">
      <div className="pot"><ChefHat size={46} strokeWidth={1.8} /><span /><span /><span /></div>
      <h2 className="h-block">Preparando tu plan</h2>
      <p key={i} className="muted roll-text">{LOADING[i]}</p>
    </div>
  );
}

// ───────────── Plan ─────────────

function PlanView({ plan, onRedo, onEdit }: { plan: MealPlan; onRedo: () => void; onEdit: () => void }) {
  const idx = useStore((s) => s.idx);
  const t = plan.totals;
  const pct = Math.min(100, (t.shopping / t.budget) * 100);
  const shopping = useMemo(() => plan.shopping.filter((s) => idx.products.size === 0 || idx.products.has(s.productId)), [plan, idx]);

  return (
    <>
      <div className="row-head" style={{ marginTop: 6 }}>
        <h1 className="h-section"><span className="h-icon"><ChefHat size={22} strokeWidth={2.2} /></span>Tus cenas</h1>
        <button className="btn" onClick={onEdit}><SlidersHorizontal size={18} /> Ajustar</button>
      </div>

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
          <div><b className="num">{t.kcalAvg}</b><span>kcal de media</span></div>
          <div><b className="num">{t.proteinAvg} g</b><span>proteína</span></div>
        </div>
        <div className="total-actions">
          <button className="btn white" onClick={() => {
            haptic.success();
            const n = addPlanToBasket(shopping);
            toast(`${n} productos en la cesta`);
          }}><ShoppingBasket size={18} /> Añadir la compra a la cesta</button>
          <button className="btn ghost-white" onClick={onRedo}><RefreshCw size={18} /> Otro plan</button>
        </div>
      </section>

      {plan.summary && <p className="muted lead" style={{ marginTop: 14 }}>{plan.summary}</p>}
      {plan.warnings.map((w) => <div key={w} className="notice up"><AlertTriangle size={20} /><span>{w}</span></div>)}

      <h2 className="h-section" style={{ marginTop: 30, marginBottom: 12, fontSize: '1.6rem' }}>La semana</h2>
      <div className="dinners">
        {plan.recipes.map((r, i) => (
          <a key={r.day} href={href({ name: 'plan', recipe: i })} className="dinner" style={{ '--d': i } as React.CSSProperties} onClick={() => haptic.tap()}>
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
                {r.appliances.includes('microondas') && <span><Microwave size={14} /></span>}
              </div>
            </div>
          </a>
        ))}
      </div>

      <div className="row-head" style={{ marginTop: 34 }}>
        <h2 className="h-section" style={{ fontSize: '1.6rem' }}>Lista de la compra</h2>
        <span className="stamp num">{shopping.length} productos</span>
      </div>
      <div className="panel" style={{ marginTop: 0, padding: '6px 16px' }}>
        {shopping.map((s) => (
          <div key={s.productId} className="shop-row">
            {s.image ? <img src={s.image} alt="" loading="lazy" /> : <span className="ph" />}
            <div className="info">
              <b>{s.name}</b>
              <span className="muted small">{s.size} · para {s.usedIn.map((d) => d.slice(0, 3)).join(', ')}</span>
            </div>
            <div className="right num"><b>{money(s.cost)}</b><span className="muted small">{s.packs} × {money(s.price)}</span></div>
          </div>
        ))}
        <div className="shop-total num"><span>Total</span><b>{money(t.shopping)}</b></div>
      </div>
      <p className="muted small" style={{ marginTop: 12 }}>
        Precios reales de hoy. La nutrición es una estimación hecha por IA: si tienes alergias, revisa siempre la etiqueta del producto.
      </p>
      <button className="btn danger block" style={{ marginTop: 18 }} onClick={() => { clearPlan(); toast('Plan borrado'); }}>Borrar el plan</button>
    </>
  );
}

// ───────────── Receta ─────────────

function RecipeSheet({ r, onClose }: { r: PlanRecipe; onClose: () => void }) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { removeEventListener('keydown', onKey); document.body.style.overflow = prev; };
  }, [onClose]);
  const n = r.nutrition;
  const bought = r.ingredients.filter((i) => i.productId).map((i) => ({ productId: i.productId!, packs: Math.max(1, Math.ceil(i.fraction - 0.05)) }));

  return (
    <>
      <div className="sheet-scrim" onClick={onClose} />
      <div className="sheet recipe" role="dialog" aria-modal="true" aria-labelledby="recipe-title">
        <div className="grip" />
        <span className="stamp">{r.day} · {r.servings} {r.servings === 1 ? 'ración' : 'raciones'}</span>
        <h2 id="recipe-title">{r.name}</h2>
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
        <ol className="steps">{r.steps.map((s, k) => <li key={k}>{s}</li>)}</ol>
        {r.tip && <div className="notice down" style={{ marginTop: 14 }}><Sparkles size={18} /><span>{r.tip}</span></div>}

        <button className="btn primary block" style={{ marginTop: 20 }} onClick={() => {
          haptic.success();
          toast(`${addPlanToBasket(bought)} ingredientes en la cesta`);
        }}><ShoppingBasket size={18} /> Añadir ingredientes a la cesta</button>
        <button className="btn block" style={{ marginTop: 10 }} onClick={onClose}>Cerrar</button>
      </div>
    </>
  );
}
