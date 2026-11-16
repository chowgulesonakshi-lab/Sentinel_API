import { Routes, Route } from "react-router-dom";

import Auth from "./pages/Auth";
import Dashboard from "./pages/Dashboard";
import Projects from "./pages/Projects";
import Services from "./pages/Services";
import Endpoints from "./pages/Endpoints";
import Metrics from "./pages/Metrics";
import NotFound from "./pages/NotFound";
import Alerts from "./pages/Alerts";
import Dependencies from "./pages/Dependencies";

function App() {
    return (
        <Routes>
            <Route path="/" element={<Auth />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/projects" element={<Projects />} />
            <Route path="/services" element={<Services />} />
            <Route path="/alerts" element={<Alerts />} />
            <Route path="/dependencies" element={<Dependencies />} />
            <Route path="/endpoints" element={<Endpoints />} />
            <Route path="/metrics" element={<Metrics />} />
            <Route path="*" element={<NotFound />} />
        </Routes>
    );
}

export default App;
