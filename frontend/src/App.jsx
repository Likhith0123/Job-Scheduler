import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import ConnectPage from './pages/ConnectPage';
import JobsPage from './pages/JobsPage';
import JobDetailPage from './pages/JobDetailPage';
import SetupPage from './pages/SetupPage';
import { getApiKey } from './api';

function RequireApiKey({ children }) {
  if (!getApiKey()) {
    return <Navigate to="/connect" replace />;
  }
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/connect" element={<ConnectPage />} />
      <Route path="/setup" element={<SetupPage />} />
      <Route element={<Layout />}>
        <Route
          path="/"
          element={
            <RequireApiKey>
              <JobsPage />
            </RequireApiKey>
          }
        />
        <Route
          path="/jobs/:jobId"
          element={
            <RequireApiKey>
              <JobDetailPage />
            </RequireApiKey>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
