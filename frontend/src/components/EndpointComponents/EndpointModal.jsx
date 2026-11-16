import { useEffect, useState } from "react";
import {
    createEndpoint,
    updateEndpoint
} from "../../services/endpointsService";
import { getServices } from "../../services/apiServiceService";
import { Sliders, Globe, Code, ShieldAlert, Clock } from "lucide-react";

function EndpointModal({
    isOpen,
    onClose,
    refreshEndpoints,
    selectedEndpoint
}) {
    const [services, setServices] = useState([]);
    const [activeTab, setActiveTab] = useState("general");

    const [endpoint, setEndpoint] = useState({
        method: "GET",
        url: "",
        timeoutMillis: 5000,
        intervalSeconds: 60,
        expectedStatusCodes: "200,201,202,204",
        slowResponseThresholdMillis: 1000,
        consecutiveFailuresThreshold: 1,
        anomalyDetectionEnabled: true,
        anomalySensitivityMultiplier: 3,
        anomalyObservationWindow: 20,
        anomalyMinimumObservations: 5,
        headers: "",
        requestBody: "",
        enabled: true,
        apiServiceId: ""
    });

    useEffect(() => {
        if (!isOpen) return;

        loadServices();

        if (selectedEndpoint) {
            setEndpoint({
                method: selectedEndpoint.method || "GET",
                url: selectedEndpoint.url || "",
                timeoutMillis: selectedEndpoint.timeoutMillis || 5000,
                intervalSeconds: selectedEndpoint.intervalSeconds || 60,
                expectedStatusCodes: selectedEndpoint.expectedStatusCodes || "200,201,202,204",
                slowResponseThresholdMillis: selectedEndpoint.slowResponseThresholdMillis || 1000,
                consecutiveFailuresThreshold: selectedEndpoint.consecutiveFailuresThreshold || 1,
                anomalyDetectionEnabled: selectedEndpoint.anomalyDetectionEnabled ?? true,
                anomalySensitivityMultiplier: selectedEndpoint.anomalySensitivityMultiplier || 3,
                anomalyObservationWindow: selectedEndpoint.anomalyObservationWindow || 20,
                anomalyMinimumObservations: selectedEndpoint.anomalyMinimumObservations || 5,
                headers: selectedEndpoint.headers || "",
                requestBody: selectedEndpoint.requestBody || "",
                enabled: selectedEndpoint.enabled ?? true,
                apiServiceId: selectedEndpoint.apiServiceId || ""
            });
        } else {
            setEndpoint({
                method: "GET",
                url: "",
                timeoutMillis: 5000,
                intervalSeconds: 60,
                expectedStatusCodes: "200,201,202,204",
                slowResponseThresholdMillis: 1000,
                consecutiveFailuresThreshold: 1,
                anomalyDetectionEnabled: true,
                anomalySensitivityMultiplier: 3,
                anomalyObservationWindow: 20,
                anomalyMinimumObservations: 5,
                headers: "",
                requestBody: "",
                enabled: true,
                apiServiceId: ""
            });
        }
        setActiveTab("general");
    }, [isOpen, selectedEndpoint]);

    const loadServices = async () => {
        try {
            const data = await getServices();
            setServices(data);
        } catch (error) {
            console.error(error);
        }
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setEndpoint({
            ...endpoint,
            [name]: type === "checkbox" ? checked : value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const payload = {
            ...endpoint,
            timeoutMillis: Number(endpoint.timeoutMillis),
            intervalSeconds: Number(endpoint.intervalSeconds),
            slowResponseThresholdMillis: Number(endpoint.slowResponseThresholdMillis),
            consecutiveFailuresThreshold: Number(endpoint.consecutiveFailuresThreshold),
            anomalySensitivityMultiplier: Number(endpoint.anomalySensitivityMultiplier),
            anomalyObservationWindow: Number(endpoint.anomalyObservationWindow),
            anomalyMinimumObservations: Number(endpoint.anomalyMinimumObservations),
            apiServiceId: Number(endpoint.apiServiceId)
        };

        try {
            if (selectedEndpoint) {
                await updateEndpoint(selectedEndpoint.id, payload);
            } else {
                await createEndpoint(payload);
            }

            refreshEndpoints();
            onClose();
        } catch (error) {
            console.error("Failed to save endpoint configuration:", error);
            alert("Error saving endpoint. Please ensure all values are valid.");
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-xs flex items-center justify-center z-50 p-4">
            <div className="bg-white dark:bg-slate-900 rounded-2xl w-full max-w-2xl border border-slate-200 dark:border-slate-800 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
                {/* Header */}
                <div className="p-6 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center">
                    <div>
                        <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
                            {selectedEndpoint ? "Configure Endpoint" : "Add New Endpoint"}
                        </h2>
                        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                            Set up endpoint monitoring, intervals, thresholds, and payloads.
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 text-xl font-bold p-1"
                    >
                        ✕
                    </button>
                </div>

                {/* Tabs */}
                <div className="flex border-b border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50 px-6 gap-2">
                    <button
                        type="button"
                        onClick={() => setActiveTab("general")}
                        className={`py-3 px-4 text-sm font-semibold border-b-2 flex items-center gap-2 transition ${
                            activeTab === "general"
                                ? "border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400"
                                : "border-transparent text-slate-500 hover:text-slate-800 dark:hover:text-slate-300"
                        }`}
                    >
                        <Globe size={16} />
                        General
                    </button>
                    <button
                        type="button"
                        onClick={() => setActiveTab("rules")}
                        className={`py-3 px-4 text-sm font-semibold border-b-2 flex items-center gap-2 transition ${
                            activeTab === "rules"
                                ? "border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400"
                                : "border-transparent text-slate-500 hover:text-slate-800 dark:hover:text-slate-300"
                        }`}
                    >
                        <Sliders size={16} />
                        Monitoring Rules
                    </button>
                    <button
                        type="button"
                        onClick={() => setActiveTab("payload")}
                        className={`py-3 px-4 text-sm font-semibold border-b-2 flex items-center gap-2 transition ${
                            activeTab === "payload"
                                ? "border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400"
                                : "border-transparent text-slate-500 hover:text-slate-800 dark:hover:text-slate-300"
                        }`}
                    >
                        <Code size={16} />
                        Headers & Body
                    </button>
                </div>

                {/* Body Form */}
                <form onSubmit={handleSubmit} className="p-6 overflow-y-auto space-y-4 flex-1">
                    {activeTab === "general" && (
                        <div className="space-y-4">
                            <div>
                                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                    Target Service *
                                </label>
                                <select
                                    name="apiServiceId"
                                    value={endpoint.apiServiceId}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none"
                                    required
                                >
                                    <option value="">Select Service</option>
                                    {services.map((service) => (
                                        <option key={service.id} value={service.id}>
                                            {service.projectName} • {service.name}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="border-t border-slate-200 dark:border-slate-800 pt-4 space-y-3">
                                <label className="flex items-center gap-3 text-slate-700 dark:text-slate-300 cursor-pointer select-none">
                                    <input
                                        type="checkbox"
                                        name="anomalyDetectionEnabled"
                                        checked={endpoint.anomalyDetectionEnabled}
                                        onChange={handleChange}
                                        className="w-4 h-4 rounded text-blue-600 focus:ring-blue-500 border-slate-300"
                                    />
                                    <span className="font-medium">Statistical Anomaly Detection</span>
                                </label>
                                <div className="grid grid-cols-3 gap-3">
                                    <div>
                                        <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">Sensitivity (σ)</label>
                                        <input type="number" min="0.1" step="0.1" name="anomalySensitivityMultiplier" value={endpoint.anomalySensitivityMultiplier} onChange={handleChange} disabled={!endpoint.anomalyDetectionEnabled} className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white" />
                                    </div>
                                    <div>
                                        <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">History Window</label>
                                        <input type="number" min="1" name="anomalyObservationWindow" value={endpoint.anomalyObservationWindow} onChange={handleChange} disabled={!endpoint.anomalyDetectionEnabled} className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white" />
                                    </div>
                                    <div>
                                        <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">Minimum Samples</label>
                                        <input type="number" min="3" name="anomalyMinimumObservations" value={endpoint.anomalyMinimumObservations} onChange={handleChange} disabled={!endpoint.anomalyDetectionEnabled} className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white" />
                                    </div>
                                </div>
                                <span className="text-[11px] text-slate-400">An anomaly is raised only when the current successful latency exceeds the endpoint’s historical average plus the configured standard-deviation multiplier.</span>
                            </div>

                            <div className="grid grid-cols-4 gap-3">
                                <div className="col-span-1">
                                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                        Method *
                                    </label>
                                    <select
                                        name="method"
                                        value={endpoint.method}
                                        onChange={handleChange}
                                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono font-bold focus:ring-2 focus:ring-blue-500 outline-none"
                                    >
                                        <option>GET</option>
                                        <option>POST</option>
                                        <option>PUT</option>
                                        <option>PATCH</option>
                                        <option>DELETE</option>
                                    </select>
                                </div>
                                <div className="col-span-3">
                                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                        Endpoint URL *
                                    </label>
                                    <input
                                        type="url"
                                        name="url"
                                        placeholder="https://api.example.com/health"
                                        value={endpoint.url}
                                        onChange={handleChange}
                                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono text-sm focus:ring-2 focus:ring-blue-500 outline-none"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="pt-2">
                                <label className="flex items-center gap-3 text-slate-700 dark:text-slate-300 cursor-pointer select-none">
                                    <input
                                        type="checkbox"
                                        name="enabled"
                                        checked={endpoint.enabled}
                                        onChange={handleChange}
                                        className="w-4 h-4 rounded text-blue-600 focus:ring-blue-500 border-slate-300"
                                    />
                                    <span className="font-medium">Active Monitoring Enabled</span>
                                    <span className="text-xs text-slate-400">(Endpoint will be polled dynamically by scheduler)</span>
                                </label>
                            </div>
                        </div>
                    )}

                    {activeTab === "rules" && (
                        <div className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                        Monitoring Interval (Seconds) *
                                    </label>
                                    <div className="relative">
                                        <input
                                            type="number"
                                            name="intervalSeconds"
                                            min="5"
                                            max="86400"
                                            value={endpoint.intervalSeconds}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none pr-8"
                                            required
                                        />
                                        <span className="absolute right-3 top-2 text-xs text-slate-400">sec</span>
                                    </div>
                                    <span className="text-[11px] text-slate-400">Frequency of scheduled health checks (e.g. 10s, 60s, 300s)</span>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                        Request Timeout (ms) *
                                    </label>
                                    <div className="relative">
                                        <input
                                            type="number"
                                            name="timeoutMillis"
                                            min="100"
                                            max="60000"
                                            value={endpoint.timeoutMillis}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none pr-8"
                                            required
                                        />
                                        <span className="absolute right-3 top-2 text-xs text-slate-400">ms</span>
                                    </div>
                                    <span className="text-[11px] text-slate-400">Abort request after this time limit (e.g. 5000ms)</span>
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                        Expected HTTP Status Codes *
                                    </label>
                                    <input
                                        type="text"
                                        name="expectedStatusCodes"
                                        placeholder="200,201,202,204 or 200-299"
                                        value={endpoint.expectedStatusCodes}
                                        onChange={handleChange}
                                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono text-sm focus:ring-2 focus:ring-blue-500 outline-none"
                                        required
                                    />
                                    <span className="text-[11px] text-slate-400">Comma-separated list (e.g. 200,204,401) or ranges (200-299)</span>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                        Slow Response Threshold (ms) *
                                    </label>
                                    <div className="relative">
                                        <input
                                            type="number"
                                            name="slowResponseThresholdMillis"
                                            min="50"
                                            max="60000"
                                            value={endpoint.slowResponseThresholdMillis}
                                            onChange={handleChange}
                                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none pr-8"
                                            required
                                        />
                                        <span className="absolute right-3 top-2 text-xs text-slate-400">ms</span>
                                    </div>
                                    <span className="text-[11px] text-slate-400">Trigger SLOW_RESPONSE warning if latency exceeds this</span>
                                </div>
                            </div>

                            <div>
                                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                    Consecutive Failures Required for Alert *
                                </label>
                                <input
                                    type="number"
                                    name="consecutiveFailuresThreshold"
                                    min="1"
                                    max="20"
                                    value={endpoint.consecutiveFailuresThreshold}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none"
                                    required
                                />
                                <span className="text-[11px] text-slate-400">
                                    Only trigger a DOWN alert after N consecutive failures (prevents transient false alarms)
                                </span>
                            </div>
                        </div>
                    )}

                    {activeTab === "payload" && (
                        <div className="space-y-4">
                            <div>
                                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                    Custom Request Headers (JSON or Key: Value per line)
                                </label>
                                <textarea
                                    name="headers"
                                    rows="4"
                                    placeholder={'{\n  "Authorization": "Bearer YOUR_TOKEN",\n  "X-Custom-Header": "Sentinel"\n}'}
                                    value={endpoint.headers}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono text-xs focus:ring-2 focus:ring-blue-500 outline-none"
                                />
                                <span className="text-[11px] text-slate-400">Pass custom authorization, API keys, or custom user agents.</span>
                            </div>

                            {["POST", "PUT", "PATCH"].includes(endpoint.method) && (
                                <div>
                                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                        Request Body ({endpoint.method})
                                    </label>
                                    <textarea
                                        name="requestBody"
                                        rows="4"
                                        placeholder={'{\n  "status": "ping",\n  "timestamp": 123456789\n}'}
                                        value={endpoint.requestBody}
                                        onChange={handleChange}
                                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono text-xs focus:ring-2 focus:ring-blue-500 outline-none"
                                    />
                                    <span className="text-[11px] text-slate-400">Payload sent with POST/PUT/PATCH requests.</span>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Footer Buttons */}
                    <div className="flex justify-between items-center pt-4 border-t border-slate-200 dark:border-slate-800">
                        <div className="text-xs text-slate-400">
                            Changes take effect dynamically without restarting.
                        </div>
                        <div className="flex gap-3">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 text-sm font-medium transition"
                            >
                                Cancel
                            </button>

                            <button
                                type="submit"
                                className="px-5 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-sm transition"
                            >
                                {selectedEndpoint ? "Save Changes" : "Create Endpoint"}
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default EndpointModal;
