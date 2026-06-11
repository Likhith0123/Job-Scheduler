import { Link, Outlet, useNavigate } from 'react-router-dom';
import { clearApiKey } from '../api';

export default function Layout() {
  const navigate = useNavigate();

  function handleLogout() {
    clearApiKey();
    navigate('/connect');
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark">⧖</span>
          <div>
            <strong>ChronoFlow</strong>
            <p>Job scheduling dashboard</p>
          </div>
        </div>
        <nav className="topnav">
          <Link to="/">Jobs</Link>
          <Link to="/setup">Setup</Link>
          <button type="button" className="btn btn-ghost" onClick={handleLogout}>
            Sign out
          </button>
        </nav>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
