import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { jobsApi } from '../api';

function statusClass(status) {
  return `badge badge-${status.toLowerCase()}`;
}

export default function JobDetailPage() {
  const { jobId } = useParams();
  const [job, setJob] = useState(null);
  const [runs, setRuns] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setError('');
    try {
      const [jobData, runsData] = await Promise.all([
        jobsApi.get(jobId),
        jobsApi.runs(jobId),
      ]);
      setJob(jobData);
      setRuns(runsData);
    } catch (err) {
      setError(err.message || 'Failed to load job');
    } finally {
      setLoading(false);
    }
  }, [jobId]);

  useEffect(() => {
    load();
    const interval = setInterval(load, 10000);
    return () => clearInterval(interval);
  }, [load]);

  async function handleAction(action) {
    setError('');
    try {
      if (action === 'pause') await jobsApi.pause(jobId);
      if (action === 'resume') await jobsApi.resume(jobId);
      if (action === 'delete') {
        if (!window.confirm('Delete this job?')) return;
        await jobsApi.remove(jobId);
        window.location.href = '/';
        return;
      }
      await load();
    } catch (err) {
      setError(err.message || 'Action failed');
    }
  }

  if (loading) return <p className="muted">Loading…</p>;
  if (!job) return <p className="error">Job not found</p>;

  return (
    <div className="stack-lg">
      <Link to="/" className="back-link">← Back to jobs</Link>

      <div className="page-header">
        <div>
          <h1>{job.name}</h1>
          <p className="mono muted">{job.id}</p>
        </div>
        <div className="action-row">
          {job.status === 'ACTIVE' ? (
            <button type="button" className="btn btn-ghost" onClick={() => handleAction('pause')}>Pause</button>
          ) : (
            <button type="button" className="btn btn-ghost" onClick={() => handleAction('resume')}>Resume</button>
          )}
          <button type="button" className="btn btn-danger" onClick={() => handleAction('delete')}>Delete</button>
        </div>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="grid-2">
        <div className="panel stack">
          <h2>Details</h2>
          <p><span className="muted">Status</span> <span className={statusClass(job.status)}>{job.status}</span></p>
          <p><span className="muted">Cron</span> <code>{job.cronExpression}</code></p>
          <p><span className="muted">Next run</span> {job.nextRunAt ? new Date(job.nextRunAt).toLocaleString() : '—'}</p>
          <p><span className="muted">Created</span> {new Date(job.createdAt).toLocaleString()}</p>
          <div>
            <span className="muted">Payload</span>
            <pre className="code-block">{JSON.stringify(job.payload, null, 2)}</pre>
          </div>
        </div>

        <div className="panel stack">
          <h2>Run history</h2>
          <p className="muted">Refreshes every 10s</p>
          {runs.length === 0 ? (
            <p className="muted">No runs yet. Wait for the scheduler to fire.</p>
          ) : (
            <div className="runs-list">
              {runs.map((run) => (
                <div key={run.id} className="run-card">
                  <div className="run-card-header">
                    <span className={statusClass(run.status)}>{run.status}</span>
                    <span className="muted-sm">{new Date(run.startedAt).toLocaleString()}</span>
                  </div>
                  {run.result && <p className="mono muted-sm">{run.result}</p>}
                  {run.completedAt && (
                    <p className="muted-sm">Completed {new Date(run.completedAt).toLocaleString()}</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
