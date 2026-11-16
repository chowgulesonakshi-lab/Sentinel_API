import { useEffect, useState } from "react";
import MainLayout from "../layouts/MainLayout";
import { getAllAlerts, resolveAlert } from "../services/alertsService";
import { getAlertRules, deleteAlertRule, toggleAlertRule } from "../services/alertRuleService";
import AlertRuleModal from "../components/AlertComponents/AlertRuleModal";
import axios from "axios";
import { Sparkles, CheckCircle2, AlertTriangle, Info, Sliders, Plus, Edit2, Trash2, Check, ShieldAlert } from "lucide-react";

function Alerts() {
    const [alerts, setAlerts] = useState([]);
    const [rules, setRules] = useState([]);
    const [activeTab, setActiveTab] = useState("active"); // active, resolved, rules
    const [showAll, setShowAll] = useState(false);
    const [aiAnalysis, setAiAnalysis] = useState({});
    const [analyzingAlert, setAnalyzingAlert] = useState(null);
    const [openAnalysis, setOpenAnalysis] = useState(null);

    // Rule modal states
    const [showRuleModal, setShowRuleModal] = useState(false);
    const [selectedRule, setSelectedRule] = useState(null);

    useEffect(() => {
        loadAlerts();
        loadRules();
    }, []);

    const loadAlerts = async () => {
        try {
            const data = await getAllAlerts();
            setAlerts(data);
        } catch (err) {
            console.error("ALERT ERROR:", err);
        }
    };

    const loadRules = async () => {
        try {
            const data = await getAlertRules();
            setRules(data);
        } catch (err) {
            console.error("ALERT RULES ERROR:", err);
        }
    };

    const handleResolve = async (alertId) => {
        try {
            await resolveAlert(alertId);
            loadAlerts();
        } catch (err) {
            console.error("Failed to resolve alert:", err);
        }
    };

    const handleDeleteRule = async (ruleId) => {
        if (!window.confirm("Are you sure you want to delete this alert rule?")) return;
        try {
            await deleteAlertRule(ruleId);
            loadRules();
        } catch (err) {
            console.error("Failed to delete rule:", err);
        }
    };

    const handleToggleRule = async (ruleId) => {
        try {
            await toggleAlertRule(ruleId);
            loadRules();
        } catch (err) {
            console.error("Failed to toggle rule:", err);
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

    const formatParams = (conditionType, paramsStr) => {
        if (!paramsStr) return "Default Threshold";
        try {
            const p = JSON.parse(paramsStr);
            switch (conditionType) {
                case "CONSECUTIVE_FAILURES":
                    return `${p.threshold || 3} failures`;
                case "STATUS_CODE":
                    return `Status ${p.operator || "=="} ${p.statusCode || 500}`;
                case "RESPONSE_TIME_THRESHOLD":
                    return `Latency >= ${p.thresholdMillis || 1000}ms`;
                case "RESPONSE_TIME_INCREASE":
                    return `+${p.percent || 50}% above baseline`;
                case "UNAVAILABILITY_DURATION":
                    return `Down >= ${p.durationMinutes || 5} min`;
                default:
                    return paramsStr;
            }
        } catch (e) {
            return paramsStr;
        }
    };

    return (
        <MainLayout>
            <div className="space-y-6">
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
                            Incident Alerts & Rule Engine
                        </h1>
                        <p className="text-slate-500 dark:text-slate-400 mt-2">
                            Manage live incident telemetry, configurable rule evaluations, and root-cause intelligence.
                        </p>
                    </div>

                    {activeTab === "rules" && (
                        <button
                            onClick={() => {
                                setSelectedRule(null);
                                setShowRuleModal(true);
                            }}
                            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl text-sm font-semibold flex items-center gap-2 shadow-sm transition"
                        >
                            <Plus size={16} />
                            Create Alert Rule
                        </button>
                    )}
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
                                Active Incidents ({activeAlerts.length})
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
                                Resolved Incidents ({resolvedAlerts.length})
                            </button>
                            <button
                                onClick={() => { setActiveTab("rules"); }}
                                className={`px-4 py-2 rounded-lg text-sm font-semibold flex items-center gap-2 transition ${
                                    activeTab === "rules"
                                        ? "bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20"
                                        : "text-slate-500 hover:text-slate-900 dark:hover:text-white"
                                }`}
                            >
                                <Sliders size={16} />
                                Configured Rules ({rules.length})
                            </button>
                        </div>
                    </div>

                    {/* RULES TAB */}
                    {activeTab === "rules" && (
                        <div className="space-y-4">
                            {rules.length === 0 ? (
                                <div className="text-center py-12 text-slate-500 dark:text-slate-400">
                                    <Sliders className="mx-auto h-10 w-10 text-slate-400 mb-2" />
                                    <p className="font-semibold text-slate-700 dark:text-slate-300">No dynamic alert rules configured</p>
                                    <p className="text-xs text-slate-400 mt-1">Click "Create Alert Rule" to define status code, consecutive failure, or latency rules.</p>
                                </div>
                            ) : (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    {rules.map((rule) => (
                                        <div
                                            key={rule.id}
                                            className={`p-5 rounded-xl border transition ${
                                                rule.enabled
                                                    ? "bg-slate-50/70 dark:bg-slate-800/40 border-slate-200 dark:border-slate-800"
                                                    : "bg-slate-100/50 dark:bg-slate-900/50 border-slate-200 dark:border-slate-800/50 opacity-60"
                                            }`}
                                        >
                                            <div className="flex justify-between items-start gap-2">
                                                <div>
                                                    <div className="flex items-center gap-2">
                                                        <h3 className="font-bold text-slate-900 dark:text-white">
                                                            {rule.name}
                                                        </h3>
                                                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                                                            rule.severity === "CRITICAL"
                                                                ? "bg-red-500/10 text-red-600 border border-red-500/20"
                                                                : rule.severity === "WARNING"
                                                                ? "bg-amber-500/10 text-amber-600 border border-amber-500/20"
                                                                : "bg-blue-500/10 text-blue-600 border border-blue-500/20"
                                                        }`}>
                                                            {rule.severity}
                                                        </span>
                                                    </div>

                                                    <p className="text-xs font-mono text-blue-600 dark:text-blue-400 mt-1">
                                                        {rule.conditionType.replace(/_/g, " ")} • {formatParams(rule.conditionType, rule.conditionParameters)}
                                                    </p>
                                                </div>

                                                {/* Enable Switch */}
                                                <button
                                                    onClick={() => handleToggleRule(rule.id)}
                                                    className={`px-2.5 py-1 rounded-full text-xs font-semibold flex items-center gap-1 transition ${
                                                        rule.enabled
                                                            ? "bg-emerald-500/10 text-emerald-600 border border-emerald-500/20"
                                                            : "bg-slate-200 text-slate-600 dark:bg-slate-800 dark:text-slate-400"
                                                    }`}
                                                >
                                                    {rule.enabled ? "Active" : "Disabled"}
                                                </button>
                                            </div>

                                            <div className="mt-3 text-xs text-slate-500 dark:text-slate-400 flex items-center justify-between border-t border-slate-200/60 dark:border-slate-800 pt-2.5">
                                                <span>
                                                    Scope: {rule.endpointUrl ? `Endpoint (${rule.endpointMethod} ${rule.endpointUrl})` : rule.apiServiceName ? `Service (${rule.apiServiceName})` : "Global (All Endpoints)"}
                                                </span>

                                                <div className="flex items-center gap-2">
                                                    <button
                                                        onClick={() => {
                                                            setSelectedRule(rule);
                                                            setShowRuleModal(true);
                                                        }}
                                                        className="text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 p-1"
                                                        title="Edit Rule"
                                                    >
                                                        <Edit2 size={14} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteRule(rule.id)}
                                                        className="text-rose-500 hover:text-rose-700 p-1"
                                                        title="Delete Rule"
                                                    >
                                                        <Trash2 size={14} />
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}

                    {/* ALERTS TAB (ACTIVE OR RESOLVED) */}
                    {activeTab !== "rules" && (
                        <div className="space-y-3">
                            {displayedAlerts.length === 0 ? (
                                <div className="text-center py-8 text-slate-500 dark:text-slate-400">
                                    No {activeTab} incidents found
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
                                                <div className="flex items-center gap-2">
                                                    <p className="text-xs text-slate-500 dark:text-slate-400">
                                                        Incident #{alert.id}
                                                    </p>
                                                    {alert.ruleName && (
                                                        <span className="bg-purple-500/10 text-purple-600 dark:text-purple-400 border border-purple-500/20 text-[10px] font-semibold px-2 py-0.5 rounded">
                                                            Rule: {alert.ruleName}
                                                        </span>
                                                    )}
                                                </div>

                                                <span
                                                    className={`text-xs font-bold px-2 py-0.5 rounded ${
                                                        alert.resolved
                                                            ? "bg-emerald-500/10 text-emerald-600"
                                                            : alert.severity === "CRITICAL"
                                                            ? "bg-red-500/10 text-red-600"
                                                            : "bg-yellow-500/10 text-yellow-600"
                                                    }`}
                                                >
                                                    {alert.resolved ? "RESOLVED" : alert.severity}
                                                </span>
                                            </div>

                                            <p className="font-mono text-sm font-medium text-slate-800 dark:text-white break-all">
                                                {alert.endpointUrl}
                                            </p>

                                            <p
                                                className={`text-sm mt-2 font-medium ${
                                                    alert.resolved
                                                        ? "text-slate-600 dark:text-slate-400"
                                                        : alert.severity === "CRITICAL"
                                                        ? "text-red-500"
                                                        : "text-yellow-600 dark:text-yellow-400"
                                                }`}
                                            >
                                                {alert.message}
                                            </p>

                                            <div className="flex justify-between items-center mt-3 text-xs text-slate-500 dark:text-slate-400">
                                                <span>Type: {alert.type.replace(/_/g, " ")}</span>
                                                {alert.createdAt && <span>Triggered: {new Date(alert.createdAt).toLocaleString()}</span>}
                                            </div>

                                            <div className="flex justify-between items-center mt-3 pt-3 border-t border-slate-200/50 dark:border-slate-800">
                                                {!alert.resolved ? (
                                                    <button
                                                        onClick={() => handleResolve(alert.id)}
                                                        className="px-3 py-1 bg-emerald-600/10 text-emerald-600 hover:bg-emerald-600/20 border border-emerald-500/20 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition"
                                                    >
                                                        <Check size={14} />
                                                        Mark as Resolved
                                                    </button>
                                                ) : (
                                                    <span className="text-xs text-slate-400">
                                                        Resolved at: {alert.resolvedAt ? new Date(alert.resolvedAt).toLocaleString() : "Auto"}
                                                    </span>
                                                )}

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
                                                <div className="mt-4 pt-4 border-t border-slate-200 dark:border-slate-800 bg-slate-900/5 dark:bg-slate-950/40 p-5 rounded-2xl border border-indigo-500/20 space-y-4">
                                                    {/* Header with Model & Confidence */}
                                                    <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-3 border-b border-slate-200/60 dark:border-slate-800 gap-2">
                                                        <div className="flex items-center gap-2">
                                                            <div className="w-8 h-8 rounded-lg bg-indigo-500/10 text-indigo-500 flex items-center justify-center">
                                                                <Sparkles size={18} />
                                                            </div>
                                                            <div>
                                                                <h4 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                                                                    Sentinel AI Incident Intelligence
                                                                    <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border border-indigo-500/20">
                                                                        {aiAnalysis[alert.id].aiModel || "Qwen 2.5 7B"}
                                                                    </span>
                                                                </h4>
                                                                <p className="text-[11px] text-slate-500 dark:text-slate-400">
                                                                    Empirically correlated telemetry and root-cause hypothesis analysis
                                                                </p>
                                                            </div>
                                                        </div>

                                                        {/* Confidence Score Bar */}
                                                        <div className="flex items-center gap-2.5 bg-white dark:bg-slate-900 px-3 py-1.5 rounded-xl border border-slate-200/80 dark:border-slate-800 text-xs">
                                                            <span className="text-slate-500 font-medium">Confidence:</span>
                                                            <div className="w-16 h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                                                                <div
                                                                    className={`h-full rounded-full ${
                                                                        aiAnalysis[alert.id].confidence >= 80
                                                                            ? "bg-emerald-500"
                                                                            : aiAnalysis[alert.id].confidence >= 60
                                                                            ? "bg-amber-500"
                                                                            : "bg-blue-500"
                                                                    }`}
                                                                    style={{ width: `${aiAnalysis[alert.id].confidence || 50}%` }}
                                                                />
                                                            </div>
                                                            <span className="font-bold text-slate-800 dark:text-white">
                                                                {aiAnalysis[alert.id].confidence}%
                                                            </span>
                                                        </div>
                                                    </div>

                                                    {/* Incident Summary */}
                                                    <div className="bg-white dark:bg-slate-900/90 p-3.5 rounded-xl border border-slate-200/70 dark:border-slate-800">
                                                        <span className="text-[10px] font-bold tracking-wider uppercase text-slate-400 block mb-1">
                                                            Incident Summary
                                                        </span>
                                                        <p className="text-sm text-slate-800 dark:text-slate-200 font-medium">
                                                            {aiAnalysis[alert.id].summary}
                                                        </p>
                                                    </div>

                                                    {/* Two-Column Grid: Empirical Evidence vs Hypotheses */}
                                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                        {/* Empirical Supporting Evidence */}
                                                        <div className="bg-white dark:bg-slate-900/90 p-4 rounded-xl border border-slate-200/70 dark:border-slate-800 space-y-2">
                                                            <div className="flex items-center gap-1.5 text-xs font-bold text-slate-900 dark:text-white">
                                                                <CheckCircle2 size={15} className="text-emerald-500" />
                                                                Empirical Supporting Evidence
                                                            </div>
                                                            <ul className="text-xs text-slate-600 dark:text-slate-300 space-y-1.5 pt-1">
                                                                {aiAnalysis[alert.id].supportingEvidence && aiAnalysis[alert.id].supportingEvidence.length > 0 ? (
                                                                    aiAnalysis[alert.id].supportingEvidence.map((ev, i) => (
                                                                        <li key={i} className="flex items-start gap-1.5 font-mono text-[11px] leading-relaxed">
                                                                            <span className="text-emerald-500 font-bold shrink-0">•</span>
                                                                            <span>{ev}</span>
                                                                        </li>
                                                                    ))
                                                                ) : (
                                                                    <li className="text-slate-400">Monitoring telemetry metrics recorded in time window.</li>
                                                                )}
                                                            </ul>
                                                        </div>

                                                        {/* Probable Root Causes */}
                                                        <div className="bg-white dark:bg-slate-900/90 p-4 rounded-xl border border-slate-200/70 dark:border-slate-800 space-y-2">
                                                            <div className="flex items-center gap-1.5 text-xs font-bold text-slate-900 dark:text-white">
                                                                <AlertTriangle size={15} className="text-amber-500" />
                                                                Hypotheses & Plausible Causes
                                                            </div>
                                                            <div className="text-xs text-slate-700 dark:text-slate-200 pt-1 space-y-2">
                                                                <div>
                                                                    <span className="text-[10px] uppercase font-bold text-amber-500 block">Primary Hypothesis</span>
                                                                    <p className="font-semibold text-slate-900 dark:text-slate-100 mt-0.5">
                                                                        {aiAnalysis[alert.id].likelyCause}
                                                                    </p>
                                                                </div>

                                                                {aiAnalysis[alert.id].possibleCauses && aiAnalysis[alert.id].possibleCauses.length > 0 && (
                                                                    <div>
                                                                        <span className="text-[10px] uppercase font-bold text-slate-400 block mb-1">Alternative Factors</span>
                                                                        <ul className="space-y-1 text-slate-600 dark:text-slate-400 text-[11px]">
                                                                            {aiAnalysis[alert.id].possibleCauses.map((c, i) => (
                                                                                <li key={i} className="flex items-start gap-1.5">
                                                                                    <span className="text-amber-400">▹</span>
                                                                                    <span>{c}</span>
                                                                                </li>
                                                                            ))}
                                                                        </ul>
                                                                    </div>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </div>

                                                    {/* Investigation Steps & Remediation */}
                                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                        {/* Investigation Steps */}
                                                        <div className="bg-white dark:bg-slate-900/90 p-4 rounded-xl border border-slate-200/70 dark:border-slate-800 space-y-2">
                                                            <span className="text-xs font-bold text-slate-900 dark:text-white flex items-center gap-1.5">
                                                                Recommended Investigation Steps
                                                            </span>
                                                            <div className="space-y-1.5 text-xs text-slate-600 dark:text-slate-300 pt-1">
                                                                {aiAnalysis[alert.id].investigationSteps && aiAnalysis[alert.id].investigationSteps.length > 0 ? (
                                                                    aiAnalysis[alert.id].investigationSteps.map((step, idx) => (
                                                                        <div key={idx} className="flex items-start gap-2 bg-slate-50 dark:bg-slate-800/60 p-2 rounded-lg text-[11px]">
                                                                            <span className="font-bold text-indigo-500 shrink-0">{idx + 1}.</span>
                                                                            <span>{step.replace(/^\d+\.\s*/, "")}</span>
                                                                        </div>
                                                                    ))
                                                                ) : (
                                                                    <p className="text-xs text-slate-500">{aiAnalysis[alert.id].recommendation}</p>
                                                                )}
                                                            </div>
                                                        </div>

                                                        {/* Remediation Actions */}
                                                        <div className="bg-white dark:bg-slate-900/90 p-4 rounded-xl border border-slate-200/70 dark:border-slate-800 space-y-2">
                                                            <span className="text-xs font-bold text-slate-900 dark:text-white flex items-center gap-1.5">
                                                                Suggested Remediation & Fixes
                                                            </span>
                                                            <div className="space-y-1.5 text-xs text-slate-600 dark:text-slate-300 pt-1">
                                                                {aiAnalysis[alert.id].remediationActions && aiAnalysis[alert.id].remediationActions.length > 0 ? (
                                                                    aiAnalysis[alert.id].remediationActions.map((action, idx) => (
                                                                        <div key={idx} className="flex items-start gap-2 bg-emerald-500/5 dark:bg-emerald-500/10 border border-emerald-500/10 p-2 rounded-lg text-[11px] text-emerald-800 dark:text-emerald-300">
                                                                            <span className="font-bold text-emerald-500 shrink-0">✓</span>
                                                                            <span>{action.replace(/^\d+\.\s*/, "")}</span>
                                                                        </div>
                                                                    ))
                                                                ) : (
                                                                    <p className="text-xs text-slate-500">{aiAnalysis[alert.id].recommendation}</p>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </div>

                                                    {/* Confidence Explanation & Blast Radius Footer */}
                                                    <div className="flex flex-col sm:flex-row sm:items-center justify-between text-xs text-slate-500 dark:text-slate-400 bg-white dark:bg-slate-900/60 p-3 rounded-xl border border-slate-200/60 dark:border-slate-800 gap-2">
                                                        <div className="flex items-center gap-1.5">
                                                            <Info size={14} className="text-indigo-400 shrink-0" />
                                                            <span>{aiAnalysis[alert.id].confidenceExplanation || "Telemetry-grounded analysis."}</span>
                                                        </div>

                                                        {aiAnalysis[alert.id].affectedServices && aiAnalysis[alert.id].affectedServices.length > 0 && (
                                                            <div className="flex items-center gap-1.5 shrink-0">
                                                                <span className="text-[11px] font-semibold text-slate-400">Affected Scope:</span>
                                                                <span className="px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-800 font-mono text-[10px] text-slate-700 dark:text-slate-300">
                                                                    {aiAnalysis[alert.id].affectedServices.join(", ")}
                                                                </span>
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    ))
                            )}
                        </div>
                    )}

                    {activeTab !== "rules" && displayedAlerts.length > 5 && (
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

                <AlertRuleModal
                    isOpen={showRuleModal}
                    onClose={() => {
                        setShowRuleModal(false);
                        setSelectedRule(null);
                    }}
                    refreshRules={loadRules}
                    selectedRule={selectedRule}
                />
            </div>
        </MainLayout>
    );
}

export default Alerts;