import { useEffect, useState } from "react";
import MainLayout from "../layouts/MainLayout";
import { getAllAlerts } from "../services/alertsService";
import axios from "axios";
import { Sparkles, CheckCircle2, AlertTriangle, Info } from "lucide-react";

function Alerts() {
    const [alerts, setAlerts] = useState([]);
    const [activeTab, setActiveTab] = useState("active");
    const [showAll, setShowAll] = useState(false);
    const [aiAnalysis, setAiAnalysis] = useState({});
    const [analyzingAlert, setAnalyzingAlert] = useState(null);
    const [openAnalysis, setOpenAnalysis] = useState(null);

    useEffect(() => {
        loadAlerts();
    }, []);

    const loadAlerts = async () => {
        try {
            const data = await getAllAlerts();
            setAlerts(data);
        } catch (err) {
            console.error("ALERT ERROR:", err);
        }
    };

    const activeAlerts = alerts.filter(alert => !alert.resolved);
    const resolvedAlerts = alerts.filter(alert => alert.resolved);
    const displayedAlerts = activeTab === "active" ? activeAlerts : resolvedAlerts;

    const analyzeAlert = async (alertId) => {
        if (aiAnalysis[alertId]) {
            setOpenAnalysis(openAnalysis === alertId ? null : alertId);
            return;
        }
        setAnalyzingAlert(alertId);
        try {
            const response = await axios.post(`http://localhost:8080/ai/analyze/${alertId}`);
            setAiAnalysis(prev => ({
                ...prev,
                [alertId]: { success: true, ...response.data }
            }));
            setOpenAnalysis(alertId);
        } catch (error) {
            console.error("AI Analysis failed: ", error);
            setAiAnalysis(prev => ({
                ...prev,
                [alertId]: {
                    success: false,
                    summary: "AI Service Unreachable",
                    likelyCause: "Ollama server is not running or model 'qwen2.5:7b' is missing.",
                    recommendation: "Run 'ollama serve' and ensure 'ollama pull qwen2.5:7b' has completed.",
                    confidence: 0
                }
            }));
            setOpenAnalysis(alertId);
        } finally {
            setAnalyzingAlert(null);
        }
    };

    return (
        <MainLayout>
            <div className="space-y-6">
                <div>
                    <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
                        Alerts & Incidents
                    </h1>
                    <p className="text-slate-500 dark:text-slate-400 mt-2">
                        Monitor active API failures and review resolved performance issues
                    </p>
                </div>

                <div className="rounded-2xl p-6 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
                    {/* Navigation Tabs */}
                    <div className="flex justify-between items-center mb-6 border-b border-slate-200 dark:border-slate-800 pb-4">
                        <div className="flex gap-4">
                            <button
                                onClick={() => { setActiveTab("active"); setShowAll(false); }}
                                className={`px-4 py-2 rounded-lg text-sm font-semibold flex items-center gap-2 transition ${
                                    activeTab === "active"
                                        ? "bg-red-500/10 text-red-500 border border-red-500/20"
                                        : "text-slate-500 hover:text-slate-900 dark:hover:text-white"
                                }`}
                            >
                                <AlertTriangle size={16} />
                                Active Alerts ({activeAlerts.length})
                            </button>
                            <button
                                onClick={() => { setActiveTab("resolved"); setShowAll(false); }}
                                className={`px-4 py-2 rounded-lg text-sm font-semibold flex items-center gap-2 transition ${
                                    activeTab === "resolved"
                                        ? "bg-emerald-500/10 text-emerald-500 border border-emerald-500/20"
                                        : "text-slate-500 hover:text-slate-900 dark:hover:text-white"
                                }`}
                            >
                                <CheckCircle2 size={16} />
                                Resolved Alerts ({resolvedAlerts.length})
                            </button>
                        </div>
                    </div>

                    <div className="space-y-3">
                        {displayedAlerts.length === 0 ? (
                            <div className="text-center py-8 text-slate-500 dark:text-slate-400">
                                No {activeTab} alerts found
                            </div>
                        ) : (
                            (showAll ? displayedAlerts : displayedAlerts.slice(0, 5))
                                .map((alert) => (
                                    <div
                                        key={alert.id}
                                        className={`p-4 rounded-xl border ${
                                            alert.resolved
                                                ? "bg-slate-50 dark:bg-slate-800/40 border-slate-200 dark:border-slate-700/50"
                                                : alert.severity === "CRITICAL"
                                                ? "bg-red-500/5 border-red-500/20"
                                                : "bg-yellow-500/5 border-yellow-500/20"
                                        }`}
                                    >
                                        <div className="flex justify-between items-center mb-2">
                                            <p className="text-xs text-slate-500 dark:text-slate-400">
                                                Alert ID: #{alert.id}
                                            </p>
                                            <span
                                                className={`text-sm font-semibold ${
                                                    alert.resolved
                                                        ? "text-emerald-500"
                                                        : alert.severity === "CRITICAL"
                                                        ? "text-red-500"
                                                        : "text-yellow-500"
                                                }`}
                                            >
                                                {alert.resolved ? "RESOLVED" : alert.severity}
                                            </span>
                                        </div>
                                        <p className="font-medium text-slate-800 dark:text-white">
                                            {alert.endpointUrl}
                                        </p>
                                        <p
                                            className={`text-sm mt-2 ${
                                                alert.resolved
                                                    ? "text-slate-600 dark:text-slate-400"
                                                    : alert.severity === "CRITICAL"
                                                    ? "text-red-500"
                                                    : "text-yellow-500"
                                            }`}
                                        >
                                            {alert.message}
                                        </p>
                                        <div className="flex justify-between items-center mt-3 text-xs text-slate-500 dark:text-slate-400">
                                            <span>Type: {alert.type.replace("_", " ")}</span>
                                            {alert.createdAt && <span>Triggered: {new Date(alert.createdAt).toLocaleString()}</span>}
                                        </div>

                                        <div className="flex justify-end mt-3">
                                            <button
                                                onClick={() => analyzeAlert(alert.id)}
                                                disabled={analyzingAlert === alert.id}
                                                className="inline-flex items-center gap-1.5 text-xs font-medium text-indigo-600 dark:text-indigo-400
                                                hover:text-indigo-700 dark:hover:text-indigo-300 hover:underline
                                                underline-offset-4 transition-colors disabled:opacity-50"
                                            >
                                                {analyzingAlert === alert.id ? (
                                                    "Analyzing with AI..."
                                                ) : aiAnalysis[alert.id] && openAnalysis === alert.id ? (
                                                    "Hide AI Analysis"
                                                ) : (
                                                    <>
                                                        <Sparkles size={14} className="shrink-0 text-indigo-500" />
                                                        <span>Analyze Anomaly with AI</span>
                                                    </>
                                                )}
                                            </button>
                                        </div>

                                        {aiAnalysis[alert.id] && openAnalysis === alert.id && (
                                            <div className="mt-4 pt-4 border-t border-slate-200 dark:border-slate-700 bg-indigo-50/50 dark:bg-indigo-950/20 p-4 rounded-xl">
                                                <p className="inline-flex items-center gap-1.5 text-sm font-semibold text-indigo-900 dark:text-indigo-200 mb-2">
                                                    <Sparkles size={16} className="text-indigo-500" />
                                                    Sentinel AI Root Cause Analysis
                                                </p>

                                                <p className="text-sm text-slate-700 dark:text-slate-300">
                                                    <span className="font-semibold">Summary:</span> {aiAnalysis[alert.id].summary}
                                                </p>

                                                <p className="text-sm mt-2 text-slate-700 dark:text-slate-300">
                                                    <span className="font-semibold">Likely Cause:</span> {aiAnalysis[alert.id].likelyCause}
                                                </p>

                                                <p className="text-sm mt-2 text-slate-700 dark:text-slate-300">
                                                    <span className="font-semibold">Recommended Action:</span> {aiAnalysis[alert.id].recommendation}
                                                </p>

                                                <div className="mt-3 flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
                                                    <span>Confidence Score: {aiAnalysis[alert.id].confidence}%</span>
                                                    {!aiAnalysis[alert.id].success && (
                                                        <span className="text-amber-600 dark:text-amber-400 flex items-center gap-1">
                                                            <Info size={12} /> Local LLM Service Offline
                                                        </span>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ))
                        )}
                    </div>

                    {displayedAlerts.length > 5 && (
                        <div className="flex justify-center mt-5">
                            <button
                                onClick={() => setShowAll(!showAll)}
                                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition"
                            >
                                {showAll ? "Show Less" : `View All (${displayedAlerts.length})`}
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </MainLayout>
    );
}

export default Alerts;