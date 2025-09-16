import { Link, NavLink, useLocation } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";

export default function Header() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const isHome = location.pathname === "/";

  return (
    <header className="sticky top-0 z-40 border-b bg-background/80 backdrop-blur">
      <div className="container mx-auto flex h-16 items-center justify-between">
        <Link to="/" className="flex items-center gap-2 text-xl font-extrabold tracking-tight">
          <span className="inline-block size-7 rounded-md bg-primary/20 ring-1 ring-primary/30" />
          Marrakech Online
        </Link>
        <div className="flex items-center gap-3">
          {user ? (
            <div className="flex items-center gap-3">
              <span className="hidden text-sm text-muted-foreground sm:inline">{user.name}</span>
              <Button variant="outline" onClick={logout}>Выйти</Button>
              {isHome ? (
                <a href="#new" className="hidden sm:block"><Button>Создать партию</Button></a>
              ) : null}
            </div>
          ) : (
            <Link to="/auth"><Button variant="outline">Вход / Регистрация</Button></Link>
          )}
        </div>
      </div>
    </header>
  );
}
