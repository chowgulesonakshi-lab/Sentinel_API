import { useEffect, useState } from "react";
import MainLayout from "../layouts/MainLayout";
import EndpointModal from "../components/EndpointComponents/EndpointModal";
import {
    getEndpoints,
    deleteEndpoint
} from "../services/endpointsService";

function Endpoints() {

    const [endpoints, setEndpoints] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [selectedEndpoint, setSelectedEndpoint] = useState(null);

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
                return "bg-green-100 text-green-700";

            case "POST":
                return "bg-blue-100 text-blue-700";

            case "PUT":
                return "bg-orange-100 text-orange-700";

            case "PATCH":
                return "bg-purple-100 text-purple-700";

            case "DELETE":
                return "bg-red-100 text-red-700";

            default:
                return "bg-slate-100 text-slate-700";
        }
    };
    return (
        <MainLayout>
            <div className="space-y-6">

                <div className="flex justify-between items-center">

                    <div>
                        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
                            Endpoints
                        </h1>

                        <p className="mt-2 text-slate-500 dark:text-slate-400">
                            Monitor all registered API endpoints.
                        </p>
                    </div>

                    <button
                        onClick={() => {
                            setSelectedEndpoint(null);
                            setShowModal(true);
                        }}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg"
                    >
                        + Add Endpoint
                    </button>

                </div>

                {Object.keys(groupedEndpoints).length === 0 ? (

                    <div className="bg-white dark:bg-slate-800 rounded-xl p-6 shadow border border-slate-200 dark:border-slate-700">

                        <p className="text-slate-500 dark:text-slate-400">
                            No endpoints available.
                        </p>

                    </div>

                ) : (

                    Object.entries(groupedEndpoints).map(([projectName, services]) => (

                        <div
                            key={projectName}
                            className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow"
                        >

                            <button
                                onClick={() =>
                                    setExpandedProjects({
                                        ...expandedProjects,
                                        [projectName]:
                                            !expandedProjects[projectName]
                                    })
                                }
                                className="w-full flex justify-between items-center px-6 py-4"
                            >

                                <h2 className="text-xl font-bold text-blue-600">
                                    🌐 {projectName}
                                </h2>

                                <span className="text-xl">
                                    {expandedProjects[projectName] ? "▼" : "▶"}
                                </span>

                            </button>

                            {expandedProjects[projectName] && (

                                <div className="px-6 pb-6 space-y-5">

                                    {Object.entries(services).map(
                                        ([serviceName, endpointList]) => {

                                            const serviceKey =
                                                `${projectName}-${serviceName}`;

                                            const visibleEndpoints =
                                                showAllEndpoints[serviceKey]
                                                    ? endpointList
                                                    : endpointList.slice(0, 5);

                                            return (

                                                <div key={serviceKey}>

                                                    <button
                                                        onClick={() =>
                                                            setExpandedServices({
                                                                ...expandedServices,
                                                                [serviceKey]:
                                                                    !expandedServices[
                                                                        serviceKey
                                                                    ]
                                                            })
                                                        }
                                                        className="w-full flex justify-between items-center py-3"
                                                    >

                                                        <h3 className="font-semibold text-slate-800 dark:text-white">

                                                            📦 {serviceName}
                                                            <span className="text-slate-400 ml-2">
                                                                ({endpointList.length})
                                                            </span>

                                                        </h3>

                                                        <span>

                                                            {expandedServices[
                                                                serviceKey
                                                            ]
                                                                ? "▼"
                                                                : "▶"}

                                                        </span>

                                                    </button>

                                                    {expandedServices[
                                                        serviceKey
                                                    ] && (
                                                        <div className="space-y-4 mt-3">
                                                            {visibleEndpoints.map((endpoint) => (

                                                                <div
                                                                    key={endpoint.id}
                                                                    className="bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl p-5"
                                                                >

                                                                    <div className="flex justify-between items-start">

                                                                        <div>

                                                                            <span
                                                                                className={`px-3 py-1 rounded-full text-xs font-semibold ${methodColor(endpoint.method)}`}
                                                                            >
                                                                                {endpoint.method}
                                                                            </span>

                                                                            <p className="mt-3 font-medium break-all text-slate-900 dark:text-white">
                                                                                {endpoint.url}
                                                                            </p>

                                                                        </div>

                                                                        <span
                                                                            className={`px-3 py-1 rounded-full text-xs font-medium ${
                                                                                endpoint.enabled
                                                                                    ? "bg-emerald-100 text-emerald-700"
                                                                                    : "bg-red-100 text-red-700"
                                                                            }`}
                                                                        >
                                                                            {endpoint.enabled
                                                                                ? "🟢 Enabled"
                                                                                : "🔴 Disabled"}
                                                                        </span>

                                                                    </div>

                                                                    <div className="mt-4 text-sm text-slate-600 dark:text-slate-400">

                                                                        Timeout:
                                                                        <span className="font-semibold ml-2">
                                                                            {endpoint.timeoutMillis} ms
                                                                        </span>

                                                                    </div>

                                                                    <div className="flex justify-end gap-3 mt-5">

                                                                        <button
                                                                            onClick={() => {
                                                                                setSelectedEndpoint(endpoint);
                                                                                setShowModal(true);
                                                                            }}
                                                                            className="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded-lg
                                                                                hover:bg-slate-100 dark:hover:bg-slate-700
                                                                                text-slate-900 dark:text-white
                                                                                transition-colors"
                                                                            >
                                                                            Edit
                                                                        </button>

                                                                        <button
                                                                            onClick={() =>
                                                                                handleDeleteEndpoint(endpoint.id)
                                                                            }
                                                                            className="px-4 py-2 border border-red-500 text-red-500 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20"
                                                                        >
                                                                            Delete
                                                                        </button>

                                                                    </div>

                                                                </div>

                                                            ))}

                                                            {endpointList.length > 5 && (

                                                                <button
                                                                    onClick={() =>
                                                                        setShowAllEndpoints({
                                                                            ...showAllEndpoints,
                                                                            [serviceKey]:
                                                                                !showAllEndpoints[
                                                                                    serviceKey
                                                                                ]
                                                                        })
                                                                    }
                                                                    className="text-blue-600 hover:underline font-medium"
                                                                >
                                                                    {showAllEndpoints[serviceKey]
                                                                        ? "View Less"
                                                                        : `View All (${endpointList.length})`}
                                                                </button>

                                                            )}

                                                        </div>
                                                    )}

                                                </div>

                                            );

                                        }
                                    )}

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