import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { API } from "@/lib/api";
import { Session } from "@shared/api";
import { useAuth } from "@/context/AuthContext";
import { toast } from "sonner";

export default function SessionsPage() {
  const { token } = useAuth();
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    refresh();
    const id = setInterval(refresh, 5000);
    return () => clearInterval(id);
  }, [token]);

  async function refresh() {
    try {
      setLoading(true);
      const list = await API.listSessions(token ?? undefined);
      setSessions(list);
    } catch {
      // ignore until backend is connected
    } finally {
      setLoading(false);
    }
  }

  async function onCreate(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const name = String(fd.get("name")) || "Новая партия";
    try {
      setCreating(true);
      const s = await API.createSession({ name }, token ?? undefined);
      setSessions((prev) => [s, ...prev]);
      toast.success("Партия создана");
      (e.target as HTMLFormElement).reset();
    } catch (e: any) {
      toast.error(e.message || "Не удалось создать партию");
    } finally {
      setCreating(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-2xl border bg-card p-6 shadow-sm">
        <h1 className="text-3xl font-extrabold tracking-tight">Игровые сессии</h1>
        <p className="mt-2 text-muted-foreground">Создайте новую или присоединяйтесь к существующей сессии.</p>
        <form onSubmit={onCreate} className="mt-4 flex flex-col gap-3 sm:flex-row">
          <Input name="name" placeholder="Название партии" />
          <Button disabled={creating} type="submit">Создать</Button>
        </form>
      </div>

      <div className="rounded-2xl border bg-card p-6 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold">Список сессий</h2>
          <Button variant="outline" onClick={refresh} disabled={loading}>Обновить</Button>
        </div>
        <ul className="space-y-3">
          {sessions.length === 0 && (
            <li className="text-sm text-muted-foreground">Пока нет активных партий.</li>
          )}
          {sessions.map((s) => (
            <li key={s.id} className="flex flex-col gap-3 rounded-xl border p-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <div className="flex items-center gap-3">
                  <span className={"inline-flex size-2 rounded-full "+(s.status === "active" ? "bg-emerald-500" : s.status === "waiting" ? "bg-amber-500" : "bg-stone-400")} />
                  <div className="font-semibold">{s.name}</div>
                </div>
                <div className="mt-1 text-sm text-muted-foreground">Игроков: {s.players?.length ?? 0}</div>
              </div>
              <div className="flex gap-2">
                <Button variant="outline" onClick={async ()=>{ try { const x = await API.autoOrder(s.id, token ?? undefined); setSessions(prev=>prev.map(p=>p.id===s.id?x:p)); toast.success("Очередность назначена"); } catch(e:any){ toast.error(e.message||"Ошибка"); } }}>Авто‑очередность</Button>
                <a href={`/session/${s.id}`}><Button>Открыть</Button></a>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
