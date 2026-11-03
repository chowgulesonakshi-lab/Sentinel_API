import { useEffect, useState } from "react";
import MainLayout from "../layouts/MainLayout";
import EndpointModal from "../components/EndpointComponents/EndpointModal";
import {
    getEndpoints,
    deleteEndpoint,
    triggerEndpointCheck
} from "../services/endpointsService";

import { FolderKanban, Package, ChevronDown, ChevronRight, Circle, Play, Clock, ShieldAlert, CheckCircle2, XCircle } from "lucide-react";

function Endpoints() {
    const [endpoints, setEndpoints] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [selectedEndpoint, setSelectedEndpoint] = useState(null);
    const [testingEndpointId, setTestingEndpointId] = useState(null);
    const [testResults, setTestResults] = useState({});

    const [expandedProjects, setExpandedProjects] = useState({});
    const [expandedServices, setExpandedServices] = useState({});
    const [showAllEndpoints, setShowAllEndpoints] = useState({});

    useEffect(() => {
        loadEndpoints();
    }, []);

    const loadEndpoints = async () => {
        try {
            const data = await getEndpoints();
            setEndpoints(data);

            // Auto-expand projects and services initially
            if (data && data.length > 0) {
                const projMap = {};
                const servMap = {};
                data.forEach(ep => {
                    const p = ep.projectName || "No Project";
                    const s = `${p}-${ep.apiServiceName || "Unknown Service"}`;
                    projMap[p] = true;
                    servMap[s] = true;
                });
                setExpandedProjects(prev => ({ ...projMap, ...prev }));
                setExpandedServices(prev => ({ ...servMap, ...prev }));
            }
        } catch (error) {
            console.error("Error loading endpoints:", error);
        }
    };

    const handleDeleteEndpoint = async (id) => {
        const confirmDelete = window.confirm(
            "Are you sure you want to delete this endpoint?"
        );
        if (!confirmDelete) return;

        try {
            await deleteEndpoint(id);
            loadEndpoints();
        } catch (error) {
            console.error(error);
        }
    };

    const handleTestNow = async (endpointId) => {
        setTestingEndpointId(endpointId);
        try {
            const result = await triggerEndpointCheck(endpointId);
            setTestResults(prev => ({
                ...prev,
                [endpointId]: {
                    status: result.status,
                    statusCode: result.statusCode,
                    responseTime: result.responseTime,
                    success: result.success,
                    time: new Date().toLocaleTimeString()
                }
            }));
            loadEndpoints();
        } catch (error) {
            setTestResults(prev => ({
                ...prev,
                [endpointId]: {
                    status: "DOWN",
                    statusCode: 500,
                    responseTime: 0,
                    success: false,
                    error: "Check failed",
                    time: new Date().toLocaleTimeString()
                }
            }));
        } finally {
            setTestingEndpointId(null);
        }
    };

    const groupedEndpoints = endpoints.reduce((projects, endpoint) => {
        const projectName = endpoint.projectName || "No Project";
        const serviceName = endpoint.apiServiceName || "Unknown Service";

        if (!projects[projectName]) {
            projects[projectName] = {};
        }

        if (!projects[projectName][serviceName]) {
            projects[projectName][serviceName] = [];
        }

        projects[projectName][serviceName].push(endpoint);
        return projects;
    }, {});

    const methodColor = (method) => {
        switch (method) {
            case "GET":
                return "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20";
            case "POST":
                return "bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20";
            case "PUT":
                return "bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20";
            case "PATCH":
                return "bg-purple-500/10 text-purple-600 dark:text-purple-400 border border-purple-500/20";
            case "DELETE":
                return "bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20";
            default:
                return "bg-slate-500/10 text-slate-600 dark:text-slate-400 border border-slate-500/20";
        }
    };

    return (
        <MainLayout>
            <div className="space-y-6">
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
                            Configurable Endpoints
                        </h1>
                        <p className="mt-2 text-slate-500 dark:text-slate-400">
                            Configure polling intervals, custom timeouts, expected status codes, failure thresholds, and headers per endpoint.
                        </p>
                    </div>

                    <button
                        onClick={() => {
                            setSelectedEndpoint(null);
                            setShowModal(true);
                        }}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl font-medium shadow-sm transition flex items-center gap-2"
                    >
                        <span>+ Add Endpoint</span>
                    </button>
                </div>

                {Object.keys(groupedEndpoints).length === 0 ? (
                    <div className="bg-white dark:bg-slate-900 rounded-2xl p-12 text-center border border-slate-200 dark:border-slate-800 shadow-sm">
                        <Package className="mx-auto h-12 w-12 text-slate-400 mb-3" />
                        <h3 className="text-lg font-semibold text-slate-800 dark:text-white">No endpoints configured</h3>
                        <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">
                            Click "+ Add Endpoint" to register and configure your first API monitor.
                        </p>
                    </div>
                ) : (
                    Object.entries(groupedEndpoints).map(([projectName, services]) => (
                        <div
                            key={projectName}
                            className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden"
                        >
                            <button
                                onClick={() =>
                                    setExpandedProjects({
                                        ...expandedProjects,
                                        [projectName]: !expandedProjects[projectName]
                                    })
                                }
                                className="w-full flex justify-between items-center px-6 py-4 hover:bg-slate-50/50 dark:hover:bg-slate-800/40 transition"
                            >
                                <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2.5">
                                    <FolderKanban size={20} className="text-blue-500" />
                                    {projectName}
                                </h2>

                                <span className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-500 dark:text-slate-300">
                                    <ChevronRight
                                        size={17}
                                        className={`transition-transform duration-200 ${
                                            expandedProjects[projectName] ? "rotate-90" : ""
                                        }`}
                                    />
                                </span>
                            </button>

                            {expandedProjects[projectName] && (
                                <div className="px-6 pb-6 space-y-6 border-t border-slate-100 dark:border-slate-800/60 pt-4">
                                    {Object.entries(services).map(([serviceName, endpointList]) => {
                                        const serviceKey = `${projectName}-${serviceName}`;
                                        const visibleEndpoints = showAllEndpoints[serviceKey]
                                            ? endpointList
                                            : endpointList.slice(0, 5);

                                        return (
                                            <div key={serviceKey} className="space-y-3">
                                                <button
                                                    onClick={() =>
                                                        setExpandedServices({
                                                            ...expandedServices,
                                                            [serviceKey]: !expandedServices[serviceKey]
                                                        })
                                                    }
                                                    className="w-full flex justify-between items-center py-2 text-left"
                                                >
                                                    <h3 className="font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2">
                                                        <Package size={17} className="text-slate-400" />
                                                        {serviceName}
                                                        <span className="text-xs bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded-full text-slate-500">
                                                            {endpointList.length} endpoint{endpointList.length > 1 ? "s" : ""}
                                                        </span>
                                                    </h3>

                                                    <span className="text-slate-400">
                                                        {expandedServices[serviceKey] ? (
                                                            <ChevronDown size={18} />
                                                        ) : (
                                                            <ChevronRight size={18} />
                                                        )}
                                                    </span>
                                                </button>

                                                {expandedServices[serviceKey] && (
                                                    <div className="space-y-4">
                                                        {visibleEndpoints.map((endpoint) => (
                                                            <div
                                                                key={endpoint.id}
                                                                className="bg-slate-50/70 dark:bg-slate-800/40 border border-slate-200/80 dark:border-slate-800 rounded-xl p-5 hover:border-slate-300 dark:hover:border-slate-700 transition"
                                                            >
                                                                <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
                                                                    <div className="space-y-1.5 flex-1 min-w-0">
                                                                        <div className="flex items-center gap-2 flex-wrap">
                                                                            <span className={`px-2.5 py-0.5 rounded-md text-xs font-mono font-bold ${methodColor(endpoint.method)}`}>
                                                                                {endpoint.method}
                                                                            </span>

                                                                            <span className={`px-2.5 py-0.5 rounded-md text-xs font-medium flex items-center gap-1.5 ${
                                                                                endpoint.enabled
                                                                                    ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20"
                                                                                    : "bg-slate-500/10 text-slate-500 border border-slate-500/20"
                                                                            }`}>
                                                                                <Circle size={6} fill="currentColor" strokeWidth={0} />
                                                                                {endpoint.enabled ? "Active" : "Disabled"}
                                                                            </span>

                                                                            {endpoint.consecutiveFailuresCount > 0 && (
                                                                                <span className="px-2.5 py-0.5 rounded-md text-xs font-medium bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20 flex items-center gap-1">
                                                                                    <ShieldAlert size={12} />
                                                                                    {endpoint.consecutiveFailuresCount} failure(s)
                                                                                </span>
                                                                            )}
                                                                        </div>

                                                                        <p className="font-mono text-sm text-slate-900 dark:text-slate-100 break-all pt-1">
                                                                            {endpoint.url}
                                                                        </p>
                                                                    </div>

                                                                    {/* Action Buttons */}
                                                                    <div className="flex items-center gap-2 self-end md:self-auto shrink-0">
                                                                        <button
                                                                            onClick={() => handleTestNow(endpoint.id)}
                                                                            disabled={testingEndpointId === endpoint.id}
                                                                            className="px-3 py-1.5 rounded-lg border border-blue-500/30 text-blue-600 dark:text-blue-400 hover:bg-blue-500/10 text-xs font-medium flex items-center gap-1.5 transition disabled:opacity-50"
                                                                            title="Run health check right now"
                                                                        >
                                                                            <Play size={13} className={testingEndpointId === endpoint.id ? "animate-spin" : ""} />
                                                                            {testingEndpointId === endpoint.id ? "Testing..." : "Test Now"}
                                                                        </button>

                                                                        <button
                                                                            onClick={() => {
                                                                                setSelectedEndpoint(endpoint);
                                                                                setShowModal(true);
                                                                            }}
                                                                            className="px-3 py-1.5 border border-slate-300 dark:border-slate-700 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 text-xs font-medium transition"
                                                                        >
                                                                            Configure
                                                                        </button>

                                                                        <button
                                                                            onClick={() => handleDeleteEndpoint(endpoint.id)}
                                                                            className="px-3 py-1.5 border border-rose-500/30 text-rose-600 dark:text-rose-400 rounded-lg hover:bg-rose-50 dark:hover:bg-rose-900/20 text-xs font-medium transition"
                                                                        >
                                                                            Delete
                                                                        </button>
                                                                    </div>
                                                                </div>

                                                                {/* Test Result Bar if available */}
                                                                {testResults[endpoint.id] && (
                                                                    <div className={`mt-3 p-2.5 rounded-lg text-xs font-medium flex items-center justify-between border ${
                                                                        testResults[endpoint.id].success
                                                                            ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/20"
                                                                            : "bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-500/20"
                                                                    }`}>
                                                                        <div className="flex items-center gap-2">
                                                                            {testResults[endpoint.id].success ? (
                                                                                <CheckCircle2 size={15} />
                                                                            ) : (
                                                                                <XCircle size={15} />
                                                                            )}
                                                                            <span>
                                                                                Result: <strong>{testResults[endpoint.id].status}</strong> (HTTP {testResults[endpoint.id].statusCode}) • {Math.round(testResults[endpoint.id].responseTime)} ms
                                                                            </span>
                                                                        </div>
                                                                        <span className="text-[11px] opacity-75">Checked at {testResults[endpoint.id].time}</span>
                                                                    </div>
                                                                )}

                                                                {/* Configurable Monitoring Properties Grid */}
                                                                <div className="mt-4 pt-3 border-t border-slate-200/60 dark:border-slate-800/60 grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
                                                                    <div className="bg-white dark:bg-slate-900/60 p-2 rounded-lg border border-slate-200/50 dark:border-slate-800">
                                                                        <span className="text-slate-400 block text-[10px] uppercase font-semibold">Polling Interval</span>
                                                                        <span className="font-semibold text-slate-800 dark:text-slate-200">
                                                                            Every {endpoint.intervalSeconds || 60}s
                                                                        </span>
                                                                    </div>

                                                                    <div className="bg-white dark:bg-slate-900/60 p-2 rounded-lg border border-slate-200/50 dark:border-slate-800">
                                                                        <span className="text-slate-400 block text-[10px] uppercase font-semibold">Request Timeout</span>
                                                                        <span className="font-semibold text-slate-800 dark:text-slate-200">
                                                                            {endpoint.timeoutMillis || 5000}ms
                                                                        </span>
                                                                    </div>

                                                                    <div className="bg-white dark:bg-slate-900/60 p-2 rounded-lg border border-slate-200/50 dark:border-slate-800">
                                                                        <span className="text-slate-400 block text-[10px] uppercase font-semibold">Expected Status</span>
                                                                        <span className="font-mono font-medium text-slate-800 dark:text-slate-200 truncate block" title={endpoint.expectedStatusCodes || "200"}>
                                                                            {endpoint.expectedStatusCodes || "200,201..."}
                                                                        </span>
                                                                    </div>

                                                                    <div className="bg-white dark:bg-slate-900/60 p-2 rounded-lg border border-slate-200/50 dark:border-slate-800">
                                                                        <span className="text-slate-400 block text-[10px] uppercase font-semibold">Slow Threshold / Alert Rule</span>
                                                                        <span className="font-semibold text-slate-800 dark:text-slate-200">
                                                                            &gt;{endpoint.slowResponseThresholdMillis || 1000}ms • {endpoint.consecutiveFailuresThreshold || 1} fail(s)
                                                                        </span>
                                                                    </div>
                                                                </div>

                                                                {/* Last Monitored Time */}
                                                                <div className="mt-2.5 flex items-center justify-between text-[11px] text-slate-400">
                                                                    <span className="flex items-center gap-1">
                                                                        <Clock size={12} />
                                                                        Last checked: {endpoint.lastMonitoredAt ? new Date(endpoint.lastMonitoredAt).toLocaleString() : "Pending next cycle"}
                                                                    </span>
                                                                    {endpoint.headers && (
                                                                        <span className="font-mono text-[10px] bg-slate-200/50 dark:bg-slate-800 px-1.5 py-0.5 rounded text-slate-500">
                                                                            Custom Headers Configured
                                                                        </span>
                                                                    )}
                                                                </div>
                                                            </div>
                                                        ))}

                                                        {endpointList.length > 5 && (
                                                            <button
                                                                onClick={() =>
                                                                    setShowAllEndpoints({
                                                                        ...showAllEndpoints,
                                                                        [serviceKey]: !showAllEndpoints[serviceKey]
                                                                    })
                                                                }
                                                                className="text-blue-600 dark:text-blue-400 hover:underline font-medium text-xs pt-1"
                                                            >
                                                                {showAllEndpoints[serviceKey]
                                                                    ? "Show Fewer Endpoints"
                                                                    : `View All ${endpointList.length} Endpoints`}
                                                            </button>
                                                        )}
                                                    </div>
                                                )}
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    ))
                )}

                <EndpointModal
                    isOpen={showModal}
                    onClose={() => {
                        setShowModal(false);
                        setSelectedEndpoint(null);
                    }}
                    refreshEndpoints={loadEndpoints}
                    selectedEndpoint={selectedEndpoint}
                />
            </div>
        </MainLayout>
    );
}

export default Endpoints;