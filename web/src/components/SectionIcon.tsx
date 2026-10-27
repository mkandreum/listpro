import { Baby, Leaf, Milk, PawPrint, ShoppingBag, Snowflake, Sparkles, SprayCan, Wheat, Wine, type LucideProps } from 'lucide-react';

const ICONS = { leaf: Leaf, milk: Milk, wheat: Wheat, snowflake: Snowflake, wine: Wine, sparkles: Sparkles, spray: SprayCan, baby: Baby, paw: PawPrint, bag: ShoppingBag };

/** Icono de un pasillo; el nombre llega de la API para que web y Android usen el mismo. */
export function SectionIcon({ name, ...props }: { name: string } & LucideProps) {
  const Icon = ICONS[name as keyof typeof ICONS] ?? ShoppingBag;
  return <Icon {...props} />;
}
