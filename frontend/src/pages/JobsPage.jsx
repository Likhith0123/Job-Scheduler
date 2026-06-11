import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { jobsApi } from '../api';

const CRON_PRESETS = [
  { label: 'Every minute', value: '0 * * * * *' },
  { label: 'Every 30 seconds', value: '0/30 * * * * *' },
  { label: 'Every hour', value: '0 0 * * * *' },
  { label: 'Daily at 2am', value: '0 0 2 * * *' },
];

function statusClass(status) {
  return `badge badge-${status.toLowerCase()}`;
}

export default function JobsPage() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    name: '',
    cronExpression: '0 * * * * *',
    payload: '{"message":"Hello from UI"}',
  });

  const loadJobs = useCallback(async () => {
    setError('');
    try {
      const data = await jobsApi.list();
      setJobs(data);
    } catch (err) {
      setError(err.message || 'Failed to load jobs');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadJobs();
    const interval = setInterval(loadJobs, 15000);
    return () => clearInterval(interval);
  }, [loadJobs]);

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    try {
      const payload = JSON.parse(form.payload);
      await jobsApi.create({
        name: form.name,
        cronExpression: form.cronExpression,
        payload,
      });
      setShowForm(false);
      setForm({ name: '', cronExpression: '0 * * * * *', payload: '{"message":"Hello from UI"}' });
      await loadJobs();
    } catch (err) {
      setError(err.message || 'Failed to create job');
    }
  }

  return (
    <div className="stack-lg">
      <div className="page-header">
        <div>
          <h1>Jobs</h1>
          <p className="muted">Scheduled tasks for your tenant. Auto-refreshes every 15s.</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={() => setShowForm((v) => !v)}>
          {showForm ? 'Cancel' : 'New job'}
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      {showForm && (
        <form className="panel stack" onSubmit={handleCreate}>
          <h2>Create job</h2>
          <label>
            Name
            <input
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              placeholder="nightly-sync"
              required
            />
          </label>
          <label>
            Cron expression
            <input
              value={form.cronExpression}
              onChange={(e) => setForm({ ...form, cronExpression: e.target.value })}
              required
            />
          </label>
          <div className="preset-row">
            {CRON_PRESETS.map((preset) => (
              <button
                key={preset.value}
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={() => setForm({ ...form, cronExpression: preset.value })}
              >
                {preset.label}
              </button>
            ))}
          </div>
          <label>
            Payload (JSON)
            <textarea
              rows={4}
              value={form.payload}
              onChange={(e) => setForm({ ...form, payload: e.target.value })}
              required
            />
          </label>
          <button type="submit" className="btn btn-primary">Create</button>
        </form>
      )}

      {loading ? (
        <p className="muted">Loading jobs…</p>
      ) : jobs.length === 0 ? (
        <div className="panel empty">No jobs yet. Create one to get started.</div>
      ) : (
        <div className="table-wrap panel">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Status</th>
                <th>Cron</th>
                <th>Next run</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {jobs.map((job) => (
                <tr key={job.id}>
                  <td>
                    <strong>{job.name}</strong>
                    <div className="mono muted-sm">{job.id}</div>
                  </td>
                  <td><span className={statusClass(job.status)}>{job.status}</span></td>
                  <td className="mono">{job.cronExpression}</td>
                  <td>{job.nextRunAt ? new Date(job.nextRunAt).toLocaleString() : '—'}</td>
                  <td><Link className="btn btn-ghost btn-sm" to={`/jobs/${job.id}`}>Details</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
