import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi, setApiKey } from '../api';

export default function SetupPage() {
  const navigate = useNavigate();
  const [tenantName, setTenantName] = useState('my-tenant');
  const [rateLimit, setRateLimit] = useState(120);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    setResult(null);
    try {
      const tenant = await authApi.createTenant(tenantName, Number(rateLimit));
      const key = await authApi.createKey(tenant.id, 'ui-key');
      setResult({ tenant, key });
      setApiKey(key.apiKey);
    } catch (err) {
      setError(err.message || 'Setup failed. Is auth-service running on port 8081?');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="panel auth-panel wide">
        <div className="brand compact">
          <span className="brand-mark">⧖</span>
          <div>
            <strong>First-time setup</strong>
            <p>Create a tenant and API key (calls auth-service directly)</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="stack">
          <label>
            Tenant name
            <input value={tenantName} onChange={(e) => setTenantName(e.target.value)} required />
          </label>
          <label>
            Rate limit (requests / minute)
            <input
              type="number"
              min="1"
              value={rateLimit}
              onChange={(e) => setRateLimit(e.target.value)}
              required
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating…' : 'Create tenant & key'}
          </button>
        </form>

        {result && (
          <div className="success-box stack">
            <p><strong>Tenant ID:</strong> <code>{result.tenant.id}</code></p>
            <p><strong>API Key:</strong> <code>{result.key.apiKey}</code></p>
            <button type="button" className="btn btn-primary" onClick={() => navigate('/')}>
              Go to dashboard
            </button>
          </div>
        )}

        <p className="muted">
          Already have a key? <Link to="/connect">Connect</Link>
        </p>
      </div>
    </div>
  );
}
