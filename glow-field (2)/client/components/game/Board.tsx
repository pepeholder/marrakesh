import { motion } from "framer-motion";
import { useMemo } from "react";

export interface Token {
  id: string;
  color: string;
  x: number;
  y: number;
}

export function Board({ size = 7, tokens = [] as Token[], backgroundUrl }: { size?: number; tokens?: Token[]; backgroundUrl?: string }) {
  const cells = useMemo(() => Array.from({ length: size * size }, (_, i) => i), [size]);

  return (
    <div
      className="relative mx-auto aspect-square w-full max-w-[720px] overflow-hidden rounded-xl border border-border bg-amber-100 p-2 shadow-inner"
      style={backgroundUrl ? { backgroundImage: `url(${backgroundUrl})`, backgroundSize: "cover", backgroundPosition: "center" } : undefined}
    >
      <div
        className="grid h-full w-full"
        style={{ gridTemplateColumns: `repeat(${size}, minmax(0, 1fr))`, gridTemplateRows: `repeat(${size}, minmax(0, 1fr))` }}
      >
        {cells.map((i) => (
          <div key={i} className="relative border border-amber-700/20">
            <div className="absolute inset-0 bg-amber-900/5" />
          </div>
        ))}
      </div>
      {/* Tokens */}
      {tokens.map((t) => (
        <motion.div
          key={t.id}
          className="absolute size-8 -translate-x-1/2 -translate-y-1/2 rounded-full ring-2 ring-black/10 shadow-md"
          style={{ backgroundColor: t.color }}
          animate={{
            left: `${((t.x + 0.5) / size) * 100}%`,
            top: `${((t.y + 0.5) / size) * 100}%`,
          }}
          transition={{ type: "spring", stiffness: 300, damping: 24 }}
        />
      ))}
    </div>
  );
}
