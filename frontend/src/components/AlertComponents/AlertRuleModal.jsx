import { useState, useEffect } from "react";
import { getEndpoints } from "../../services/endpointsService";
import { getServices } from "../../services/apiServiceService";
import { createAlertRule, updateAlertRule } from "../../services/alertRuleService";
import { Sliders, ShieldAlert, Target, Clock } from "lucide-react";

function AlertRuleModal({ isOpen, onClose, refreshRules, selectedRule }) {
    const [endpoints, setEndpoints] = useState([]);
    const [services, setServices] = useState([]);

    const [rule, setRule] = useState({
        name: "",
        conditionType: "CONSECUTIVE_FAILURES",
        severity: "CRITICAL",
        targetScope: "GLOBAL", // GLOBAL, SERVICE, ENDPOINT
        endpointId: "",
        apiServiceId: "",
        evaluationWindowMinutes: 15,
        enabled: true,

        // Specific condition params
        consecutiveThreshold: 3,
        statusCode: 500,
        statusOperator: ">=",
        responseTimeThreshold: 1000,
        percentageIncrease: 50,
        unavailabilityMinutes: 5
    });

    useEffect(() => {
        if (!isOpen) return;

        loadOptions();

        if (selectedRule) {
            let scope = "GLOBAL";
            if (selectedRule.endpointId) scope = "ENDPOINT";
            else if (selectedRule.apiServiceId) scope = "SERVICE";

            let consecutiveVal = 3;
            let statusVal = 500;
            let statusOp = ">=";
            let latencyVal = 1000;
            let percentVal = 50;
            let durationVal = 5;

            try {
                if (selectedRule.conditionParameters) {
                    const parsed = JSON.parse(selectedRule.conditionParameters);
                    if (parsed.threshold) consecutiveVal = parsed.threshold;
                    if (parsed.statusCode) statusVal = parsed.statusCode;
                    if (parsed.operator) statusOp = parsed.operator;
                    if (parsed.thresholdMillis) latencyVal = parsed.thresholdMillis;
                    if (parsed.percent) percentVal = parsed.percent;
                    if (parsed.durationMinutes) durationVal = parsed.durationMinutes;
                }
            } catch (e) {
                // Not JSON or plain number
                const num = Number(selectedRule.conditionParameters);
                if (!isNaN(num)) {
                    consecutiveVal = num;
                    latencyVal = num;
                    percentVal = num;
                    durationVal = num;
                }
            }

            setRule({
                name: selectedRule.name,
                conditionType: selectedRule.conditionType,
                severity: selectedRule.severity,
                targetScope: scope,
                endpointId: selectedRule.endpointId || "",
                apiServiceId: selectedRule.apiServiceId || "",
                evaluationWindowMinutes: selectedRule.evaluationWindowMinutes || 15,
                enabled: selectedRule.enabled ?? true,
                consecutiveThreshold: consecutiveVal,
                statusCode: statusVal,
                statusOperator: statusOp,
                responseTimeThreshold: latencyVal,
                percentageIncrease: percentVal,
                unavailabilityMinutes: durationVal
            });
        } else {
            setRule({
                name: "",
                conditionType: "CONSECUTIVE_FAILURES",
                severity: "CRITICAL",
                targetScope: "GLOBAL",
                endpointId: "",
                apiServiceId: "",
                evaluationWindowMinutes: 15,
                enabled: true,
                consecutiveThreshold: 3,
                statusCode: 500,
                statusOperator: ">=",
                responseTimeThreshold: 1000,
                percentageIncrease: 50,
                unavailabilityMinutes: 5
            });
        }
    }, [isOpen, selectedRule]);

    const loadOptions = async () => {
        try {
            const [epData, servData] = await Promise.all([
                getEndpoints(),
                getServices()
            ]);
            setEndpoints(epData || []);
            setServices(servData || []);
        } catch (error) {
            console.error("Failed to load endpoints/services for rule modal:", error);
        }
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setRule(prev => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Build conditionParameters JSON
        let paramsJson = "";
        switch (rule.conditionType) {
            case "CONSECUTIVE_FAILURES":
                paramsJson = JSON.stringify({ threshold: Number(rule.consecutiveThreshold) });
                break;
            case "STATUS_CODE":
                paramsJson = JSON.stringify({
                    statusCode: Number(rule.statusCode),
                    operator: rule.statusOperator
                });
                break;
            case "RESPONSE_TIME_THRESHOLD":
                paramsJson = JSON.stringify({ thresholdMillis: Number(rule.responseTimeThreshold) });
                break;
            case "RESPONSE_TIME_INCREASE":
                paramsJson = JSON.stringify({ percent: Number(rule.percentageIncrease) });
                break;
            case "UNAVAILABILITY_DURATION":
                paramsJson = JSON.stringify({ durationMinutes: Number(rule.unavailabilityMinutes) });
                break;
            default:
                paramsJson = "";
        }

        const payload = {
            name: rule.name,
            conditionType: rule.conditionType,
            conditionParameters: paramsJson,
            severity: rule.severity,
            enabled: rule.enabled,
            evaluationWindowMinutes: Number(rule.evaluationWindowMinutes),
            endpointId: rule.targetScope === "ENDPOINT" && rule.endpointId ? Number(rule.endpointId) : null,
            apiServiceId: rule.targetScope === "SERVICE" && rule.apiServiceId ? Number(rule.apiServiceId) : null
        };

        try {
            if (selectedRule) {
                await updateAlertRule(selectedRule.id, payload);
            } else {
                await createAlertRule(payload);
            }
            refreshRules();
            onClose();
        } catch (error) {
            console.error("Failed to save alert rule:", error);
            alert("Error saving rule. Please check input parameters.");
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-xs flex items-center justify-center z-50 p-4">
            <div className="bg-white dark:bg-slate-900 rounded-2xl w-full max-w-xl border border-slate-200 dark:border-slate-800 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
                {/* Header */}
                <div className="p-6 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center">
                    <div>
                        <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
                            {selectedRule ? "Edit Alert Rule" : "Create Dynamic Alert Rule"}
                        </h2>
                        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                            Define dynamic trigger conditions evaluated against monitoring telemetry.
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 font-bold p-1 text-lg"
                    >
                        ✕
                    </button>
                </div>

                {/* Form Body */}
                <form onSubmit={handleSubmit} className="p-6 overflow-y-auto space-y-4 flex-1 text-sm">
                    {/* Rule Name */}
                    <div>
                        <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                            Rule Name *
                        </label>
                        <input
                            type="text"
                            name="name"
                            placeholder="e.g., Critical Payment Failures"
                            value={rule.name}
                            onChange={handleChange}
                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none"
                            required
                        />
                    </div>

                    {/* Condition Type & Severity Grid */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                Condition Type *
                            </label>
                            <select
                                name="conditionType"
                                value={rule.conditionType}
                                onChange={handleChange}
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none"
                            >
                                <option value="CONSECUTIVE_FAILURES">Consecutive Failures</option>
                                <option value="STATUS_CODE">HTTP Status Code</option>
                                <option value="RESPONSE_TIME_THRESHOLD">Response Time Threshold</option>
                                <option value="RESPONSE_TIME_INCREASE">Latency Spike vs Baseline</option>
                                <option value="UNAVAILABILITY_DURATION">Unavailability Duration</option>
                            </select>
                        </div>

                        <div>
                            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                                Severity *
                            </label>
                            <select
                                name="severity"
                                value={rule.severity}
                                onChange={handleChange}
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none font-semibold"
                            >
                                <option value="CRITICAL">CRITICAL</option>
                                <option value="WARNING">WARNING</option>
                                <option value="INFO">INFO</option>
                            </select>
                        </div>
                    </div>

                    {/* Dynamic Condition Parameters Box */}
                    <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-700/80 space-y-3">
                        <span className="text-xs font-semibold text-slate-700 dark:text-slate-300 block">
                            Condition Parameters
                        </span>

                        {rule.conditionType === "CONSECUTIVE_FAILURES" && (
                            <div>
                                <label className="block text-xs text-slate-500 mb-1">
                                    Number of Consecutive Failures:
                                </label>
                                <input
                                    type="number"
                                    name="consecutiveThreshold"
                                    min="1"
                                    max="50"
                                    value={rule.consecutiveThreshold}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
                                    required
                                />
                                <span className="text-[11px] text-slate-400">Trigger alert when endpoint fails N checks consecutively.</span>
                            </div>
                        )}

                        {rule.conditionType === "STATUS_CODE" && (
                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-xs text-slate-500 mb-1">Operator:</label>
                                    <select
                                        name="statusOperator"
                                        value={rule.statusOperator}
                                        onChange={handleChange}
                                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
                                    >
                                        <option value=">=">&gt;= (Greater or Equal)</option>
                                        <option value="==">== (Exactly Equals)</option>
                                        <option value="!=">!= (Not Equal)</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-xs text-slate-500 mb-1">HTTP Status Code:</label>
                                    <input
                                        type="number"
                                        name="statusCode"
                                        value={rule.statusCode}
                                        onChange={handleChange}
                                        placeholder="500"
                                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono"
                                        required
                                    />
                                </div>
                            </div>
                        )}

                        {rule.conditionType === "RESPONSE_TIME_THRESHOLD" && (
                            <div>
                                <label className="block text-xs text-slate-500 mb-1">
                                    Latency Threshold (ms):
                                </label>
                                <input
                                    type="number"
                                    name="responseTimeThreshold"
                                    min="10"
                                    value={rule.responseTimeThreshold}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
                                    required
                                />
                                <span className="text-[11px] text-slate-400">Trigger alert if response time exceeds this value.</span>
                            </div>
                        )}

                        {rule.conditionType === "RESPONSE_TIME_INCREASE" && (
                            <div>
                                <label className="block text-xs text-slate-500 mb-1">
                                    Percentage Above Historical Baseline (%):
                                </label>
                                <input
                                    type="number"
                                    name="percentageIncrease"
                                    min="10"
                                    value={rule.percentageIncrease}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
                                    required
                                />
                                <span className="text-[11px] text-slate-400">E.g. 50% triggers when latency is 50% higher than recent average.</span>
                            </div>
                        )}

                        {rule.conditionType === "UNAVAILABILITY_DURATION" && (
                            <div>
                                <label className="block text-xs text-slate-500 mb-1">
                                    Continuous Downtime (Minutes):
                                </label>
                                <input
                                    type="number"
                                    name="unavailabilityMinutes"
                                    min="1"
                                    value={rule.unavailabilityMinutes}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
                                    required
                                />
                                <span className="text-[11px] text-slate-400">Trigger alert when endpoint has been down continuously for N minutes.</span>
                            </div>
                        )}
                    </div>

                    {/* Scope Selector */}
                    <div>
                        <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
                            Application Scope *
                        </label>
                        <select
                            name="targetScope"
                            value={rule.targetScope}
                            onChange={handleChange}
                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none"
                        >
                            <option value="GLOBAL">Global (Applies to All Endpoints)</option>
                            <option value="SERVICE">Service-Wide (All Endpoints in a Service)</option>
                            <option value="ENDPOINT">Specific Endpoint</option>
                        </select>
                    </div>

                    {rule.targetScope === "SERVICE" && (
                        <div>
                            <label className="block text-xs text-slate-500 mb-1">Select Service:</label>
                            <select
                                name="apiServiceId"
                                value={rule.apiServiceId}
                                onChange={handleChange}
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
                                required
                            >
                                <option value="">Choose a service...</option>
                                {services.map(s => (
                                    <option key={s.id} value={s.id}>
                                        {s.projectName} • {s.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    {rule.targetScope === "ENDPOINT" && (
                        <div>
                            <label className="block text-xs text-slate-500 mb-1">Select Endpoint:</label>
                            <select
                                name="endpointId"
                                value={rule.endpointId}
                                onChange={handleChange}
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono text-xs"
                                required
                            >
                                <option value="">Choose an endpoint...</option>
                                {endpoints.map(ep => (
                                    <option key={ep.id} value={ep.id}>
                                        [{ep.method}] {ep.url} ({ep.apiServiceName})
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    {/* Enable Toggle */}
                    <div className="pt-2">
                        <label className="flex items-center gap-3 text-slate-700 dark:text-slate-300 cursor-pointer select-none">
                            <input
                                type="checkbox"
                                name="enabled"
                                checked={rule.enabled}
                                onChange={handleChange}
                                className="w-4 h-4 rounded text-blue-600 focus:ring-blue-500 border-slate-300"
                            />
                            <span className="font-medium">Rule Active & Enabled</span>
                        </label>
                    </div>

                    {/* Footer */}
                    <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-800">
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
                            {selectedRule ? "Save Changes" : "Create Rule"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default AlertRuleModal;
