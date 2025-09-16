import { useEffect, useMemo, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { API } from "@/lib/api";
import { Board, Token } from "@/components/game/Board";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { GameState, Session } from "@shared/api";
import { toast } from "sonner";

const RUG_URL = "https://cdn.builder.io/api/v1/image/assets%2F9c98f74ce2d9433495c720297d8c0a5c%2Fa1be94ade0a9440eacc574b97f38cc29?format=webp&width=800";

export default function SessionPage() {
  const { id = "" } = useParams();
  const { token } = useAuth();
  const [session, setSession] = useState<Session | null>(null);
  const [state, setState] = useState<GameState | null>(null);
  const pollRef = useRef<number | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const s = await API.getSession(id, token ?? undefined);
        setSession(s);
      } catch (e: any) {
        toast.error(e.message || "Сессия не найдена");
      }
    })();
  }, [id, token]);

  useEffect(() => {
    // Try SSE first; if not available, fallback to polling
    let es: EventSource | null = null;
    const url = `/api/sessions/${id}/events`;
    try {
      es = new EventSource(url);
      es.onmessage = (ev) => {
        try {
          const data = JSON.parse(ev.data) as GameState;
          setState(data);
        } catch {}
      };
      es.onerror = () => {
        es?.close();
        es = null;
        startPoll();
      };
    } catch {
      startPoll();
    }

    function startPoll() {
      stopPoll();
      pollRef.current = window.setInterval(async () => {
        try {
          const st = await API.getGameState(id, token ?? undefined);
          setState(st);
        } catch {}
      }, 1500);
    }
    function stopPoll() {
      if (pollRef.current) window.clearInterval(pollRef.current);
      pollRef.current = null;
    }
    return () => {
      es?.close();
      stopPoll();
    };
  }, [id, token]);

  const tokenViews: Token[] = useMemo(() => {
    const p = state?.pieces?.[0];
    if (!p) return [];
    return [
      { id: "assam", color: "#c2410c", x: p.x ?? 3, y: p.y ?? 3 },
    ];
  }, [state]);

  async function move(dir: "left" | "right" | "forward") {
    try {
      await API.makeMove(id, { direction: dir }, token ?? undefined);
    } catch (e: any) {
      toast.error(e.message || "Не удалось сделать ход");
    }
  }

  return (
    <div className="grid grid-cols-1 gap-10 xl:grid-cols-[1fr_320px]">
      <section className="space-y-4">
        <div className="rounded-2xl border bg-card p-4">
          <Board size={7} tokens={tokenViews} backgroundUrl={RUG_URL} />
        </div>
        <div className="rounded-xl border p-4">
          <div className="mb-2 text-sm text-muted-foreground">Доступные направления</div>
          <div className="flex gap-3">
            <Button onClick={() => move("left")} variant="secondary">← Влево</Button>
            <Button onClick={() => move("forward")} className="bg-emerald-600 hover:bg-emerald-600/90">↑ Вперед</Button>
            <Button onClick={() => move("right")} variant="secondary">Вправо →</Button>
          </div>
        </div>
      </section>
      <aside className="space-y-4">
        <div className="rounded-xl border p-4">
          <div className="text-xs text-muted-foreground">Сессия</div>
          <div className="font-semibold">{session?.name ?? `#${id}`}</div>
          <div className="mt-2 text-sm text-muted-foreground">Игроков: {session?.players?.length ?? 0}</div>
        </div>
        <div className="rounded-xl border p-4">
          <div className="text-xs text-muted-foreground">Статус</div>
          <div className="font-semibold">{state?.status === "finished" ? "Игра завершена" : "Идет игра"}</div>
          {state?.winnerId && (
            <div className="mt-2 rounded-md bg-emerald-100 p-2 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-200">Победитель: {state.winnerId}</div>
          )}
          {state?.disconnectedPlayerIds?.length ? (
            <div className="mt-2 rounded-md bg-amber-100 p-2 text-amber-800 dark:bg-amber-900/30 dark:text-amber-200">Дисконнекты: {state.disconnectedPlayerIds.join(", ")}</div>
          ) : null}
        </div>
        <div className="rounded-xl border p-4">
          <div className="text-xs text-muted-foreground">Бросок кубика</div>
          <Button onClick={async () => { try { await API.rollDice(id, token ?? undefined); } catch (e:any){ toast.error(e.message || "Ошибка броска"); } }} className="mt-2 w-full">Бросить</Button>
        </div>
      </aside>
    </div>
  );
}
