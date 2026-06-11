import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { gatewayFetch, setApiKey } from '../api';

export default function ConnectPage() {
  const navigate = useNavigate();
  const [apiKey, setApiKeyInput] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    setApiKey(apiKey);
    try {
      await gatewayFetch('/api/jobs');
      navigate('/');
    } catch (err) {
      setError('Invalid API key or gateway unreachable.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="panel auth-panel">
        <div className="brand compact">
          <span className="brand-mark">⧖</span>
          <div>
            <strong>ChronoFlow</strong>
            <p>Connect with your API key</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="stack">
          <label>
            API Key
            <input
              type="password"
              value={apiKey}
              onChange={(e) => setApiKeyInput(e.target.value)}
              placeholder="cfk_..."
              required
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Connecting…' : 'Connect'}
          </button>
        </form>

        <p className="muted">
          No key yet? <Link to="/setup">Create a tenant &amp; API key</Link>
        </p>
      </div>
    </div>
  );
}
